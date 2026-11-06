package anilink.buyer;

import anilink.data.BuyerDAO;
import anilink.data.UserDAO;
import anilink.model.Buyer;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;

public class BuyerProfilePanel extends javax.swing.JPanel {

    private final MainFrame frame;

    public BuyerProfilePanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        txtEmail.setEditable(false);
        UiUtil.styleBanner(lblTitle, "My Profile");
        UiUtil.styleSection(lblSectionProfile);
        UiUtil.styleFieldLabel(lblFirst);
        UiUtil.styleFieldLabel(lblMiddle);
        UiUtil.styleFieldLabel(lblLast);
        UiUtil.styleFieldLabel(lblEmailCaption);
        UiUtil.styleFieldLabel(lblContactCaption);
        UiUtil.styleFieldLabel(lblAddressCaption);
        UiUtil.styleSection(lblSectionPassword);
        UiUtil.styleFieldLabel(lblCurrent);
        UiUtil.styleFieldLabel(lblNew);
        UiUtil.styleFieldLabel(lblConfirm);
        UiUtil.styleStrip(pnlNameRow);
        UiUtil.styleStrip(pnlPasswordRow);
        UiUtil.styleButton(btnSaveProfile);
        UiUtil.styleButton(btnSavePassword);
    }

    public void refreshData() {
        try {
            User user = SessionContext.getCurrentUser();
            if (user == null) {
                return;
            }
            Buyer buyer = SessionContext.getBuyerProfile();
            if (buyer == null) {
                buyer = new BuyerDAO().findByUserId(user.getId()).orElse(null);
                if (buyer != null) {
                    SessionContext.setBuyerProfile(buyer);
                }
            }
            txtFirst.setText(user.getFirstName() == null ? "" : user.getFirstName());
            txtMiddle.setText(user.getMiddleName() == null ? "" : user.getMiddleName());
            txtLast.setText(user.getLastName() == null ? "" : user.getLastName());
            txtEmail.setText(user.getEmail() == null ? "" : user.getEmail());
            txtContact.setText(buyer != null && buyer.getContactNumber() != null
                    ? buyer.getContactNumber() : "");
            txtAddress.setText(buyer != null && buyer.getAddress() != null
                    ? buyer.getAddress() : "");
            pwdCurrent.setText("");
            pwdNew.setText("");
            pwdConfirm.setText("");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void btnSaveProfileActionPerformed(java.awt.event.ActionEvent evt) {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            UiUtil.error(this, "You are not logged in.");
            return;
        }
        String first = txtFirst.getText().trim();
        String middle = txtMiddle.getText().trim();
        String last = txtLast.getText().trim();
        String contact = txtContact.getText().trim();
        String address = txtAddress.getText().trim();
        if (first.isEmpty() || last.isEmpty()) {
            UiUtil.error(this, "First and last name are required.");
            return;
        }
        if (contact.isEmpty() || !UiUtil.isValidMobile(contact)) {
            UiUtil.error(this, "Please enter a valid PH mobile number (09XXXXXXXXX).");
            return;
        }
        try {
            UserDAO userDAO = new UserDAO();
            String email = user.getEmail();
            userDAO.updateProfile(user.getId(), first, middle, last, email);

            int buyerId = SessionContext.getBuyerId();
            if (buyerId > 0) {
                new BuyerDAO().update(buyerId, contact, address);
                Buyer buyer = SessionContext.getBuyerProfile();
                if (buyer != null) {
                    buyer.setContactNumber(contact);
                    buyer.setAddress(address);
                }
            }

            user.setFirstName(first);
            user.setMiddleName(middle);
            user.setLastName(last);
            user.setName(user.buildFullName());
            SessionContext.setCurrentUser(user);
            frame.refreshChrome();
            UiUtil.info(this, "Profile updated successfully!");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void btnSavePasswordActionPerformed(java.awt.event.ActionEvent evt) {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            UiUtil.error(this, "You are not logged in.");
            return;
        }
        String current = new String(pwdCurrent.getPassword());
        String newPassword = new String(pwdNew.getPassword());
        String confirm = new String(pwdConfirm.getPassword());
        if (current.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
            UiUtil.error(this, "Please fill in all password fields.");
            return;
        }
        if (!UiUtil.isValidPassword(newPassword)) {
            UiUtil.error(this, "New password must be at least 8 characters.");
            return;
        }
        if (!newPassword.equals(confirm)) {
            UiUtil.error(this, "New passwords do not match.");
            return;
        }
        try {
            UserDAO userDAO = new UserDAO();
            if (!userDAO.verifyPassword(user.getId(), current)) {
                UiUtil.error(this, "Current password is incorrect.");
                return;
            }
            userDAO.updatePassword(user.getId(), newPassword);
            pwdCurrent.setText("");
            pwdNew.setText("");
            pwdConfirm.setText("");
            UiUtil.info(this, "Password changed successfully!");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblSectionProfile = new javax.swing.JLabel();
        pnlNameRow = new javax.swing.JPanel();
        lblFirst = new javax.swing.JLabel();
        txtFirst = new javax.swing.JTextField();
        lblMiddle = new javax.swing.JLabel();
        txtMiddle = new javax.swing.JTextField();
        lblLast = new javax.swing.JLabel();
        txtLast = new javax.swing.JTextField();
        lblEmailCaption = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblContactCaption = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        lblAddressCaption = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        btnSaveProfile = new javax.swing.JButton();
        lblSectionPassword = new javax.swing.JLabel();
        pnlPasswordRow = new javax.swing.JPanel();
        lblCurrent = new javax.swing.JLabel();
        pwdCurrent = new javax.swing.JPasswordField();
        lblNew = new javax.swing.JLabel();
        pwdNew = new javax.swing.JPasswordField();
        lblConfirm = new javax.swing.JLabel();
        pwdConfirm = new javax.swing.JPasswordField();
        btnSavePassword = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("My Profile");

        lblSectionProfile.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSectionProfile.setForeground(new java.awt.Color(46, 125, 50));
        lblSectionProfile.setText("Edit Profile");

        lblFirst.setText("First Name *");

        lblMiddle.setText("Middle Name");

        lblLast.setText("Last Name *");

        javax.swing.GroupLayout pnlNameRowLayout = new javax.swing.GroupLayout(pnlNameRow);
        pnlNameRow.setLayout(pnlNameRowLayout);
        pnlNameRowLayout.setHorizontalGroup(
            pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNameRowLayout.createSequentialGroup()
                .addGroup(pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFirst)
                    .addComponent(txtFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMiddle)
                    .addComponent(txtMiddle, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLast)
                    .addComponent(txtLast, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 128, Short.MAX_VALUE))
        );
        pnlNameRowLayout.setVerticalGroup(
            pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNameRowLayout.createSequentialGroup()
                .addGroup(pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFirst)
                    .addComponent(lblMiddle)
                    .addComponent(lblLast))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlNameRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFirst, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMiddle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLast, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        lblEmailCaption.setText("Email (read-only):");

        txtEmail.setEditable(false);

        lblContactCaption.setText("Contact Number * (09XXXXXXXXX):");

        lblAddressCaption.setText("Delivery Address:");

        btnSaveProfile.setBackground(new java.awt.Color(46, 125, 50));
        btnSaveProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnSaveProfile.setText("Save Profile");
        btnSaveProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveProfileActionPerformed(evt);
            }
        });

        lblSectionPassword.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblSectionPassword.setForeground(new java.awt.Color(46, 125, 50));
        lblSectionPassword.setText("Change Password");

        lblCurrent.setText("Current Password *");

        lblNew.setText("New Password * (min 8 chars)");

        lblConfirm.setText("Confirm New Password *");

        javax.swing.GroupLayout pnlPasswordRowLayout = new javax.swing.GroupLayout(pnlPasswordRow);
        pnlPasswordRow.setLayout(pnlPasswordRowLayout);
        pnlPasswordRowLayout.setHorizontalGroup(
            pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPasswordRowLayout.createSequentialGroup()
                .addGroup(pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCurrent)
                    .addComponent(pwdCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNew)
                    .addComponent(pwdNew, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblConfirm)
                    .addComponent(pwdConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 68, Short.MAX_VALUE))
        );
        pnlPasswordRowLayout.setVerticalGroup(
            pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPasswordRowLayout.createSequentialGroup()
                .addGroup(pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrent)
                    .addComponent(lblNew)
                    .addComponent(lblConfirm))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPasswordRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pwdCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pwdNew, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pwdConfirm, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        btnSavePassword.setBackground(new java.awt.Color(46, 125, 50));
        btnSavePassword.setForeground(new java.awt.Color(255, 255, 255));
        btnSavePassword.setText("Save Password");
        btnSavePassword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSavePasswordActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSectionProfile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNameRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEmailCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblContactCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtContact, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblAddressCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(btnSaveProfile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSectionPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlPasswordRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSavePassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblSectionProfile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlNameRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblEmailCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblContactCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtContact, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblAddressCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSaveProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblSectionPassword)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlPasswordRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSavePassword, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSavePassword;
    private javax.swing.JButton btnSaveProfile;
    private javax.swing.JLabel lblAddressCaption;
    private javax.swing.JLabel lblConfirm;
    private javax.swing.JLabel lblContactCaption;
    private javax.swing.JLabel lblCurrent;
    private javax.swing.JLabel lblEmailCaption;
    private javax.swing.JLabel lblFirst;
    private javax.swing.JLabel lblLast;
    private javax.swing.JLabel lblMiddle;
    private javax.swing.JLabel lblNew;
    private javax.swing.JLabel lblSectionPassword;
    private javax.swing.JLabel lblSectionProfile;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlNameRow;
    private javax.swing.JPanel pnlPasswordRow;
    private javax.swing.JPasswordField pwdConfirm;
    private javax.swing.JPasswordField pwdCurrent;
    private javax.swing.JPasswordField pwdNew;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirst;
    private javax.swing.JTextField txtLast;
    private javax.swing.JTextField txtMiddle;
    // End of variables declaration//GEN-END:variables
}
