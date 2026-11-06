package anilink.admin;

import anilink.data.PaymentDAO;
import anilink.model.Payment;
import anilink.model.PaymentSettings;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * The admin's delivery-payment queue.
 *
 * <p>Only the delivery-fee portion of a delivery order's G-Cash payment
 * appears here - pickup orders, cash-on-delivery, and the product-subtotal
 * portion (now the farmer's job, see {@link anilink.farmer.FarmerPaymentPanel})
 * are handled elsewhere. "View Details" and "Assign Delivery Man" sit in the
 * toolbar next to the date filter and act on whichever row is selected: the
 * former opens {@link DeliveryPaymentDetailsDialog} to check the reference
 * number and receipt, the latter opens {@link AssignRiderDialog} to pick a
 * delivery man - staying disabled until the selected payment has been
 * approved.
 *
 * <p>The G-Cash account section is kept because the buyer's payment modal
 * reads these values through {@link PaymentDAO#findSettings()}.
 */
public class PaymentManagementPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private final List<Payment> payments = new ArrayList<>();
    /** Each displayed payment's row-rank number (1 for the first row), keyed
     * by order id - so {@link #openAssign}'s warning message can reference
     * the same number the row shows. */
    private java.util.Map<Integer, Integer> orderNumbers = new java.util.HashMap<>();

    private final JComboBox<String> cmbFilter = new JComboBox<>();
    private final JButton btnView = new JButton("View Details");
    private final JButton btnAssign = new JButton("Assign Delivery Man");
    private TablePager pager;
    private JLabel lblCount;
    private boolean initializing = true;

    public PaymentManagementPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Manage Delivery Payment");
        setupTable();
        buildFilterBar();
        pager = TablePager.install(tblPayments, true);
        wireActions();
        initializing = false;
    }


    private void setupTable() {
        tblPayments.setModel(new DefaultTableModel(new Object[0][0], new Object[]{
            "Order", "Buyer", "Items", "Date", "Amount", "Delivery Fee",
            "Payment Status", "Delivery Man"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblPayments.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblPayments.getTableHeader().setReorderingAllowed(false);
        UiUtil.styleTable(tblPayments);
        tblPayments.setRowHeight(34);
        int[] widths = {80, 150, 220, 150, 110, 110, 140, 150};
        for (int i = 0; i < widths.length; i++) {
            tblPayments.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /**
     * View/Assign act on whichever row is selected, instead of a button per
     * row - so their state is recomputed whenever the selection changes (or
     * the table reloads, which clears it).
     */
    private void wireActions() {
        JPanel trailing = pager.dateFilterTrailing();
        if (trailing != null) {
            UiUtil.styleTintedButton(btnView);
            btnView.setPreferredSize(new Dimension(120, 32));
            trailing.add(btnView);

            UiUtil.stylePrimary(btnAssign);
            btnAssign.setPreferredSize(new Dimension(160, 32));
            trailing.add(btnAssign);
        }
        btnView.addActionListener(e -> {
            Payment p = selectedPayment();
            if (p == null) {
                UiUtil.info(this, "Please select a payment first.");
                return;
            }
            openDetails(p);
        });
        btnAssign.addActionListener(e -> {
            Payment p = selectedPayment();
            if (p == null) {
                UiUtil.info(this, "Please select a payment first.");
                return;
            }
            openAssign(p);
        });
        tblPayments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtons();
            }
        });
        updateActionButtons();
    }

    /** Assigning only makes sense once the money is confirmed, so the button
     * stays greyed out until the selected payment is approved. */
    private void updateActionButtons() {
        Payment p = selectedPayment();
        boolean verified = p != null && p.isVerified();
        btnAssign.setText(p != null && p.getRiderId() != null ? "Reassign" : "Assign Delivery Man");
        btnAssign.setEnabled(verified);
        btnAssign.setToolTipText(verified ? null
                : "Select a payment and approve it first before assigning a delivery man.");
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
        for (String option : new String[]{"All", "awaiting", "pending", "verified", "rejected"}) {
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
        JLabel hint = new JLabel("G-Cash account details are in Profile → G-Cash Payment.");
        UiUtil.styleNote(hint);
        right.add(hint);
        lblCount = new JLabel();
        UiUtil.styleNote(lblCount);
        right.add(lblCount);

        card.add(right, BorderLayout.EAST);

        pnlFilter.add(card, BorderLayout.CENTER);
    }

    /** Maps a row on the current page back to its payment record. */
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

    private Payment selectedPayment() {
        return paymentAt(tblPayments.getSelectedRow());
    }

    private void openDetails(Payment payment) {
        if (payment == null) {
            return;
        }
        DeliveryPaymentDetailsDialog dialog = new DeliveryPaymentDetailsDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), payment);
        dialog.setVisible(true);
        if (dialog.isChanged()) {
            refreshData();
        }
    }

    private void openAssign(Payment payment) {
        if (payment == null) {
            return;
        }
        if (!payment.isVerified()) {
            int number = orderNumbers.getOrDefault(payment.getOrderId(), payment.getOrderId());
            UiUtil.info(this, "Approve the payment for order #" + number
                    + " before assigning a delivery man.");
            return;
        }
        AssignRiderDialog dialog = new AssignRiderDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this), payment);
        dialog.setVisible(true);
        if (dialog.isAssigned()) {
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
        orderNumbers = new java.util.HashMap<>();
        try {
            int rowNumber = 1;
            for (Payment p : new anilink.data.DeliveryPaymentDAO().findAll(statusFilter)) {
                payments.add(p);
                boolean hasPayment = p.getId() > 0;
                int number = rowNumber++;
                orderNumbers.put(p.getOrderId(), number);
                pager.addRow(new Object[]{
                    "#" + number,
                    p.getBuyerName() == null ? "-" : p.getBuyerName(),
                    p.getItemsSummary() == null ? "" : p.getItemsSummary(),
                    UiUtil.formatDateTime(p.getOrderCreatedAt()),
                    UiUtil.formatPrice(p.getOrderTotal()),
                    UiUtil.formatPrice(p.getDeliveryFee()),
                    PaymentDAO.statusLabel(hasPayment ? p.getStatus() : null),
                    p.getRiderName() == null || p.getRiderName().isEmpty()
                            ? "Unassigned" : p.getRiderName()
                }, p.getOrderCreatedAt());
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        pager.apply();
        lblCount.setText(payments.size() + " G-Cash delivery payment"
                + (payments.size() == 1 ? "" : "s"));
        updateActionButtons();
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
        lblTitle.setText("Manage Delivery Payment");

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
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollPayments, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollPayments, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JScrollPane scrollPayments;
    private javax.swing.JTable tblPayments;
    // End of variables declaration//GEN-END:variables
}
