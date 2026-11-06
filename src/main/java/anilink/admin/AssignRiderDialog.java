package anilink.admin;

import anilink.data.OrderDAO;
import anilink.data.RiderDAO;
import anilink.model.Payment;
import anilink.model.Rider;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Picks a delivery man for an approved delivery order.
 *
 * <p>Only active delivery men are offered - {@link RiderDAO#findAllWithNames()}
 * already filters on {@code is_active = 1}, so a disabled account can never be
 * assigned new work.
 */
public class AssignRiderDialog extends JDialog {

    private final Payment payment;
    private final JComboBox<RiderItem> cmbRiders = new JComboBox<>();
    private boolean assigned;

    public AssignRiderDialog(Window owner, Payment payment) {
        super(owner, "Assign Delivery Man", ModalityType.APPLICATION_MODAL);
        this.payment = payment;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        setSize(460, 300);
        setLocationRelativeTo(owner);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Assign Delivery Man");
        title.setFont(UiUtil.bold(17));
        title.setForeground(UiUtil.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Order #" + payment.getOrderId()
                + "  -  " + blank(payment.getBuyerName()));
        sub.setFont(UiUtil.plain(12));
        sub.setForeground(new java.awt.Color(220, 237, 221));
        header.add(sub, BorderLayout.SOUTH);
        return header;
    }

    private JPanel buildBody() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiUtil.BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JPanel card = UiUtil.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel current = new JLabel("Currently assigned: " + blank(payment.getRiderName()));
        current.setFont(UiUtil.plain(12));
        current.setForeground(UiUtil.MUTED);
        current.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(current);
        card.add(UiUtil.vgap(12));

        JLabel caption = new JLabel("Delivery man");
        UiUtil.styleFieldLabel(caption);
        caption.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        card.add(caption);
        card.add(UiUtil.vgap(6));

        cmbRiders.setFont(UiUtil.plain(13));
        cmbRiders.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        cmbRiders.setMaximumSize(new Dimension(380, 34));
        cmbRiders.setPreferredSize(new Dimension(380, 34));
        loadRiders();
        card.add(cmbRiders);

        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private void loadRiders() {
        DefaultComboBoxModel<RiderItem> model = new DefaultComboBoxModel<>();
        try {
            List<Rider> riders = new RiderDAO().findAllWithNames();
            if (riders.isEmpty()) {
                model.addElement(new RiderItem(null, "No active delivery men"));
            } else {
                for (Rider rider : riders) {
                    model.addElement(new RiderItem(rider.getId(),
                            rider.getUserName() == null
                                    ? "Rider #" + rider.getId() : rider.getUserName()));
                }
            }
        } catch (SQLException e) {
            model.addElement(new RiderItem(null, "Failed to load delivery men"));
            UiUtil.error(this, "Failed to load delivery men: " + e.getMessage());
        }
        cmbRiders.setModel(model);

        // Preselect whoever is already on the order.
        if (payment.getRiderId() != null) {
            for (int i = 0; i < model.getSize(); i++) {
                Integer id = model.getElementAt(i).id;
                if (id != null && id.equals(payment.getRiderId())) {
                    cmbRiders.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(UiUtil.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiUtil.BORDER));

        JButton cancel = new JButton("Cancel");
        UiUtil.styleTintedButton(cancel);
        cancel.setPreferredSize(new Dimension(110, 36));
        cancel.addActionListener(e -> dispose());
        footer.add(cancel);

        JButton assign = new JButton("Assign");
        UiUtil.stylePrimary(assign);
        assign.setPreferredSize(new Dimension(130, 36));
        assign.addActionListener(e -> assign());
        footer.add(assign);
        return footer;
    }

    private void assign() {
        RiderItem selected = (RiderItem) cmbRiders.getSelectedItem();
        if (selected == null || selected.id == null) {
            UiUtil.error(this, "There is no delivery man available to assign. "
                    + "Add one from the Add Delivery Man page first.");
            return;
        }
        if (!UiUtil.confirm(this, "Assign " + selected.name
                + " to order #" + payment.getOrderId() + "?")) {
            return;
        }
        try {
            if (new OrderDAO().assignRider(payment.getOrderId(), selected.id)) {
                payment.setRiderId(selected.id);
                payment.setRiderName(selected.name);
                assigned = true;
                UiUtil.info(this, selected.name + " is now assigned to order #"
                        + payment.getOrderId() + ".");
                dispose();
            } else {
                UiUtil.error(this, "Could not assign that delivery man.");
            }
        } catch (SQLException e) {
            UiUtil.error(this, "Database error: " + e.getMessage());
        }
    }

    private static String blank(String value) {
        return value == null || value.trim().isEmpty() ? "nobody yet" : value;
    }

    /** True when a delivery man was assigned, so the caller can refresh. */
    public boolean isAssigned() {
        return assigned;
    }

    /** Combo entry carrying the rider id behind a readable name. */
    private static final class RiderItem {

        private final Integer id;
        private final String name;

        RiderItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
