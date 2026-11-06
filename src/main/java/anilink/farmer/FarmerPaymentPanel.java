package anilink.farmer;

import anilink.admin.PaymentDetailsDialog;
import anilink.data.PaymentDAO;
import anilink.model.Payment;
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
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * The farmer's G-Cash payment queue: pickup orders' combined payment, plus
 * (new) a delivery order's product-subtotal payment - the delivery-fee
 * portion of a delivery order is reviewed separately by admin, see
 * {@link anilink.admin.PaymentManagementPanel}/{@link anilink.data.DeliveryPaymentDAO}.
 *
 * <p>A pickup order paid by G-Cash stays off Manage Order until the farmer
 * approves it here; a delivery order stays off Manage Order until BOTH this
 * approval and admin's delivery-fee approval are done - see
 * {@link anilink.farmer.FarmerOrdersPanel}'s gate in {@code refreshData()}.
 * Cash-on-pickup orders never appear here; they are paid and verified
 * directly in Manage Order's "Paid" step once the buyer arrives, since that
 * cash is collected in person.
 *
 * <p>No rider assignment here - that is Manage Deliveries' job once both
 * payments for a delivery order are approved.
 */
public class FarmerPaymentPanel extends javax.swing.JPanel {

    private static final int COL_VIEW = 7;

    private final MainFrame frame;
    private final List<Payment> payments = new ArrayList<>();

    private final JComboBox<String> cmbFilter = new JComboBox<>();
    private TablePager pager;
    private JLabel lblCount;
    private boolean initializing = true;

    public FarmerPaymentPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Manage Payment");
        setupTable();
        buildFilterBar();
        pager = TablePager.install(tblPayments, true);
        installActionColumn();
        initializing = false;
    }

    private void setupTable() {
        tblPayments.setModel(new DefaultTableModel(new Object[0][0], new Object[]{
            "Order", "Method", "Buyer", "Items", "Date", "Amount", "Payment Status", "View"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == COL_VIEW;
            }
        });
        tblPayments.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblPayments.getTableHeader().setReorderingAllowed(false);
        UiUtil.styleTable(tblPayments);
        tblPayments.setRowHeight(34);
        int[] widths = {80, 90, 150, 220, 150, 110, 150, 170};
        for (int i = 0; i < widths.length; i++) {
            tblPayments.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void installActionColumn() {
        ButtonColumn.install(tblPayments, COL_VIEW,
                ButtonColumn.simple("View Details", UiUtil.PRIMARY),
                row -> openDetails(paymentAt(row)));
    }

    private void buildFilterBar() {
        pnlFilter.setLayout(new BorderLayout());
        pnlFilter.setOpaque(false);

        JPanel card = UiUtil.card(8, 12, 8, 12);
        card.setLayout(new BorderLayout(10, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel caption = new JLabel("Payment status");
        UiUtil.styleFieldLabel(caption);
        left.add(caption);

        cmbFilter.setFont(UiUtil.plain(12));
        cmbFilter.setPreferredSize(new Dimension(150, 32));
        for (String option : new String[]{"All", "awaiting", "pending", "rejected"}) {
            cmbFilter.addItem(option);
        }
        cmbFilter.addActionListener(e -> {
            if (!initializing) {
                refreshData();
            }
        });
        left.add(cmbFilter);

        JButton btnRefresh = new JButton("Refresh");
        UiUtil.styleTintedButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(110, 32));
        btnRefresh.addActionListener(e -> refreshData());
        left.add(btnRefresh);

        card.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel hint = new JLabel("G-Cash pickup payments, and delivery orders' product payments, wait here until approved.");
        UiUtil.styleNote(hint);
        right.add(hint);
        lblCount = new JLabel();
        UiUtil.styleNote(lblCount);
        right.add(lblCount);
        card.add(right, BorderLayout.EAST);

        pnlFilter.add(card, BorderLayout.CENTER);
    }

    private Payment paymentAt(int modelRow) {
        if (pager == null) {
            return null;
        }
        int sourceIndex = pager.sourceIndexOf(modelRow);
        if (sourceIndex < 0 || sourceIndex >= payments.size()) {
            return null;
        }
        return payments.get(sourceIndex);
    }

    private void openDetails(Payment payment) {
        if (payment == null) {
            return;
        }
        PaymentDetailsDialog dialog = new PaymentDetailsDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), payment);
        dialog.setVisible(true);
        if (dialog.isChanged()) {
            refreshData();
        }
    }

    public void refreshData() {
        if (pager == null) {
            return;
        }
        Object filter = cmbFilter.getSelectedItem();
        String statusFilter = filter == null || "All".equals(filter)
                ? null : String.valueOf(filter);

        pager.clear();
        payments.clear();
        try {
            int farmerId = SessionContext.getFarmerId();
            if (farmerId > 0) {
                PaymentDAO paymentDAO = new PaymentDAO();
                List<Payment> combined = new ArrayList<>(
                        paymentDAO.findPickupGcashForFarmer(farmerId, statusFilter));
                combined.addAll(paymentDAO.findDeliveryProductGcashForFarmer(farmerId, statusFilter));
                combined.sort((a, b) -> {
                    java.util.Date da = a.getOrderCreatedAt();
                    java.util.Date db = b.getOrderCreatedAt();
                    if (da == null || db == null) {
                        return 0;
                    }
                    return db.compareTo(da);
                });
                for (Payment p : combined) {
                    payments.add(p);
                    boolean hasPayment = p.getId() > 0;
                    boolean isDelivery = "delivery".equals(p.getDeliveryMethod());
                    // For a delivery order this queue only covers the product
                    // subtotal (the fee is admin's queue), so show that
                    // portion specifically rather than the order's grand
                    // total, which would otherwise look like the farmer is
                    // also responsible for approving the delivery fee.
                    double productAmount = isDelivery
                            ? p.getOrderTotal() - p.getDeliveryFee() : p.getOrderTotal();
                    pager.addRow(new Object[]{
                        "#" + p.getOrderId(),
                        isDelivery ? "Delivery" : "Pickup",
                        p.getBuyerName() == null ? "-" : p.getBuyerName(),
                        p.getItemsSummary() == null ? "" : p.getItemsSummary(),
                        UiUtil.formatDateTime(p.getOrderCreatedAt()),
                        UiUtil.formatPrice(productAmount),
                        PaymentDAO.statusLabel(hasPayment ? p.getStatus() : null),
                        ""
                    }, p.getOrderCreatedAt());
                }
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        pager.apply();
        lblCount.setText(payments.size() + " G-Cash payment"
                + (payments.size() == 1 ? "" : "s"));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        scrollPayments = new javax.swing.JScrollPane();
        tblPayments = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Manage Payment");

        // Laid out at runtime in buildFilterBar().
        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 52, Short.MAX_VALUE)
        );

        tblPayments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        tblPayments.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollPayments.setViewportView(tblPayments);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(scrollPayments, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollPayments, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JScrollPane scrollPayments;
    private javax.swing.JTable tblPayments;
    // End of variables declaration//GEN-END:variables
}
