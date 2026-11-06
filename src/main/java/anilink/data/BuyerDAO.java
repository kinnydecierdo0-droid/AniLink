package anilink.data;

import anilink.model.Buyer;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class BuyerDAO {

    private Buyer map(ResultSet rs) throws SQLException {
        Buyer b = new Buyer();
        b.setId(rs.getInt("id"));
        b.setUserId(rs.getInt("user_id"));
        b.setContactNumber(rs.getString("contact_number"));
        b.setAddress(rs.getString("address"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        b.setUpdatedAt(rs.getTimestamp("updated_at"));
        return b;
    }

    public Optional<Buyer> findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM buyers WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Buyer> findByUserId(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM buyers WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Buyer buyer) throws SQLException {
        try (Connection c = DBConnection.get()) {
            return insert(c, buyer);
        }
    }

    /** Same as {@link #insert(Buyer)}, but reuses a caller-supplied
     * connection - see {@link UserDAO#insert(Connection, anilink.model.User)}. */
    public int insert(Connection c, Buyer buyer) throws SQLException {
        String sql = "INSERT INTO buyers (user_id, contact_number, address) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyer.getUserId());
            ps.setString(2, buyer.getContactNumber());
            ps.setString(3, buyer.getAddress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert buyer");
    }

    public boolean update(int id, String contactNumber, String address) throws SQLException {
        String sql = "UPDATE buyers SET contact_number = ?, address = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, contactNumber);
            ps.setString(2, address);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int orderCount(int buyerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE buyer_id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
