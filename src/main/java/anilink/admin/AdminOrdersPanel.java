package anilink.admin;

import anilink.data.OrderDAO;
import anilink.model.Order;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.sql.SQLException;

/**
 * The admin's ledger of finished business: every order that reached
 * {@code completed}, pickup or delivery alike. This is read-only by design -
 * progressing a delivery order's status or assigning a rider belongs on the
 * Manage Deliveries page, not here, since this page's whole point is to show
 * what has already finished.
 */
public class AdminOrdersPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private TablePager pager;

    public AdminOrdersPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Transaction History");
        tblOrders.setModel(new javax.swing.table.DefaultTableModel(new Object[0][0],
                new String[]{"ID", "Buyer", "Contact", "Date", "Delivery",
                    "Payment", "Total", "Status", "Rider", "Items"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tblOrders.setDefaultEditor(Object.class, null);
        tblOrders.setRowHeight(24);
        tblOrders.setAutoCreateRowSorter(true);
        tblOrders.getTableHeader().setReorderingAllowed(false);
        UiUtil.styleTable(tblOrders);
        pager = TablePager.install(tblOrders, true);
    }

    public void refreshData() {
        pager.clear();
        try {
            int rowNumber = 1;
            for (Order order : new OrderDAO().findAll("completed")) {
                pager.addRow(new Object[]{
                    rowNumber++,
                    order.getBuyerName(),
                    order.getBuyerContact() == null ? "-" : order.getBuyerContact(),
                    UiUtil.formatDateTime(order.getCreatedAt()),
                    order.getDeliveryMethod(),
                    UiUtil.paymentLabel(order.getPaymentMethod()),
                    UiUtil.formatPrice(order.getTotalAmount()),
                    UiUtil.statusBadge(order.getStatus()),
                    order.getRiderName() == null || order.getRiderName().isEmpty()
                            ? "-" : order.getRiderName(),
                    order.getItemsSummary() == null ? "" : order.getItemsSummary()
                }, order.getCreatedAt());
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        pager.apply();
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
        lblTitle.setText("Transaction History");

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
                .addComponent(scrollOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JScrollPane scrollOrders;
    private javax.swing.JTable tblOrders;
    // End of variables declaration//GEN-END:variables
}
