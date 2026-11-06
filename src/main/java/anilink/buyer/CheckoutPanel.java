package anilink.buyer;

import anilink.data.BuyerDAO;
import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.data.ProductDAO;
import anilink.model.Buyer;
import anilink.model.Payment;
import anilink.model.Product;
import anilink.model.User;
import anilink.model.VehicleType;
import anilink.session.CartItem;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;

public class CheckoutPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    /** Item total for the current cart, kept so the fee can be re-applied. */
    private double itemSubtotal;
    /** Placeholder vehicle note; any selection ends with .toString() == its
     * label, so the real {@link VehicleType} values are distinguished by
     * {@code instanceof} in {@link #selectedVehicle()} instead. */
    private final javax.swing.JComboBox<Object> cmbVehicle = new javax.swing.JComboBox<>(
            new Object[]{"-- Select vehicle --", VehicleType.MOTORCYCLE, VehicleType.MINI_VAN, VehicleType.WING_VAN});

    public CheckoutPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblSummary.setDefaultEditor(Object.class, null);
        ((DefaultTableModel) tblSummary.getModel()).setColumnIdentifiers(
                new Object[]{"Product", "Unit", "Price", "Qty", "Subtotal"});
        buttonGroup1.add(rbPickup);
        buttonGroup1.add(rbDelivery);
        rbPickup.setSelected(true);
        rbPickup.addActionListener(e -> onDeliveryMethodChanged());
        rbDelivery.addActionListener(e -> onDeliveryMethodChanged());
        cmbVehicle.setVisible(false);
        cmbVehicle.addActionListener(e -> onVehicleChanged());
        pnlDeliveryRow.add(cmbVehicle);
        cmbPayment.addActionListener(e -> recalculateTotals());
        updatePaymentOptions();
        lblDeliveryNote.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 4, 0, 0, UiUtil.ACCENT_DARK),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        lblDeliveryNote.setVisible(false);
        UiUtil.styleFieldLabel(lblFeeCaption);
        UiUtil.styleValue(lblFeeValue);
        UiUtil.styleBanner(lblTitle, "Checkout");
        UiUtil.styleSection(lblSummaryTitle);
        UiUtil.styleSection(lblTotalCaption);
        UiUtil.styleSection(lblTotalValue);
        UiUtil.styleFieldLabel(lblBuyerNameCaption);
        UiUtil.styleFieldLabel(lblContactCaption);
        UiUtil.styleFieldLabel(lblAddressCaption);
        UiUtil.styleFieldLabel(lblDeliveryCaption);
        UiUtil.styleFieldLabel(lblPaymentCaption);
        UiUtil.styleFieldLabel(lblSubtotalCaption);
        UiUtil.styleValue(lblSubtotalValue);
        UiUtil.styleValue(lblBuyerNameValue);
        UiUtil.styleValue(lblContactValue);
        UiUtil.styleStrip(pnlDeliveryRow);
        UiUtil.styleStrip(pnlTotalRow);
        UiUtil.styleStrip(pnlButtons);
        UiUtil.styleTintedButton(btnBackToCart);
        UiUtil.styleButton(btnPlaceOrder);
        UiUtil.styleTable(tblSummary);
    }

    public void refreshData() {
        try {
            User user = SessionContext.getCurrentUser();
            Buyer buyer = SessionContext.getBuyerProfile();
            if (buyer == null && SessionContext.isLoggedIn()) {
                Optional<Buyer> found = new BuyerDAO().findByUserId(user.getId());
                if (found.isPresent()) {
                    buyer = found.get();
                    SessionContext.setBuyerProfile(buyer);
                }
            }
            String name = user != null ? user.getName() : "";
            String contact = buyer != null && buyer.getContactNumber() != null
                    ? buyer.getContactNumber() : "";
            String address = buyer != null && buyer.getAddress() != null
                    ? buyer.getAddress() : "";
            lblBuyerNameValue.setText(name == null || name.isEmpty() ? "-" : name);
            lblContactValue.setText(contact.isEmpty() ? "-" : contact);
            if (txtAddress.getText().isEmpty() || address.equals(txtAddress.getText())) {
                txtAddress.setText(address);
            }

            DefaultTableModel model = (DefaultTableModel) tblSummary.getModel();
            model.setRowCount(0);
            itemSubtotal = 0;
            for (CartItem item : new ArrayList<>(SessionContext.getCart())) {
                try {
                    Optional<Product> fresh = new ProductDAO().findById(item.getProduct().getId());
                    if (fresh.isPresent()) {
                        item.setProduct(fresh.get());
                    }
                } catch (SQLException ignored) {
                }
                model.addRow(new Object[]{
                    item.getProduct().getProductName(),
                    item.getProduct().getUnit(),
                    UiUtil.formatPrice(item.getProduct().getPrice()),
                    item.getQuantity(),
                    UiUtil.formatPrice(item.getSubtotal())
                });
                itemSubtotal += item.getSubtotal();
            }
            updatePaymentOptions();
            onDeliveryMethodChanged();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    /**
     * Delivery orders are G-Cash only - cash never changes hands with a rider
     * in the middle, so there is nobody to collect cash on arrival. Pickup
     * orders keep both options since the farmer collects payment in person.
     */
    private void updatePaymentOptions() {
        String previous = cmbPayment.getSelectedItem() instanceof String
                ? (String) cmbPayment.getSelectedItem() : null;
        cmbPayment.removeAllItems();
        // Buyers see "G-Cash Payment"; the stored value stays "Online Payment".
        if (!rbDelivery.isSelected()) {
            cmbPayment.addItem(Payment.METHOD_COD);
        }
        cmbPayment.addItem(UiUtil.paymentLabel(Payment.METHOD_ONLINE));
        if (previous != null) {
            for (int i = 0; i < cmbPayment.getItemCount(); i++) {
                if (previous.equals(cmbPayment.getItemAt(i))) {
                    cmbPayment.setSelectedIndex(i);
                    break;
                }
            }
        }
        cmbPayment.setEnabled(cmbPayment.getItemCount() > 1);
    }

    private void onDeliveryMethodChanged() {
        boolean delivery = rbDelivery.isSelected();
        cmbVehicle.setVisible(delivery);
        if (!delivery) {
            cmbVehicle.setSelectedIndex(0);
        }
        updateDeliveryNote();
        updatePaymentOptions();
        recalculateTotals();
        revalidate();
        repaint();
    }

    private void onVehicleChanged() {
        updateDeliveryNote();
        recalculateTotals();
    }

    /** Only shows once a specific vehicle is picked - acknowledges that
     * vehicle's own fee, instead of a generic flat-fee note. */
    private void updateDeliveryNote() {
        VehicleType vehicle = selectedVehicle();
        boolean show = rbDelivery.isSelected() && vehicle != null;
        if (show) {
            lblDeliveryNote.setText("Note: You selected " + vehicle.getLabel()
                    + " - a delivery fee of " + UiUtil.formatPrice(vehicle.getFee()) + " applies.");
        }
        lblDeliveryNote.setVisible(show);
    }

    private VehicleType selectedVehicle() {
        Object selected = cmbVehicle.getSelectedItem();
        return selected instanceof VehicleType ? (VehicleType) selected : null;
    }

    /** Recomputes subtotal / delivery fee / total and shows or hides the fee. */
    private void recalculateTotals() {
        double fee = deliveryFee();
        lblSubtotalValue.setText(UiUtil.formatPrice(itemSubtotal));
        lblFeeValue.setText(UiUtil.formatPrice(fee));
        lblTotalValue.setText(UiUtil.formatPrice(itemSubtotal + fee));
        // The fee line only exists for delivery orders.
        lblFeeCaption.setVisible(fee > 0);
        lblFeeValue.setVisible(fee > 0);
    }

    private double deliveryFee() {
        VehicleType vehicle = selectedVehicle();
        return rbDelivery.isSelected() && vehicle != null ? vehicle.getFee() : 0;
    }

    private String selectedPaymentValue() {
        Object selected = cmbPayment.getSelectedItem();
        return UiUtil.paymentValue(selected == null ? null : selected.toString());
    }

    private void btnPlaceOrderActionPerformed(java.awt.event.ActionEvent evt) {
        if (!SessionContext.isLoggedIn() || !SessionContext.isRole("buyer")) {
            UiUtil.error(this, "Please log in as a buyer to place an order.");
            return;
        }
        int buyerId = SessionContext.getBuyerId();
        if (buyerId <= 0) {
            UiUtil.error(this, "Buyer profile not found. Please re-login.");
            return;
        }
        if (SessionContext.getCart().isEmpty()) {
            UiUtil.error(this, "Your cart is empty.");
            return;
        }
        boolean delivery = rbDelivery.isSelected();
        String address = txtAddress.getText().trim();
        if (delivery && address.isEmpty()) {
            UiUtil.error(this, "Delivery address is required for delivery orders.");
            txtAddress.requestFocus();
            return;
        }
        VehicleType vehicle = selectedVehicle();
        if (delivery && vehicle == null) {
            UiUtil.error(this, "Please select a delivery vehicle.");
            return;
        }
        String method = delivery ? "delivery" : "pickup";
        String payment = selectedPaymentValue();
        if (payment == null || payment.isEmpty()) {
            UiUtil.error(this, "Please select a payment method.");
            return;
        }
        double fee = deliveryFee();

        // Collect the G-Cash details BEFORE anything is written, so cancelling
        // the payment dialog leaves no order behind.
        String referenceNo = null;
        String proofImage = null;
        if (Payment.METHOD_ONLINE.equals(payment)) {
            GcashPaymentDialog dialog = new GcashPaymentDialog(
                    javax.swing.SwingUtilities.getWindowAncestor(this), itemSubtotal, fee);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) {
                return;
            }
            referenceNo = dialog.getReferenceNo();
            proofImage = dialog.getProofImage();
        }

        btnPlaceOrder.setEnabled(false);
        try {
            int orderId = new OrderDAO().placeOrder(buyerId, SessionContext.getCart(),
                    method, address, payment, fee, vehicle == null ? null : vehicle.name());

            // placeOrder and PaymentDAO.create each own their transaction, so a
            // failure here leaves the order unpaid - payable later from My Orders.
            String paymentError = null;
            try {
                new PaymentDAO().create(buyerId, orderId, payment, referenceNo, proofImage);
            } catch (SQLException e) {
                paymentError = e.getMessage();
            }

            SessionContext.clearCart();
            frame.showOrderConfirmation(orderId);
            if (paymentError != null) {
                UiUtil.error(frame, "Your order was placed, but the payment could not be "
                        + "recorded:\n" + paymentError
                        + "\n\nYou can submit it again from My Orders.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage() != null && !e.getMessage().isEmpty()
                    ? e.getMessage() : "Failed to place order. Please try again.");
            refreshData();
        } finally {
            btnPlaceOrder.setEnabled(true);
        }
    }

    private void btnBackToCartActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.FAVORITES);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblTitle = new javax.swing.JLabel();
        lblBuyerNameCaption = new javax.swing.JLabel();
        lblBuyerNameValue = new javax.swing.JLabel();
        lblContactCaption = new javax.swing.JLabel();
        lblContactValue = new javax.swing.JLabel();
        lblAddressCaption = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        lblDeliveryCaption = new javax.swing.JLabel();
        rbPickup = new javax.swing.JRadioButton();
        rbDelivery = new javax.swing.JRadioButton();
        lblDeliveryNote = new javax.swing.JLabel();
        lblPaymentCaption = new javax.swing.JLabel();
        lblSubtotalCaption = new javax.swing.JLabel();
        lblSubtotalValue = new javax.swing.JLabel();
        lblFeeCaption = new javax.swing.JLabel();
        lblFeeValue = new javax.swing.JLabel();
        cmbPayment = new javax.swing.JComboBox<>();
        pnlDeliveryRow = new javax.swing.JPanel();
        lblSummaryTitle = new javax.swing.JLabel();
        scrollSummary = new javax.swing.JScrollPane();
        tblSummary = new javax.swing.JTable();
        pnlTotalRow = new javax.swing.JPanel();
        lblTotalCaption = new javax.swing.JLabel();
        lblTotalValue = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        btnBackToCart = new javax.swing.JButton();
        btnPlaceOrder = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Checkout");

        lblBuyerNameCaption.setText("Buyer Name:");

        lblBuyerNameValue.setText("-");

        lblContactCaption.setText("Contact Number:");

        lblContactValue.setText("-");

        lblAddressCaption.setText("Delivery Address:");
        lblAddressCaption.setToolTipText("");

        txtAddress.setToolTipText("Street, barangay, city / municipality");

        lblDeliveryCaption.setText("Delivery Method:");

        rbPickup.setSelected(true);
        rbPickup.setText("Pickup");
        buttonGroup1.add(rbPickup);

        rbDelivery.setText("Delivery");
        buttonGroup1.add(rbDelivery);

        lblDeliveryNote.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblDeliveryNote.setOpaque(true);
        lblDeliveryNote.setBackground(new java.awt.Color(255, 248, 225));
        lblDeliveryNote.setForeground(new java.awt.Color(51, 38, 0));
        lblDeliveryNote.setText("Note: A delivery fee applies to delivery orders.");

        lblPaymentCaption.setText("Payment Method:");

        lblSubtotalCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblSubtotalCaption.setText("Subtotal:");

        lblSubtotalValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblSubtotalValue.setText("\u20B10.00");

        lblFeeCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblFeeCaption.setText("Delivery Fee:");

        lblFeeValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblFeeValue.setText("\u20B10.00");

        cmbPayment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash on Delivery", "G-Cash Payment" }));

        pnlDeliveryRow.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        pnlDeliveryRow.add(rbPickup);
        pnlDeliveryRow.add(rbDelivery);

        lblSummaryTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSummaryTitle.setText("Order Summary");

        tblSummary.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        scrollSummary.setViewportView(tblSummary);

        lblTotalCaption.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblTotalCaption.setForeground(new java.awt.Color(46, 125, 50));
        lblTotalCaption.setText("Total:");

        lblTotalValue.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblTotalValue.setForeground(new java.awt.Color(46, 125, 50));
        lblTotalValue.setText("\u20B10.00");

        javax.swing.GroupLayout pnlTotalRowLayout = new javax.swing.GroupLayout(pnlTotalRow);
        pnlTotalRow.setLayout(pnlTotalRowLayout);
        pnlTotalRowLayout.setHorizontalGroup(
            pnlTotalRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalRowLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSubtotalCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtotalValue, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(lblFeeCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFeeValue, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(lblTotalCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalValue, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlTotalRowLayout.setVerticalGroup(
            pnlTotalRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalRowLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(pnlTotalRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubtotalCaption)
                    .addComponent(lblSubtotalValue)
                    .addComponent(lblFeeCaption)
                    .addComponent(lblFeeValue)
                    .addComponent(lblTotalCaption)
                    .addComponent(lblTotalValue)))
        );

        btnBackToCart.setText("Back to Cart");
        btnBackToCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackToCartActionPerformed(evt);
            }
        });
        pnlButtons.add(btnBackToCart);

        btnPlaceOrder.setBackground(new java.awt.Color(46, 125, 50));
        btnPlaceOrder.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnPlaceOrder.setForeground(new java.awt.Color(255, 255, 255));
        btnPlaceOrder.setText("Place Order");
        btnPlaceOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPlaceOrderActionPerformed(evt);
            }
        });
        pnlButtons.add(btnPlaceOrder);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblBuyerNameCaption)
                            .addComponent(lblContactCaption)
                            .addComponent(lblAddressCaption)
                            .addComponent(lblDeliveryCaption)
                            .addComponent(lblPaymentCaption)
                            .addComponent(lblSummaryTitle))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblBuyerNameValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblContactValue, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlDeliveryRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDeliveryNote, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbPayment, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollSummary, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlTotalRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblBuyerNameCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBuyerNameValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblContactCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblContactValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblAddressCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDeliveryCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlDeliveryRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(lblDeliveryNote, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblPaymentCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbPayment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblSummaryTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollSummary, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlTotalRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBackToCart;
    private javax.swing.JButton btnPlaceOrder;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cmbPayment;
    private javax.swing.JLabel lblAddressCaption;
    private javax.swing.JLabel lblBuyerNameCaption;
    private javax.swing.JLabel lblBuyerNameValue;
    private javax.swing.JLabel lblContactCaption;
    private javax.swing.JLabel lblContactValue;
    private javax.swing.JLabel lblDeliveryCaption;
    private javax.swing.JLabel lblDeliveryNote;
    private javax.swing.JLabel lblFeeCaption;
    private javax.swing.JLabel lblFeeValue;
    private javax.swing.JLabel lblPaymentCaption;
    private javax.swing.JLabel lblSubtotalCaption;
    private javax.swing.JLabel lblSubtotalValue;
    private javax.swing.JLabel lblSummaryTitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalCaption;
    private javax.swing.JLabel lblTotalValue;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlDeliveryRow;
    private javax.swing.JPanel pnlTotalRow;
    private javax.swing.JRadioButton rbDelivery;
    private javax.swing.JRadioButton rbPickup;
    private javax.swing.JScrollPane scrollSummary;
    private javax.swing.JTable tblSummary;
    private javax.swing.JTextField txtAddress;
    // End of variables declaration//GEN-END:variables
}
