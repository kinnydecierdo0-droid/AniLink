package anilink.data;

import anilink.model.Farmer;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class FarmerDAO {

    private Farmer map(ResultSet rs) throws SQLException {
        Farmer f = new Farmer();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setContactNumber(rs.getString("contact_number"));
        f.setFarmAddress(rs.getString("farm_address"));
        f.setCreatedAt(rs.getTimestamp("created_at"));
        f.setUpdatedAt(rs.getTimestamp("updated_at"));
        return f;
    }

    public Optional<Farmer> findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM farmers WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Farmer> findByUserId(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM farmers WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Farmer farmer) throws SQLException {
        try (Connection c = DBConnection.get()) {
            return insert(c, farmer);
        }
    }

    /** Same as {@link #insert(Farmer)}, but reuses a caller-supplied
     * connection - see {@link UserDAO#insert(Connection, anilink.model.User)}. */
    public int insert(Connection c, Farmer farmer) throws SQLException {
        String sql = "INSERT INTO farmers (user_id, contact_number, farm_address) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, farmer.getUserId());
            ps.setString(2, farmer.getContactNumber());
            ps.setString(3, farmer.getFarmAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert farmer");
    }

    public boolean update(int id, String contactNumber, String farmAddress) throws SQLException {
        String sql = "UPDATE farmers SET contact_number = ?, farm_address = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ps.setString(2, farmAddress);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int productCount(int farmerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE farmer_id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
