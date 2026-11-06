package anilink.data;

import anilink.model.Payment;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Admin's review of the delivery-fee portion of a delivery order's G-Cash
 * payment - the sibling of {@link PaymentDAO} for the new, separate
 * {@code delivery_payments} table.
 *
 * <p>The {@code payments} table (via {@link PaymentDAO}) is completely
 * untouched by this split: it still holds exactly one row per order and now
 * represents the product-subtotal portion for a delivery order (farmer
 * reviews it, same as it always has for pickup orders), while this class's
 * table holds the delivery-fee portion (admin reviews it, same role as
 * before - just now scoped to a fee amount instead of the whole total).
 *
 * <p>Always G-Cash (delivery is G-Cash-only in this app), so unlike
 * {@code payments} there is no {@code payment_method} column to store -
 * {@link #map} fills it in as a constant for display consistency with the
 * shared {@link Payment} model.
 */
public class DeliveryPaymentDAO {

    private static final String ORDER_SELECT
            = "SELECT o.id AS order_id, o.buyer_id, o.total_amount, o.status AS order_status, "
            + "o.created_at AS order_created_at, "
            + "o.delivery_method, o.delivery_fee, o.rider_id, ru.name AS rider_name, "
            + "bu.name AS buyer_name, b.contact_number AS buyer_contact, "
            + "(SELECT GROUP_CONCAT(CONCAT(p.product_name, ' x', oi.quantity) SEPARATOR ', ') "
            + " FROM order_items oi JOIN products p ON oi.product_id = p.id "
            + " WHERE oi.order_id = o.id) AS items_summary, "
            + "dp.id AS payment_id, dp.reference_no, dp.proof_image, dp.amount, "
            + "dp.status, dp.submitted_at, dp.reviewed_at "
            + "FROM orders o "
            + "JOIN buyers b ON o.buyer_id = b.id "
            + "JOIN users bu ON b.user_id = bu.id "
            + "LEFT JOIN riders r ON o.rider_id = r.id "
            + "LEFT JOIN users ru ON r.user_id = ru.id "
            + "LEFT JOIN delivery_payments dp ON dp.order_id = o.id ";

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
        p.setPaymentMethod(anilink.model.Payment.METHOD_ONLINE);
        int rid = rs.getInt("rider_id");
        p.setRiderId(rs.wasNull() ? null : rid);
        p.setRiderName(rs.getString("rider_name"));
        int paymentId = rs.getInt("payment_id");
        if (rs.wasNull()) {
            p.setId(0);
            p.setStatus(null);
            return p;
        }
        p.setId(paymentId);
        p.setReferenceNo(rs.getString("reference_no"));
        p.setProofImage(rs.getString("proof_image"));
        p.setStatus(rs.getString("status"));
        p.setSubmittedAt(rs.getTimestamp("submitted_at"));
        p.setReviewedAt(rs.getTimestamp("reviewed_at"));
        return p;
    }

    /**
     * Records the delivery-fee payment for an order. Mirrors
     * {@link PaymentDAO#create}'s one-row-per-order guard (pre-check plus a
     * unique-constraint fallback), scoped to this table instead.
     */
    public int create(int orderId, String referenceNo, String proofImage, double amount) throws SQLException {
        String ref = referenceNo == null ? "" : referenceNo.trim();
        if (ref.isEmpty()) {
            throw new SQLException("Please enter your payment reference number.");
        }
        try (Connection c = DBConnection.get()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM delivery_payments WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return 0;
                    }
                }
            }
            String insert = "INSERT INTO delivery_payments (order_id, reference_no, proof_image, amount, status) "
                    + "VALUES (?, ?, ?, ?, 'pending')";
            try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, orderId);
                ps.setString(2, ref);
                ps.setString(3, proofImage == null || proofImage.isEmpty() ? null : proofImage);
                ps.setDouble(4, amount);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                return 0;
            }
        }
        throw new SQLException("Failed to record delivery payment.");
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

    public boolean isVerified(int orderId) throws SQLException {
        return findByOrder(orderId).map(Payment::isVerified).orElse(false);
    }

    /** Admin's delivery-fee review queue: every delivery order's fee payment, not yet completed. */
    public List<Payment> findAll(String statusFilter) throws SQLException {
        String f = statusFilter == null ? "" : statusFilter.trim().toLowerCase();
        String sql = ORDER_SELECT
                + "WHERE o.delivery_method = 'delivery' AND o.status <> 'completed' ";
        if ("awaiting".equals(f)) {
            sql += "AND dp.id IS NULL ";
        } else if (Payment.STATUS_PENDING.equals(f)
                || Payment.STATUS_VERIFIED.equals(f)
                || Payment.STATUS_REJECTED.equals(f)) {
            sql += "AND dp.status = ? ";
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

    public boolean updateStatus(int orderId, String status) throws SQLException {
        String s = status == null ? "" : status.trim().toLowerCase();
        if (!Payment.STATUS_PENDING.equals(s)
                && !Payment.STATUS_VERIFIED.equals(s)
                && !Payment.STATUS_REJECTED.equals(s)) {
            throw new SQLException("Invalid payment status.");
        }
        String sql = "UPDATE delivery_payments SET status = ?, "
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
}
