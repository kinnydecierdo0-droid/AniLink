package anilink.auth;

import anilink.data.AppealDAO;
import anilink.model.Appeal;
import anilink.model.User;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Shown when a disabled account attempts to log in: explains why (the
 * admin's stored reason) and, unless an appeal is already pending, lets the
 * user submit one for the admin to review.
 */
public class AccountDisabledDialog extends JDialog {

    private final User user;
    private final JPanel body = new JPanel();

    public AccountDisabledDialog(Window owner, User user) {
        super(owner, "Account Disabled", ModalityType.APPLICATION_MODAL);
        this.user = user;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        body.setBackground(UiUtil.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        rebuildBody();
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UiUtil.BG);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(440, 380);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.DANGER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Account Disabled");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.CENTER);
        return header;
    }

    private void rebuildBody() {
        body.removeAll();

        JPanel reasonCard = UiUtil.sectionCard("Reason");
        reasonCard.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        String reason = user.getDisabledReason();
        JLabel reasonLabel = new JLabel(UiUtil.wrapHtml(
                reason == null || reason.isEmpty() ? "No reason was provided." : reason, 360));
        reasonLabel.setFont(UiUtil.plain(13));
        reasonLabel.setForeground(UiUtil.DARK);
        reasonCard.add(reasonLabel, BorderLayout.CENTER);
        body.add(reasonCard);
        body.add(UiUtil.vgap(14));

        body.add(buildAppealCard());
        body.add(javax.swing.Box.createVerticalGlue());

        body.revalidate();
        body.repaint();
    }

    private JPanel buildAppealCard() {
        JPanel card = UiUtil.sectionCard("Appeal");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        Optional<Appeal> latest;
        try {
            latest = new AppealDAO().findLatestByUser(user.getId());
        } catch (SQLException e) {
            JLabel error = new JLabel("Could not check your appeal status: " + e.getMessage());
            error.setFont(UiUtil.plain(12));
            error.setForeground(UiUtil.DANGER);
            card.add(error, BorderLayout.CENTER);
            return card;
        }

        if (latest.isPresent() && Appeal.STATUS_PENDING.equals(latest.get().getStatus())) {
            JLabel pending = new JLabel(UiUtil.wrapHtml(
                    "Your appeal is already under review. "
                    + "You will be notified once the administrator responds.", 360));
            pending.setFont(UiUtil.plain(13));
            pending.setForeground(UiUtil.MUTED);
            card.add(pending, BorderLayout.CENTER);
            return card;
        }

        JPanel form = new JPanel(new BorderLayout(0, 8));
        form.setOpaque(false);

        JLabel caption = new JLabel("Explain why your account should be re-enabled:");
        caption.setFont(UiUtil.plain(12));
        caption.setForeground(UiUtil.MUTED);
        form.add(caption, BorderLayout.NORTH);

        JTextArea message = new JTextArea(4, 20);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(UiUtil.plain(13));
        JScrollPane fieldScroll = new JScrollPane(message);
        fieldScroll.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, 0));
        form.add(fieldScroll, BorderLayout.CENTER);

        JButton submit = new JButton("Submit Appeal");
        UiUtil.stylePrimary(submit);
        submit.setPreferredSize(new Dimension(140, 32));
        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        submitRow.setOpaque(false);
        submitRow.add(submit);
        form.add(submitRow, BorderLayout.SOUTH);

        submit.addActionListener(e -> {
            String text = message.getText().trim();
            if (text.isEmpty()) {
                UiUtil.error(this, "Please explain why your account should be re-enabled.");
                return;
            }
            try {
                new AppealDAO().insert(user.getId(), text);
                UiUtil.info(this, "Your appeal has been submitted.");
                rebuildBody();
            } catch (SQLException ex) {
                UiUtil.error(this, ex.getMessage());
            }
        });

        card.add(form, BorderLayout.CENTER);
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
        return footer;
    }
}
