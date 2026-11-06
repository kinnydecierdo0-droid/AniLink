package anilink.auth;

import anilink.data.BuyerDAO;
import anilink.data.FarmerDAO;
import anilink.data.UserDAO;
import anilink.model.Buyer;
import anilink.model.Farmer;
import anilink.model.User;
import anilink.util.PasswordUtil;
import anilink.util.UiUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import anilink.util.DBConnection;
import javax.swing.JOptionPane;

public class RegisterForm extends javax.swing.JDialog {

    public RegisterForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        buttonGroup1.add(rbFarmer);
        buttonGroup1.add(rbBuyer);
        rbFarmer.setSelected(true);

        rbFarmer.addActionListener(e -> updateAddressLabel());
        rbBuyer.addActionListener(e -> updateAddressLabel());
        updateAddressLabel();

        btnTogglePassword.setIcon(UiUtil.eyeIcon(true, 18));
        btnTogglePassword.setFocusable(false);
        btnTogglePassword.setBorderPainted(false);
        btnTogglePassword.setContentAreaFilled(false);
        btnTogglePassword.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTogglePassword.setToolTipText("Show passwords");
    }

    /** Address label reflects whichever role is currently selected, since
     * a buyer's delivery address and a farmer's farm address mean different
     * things even though they share one field. */
    private void updateAddressLabel() {
        lblAddress.setText(rbFarmer.isSelected() ? "Address / Farm Address *" : "Address / Buyer Address *");
    }

    /** One shared toggle reveals/hides both password fields together - same
     * mechanism as {@link anilink.admin.AddDeliveryManDialog}'s toggle. */
    private void btnTogglePasswordActionPerformed(java.awt.event.ActionEvent evt) {
        boolean reveal = txtPassword.getEchoChar() != (char) 0;
        char echo = reveal ? (char) 0 : (char) 8226;
        txtPassword.setEchoChar(echo);
        txtConfirm.setEchoChar(echo);
        btnTogglePassword.setIcon(UiUtil.eyeIcon(!reveal, 18));
        btnTogglePassword.setToolTipText(reveal ? "Hide passwords" : "Show passwords");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        lblLogo = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        lblAccountType = new javax.swing.JLabel();
        pnlAccountType = new javax.swing.JPanel();
        rbFarmer = new javax.swing.JRadioButton();
        rbBuyer = new javax.swing.JRadioButton();
        pnlNameRow = new javax.swing.JPanel();
        lblFirst = new javax.swing.JLabel();
        txtFirst = new javax.swing.JTextField();
        lblMiddle = new javax.swing.JLabel();
        txtMiddle = new javax.swing.JTextField();
        lblLast = new javax.swing.JLabel();
        txtLast = new javax.swing.JTextField();
        lblContact = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblAddress = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        pnlPasswordRow = new javax.swing.JPanel();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblConfirm = new javax.swing.JLabel();
        txtConfirm = new javax.swing.JPasswordField();
        btnTogglePassword = new javax.swing.JButton();
        pnlButtons = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();

        setTitle("AniLink - Register");
        setModal(true);

        lblLogo.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblLogo.setForeground(new java.awt.Color(46, 125, 50));
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setText("AniLink");
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/anilink-logo-64.png"))); // NOI18N

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblTitle.setText("Create Account");

        lblError.setForeground(new java.awt.Color(211, 47, 47));
        lblError.setText(" ");

        lblAccountType.setText("Account Type *");

        rbFarmer.setSelected(true);
        rbFarmer.setText("Farmer");
        buttonGroup1.add(rbFarmer);

        rbBuyer.setText("Buyer");
        buttonGroup1.add(rbBuyer);

        pnlAccountType.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        javax.swing.GroupLayout pnlAccountTypeLayout = new javax.swing.GroupLayout(pnlAccountType);
        pnlAccountType.setLayout(pnlAccountTypeLayout);
        pnlAccountTypeLayout.setHorizontalGroup(
            pnlAccountTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccountTypeLayout.createSequentialGroup()
                .addComponent(rbFarmer)
                .addGap(18, 18, 18)
                .addComponent(rbBuyer)
                .addGap(0, 476, Short.MAX_VALUE))
        );
        pnlAccountTypeLayout.setVerticalGroup(
            pnlAccountTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAccountTypeLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(pnlAccountTypeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbFarmer)
                    .addComponent(rbBuyer)))
        );

        lblFirst.setText("First Name *");
        lblMiddle.setText("Middle Name");
        lblLast.setText("Last Name *");

        pnlNameRow.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlNameRow.add(lblFirst, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        pnlNameRow.add(txtFirst, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 22, 200, 30));
        pnlNameRow.add(lblMiddle, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 0, -1, -1));
        pnlNameRow.add(txtMiddle, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 22, 200, 30));
        pnlNameRow.add(lblLast, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 0, -1, -1));
        pnlNameRow.add(txtLast, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 22, 200, 30));

        lblContact.setText("Contact Number * (09XXXXXXXXX)");
        lblEmail.setText("Email (optional)");
        lblAddress.setText("Address / Farm Address *");

        lblPassword.setText("Password * (min 8 chars)");
        lblConfirm.setText("Confirm Password *");

        pnlPasswordRow.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        pnlPasswordRow.add(lblPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));
        pnlPasswordRow.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 22, 300, 30));
        pnlPasswordRow.add(lblConfirm, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 0, -1, -1));
        pnlPasswordRow.add(txtConfirm, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 22, 264, 30));

        btnTogglePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTogglePasswordActionPerformed(evt);
            }
        });
        pnlPasswordRow.add(btnTogglePassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(584, 22, 30, 30));

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlButtons.add(btnCancel);

        btnRegister.setBackground(new java.awt.Color(46, 125, 50));
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("Register");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        pnlButtons.add(btnRegister);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblLogo, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblAccountType, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(pnlAccountType, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(pnlNameRow, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblContact, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(txtContact, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(lblAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                    .addComponent(pnlPasswordRow, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblError)
                .addGap(12, 12, 12)
                .addComponent(lblAccountType)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAccountType, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlNameRow, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblContact)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblEmail)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPasswordRow, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        lblError.setText(" ");
        String first = txtFirst.getText().trim();
        String middle = txtMiddle.getText().trim();
        String last = txtLast.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirm.getPassword());
        boolean isFarmer = rbFarmer.isSelected();

        if (first.isEmpty() || last.isEmpty() || contact.isEmpty() || address.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            lblError.setText("Please fill in all required fields.");
            return;
        }
        if (!UiUtil.isValidMobile(contact)) {
            lblError.setText("Please enter a valid PH mobile number (09XXXXXXXXX).");
            return;
        }
        if (!email.isEmpty() && !UiUtil.isEmail(email)) {
            lblError.setText("Please enter a valid email address.");
            return;
        }
        if (!UiUtil.isValidPassword(password)) {
            lblError.setText("Password must be at least 8 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            lblError.setText("Passwords do not match.");
            return;
        }

        try {
            UserDAO userDAO = new UserDAO();
            if (!email.isEmpty() && userDAO.emailExists(email)) {
                lblError.setText("An account with this email already exists.");
                return;
            }

            Connection c = DBConnection.get();
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                User user = new User();
                user.setFirstName(first);
                user.setMiddleName(middle);
                user.setLastName(last);
                user.setName(user.buildFullName());
                user.setEmail(email.isEmpty() ? null : email);
                user.setPassword(PasswordUtil.hash(password));
                user.setRole(isFarmer ? "farmer" : "buyer");
                user.setActive(true);
                int userId = userDAO.insert(c, user);

                if (isFarmer) {
                    Farmer farmer = new Farmer();
                    farmer.setUserId(userId);
                    farmer.setContactNumber(contact);
                    farmer.setFarmAddress(address);
                    new FarmerDAO().insert(c, farmer);
                } else {
                    Buyer buyer = new Buyer();
                    buyer.setUserId(userId);
                    buyer.setContactNumber(contact);
                    buyer.setAddress(address);
                    new BuyerDAO().insert(c, buyer);
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(oldAuto);
            }

            JOptionPane.showMessageDialog(this,
                    "Registration successful! You can now log in.", "AniLink",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                lblError.setText("An account with this email already exists.");
            } else {
                lblError.setText("Registration failed: " + e.getMessage());
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnTogglePassword;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel lblAccountType;
    private javax.swing.JLabel lblAddress;
    private javax.swing.JLabel lblConfirm;
    private javax.swing.JLabel lblContact;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblFirst;
    private javax.swing.JLabel lblLast;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMiddle;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlAccountType;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlNameRow;
    private javax.swing.JPanel pnlPasswordRow;
    private javax.swing.JRadioButton rbBuyer;
    private javax.swing.JRadioButton rbFarmer;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JPasswordField txtConfirm;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirst;
    private javax.swing.JTextField txtLast;
    private javax.swing.JTextField txtMiddle;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables
}
