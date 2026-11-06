package anilink.admin;

import anilink.data.DeliveryPaymentDAO;
import anilink.data.PaymentDAO;
import anilink.model.Payment;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Admin's review of one delivery order's DELIVERY-FEE payment specifically
 * (the sibling of {@link PaymentDetailsDialog}, which the farmer now uses
 * for the same order's product-subtotal payment instead). Structurally
 * identical to {@code PaymentDetailsDialog} - kept as its own class rather
 * than merged into it so the existing, already-working dialog is never
 * touched by this change.
 */
public class DeliveryPaymentDetailsDialog extends JDialog {

    private final Payment payment;
    private boolean changed;

    public DeliveryPaymentDetailsDialog(Window owner, Payment payment) {
        super(owner, "Delivery Fee Payment Details", ModalityType.APPLICATION_MODAL);
        this.payment = payment;

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

        setSize(560, 680);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Order #" + payment.getOrderId() + " - Delivery Fee");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("This is the delivery-fee portion only - the product "
                + "payment is reviewed separately by the farmer.");
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

        body.add(buildDetailCard());
        body.add(UiUtil.vgap(14));
        body.add(buildProofCard());
        body.add(javax.swing.Box.createVerticalGlue());
        return body;
    }

    private JPanel buildDetailCard() {
        JPanel card = UiUtil.sectionCard("Delivery Fee Payment");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 0, 4, 12);
        gc.gridy = 0;

        row(rows, gc, "Customer", blank(payment.getBuyerName()));
        row(rows, gc, "Contact", blank(payment.getBuyerContact()));
        row(rows, gc, "Items", blank(payment.getItemsSummary()));
        row(rows, gc, "Delivery fee", UiUtil.formatPrice(payment.getDeliveryFee()));
        row(rows, gc, "Method", UiUtil.paymentLabel(payment.getPaymentMethod()));
        row(rows, gc, "Reference number", blank(payment.getReferenceNo()));
        row(rows, gc, "Payment status", PaymentDAO.statusLabel(
                payment.getId() > 0 ? payment.getStatus() : null));
        row(rows, gc, "Submitted", UiUtil.formatDateTime(payment.getSubmittedAt()));
        row(rows, gc, "Delivery man", blank(payment.getRiderName()));

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private void row(JPanel rows, GridBagConstraints gc, String caption, String value) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(UiUtil.bold(12));
        captionLabel.setForeground(UiUtil.MUTED);
        gc.gridx = 0;
        rows.add(captionLabel, gc);

        JLabel valueLabel = new JLabel(UiUtil.wrapHtml(value, 300));
        valueLabel.setFont(UiUtil.plain(13));
        valueLabel.setForeground(UiUtil.DARK);
        gc.gridx = 1;
        rows.add(valueLabel, gc);
        gc.gridy++;
    }

    private JPanel buildProofCard() {
        JPanel card = UiUtil.sectionCard("Proof of receipt");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        String proof = payment.getProofImage();
        if (proof == null || proof.isEmpty()) {
            JLabel none = new JLabel("No receipt was attached to this payment.");
            none.setFont(UiUtil.plain(13));
            none.setForeground(UiUtil.MUTED);
            card.add(none, BorderLayout.CENTER);
            return card;
        }

        JLabel image = new JLabel(UiUtil.loadProductIcon(proof, 420, 420),
                SwingConstants.CENTER);
        image.setOpaque(true);
        image.setBackground(UiUtil.TINT);
        image.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, 0));
        card.add(image, BorderLayout.CENTER);

        JLabel name = new JLabel(proof);
        name.setFont(UiUtil.plain(11));
        name.setForeground(UiUtil.MUTED);
        name.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        card.add(name, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton close = new JButton("Close");
        UiUtil.styleTintedButton(close);
        close.setPreferredSize(new Dimension(100, 36));
        close.addActionListener(e -> dispose());
        footer.add(close);

        boolean reviewable = payment.getId() > 0;

        JButton reject = new JButton("Reject Payment");
        UiUtil.styleDanger(reject);
        reject.setPreferredSize(new Dimension(160, 36));
        reject.setEnabled(reviewable);
        if (!reviewable) {
            reject.setToolTipText("This order has no delivery payment submission yet.");
        }
        reject.addActionListener(e -> review(Payment.STATUS_REJECTED, "Reject"));
        footer.add(reject);

        JButton approve = new JButton("Approve Payment");
        UiUtil.stylePrimary(approve);
        approve.setPreferredSize(new Dimension(170, 36));
        approve.setEnabled(reviewable);
        if (!reviewable) {
            approve.setToolTipText("This order has no delivery payment submission yet.");
        }
        approve.addActionListener(e -> review(Payment.STATUS_VERIFIED, "Approve"));
        footer.add(approve);
        return footer;
    }

    private void review(String status, String verb) {
        if (!UiUtil.confirm(this, verb + " the delivery fee payment for order #"
                + payment.getOrderId() + "?")) {
            return;
        }
        try {
            if (new DeliveryPaymentDAO().updateStatus(payment.getOrderId(), status)) {
                payment.setStatus(status);
                changed = true;
                UiUtil.info(this, "Delivery fee payment " + PaymentDAO.statusLabel(status).toLowerCase()
                        + " for order #" + payment.getOrderId() + ".");
                dispose();
            } else {
                UiUtil.error(this, "No delivery payment record found for order #"
                        + payment.getOrderId() + ".");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private static String blank(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    /** True when the payment status was changed, so the caller can refresh. */
    public boolean isChanged() {
        return changed;
    }
}
