package anilink.rider;

import anilink.data.OrderDAO;
import anilink.data.RiderDAO;
import anilink.model.Order;
import anilink.model.Rider;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class RiderDashboardPanel extends javax.swing.JPanel {

    private final MainFrame frame;

    public RiderDashboardPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Rider Dashboard");
        javax.swing.border.TitledBorder accountBorder = javax.swing.BorderFactory.createTitledBorder("Account Details");
        accountBorder.setTitleFont(new java.awt.Font("Segoe UI", 1, 13));
        accountBorder.setTitleColor(new java.awt.Color(27, 94, 32));
        infoPanel.setBorder(accountBorder);
        UiUtil.styleNote(lblNote);
        setupTable(tblDeliveries, new String[]{"Order #", "Date", "Buyer", "Products", "Address", "Status"});
        tblDeliveries.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblDeliveries.getSelectedRow() >= 0) {
                    frame.showCard(Cards.RIDER_ORDERS);
                }
            }
        });
    }

    private static void setupTable(javax.swing.JTable table, String[] columns) {
        table.setModel(new DefaultTableModel(new Object[0][0], columns) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {60, 110, 90, 240, 200, 120};
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        UiUtil.styleTable(table);
    }

    public void refreshData() {
        try {
            User user = SessionContext.getCurrentUser();
            if (user == null) {
                return;
            }
            user = new anilink.data.UserDAO().findById(user.getId()).orElse(user);
            SessionContext.setCurrentUser(user);

            lblWelcome.setText("Welcome, " + user.getName() + "!");
            lblNameValue.setText(user.getName());
            lblEmailValue.setText(user.getEmail() == null || user.getEmail().isEmpty()
                    ? "-" : user.getEmail());

            Rider rider = new RiderDAO().findByUserId(user.getId()).orElse(null);
            lblContactValue.setText(rider != null && rider.getContactNumber() != null
                    && !rider.getContactNumber().isEmpty() ? rider.getContactNumber() : "-");
            lblVehicleValue.setText(rider != null && rider.getVehicleType() != null
                    && !rider.getVehicleType().isEmpty() ? rider.getVehicleType() : "-");
            lblStatusValue.setText(user.isActive() ? "Active" : "Disabled");

            DefaultTableModel deliveriesModel = (DefaultTableModel) tblDeliveries.getModel();
            deliveriesModel.setRowCount(0);
            if (rider != null) {
                java.util.List<Order> allForRider = new OrderDAO().findByRider(rider.getId(), null);
                // Same per-rider numbering as Manage Delivery/Transaction
                // history, so a delivery keeps the same number everywhere.
                java.util.Map<Integer, Integer> orderNumbers = UiUtil.stableNumbers(allForRider, Order::getId);
                for (Order order : allForRider) {
                    String status = order.getStatus();
                    if ("completed".equals(status) || "cancelled".equals(status)) {
                        continue;
                    }
                    String address = order.getDeliveryAddress();
                    deliveriesModel.addRow(new Object[]{
                        orderNumbers.getOrDefault(order.getId(), order.getId()),
                        UiUtil.formatDateTime(order.getCreatedAt()),
                        order.getBuyerName(),
                        order.getItemsSummary(),
                        address == null || address.isEmpty() ? "-" : address,
                        UiUtil.deliveryBadge(status, order.getDeliveryMethod())
                    });
                }
            }
            lblNote.setVisible(deliveriesModel.getRowCount() == 0);
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        infoPanel = new javax.swing.JPanel();
        lblNameCaption = new javax.swing.JLabel();
        lblNameValue = new javax.swing.JLabel();
        lblEmailCaption = new javax.swing.JLabel();
        lblEmailValue = new javax.swing.JLabel();
        lblContactCaption = new javax.swing.JLabel();
        lblContactValue = new javax.swing.JLabel();
        lblVehicleCaption = new javax.swing.JLabel();
        lblVehicleValue = new javax.swing.JLabel();
        lblStatusCaption = new javax.swing.JLabel();
        lblStatusValue = new javax.swing.JLabel();
        lblDeliveries = new javax.swing.JLabel();
        scrollDeliveries = new javax.swing.JScrollPane();
        tblDeliveries = new javax.swing.JTable();
        lblNote = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        btnManageDelivery = new javax.swing.JButton();
        btnOrdersReport = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Rider Dashboard");

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblWelcome.setForeground(new java.awt.Color(96, 125, 107));
        lblWelcome.setText("Welcome!");

        infoPanel.setBackground(new java.awt.Color(255, 255, 255));

        lblNameCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblNameCaption.setText("Name:");
        lblNameValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblNameValue.setText("-");

        lblEmailCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblEmailCaption.setText("Email:");
        lblEmailValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblEmailValue.setText("-");

        lblContactCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblContactCaption.setText("Contact:");
        lblContactValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblContactValue.setText("-");

        lblVehicleCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblVehicleCaption.setText("Vehicle:");
        lblVehicleValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblVehicleValue.setText("-");

        lblStatusCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblStatusCaption.setText("Status:");
        lblStatusValue.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblStatusValue.setText("-");

        lblDeliveries.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblDeliveries.setForeground(new java.awt.Color(27, 94, 32));
        lblDeliveries.setText("My Deliveries");

        lblNote.setForeground(new java.awt.Color(96, 125, 107));
        lblNote.setText("No deliveries assigned yet.");

        tblDeliveries.setFont(new java.awt.Font("Segoe UI", 0, 13));
        tblDeliveries.setFillsViewportHeight(true);
        tblDeliveries.setGridColor(new java.awt.Color(224, 224, 224));
        tblDeliveries.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {},
            new String[] {}
        ));
        tblDeliveries.setRowHeight(28);
        tblDeliveries.setSelectionBackground(new java.awt.Color(200, 230, 201));
        scrollDeliveries.setViewportView(tblDeliveries);

        pnlButtons.setBackground(new java.awt.Color(248, 250, 248));

        btnManageDelivery.setBackground(new java.awt.Color(46, 125, 50));
        btnManageDelivery.setBorderPainted(false);
        btnManageDelivery.setFocusPainted(false);
        btnManageDelivery.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnManageDelivery.setForeground(new java.awt.Color(255, 255, 255));
        btnManageDelivery.setText("Manage Delivery");
        btnManageDelivery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManageDeliveryActionPerformed(evt);
            }
        });

        btnOrdersReport.setBackground(new java.awt.Color(27, 94, 32));
        btnOrdersReport.setBorderPainted(false);
        btnOrdersReport.setFocusPainted(false);
        btnOrdersReport.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnOrdersReport.setForeground(new java.awt.Color(255, 255, 255));
        btnOrdersReport.setText("Orders Report");
        btnOrdersReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOrdersReportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlButtonsLayout = new javax.swing.GroupLayout(pnlButtons);
        pnlButtons.setLayout(pnlButtonsLayout);
        pnlButtonsLayout.setHorizontalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlButtonsLayout.createSequentialGroup()
                .addComponent(btnManageDelivery)
                .addGap(12, 12, 12)
                .addComponent(btnOrdersReport))
        );
        pnlButtonsLayout.setVerticalGroup(
            pnlButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(btnManageDelivery, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnOrdersReport, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNameCaption)
                    .addComponent(lblEmailCaption)
                    .addComponent(lblContactCaption)
                    .addComponent(lblVehicleCaption)
                    .addComponent(lblStatusCaption))
                .addGap(16, 16, 16)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNameValue)
                    .addComponent(lblEmailValue)
                    .addComponent(lblContactValue)
                    .addComponent(lblVehicleValue)
                    .addComponent(lblStatusValue))
                .addGap(16, 16, 16))
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNameCaption)
                    .addComponent(lblNameValue))
                .addGap(8, 8, 8)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEmailCaption)
                    .addComponent(lblEmailValue))
                .addGap(8, 8, 8)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblContactCaption)
                    .addComponent(lblContactValue))
                .addGap(8, 8, 8)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblVehicleCaption)
                    .addComponent(lblVehicleValue))
                .addGap(8, 8, 8)
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStatusCaption)
                    .addComponent(lblStatusValue))
                .addGap(4, 4, 4))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 24, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWelcome)
                    .addComponent(infoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 812, Short.MAX_VALUE)
                    .addComponent(lblDeliveries)
                    .addComponent(scrollDeliveries, javax.swing.GroupLayout.PREFERRED_SIZE, 812, Short.MAX_VALUE)
                    .addComponent(lblNote)
                    .addComponent(pnlButtons))
                .addGap(0, 24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(lblWelcome)
                .addGap(16, 16, 16)
                .addComponent(infoPanel)
                .addGap(16, 16, 16)
                .addComponent(lblDeliveries)
                .addGap(4, 4, 4)
                .addComponent(scrollDeliveries, javax.swing.GroupLayout.PREFERRED_SIZE, 260, Short.MAX_VALUE)
                .addGap(4, 4, 4)
                .addComponent(lblNote)
                .addGap(16, 16, 16)
                .addComponent(pnlButtons)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnManageDeliveryActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.RIDER_ORDERS);
    }

    private void btnOrdersReportActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.RIDER_SALES);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnManageDelivery;
    private javax.swing.JButton btnOrdersReport;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JLabel lblContactCaption;
    private javax.swing.JLabel lblContactValue;
    private javax.swing.JLabel lblDeliveries;
    private javax.swing.JLabel lblEmailCaption;
    private javax.swing.JLabel lblEmailValue;
    private javax.swing.JLabel lblNameCaption;
    private javax.swing.JLabel lblNameValue;
    private javax.swing.JLabel lblNote;
    private javax.swing.JLabel lblStatusCaption;
    private javax.swing.JLabel lblStatusValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblVehicleCaption;
    private javax.swing.JLabel lblVehicleValue;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JScrollPane scrollDeliveries;
    private javax.swing.JTable tblDeliveries;
    // End of variables declaration//GEN-END:variables
}
