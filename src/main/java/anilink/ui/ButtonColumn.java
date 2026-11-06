package anilink.ui;

import anilink.util.UiUtil;
import java.awt.Color;
import java.awt.Component;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Renders a table column as a button and runs a callback when it is clicked.
 *
 * <p>Replaces the hand-rolled renderer/editor pairs that used to be duplicated
 * in the admin panels. Everything is per-row and resolved lazily, so a button's
 * label, colour and enabled state can all follow the record behind the row -
 * e.g. "Disable"/"Enable" flipping with the account, or Assign staying greyed
 * out until a payment is approved.
 *
 * <p>Row indexes handed to the callbacks are <strong>model</strong> rows, which
 * with {@link TablePager} means rows on the current page. Callers map them with
 * {@link TablePager#sourceIndexOf(int)} before indexing their own list.
 */
public final class ButtonColumn {

    private ButtonColumn() {
    }

    /** Per-row appearance and behaviour of the button. */
    public interface Spec {

        String label(int modelRow);

        boolean enabled(int modelRow);

        Color background(int modelRow);

        /** Shown when the button is disabled, to explain why. Nullable. */
        String disabledTooltip(int modelRow);
    }

    /**
     * Installs the button into {@code column}. {@code onClick} receives the
     * model row that was clicked.
     */
    public static void install(JTable table, int column, Spec spec, Consumer<Integer> onClick) {
        table.getColumnModel().getColumn(column)
                .setCellRenderer(new ButtonRenderer(spec));
        table.getColumnModel().getColumn(column)
                .setCellEditor(new ButtonEditor(spec, onClick));
    }

    /** Convenience for a fixed label and colour that is always enabled. */
    public static void install(JTable table, int column, String label,
            Color background, Consumer<Integer> onClick) {
        install(table, column, simple(label, background), onClick);
    }

    public static Spec simple(String label, Color background) {
        return of(row -> label, row -> true, row -> background, row -> null);
    }

    public static Spec of(Function<Integer, String> label,
            Function<Integer, Boolean> enabled,
            Function<Integer, Color> background,
            Function<Integer, String> disabledTooltip) {
        return new Spec() {
            @Override
            public String label(int modelRow) {
                return label.apply(modelRow);
            }

            @Override
            public boolean enabled(int modelRow) {
                return Boolean.TRUE.equals(enabled.apply(modelRow));
            }

            @Override
            public Color background(int modelRow) {
                return background.apply(modelRow);
            }

            @Override
            public String disabledTooltip(int modelRow) {
                return disabledTooltip.apply(modelRow);
            }
        };
    }

    private static void dress(JButton button, Spec spec, int modelRow) {
        boolean on = spec.enabled(modelRow);
        button.setText(spec.label(modelRow));
        button.setEnabled(on);
        button.setFont(UiUtil.bold(11));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        if (on) {
            button.setBackground(spec.background(modelRow));
            button.setForeground(UiUtil.WHITE);
            button.setToolTipText(null);
        } else {
            button.setBackground(new Color(224, 224, 224));
            button.setForeground(UiUtil.MUTED);
            button.setToolTipText(spec.disabledTooltip(modelRow));
        }
    }

    private static final class ButtonRenderer extends JButton implements TableCellRenderer {

        private final Spec spec;

        ButtonRenderer(Spec spec) {
            this.spec = spec;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            dress(this, spec, table.convertRowIndexToModel(row));
            return this;
        }
    }

    private static final class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button = new JButton();
        private final Spec spec;
        private final Consumer<Integer> onClick;
        private int modelRow = -1;

        ButtonEditor(Spec spec, Consumer<Integer> onClick) {
            this.spec = spec;
            this.onClick = onClick;
            button.addActionListener(e -> {
                int clicked = modelRow;
                fireEditingStopped();
                if (clicked >= 0) {
                    onClick.accept(clicked);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            modelRow = table.convertRowIndexToModel(row);
            dress(button, spec, modelRow);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
