package anilink.shell;

import anilink.session.SessionContext;
import anilink.admin.AddStaffPanel;
import anilink.admin.AdminDashboardPanel;
import anilink.admin.AdminOrdersPanel;
import anilink.admin.ManageDeliveriesPanel;
import anilink.admin.AdminProfilePanel;
import anilink.admin.AdminProductsPanel;
import anilink.admin.UserReportPanel;
import anilink.admin.PaymentManagementPanel;
import anilink.admin.ReportsPanel;
import anilink.auth.LoginForm;
import anilink.auth.RegisterForm;
import anilink.buyer.BuyerDashboardPanel;
import anilink.buyer.BuyerOrdersPanel;
import anilink.buyer.BuyerTransactionsPanel;
import anilink.buyer.BuyerProfilePanel;
import anilink.buyer.CartPanel;
import anilink.buyer.CheckoutPanel;
import anilink.buyer.OrderConfirmationPanel;
import anilink.farmer.AddProductPanel;
import anilink.farmer.EditProductPanel;
import anilink.farmer.FarmerDashboardPanel;
import anilink.farmer.FarmerOrdersPanel;
import anilink.farmer.FarmerPaymentPanel;
import anilink.farmer.FarmerProductsPanel;
import anilink.farmer.FarmerProfilePanel;
import anilink.farmer.FarmerTransactionsPanel;
import anilink.farmer.SalesPanel;
import anilink.home.GuestHomeFrame;
import anilink.home.HomePanel;
import anilink.rider.RiderDashboardPanel;
import anilink.rider.RiderDeliveriesPanel;
import anilink.rider.RiderOrdersPanel;
import anilink.rider.RiderSalesReportPanel;
import anilink.shop.MarketplacePanel;
import anilink.shop.ProductDetailPanel;
import anilink.util.UiUtil;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;

public class MainFrame extends javax.swing.JFrame {

    private final CardLayout cardLayout = new VisibleCardLayout();
    /** Added to pnlTopbar at runtime in polishChrome(); no palette component for it. */
    private anilink.ui.NotificationBell notificationBell;
    private HomePanel homePanel;
    private MarketplacePanel marketplacePanel;
    private ProductDetailPanel productDetailPanel;
    private BuyerDashboardPanel buyerDashboardPanel;
    private CartPanel cartPanel;
    private CheckoutPanel checkoutPanel;
    private OrderConfirmationPanel orderConfirmationPanel;
    private BuyerOrdersPanel buyerOrdersPanel;
    private BuyerTransactionsPanel buyerTransactionsPanel;
    private BuyerProfilePanel buyerProfilePanel;
    private FarmerDashboardPanel farmerDashboardPanel;
    private FarmerProductsPanel farmerProductsPanel;
    private AddProductPanel addProductPanel;
    private EditProductPanel editProductPanel;
    private FarmerOrdersPanel farmerOrdersPanel;
    private FarmerPaymentPanel farmerPaymentPanel;
    private SalesPanel salesPanel;
    private FarmerProfilePanel farmerProfilePanel;
    private FarmerTransactionsPanel farmerTransactionsPanel;
    private AdminDashboardPanel adminDashboardPanel;
    private UserReportPanel userReportPanel;
    private AddStaffPanel addStaffPanel;
    private AdminProductsPanel adminProductsPanel;
    private AdminOrdersPanel adminOrdersPanel;
    private ManageDeliveriesPanel manageDeliveriesPanel;
    private PaymentManagementPanel paymentManagementPanel;
    private ReportsPanel reportsPanel;
    private AdminProfilePanel adminProfilePanel;
    private RiderDashboardPanel riderDashboardPanel;
    private RiderDeliveriesPanel riderOrdersPanel;
    private RiderOrdersPanel riderTransactionsPanel;
    private RiderSalesReportPanel riderSalesPanel;

    public MainFrame() {
        initComponents();
        setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        polishChrome();
        setupNavigation();
        applyRoleVisibility();
        updateUserLabel();
        showDashboardForRole();
        pnlSidebar.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                javax.swing.SwingUtilities.invokeLater(() -> applyRoleVisibility());
            }
        });
    }

    /**
     * Shell styling applied outside the generated region so the NetBeans
     * designer cannot regenerate it away.
     */
    private void polishChrome() {
        pnlTopbar.setBorder(javax.swing.BorderFactory.createMatteBorder(
                0, 0, 1, 0, UiUtil.BORDER));
        lblPageTitle.setFont(UiUtil.bold(18));
        lblPageTitle.setForeground(UiUtil.DARK);
        lblUser.setFont(UiUtil.plain(12));
        lblUser.setForeground(UiUtil.MUTED);
        scrollContent.getVerticalScrollBar().setUnitIncrement(16);
        scrollContent.getViewport().setBackground(UiUtil.BG);

        // The bell is the one piece of chrome no NetBeans palette component
        // can represent, so it replaces pnlTopbar's generated GroupLayout
        // with a BorderLayout here - after initComponents(), so the designer
        // still owns the panel and its two labels, it just no longer governs
        // their positions. lblPageTitle/lblUser keep their generated styling;
        // only their container's layout changes.
        pnlTopbar.setLayout(new java.awt.BorderLayout());
        lblPageTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        pnlTopbar.add(lblPageTitle, java.awt.BorderLayout.WEST);

        javax.swing.JPanel topbarRight = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        topbarRight.setOpaque(false);
        topbarRight.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));
        topbarRight.add(lblUser);
        notificationBell = new anilink.ui.NotificationBell(this::openMyOrders);
        topbarRight.add(notificationBell);
        pnlTopbar.add(topbarRight, java.awt.BorderLayout.CENTER);

        for (javax.swing.JButton nav : navButtons()) {
            nav.setFont(UiUtil.bold(12));
            nav.setForeground(UiUtil.WHITE);
            nav.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            nav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 8));
            nav.setFocusPainted(false);
            nav.setBorderPainted(false);
            nav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }
        // Logout rests on yellow-green and turns red under the pointer.
        btnLogout.setFont(UiUtil.bold(12));
        btnLogout.setFocusPainted(false);
        btnLogout.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogout.setBorder(UiUtil.roundedBorder(NAV_BORDER, UiUtil.RADIUS, 0, 12, 0, 8));
        UiUtil.setHoverBase(btnLogout, UiUtil.YELLOW_GREEN, UiUtil.ON_ACCENT);
        UiUtil.installHover(btnLogout, UiUtil.DANGER, UiUtil.WHITE);
    }

    private void showDashboardForRole() {
        String role = SessionContext.getRole();
        if ("farmer".equals(role)) {
            showCard(Cards.FARMER_DASHBOARD);
        } else if ("buyer".equals(role)) {
            showCard(Cards.BUYER_DASHBOARD);
        } else if ("admin".equals(role)) {
            showCard(Cards.ADMIN_DASHBOARD);
        } else if ("rider".equals(role)) {
            showCard(Cards.RIDER_DASHBOARD);
        } else {
            showCard(Cards.HOME);
        }
    }

    private void setupNavigation() {
        pnlContent.setLayout(cardLayout);

        homePanel = new HomePanel(this);
        marketplacePanel = new MarketplacePanel(this);
        productDetailPanel = new ProductDetailPanel(this);
        buyerDashboardPanel = new BuyerDashboardPanel(this);
        cartPanel = new CartPanel(this);
        checkoutPanel = new CheckoutPanel(this);
        orderConfirmationPanel = new OrderConfirmationPanel(this);
        buyerOrdersPanel = new BuyerOrdersPanel(this);
        buyerTransactionsPanel = new BuyerTransactionsPanel(this);
        buyerProfilePanel = new BuyerProfilePanel(this);
        farmerDashboardPanel = new FarmerDashboardPanel(this);
        farmerProductsPanel = new FarmerProductsPanel(this);
        addProductPanel = new AddProductPanel(this);
        editProductPanel = new EditProductPanel(this);
        farmerOrdersPanel = new FarmerOrdersPanel(this);
        farmerPaymentPanel = new FarmerPaymentPanel(this);
        salesPanel = new SalesPanel(this);
        farmerProfilePanel = new FarmerProfilePanel(this);
        farmerTransactionsPanel = new FarmerTransactionsPanel(this);
        adminDashboardPanel = new AdminDashboardPanel(this);
        userReportPanel = new UserReportPanel(this);
        addStaffPanel = new AddStaffPanel(this);
        adminProductsPanel = new AdminProductsPanel(this);
        adminOrdersPanel = new AdminOrdersPanel(this);
        manageDeliveriesPanel = new ManageDeliveriesPanel(this);
        paymentManagementPanel = new PaymentManagementPanel(this);
        reportsPanel = new ReportsPanel(this);
        adminProfilePanel = new AdminProfilePanel(this);
        riderDashboardPanel = new RiderDashboardPanel(this);
        riderOrdersPanel = new RiderDeliveriesPanel(this);
        riderSalesPanel = new RiderSalesReportPanel(this);
        riderTransactionsPanel = new RiderOrdersPanel(this, "Transaction history");

        pnlContent.add(homePanel, Cards.HOME);
        pnlContent.add(marketplacePanel, Cards.MARKETPLACE);
        pnlContent.add(productDetailPanel, Cards.PRODUCT);
        pnlContent.add(buyerDashboardPanel, Cards.BUYER_DASHBOARD);
        pnlContent.add(cartPanel, Cards.FAVORITES);
        pnlContent.add(checkoutPanel, Cards.CHECKOUT);
        pnlContent.add(orderConfirmationPanel, Cards.ORDER_SUCCESS);
        pnlContent.add(buyerOrdersPanel, Cards.BUYER_ORDERS);
        pnlContent.add(buyerTransactionsPanel, Cards.BUYER_TRANSACTIONS);
        pnlContent.add(buyerProfilePanel, Cards.BUYER_PROFILE);
        pnlContent.add(farmerDashboardPanel, Cards.FARMER_DASHBOARD);
        pnlContent.add(farmerProductsPanel, Cards.FARMER_PRODUCTS);
        pnlContent.add(addProductPanel, Cards.FARMER_ADD_PRODUCT);
        pnlContent.add(editProductPanel, Cards.FARMER_EDIT_PRODUCT);
        pnlContent.add(farmerOrdersPanel, Cards.FARMER_ORDERS);
        pnlContent.add(farmerPaymentPanel, Cards.FARMER_PAYMENTS);
        pnlContent.add(salesPanel, Cards.FARMER_SALES);
        pnlContent.add(farmerTransactionsPanel, Cards.FARMER_TRANSACTIONS);
        pnlContent.add(farmerProfilePanel, Cards.FARMER_PROFILE);
        pnlContent.add(riderDashboardPanel, Cards.RIDER_DASHBOARD);
        pnlContent.add(riderOrdersPanel, Cards.RIDER_ORDERS);
        pnlContent.add(riderSalesPanel, Cards.RIDER_SALES);
        pnlContent.add(riderTransactionsPanel, Cards.RIDER_TRANSACTIONS);
        pnlContent.add(adminDashboardPanel, Cards.ADMIN_DASHBOARD);
        pnlContent.add(userReportPanel, Cards.ADMIN_USER_REPORT);
        pnlContent.add(addStaffPanel, Cards.ADMIN_ADD_STAFF);
        pnlContent.add(adminProductsPanel, Cards.ADMIN_PRODUCTS);
        pnlContent.add(adminOrdersPanel, Cards.ADMIN_ORDERS);
        pnlContent.add(manageDeliveriesPanel, Cards.ADMIN_DELIVERIES);
        pnlContent.add(paymentManagementPanel, Cards.ADMIN_PAYMENTS);
        pnlContent.add(reportsPanel, Cards.ADMIN_REPORTS);
        pnlContent.add(adminProfilePanel, Cards.ADMIN_PROFILE);
    }

    private void applyRoleVisibility() {
        String role = SessionContext.getRole();
        boolean guest = !SessionContext.isLoggedIn();
        boolean buyer = "buyer".equals(role);
        boolean farmer = "farmer".equals(role);
        boolean admin = "admin".equals(role);
        boolean rider = "rider".equals(role);

        // Marketplace is for shoppers; the admin has no use for it.
        btnMarketplace.setVisible(guest || buyer);

        btnLogin.setVisible(guest);
        btnRegister.setVisible(guest);

        btnBuyerDashboard.setVisible(buyer);
        btnCart.setVisible(buyer);
        btnBuyerOrders.setVisible(buyer);
        btnBuyerTransactions.setVisible(buyer);

        btnFarmerDashboard.setVisible(farmer);
        btnFarmerProducts.setVisible(farmer);
        btnFarmerOrders.setVisible(farmer);
        btnFarmerPayments.setVisible(farmer);
        btnFarmerSales.setVisible(farmer);
        btnFarmerTransactions.setVisible(farmer);

        btnRiderDashboard.setVisible(rider);
        btnRiderOrders.setVisible(rider);
        btnRiderSales.setVisible(rider);
        btnRiderTransactions.setVisible(rider);

        btnAdminDashboard.setVisible(admin);
        btnAdminUserReport.setVisible(admin);
        btnAdminAddStaff.setVisible(admin);
        btnAdminProducts.setVisible(admin);
        btnAdminOrders.setVisible(admin);
        btnAdminDeliveries.setVisible(admin);
        btnAdminPayments.setVisible(admin);
        btnAdminReports.setVisible(admin);

        btnProfile.setVisible(!guest);
        btnLogout.setVisible(!guest);

        int y = 120;
        // Buyer order: Dashboard, Marketplace, Favorites, My Orders,
        // Transaction History. The dashboard leads, so it sits above
        // Marketplace rather than after it.
        if (btnBuyerDashboard.isVisible()) {
            placeNav(btnBuyerDashboard, y);
            y += 45;
        }
        if (btnMarketplace.isVisible()) {
            placeNav(btnMarketplace, y);
            y += 45;
        }
        if (btnLogin.isVisible()) {
            placeNav(btnLogin, y);
            y += 45;
        }
        if (btnRegister.isVisible()) {
            placeNav(btnRegister, y);
            y += 45;
        }
        if (btnCart.isVisible()) {
            placeNav(btnCart, y);
            y += 45;
        }
        if (btnBuyerOrders.isVisible()) {
            placeNav(btnBuyerOrders, y);
            y += 45;
        }
        if (btnBuyerTransactions.isVisible()) {
            placeNav(btnBuyerTransactions, y);
            y += 45;
        }
        if (btnFarmerDashboard.isVisible()) {
            placeNav(btnFarmerDashboard, y);
            y += 45;
        }
        if (btnFarmerProducts.isVisible()) {
            placeNav(btnFarmerProducts, y);
            y += 45;
        }
        if (btnFarmerOrders.isVisible()) {
            placeNav(btnFarmerOrders, y);
            y += 45;
        }
        if (btnFarmerPayments.isVisible()) {
            placeNav(btnFarmerPayments, y);
            y += 45;
        }
        if (btnFarmerSales.isVisible()) {
            placeNav(btnFarmerSales, y);
            y += 45;
        }
        if (btnFarmerTransactions.isVisible()) {
            placeNav(btnFarmerTransactions, y);
            y += 45;
        }
        if (btnRiderDashboard.isVisible()) {
            placeNav(btnRiderDashboard, y);
            y += 45;
        }
        if (btnRiderOrders.isVisible()) {
            placeNav(btnRiderOrders, y);
            y += 45;
        }
        if (btnRiderSales.isVisible()) {
            placeNav(btnRiderSales, y);
            y += 45;
        }
        if (btnRiderTransactions.isVisible()) {
            placeNav(btnRiderTransactions, y);
            y += 45;
        }
        if (btnAdminDashboard.isVisible()) {
            placeNav(btnAdminDashboard, y);
            y += 45;
        }
        if (btnAdminUserReport.isVisible()) {
            placeNav(btnAdminUserReport, y);
            y += 45;
        }
        if (btnAdminAddStaff.isVisible()) {
            placeNav(btnAdminAddStaff, y);
            y += 45;
        }
        if (btnAdminProducts.isVisible()) {
            placeNav(btnAdminProducts, y);
            y += 45;
        }
        if (btnAdminReports.isVisible()) {
            placeNav(btnAdminReports, y);
            y += 45;
        }
        if (btnAdminOrders.isVisible()) {
            placeNav(btnAdminOrders, y);
            y += 45;
        }
        if (btnAdminDeliveries.isVisible()) {
            placeNav(btnAdminDeliveries, y);
            y += 45;
        }
        if (btnAdminPayments.isVisible()) {
            placeNav(btnAdminPayments, y);
        }

        int sidebarHeight = pnlSidebar.getHeight();
        if (sidebarHeight > 0) {
            if (btnProfile.isVisible()) {
                placeNav(btnProfile, sidebarHeight - 87);
            }
            if (btnLogout.isVisible()) {
                placeNav(btnLogout, sidebarHeight - 42);
            }
        }

        if ("admin".equals(role)) {
            lblRole.setText("Administrator");
        } else if ("farmer".equals(role)) {
            lblRole.setText("Farmer");
        } else if ("buyer".equals(role)) {
            lblRole.setText("Buyer");
        } else if ("rider".equals(role)) {
            lblRole.setText("Rider");
        } else {
            lblRole.setText("Guest");
        }
    }

    private void placeNav(javax.swing.JButton btn, int y) {
        btn.setLocation(10, y);
        if (pnlSidebar.getLayout() instanceof org.netbeans.lib.awtextra.AbsoluteLayout) {
            org.netbeans.lib.awtextra.AbsoluteLayout abs
                    = (org.netbeans.lib.awtextra.AbsoluteLayout) pnlSidebar.getLayout();
            abs.removeLayoutComponent(btn);
            abs.addLayoutComponent(btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, y, 190, 35));
        }
    }

    private void updateUserLabel() {
        if (SessionContext.isLoggedIn() && SessionContext.getCurrentUser() != null) {
            lblUser.setText(SessionContext.getCurrentUser().getName()
                    + "  |  " + (SessionContext.getRole() == null ? "" : SessionContext.getRole().toUpperCase()));
        } else {
            lblUser.setText("Guest");
        }
        btnCart.setText("Favorites (" + SessionContext.cartCount() + ")");
    }

    public void refreshChrome() {
        applyRoleVisibility();
        updateUserLabel();
        if (notificationBell != null) {
            notificationBell.refresh();
        }
    }

    private boolean ensureSessionActive() {
        anilink.model.User current = SessionContext.getCurrentUser();
        if (current == null) {
            return false;
        }
        try {
            anilink.model.User fresh = new anilink.data.UserDAO().findById(current.getId()).orElse(null);
            if (fresh != null && !fresh.isActive()) {
                forceLogout("Your account has been disabled. Please contact the administrator.");
                return true;
            }
        } catch (java.sql.SQLException e) {
            // Keep the session if the status check itself fails (e.g. transient DB error).
        }
        return false;
    }

    private void forceLogout(String reason) {
        SessionContext.logout();
        dispose();
        LoginForm login = new LoginForm();
        login.setLocationRelativeTo(null);
        login.setError(reason);
        login.setVisible(true);
    }

    private static boolean isAdminCard(String name) {
        return Cards.ADMIN_DASHBOARD.equals(name)
                || Cards.ADMIN_USER_REPORT.equals(name)
                || Cards.ADMIN_ADD_STAFF.equals(name)
                || Cards.ADMIN_PRODUCTS.equals(name)
                || Cards.ADMIN_ORDERS.equals(name)
                || Cards.ADMIN_DELIVERIES.equals(name)
                || Cards.ADMIN_PAYMENTS.equals(name)
                || Cards.ADMIN_REPORTS.equals(name);
    }

    public void showCard(String name) {
        if (ensureSessionActive()) {
            return;
        }
        if (isAdminCard(name) && !"admin".equals(SessionContext.getRole())) {
            UiUtil.info(this, "Administrator access is required to view that page.");
            showDashboardForRole();
            return;
        }
        refreshChrome();
        if (homePanel != null && Cards.HOME.equals(name)) {
            homePanel.refreshData();
        } else if (marketplacePanel != null && Cards.MARKETPLACE.equals(name)) {
            marketplacePanel.refreshData();
        } else if (productDetailPanel != null && Cards.PRODUCT.equals(name)) {
            productDetailPanel.refreshData();
        } else if (buyerDashboardPanel != null && Cards.BUYER_DASHBOARD.equals(name)) {
            buyerDashboardPanel.refreshData();
        } else if (cartPanel != null && Cards.FAVORITES.equals(name)) {
            cartPanel.refreshData();
        } else if (checkoutPanel != null && Cards.CHECKOUT.equals(name)) {
            checkoutPanel.refreshData();
        } else if (buyerOrdersPanel != null && Cards.BUYER_ORDERS.equals(name)) {
            buyerOrdersPanel.refreshData();
        } else if (buyerTransactionsPanel != null && Cards.BUYER_TRANSACTIONS.equals(name)) {
            buyerTransactionsPanel.refreshData();
        } else if (buyerProfilePanel != null && Cards.BUYER_PROFILE.equals(name)) {
            buyerProfilePanel.refreshData();
        } else if (farmerDashboardPanel != null && Cards.FARMER_DASHBOARD.equals(name)) {
            farmerDashboardPanel.refreshData();
        } else if (farmerProductsPanel != null && Cards.FARMER_PRODUCTS.equals(name)) {
            farmerProductsPanel.refreshData();
        } else if (farmerOrdersPanel != null && Cards.FARMER_ORDERS.equals(name)) {
            farmerOrdersPanel.refreshData();
        } else if (farmerPaymentPanel != null && Cards.FARMER_PAYMENTS.equals(name)) {
            farmerPaymentPanel.refreshData();
        } else if (salesPanel != null && Cards.FARMER_SALES.equals(name)) {
            salesPanel.refreshData();
        } else if (farmerTransactionsPanel != null && Cards.FARMER_TRANSACTIONS.equals(name)) {
            farmerTransactionsPanel.refreshData();
        } else if (farmerProfilePanel != null && Cards.FARMER_PROFILE.equals(name)) {
            farmerProfilePanel.refreshData();
        } else if (riderDashboardPanel != null && Cards.RIDER_DASHBOARD.equals(name)) {
            riderDashboardPanel.refreshData();
        } else if (riderOrdersPanel != null && Cards.RIDER_ORDERS.equals(name)) {
            riderOrdersPanel.refreshData();
        } else if (riderSalesPanel != null && Cards.RIDER_SALES.equals(name)) {
            riderSalesPanel.refreshData();
        } else if (riderTransactionsPanel != null && Cards.RIDER_TRANSACTIONS.equals(name)) {
            riderTransactionsPanel.refreshData();
        } else if (adminDashboardPanel != null && Cards.ADMIN_DASHBOARD.equals(name)) {
            adminDashboardPanel.refreshData();
        } else if (userReportPanel != null && Cards.ADMIN_USER_REPORT.equals(name)) {
            userReportPanel.refreshData();
        } else if (addStaffPanel != null && Cards.ADMIN_ADD_STAFF.equals(name)) {
            addStaffPanel.refreshData();
        } else if (adminProductsPanel != null && Cards.ADMIN_PRODUCTS.equals(name)) {
            adminProductsPanel.refreshData();
            } else if (adminOrdersPanel != null && Cards.ADMIN_ORDERS.equals(name)) {
                adminOrdersPanel.refreshData();
            } else if (manageDeliveriesPanel != null && Cards.ADMIN_DELIVERIES.equals(name)) {
                manageDeliveriesPanel.refreshData();
            } else if (paymentManagementPanel != null && Cards.ADMIN_PAYMENTS.equals(name)) {
                paymentManagementPanel.refreshData();
        } else if (reportsPanel != null && Cards.ADMIN_REPORTS.equals(name)) {
            reportsPanel.refreshData();
        } else if (adminProfilePanel != null && Cards.ADMIN_PROFILE.equals(name)) {
            adminProfilePanel.refreshData();
        }

        cardLayout.show(pnlContent, name);
        lblPageTitle.setText(titleFor(name));
        highlightNav(name);
        pnlContent.revalidate();
        pnlContent.repaint();
        scrollContent.getVerticalScrollBar().setValue(0);
    }

    public void openProduct(int productId) {
        productDetailPanel.loadProduct(productId);
        showCard(Cards.PRODUCT);
    }

    public void openEditProduct(int productId) {
        editProductPanel.loadProduct(productId);
        showCard(Cards.FARMER_EDIT_PRODUCT);
    }

    public void openCheckout() {
        showCard(Cards.CHECKOUT);
    }

    public void showOrderConfirmation(int orderId) {
        orderConfirmationPanel.loadOrder(orderId);
        showCard(Cards.ORDER_SUCCESS);
    }

    public void openBuyerOrders(int orderId) {
        showCard(Cards.BUYER_ORDERS);
        buyerOrdersPanel.selectOrder(orderId);
    }

    /**
     * Routes a notification-tray click to whichever orders page belongs to
     * the signed-in role. Farmer and rider land on their orders page without
     * a specific row selected - only My Orders currently supports jumping to
     * one order directly.
     */
    public void openMyOrders(int orderId) {
        if (SessionContext.isRole("buyer")) {
            openBuyerOrders(orderId);
        } else if (SessionContext.isRole("farmer")) {
            showCard(Cards.FARMER_ORDERS);
        } else if (SessionContext.isRole("rider")) {
            showCard(Cards.RIDER_ORDERS);
        }
    }

    private String titleFor(String name) {
        switch (name) {
            case Cards.HOME:
                return "Home";
            case Cards.MARKETPLACE:
                return "Marketplace";
            case Cards.PRODUCT:
                return "Product Details";
            case Cards.BUYER_DASHBOARD:
                return "Buyer Dashboard";
            case Cards.FAVORITES:
                return "Favorites";
            case Cards.CHECKOUT:
                return "Checkout";
            case Cards.ORDER_SUCCESS:
                return "Order Confirmation";
            case Cards.BUYER_ORDERS:
                return "My Orders";
            case Cards.BUYER_TRANSACTIONS:
                return "Transaction History";
            case Cards.BUYER_PROFILE:
                return "My Profile";
            case Cards.FARMER_DASHBOARD:
                return "Farmer Dashboard";
            case Cards.FARMER_PRODUCTS:
                return "Manage Products";
            case Cards.FARMER_ADD_PRODUCT:
                return "Add Product";
            case Cards.FARMER_EDIT_PRODUCT:
                return "Edit Product";
            case Cards.FARMER_ORDERS:
                return "Manage Order";
            case Cards.FARMER_PAYMENTS:
                return "Manage Payment";
            case Cards.FARMER_SALES:
                return "Sales";
            case Cards.FARMER_TRANSACTIONS:
                return "Transaction History";
            case Cards.FARMER_PROFILE:
                return "My Profile";
            case Cards.RIDER_DASHBOARD:
                return "Rider Dashboard";
            case Cards.RIDER_ORDERS:
                return "Manage delivery";
            case Cards.RIDER_SALES:
                return "Orders report";
            case Cards.RIDER_TRANSACTIONS:
                return "Transaction history";
            case Cards.ADMIN_DASHBOARD:
                return "Admin Dashboard";
            case Cards.ADMIN_ADD_STAFF:
                return "Add Delivery Man";
            case Cards.ADMIN_USER_REPORT:
                return "User Report";
            case Cards.ADMIN_PRODUCTS:
                return "Products";
            case Cards.ADMIN_DELIVERIES:
                return "Manage Deliveries";
            case Cards.ADMIN_ORDERS:
                return "Transaction history";
            case Cards.ADMIN_PAYMENTS:
                return "Manage Delivery Payment";
            case Cards.ADMIN_REPORTS:
                return "Sales report";
            case Cards.ADMIN_PROFILE:
                return "My Profile";
            default:
                return "AniLink";
        }
    }

    /**
     * Plain {@link CardLayout} reports its preferred/minimum size as the max
     * over <em>every</em> card ever added, even the ones not showing - with
     * ~25 cards sharing one {@code pnlContent}, that means every page gets
     * stretched to fit whichever single page is tallest anywhere in the app.
     * {@code scrollContent} (the shared outer scroll pane wrapping
     * {@code pnlContent}) then has to scroll just to reach the bottom of a
     * short page, because the page was forced taller than it needed to be.
     *
     * <p>Overriding both methods to measure only the one visible card fixes
     * that for every page at once: each page reports (and is sized to) its
     * own real content height, so a short page like Favorites no longer
     * inherits scrolling it never needed.
     */
    private static final class VisibleCardLayout extends CardLayout {

        @Override
        public java.awt.Dimension preferredLayoutSize(Container parent) {
            Component visible = visibleComponent(parent);
            if (visible == null) {
                return super.preferredLayoutSize(parent);
            }
            java.awt.Insets insets = parent.getInsets();
            java.awt.Dimension d = visible.getPreferredSize();
            return new java.awt.Dimension(d.width + insets.left + insets.right,
                    d.height + insets.top + insets.bottom);
        }

        @Override
        public java.awt.Dimension minimumLayoutSize(Container parent) {
            Component visible = visibleComponent(parent);
            if (visible == null) {
                return super.minimumLayoutSize(parent);
            }
            java.awt.Insets insets = parent.getInsets();
            java.awt.Dimension d = visible.getMinimumSize();
            return new java.awt.Dimension(d.width + insets.left + insets.right,
                    d.height + insets.top + insets.bottom);
        }

        private Component visibleComponent(Container parent) {
            synchronized (parent.getTreeLock()) {
                for (Component c : parent.getComponents()) {
                    if (c.isVisible()) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSidebar = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblBrand = new javax.swing.JLabel();
        lblRole = new javax.swing.JLabel();
        btnMarketplace = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        btnBuyerDashboard = new javax.swing.JButton();
        btnCart = new javax.swing.JButton();
        btnBuyerTransactions = new javax.swing.JButton();
        btnBuyerOrders = new javax.swing.JButton();
        btnFarmerDashboard = new javax.swing.JButton();
        btnFarmerProducts = new javax.swing.JButton();
        btnFarmerOrders = new javax.swing.JButton();
        btnFarmerPayments = new javax.swing.JButton();
        btnFarmerSales = new javax.swing.JButton();
        btnFarmerTransactions = new javax.swing.JButton();
        btnRiderDashboard = new javax.swing.JButton();
        btnRiderOrders = new javax.swing.JButton();
        btnRiderSales = new javax.swing.JButton();
        btnRiderTransactions = new javax.swing.JButton();
        btnAdminDashboard = new javax.swing.JButton();
        btnAdminAddStaff = new javax.swing.JButton();
        btnAdminProducts = new javax.swing.JButton();
        btnAdminOrders = new javax.swing.JButton();
        btnAdminDeliveries = new javax.swing.JButton();
        btnAdminPayments = new javax.swing.JButton();
        btnAdminReports = new javax.swing.JButton();
        btnAdminUserReport = new javax.swing.JButton();
        btnProfile = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        pnlTopbar = new javax.swing.JPanel();
        lblPageTitle = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        scrollContent = new javax.swing.JScrollPane();
        pnlContent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AniLink - Local Farmer Product Marketplace");
        setMinimumSize(new java.awt.Dimension(1100, 720));

        pnlSidebar.setBackground(new java.awt.Color(27, 94, 32));
        pnlSidebar.setPreferredSize(new java.awt.Dimension(210, 670));
        pnlSidebar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/anilink-logo-40.png")));
        pnlSidebar.add(lblLogo, new org.netbeans.lib.awtextra.AbsoluteConstraints(85, 12, 40, 40));

        lblBrand.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblBrand.setForeground(new java.awt.Color(255, 255, 255));
        lblBrand.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBrand.setText("AniLink");
        pnlSidebar.add(lblBrand, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 58, 190, 27));

        lblRole.setForeground(new java.awt.Color(165, 214, 167));
        lblRole.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRole.setText(" ");
        pnlSidebar.add(lblRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 88, 190, 20));

        styleNavButton(btnMarketplace, "Marketplace");
        btnMarketplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMarketplaceActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnMarketplace, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 165, 190, 35));

        styleNavButton(btnLogin, "Login");
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 190, 35));

        styleNavButton(btnRegister, "Register");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 255, 190, 35));

        styleNavButton(btnBuyerDashboard, "Dashboard");
        btnBuyerDashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuyerDashboardActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnBuyerDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 190, 35));

        styleNavButton(btnCart, "Favorites (0)");
        btnCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCartActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnCart, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 255, 190, 35));

        styleNavButton(btnBuyerOrders, "My Orders");
        btnBuyerOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuyerOrdersActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnBuyerOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 190, 35));

        styleNavButton(btnBuyerTransactions, "Transaction History");
        btnBuyerTransactions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuyerTransactionsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnBuyerTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 345, 190, 35));

        styleNavButton(btnFarmerDashboard, "Dashboard");
        btnFarmerDashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFarmerDashboardActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnFarmerDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 165, 190, 35));

        styleNavButton(btnFarmerProducts, "Manage Products");
        btnFarmerProducts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFarmerProductsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnFarmerProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 190, 35));

        styleNavButton(btnFarmerOrders, "Manage Order");
        btnFarmerOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFarmerOrdersActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnFarmerOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 255, 190, 35));

        styleNavButton(btnFarmerPayments, "Manage Payment");
        btnFarmerPayments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFarmerPaymentsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnFarmerPayments, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 190, 35));

        styleNavButton(btnFarmerSales, "Sales");
        btnFarmerSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFarmerSalesActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnFarmerSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 190, 35));

        styleNavButton(btnFarmerTransactions, "Transaction History");
        btnFarmerTransactions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFarmerTransactionsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnFarmerTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 345, 190, 35));

        styleNavButton(btnRiderDashboard, "Dashboard");
        btnRiderDashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRiderDashboardActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnRiderDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 165, 190, 35));

        styleNavButton(btnRiderOrders, "Manage delivery");
        btnRiderOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRiderOrdersActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnRiderOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 190, 35));

        styleNavButton(btnRiderSales, "Orders report");
        btnRiderSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRiderSalesActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnRiderSales, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 255, 190, 35));

        styleNavButton(btnRiderTransactions, "Transaction history");
        btnRiderTransactions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRiderTransactionsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnRiderTransactions, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 190, 35));

        styleNavButton(btnAdminDashboard, "Dashboard");
        btnAdminDashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminDashboardActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminDashboard, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 165, 190, 35));

        styleNavButton(btnAdminAddStaff, "Add Delivery Man");
        btnAdminAddStaff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminAddStaffActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminAddStaff, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 210, 190, 35));

        styleNavButton(btnAdminProducts, "Products");
        btnAdminProducts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminProductsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminProducts, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 255, 190, 35));

        styleNavButton(btnAdminOrders, "Transaction history");
        btnAdminOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminOrdersActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 345, 190, 35));

        styleNavButton(btnAdminDeliveries, "Manage Deliveries");
        btnAdminDeliveries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminDeliveriesActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminDeliveries, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 190, 35));

        styleNavButton(btnAdminReports, "Sales report");
        btnAdminReports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminReportsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminReports, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 300, 190, 35));

        styleNavButton(btnAdminUserReport, "User Report");
        btnAdminUserReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminUserReportActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminUserReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 390, 190, 35));

        styleNavButton(btnAdminPayments, "Manage Delivery Payment");
        btnAdminPayments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdminPaymentsActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnAdminPayments, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 480, 190, 35));

        styleNavButton(btnProfile, "Profile");
        btnProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProfileActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 590, 190, 35));

        btnLogout.setBackground(new java.awt.Color(211, 47, 47));
        btnLogout.setFocusPainted(false);
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("Logout");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        pnlSidebar.add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 635, 190, 35));

        pnlTopbar.setBackground(new java.awt.Color(255, 255, 255));

        lblPageTitle.setFont(new java.awt.Font("Segoe UI", 1, 16));
        lblPageTitle.setText("Home");

        lblUser.setForeground(new java.awt.Color(96, 125, 107));
        lblUser.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUser.setText(" ");

        javax.swing.GroupLayout pnlTopbarLayout = new javax.swing.GroupLayout(pnlTopbar);
        pnlTopbar.setLayout(pnlTopbarLayout);
        pnlTopbarLayout.setHorizontalGroup(
            pnlTopbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTopbarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblPageTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(lblUser)
                .addGap(20, 20, 20))
        );
        pnlTopbarLayout.setVerticalGroup(
            pnlTopbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTopbarLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlTopbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPageTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );

        scrollContent.setBorder(null);
        scrollContent.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        pnlContent.setBackground(new java.awt.Color(248, 250, 248));
        pnlContent.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        scrollContent.setViewportView(pnlContent);

        getContentPane().add(pnlSidebar, java.awt.BorderLayout.WEST);
        getContentPane().add(pnlTopbar, java.awt.BorderLayout.NORTH);
        getContentPane().add(scrollContent, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private static final java.awt.Color NAV_IDLE = new java.awt.Color(27, 94, 32);
    private static final java.awt.Color NAV_ACTIVE = new java.awt.Color(39, 110, 43);
    /** Soft border around each sidebar item, a shade lighter than the panel. */
    private static final java.awt.Color NAV_BORDER = new java.awt.Color(58, 122, 62);

    /** Every sidebar nav button (excluding Logout, which is styled apart). */
    private javax.swing.JButton[] navButtons() {
        return new javax.swing.JButton[]{
            btnMarketplace, btnLogin, btnRegister, btnBuyerDashboard, btnCart, btnBuyerOrders, btnBuyerTransactions,
            btnFarmerDashboard, btnFarmerProducts, btnFarmerOrders, btnFarmerPayments, btnFarmerSales, btnFarmerTransactions,
            btnRiderDashboard, btnRiderOrders, btnRiderSales, btnRiderTransactions,
            btnAdminDashboard, btnAdminAddStaff, btnAdminProducts, btnAdminOrders, btnAdminDeliveries, btnAdminReports,
            btnAdminPayments,
            btnAdminUserReport,
            btnProfile};
    }

    private void highlightNav(String name) {
        for (javax.swing.JButton b : navButtons()) {
            UiUtil.setHoverBase(b, NAV_IDLE, UiUtil.WHITE);
        }
        javax.swing.JButton active = navButtonFor(name);
        if (active != null) {
            UiUtil.setHoverBase(active, NAV_ACTIVE, UiUtil.WHITE);
        }
    }

    private javax.swing.JButton navButtonFor(String name) {
        switch (name) {
            case Cards.MARKETPLACE:
            case Cards.PRODUCT:
                return btnMarketplace;
            case Cards.BUYER_DASHBOARD:
                return btnBuyerDashboard;
            case Cards.FAVORITES:
            case Cards.CHECKOUT:
                return btnCart;
            case Cards.BUYER_ORDERS:
                return btnBuyerOrders;
            case Cards.BUYER_TRANSACTIONS:
                return btnBuyerTransactions;
            case Cards.FARMER_DASHBOARD:
                return btnFarmerDashboard;
            case Cards.FARMER_PRODUCTS:
            case Cards.FARMER_ADD_PRODUCT:
            case Cards.FARMER_EDIT_PRODUCT:
                return btnFarmerProducts;
            case Cards.FARMER_ORDERS:
                return btnFarmerOrders;
            case Cards.FARMER_PAYMENTS:
                return btnFarmerPayments;
            case Cards.FARMER_SALES:
                return btnFarmerSales;
            case Cards.FARMER_TRANSACTIONS:
                return btnFarmerTransactions;
            case Cards.RIDER_DASHBOARD:
                return btnRiderDashboard;
            case Cards.RIDER_ORDERS:
                return btnRiderOrders;
            case Cards.RIDER_SALES:
                return btnRiderSales;
            case Cards.RIDER_TRANSACTIONS:
                return btnRiderTransactions;
            case Cards.ADMIN_DASHBOARD:
                return btnAdminDashboard;
            case Cards.ADMIN_USER_REPORT:
                return btnAdminUserReport;
            case Cards.ADMIN_ADD_STAFF:
                return btnAdminAddStaff;
            case Cards.ADMIN_PRODUCTS:
                return btnAdminProducts;
            case Cards.ADMIN_ORDERS:
                return btnAdminOrders;
            case Cards.ADMIN_DELIVERIES:
                return btnAdminDeliveries;
            case Cards.ADMIN_PAYMENTS:
                return btnAdminPayments;
            case Cards.ADMIN_REPORTS:
                return btnAdminReports;
            case Cards.BUYER_PROFILE:
            case Cards.FARMER_PROFILE:
            case Cards.ADMIN_PROFILE:
                return btnProfile;
            default:
                return null;
        }
    }

    private void styleNavButton(javax.swing.JButton btn, String text) {
        btn.setBackground(NAV_IDLE);
        btn.setFocusPainted(false);
        btn.setForeground(UiUtil.WHITE);
        btn.setText(text);
        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setFont(UiUtil.bold(12));
        btn.setBorder(UiUtil.roundedBorder(NAV_BORDER, UiUtil.RADIUS, 0, 12, 0, 8));
        UiUtil.installHover(btn, UiUtil.ACCENT, UiUtil.ON_ACCENT);
    }

    private void btnMarketplaceActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.MARKETPLACE);
    }

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {
        LoginForm login = new LoginForm(this);
        login.setLocationRelativeTo(this);
        login.setVisible(true);
    }

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
        RegisterForm register = new RegisterForm(this, true);
        register.setLocationRelativeTo(this);
        register.setVisible(true);
    }

    private void btnBuyerDashboardActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.BUYER_DASHBOARD);
    }

    private void btnCartActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FAVORITES);
    }

    private void btnBuyerOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.BUYER_ORDERS);
    }

    private void btnBuyerTransactionsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.BUYER_TRANSACTIONS);
    }

    private void btnFarmerDashboardActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FARMER_DASHBOARD);
    }

    private void btnFarmerProductsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FARMER_PRODUCTS);
    }

    private void btnFarmerOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FARMER_ORDERS);
    }

    private void btnFarmerPaymentsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FARMER_PAYMENTS);
    }

    private void btnFarmerSalesActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FARMER_SALES);
    }

    private void btnFarmerTransactionsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.FARMER_TRANSACTIONS);
    }

    private void btnRiderDashboardActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.RIDER_DASHBOARD);
    }

    private void btnRiderOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.RIDER_ORDERS);
    }

    private void btnRiderSalesActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.RIDER_SALES);
    }

    private void btnRiderTransactionsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.RIDER_TRANSACTIONS);
    }

    private void btnAdminDashboardActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_DASHBOARD);
    }

    private void btnAdminAddStaffActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_ADD_STAFF);
    }

    private void btnAdminProductsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_PRODUCTS);
    }

    private void btnAdminOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_ORDERS);
    }

    private void btnAdminDeliveriesActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_DELIVERIES);
    }

    private void btnAdminReportsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_REPORTS);
    }

    private void btnAdminUserReportActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_USER_REPORT);
    }

    private void btnAdminPaymentsActionPerformed(java.awt.event.ActionEvent evt) {
        showCard(Cards.ADMIN_PAYMENTS);
    }

    private void btnProfileActionPerformed(java.awt.event.ActionEvent evt) {
        String role = SessionContext.getRole();
        if ("farmer".equals(role)) {
            showCard(Cards.FARMER_PROFILE);
        } else if ("admin".equals(role) || "rider".equals(role)) {
            showCard(Cards.ADMIN_PROFILE);
        } else {
            showCard(Cards.BUYER_PROFILE);
        }
    }

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {
        if (!UiUtil.confirm(this, "Are you sure you want to log out?")) {
            return;
        }
        SessionContext.logout();
        dispose();
        GuestHomeFrame guest = new GuestHomeFrame();
        guest.setLocationRelativeTo(null);
        guest.setVisible(true);
    }

    public void onLoginSuccess() {
        refreshChrome();
        showDashboardForRole();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdminAddStaff;
    private javax.swing.JButton btnAdminDashboard;
    private javax.swing.JButton btnAdminUserReport;
    private javax.swing.JButton btnAdminOrders;
    private javax.swing.JButton btnAdminDeliveries;
    private javax.swing.JButton btnAdminPayments;
    private javax.swing.JButton btnAdminProducts;
    private javax.swing.JButton btnAdminReports;
    private javax.swing.JButton btnBuyerDashboard;
    private javax.swing.JButton btnBuyerOrders;
    private javax.swing.JButton btnBuyerTransactions;
    private javax.swing.JButton btnCart;
    private javax.swing.JButton btnFarmerDashboard;
    private javax.swing.JButton btnFarmerOrders;
    private javax.swing.JButton btnFarmerPayments;
    private javax.swing.JButton btnFarmerProducts;
    private javax.swing.JButton btnFarmerSales;
    private javax.swing.JButton btnFarmerTransactions;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMarketplace;
    private javax.swing.JButton btnProfile;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnRiderDashboard;
    private javax.swing.JButton btnRiderOrders;
    private javax.swing.JButton btnRiderSales;
    private javax.swing.JButton btnRiderTransactions;
    private javax.swing.JLabel lblBrand;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblPageTitle;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlSidebar;
    private javax.swing.JPanel pnlTopbar;
    private javax.swing.JScrollPane scrollContent;
    // End of variables declaration//GEN-END:variables
}
