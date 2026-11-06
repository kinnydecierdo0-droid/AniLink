package anilink.rider;

import anilink.data.OrderDAO;
import anilink.data.RiderDAO;
import anilink.model.Rider;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.model.User;
import anilink.util.UiUtil;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RiderSalesReportPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private final JLabel[] values = new JLabel[5];
    private static final String[] CAPTIONS = {
        "Total Deliveries", "Completed", "In Progress", "Cancelled", "Completed Value"
    };

    public RiderSalesReportPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        buildCards();
        UiUtil.styleBanner(lblTitle, "Orders report");
        UiUtil.styleNote(lblHint);
        UiUtil.styleStrip(cardsPanel);
        UiUtil.styleButton(btnRefresh);
    }

    public void refreshData() {
        try {
            User user = SessionContext.getCurrentUser();
            if (user == null) {
                return;
            }
            Rider rider = new RiderDAO().findByUserId(user.getId()).orElse(null);
            if (rider == null) {
                for (JLabel value : values) {
                    value.setText("-");
                }
                return;
            }
            OrderDAO orderDAO = new OrderDAO();
            int riderId = rider.getId();
            int total = orderDAO.countByRider(riderId);
            int completed = orderDAO.countByRiderAndStatus(riderId, "completed");
            int cancelled = orderDAO.countByRiderAndStatus(riderId, "cancelled");
            int inProgress = total - completed - cancelled;

            values[0].setText(String.valueOf(total));
            values[1].setText(String.valueOf(completed));
            values[2].setText(String.valueOf(inProgress));
            values[3].setText(String.valueOf(cancelled));
            values[4].setText(UiUtil.formatPrice(orderDAO.totalValueForRider(riderId)));
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private JPanel createCard(int index) {
        JPanel card = new JPanel();
        card.setBackground(new java.awt.Color(255, 255, 255));
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(224, 224, 224)),
                javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new java.awt.Dimension(150, 84));
        card.setLayout(new javax.swing.BoxLayout(card, javax.swing.BoxLayout.Y_AXIS));

        JLabel caption = new JLabel(CAPTIONS[index]);
        caption.setFont(new java.awt.Font("Segoe UI", 0, 12));
        caption.setForeground(new java.awt.Color(96, 125, 107));
        caption.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        card.add(caption);

        card.add(Box.createVerticalStrut(4));

        values[index] = new JLabel("-");
        values[index].setFont(new java.awt.Font("Segoe UI", 1, 24));
        values[index].setForeground(new java.awt.Color(27, 94, 32));
        values[index].setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        card.add(values[index]);
        return card;
    }

    private void buildCards() {
        for (int i = 0; i < CAPTIONS.length; i++) {
            cardsPanel.add(createCard(i));
        }
    }

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        btnRefresh = new javax.swing.JButton();
        cardsPanel = new javax.swing.JPanel();
        lblHint = new javax.swing.JLabel();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Orders report");

        btnRefresh.setBackground(new java.awt.Color(27, 94, 32));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnRefresh.setForeground(new java.awt.Color(255, 255, 255));
        btnRefresh.setText("Refresh");
        btnRefresh.setPreferredSize(new java.awt.Dimension(90, 32));
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        cardsPanel.setOpaque(false);
        cardsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 0));

        lblHint.setForeground(new java.awt.Color(96, 125, 107));
        lblHint.setText("Figures cover orders assigned to you. Completed value totals completed order amounts.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 24, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnRefresh))
                    .addComponent(cardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHint))
                .addGap(0, 24, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnRefresh)
                .addGap(16, 16, 16)
                .addComponent(cardsPanel)
                .addGap(16, 16, 16)
                .addComponent(lblHint)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel cardsPanel;
    private javax.swing.JLabel lblHint;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables
}
