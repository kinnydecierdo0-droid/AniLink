package anilink.buyer;

import anilink.data.PaymentDAO;
import anilink.model.PaymentSettings;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The G-Cash payment step. Shows the admin-configured provider details and the
 * amount breakdown, and collects a reference number plus an optional receipt
 * image.
 *
 * <p>Deliberately does not write to the database: the caller places the order
 * first and then records the payment, so cancelling here leaves nothing behind.
 * Hand-coded rather than a NetBeans form so the designer cannot regenerate it.
 */
public class GcashPaymentDialog extends JDialog {

    private final double subtotal;
    private final double deliveryFee;
    private final String headerTitle;
    /** Non-null switches the amount card to a single labeled row instead of
     * the Subtotal/Delivery Fee breakdown - used by the delivery-order
     * split-payment flow, where each dialog collects just one portion. */
    private final String singleAmountLabel;

    private final JTextField txtReference = new JTextField();
    private final JLabel lblProofName = new JLabel("No receipt attached yet.");
    private final JLabel lblProofPreview = new JLabel();
    private final JButton btnConfirm = new JButton("Confirm Payment");

    private String proofImage;
    private boolean confirmed;

    public GcashPaymentDialog(Window owner, double subtotal, double deliveryFee) {
        this(owner, "Pay with G-Cash", subtotal, deliveryFee, null);
    }

    /** For collecting just one labeled amount (e.g. "Delivery Fee" or
     * "Product Payment") - the delivery-order split-payment flow. */
    public GcashPaymentDialog(Window owner, String headerTitle, String amountLabel, double amount) {
        this(owner, headerTitle, amount, 0, amountLabel);
    }

    private GcashPaymentDialog(Window owner, String headerTitle, double subtotal,
            double deliveryFee, String singleAmountLabel) {
        super(owner, "G-Cash Payment", ModalityType.APPLICATION_MODAL);
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.headerTitle = headerTitle;
        this.singleAmountLabel = singleAmountLabel;

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

        setSize(520, 660);
        setLocationRelativeTo(owner);
        txtReference.requestFocusInWindow();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel(headerTitle);
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Send the exact amount, then enter your reference number below.");
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

        body.add(buildProviderCard());
        body.add(UiUtil.vgap(14));
        body.add(buildAmountCard());
        body.add(UiUtil.vgap(14));
        body.add(buildReferenceCard());
        body.add(UiUtil.vgap(14));
        body.add(buildProofCard());
        body.add(javax.swing.Box.createVerticalGlue());
        return body;
    }

    private JPanel buildProviderCard() {
        PaymentSettings settings = new PaymentDAO().findSettings();

        JPanel card = UiUtil.sectionCard("Send payment to");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(3, 0, 3, 10);
        gc.gridy = 0;

        addDetailRow(rows, gc, "Provider", blankToDash(settings.getProvider()));
        addDetailRow(rows, gc, "Account Name", blankToDash(settings.getAccountName()));
        addDetailRow(rows, gc, "Account Number", blankToDash(settings.getAccountNumber()));

        card.add(rows, BorderLayout.CENTER);

        String instructions = settings.getInstructions();
        if (instructions != null && !instructions.trim().isEmpty()) {
            JLabel note = new JLabel(UiUtil.wrapHtml(instructions, 420));
            note.setFont(UiUtil.plain(12));
            note.setForeground(UiUtil.MUTED);
            note.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            card.add(note, BorderLayout.SOUTH);
        }
        return card;
    }

    private void addDetailRow(JPanel rows, GridBagConstraints gc, String caption, String value) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(UiUtil.bold(12));
        captionLabel.setForeground(UiUtil.MUTED);
        gc.gridx = 0;
        rows.add(captionLabel, gc);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UiUtil.bold(13));
        valueLabel.setForeground(UiUtil.DARK);
        gc.gridx = 1;
        rows.add(valueLabel, gc);
        gc.gridy++;
    }

    private JPanel buildAmountCard() {
        JPanel card = UiUtil.sectionCard("Amount breakdown");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        if (singleAmountLabel != null) {
            rows.add(amountRow(singleAmountLabel, UiUtil.formatPrice(subtotal), true));
        } else {
            rows.add(amountRow("Subtotal", UiUtil.formatPrice(subtotal), false));
            // The fee row exists only when the buyer actually chose Delivery.
            if (deliveryFee > 0) {
                rows.add(UiUtil.vgap(4));
                rows.add(amountRow("Delivery Fee", UiUtil.formatPrice(deliveryFee), false));
            }
            rows.add(UiUtil.vgap(8));
            JSeparator separator = new JSeparator();
            separator.setForeground(UiUtil.BORDER);
            rows.add(separator);
            rows.add(UiUtil.vgap(8));
            rows.add(amountRow("Amount to Pay", UiUtil.formatPrice(subtotal + deliveryFee), true));
        }

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    private JPanel amountRow(String caption, String value, boolean emphasise) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(emphasise ? UiUtil.bold(15) : UiUtil.plain(13));
        captionLabel.setForeground(emphasise ? UiUtil.PRIMARY : UiUtil.MUTED);
        row.add(captionLabel, BorderLayout.WEST);

        JLabel valueLabel = new JLabel(value, SwingConstants.RIGHT);
        valueLabel.setFont(emphasise ? UiUtil.bold(16) : UiUtil.plain(13));
        valueLabel.setForeground(emphasise ? UiUtil.PRIMARY : UiUtil.DARK);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildReferenceCard() {
        JPanel card = UiUtil.sectionCard("Reference number");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel inner = new JPanel(new BorderLayout(0, 6));
        inner.setOpaque(false);

        JLabel hint = new JLabel("The reference number shown in your G-Cash receipt.");
        hint.setFont(UiUtil.plain(12));
        hint.setForeground(UiUtil.MUTED);
        inner.add(hint, BorderLayout.NORTH);

        txtReference.setFont(UiUtil.plain(14));
        txtReference.setPreferredSize(new Dimension(300, 34));
        txtReference.setToolTipText("Required - e.g. 0123456789012");
        inner.add(txtReference, BorderLayout.CENTER);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildProofCard() {
        JPanel card = UiUtil.sectionCard("Proof of receipt");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel inner = new JPanel(new BorderLayout(0, 8));
        inner.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        JButton choose = new JButton("Choose Image...");
        UiUtil.styleGhost(choose);
        choose.addActionListener(e -> chooseProof());
        top.add(choose);

        lblProofName.setFont(UiUtil.plain(12));
        lblProofName.setForeground(UiUtil.MUTED);
        top.add(lblProofName);
        inner.add(top, BorderLayout.NORTH);

        lblProofPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblProofPreview.setPreferredSize(new Dimension(160, 160));
        lblProofPreview.setBorder(BorderFactory.createDashedBorder(UiUtil.BORDER, 5, 3));
        lblProofPreview.setIcon(UiUtil.placeholderIcon(156, 156));
        inner.add(lblProofPreview, BorderLayout.CENTER);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void chooseProof() {
        String stored = UiUtil.chooseAndStoreImage(this, "proof_");
        if (stored == null) {
            return;
        }
        proofImage = stored;
        lblProofName.setText(stored);
        lblProofName.setForeground(UiUtil.PRIMARY);
        lblProofPreview.setIcon(UiUtil.loadProductIcon(stored, 156, 156));
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton cancel = new JButton("Cancel");
        UiUtil.styleTintedButton(cancel);
        cancel.setPreferredSize(new Dimension(110, 36));
        cancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        footer.add(cancel);

        UiUtil.stylePrimary(btnConfirm);
        btnConfirm.setPreferredSize(new Dimension(170, 36));
        btnConfirm.addActionListener(e -> confirm());
        footer.add(btnConfirm);
        return footer;
    }

    private void confirm() {
        String reference = txtReference.getText().trim();
        if (reference.isEmpty()) {
            UiUtil.error(this, "Please enter your G-Cash reference number.");
            txtReference.requestFocus();
            return;
        }
        // payments.proof_image is nullable and PaymentDAO only requires the
        // reference, so the receipt is encouraged rather than mandatory.
        if (proofImage == null && !UiUtil.confirm(this,
                "You have not attached a receipt image.\n\n"
                + "Attaching one helps the admin verify your payment faster.\n"
                + "Continue without it?")) {
            return;
        }
        confirmed = true;
        dispose();
    }

    private static String blankToDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getReferenceNo() {
        return txtReference.getText().trim();
    }

    public String getProofImage() {
        return proofImage;
    }
}
