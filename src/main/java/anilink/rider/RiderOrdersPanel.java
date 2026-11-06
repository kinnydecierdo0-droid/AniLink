package anilink.rider;

import anilink.data.OrderDAO;
import anilink.data.RiderDAO;
import anilink.model.Order;
import anilink.model.Rider;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

/**
 * Transaction history: a read-only record of every order ever assigned to
 * this rider, any status. The interactive workflow (Received / On the Way)
 * lives in {@link RiderDeliveriesPanel} - this page is purely for looking
 * back, the same role Transaction History plays for admin and farmer.
 */
public class RiderOrdersPanel extends javax.swing.JPanel {

    private static final String[] STATUSES = {
        "pending", "preparing", "handed_to_rider", "on_the_way", "completed", "cancelled"
    };

    private final MainFrame frame;
    private TablePager pager;
    private boolean initializing = true;

    public RiderOrdersPanel(MainFrame frame, String title) {
        this.frame = frame;
        initComponents();
        setupTable();
        UiUtil.styleBanner(lblTitle, title);
        UiUtil.styleFieldLabel(lblStatusFilter);
        UiUtil.styleStrip(pnlFilter);
        UiUtil.styleButton(btnRefresh);
        UiUtil.styleTable(tblOrders);
        pager = TablePager.install(tblOrders, true);

        cmbFilter.removeAllItems();
        cmbFilter.addItem("All");
        for (String status : STATUSES) {
            cmbFilter.addItem(status);
        }
        initializing = false;
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
        try {
            Object filter = cmbFilter.getSelectedItem();
            String statusFilter = filter == null || "All".equals(filter)
                    ? null : String.valueOf(filter);

            pager.clear();
            Integer riderId = getRiderId();
            if (riderId != null) {
                OrderDAO orderDAO = new OrderDAO();
                // Numbered against this rider's full order history (not just
                // whatever the status dropdown currently narrows it to), so
                // a number doesn't shift when the filter changes, and stays
                // the same as on Manage Delivery/Rider Dashboard.
                java.util.Map<Integer, Integer> orderNumbers = UiUtil.stableNumbers(
                        orderDAO.findByRider(riderId, null), Order::getId);
                for (Order order : orderDAO.findByRider(riderId, statusFilter)) {
                    pager.addRow(new Object[]{
                        orderNumbers.getOrDefault(order.getId(), order.getId()),
                        UiUtil.formatDateTime(order.getCreatedAt()),
                        order.getBuyerName(),
                        order.getDeliveryAddress() == null || order.getDeliveryAddress().isEmpty()
                                ? "-" : order.getDeliveryAddress(),
                        UiUtil.formatPrice(order.getTotalAmount()),
                        UiUtil.deliveryBadge(order.getStatus(), order.getDeliveryMethod()),
                        order.getItemsSummary() == null ? "" : order.getItemsSummary()
                    }, order.getCreatedAt());
                }
            }
            pager.apply();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void setupTable() {
        tblOrders.setModel(new DefaultTableModel(new Object[0][0],
                new String[]{"Order #", "Date", "Buyer", "Address", "Total", "Status", "Items"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tblOrders.setDefaultEditor(Object.class, null);
        tblOrders.setRowHeight(24);
        tblOrders.setAutoCreateRowSorter(true);
        tblOrders.getTableHeader().setReorderingAllowed(false);
    }

    private void cmbFilterActionPerformed(java.awt.event.ActionEvent evt) {
        if (initializing) {
            return;
        }
        refreshData();
    }

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        lblStatusFilter = new javax.swing.JLabel();
        cmbFilter = new javax.swing.JComboBox();
        btnRefresh = new javax.swing.JButton();
        scrollOrders = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Transaction history");

        pnlFilter.setOpaque(false);
        pnlFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblStatusFilter.setText("Filter by status:");
        pnlFilter.add(lblStatusFilter);

        cmbFilter.setPreferredSize(new java.awt.Dimension(170, 32));
        cmbFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterActionPerformed(evt);
            }
        });
        pnlFilter.add(cmbFilter);

        btnRefresh.setBackground(new java.awt.Color(27, 94, 32));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setPreferredSize(new java.awt.Dimension(90, 32));
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        pnlFilter.add(btnRefresh);

        scrollOrders.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 0, 24));

        tblOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        scrollOrders.setViewportView(tblOrders);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 24, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 812, Short.MAX_VALUE)
                    .addComponent(scrollOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 812, Short.MAX_VALUE))
                .addGap(0, 24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 400, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox cmbFilter;
    private javax.swing.JLabel lblStatusFilter;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JScrollPane scrollOrders;
    private javax.swing.JTable tblOrders;
    // End of variables declaration//GEN-END:variables
}
