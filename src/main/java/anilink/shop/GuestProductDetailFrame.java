package anilink.shop;

import anilink.data.FarmerDAO;
import anilink.data.ProductDAO;
import anilink.data.ReviewDAO;
import anilink.model.Farmer;
import anilink.model.Product;
import anilink.model.Review;
import anilink.session.SessionContext;
import anilink.auth.LoginForm;
import anilink.auth.RegisterForm;
import anilink.home.GuestHomeFrame;
import anilink.util.UiUtil;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.swing.table.DefaultTableModel;

public class GuestProductDetailFrame extends javax.swing.JFrame {

    private final int productId;
    private Product currentProduct;

    public GuestProductDetailFrame(int productId) {
        this(productId, null);
    }

    public GuestProductDetailFrame(int productId, GuestMarketplaceFrame marketplaceFrame) {
        this.productId = productId;
        initComponents();
        ((DefaultTableModel) reviewsTable.getModel()).setColumnIdentifiers(
                new Object[]{"Rating", "Buyer", "Review", "Date"});
        reviewsTable.setDefaultEditor(Object.class, null);
        UiUtil.styleTable(reviewsTable);
        loadProduct();
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    public void refreshData() {
        loadProduct();
    }

    private void loadProduct() {
        try {
            Optional<Product> found = new ProductDAO().findById(productId);
            if (found.isEmpty()) {
                showNotFound();
                return;
            }
            currentProduct = found.get();
            populate(currentProduct);
            loadReviews();
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load product: " + e.getMessage());
        }
    }

    private void showNotFound() {
        currentProduct = null;
        lblImage.setIcon(UiUtil.loadProductIcon(null, 320, 240));
        lblName.setText("Product not found");
        lblCategory.setText("-");
        lblPrice.setText("");
        lblStock.setText("The product may have been removed or is no longer available.");
        lblFarmer.setText("");
        lblFarmAddress.setText("");
        lblContact.setText("");
        txtDescription.setText("");
        btnAddToCart.setEnabled(false);
        spinQty.setEnabled(false);
        DefaultTableModel model = (DefaultTableModel) reviewsTable.getModel();
        model.setRowCount(0);
    }

    private void populate(Product product) {
        lblImage.setIcon(UiUtil.loadProductIcon(product.getImage(), 320, 240));
        lblName.setText(product.getProductName());
        String category = product.getCategoryName() == null ? "Other" : product.getCategoryName();
        lblCategory.setText(category);
        lblBreadcrumb.setText("Home  /  Marketplace  /  " + product.getProductName());
        setTitle("AniLink - " + product.getProductName());
        lblPrice.setText(UiUtil.formatPrice(product.getPrice()) + "  /  " + product.getUnit());

        if (product.getQuantity() > 0) {
            lblStock.setText(product.getQuantity() + " " + product.getUnit() + " in stock   |   "
                    + UiUtil.statusBadge(product.getStatus()));
            lblStock.setForeground(new Color(46, 125, 50));
        } else {
            lblStock.setText("Out of Stock");
            lblStock.setForeground(new Color(211, 47, 47));
        }

        lblFarmer.setText("Farmer: " + (product.getFarmerName() == null ? "-" : product.getFarmerName()));
        lblFarmAddress.setText("Farm Location: "
                + (product.getFarmAddress() == null || product.getFarmAddress().isEmpty()
                        ? "Not specified" : product.getFarmAddress()));

        String contact = "Not specified";
        try {
            Optional<Farmer> farmer = new FarmerDAO().findById(product.getFarmerId());
            if (farmer.isPresent() && farmer.get().getContactNumber() != null
                    && !farmer.get().getContactNumber().isEmpty()) {
                contact = farmer.get().getContactNumber();
            }
        } catch (SQLException ignored) {
        }
        lblContact.setText("Contact: " + contact);

        txtDescription.setText(product.getDescription() == null ? "" : product.getDescription());
        txtDescription.setCaretPosition(0);

        boolean canBuy = "available".equals(product.getStatus()) && product.getQuantity() > 0;
        btnAddToCart.setEnabled(canBuy);
        spinQty.setEnabled(canBuy);
        int maxQty = Math.max(1, product.getQuantity());
        spinQty.setModel(new javax.swing.SpinnerNumberModel(1, 1, maxQty, 1));
    }

    private void loadReviews() throws SQLException {
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

    private void goMarketplace() {
        GuestMarketplaceFrame marketplace = new GuestMarketplaceFrame();
        marketplace.setLocationRelativeTo(null);
        marketplace.setVisible(true);
        dispose();
    }

    private void openHome() {
        GuestHomeFrame home = new GuestHomeFrame();
        home.setLocationRelativeTo(null);
        home.setVisible(true);
        dispose();
    }

    private void openLogin() {
        LoginForm login = new LoginForm(null);
        login.setLocationRelativeTo(this);
        login.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (SessionContext.isLoggedIn()) {
                    dispose();
                }
            }
        });
        login.setVisible(true);
    }

    private void openRegister() {
        RegisterForm register = new RegisterForm(null, true);
        register.setLocationRelativeTo(this);
        register.setVisible(true);
        if (SessionContext.isLoggedIn()) {
            dispose();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlNavbar = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblBrand = new javax.swing.JLabel();
        btnHome = new javax.swing.JButton();
        btnMarketplace = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        scrollBody = new javax.swing.JScrollPane();
        pnlBody = new javax.swing.JPanel();
        btnBack = new javax.swing.JButton();
        lblBreadcrumb = new javax.swing.JLabel();
        pnlMain = new javax.swing.JPanel();
        lblImage = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        lblPrice = new javax.swing.JLabel();
        lblStock = new javax.swing.JLabel();
        lblFarmer = new javax.swing.JLabel();
        lblFarmAddress = new javax.swing.JLabel();
        lblContact = new javax.swing.JLabel();
        lblDescCaption = new javax.swing.JLabel();
        scrollDesc = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        pnlQtyRow = new javax.swing.JPanel();
        lblQtyCaption = new javax.swing.JLabel();
        spinQty = new javax.swing.JSpinner();
        btnAddToCart = new javax.swing.JButton();
        lblReviewsTitle = new javax.swing.JLabel();
        scrollReviews = new javax.swing.JScrollPane();
        reviewsTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("AniLink - Product Details");
        setMinimumSize(new java.awt.Dimension(1100, 720));

        pnlNavbar.setBackground(new java.awt.Color(46, 125, 50));
        pnlNavbar.setPreferredSize(new java.awt.Dimension(1100, 56));

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/anilink-logo-40.png"))); // NOI18N

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblBrand.setForeground(new java.awt.Color(255, 255, 255));
        lblBrand.setText("AniLink");

        btnHome.setForeground(new java.awt.Color(255, 255, 255));
        btnHome.setText("Home");
        btnHome.setBorderPainted(false);
        btnHome.setContentAreaFilled(false);
        btnHome.setFocusPainted(false);
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });

        btnMarketplace.setForeground(new java.awt.Color(255, 255, 255));
        btnMarketplace.setText("Marketplace");
        btnMarketplace.setBorderPainted(false);
        btnMarketplace.setContentAreaFilled(false);
        btnMarketplace.setFocusPainted(false);
        btnMarketplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarketplaceActionPerformed(evt);
            }
        });

        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Login");
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnRegister.setBackground(new java.awt.Color(255, 255, 255));
        btnRegister.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnRegister.setForeground(new java.awt.Color(46, 125, 50));
        btnRegister.setText("Register");
        btnRegister.setFocusPainted(false);
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlNavbarLayout = new javax.swing.GroupLayout(pnlNavbar);
        pnlNavbar.setLayout(pnlNavbarLayout);
        pnlNavbarLayout.setHorizontalGroup(
            pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNavbarLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblBrand)
                .addGap(28, 28, 28)
                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMarketplace, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 342, Short.MAX_VALUE)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
        pnlNavbarLayout.setVerticalGroup(
            pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNavbarLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBrand)
                    .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMarketplace, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        scrollBody.setBorder(null);

        pnlBody.setBackground(new java.awt.Color(248, 250, 248));

        btnBack.setForeground(new java.awt.Color(46, 125, 50));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnBack.setText("Back to Marketplace");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        lblBreadcrumb.setForeground(new java.awt.Color(96, 125, 107));
        lblBreadcrumb.setText("Home  /  Marketplace  /  Product");

        pnlMain.setOpaque(false);

        lblImage.setBackground(new java.awt.Color(232, 245, 233));
        lblImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImage.setOpaque(true);
        lblImage.setPreferredSize(new java.awt.Dimension(320, 240));

        lblCategory.setBackground(new java.awt.Color(232, 245, 233));
        lblCategory.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblCategory.setForeground(new java.awt.Color(46, 125, 50));
        lblCategory.setOpaque(true);
        lblCategory.setText("Category");
        lblCategory.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblName.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblName.setForeground(new java.awt.Color(27, 94, 32));
        lblName.setText("Product Name");

        lblPrice.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblPrice.setForeground(new java.awt.Color(46, 125, 50));
        lblPrice.setText("Price");

        lblStock.setText("Stock: 0");

        lblFarmer.setForeground(new java.awt.Color(55, 71, 59));
        lblFarmer.setText("Farmer:");

        lblFarmAddress.setForeground(new java.awt.Color(55, 71, 59));
        lblFarmAddress.setText("Farm Location:");

        lblContact.setForeground(new java.awt.Color(55, 71, 59));
        lblContact.setText("Contact:");

        lblDescCaption.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblDescCaption.setText("Description");

        txtDescription.setColumns(20);
        txtDescription.setRows(5);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        scrollDesc.setViewportView(txtDescription);

        pnlQtyRow.setOpaque(false);

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

        javax.swing.GroupLayout pnlQtyRowLayout = new javax.swing.GroupLayout(pnlQtyRow);
        pnlQtyRow.setLayout(pnlQtyRowLayout);
        pnlQtyRowLayout.setHorizontalGroup(
            pnlQtyRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQtyRowLayout.createSequentialGroup()
                .addComponent(lblQtyCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spinQty, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(btnAddToCart, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 200, Short.MAX_VALUE))
        );
        pnlQtyRowLayout.setVerticalGroup(
            pnlQtyRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlQtyRowLayout.createSequentialGroup()
                .addGroup(pnlQtyRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQtyCaption)
                    .addComponent(spinQty, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddToCart, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout pnlMainLayout = new javax.swing.GroupLayout(pnlMain);
        pnlMain.setLayout(pnlMainLayout);
        pnlMainLayout.setHorizontalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addGroup(pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblName, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(lblPrice, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(lblStock, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(lblFarmer, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(lblFarmAddress, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(lblContact, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(lblDescCaption, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(scrollDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                    .addComponent(pnlQtyRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnlMainLayout.setVerticalGroup(
            pnlMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblImage, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(pnlMainLayout.createSequentialGroup()
                .addComponent(lblCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPrice)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStock)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFarmer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFarmAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblContact)
                .addGap(12, 12, 12)
                .addComponent(lblDescCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlQtyRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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
        reviewsTable.setPreferredScrollableViewportSize(new java.awt.Dimension(760, 200));
        scrollReviews.setViewportView(reviewsTable);

        javax.swing.GroupLayout pnlBodyLayout = new javax.swing.GroupLayout(pnlBody);
        pnlBody.setLayout(pnlBodyLayout);
        pnlBodyLayout.setHorizontalGroup(
            pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBreadcrumb, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                    .addComponent(pnlMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblReviewsTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                    .addComponent(scrollReviews, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        pnlBodyLayout.setVerticalGroup(
            pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(btnBack, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblBreadcrumb)
                .addGap(12, 12, 12)
                .addComponent(pnlMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(lblReviewsTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollReviews, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        scrollBody.setViewportView(pnlBody);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlNavbar, javax.swing.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
            .addComponent(scrollBody)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlNavbar, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollBody))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {
        openHome();
    }

    private void btnMarketplaceActionPerformed(java.awt.event.ActionEvent evt) {
        goMarketplace();
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        openLogin();
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        openRegister();
    }

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {
        goMarketplace();
    }

    private void btnAddToCartActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentProduct == null) {
            UiUtil.error(this, "Product not found.");
            return;
        }
        if (!SessionContext.isLoggedIn()) {
            UiUtil.info(this, "Please log in as a buyer to add products to cart.");
            openLogin();
            return;
        }
        if (!SessionContext.isRole("buyer")) {
            UiUtil.info(this, "Only buyers can add products to cart.");
            return;
        }
        int qty = ((Number) spinQty.getValue()).intValue();
        if (qty < 1) {
            UiUtil.error(this, "Quantity must be at least 1.");
            return;
        }
        if (qty > currentProduct.getQuantity()) {
            UiUtil.error(this, "Insufficient stock. Only " + currentProduct.getQuantity()
                    + " " + currentProduct.getUnit() + " is currently available.");
            return;
        }
        SessionContext.addToCart(currentProduct, qty);
        UiUtil.info(this, "Product added to cart!");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddToCart;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnMarketplace;
    private javax.swing.JButton btnRegister;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblBreadcrumb;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblContact;
    private javax.swing.JLabel lblDescCaption;
    private javax.swing.JLabel lblFarmAddress;
    private javax.swing.JLabel lblFarmer;
    private javax.swing.JLabel lblImage;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblQtyCaption;
    private javax.swing.JLabel lblReviewsTitle;
    private javax.swing.JLabel lblStock;
    private javax.swing.JPanel pnlBody;
    private javax.swing.JPanel pnlMain;
    private javax.swing.JPanel pnlNavbar;
    private javax.swing.JPanel pnlQtyRow;
    private javax.swing.JTable reviewsTable;
    private javax.swing.JScrollPane scrollBody;
    private javax.swing.JScrollPane scrollDesc;
    private javax.swing.JScrollPane scrollReviews;
    private javax.swing.JSpinner spinQty;
    private javax.swing.JTextArea txtDescription;
    // End of variables declaration//GEN-END:variables
}
