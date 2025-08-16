package lyfjshs.gomis.components.table;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Default implementation of TableActionManager that provides functionality
 * for adding and managing table actions.
 */
public class DefaultTableActionManager implements TableActionManager {
    private final List<TableRowAction> actions = new ArrayList<>();

    /**
     * Adds an action button to the column with the specified parameters.
     *
     * @param text        the label for the button
     * @param action      the action to perform when the button is clicked
     * @param buttonColor the background color of the button
     * @param icon        the optional icon for the button (can be null)
     * @return this instance for method chaining
     */
    public DefaultTableActionManager addAction(String text, BiConsumer<JTable, Integer> action,
                                            Color buttonColor, Icon icon) {
        actions.add(new TableRowAction(text, action, buttonColor, icon));
        return this;
    }

    @Override
    public void onTableAction(GTable table, int row) {
        // This method will be called by the ActionColumnEditor when an action is performed
        // The actual action handling is done through the BiConsumer in TableRowAction
    }

    @Override
    public void setupTableColumn(GTable table, int actionColumnIndex) {
        TableColumn actionColumn = table.getColumnModel().getColumn(actionColumnIndex);
        actionColumn.setCellRenderer(new ActionColumnRenderer(actions));
        actionColumn.setCellEditor(new ActionColumnEditor(actions));

        // Set preferred width based on number of actions
        int buttonWidth = 80; // width per button
        int spacing = 10; // spacing between buttons
        actionColumn.setPreferredWidth((buttonWidth + spacing) * actions.size());
    }

    @Override
    public void applyTo(GTable table) {
        // This method will no longer be used as setup is done manually in SessionsFillUpFormPanel
        // Find the last column index (actions column)
        // int actionColumnIndex = table.getColumnCount() - 1;
        // setupTableColumn(table, actionColumnIndex);
    }

    public List<TableRowAction> getActions() {
        return actions;
    }
} 