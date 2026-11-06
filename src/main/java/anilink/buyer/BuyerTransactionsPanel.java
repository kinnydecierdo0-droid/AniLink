package anilink.buyer;

import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.data.ReportDAO;
import anilink.model.Order;
import anilink.model.Payment;
import anilink.model.ReportTarget;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.ButtonColumn;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Read-only receipt trail of the buyer's finished orders: everything that has
 * been completed or cancelled, with its payment record. Completed orders also
 * carry a Report button here - reporting the farmer only makes sense once the
 * order has actually been delivered/completed, and this is the only page
 * where a completed order still appears (My Orders drops it once finished).
 *
 * <p>Hand-coded rather than a NetBeans form so the designer cannot regenerate
 * it; My Orders stays the place for active orders.
 */
public class BuyerTransactionsPanel extends JPanel {

    /** Order statuses that count as finished. */
    private static final List<String> FINISHED = List.of("completed", "cancelled");
    private static final int COL_REPORT = 11;

    private final MainFrame frame;
    /** Rows currently loaded, in the same order as the table - for mapping a
     * model row back to its order on a Report click. */
    private final List<Order> visible = new ArrayList<>();
    /** Orders this buyer has already reported, so the Report button shows
     * "Reported" (disabled) instead of letting a second report be filed. */
    private Set<Integer> reportedOrderIds = Collections.emptySet();
    private final JTable tblTransactions = new JTable();
    private final JLabel lblCount = new JLabel("0 transaction(s)");
    private final JLabel lblSpentValue = new JLabel(UiUtil.formatPrice(0));
    private final JLabel lblEmpty = new JLabel(
            "No finished transactions yet. Completed and cancelled orders appear here.",
            SwingConstants.CENTER);
    private final JScrollPane scrollTable = new JScrollPane(tblTransactions);
    private anilink.ui.TablePager pager;

    public BuyerTransactionsPanel(MainFrame frame) {
        this.frame = frame;
        setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.ACCENT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiUtil.ACCENT_DARK),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Transaction History");
        title.setFont(UiUtil.bold(22));
        title.setForeground(UiUtil.ON_ACCENT);
        titles.add(title);

        JLabel subtitle = new JLabel("Your completed and cancelled orders");
        subtitle.setFont(UiUtil.plain(12));
        subtitle.setForeground(new java.awt.Color(99, 76, 10));
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JButton refresh = new JButton("Refresh");
        refresh.setFont(UiUtil.bold(12));
        refresh.setForeground(UiUtil.ON_ACCENT);
        refresh.setBackground(UiUtil.ACCENT);
        refresh.setFocusPainted(false);
        refresh.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        refresh.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtil.ON_ACCENT),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        refresh.addActionListener(e -> refreshData());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        actions.add(refresh);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setBackground(UiUtil.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        body.add(buildSummaryRow(), BorderLayout.NORTH);

        tblTransactions.setModel(new DefaultTableModel(new Object[0][0], new Object[]{
            "Order #", "Date", "Items", "Delivery", "Subtotal", "Delivery Fee",
            "Total", "Payment", "Reference No", "Payment Status", "Order Status", "Report"}));
        tblTransactions.setDefaultEditor(Object.class, null);
        tblTransactions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        UiUtil.styleTable(tblTransactions);
        pager = anilink.ui.TablePager.install(tblTransactions, true);

        ButtonColumn.install(tblTransactions, COL_REPORT, ButtonColumn.of(
                row -> isReported(row) ? "Reported" : "Report",
                row -> canReport(row),
                row -> UiUtil.DANGER,
                row -> isReported(row) ? "You already reported this order."
                        : "Only completed orders can be reported."),
                this::onReport);

        scrollTable.setBorder(BorderFactory.createLineBorder(UiUtil.BORDER));
        scrollTable.getViewport().setBackground(UiUtil.WHITE);
        scrollTable.getVerticalScrollBar().setUnitIncrement(16);
        body.add(scrollTable, BorderLayout.CENTER);

        lblEmpty.setFont(UiUtil.bold(13));
        lblEmpty.setForeground(UiUtil.MUTED);
        lblEmpty.setPreferredSize(new Dimension(760, 60));
        lblEmpty.setVisible(false);
        body.add(lblEmpty, BorderLayout.SOUTH);

        sizeColumns();
        return body;
    }

    private JPanel buildSummaryRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        UiUtil.styleNote(lblCount);
        row.add(lblCount, BorderLayout.WEST);

        JPanel spent = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        spent.setOpaque(false);
        JLabel caption = new JLabel("Total spent on completed orders:");
        caption.setFont(UiUtil.bold(13));
        caption.setForeground(UiUtil.MUTED);
        spent.add(caption);
        lblSpentValue.setFont(UiUtil.bold(16));
        lblSpentValue.setForeground(UiUtil.PRIMARY);
        spent.add(lblSpentValue);
        row.add(spent, BorderLayout.EAST);
        return row;
    }

    private void sizeColumns() {
        int[] widths = {70, 140, 220, 80, 100, 100, 110, 130, 140, 130, 110, 100};
        for (int i = 0; i < widths.length && i < tblTransactions.getColumnCount(); i++) {
            tblTransactions.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private Order orderAt(int modelRow) {
        int sourceIndex = pager.sourceIndexOf(modelRow);
        return sourceIndex < 0 || sourceIndex >= visible.size() ? null : visible.get(sourceIndex);
    }

    private boolean isReported(int modelRow) {
        Order order = orderAt(modelRow);
        return order != null && reportedOrderIds.contains(order.getId());
    }

    /** Only a completed order (not cancelled) that hasn't been reported yet. */
    private boolean canReport(int modelRow) {
        Order order = orderAt(modelRow);
        return order != null && "completed".equals(order.getStatus()) && !isReported(modelRow);
    }

    private void onReport(int modelRow) {
        Order order = orderAt(modelRow);
        if (order == null) {
            UiUtil.info(this, "Please select an order first.");
            return;
        }
        try {
            List<ReportTarget> targets = new ReportDAO().targetsForOrder(order.getId());
            if (targets.isEmpty()) {
                UiUtil.info(this, "No reportable party found for this order.");
                return;
            }
            ReportDialog dialog = new ReportDialog(frame, order, targets);
            dialog.setVisible(true);
            if (dialog.isSubmitted()) {
                refreshData();
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    /** Same self-healing profile lookup the My Orders page uses. */
    private int resolveBuyerId() {
        int buyerId = SessionContext.getBuyerId();
        if (buyerId > 0 || !SessionContext.isLoggedIn()) {
            return buyerId;
        }
        anilink.model.User user = SessionContext.getCurrentUser();
        if (user == null) {
            return -1;
        }
        try {
            java.util.Optional<anilink.model.Buyer> found
                    = new anilink.data.BuyerDAO().findByUserId(user.getId());
            if (found.isPresent()) {
                SessionContext.setBuyerProfile(found.get());
                return found.get().getId();
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Could not load your buyer profile: " + e.getMessage());
        }
        return -1;
    }

    public void refreshData() {
        pager.clear();
        visible.clear();
        double spent = 0;
        int rows = 0;
        try {
            int buyerId = resolveBuyerId();
            if (buyerId > 0) {
                Map<Integer, Payment> payments = new PaymentDAO().findByBuyer(buyerId);
                List<Order> finished = new java.util.ArrayList<>();
                for (Order order : new OrderDAO().findByBuyer(buyerId)) {
                    if (order.getStatus() != null && FINISHED.contains(order.getStatus())) {
                        finished.add(order);
                    }
                }
                reportedOrderIds = new ReportDAO().findReportedOrderIds(buyerId, anilink.model.Report.REPORTER_BUYER);
                int total = finished.size();
                for (int i = 0; i < total; i++) {
                    Order order = finished.get(i);
                    int displayNumber = i + 1;
                    Payment pay = payments.get(order.getId());
                    boolean hasPayment = pay != null && pay.getId() > 0;
                    visible.add(order);
                    pager.addRow(new Object[]{
                        "#" + displayNumber,
                        UiUtil.formatDateTime(order.getCreatedAt()),
                        order.getItemsSummary() == null ? "" : order.getItemsSummary(),
                        "delivery".equals(order.getDeliveryMethod()) ? "Delivery" : "Pickup",
                        UiUtil.formatPrice(order.getSubtotal()),
                        order.getDeliveryFee() > 0
                                ? UiUtil.formatPrice(order.getDeliveryFee()) : "-",
                        UiUtil.formatPrice(order.getTotalAmount()),
                        UiUtil.paymentLabel(hasPayment
                                ? pay.getPaymentMethod() : order.getPaymentMethod()),
                        hasPayment && pay.getReferenceNo() != null
                                && !pay.getReferenceNo().isEmpty() ? pay.getReferenceNo() : "-",
                        PaymentDAO.statusLabel(hasPayment ? pay.getStatus() : null),
                        UiUtil.statusBadge(order.getStatus()),
                        ""
                    }, order.getCreatedAt());
                    rows++;
                    if ("completed".equals(order.getStatus())) {
                        spent += order.getTotalAmount();
                    }
                }
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load transactions: " + e.getMessage());
        }
        pager.apply();
        lblCount.setText(rows + " transaction(s)");
        lblSpentValue.setText(UiUtil.formatPrice(spent));
        lblEmpty.setVisible(rows == 0);
        // The table stays visible so the date filter and pager bar remain
        // reachable; the pager prints its own "No records" message.
        revalidate();
        repaint();
    }
}
