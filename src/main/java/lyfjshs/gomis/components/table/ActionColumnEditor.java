package lyfjshs.gomis.components.table;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Custom TableCellEditor for handling action button clicks in a table column.
 */
public class ActionColumnEditor extends AbstractCellEditor implements TableCellEditor {
	private final List<TableRowAction> actions;
	private final JPanel panel;
	private JTable table;
	private int currentRow;

	/**
	 * Creates a new ActionColumnEditor with the specified actions.
	 *
	 * @param actions the list of actions to handle
	 */
	public ActionColumnEditor(List<TableRowAction> actions) {
		this.actions = actions;
		this.panel = new JPanel();
		this.panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
											   boolean isSelected, int row, int column) {
		this.table = table;
		this.currentRow = row;
		panel.removeAll();

		for (TableRowAction action : actions) {
			JButton button = new JButton(action.getText());
			button.setBackground(action.getButtonColor());
			if (action.getIcon() != null) {
				button.setIcon(action.getIcon());
			}

			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					action.getAction().accept(table, currentRow);
					fireEditingStopped();
				}
			});

			panel.add(button);
		}

		return panel;
	}

	@Override
	public Object getCellEditorValue() {
		return "";
	}
}
