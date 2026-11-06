package anilink.buyer;

import anilink.data.ReportDAO;
import anilink.model.Order;
import anilink.model.Report;
import anilink.model.ReportTarget;
import anilink.session.SessionContext;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * Lets a buyer file a complaint against the farmer or rider on one of their
 * orders, or (via {@link #forFarmer}) a farmer file one against the buyer -
 * either way, for admin to review on the User Report page's Reports tab.
 */
public class ReportDialog extends JDialog {

    /** The buyer-filed reasons used when reporting a farmer or rider. */
    private static final String[] BUYER_REASONS = {
        "Received damaged", "Received incorrect", "Did not receive", "Others"
    };

    /** The farmer-filed reasons used when reporting a buyer. */
    private static final String[] FARMER_REASONS = {
        "Did not pay", "Did not pick up order", "Abusive behavior", "Others"
    };

    private final Order order;
    private final String reporterType;
    private final int reporterId;
    private final JComboBox<ReportTarget> cmbTarget = new JComboBox<>();
    private final JComboBox<String> cmbReason;
    private final JTextArea txtDetails = new JTextArea(4, 28);
    private final JLabel lblProofName = new JLabel("No proof attached yet.");
    private final JLabel lblProofPreview = new JLabel();
    private String proofImage;
    private boolean submitted;

    /** Convenience for the buyer flow - reports a farmer/rider as the signed-in buyer. */
    public ReportDialog(Window owner, Order order, List<ReportTarget> targets) {
        this(owner, order, targets, BUYER_REASONS, Report.REPORTER_BUYER, SessionContext.getBuyerId());
    }

    /** Convenience for the farmer flow - reports the buyer on this order as the signed-in farmer. */
    public static ReportDialog forFarmer(Window owner, Order order, List<ReportTarget> targets) {
        return new ReportDialog(owner, order, targets,
                FARMER_REASONS, Report.REPORTER_FARMER, SessionContext.getFarmerId());
    }

    public ReportDialog(Window owner, Order order, List<ReportTarget> targets,
            String[] reasons, String reporterType, int reporterId) {
        super(owner, "Report an Issue", ModalityType.APPLICATION_MODAL);
        this.order = order;
        this.reporterType = reporterType;
        this.reporterId = reporterId;
        this.cmbReason = new JComboBox<>(reasons);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane bodyScroll = new JScrollPane(buildBody(targets));
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
        bodyScroll.getViewport().setBackground(UiUtil.BG);
        add(bodyScroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(460, 560);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.DANGER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Report an Issue");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Order #" + order.getId());
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(UiUtil.WHITE);
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody(List<ReportTarget> targets) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiUtil.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel card = UiUtil.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel targetCaption = new JLabel("Who are you reporting?");
        UiUtil.styleFieldLabel(targetCaption);
        targetCaption.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(targetCaption);
        card.add(UiUtil.vgap(6));

        for (ReportTarget t : targets) {
            cmbTarget.addItem(t);
        }
        cmbTarget.setFont(UiUtil.plain(13));
        cmbTarget.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        cmbTarget.setMaximumSize(new Dimension(400, 34));
        cmbTarget.setPreferredSize(new Dimension(400, 34));
        card.add(cmbTarget);
        card.add(UiUtil.vgap(14));

        JLabel reasonCaption = new JLabel("Reason");
        UiUtil.styleFieldLabel(reasonCaption);
        reasonCaption.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(reasonCaption);
        card.add(UiUtil.vgap(6));

        cmbReason.setFont(UiUtil.plain(13));
        cmbReason.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        cmbReason.setMaximumSize(new Dimension(400, 34));
        cmbReason.setPreferredSize(new Dimension(400, 34));
        card.add(cmbReason);
        card.add(UiUtil.vgap(14));

        JLabel proofCaption = new JLabel("Proof (optional)");
        UiUtil.styleFieldLabel(proofCaption);
        proofCaption.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(proofCaption);
        card.add(UiUtil.vgap(6));

        JPanel proofRow = new JPanel(new BorderLayout(10, 0));
        proofRow.setOpaque(false);
        proofRow.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        proofRow.setMaximumSize(new Dimension(400, 24));

        JButton chooseProof = new JButton("Choose Image");
        UiUtil.styleTintedButton(chooseProof);
        chooseProof.addActionListener(e -> chooseProof());
        proofRow.add(chooseProof, BorderLayout.WEST);

        lblProofName.setFont(UiUtil.plain(12));
        lblProofName.setForeground(UiUtil.MUTED);
        proofRow.add(lblProofName, BorderLayout.CENTER);
        card.add(proofRow);
        card.add(UiUtil.vgap(10));

        lblProofPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblProofPreview.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        lblProofPreview.setPreferredSize(new Dimension(140, 140));
        lblProofPreview.setMaximumSize(new Dimension(140, 140));
        lblProofPreview.setBorder(BorderFactory.createDashedBorder(UiUtil.BORDER, 5, 3));
        lblProofPreview.setIcon(UiUtil.placeholderIcon(136, 136));
        card.add(lblProofPreview);
        card.add(UiUtil.vgap(14));

        JLabel detailsCaption = new JLabel("Details (optional)");
        UiUtil.styleFieldLabel(detailsCaption);
        detailsCaption.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(detailsCaption);
        card.add(UiUtil.vgap(6));

        txtDetails.setFont(UiUtil.plain(13));
        txtDetails.setLineWrap(true);
        txtDetails.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtDetails);
        scroll.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(400, 110));
        scroll.setPreferredSize(new Dimension(400, 110));
        card.add(scroll);

        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private void chooseProof() {
        String stored = UiUtil.chooseAndStoreImage(this, "proof_");
        if (stored == null) {
            return;
        }
        proofImage = stored;
        lblProofName.setText(stored);
        lblProofName.setForeground(UiUtil.PRIMARY);
        lblProofPreview.setIcon(UiUtil.loadProductIcon(stored, 136, 136));
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

        JButton submit = new JButton("Submit Report");
        UiUtil.stylePrimary(submit);
        submit.setPreferredSize(new Dimension(150, 36));
        submit.addActionListener(e -> submit());
        footer.add(submit);
        return footer;
    }

    private void submit() {
        ReportTarget target = (ReportTarget) cmbTarget.getSelectedItem();
        if (target == null) {
            UiUtil.error(this, "Please select who you are reporting.");
            return;
        }
        String reasonLabel = (String) cmbReason.getSelectedItem();
        if (proofImage == null && !UiUtil.confirm(this,
                "You have not attached proof.\n\n"
                + "Attaching a photo helps admin review your report faster.\n"
                + "Continue without it?")) {
            return;
        }
        if (!UiUtil.confirm(this, "Submit this report against " + target + "?")) {
            return;
        }
        Report r = new Report();
        r.setOrderId(order.getId());
        r.setReporterId(reporterId);
        r.setReporterType(reporterType);
        r.setTargetType(target.getType());
        r.setTargetId(target.getId());
        r.setReason(ReportDAO.reasonCode(reasonLabel));
        r.setDetails(txtDetails.getText().trim());
        r.setProofImage(proofImage);
        try {
            new ReportDAO().insert(r);
            submitted = true;
            UiUtil.info(this, "Thank you - your report has been submitted for review.");
            dispose();
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to submit report: " + e.getMessage());
        }
    }

    /** True once the report was successfully saved, so the caller can refresh. */
    public boolean isSubmitted() {
        return submitted;
    }
}
