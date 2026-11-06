package anilink.home;

import anilink.data.ProductDAO;
import anilink.model.Product;
import anilink.session.SessionContext;
import anilink.auth.LoginForm;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class HomePanel extends javax.swing.JPanel {

    private final MainFrame frame;
    /** The products currently shown in {@code freshTable}, in row order - so
     * the double-click handler can resolve the real product id by row index
     * now that column 0 shows a friendly 1..8 number instead of the id. */
    private final List<Product> freshProducts = new ArrayList<>();

    public HomePanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        ((DefaultTableModel) freshTable.getModel()).setColumnIdentifiers(
                new Object[]{"ID", "Product", "Category", "Price", "Stock", "Unit", "Farmer"});
        freshTable.setDefaultEditor(Object.class, null);
        UiUtil.styleTable(freshTable);
        refreshData();
    }

    public void refreshData() {
        btnRegisterLogin.setVisible(!SessionContext.isLoggedIn());
        loadFreshProducts();
    }

    private void loadFreshProducts() {
        DefaultTableModel model = (DefaultTableModel) freshTable.getModel();
        model.setRowCount(0);
        freshProducts.clear();
        try {
            List<Product> products = new ProductDAO().latestAvailable(8);
            freshProducts.addAll(products);
            int number = products.size();
            for (Product product : products) {
                model.addRow(new Object[]{
                    number--,
                    product.getProductName(),
                    product.getCategoryName() == null ? "Other" : product.getCategoryName(),
                    UiUtil.formatPrice(product.getPrice()),
                    product.getQuantity(),
                    product.getUnit(),
                    product.getFarmerName()
                });
            }
            if (products.isEmpty()) {
                lblFreshEmpty.setText("No products available yet. Check back soon for fresh farm products!");
                lblFreshEmpty.setVisible(true);
            } else {
                lblFreshEmpty.setVisible(false);
            }
        } catch (SQLException e) {
            lblFreshEmpty.setText("Failed to load products: " + e.getMessage());
            lblFreshEmpty.setVisible(true);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblSubtitle = new javax.swing.JLabel();
        lblHero = new javax.swing.JLabel();
        pnlValueProps = new javax.swing.JPanel();
        lblProp1 = new javax.swing.JLabel();
        lblProp2 = new javax.swing.JLabel();
        lblProp3 = new javax.swing.JLabel();
        btnBrowse = new javax.swing.JButton();
        btnRegisterLogin = new javax.swing.JButton();
        lblFreshTitle = new javax.swing.JLabel();
        lblFreshEmpty = new javax.swing.JLabel();
        scrollFresh = new javax.swing.JScrollPane();
        freshTable = new javax.swing.JTable();
        btnViewAll = new javax.swing.JButton();
        lblHowTitle = new javax.swing.JLabel();
        pnlSteps = new javax.swing.JPanel();
        lblStep1 = new javax.swing.JLabel();
        lblStep2 = new javax.swing.JLabel();
        lblStep3 = new javax.swing.JLabel();
        lblStep4 = new javax.swing.JLabel();
        lblStep5 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setBackground(new java.awt.Color(232, 245, 233));
        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblTitle.setOpaque(true);
        lblTitle.setText("<html>AniLink: Local Farmer Product Marketplace<br>and Management System</html>");
        lblTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18));

        lblSubtitle.setFont(new java.awt.Font("Segoe UI", 0, 14));
        lblSubtitle.setForeground(new java.awt.Color(96, 125, 107));
        lblSubtitle.setText("Fresh local produce, straight from farms to your table");

        lblHero.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblHero.setForeground(new java.awt.Color(55, 71, 59));
        lblHero.setText("<html>AniLink connects local farmers and buyers in one simple marketplace.<br>Farmers post their fresh harvests, buyers discover quality produce nearby,<br>and every order supports the local community.</html>");

        pnlValueProps.setOpaque(false);

        lblProp1.setBackground(new java.awt.Color(232, 245, 233));
        lblProp1.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblProp1.setForeground(new java.awt.Color(46, 125, 50));
        lblProp1.setOpaque(true);
        lblProp1.setText("Fresh local products");
        lblProp1.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));

        lblProp2.setBackground(new java.awt.Color(232, 245, 233));
        lblProp2.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblProp2.setForeground(new java.awt.Color(46, 125, 50));
        lblProp2.setOpaque(true);
        lblProp2.setText("Direct from farmers");
        lblProp2.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));

        lblProp3.setBackground(new java.awt.Color(232, 245, 233));
        lblProp3.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblProp3.setForeground(new java.awt.Color(46, 125, 50));
        lblProp3.setOpaque(true);
        lblProp3.setText("Simple ordering");
        lblProp3.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 14, 12, 14));

        javax.swing.GroupLayout pnlValuePropsLayout = new javax.swing.GroupLayout(pnlValueProps);
        pnlValueProps.setLayout(pnlValuePropsLayout);
        pnlValuePropsLayout.setHorizontalGroup(
            pnlValuePropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlValuePropsLayout.createSequentialGroup()
                .addComponent(lblProp1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblProp2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblProp3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlValuePropsLayout.setVerticalGroup(
            pnlValuePropsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblProp1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblProp2, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblProp3, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        btnBrowse.setBackground(new java.awt.Color(46, 125, 50));
        btnBrowse.setFont(new java.awt.Font("Segoe UI", 1, 14));
        btnBrowse.setForeground(new java.awt.Color(255, 255, 255));
        btnBrowse.setFocusPainted(false);
        btnBrowse.setText("Browse Marketplace");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        btnRegisterLogin.setBackground(new java.awt.Color(255, 255, 255));
        btnRegisterLogin.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnRegisterLogin.setForeground(new java.awt.Color(46, 125, 50));
        btnRegisterLogin.setText("Register / Login");
        btnRegisterLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterLoginActionPerformed(evt);
            }
        });

        lblFreshTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblFreshTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblFreshTitle.setText("Fresh Products");

        lblFreshEmpty.setForeground(new java.awt.Color(96, 125, 107));
        lblFreshEmpty.setText("Hand-picked produce straight from local farms");

        freshTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {

            },
            new String[] {

            }
        ));
        freshTable.setPreferredScrollableViewportSize(new java.awt.Dimension(760, 220));
        freshTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                freshTableMouseClicked(evt);
            }
        });
        scrollFresh.setViewportView(freshTable);

        btnViewAll.setBackground(new java.awt.Color(46, 125, 50));
        btnViewAll.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnViewAll.setForeground(new java.awt.Color(255, 255, 255));
        btnViewAll.setFocusPainted(false);
        btnViewAll.setText("View All Products");
        btnViewAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewAllActionPerformed(evt);
            }
        });

        lblHowTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblHowTitle.setForeground(new java.awt.Color(46, 125, 50));
        lblHowTitle.setText("How It Works");

        pnlSteps.setOpaque(false);

        lblStep1.setBackground(new java.awt.Color(232, 245, 233));
        lblStep1.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblStep1.setForeground(new java.awt.Color(27, 94, 32));
        lblStep1.setOpaque(true);
        lblStep1.setText("<html><b>1.</b> Farmer posts products</html>");
        lblStep1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblStep2.setBackground(new java.awt.Color(232, 245, 233));
        lblStep2.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblStep2.setForeground(new java.awt.Color(27, 94, 32));
        lblStep2.setOpaque(true);
        lblStep2.setText("<html><b>2.</b> Buyer browses marketplace</html>");
        lblStep2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblStep3.setBackground(new java.awt.Color(232, 245, 233));
        lblStep3.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblStep3.setForeground(new java.awt.Color(27, 94, 32));
        lblStep3.setOpaque(true);
        lblStep3.setText("<html><b>3.</b> Places order</html>");
        lblStep3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblStep4.setBackground(new java.awt.Color(232, 245, 233));
        lblStep4.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblStep4.setForeground(new java.awt.Color(27, 94, 32));
        lblStep4.setOpaque(true);
        lblStep4.setText("<html><b>4.</b> Farmer prepares</html>");
        lblStep4.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblStep5.setBackground(new java.awt.Color(232, 245, 233));
        lblStep5.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblStep5.setForeground(new java.awt.Color(27, 94, 32));
        lblStep5.setOpaque(true);
        lblStep5.setText("<html><b>5.</b> Completed</html>");
        lblStep5.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12));

        javax.swing.GroupLayout pnlStepsLayout = new javax.swing.GroupLayout(pnlSteps);
        pnlSteps.setLayout(pnlStepsLayout);
        pnlStepsLayout.setHorizontalGroup(
            pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlStepsLayout.createSequentialGroup()
                .addComponent(lblStep1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStep5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlStepsLayout.setVerticalGroup(
            pnlStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblStep1, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblStep2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblStep3, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblStep4, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblStep5, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(lblSubtitle, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(lblHero, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(pnlValueProps, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnRegisterLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblFreshTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(lblFreshEmpty, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(scrollFresh, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(btnViewAll, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHowTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addComponent(pnlSteps, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtitle)
                .addGap(18, 18, 18)
                .addComponent(lblHero)
                .addGap(20, 20, 20)
                .addComponent(pnlValueProps, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRegisterLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addComponent(lblFreshTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFreshEmpty)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollFresh, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnViewAll, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(lblHowTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSteps, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.MARKETPLACE);
    }

    private void btnRegisterLoginActionPerformed(java.awt.event.ActionEvent evt) {
        LoginForm login = new LoginForm(frame);
        login.setLocationRelativeTo(frame);
        login.setVisible(true);
    }

    private void btnViewAllActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.MARKETPLACE);
    }

    private void freshTableMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 2 && evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            int row = freshTable.getSelectedRow();
            if (row >= 0 && row < freshProducts.size()) {
                frame.openProduct(freshProducts.get(row).getId());
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnRegisterLogin;
    private javax.swing.JButton btnViewAll;
    private javax.swing.JTable freshTable;
    private javax.swing.JLabel lblFreshEmpty;
    private javax.swing.JLabel lblFreshTitle;
    private javax.swing.JLabel lblHero;
    private javax.swing.JLabel lblHowTitle;
    private javax.swing.JLabel lblProp1;
    private javax.swing.JLabel lblProp2;
    private javax.swing.JLabel lblProp3;
    private javax.swing.JLabel lblStep1;
    private javax.swing.JLabel lblStep2;
    private javax.swing.JLabel lblStep3;
    private javax.swing.JLabel lblStep4;
    private javax.swing.JLabel lblStep5;
    private javax.swing.JLabel lblSubtitle;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlSteps;
    private javax.swing.JPanel pnlValueProps;
    private javax.swing.JScrollPane scrollFresh;
    // End of variables declaration//GEN-END:variables
}
