package anilink.ui;

import anilink.util.UiUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Custom-painted single-series bar chart.
 *
 * <p>Design notes, per the visualization guidance: the job of this data is
 * magnitude over time, so bars. There is exactly one series, so colour carries
 * no identity - one hue is used and no legend is drawn, because the caption
 * already names the measure. Marks are thin with rounded tops anchored to the
 * baseline and separated by a surface gap; grid lines and axis text stay
 * recessive. Value labels are selective rather than one per bar: the tallest
 * bar is labelled, plus whichever bar is under the cursor.
 */
public class BarChartPanel extends JPanel {

    private static final int PAD_LEFT = 46;
    private static final int PAD_RIGHT = 12;
    private static final int PAD_TOP = 18;
    private static final int PAD_BOTTOM = 36;
    private static final int BAR_GAP = 2;
    private static final int MAX_BAR_WIDTH = 46;
    private static final int CORNER = 4;

    private List<String> labels = new ArrayList<>();
    private List<Integer> values = new ArrayList<>();
    private String caption = "";
    private int hoverIndex = -1;
    private final List<Rectangle> barBounds = new ArrayList<>();
    private String unitSingular = "delivery";
    private String unitPlural = "deliveries";
    private String emptyMessage = "No deliveries in this period";

    public BarChartPanel() {
        setBackground(UiUtil.WHITE);
        setPreferredSize(new Dimension(720, 260));
        setToolTipText("");

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int found = barAt(e.getX(), e.getY());
                if (found != hoverIndex) {
                    hoverIndex = found;
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (hoverIndex != -1) {
                    hoverIndex = -1;
                    repaint();
                }
            }
        });
    }

    /**
     * Replaces the chart data. The two lists must be the same length;
     * {@code caption} names the measure.
     */
    public void setData(List<String> labels, List<Integer> values, String caption) {
        this.labels = labels == null ? new ArrayList<>() : new ArrayList<>(labels);
        this.values = values == null ? new ArrayList<>() : new ArrayList<>(values);
        this.caption = caption == null ? "" : caption;
        this.hoverIndex = -1;
        repaint();
    }

    /**
     * Names the unit shown in the hover tooltip ("12 — deliveries" vs "12 —
     * orders"). Call once after construction; it is presentation, not data.
     */
    public void setUnitLabel(String singular, String plural) {
        this.unitSingular = singular;
        this.unitPlural = plural;
    }

    /** Message shown when there is no data to plot. */
    public void setEmptyMessage(String message) {
        this.emptyMessage = message;
    }

    public int getTotal() {
        int total = 0;
        for (Integer v : values) {
            total += v == null ? 0 : v;
        }
        return total;
    }

    private int barAt(int x, int y) {
        for (int i = 0; i < barBounds.size(); i++) {
            Rectangle r = barBounds.get(i);
            // Generous hit target: the whole column, not just the drawn bar.
            if (x >= r.x && x <= r.x + r.width
                    && y >= PAD_TOP && y <= r.y + r.height) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int i = barAt(event.getX(), event.getY());
        if (i < 0 || i >= values.size()) {
            return null;
        }
        int v = values.get(i) == null ? 0 : values.get(i);
        return labels.get(i) + "  —  " + v + " " + (v == 1 ? unitSingular : unitPlural);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        barBounds.clear();

        if (values.isEmpty() || getTotal() == 0) {
            paintEmpty(g2, w, h);
            g2.dispose();
            return;
        }

        int plotLeft = PAD_LEFT;
        int plotRight = w - PAD_RIGHT;
        int plotTop = PAD_TOP;
        int baseline = h - PAD_BOTTOM;
        int plotHeight = baseline - plotTop;
        int plotWidth = plotRight - plotLeft;
        if (plotWidth <= 0 || plotHeight <= 0) {
            g2.dispose();
            return;
        }

        int max = 0;
        int maxIndex = 0;
        for (int i = 0; i < values.size(); i++) {
            int v = values.get(i) == null ? 0 : values.get(i);
            if (v > max) {
                max = v;
                maxIndex = i;
            }
        }
        int axisMax = niceCeiling(max);

        // Recessive gridlines and y-axis labels.
        g2.setFont(UiUtil.plain(10));
        FontMetrics fm = g2.getFontMetrics();
        int ticks = 4;
        for (int t = 0; t <= ticks; t++) {
            int value = (int) Math.round(axisMax * (t / (double) ticks));
            int y = baseline - (int) Math.round(plotHeight * (t / (double) ticks));
            g2.setColor(t == 0 ? UiUtil.GRID : new Color(238, 238, 238));
            g2.drawLine(plotLeft, y, plotRight, y);
            g2.setColor(UiUtil.MUTED);
            String text = String.valueOf(value);
            g2.drawString(text, plotLeft - 8 - fm.stringWidth(text),
                    y + fm.getAscent() / 2 - 1);
        }

        // Bars.
        int slot = plotWidth / values.size();
        int barWidth = Math.max(3, Math.min(MAX_BAR_WIDTH, slot - BAR_GAP * 2));
        for (int i = 0; i < values.size(); i++) {
            int v = values.get(i) == null ? 0 : values.get(i);
            int barHeight = axisMax == 0 ? 0
                    : (int) Math.round(plotHeight * (v / (double) axisMax));
            int x = plotLeft + i * slot + (slot - barWidth) / 2;
            int y = baseline - barHeight;
            barBounds.add(new Rectangle(x, y, barWidth, Math.max(barHeight, 1)));

            if (barHeight > 0) {
                g2.setColor(i == hoverIndex ? UiUtil.DARK : UiUtil.PRIMARY);
                // Rounded top, flat foot: overdraw the bottom corners.
                g2.fill(new RoundRectangle2D.Float(x, y, barWidth,
                        barHeight + CORNER * 2, CORNER * 2, CORNER * 2));
                g2.fillRect(x, baseline - CORNER * 2, barWidth, CORNER * 2);
            }

            // Selective labels: the tallest bar, plus the hovered one.
            if (v > 0 && (i == maxIndex || i == hoverIndex)) {
                g2.setColor(UiUtil.DARK);
                g2.setFont(UiUtil.bold(10));
                String text = String.valueOf(v);
                int tw = g2.getFontMetrics().stringWidth(text);
                g2.drawString(text, x + (barWidth - tw) / 2, y - 4);
                g2.setFont(UiUtil.plain(10));
            }
        }

        // X-axis labels, thinned so they never collide.
        fm = g2.getFontMetrics();
        int widest = 1;
        for (String label : labels) {
            widest = Math.max(widest, fm.stringWidth(label));
        }
        int step = Math.max(1, (int) Math.ceil((widest + 10.0) / Math.max(1, slot)));
        g2.setColor(UiUtil.MUTED);
        for (int i = 0; i < labels.size(); i++) {
            if (i % step != 0 && i != hoverIndex) {
                continue;
            }
            String label = labels.get(i);
            int x = plotLeft + i * slot + (slot - fm.stringWidth(label)) / 2;
            g2.drawString(label, x, baseline + fm.getAscent() + 6);
        }

        if (!caption.isEmpty()) {
            g2.setColor(UiUtil.MUTED);
            g2.setFont(UiUtil.plain(11));
            g2.drawString(caption, plotLeft, h - 8);
        }
        g2.dispose();
    }

    private void paintEmpty(Graphics2D g2, int w, int h) {
        g2.setColor(UiUtil.MUTED);
        g2.setFont(UiUtil.bold(13));
        String text = emptyMessage;
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
        if (!caption.isEmpty()) {
            g2.setFont(UiUtil.plain(11));
            fm = g2.getFontMetrics();
            g2.drawString(caption, (w - fm.stringWidth(caption)) / 2, h / 2 + 20);
        }
    }

    /** Rounds the axis maximum up to a readable 1/2/5 step. */
    private static int niceCeiling(int max) {
        if (max <= 4) {
            return Math.max(1, max);
        }
        int[] steps = {5, 10, 20, 25, 50, 100, 200, 250, 500, 1000};
        for (int s : steps) {
            if (max <= s) {
                return s;
            }
        }
        return ((max + 999) / 1000) * 1000;
    }
}
