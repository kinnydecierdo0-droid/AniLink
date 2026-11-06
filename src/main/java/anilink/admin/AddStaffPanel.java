package anilink.admin;

import anilink.data.RiderDAO;
import anilink.data.UserDAO;
import anilink.data.WarningDAO;
import anilink.model.Rider;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * The Add Delivery Man page: the roster of delivery men, with the add form
 * behind a button so the table is what the admin sees first.
 *
 * <p>Class name kept as {@code AddStaffPanel} so the card id, the form file
 * and the shell wiring stay in step; everything the admin reads now says
 * "delivery man".
 */
public class AddStaffPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private final List<User> riders = new ArrayList<>();
    private final JTextField txtSearch = new JTextField();
    private TablePager pager;
    private JButton btnToggleStatus;

    public AddStaffPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Add Delivery Man");
        UiUtil.styleNote(lblDetail);
        setupTable();
        buildToolbar();
        pager = TablePager.install(tblRiders, false);
    }


    /** Add button and search, above the table. Built outside the GEN region. */
    private void buildToolbar() {
        pnlToolbar.setLayout(new BorderLayout());
        pnlToolbar.setOpaque(false);

        JPanel card = UiUtil.card(8, 12, 8, 12);
        card.setLayout(new BorderLayout(10, 0));

        JButton btnAdd = new JButton("+  Add Delivery Man");
        UiUtil.stylePrimary(btnAdd);
        btnAdd.setPreferredSize(new Dimension(200, 34));
        btnAdd.addActionListener(e -> openAddDialog());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(btnAdd);
        card.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JLabel caption = new JLabel("Search");
        UiUtil.styleFieldLabel(caption);
        right.add(caption);

        UiUtil.styleSearchField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.setToolTipText("Search delivery men by name or email");
        txtSearch.addActionListener(e -> refreshData());
        right.add(txtSearch);

        btnToggleStatus = new JButton("Enable / Disable");
        UiUtil.styleTintedButton(btnToggleStatus);
        btnToggleStatus.setPreferredSize(new Dimension(150, 32));
        btnToggleStatus.addActionListener(e -> toggleStatus(getSelectedUser()));
        right.add(btnToggleStatus);

        JButton btnWarn = new JButton("Warn");
        UiUtil.styleTintedButton(btnWarn);
        btnWarn.setPreferredSize(new Dimension(90, 32));
        btnWarn.addActionListener(e -> warnUser(getSelectedUser()));
        right.add(btnWarn);

        card.add(right, BorderLayout.EAST);
        pnlToolbar.add(card, BorderLayout.CENTER);
    }

    private void openAddDialog() {
        AddDeliveryManDialog dialog = new AddDeliveryManDialog(
                javax.swing.SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void setupTable() {
        tblRiders.setModel(new DefaultTableModel(new Object[0][0],
                new String[]{"ID", "Name", "Email", "Contact", "Vehicle", "Status"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        tblRiders.setDefaultEditor(Object.class, null);
        tblRiders.getTableHeader().setReorderingAllowed(false);
        UiUtil.styleTable(tblRiders);
        tblRiders.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetail();
            }
        });
    }

    public void refreshData() {
        if (pager == null) {
            return;
        }
        try {
            Integer selectedId = getSelectedUserId();
            pager.clear();
            riders.clear();
            RiderDAO riderDAO = new RiderDAO();
            UserDAO userDAO = new UserDAO();
            int rowNumber = 1;
            for (User user : userDAO.searchByRole("rider", txtSearch.getText())) {
                riders.add(user);
                Rider rider = riderDAO.findByUserId(user.getId()).orElse(null);
                pager.addRow(new Object[]{
                    rowNumber++,
                    user.getName(),
                    user.getEmail() == null || user.getEmail().isEmpty() ? "-" : user.getEmail(),
                    rider != null && rider.getContactNumber() != null
                            && !rider.getContactNumber().isEmpty()
                            ? rider.getContactNumber() : "-",
                    rider != null && rider.getVehicleType() != null
                            && !rider.getVehicleType().isEmpty()
                            ? rider.getVehicleType() : "-",
                    user.isActive() ? "Active" : "Disabled"
                }, null, user.getId());
            }
            pager.apply();
            reselectUser(selectedId);
            updateDetail();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void reselectUser(Integer userId) {
        if (userId == null) {
            tblRiders.clearSelection();
            return;
        }
        // Select by the row's key (the real id), not column 0 - that now
        // shows a friendly number instead of the id.
        if (!pager.selectRowByKey(userId)) {
            tblRiders.clearSelection();
        }
    }

    private Integer getSelectedUserId() {
        User user = getSelectedUser();
        return user == null ? null : user.getId();
    }

    private User getSelectedUser() {
        int row = tblRiders.getSelectedRow();
        if (row < 0 || pager == null) {
            return null;
        }
        // The model holds only the current page, so map back to the position
        // the row was added in before indexing the list.
        int sourceIndex = pager.sourceIndexOf(tblRiders.convertRowIndexToModel(row));
        if (sourceIndex < 0 || sourceIndex >= riders.size()) {
            return null;
        }
        return riders.get(sourceIndex);
    }

    private void updateDetail() {
        User user = getSelectedUser();
        if (user == null) {
            lblDetail.setText("Select a delivery man to view details.");
            if (btnToggleStatus != null) {
                btnToggleStatus.setText("Enable / Disable");
            }
            return;
        }
        if (btnToggleStatus != null) {
            btnToggleStatus.setText(user.isActive() ? "Disable Account" : "Enable Account");
        }
        lblDetail.setText("Delivery man   |   Status: "
                + (user.isActive() ? "Active" : "Disabled"));
    }

    private void toggleStatus(User user) {
        if (user == null) {
            UiUtil.info(this, "Select a delivery man first.");
            return;
        }
        User current = SessionContext.getCurrentUser();
        if (current != null && current.getId() == user.getId() && user.isActive()) {
            UiUtil.error(this, "You cannot disable your own account.");
            return;
        }
        boolean enabling = !user.isActive();
        String reason = null;
        if (!enabling) {
            reason = UiUtil.promptText(this, "Disable Account", "Reason for disabling this account:");
            if (reason == null) {
                return;
            }
        }
        String message = enabling
                ? "Enable this account? They will be able to log in again."
                : "Disable this account? They will no longer be able to log in.";
        if (!UiUtil.confirm(this, message)) {
            return;
        }
        try {
            new UserDAO().setActive(user.getId(), enabling, reason);
            UiUtil.info(this, enabling ? "Delivery man account enabled."
                    : "Delivery man account disabled.");
            refreshData();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void warnUser(User user) {
        if (user == null) {
            UiUtil.info(this, "Select a delivery man first.");
            return;
        }
        String reason = UiUtil.promptText(this, "Warn Account", "Reason for this warning:");
        if (reason == null) {
            return;
        }
        try {
            new WarningDAO().insert(user.getId(), reason);
            UiUtil.info(this, "Warning recorded. The delivery man will see it next time they log in.");
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlToolbar = new javax.swing.JPanel();
        scrollRiders = new javax.swing.JScrollPane();
        tblRiders = new javax.swing.JTable();
        lblDetail = new javax.swing.JLabel();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Add Delivery Man");

        // Laid out at runtime in buildToolbar().
        javax.swing.GroupLayout pnlToolbarLayout = new javax.swing.GroupLayout(pnlToolbar);
        pnlToolbar.setLayout(pnlToolbarLayout);
        pnlToolbarLayout.setHorizontalGroup(
            pnlToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlToolbarLayout.setVerticalGroup(
            pnlToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 56, Short.MAX_VALUE)
        );

        tblRiders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        tblRiders.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        scrollRiders.setViewportView(tblRiders);

        lblDetail.setText("Select a delivery man to view details.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollRiders, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollRiders, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(lblDetail)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblDetail;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlToolbar;
    private javax.swing.JScrollPane scrollRiders;
    private javax.swing.JTable tblRiders;
    // End of variables declaration//GEN-END:variables
}
