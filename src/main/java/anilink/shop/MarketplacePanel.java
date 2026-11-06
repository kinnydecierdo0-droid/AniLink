package anilink.shop;

import anilink.buyer.BuyNowDialog;
import anilink.data.CategoryDAO;
import anilink.data.ProductDAO;
import anilink.model.Category;
import anilink.model.Product;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.ui.ProductCardFactory;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MarketplacePanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private JButton btnHeaderCart;
    private JLabel lblEmpty;

    public MarketplacePanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        UiUtil.styleFieldLabel(lblCategoryCaption);
        UiUtil.styleFieldLabel(lblAvailabilityCaption);
        UiUtil.styleNote(lblCount);
        UiUtil.stylePrimary(btnSearch);
        UiUtil.styleTintedButton(btnClearFilters);
        txtSearch.setFont(UiUtil.plain(14));
        pnlFilterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtil.BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        scrollGrid.setBorder(BorderFactory.createEmptyBorder());
        scrollGrid.getViewport().setBackground(UiUtil.BG);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);
        buildHeader();
        buildEmptyState();
        refreshData();
    }

    /**
     * Fills the yellow header band. Built here rather than in the form so the
     * NetBeans designer cannot regenerate it away.
     */
    private void buildHeader() {
        pnlHeader.setLayout(new BorderLayout());
        pnlHeader.setBackground(UiUtil.ACCENT);
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiUtil.ACCENT_DARK),
                BorderFactory.createEmptyBorder(12, 24, 12, 16)));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Marketplace");
        title.setFont(UiUtil.bold(22));
        title.setForeground(UiUtil.ON_ACCENT);
        titles.add(title);

        JLabel subtitle = new JLabel("Fresh produce straight from local farmers");
        subtitle.setFont(UiUtil.plain(12));
        subtitle.setForeground(new java.awt.Color(99, 76, 10));
        titles.add(subtitle);

        pnlHeader.add(titles, BorderLayout.WEST);

        // Right corner: favorites shortcut (the notification bell lives in MainFrame's top bar).
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        btnHeaderCart = new JButton("Favorites (0)");
        btnHeaderCart.setFont(UiUtil.bold(12));
        btnHeaderCart.setForeground(UiUtil.ON_ACCENT);
        btnHeaderCart.setBackground(UiUtil.ACCENT);
        btnHeaderCart.setFocusPainted(false);
        btnHeaderCart.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnHeaderCart.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiUtil.ON_ACCENT),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        btnHeaderCart.setToolTipText("Go to your favorites");
        btnHeaderCart.addActionListener(e -> frame.showCard(Cards.FAVORITES));
        actions.add(btnHeaderCart);

        pnlHeader.add(actions, BorderLayout.EAST);
    }

    private void buildEmptyState() {
        lblEmpty = new JLabel("No products match your filters.", SwingConstants.CENTER);
        lblEmpty.setFont(UiUtil.bold(14));
        lblEmpty.setForeground(UiUtil.MUTED);
        lblEmpty.setPreferredSize(new Dimension(760, 160));
    }

    public void refreshData() {
        String previousAvailability = comboAvailability.getSelectedItem() == null
                ? "All" : comboAvailability.getSelectedItem().toString();
        comboAvailability.removeAllItems();
        comboAvailability.addItem("All");
        comboAvailability.addItem("In Stock");
        comboAvailability.addItem("Out of Stock");
        comboAvailability.setSelectedItem(previousAvailability);
        loadCategories();
        updateHeaderChrome();
        performSearch();
    }

    /**
     * Keeps the header cart count current. The notification bell now lives
     * once in MainFrame's shared top bar instead of being rebuilt per page.
     */
    private void updateHeaderChrome() {
        if (btnHeaderCart != null) {
            btnHeaderCart.setText("Favorites (" + SessionContext.cartCount() + ")");
            btnHeaderCart.setVisible(SessionContext.isRole("buyer"));
        }
    }

    private void loadCategories() {
        Object selected = comboCategory.getSelectedItem();
        int selectedId = -1;
        if (selected instanceof Category) {
            selectedId = ((Category) selected).getId();
        }
        comboCategory.removeAllItems();
        comboCategory.addItem("All Categories");
        try {
            for (Category category : new CategoryDAO().findAll()) {
                comboCategory.addItem(category);
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load categories: " + e.getMessage());
        }
        for (int i = 0; i < comboCategory.getItemCount(); i++) {
            Object item = comboCategory.getItemAt(i);
            if (item instanceof Category && ((Category) item).getId() == selectedId) {
                comboCategory.setSelectedIndex(i);
                return;
            }
        }
        comboCategory.setSelectedIndex(0);
    }

    private void performSearch() {
        Integer categoryId = null;
        Object category = comboCategory.getSelectedItem();
        if (category instanceof Category && ((Category) category).getId() > 0) {
            categoryId = ((Category) category).getId();
        }

        String availability = null;
        Object availabilityItem = comboAvailability.getSelectedItem();
        if (availabilityItem != null) {
            String value = availabilityItem.toString();
            if ("In Stock".equals(value)) {
                availability = "in_stock";
            } else if ("Out of Stock".equals(value)) {
                availability = "out_of_stock";
            }
        }

        String search = txtSearch.getText().trim();
        try {
            // Price filtering was removed from this page; the guest frames
            // still use the min/max arguments of the same DAO method.
            List<Product> products = new ProductDAO().search(search, categoryId, availability, null, null);
            fillGrid(products);
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load products: " + e.getMessage());
        }
    }

    private void fillGrid(List<Product> products) {
        lblCount.setText(products.size() + " product(s) found");
        if (products.isEmpty()) {
            pnlProductGrid.removeAll();
            pnlProductGrid.setLayout(new BorderLayout());
            pnlProductGrid.add(lblEmpty, BorderLayout.NORTH);
            pnlProductGrid.revalidate();
            pnlProductGrid.repaint();
            return;
        }
        ProductCardFactory.fillGrid(pnlProductGrid, products, product
                -> ProductCardFactory.card(product,
                        "Buy", this::openBuyNow,
                        "View Details", this::openDetails,
                        this::addToFavorites));
    }

    /** Heart action: saves the product to the buyer's favorites list. */
    private void addToFavorites(Product product) {
        if (!SessionContext.isLoggedIn() || !SessionContext.isRole("buyer")) {
            UiUtil.info(this, "Please log in as a buyer to save favorites.");
            return;
        }
        if (product.getQuantity() <= 0) {
            UiUtil.error(this, "This product is out of stock.");
            return;
        }
        if (SessionContext.findCartItem(product.getId()).isPresent()) {
            UiUtil.info(this, product.getProductName() + " is already in your favorites.");
            return;
        }
        SessionContext.addToCart(product, 1);
        frame.refreshChrome();
        updateHeaderChrome();
        performSearch();
        UiUtil.info(this, product.getProductName() + " was added to your favorites.");
    }

    private void openDetails(Product product) {
        new ProductDetailsDialog(frame, product.getId()).setVisible(true);
        // Stock may have changed while the modal was open.
        performSearch();
    }

    private void openBuyNow(Product product) {
        if (!SessionContext.isLoggedIn() || !SessionContext.isRole("buyer")) {
            UiUtil.info(this, "Please log in as a buyer to place an order.");
            return;
        }
        new BuyNowDialog(frame, product).setVisible(true);
        updateHeaderChrome();
        performSearch();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHeader = new javax.swing.JPanel();
        pnlFilterBar = new javax.swing.JPanel();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        lblCategoryCaption = new javax.swing.JLabel();
        comboCategory = new javax.swing.JComboBox<>();
        lblAvailabilityCaption = new javax.swing.JLabel();
        comboAvailability = new javax.swing.JComboBox<>();
        btnClearFilters = new javax.swing.JButton();
        pnlResultsBar = new javax.swing.JPanel();
        lblCount = new javax.swing.JLabel();
        scrollGrid = new javax.swing.JScrollPane();
        pnlProductGrid = new javax.swing.JPanel();

        setBackground(new java.awt.Color(248, 250, 248));

        pnlHeader.setBackground(new java.awt.Color(255, 193, 7));
        // Contents are built at runtime in buildHeader().

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 780, Short.MAX_VALUE)
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 76, Short.MAX_VALUE)
        );

        pnlFilterBar.setBackground(new java.awt.Color(255, 255, 255));

        txtSearch.setToolTipText("Search products by name or category, then press Enter");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setBackground(new java.awt.Color(46, 125, 50));
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnSearch.setFocusPainted(false);
        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        lblCategoryCaption.setText("Category");

        comboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All Categories"}));

        lblAvailabilityCaption.setText("Availability");

        comboAvailability.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All"}));

        btnClearFilters.setText("Clear Filters");
        btnClearFilters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearFiltersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFilterBarLayout = new javax.swing.GroupLayout(pnlFilterBar);
        pnlFilterBar.setLayout(pnlFilterBarLayout);
        pnlFilterBarLayout.setHorizontalGroup(
            pnlFilterBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterBarLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlFilterBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFilterBarLayout.createSequentialGroup()
                        .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                        .addGap(10, 10, 10)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFilterBarLayout.createSequentialGroup()
                        .addComponent(lblCategoryCaption)
                        .addGap(8, 8, 8)
                        .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(lblAvailabilityCaption)
                        .addGap(8, 8, 8)
                        .addComponent(comboAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, Short.MAX_VALUE)
                        .addComponent(btnClearFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12))
        );
        pnlFilterBarLayout.setVerticalGroup(
            pnlFilterBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterBarLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlFilterBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(pnlFilterBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCategoryCaption, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAvailabilityCaption, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClearFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12))
        );

        lblCount.setForeground(new java.awt.Color(96, 125, 107));
        lblCount.setText("0 product(s) found");

        javax.swing.GroupLayout pnlResultsBarLayout = new javax.swing.GroupLayout(pnlResultsBar);
        pnlResultsBar.setLayout(pnlResultsBarLayout);
        pnlResultsBarLayout.setHorizontalGroup(
            pnlResultsBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultsBarLayout.createSequentialGroup()
                .addComponent(lblCount, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE))
        );
        pnlResultsBarLayout.setVerticalGroup(
            pnlResultsBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResultsBarLayout.createSequentialGroup()
                .addComponent(lblCount))
        );

        scrollGrid.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnlProductGrid.setBackground(new java.awt.Color(248, 250, 248));
        // Cards are added at runtime by ProductCardFactory.fillGrid().

        javax.swing.GroupLayout pnlProductGridLayout = new javax.swing.GroupLayout(pnlProductGrid);
        pnlProductGrid.setLayout(pnlProductGridLayout);
        pnlProductGridLayout.setHorizontalGroup(
            pnlProductGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 780, Short.MAX_VALUE)
        );
        pnlProductGridLayout.setVerticalGroup(
            pnlProductGridLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 340, Short.MAX_VALUE)
        );

        scrollGrid.setViewportView(pnlProductGrid);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilterBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlResultsBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlFilterBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlResultsBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollGrid, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {
        performSearch();
    }

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {
        performSearch();
    }

    private void btnClearFiltersActionPerformed(java.awt.event.ActionEvent evt) {
        txtSearch.setText("");
        comboCategory.setSelectedIndex(0);
        comboAvailability.setSelectedItem("All");
        performSearch();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearFilters;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> comboAvailability;
    private javax.swing.JComboBox<Object> comboCategory;
    private javax.swing.JLabel lblAvailabilityCaption;
    private javax.swing.JLabel lblCategoryCaption;
    private javax.swing.JLabel lblCount;
    private javax.swing.JPanel pnlFilterBar;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlProductGrid;
    private javax.swing.JPanel pnlResultsBar;
    private javax.swing.JScrollPane scrollGrid;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
