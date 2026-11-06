package anilink.buyer;

import anilink.data.BuyerDAO;
import anilink.data.OrderDAO;
import anilink.model.Buyer;
import anilink.model.Order;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;

/**
 * The buyer's order list, for tracking orders after checkout.
 *
 * <p>Payment selection lives in the Buy modal on the marketplace, so this page
 * carries no payment controls. The table is paginated and date-filterable via
 * {@link TablePager}.
 */
public class BuyerOrdersPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private final TablePager pager;
    /** Rows currently in the pager, in the same order - for mapping a model row
     * back to its order on a button click. */
    private final List<Order> visible = new ArrayList<>();
    /** Each displayed order's row-position number (1 for the first row),
     * keyed by order id - so {@link #onReceived}'s confirm message can
     * reference the same number the row shows. */
    private Map<Integer, Integer> orderNumbers = new HashMap<>();
    /** Only enabled while the selected row's order is "on the way". */
    private final javax.swing.JButton btnMarkReceived = new javax.swing.JButton("Mark as Received");

    public BuyerOrdersPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblOrders.setDefaultEditor(Object.class, null);
        tblOrders.setModel(new DefaultTableModel(new Object[0][0],
                new Object[]{"ID", "Date", "Delivery", "Status", "Total", "Items"}) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });
        UiUtil.styleBanner(lblTitle, "My Orders");
        UiUtil.styleTable(tblOrders);
        tblOrders.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        int[] widths = {70, 140, 90, 140, 100, 220};
        for (int i = 0; i < widths.length && i < tblOrders.getColumnModel().getColumnCount(); i++) {
            tblOrders.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        pager = TablePager.install(tblOrders, true);

        javax.swing.JPanel trailing = pager.dateFilterTrailing();
        if (trailing != null) {
            UiUtil.stylePrimary(btnMarkReceived);
            btnMarkReceived.setPreferredSize(new java.awt.Dimension(160, 32));
            btnMarkReceived.setEnabled(false);
            btnMarkReceived.addActionListener(e -> onReceived(tblOrders.getSelectedRow()));
            trailing.add(btnMarkReceived);
        }

        // The button only makes sense for an "on the way" order, so it stays
        // disabled until a selection actually qualifies - clicking it on a
        // pending/preparing/etc. row should not even be possible.
        tblOrders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateMarkReceivedButton();
            }
        });
    }

    private void updateMarkReceivedButton() {
        Order order = orderAt(tblOrders.getSelectedRow());
        btnMarkReceived.setEnabled(order != null && "on_the_way".equals(order.getStatus()));
    }

    private Order orderAt(int modelRow) {
        int sourceIndex = pager.sourceIndexOf(modelRow);
        return sourceIndex < 0 || sourceIndex >= visible.size() ? null : visible.get(sourceIndex);
    }

    private void onReceived(int modelRow) {
        Order order = orderAt(modelRow);
        if (order == null) {
            UiUtil.info(this, "Please select an order first.");
            return;
        }
        if (!"on_the_way".equals(order.getStatus())) {
            UiUtil.info(this, "Only orders that are \"On the way\" can be marked received.");
            return;
        }
        int number = orderNumbers.getOrDefault(order.getId(), order.getId());
        if (!UiUtil.confirm(this, "Confirm you received order #" + number + "?")) {
            return;
        }
        try {
            if (new OrderDAO().updateStatus(order.getId(), "completed")) {
                refreshData();
            } else {
                UiUtil.error(this, "Unable to update the order. Please try again.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    /** Yellow page banner, matching the Marketplace and Transaction headers. */

    /**
     * Resolves the buyer id, re-loading the profile from the database when the
     * session is missing it. Without this a logged-in buyer whose profile was
     * never cached would see an empty table and no explanation.
     */
    private int resolveBuyerId() {
        int buyerId = SessionContext.getBuyerId();
        if (buyerId > 0 || !SessionContext.isLoggedIn()) {
            return buyerId;
        }
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return -1;
        }
        try {
            Optional<Buyer> found = new BuyerDAO().findByUserId(user.getId());
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
        try {
            int buyerId = resolveBuyerId();
            if (buyerId <= 0) {
                UiUtil.styleBanner(lblTitle, "My Orders  —  please log in as a buyer");
                pager.apply();
                return;
            }
            List<Order> orders = new OrderDAO().findByBuyer(buyerId);
            orderNumbers = new HashMap<>();
            int shown = 0;
            int rowNumber = 1;
            for (Order order : orders) {
                // Finished orders (completed/delivered, or cancelled) have
                // nothing left to track - they live in Transaction History.
                if ("completed".equals(order.getStatus()) || "cancelled".equals(order.getStatus())) {
                    continue;
                }
                shown++;
                visible.add(order);
                int number = rowNumber++;
                orderNumbers.put(order.getId(), number);
                pager.addRow(new Object[]{
                    "#" + number,
                    UiUtil.formatDateTime(order.getCreatedAt()),
                    "delivery".equals(order.getDeliveryMethod()) ? "Delivery" : "Pickup",
                    UiUtil.deliveryBadge(order.getStatus(), order.getDeliveryMethod()),
                    UiUtil.formatPrice(order.getTotalAmount()),
                    order.getItemsSummary() == null ? "" : order.getItemsSummary()
                }, order.getCreatedAt(), order.getId());
            }
            UiUtil.styleBanner(lblTitle, shown == 0
                    ? "My Orders  —  you have no active orders"
                    : "My Orders  —  " + shown + " order(s)");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        pager.apply();
        updateMarkReceivedButton();
    }

    /** Jumps to and selects an order, paging to it if necessary. */
    public void selectOrder(int orderId) {
        pager.selectRowByKey(orderId);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        scrollOrders = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("My Orders");

        tblOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        tblOrders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollOrders.setViewportView(tblOrders);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(scrollOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 420, 420)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollOrders;
    private javax.swing.JTable tblOrders;
    // End of variables declaration//GEN-END:variables
}
