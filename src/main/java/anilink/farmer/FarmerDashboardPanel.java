package anilink.farmer;

import anilink.data.OrderDAO;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.BarChartPanel;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 * Farmer landing page: three headline counts plus a sales-trend chart.
 *
 * <p>The chart is assembled in {@code buildChart()} outside the generated
 * region, so the NetBeans designer cannot regenerate it away - same approach
 * as {@link anilink.admin.AdminDashboardPanel}.
 */
public class FarmerDashboardPanel extends javax.swing.JPanel {

    private static final String LAST_7 = "Last 7 days";
    private static final String LAST_30 = "Last 30 days";
    private static final String THIS_MONTH = "This month";
    private static final String SPECIFIC = "Specific date";

    private final MainFrame frame;
    private final BarChartPanel chart = new BarChartPanel();

    private JComboBox<String> cmbRange;
    private JSpinner spinDate;
    private JLabel lblChartTotal;

    public FarmerDashboardPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Farmer Dashboard");
        UiUtil.styleStrip(pnlStats);
        buildChart();
    }

    private void buildChart() {
        pnlChart.setLayout(new BorderLayout());
        pnlChart.setOpaque(false);

        JPanel card = UiUtil.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel heading = new JLabel("Sales Trend");
        UiUtil.styleSection(heading);
        top.add(heading, BorderLayout.WEST);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filter.setOpaque(false);

        JLabel caption = new JLabel("Date:");
        UiUtil.styleFieldLabel(caption);
        filter.add(caption);

        cmbRange = new JComboBox<>(new String[]{LAST_7, LAST_30, THIS_MONTH, SPECIFIC});
        cmbRange.setFont(UiUtil.plain(12));
        cmbRange.setPreferredSize(new Dimension(150, 30));
        cmbRange.addActionListener(e -> {
            spinDate.setVisible(SPECIFIC.equals(cmbRange.getSelectedItem()));
            filter.revalidate();
            loadChart();
        });
        filter.add(cmbRange);

        spinDate = new JSpinner(new SpinnerDateModel(
                new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        spinDate.setEditor(new JSpinner.DateEditor(spinDate, "yyyy-MM-dd"));
        spinDate.setFont(UiUtil.plain(12));
        spinDate.setPreferredSize(new Dimension(120, 30));
        spinDate.setVisible(false);
        spinDate.addChangeListener(e -> {
            if (SPECIFIC.equals(cmbRange.getSelectedItem())) {
                loadChart();
            }
        });
        filter.add(spinDate);

        lblChartTotal = new JLabel();
        lblChartTotal.setFont(UiUtil.bold(12));
        lblChartTotal.setForeground(UiUtil.PRIMARY);
        filter.add(lblChartTotal);

        top.add(filter, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        chart.setUnitLabel("peso", "pesos");
        chart.setEmptyMessage("No completed sales in this period");
        card.add(chart, BorderLayout.CENTER);

        pnlChart.add(card, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            int farmerId = SessionContext.getFarmerId();
            OrderDAO orderDAO = new OrderDAO();

            int totalOrders = 0;
            int pending = 0;
            double totalSales = 0;
            if (farmerId > 0) {
                totalOrders = orderDAO.countAllForFarmer(farmerId);
                pending = orderDAO.countByFarmerAndStatus(farmerId, "pending");
                totalSales = orderDAO.totalSalesForFarmer(farmerId);
            }
            lblOrdersValue.setText(String.valueOf(totalOrders));
            lblPendingValue.setText(String.valueOf(pending));
            lblSalesValue.setText(UiUtil.formatPrice(totalSales));
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
        loadChart();
    }

    private void loadChart() {
        Object selected = cmbRange == null ? LAST_7 : cmbRange.getSelectedItem();
        try {
            if (SPECIFIC.equals(selected)) {
                loadHourly((Date) spinDate.getValue());
            } else {
                loadDaily(String.valueOf(selected));
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load the sales chart: " + e.getMessage());
        }
    }

    private void loadHourly(Date day) throws SQLException {
        int farmerId = SessionContext.getFarmerId();
        double[] totals = new double[24];
        if (farmerId > 0) {
            for (Object[] row : new OrderDAO().salesPerHourForFarmer(farmerId, day)) {
                int hour = ((Number) row[0]).intValue();
                if (hour >= 0 && hour < 24) {
                    totals[hour] = ((Number) row[1]).doubleValue();
                }
            }
        }
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            labels.add(hourLabel(h));
            values.add((int) Math.round(totals[h]));
        }
        String date = new SimpleDateFormat("MMM dd, yyyy").format(day);
        chart.setData(labels, values, "Sales per hour on " + date);
        lblChartTotal.setText(UiUtil.formatPrice(chart.getTotal()) + " total");
    }

    private void loadDaily(String preset) throws SQLException {
        int farmerId = SessionContext.getFarmerId();
        java.util.Calendar c = java.util.Calendar.getInstance();
        Date to = endOfDay(c.getTime());
        Date from;
        if (THIS_MONTH.equals(preset)) {
            c.set(java.util.Calendar.DAY_OF_MONTH, 1);
            from = startOfDay(c.getTime());
        } else {
            int days = LAST_30.equals(preset) ? 29 : 6;
            c.add(java.util.Calendar.DAY_OF_MONTH, -days);
            from = startOfDay(c.getTime());
        }

        Map<String, Double> byDay = new LinkedHashMap<>();
        SimpleDateFormat key = new SimpleDateFormat("yyyy-MM-dd");
        if (farmerId > 0) {
            for (Object[] row : new OrderDAO().salesPerDayForFarmer(farmerId, from, to)) {
                byDay.put(key.format((java.util.Date) row[0]), ((Number) row[1]).doubleValue());
            }
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        SimpleDateFormat shortLabel = new SimpleDateFormat("MMM d");
        java.util.Calendar cursor = java.util.Calendar.getInstance();
        cursor.setTime(from);
        while (!cursor.getTime().after(to)) {
            String k = key.format(cursor.getTime());
            labels.add(shortLabel.format(cursor.getTime()));
            values.add((int) Math.round(byDay.getOrDefault(k, 0.0)));
            cursor.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        chart.setData(labels, values, "Sales per day - " + preset.toLowerCase());
        lblChartTotal.setText(UiUtil.formatPrice(chart.getTotal()) + " total");
    }

    private static String hourLabel(int hour) {
        int display = hour % 12 == 0 ? 12 : hour % 12;
        return display + (hour < 12 ? "a" : "p");
    }

    private static Date startOfDay(Date date) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0);
        c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date endOfDay(Date date) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(date);
        c.set(java.util.Calendar.HOUR_OF_DAY, 23);
        c.set(java.util.Calendar.MINUTE, 59);
        c.set(java.util.Calendar.SECOND, 59);
        c.set(java.util.Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlStats = new javax.swing.JPanel();
        pnlStatOrders = new javax.swing.JPanel();
        lblOrdersValue = new javax.swing.JLabel();
        lblOrdersCaption = new javax.swing.JLabel();
        pnlStatPending = new javax.swing.JPanel();
        lblPendingValue = new javax.swing.JLabel();
        lblPendingCaption = new javax.swing.JLabel();
        pnlStatSales = new javax.swing.JPanel();
        lblSalesValue = new javax.swing.JLabel();
        lblSalesCaption = new javax.swing.JLabel();
        pnlChart = new javax.swing.JPanel();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Farmer Dashboard");

        pnlStatOrders.setBackground(new java.awt.Color(232, 245, 233));

        lblOrdersValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblOrdersValue.setForeground(new java.awt.Color(46, 125, 50));
        lblOrdersValue.setText("0");

        lblOrdersCaption.setForeground(new java.awt.Color(96, 125, 107));
        lblOrdersCaption.setText("Total Orders");

        javax.swing.GroupLayout pnlStatOrdersLayout = new javax.swing.GroupLayout(pnlStatOrders);
        pnlStatOrders.setLayout(pnlStatOrdersLayout);
        pnlStatOrdersLayout.setHorizontalGroup(
            pnlStatOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblOrdersValue, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(lblOrdersCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatOrdersLayout.setVerticalGroup(
            pnlStatOrdersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatOrdersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblOrdersValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblOrdersCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatPending.setBackground(new java.awt.Color(255, 243, 224));

        lblPendingValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblPendingValue.setForeground(new java.awt.Color(230, 126, 34));
        lblPendingValue.setText("0");

        lblPendingCaption.setForeground(new java.awt.Color(96, 125, 107));
        lblPendingCaption.setText("Pending Orders");

        javax.swing.GroupLayout pnlStatPendingLayout = new javax.swing.GroupLayout(pnlStatPending);
        pnlStatPending.setLayout(pnlStatPendingLayout);
        pnlStatPendingLayout.setHorizontalGroup(
            pnlStatPendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatPendingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatPendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPendingValue, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(lblPendingCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatPendingLayout.setVerticalGroup(
            pnlStatPendingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatPendingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPendingValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPendingCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlStatSales.setBackground(new java.awt.Color(46, 125, 50));

        lblSalesValue.setFont(new java.awt.Font("Segoe UI", 1, 24));
        lblSalesValue.setForeground(new java.awt.Color(255, 255, 255));
        lblSalesValue.setText("₱0.00");

        lblSalesCaption.setForeground(new java.awt.Color(200, 230, 201));
        lblSalesCaption.setText("Total Sales");

        javax.swing.GroupLayout pnlStatSalesLayout = new javax.swing.GroupLayout(pnlStatSales);
        pnlStatSales.setLayout(pnlStatSalesLayout);
        pnlStatSalesLayout.setHorizontalGroup(
            pnlStatSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatSalesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlStatSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSalesValue, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(lblSalesCaption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlStatSalesLayout.setVerticalGroup(
            pnlStatSalesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatSalesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSalesValue)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSalesCaption)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlStatsLayout = new javax.swing.GroupLayout(pnlStats);
        pnlStats.setLayout(pnlStatsLayout);
        pnlStatsLayout.setHorizontalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStatsLayout.createSequentialGroup()
                .addComponent(pnlStatOrders, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatPending, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlStatSales, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlStatsLayout.setVerticalGroup(
            pnlStatsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlStatOrders, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
            .addComponent(pnlStatPending, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
            .addComponent(pnlStatSales, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
        );

        // Date filter and BarChartPanel are built at runtime in buildChart().
        javax.swing.GroupLayout pnlChartLayout = new javax.swing.GroupLayout(pnlChart);
        pnlChart.setLayout(pnlChartLayout);
        pnlChartLayout.setHorizontalGroup(
            pnlChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlChartLayout.setVerticalGroup(
            pnlChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlStats, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlChart, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlStats, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlChart, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblOrdersCaption;
    private javax.swing.JLabel lblOrdersValue;
    private javax.swing.JLabel lblPendingCaption;
    private javax.swing.JLabel lblPendingValue;
    private javax.swing.JLabel lblSalesCaption;
    private javax.swing.JLabel lblSalesValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlChart;
    private javax.swing.JPanel pnlStatOrders;
    private javax.swing.JPanel pnlStatPending;
    private javax.swing.JPanel pnlStatSales;
    private javax.swing.JPanel pnlStats;
    // End of variables declaration//GEN-END:variables
}
