package anilink.farmer;

import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.model.Order;
import anilink.model.Payment;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.ButtonColumn;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Manage Order: the farmer's per-order status workflow, as six tabs instead
 * of one flat table with a status combo.
 *
 * <p>Pending -&gt; Preparing -&gt; (Ready to Pick-up | Hand in to Rider) -&gt;
 * Completed (pickup) / Delivered (delivery). The "Hand in to Rider" tab also
 * holds orders already picked up by the rider ({@code on_the_way}) since the
 * farmer has nothing further to do until the buyer confirms receipt - both
 * statuses read as "with the rider" from here. Delivered orders got there
 * because the buyer clicked Received on the rider's "On the Way" tab; this
 * panel only reflects that, it never writes it.
 *
 * <p>Pickup orders pay the farmer directly, so "Ready to Pick-up" also carries
 * the farmer's own payment confirmation: "Paid" records the cash received
 * (reusing the same {@code payments} row/verify flow admin uses for G-Cash),
 * and "Completed" only unlocks once that is done.
 */
public class FarmerOrdersPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private final List<Order> orders = new ArrayList<>();
    /** This farmer's pickup orders waiting for payment, by order id. */
    private final Map<Integer, Boolean> pickupPaid = new HashMap<>();

    private Tab tabPending;
    private Tab tabPreparing;
    private Tab tabHandRider;
    private Tab tabReadyPickup;
    private Tab tabCompleted;
    private Tab tabDelivered;

    public FarmerOrdersPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Manage Order");
        buildToolbar();
        buildTabs();
    }

    private void buildToolbar() {
        pnlToolbar.setLayout(new BorderLayout());
        pnlToolbar.setOpaque(false);
        JButton btnRefresh = new JButton("Refresh");
        UiUtil.styleTintedButton(btnRefresh);
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.addActionListener(e -> refreshData());
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.add(btnRefresh);
        pnlToolbar.add(right, BorderLayout.EAST);
    }

    private void buildTabs() {
        pnlTabs.setLayout(new BorderLayout());
        pnlTabs.setOpaque(false);

        JTabbedPane tabs = new JTabbedPane();

        tabPending = newTab(new String[]{"Preparing"});
        tabPreparing = newTab(new String[]{"Next Step"});
        tabHandRider = newTab(null);
        tabReadyPickup = newTab(new String[]{"Paid", "Completed"});
        tabCompleted = newTab(null);
        tabDelivered = newTab(null);

        tabs.addTab("Pending", tabPending.panel);
        tabs.addTab("Preparing", tabPreparing.panel);
        tabs.addTab("Hand-in to Rider", tabHandRider.panel);
        tabs.addTab("Ready to Pick-up", tabReadyPickup.panel);
        tabs.addTab("Completed", tabCompleted.panel);
        tabs.addTab("Delivered", tabDelivered.panel);

        pnlTabs.add(tabs, BorderLayout.CENTER);

        ButtonColumn.install(tabPending.table, tabPending.actionCol(0), "Preparing", UiUtil.PRIMARY,
                row -> onAdvance(tabPending, row, "preparing", null));

        ButtonColumn.install(tabPreparing.table, tabPreparing.actionCol(0),
                ButtonColumn.of(
                        row -> "pickup".equals(tabPreparing.orderAt(row) == null
                                ? "" : tabPreparing.orderAt(row).getDeliveryMethod())
                                ? "Ready to Pick-up" : "Hand in to Rider",
                        row -> true,
                        row -> UiUtil.PRIMARY,
                        row -> null),
                row -> {
                    Order order = tabPreparing.orderAt(row);
                    if (order == null) {
                        return;
                    }
                    String next = "pickup".equals(order.getDeliveryMethod())
                            ? "ready_for_pickup" : "handed_to_rider";
                    onAdvance(tabPreparing, row, next, null);
                });

        ButtonColumn.install(tabReadyPickup.table, tabReadyPickup.actionCol(0),
                ButtonColumn.of(
                        row -> "Paid",
                        row -> {
                            Order order = tabReadyPickup.orderAt(row);
                            return order != null && !Boolean.TRUE.equals(pickupPaid.get(order.getId()));
                        },
                        row -> UiUtil.ACCENT_DARK,
                        row -> "This order is already paid."),
                row -> onMarkPaid(row));
        ButtonColumn.install(tabReadyPickup.table, tabReadyPickup.actionCol(1),
                ButtonColumn.of(
                        row -> "Completed",
                        row -> {
                            Order order = tabReadyPickup.orderAt(row);
                            return order != null && Boolean.TRUE.equals(pickupPaid.get(order.getId()));
                        },
                        row -> UiUtil.PRIMARY,
                        row -> "Record the payment first with \"Paid\"."),
                row -> onAdvance(tabReadyPickup, row, "completed", null));
    }

    private Tab newTab(String[] actionLabels) {
        JTable table = new JTable();
        List<String> columns = new ArrayList<>(List.of(
                "ID", "Buyer", "Contact", "Date", "Delivery", "Rider", "Total", "Status", "Items"));
        int actionCount = actionLabels == null ? 0 : actionLabels.length;
        for (String label : actionLabels == null ? new String[0] : actionLabels) {
            columns.add(label);
        }
        table.setModel(new DefaultTableModel(new Object[0][0], columns.toArray()) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col >= columns.size() - actionCount && actionCount > 0;
            }
        });
        UiUtil.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(scroll, BorderLayout.CENTER);
        TablePager pager = TablePager.install(table, false);
        Tab tab = new Tab(panel, table, pager, actionCount);
        return tab;
    }

    public void refreshData() {
        try {
            int farmerId = SessionContext.getFarmerId();
            orders.clear();
            if (farmerId > 0) {
                orders.addAll(new OrderDAO().findForFarmer(farmerId));
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
            return;
        }

        tabPending.visible.clear();
        tabPreparing.visible.clear();
        tabHandRider.visible.clear();
        tabReadyPickup.visible.clear();
        tabCompleted.visible.clear();
        tabDelivered.visible.clear();
        pickupPaid.clear();

        PaymentDAO paymentDAO = new PaymentDAO();
        anilink.data.DeliveryPaymentDAO deliveryPaymentDAO = new anilink.data.DeliveryPaymentDAO();
        for (Order order : orders) {
            if (Payment.METHOD_ONLINE.equals(order.getPaymentMethod())) {
                if ("pickup".equals(order.getDeliveryMethod())) {
                    // A G-Cash pickup order waits in Manage Payment until the
                    // farmer approves it there; cash-on-pickup has nothing to
                    // pre-approve.
                    boolean verified;
                    try {
                        Optional<Payment> payment = paymentDAO.findByOrder(order.getId());
                        verified = payment.isPresent() && payment.get().isVerified();
                    } catch (SQLException e) {
                        verified = false;
                    }
                    if (!verified) {
                        continue;
                    }
                } else if ("delivery".equals(order.getDeliveryMethod())) {
                    // A G-Cash delivery order waits here until BOTH the
                    // product payment (this farmer, above) and the delivery
                    // fee (admin, Manage Delivery Payment) are approved.
                    boolean productVerified;
                    boolean deliveryFeeVerified;
                    try {
                        Optional<Payment> payment = paymentDAO.findByOrder(order.getId());
                        productVerified = payment.isPresent() && payment.get().isVerified();
                    } catch (SQLException e) {
                        productVerified = false;
                    }
                    try {
                        deliveryFeeVerified = deliveryPaymentDAO.isVerified(order.getId());
                    } catch (SQLException e) {
                        deliveryFeeVerified = false;
                    }
                    if (!productVerified || !deliveryFeeVerified) {
                        continue;
                    }
                }
            }
            String status = order.getStatus();
            boolean delivery = "delivery".equals(order.getDeliveryMethod());
            if ("pending".equals(status)) {
                tabPending.visible.add(order);
            } else if ("preparing".equals(status)) {
                tabPreparing.visible.add(order);
            } else if ("handed_to_rider".equals(status) || "on_the_way".equals(status)) {
                tabHandRider.visible.add(order);
            } else if ("ready_for_pickup".equals(status)) {
                tabReadyPickup.visible.add(order);
                try {
                    Optional<Payment> payment = paymentDAO.findByOrder(order.getId());
                    pickupPaid.put(order.getId(), payment.isPresent() && payment.get().isVerified());
                } catch (SQLException e) {
                    pickupPaid.put(order.getId(), false);
                }
            } else if ("completed".equals(status)) {
                (delivery ? tabDelivered : tabCompleted).visible.add(order);
            }
        }

        render(tabPending);
        render(tabPreparing);
        render(tabHandRider);
        render(tabReadyPickup);
        render(tabCompleted);
        render(tabDelivered);
    }

    private void render(Tab tab) {
        tab.pager.clear();
        tab.numbers.clear();
        int rowNumber = 1;
        for (Order order : tab.visible) {
            int number = rowNumber++;
            tab.numbers.put(order.getId(), number);
            List<Object> cells = new ArrayList<>(List.of(
                    number,
                    order.getBuyerName() == null ? "" : order.getBuyerName(),
                    order.getBuyerContact() == null ? "-" : order.getBuyerContact(),
                    UiUtil.formatDateTime(order.getCreatedAt()),
                    order.getDeliveryMethod() == null ? "" : order.getDeliveryMethod(),
                    order.getRiderName() == null || order.getRiderName().isEmpty()
                            ? "-" : order.getRiderName(),
                    UiUtil.formatPrice(order.getTotalAmount()),
                    UiUtil.deliveryBadge(order.getStatus(), order.getDeliveryMethod()),
                    order.getItemsSummary() == null ? "" : order.getItemsSummary()));
            for (int i = 0; i < tab.actionCount; i++) {
                cells.add("");
            }
            tab.pager.addRow(cells.toArray());
        }
        tab.pager.apply();
    }

    private void onAdvance(Tab tab, int modelRow, String newStatus, String confirmMessageOverride) {
        Order order = tab.orderAt(modelRow);
        if (order == null) {
            return;
        }
        int number = tab.numbers.getOrDefault(order.getId(), order.getId());
        String message = confirmMessageOverride != null ? confirmMessageOverride
                : "Move order #" + number + " to \"" + UiUtil.statusBadge(newStatus) + "\"?";
        if (!UiUtil.confirm(this, message)) {
            return;
        }
        try {
            OrderDAO orderDAO = new OrderDAO();
            int farmerId = SessionContext.getFarmerId();
            if (!orderDAO.farmerHasOrderItem(order.getId(), farmerId)) {
                UiUtil.error(this, "Order not found for your farm.");
                return;
            }
            if (orderDAO.updateStatus(order.getId(), newStatus)) {
                refreshData();
            } else {
                UiUtil.error(this, "Unable to update the order. Please try again.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void onMarkPaid(int modelRow) {
        Order order = tabReadyPickup.orderAt(modelRow);
        if (order == null) {
            return;
        }
        JTextField txtAmount = new JTextField();
        JPanel panel = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Order total: " + UiUtil.formatPrice(order.getTotalAmount())));
        panel.add(new JLabel("Amount received from buyer:"));
        panel.add(txtAmount);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, panel,
                    "Record Payment - Order #"
                            + tabReadyPickup.numbers.getOrDefault(order.getId(), order.getId()),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(txtAmount.getText().trim());
            } catch (NumberFormatException e) {
                UiUtil.error(this, "Enter a valid amount.");
                continue;
            }
            if (amount < order.getTotalAmount()) {
                UiUtil.error(this, "Amount must be at least "
                        + UiUtil.formatPrice(order.getTotalAmount()) + ".");
                continue;
            }
            break;
        }

        try {
            PaymentDAO paymentDAO = new PaymentDAO();
            if (!paymentDAO.existsForOrder(order.getId())) {
                paymentDAO.create(order.getBuyerId(), order.getId(), Payment.METHOD_COD, null, null);
            }
            paymentDAO.updateStatus(order.getId(), Payment.STATUS_VERIFIED);
            UiUtil.info(this, "Payment recorded. You can now mark this order Completed.");
            refreshData();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    /** One tab's table, pager and the order list it currently shows. */
    private static final class Tab {

        final JPanel panel;
        final JTable table;
        final TablePager pager;
        final List<Order> visible = new ArrayList<>();
        final int actionCount;
        /** This tab's own row-position numbers (1 for its own first row),
         * keyed by order id - for {@link #onAdvance}/{@link #onMarkPaid}'s
         * messages to show the same number the row does. */
        final Map<Integer, Integer> numbers = new HashMap<>();

        Tab(JPanel panel, JTable table, TablePager pager, int actionCount) {
            this.panel = panel;
            this.table = table;
            this.pager = pager;
            this.actionCount = actionCount;
        }

        int actionCol(int index) {
            return table.getColumnCount() - actionCount + index;
        }

        Order orderAt(int modelRow) {
            int sourceIndex = pager.sourceIndexOf(modelRow);
            return sourceIndex < 0 || sourceIndex >= visible.size() ? null : visible.get(sourceIndex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlToolbar = new javax.swing.JPanel();
        pnlTabs = new javax.swing.JPanel();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Manage Order");

        // Refresh button is built at runtime in buildToolbar().
        javax.swing.GroupLayout pnlToolbarLayout = new javax.swing.GroupLayout(pnlToolbar);
        pnlToolbar.setLayout(pnlToolbarLayout);
        pnlToolbarLayout.setHorizontalGroup(
            pnlToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlToolbarLayout.setVerticalGroup(
            pnlToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 34, Short.MAX_VALUE)
        );

        // The JTabbedPane and its per-tab tables are built at runtime in buildTabs().
        javax.swing.GroupLayout pnlTabsLayout = new javax.swing.GroupLayout(pnlTabs);
        pnlTabs.setLayout(pnlTabsLayout);
        pnlTabsLayout.setHorizontalGroup(
            pnlTabsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlTabsLayout.setVerticalGroup(
            pnlTabsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 440, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(pnlToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(pnlTabs, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlTabs;
    private javax.swing.JPanel pnlToolbar;
    // End of variables declaration//GEN-END:variables
}
