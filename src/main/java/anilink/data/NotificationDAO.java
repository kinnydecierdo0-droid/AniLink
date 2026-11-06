package anilink.data;

import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Derives the notification feed for buyers, farmers and riders from existing
 * order and payment rows. No dedicated notifications table is needed: every
 * entry is a consequence of an order status change or a payment review, both
 * of which already stamp their own {@code updated_at}.
 *
 * <p>The three audiences share one query shape and one note-building rule;
 * only the {@code WHERE} clause that picks "my orders" differs per role.
 */
public class NotificationDAO {

    private static final String BASE_SELECT =
            "SELECT o.id, o.status, o.delivery_method, o.updated_at, "
            + "p.status AS pay_status, p.reviewed_at "
            + "FROM orders o LEFT JOIN payments p ON p.order_id = o.id ";

    /** One feed entry. */
    public static final class Note {

        private final String text;
        private final Date when;
        private final int orderId;
        private final boolean unread;

        Note(String text, Date when, int orderId, boolean unread) {
            this.text = text;
            this.when = when;
            this.orderId = orderId;
            this.unread = unread;
        }

        public String getText() {
            return text;
        }

        public Date getWhen() {
            return when;
        }

        public int getOrderId() {
            return orderId;
        }

        public boolean isUnread() {
            return unread;
        }
    }

    /**
     * Most recent notifications for a buyer, newest first. Anything whose
     * underlying row changed after {@code seenAt} is reported unread; passing
     * {@code null} marks everything unread (fresh session).
     */
    public List<Note> findForBuyer(int buyerId, int limit, Date seenAt) throws SQLException {
        if (buyerId <= 0) {
            return new ArrayList<>();
        }
        String sql = BASE_SELECT + "WHERE o.buyer_id = ? ORDER BY o.updated_at DESC LIMIT ?";
        return query(sql, buyerId, limit, seenAt);
    }

    /**
     * Most recent notifications for a farmer: every order that includes at
     * least one of their products, even when the order also contains other
     * farmers' products.
     */
    public List<Note> findForFarmer(int farmerId, int limit, Date seenAt) throws SQLException {
        if (farmerId <= 0) {
            return new ArrayList<>();
        }
        // A subquery rather than a JOIN to order_items/products, so an order
        // with several of this farmer's line items is never reported twice.
        String sql = BASE_SELECT
                + "WHERE o.id IN (SELECT oi.order_id FROM order_items oi "
                + "JOIN products p ON p.id = oi.product_id WHERE p.farmer_id = ?) "
                + "ORDER BY o.updated_at DESC LIMIT ?";
        return query(sql, farmerId, limit, seenAt);
    }

    /** Most recent notifications for a rider: every order assigned to them. */
    public List<Note> findForRider(int riderId, int limit, Date seenAt) throws SQLException {
        if (riderId <= 0) {
            return new ArrayList<>();
        }
        String sql = BASE_SELECT + "WHERE o.rider_id = ? ORDER BY o.updated_at DESC LIMIT ?";
        return query(sql, riderId, limit, seenAt);
    }

    private List<Note> query(String sql, int id, int limit, Date seenAt) throws SQLException {
        List<Note> notes = new ArrayList<>();
        // DBConnection.get() hands back a shared singleton, so it is not closed here.
        Connection c = DBConnection.get();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notes.add(toNote(rs, seenAt));
                }
            }
        }
        return notes;
    }

    private Note toNote(ResultSet rs, Date seenAt) throws SQLException {
        int orderId = rs.getInt("id");
        String status = rs.getString("status");
        String delivery = rs.getString("delivery_method");
        Date updatedAt = toDate(rs.getTimestamp("updated_at"));
        String payStatus = rs.getString("pay_status");
        Date reviewedAt = toDate(rs.getTimestamp("reviewed_at"));

        // A reviewed payment is the more newsworthy event, so it wins over
        // the plain order status for the same order.
        if (reviewedAt != null && payStatus != null && !"pending".equals(payStatus)) {
            return new Note("Payment for order #" + orderId + " was " + payStatus + ".",
                    reviewedAt, orderId, isUnread(reviewedAt, seenAt));
        }
        if (payStatus == null && !"cancelled".equals(status)) {
            return new Note("Order #" + orderId + " is waiting for payment.",
                    updatedAt, orderId, isUnread(updatedAt, seenAt));
        }
        return new Note("Order #" + orderId + " is " + statusPhrase(status, delivery) + ".",
                updatedAt, orderId, isUnread(updatedAt, seenAt));
    }

    /** Unread count for the buyer bell badge. */
    public int countUnread(int buyerId, Date seenAt) throws SQLException {
        return countUnread(findForBuyer(buyerId, 20, seenAt));
    }

    /** Unread count for a feed already fetched - avoids a second query. */
    public static int countUnread(List<Note> notes) {
        int unread = 0;
        for (Note note : notes) {
            if (note.isUnread()) {
                unread++;
            }
        }
        return unread;
    }

    private static String statusPhrase(String status, String deliveryMethod) {
        boolean delivery = "delivery".equals(deliveryMethod);
        if (status == null) {
            return "updated";
        }
        switch (status) {
            case "pending":
                return "awaiting confirmation";
            case "preparing":
                return "being prepared";
            case "ready_for_pickup":
                return "ready for pickup";
            case "handed_to_rider":
                return "handed to the rider";
            case "on_the_way":
                return "on the way";
            case "completed":
                return delivery ? "delivered" : "completed";
            case "cancelled":
                return "cancelled";
            default:
                return status;
        }
    }

    private static boolean isUnread(Date changedAt, Date seenAt) {
        if (seenAt == null) {
            return true;
        }
        return changedAt != null && changedAt.after(seenAt);
    }

    private static Date toDate(Timestamp ts) {
        return ts == null ? null : new Date(ts.getTime());
    }
}
