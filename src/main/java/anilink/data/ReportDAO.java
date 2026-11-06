package anilink.data;

import anilink.model.Report;
import anilink.model.ReportTarget;
import anilink.model.User;
import anilink.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ReportDAO {

    private static final String AUTO_DISABLE_REASON
            = "Automatically disabled after reaching the active-reports threshold.";

    /**
     * Every farmer behind a line item on this order, plus the assigned rider
     * if one exists - the choices offered to a buyer reporting someone on
     * this order. A self-contained lookup (not reusing
     * {@link OrderDAO#findByBuyer(int)}'s {@code Order.riderId}/{@code riderName},
     * which that query never joins) so the buyer's own order list stays
     * untouched by this feature.
     */
    public List<ReportTarget> targetsForOrder(int orderId) throws SQLException {
        List<ReportTarget> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT DISTINCT f.id AS farmer_id, u.name AS farmer_name "
                     + "FROM order_items oi "
                     + "JOIN products p ON p.id = oi.product_id "
                     + "JOIN farmers f ON f.id = p.farmer_id "
                     + "JOIN users u ON u.id = f.user_id "
                     + "WHERE oi.order_id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ReportTarget(Report.TARGET_FARMER,
                            rs.getInt("farmer_id"), rs.getString("farmer_name")));
                }
            }
        }
        out.addAll(riderTargetForOrder(orderId));
        return out;
    }

    /** The order's assigned rider, or empty when none is assigned yet. */
    public List<ReportTarget> riderTargetForOrder(int orderId) throws SQLException {
        List<ReportTarget> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT r.id AS rider_id, u.name AS rider_name "
                     + "FROM orders o "
                     + "JOIN riders r ON r.id = o.rider_id "
                     + "JOIN users u ON u.id = r.user_id "
                     + "WHERE o.id = ? AND o.rider_id IS NOT NULL")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.add(new ReportTarget(Report.TARGET_RIDER,
                            rs.getInt("rider_id"), rs.getString("rider_name")));
                }
            }
        }
        return out;
    }

    /** The single buyer on this order - the only choice offered to a farmer or rider reporting someone. */
    public List<ReportTarget> buyerTargetForOrder(int orderId) throws SQLException {
        List<ReportTarget> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT b.id AS buyer_id, u.name AS buyer_name "
                     + "FROM orders o "
                     + "JOIN buyers b ON b.id = o.buyer_id "
                     + "JOIN users u ON u.id = b.user_id "
                     + "WHERE o.id = ?")) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    out.add(new ReportTarget(Report.TARGET_BUYER,
                            rs.getInt("buyer_id"), rs.getString("buyer_name")));
                }
            }
        }
        return out;
    }

    public int insert(Report r) throws SQLException {
        String sql = "INSERT INTO reports (order_id, reporter_id, reporter_type, target_type, "
                + "target_id, reason, details, proof_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int id;
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getOrderId());
            ps.setInt(2, r.getReporterId());
            ps.setString(3, r.getReporterType());
            ps.setString(4, r.getTargetType());
            ps.setInt(5, r.getTargetId());
            ps.setString(6, r.getReason());
            ps.setString(7, r.getDetails());
            ps.setString(8, r.getProofImage());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to save report.");
                }
                id = keys.getInt(1);
            }
        }
        autoDisableIfThresholdReached(r.getTargetType(), r.getTargetId());
        return id;
    }

    /** Reports still counting against a target (open or reviewed - not yet resolved). */
    public int countActiveReports(String targetType, int targetId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT COUNT(*) FROM reports WHERE target_type = ? AND target_id = ? "
                     + "AND status IN ('open', 'reviewed')")) {
            ps.setString(1, targetType);
            ps.setInt(2, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Disables the target's account once it reaches {@link Report#AUTO_DISABLE_THRESHOLD}. */
    private void autoDisableIfThresholdReached(String targetType, int targetId) throws SQLException {
        if (countActiveReports(targetType, targetId) < Report.AUTO_DISABLE_THRESHOLD) {
            return;
        }
        Integer userId = resolveTargetUserId(targetType, targetId);
        if (userId != null) {
            new UserDAO().setActive(userId, false, AUTO_DISABLE_REASON);
        }
    }

    private Integer resolveTargetUserId(String targetType, int targetId) throws SQLException {
        String table = Report.TARGET_RIDER.equals(targetType) ? "riders"
                : Report.TARGET_BUYER.equals(targetType) ? "buyers" : "farmers";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT user_id FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, targetId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : null;
            }
        }
    }

    /** The live account behind a report's target, for the admin to act on. */
    public Optional<User> resolveTargetUser(String targetType, int targetId) throws SQLException {
        Integer userId = resolveTargetUserId(targetType, targetId);
        return userId == null ? Optional.empty() : new UserDAO().findById(userId);
    }

    /** Orders this reporter (a buyer or a farmer) has already filed at least one report against. */
    public Set<Integer> findReportedOrderIds(int reporterId, String reporterType) throws SQLException {
        Set<Integer> out = new HashSet<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT DISTINCT order_id FROM reports WHERE reporter_id = ? AND reporter_type = ?")) {
            ps.setInt(1, reporterId);
            ps.setString(2, reporterType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getInt("order_id"));
                }
            }
        }
        return out;
    }

    private Report map(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId(rs.getInt("id"));
        r.setOrderId(rs.getInt("order_id"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setReporterType(rs.getString("reporter_type"));
        r.setTargetType(rs.getString("target_type"));
        r.setTargetId(rs.getInt("target_id"));
        r.setReason(rs.getString("reason"));
        r.setDetails(rs.getString("details"));
        r.setProofImage(rs.getString("proof_image"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setTargetName(rs.getString("target_name"));
        return r;
    }

    /**
     * The admin review queue, optionally filtered by status
     * ("open"/"reviewed"/"resolved") and/or by the reported farmer's name
     * ({@code farmerSearch} - matches only {@code target_type = 'farmer'}
     * rows, since this search is specifically for finding a farmer's account).
     */
    public List<Report> findAll(String statusFilter, String farmerSearch) throws SQLException {
        String sql = "SELECT rp.*, "
                + "CASE WHEN rp.reporter_type = 'farmer' "
                + "  THEN (SELECT u.name FROM farmers f JOIN users u ON u.id = f.user_id WHERE f.id = rp.reporter_id) "
                + "  WHEN rp.reporter_type = 'rider' "
                + "  THEN (SELECT u.name FROM riders r JOIN users u ON u.id = r.user_id WHERE r.id = rp.reporter_id) "
                + "  ELSE (SELECT u.name FROM buyers b JOIN users u ON u.id = b.user_id WHERE b.id = rp.reporter_id) "
                + "END AS reporter_name, "
                + "CASE WHEN rp.target_type = 'rider' "
                + "  THEN (SELECT u.name FROM riders r JOIN users u ON u.id = r.user_id WHERE r.id = rp.target_id) "
                + "  WHEN rp.target_type = 'buyer' "
                + "  THEN (SELECT u.name FROM buyers b JOIN users u ON u.id = b.user_id WHERE b.id = rp.target_id) "
                + "  ELSE (SELECT u.name FROM farmers f JOIN users u ON u.id = f.user_id WHERE f.id = rp.target_id) "
                + "END AS target_name "
                + "FROM reports rp ";
        String f = statusFilter == null ? "" : statusFilter.trim().toLowerCase();
        boolean filtered = Report.STATUS_OPEN.equals(f)
                || Report.STATUS_REVIEWED.equals(f)
                || Report.STATUS_RESOLVED.equals(f);
        if (filtered) {
            sql += "WHERE rp.status = ? ";
        }
        String search = farmerSearch == null ? "" : farmerSearch.trim();
        boolean searching = !search.isEmpty();
        if (searching) {
            sql += "HAVING target_type = 'farmer' AND target_name LIKE ? ";
        }
        sql += "ORDER BY rp.created_at DESC";

        List<Report> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int idx = 1;
            if (filtered) {
                ps.setString(idx++, f);
            }
            if (searching) {
                ps.setString(idx++, "%" + search + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        }
        return out;
    }

    public boolean updateStatus(int reportId, String status) throws SQLException {
        String s = status == null ? "" : status.trim().toLowerCase();
        if (!Report.STATUS_OPEN.equals(s) && !Report.STATUS_REVIEWED.equals(s)
                && !Report.STATUS_RESOLVED.equals(s)) {
            throw new SQLException("Invalid report status.");
        }
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE reports SET status = ? WHERE id = ?")) {
            ps.setString(1, s);
            ps.setInt(2, reportId);
            return ps.executeUpdate() > 0;
        }
    }

    public static String reasonLabel(String reason) {
        if (reason == null) {
            return "-";
        }
        switch (reason) {
            case Report.REASON_DAMAGED:
                return "Received damaged";
            case Report.REASON_INCORRECT:
                return "Received incorrect";
            case Report.REASON_NOT_RECEIVED:
                return "Did not receive";
            case Report.REASON_NOT_PAID:
                return "Did not pay";
            case Report.REASON_NOT_PICKED_UP:
                return "Did not pick up order";
            case Report.REASON_ABUSIVE:
                return "Abusive behavior";
            case Report.REASON_ITEM_RECEIVED:
                return "Buyer received the item";
            case Report.REASON_ITEM_NOT_RECEIVED:
                return "Buyer did not receive the item";
            case Report.REASON_OTHERS:
                return "Others";
            default:
                return reason;
        }
    }

    public static String reasonCode(String label) {
        if (label == null) {
            return Report.REASON_OTHERS;
        }
        switch (label) {
            case "Received damaged":
                return Report.REASON_DAMAGED;
            case "Received incorrect":
                return Report.REASON_INCORRECT;
            case "Did not receive":
                return Report.REASON_NOT_RECEIVED;
            case "Did not pay":
                return Report.REASON_NOT_PAID;
            case "Did not pick up order":
                return Report.REASON_NOT_PICKED_UP;
            case "Abusive behavior":
                return Report.REASON_ABUSIVE;
            case "Buyer received the item":
                return Report.REASON_ITEM_RECEIVED;
            case "Buyer did not receive the item":
                return Report.REASON_ITEM_NOT_RECEIVED;
            default:
                return Report.REASON_OTHERS;
        }
    }

    public static String statusLabel(String status) {
        if (status == null) {
            return "OPEN";
        }
        return status.toUpperCase();
    }
}
