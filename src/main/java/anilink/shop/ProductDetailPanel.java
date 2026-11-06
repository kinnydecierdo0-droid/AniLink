package anilink.shop;

import anilink.data.ProductDAO;
import anilink.data.ReviewDAO;
import anilink.model.Product;
import anilink.model.Review;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;

public class ProductDetailPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private int productId;
    private Product currentProduct;

    public ProductDetailPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        ((DefaultTableModel) reviewsTable.getModel()).setColumnIdentifiers(
                new Object[]{"Rating", "Buyer", "Review", "Date"});
        reviewsTable.setDefaultEditor(Object.class, null);
        btnAddToCart.setEnabled(false);
        btnBuyNow.setEnabled(false);
        lblImage.setIcon(UiUtil.loadProductIcon(null, 280, 200));
        UiUtil.styleSection(lblReviewsTitle);
        UiUtil.styleFieldLabel(lblFarmer);
        UiUtil.styleFieldLabel(lblFarmAddress);
        UiUtil.styleFieldLabel(lblDescCaption);
        UiUtil.styleFieldLabel(lblQtyCaption);
        UiUtil.styleStrip(pnlMain);
        UiUtil.styleStrip(pnlDetails);
        UiUtil.styleStrip(pnlQtyRow);
        UiUtil.styleTintedButton(btnBack);
        UiUtil.styleButton(btnAddToCart);
        UiUtil.styleTintedButton(btnBuyNow);
        UiUtil.styleTable(reviewsTable);
    }

    public void refreshData() {
        if (productId > 0) {
            loadProduct(productId);
        }
    }

    public void loadProduct(int productId) {
        this.productId = productId;
        try {
            Optional<Product> found = new ProductDAO().findById(productId);
            if (found.isEmpty()) {
                showNotFound();
                return;
            }
            currentProduct = found.get();
            populate(currentProduct);
            loadReviews(productId);
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load product: " + e.getMessage());
        }
    }

    private void showNotFound() {
        currentProduct = null;
        lblImage.setIcon(UiUtil.loadProductIcon(null, 280, 200));
        lblName.setText("Product not found");
        lblCategory.setText("-");
        lblPrice.setText("");
        lblStock.setText("The product may have been removed or is no longer available.");
        lblFarmer.setText("");
        lblFarmAddress.setText("");
        txtDescription.setText("");
        btnAddToCart.setEnabled(false);
        btnBuyNow.setEnabled(false);
        spinQty.setEnabled(false);
        spinQty.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1, 1));
        DefaultTableModel model = (DefaultTableModel) reviewsTable.getModel();
        model.setRowCount(0);
    }

    private void populate(Product product) {
        lblImage.setIcon(UiUtil.loadProductIcon(product.getImage(), 280, 200));
        lblName.setText(product.getProductName());
        lblCategory.setText(product.getCategoryName() == null ? "Other" : product.getCategoryName());
        lblPrice.setText(UiUtil.formatPrice(product.getPrice()) + "  /  " + product.getUnit());
        lblStock.setText("Stock: " + product.getQuantity() + " " + product.getUnit()
                + "   |   " + UiUtil.statusBadge(product.getStatus()));
        lblFarmer.setText("Farmer: " + (product.getFarmerName() == null ? "" : product.getFarmerName()));
        lblFarmAddress.setText("Farm Address: "
                + (product.getFarmAddress() == null || product.getFarmAddress().isEmpty()
                        ? "Not specified" : product.getFarmAddress()));
        txtDescription.setText(product.getDescription() == null ? "" : product.getDescription());
        txtDescription.setCaretPosition(0);

        boolean canBuy = "available".equals(product.getStatus()) && product.getQuantity() > 0;
        btnAddToCart.setEnabled(canBuy);
        btnBuyNow.setEnabled(canBuy);
        spinQty.setEnabled(canBuy);
        int maxQty = Math.max(1, product.getQuantity());
        spinQty.setModel(new javax.swing.SpinnerNumberModel(1, 1, maxQty, 1));
    }

    private void loadReviews(int productId) throws SQLException {
        DefaultTableModel model = (DefaultTableModel) reviewsTable.getModel();
        model.setRowCount(0);
        List<Review> reviews = new ReviewDAO().findByProduct(productId);
        for (Review review : reviews) {
            model.addRow(new Object[]{
                review.stars(),
                review.getBuyerName(),
                review.getReview(),
                UiUtil.formatDate(review.getCreatedAt())
            });
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnBack = new javax.swing.JButton();
        pnlMain = new javax.swing.JPanel();
        lblImage = new javax.swing.JLabel();
        pnlDetails = new javax.swing.JPanel();
        lblName = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        lblPrice = new javax.swing.JLabel();
        lblStock = new javax.swing.JLabel();
        lblFarmer = new javax.swing.JLabel();
        lblFarmAddress = new javax.swing.JLabel();
        lblDescCaption = new javax.swing.JLabel();
        scrollDesc = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        pnlQtyRow = new javax.swing.JPanel();
        lblQtyCaption = new javax.swing.JLabel();
        spinQty = new javax.swing.JSpinner();
        btnAddToCart = new javax.swing.JButton();
        btnBuyNow = new javax.swing.JButton();
        lblReviewsTitle = new javax.swing.JLabel();
        scrollReviews = new javax.swing.JScrollPane();
        reviewsTable = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        btnBack.setForeground(new java.awt.Color(46, 125, 50));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnBack.setText("← Back to Marketplace");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        lblImage.setBackground(new java.awt.Color(232, 245, 233));
        lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImage.setOpaque(true);
        lblImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        lblImage.setPreferredSize(new java.awt.Dimension(280, 200));

        lblName.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblName.setForeground(new java.awt.Color(27, 94, 32));
        lblName.setText("Product Name");

        lblCategory.setBackground(new java.awt.Color(232, 245, 233));
        lblCategory.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblCategory.setForeground(new java.awt.Color(46, 125, 50));
        lblCategory.setOpaque(true);
        lblCategory.setText("Category");
        lblCategory.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblPrice.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblPrice.setForeground(new java.awt.Color(46, 125, 50));
        lblPrice.setText("₱0.00");

        lblStock.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblStock.setText("Stock: 0");

        lblFarmer.setForeground(new java.awt.Color(55, 71, 59));
        lblFarmer.setText("Farmer:");

        lblFarmAddress.setForeground(new java.awt.Color(55, 71, 59));
        lblFarmAddress.setText("Farm Address:");

        lblDescCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblDescCaption.setText("Description");

        txtDescription.setColumns(20);
        txtDescription.setRows(5);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        scrollDesc.setViewportView(txtDescription);

        lblQtyCaption.setText("Quantity");

        btnAddToCart.setBackground(new java.awt.Color(46, 125, 50));
        btnAddToCart.setForeground(new java.awt.Color(255, 255, 255));
        btnAddToCart.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.setText("Add to Cart");
        btnAddToCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddToCartActionPerformed(evt);
            }
        });

        btnBuyNow.setBackground(new java.awt.Color(232, 245, 233));
        btnBuyNow.setForeground(new java.awt.Color(27, 94, 32));
        btnBuyNow.setFont(new java.awt.Font("Segoe UI", 1, 13));
        btnBuyNow.setFocusPainted(false);
        btnBuyNow.setText("Buy Now");
        btnBuyNow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuyNowActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlQtyRowLayout = new javax.swing.GroupLayout(pnlQtyRow);
        pnlQtyRow.setLayout(pnlQtyRowLayout);
        pnlQtyRowLayout.setHorizontalGroup(
            pnlQtyRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQtyRowLayout.createSequentialGroup()
                .addComponent(lblQtyCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinQty, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, Short.MAX_VALUE)
                .addComponent(btnAddToCart, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnBuyNow, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlQtyRowLayout.setVerticalGroup(
            pnlQtyRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQtyRowLayout.createSequentialGroup()
                .addGroup(pnlQtyRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblQtyCaption)
                    .addComponent(spinQty, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddToCart, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuyNow, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout pnlDetailsLayout = new javax.swing.GroupLayout(pnlDetails);
        pnlDetails.setLayout(pnlDetailsLayout);
        pnlDetailsLayout.setHorizontalGroup(
            pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(lblCategory, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(lblPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(lblStock, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(lblFarmer, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(lblFarmAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(lblDescCaption, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(scrollDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
            .addComponent(pnlQtyRow, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
        );
        pnlDetailsLayout.setVerticalGroup(
            pnlDetailsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailsLayout.createSequentialGroup()
                .addComponent(lblName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPrice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStock)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFarmer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFarmAddress)
                .addGap(12, 12, 12)
                .addComponent(lblDescCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(pnlQtyRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(pnlDetails, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        lblReviewsTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblReviewsTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblReviewsTitle.setText("Customer Reviews");

        reviewsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {

            },
            new String[] {

            }
        ));
        scrollReviews.setViewportView(reviewsTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollReviews, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblReviewsTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(lblReviewsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollReviews, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.MARKETPLACE);
    }

    private void btnAddToCartActionPerformed(java.awt.event.ActionEvent evt) {
        if (!validatePurchase()) {
            return;
        }
        int qty = ((Number) spinQty.getValue()).intValue();
        SessionContext.addToCart(currentProduct, qty);
        UiUtil.info(this, "Product added to cart!");
        frame.refreshChrome();
    }

    private void btnBuyNowActionPerformed(java.awt.event.ActionEvent evt) {
        if (!validatePurchase()) {
            return;
        }
        int qty = ((Number) spinQty.getValue()).intValue();
        SessionContext.addToCart(currentProduct, qty);
        frame.refreshChrome();
        frame.openCheckout();
    }

    private boolean validatePurchase() {
        if (currentProduct == null) {
            UiUtil.error(this, "Product not found.");
            return false;
        }
        if (!SessionContext.isLoggedIn()) {
            UiUtil.info(this, "Please log in as a buyer");
            return false;
        }
        if (!SessionContext.isRole("buyer")) {
            UiUtil.info(this, "Only buyers can purchase products.");
            return false;
        }
        int qty = ((Number) spinQty.getValue()).intValue();
        if (qty < 1) {
            UiUtil.error(this, "Quantity must be at least 1.");
            return false;
        }
        Product fresh;
        try {
            Optional<Product> found = new ProductDAO().findById(currentProduct.getId());
            if (found.isEmpty()) {
                showNotFound();
                UiUtil.error(this, "Product is no longer available.");
                return false;
            }
            fresh = found.get();
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load product: " + e.getMessage());
            return false;
        }
        if (!"available".equals(fresh.getStatus()) || fresh.getQuantity() <= 0) {
            UiUtil.error(this, "This product is no longer available.");
            return false;
        }
        int inCart = SessionContext.findCartItem(fresh.getId())
                .map(item -> item.getQuantity()).orElse(0);
        if (inCart + qty > fresh.getQuantity()) {
            UiUtil.error(this, "Insufficient stock. Only " + fresh.getQuantity() + " "
                    + fresh.getUnit() + " available"
                    + (inCart > 0 ? " (" + inCart + " already in your cart)." : "."));
            return false;
        }
        currentProduct = fresh;
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddToCart;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnBuyNow;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblDescCaption;
    private javax.swing.JLabel lblFarmAddress;
    private javax.swing.JLabel lblFarmer;
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblQtyCaption;
    private javax.swing.JLabel lblReviewsTitle;
    private javax.swing.JLabel lblStock;
    private javax.swing.JPanel pnlDetails;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlQtyRow;
    private javax.swing.JTable reviewsTable;
    private javax.swing.JScrollPane scrollDesc;
    private javax.swing.JScrollPane scrollReviews;
    private javax.swing.JSpinner spinQty;
    private javax.swing.JTextArea txtDescription;
    // End of variables declaration//GEN-END:variables
}
