package anilink.farmer;

import anilink.data.ProductDAO;
import anilink.model.Product;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Manage Products: the farmer's product catalog, with search, a date filter,
 * 10-row pagination (both via {@link TablePager}) and a select-then-click
 * Update/View Details action row sharing the date filter's own row (via
 * {@link TablePager#dateFilterTrailing()}), the same pattern used by
 * {@link anilink.admin.PaymentManagementPanel}'s View Details/Assign
 * Delivery Man toolbar.
 */
public class FarmerProductsPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    /** Every one of this farmer's products, as loaded from the database. */
    private final List<Product> products = new ArrayList<>();
    /** The subset currently handed to the pager, in the order added - what
     * {@link TablePager#sourceIndexOf(int)} actually indexes into. */
    private final List<Product> visible = new ArrayList<>();
    private final JButton btnUpdate = new JButton("Update");
    private final JButton btnDetails = new JButton("View Details");
    private TablePager pager;
    private JTextField txtSearch;

    public FarmerProductsPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblProducts.setDefaultEditor(Object.class, null);
        tblProducts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ((DefaultTableModel) tblProducts.getModel()).setColumnIdentifiers(
                new Object[]{"ID", "Image", "Name", "Category", "Price", "Qty", "Status", "Unit"});
        UiUtil.styleBanner(lblTitle, "Manage Products");
        UiUtil.styleTable(tblProducts);
        UiUtil.colorColumn(tblProducts, 6, label -> {
            switch (label) {
                case "AVAILABLE":
                    return UiUtil.PRIMARY;
                case "LOW STOCK":
                    return UiUtil.ACCENT_DARK;
                case "OUT OF STOCK":
                    return UiUtil.DANGER;
                default:
                    return null;
            }
        });
        buildToolbar();
        pager = TablePager.install(tblProducts, true);

        JPanel trailing = pager.dateFilterTrailing();
        if (trailing != null) {
            UiUtil.styleTintedButton(btnUpdate);
            btnUpdate.setPreferredSize(new Dimension(100, 32));
            btnUpdate.addActionListener(e -> {
                if (selectedProduct() == null) {
                    UiUtil.info(this, "Please select a product first.");
                    return;
                }
                openEdit(tblProducts.getSelectedRow());
            });
            trailing.add(btnUpdate);

            UiUtil.stylePrimary(btnDetails);
            btnDetails.setPreferredSize(new Dimension(130, 32));
            btnDetails.addActionListener(e -> {
                if (selectedProduct() == null) {
                    UiUtil.info(this, "Please select a product first.");
                    return;
                }
                openDetails(tblProducts.getSelectedRow());
            });
            trailing.add(btnDetails);
        }
    }

    private void buildToolbar() {
        pnlToolbar.setLayout(new BorderLayout(10, 0));
        pnlToolbar.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Search products by name...");
        UiUtil.styleSearchField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(280, 34));
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                render();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                render();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                render();
            }
        });
        pnlToolbar.add(txtSearch, BorderLayout.WEST);

        JButton btnAddProduct = new JButton("Add Product");
        UiUtil.stylePrimary(btnAddProduct);
        btnAddProduct.setPreferredSize(new Dimension(150, 36));
        btnAddProduct.addActionListener(e -> frame.showCard(Cards.FARMER_ADD_PRODUCT));
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        right.add(btnAddProduct);
        pnlToolbar.add(right, BorderLayout.EAST);
    }

    public void refreshData() {
        try {
            int farmerId = SessionContext.getFarmerId();
            products.clear();
            if (farmerId > 0) {
                products.addAll(new ProductDAO().findByFarmer(farmerId));
            }
            render();
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    /** Re-filters {@link #products} by the search box and refills the pager. */
    private void render() {
        String term = txtSearch == null || txtSearch.getText() == null
                ? "" : txtSearch.getText().trim().toLowerCase();
        visible.clear();
        pager.clear();
        int rowNumber = 1;
        for (Product p : products) {
            if (!term.isEmpty() && (p.getProductName() == null
                    || !p.getProductName().toLowerCase().contains(term))) {
                continue;
            }
            visible.add(p);
            pager.addRow(new Object[]{
                rowNumber++,
                p.getImage() == null ? "" : p.getImage(),
                p.getProductName(),
                p.getCategoryName() == null ? "" : p.getCategoryName(),
                UiUtil.formatPrice(p.getPrice()),
                p.getQuantity(),
                productStatusLabel(p),
                p.getUnit()
            }, p.getCreatedAt());
        }
        pager.apply();
    }

    /** Low stock isn't a status this app stores - it's "available" with a
     * low quantity - so it's derived here instead of coming straight from
     * {@link UiUtil#statusBadge}. Checking quantity first for "out of
     * stock" also means a product reads correctly even if its stored
     * {@code status} hasn't caught up with {@code quantity} hitting 0. */
    private static final int LOW_STOCK_THRESHOLD = 10;

    private String productStatusLabel(Product p) {
        if (p.getQuantity() <= 0) {
            return "OUT OF STOCK";
        }
        if ("available".equals(p.getStatus()) && p.getQuantity() <= LOW_STOCK_THRESHOLD) {
            return "LOW STOCK";
        }
        return UiUtil.statusBadge(p.getStatus());
    }

    private Product productAt(int modelRow) {
        int sourceIndex = pager.sourceIndexOf(modelRow);
        return sourceIndex < 0 || sourceIndex >= visible.size() ? null : visible.get(sourceIndex);
    }

    private Product selectedProduct() {
        return productAt(tblProducts.getSelectedRow());
    }

    private void openEdit(int modelRow) {
        Product product = productAt(modelRow);
        if (product == null) {
            return;
        }
        new FarmerProductEditDialog(frame, product).setVisible(true);
        refreshData();
    }

    private void openDetails(int modelRow) {
        Product product = productAt(modelRow);
        if (product == null) {
            return;
        }
        new FarmerProductViewDialog(frame, product).setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlToolbar = new javax.swing.JPanel();
        scrollProducts = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Manage Products");

        // Search field and Add Product button are built at runtime in buildToolbar().
        javax.swing.GroupLayout pnlToolbarLayout = new javax.swing.GroupLayout(pnlToolbar);
        pnlToolbar.setLayout(pnlToolbarLayout);
        pnlToolbarLayout.setHorizontalGroup(
            pnlToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 760, Short.MAX_VALUE)
        );
        pnlToolbarLayout.setVerticalGroup(
            pnlToolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        scrollProducts.setViewportView(tblProducts);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(scrollProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlToolbar;
    private javax.swing.JScrollPane scrollProducts;
    private javax.swing.JTable tblProducts;
    // End of variables declaration//GEN-END:variables
}
