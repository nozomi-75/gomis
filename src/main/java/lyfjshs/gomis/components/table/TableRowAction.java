package lyfjshs.gomis.components.table;

import java.awt.Color;
import java.util.function.BiConsumer;

import javax.swing.Icon;
import javax.swing.JTable;


/**
 * Represents an action that can be performed on a specific row of a
 * {@link JTable}. This class encapsulates the action's properties, including
 * text, behavior, button color, and an optional icon.
 * 
 * Instances of this class are used in conjunction with
 * {@link TableActionBuilder} to define and apply actions to table rows.
 */
public class TableRowAction {
	private final String text; // The label displayed on the action button
	private final java.util.function.BiConsumer<javax.swing.JTable, Integer> action; // The logic to execute when the action is triggered
	private final java.awt.Color buttonColor; // Background color of the button
	private final Icon icon; // Optional icon for the button

	/**
	 * Constructs a new {@code TableRowAction} with the specified properties.
	 *
	 * @param text        the label displayed on the button
	 * @param action      the function to execute when the button is clicked
	 *                    (receives the table and row index as parameters)
	 * @param buttonColor the background color of the button
	 * @param icon        an optional icon to display on the button (can be
	 *                    {@code null})
	 */
	public TableRowAction(String text, java.util.function.BiConsumer<javax.swing.JTable, Integer> action, java.awt.Color buttonColor, Icon icon) {
		this.text = text;
		this.action = action;
		this.buttonColor = buttonColor;
		this.icon = icon;
	}

	/**
	 * Gets the icon associated with this action.
	 *
	 * @return the icon representing the icon, or {@code null} if
	 *         none is set
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Gets the text label of this action.
	 *
	 * @return the action label
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the action behavior to be executed when this action is triggered.
	 *
	 * @return a {@link BiConsumer} that takes a {@link JTable} and row index as
	 *         arguments
	 */
	public java.util.function.BiConsumer<javax.swing.JTable, Integer> getAction() {
		return action;
	}

	/**
	 * Gets the background color of the button representing this action.
	 *
	 * @return the {@link Color} of the button
	 */
	public java.awt.Color getButtonColor() {
		return buttonColor;
	}
}
