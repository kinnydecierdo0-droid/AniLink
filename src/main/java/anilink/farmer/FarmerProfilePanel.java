package anilink.farmer;

import anilink.data.FarmerDAO;
import anilink.data.UserDAO;
import anilink.model.Farmer;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;

public class FarmerProfilePanel extends javax.swing.JPanel {

    private final MainFrame frame;

    public FarmerProfilePanel(anilink.shell.MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "My Profile");
        UiUtil.styleSection(lblProfileCaption);
        UiUtil.styleSection(lblPasswordCaption);
        UiUtil.styleFieldLabel(lblFirstName);
        UiUtil.styleFieldLabel(lblMiddleName);
        UiUtil.styleFieldLabel(lblLastName);
        UiUtil.styleFieldLabel(lblEmail);
        UiUtil.styleFieldLabel(lblContact);
        UiUtil.styleFieldLabel(lblFarmAddress);
        UiUtil.styleFieldLabel(lblCurrentPassword);
        UiUtil.styleFieldLabel(lblNewPassword);
        UiUtil.styleFieldLabel(lblConfirmPassword);
        UiUtil.styleButton(btnSaveProfile);
        UiUtil.styleButton(btnChangePassword);
        UiUtil.styleTintedButton(btnCancelProfile);
        UiUtil.styleTintedButton(btnCancelPassword);
        refreshData();
    }

    public void refreshData() {
        try {
            User user = SessionContext.getCurrentUser();
            if (user != null) {
                txtFirstName.setText(user.getFirstName() == null ? "" : user.getFirstName());
                txtMiddleName.setText(user.getMiddleName() == null ? "" : user.getMiddleName());
                txtLastName.setText(user.getLastName() == null ? "" : user.getLastName());
                txtEmail.setText(user.getEmail() == null ? "" : user.getEmail());
            }
            Farmer farmer = SessionContext.getFarmerProfile();
            if (farmer != null) {
                txtContact.setText(farmer.getContactNumber() == null ? "" : farmer.getContactNumber());
                txtFarmAddress.setText(farmer.getFarmAddress() == null ? "" : farmer.getFarmAddress());
            }
            txtCurrentPassword.setText("");
            txtNewPassword.setText("");
            txtConfirmPassword.setText("");
        } catch (Exception e) {
            UiUtil.error(this, "Unable to load profile: " + e.getMessage());
        }
    }

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            User user = SessionContext.getCurrentUser();
            if (user == null) {
                UiUtil.error(this, "No active session.");
                return;
            }
            String first = txtFirstName.getText().trim();
            String middle = txtMiddleName.getText().trim();
            String last = txtLastName.getText().trim();
            String contact = txtContact.getText().trim();
            String address = txtFarmAddress.getText().trim();
            if (first.isEmpty() || last.isEmpty()) {
                UiUtil.error(this, "First and last name are required.");
                return;
            }
            if (!contact.isEmpty() && !UiUtil.isValidMobile(contact)) {
                UiUtil.error(this, "Please enter a valid contact number.");
                return;
            }
            UserDAO userDAO = new UserDAO();
            if (!userDAO.updateProfile(user.getId(), first, middle, last, user.getEmail())) {
                UiUtil.error(this, "Unable to update profile. Please try again.");
                return;
            }
            Farmer farmer = SessionContext.getFarmerProfile();
            if (farmer != null) {
                farmer.setContactNumber(contact);
                farmer.setFarmAddress(address);
                FarmerDAO farmerDAO = new FarmerDAO();
                if (!farmerDAO.update(farmer.getId(), contact, address)) {
                    UiUtil.error(this, "Unable to update farm details. Please try again.");
                    return;
                }
            }
            if (frame != null) {
                frame.refreshChrome();
            }
            UiUtil.info(this, "Profile updated successfully!");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void btnCancelProfileActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
    }

    private void btnChangePasswordActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            User user = SessionContext.getCurrentUser();
            if (user == null) {
                UiUtil.error(this, "No active session.");
                return;
            }
            String current = new String(txtCurrentPassword.getPassword());
            String next = new String(txtNewPassword.getPassword());
            String confirm = new String(txtConfirmPassword.getPassword());
            if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
                UiUtil.error(this, "Please fill in all password fields.");
                return;
            }
            if (!UiUtil.isValidPassword(next)) {
                UiUtil.error(this, "New password must be at least 8 characters.");
                return;
            }
            if (!next.equals(confirm)) {
                UiUtil.error(this, "New passwords do not match.");
                return;
            }
            UserDAO userDAO = new UserDAO();
            if (!userDAO.verifyPassword(user.getId(), current)) {
                UiUtil.error(this, "Current password is incorrect.");
                return;
            }
            if (userDAO.updatePassword(user.getId(), next)) {
                UiUtil.info(this, "Password changed successfully!");
                txtCurrentPassword.setText("");
                txtNewPassword.setText("");
                txtConfirmPassword.setText("");
            } else {
                UiUtil.error(this, "Unable to change password. Please try again.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void btnCancelPasswordActionPerformed(java.awt.event.ActionEvent evt) {
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlProfile = new javax.swing.JPanel();
        lblProfileCaption = new javax.swing.JLabel();
        lblFirstName = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        lblMiddleName = new javax.swing.JLabel();
        txtMiddleName = new javax.swing.JTextField();
        lblLastName = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        lblEmail = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblContact = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        lblFarmAddress = new javax.swing.JLabel();
        scrollAddress = new javax.swing.JScrollPane();
        txtFarmAddress = new javax.swing.JTextArea();
        btnSaveProfile = new javax.swing.JButton();
        btnCancelProfile = new javax.swing.JButton();
        pnlPassword = new javax.swing.JPanel();
        lblPasswordCaption = new javax.swing.JLabel();
        lblCurrentPassword = new javax.swing.JLabel();
        txtCurrentPassword = new javax.swing.JPasswordField();
        lblNewPassword = new javax.swing.JLabel();
        txtNewPassword = new javax.swing.JPasswordField();
        lblConfirmPassword = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JPasswordField();
        btnChangePassword = new javax.swing.JButton();
        btnCancelPassword = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("My Profile");

        pnlProfile.setBackground(new java.awt.Color(255, 255, 255));

        lblProfileCaption.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblProfileCaption.setText("Account Details");

        lblFirstName.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblFirstName.setText("First Name");

        lblMiddleName.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblMiddleName.setText("Middle Name");

        lblLastName.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblLastName.setText("Last Name");

        lblEmail.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblEmail.setText("Email (read-only)");

        txtEmail.setEditable(false);
        txtEmail.setBackground(new java.awt.Color(240, 240, 240));

        lblContact.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblContact.setText("Contact Number");

        lblFarmAddress.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblFarmAddress.setText("Farm Address");

        txtFarmAddress.setColumns(20);
        txtFarmAddress.setRows(3);
        txtFarmAddress.setLineWrap(true);
        txtFarmAddress.setWrapStyleWord(true);
        scrollAddress.setViewportView(txtFarmAddress);

        btnSaveProfile.setBackground(new java.awt.Color(46, 125, 50));
        btnSaveProfile.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save Changes");
        btnSaveProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveProfileActionPerformed(evt);
            }
        });

        btnCancelProfile.setText("Cancel");
        btnCancelProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelProfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlProfileLayout = new javax.swing.GroupLayout(pnlProfile);
        pnlProfile.setLayout(pnlProfileLayout);
        pnlProfileLayout.setHorizontalGroup(
            pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProfileCaption)
                    .addGroup(pnlProfileLayout.createSequentialGroup()
                        .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtFirstName, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblMiddleName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtMiddleName, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblLastName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtLastName, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)))
                    .addGroup(pnlProfileLayout.createSequentialGroup()
                        .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblEmail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblContact, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtContact, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)))
                    .addComponent(lblFarmAddress)
                    .addComponent(scrollAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlProfileLayout.createSequentialGroup()
                        .addComponent(btnSaveProfile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancelProfile)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlProfileLayout.setVerticalGroup(
            pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProfileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProfileCaption)
                .addGap(12, 12, 12)
                .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFirstName)
                    .addComponent(lblMiddleName)
                    .addComponent(lblLastName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMiddleName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmail)
                    .addComponent(lblContact))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(lblFarmAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSaveProfile)
                    .addComponent(btnCancelProfile))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlPassword.setBackground(new java.awt.Color(255, 255, 255));

        lblPasswordCaption.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblPasswordCaption.setText("Change Password");

        lblCurrentPassword.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblCurrentPassword.setText("Current Password");

        lblNewPassword.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNewPassword.setText("New Password");

        lblConfirmPassword.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblConfirmPassword.setText("Confirm New Password");

        btnChangePassword.setBackground(new java.awt.Color(46, 125, 50));
        btnChangePassword.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnChangePassword.setForeground(new java.awt.Color(255, 255, 255));
        btnChangePassword.setText("Change Password");
        btnChangePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangePasswordActionPerformed(evt);
            }
        });

        btnCancelPassword.setText("Clear");
        btnCancelPassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelPasswordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlPasswordLayout = new javax.swing.GroupLayout(pnlPassword);
        pnlPassword.setLayout(pnlPasswordLayout);
        pnlPasswordLayout.setHorizontalGroup(
            pnlPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPasswordLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPasswordCaption)
                    .addComponent(lblCurrentPassword)
                    .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewPassword)
                    .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblConfirmPassword)
                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlPasswordLayout.createSequentialGroup()
                        .addComponent(btnChangePassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancelPassword)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlPasswordLayout.setVerticalGroup(
            pnlPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPasswordLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPasswordCaption)
                .addGap(12, 12, 12)
                .addComponent(lblCurrentPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCurrentPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblNewPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNewPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(lblConfirmPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlPasswordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChangePassword)
                    .addComponent(btnCancelPassword))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(pnlPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelPassword;
    private javax.swing.JButton btnCancelProfile;
    private javax.swing.JButton btnChangePassword;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JLabel lblConfirmPassword;
    private javax.swing.JLabel lblContact;
    private javax.swing.JLabel lblCurrentPassword;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblFarmAddress;
    private javax.swing.JLabel lblFirstName;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblMiddleName;
    private javax.swing.JLabel lblNewPassword;
    private javax.swing.JLabel lblPasswordCaption;
    private javax.swing.JLabel lblProfileCaption;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlPassword;
    private javax.swing.JPanel pnlProfile;
    private javax.swing.JScrollPane scrollAddress;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtContact;
    private javax.swing.JPasswordField txtCurrentPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextArea txtFarmAddress;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtMiddleName;
    private javax.swing.JPasswordField txtNewPassword;
    // End of variables declaration//GEN-END:variables
}
