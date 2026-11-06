package anilink.farmer;

import anilink.model.Product;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Read-only product details for the farmer's "View Details" button on
 * Manage Products - the product's image plus its full facts, same build
 * pattern as {@link anilink.shop.ProductDetailsDialog} but without the buyer
 * "Buy Now" footer.
 */
public class FarmerProductViewDialog extends JDialog {

    private final Product product;

    public FarmerProductViewDialog(JFrame frame, Product product) {
        super(frame, "Product Details", ModalityType.APPLICATION_MODAL);
        this.product = product;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildBody());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(UiUtil.BG);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(640, 520);
        setMinimumSize(new Dimension(520, 420));
        setLocationRelativeTo(frame);
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
        body.add(javax.swing.Box.createVerticalGlue());
        return body;
    }

    private JPanel buildOverviewCard() {
        JPanel card = UiUtil.card();
        card.setLayout(new BorderLayout(16, 0));
        card.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel image = new JLabel(UiUtil.loadProductIcon(product.getImage(), 240, 200),
                SwingConstants.CENTER);
        image.setOpaque(true);
        image.setBackground(UiUtil.TINT);
        image.setPreferredSize(new Dimension(240, 200));
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

        addFact(facts, gc, "Quantity", product.getQuantity() + " " + product.getUnit());
        addFact(facts, gc, "Category", product.getCategoryName());
        addFact(facts, gc, "Status", UiUtil.statusBadge(product.getStatus()));
        addFact(facts, gc, "Date Added", UiUtil.formatDate(product.getCreatedAt()));

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
                        ? "No description added yet." : description, 540));
        text.setFont(UiUtil.plain(13));
        text.setForeground(description == null || description.trim().isEmpty()
                ? UiUtil.MUTED : UiUtil.DARK);
        text.setVerticalAlignment(SwingConstants.TOP);
        card.add(text, BorderLayout.CENTER);
        return card;
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
        return footer;
    }
}
