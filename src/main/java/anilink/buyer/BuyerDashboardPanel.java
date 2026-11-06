package anilink.buyer;

import anilink.data.OrderDAO;
import anilink.model.Order;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class BuyerDashboardPanel extends javax.swing.JPanel {

    private final MainFrame frame;

    public BuyerDashboardPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblRecentOrders.setDefaultEditor(Object.class, null);
        ((DefaultTableModel) tblRecentOrders.getModel()).setColumnIdentifiers(
                new Object[]{"ID", "Date", "Items", "Total", "Status", "Delivery"});
        UiUtil.styleBanner(lblTitle, "Welcome, Buyer!");
        UiUtil.styleTable(tblRecentOrders);
    }

    public void refreshData() {
        try {
            int buyerId = SessionContext.getBuyerId();
            OrderDAO orderDAO = new OrderDAO();
            int pending = 0;
            int completed = 0;
            if (buyerId > 0) {
                pending = orderDAO.countByBuyerAndStatus(buyerId, "pending");
                completed = orderDAO.countByBuyerAndStatus(buyerId, "completed");
            }
            lblCartValue.setText(String.valueOf(SessionContext.cartCount()));
            lblPendingValue.setText(String.valueOf(pending));
            lblCompletedValue.setText(String.valueOf(completed));

            DefaultTableModel model = (DefaultTableModel) tblRecentOrders.getModel();
            model.setRowCount(0);
            if (buyerId > 0) {
                List<Order> orders = orderDAO.findByBuyer(buyerId);
                int shown = 0;
                int rowNumber = 1;
                for (Order order : orders) {
                    if (shown >= 5) {
                        break;
                    }
                    model.addRow(new Object[]{
                        "#" + rowNumber++,
                        UiUtil.formatDateTime(order.getCreatedAt()),
                        order.getItemsSummary() == null ? "" : order.getItemsSummary(),
                        UiUtil.formatPrice(order.getTotalAmount()),
                        UiUtil.statusBadge(order.getStatus()),
                        "delivery".equals(order.getDeliveryMethod()) ? "Delivery" : "Pickup"
                    });
                    shown++;
                }
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void btnBrowseMarketplaceActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.MARKETPLACE);
    }

    private void btnViewCartActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.FAVORITES);
    }

    private void btnMyOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.BUYER_ORDERS);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        pnlStats = new javax.swing.JPanel();
        pnlStatCart = new javax.swing.JPanel();
        lblCartValue = new javax.swing.JLabel();
        lblCartLabel = new javax.swing.JLabel();
        pnlStatPending = new javax.swing.JPanel();
        lblPendingValue = new javax.swing.JLabel();
        lblPendingLabel = new javax.swing.JLabel();
        pnlStatCompleted = new javax.swing.JPanel();
        lblCompletedValue = new javax.swing.JLabel();
        lblCompletedLabel = new javax.swing.JLabel();
        pnlActions = new javax.swing.JPanel();
        btnBrowseMarketplace = new javax.swing.JButton();
        btnViewCart = new javax.swing.JButton();
        btnMyOrders = new javax.swing.JButton();
        lblRecentTitle = new javax.swing.JLabel();
        scrollRecent = new javax.swing.JScrollPane();
        tblRecentOrders = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Welcome, Buyer!");

        lblSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSubtitle.setForeground(new java.awt.Color(96, 125, 107));
        lblSubtitle.setText("Browse fresh products from local farmers.");

        pnlStats.setBackground(new java.awt.Color(248, 250, 248));
        javax.swing.GroupLayout pnlStatsLayout = new javax.swing.GroupLayout(pnlStats);
        pnlStats.setLayout(pnlStatsLayout);
        pnlStatsLayout.setHorizontalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatsLayout.createSequentialGroup()
                .addComponent(pnlStatCart, javax.swing.GroupLayout.PREFERRED_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatPending, javax.swing.GroupLayout.PREFERRED_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, 230, Short.MAX_VALUE))
        );
        pnlStatsLayout.setVerticalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatsLayout.createSequentialGroup()
                .addGroup(pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlStatCart, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlStatPending, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pnlStatCompleted, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlStatCart.setBackground(new java.awt.Color(232, 245, 233));

        lblCartValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblCartValue.setForeground(new java.awt.Color(46, 125, 50));
        lblCartValue.setText("0");

        lblCartLabel.setForeground(new java.awt.Color(96, 125, 107));
        lblCartLabel.setText("Cart Items");

        javax.swing.GroupLayout pnlStatCartLayout = new javax.swing.GroupLayout(pnlStatCart);
        pnlStatCart.setLayout(pnlStatCartLayout);
        pnlStatCartLayout.setHorizontalGroup(
            pnlStatCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatCartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCartValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblCartLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatCartLayout.setVerticalGroup(
            pnlStatCartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatCartLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCartValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCartLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatPending.setBackground(new java.awt.Color(255, 243, 224));

        lblPendingValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblPendingValue.setForeground(new java.awt.Color(230, 126, 34));
        lblPendingValue.setText("0");

        lblPendingLabel.setForeground(new java.awt.Color(96, 125, 107));
        lblPendingLabel.setText("Pending Orders");

        javax.swing.GroupLayout pnlStatPendingLayout = new javax.swing.GroupLayout(pnlStatPending);
        pnlStatPending.setLayout(pnlStatPendingLayout);
        pnlStatPendingLayout.setHorizontalGroup(
            pnlStatPendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatPendingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatPendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPendingValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblPendingLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatPendingLayout.setVerticalGroup(
            pnlStatPendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatPendingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPendingValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPendingLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatCompleted.setBackground(new java.awt.Color(227, 242, 253));

        lblCompletedValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblCompletedValue.setForeground(new java.awt.Color(21, 101, 192));
        lblCompletedValue.setText("0");

        lblCompletedLabel.setForeground(new java.awt.Color(96, 125, 107));
        lblCompletedLabel.setText("Completed Orders");

        javax.swing.GroupLayout pnlStatCompletedLayout = new javax.swing.GroupLayout(pnlStatCompleted);
        pnlStatCompleted.setLayout(pnlStatCompletedLayout);
        pnlStatCompletedLayout.setHorizontalGroup(
            pnlStatCompletedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatCompletedLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatCompletedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCompletedValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblCompletedLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatCompletedLayout.setVerticalGroup(
            pnlStatCompletedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatCompletedLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCompletedValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCompletedLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlActions.setBackground(new java.awt.Color(248, 250, 248));
        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnBrowseMarketplace.setBackground(new java.awt.Color(46, 125, 50));
        btnBrowseMarketplace.setBorderPainted(false);
        btnBrowseMarketplace.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnBrowseMarketplace.setForeground(new java.awt.Color(255, 255, 255));
        btnBrowseMarketplace.setText("Browse Marketplace");
        btnBrowseMarketplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseMarketplaceActionPerformed(evt);
            }
        });
        pnlActions.add(btnBrowseMarketplace);

        btnViewCart.setBackground(new java.awt.Color(232, 245, 233));
        btnViewCart.setBorderPainted(false);
        btnViewCart.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnViewCart.setForeground(new java.awt.Color(27, 94, 32));
        btnViewCart.setText("View Cart");
        btnViewCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewCartActionPerformed(evt);
            }
        });
        pnlActions.add(btnViewCart);

        btnMyOrders.setBackground(new java.awt.Color(232, 245, 233));
        btnMyOrders.setBorderPainted(false);
        btnMyOrders.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnMyOrders.setForeground(new java.awt.Color(27, 94, 32));
        btnMyOrders.setText("My Orders");
        btnMyOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMyOrdersActionPerformed(evt);
            }
        });
        pnlActions.add(btnMyOrders);

        lblRecentTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblRecentTitle.setForeground(new java.awt.Color(27, 94, 32));
        lblRecentTitle.setText("Recent Orders");

        tblRecentOrders.setFont(new java.awt.Font("Segoe UI", 0, 13));
        tblRecentOrders.setFillsViewportHeight(true);
        tblRecentOrders.setGridColor(new java.awt.Color(224, 224, 224));
        tblRecentOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        tblRecentOrders.setRowHeight(28);
        tblRecentOrders.setSelectionBackground(new java.awt.Color(200, 230, 201));
        scrollRecent.setViewportView(tblRecentOrders);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSubtitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlActions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRecentTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollRecent, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblSubtitle)
                .addGap(18, 18, 18)
                .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlActions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblRecentTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollRecent, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowseMarketplace;
    private javax.swing.JButton btnMyOrders;
    private javax.swing.JButton btnViewCart;
    private javax.swing.JLabel lblCartLabel;
    private javax.swing.JLabel lblCartValue;
    private javax.swing.JLabel lblCompletedLabel;
    private javax.swing.JLabel lblCompletedValue;
    private javax.swing.JLabel lblPendingLabel;
    private javax.swing.JLabel lblPendingValue;
    private javax.swing.JLabel lblRecentTitle;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlActions;
    private javax.swing.JPanel pnlStatCart;
    private javax.swing.JPanel pnlStatCompleted;
    private javax.swing.JPanel pnlStatPending;
    private javax.swing.JPanel pnlStats;
    private javax.swing.JScrollPane scrollRecent;
    private javax.swing.JTable tblRecentOrders;
    // End of variables declaration//GEN-END:variables
}
