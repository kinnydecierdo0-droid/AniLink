package anilink.admin;

import anilink.data.RiderDAO;
import anilink.data.UserDAO;
import anilink.model.Rider;
import anilink.model.User;
import anilink.model.VehicleType;
import anilink.util.PasswordUtil;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Modal form for creating a delivery man, opened from the Add Delivery Man
 * page so the page itself can lead with the table.
 *
 * <p>Validation and the two-step create (a {@code users} row with
 * {@code role='rider'}, then a {@code riders} row) are carried over unchanged
 * from the old inline form. Hand-coded rather than a NetBeans form so the
 * designer cannot regenerate it.
 */
public class AddDeliveryManDialog extends JDialog {

    private final JTextField txtFirst = new JTextField();
    private final JTextField txtLast = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtContact = new JTextField();
    private final JComboBox<Object> cmbVehicle = new JComboBox<>(
            new Object[]{"-- Select vehicle --", VehicleType.MOTORCYCLE, VehicleType.MINI_VAN, VehicleType.WING_VAN});
    private final JPasswordField pwdPassword = new JPasswordField();
    private final JPasswordField pwdConfirm = new JPasswordField();
    private final JButton btnSave = new JButton("Add Delivery Man");

    private boolean saved;

    public AddDeliveryManDialog(Window owner) {
        super(owner, "Add Delivery Man", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        // Scrolled like every other dialog here, so a field can never end up
        // off the bottom edge and unreachable.
        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UiUtil.BG);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // Size from the content rather than guessing, then keep that as the
        // floor; clamp the height so it still fits a small laptop screen.
        pack();
        int width = Math.max(getWidth(), 540);
        int screenHeight = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().height;
        int height = Math.min(getHeight(), screenHeight - 40);
        setSize(width, height);
        setMinimumSize(new Dimension(width, Math.min(420, height)));
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("New Delivery Man");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Creates a rider account that can sign in and take deliveries.");
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(new java.awt.Color(220, 237, 221));
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiUtil.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel card = UiUtil.card();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 0, 6, 10);
        gc.gridy = 0;

        row(card, gc, "First name *", txtFirst, null);
        row(card, gc, "Last name *", txtLast, null);
        row(card, gc, "Email", txtEmail, "Optional, but must be unique");
        row(card, gc, "Contact number", txtContact, null);
        comboRow(card, gc, "Vehicle type", cmbVehicle, "Select the vehicle this delivery man uses.");
        row(card, gc, "Password *", pwdPassword, "At least 8 characters");
        passwordRow(card, gc, "Confirm password *", pwdConfirm, pwdPassword, "Must match the password above");

        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private void row(JPanel card, GridBagConstraints gc, String caption,
            javax.swing.text.JTextComponent field, String tip) {
        JLabel label = new JLabel(caption);
        UiUtil.styleFieldLabel(label);
        gc.gridx = 0;
        card.add(label, gc);

        UiUtil.styleFormField(field, 280);
        if (tip != null) {
            field.setToolTipText(tip);
        }
        gc.gridx = 1;
        card.add(field, gc);
        gc.gridy++;
    }

    /** Same as {@link #row}, but for a {@link JComboBox} - not a
     * {@link javax.swing.text.JTextComponent}, so it can't reuse {@link #row}
     * or {@link UiUtil#styleFormField}. */
    private void comboRow(JPanel card, GridBagConstraints gc, String caption,
            JComboBox<?> field, String tip) {
        JLabel label = new JLabel(caption);
        UiUtil.styleFieldLabel(label);
        gc.gridx = 0;
        card.add(label, gc);

        field.setFont(UiUtil.plain(13));
        field.setPreferredSize(new Dimension(280, 34));
        if (tip != null) {
            field.setToolTipText(tip);
        }
        gc.gridx = 1;
        card.add(field, gc);
        gc.gridy++;
    }

    /**
     * Same as {@link #row}, but for a password field: one eye-icon toggle
     * sits next to {@code field} and reveals/hides it <em>and</em>
     * {@code other} together - the same echo-char flip
     * {@link anilink.auth.LoginForm#togglePassword()} uses, just drawn as an
     * icon ({@link UiUtil#eyeIcon}) instead of a "Show"/"Hide" text label,
     * and shared by both password fields instead of one button each.
     */
    private void passwordRow(JPanel card, GridBagConstraints gc, String caption,
            JPasswordField field, JPasswordField other, String tip) {
        JLabel label = new JLabel(caption);
        UiUtil.styleFieldLabel(label);
        gc.gridx = 0;
        card.add(label, gc);

        // Narrower than a plain row's 280px by the toggle button's width, so
        // the combined wrapper still lines up with every other field.
        UiUtil.styleFormField(field, 280 - 36);
        if (tip != null) {
            field.setToolTipText(tip);
        }

        javax.swing.ImageIcon shown = UiUtil.eyeIcon(false, 18);
        javax.swing.ImageIcon hidden = UiUtil.eyeIcon(true, 18);
        JButton toggle = new JButton(hidden);
        toggle.setFocusable(false);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        toggle.setToolTipText("Show passwords");
        toggle.addActionListener(e -> {
            boolean reveal = field.getEchoChar() != (char) 0;
            char echo = reveal ? (char) 0 : (char) 8226;
            field.setEchoChar(echo);
            other.setEchoChar(echo);
            toggle.setIcon(reveal ? shown : hidden);
            toggle.setToolTipText(reveal ? "Hide passwords" : "Show passwords");
        });

        JPanel wrap = new JPanel(new BorderLayout(4, 0));
        wrap.setOpaque(false);
        wrap.add(field, BorderLayout.CENTER);
        wrap.add(toggle, BorderLayout.EAST);

        gc.gridx = 1;
        card.add(wrap, gc);
        gc.gridy++;
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

        UiUtil.stylePrimary(btnSave);
        btnSave.setPreferredSize(new Dimension(180, 36));
        btnSave.addActionListener(e -> save());
        footer.add(btnSave);
        return footer;
    }

    private void save() {
        String first = txtFirst.getText().trim();
        String last = txtLast.getText().trim();
        String email = txtEmail.getText().trim();
        String contact = txtContact.getText().trim();
        Object vehicleSelection = cmbVehicle.getSelectedItem();
        VehicleType vehicle = vehicleSelection instanceof VehicleType ? (VehicleType) vehicleSelection : null;
        String password = new String(pwdPassword.getPassword());

        if (first.isEmpty() || last.isEmpty()) {
            UiUtil.error(this, "First and last name are required.");
            return;
        }
        if (!email.isEmpty() && !UiUtil.isEmail(email)) {
            UiUtil.error(this, "Please enter a valid email address.");
            return;
        }
        if (!UiUtil.isValidPassword(password)) {
            UiUtil.error(this, "Password must be at least 8 characters.");
            return;
        }
        if (!password.equals(new String(pwdConfirm.getPassword()))) {
            UiUtil.error(this, "The passwords do not match.");
            pwdConfirm.requestFocus();
            return;
        }
        if (!UiUtil.confirm(this, "Add this person as a delivery man?")) {
            return;
        }
        btnSave.setEnabled(false);
        try {
            UserDAO userDAO = new UserDAO();
            if (!email.isEmpty() && userDAO.emailExists(email)) {
                UiUtil.error(this, "An account with that email already exists.");
                return;
            }
            User user = new User();
            user.setFirstName(first);
            user.setLastName(last);
            user.setName(user.buildFullName());
            user.setEmail(email.isEmpty() ? null : email);
            user.setPassword(PasswordUtil.hash(password));
            user.setRole("rider");
            user.setActive(true);
            int userId = userDAO.insert(user);

            Rider rider = new Rider();
            rider.setUserId(userId);
            rider.setContactNumber(contact.isEmpty() ? null : contact);
            rider.setVehicleType(vehicle == null ? null : vehicle.getLabel());
            new RiderDAO().insert(rider);

            saved = true;
            UiUtil.info(this, "Delivery man added successfully!");
            dispose();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }

    /** True when a delivery man was created, so the caller can refresh. */
    public boolean isSaved() {
        return saved;
    }
}
