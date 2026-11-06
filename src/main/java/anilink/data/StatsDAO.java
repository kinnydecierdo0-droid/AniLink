package anilink.data;

import anilink.model.Product;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StatsDAO {

    public int countFarmers() throws SQLException {
        return countRole("farmer");
    }

    public int countBuyers() throws SQLException {
        return countRole("buyer");
    }

    private int countRole(String role) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role = ?")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countProducts() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countOrders() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM orders");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double totalSales() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE status='completed'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public List<Product> recentProducts(int limit) throws SQLException {
        String sql = "SELECT p.*, c.category_name, u.name AS farmer_name, f.farm_address "
                + "FROM products p "
                + "JOIN farmers f ON p.farmer_id = f.id "
                + "JOIN users u ON f.user_id = u.id "
                + "LEFT JOIN categories c ON p.category_id = c.id "
                + "ORDER BY p.created_at DESC LIMIT ?";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setFarmerId(rs.getInt("farmer_id"));
                    int catId = rs.getInt("category_id");
                    p.setCategoryId(rs.wasNull() ? null : catId);
                    p.setProductName(rs.getString("product_name"));
                    p.setPrice(rs.getDouble("price"));
                    p.setUnit(rs.getString("unit"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setStatus(rs.getString("status"));
                    p.setCategoryName(rs.getString("category_name"));
                    p.setFarmerName(rs.getString("farmer_name"));
                    p.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public List<Product> lowStock(int farmerId, int threshold) throws SQLException {
        String sql = "SELECT p.*, c.category_name, u.name AS farmer_name, f.farm_address "
                + "FROM products p "
                + "JOIN farmers f ON p.farmer_id = f.id "
                + "JOIN users u ON f.user_id = u.id "
                + "LEFT JOIN categories c ON p.category_id = c.id "
                + "WHERE p.farmer_id = ? AND p.quantity BETWEEN 1 AND ? "
                + "ORDER BY p.quantity ASC";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setInt(2, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setProductName(rs.getString("product_name"));
                    p.setQuantity(rs.getInt("quantity"));
                    p.setUnit(rs.getString("unit"));
                    p.setStatus(rs.getString("status"));
                    p.setCategoryName(rs.getString("category_name"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}
