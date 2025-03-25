package lyfjshs.gomis.components.table;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatTable;
import com.formdev.flatlaf.extras.components.FlatTableHeader;

import lyfjshs.gomis.Main;

public class GTable extends FlatTable {
    private static final long serialVersionUID = 1L;
    private double[] columnProportions;
    private int[] columnAlignments;
    private boolean hasCheckbox;
    private TableActionManager actionManager;

    public GTable(Object[][] data, String[] columnNames, Class<?>[] columnTypes,
            boolean[] editableColumns, double[] columnWidths, int[] alignments,
            boolean includeCheckbox, TableActionManager actionManager) {

        DefaultTableModel defaultTableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return editableColumns[columnIndex];
            }
        };

        this.setModel(defaultTableModel);

        this.hasCheckbox = includeCheckbox;
        this.columnProportions = columnWidths.clone();
        this.columnAlignments = alignments.clone();
        this.actionManager = actionManager;

        configureTable();
//        applyColumnWidths();
        applyColumnAlignments();
        updateRowHeightFromSettings();

        // Add listener to update column widths on parent resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
//                applyColumnWidths();
            }
        });
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (isRowSelected(row)) {
            c.setBackground(UIManager.getColor("Table.selectionBackground"));
            c.setForeground(UIManager.getColor("Table.selectionForeground"));
        } else {
            c.setBackground(UIManager.getColor("Table.background"));
            c.setForeground(UIManager.getColor("Table.foreground"));
        }
        return c;
    }

    private void configureTable() {
        setShowVerticalLines(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Calculate row height based on font size
        updateRowHeightFromSettings();

        // Use FlatTableHeader for a modern header
        FlatTableHeader header = new FlatTableHeader();
        header.setColumnModel(getColumnModel());
        setTableHeader(header);

        putClientProperty(FlatClientProperties.STYLE,
                "showHorizontalLines:true;" +
                        "intercellSpacing:0,1;" +
                        "cellFocusColor:$TableHeader.hoverBackground;" +
                        "selectionBackground:$Table.selectionBackground;" +
                        "selectionForeground:$Table.selectionForeground");

        getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "height:30;" +
                        "hoverBackground:$TableHeader.hoverBackground;" +
                        "pressedBackground:$TableHeader.pressedBackground;" +
                        "separatorColor:$TableHeader.separatorColor;" +
                        "font:bold");

        if (hasCheckbox) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            TableColumn checkColumn = getColumnModel().getColumn(0);
            checkColumn.setCellRenderer(new BooleanRenderer());
            checkColumn.setCellEditor(new DefaultCellEditor(checkBox));
            checkColumn.setMaxWidth(50);
        }

        if (actionManager != null) {
            actionManager.applyTo(this, getColumnCount() - 1);
        }
    }

    public void updateRowHeightFromSettings() {
        int fontSize = Main.settings.getSettingsState().fontSize; // Get font size from settings
        int rowHeight = fontSize + 20; // Dynamically adjust height
        setRowHeight(rowHeight);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (Main.settings != null) {
            SwingUtilities.invokeLater(this::updateRowHeightFromSettings); // 🔹 Forces row height update
        }
    }

    public void setColumnWidths(double[] widths) {
        if (widths.length != getColumnCount()) {
            throw new IllegalArgumentException("Width array must match column count");
        }
        this.columnProportions = widths.clone();
//        applyColumnWidths();
    }

//    private void applyColumnWidths() {
//        int totalWidth = getParent() != null ? getParent().getWidth() : 800;
//        if (totalWidth <= 0)
//            totalWidth = 800;
//
//        TableColumnModel columnModel = getColumnModel();
//        for (int i = 0; i < columnModel.getColumnCount(); i++) {
//            TableColumn column = columnModel.getColumn(i);
//
//            // Handle checkbox column
//            if (hasCheckbox && i == 0)
//                continue;
//
//            // Handle Actions column (Allow Resizing)
//            if (actionManager != null && i == getColumnCount() - 1) {
//                int actionColumnWidth = (int) (totalWidth * 0.15); // Make it responsive (15% of table width)
//                column.setPreferredWidth(actionColumnWidth);
//                column.setResizable(true);
//                continue;
//            }
//
//            // Normal columns
//            int width = (int) (totalWidth * columnProportions[i]);
//            column.setPreferredWidth(width);
//        }
//    }

    public void setColumnAlignments(int[] alignments) {
        if (alignments.length != getColumnCount()) {
            throw new IllegalArgumentException("Alignments array must match column count");
        }
        this.columnAlignments = alignments.clone();
        applyColumnAlignments();
    }

    private void applyColumnAlignments() {
        TableColumnModel columnModel = getColumnModel();
        int startIndex = hasCheckbox ? 1 : 0;
        int endIndex = actionManager != null ? getColumnCount() - 2 : getColumnCount() - 1;
        for (int i = startIndex; i <= endIndex; i++) {
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(columnAlignments[i]);
            columnModel.getColumn(i).setCellRenderer(renderer);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
//        applyColumnWidths();
    }
}
