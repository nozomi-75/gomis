package lyfjshs.gomis.components.table;

import javax.swing.JTable;

/**
 * Utility class for managing and applying action columns to {@link JTable}
 * instances. This class allows dynamic addition of action buttons to a table
 * column, enabling row-specific operations such as editing, deleting, or
 * viewing records.
 * <p>
 * It provides a fluent API to define multiple actions with custom labels,
 * colors, and icons.
 * </p>
 */
public class TableActionManager {
	private final java.util.List<TableRowAction> actions = new java.util.ArrayList<>();

	/**
	 * Adds an action button to the column with the specified parameters.
	 *
	 * @param text        the label for the button
	 * @param action      the action to perform when the button is clicked
	 * @param buttonColor the background color of the button
	 * @param icon        the optional icon for the button (can be {@code null})
	 * @return this instance for method chaining
	 */
	public TableActionManager addAction(String text, java.util.function.BiConsumer<javax.swing.JTable, Integer> action,
			java.awt.Color buttonColor, javax.swing.Icon icon) {
		actions.add(new TableRowAction(text, action, buttonColor, icon));
		return this;
	}

	/**
	 * Applies the defined actions as an interactive column to the specified table.
	 *
	 * @param table       the {@link JTable} to which the action column should be
	 *                    added
	 * @param columnIndex the index of the column where the action buttons should be
	 *                    placed
	 */
	public void applyTo(javax.swing.JTable table, int columnIndex) {
		javax.swing.table.TableColumn actionColumn = table.getColumnModel().getColumn(columnIndex);
		actionColumn.setCellRenderer(new ActionColumnRenderer(actions));
		actionColumn.setCellEditor(new ActionColumnEditor(actions));

		// Set preferred width based on number of actions
		int buttonWidth = 80; // width per button
		int spacing = 10; // spacing between buttons
		actionColumn.setPreferredWidth((buttonWidth + spacing) * actions.size());
	}
}
