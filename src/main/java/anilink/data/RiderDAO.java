package anilink.data;

import anilink.model.Rider;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class RiderDAO {

    private Rider map(ResultSet rs) throws SQLException {
        Rider r = new Rider();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setVehicleType(rs.getString("vehicle_type"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }

    public Optional<Rider> findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM riders WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Rider> findByUserId(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM riders WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Rider rider) throws SQLException {
        String sql = "INSERT INTO riders (user_id, contact_number, vehicle_type) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rider.getUserId());
            ps.setString(2, rider.getContactNumber());
            ps.setString(3, rider.getVehicleType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert rider");
    }

    public boolean update(int id, String contactNumber, String vehicleType) throws SQLException {
        String sql = "UPDATE riders SET contact_number = ?, vehicle_type = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ps.setString(2, vehicleType);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public java.util.List<Rider> findAllWithNames() throws SQLException {
        String sql = "SELECT r.*, u.name AS user_name FROM riders r "
                + "JOIN users u ON r.user_id = u.id "
                + "WHERE u.is_active = 1 "
                + "ORDER BY u.name";
        java.util.List<Rider> list = new java.util.ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Rider r = map(rs);
                r.setUserName(rs.getString("user_name"));
                list.add(r);
            }
        }
        return list;
    }
}
