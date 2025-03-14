package lyfjshs.gomis.components.table;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * A panel that contains action buttons for a specific row in a JTable. This
 * panel is used to render action buttons inside a table column.
 * <p>
 * The panel dynamically creates buttons based on the provided list of actions.
 * Each button is associated with a corresponding {@link TableRowAction} that
 * defines its behavior.
 * </p>
 */
public class ActionColumnPanel extends JPanel {
	private final List<JButton> buttons = new ArrayList<JButton>(); // Explicit type

	/**
	 * Constructs an {@code ActionColumnPanel} that holds action buttons for a
	 * specific row.
	 *
	 * @param actions the list of {@link TableRowAction} defining the buttons to be
	 *                displayed
	 * @param table   the table that contains this panel
	 * @param row     the index of the row for which actions are created
	 */
	public ActionColumnPanel(List<TableRowAction> actions, JTable table, int row) {
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		setOpaque(true);

		for (TableRowAction action : actions) {
			JButton btn = createActionButton(action, table, row);
			buttons.add(btn);
			add(btn);
		}
	}

	/**
	 * Creates a button for a given action and assigns its properties.
	 *
	 * @param action the action that defines the button's properties
	 * @param table  the table that contains this button
	 * @param row    the row index for which this button is created
	 * @return a configured {@link JButton} instance
	 */
	private JButton createActionButton(TableRowAction action, JTable table, int row) {
		JButton btn = new JButton(action.getText());
		if (action.getIcon() != null) {
			btn.setIcon(action.getIcon());
		}
		btn.setFocusPainted(false);
		btn.setBackground(action.getButtonColor());
		btn.setForeground(Color.WHITE);

		if (row >= 0) {
			btn.addActionListener(e -> action.getAction().accept(table, row));
		}

		return btn;
	}
}
