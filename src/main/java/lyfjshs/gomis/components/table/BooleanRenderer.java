/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.table;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class BooleanRenderer extends JCheckBox implements TableCellRenderer {
	public BooleanRenderer() {
		setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		setSelected(value != null && (Boolean) value);
		if (isSelected) {
			setBackground(UIManager.getColor("TableHeader.hoverBackground"));
			setForeground(UIManager.getColor("Table.foreground"));
		} else {
			setBackground(UIManager.getColor("Table.background"));
			setForeground(UIManager.getColor("Table.foreground"));
		}
		return this;
	}
}