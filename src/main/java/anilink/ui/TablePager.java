package anilink.ui;

import anilink.util.UiUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

/**
 * Adds fixed-size pagination and an optional date-range filter to an existing
 * {@link JTable} without rebuilding its panel's layout.
 *
 * <p>Panels keep their generated {@code .form} layout untouched: on install the
 * table's {@link JScrollPane} is swapped for a wrapper holding the filter bar,
 * the scroll pane and the pager bar, using {@link GroupLayout#replace} so the
 * original layout position and constraints are preserved.
 *
 * <p>Rows are handed to the pager instead of straight to the table model; it
 * keeps the full list and pushes only the current page into the model. Where a
 * row has a timestamp, pass it to {@link #addRow(Object[], Date)} so the date
 * filter can work on the real value rather than parsing the formatted cell.
 *
 * <pre>
 * pager.clear();
 * for (Order o : orders) {
 *     pager.addRow(new Object[]{...}, o.getCreatedAt());
 * }
 * pager.apply();
 * </pre>
 */
public final class TablePager {

    /** Rows shown per page. */
    public static final int PAGE_SIZE = 10;

    private static final String ALL = "All dates";
    private static final String TODAY = "Today";
    private static final String DAYS_7 = "Last 7 days";
    private static final String DAYS_30 = "Last 30 days";
    private static final String THIS_MONTH = "This month";
    private static final String CUSTOM = "Custom range";

    private static final class Row {

        final Object[] cells;
        final Date when;
        /** Position this row was added in, i.e. its index in the panel's list. */
        final int sourceIndex;
        /**
         * Optional identity separate from what's displayed - e.g. a record's
         * real database id, for callers whose cells show a friendly number
         * instead. Null unless the caller passed one to {@link #addRow}.
         */
        final Object key;

        Row(Object[] cells, Date when, int sourceIndex, Object key) {
            this.cells = cells;
            this.when = when;
            this.sourceIndex = sourceIndex;
            this.key = key;
        }
    }

    private final JTable table;
    private final DefaultTableModel model;
    private final List<Row> all = new ArrayList<>();
    private final List<Row> filtered = new ArrayList<>();

    private final JPanel wrapper = new JPanel(new BorderLayout(0, 8));
    private final JPanel filterBar = new JPanel(new BorderLayout());
    private final JPanel dateControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JPanel trailing = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
    private final JPanel pagerBar = new JPanel(new BorderLayout());
    private final JLabel lblShowing = new JLabel();
    private final JLabel lblPage = new JLabel();
    private final JButton btnPrev = new JButton("‹ Prev");
    private final JButton btnNext = new JButton("Next ›");

    private JComboBox<String> cmbPreset;
    private JSpinner spinFrom;
    private JSpinner spinTo;
    private JLabel lblFrom;
    private JLabel lblTo;

    private final boolean hasDates;
    private int page;
    /** Guards against the spinners firing while the preset is being set. */
    private boolean updatingFilter;

    private TablePager(JTable table, boolean hasDates) {
        this.table = table;
        this.model = (DefaultTableModel) table.getModel();
        this.hasDates = hasDates;
    }

    /**
     * Installs pagination on {@code table}. Pass {@code true} for
     * {@code withDateFilter} only when rows will carry a timestamp.
     */
    public static TablePager install(JTable table, boolean withDateFilter) {
        TablePager pager = new TablePager(table, withDateFilter);
        pager.build();
        return pager;
    }

    private void build() {
        if (hasDates) {
            buildFilterBar();
        }
        buildPagerBar();

        wrapper.setOpaque(false);
        JScrollPane scroll = findScrollPane();
        if (scroll == null) {
            return;
        }
        if (scroll.getParent() == null) {
            // Installed before the scroll pane was added to its panel; wire up
            // as soon as it gets a parent so the bars are never silently lost.
            scroll.addHierarchyListener(new java.awt.event.HierarchyListener() {
                @Override
                public void hierarchyChanged(java.awt.event.HierarchyEvent e) {
                    if (scroll.getParent() != null && wrapper.getParent() == null) {
                        scroll.removeHierarchyListener(this);
                        attach(scroll);
                    }
                }
            });
            return;
        }
        attach(scroll);
    }

    private void attach(JScrollPane scroll) {
        Container parent = scroll.getParent();
        if (parent == null) {
            return;
        }
        // Take the scroll pane's place, then adopt it as our centre.
        if (parent.getLayout() instanceof GroupLayout) {
            ((GroupLayout) parent.getLayout()).replace(scroll, wrapper);
        } else {
            int index = indexOf(parent, scroll);
            Object constraints = constraintsOf(parent, scroll);
            parent.remove(scroll);
            if (constraints != null) {
                parent.add(wrapper, constraints, index);
            } else {
                parent.add(wrapper, index);
            }
        }
        if (hasDates) {
            wrapper.add(filterBar, BorderLayout.NORTH);
        }
        wrapper.add(scroll, BorderLayout.CENTER);
        wrapper.add(pagerBar, BorderLayout.SOUTH);
    }

    private void buildFilterBar() {
        filterBar.setOpaque(false);
        dateControls.setOpaque(false);
        trailing.setOpaque(false);
        filterBar.add(dateControls, BorderLayout.WEST);
        filterBar.add(trailing, BorderLayout.EAST);

        JLabel caption = new JLabel("Date:");
        UiUtil.styleFieldLabel(caption);
        dateControls.add(caption);

        cmbPreset = new JComboBox<>(new String[]{
            ALL, TODAY, DAYS_7, DAYS_30, THIS_MONTH, CUSTOM});
        cmbPreset.setFont(UiUtil.plain(12));
        cmbPreset.setPreferredSize(new Dimension(150, 28));
        cmbPreset.addActionListener(e -> onPresetChanged());
        dateControls.add(cmbPreset);

        lblFrom = new JLabel("From");
        UiUtil.styleNote(lblFrom);
        dateControls.add(lblFrom);
        spinFrom = dateSpinner(daysAgo(30));
        dateControls.add(spinFrom);

        lblTo = new JLabel("To");
        UiUtil.styleNote(lblTo);
        dateControls.add(lblTo);
        spinTo = dateSpinner(new Date());
        dateControls.add(spinTo);

        JButton clear = new JButton("Clear");
        UiUtil.styleTintedButton(clear);
        clear.setPreferredSize(new Dimension(80, 28));
        clear.addActionListener(e -> {
            updatingFilter = true;
            cmbPreset.setSelectedItem(ALL);
            updatingFilter = false;
            onPresetChanged();
        });
        dateControls.add(clear);

        setCustomVisible(false);
    }

    /**
     * The right-aligned end of the date-filter row, for a page that wants an
     * action (e.g. "View Details") to sit on the same row as the date filter
     * instead of its own separate banner row. Callers add their own
     * components to it directly.
     *
     * <p>Only non-null when this pager was installed with
     * {@code withDateFilter = true} - there is no date row to attach to
     * otherwise.
     */
    public JPanel dateFilterTrailing() {
        return hasDates ? trailing : null;
    }

    private JSpinner dateSpinner(Date initial) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(
                initial, null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setFont(UiUtil.plain(12));
        spinner.setPreferredSize(new Dimension(110, 28));
        spinner.addChangeListener(e -> {
            if (!updatingFilter) {
                apply();
            }
        });
        return spinner;
    }

    private void buildPagerBar() {
        pagerBar.setOpaque(false);
        pagerBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        UiUtil.styleNote(lblShowing);
        pagerBar.add(lblShowing, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        UiUtil.styleTintedButton(btnPrev);
        btnPrev.setPreferredSize(new Dimension(84, 28));
        btnPrev.addActionListener(e -> {
            if (page > 0) {
                page--;
                render();
            }
        });
        right.add(btnPrev);

        lblPage.setFont(UiUtil.bold(12));
        lblPage.setForeground(UiUtil.DARK);
        right.add(lblPage);

        UiUtil.styleTintedButton(btnNext);
        btnNext.setPreferredSize(new Dimension(84, 28));
        btnNext.addActionListener(e -> {
            if (page < pageCount() - 1) {
                page++;
                render();
            }
        });
        right.add(btnNext);

        pagerBar.add(right, BorderLayout.EAST);
    }

    private void onPresetChanged() {
        boolean custom = CUSTOM.equals(cmbPreset.getSelectedItem());
        setCustomVisible(custom);
        filterBar.revalidate();
        filterBar.repaint();
        apply();
    }

    private void setCustomVisible(boolean visible) {
        lblFrom.setVisible(visible);
        spinFrom.setVisible(visible);
        lblTo.setVisible(visible);
        spinTo.setVisible(visible);
    }

    // ------------------------------------------------------------------ rows

    /** Drops every row. Call before re-populating. */
    public void clear() {
        all.clear();
        model.setRowCount(0);
    }

    /** Adds a row with no timestamp (never excluded by the date filter). */
    public void addRow(Object[] cells) {
        addRow(cells, null, null);
    }

    /** Adds a row whose {@code when} the date filter applies to. */
    public void addRow(Object[] cells, Date when) {
        addRow(cells, when, null);
    }

    /**
     * Adds a row carrying a {@code key} distinct from its displayed
     * {@code cells} - for {@link #selectRowByKey}, when a caller needs to
     * reselect a specific record by its real id even though the cells show
     * a friendly number instead of that id.
     */
    public void addRow(Object[] cells, Date when, Object key) {
        all.add(new Row(cells, when, all.size(), key));
    }

    /**
     * Maps a row index in the table model (i.e. a row on the current page)
     * back to the position it was added in, so callers can index into the
     * parallel list they built the rows from. Returns -1 when out of range.
     *
     * <p>This is essential with pagination: the model holds only the current
     * page, so model row 0 on page 2 is <em>not</em> element 0 of the list.
     */
    public int sourceIndexOf(int modelRow) {
        if (modelRow < 0) {
            return -1;
        }
        int index = page * PAGE_SIZE + modelRow;
        if (index >= filtered.size()) {
            return -1;
        }
        return filtered.get(index).sourceIndex;
    }

    /** Re-applies the filter, resets to the first page and renders. */
    public void apply() {
        filtered.clear();
        Date from = filterFrom();
        Date to = filterTo();
        for (Row row : all) {
            if (matches(row, from, to)) {
                filtered.add(row);
            }
        }
        page = 0;
        render();
    }

    private boolean matches(Row row, Date from, Date to) {
        if (from == null && to == null) {
            return true;
        }
        if (row.when == null) {
            // Undated rows are kept; the filter has nothing to test them on.
            return true;
        }
        if (from != null && row.when.before(from)) {
            return false;
        }
        return to == null || !row.when.after(to);
    }

    private Date filterFrom() {
        if (!hasDates || cmbPreset == null) {
            return null;
        }
        Object preset = cmbPreset.getSelectedItem();
        if (ALL.equals(preset)) {
            return null;
        }
        if (TODAY.equals(preset)) {
            return startOfDay(new Date());
        }
        if (DAYS_7.equals(preset)) {
            return startOfDay(daysAgo(6));
        }
        if (DAYS_30.equals(preset)) {
            return startOfDay(daysAgo(29));
        }
        if (THIS_MONTH.equals(preset)) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DAY_OF_MONTH, 1);
            return startOfDay(c.getTime());
        }
        return startOfDay((Date) spinFrom.getValue());
    }

    private Date filterTo() {
        if (!hasDates || cmbPreset == null) {
            return null;
        }
        Object preset = cmbPreset.getSelectedItem();
        if (ALL.equals(preset)) {
            return null;
        }
        if (CUSTOM.equals(preset)) {
            return endOfDay((Date) spinTo.getValue());
        }
        return endOfDay(new Date());
    }

    // --------------------------------------------------------------- render

    private void render() {
        model.setRowCount(0);
        int total = filtered.size();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        for (int i = start; i < end; i++) {
            model.addRow(filtered.get(i).cells);
        }

        if (total == 0) {
            lblShowing.setText(all.isEmpty()
                    ? "No records" : "No records match the selected dates");
        } else {
            lblShowing.setText("Showing " + (start + 1) + "-" + end + " of " + total
                    + (all.size() != total ? " (filtered from " + all.size() + ")" : ""));
        }
        lblPage.setText("Page " + (total == 0 ? 1 : page + 1) + " of " + pageCount());
        btnPrev.setEnabled(page > 0);
        btnNext.setEnabled(page < pageCount() - 1);

        table.clearSelection();
        table.revalidate();
        table.repaint();
    }

    private int pageCount() {
        return Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    /** Rows passing the date filter, across all pages. */
    public int getFilteredCount() {
        return filtered.size();
    }

    /** Rows loaded, before date filtering. */
    public int getTotalCount() {
        return all.size();
    }

    /**
     * Moves to the page holding the row whose column {@code column} equals
     * {@code value}, and selects it. Returns false when no row matches.
     */
    public boolean selectRowWhere(int column, Object value) {
        for (int i = 0; i < filtered.size(); i++) {
            Object cell = filtered.get(i).cells[column];
            if (cell != null && cell.equals(value)) {
                page = i / PAGE_SIZE;
                render();
                int visibleRow = i - page * PAGE_SIZE;
                table.setRowSelectionInterval(visibleRow, visibleRow);
                table.scrollRectToVisible(table.getCellRect(visibleRow, 0, true));
                return true;
            }
        }
        return false;
    }

    /**
     * Same as {@link #selectRowWhere}, but matches a row's {@link Row#key}
     * (set via {@link #addRow(Object[], Date, Object)}) instead of a
     * displayed cell value - for tables whose id column shows a friendly
     * number rather than the real database id.
     */
    public boolean selectRowByKey(Object key) {
        for (int i = 0; i < filtered.size(); i++) {
            Object rowKey = filtered.get(i).key;
            if (rowKey != null && rowKey.equals(key)) {
                page = i / PAGE_SIZE;
                render();
                int visibleRow = i - page * PAGE_SIZE;
                table.setRowSelectionInterval(visibleRow, visibleRow);
                table.scrollRectToVisible(table.getCellRect(visibleRow, 0, true));
                return true;
            }
        }
        return false;
    }

    // --------------------------------------------------------------- helpers

    private JScrollPane findScrollPane() {
        Container c = table.getParent();
        while (c != null && !(c instanceof JScrollPane)) {
            c = c.getParent();
        }
        return (JScrollPane) c;
    }

    private static int indexOf(Container parent, Component child) {
        for (int i = 0; i < parent.getComponentCount(); i++) {
            if (parent.getComponent(i) == child) {
                return i;
            }
        }
        return -1;
    }

    private static Object constraintsOf(Container parent, Component child) {
        if (parent.getLayout() instanceof BorderLayout) {
            return ((BorderLayout) parent.getLayout()).getConstraints(child);
        }
        return null;
    }

    private static Date daysAgo(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -days);
        return c.getTime();
    }

    private static Date startOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date endOfDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }
}
