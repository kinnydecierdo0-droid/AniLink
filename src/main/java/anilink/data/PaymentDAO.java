package anilink.data;

import anilink.model.Payment;
import anilink.model.PaymentSettings;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaymentDAO {

    private static final String ORDER_SELECT
            = "SELECT o.id AS order_id, o.buyer_id, o.total_amount, o.status AS order_status, "
            + "o.created_at AS order_created_at, o.payment_method AS checkout_method, "
            + "o.delivery_method, o.delivery_fee, o.rider_id, ru.name AS rider_name, "
            + "bu.name AS buyer_name, b.contact_number AS buyer_contact, "
            + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
            + " FROM order_items oi JOIN products p ON oi.product_id = p.id "
            + " WHERE oi.order_id = o.id) AS items_summary, "
            + "py.id AS payment_id, py.payment_method, py.reference_no, py.proof_image, "
            + "py.status, py.submitted_at, py.reviewed_at "
            + "FROM orders o "
            + "JOIN buyers b ON o.buyer_id = b.id "
            + "JOIN users bu ON b.user_id = bu.id "
            + "LEFT JOIN riders r ON o.rider_id = r.id "
            + "LEFT JOIN users ru ON r.user_id = ru.id "
            + "LEFT JOIN payments py ON py.order_id = o.id ";

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setOrderId(rs.getInt("order_id"));
        p.setBuyerId(rs.getInt("buyer_id"));
        p.setOrderTotal(rs.getDouble("total_amount"));
        p.setOrderStatus(rs.getString("order_status"));
        p.setOrderCreatedAt(rs.getTimestamp("order_created_at"));
        p.setBuyerName(rs.getString("buyer_name"));
        p.setBuyerContact(rs.getString("buyer_contact"));
        p.setItemsSummary(rs.getString("items_summary"));
        p.setDeliveryMethod(rs.getString("delivery_method"));
        p.setDeliveryFee(rs.getDouble("delivery_fee"));
        int rid = rs.getInt("rider_id");
        p.setRiderId(rs.wasNull() ? null : rid);
        p.setRiderName(rs.getString("rider_name"));
        int paymentId = rs.getInt("payment_id");
        if (rs.wasNull()) {
            p.setId(0);
            p.setStatus(null);
            p.setPaymentMethod(rs.getString("checkout_method"));
            return p;
        }
        p.setId(paymentId);
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setReferenceNo(rs.getString("reference_no"));
        p.setProofImage(rs.getString("proof_image"));
        p.setStatus(rs.getString("status"));
        p.setSubmittedAt(rs.getTimestamp("submitted_at"));
        p.setReviewedAt(rs.getTimestamp("reviewed_at"));
        return p;
    }

    public Map<Integer, Payment> findByBuyer(int buyerId) throws SQLException {
        String sql = ORDER_SELECT + "WHERE o.buyer_id = ? ORDER BY o.created_at DESC";
        Map<Integer, Payment> out = new LinkedHashMap<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Payment p = map(rs);
                    out.put(p.getOrderId(), p);
                }
            }
        }
        return out;
    }

    public Optional<Payment> findByOrder(int orderId) throws SQLException {
        String sql = ORDER_SELECT + "WHERE o.id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Payment> findAll(String statusFilter) throws SQLException {
        String sql = ORDER_SELECT;
        String f = statusFilter == null ? "" : statusFilter.trim().toLowerCase();
        if ("awaiting".equals(f)) {
            sql += "WHERE py.id IS NULL ";
        } else if (Payment.STATUS_PENDING.equals(f)
                || Payment.STATUS_VERIFIED.equals(f)
                || Payment.STATUS_REJECTED.equals(f)) {
            sql += "WHERE py.status = ? ";
        }
        sql += "ORDER BY o.created_at DESC";
        List<Payment> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (!f.isEmpty() && !"awaiting".equals(f)) {
                ps.setString(1, f);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    public boolean existsForOrder(int orderId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT 1 FROM payments WHERE order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Creates the payment record for an order and syncs orders.payment_method.
     * Returns the new payment id, or 0 when a payment already exists.
     * Throws SQLException on validation failure (caller shows getMessage()).
     */
    public int create(int buyerId, int orderId, String method, String referenceNo,
                      String proofImage) throws SQLException {
        if (!Payment.METHOD_COD.equals(method) && !Payment.METHOD_ONLINE.equals(method)) {
            throw new SQLException("Invalid payment method.");
        }
        String ref = referenceNo == null ? "" : referenceNo.trim();
        if (Payment.METHOD_ONLINE.equals(method) && ref.isEmpty()) {
            throw new SQLException("Please enter your payment reference number.");
        }
        Connection c = DBConnection.get();
        boolean oldAuto = c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            int owner;
            String orderStatus;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT buyer_id, status FROM orders WHERE id = ? FOR UPDATE")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Order not found.");
                    }
                    owner = rs.getInt("buyer_id");
                    orderStatus = rs.getString("status");
                }
            }
            if (owner != buyerId) {
                throw new SQLException("You can only pay for your own orders.");
            }
            if ("cancelled".equals(orderStatus)) {
                throw new SQLException("Cancelled orders cannot be paid.");
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM payments WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        c.rollback();
                        return 0;
                    }
                }
            }

            int paymentId;
            String insert = "INSERT INTO payments (order_id, payment_method, reference_no, proof_image, status) "
                    + "VALUES (?, ?, ?, ?, 'pending')";
            try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orderId);
                ps.setString(2, method);
                ps.setString(3, ref.isEmpty() ? null : ref);
                ps.setString(4, proofImage == null || proofImage.isEmpty() ? null : proofImage);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to record payment.");
                    }
                    paymentId = keys.getInt(1);
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE orders SET payment_method = ? WHERE id = ?")) {
                ps.setString(1, method);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }
            c.commit();
            return paymentId;
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException ignored) {
            }
            if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                return 0;
            }
            throw e;
        } finally {
            try {
                c.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean updateStatus(int orderId, String status) throws SQLException {
        String s = status == null ? "" : status.trim().toLowerCase();
        if (!Payment.STATUS_PENDING.equals(s)
                && !Payment.STATUS_VERIFIED.equals(s)
                && !Payment.STATUS_REJECTED.equals(s)) {
            throw new SQLException("Invalid payment status.");
        }
        String sql = "UPDATE payments SET status = ?, "
                + "reviewed_at = CASE WHEN ? = 'pending' THEN NULL ELSE CURRENT_TIMESTAMP END "
                + "WHERE order_id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s);
            ps.setString(2, s);
            ps.setInt(3, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    public PaymentSettings findSettings() {
        PaymentSettings out = new PaymentSettings();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, provider, account_name, account_number, instructions "
                     + "FROM payment_settings ORDER BY id LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                out.setId(rs.getInt("id"));
                out.setProvider(nullSafe(rs.getString("provider"), "GCash"));
                out.setAccountName(nullSafe(rs.getString("account_name"), ""));
                out.setAccountNumber(nullSafe(rs.getString("account_number"), ""));
                out.setInstructions(nullSafe(rs.getString("instructions"), ""));
            }
        } catch (SQLException e) {
            System.err.println("[payments] settings load failed: " + e.getMessage());
        }
        return out;
    }

    public boolean saveSettings(String provider, String accountName,
                                String accountNumber, String instructions) throws SQLException {
        String prov = provider == null ? "" : provider.trim();
        if (prov.isEmpty()) {
            throw new SQLException("Payment provider/name is required.");
        }
        int id = findSettings().getId();
        String sql = "UPDATE payment_settings SET provider = ?, account_name = ?, "
                + "account_number = ?, instructions = ? WHERE id = ?";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prov);
            ps.setString(2, trim(accountName));
            ps.setString(3, trim(accountNumber));
            ps.setString(4, trim(instructions));
            ps.setInt(5, id);
            if (ps.executeUpdate() > 0) {
                return true;
            }
        }
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO payment_settings (provider, account_name, account_number, instructions) "
                     + "VALUES (?, ?, ?, ?)")) {
            ps.setString(1, prov);
            ps.setString(2, trim(accountName));
            ps.setString(3, trim(accountNumber));
            ps.setString(4, trim(instructions));
            return ps.executeUpdate() > 0;
        }
    }

    public static String statusLabel(String status) {
        if (status == null || status.isEmpty()) {
            return "AWAITING PAYMENT";
        }
        switch (status.toLowerCase()) {
            case Payment.STATUS_PENDING:
                return "PENDING";
            case Payment.STATUS_VERIFIED:
                return "VERIFIED";
            case Payment.STATUS_REJECTED:
                return "REJECTED";
            default:
                return status.toUpperCase();
        }
    }

    public static boolean isOnline(Payment p) {
        return p != null && Payment.METHOD_ONLINE.equals(p.getPaymentMethod());
    }

    private static String trim(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String nullSafe(String v, String fallback) {
        return v == null ? fallback : v;
    }

    /**
     * The admin's delivery-payment queue: G-Cash payments on orders the buyer
     * chose to have delivered. Pickup orders and cash-on-delivery never appear.
     *
     * <p>Once the order reaches {@code completed} ("Delivered") it has
     * nothing left for admin to do here, so it drops off this queue - it is
     * already visible in Transaction History.
     *
     * <p>{@code statusFilter} keeps the same semantics as
     * {@link #findAll(String)}: blank for all, "awaiting" for orders with no
     * payment row yet, or one of pending/verified/rejected.
     */
    public List<Payment> findDeliveryGcash(String statusFilter) throws SQLException {
        String f = statusFilter == null ? "" : statusFilter.trim().toLowerCase();
        String sql = ORDER_SELECT
                + "WHERE o.delivery_method = 'delivery' AND o.payment_method = ? "
                + "AND o.status <> 'completed' ";
        if ("awaiting".equals(f)) {
            sql += "AND py.id IS NULL ";
        } else if (Payment.STATUS_PENDING.equals(f)
                || Payment.STATUS_VERIFIED.equals(f)
                || Payment.STATUS_REJECTED.equals(f)) {
            sql += "AND py.status = ? ";
        }
        sql += "ORDER BY o.created_at DESC";

        List<Payment> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Payment.METHOD_ONLINE);
            if (!f.isEmpty() && !"awaiting".equals(f)) {
                ps.setString(2, f);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    /**
     * A farmer's delivery-payment queue, but for pickup instead of delivery:
     * G-Cash payments on pickup orders containing at least one of their
     * products. Cash-on-pickup orders are verified by the farmer directly in
     * Manage Order (the "Paid" button there), so they never appear here.
     *
     * <p>{@code items_summary} is scoped to this farmer's own line items,
     * same as {@link anilink.data.OrderDAO#findForFarmer(int)} - a pickup
     * order can in principle span more than one farmer's products.
     *
     * <p>Unlike {@link #findDeliveryGcash}, this drops a payment the moment
     * it is verified rather than waiting for the order to complete: the
     * farmer's only job here is the approval itself, and once that is done
     * the order moves on to Manage Order where the rest of the workflow
     * lives, so leaving it here would just be a stale duplicate. A rejected
     * payment still shows, since nothing has resolved it yet.
     */
    public List<Payment> findPickupGcashForFarmer(int farmerId, String statusFilter) throws SQLException {
        String f = statusFilter == null ? "" : statusFilter.trim().toLowerCase();
        String sql = "SELECT o.id AS order_id, o.buyer_id, o.total_amount, o.status AS order_status, "
                + "o.created_at AS order_created_at, o.payment_method AS checkout_method, "
                + "o.delivery_method, o.delivery_fee, o.rider_id, ru.name AS rider_name, "
                + "bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON oi.product_id = p.id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) AS items_summary, "
                + "py.id AS payment_id, py.payment_method, py.reference_no, py.proof_image, "
                + "py.status, py.submitted_at, py.reviewed_at "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "LEFT JOIN payments py ON py.order_id = o.id "
                + "WHERE o.delivery_method = 'pickup' AND o.payment_method = ? "
                + "AND EXISTS (SELECT 1 FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) "
                + "AND (py.status IS NULL OR py.status <> 'verified') ";
        if ("awaiting".equals(f)) {
            sql += "AND py.id IS NULL ";
        } else if (Payment.STATUS_PENDING.equals(f)
                || Payment.STATUS_VERIFIED.equals(f)
                || Payment.STATUS_REJECTED.equals(f)) {
            sql += "AND py.status = ? ";
        }
        sql += "ORDER BY o.created_at DESC";

        List<Payment> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setString(2, Payment.METHOD_ONLINE);
            ps.setInt(3, farmerId);
            if (!f.isEmpty() && !"awaiting".equals(f)) {
                ps.setString(4, f);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    /**
     * A farmer's product-payment queue for DELIVERY orders - the sibling of
     * {@link #findPickupGcashForFarmer}, scoped to delivery instead of
     * pickup. Part of the delivery-order payment split: the farmer reviews
     * this (the product subtotal, still the same {@code payments} table
     * every pickup order already uses), while admin separately reviews the
     * delivery-fee portion via {@link DeliveryPaymentDAO}.
     *
     * <p>Same drop-when-verified behavior as {@link #findPickupGcashForFarmer}
     * - once the farmer approves it, it moves on to Manage Order.
     */
    public List<Payment> findDeliveryProductGcashForFarmer(int farmerId, String statusFilter) throws SQLException {
        String f = statusFilter == null ? "" : statusFilter.trim().toLowerCase();
        String sql = "SELECT o.id AS order_id, o.buyer_id, o.total_amount, o.status AS order_status, "
                + "o.created_at AS order_created_at, o.payment_method AS checkout_method, "
                + "o.delivery_method, o.delivery_fee, o.rider_id, ru.name AS rider_name, "
                + "bu.name AS buyer_name, b.contact_number AS buyer_contact, "
                + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
                + " FROM order_items oi JOIN products p ON oi.product_id = p.id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) AS items_summary, "
                + "py.id AS payment_id, py.payment_method, py.reference_no, py.proof_image, "
                + "py.status, py.submitted_at, py.reviewed_at "
                + "FROM orders o "
                + "JOIN buyers b ON o.buyer_id = b.id "
                + "JOIN users bu ON b.user_id = bu.id "
                + "LEFT JOIN riders r ON o.rider_id = r.id "
                + "LEFT JOIN users ru ON r.user_id = ru.id "
                + "LEFT JOIN payments py ON py.order_id = o.id "
                + "WHERE o.delivery_method = 'delivery' AND o.payment_method = ? "
                + "AND EXISTS (SELECT 1 FROM order_items oi JOIN products p ON p.id = oi.product_id "
                + " WHERE oi.order_id = o.id AND p.farmer_id = ?) "
                + "AND (py.status IS NULL OR py.status <> 'verified') ";
        if ("awaiting".equals(f)) {
            sql += "AND py.id IS NULL ";
        } else if (Payment.STATUS_PENDING.equals(f)
                || Payment.STATUS_VERIFIED.equals(f)
                || Payment.STATUS_REJECTED.equals(f)) {
            sql += "AND py.status = ? ";
        }
        sql += "ORDER BY o.created_at DESC";

        List<Payment> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setString(2, Payment.METHOD_ONLINE);
            ps.setInt(3, farmerId);
            if (!f.isEmpty() && !"awaiting".equals(f)) {
                ps.setString(4, f);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }
}
