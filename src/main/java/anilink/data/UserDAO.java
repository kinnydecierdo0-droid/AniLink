package anilink.data;

import anilink.model.User;
import anilink.util.DBConnection;
import anilink.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFirstName(rs.getString("first_name"));
        u.setMiddleName(rs.getString("middle_name"));
        u.setLastName(rs.getString("last_name"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));
        u.setDisabledReason(rs.getString("disabled_reason"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setUpdatedAt(rs.getTimestamp("updated_at"));
        return u;
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findByContact(String phoneDigits) throws SQLException {
        String sql = "SELECT u.* FROM users u "
                + "LEFT JOIN farmers f ON f.user_id = u.id "
                + "LEFT JOIN buyers b ON b.user_id = u.id "
                + "WHERE REPLACE(REPLACE(REPLACE(f.contact_number, ' ', ''), '-', ''), '+', '') = ? "
                + "OR REPLACE(REPLACE(REPLACE(b.contact_number, ' ', ''), '-', ''), '+', '') = ? "
                + "LIMIT 1";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phoneDigits);
            ps.setString(2, phoneDigits);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> authenticate(String identifier, String password) throws SQLException {
        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            return Optional.empty();
        }
        String id = identifier.trim();
        Optional<User> found;
        if (id.contains("@")) {
            found = findByEmail(id.toLowerCase());
        } else {
            found = findByContact(anilink.util.UiUtil.digitsOnly(id));
        }
        if (found.isEmpty()) {
            return Optional.empty();
        }
        User user = found.get();
        if (!PasswordUtil.verify(password, user.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public int insert(User user) throws SQLException {
        try (Connection c = DBConnection.get()) {
            return insert(c, user);
        }
    }

    /**
     * Same as {@link #insert(User)}, but reuses a caller-supplied connection
     * instead of acquiring (and closing) its own - for callers that need
     * this insert to be part of a larger transaction (e.g. {@code
     * RegisterForm}, which also inserts a {@code farmers}/{@code buyers}
     * row on the same connection before committing).
     */
    public int insert(Connection c, User user) throws SQLException {
        String sql = "INSERT INTO users (first_name, middle_name, last_name, name, email, password, role, is_active) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getMiddleName());
            ps.setString(3, user.getLastName());
            ps.setString(4, user.getName() == null || user.getName().isEmpty()
                    ? user.buildFullName() : user.getName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPassword());
            ps.setString(7, user.getRole());
            ps.setBoolean(8, user.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert user");
    }

    public boolean updateProfile(int id, String firstName, String middleName, String lastName, String email)
            throws SQLException {
        String sql = "UPDATE users SET first_name = ?, middle_name = ?, last_name = ?, name = ?, email = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String full = ((firstName == null ? "" : firstName) + " "
                    + (middleName == null || middleName.isEmpty() ? "" : middleName + " ")
                    + (lastName == null ? "" : lastName)).trim().replaceAll("\\s+", " ");
            ps.setString(1, firstName);
            ps.setString(2, middleName);
            ps.setString(3, lastName);
            ps.setString(4, full);
            ps.setString(5, email);
            ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int id, String plainPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(plainPassword));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Enables or disables an account. {@code reason} is stored as the
     * account's {@code disabled_reason} when disabling (shown to the user at
     * their next login attempt); pass {@code null} when enabling - enabling
     * always clears any previously stored reason regardless of what's
     * passed, since a currently-enabled account has nothing to explain.
     */
    public boolean setActive(int id, boolean active, String reason) throws SQLException {
        String sql = "UPDATE users SET is_active = ?, disabled_reason = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setString(2, active ? null : reason);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }

    public List<User> searchByRole(String role, String query) throws SQLException {
        return searchByRole(role, query, null);
    }

    public List<User> searchByRole(String role, String query, Boolean active) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ? "
                + "AND (? = '' OR name LIKE ? OR email LIKE ? OR CONCAT(first_name, ' ', last_name) LIKE ?) "
                + "AND (? IS NULL OR is_active = ?) "
                + "ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();
        String like = "%" + (query == null ? "" : query.trim()) + "%";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setString(2, query == null ? "" : query.trim());
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            ps.setObject(6, active);
            ps.setBoolean(7, active == null || active);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** Every account, regardless of role. */
    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public boolean verifyPassword(int userId, String plainPassword) throws SQLException {
        Optional<User> user = findById(userId);
        return user.isPresent() && PasswordUtil.verify(plainPassword, user.get().getPassword());
    }
}
