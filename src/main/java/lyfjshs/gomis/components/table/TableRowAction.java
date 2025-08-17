/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.table;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

/**
 * Represents a single action that can be performed on a table row.
 * This class holds the information needed to display and execute a table action.
 */
public class TableRowAction {
	private final String text;
	private final BiConsumer<JTable, Integer> action;
	private final Color buttonColor;
	private final Icon icon;

	/**
	 * Creates a new TableRowAction with the specified parameters.
	 *
	 * @param text        the label for the action button
	 * @param action      the action to perform when clicked
	 * @param buttonColor the background color of the button
	 * @param icon        the icon to display on the button (can be null)
	 */
	public TableRowAction(String text, BiConsumer<JTable, Integer> action, Color buttonColor, Icon icon) {
		this.text = text;
		this.action = action;
		this.buttonColor = buttonColor;
		this.icon = icon;
	}

	/**
	 * Gets the button label text.
	 *
	 * @return the text to display on the button
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the action to perform.
	 *
	 * @return the action as a BiConsumer
	 */
	public BiConsumer<JTable, Integer> getAction() {
		return action;
	}

	/**
	 * Gets the button background color.
	 *
	 * @return the color for the button
	 */
	public Color getButtonColor() {
		return buttonColor;
	}

	/**
	 * Gets the button icon.
	 *
	 * @return the icon to display, or null if no icon
	 */
	public Icon getIcon() {
		return icon;
	}
}
