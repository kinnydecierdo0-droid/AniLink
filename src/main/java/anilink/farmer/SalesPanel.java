package anilink.farmer;

import anilink.data.OrderDAO;
import anilink.model.Order;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.Calendar;
import javax.swing.table.DefaultTableModel;
import anilink.ui.TablePager;

public class SalesPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private TablePager pager;

    public SalesPanel(anilink.shell.MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblHistory.setDefaultEditor(Object.class, null);
        ((DefaultTableModel) tblHistory.getModel()).setColumnIdentifiers(
                new Object[]{"Date", "Items", "Total"});
        UiUtil.styleBanner(lblTitle, "My Sales");
        UiUtil.styleSection(lblHistoryTitle);
        UiUtil.styleStrip(pnlStats);
        UiUtil.styleTable(tblHistory);
        pager = TablePager.install(tblHistory, true);
    }

    private java.sql.Timestamp startOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Timestamp(cal.getTimeInMillis());
    }

    private java.sql.Timestamp startOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Timestamp(cal.getTimeInMillis());
    }

    private java.sql.Timestamp startOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Timestamp(cal.getTimeInMillis());
    }

    public void refreshData() {
        try {
            int farmerId = SessionContext.getFarmerId();
            OrderDAO orderDAO = new OrderDAO();
            double today = 0;
            double week = 0;
            double month = 0;
            double total = 0;
            if (farmerId > 0) {
                today = orderDAO.totalSalesForFarmerSince(farmerId, startOfDay());
                week = orderDAO.totalSalesForFarmerSince(farmerId, startOfWeek());
                month = orderDAO.totalSalesForFarmerSince(farmerId, startOfMonth());
                total = orderDAO.totalSalesForFarmer(farmerId);
            }
            lblTodayValue.setText(UiUtil.formatPrice(today));
            lblWeekValue.setText(UiUtil.formatPrice(week));
            lblMonthValue.setText(UiUtil.formatPrice(month));
            lblTotalValue.setText(UiUtil.formatPrice(total));

            pager.clear();
            if (farmerId > 0) {
                for (Order order : orderDAO.findForFarmer(farmerId)) {
                    if (!"completed".equals(order.getStatus())) {
                        continue;
                    }
                    pager.addRow(new Object[]{
                        UiUtil.formatDate(order.getCreatedAt()),
                        order.getItemsSummary() == null ? "" : order.getItemsSummary(),
                        UiUtil.formatPrice(order.getTotalAmount())
                    }, order.getCreatedAt());
                }
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        pager.apply();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlStats = new javax.swing.JPanel();
        pnlStatToday = new javax.swing.JPanel();
        lblTodayValue = new javax.swing.JLabel();
        lblTodayCaption = new javax.swing.JLabel();
        pnlStatWeek = new javax.swing.JPanel();
        lblWeekValue = new javax.swing.JLabel();
        lblWeekCaption = new javax.swing.JLabel();
        pnlStatMonth = new javax.swing.JPanel();
        lblMonthValue = new javax.swing.JLabel();
        lblMonthCaption = new javax.swing.JLabel();
        pnlStatTotal = new javax.swing.JPanel();
        lblTotalValue = new javax.swing.JLabel();
        lblTotalCaption = new javax.swing.JLabel();
        lblHistoryTitle = new javax.swing.JLabel();
        scrollHistory = new javax.swing.JScrollPane();
        tblHistory = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("My Sales");

        javax.swing.GroupLayout pnlStatsLayout = new javax.swing.GroupLayout(pnlStats);
        pnlStats.setLayout(pnlStatsLayout);
        pnlStatsLayout.setHorizontalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatsLayout.createSequentialGroup()
                .addComponent(pnlStatToday, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlStatsLayout.setVerticalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlStatToday, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
            .addComponent(pnlStatWeek, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
            .addComponent(pnlStatMonth, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
            .addComponent(pnlStatTotal, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
        );

        pnlStatToday.setBackground(new java.awt.Color(232, 245, 233));

        lblTodayValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblTodayValue.setForeground(new java.awt.Color(46, 125, 50));
        lblTodayValue.setText("\u20B10.00");

        lblTodayCaption.setForeground(new java.awt.Color(96, 125, 107));
        lblTodayCaption.setText("Today's Sales");

        javax.swing.GroupLayout pnlStatTodayLayout = new javax.swing.GroupLayout(pnlStatToday);
        pnlStatToday.setLayout(pnlStatTodayLayout);
        pnlStatTodayLayout.setHorizontalGroup(
            pnlStatTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatTodayLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTodayValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblTodayCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatTodayLayout.setVerticalGroup(
            pnlStatTodayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatTodayLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTodayValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTodayCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatWeek.setBackground(new java.awt.Color(227, 242, 253));

        lblWeekValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblWeekValue.setForeground(new java.awt.Color(21, 101, 192));
        lblWeekValue.setText("\u20B10.00");

        lblWeekCaption.setForeground(new java.awt.Color(96, 125, 107));
        lblWeekCaption.setText("This Week");

        javax.swing.GroupLayout pnlStatWeekLayout = new javax.swing.GroupLayout(pnlStatWeek);
        pnlStatWeek.setLayout(pnlStatWeekLayout);
        pnlStatWeekLayout.setHorizontalGroup(
            pnlStatWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatWeekLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWeekValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblWeekCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatWeekLayout.setVerticalGroup(
            pnlStatWeekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatWeekLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWeekValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWeekCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatMonth.setBackground(new java.awt.Color(255, 243, 224));

        lblMonthValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblMonthValue.setForeground(new java.awt.Color(230, 126, 34));
        lblMonthValue.setText("\u20B10.00");

        lblMonthCaption.setForeground(new java.awt.Color(96, 125, 107));
        lblMonthCaption.setText("This Month");

        javax.swing.GroupLayout pnlStatMonthLayout = new javax.swing.GroupLayout(pnlStatMonth);
        pnlStatMonth.setLayout(pnlStatMonthLayout);
        pnlStatMonthLayout.setHorizontalGroup(
            pnlStatMonthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatMonthLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatMonthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMonthValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblMonthCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatMonthLayout.setVerticalGroup(
            pnlStatMonthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatMonthLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblMonthValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblMonthCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatTotal.setBackground(new java.awt.Color(243, 233, 248));

        lblTotalValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblTotalValue.setForeground(new java.awt.Color(106, 27, 154));
        lblTotalValue.setText("\u20B10.00");

        lblTotalCaption.setForeground(new java.awt.Color(96, 125, 107));
        lblTotalCaption.setText("Total Sales");

        javax.swing.GroupLayout pnlStatTotalLayout = new javax.swing.GroupLayout(pnlStatTotal);
        pnlStatTotal.setLayout(pnlStatTotalLayout);
        pnlStatTotalLayout.setHorizontalGroup(
            pnlStatTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalValue, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                    .addComponent(lblTotalCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatTotalLayout.setVerticalGroup(
            pnlStatTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTotalValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblHistoryTitle.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblHistoryTitle.setText("Sales History (completed & ready orders)");

        tblHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        scrollHistory.setViewportView(tblHistory);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblHistoryTitle)
                    .addComponent(scrollHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblHistoryTitle)
                .addGap(6, 6, 6)
                .addComponent(scrollHistory, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblHistoryTitle;
    private javax.swing.JLabel lblMonthCaption;
    private javax.swing.JLabel lblMonthValue;
    private javax.swing.JLabel lblTodayCaption;
    private javax.swing.JLabel lblTodayValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalCaption;
    private javax.swing.JLabel lblTotalValue;
    private javax.swing.JLabel lblWeekCaption;
    private javax.swing.JLabel lblWeekValue;
    private javax.swing.JPanel pnlStatMonth;
    private javax.swing.JPanel pnlStatToday;
    private javax.swing.JPanel pnlStatTotal;
    private javax.swing.JPanel pnlStatWeek;
    private javax.swing.JPanel pnlStats;
    private javax.swing.JScrollPane scrollHistory;
    private javax.swing.JTable tblHistory;
    // End of variables declaration//GEN-END:variables
}
