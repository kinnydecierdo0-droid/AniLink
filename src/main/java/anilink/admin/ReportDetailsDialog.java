package anilink.admin;

import anilink.data.ReportDAO;
import anilink.data.UserDAO;
import anilink.data.WarningDAO;
import anilink.model.Report;
import anilink.model.User;
import anilink.session.SessionContext;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * The admin's review of one buyer-filed report: who reported whom, why, the
 * free-text details, and the ability to move it through Open -> Reviewed ->
 * Resolved. Hand-coded rather than a NetBeans form so the designer cannot
 * regenerate it, matching {@code PaymentDetailsDialog}.
 */
public class ReportDetailsDialog extends JDialog {

    private final Report report;
    private User targetUser;
    private boolean changed;

    public ReportDetailsDialog(Window owner, Report report) {
        super(owner, "Report Details", ModalityType.APPLICATION_MODAL);
        this.report = report;
        try {
            targetUser = new ReportDAO().resolveTargetUser(report.getTargetType(), report.getTargetId())
                    .orElse(null);
        } catch (SQLException e) {
            UiUtil.error(owner, "Could not load the reported account: " + e.getMessage());
        }

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

        setSize(500, 560);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.DANGER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Order #" + report.getOrderId());
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Reported by " + blank(report.getReporterName()));
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(UiUtil.WHITE);
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
        body.add(buildAccountCard());
        body.add(UiUtil.vgap(14));
        body.add(buildDetailsTextCard());
        body.add(UiUtil.vgap(14));
        body.add(buildProofCard());
        body.add(javax.swing.Box.createVerticalGlue());
        return body;
    }

    private JPanel buildDetailCard() {
        JPanel card = UiUtil.sectionCard("Report");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 0, 4, 12);
        gc.gridy = 0;

        row(rows, gc, "Reported party", capitalize(report.getTargetType()) + ": " + blank(report.getTargetName()));
        row(rows, gc, "Reason", ReportDAO.reasonLabel(report.getReason()));
        row(rows, gc, "Status", ReportDAO.statusLabel(report.getStatus()));
        row(rows, gc, "Filed", UiUtil.formatDateTime(report.getCreatedAt()));

        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    /** The reported party's live account state, with Disable/Enable/Warn acting on it directly. */
    private JPanel buildAccountCard() {
        JPanel card = UiUtil.sectionCard("Account");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel status = new JLabel(targetUser == null
                ? "Account status unavailable."
                : "Account status: " + (targetUser.isActive() ? "Enabled" : "Disabled"));
        status.setFont(UiUtil.bold(13));
        status.setForeground(targetUser != null && !targetUser.isActive() ? UiUtil.DANGER : UiUtil.DARK);
        content.add(status);

        if (targetUser != null && !targetUser.isActive()
                && targetUser.getDisabledReason() != null && !targetUser.getDisabledReason().isEmpty()) {
            JLabel reasonLabel = new JLabel(UiUtil.wrapHtml("Reason: " + targetUser.getDisabledReason(), 380));
            reasonLabel.setFont(UiUtil.plain(12));
            reasonLabel.setForeground(UiUtil.MUTED);
            content.add(reasonLabel);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttons.setOpaque(false);

        JButton disable = new JButton("Disable");
        UiUtil.styleButton(disable);
        disable.setEnabled(targetUser != null && targetUser.isActive());
        disable.addActionListener(e -> disableTarget());
        buttons.add(disable);

        JButton enable = new JButton("Enable");
        UiUtil.stylePrimary(enable);
        enable.setEnabled(targetUser != null && !targetUser.isActive());
        enable.addActionListener(e -> enableTarget());
        buttons.add(enable);

        JButton warn = new JButton("Warn");
        UiUtil.styleTintedButton(warn);
        warn.setEnabled(targetUser != null);
        warn.addActionListener(e -> warnTarget());
        buttons.add(warn);

        content.add(buttons);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private void disableTarget() {
        User current = SessionContext.getCurrentUser();
        if (current != null && current.getId() == targetUser.getId()) {
            UiUtil.error(this, "You cannot disable your own account.");
            return;
        }
        String reason = UiUtil.promptText(this, "Disable Account", "Reason for disabling this account:");
        if (reason == null) {
            return;
        }
        if (!UiUtil.confirm(this, "Disable this account? They will no longer be able to log in.")) {
            return;
        }
        try {
            new UserDAO().setActive(targetUser.getId(), false, reason);
            changed = true;
            UiUtil.info(this, "Account disabled.");
            dispose();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void enableTarget() {
        if (!UiUtil.confirm(this, "Enable this account? They will be able to log in again.")) {
            return;
        }
        try {
            new UserDAO().setActive(targetUser.getId(), true, null);
            changed = true;
            UiUtil.info(this, "Account enabled.");
            dispose();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void warnTarget() {
        String reason = UiUtil.promptText(this, "Warn Account", "Reason for this warning:");
        if (reason == null) {
            return;
        }
        try {
            new WarningDAO().insert(targetUser.getId(), reason);
            changed = true;
            UiUtil.info(this, "Warning recorded. The user will see it next time they log in.");
            dispose();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private JPanel buildDetailsTextCard() {
        JPanel card = UiUtil.sectionCard("Details");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        String text = report.getDetails();
        JTextArea area = new JTextArea(text == null || text.isEmpty() ? "No additional details provided." : text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(UiUtil.plain(13));
        area.setForeground(UiUtil.DARK);
        card.add(area, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildProofCard() {
        JPanel card = UiUtil.sectionCard("Proof");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        String proof = report.getProofImage();
        if (proof == null || proof.isEmpty()) {
            JLabel none = new JLabel("No proof was attached to this report.");
            none.setFont(UiUtil.plain(13));
            none.setForeground(UiUtil.MUTED);
            card.add(none, BorderLayout.CENTER);
            return card;
        }

        JLabel image = new JLabel(UiUtil.loadProductIcon(proof, 300, 300), SwingConstants.CENTER);
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

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton close = new JButton("Close");
        UiUtil.styleTintedButton(close);
        close.setPreferredSize(new Dimension(100, 36));
        close.addActionListener(e -> dispose());
        footer.add(close);

        JButton reviewed = new JButton("Mark Reviewed");
        UiUtil.styleButton(reviewed);
        reviewed.setPreferredSize(new Dimension(150, 36));
        reviewed.setEnabled(!Report.STATUS_REVIEWED.equals(report.getStatus()));
        reviewed.addActionListener(e -> updateStatus(Report.STATUS_REVIEWED));
        footer.add(reviewed);

        JButton resolved = new JButton("Mark Resolved");
        UiUtil.stylePrimary(resolved);
        resolved.setPreferredSize(new Dimension(150, 36));
        resolved.setEnabled(!Report.STATUS_RESOLVED.equals(report.getStatus()));
        resolved.addActionListener(e -> updateStatus(Report.STATUS_RESOLVED));
        footer.add(resolved);
        return footer;
    }

    private void updateStatus(String status) {
        if (!UiUtil.confirm(this, "Mark this report as " + ReportDAO.statusLabel(status) + "?")) {
            return;
        }
        try {
            if (new ReportDAO().updateStatus(report.getId(), status)) {
                report.setStatus(status);
                changed = true;
                UiUtil.info(this, "Report marked as " + ReportDAO.statusLabel(status) + ".");
                dispose();
            } else {
                UiUtil.error(this, "Report not found.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private static String blank(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "-";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /** True when the report's status was changed, so the caller can refresh. */
    public boolean isChanged() {
        return changed;
    }
}
