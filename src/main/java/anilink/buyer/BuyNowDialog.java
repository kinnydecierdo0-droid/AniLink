package anilink.buyer;

import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.data.ProductDAO;
import anilink.model.Buyer;
import anilink.model.Payment;
import anilink.model.Product;
import anilink.session.CartItem;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * Buy-now modal: orders a single product without going through the cart.
 *
 * <p>Collects quantity, delivery method, address and payment method, then
 * places the order and records its payment. When G-Cash is selected the
 * {@link GcashPaymentDialog} runs first, so cancelling it saves nothing.
 * Hand-coded rather than a NetBeans form so the designer cannot regenerate it.
 */
public class BuyNowDialog extends JDialog {

    private final MainFrame frame;
    private Product product;

    private final JSpinner spinQty;
    private final JRadioButton rbPickup = new JRadioButton("Pickup");
    private final JRadioButton rbDelivery = new JRadioButton("Delivery");
    private final JTextField txtAddress = new JTextField();
    private final JComboBox<String> cmbPayment = new JComboBox<>();
    /** Placeholder + the three real options; a selection's identity is
     * checked with {@code instanceof} in {@link #selectedVehicle()} since
     * the placeholder shares the combo's generic {@code Object} type. */
    private final JComboBox<Object> cmbVehicle = new JComboBox<>(
            new Object[]{"-- Select vehicle --", anilink.model.VehicleType.MOTORCYCLE,
                anilink.model.VehicleType.MINI_VAN, anilink.model.VehicleType.WING_VAN});
    private final JPanel pnlDeliveryNote = new JPanel(new BorderLayout());
    private final JLabel lblDeliveryNoteText = new JLabel();
    private final JLabel lblSubtotalValue = new JLabel();
    private final JLabel lblFeeCaption = new JLabel("Delivery Fee");
    private final JLabel lblFeeValue = new JLabel();
    private final JLabel lblTotalValue = new JLabel();
    private JPanel pnlFeeRow;
    private final JButton btnPlaceOrder = new JButton("Place Order");

    public BuyNowDialog(MainFrame frame, Product product) {
        super(frame, "Buy Now", ModalityType.APPLICATION_MODAL);
        this.frame = frame;
        this.product = product;

        int maxQty = Math.max(1, product.getQuantity());
        spinQty = new JSpinner(new SpinnerNumberModel(1, 1, maxQty, 1));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UiUtil.BG);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        wireEvents();
        prefillAddress();
        updateTotals();

        setSize(620, 680);
        setLocationRelativeTo(frame);
    }

    // ------------------------------------------------------------------ build

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Buy Now");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Review your order, then choose how you want to receive and pay for it.");
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(new java.awt.Color(220, 237, 221));
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setBackground(UiUtil.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        body.add(buildProductCard());
        body.add(UiUtil.vgap(14));
        body.add(buildOptionsCard());
        body.add(UiUtil.vgap(14));
        body.add(buildTotalsCard());
        body.add(javax.swing.Box.createVerticalGlue());
        return body;
    }

    private JPanel buildProductCard() {
        JPanel card = UiUtil.card();
        card.setLayout(new BorderLayout(12, 0));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel image = new JLabel(UiUtil.loadProductIcon(product.getImage(), 96, 96));
        image.setOpaque(true);
        image.setBackground(UiUtil.TINT);
        image.setPreferredSize(new Dimension(96, 96));
        card.add(image, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(product.getProductName());
        name.setFont(UiUtil.bold(16));
        name.setForeground(UiUtil.DARK);
        info.add(name);
        info.add(UiUtil.vgap(4));

        JLabel price = new JLabel(UiUtil.formatPrice(product.getPrice()) + " / " + product.getUnit());
        price.setFont(UiUtil.bold(14));
        price.setForeground(UiUtil.PRIMARY);
        info.add(price);
        info.add(UiUtil.vgap(4));

        JLabel stock = new JLabel("In stock: " + product.getQuantity() + " " + product.getUnit());
        stock.setFont(UiUtil.plain(12));
        stock.setForeground(UiUtil.MUTED);
        info.add(stock);
        info.add(UiUtil.vgap(4));

        JLabel farmer = new JLabel("Farmer: "
                + (product.getFarmerName() == null ? "-" : product.getFarmerName()));
        farmer.setFont(UiUtil.plain(12));
        farmer.setForeground(UiUtil.MUTED);
        info.add(farmer);

        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildOptionsCard() {
        JPanel card = UiUtil.sectionCard("Order options");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 0, 6, 10);
        gc.gridy = 0;

        // Quantity
        gc.gridx = 0;
        rows.add(fieldLabel("Quantity"), gc);
        spinQty.setPreferredSize(new Dimension(90, 32));
        spinQty.setFont(UiUtil.plain(13));
        gc.gridx = 1;
        rows.add(spinQty, gc);
        gc.gridy++;

        // Delivery method
        ButtonGroup group = new ButtonGroup();
        group.add(rbPickup);
        group.add(rbDelivery);
        rbPickup.setSelected(true);
        for (JRadioButton rb : new JRadioButton[]{rbPickup, rbDelivery}) {
            rb.setOpaque(false);
            rb.setFont(UiUtil.plain(13));
            rb.setFocusPainted(false);
            rb.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
        cmbVehicle.setPreferredSize(new Dimension(170, 28));
        cmbVehicle.setFont(UiUtil.plain(12));
        cmbVehicle.setVisible(false);
        cmbVehicle.addActionListener(e -> onVehicleChanged());

        JPanel methods = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        methods.setOpaque(false);
        methods.add(rbPickup);
        methods.add(rbDelivery);
        methods.add(cmbVehicle);

        gc.gridx = 0;
        rows.add(fieldLabel("Delivery Method"), gc);
        gc.gridx = 1;
        rows.add(methods, gc);
        gc.gridy++;

        // Delivery fee note - hidden until a specific vehicle is chosen, not
        // just as soon as "Delivery" is, since the fee depends on which one.
        pnlDeliveryNote.setBackground(new java.awt.Color(255, 248, 225));
        pnlDeliveryNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UiUtil.ACCENT_DARK),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        lblDeliveryNoteText.setFont(UiUtil.bold(12));
        lblDeliveryNoteText.setForeground(UiUtil.ON_ACCENT);
        pnlDeliveryNote.add(lblDeliveryNoteText, BorderLayout.CENTER);
        pnlDeliveryNote.setVisible(false);

        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        rows.add(pnlDeliveryNote, gc);
        gc.fill = GridBagConstraints.NONE;
        gc.gridy++;

        // Address
        txtAddress.setPreferredSize(new Dimension(320, 32));
        txtAddress.setFont(UiUtil.plain(13));
        txtAddress.setToolTipText("Street, barangay, city / municipality");
        txtAddress.setEnabled(false);
        gc.gridx = 0;
        rows.add(fieldLabel("Delivery Address"), gc);
        gc.gridx = 1;
        rows.add(txtAddress, gc);
        gc.gridy++;

        // Payment method - buyers see "G-Cash Payment"; options narrow to
        // G-Cash only once Delivery is chosen, via updatePaymentOptions().
        updatePaymentOptions();
        cmbPayment.setPreferredSize(new Dimension(220, 32));
        cmbPayment.setFont(UiUtil.plain(13));
        gc.gridx = 0;
        rows.add(fieldLabel("Payment Method"), gc);
        gc.gridx = 1;
        rows.add(cmbPayment, gc);

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTotalsCard() {
        JPanel card = UiUtil.sectionCard("Order total");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        rows.add(totalRow("Subtotal", lblSubtotalValue, false));
        rows.add(UiUtil.vgap(4));
        pnlFeeRow = totalRow(lblFeeCaption, lblFeeValue, false);
        rows.add(pnlFeeRow);
        rows.add(UiUtil.vgap(8));
        JSeparator separator = new JSeparator();
        separator.setForeground(UiUtil.BORDER);
        rows.add(separator);
        rows.add(UiUtil.vgap(8));
        rows.add(totalRow("Total", lblTotalValue, true));

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel totalRow(String caption, JLabel valueLabel, boolean emphasise) {
        JLabel captionLabel = new JLabel(caption);
        return totalRow(captionLabel, valueLabel, emphasise);
    }

    private JPanel totalRow(JLabel captionLabel, JLabel valueLabel, boolean emphasise) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        captionLabel.setFont(emphasise ? UiUtil.bold(15) : UiUtil.plain(13));
        captionLabel.setForeground(emphasise ? UiUtil.PRIMARY : UiUtil.MUTED);
        row.add(captionLabel, BorderLayout.WEST);

        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        valueLabel.setFont(emphasise ? UiUtil.bold(16) : UiUtil.plain(13));
        valueLabel.setForeground(emphasise ? UiUtil.PRIMARY : UiUtil.DARK);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        UiUtil.styleFieldLabel(label);
        return label;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton cancel = new JButton("Cancel");
        UiUtil.styleTintedButton(cancel);
        cancel.setPreferredSize(new Dimension(110, 36));
        cancel.addActionListener(e -> dispose());
        footer.add(cancel);

        UiUtil.stylePrimary(btnPlaceOrder);
        btnPlaceOrder.setPreferredSize(new Dimension(150, 36));
        btnPlaceOrder.addActionListener(e -> placeOrder());
        footer.add(btnPlaceOrder);
        return footer;
    }

    // ----------------------------------------------------------------- events

    private void wireEvents() {
        spinQty.addChangeListener(e -> updateTotals());
        rbPickup.addActionListener(e -> onDeliveryMethodChanged());
        rbDelivery.addActionListener(e -> onDeliveryMethodChanged());
        cmbPayment.addActionListener(e -> updateTotals());
    }

    private void onDeliveryMethodChanged() {
        boolean delivery = rbDelivery.isSelected();
        cmbVehicle.setVisible(delivery);
        if (!delivery) {
            cmbVehicle.setSelectedIndex(0);
        }
        txtAddress.setEnabled(delivery);
        if (!delivery) {
            txtAddress.setBackground(UiUtil.BG);
        } else {
            txtAddress.setBackground(UiUtil.WHITE);
            txtAddress.requestFocus();
        }
        updateDeliveryNote();
        updatePaymentOptions();
        updateTotals();
        revalidate();
        repaint();
    }

    private void onVehicleChanged() {
        updateDeliveryNote();
        updateTotals();
    }

    private void updateDeliveryNote() {
        anilink.model.VehicleType vehicle = selectedVehicle();
        boolean show = rbDelivery.isSelected() && vehicle != null;
        if (show) {
            lblDeliveryNoteText.setText("Note: You selected " + vehicle.getLabel()
                    + " - a delivery fee of " + UiUtil.formatPrice(vehicle.getFee()) + " applies.");
        }
        pnlDeliveryNote.setVisible(show);
    }

    private anilink.model.VehicleType selectedVehicle() {
        Object selected = cmbVehicle.getSelectedItem();
        return selected instanceof anilink.model.VehicleType ? (anilink.model.VehicleType) selected : null;
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

    private void prefillAddress() {
        Buyer buyer = SessionContext.getBuyerProfile();
        if (buyer != null && buyer.getAddress() != null) {
            txtAddress.setText(buyer.getAddress());
        }
    }

    private int quantity() {
        return ((Number) spinQty.getValue()).intValue();
    }

    private double deliveryFee() {
        anilink.model.VehicleType vehicle = selectedVehicle();
        return rbDelivery.isSelected() && vehicle != null ? vehicle.getFee() : 0;
    }

    private void updateTotals() {
        double subtotal = product.getPrice() * quantity();
        double fee = deliveryFee();
        lblSubtotalValue.setText(UiUtil.formatPrice(subtotal));
        lblFeeValue.setText(UiUtil.formatPrice(fee));
        lblTotalValue.setText(UiUtil.formatPrice(subtotal + fee));

        // The fee line only exists for delivery orders.
        if (pnlFeeRow != null) {
            pnlFeeRow.setVisible(fee > 0);
        }
    }

    private String selectedPaymentValue() {
        Object selected = cmbPayment.getSelectedItem();
        return UiUtil.paymentValue(selected == null ? null : selected.toString());
    }

    // ------------------------------------------------------------ place order

    private void placeOrder() {
        if (!SessionContext.isLoggedIn() || !SessionContext.isRole("buyer")) {
            UiUtil.error(this, "Please log in as a buyer to place an order.");
            return;
        }
        int buyerId = SessionContext.getBuyerId();
        if (buyerId <= 0) {
            UiUtil.error(this, "Buyer profile not found. Please re-login.");
            return;
        }
        boolean delivery = rbDelivery.isSelected();
        String address = txtAddress.getText().trim();
        if (delivery && address.isEmpty()) {
            UiUtil.error(this, "Delivery address is required for delivery orders.");
            txtAddress.requestFocus();
            return;
        }
        anilink.model.VehicleType vehicle = selectedVehicle();
        if (delivery && vehicle == null) {
            UiUtil.error(this, "Please select a delivery vehicle.");
            return;
        }
        if (!refreshProduct()) {
            return;
        }

        String paymentMethod = selectedPaymentValue();
        double subtotal = product.getPrice() * quantity();
        double fee = deliveryFee();
        // Delivery is G-Cash-only (see updatePaymentOptions()), so this is
        // every delivery order placed from here - the product subtotal and
        // delivery fee are paid and approved separately: farmer approves the
        // product payment, admin approves the delivery fee. See
        // SplitPaymentDialog for the two-button flow and why the order is
        // created first instead of collecting payment beforehand like the
        // single-payment path below still does.
        boolean splitDelivery = delivery && Payment.METHOD_ONLINE.equals(paymentMethod);

        // Collect the G-Cash details BEFORE anything is written, so cancelling
        // the payment dialog leaves no order behind. Not used for a split
        // delivery order - that payment is collected after the order exists.
        String referenceNo = null;
        String proofImage = null;
        if (Payment.METHOD_ONLINE.equals(paymentMethod) && !splitDelivery) {
            GcashPaymentDialog dialog = new GcashPaymentDialog(this, subtotal, fee);
            dialog.setVisible(true);
            if (!dialog.isConfirmed()) {
                return;
            }
            referenceNo = dialog.getReferenceNo();
            proofImage = dialog.getProofImage();
        }

        btnPlaceOrder.setEnabled(false);
        try {
            List<CartItem> items = List.of(new CartItem(product, quantity()));
            int orderId = new OrderDAO().placeOrder(buyerId, items,
                    delivery ? "delivery" : "pickup", address, paymentMethod, fee,
                    vehicle == null ? null : vehicle.name());

            // placeOrder and the payment-recording calls below each own their
            // own transaction, so a failure here leaves the order unpaid -
            // the same state the cart flow leaves orders in, payable later
            // from My Orders.
            String paymentError = null;
            if (splitDelivery) {
                SplitPaymentDialog splitDialog = new SplitPaymentDialog(
                        this, buyerId, orderId, subtotal, fee);
                splitDialog.setVisible(true);
                if (!splitDialog.isBothPaid()) {
                    // Closed (X or Close) before completing both payments -
                    // SplitPaymentDialog already cancelled the order itself,
                    // so this does NOT proceed to order confirmation.
                    dispose();
                    frame.refreshChrome();
                    UiUtil.info(frame, "Order #" + orderId + " was cancelled - "
                            + "both the product and delivery payments are required.");
                    return;
                }
            } else {
                try {
                    new PaymentDAO().create(buyerId, orderId, paymentMethod, referenceNo, proofImage);
                } catch (SQLException e) {
                    paymentError = e.getMessage();
                }
            }

            dispose();
            frame.refreshChrome();
            frame.showOrderConfirmation(orderId);
            if (paymentError != null) {
                UiUtil.error(frame, "Your order was placed, but the payment could not be "
                        + "recorded:\n" + paymentError
                        + "\n\nYou can submit it again from My Orders.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, e.getMessage() != null && !e.getMessage().isEmpty()
                    ? e.getMessage() : "Failed to place order. Please try again.");
        } finally {
            btnPlaceOrder.setEnabled(true);
        }
    }

    /** Re-reads the product so stock and price are current before ordering. */
    private boolean refreshProduct() {
        try {
            Optional<Product> found = new ProductDAO().findById(product.getId());
            if (found.isEmpty()) {
                UiUtil.error(this, "This product is no longer available.");
                return false;
            }
            Product fresh = found.get();
            if (!"available".equals(fresh.getStatus()) || fresh.getQuantity() <= 0) {
                UiUtil.error(this, "This product is out of stock.");
                return false;
            }
            if (quantity() > fresh.getQuantity()) {
                UiUtil.error(this, "Only " + fresh.getQuantity() + " " + fresh.getUnit()
                        + " available. Please lower your quantity.");
                product = fresh;
                ((SpinnerNumberModel) spinQty.getModel()).setMaximum(fresh.getQuantity());
                updateTotals();
                return false;
            }
            product = fresh;
            return true;
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to verify the product: " + e.getMessage());
            return false;
        }
    }
}
