package lyfjshs.gomis.components.table;

/**
 * A custom table cell editor for handling action buttons inside a specific
 * column of a {@link JTable}. This editor replaces the default cell content
 * with an {@link ActionColumnPanel}, allowing interaction with row-specific
 * actions.
 * <p>
 * The actions are defined as a list of {@link TableRowAction} and passed during
 * initialization.
 * </p>
 */
public class ActionColumnEditor extends javax.swing.DefaultCellEditor {
	private final java.util.List<TableRowAction> actions;

	/**
	 * Constructs an {@code ActionColumnEditor} with the specified list of actions.
	 *
	 * @param actions the list of {@link TableRowAction} defining the available
	 *                actions for each row
	 */
	public ActionColumnEditor(java.util.List<TableRowAction> actions) {
		super(new javax.swing.JCheckBox());
		this.actions = actions;
	}

	/**
	 * Returns a component that renders action buttons for the specified table cell
	 * when it is being edited.
	 *
	 * @param table      the {@link JTable} that contains this editor
	 * @param value      the value to be edited (not used, since this column
	 *                   contains buttons)
	 * @param isSelected whether the cell is selected
	 * @param row        the row index of the cell being edited
	 * @param column     the column index of the cell being edited
	 * @return a {@link ActionColumnPanel} containing action buttons for the row
	 */
	@Override
	public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row,
			int column) {
		ActionColumnPanel panel = new ActionColumnPanel(actions, table, row);
		panel.setBackground(table.getSelectionBackground());
		return panel;
	}
}
