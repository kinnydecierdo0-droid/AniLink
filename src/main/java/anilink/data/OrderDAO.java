package anilink.data;

import anilink.model.Order;
import anilink.model.OrderItem;
import anilink.model.Product;
import anilink.session.CartItem;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDAO {

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setBuyerId(rs.getInt("buyer_id"));
        o.setTotalAmount(rs.getDouble("total_amount"));
        try {
            o.setDeliveryFee(rs.getDouble("delivery_fee"));
        } catch (SQLException ignored) {
            // Column missing: migration_delivery_fee.sql has not been run yet.
        }
        o.setDeliveryMethod(rs.getString("delivery_method"));
        try {
            o.setVehicleType(rs.getString("vehicle_type"));
        } catch (SQLException ignored) {
            // Column missing: migration_vehicle_type.sql has not been run yet.
        }
        o.setDeliveryAddress(rs.getString("delivery_address"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        try {
            o.setBuyerName(rs.getString("buyer_name"));
        } catch (SQLException ignored) {
        }
        try {
            o.setBuyerContact(rs.getString("buyer_contact"));
        } catch (SQLException ignored) {
        }
        try {
            o.setItemsSummary(rs.getString("items_summary"));
        } catch (SQLException ignored) {
        }
        try {
            int rid = rs.getInt("rider_id");
            o.setRiderId(rs.wasNull() ? null : rid);
        } catch (SQLException ignored) {
        }
        try {
            o.setRiderName(rs.getString("rider_name"));
        } catch (SQLException ignored) {
        }
        return o;
    }

    /** Flat charge applied to delivery orders. Pickup orders are never charged. */
    public static final double DELIVERY_FEE = 500.00;

    /** Backwards-compatible overload: places an order with no delivery fee. */
    public int placeOrder(int buyerId, List<CartItem> cart, String deliveryMethod,
                          String deliveryAddress, String paymentMethod) throws SQLException {
        return placeOrder(buyerId, cart, deliveryMethod, deliveryAddress, paymentMethod, 0);
    }

    /**
     * Places an order and its items in one transaction.
     *
     * <p>{@code total_amount} stores the grand total (items + delivery fee)
     * because every existing dashboard and report sums that column;
     * {@code delivery_fee} keeps the fee so the breakdown can be shown later.
     * The fee is forced to zero for pickup orders regardless of what the
     * caller passes.
     */
    public int placeOrder(int buyerId, List<CartItem> cart, String deliveryMethod,
                          String deliveryAddress, String paymentMethod,
                          double deliveryFee) throws SQLException {
        return placeOrder(buyerId, cart, deliveryMethod, deliveryAddress, paymentMethod,
                deliveryFee, null);
    }

    /**
     * Same as the six-argument {@link #placeOrder}, plus the delivery
     * vehicle chosen (a {@link anilink.model.VehicleType#name()}), stored
     * alongside the fee. {@code vehicleType} is ignored (stored as
     * {@code null}) for pickup orders, same as {@code deliveryFee}.
     */
    public int placeOrder(int buyerId, List<CartItem> cart, String deliveryMethod,
                          String deliveryAddress, String paymentMethod,
                          double deliveryFee, String vehicleType) throws SQLException {
        double fee = "delivery".equals(deliveryMethod) ? Math.max(0, deliveryFee) : 0;
        String vehicle = "delivery".equals(deliveryMethod) ? vehicleType : null;
        Connection c = DBConnection.get();
        boolean oldAuto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            double total = 0;
            List<Product> freshProducts = new ArrayList<>();
            String checkSql = "SELECT product_name, price, quantity, status FROM products WHERE id = ?";
            try (PreparedStatement ps = c.prepareStatement(checkSql)) {
                for (CartItem item : cart) {
                    Product stale = item.getProduct();
                    ps.setInt(1, stale.getId());
                    Product fresh = null;
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            fresh = new Product();
                            fresh.setId(stale.getId());
                            fresh.setProductName(rs.getString("product_name"));
                            fresh.setPrice(rs.getDouble("price"));
                            fresh.setQuantity(rs.getInt("quantity"));
                            fresh.setStatus(rs.getString("status"));
                        }
                    }
                    if (fresh == null) {
                        throw new SQLException("Product no longer exists: " + stale.getProductName());
                    }
                    if ("inactive".equals(fresh.getStatus())) {
                        throw new SQLException("Product is no longer available: " + fresh.getProductName());
                    }
                    if (fresh.getQuantity() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for " + fresh.getProductName()
                                + " (only " + fresh.getQuantity() + " left)");
                    }
                    total += fresh.getPrice() * item.getQuantity();
                    freshProducts.add(fresh);
                }
            }

            int orderId;
            String sqlWithVehicle = "INSERT INTO orders (buyer_id, total_amount, delivery_fee, delivery_method, vehicle_type, delivery_address, payment_method, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')";
            try {
                try (PreparedStatement ps = c.prepareStatement(sqlWithVehicle, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, buyerId);
                    ps.setDouble(2, total + fee);
                    ps.setDouble(3, fee);
                    ps.setString(4, deliveryMethod);
                    ps.setString(5, vehicle);
                    ps.setString(6, deliveryAddress);
                    ps.setString(7, paymentMethod);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to create order");
                        }
                        orderId = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                // vehicle_type column missing: migration_vehicle_type.sql has
                // not been run yet. Fall back to the pre-migration column
                // list so placing an order never breaks on this alone.
                String sqlNoVehicle = "INSERT INTO orders (buyer_id, total_amount, delivery_fee, delivery_method, delivery_address, payment_method, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 'pending')";
                try (PreparedStatement ps = c.prepareStatement(sqlNoVehicle, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, buyerId);
                    ps.setDouble(2, total + fee);
                    ps.setDouble(3, fee);
                    ps.setString(4, deliveryMethod);
                    ps.setString(5, deliveryAddress);
                    ps.setString(6, paymentMethod);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to create order");
                        }
                        orderId = keys.getInt(1);
                    }
                }
            }

            String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(itemSql)) {
                for (int i = 0; i < cart.size(); i++) {
                    CartItem item = cart.get(i);
                    Product fresh = freshProducts.get(i);
                    ps.setInt(1, orderId);
                    ps.setInt(2, fresh.getId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, fresh.getPrice());
                    ps.setDouble(5, fresh.getPrice() * item.getQuantity());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            ProductDAO productDAO = new ProductDAO();
            for (int i = 0; i < cart.size(); i++) {
                CartItem item = cart.get(i);
                if (!productDAO.decrementStock(c, item.getProduct().getId(), item.getQuantity())) {
                    throw new SQLException("Insufficient stock for " + freshProducts.get(i).getProductName());
                }
            }

            if (deliveryAddress != null && !deliveryAddress.isEmpty()) {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE buyers SET address = ? WHERE id = ?")) {
                    ps.setString(1, deliveryAddress);
                    ps.setInt(2, buyerId);
                    ps.executeUpdate();
                }
            }

            c.commit();
            return orderId;
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException ignored) {
            }
            throw e;
        } finally {
            try {
                c.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    public List<Order> findByBuyer(int buyerId) throws SQLException {
        String sql = "SELECT o.*, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id) AS items_summary "
                + "FROM orders o WHERE o.buyer_id = ? ORDER BY o.created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Optional<Order> findById(int id) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "WHERE o.id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = map(rs);
                    order.getItems().addAll(findItems(id));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public List<OrderItem> findItems(int orderId) throws SQLException {
        String sql = "SELECT oi.*, p.product_name, p.unit, p.image "
                + "FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + "WHERE oi.order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setImage(rs.getString("image"));
                    item.setStatus(rs.getString("status"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public List<OrderItem> findItems(int orderId, int farmerId) throws SQLException {
        String sql = "SELECT oi.*, p.product_name, p.unit, p.image "
                + "FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + "WHERE oi.order_id = ? AND p.farmer_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    item.setSubtotal(rs.getDouble("subtotal"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setImage(rs.getString("image"));
                    item.setStatus(rs.getString("status"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public List<Order> findForFarmer(int farmerId) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "ru.name AS rider_name, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi "
                + " JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "WHERE EXISTS (SELECT 1 FROM order_items oi "
                + " JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) "
                + "ORDER BY o.created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setInt(2, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Order> findAll(String statusFilter) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "ru.name AS rider_name, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "WHERE (? IS NULL OR ? = '' OR o.status = ?) "
                + "ORDER BY o.created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /**
     * Orders where the buyer chose delivery, optionally narrowed by status -
     * and only once there is nothing left for the admin to review first.
     * Powers the Manage Deliveries page - unlike {@link #findAll}, this
     * always excludes pickup orders regardless of status.
     *
     * <p>A G-Cash order ({@code payment_method = 'Online Payment'}) stays
     * hidden here until BOTH of its split payments are {@code verified}:
     * the product subtotal (farmer, {@code payments}) and the delivery fee
     * (admin, {@code delivery_payments}) - that is the "both approved" gate.
     * Cash on Delivery has nothing to review ahead of time (the cash is
     * collected on arrival), so those orders are never held back by this gate.
     *
     * <p>Once an order reaches {@code completed} it has nothing left for
     * admin to manage here either, so it drops off this queue regardless of
     * the status filter - it is already visible in Transaction History.
     */
    public List<Order> findDeliveries(String statusFilter) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "ru.name AS rider_name, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "LEFT JOIN payments py ON py.order_id = o.id "
                + "LEFT JOIN delivery_payments dp ON dp.order_id = o.id "
                + "WHERE o.delivery_method = 'delivery' "
                + "AND o.status <> 'completed' "
                + "AND (o.payment_method <> 'Online Payment' "
                + "     OR (py.status = 'verified' AND dp.status = 'verified')) "
                + "AND (? IS NULL OR ? = '' OR o.status = ?) "
                + "ORDER BY o.created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Order> findByRider(int riderId, String statusFilter) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "ru.name AS rider_name, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "WHERE o.rider_id = ? AND (? IS NULL OR ? = '' OR o.status = ?) "
                + "ORDER BY o.created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, riderId);
            ps.setString(2, statusFilter);
            ps.setString(3, statusFilter);
            ps.setString(4, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public boolean assignRider(int orderId, Integer riderId) throws SQLException {
        String sql = "UPDATE orders SET rider_id = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (riderId == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, riderId);
            }
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countByRider(int riderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE rider_id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, riderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByRiderAndStatus(int riderId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE rider_id = ? AND status = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, riderId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public double totalValueForRider(int riderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders "
                + "WHERE rider_id = ? AND status = 'completed'";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, riderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    public boolean updateStatus(int orderId, String status) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateItemStatus(int orderItemId, String status) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE order_items SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, orderItemId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean farmerHasOrderItem(int orderId, int farmerId) throws SQLException {
        String sql = "SELECT 1 FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + "WHERE oi.order_id = ? AND p.farmer_id = ? LIMIT 1";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countByBuyerAndStatus(int buyerId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE buyer_id = ? AND status = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Every order containing at least one of this farmer's products, any status. */
    public int countAllForFarmer(int farmerId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT o.id) FROM orders o "
                + "JOIN order_items oi ON oi.order_id = o.id "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE p.farmer_id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * This farmer's completed sales grouped by calendar day, for the
     * dashboard sales-trend chart. Mirrors {@link #deliveriesPerDay} but sums
     * revenue (this farmer's line items only) instead of counting orders.
     * Each row is {@code {java.sql.Date day, Double total}}.
     */
    public List<Object[]> salesPerDayForFarmer(int farmerId, java.util.Date from, java.util.Date to)
            throws SQLException {
        String sql = "SELECT DATE(o.updated_at) AS d, COALESCE(SUM(oi.subtotal), 0) AS total "
                + "FROM order_items oi "
                + "JOIN products p ON p.id = oi.product_id "
                + "JOIN orders o ON o.id = oi.order_id "
                + "WHERE p.farmer_id = ? AND o.status = 'completed' AND o.updated_at BETWEEN ? AND ? "
                + "GROUP BY d ORDER BY d";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setTimestamp(2, new java.sql.Timestamp(from.getTime()));
            ps.setTimestamp(3, new java.sql.Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getDate("d"), rs.getDouble("total")});
                }
            }
        }
        return rows;
    }

    /**
     * This farmer's completed sales grouped by hour for one calendar day.
     * Each row is {@code {Integer hour (0-23), Double total}}.
     */
    public List<Object[]> salesPerHourForFarmer(int farmerId, java.util.Date day) throws SQLException {
        String sql = "SELECT HOUR(o.updated_at) AS h, COALESCE(SUM(oi.subtotal), 0) AS total "
                + "FROM order_items oi "
                + "JOIN products p ON p.id = oi.product_id "
                + "JOIN orders o ON o.id = oi.order_id "
                + "WHERE p.farmer_id = ? AND o.status = 'completed' AND DATE(o.updated_at) = DATE(?) "
                + "GROUP BY h ORDER BY h";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setTimestamp(2, new java.sql.Timestamp(day.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getInt("h"), rs.getDouble("total")});
                }
            }
        }
        return rows;
    }

    public int countByFarmerAndStatus(int farmerId, String status) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT o.id) FROM orders o "
                + "JOIN order_items oi ON oi.order_id = o.id "
                + "JOIN products p ON p.id = oi.product_id "
                + "WHERE p.farmer_id = ? AND o.status = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countAll() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM orders");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double totalSalesAll() throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'completed'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public double totalSalesForFarmer(int farmerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(oi.subtotal), 0) FROM order_items oi "
                + "JOIN products p ON p.id = oi.product_id "
                + "JOIN orders o ON o.id = oi.order_id "
                + "WHERE p.farmer_id = ? AND o.status = 'completed'";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    public double totalSalesForFarmerSince(int farmerId, java.sql.Timestamp since) throws SQLException {
        String sql = "SELECT COALESCE(SUM(oi.subtotal), 0) FROM order_items oi "
                + "JOIN products p ON p.id = oi.product_id "
                + "JOIN orders o ON o.id = oi.order_id "
                + "WHERE p.farmer_id = ? AND o.status = 'completed' AND o.updated_at >= ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setTimestamp(2, since);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    public List<Order> recentForFarmer(int farmerId, int limit) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "WHERE EXISTS (SELECT 1 FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) "
                + "ORDER BY o.created_at DESC LIMIT ?";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setInt(2, farmerId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Order> recentAll(int limit) throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "ORDER BY o.created_at DESC LIMIT ?";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public List<Object[]> salesByCategory() throws SQLException {
        String sql = "SELECT COALESCE(c.category_name, 'Uncategorized') AS category_name, "
                + "COALESCE(SUM(oi.subtotal), 0) AS total, COUNT(DISTINCT oi.id) AS items "
                + "FROM order_items oi "
                + "JOIN orders o ON o.id = oi.order_id "
                + "JOIN products p ON p.id = oi.product_id "
                + "LEFT JOIN categories c ON c.id = p.category_id "
                + "WHERE o.status = 'completed' "
                + "GROUP BY COALESCE(c.category_name, 'Uncategorized') "
                + "ORDER BY total DESC";
        List<Object[]> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{rs.getString(1), rs.getDouble(2), rs.getInt(3)});
            }
        }
        return list;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ------------------------------------------------- delivery reporting

    /** Orders whose delivery method is Delivery, regardless of status. */
    public int countDeliveries() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE delivery_method = 'delivery'";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Delivery counts grouped by calendar day, for the dashboard chart.
     * Each row is {@code {java.sql.Date day, Integer count}}; days with no
     * deliveries are simply absent, so the caller fills the gaps.
     */
    public List<Object[]> deliveriesPerDay(java.util.Date from, java.util.Date to)
            throws SQLException {
        String sql = "SELECT DATE(created_at) AS d, COUNT(*) AS n FROM orders "
                + "WHERE delivery_method = 'delivery' AND created_at BETWEEN ? AND ? "
                + "GROUP BY d ORDER BY d";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, new java.sql.Timestamp(from.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(to.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getDate("d"), rs.getInt("n")});
                }
            }
        }
        return rows;
    }

    /**
     * Delivery counts grouped by hour for one calendar day, so the chart can
     * show the rate across a specific date.
     * Each row is {@code {Integer hour (0-23), Integer count}}.
     */
    public List<Object[]> deliveriesPerHour(java.util.Date day) throws SQLException {
        String sql = "SELECT HOUR(created_at) AS h, COUNT(*) AS n FROM orders "
                + "WHERE delivery_method = 'delivery' AND DATE(created_at) = DATE(?) "
                + "GROUP BY h ORDER BY h";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, new java.sql.Timestamp(day.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getInt("h"), rs.getInt("n")});
                }
            }
        }
        return rows;
    }

    /**
     * Completed delivery orders for the admin sales report: the finished
     * transactions that actually earned a delivery fee. Totals are summed by
     * the caller from these rows so the figures can never disagree.
     */
    public List<Order> findCompletedDeliveries() throws SQLException {
        String sql = "SELECT o.*, bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "ru.name AS rider_name, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id) AS items_summary "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "WHERE o.status = 'completed' AND o.delivery_method = 'delivery' "
                + "ORDER BY o.created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }
}
