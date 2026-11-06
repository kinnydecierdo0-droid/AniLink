package anilink.shop;

import anilink.data.CategoryDAO;
import anilink.data.ProductDAO;
import anilink.model.Category;
import anilink.model.Product;
import anilink.session.SessionContext;
import anilink.auth.LoginForm;
import anilink.auth.RegisterForm;
import anilink.home.GuestHomeFrame;
import anilink.ui.ProductCardFactory;
import anilink.util.UiUtil;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

public class GuestMarketplaceFrame extends javax.swing.JFrame {

    public GuestMarketplaceFrame() {
        initComponents();
        refreshData();
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
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
        performSearch();
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
        Double min = null;
        Double max = null;
        String minText = txtMinPrice.getText().trim();
        String maxText = txtMaxPrice.getText().trim();
        try {
            if (!minText.isEmpty()) {
                min = Double.parseDouble(minText);
            }
            if (!maxText.isEmpty()) {
                max = Double.parseDouble(maxText);
            }
        } catch (NumberFormatException e) {
            UiUtil.error(this, "Please enter valid numbers for min/max price.");
            return;
        }

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
            List<Product> products = new ProductDAO().search(search, categoryId, availability, min, max);
            fillGrid(products);
        } catch (SQLException e) {
            UiUtil.error(this, "Failed to load products: " + e.getMessage());
        }
    }

    private void fillGrid(List<Product> products) {
        lblCount.setText(products.size() + " product(s) found");
        if (products.isEmpty()) {
            pnlProductGrid.removeAll();
            pnlEmpty.setVisible(true);
            pnlProductGrid.setVisible(false);
            pnlProductGrid.revalidate();
            pnlProductGrid.repaint();
        } else {
            pnlEmpty.setVisible(false);
            pnlProductGrid.setVisible(true);
            ProductCardFactory.fillGrid(pnlProductGrid, products, product
                    -> ProductCardFactory.card(product, "View Product",
                            p -> openProduct(p.getId())));
        }
        pnlBody.revalidate();
        pnlBody.repaint();
    }

    private void openProduct(int productId) {
        GuestProductDetailFrame detail = new GuestProductDetailFrame(productId, this);
        detail.setLocationRelativeTo(null);
        detail.setVisible(true);
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
        pnlHeader = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblCount = new javax.swing.JLabel();
        pnlFilters = new javax.swing.JPanel();
        lblSearchCaption = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblCategoryCaption = new javax.swing.JLabel();
        comboCategory = new javax.swing.JComboBox<>();
        lblAvailabilityCaption = new javax.swing.JLabel();
        comboAvailability = new javax.swing.JComboBox<>();
        lblMinCaption = new javax.swing.JLabel();
        txtMinPrice = new javax.swing.JTextField();
        lblMaxCaption = new javax.swing.JLabel();
        txtMaxPrice = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        btnClearFilters = new javax.swing.JButton();
        pnlEmpty = new javax.swing.JPanel();
        lblEmpty = new javax.swing.JLabel();
        pnlProductGrid = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AniLink - Marketplace");
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

        pnlHeader.setOpaque(false);

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblTitle.setText("Marketplace");

        lblCount.setForeground(new java.awt.Color(96, 125, 107));
        lblCount.setText("0 product(s) found");

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 600, Short.MAX_VALUE)
                .addComponent(lblCount))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGroup(pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitle)
                    .addComponent(lblCount))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        pnlFilters.setBackground(new java.awt.Color(255, 255, 255));

        lblSearchCaption.setText("Search");

        txtSearch.setToolTipText("Search products by name or category");
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        lblCategoryCaption.setText("Category");

        comboCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All Categories"}));

        lblAvailabilityCaption.setText("Availability");

        comboAvailability.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"All", "In Stock", "Out of Stock"}));

        lblMinCaption.setText("Min Price");

        lblMaxCaption.setText("Max Price");

        btnSearch.setBackground(new java.awt.Color(46, 125, 50));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setFocusPainted(false);
        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnClearFilters.setText("Clear Filters");
        btnClearFilters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearFiltersActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFiltersLayout = new javax.swing.GroupLayout(pnlFilters);
        pnlFilters.setLayout(pnlFiltersLayout);
        pnlFiltersLayout.setHorizontalGroup(
            pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltersLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFiltersLayout.createSequentialGroup()
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSearchCaption)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCategoryCaption)
                            .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblAvailabilityCaption)
                            .addComponent(comboAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMinCaption)
                            .addComponent(txtMinPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMaxCaption)
                            .addComponent(txtMaxPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnClearFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlFiltersLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        pnlFiltersLayout.setVerticalGroup(
            pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFiltersLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlFiltersLayout.createSequentialGroup()
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSearchCaption)
                            .addComponent(lblCategoryCaption)
                            .addComponent(lblAvailabilityCaption)
                            .addComponent(lblMinCaption)
                            .addComponent(lblMaxCaption))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(comboAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtMinPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtMaxPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFiltersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnClearFilters, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16))
        );

        pnlEmpty.setOpaque(false);
        pnlEmpty.setVisible(false);

        lblEmpty.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblEmpty.setForeground(new java.awt.Color(96, 125, 107));
        lblEmpty.setText("No products found. Try searching for another product.");
        pnlEmpty.add(lblEmpty);
        lblEmpty.setBounds(0, 20, 600, 30);

        pnlProductGrid.setBackground(new java.awt.Color(248, 250, 248));
        pnlProductGrid.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        javax.swing.GroupLayout pnlBodyLayout = new javax.swing.GroupLayout(pnlBody);
        pnlBody.setLayout(pnlBodyLayout);
        pnlBodyLayout.setHorizontalGroup(
            pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlHeader, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlFilters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlEmpty, javax.swing.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
                    .addComponent(pnlProductGrid, 400, 800, Short.MAX_VALUE))
                .addGap(40, 40, 40))
        );
        pnlBodyLayout.setVerticalGroup(
            pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(pnlHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFilters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlEmpty, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProductGrid, 400, 800, Short.MAX_VALUE)
                .addGap(40, 40, 40))
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
        refreshData();
        scrollBody.getVerticalScrollBar().setValue(0);
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        openLogin();
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        openRegister();
    }

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {
        performSearch();
    }

    private void btnClearFiltersActionPerformed(java.awt.event.ActionEvent evt) {
        txtSearch.setText("");
        txtMinPrice.setText("");
        txtMaxPrice.setText("");
        comboCategory.setSelectedIndex(0);
        comboAvailability.setSelectedItem("All");
        performSearch();
    }

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {
        performSearch();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClearFilters;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnMarketplace;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> comboAvailability;
    private javax.swing.JComboBox<Object> comboCategory;
    private javax.swing.JLabel lblAvailabilityCaption;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblCategoryCaption;
    private javax.swing.JLabel lblCount;
    private javax.swing.JLabel lblEmpty;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMaxCaption;
    private javax.swing.JLabel lblMinCaption;
    private javax.swing.JLabel lblSearchCaption;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlBody;
    private javax.swing.JPanel pnlEmpty;
    private javax.swing.JPanel pnlFilters;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlNavbar;
    private javax.swing.JPanel pnlProductGrid;
    private javax.swing.JScrollPane scrollBody;
    private javax.swing.JTextField txtMaxPrice;
    private javax.swing.JTextField txtMinPrice;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
