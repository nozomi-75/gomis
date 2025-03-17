package lyfjshs.gomis.components.table;

/**
 * A custom table cell renderer for displaying action buttons inside a specific column of a {@link JTable}.
 * This renderer replaces the default cell content with an {@link ActionColumnPanel}, which contains buttons
 * for performing actions on the corresponding row.
 * <p>
 * The actions are defined as a list of {@link TableRowAction} and passed during initialization.
 * </p>
 */
public class ActionColumnRenderer extends javax.swing.table.DefaultTableCellRenderer {
    private final java.util.List<TableRowAction> actions;

    /**
     * Constructs an {@code ActionColumnRenderer} with the specified list of actions.
     *
     * @param actions the list of {@link TableRowAction} defining the available actions for each row
     */
    public ActionColumnRenderer(java.util.List<TableRowAction> actions) {
        this.actions = actions;
    }

    /**
     * Returns a component that renders action buttons in the specified table cell.
     *
     * @param table      the {@link JTable} that contains this renderer
     * @param value      the value to be rendered (not used, since this column contains buttons)
     * @param isSelected whether the cell is selected
     * @param hasFocus   whether the cell has focus
     * @param row        the row index of the cell being rendered
     * @param column     the column index of the cell being rendered
     * @return a {@link ActionColumnPanel} containing action buttons for the row
     */
    @Override
    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        ActionColumnPanel panel = new ActionColumnPanel(actions, table, -1);
        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return panel;
    }
}
