/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.table;

import javax.swing.JTable;

/**
 * Interface for managing table actions in GTable
 */
public interface TableActionManager {
	/**
	 * Called when an action is performed on a table row
	 * @param table The GTable instance
	 * @param row The row index where the action was performed
	 */
	void onTableAction(GTable table, int row);

	/**
	 * Sets up the action column in the table
	 * @param table The GTable instance
	 * @param actionColumnIndex The index of the action column
	 */
	void setupTableColumn(GTable table, int actionColumnIndex);

	/**
	 * Applies the action manager to a table
	 * @param table The GTable instance
	 */
	void applyTo(GTable table);
}
