package anilink.buyer;

import anilink.data.DeliveryPaymentDAO;
import anilink.data.OrderDAO;
import anilink.data.PaymentDAO;
import anilink.model.Payment;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Shown right after a delivery+G-Cash order is placed: the buyer must pay
 * the delivery fee and the product subtotal as two separate G-Cash
 * payments, reviewed independently (admin approves the fee, the farmer
 * approves the product payment - see {@link anilink.data.DeliveryPaymentDAO}
 * and {@link anilink.data.PaymentDAO#findDeliveryProductGcashForFarmer}).
 *
 * <p>The order already exists by the time this dialog opens (created first,
 * same as every other order type), so each "Pay for..." button writes its
 * payment straight to the database the moment it's confirmed - there is no
 * batched "Done" step. Closing this dialog (the Close button OR the window's
 * own X) before both are paid CANCELS the order - the buyer explicitly does
 * not want a half-paid order silently proceeding, so unlike the app's usual
 * "you can complete payment later" pattern for a single failed payment, this
 * one asks for confirmation and then marks the order {@code cancelled}
 * rather than leaving it pending. {@link #isBothPaid()} tells the caller
 * (BuyNowDialog) whether to show the order confirmation screen at all.
 */
public class SplitPaymentDialog extends JDialog {

    private final int buyerId;
    private final int orderId;
    private final double productAmount;
    private final double deliveryAmount;

    private final JButton btnPayProduct = new JButton();
    private final JButton btnPayDelivery = new JButton();
    private final JLabel lblProductStatus = new JLabel();
    private final JLabel lblDeliveryStatus = new JLabel();

    private boolean productPaid;
    private boolean deliveryPaid;

    public SplitPaymentDialog(Window owner, int buyerId, int orderId,
            double productAmount, double deliveryAmount) {
        super(owner, "Complete Your Payment", ModalityType.APPLICATION_MODAL);
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.productAmount = productAmount;
        this.deliveryAmount = deliveryAmount;

        // DO_NOTHING + a windowClosing handler, so the title bar's X runs the
        // exact same "cancel the order if not both paid" confirmation as the
        // Close button instead of silently disposing.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptClose();
            }
        });
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(480, 420);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Order #" + orderId + " - Two Payments Required");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Pay the product and delivery fee separately - "
                + "the farmer approves the product payment, admin approves the delivery fee.");
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

        body.add(buildPaymentRow("Product Payment",
                "Reviewed and approved by the farmer.", productAmount,
                btnPayProduct, lblProductStatus, this::payProduct));
        body.add(UiUtil.vgap(14));
        body.add(buildPaymentRow("Delivery Fee",
                "Reviewed and approved by admin.", deliveryAmount,
                btnPayDelivery, lblDeliveryStatus, this::payDelivery));
        body.add(javax.swing.Box.createVerticalGlue());

        updateButtons();
        return body;
    }

    private JPanel buildPaymentRow(String title, String note, double amount,
            JButton button, JLabel statusLabel, Runnable onClick) {
        JPanel card = UiUtil.sectionCard(title);
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel inner = new JPanel(new BorderLayout(12, 6));
        inner.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel amountLabel = new JLabel(UiUtil.formatPrice(amount));
        amountLabel.setFont(UiUtil.bold(18));
        amountLabel.setForeground(UiUtil.PRIMARY);
        left.add(amountLabel);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(UiUtil.plain(11));
        noteLabel.setForeground(UiUtil.MUTED);
        left.add(noteLabel);
        statusLabel.setFont(UiUtil.bold(11));
        statusLabel.setForeground(UiUtil.PRIMARY);
        left.add(statusLabel);
        inner.add(left, BorderLayout.WEST);

        UiUtil.stylePrimary(button);
        button.setPreferredSize(new Dimension(150, 36));
        button.addActionListener(e -> onClick.run());
        inner.add(button, BorderLayout.EAST);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void payProduct() {
        GcashPaymentDialog dialog = new GcashPaymentDialog(
                this, "Pay for Product", "Product Payment", productAmount);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        try {
            new PaymentDAO().create(buyerId, orderId, Payment.METHOD_ONLINE,
                    dialog.getReferenceNo(), dialog.getProofImage());
            productPaid = true;
            updateButtons();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not record the product payment: " + e.getMessage());
        }
    }

    private void payDelivery() {
        GcashPaymentDialog dialog = new GcashPaymentDialog(
                this, "Pay for Delivery", "Delivery Fee", deliveryAmount);
        dialog.setVisible(true);
        if (!dialog.isConfirmed()) {
            return;
        }
        try {
            new DeliveryPaymentDAO().create(orderId,
                    dialog.getReferenceNo(), dialog.getProofImage(), deliveryAmount);
            deliveryPaid = true;
            updateButtons();
        } catch (SQLException e) {
            UiUtil.error(this, "Could not record the delivery payment: " + e.getMessage());
        }
    }

    private void updateButtons() {
        btnPayProduct.setText(productPaid ? "Paid" : "Pay for Product");
        btnPayProduct.setEnabled(!productPaid);
        lblProductStatus.setText(productPaid ? "Submitted - awaiting farmer approval" : "Not paid yet");

        btnPayDelivery.setText(deliveryPaid ? "Paid" : "Pay for Delivery");
        btnPayDelivery.setEnabled(!deliveryPaid);
        lblDeliveryStatus.setText(deliveryPaid ? "Submitted - awaiting admin approval" : "Not paid yet");
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton close = new JButton("Close");
        UiUtil.styleTintedButton(close);
        close.setPreferredSize(new Dimension(110, 36));
        close.addActionListener(e -> attemptClose());
        footer.add(close);
        return footer;
    }

    /** Shared by the Close button and the window's own X - closing before
     * both payments are done cancels the order instead of leaving it pending. */
    private void attemptClose() {
        if (bothPaid()) {
            dispose();
            return;
        }
        if (!UiUtil.confirm(this,
                "You haven't completed both payments yet.\n\n"
                + "Closing now will cancel this order - you'll need to place "
                + "it again if you still want these items.\n"
                + "Close and cancel the order?")) {
            return;
        }
        try {
            new OrderDAO().updateStatus(orderId, "cancelled");
        } catch (SQLException e) {
            UiUtil.error(this, "Could not cancel the order: " + e.getMessage());
        }
        dispose();
    }

    private boolean bothPaid() {
        return productPaid && deliveryPaid;
    }

    /** True once both the product and delivery payments were submitted -
     * false means the order was cancelled (see {@link #attemptClose()}). */
    public boolean isBothPaid() {
        return bothPaid();
    }
}
