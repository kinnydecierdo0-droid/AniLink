package anilink.admin;

import anilink.data.AppealDAO;
import anilink.data.ReportDAO;
import anilink.data.UserDAO;
import anilink.data.WarningDAO;
import anilink.model.Appeal;
import anilink.model.Report;
import anilink.model.User;
import anilink.session.SessionContext;
import anilink.shell.MainFrame;
import anilink.ui.ButtonColumn;
import anilink.ui.TablePager;
import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * The admin's one-stop page for reports and account moderation: filed
 * reports (any reporter role against any target role - replaces the old
 * standalone "Buyer Reports" page), the farmer-account directory, the
 * buyer-account directory, and pending appeals from disabled accounts.
 *
 * <p>Hand-coded rather than a NetBeans form because the designer cannot
 * usefully lay out these tabs' dynamically-built content.
 */
public class UserReportPanel extends JPanel {

    private final MainFrame frame;
    private final ReportsTab reports = new ReportsTab();
    private final RoleTab farmers = new RoleTab("farmer", "Farmer");
    private final RoleTab buyers = new RoleTab("buyer", "Buyer");
    private final AppealsTab appeals = new AppealsTab();

    public UserReportPanel(MainFrame frame) {
        this.frame = frame;
        setBackground(UiUtil.BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UiUtil.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Reports", reports.build());
        tabs.addTab("Farmers", farmers.build());
        tabs.addTab("Buyers", buyers.build());
        tabs.addTab("Appeals", appeals.build());
        body.add(tabs, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiUtil.ACCENT);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiUtil.ACCENT_DARK),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("User Report");
        title.setFont(UiUtil.bold(22));
        title.setForeground(UiUtil.ON_ACCENT);
        titles.add(title);

        JLabel subtitle = new JLabel("Review reports, manage farmer accounts, and handle appeals");
        subtitle.setFont(UiUtil.plain(12));
        subtitle.setForeground(new java.awt.Color(99, 76, 10));
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    public void refreshData() {
        reports.refresh();
        farmers.refresh();
        buyers.refresh();
        appeals.refresh();
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "-";
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    /** Reports tab: every filed report, any reporter/target role, linking to {@link ReportDetailsDialog}. */
    private final class ReportsTab {

        private final List<Report> visible = new ArrayList<>();
        private final JTable table = new JTable();
        private final JComboBox<String> cmbFilter = new JComboBox<>();
        private final JTextField txtSearch = new JTextField();
        private TablePager pager;

        JPanel build() {
            JPanel panel = new JPanel(new BorderLayout(0, 12));
            panel.setBackground(UiUtil.BG);
            panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            panel.add(buildFilterBar(), BorderLayout.NORTH);

            table.setModel(new DefaultTableModel(new Object[0][0],
                    new Object[]{"Order #", "Reported By", "Reported", "Reason", "Date", "Status"}) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
            table.setDefaultEditor(Object.class, null);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            UiUtil.styleTable(table);
            int[] widths = {80, 160, 200, 150, 140, 100};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, 0));
            scroll.getViewport().setBackground(UiUtil.WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            panel.add(scroll, BorderLayout.CENTER);

            pager = TablePager.install(table, true);

            JPanel trailing = pager.dateFilterTrailing();
            if (trailing != null) {
                JButton btnView = new JButton("View Details");
                UiUtil.stylePrimary(btnView);
                btnView.setPreferredSize(new Dimension(140, 32));
                btnView.addActionListener(e -> onViewDetails());
                trailing.add(btnView);
            }

            return panel;
        }

        private JPanel buildFilterBar() {
            JPanel card = UiUtil.card();
            card.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

            JLabel statusCaption = new JLabel("Filter by status:");
            UiUtil.styleFieldLabel(statusCaption);
            card.add(statusCaption);

            cmbFilter.removeAllItems();
            cmbFilter.addItem("All");
            cmbFilter.addItem("open");
            cmbFilter.addItem("reviewed");
            cmbFilter.addItem("resolved");
            card.add(cmbFilter);
            // Attached only after the model is populated, so building the
            // combo's items doesn't itself trigger a refresh.
            cmbFilter.addActionListener(e -> refresh());

            JButton btnRefresh = new JButton("Refresh");
            UiUtil.styleButton(btnRefresh);
            btnRefresh.addActionListener(e -> refresh());
            card.add(btnRefresh);

            JLabel searchCaption = new JLabel("Search reported farmer:");
            searchCaption.setToolTipText("Only matches when the reported account is a farmer.");
            UiUtil.styleFieldLabel(searchCaption);
            card.add(searchCaption);

            UiUtil.styleSearchField(txtSearch);
            txtSearch.setPreferredSize(new Dimension(200, 32));
            txtSearch.setToolTipText("Only matches when the reported account is a farmer.");
            txtSearch.addActionListener(e -> refresh());
            card.add(txtSearch);

            JButton btnSearch = new JButton("Search");
            UiUtil.stylePrimary(btnSearch);
            btnSearch.addActionListener(e -> refresh());
            card.add(btnSearch);

            JButton btnClear = new JButton("Clear");
            UiUtil.styleTintedButton(btnClear);
            btnClear.addActionListener(e -> {
                txtSearch.setText("");
                refresh();
            });
            card.add(btnClear);

            return card;
        }

        private Report reportAt(int modelRow) {
            if (pager == null) {
                return null;
            }
            int sourceIndex = pager.sourceIndexOf(modelRow);
            return sourceIndex < 0 || sourceIndex >= visible.size() ? null : visible.get(sourceIndex);
        }

        void refresh() {
            if (pager == null) {
                return;
            }
            pager.clear();
            visible.clear();
            try {
                Object filter = cmbFilter.getSelectedItem();
                String statusFilter = filter == null || "All".equals(filter) ? null : String.valueOf(filter);
                for (Report r : new ReportDAO().findAll(statusFilter, txtSearch.getText())) {
                    visible.add(r);
                    pager.addRow(new Object[]{
                        "#" + r.getOrderId(),
                        r.getReporterName(),
                        capitalize(r.getTargetType()) + ": " + r.getTargetName(),
                        ReportDAO.reasonLabel(r.getReason()),
                        UiUtil.formatDateTime(r.getCreatedAt()),
                        ReportDAO.statusLabel(r.getStatus())
                    }, r.getCreatedAt(), r.getId());
                }
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
            pager.apply();
        }

        private void onViewDetails() {
            Report report = reportAt(table.getSelectedRow());
            if (report == null) {
                UiUtil.info(UserReportPanel.this, "Please select a report first.");
                return;
            }
            ReportDetailsDialog dialog = new ReportDetailsDialog(frame, report);
            dialog.setVisible(true);
            if (dialog.isChanged()) {
                refresh();
                // An account action here may have touched a farmer or a
                // buyer, so keep both directory tabs in step too.
                farmers.refresh();
                buyers.refresh();
            }
        }
    }

    /** One tab: search field, paginated user table, Disable/Enable/Warn columns. */
    private final class RoleTab {

        private static final int COL_ACTION = 4;
        private static final int COL_WARN = 5;

        private final String role;
        private final String label;
        private final List<User> users = new ArrayList<>();
        private final JTable table = new JTable();
        private final JTextField txtSearch = new JTextField();
        private final JLabel lblCount = new JLabel();
        private TablePager pager;

        RoleTab(String role, String label) {
            this.role = role;
            this.label = label;
        }

        JPanel build() {
            JPanel panel = new JPanel(new BorderLayout(0, 12));
            panel.setBackground(UiUtil.BG);
            panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            panel.add(buildSearchBar(), BorderLayout.NORTH);

            table.setModel(new DefaultTableModel(new Object[0][0],
                    new Object[]{"ID", "Name", "Email", "Status", "Action", "Warn"}) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == COL_ACTION || column == COL_WARN;
                }
            });
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            UiUtil.styleTable(table);
            int[] widths = {60, 200, 220, 100, 100, 90};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
            UiUtil.alignColumn(table, 0, SwingConstants.RIGHT); // ID
            UiUtil.alignColumn(table, 3, SwingConstants.CENTER); // Status

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, 0));
            scroll.getViewport().setBackground(UiUtil.WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            panel.add(scroll, BorderLayout.CENTER);

            pager = TablePager.install(table, false);

            // Label and colour follow the account, so the button flips to
            // "Enable" the moment a user is disabled.
            ButtonColumn.install(table, COL_ACTION, ButtonColumn.of(
                    row -> {
                        User u = userAt(row);
                        return u == null ? "-" : (u.isActive() ? "Disable" : "Enable");
                    },
                    row -> userAt(row) != null,
                    row -> {
                        User u = userAt(row);
                        return u != null && u.isActive() ? UiUtil.DANGER : UiUtil.PRIMARY;
                    },
                    row -> null),
                    row -> toggleStatus(userAt(row)));

            ButtonColumn.install(table, COL_WARN, ButtonColumn.of(
                    row -> "Warn",
                    row -> userAt(row) != null,
                    row -> UiUtil.ACCENT_DARK,
                    row -> null),
                    row -> warnUser(userAt(row)));

            return panel;
        }

        private JPanel buildSearchBar() {
            JPanel card = UiUtil.card();
            card.setLayout(new BorderLayout(10, 0));

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);

            JLabel caption = new JLabel("Search");
            UiUtil.styleFieldLabel(caption);
            left.add(caption);

            UiUtil.styleSearchField(txtSearch);
            txtSearch.setPreferredSize(new Dimension(280, 32));
            txtSearch.setToolTipText("Search " + label.toLowerCase() + "s by name or email");
            txtSearch.addActionListener(e -> refresh());
            left.add(txtSearch);

            JButton btnSearch = new JButton("Search");
            UiUtil.stylePrimary(btnSearch);
            btnSearch.setPreferredSize(new Dimension(100, 32));
            btnSearch.addActionListener(e -> refresh());
            left.add(btnSearch);

            JButton btnClear = new JButton("Clear");
            UiUtil.styleTintedButton(btnClear);
            btnClear.setPreferredSize(new Dimension(90, 32));
            btnClear.addActionListener(e -> {
                txtSearch.setText("");
                refresh();
            });
            left.add(btnClear);

            card.add(left, BorderLayout.WEST);

            UiUtil.styleNote(lblCount);
            card.add(lblCount, BorderLayout.EAST);
            return card;
        }

        /**
         * Maps a row on the current page back to its user. Without the pager's
         * source index this would act on the wrong account from page 2 on.
         */
        private User userAt(int modelRow) {
            if (pager == null) {
                return null;
            }
            int sourceIndex = pager.sourceIndexOf(modelRow);
            if (sourceIndex < 0 || sourceIndex >= users.size()) {
                return null;
            }
            return users.get(sourceIndex);
        }

        void refresh() {
            if (pager == null) {
                return;
            }
            pager.clear();
            users.clear();
            try {
                UserDAO userDAO = new UserDAO();
                int rowNumber = 1;
                for (User user : userDAO.searchByRole(role, txtSearch.getText(), null)) {
                    users.add(user);
                    pager.addRow(new Object[]{
                        rowNumber++,
                        user.getName(),
                        user.getEmail() == null || user.getEmail().isEmpty()
                                ? "-" : user.getEmail(),
                        user.isActive() ? "Enabled" : "Disabled",
                        "",
                        ""
                    });
                }
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
            pager.apply();
            lblCount.setText(users.size() + " " + label.toLowerCase()
                    + (users.size() == 1 ? "" : "s"));
        }

        private void toggleStatus(User user) {
            if (user == null) {
                UiUtil.info(UserReportPanel.this, "Select a " + label.toLowerCase() + " first.");
                return;
            }
            if (!"admin".equals(SessionContext.getRole())) {
                UiUtil.error(UserReportPanel.this,
                        "Only administrators can change account status.");
                return;
            }
            User current = SessionContext.getCurrentUser();
            if (current != null && current.getId() == user.getId() && user.isActive()) {
                UiUtil.error(UserReportPanel.this, "You cannot disable your own account.");
                return;
            }
            boolean enabling = !user.isActive();
            String reason = null;
            if (!enabling) {
                reason = UiUtil.promptText(UserReportPanel.this,
                        "Disable Account", "Reason for disabling this account:");
                if (reason == null) {
                    return;
                }
            }
            String message = enabling
                    ? "Enable this account? They will be able to log in again."
                    : "Disable this account? They will no longer be able to log in "
                      + "and will be signed out.";
            if (!UiUtil.confirm(UserReportPanel.this, message)) {
                return;
            }
            try {
                new UserDAO().setActive(user.getId(), enabling, reason);
                UiUtil.info(UserReportPanel.this, enabling
                        ? label + " account enabled successfully!"
                        : label + " account disabled.");
                refresh();
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
        }

        private void warnUser(User user) {
            if (user == null) {
                UiUtil.info(UserReportPanel.this, "Select a " + label.toLowerCase() + " first.");
                return;
            }
            String reason = UiUtil.promptText(UserReportPanel.this, "Warn Account", "Reason for this warning:");
            if (reason == null) {
                return;
            }
            try {
                new WarningDAO().insert(user.getId(), reason);
                UiUtil.info(UserReportPanel.this, "Warning recorded. The user will see it next time they log in.");
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
        }
    }

    /** Appeals tab: pending appeals from disabled accounts, Approve/Deny. */
    private final class AppealsTab {

        private static final int COL_APPROVE = 4;
        private static final int COL_DENY = 5;

        private final List<Appeal> visible = new ArrayList<>();
        private final JTable table = new JTable();
        private TablePager pager;

        JPanel build() {
            JPanel panel = new JPanel(new BorderLayout(0, 12));
            panel.setBackground(UiUtil.BG);
            panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

            table.setModel(new DefaultTableModel(new Object[0][0],
                    new Object[]{"Requester", "Role", "Message", "Submitted", "Approve", "Deny"}) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == COL_APPROVE || column == COL_DENY;
                }
            });
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            UiUtil.styleTable(table);
            int[] widths = {160, 90, 260, 130, 100, 100};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(UiUtil.roundedBorder(UiUtil.BORDER, 0));
            scroll.getViewport().setBackground(UiUtil.WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            panel.add(scroll, BorderLayout.CENTER);

            pager = TablePager.install(table, false);

            ButtonColumn.install(table, COL_APPROVE, ButtonColumn.of(
                    row -> "Approve",
                    row -> appealAt(row) != null,
                    row -> UiUtil.PRIMARY,
                    row -> null),
                    row -> approve(appealAt(row)));

            ButtonColumn.install(table, COL_DENY, ButtonColumn.of(
                    row -> "Deny",
                    row -> appealAt(row) != null,
                    row -> UiUtil.DANGER,
                    row -> null),
                    row -> deny(appealAt(row)));

            return panel;
        }

        private Appeal appealAt(int modelRow) {
            if (pager == null) {
                return null;
            }
            int sourceIndex = pager.sourceIndexOf(modelRow);
            return sourceIndex < 0 || sourceIndex >= visible.size() ? null : visible.get(sourceIndex);
        }

        void refresh() {
            if (pager == null) {
                return;
            }
            pager.clear();
            visible.clear();
            try {
                for (Appeal a : new AppealDAO().findPending()) {
                    visible.add(a);
                    pager.addRow(new Object[]{
                        a.getUserName(),
                        capitalize(a.getUserRole()),
                        a.getMessage(),
                        UiUtil.formatDateTime(a.getCreatedAt()),
                        "",
                        ""
                    });
                }
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
            pager.apply();
        }

        private void approve(Appeal appeal) {
            if (appeal == null) {
                UiUtil.info(UserReportPanel.this, "Select an appeal first.");
                return;
            }
            if (!UiUtil.confirm(UserReportPanel.this, "Approve this appeal and re-enable the account?")) {
                return;
            }
            try {
                new UserDAO().setActive(appeal.getUserId(), true, null);
                new AppealDAO().updateStatus(appeal.getId(), Appeal.STATUS_APPROVED);
                UiUtil.info(UserReportPanel.this, "Appeal approved. The account is re-enabled.");
                refresh();
                farmers.refresh();
                buyers.refresh();
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
        }

        private void deny(Appeal appeal) {
            if (appeal == null) {
                UiUtil.info(UserReportPanel.this, "Select an appeal first.");
                return;
            }
            if (!UiUtil.confirm(UserReportPanel.this, "Deny this appeal? The account stays disabled.")) {
                return;
            }
            try {
                new AppealDAO().updateStatus(appeal.getId(), Appeal.STATUS_DENIED);
                UiUtil.info(UserReportPanel.this, "Appeal denied.");
                refresh();
            } catch (SQLException e) {
                UiUtil.error(UserReportPanel.this, "Database error: " + e.getMessage());
            }
        }
    }
}
