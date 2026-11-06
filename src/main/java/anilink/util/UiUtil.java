package anilink.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class UiUtil {

    public static final String IMAGE_DIR = "images";

    /** Theme palette. Prefer these over inline Color literals in new code. */
    public static final Color PRIMARY = new Color(46, 125, 50);
    public static final Color DARK = new Color(27, 94, 32);
    public static final Color TINT = new Color(232, 245, 233);
    public static final Color MUTED = new Color(96, 125, 107);
    public static final Color BG = new Color(248, 250, 248);
    public static final Color BORDER = new Color(224, 224, 224);
    public static final Color DANGER = new Color(211, 47, 47);
    public static final Color WHITE = Color.WHITE;
    public static final Color ACCENT = new Color(255, 193, 7);
    public static final Color ACCENT_DARK = new Color(255, 160, 0);
    public static final Color ON_ACCENT = new Color(51, 38, 0);
    public static final Color ACCENT_TINT = new Color(255, 248, 225);
    /** Row highlight for tables; pale yellow so it matches the headers. */
    public static final Color TABLE_SELECTION = new Color(255, 236, 179);
    /** Grid line colour, dark enough to read against white rows. */
    public static final Color GRID = new Color(189, 189, 189);

    /** Flat fee added to delivery orders (pickup orders are never charged). */
    public static final double DELIVERY_FEE = 500.00;

    public static final String FONT_FAMILY = "Segoe UI";

    /** Corner radius for the soft-edged look applied app-wide. */
    public static final int RADIUS = 10;
    /** Logout button's resting colour; it turns {@link #DANGER} on hover. */
    public static final Color YELLOW_GREEN = new Color(154, 205, 50);

    private UiUtil() {
    }

    /**
     * Installs the FlatLaf look and feel. Falls back to the platform default
     * when the jar is missing so the app still starts.
     */
    public static void applyLookAndFeel() {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", RADIUS);
            UIManager.put("Component.arc", RADIUS);
            UIManager.put("TextComponent.arc", RADIUS);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.trackArc", 8);
            UIManager.put("TabbedPane.selectedBackground", WHITE);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("defaultFont", new Font(FONT_FAMILY, Font.PLAIN, 13));
        } catch (RuntimeException | LinkageError e) {
            // Keep the default Swing look; every panel already styles itself.
        }
    }

    public static String formatPrice(Object price) {
        double value = 0;
        if (price instanceof Number) {
            value = ((Number) price).doubleValue();
        }
        return String.format(new Locale("fil", "PH"), "\u20B1%,.2f", value);
    }

    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("MMM dd, yyyy hh:mm a").format(date);
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("MMM dd, yyyy").format(date);
    }

    public static String statusBadge(String status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case "pending":
                return "PENDING";
            case "preparing":
                return "PREPARING";
            case "ready_for_pickup":
                return "READY TO PICK-UP";
            case "handed_to_rider":
                return "HANDED TO RIDER";
            case "on_the_way":
                return "ON THE WAY";
            case "completed":
                return "COMPLETED";
            case "cancelled":
                return "CANCELLED";
            case "available":
                return "AVAILABLE";
            case "out_of_stock":
                return "OUT OF STOCK";
            case "inactive":
                return "INACTIVE";
            default:
                return status.toUpperCase(Locale.ROOT);
        }
    }

    /**
     * Same labels as {@link #statusBadge}, but a completed order reads
     * "DELIVERED" when it was a delivery order (matches the farmer/rider
     * "Delivered" tab) and "COMPLETED" when it was a pickup order.
     */
    public static String deliveryBadge(String status, String deliveryMethod) {
        if ("completed".equals(status) && "delivery".equals(deliveryMethod)) {
            return "DELIVERED";
        }
        return statusBadge(status);
    }

    public static void info(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "AniLink", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "AniLink", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(java.awt.Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Prompts for a short, required piece of free text (a disable reason, a
     * warning message). Re-prompts on blank input instead of accepting it
     * silently. Returns the trimmed, non-blank text, or {@code null} only
     * when the user cancels/closes the dialog - never blank/whitespace.
     */
    public static String promptText(java.awt.Component parent, String title, String message) {
        while (true) {
            String input = JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
            if (input == null) {
                return null;
            }
            String trimmed = input.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
            error(parent, "This field is required.");
        }
    }

    public static boolean isEmail(String value) {
        return value != null && value.contains("@") && value.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public static String digitsOnly(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^0-9]", "");
    }

    public static boolean isValidMobile(String value) {
        String digits = digitsOnly(value);
        return digits.matches("0\\d{9,12}");
    }

    public static ImageIcon loadProductIcon(String fileName, int width, int height) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return placeholderIcon(width, height);
        }
        File file = new File(IMAGE_DIR, fileName);
        if (!file.isFile()) {
            java.io.InputStream in = UiUtil.class.getResourceAsStream("/images/" + fileName);
            if (in == null) {
                return placeholderIcon(width, height);
            }
            try {
                Image image = new ImageIcon(readAll(in)).getImage();
                return scale(image, width, height);
            } catch (IOException e) {
                return placeholderIcon(width, height);
            } finally {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
        Image image = new ImageIcon(file.getAbsolutePath()).getImage();
        return scale(image, width, height);
    }

    private static byte[] readAll(java.io.InputStream in) throws IOException {
        return in.readAllBytes();
    }

    private static ImageIcon scale(Image image, int width, int height) {
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    public static ImageIcon placeholderIcon(int width, int height) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setColor(new java.awt.Color(0xE8, 0xF5, 0xE9));
        g.fillRect(0, 0, width, height);
        g.setColor(new java.awt.Color(0x2E, 0x7D, 0x32));
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, Math.max(12, width / 8)));
        String text = "No Image";
        java.awt.FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height + fm.getAscent()) / 2;
        g.drawString(text, x, y);
        g.dispose();
        return new ImageIcon(img);
    }

    /**
     * A small hand-painted eye glyph for password show/hide toggles - drawn
     * rather than an emoji character, so it renders identically everywhere
     * instead of depending on an emoji font being available (one was not,
     * in this Swing/FlatLaf setup). {@code crossed = true} draws a slash
     * through it for the "currently hidden, click to reveal" state.
     */
    public static ImageIcon eyeIcon(boolean crossed, int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(MUTED);
        g.setStroke(new java.awt.BasicStroke(Math.max(1.4f, size / 11f)));

        double w = size;
        double h = size;
        java.awt.geom.Path2D outline = new java.awt.geom.Path2D.Double();
        outline.moveTo(w * 0.08, h * 0.5);
        outline.quadTo(w * 0.5, h * 0.08, w * 0.92, h * 0.5);
        outline.quadTo(w * 0.5, h * 0.92, w * 0.08, h * 0.5);
        outline.closePath();
        g.draw(outline);

        double pupilR = size * 0.14;
        g.fill(new java.awt.geom.Ellipse2D.Double(
                w / 2 - pupilR, h / 2 - pupilR, pupilR * 2, pupilR * 2));

        if (crossed) {
            g.draw(new java.awt.geom.Line2D.Double(w * 0.1, h * 0.1, w * 0.9, h * 0.9));
        }
        g.dispose();
        return new ImageIcon(img);
    }

    /** A hand-painted bell glyph, used in place of an emoji character so it
     * renders the same regardless of font/emoji support (see {@link #eyeIcon}). */
    public static ImageIcon bellIcon(int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = img.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(MUTED);

        double w = size;
        double h = size;

        // Dome: a rounded bell body from one shoulder, up over the top, down
        // to the other shoulder, then straight across the bottom lip.
        java.awt.geom.Path2D dome = new java.awt.geom.Path2D.Double();
        dome.moveTo(w * 0.22, h * 0.62);
        dome.curveTo(w * 0.22, h * 0.22, w * 0.30, h * 0.12, w * 0.5, h * 0.12);
        dome.curveTo(w * 0.70, h * 0.12, w * 0.78, h * 0.22, w * 0.78, h * 0.62);
        dome.lineTo(w * 0.22, h * 0.62);
        dome.closePath();
        g.fill(dome);

        // Small knob on top.
        double knobR = size * 0.045;
        g.fill(new java.awt.geom.Ellipse2D.Double(
                w / 2 - knobR, h * 0.07, knobR * 2, knobR * 2));

        // Base lip, slightly wider than the dome.
        g.fill(new java.awt.geom.RoundRectangle2D.Double(
                w * 0.16, h * 0.62, w * 0.68, h * 0.07, h * 0.03, h * 0.03));

        // Clapper underneath.
        double clapperR = size * 0.07;
        g.fill(new java.awt.geom.Ellipse2D.Double(
                w / 2 - clapperR, h * 0.74, clapperR * 2, clapperR * 2));

        g.dispose();
        return new ImageIcon(img);
    }

    public static String chooseAndStoreImage(java.awt.Component parent) {
        return chooseAndStoreImage(parent, "product_");
    }

    /**
     * Same as {@link #chooseAndStoreImage(java.awt.Component)} but lets the
     * caller pick the stored filename prefix (e.g. "proof_" for receipts).
     */
    public static String chooseAndStoreImage(java.awt.Component parent, String prefix) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (jpg, jpeg, png, gif, webp)", "jpg", "jpeg", "png", "gif", "webp"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File src = chooser.getSelectedFile();
        if (src.length() > 2 * 1024 * 1024) {
            error(parent, "Image must be 2 MB or smaller.");
            return null;
        }
        String name = src.getName().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                || name.endsWith(".gif") || name.endsWith(".webp"))) {
            error(parent, "Unsupported image type.");
            return null;
        }
        String stored = (prefix == null || prefix.isEmpty() ? "product_" : prefix)
                + System.currentTimeMillis()
                + name.substring(name.lastIndexOf('.'));
        try {
            Path dir = Path.of(IMAGE_DIR);
            Files.createDirectories(dir);
            Files.copy(src.toPath(), dir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
            return stored;
        } catch (IOException e) {
            error(parent, "Failed to store image: " + e.getMessage());
            return null;
        }
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static void styleTableHeader(javax.swing.JTable table) {
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(ACCENT);
        header.setForeground(ON_ACCENT);
        header.setFont(bold(13));
        header.setOpaque(true);
        // FlatLaf paints its own flat header, so force the colour through.
        header.setDefaultRenderer(new HeaderRenderer(header.getDefaultRenderer()));
        header.setPreferredSize(new java.awt.Dimension(
                header.getPreferredSize().width, 32));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_DARK));
    }

    /**
     * Shared table styling: yellow header to match the page headers, and row
     * AND column grid lines visible.
     */
    public static void styleTable(javax.swing.JTable table) {
        table.setFont(plain(13));
        table.setFillsViewportHeight(true);
        table.setRowHeight(30);
        table.setGridColor(GRID);
        // FlatLaf hides the grid by default; all three are needed to show it.
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(ON_ACCENT);
        styleTableHeader(table);
    }

    /** Right/center/left-justifies one column's body cells (header is untouched). */
    public static void alignColumn(javax.swing.JTable table, int column, int alignment) {
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(alignment);
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    /**
     * Colors one column's body text per-cell, based on a function of the
     * cell's own string value (e.g. a status label) - {@code colorFor}
     * returning {@code null} leaves that cell's default color alone.
     */
    public static void colorColumn(javax.swing.JTable table, int column,
            java.util.function.Function<String, java.awt.Color> colorFor) {
        javax.swing.table.DefaultTableCellRenderer renderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    java.awt.Color color = colorFor.apply(String.valueOf(value));
                    if (color != null) {
                        c.setForeground(color);
                    }
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(column).setCellRenderer(renderer);
    }

    /**
     * Maps each item's id to a friendly sequential number, {@code 1} for the
     * oldest. {@code newestFirst} must already be sorted newest-first (every
     * DAO query this is used with already is, {@code ORDER BY created_at
     * DESC}), so the last item in the list gets {@code 1} and the first gets
     * {@code newestFirst.size()}.
     *
     * <p>Callers should build this map from the <em>full</em>, unfiltered
     * list for whatever the record's natural owner is (one buyer's orders,
     * one farmer's orders, every order system-wide, etc.) - not from
     * whatever subset a search box or status filter currently narrows the
     * page to - so the same record keeps the same number regardless of
     * which page or filter it's viewed through.
     */
    public static <T> java.util.Map<Integer, Integer> stableNumbers(
            java.util.List<T> newestFirst, java.util.function.ToIntFunction<T> idOf) {
        java.util.Map<Integer, Integer> numbers = new java.util.HashMap<>();
        int total = newestFirst.size();
        for (int i = 0; i < total; i++) {
            numbers.put(idOf.applyAsInt(newestFirst.get(i)), total - i);
        }
        return numbers;
    }

    /** Keeps the yellow header background under FlatLaf's own renderer. */
    private static final class HeaderRenderer
            implements javax.swing.table.TableCellRenderer {

        private final javax.swing.table.TableCellRenderer delegate;

        HeaderRenderer(javax.swing.table.TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getTableCellRendererComponent(javax.swing.JTable table,
                Object value, boolean selected, boolean focused, int row, int column) {
            Component c = delegate.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                label.setOpaque(true);
                label.setBackground(ACCENT);
                label.setForeground(ON_ACCENT);
                label.setFont(bold(13));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, ACCENT_DARK),
                        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
            }
            return c;
        }
    }

    public static void styleTitle(javax.swing.JLabel label) {
        label.setFont(bold(20));
        label.setForeground(DARK);
    }

    public static void styleSection(javax.swing.JLabel label) {
        label.setFont(bold(14));
        label.setForeground(DARK);
    }

    public static void styleFieldLabel(javax.swing.JLabel label) {
        label.setFont(bold(13));
    }

    public static void styleNote(javax.swing.JLabel label) {
        label.setFont(plain(13));
        label.setForeground(MUTED);
    }

    public static void styleValue(javax.swing.JLabel label) {
        label.setFont(plain(13));
    }

    public static void styleStatValue(javax.swing.JLabel label) {
        label.setFont(bold(24));
    }

    public static void styleButton(javax.swing.JButton button) {
        button.setFont(bold(13));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTintedButton(javax.swing.JButton button) {
        styleButton(button);
        button.setBackground(TINT);
        button.setForeground(DARK);
    }

    public static void styleStrip(javax.swing.JPanel panel) {
        panel.setBackground(BG);
    }

    // ---------------------------------------------------------------- fonts

    public static Font plain(int size) {
        return new Font(FONT_FAMILY, Font.PLAIN, size);
    }

    public static Font bold(int size) {
        return new Font(FONT_FAMILY, Font.BOLD, size);
    }

    // -------------------------------------------------------------- widgets

    /** White panel with a hairline border and comfortable inner padding. */
    public static JPanel card() {
        return card(12, 14, 12, 14);
    }

    public static JPanel card(int top, int left, int bottom, int right) {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(roundedBorder(BORDER, RADIUS, top, left, bottom, right));
        return panel;
    }

    /** A {@link #card()} with a bold section heading across the top. */
    public static JPanel sectionCard(String title) {
        JPanel panel = card();
        panel.setLayout(new BorderLayout(0, 10));
        JLabel heading = new JLabel(title);
        styleSection(heading);
        panel.add(heading, BorderLayout.NORTH);
        return panel;
    }

    /** Small rounded chip used for categories and statuses. */
    public static JLabel pill(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text == null ? "" : text);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setFont(plain(11));
        label.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));
        return label;
    }

    public static void stylePrimary(javax.swing.JButton button) {
        styleButton(button);
        button.setBackground(PRIMARY);
        button.setForeground(WHITE);
    }

    public static void styleAccent(javax.swing.JButton button) {
        styleButton(button);
        button.setBackground(ACCENT);
        button.setForeground(ON_ACCENT);
    }

    public static void styleGhost(javax.swing.JButton button) {
        button.setFont(bold(13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(WHITE);
        button.setForeground(DARK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    public static void styleDanger(javax.swing.JButton button) {
        styleButton(button);
        button.setBackground(DANGER);
        button.setForeground(WHITE);
    }

    /**
     * Display name for a stored payment method. The database and
     * {@code PaymentDAO} keep using "Online Payment"; buyers see "G-Cash
     * Payment", so the rename never touches stored rows or admin validation.
     */
    public static String paymentLabel(String storedMethod) {
        if (storedMethod == null || storedMethod.trim().isEmpty()) {
            return "-";
        }
        if (anilink.model.Payment.METHOD_ONLINE.equals(storedMethod.trim())) {
            return "G-Cash Payment";
        }
        return storedMethod;
    }

    /** Reverse of {@link #paymentLabel(String)} for values read off a combo. */
    public static String paymentValue(String displayLabel) {
        if (displayLabel == null) {
            return null;
        }
        if ("G-Cash Payment".equals(displayLabel.trim())) {
            return anilink.model.Payment.METHOD_ONLINE;
        }
        return displayLabel;
    }

    /** Escapes HTML and wraps the text so long labels wrap inside a card. */
    public static String wrapHtml(String text, int widthPx) {
        if (text == null || text.isEmpty()) {
            return "-";
        }
        String safe = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='width:" + widthPx + "px'>" + safe + "</div></html>";
    }

    /** Vertical spacer that works inside BoxLayout columns. */
    public static Component vgap(int height) {
        return javax.swing.Box.createVerticalStrut(height);
    }

    // ---------------------------------------------------------- soft edges

    /**
     * Antialiased round-rect border with inner padding. Swing's
     * {@code LineBorder(c, 1, true)} rounds too, but this keeps the padding and
     * the antialiasing under our control so cards and nav items match.
     */
    public static javax.swing.border.Border roundedBorder(Color line, int radius,
            int top, int left, int bottom, int right) {
        return new RoundedBorder(line, radius, top, left, bottom, right);
    }

    /** Convenience: a rounded border with even padding. */
    public static javax.swing.border.Border roundedBorder(Color line, int pad) {
        return new RoundedBorder(line, RADIUS, pad, pad, pad, pad);
    }

    private static final class RoundedBorder implements javax.swing.border.Border {

        private final Color line;
        private final int radius;
        private final java.awt.Insets pad;

        RoundedBorder(Color line, int radius, int top, int left, int bottom, int right) {
            this.line = line;
            this.radius = radius;
            this.pad = new java.awt.Insets(top + 1, left + 1, bottom + 1, right + 1);
        }

        @Override
        public void paintBorder(Component c, java.awt.Graphics g,
                int x, int y, int width, int height) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(line);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public java.awt.Insets getBorderInsets(Component c) {
            return (java.awt.Insets) pad.clone();
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    /** Rounded, padded search/text field. */
    public static void styleSearchField(javax.swing.text.JTextComponent field) {
        field.setFont(plain(13));
        field.setBorder(roundedBorder(BORDER, RADIUS, 6, 10, 6, 10));
    }

    /**
     * A form input that keeps its size even when its container is short.
     *
     * <p>{@code setPreferredSize} alone is not enough: when a GridBagLayout
     * parent is allocated less room than its children want, it falls back to
     * each child's <em>minimum</em> size, and a JTextField's minimum is small
     * enough that the text becomes invisible. Setting a minimum too is what
     * actually stops the box collapsing.
     */
    public static void styleFormField(javax.swing.text.JTextComponent field, int width) {
        field.setFont(plain(13));
        field.setBorder(roundedBorder(BORDER, RADIUS, 5, 10, 5, 10));
        field.setPreferredSize(new java.awt.Dimension(width, 34));
        field.setMinimumSize(new java.awt.Dimension(Math.min(140, width), 34));
    }

    /**
     * The yellow page banner used across the admin pages. Promoted here from
     * the private copies that had accumulated in several panels.
     */
    public static void styleBanner(JLabel label, String text) {
        label.setText(text);
        label.setFont(bold(20));
        label.setOpaque(true);
        label.setBackground(ACCENT);
        label.setForeground(ON_ACCENT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, ACCENT_DARK),
                BorderFactory.createEmptyBorder(0, 24, 0, 24)));
    }

    // -------------------------------------------------------------- hover

    private static final String BASE_BG = "hover.baseBg";
    private static final String BASE_FG = "hover.baseFg";

    /**
     * Makes {@code button} swap to {@code hoverBg}/{@code hoverFg} while the
     * pointer is over it, restoring whatever its base colours were.
     *
     * <p>Code that later recolours the button must call
     * {@link #setHoverBase(javax.swing.JButton, Color, Color)} so the restore
     * does not put back a stale colour.
     */
    public static void installHover(javax.swing.JButton button,
            Color hoverBg, Color hoverFg) {
        setHoverBase(button, button.getBackground(), button.getForeground());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                    button.setForeground(hoverFg);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                restoreHoverBase(button);
            }
        });
    }

    /** Records the colours {@link #installHover} should restore, and applies them. */
    public static void setHoverBase(javax.swing.JButton button, Color bg, Color fg) {
        button.putClientProperty(BASE_BG, bg);
        button.putClientProperty(BASE_FG, fg);
        button.setBackground(bg);
        button.setForeground(fg);
    }

    private static void restoreHoverBase(javax.swing.JButton button) {
        Object bg = button.getClientProperty(BASE_BG);
        Object fg = button.getClientProperty(BASE_FG);
        if (bg instanceof Color) {
            button.setBackground((Color) bg);
        }
        if (fg instanceof Color) {
            button.setForeground((Color) fg);
        }
    }
}
