package lyfjshs.gomis.components.table;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Custom TableCellRenderer for rendering action buttons in a table column.
 */
public class ActionColumnRenderer implements TableCellRenderer {
    private final List<TableRowAction> actions;
    private final JPanel panel;

    /**
     * Creates a new ActionColumnRenderer with the specified actions.
     *
     * @param actions the list of actions to render as buttons
     */
    public ActionColumnRenderer(List<TableRowAction> actions) {
        this.actions = actions;
        this.panel = new JPanel();
        this.panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
        panel.removeAll();
        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        for (TableRowAction action : actions) {
            JButton button = new JButton(action.getText());
            button.setBackground(action.getButtonColor());
            if (action.getIcon() != null) {
                button.setIcon(action.getIcon());
            }
            panel.add(button);
        }

        return panel;
    }
}
