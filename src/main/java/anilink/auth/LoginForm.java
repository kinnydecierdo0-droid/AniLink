package anilink.auth;

import anilink.data.BuyerDAO;
import anilink.data.FarmerDAO;
import anilink.data.UserDAO;
import anilink.data.WarningDAO;
import anilink.model.Buyer;
import anilink.model.Farmer;
import anilink.model.User;
import anilink.model.Warning;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;

public class LoginForm extends javax.swing.JFrame {

    private final MainFrame mainFrame;

    public LoginForm() {
        this(null);
    }

    public LoginForm(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initComponents();
        getRootPane().setDefaultButton(btnLogin);
    }

    public void setError(String message) {
        lblError.setText(message);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblLogo = new javax.swing.JLabel();
        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        lblIdentifierCaption = new javax.swing.JLabel();
        txtIdentifier = new javax.swing.JTextField();
        lblPasswordCaption = new javax.swing.JLabel();
        pnlPassword = new javax.swing.JPanel();
        pwdPassword = new javax.swing.JPasswordField();
        btnTogglePassword = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        lblNoAccount = new javax.swing.JLabel();
        btnRegister = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AniLink - Login");
        setResizable(false);

        lblLogo.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblLogo.setForeground(new java.awt.Color(46, 125, 50));
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/anilink-logo-64.png"))); // NOI18N
        lblLogo.setText("AniLink");

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Welcome Back");

        lblSubtitle.setForeground(new java.awt.Color(96, 124, 107));
        lblSubtitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtitle.setText("Log in to your AniLink account");

        lblError.setForeground(new java.awt.Color(211, 47, 47));
        lblError.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblError.setText(" ");

        lblIdentifierCaption.setText("Email or Contact Number *");

        txtIdentifier.setToolTipText("you@example.com or 09XXXXXXXXX");

        lblPasswordCaption.setText("Password *");

        pnlPassword.setLayout(new java.awt.BorderLayout());

        pwdPassword.setToolTipText("Enter your password");
        pwdPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdPasswordActionPerformed(evt);
            }
        });
        pwdPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                pwdPasswordKeyPressed(evt);
            }
        });
        pnlPassword.add(pwdPassword, java.awt.BorderLayout.CENTER);

        btnTogglePassword.setText("Show");
        btnTogglePassword.setFocusable(false);
        btnTogglePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTogglePasswordActionPerformed(evt);
            }
        });
        pnlPassword.add(btnTogglePassword, java.awt.BorderLayout.LINE_END);

        btnLogin.setBackground(new java.awt.Color(46, 125, 50));
        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        lblNoAccount.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNoAccount.setText("Don't have an account?");

        btnRegister.setForeground(new java.awt.Color(46, 125, 50));
        btnRegister.setText("Register");
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 80, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(lblTitle)
                    .addComponent(lblSubtitle)
                    .addComponent(lblError, javax.swing.GroupLayout.PREFERRED_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(lblIdentifierCaption)
                    .addComponent(txtIdentifier, javax.swing.GroupLayout.PREFERRED_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(lblPasswordCaption)
                    .addComponent(pnlPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(lblNoAccount)
                    .addComponent(btnRegister))
                .addGap(0, 80, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 30, Short.MAX_VALUE)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitle)
                .addGap(18, 18, 18)
                .addComponent(lblIdentifierCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtIdentifier, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPasswordCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblError)
                .addGap(16, 16, 16)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblNoAccount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegister)
                .addGap(0, 30, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void pwdPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdPasswordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pwdPasswordActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        lblError.setText(" ");
        String identifier = txtIdentifier.getText().trim();
        String password = new String(pwdPassword.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            lblError.setText("Please fill in all required fields.");
            return;
        }
        if (identifier.contains("@") && !UiUtil.isEmail(identifier)) {
            lblError.setText("Please enter a valid email address.");
            return;
        }
        if (!identifier.contains("@")) {
            String phone = UiUtil.digitsOnly(identifier);
            if (phone.length() < 7 || phone.length() > 15) {
                lblError.setText("Please enter a valid email or contact number.");
                return;
            }
        }

        try {
            Optional<User> found = new UserDAO().authenticate(identifier, password);
            if (found.isEmpty()) {
                lblError.setText("Invalid email/phone or password. Please check your credentials and try again.");
                return;
            }
            User user = found.get();
            if (!user.isActive()) {
                lblError.setText("Your account has been disabled. Please contact the administrator.");
                new AccountDisabledDialog(this, user).setVisible(true);
                return;
            }
            SessionContext.login(user);
            if ("farmer".equals(user.getRole())) {
                new FarmerDAO().findByUserId(user.getId()).ifPresent(SessionContext::setFarmerProfile);
            } else if ("buyer".equals(user.getRole())) {
                new BuyerDAO().findByUserId(user.getId()).ifPresent(SessionContext::setBuyerProfile);
                SessionContext.loadPersistedCart();
            }

            try {
                List<Warning> pending = new WarningDAO().findUnacknowledgedByUser(user.getId());
                for (Warning w : pending) {
                    UiUtil.info(this, "Warning from the administrator:\n\n" + w.getReason());
                    new WarningDAO().acknowledge(w.getId());
                }
            } catch (SQLException ignored) {
                // Non-fatal: don't block login over a warnings-lookup failure.
            }

            if (mainFrame != null) {
                mainFrame.onLoginSuccess();
                dispose();
            } else {
                MainFrame frame = new MainFrame();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                dispose();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(), "AniLink", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        RegisterForm register = new RegisterForm(this, true);
        register.setLocationRelativeTo(this);
        register.setVisible(true);
    }

    private void btnTogglePasswordActionPerformed(java.awt.event.ActionEvent evt) {
        togglePassword();
    }

    private void pwdPasswordKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            btnLoginActionPerformed(null);
        }
    }

    private void togglePassword() {
        if (pwdPassword.getEchoChar() == (char) 0) {
            pwdPassword.setEchoChar((char) 8226);
            btnTogglePassword.setText("Show");
        } else {
            pwdPassword.setEchoChar((char) 0);
            btnTogglePassword.setText("Hide");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnTogglePassword;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblIdentifierCaption;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblNoAccount;
    private javax.swing.JLabel lblPasswordCaption;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlPassword;
    private javax.swing.JPasswordField pwdPassword;
    private javax.swing.JTextField txtIdentifier;
    // End of variables declaration//GEN-END:variables
}
