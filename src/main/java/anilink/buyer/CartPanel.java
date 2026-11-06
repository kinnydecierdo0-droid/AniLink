package anilink.buyer;

import anilink.data.ProductDAO;
import anilink.model.Product;
import anilink.session.CartItem;
import anilink.session.SessionContext;
import anilink.shell.Cards;
import anilink.shell.MainFrame;
import anilink.shop.ProductDetailsDialog;
import anilink.util.UiUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class CartPanel extends javax.swing.JPanel {

    private static final int COL_ACTION = 5;

    private final MainFrame frame;

    public CartPanel(MainFrame frame) {
        this.frame = frame;
        initComponents();
        tblCart.setModel(new DefaultTableModel(new Object[0][0],
                new Object[]{"Product", "Unit", "Price", "Qty", "Subtotal", "Action"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex == COL_ACTION;
            }
        });
        tblCart.setDefaultEditor(Object.class, null);
        tblCart.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    syncSpinnerToSelection();
                }
            }
        });
        tblCart.setPreferredScrollableViewportSize(new java.awt.Dimension(760, 340));
        int[] widths = {230, 70, 110, 60, 120, 110};
        for (int i = 0; i < widths.length && i < tblCart.getColumnModel().getColumnCount(); i++) {
            tblCart.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        if (tblCart.getParent() instanceof javax.swing.JViewport) {
            tblCart.getParent().setBackground(new java.awt.Color(248, 250, 248));
        }
        javax.swing.table.TableColumn actionCol = tblCart.getColumnModel().getColumn(COL_ACTION);
        actionCol.setCellRenderer(new QtyActionRenderer());
        actionCol.setCellEditor(new QtyActionEditor());
        UiUtil.styleBanner(lblTitle, "Favorites");
        UiUtil.styleSection(lblGrandCaption);
        UiUtil.styleSection(lblGrandValue);
        UiUtil.styleFieldLabel(lblSubtotalCaption);
        UiUtil.styleFieldLabel(lblSubtotalValue);
        UiUtil.styleFieldLabel(lblQtyCaption);
        UiUtil.styleStrip(pnlQtyRow);
        UiUtil.styleStrip(pnlTotals);
        UiUtil.styleStrip(pnlButtons);
        UiUtil.styleTintedButton(btnUpdateQty);
        UiUtil.styleButton(btnBuy);
        UiUtil.styleTintedButton(btnViewDetails);
        UiUtil.styleTintedButton(btnRemove);
        UiUtil.styleTintedButton(btnContinueShopping);
        UiUtil.styleButton(btnCheckout);
        UiUtil.styleTable(tblCart);
    }

    public void refreshData() {
        Integer selectedId = getSelectedProductId();
        for (CartItem item : new ArrayList<>(SessionContext.getCart())) {
            try {
                Optional<Product> fresh = new ProductDAO().findById(item.getProduct().getId());
                if (fresh.isPresent()) {
                    item.setProduct(fresh.get());
                } else if (UiUtil.confirm(this, "\u201C" + item.getProduct().getProductName()
                        + "\u201D is no longer available. Remove it from your favorites?")) {
                    SessionContext.removeFromCart(item.getProduct().getId());
                }
            } catch (SQLException e) {
                System.err.println("[cart] refresh failed: " + e.getMessage());
            }
        }
        DefaultTableModel model = (DefaultTableModel) tblCart.getModel();
        model.setRowCount(0);
        for (CartItem item : SessionContext.getCart()) {
            Product p = item.getProduct();
            model.addRow(new Object[]{
                p.getProductName(),
                p.getUnit(),
                UiUtil.formatPrice(p.getPrice()),
                item.getQuantity(),
                UiUtil.formatPrice(item.getSubtotal()),
                ""
            });
        }
        double total = SessionContext.cartTotal();
        lblSubtotalValue.setText(UiUtil.formatPrice(total));
        lblGrandValue.setText(UiUtil.formatPrice(total));
        btnCheckout.setEnabled(!SessionContext.getCart().isEmpty());
        reselectProductId(selectedId);
        syncSpinnerToSelection();
        frame.refreshChrome();
    }

    private CartItem itemAt(int viewRow) {
        if (viewRow < 0) {
            return null;
        }
        int modelRow = tblCart.convertRowIndexToModel(viewRow);
        List<CartItem> cart = SessionContext.getCart();
        if (modelRow < 0 || modelRow >= cart.size()) {
            return null;
        }
        return cart.get(modelRow);
    }

    private Integer getSelectedProductId() {
        CartItem item = itemAt(tblCart.getSelectedRow());
        return item == null ? null : item.getProduct().getId();
    }

    private void reselectProductId(Integer productId) {
        if (productId == null) {
            tblCart.clearSelection();
            return;
        }
        List<CartItem> cart = SessionContext.getCart();
        for (int modelRow = 0; modelRow < cart.size(); modelRow++) {
            if (cart.get(modelRow).getProduct().getId() == productId) {
                int viewRow = tblCart.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    tblCart.setRowSelectionInterval(viewRow, viewRow);
                }
                return;
            }
        }
    }

    private void syncSpinnerToSelection() {
        CartItem item = itemAt(tblCart.getSelectedRow());
        if (item == null) {
            cartQty.setEnabled(false);
            return;
        }
        cartQty.setEnabled(true);
        int stock = item.getProduct().getQuantity();
        int max = Math.max(stock, item.getQuantity());
        int value = Math.min(Math.max(item.getQuantity(), 1), max);
        cartQty.setModel(new SpinnerNumberModel(value, 1, max, 1));
    }

    private void applySpinnerQty() {
        CartItem item = itemAt(tblCart.getSelectedRow());
        if (item == null) {
            UiUtil.info(this, "Please select a favorite item first.");
            return;
        }
        Product p = item.getProduct();
        if (p.getQuantity() < 1) {
            UiUtil.error(this, p.getProductName() + " is out of stock.");
            return;
        }
        int qty = ((Number) cartQty.getValue()).intValue();
        if (qty < 1) {
            qty = 1;
            cartQty.setValue(1);
        }
        if (qty > p.getQuantity()) {
            UiUtil.error(this, "Only " + p.getQuantity() + " " + p.getUnit()
                    + " of " + p.getProductName() + " available.");
            qty = p.getQuantity();
            cartQty.setValue(qty);
        }
        SessionContext.setCartQuantity(p.getId(), qty);
        refreshData();
    }

    private void changeQty(CartItem item, int delta) {
        if (item == null) {
            return;
        }
        Product p = item.getProduct();
        int next = item.getQuantity() + delta;
        if (next < 1) {
            return;
        }
        if (p.getQuantity() < 1) {
            UiUtil.info(this, p.getProductName() + " is out of stock.");
            return;
        }
        if (next > p.getQuantity()) {
            UiUtil.info(this, "Only " + p.getQuantity() + " " + p.getUnit() + " available.");
            return;
        }
        SessionContext.setCartQuantity(p.getId(), next);
        refreshData();
    }

    private void btnUpdateQtyActionPerformed(java.awt.event.ActionEvent evt) {
        applySpinnerQty();
    }

    private void btnViewDetailsActionPerformed(java.awt.event.ActionEvent evt) {
        CartItem item = itemAt(tblCart.getSelectedRow());
        if (item == null) {
            UiUtil.info(this, "Please select a favorite item first.");
            return;
        }
        new ProductDetailsDialog(frame, item.getProduct().getId()).setVisible(true);
        refreshData();
    }

    private void btnBuyActionPerformed(java.awt.event.ActionEvent evt) {
        CartItem item = itemAt(tblCart.getSelectedRow());
        if (item == null) {
            UiUtil.info(this, "Please select a favorite item first.");
            return;
        }
        if (!SessionContext.isLoggedIn() || !SessionContext.isRole("buyer")) {
            UiUtil.error(this, "Please log in as a buyer to place an order.");
            return;
        }
        if (SessionContext.getBuyerId() <= 0) {
            UiUtil.error(this, "Buyer profile not found. Please re-login.");
            return;
        }
        new BuyNowDialog(frame, item.getProduct()).setVisible(true);
        refreshData();
    }

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {
        CartItem item = itemAt(tblCart.getSelectedRow());
        if (item == null) {
            UiUtil.info(this, "Please select a favorite item to remove.");
            return;
        }
        if (!UiUtil.confirm(this, "Remove this item from your favorites?")) {
            return;
        }
        SessionContext.removeFromCart(item.getProduct().getId());
        refreshData();
    }

    private void btnCheckoutActionPerformed(java.awt.event.ActionEvent evt) {
        if (SessionContext.getCart().isEmpty()) {
            UiUtil.info(this, "Your favorites list is empty.");
            return;
        }
        if (!SessionContext.isLoggedIn() || !SessionContext.isRole("buyer")) {
            UiUtil.error(this, "Please log in as a buyer to checkout.");
            return;
        }
        if (SessionContext.getBuyerId() <= 0) {
            UiUtil.error(this, "Buyer profile not found. Please re-login.");
            return;
        }
        frame.openCheckout();
    }

    private void btnContinueShoppingActionPerformed(java.awt.event.ActionEvent evt) {
        frame.showCard(Cards.MARKETPLACE);
    }

    private class QtyActionPanel extends javax.swing.JPanel {

        final javax.swing.JButton btnMinus = new javax.swing.JButton("\u2212");
        final javax.swing.JButton btnPlus = new javax.swing.JButton("+");

        QtyActionPanel() {
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 4, 2));
            setOpaque(true);
            setBackground(java.awt.Color.WHITE);
            javax.swing.JButton[] buttons = {btnMinus, btnPlus};
            for (javax.swing.JButton b : buttons) {
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setOpaque(true);
                b.setPreferredSize(new java.awt.Dimension(34, 22));
                b.setFont(new java.awt.Font("Segoe UI", 1, 13));
                b.setBackground(new java.awt.Color(232, 245, 233));
                b.setForeground(new java.awt.Color(27, 94, 32));
                add(b);
            }
        }
    }

    private final class QtyActionRenderer extends QtyActionPanel
            implements javax.swing.table.TableCellRenderer {

        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            CartItem item = itemAt(row);
            boolean has = item != null;
            btnMinus.setEnabled(has && item.getQuantity() > 1);
            btnPlus.setEnabled(has && item.getProduct().getQuantity() > item.getQuantity());
            setBackground(isSelected ? new java.awt.Color(200, 230, 201) : java.awt.Color.WHITE);
            return this;
        }
    }

    private final class QtyActionEditor extends javax.swing.AbstractCellEditor
            implements javax.swing.table.TableCellEditor {

        private final QtyActionPanel panel = new QtyActionPanel();
        private CartItem target;

        QtyActionEditor() {
            panel.btnMinus.addActionListener(evt -> {
                fireEditingStopped();
                changeQty(target, -1);
            });
            panel.btnPlus.addActionListener(evt -> {
                fireEditingStopped();
                changeQty(target, +1);
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table,
                Object value, boolean isSelected, int row, int column) {
            target = itemAt(row);
            boolean has = target != null;
            panel.btnMinus.setEnabled(has && target.getQuantity() > 1);
            panel.btnPlus.setEnabled(has && target.getProduct().getQuantity() > target.getQuantity());
            panel.setBackground(isSelected ? new java.awt.Color(200, 230, 201) : java.awt.Color.WHITE);
            return panel;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        scrollCart = new javax.swing.JScrollPane();
        tblCart = new javax.swing.JTable();
        pnlQtyRow = new javax.swing.JPanel();
        lblQtyCaption = new javax.swing.JLabel();
        cartQty = new javax.swing.JSpinner();
        btnUpdateQty = new javax.swing.JButton();
        pnlTotals = new javax.swing.JPanel();
        lblSubtotalCaption = new javax.swing.JLabel();
        lblSubtotalValue = new javax.swing.JLabel();
        lblGrandCaption = new javax.swing.JLabel();
        lblGrandValue = new javax.swing.JLabel();
        pnlButtons = new javax.swing.JPanel();
        btnBuy = new javax.swing.JButton();
        btnViewDetails = new javax.swing.JButton();
        btnRemove = new javax.swing.JButton();
        btnContinueShopping = new javax.swing.JButton();
        btnCheckout = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 250, 248));

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new java.awt.Color(255, 193, 7));
        lblTitle.setForeground(new java.awt.Color(51, 38, 0));
        lblTitle.setText("Favorites");

        tblCart.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new Object [] {}
        ));
        scrollCart.setViewportView(tblCart);

        lblQtyCaption.setText("Quantity for selected item:");

        cartQty.setModel(new javax.swing.SpinnerNumberModel(1, 1, 1, 1));
        cartQty.setEnabled(false);

        btnUpdateQty.setText("Update Qty");
        btnUpdateQty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateQtyActionPerformed(evt);
            }
        });
        pnlQtyRow.add(lblQtyCaption);
        pnlQtyRow.add(cartQty);
        pnlQtyRow.add(btnUpdateQty);

        lblSubtotalCaption.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSubtotalCaption.setText("Subtotal:");

        lblSubtotalValue.setFont(new java.awt.Font("Segoe UI", 1, 12));
        lblSubtotalValue.setText("\u20B10.00");

        lblGrandCaption.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblGrandCaption.setForeground(new java.awt.Color(46, 125, 50));
        lblGrandCaption.setText("Grand Total:");

        lblGrandValue.setFont(new java.awt.Font("Segoe UI", 1, 14));
        lblGrandValue.setForeground(new java.awt.Color(46, 125, 50));
        lblGrandValue.setText("\u20B10.00");

        javax.swing.GroupLayout pnlTotalsLayout = new javax.swing.GroupLayout(pnlTotals);
        pnlTotals.setLayout(pnlTotalsLayout);
        pnlTotalsLayout.setHorizontalGroup(
            pnlTotalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblSubtotalCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSubtotalValue, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(lblGrandCaption)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblGrandValue, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnlTotalsLayout.setVerticalGroup(
            pnlTotalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTotalsLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(pnlTotalsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSubtotalCaption)
                    .addComponent(lblSubtotalValue)
                    .addComponent(lblGrandCaption)
                    .addComponent(lblGrandValue)))
        );

        btnBuy.setText("Buy");
        btnBuy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuyActionPerformed(evt);
            }
        });
        pnlButtons.add(btnBuy);

        btnViewDetails.setText("View Details");
        btnViewDetails.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewDetailsActionPerformed(evt);
            }
        });
        pnlButtons.add(btnViewDetails);

        btnRemove.setForeground(new java.awt.Color(211, 47, 47));
        btnRemove.setText("Remove Item");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });
        pnlButtons.add(btnRemove);

        btnContinueShopping.setText("Continue Shopping");
        btnContinueShopping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContinueShoppingActionPerformed(evt);
            }
        });
        pnlButtons.add(btnContinueShopping);

        btnCheckout.setBackground(new java.awt.Color(46, 125, 50));
        btnCheckout.setFont(new java.awt.Font("Segoe UI", 1, 12));
        btnCheckout.setForeground(new java.awt.Color(255, 255, 255));
        btnCheckout.setText("Proceed to Checkout");
        btnCheckout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckoutActionPerformed(evt);
            }
        });
        pnlButtons.add(btnCheckout);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollCart, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(pnlQtyRow, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTotals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlButtons, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scrollCart, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlQtyRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlTotals, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlButtons, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuy;
    private javax.swing.JButton btnCheckout;
    private javax.swing.JButton btnContinueShopping;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnUpdateQty;
    private javax.swing.JButton btnViewDetails;
    private javax.swing.JSpinner cartQty;
    private javax.swing.JLabel lblGrandCaption;
    private javax.swing.JLabel lblGrandValue;
    private javax.swing.JLabel lblQtyCaption;
    private javax.swing.JLabel lblSubtotalCaption;
    private javax.swing.JLabel lblSubtotalValue;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JPanel pnlButtons;
    private javax.swing.JPanel pnlQtyRow;
    private javax.swing.JPanel pnlTotals;
    private javax.swing.JScrollPane scrollCart;
    private javax.swing.JTable tblCart;
    // End of variables declaration//GEN-END:variables
}
