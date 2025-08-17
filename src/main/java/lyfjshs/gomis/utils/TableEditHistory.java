/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.utils;

import java.util.Stack;

import javax.swing.table.DefaultTableModel;

public class TableEditHistory {
    private final Stack<TableEdit> undoStack = new Stack<>();
    private final Stack<TableEdit> redoStack = new Stack<>();
    private static final int MAX_HISTORY = 50;

    public void pushEdit(TableEdit edit) {
        undoStack.push(edit);
        redoStack.clear(); // Clear redo stack when new edit is made
        
        // Maintain history size
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.remove(0);
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo(DefaultTableModel model) {
        if (canUndo()) {
            TableEdit edit = undoStack.pop();
            redoStack.push(edit.createOpposite());
            edit.undo(model);
        }
    }

    public void redo(DefaultTableModel model) {
        if (canRedo()) {
            TableEdit edit = redoStack.pop();
            undoStack.push(edit.createOpposite());
            edit.undo(model); // We use undo here because the edit is already "opposite"
        }
    }

    public static class TableEdit {
        private final int row;
        private final int column;
        private final Object oldValue;
        private final Object newValue;

        public TableEdit(int row, int column, Object oldValue, Object newValue) {
            this.row = row;
            this.column = column;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public void undo(DefaultTableModel model) {
            model.setValueAt(oldValue, row, column);
        }

        public TableEdit createOpposite() {
            return new TableEdit(row, column, newValue, oldValue);
        }
    }
}
