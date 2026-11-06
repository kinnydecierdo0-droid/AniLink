package anilink.buyer;

import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.model.Order;
import anilink.model.OrderItem;
import anilink.model.Payment;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;

public class OrderConfirmationPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private int orderId;

    public OrderConfirmationPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblItems.setDefaultEditor(Object.class, null);
        ((DefaultTableModel) tblItems.getModel()).setColumnIdentifiers(
                new Object[]{"Product", "Qty", "Price", "Subtotal"});
        UiUtil.styleTitle(lblTitle);
        UiUtil.styleSection(lblOrderNo);
        UiUtil.styleSection(lblItemsTitle);
        UiUtil.styleFieldLabel(lblStatus);
        UiUtil.styleFieldLabel(lblPayment);
        UiUtil.styleFieldLabel(lblDelivery);
        UiUtil.styleFieldLabel(lblAddress);
        UiUtil.styleSection(lblTotal);
        UiUtil.styleStrip(pnlButtons);
        UiUtil.styleButton(btnViewOrders);
        UiUtil.styleTintedButton(btnContinueShopping);
        UiUtil.styleTable(tblItems);
    }

    public void loadOrder(int orderId) {
        this.orderId = orderId;
        DefaultTableModel model = (DefaultTableModel) tblItems.getModel();
        model.setRowCount(0);
        try {
            Optional<Order> found = new OrderDAO().findById(orderId);
            if (found.isEmpty()) {
                lblOrderNo.setText("Order #" + orderId);
                lblStatus.setText("Order not found.");
                lblPayment.setText("");
                lblDelivery.setText("");
                lblAddress.setText("");
                lblTotal.setText("");
                btnViewOrders.setEnabled(false);
                return;
            }
            Order order = found.get();
            btnViewOrders.setEnabled(true);
            lblOrderNo.setText("Order #" + order.getId() + "  |  "
                    + UiUtil.formatDateTime(order.getCreatedAt()));
            lblStatus.setText("Status: " + UiUtil.statusBadge(order.getStatus()));
            lblPayment.setText("Payment Method: " + buildPaymentLine(order));
            boolean delivery = "delivery".equals(order.getDeliveryMethod());
            lblDelivery.setText("Delivery Method: " + (delivery ? "Delivery" : "Pickup"));
            String addr = order.getDeliveryAddress();
            lblAddress.setText("Address: " + (!delivery || addr == null || addr.isEmpty()
                    ? "-" : addr));
            for (OrderItem item : order.getItems()) {
                model.addRow(new Object[]{
                    item.getProductName(),
                    item.getQuantity(),
                    UiUtil.formatPrice(item.getPrice()),
                    UiUtil.formatPrice(item.getSubtotal())
                });
            }
            lblTotal.setText(buildTotalLine(order));
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load order: " + e.getMessage());
        }
    }

    /**
     * Shows the fee breakdown when one applies, so the buyer can see where the
     * delivery charge came from.
     */
    private String buildTotalLine(Order order) {
        if (order.getDeliveryFee() <= 0) {
            return "Total:  " + UiUtil.formatPrice(order.getTotalAmount());
        }
        return "<html>Subtotal:  " + UiUtil.formatPrice(order.getSubtotal())
                + "<br>Delivery Fee:  " + UiUtil.formatPrice(order.getDeliveryFee())
                + "<br>Total:  " + UiUtil.formatPrice(order.getTotalAmount())
                + "</html>";
    }

    /** Payment method as the buyer knows it, plus status and reference. */
    private String buildPaymentLine(Order order) {
        String method = order.getPaymentMethod() == null || order.getPaymentMethod().isEmpty()
                ? "-" : UiUtil.paymentLabel(order.getPaymentMethod());
        try {
            Optional<Payment> payment = new PaymentDAO().findByOrder(order.getId());
            if (payment.isPresent()) {
                Payment p = payment.get();
                StringBuilder sb = new StringBuilder(method);
                sb.append("  |  ").append(PaymentDAO.statusLabel(p.getStatus()));
                if (p.getReferenceNo() != null && !p.getReferenceNo().isEmpty()) {
                    sb.append("  |  Ref: ").append(p.getReferenceNo());
                }
                return sb.toString();
            }
        } catch (SQLException e) {
            // Fall back to the method alone; the order itself still displays.
        }
        return method;
    }

    private void btnViewOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        frame.openBuyerOrders(orderId);
    }

    private void btnContinueShoppingActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.MARKETPLACE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblOrderNo = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblPayment = new javax.swing.JLabel();
        lblDelivery = new javax.swing.JLabel();
        lblAddress = new javax.swing.JLabel();
        lblItemsTitle = new javax.swing.JLabel();
        scrollItems = new javax.swing.JScrollPane();
        tblItems = new javax.swing.JTable();
        lblTotal = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        btnViewOrders = new javax.swing.JButton();
        btnContinueShopping = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblTitle.setText("Order Placed Successfully!");

        lblOrderNo.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblOrderNo.setText("Order #0");

        lblStatus.setText("Status: PENDING");

        lblPayment.setText("Payment Method: -");

        lblDelivery.setText("Delivery Method: Pickup");

        lblAddress.setText("Address: -");

        lblItemsTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblItemsTitle.setText("Order Items");

        tblItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        scrollItems.setViewportView(tblItems);

        lblTotal.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblTotal.setForeground(new java.awt.Color(46, 125, 50));
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotal.setText("Total:  \u20B10.00");

        btnViewOrders.setBackground(new java.awt.Color(46, 125, 50));
        btnViewOrders.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnViewOrders.setForeground(new java.awt.Color(255, 255, 255));
        btnViewOrders.setText("View My Orders");
        btnViewOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewOrdersActionPerformed(evt);
            }
        });
        pnlButtons.add(btnViewOrders);

        btnContinueShopping.setText("Continue Shopping");
        btnContinueShopping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContinueShoppingActionPerformed(evt);
            }
        });
        pnlButtons.add(btnContinueShopping);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblOrderNo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblPayment, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDelivery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblItemsTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollItems, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addGap(12, 12, 12)
                .addComponent(lblOrderNo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPayment)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDelivery)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAddress)
                .addGap(12, 12, 12)
                .addComponent(lblItemsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollItems, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(lblTotal)
                .addGap(12, 12, 12)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnContinueShopping;
    private javax.swing.JButton btnViewOrders;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblDelivery;
    private javax.swing.JLabel lblItemsTitle;
    private javax.swing.JLabel lblOrderNo;
    private javax.swing.JLabel lblPayment;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JScrollPane scrollItems;
    private javax.swing.JTable tblItems;
    // End of variables declaration//GEN-END:variables
}
