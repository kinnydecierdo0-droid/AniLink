package anilink.admin;

import anilink.data.CategoryDAO;
import anilink.data.ProductDAO;
import anilink.model.Category;
import anilink.model.Product;
import anilink.shell.MainFrame;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class AdminProductsPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private final List<Product> products = new ArrayList<>();
    private TablePager pager;

    public AdminProductsPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        setupTable(tblProducts, new String[]{"ID", "Name", "Category", "Farmer",
            "Price", "Qty", "Unit"});
        UiUtil.styleBanner(lblTitle, "Products");
        UiUtil.styleFieldLabel(lblSearch);
        UiUtil.styleFieldLabel(lblCategory);
        UiUtil.stylePrimary(btnFilter);
        UiUtil.styleSearchField(txtSearch);
        UiUtil.styleStrip(pnlFilter);
        pnlFilter.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, UiUtil.RADIUS, 8, 12, 8, 12));
        pnlFilter.setBackground(UiUtil.WHITE);
        UiUtil.styleTable(tblProducts);
        pager = TablePager.install(tblProducts, false);
        cmbCategory.addItem("All Categories");
    }


    public void refreshData() {
        try {
            int selectedId = getSelectedProductId();

            int previousCategory = -1;
            Object selectedCategory = cmbCategory.getSelectedItem();
            if (selectedCategory instanceof Category) {
                previousCategory = ((Category) selectedCategory).getId();
            }
            cmbCategory.removeAllItems();
            cmbCategory.addItem("All Categories");
            for (Category category : new CategoryDAO().findAll()) {
                cmbCategory.addItem(category);
                if (category.getId() == previousCategory) {
                    cmbCategory.setSelectedItem(category);
                }
            }

            pager.clear();
            products.clear();

            Object filter = cmbCategory.getSelectedItem();
            Integer categoryId = filter instanceof Category ? ((Category) filter).getId() : null;
            int rowNumber = 1;
            for (Product product : new ProductDAO().findAll(txtSearch.getText(), categoryId)) {
                products.add(product);
                pager.addRow(new Object[]{
                    rowNumber++,
                    product.getProductName(),
                    product.getCategoryName() == null ? "-" : product.getCategoryName(),
                    product.getFarmerName(),
                    UiUtil.formatPrice(product.getPrice()),
                    product.getQuantity(),
                    product.getUnit()
                }, null, product.getId());
            }
            pager.apply();
            reselectProduct(selectedId);
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private static void setupTable(javax.swing.JTable table, String[] columns) {
        table.setModel(new DefaultTableModel(new Object[0][0], columns) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        table.setDefaultEditor(Object.class, null);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private Product getSelectedProduct() {
        int row = tblProducts.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int sourceIndex = pager.sourceIndexOf(tblProducts.convertRowIndexToModel(row));
        if (sourceIndex < 0 || sourceIndex >= products.size()) {
            return null;
        }
        return products.get(sourceIndex);
    }

    private int getSelectedProductId() {
        Product product = getSelectedProduct();
        return product == null ? -1 : product.getId();
    }

    private void reselectProduct(int productId) {
        if (productId < 0) {
            tblProducts.clearSelection();
            return;
        }
        // Select by the row's key (the real id), not column 0 - that now
        // shows a friendly number instead of the id.
        if (!pager.selectRowByKey(productId)) {
            tblProducts.clearSelection();
        }
    }

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {
        refreshData();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlFilter = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        lblCategory = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<Object>();
        btnFilter = new javax.swing.JButton();
        scrollProducts = new javax.swing.JScrollPane();
        tblProducts = new javax.swing.JTable();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Products");

        pnlFilter.setBackground(new java.awt.Color(248, 250, 248));

        lblSearch.setText("Search:");

        txtSearch.setToolTipText("Search products by name");

        lblCategory.setText("Category:");

        btnFilter.setBackground(new java.awt.Color(46, 125, 50));
        btnFilter.setFocusPainted(false);
        btnFilter.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnFilter.setForeground(new java.awt.Color(255, 255, 255));
        btnFilter.setText("Filter");
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addComponent(lblSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(lblCategory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnFilter))
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(lblSearch)
            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(lblCategory)
            .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(btnFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        tblProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object[][] {},
            new String[] {}
        ) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
        scrollProducts.setViewportView(tblProducts);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(scrollProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(scrollProducts, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFilter;
    private javax.swing.JComboBox<Object> cmbCategory;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JScrollPane scrollProducts;
    private javax.swing.JTable tblProducts;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
