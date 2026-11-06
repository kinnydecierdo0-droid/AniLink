package anilink.data;

import anilink.model.Product;
import anilink.session.CartItem;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    private static final String PRODUCT_COLS =
            "p.id, p.farmer_id, p.category_id, p.product_name, p.description, "
            + "p.price, p.unit, p.quantity, p.image, p.status";

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setFarmerId(rs.getInt("farmer_id"));
        int catId = rs.getInt("category_id");
        p.setCategoryId(rs.wasNull() ? null : catId);
        p.setProductName(rs.getString("product_name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getDouble("price"));
        p.setUnit(rs.getString("unit"));
        p.setQuantity(rs.getInt("quantity"));
        p.setImage(rs.getString("image"));
        p.setStatus(rs.getString("status"));
        return p;
    }

    public List<CartItem> load(int buyerId) throws SQLException {
        String sql = "SELECT " + PRODUCT_COLS + ", c.quantity AS cart_quantity "
                + "FROM cart_items c JOIN products p ON p.id = c.product_id "
                + "WHERE c.buyer_id = ? ORDER BY c.id";
        List<CartItem> items = new ArrayList<>();
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CartItem(mapProduct(rs), rs.getInt("cart_quantity")));
                }
            }
        }
        return items;
    }

    public void upsert(int buyerId, int productId, int quantity) throws SQLException {
        String sql = "INSERT INTO cart_items (buyer_id, product_id, quantity) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    public void remove(int buyerId, int productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE buyer_id = ? AND product_id = ?";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    public void clear(int buyerId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE buyer_id = ?";
        try (Connection conn = DBConnection.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.executeUpdate();
        }
    }
}
