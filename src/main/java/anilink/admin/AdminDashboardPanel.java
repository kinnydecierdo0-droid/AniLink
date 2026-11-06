package anilink.admin;

import anilink.data.OrderDAO;
import anilink.data.UserDAO;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.ui.BarChartPanel;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 * Admin landing page: five headline counts plus a deliveries chart.
 *
 * <p>The cards and the chart are assembled in {@code buildCards()} /
 * {@code buildChart()} outside the generated region, so the NetBeans designer
 * cannot regenerate them away.
 */
public class AdminDashboardPanel extends javax.swing.JPanel {

    private static final String LAST_7 = "Last 7 days";
    private static final String LAST_30 = "Last 30 days";
    private static final String THIS_MONTH = "This month";
    private static final String SPECIFIC = "Specific date";

    private final MainFrame frame;
    private final BarChartPanel chart = new BarChartPanel();
    private final Map<String, JLabel> cardValues = new LinkedHashMap<>();

    private JComboBox<String> cmbRange;
    private JSpinner spinDate;
    private JLabel lblChartTotal;

    public AdminDashboardPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Admin Dashboard");
        UiUtil.styleTintedButton(btnUserReport);
        UiUtil.stylePrimary(btnReports);
        btnUserReport.setPreferredSize(new Dimension(150, 34));
        btnReports.setPreferredSize(new Dimension(150, 34));
        UiUtil.styleStrip(pnlButtons);
        buildCards();
        buildChart();
    }


    /** The five headline counts, as evenly sized rounded cards. */
    private void buildCards() {
        pnlCards.setLayout(new GridLayout(1, 5, 12, 0));
        pnlCards.setOpaque(false);
        pnlCards.add(statCard("Total Users", UiUtil.DARK));
        pnlCards.add(statCard("Delivery Men", new Color(2, 119, 189)));
        pnlCards.add(statCard("Customers", new Color(0, 105, 92)));
        pnlCards.add(statCard("Farmers", UiUtil.PRIMARY));
        pnlCards.add(statCard("Deliveries", new Color(142, 36, 170)));
    }

    private JPanel statCard(String caption, Color accent) {
        JPanel card = UiUtil.card(12, 14, 12, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel value = new JLabel("0");
        value.setFont(UiUtil.bold(26));
        value.setForeground(accent);
        value.setAlignmentX(LEFT_ALIGNMENT);
        card.add(value);
        card.add(UiUtil.vgap(4));

        JLabel label = new JLabel(caption);
        label.setFont(UiUtil.bold(12));
        label.setForeground(UiUtil.MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        card.add(label);

        cardValues.put(caption, value);
        return card;
    }

    /** Date filter above the chart, both inside one rounded section card. */
    private void buildChart() {
        pnlChart.setLayout(new BorderLayout());
        pnlChart.setOpaque(false);

        JPanel card = UiUtil.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel heading = new JLabel("Deliveries");
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
        card.add(chart, BorderLayout.CENTER);

        pnlChart.add(card, BorderLayout.CENTER);
    }

    public void refreshData() {
        loadCards();
        loadChart();
    }

    private void loadCards() {
        try {
            UserDAO users = new UserDAO();
            set("Total Users", users.countAll());
            set("Delivery Men", users.countByRole("rider"));
            set("Customers", users.countByRole("buyer"));
            set("Farmers", users.countByRole("farmer"));
            set("Deliveries", new OrderDAO().countDeliveries());
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load dashboard counts: " + e.getMessage());
        }
    }

    private void set(String caption, int value) {
        JLabel label = cardValues.get(caption);
        if (label != null) {
            label.setText(String.valueOf(value));
        }
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
            UiUtil.error(this, "Failed to load the deliveries chart: " + e.getMessage());
        }
    }

    /** One bar per hour for a single date, so the rate across the day shows. */
    private void loadHourly(Date day) throws SQLException {
        int[] counts = new int[24];
        for (Object[] row : new OrderDAO().deliveriesPerHour(day)) {
            int hour = ((Number) row[0]).intValue();
            if (hour >= 0 && hour < 24) {
                counts[hour] = ((Number) row[1]).intValue();
            }
        }
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            labels.add(hourLabel(h));
            values.add(counts[h]);
        }
        String date = new SimpleDateFormat("MMM dd, yyyy").format(day);
        chart.setData(labels, values, "Deliveries per hour on " + date);
        lblChartTotal.setText(chart.getTotal() + " total");
    }

    /** One bar per day across the selected range. */
    private void loadDaily(String preset) throws SQLException {
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

        // Index the rows the database returned, then walk every day in the
        // range so days with no deliveries still render as a zero bar.
        Map<String, Integer> byDay = new LinkedHashMap<>();
        SimpleDateFormat key = new SimpleDateFormat("yyyy-MM-dd");
        for (Object[] row : new OrderDAO().deliveriesPerDay(from, to)) {
            byDay.put(key.format((java.util.Date) row[0]), ((Number) row[1]).intValue());
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        SimpleDateFormat shortLabel = new SimpleDateFormat("MMM d");
        java.util.Calendar cursor = java.util.Calendar.getInstance();
        cursor.setTime(from);
        while (!cursor.getTime().after(to)) {
            String k = key.format(cursor.getTime());
            labels.add(shortLabel.format(cursor.getTime()));
            values.add(byDay.getOrDefault(k, 0));
            cursor.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        chart.setData(labels, values, "Deliveries per day - " + preset.toLowerCase());
        lblChartTotal.setText(chart.getTotal() + " total");
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

    private void btnUserReportActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.ADMIN_USER_REPORT);
    }

    private void btnReportsActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.ADMIN_REPORTS);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlCards = new javax.swing.JPanel();
        pnlChart = new javax.swing.JPanel();
        pnlButtons = new javax.swing.JPanel();
        btnUserReport = new javax.swing.JButton();
        btnReports = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Admin Dashboard");

        // Stat cards are built at runtime in buildCards().
        javax.swing.GroupLayout pnlCardsLayout = new javax.swing.GroupLayout(pnlCards);
        pnlCards.setLayout(pnlCardsLayout);
        pnlCardsLayout.setHorizontalGroup(
            pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlCardsLayout.setVerticalGroup(
            pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 96, Short.MAX_VALUE)
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
            .addGap(0, 330, Short.MAX_VALUE)
        );

        btnUserReport.setText("User Report");
        btnUserReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUserReportActionPerformed(evt);
            }
        });
        pnlButtons.add(btnUserReport);

        btnReports.setText("Sales Report");
        btnReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReportsActionPerformed(evt);
            }
        });
        pnlButtons.add(btnReports);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlChart, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlCards, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlChart, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnReports;
    private javax.swing.JButton btnUserReport;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlChart;
    // End of variables declaration//GEN-END:variables
}
