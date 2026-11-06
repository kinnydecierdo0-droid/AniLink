package anilink.home;

import anilink.data.ProductDAO;
import anilink.model.Product;
import anilink.session.SessionContext;
import anilink.auth.LoginForm;
import anilink.auth.RegisterForm;
import anilink.shop.GuestMarketplaceFrame;
import anilink.shop.GuestProductDetailFrame;
import anilink.ui.ProductCardFactory;
import anilink.util.UiUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class GuestHomeFrame extends javax.swing.JFrame {

    private static final Color PRIMARY = new Color(46, 125, 50);
    private static final Color DARK = new Color(27, 94, 32);
    private static final Color LIGHT_GREEN = new Color(232, 245, 233);
    private static final Color MUTED = new Color(96, 125, 107);
    private static final Color BORDER = new Color(221, 232, 222);
    private static final Color WHITE = Color.WHITE;

    public GuestHomeFrame() {
        initComponents();
        pnlHero.setImages("/images/hero-farmer.jpg", "/images/hero-corn.jpg",
                "/images/hero-tomato.jpg", "/images/hero-rice.jpg");
        pnlHero.start();
        refreshData();
        pack();
        setLocationRelativeTo(null);
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    }

    public void refreshData() {
        loadFreshProducts();
    }

    private void loadFreshProducts() {
        try {
            List<Product> products = new ProductDAO().latestAvailable(8);
            if (products.isEmpty()) {
                pnlProductGrid.removeAll();
                lblFreshEmpty.setText("No products available yet. Check back soon for fresh farm products!");
                lblFreshEmpty.setVisible(true);
                pnlProductGrid.setVisible(false);
                pnlProductGrid.revalidate();
                pnlProductGrid.repaint();
            } else {
                lblFreshEmpty.setVisible(false);
                pnlProductGrid.setVisible(true);
                ProductCardFactory.fillGrid(pnlProductGrid, products, 5, product
                        -> ProductCardFactory.card(product, "View Product",
                                p -> openProduct(p.getId())));
            }
        } catch (SQLException e) {
            pnlProductGrid.removeAll();
            lblFreshEmpty.setText("Failed to load products: " + e.getMessage());
            lblFreshEmpty.setVisible(true);
            pnlProductGrid.setVisible(false);
            pnlProductGrid.revalidate();
            pnlProductGrid.repaint();
        }
        pnlBody.revalidate();
        pnlBody.repaint();
    }

    private void openMarketplace() {
        GuestMarketplaceFrame marketplace = new GuestMarketplaceFrame();
        marketplace.setLocationRelativeTo(null);
        marketplace.setVisible(true);
        dispose();
    }

    private void openProduct(int productId) {
        GuestProductDetailFrame detail = new GuestProductDetailFrame(productId);
        detail.setLocationRelativeTo(null);
        detail.setVisible(true);
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
        pnlHero = new anilink.ui.SlideshowPanel();
        lblEyebrow = new javax.swing.JLabel();
        lblHeroTitle = new javax.swing.JLabel();
        lblHeroSub = new javax.swing.JLabel();
        lblTrust = new javax.swing.JLabel();
        btnBrowse = new javax.swing.JButton();
        btnBecomeFarmer = new javax.swing.JButton();
        lblFreshTitle = new javax.swing.JLabel();
        lblFreshSub = new javax.swing.JLabel();
        lblFreshEmpty = new javax.swing.JLabel();
        pnlProductGrid = new javax.swing.JPanel();
        pnlSampleCard = new javax.swing.JPanel();
        lblSampleImg = new javax.swing.JLabel();
        lblSampleName = new javax.swing.JLabel();
        lblSampleCat = new javax.swing.JLabel();
        lblSamplePrice = new javax.swing.JLabel();
        lblSampleStock = new javax.swing.JLabel();
        lblSampleFarmer = new javax.swing.JLabel();
        btnSampleView = new javax.swing.JButton();
        btnViewAll = new javax.swing.JButton();
        lblWhyTitle = new javax.swing.JLabel();
        lblWhySub = new javax.swing.JLabel();
        pnlFeatures = new javax.swing.JPanel();
        pnlFeature1 = new javax.swing.JPanel();
        lblFeatTitle1 = new javax.swing.JLabel();
        lblFeatText1 = new javax.swing.JLabel();
        pnlFeature2 = new javax.swing.JPanel();
        lblFeatTitle2 = new javax.swing.JLabel();
        lblFeatText2 = new javax.swing.JLabel();
        pnlFeature3 = new javax.swing.JPanel();
        lblFeatTitle3 = new javax.swing.JLabel();
        lblFeatText3 = new javax.swing.JLabel();
        pnlFeature4 = new javax.swing.JPanel();
        lblFeatTitle4 = new javax.swing.JLabel();
        lblFeatText4 = new javax.swing.JLabel();
        pnlAbout = new javax.swing.JPanel();
        lblAboutText = new javax.swing.JLabel();
        lblHowTitle = new javax.swing.JLabel();
        lblHowSub = new javax.swing.JLabel();
        pnlSteps = new javax.swing.JPanel();
        pnlStep1 = new javax.swing.JPanel();
        lblStepNum1 = new javax.swing.JLabel();
        lblStepText1 = new javax.swing.JLabel();
        pnlStep2 = new javax.swing.JPanel();
        lblStepNum2 = new javax.swing.JLabel();
        lblStepText2 = new javax.swing.JLabel();
        pnlStep3 = new javax.swing.JPanel();
        lblStepNum3 = new javax.swing.JLabel();
        lblStepText3 = new javax.swing.JLabel();
        pnlStep4 = new javax.swing.JPanel();
        lblStepNum4 = new javax.swing.JLabel();
        lblStepText4 = new javax.swing.JLabel();
        pnlStep5 = new javax.swing.JPanel();
        lblStepNum5 = new javax.swing.JLabel();
        lblStepText5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AniLink - Home");

        pnlNavbar.setBackground(new java.awt.Color(46, 125, 50));
        pnlNavbar.setPreferredSize(new java.awt.Dimension(1100, 56));

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/anilink-logo-40.png"))); // NOI18N

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblBrand.setForeground(new java.awt.Color(255, 255, 255));
        lblBrand.setText("AniLink");

        btnHome.setBackground(new java.awt.Color(39, 110, 43));
        btnHome.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHome.setForeground(new java.awt.Color(255, 255, 255));
        btnHome.setText("Home");
        btnHome.setBorderPainted(false);
        btnHome.setFocusPainted(false);
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });

        btnMarketplace.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
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

        btnLogin.setBackground(new java.awt.Color(255, 255, 255));
        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(27, 94, 32));
        btnLogin.setText("Login");
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnRegister.setBackground(new java.awt.Color(39, 110, 43));
        btnRegister.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("Register");
        btnRegister.setBorderPainted(false);
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
                .addGap(24, 24, 24)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblBrand)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMarketplace, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 523, Short.MAX_VALUE)
                .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        pnlNavbarLayout.setVerticalGroup(
            pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNavbarLayout.createSequentialGroup()
                .addGap(0, 8, Short.MAX_VALUE)
                .addGroup(pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblBrand)
                        .addComponent(btnMarketplace, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlNavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnRegister, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 8, Short.MAX_VALUE))
        );

        getContentPane().add(pnlNavbar, java.awt.BorderLayout.NORTH);

        pnlBody.setBackground(new java.awt.Color(248, 250, 248));

        pnlHero.setBackground(new java.awt.Color(27, 94, 32));

        lblEyebrow.setForeground(new java.awt.Color(165, 214, 167));
        lblEyebrow.setText("LOCAL FARM MARKETPLACE");

        lblHeroTitle.setFont(new java.awt.Font("Segoe UI", 1, 32)); // NOI18N
        lblHeroTitle.setForeground(new java.awt.Color(255, 255, 255));
        lblHeroTitle.setText("<html>AniLink Local Farmers Product Marketplace<br>and Management System</html>");

        lblHeroSub.setForeground(new java.awt.Color(242, 242, 242));
        lblHeroSub.setText("<html><font face='Segoe UI' size='3'>Connect with local farmers. Discover fresh, quality produce and order with confidence.</font></html>");

        lblTrust.setForeground(new java.awt.Color(165, 214, 167));
        lblTrust.setText("Fresh Produce   |   Direct from Farms   |   Support Local");

        btnBrowse.setBackground(new java.awt.Color(255, 255, 255));
        btnBrowse.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBrowse.setForeground(new java.awt.Color(27, 94, 32));
        btnBrowse.setText("Browse Products");
        btnBrowse.setBorderPainted(false);
        btnBrowse.setFocusPainted(false);
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnBecomeFarmer.setBackground(new java.awt.Color(39, 110, 43));
        btnBecomeFarmer.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBecomeFarmer.setForeground(new java.awt.Color(255, 255, 255));
        btnBecomeFarmer.setText("Become a Seller");
        btnBecomeFarmer.setBorderPainted(false);
        btnBecomeFarmer.setFocusPainted(false);
        btnBecomeFarmer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBecomeFarmerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlHeroLayout = new javax.swing.GroupLayout(pnlHero);
        pnlHero.setLayout(pnlHeroLayout);
        pnlHeroLayout.setHorizontalGroup(
            pnlHeroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeroLayout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(pnlHeroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEyebrow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHeroTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHeroSub, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTrust, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlHeroLayout.createSequentialGroup()
                        .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(btnBecomeFarmer, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlHeroLayout.setVerticalGroup(
            pnlHeroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeroLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(lblEyebrow)
                .addGap(14, 14, 14)
                .addComponent(lblHeroTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(16, 16, 16)
                .addComponent(lblHeroSub, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(lblTrust)
                .addGap(24, 24, 24)
                .addGroup(pnlHeroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBecomeFarmer, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48))
        );

        lblFreshTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblFreshTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblFreshTitle.setText("Fresh Products");

        lblFreshSub.setForeground(new java.awt.Color(96, 125, 107));
        lblFreshSub.setText("Hand-picked produce straight from local farms");

        lblFreshEmpty.setForeground(new java.awt.Color(96, 125, 107));
        lblFreshEmpty.setText(" ");

        pnlProductGrid.setBackground(new java.awt.Color(248, 250, 248));
        pnlProductGrid.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlSampleCard.setBackground(new java.awt.Color(255, 255, 255));
        pnlSampleCard.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblSampleImg.setBackground(new java.awt.Color(232, 245, 233));
        lblSampleImg.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblSampleImg.setForeground(new java.awt.Color(96, 125, 107));
        lblSampleImg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSampleImg.setText("Product image");
        lblSampleImg.setOpaque(true);

        lblSampleName.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSampleName.setForeground(new java.awt.Color(27, 94, 32));
        lblSampleName.setText("Fresh Tomatoes");

        lblSampleCat.setBackground(new java.awt.Color(232, 245, 233));
        lblSampleCat.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblSampleCat.setForeground(new java.awt.Color(46, 125, 50));
        lblSampleCat.setText("Vegetables");
        lblSampleCat.setOpaque(true);

        lblSamplePrice.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblSamplePrice.setForeground(new java.awt.Color(46, 125, 50));
        lblSamplePrice.setText("₱120.00 / kg");

        lblSampleStock.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblSampleStock.setForeground(new java.awt.Color(96, 125, 107));
        lblSampleStock.setText("Available: 50 kg");

        lblSampleFarmer.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        lblSampleFarmer.setForeground(new java.awt.Color(96, 125, 107));
        lblSampleFarmer.setText("Farmer: Juan Dela Cruz");

        btnSampleView.setBackground(new java.awt.Color(46, 125, 50));
        btnSampleView.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnSampleView.setForeground(new java.awt.Color(255, 255, 255));
        btnSampleView.setText("View Product");
        btnSampleView.setFocusPainted(false);

        pnlSampleCard.add(lblSampleImg, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 8, 208, 140));
        pnlSampleCard.add(lblSampleName, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 158, 208, -1));
        pnlSampleCard.add(lblSampleCat, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 182, 90, 20));
        pnlSampleCard.add(lblSamplePrice, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 208, -1));
        pnlSampleCard.add(lblSampleStock, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 234, 208, -1));
        pnlSampleCard.add(lblSampleFarmer, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 252, 208, -1));
        pnlSampleCard.add(btnSampleView, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 208, 34));
        pnlProductGrid.add(pnlSampleCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 400));

        btnViewAll.setBackground(new java.awt.Color(46, 125, 50));
        btnViewAll.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnViewAll.setForeground(new java.awt.Color(255, 255, 255));
        btnViewAll.setText("View All Products");
        btnViewAll.setFocusPainted(false);
        btnViewAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewAllActionPerformed(evt);
            }
        });

        lblWhyTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblWhyTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblWhyTitle.setText("Why AniLink");

        lblWhySub.setForeground(new java.awt.Color(96, 125, 107));
        lblWhySub.setText("Everything you need for farm-fresh shopping");

        pnlFeatures.setBackground(new java.awt.Color(248, 250, 248));
        pnlFeatures.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlFeature1.setBackground(new java.awt.Color(255, 255, 255));
        pnlFeature1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblFeatTitle1.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFeatTitle1.setForeground(new java.awt.Color(27, 94, 32));
        lblFeatTitle1.setText("Fresh Produce");

        lblFeatText1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblFeatText1.setForeground(new java.awt.Color(96, 125, 107));
        lblFeatText1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblFeatText1.setText("<html>Harvested and delivered at peak freshness from nearby farms.</html>");

        pnlFeature1.add(lblFeatTitle1, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 16, -1, -1));
        pnlFeature1.add(lblFeatText1, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 42, 202, 76));
        pnlFeatures.add(pnlFeature1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 230, 140));

        pnlFeature2.setBackground(new java.awt.Color(255, 255, 255));
        pnlFeature2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblFeatTitle2.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFeatTitle2.setForeground(new java.awt.Color(27, 94, 32));
        lblFeatTitle2.setText("Verified Farmers");

        lblFeatText2.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblFeatText2.setForeground(new java.awt.Color(96, 125, 107));
        lblFeatText2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblFeatText2.setText("<html>Every seller is a real local farmer you can trust.</html>");

        pnlFeature2.add(lblFeatTitle2, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 16, -1, -1));
        pnlFeature2.add(lblFeatText2, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 42, 202, 76));
        pnlFeatures.add(pnlFeature2, new org.netbeans.lib.awtextra.AbsoluteConstraints(265, 0, 230, 140));

        pnlFeature3.setBackground(new java.awt.Color(255, 255, 255));
        pnlFeature3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblFeatTitle3.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFeatTitle3.setForeground(new java.awt.Color(27, 94, 32));
        lblFeatTitle3.setText("Easy Ordering");

        lblFeatText3.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblFeatText3.setForeground(new java.awt.Color(96, 125, 107));
        lblFeatText3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblFeatText3.setText("<html>Browse, add to cart, and place orders in just a few taps.</html>");

        pnlFeature3.add(lblFeatTitle3, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 16, -1, -1));
        pnlFeature3.add(lblFeatText3, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 42, 202, 76));
        pnlFeatures.add(pnlFeature3, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, 230, 140));

        pnlFeature4.setBackground(new java.awt.Color(255, 255, 255));
        pnlFeature4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblFeatTitle4.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblFeatTitle4.setForeground(new java.awt.Color(27, 94, 32));
        lblFeatTitle4.setText("Community");

        lblFeatText4.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        lblFeatText4.setForeground(new java.awt.Color(96, 125, 107));
        lblFeatText4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblFeatText4.setText("<html>Support local agriculture and strengthen your community.</html>");

        pnlFeature4.add(lblFeatTitle4, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 16, -1, -1));
        pnlFeature4.add(lblFeatText4, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 42, 202, 76));
        pnlFeatures.add(pnlFeature4, new org.netbeans.lib.awtextra.AbsoluteConstraints(794, 0, 230, 140));

        pnlAbout.setBackground(new java.awt.Color(232, 245, 233));
        pnlAbout.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblAboutText.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblAboutText.setForeground(new java.awt.Color(38, 50, 56));
        lblAboutText.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblAboutText.setText("<html><b style='font-size:16px;color:#1B5E20'>About AniLink</b><br><br>"
                + "AniLink helps local farmers showcase their products and allows buyers to conveniently "
                + "find and order local agricultural products.<br><br>"
                + "We connect communities by bringing fresh, locally grown produce directly from farms to "
                + "your table. Our platform makes it simple for farmers to sell and easy for buyers to "
                + "discover quality products nearby.<br><br>"
                + "- Farm-fresh products delivered to your community<br>"
                + "- Fair prices that support local farmers<br>"
                + "- Simple ordering with real-time availability</html>");

        pnlAbout.add(lblAboutText, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 16, 984, 216));

        lblHowTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblHowTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblHowTitle.setText("How It Works");

        lblHowSub.setForeground(new java.awt.Color(96, 125, 107));
        lblHowSub.setText("Five simple steps from farm to table");

        pnlSteps.setBackground(new java.awt.Color(248, 250, 248));
        pnlSteps.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlStep1.setBackground(new java.awt.Color(232, 245, 233));
        pnlStep1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStepNum1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblStepNum1.setForeground(new java.awt.Color(46, 125, 50));
        lblStepNum1.setText("1");

        lblStepText1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStepText1.setForeground(new java.awt.Color(27, 94, 32));
        lblStepText1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblStepText1.setText("<html>Farmers Post Products</html>");

        pnlStep1.add(lblStepNum1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        pnlStep1.add(lblStepText1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 42, 160, 40));
        pnlSteps.add(pnlStep1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 180, 90));

        pnlStep2.setBackground(new java.awt.Color(232, 245, 233));
        pnlStep2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStepNum2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblStepNum2.setForeground(new java.awt.Color(46, 125, 50));
        lblStepNum2.setText("2");

        lblStepText2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStepText2.setForeground(new java.awt.Color(27, 94, 32));
        lblStepText2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblStepText2.setText("<html>Buyers Browse Products</html>");

        pnlStep2.add(lblStepNum2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        pnlStep2.add(lblStepText2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 42, 160, 40));
        pnlSteps.add(pnlStep2, new org.netbeans.lib.awtextra.AbsoluteConstraints(211, 0, 180, 90));

        pnlStep3.setBackground(new java.awt.Color(232, 245, 233));
        pnlStep3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStepNum3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblStepNum3.setForeground(new java.awt.Color(46, 125, 50));
        lblStepNum3.setText("3");

        lblStepText3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStepText3.setForeground(new java.awt.Color(27, 94, 32));
        lblStepText3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblStepText3.setText("<html>Buyers Place Orders</html>");

        pnlStep3.add(lblStepNum3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        pnlStep3.add(lblStepText3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 42, 160, 40));
        pnlSteps.add(pnlStep3, new org.netbeans.lib.awtextra.AbsoluteConstraints(422, 0, 180, 90));

        pnlStep4.setBackground(new java.awt.Color(232, 245, 233));
        pnlStep4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStepNum4.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblStepNum4.setForeground(new java.awt.Color(46, 125, 50));
        lblStepNum4.setText("4");

        lblStepText4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStepText4.setForeground(new java.awt.Color(27, 94, 32));
        lblStepText4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblStepText4.setText("<html>Farmers Prepare Orders</html>");

        pnlStep4.add(lblStepNum4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        pnlStep4.add(lblStepText4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 42, 160, 40));
        pnlSteps.add(pnlStep4, new org.netbeans.lib.awtextra.AbsoluteConstraints(633, 0, 180, 90));

        pnlStep5.setBackground(new java.awt.Color(232, 245, 233));
        pnlStep5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblStepNum5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblStepNum5.setForeground(new java.awt.Color(46, 125, 50));
        lblStepNum5.setText("5");

        lblStepText5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblStepText5.setForeground(new java.awt.Color(27, 94, 32));
        lblStepText5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblStepText5.setText("<html>Order Completed</html>");

        pnlStep5.add(lblStepNum5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        pnlStep5.add(lblStepText5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 42, 160, 40));
        pnlSteps.add(pnlStep5, new org.netbeans.lib.awtextra.AbsoluteConstraints(844, 0, 180, 90));

        javax.swing.GroupLayout pnlBodyLayout = new javax.swing.GroupLayout(pnlBody);
        pnlBody.setLayout(pnlBodyLayout);
        pnlBodyLayout.setHorizontalGroup(
            pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addComponent(pnlHero, 400, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addGap(64, 64, 64)
                .addGroup(pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFreshTitle)
                    .addComponent(lblFreshSub)
                    .addComponent(lblFreshEmpty)
                    .addComponent(pnlProductGrid, 400, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlBodyLayout.createSequentialGroup()
                        .addGap(0, 412, Short.MAX_VALUE)
                        .addComponent(btnViewAll, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 412, Short.MAX_VALUE))
                    .addComponent(lblWhyTitle)
                    .addComponent(lblWhySub)
                    .addComponent(pnlFeatures, 400, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlAbout, 400, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblHowTitle)
                    .addComponent(lblHowSub)
                    .addComponent(pnlSteps, 400, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(64, 64, 64))
        );
        pnlBodyLayout.setVerticalGroup(
            pnlBodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBodyLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(pnlHero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblFreshTitle)
                .addGap(6, 6, 6)
                .addComponent(lblFreshSub)
                .addGap(16, 16, 16)
                .addComponent(lblFreshEmpty)
                .addGap(18, 18, 18)
                .addComponent(pnlProductGrid, 400, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnViewAll, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(lblWhyTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblWhySub)
                .addGap(16, 16, 16)
                .addComponent(pnlFeatures, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(pnlAbout, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(lblHowTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblHowSub)
                .addGap(16, 16, 16)
                .addComponent(pnlSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 40, Short.MAX_VALUE))
        );

        scrollBody.setViewportView(pnlBody);

        getContentPane().add(scrollBody, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {
        scrollBody.getVerticalScrollBar().setValue(0);
        refreshData();
    }

    private void btnMarketplaceActionPerformed(java.awt.event.ActionEvent evt) {
        openMarketplace();
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        openLogin();
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        openRegister();
    }

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {
        openMarketplace();
    }

    private void btnBecomeFarmerActionPerformed(java.awt.event.ActionEvent evt) {
        openRegister();
    }

    private void btnViewAllActionPerformed(java.awt.event.ActionEvent evt) {
        openMarketplace();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBecomeFarmer;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnHome;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnMarketplace;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSampleView;
    private javax.swing.JButton btnViewAll;
    private javax.swing.JLabel lblAboutText;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblEyebrow;
    private javax.swing.JLabel lblFeatText1;
    private javax.swing.JLabel lblFeatText2;
    private javax.swing.JLabel lblFeatText3;
    private javax.swing.JLabel lblFeatText4;
    private javax.swing.JLabel lblFeatTitle1;
    private javax.swing.JLabel lblFeatTitle2;
    private javax.swing.JLabel lblFeatTitle3;
    private javax.swing.JLabel lblFeatTitle4;
    private javax.swing.JLabel lblFreshEmpty;
    private javax.swing.JLabel lblFreshSub;
    private javax.swing.JLabel lblFreshTitle;
    private javax.swing.JLabel lblHeroSub;
    private javax.swing.JLabel lblHeroTitle;
    private javax.swing.JLabel lblHowSub;
    private javax.swing.JLabel lblHowTitle;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblSampleCat;
    private javax.swing.JLabel lblSampleFarmer;
    private javax.swing.JLabel lblSampleImg;
    private javax.swing.JLabel lblSampleName;
    private javax.swing.JLabel lblSamplePrice;
    private javax.swing.JLabel lblSampleStock;
    private javax.swing.JLabel lblStepNum1;
    private javax.swing.JLabel lblStepNum2;
    private javax.swing.JLabel lblStepNum3;
    private javax.swing.JLabel lblStepNum4;
    private javax.swing.JLabel lblStepNum5;
    private javax.swing.JLabel lblStepText1;
    private javax.swing.JLabel lblStepText2;
    private javax.swing.JLabel lblStepText3;
    private javax.swing.JLabel lblStepText4;
    private javax.swing.JLabel lblStepText5;
    private javax.swing.JLabel lblTrust;
    private javax.swing.JLabel lblWhySub;
    private javax.swing.JLabel lblWhyTitle;
    private javax.swing.JPanel pnlAbout;
    private javax.swing.JPanel pnlBody;
    private javax.swing.JPanel pnlFeature1;
    private javax.swing.JPanel pnlFeature2;
    private javax.swing.JPanel pnlFeature3;
    private javax.swing.JPanel pnlFeature4;
    private javax.swing.JPanel pnlFeatures;
    private anilink.ui.SlideshowPanel pnlHero;
    private javax.swing.JPanel pnlNavbar;
    private javax.swing.JPanel pnlProductGrid;
    private javax.swing.JPanel pnlSampleCard;
    private javax.swing.JPanel pnlStep1;
    private javax.swing.JPanel pnlStep2;
    private javax.swing.JPanel pnlStep3;
    private javax.swing.JPanel pnlStep4;
    private javax.swing.JPanel pnlStep5;
    private javax.swing.JPanel pnlSteps;
    private javax.swing.JScrollPane scrollBody;
    // End of variables declaration//GEN-END:variables
}
