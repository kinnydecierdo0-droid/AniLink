package anilink.data;

import anilink.model.Appeal;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppealDAO {

    private Appeal map(ResultSet rs) throws SQLException {
        Appeal a = new Appeal();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setMessage(rs.getString("message"));
        a.setStatus(rs.getString("status"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setReviewedAt(rs.getTimestamp("reviewed_at"));
        return a;
    }

    /** Guards against a second pending appeal piling up for the same account. */
    public int insert(int userId, String message) throws SQLException {
        try (Connection c = DBConnection.get()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT COUNT(*) FROM appeals WHERE user_id = ? AND status = 'pending'")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("An appeal is already pending for this account.");
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO appeals (user_id, message) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, message);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to submit appeal.");
    }

    /** Every pending appeal, newest-filed last - the admin's review queue. */
    public List<Appeal> findPending() throws SQLException {
        List<Appeal> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT a.*, u.name AS user_name, u.role AS user_role "
                     + "FROM appeals a JOIN users u ON u.id = a.user_id "
                     + "WHERE a.status = 'pending' ORDER BY a.created_at ASC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appeal a = map(rs);
                    a.setUserName(rs.getString("user_name"));
                    a.setUserRole(rs.getString("user_role"));
                    out.add(a);
                }
            }
        }
        return out;
    }

    public boolean updateStatus(int appealId, String status) throws SQLException {
        String s = status == null ? "" : status.trim().toLowerCase();
        if (!Appeal.STATUS_APPROVED.equals(s) && !Appeal.STATUS_DENIED.equals(s)) {
            throw new SQLException("Invalid appeal status.");
        }
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE appeals SET status = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            ps.setString(1, s);
            ps.setInt(2, appealId);
            return ps.executeUpdate() > 0;
        }
    }

    /** The most recent appeal this user filed, if any - tells the login dialog whether one is already pending. */
    public Optional<Appeal> findLatestByUser(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM appeals WHERE user_id = ? ORDER BY created_at DESC LIMIT 1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }
}
