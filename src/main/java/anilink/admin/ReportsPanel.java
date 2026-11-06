package anilink.admin;

import anilink.data.OrderDAO;
import anilink.model.Order;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Admin sales report: the completed delivery transactions, which are the ones
 * that actually earned a delivery fee.
 *
 * <p>Only orders that are both {@code completed} and delivered appear; pickup
 * and unfinished orders are out of scope by design. Every figure in the
 * summary cards and the TOTAL strip is summed from the same row list, so they
 * can never disagree with the table.
 */
public class ReportsPanel extends javax.swing.JPanel {

    /** Column indexes of the money columns, used to align the TOTAL strip. */
    private static final int COL_SUBTOTAL = 5;
    private static final int COL_FEE = 6;
    private static final int COL_TOTAL = 7;

    private final MainFrame frame;
    private final Map<String, JLabel> cardValues = new LinkedHashMap<>();
    private TablePager pager;
    private JLabel lblTotalSubtotal;
    private JLabel lblTotalFee;
    private JLabel lblTotalGrand;

    public ReportsPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleBanner(lblTitle, "Sales Report");

        tblSales.setModel(new DefaultTableModel(new Object[0][0], new Object[]{
            "Order #", "Date", "Customer", "Items", "Payment", "Subtotal", "Delivery Fee", "Total"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblSales.setDefaultEditor(Object.class, null);
        tblSales.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblSales.getTableHeader().setReorderingAllowed(false);
        UiUtil.styleTable(tblSales);
        int[] widths = {80, 150, 180, 200, 120, 110, 110, 110};
        for (int i = 0; i < widths.length; i++) {
            tblSales.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        buildCards();
        buildTotals();
        pager = TablePager.install(tblSales, true);
    }


    private void buildCards() {
        pnlCards.setLayout(new GridLayout(1, 4, 12, 0));
        pnlCards.setOpaque(false);
        pnlCards.add(statCard("Completed Deliveries", UiUtil.DARK));
        pnlCards.add(statCard("Total Sales", UiUtil.PRIMARY));
        pnlCards.add(statCard("Total Delivery Fees", new Color(255, 143, 0)));
        pnlCards.add(statCard("G-Cash Sales", new Color(2, 119, 189)));
    }

    private JPanel statCard(String caption, Color accent) {
        JPanel card = UiUtil.card(12, 14, 12, 14);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel value = new JLabel("0");
        value.setFont(UiUtil.bold(24));
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

    /**
     * The TOTAL strip. It sits outside the scroll pane on purpose: a totals row
     * added to the table model would be paginated and sorted along with the
     * data, so it would vanish on page 2.
     */
    private void buildTotals() {
        pnlTotals.setLayout(new BorderLayout());
        pnlTotals.setOpaque(false);

        JPanel strip = new JPanel(null);
        strip.setBackground(UiUtil.ACCENT_TINT);
        strip.setBorder(UiUtil.roundedBorder(UiUtil.ACCENT_DARK, UiUtil.RADIUS, 6, 10, 6, 10));
        strip.setLayout(new BorderLayout());

        JLabel caption = new JLabel("TOTAL");
        caption.setFont(UiUtil.bold(13));
        caption.setForeground(UiUtil.ON_ACCENT);
        strip.add(caption, BorderLayout.WEST);

        JPanel figures = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        figures.setOpaque(false);
        lblTotalSubtotal = totalCell(figures, COL_SUBTOTAL);
        lblTotalFee = totalCell(figures, COL_FEE);
        lblTotalGrand = totalCell(figures, COL_TOTAL);
        lblTotalGrand.setForeground(UiUtil.PRIMARY);
        strip.add(figures, BorderLayout.EAST);

        pnlTotals.add(strip, BorderLayout.CENTER);
    }

    /** A right-aligned figure sized to match its table column. */
    private JLabel totalCell(JPanel row, int column) {
        JLabel label = new JLabel(UiUtil.formatPrice(0), SwingConstants.RIGHT);
        label.setFont(UiUtil.bold(13));
        label.setForeground(UiUtil.ON_ACCENT);
        int width = tblSales.getColumnModel().getColumn(column).getPreferredWidth();
        label.setPreferredSize(new java.awt.Dimension(width, 24));
        row.add(label);
        return label;
    }

    public void refreshData() {
        if (pager == null) {
            return;
        }
        pager.clear();
        double subtotal = 0;
        double fees = 0;
        double grand = 0;
        double gcash = 0;
        int count = 0;
        try {
            List<Order> orders = new OrderDAO().findCompletedDeliveries();
            int rowNumber = 1;
            for (Order order : orders) {
                pager.addRow(new Object[]{
                    "#" + rowNumber++,
                    UiUtil.formatDateTime(order.getCreatedAt()),
                    order.getBuyerName() == null ? "-" : order.getBuyerName(),
                    order.getItemsSummary() == null ? "" : order.getItemsSummary(),
                    UiUtil.paymentLabel(order.getPaymentMethod()),
                    UiUtil.formatPrice(order.getSubtotal()),
                    UiUtil.formatPrice(order.getDeliveryFee()),
                    UiUtil.formatPrice(order.getTotalAmount())
                }, order.getCreatedAt());
                subtotal += order.getSubtotal();
                fees += order.getDeliveryFee();
                grand += order.getTotalAmount();
                if (anilink.model.Payment.METHOD_ONLINE.equals(order.getPaymentMethod())) {
                    gcash += order.getTotalAmount();
                }
                count++;
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load the sales report: " + e.getMessage());
        }
        pager.apply();

        set("Completed Deliveries", String.valueOf(count));
        set("Total Sales", UiUtil.formatPrice(grand));
        set("Total Delivery Fees", UiUtil.formatPrice(fees));
        set("G-Cash Sales", UiUtil.formatPrice(gcash));
        lblTotalSubtotal.setText(UiUtil.formatPrice(subtotal));
        lblTotalFee.setText(UiUtil.formatPrice(fees));
        lblTotalGrand.setText(UiUtil.formatPrice(grand));
    }

    private void set(String caption, String value) {
        JLabel label = cardValues.get(caption);
        if (label != null) {
            label.setText(value);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlCards = new javax.swing.JPanel();
        scrollSales = new javax.swing.JScrollPane();
        tblSales = new javax.swing.JTable();
        pnlTotals = new javax.swing.JPanel();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Sales Report");

        // Summary cards are built at runtime in buildCards().
        javax.swing.GroupLayout pnlCardsLayout = new javax.swing.GroupLayout(pnlCards);
        pnlCards.setLayout(pnlCardsLayout);
        pnlCardsLayout.setHorizontalGroup(
            pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlCardsLayout.setVerticalGroup(
            pnlCardsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 92, Short.MAX_VALUE)
        );

        tblSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        scrollSales.setViewportView(tblSales);

        // The TOTAL strip is built at runtime in buildTotals().
        javax.swing.GroupLayout pnlTotalsLayout = new javax.swing.GroupLayout(pnlTotals);
        pnlTotals.setLayout(pnlTotalsLayout);
        pnlTotalsLayout.setHorizontalGroup(
            pnlTotalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlTotalsLayout.setVerticalGroup(
            pnlTotalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlCards, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollSales, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlTotals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlCards, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(scrollSales, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addComponent(pnlTotals, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlTotals;
    private javax.swing.JScrollPane scrollSales;
    private javax.swing.JTable tblSales;
    // End of variables declaration//GEN-END:variables
}
