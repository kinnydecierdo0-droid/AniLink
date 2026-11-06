package anilink.ui;

import anilink.model.Product;
import anilink.util.UiUtil;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Builds the product cards used by the marketplace grids. Extracted from the
 * duplicate {@code createProductCard} methods that lived in
 * {@code GuestMarketplaceFrame} and {@code GuestHomeFrame}.
 */
public final class ProductCardFactory {

    public static final int CARD_WIDTH = 236;
    public static final int CARD_HEIGHT = 424;
    public static final int CONTENT_WIDTH = 208;
    public static final int COLUMNS = 4;

    /** Callback invoked with the product whose card was acted on. */
    public interface Action {
        void run(Product product);
    }

    private ProductCardFactory() {
    }

    /** Card with a single full-width action button (the guest frames' look). */
    public static JPanel card(Product product, String label, Action onClick) {
        return card(product, label, onClick, null, null, null);
    }

    /** Card with a primary and a secondary action, and no favorite heart. */
    public static JPanel card(Product product,
                              String primaryLabel, Action onPrimary,
                              String secondaryLabel, Action onSecondary) {
        return card(product, primaryLabel, onPrimary, secondaryLabel, onSecondary, null);
    }

    /**
     * Card with a primary and a secondary action side by side. The primary
     * button is disabled when the product is out of stock; the secondary
     * (details) action stays available.
     *
     * <p>When {@code onFavorite} is given, a heart button is overlaid on the
     * top-right of the image that adds the product to the buyer's favorites.
     */
    public static JPanel card(Product product,
                              String primaryLabel, Action onPrimary,
                              String secondaryLabel, Action onSecondary,
                              Action onFavorite) {
        boolean outOfStock = product.getQuantity() <= 0;

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UiUtil.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtil.BORDER),
                BorderFactory.createEmptyBorder(8, 10, 10, 10)));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT - 40));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT + 40));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel image = new JLabel(
                UiUtil.loadProductIcon(product.getImage(), CONTENT_WIDTH, 140),
                SwingConstants.CENTER);
        image.setOpaque(true);
        image.setBackground(UiUtil.TINT);
        image.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        lockSize(image, CONTENT_WIDTH, 140);
        if (onSecondary != null) {
            image.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            image.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onSecondary.run(product);
                }
            });
        } else if (onPrimary != null) {
            image.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            image.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onPrimary.run(product);
                }
            });
        }
        if (onFavorite != null) {
            // A JLabel is a Container, so the heart paints on top of the icon.
            image.setLayout(new java.awt.BorderLayout());
            JPanel heartRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 4, 4));
            heartRow.setOpaque(false);
            heartRow.add(favoriteButton(product, onFavorite));
            image.add(heartRow, java.awt.BorderLayout.NORTH);
        }
        card.add(image);
        card.add(UiUtil.vgap(8));

        JLabel name = new JLabel(UiUtil.wrapHtml(product.getProductName(), CONTENT_WIDTH - 12));
        name.setFont(UiUtil.bold(13));
        name.setForeground(UiUtil.DARK);
        name.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        name.setMaximumSize(new Dimension(CONTENT_WIDTH, 40));
        name.setPreferredSize(new Dimension(CONTENT_WIDTH, 36));
        card.add(name);
        card.add(UiUtil.vgap(6));

        JLabel category = UiUtil.pill(
                product.getCategoryName() == null ? "Other" : product.getCategoryName(),
                UiUtil.TINT, UiUtil.PRIMARY);
        category.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        lockSize(category, CONTENT_WIDTH, 22);
        category.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(category);
        card.add(UiUtil.vgap(8));

        JLabel price = new JLabel(UiUtil.formatPrice(product.getPrice()) + " / " + product.getUnit());
        price.setFont(UiUtil.bold(15));
        price.setForeground(UiUtil.PRIMARY);
        price.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        price.setMaximumSize(new Dimension(CONTENT_WIDTH, 22));
        card.add(price);
        card.add(UiUtil.vgap(6));

        JLabel stock = new JLabel(outOfStock
                ? "Out of Stock"
                : "Available: " + product.getQuantity() + " " + product.getUnit());
        stock.setFont(UiUtil.plain(11));
        stock.setForeground(outOfStock ? UiUtil.DANGER : UiUtil.MUTED);
        stock.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        stock.setMaximumSize(new Dimension(CONTENT_WIDTH, 18));
        card.add(stock);
        card.add(UiUtil.vgap(4));

        JLabel farmer = new JLabel("Farmer: "
                + (product.getFarmerName() == null ? "-" : product.getFarmerName()));
        farmer.setFont(UiUtil.plain(11));
        farmer.setForeground(UiUtil.MUTED);
        farmer.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        farmer.setMaximumSize(new Dimension(CONTENT_WIDTH, 18));
        card.add(farmer);
        card.add(javax.swing.Box.createVerticalGlue());
        card.add(UiUtil.vgap(10));

        if (onSecondary == null) {
            JButton only = new JButton(primaryLabel);
            UiUtil.stylePrimary(only);
            only.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            lockSize(only, CONTENT_WIDTH, 34);
            only.addActionListener(e -> onPrimary.run(product));
            card.add(only);
        } else {
            JPanel actions = new JPanel(new GridBagLayout());
            actions.setOpaque(false);
            actions.setAlignmentX(JPanel.LEFT_ALIGNMENT);
            lockSize(actions, CONTENT_WIDTH, 36);

            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 1;
            gc.weighty = 1;
            gc.gridy = 0;

            JButton buy = new JButton(primaryLabel);
            UiUtil.styleAccent(buy);
            if (outOfStock) {
                buy.setEnabled(false);
                buy.setToolTipText("Out of stock");
            } else {
                buy.setToolTipText("Buy " + product.getProductName() + " now");
                buy.addActionListener(e -> onPrimary.run(product));
            }
            gc.gridx = 0;
            gc.insets = new Insets(0, 0, 0, 4);
            actions.add(buy, gc);

            JButton details = new JButton(secondaryLabel);
            UiUtil.styleGhost(details);
            details.setToolTipText("See the full details of " + product.getProductName());
            details.addActionListener(e -> onSecondary.run(product));
            gc.gridx = 1;
            gc.insets = new Insets(0, 4, 0, 0);
            actions.add(details, gc);

            card.add(actions);
        }
        return card;
    }

    /**
     * Lays the cards out in a fixed-column grid inside {@code grid}, which is
     * switched to {@link GridBagLayout} and cleared first. Each product is
     * passed to {@code builder} so callers decide which buttons a card gets.
     */
    public static void fillGrid(JPanel grid, List<Product> products, CardBuilder builder) {
        fillGrid(grid, products, COLUMNS, builder);
    }

    /** Same as {@link #fillGrid(JPanel, List, CardBuilder)} with an explicit column count. */
    public static void fillGrid(JPanel grid, List<Product> products, int columns, CardBuilder builder) {
        grid.removeAll();
        grid.setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(8, 8, 18, 18);
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.NONE;
        for (int i = 0; i < products.size(); i++) {
            if (i > 0 && i % columns == 0) {
                gc.gridx = 0;
                gc.gridy++;
            }
            grid.add(builder.build(products.get(i)), gc);
            gc.gridx++;
        }
        // Soak up the leftover space so cards stay top-left aligned.
        gc.gridx = columns;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        grid.add(javax.swing.Box.createGlue(), gc);

        grid.revalidate();
        grid.repaint();
    }

    /**
     * The heart overlay. Shows a filled heart once the product is already in
     * the buyer's favorites, so the state is visible at a glance.
     */
    private static JButton favoriteButton(Product product, Action onFavorite) {
        boolean saved = anilink.session.SessionContext
                .findCartItem(product.getId()).isPresent();

        JButton heart = new JButton(saved ? "♥" : "♡");
        heart.setFont(new java.awt.Font(UiUtil.FONT_FAMILY, java.awt.Font.PLAIN, 17));
        heart.setForeground(saved ? UiUtil.DANGER : UiUtil.MUTED);
        heart.setBackground(UiUtil.WHITE);
        heart.setOpaque(true);
        heart.setFocusPainted(false);
        heart.setMargin(new Insets(0, 0, 0, 0));
        heart.setBorder(BorderFactory.createLineBorder(UiUtil.BORDER));
        heart.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        heart.setToolTipText(saved
                ? product.getProductName() + " is already in your favorites"
                : "Add " + product.getProductName() + " to favorites");
        Dimension size = new Dimension(30, 30);
        heart.setPreferredSize(size);
        heart.setMinimumSize(size);
        heart.setMaximumSize(size);
        heart.addActionListener(e -> onFavorite.run(product));
        return heart;
    }

    /** Builds the card for one product. */
    public interface CardBuilder {
        JPanel build(Product product);
    }

    private static void lockSize(javax.swing.JComponent c, int width, int height) {
        Dimension d = new Dimension(width, height);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
    }
}
