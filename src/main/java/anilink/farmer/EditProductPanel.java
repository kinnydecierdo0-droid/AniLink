package anilink.farmer;

import anilink.data.CategoryDAO;
import anilink.data.ProductDAO;
import anilink.model.Category;
import anilink.model.Product;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.Optional;

public class EditProductPanel extends javax.swing.JPanel {

    private final MainFrame frame;
    private int currentProductId = -1;
    private Integer currentCategoryId;
    private String currentImage;

    public EditProductPanel(anilink.shell.MainFrame frame) {
        this.frame = frame;
        initComponents();
        spinQuantity.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000000, 1));
        cmbUnit.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"kg", "piece", "bundle", "sack", "dozen", "liter"}));
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"available", "out_of_stock", "inactive"}));
        refreshData();
        clearFields();
        UiUtil.styleTitle(lblTitle);
        UiUtil.styleFieldLabel(lblProductInfo);
        UiUtil.styleFieldLabel(lblError);
        UiUtil.styleFieldLabel(lblNameCaption);
        UiUtil.styleFieldLabel(lblCategoryCaption);
        UiUtil.styleFieldLabel(lblDescriptionCaption);
        UiUtil.styleFieldLabel(lblPrice);
        UiUtil.styleFieldLabel(lblUnit);
        UiUtil.styleFieldLabel(lblQuantity);
        UiUtil.styleFieldLabel(lblStatus);
        UiUtil.styleFieldLabel(lblImageCaption);
        UiUtil.styleNote(lblImageName);
        UiUtil.styleStrip(pnlPriceRow);
        UiUtil.styleStrip(pnlImageRow);
        UiUtil.styleStrip(pnlButtons);
        UiUtil.styleTintedButton(btnBrowse);
        UiUtil.styleTintedButton(btnCancel);
        UiUtil.styleButton(btnSave);
    }

    public void refreshData() {
        try {
            loadCategories(currentCategoryId);
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    public void loadProduct(int productId) {
        lblError.setText(" ");
        currentProductId = -1;
        currentCategoryId = null;
        currentImage = null;
        try {
            Optional<Product> found = new ProductDAO().findById(productId);
            if (found.isEmpty()) {
                clearFields();
                lblProductInfo.setText("No product selected");
                UiUtil.error(this, "Product not found. It may have been removed.");
                return;
            }
            Product product = found.get();
            if (product.getFarmerId() != SessionContext.getFarmerId()) {
                clearFields();
                lblProductInfo.setText("No product selected");
                UiUtil.error(this, "You do not have permission to edit this product.");
                return;
            }
            currentProductId = product.getId();
            currentCategoryId = product.getCategoryId();
            currentImage = product.getImage();

            txtName.setText(product.getProductName() == null ? "" : product.getProductName());
            txtDescription.setText(product.getDescription() == null ? "" : product.getDescription());
            txtPrice.setText(String.format(java.util.Locale.getDefault(), "%.2f", product.getPrice()));
            spinQuantity.setValue(product.getQuantity());
            if (product.getUnit() != null) {
                cmbUnit.setSelectedItem(product.getUnit());
            }
            selectStatus(product.getStatus());
            selectCategory(currentCategoryId);
            lblProductInfo.setText("Editing Product #" + product.getId());
            lblImageName.setText(currentImage == null ? "No image selected" : currentImage);
            lblImagePreview.setIcon(UiUtil.loadProductIcon(currentImage, 110, 110));
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void loadCategories(Integer selectedId) throws SQLException {
        cmbCategory.removeAllItems();
        cmbCategory.addItem(new Category(0, "-- Select Category --"));
        for (Category category : new CategoryDAO().findAll()) {
            cmbCategory.addItem(category);
        }
        selectCategory(selectedId);
    }

    private void selectCategory(Integer selectedId) {
        cmbCategory.setSelectedIndex(0);
        if (selectedId == null) {
            return;
        }
        for (int i = 0; i < cmbCategory.getItemCount(); i++) {
            Category category = cmbCategory.getItemAt(i);
            if (category != null && category.getId() == selectedId) {
                cmbCategory.setSelectedIndex(i);
                return;
            }
        }
    }

    private void selectStatus(String status) {
        if (status != null) {
            cmbStatus.setSelectedItem(status);
        }
    }

    private void clearFields() {
        currentProductId = -1;
        currentCategoryId = null;
        currentImage = null;
        txtName.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        spinQuantity.setValue(0);
        cmbUnit.setSelectedItem("kg");
        cmbStatus.setSelectedItem("available");
        cmbCategory.setSelectedIndex(0);
        lblImageName.setText("No image selected");
        lblImagePreview.setIcon(UiUtil.loadProductIcon(null, 110, 110));
        lblProductInfo.setText("No product selected");
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {
        lblError.setText(" ");
        if (currentProductId < 0) {
            lblError.setText("No product selected. Please select a product to edit.");
            return;
        }
        String name = txtName.getText().trim();
        String priceText = txtPrice.getText().trim();
        int qty = ((Number) spinQuantity.getValue()).intValue();

        if (name.isEmpty()) {
            lblError.setText("Product name is required.");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            lblError.setText("Price must be a valid number greater than 0.");
            return;
        }
        if (price <= 0) {
            lblError.setText("Price must be a valid number greater than 0.");
            return;
        }
        if (qty < 0) {
            lblError.setText("Quantity must be a valid number.");
            return;
        }
        String unit = cmbUnit.getSelectedItem() == null
                ? "" : cmbUnit.getSelectedItem().toString();
        if (unit.isEmpty()) {
            lblError.setText("Unit is required.");
            return;
        }
        String status = cmbStatus.getSelectedItem() == null
                ? "available" : cmbStatus.getSelectedItem().toString();
        try {
            Product product = new Product();
            product.setId(currentProductId);
            product.setFarmerId(SessionContext.getFarmerId());
            Category category = (Category) cmbCategory.getSelectedItem();
            product.setCategoryId(category == null || category.getId() <= 0
                    ? null : category.getId());
            product.setProductName(name);
            product.setDescription(txtDescription.getText().trim());
            product.setPrice(price);
            product.setUnit(unit);
            product.setQuantity(qty);
            product.setImage(currentImage);
            product.setStatus(status);
            if (new ProductDAO().update(product)) {
                UiUtil.info(this, "Product updated successfully!");
                frame.showCard(Cards.FARMER_PRODUCTS);
            } else {
                lblError.setText("Unable to update the product. Please try again.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.FARMER_PRODUCTS);
    }

    private void btnBrowseActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentProductId < 0) {
            lblError.setText("No product selected. Please select a product to edit.");
            return;
        }
        String stored = UiUtil.chooseAndStoreImage(this);
        if (stored != null) {
            currentImage = stored;
            lblImageName.setText(stored);
            lblImagePreview.setIcon(UiUtil.loadProductIcon(stored, 110, 110));
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblProductInfo = new javax.swing.JLabel();
        lblError = new javax.swing.JLabel();
        lblNameCaption = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblCategoryCaption = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        lblDescriptionCaption = new javax.swing.JLabel();
        scrollDescription = new javax.swing.JScrollPane();
        txtDescription = new javax.swing.JTextArea();
        pnlPriceRow = new javax.swing.JPanel();
        lblPrice = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        lblUnit = new javax.swing.JLabel();
        cmbUnit = new javax.swing.JComboBox<>();
        lblQuantity = new javax.swing.JLabel();
        spinQuantity = new javax.swing.JSpinner();
        lblStatus = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        lblImageCaption = new javax.swing.JLabel();
        pnlImageRow = new javax.swing.JPanel();
        btnBrowse = new javax.swing.JButton();
        lblImageName = new javax.swing.JLabel();
        lblImagePreview = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        lblTitle.setText("Edit Product");

        lblProductInfo.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblProductInfo.setForeground(new java.awt.Color(46, 125, 50));
        lblProductInfo.setText("No product selected");

        lblError.setForeground(new java.awt.Color(211, 47, 47));
        lblError.setText(" ");

        lblNameCaption.setText("Product Name *");

        lblCategoryCaption.setText("Category *");

        lblDescriptionCaption.setText("Description");

        txtDescription.setColumns(20);
        txtDescription.setRows(3);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        scrollDescription.setViewportView(txtDescription);

        lblPrice.setText("Price (\u20B1) *");

        lblUnit.setText("Unit *");

        lblQuantity.setText("Quantity *");

        lblStatus.setText("Status *");

        javax.swing.GroupLayout pnlPriceRowLayout = new javax.swing.GroupLayout(pnlPriceRow);
        pnlPriceRow.setLayout(pnlPriceRowLayout);
        pnlPriceRowLayout.setHorizontalGroup(
            pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPriceRowLayout.createSequentialGroup()
                .addGroup(pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPrice)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblUnit)
                    .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblQuantity)
                    .addComponent(spinQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStatus)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        pnlPriceRowLayout.setVerticalGroup(
            pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPriceRowLayout.createSequentialGroup()
                .addGroup(pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPrice)
                    .addComponent(lblUnit)
                    .addComponent(lblQuantity)
                    .addComponent(lblStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlPriceRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        lblImageCaption.setText("Product Image (leave as-is to keep current image)");

        btnBrowse.setText("Replace Image...");
        btnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBrowseActionPerformed(evt);
            }
        });

        lblImageName.setForeground(new java.awt.Color(96, 125, 107));
        lblImageName.setText("No image selected");

        javax.swing.GroupLayout pnlImageRowLayout = new javax.swing.GroupLayout(pnlImageRow);
        pnlImageRow.setLayout(pnlImageRowLayout);
        pnlImageRowLayout.setHorizontalGroup(
            pnlImageRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlImageRowLayout.createSequentialGroup()
                .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblImageName, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlImageRowLayout.setVerticalGroup(
            pnlImageRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlImageRowLayout.createSequentialGroup()
                .addGroup(pnlImageRowLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBrowse, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnlImageRowLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(lblImageName, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pnlButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlButtons.add(btnCancel);

        btnSave.setBackground(new java.awt.Color(46, 125, 50));
        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("Update Product");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        pnlButtons.add(btnSave);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblProductInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblError, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblNameCaption)
                    .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblCategoryCaption)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDescriptionCaption)
                    .addComponent(scrollDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlPriceRow, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblImageCaption)
                    .addComponent(pnlImageRow, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addGap(6, 6, 6)
                .addComponent(lblProductInfo)
                .addGap(4, 4, 4)
                .addComponent(lblError)
                .addGap(12, 12, 12)
                .addComponent(lblNameCaption)
                .addGap(6, 6, 6)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblCategoryCaption)
                .addGap(6, 6, 6)
                .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblDescriptionCaption)
                .addGap(6, 6, 6)
                .addComponent(scrollDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(pnlPriceRow, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblImageCaption)
                .addGap(6, 6, 6)
                .addComponent(pnlImageRow, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnBrowse;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<Category> cmbCategory;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbUnit;
    private javax.swing.JLabel lblCategoryCaption;
    private javax.swing.JLabel lblDescriptionCaption;
    private javax.swing.JLabel lblError;
    private javax.swing.JLabel lblImageCaption;
    private javax.swing.JLabel lblImageName;
    private javax.swing.JLabel lblImagePreview;
    private javax.swing.JLabel lblNameCaption;
    private javax.swing.JLabel lblPrice;
    private javax.swing.JLabel lblProductInfo;
    private javax.swing.JLabel lblQuantity;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUnit;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlImageRow;
    private javax.swing.JPanel pnlPriceRow;
    private javax.swing.JScrollPane scrollDescription;
    private javax.swing.JSpinner spinQuantity;
    private javax.swing.JTextArea txtDescription;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPrice;
    // End of variables declaration//GEN-END:variables
}
