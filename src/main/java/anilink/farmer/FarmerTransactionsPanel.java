package anilink.farmer;

import anilink.buyer.ReportDialog;
import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.data.ReportDAO;
import anilink.model.Order;
import anilink.model.Payment;
import anilink.model.Report;
import anilink.model.ReportTarget;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.ButtonColumn;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
 * Read-only receipt trail of this farmer's finished orders: every order that
 * reached {@code completed} (pickup or delivery), with its payment record -
 * the same role {@link anilink.buyer.BuyerTransactionsPanel} plays for
 * buyers and {@link anilink.admin.AdminOrdersPanel} plays for admin.
 * Completed orders also carry a Report button here, letting the farmer
 * report the buyer (e.g. abusive behavior, not paying) once the order is
 * done - the same role {@link anilink.buyer.BuyerTransactionsPanel}'s Report
 * column plays for a buyer reporting a farmer.
 *
 * <p>Distinct from {@link anilink.farmer.SalesPanel} ("Sales"), which is the
 * revenue-summary dashboard; this is the per-order ledger.
 *
 * <p>Hand-coded rather than a NetBeans form so the designer cannot regenerate
 * it; Manage Order stays the place for active orders.
 */
public class FarmerTransactionsPanel extends JPanel {

    private static final int COL_REPORT = 8;

    private final MainFrame frame;
    /** Rows currently loaded, in the same order as the table - for mapping a
     * model row back to its order on a Report click. */
    private final List<Order> visible = new ArrayList<>();
    /** Orders this farmer has already reported, so the Report button shows
     * "Reported" (disabled) instead of letting a second report be filed. */
    private Set<Integer> reportedOrderIds = Collections.emptySet();
    private final JTable tblTransactions = new JTable();
    private final JLabel lblCount = new JLabel("0 transaction(s)");
    private final JLabel lblSalesValue = new JLabel(UiUtil.formatPrice(0));
    private final JLabel lblEmpty = new JLabel(
            "No finished transactions yet. Completed orders appear here.",
            SwingConstants.CENTER);
    private final JScrollPane scrollTable = new JScrollPane(tblTransactions);
    private TablePager pager;

    public FarmerTransactionsPanel(MainFrame frame) {
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

        JLabel subtitle = new JLabel("Your completed orders, pickup and delivery");
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
            "Order #", "Date", "Buyer", "Delivery", "Items", "Total", "Payment", "Payment Status", "Report"}) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == COL_REPORT;
            }
        });
        tblTransactions.setDefaultEditor(Object.class, null);
        tblTransactions.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        UiUtil.styleTable(tblTransactions);
        pager = TablePager.install(tblTransactions, true);

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

        JPanel sales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sales.setOpaque(false);
        JLabel caption = new JLabel("Total from completed orders:");
        caption.setFont(UiUtil.bold(13));
        caption.setForeground(UiUtil.MUTED);
        sales.add(caption);
        lblSalesValue.setFont(UiUtil.bold(16));
        lblSalesValue.setForeground(UiUtil.PRIMARY);
        sales.add(lblSalesValue);
        row.add(sales, BorderLayout.EAST);
        return row;
    }

    private void sizeColumns() {
        int[] widths = {70, 140, 130, 80, 220, 110, 130, 120, 100};
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

    /** Only a completed order that hasn't been reported yet. */
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
            List<ReportTarget> targets = new ReportDAO().buyerTargetForOrder(order.getId());
            if (targets.isEmpty()) {
                UiUtil.info(this, "No reportable party found for this order.");
                return;
            }
            ReportDialog dialog = ReportDialog.forFarmer(frame, order, targets);
            dialog.setVisible(true);
            if (dialog.isSubmitted()) {
                refreshData();
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    public void refreshData() {
        pager.clear();
        visible.clear();
        double sales = 0;
        int rows = 0;
        try {
            int farmerId = SessionContext.getFarmerId();
            if (farmerId > 0) {
                PaymentDAO paymentDAO = new PaymentDAO();
                reportedOrderIds = new ReportDAO().findReportedOrderIds(farmerId, Report.REPORTER_FARMER);
                int rowNumber = 1;
                for (Order order : new OrderDAO().findForFarmer(farmerId)) {
                    if (!"completed".equals(order.getStatus())) {
                        continue;
                    }
                    Optional<Payment> payment = paymentDAO.findByOrder(order.getId());
                    boolean hasPayment = payment.isPresent() && payment.get().getId() > 0;
                    visible.add(order);
                    pager.addRow(new Object[]{
                        "#" + rowNumber++,
                        UiUtil.formatDateTime(order.getCreatedAt()),
                        order.getBuyerName() == null ? "-" : order.getBuyerName(),
                        "delivery".equals(order.getDeliveryMethod()) ? "Delivery" : "Pickup",
                        order.getItemsSummary() == null ? "" : order.getItemsSummary(),
                        UiUtil.formatPrice(order.getTotalAmount()),
                        UiUtil.paymentLabel(hasPayment
                                ? payment.get().getPaymentMethod() : order.getPaymentMethod()),
                        PaymentDAO.statusLabel(hasPayment ? payment.get().getStatus() : null),
                        ""
                    }, order.getCreatedAt());
                    sales += order.getTotalAmount();
                    rows++;
                }
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load transactions: " + e.getMessage());
        }
        pager.apply();
        lblCount.setText(rows + " transaction(s)");
        lblSalesValue.setText(UiUtil.formatPrice(sales));
        lblEmpty.setVisible(rows == 0);
        revalidate();
        repaint();
    }
}
