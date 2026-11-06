package anilink.data;

import anilink.model.Warning;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class WarningDAO {

    private Warning map(ResultSet rs) throws SQLException {
        Warning w = new Warning();
        w.setId(rs.getInt("id"));
        w.setUserId(rs.getInt("user_id"));
        w.setReason(rs.getString("reason"));
        w.setCreatedAt(rs.getTimestamp("created_at"));
        w.setAcknowledgedAt(rs.getTimestamp("acknowledged_at"));
        return w;
    }

    public int insert(int userId, String reason) throws SQLException {
        String sql = "INSERT INTO warnings (user_id, reason) VALUES (?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, reason);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to record warning.");
    }

    public List<Warning> findUnacknowledgedByUser(int userId) throws SQLException {
        List<Warning> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM warnings WHERE user_id = ? AND acknowledged_at IS NULL "
                     + "ORDER BY created_at ASC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    public boolean acknowledge(int warningId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE warnings SET acknowledged_at = CURRENT_TIMESTAMP WHERE id = ?")) {
            ps.setInt(1, warningId);
            return ps.executeUpdate() > 0;
        }
    }
}
