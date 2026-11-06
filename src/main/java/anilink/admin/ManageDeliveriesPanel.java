package anilink.admin;

import anilink.data.OrderDAO;
import anilink.model.Order;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.sql.SQLException;

/**
 * Every delivery order the admin is allowed to see so far: pickup orders
 * never appear here (that is the farmer's page), and a G-Cash delivery order
 * stays off this page until its payment is verified in Manage Delivery
 * Payment - Cash on Delivery has nothing to review ahead of time, so it
 * shows immediately. See {@link OrderDAO#findDeliveries} for the exact rule.
 *
 * <p>View-only by design: progressing a delivery order's status already
 * belongs to the farmer (their own Orders page), and assigning a rider
 * already belongs to Manage Delivery Payment, gated on that same approval.
 * Admin's job here is purely to see which delivery orders are in flight.
 */
public class ManageDeliveriesPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private TablePager pager;
    private boolean initializing = true;

    public ManageDeliveriesPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Manage Deliveries");
        setupTable(tblOrders, new String[]{"ID", "Buyer", "Contact", "Date",
            "Payment", "Total", "Status", "Rider", "Items"});
        UiUtil.styleFieldLabel(lblStatusFilter);
        UiUtil.styleButton(btnRefresh);
        UiUtil.styleTable(tblOrders);
        pager = TablePager.install(tblOrders, true);

        cmbFilter.removeAllItems();
        cmbFilter.addItem("All");
        cmbFilter.addItem("pending");
        cmbFilter.addItem("preparing");
        cmbFilter.addItem("handed_to_rider");
        cmbFilter.addItem("on_the_way");
        cmbFilter.addItem("cancelled");
        initializing = false;
    }

    public void refreshData() {
        try {
            Object filter = cmbFilter.getSelectedItem();
            String statusFilter = filter == null || "All".equals(filter)
                    ? null : String.valueOf(filter);

            pager.clear();
            int rowNumber = 1;
            for (Order order : new OrderDAO().findDeliveries(statusFilter)) {
                pager.addRow(new Object[]{
                    rowNumber++,
                    order.getBuyerName(),
                    order.getBuyerContact() == null ? "-" : order.getBuyerContact(),
                    UiUtil.formatDateTime(order.getCreatedAt()),
                    UiUtil.paymentLabel(order.getPaymentMethod()),
                    UiUtil.formatPrice(order.getTotalAmount()),
                    UiUtil.statusBadge(order.getStatus()),
                    order.getRiderName() == null || order.getRiderName().isEmpty()
                            ? "-" : order.getRiderName(),
                    order.getItemsSummary() == null ? "" : order.getItemsSummary()
                }, order.getCreatedAt());
            }
            pager.apply();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private static void setupTable(javax.swing.JTable table, String[] columns) {
        table.setModel(new javax.swing.table.DefaultTableModel(new Object[0][0], columns) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        table.setDefaultEditor(Object.class, null);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
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
        cmbFilter = new javax.swing.JComboBox<>();
        btnRefresh = new javax.swing.JButton();
        scrollOrders = new javax.swing.JScrollPane();
        tblOrders = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Manage Deliveries");

        pnlFilter.setBackground(new java.awt.Color(248, 250, 248));

        lblStatusFilter.setText("Filter by status:");

        cmbFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All" }));
        cmbFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterActionPerformed(evt);
            }
        });

        btnRefresh.setBackground(new java.awt.Color(27, 94, 32));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addComponent(lblStatusFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnRefresh))
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblStatusFilter)
            .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        tblOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {},
            new String[] {}
        ) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        scrollOrders.setViewportView(tblOrders);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(scrollOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefresh;
    private javax.swing.JComboBox<String> cmbFilter;
    private javax.swing.JLabel lblStatusFilter;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JScrollPane scrollOrders;
    private javax.swing.JTable tblOrders;
    // End of variables declaration//GEN-END:variables
}
