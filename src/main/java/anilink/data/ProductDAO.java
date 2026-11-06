package anilink.data;

import anilink.model.Product;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    private static final String BASE_SELECT =
            "SELECT p.*, c.category_name, u.name AS farmer_name, f.farm_address "
            + "FROM products p "
            + "JOIN farmers f ON p.farmer_id = f.id "
            + "JOIN users u ON f.user_id = u.id "
            + "LEFT JOIN categories c ON p.category_id = c.id ";

    private Product map(ResultSet rs) throws SQLException {
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
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        try {
            p.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {
        }
        try {
            p.setFarmerName(rs.getString("farmer_name"));
        } catch (SQLException ignored) {
        }
        try {
            p.setFarmAddress(rs.getString("farm_address"));
        } catch (SQLException ignored) {
        }
        return p;
    }

    public List<Product> search(String search, Integer categoryId, String availability,
                                Double minPrice, Double maxPrice) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE p.status = 'available' AND u.is_active = 1 ");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (p.product_name LIKE ? OR c.category_name LIKE ?) ");
            String like = "%" + search.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if ("in_stock".equals(availability)) {
            sql.append("AND p.quantity > 0 ");
        } else if ("out_of_stock".equals(availability)) {
            sql.append("AND p.quantity = 0 ");
        }
        if (minPrice != null) {
            sql.append("AND p.price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND p.price <= ? ");
            params.add(maxPrice);
        }
        sql.append("ORDER BY p.created_at DESC");
        return execute(sql.toString(), params);
    }

    public List<Product> latestAvailable(int limit) throws SQLException {
        String sql = BASE_SELECT
                + "WHERE p.status = 'available' AND p.quantity > 0 AND u.is_active = 1 "
                + "ORDER BY p.created_at DESC LIMIT ?";
        List<Object> params = new ArrayList<>();
        params.add(limit);
        return execute(sql, params);
    }

    public Optional<Product> findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        List<Object> params = new ArrayList<>();
        params.add(id);
        List<Product> list = execute(sql, params);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Product> findByFarmer(int farmerId) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.farmer_id = ? ORDER BY p.created_at DESC";
        List<Object> params = new ArrayList<>();
        params.add(farmerId);
        return execute(sql, params);
    }

    public List<Product> findAll(String search, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND p.product_name LIKE ? ");
            params.add("%" + search.trim() + "%");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        sql.append("ORDER BY p.created_at DESC");
        return execute(sql.toString(), params);
    }

    private List<Product> execute(String sql, List<Object> params) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else if (p instanceof Double) {
                    ps.setDouble(i + 1, (Double) p);
                } else {
                    ps.setString(i + 1, String.valueOf(p));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public int insert(Product p) throws SQLException {
        String sql = "INSERT INTO products (farmer_id, category_id, product_name, description, price, unit, quantity, image, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getFarmerId());
            if (p.getCategoryId() == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, p.getCategoryId());
            }
            ps.setString(3, p.getProductName());
            ps.setString(4, p.getDescription());
            ps.setDouble(5, p.getPrice());
            ps.setString(6, p.getUnit());
            ps.setInt(7, p.getQuantity());
            ps.setString(8, p.getImage());
            ps.setString(9, p.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert product");
    }

    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, product_name = ?, description = ?, "
                + "price = ?, unit = ?, quantity = ?, image = ?, status = ? "
                + "WHERE id = ? AND farmer_id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (p.getCategoryId() == null) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, p.getCategoryId());
            }
            ps.setString(2, p.getProductName());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setString(5, p.getUnit());
            ps.setInt(6, p.getQuantity());
            ps.setString(7, p.getImage());
            ps.setString(8, p.getStatus());
            ps.setInt(9, p.getId());
            ps.setInt(10, p.getFarmerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateByAdmin(int productId, String status) throws SQLException {
        String sql = "UPDATE products SET status = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateQuantity(int productId, int quantity) throws SQLException {
        String status = quantity <= 0 ? "out_of_stock" : "available";
        String sql = "UPDATE products SET quantity = ?, status = CASE WHEN status = 'inactive' THEN 'inactive' ELSE ? END WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, status);
            ps.setInt(3, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int productId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean decrementStock(Connection c, int productId, int amount) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ?, "
                + "status = CASE WHEN quantity - ? <= 0 THEN 'out_of_stock' ELSE status END "
                + "WHERE id = ? AND quantity >= ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setInt(2, amount);
            ps.setInt(3, productId);
            ps.setInt(4, amount);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
