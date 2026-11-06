package anilink.data;

import anilink.model.Review;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public List<Review> findByProduct(int productId) throws SQLException {
        String sql = "SELECT r.*, u.name AS buyer_name FROM reviews r "
                + "JOIN buyers b ON r.buyer_id = b.id "
                + "JOIN users u ON b.user_id = u.id "
                + "WHERE r.product_id = ? ORDER BY r.created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setBuyerId(rs.getInt("buyer_id"));
                    r.setProductId(rs.getInt("product_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setReview(rs.getString("review"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setBuyerName(rs.getString("buyer_name"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    public boolean hasReviewed(int buyerId, int productId) throws SQLException {
        String sql = "SELECT 1 FROM reviews WHERE buyer_id = ? AND product_id = ? LIMIT 1";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasReviewedOrderItem(int buyerId, int orderId, int productId) throws SQLException {
        String sql = "SELECT 1 FROM reviews r "
                + "JOIN order_items oi ON oi.product_id = r.product_id "
                + "WHERE r.buyer_id = ? AND oi.order_id = ? AND r.product_id = ? LIMIT 1";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, orderId);
            ps.setInt(3, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insert(int buyerId, int productId, int rating, String review) throws SQLException {
        String sql = "INSERT INTO reviews (buyer_id, product_id, rating, review) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, productId);
            ps.setInt(3, rating);
            ps.setString(4, review);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert review");
    }
}
