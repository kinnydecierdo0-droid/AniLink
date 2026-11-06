package anilink.farmer;

import anilink.data.ProductDAO;
import anilink.model.Product;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Lets a farmer update a product's name, price, quantity and status in one
 * modal, from the "Update" button on Manage Products.
 *
 * <p>Hand-coded rather than a NetBeans form, same as
 * {@link anilink.admin.AddDeliveryManDialog}, so the designer cannot
 * regenerate it away.
 */
public class FarmerProductEditDialog extends JDialog {

    private static final String[] STATUSES = {"available", "out_of_stock", "inactive"};

    private final Product product;
    private JTextField txtName;
    private JTextField txtPrice;
    private javax.swing.JSpinner spinQuantity;
    private JComboBox<String> cmbStatus;
    private boolean saved;

    public FarmerProductEditDialog(JFrame frame, Product product) {
        super(frame, "Update Product", ModalityType.APPLICATION_MODAL);
        this.product = product;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UiUtil.BG);
        add(scroll, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setSize(Math.max(getWidth(), 460), getHeight());
        setMinimumSize(new Dimension(460, 360));
        setLocationRelativeTo(frame);
    }

    public boolean isSaved() {
        return saved;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        JLabel title = new JLabel("Update Product");
        title.setFont(UiUtil.bold(18));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);
        JLabel sub = new JLabel(product.getProductName());
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(new java.awt.Color(220, 237, 221));
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildForm() {
        JPanel card = UiUtil.card();
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 1;

        txtName = new JTextField(product.getProductName());
        row(card, gc, "Product Name", txtName);

        txtPrice = new JTextField(String.valueOf(product.getPrice()));
        row(card, gc, "Price (₱)", txtPrice);

        spinQuantity = new javax.swing.JSpinner(
                new javax.swing.SpinnerNumberModel(Math.max(0, product.getQuantity()), 0, Integer.MAX_VALUE, 1));
        spinQuantity.setFont(UiUtil.plain(13));
        spinQuantity.setPreferredSize(new Dimension(280, 34));
        row(card, gc, "Quantity", spinQuantity);

        cmbStatus = new JComboBox<>(STATUSES);
        cmbStatus.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list,
                    Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(UiUtil.statusBadge(String.valueOf(value)));
                return this;
            }
        });
        cmbStatus.setSelectedItem(product.getStatus());
        cmbStatus.setFont(UiUtil.plain(13));
        cmbStatus.setPreferredSize(new Dimension(280, 34));
        row(card, gc, "Status", cmbStatus);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(UiUtil.BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        wrap.add(card, BorderLayout.NORTH);
        return wrap;
    }

    private void row(JPanel card, GridBagConstraints gc, String caption, javax.swing.JComponent field) {
        JLabel label = new JLabel(caption);
        UiUtil.styleFieldLabel(label);
        gc.gridx = 0;
        card.add(label, gc);
        gc.gridy++;
        gc.gridx = 0;
        if (field instanceof javax.swing.text.JTextComponent) {
            UiUtil.styleFormField((javax.swing.text.JTextComponent) field, 280);
        }
        card.add(field, gc);
        gc.gridy++;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton cancel = new JButton("Cancel");
        UiUtil.styleTintedButton(cancel);
        cancel.setPreferredSize(new Dimension(100, 36));
        cancel.addActionListener(e -> dispose());
        footer.add(cancel);

        JButton save = new JButton("Save");
        UiUtil.styleAccent(save);
        save.setPreferredSize(new Dimension(120, 36));
        save.addActionListener(e -> onSave());
        footer.add(save);
        return footer;
    }

    private void onSave() {
        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        if (name.isEmpty()) {
            UiUtil.error(this, "Product name is required.");
            return;
        }
        double price;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            if (price <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            UiUtil.error(this, "Enter a valid price greater than 0.");
            return;
        }
        int quantity = ((Number) spinQuantity.getValue()).intValue();
        String status = String.valueOf(cmbStatus.getSelectedItem());

        product.setProductName(name);
        product.setPrice(price);
        product.setQuantity(quantity);
        product.setStatus(status);
        try {
            if (new ProductDAO().update(product)) {
                saved = true;
                UiUtil.info(this, "Product updated successfully!");
                dispose();
            } else {
                UiUtil.error(this, "Product could not be updated.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }
}
