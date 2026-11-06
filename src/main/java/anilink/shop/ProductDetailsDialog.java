package anilink.shop;

import anilink.buyer.BuyNowDialog;
import anilink.data.ProductDAO;
import anilink.data.ReviewDAO;
import anilink.model.Product;
import anilink.model.Review;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Full product details in a modal, opened by the marketplace card's
 * "View Details" button. Re-reads the product so stock is never stale.
 *
 * <p>Hand-coded rather than a NetBeans form so the designer cannot regenerate
 * it. The existing {@code ProductDetailPanel} card is untouched and still
 * reachable from the cart and order history.
 */
public class ProductDetailsDialog extends JDialog {

    private final MainFrame frame;
    private Product product;

    public ProductDetailsDialog(MainFrame frame, int productId) {
        super(frame, "Product Details", ModalityType.APPLICATION_MODAL);
        this.frame = frame;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        try {
            Optional<Product> found = new ProductDAO().findById(productId);
            product = found.orElse(null);
        } catch (SQLException e) {
            product = null;
            UiUtil.error(frame, "Failed to load the product: " + e.getMessage());
        }

        if (product == null) {
            add(buildNotFound(), BorderLayout.CENTER);
            add(buildFooter(), BorderLayout.SOUTH);
            setSize(420, 220);
            setLocationRelativeTo(frame);
            return;
        }

        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UiUtil.BG);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(780, 620);
        setLocationRelativeTo(frame);
    }

    private JPanel buildNotFound() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiUtil.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
        JLabel message = new JLabel("This product is no longer available.", SwingConstants.CENTER);
        message.setFont(UiUtil.bold(14));
        message.setForeground(UiUtil.MUTED);
        panel.add(message, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel(product.getProductName());
        title.setFont(UiUtil.bold(18));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel(product.getCategoryName() == null
                ? "Product details" : product.getCategoryName());
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(new java.awt.Color(220, 237, 221));
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setBackground(UiUtil.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        body.add(buildOverviewCard());
        body.add(UiUtil.vgap(14));
        body.add(buildDescriptionCard());
        body.add(UiUtil.vgap(14));
        body.add(buildReviewsCard());
        body.add(javax.swing.Box.createVerticalGlue());
        return body;
    }

    private JPanel buildOverviewCard() {
        JPanel card = UiUtil.card();
        card.setLayout(new BorderLayout(16, 0));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel image = new JLabel(UiUtil.loadProductIcon(product.getImage(), 320, 240),
                SwingConstants.CENTER);
        image.setOpaque(true);
        image.setBackground(UiUtil.TINT);
        image.setPreferredSize(new Dimension(320, 240));
        card.add(image, BorderLayout.WEST);

        JPanel facts = new JPanel(new GridBagLayout());
        facts.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(4, 0, 4, 10);
        gc.gridy = 0;

        JLabel price = new JLabel(UiUtil.formatPrice(product.getPrice())
                + " / " + product.getUnit());
        price.setFont(UiUtil.bold(20));
        price.setForeground(UiUtil.PRIMARY);
        gc.gridx = 0;
        gc.gridwidth = 2;
        facts.add(price, gc);
        gc.gridwidth = 1;
        gc.gridy++;

        boolean outOfStock = product.getQuantity() <= 0;
        JLabel stock = new JLabel(outOfStock
                ? "Out of Stock"
                : product.getQuantity() + " " + product.getUnit() + " available");
        stock.setFont(UiUtil.bold(13));
        stock.setForeground(outOfStock ? UiUtil.DANGER : UiUtil.DARK);
        gc.gridx = 0;
        gc.gridwidth = 2;
        facts.add(stock, gc);
        gc.gridwidth = 1;
        gc.gridy++;

        addFact(facts, gc, "Category", product.getCategoryName());
        addFact(facts, gc, "Status", UiUtil.statusBadge(product.getStatus()));
        addFact(facts, gc, "Farmer", product.getFarmerName());
        addFact(facts, gc, "Farm Address", product.getFarmAddress());

        card.add(facts, BorderLayout.CENTER);
        return card;
    }

    private void addFact(JPanel facts, GridBagConstraints gc, String caption, String value) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(UiUtil.bold(12));
        captionLabel.setForeground(UiUtil.MUTED);
        gc.gridx = 0;
        facts.add(captionLabel, gc);

        JLabel valueLabel = new JLabel(UiUtil.wrapHtml(
                value == null || value.trim().isEmpty() ? "-" : value, 240));
        valueLabel.setFont(UiUtil.plain(13));
        valueLabel.setForeground(UiUtil.DARK);
        gc.gridx = 1;
        facts.add(valueLabel, gc);
        gc.gridy++;
    }

    private JPanel buildDescriptionCard() {
        JPanel card = UiUtil.sectionCard("Description");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        String description = product.getDescription();
        JLabel text = new JLabel(UiUtil.wrapHtml(
                description == null || description.trim().isEmpty()
                        ? "The farmer has not added a description for this product."
                        : description, 680));
        text.setFont(UiUtil.plain(13));
        text.setForeground(description == null || description.trim().isEmpty()
                ? UiUtil.MUTED : UiUtil.DARK);
        text.setVerticalAlignment(SwingConstants.TOP);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildReviewsCard() {
        List<Review> reviews = new ArrayList<>();
        try {
            reviews = new ReviewDAO().findByProduct(product.getId());
        } catch (SQLException e) {
            // Reviews are supplementary; the dialog still shows the product.
        }

        JPanel card = UiUtil.sectionCard(reviews.isEmpty()
                ? "Reviews" : "Reviews (" + reviews.size() + ")");
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (reviews.isEmpty()) {
            JLabel empty = new JLabel("No reviews for this product yet.");
            empty.setFont(UiUtil.plain(13));
            empty.setForeground(UiUtil.MUTED);
            list.add(empty);
        } else {
            for (Review review : reviews) {
                list.add(buildReviewRow(review));
                list.add(UiUtil.vgap(8));
            }
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildReviewRow(Review review) {
        JPanel row = new JPanel(new BorderLayout(0, 2));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, UiUtil.TINT),
                BorderFactory.createEmptyBorder(2, 10, 2, 0)));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);

        JLabel stars = new JLabel(starsFor(review.getRating()));
        stars.setFont(UiUtil.bold(13));
        stars.setForeground(UiUtil.ACCENT_DARK);
        top.add(stars);

        JLabel who = new JLabel(review.getBuyerName() == null ? "Buyer" : review.getBuyerName());
        who.setFont(UiUtil.bold(12));
        who.setForeground(UiUtil.DARK);
        top.add(who);

        JLabel when = new JLabel(UiUtil.formatDate(review.getCreatedAt()));
        when.setFont(UiUtil.plain(11));
        when.setForeground(UiUtil.MUTED);
        top.add(when);
        row.add(top, BorderLayout.NORTH);

        String text = review.getReview();
        if (text != null && !text.trim().isEmpty()) {
            JLabel body = new JLabel(UiUtil.wrapHtml(text, 660));
            body.setFont(UiUtil.plain(12));
            body.setForeground(UiUtil.DARK);
            row.add(body, BorderLayout.CENTER);
        }
        return row;
    }

    private static String starsFor(int rating) {
        int stars = Math.max(0, Math.min(5, rating));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stars; i++) {
            sb.append('★');
        }
        for (int i = stars; i < 5; i++) {
            sb.append('☆');
        }
        return sb.toString();
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton close = new JButton("Close");
        UiUtil.styleTintedButton(close);
        close.setPreferredSize(new Dimension(110, 36));
        close.addActionListener(e -> dispose());
        footer.add(close);

        if (product != null) {
            JButton buy = new JButton("Buy Now");
            UiUtil.styleAccent(buy);
            buy.setPreferredSize(new Dimension(140, 36));
            if (product.getQuantity() <= 0) {
                buy.setEnabled(false);
                buy.setToolTipText("Out of stock");
            } else {
                buy.addActionListener(e -> {
                    if (!SessionContext.isRole("buyer")) {
                        UiUtil.info(this, "Please log in as a buyer to place an order.");
                        return;
                    }
                    // Chain into the buy modal, as described in the flow.
                    dispose();
                    new BuyNowDialog(frame, product).setVisible(true);
                });
            }
            footer.add(buy);
        }
        return footer;
    }
}
