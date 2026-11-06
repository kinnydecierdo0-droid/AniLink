package anilink.admin;

import anilink.data.PaymentDAO;
import anilink.data.UserDAO;
import anilink.model.PaymentSettings;
import anilink.model.User;
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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * The admin's own account page, split into tabs: the profile fields, the
 * password change, and the G-Cash account details buyers are shown at
 * checkout.
 *
 * <p>Hand-coded rather than a NetBeans form because it hosts a
 * {@link JTabbedPane}, which the designer cannot usefully lay out - the same
 * reason {@code UserReportPanel} is hand-coded. Every tab sits in its own
 * scroll pane and every input goes through
 * {@link UiUtil#styleFormField(JTextComponent, int)}, so no field can be
 * clipped or collapsed when the window is short.
 */
public class AdminProfilePanel extends JPanel {

    private final MainFrame frame;

    // Profile tab
    private final JTextField txtFirst = new JTextField();
    private final JTextField txtMiddle = new JTextField();
    private final JTextField txtLast = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtRole = new JTextField();

    // Change Password tab
    private final JPasswordField pwdCurrent = new JPasswordField();
    private final JPasswordField pwdNew = new JPasswordField();
    private final JPasswordField pwdConfirm = new JPasswordField();

    // G-Cash Payment tab
    private final JTextField txtProvider = new JTextField();
    private final JTextField txtAccountName = new JTextField();
    private final JTextField txtAccountNumber = new JTextField();
    private final JTextArea txtInstructions = new JTextArea(3, 20);

    public AdminProfilePanel(MainFrame frame) {
        this.frame = frame;
        setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiUtil.bold(13));
        tabs.addTab("Profile", buildProfileTab());
        tabs.addTab("Change Password", buildPasswordTab());
        tabs.addTab("G-Cash Payment", buildGcashTab());

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UiUtil.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        body.add(tabs, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.ACCENT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiUtil.ACCENT_DARK),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Profile");
        title.setFont(UiUtil.bold(22));
        title.setForeground(UiUtil.ON_ACCENT);
        titles.add(title);

        JLabel subtitle = new JLabel("Your account details, password, "
                + "and the G-Cash account buyers pay to");
        subtitle.setFont(UiUtil.plain(12));
        subtitle.setForeground(new java.awt.Color(99, 76, 10));
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    // ------------------------------------------------------------- tabs

    private JScrollPane buildProfileTab() {
        JPanel card = UiUtil.sectionCard("Account details");
        JPanel rows = formGrid();
        GridBagConstraints gc = formConstraints();

        row(rows, gc, "First Name *", txtFirst, null);
        row(rows, gc, "Middle Name", txtMiddle, null);
        row(rows, gc, "Last Name *", txtLast, null);
        row(rows, gc, "Email Address", txtEmail, "Read-only");
        row(rows, gc, "Role", txtRole, "Read-only");
        txtEmail.setEditable(false);
        txtRole.setEditable(false);

        card.add(rows, BorderLayout.CENTER);

        JButton save = new JButton("Save Changes");
        UiUtil.stylePrimary(save);
        save.setPreferredSize(new Dimension(160, 36));
        save.addActionListener(e -> saveProfile());
        card.add(buttonRow(save), BorderLayout.SOUTH);

        return tabScroll(card);
    }

    private JScrollPane buildPasswordTab() {
        JPanel card = UiUtil.sectionCard("Change your password");
        JPanel rows = formGrid();
        GridBagConstraints gc = formConstraints();

        row(rows, gc, "Current Password *", pwdCurrent, null);
        row(rows, gc, "New Password *", pwdNew, "Must be at least 8 characters");
        row(rows, gc, "Confirm New Password *", pwdConfirm, "Must match the new password");

        card.add(rows, BorderLayout.CENTER);

        JButton change = new JButton("Change Password");
        UiUtil.styleDanger(change);
        change.setPreferredSize(new Dimension(180, 36));
        change.addActionListener(e -> changePassword());
        card.add(buttonRow(change), BorderLayout.SOUTH);

        return tabScroll(card);
    }

    private JScrollPane buildGcashTab() {
        JPanel card = UiUtil.sectionCard("G-Cash account shown to buyers");
        JPanel rows = formGrid();
        GridBagConstraints gc = formConstraints();

        row(rows, gc, "Provider / Wallet *", txtProvider, "e.g. GCash");
        row(rows, gc, "Account Name", txtAccountName, null);
        row(rows, gc, "Account Number", txtAccountNumber, null);

        JLabel caption = new JLabel("Instructions");
        UiUtil.styleFieldLabel(caption);
        gc.gridx = 0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        rows.add(caption, gc);

        txtInstructions.setFont(UiUtil.plain(13));
        txtInstructions.setLineWrap(true);
        txtInstructions.setWrapStyleWord(true);
        JScrollPane scrollInstructions = new JScrollPane(txtInstructions);
        scrollInstructions.setPreferredSize(new Dimension(360, 80));
        scrollInstructions.setMinimumSize(new Dimension(220, 60));
        scrollInstructions.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, 0));
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        rows.add(scrollInstructions, gc);
        gc.gridy++;

        card.add(rows, BorderLayout.CENTER);

        JLabel note = new JLabel("Buyers see these details in the G-Cash step at checkout.");
        UiUtil.styleNote(note);

        JButton save = new JButton("Save Details");
        UiUtil.stylePrimary(save);
        save.setPreferredSize(new Dimension(160, 36));
        save.addActionListener(e -> saveGcashSettings());

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(note, BorderLayout.NORTH);
        footer.add(buttonRow(save), BorderLayout.SOUTH);
        card.add(footer, BorderLayout.SOUTH);

        return tabScroll(card);
    }

    // -------------------------------------------------------- tab plumbing

    /** Puts a tab's card at the top of its own scroll pane. */
    private JScrollPane tabScroll(JPanel card) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiUtil.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        wrapper.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UiUtil.BG);
        return scroll;
    }

    private JPanel formGrid() {
        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        return rows;
    }

    private GridBagConstraints formConstraints() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 0, 6, 12);
        gc.gridy = 0;
        return gc;
    }

    private void row(JPanel rows, GridBagConstraints gc, String caption,
            JTextComponent field, String tip) {
        JLabel label = new JLabel(caption);
        UiUtil.styleFieldLabel(label);
        gc.gridx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        rows.add(label, gc);

        UiUtil.styleFormField(field, 340);
        if (tip != null) {
            field.setToolTipText(tip);
        }
        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        rows.add(field, gc);
        gc.gridy++;
    }

    private JPanel buttonRow(JButton button) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        row.add(button);
        return row;
    }

    // ------------------------------------------------------------- data

    public void refreshData() {
        try {
            User user = SessionContext.getCurrentUser();
            if (user != null) {
                user = new UserDAO().findById(user.getId()).orElse(user);
                SessionContext.setCurrentUser(user);
            }
            if (user != null) {
                txtFirst.setText(user.getFirstName() == null ? "" : user.getFirstName());
                txtMiddle.setText(user.getMiddleName() == null ? "" : user.getMiddleName());
                txtLast.setText(user.getLastName() == null ? "" : user.getLastName());
                txtEmail.setText(user.getEmail() == null ? "" : user.getEmail());
                txtRole.setText(user.getRole() == null ? "" : user.getRole());
            }
            pwdCurrent.setText("");
            pwdNew.setText("");
            pwdConfirm.setText("");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        loadGcashSettings();
    }

    private void loadGcashSettings() {
        PaymentSettings s = new PaymentDAO().findSettings();
        txtProvider.setText(s.getProvider());
        txtAccountName.setText(s.getAccountName());
        txtAccountNumber.setText(s.getAccountNumber());
        txtInstructions.setText(s.getInstructions());
    }

    // --------------------------------------------------------- handlers

    private void saveProfile() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            UiUtil.error(this, "No active session found.");
            return;
        }
        String first = txtFirst.getText().trim();
        String middle = txtMiddle.getText().trim();
        String last = txtLast.getText().trim();
        if (first.isEmpty() || last.isEmpty()) {
            UiUtil.error(this, "First and last name are required.");
            return;
        }
        if (!UiUtil.confirm(this, "Save profile changes?")) {
            return;
        }
        try {
            new UserDAO().updateProfile(user.getId(), first, middle, last, user.getEmail());
            User refreshed = new UserDAO().findById(user.getId()).orElse(user);
            SessionContext.setCurrentUser(refreshed);
            UiUtil.info(this, "Profile updated successfully!");
            if (frame != null) {
                frame.refreshChrome();
            }
            refreshData();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void changePassword() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            UiUtil.error(this, "No active session found.");
            return;
        }
        String current = new String(pwdCurrent.getPassword());
        String next = new String(pwdNew.getPassword());
        String confirm = new String(pwdConfirm.getPassword());
        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            UiUtil.error(this, "Please fill in all required fields.");
            return;
        }
        if (!UiUtil.isValidPassword(next)) {
            UiUtil.error(this, "Password must be at least 8 characters.");
            return;
        }
        if (!next.equals(confirm)) {
            UiUtil.error(this, "Passwords do not match.");
            return;
        }
        try {
            if (!new UserDAO().verifyPassword(user.getId(), current)) {
                UiUtil.error(this, "Current password is incorrect.");
                return;
            }
            if (!UiUtil.confirm(this, "Change your password?")) {
                return;
            }
            new UserDAO().updatePassword(user.getId(), next);
            pwdCurrent.setText("");
            pwdNew.setText("");
            pwdConfirm.setText("");
            UiUtil.info(this, "Password changed successfully!");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void saveGcashSettings() {
        String provider = txtProvider.getText().trim();
        if (provider.isEmpty()) {
            UiUtil.error(this, "Payment provider/name is required.");
            return;
        }
        try {
            new PaymentDAO().saveSettings(provider, txtAccountName.getText(),
                    txtAccountNumber.getText(), txtInstructions.getText());
            UiUtil.info(this, "Payment details saved. Buyers will see these at checkout.");
            loadGcashSettings();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }
}
