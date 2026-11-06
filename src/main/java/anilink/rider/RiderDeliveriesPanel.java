package anilink.rider;

import anilink.data.OrderDAO;
import anilink.data.RiderDAO;
import anilink.model.Order;
import anilink.model.Rider;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.ButtonColumn;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Manage Delivery: the rider's active workflow, as three tabs.
 *
 * <p>Pending Delivery ({@code handed_to_rider}, set by the farmer) -&gt; the
 * rider clicks "Received" to acknowledge pickup (no write yet, just flips the
 * button to "On the Way" in place) -&gt; clicking that moves the order to
 * {@code on_the_way} and the On the Way tab, notifying the buyer -&gt;
 * Delivered happens only when the buyer clicks Received on their own order,
 * never from here.
 */
public class RiderDeliveriesPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    /** Orders the rider has clicked "Received" on but not yet "On the Way". */
    private final Set<Integer> acknowledged = new HashSet<>();
    /** Friendly per-rider order numbers (this rider's oldest order = 1),
     * shared with {@link RiderDashboardPanel}/{@link RiderOrdersPanel}. */
    private java.util.Map<Integer, Integer> orderNumbers = new java.util.HashMap<>();

    private Tab tabPendingDelivery;
    private Tab tabOnTheWay;
    private Tab tabDelivered;

    public RiderDeliveriesPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Manage Delivery");
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
        tabPendingDelivery = newTab(new String[]{"Action"});
        tabOnTheWay = newTab(null);
        tabDelivered = newTab(null);

        tabs.addTab("Pending Delivery", tabPendingDelivery.panel);
        tabs.addTab("On the Way", tabOnTheWay.panel);
        tabs.addTab("Delivered", tabDelivered.panel);
        pnlTabs.add(tabs, BorderLayout.CENTER);

        ButtonColumn.install(tabPendingDelivery.table, tabPendingDelivery.actionCol(0),
                ButtonColumn.of(
                        row -> {
                            Order order = tabPendingDelivery.orderAt(row);
                            return order != null && acknowledged.contains(order.getId())
                                    ? "On the Way" : "Received";
                        },
                        row -> true,
                        row -> UiUtil.PRIMARY,
                        row -> null),
                this::onPendingDeliveryClick);
    }

    private Tab newTab(String[] actionLabels) {
        JTable table = new JTable();
        List<String> columns = new ArrayList<>(List.of(
                "ID", "Buyer", "Contact", "Date", "Address", "Total", "Items"));
        int actionCount = actionLabels == null ? 0 : actionLabels.length;
        for (String label : actionLabels == null ? new String[0] : actionLabels) {
            columns.add(label);
        }
        table.setModel(new DefaultTableModel(new Object[0][0], columns.toArray()) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return actionCount > 0 && col >= columns.size() - actionCount;
            }
        });
        UiUtil.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(scroll, BorderLayout.CENTER);
        TablePager pager = TablePager.install(table, false);
        return new Tab(panel, table, pager, actionCount);
    }

    private Integer getRiderId() throws SQLException {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return null;
        }
        Rider rider = new RiderDAO().findByUserId(user.getId()).orElse(null);
        return rider == null ? null : rider.getId();
    }

    public void refreshData() {
        List<Order> orders = new ArrayList<>();
        try {
            Integer riderId = getRiderId();
            if (riderId != null) {
                orders.addAll(new OrderDAO().findByRider(riderId, null));
            }
            orderNumbers = UiUtil.stableNumbers(orders, Order::getId);
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
            return;
        }

        tabPendingDelivery.visible.clear();
        tabOnTheWay.visible.clear();
        tabDelivered.visible.clear();
        Set<Integer> stillPending = new HashSet<>();

        for (Order order : orders) {
            String status = order.getStatus();
            if ("handed_to_rider".equals(status)) {
                tabPendingDelivery.visible.add(order);
                stillPending.add(order.getId());
            } else if ("on_the_way".equals(status)) {
                tabOnTheWay.visible.add(order);
            } else if ("completed".equals(status)) {
                tabDelivered.visible.add(order);
            }
        }
        acknowledged.retainAll(stillPending);

        render(tabPendingDelivery);
        render(tabOnTheWay);
        render(tabDelivered);
    }

    private void render(Tab tab) {
        tab.pager.clear();
        for (Order order : tab.visible) {
            List<Object> cells = new ArrayList<>(List.of(
                    orderNumbers.getOrDefault(order.getId(), order.getId()),
                    order.getBuyerName() == null ? "" : order.getBuyerName(),
                    order.getBuyerContact() == null ? "-" : order.getBuyerContact(),
                    UiUtil.formatDateTime(order.getCreatedAt()),
                    order.getDeliveryAddress() == null || order.getDeliveryAddress().isEmpty()
                            ? "-" : order.getDeliveryAddress(),
                    UiUtil.formatPrice(order.getTotalAmount()),
                    order.getItemsSummary() == null ? "" : order.getItemsSummary()));
            for (int i = 0; i < tab.actionCount; i++) {
                cells.add("");
            }
            tab.pager.addRow(cells.toArray());
        }
        tab.pager.apply();
    }

    private void onPendingDeliveryClick(int modelRow) {
        Order order = tabPendingDelivery.orderAt(modelRow);
        if (order == null) {
            return;
        }
        if (!acknowledged.contains(order.getId())) {
            acknowledged.add(order.getId());
            render(tabPendingDelivery);
            return;
        }
        int number = orderNumbers.getOrDefault(order.getId(), order.getId());
        if (!UiUtil.confirm(this, "Mark order #" + number + " as on the way?")) {
            return;
        }
        try {
            if (new OrderDAO().updateStatus(order.getId(), "on_the_way")) {
                acknowledged.remove(order.getId());
                refreshData();
            } else {
                UiUtil.error(this, "Unable to update the order. Please try again.");
            }
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
        lblTitle.setText("Manage Delivery");

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
