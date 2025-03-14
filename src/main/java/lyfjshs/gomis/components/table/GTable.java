package lyfjshs.gomis.components.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.formdev.flatlaf.FlatClientProperties;

public class GTable extends JTable {    
    private static final long serialVersionUID = 1L;
    private double[] columnProportions;
    private int[] columnAlignments;
    private boolean hasCheckbox;
    private TableActionManager actionManager;

    public GTable(Object[][] data, String[] columnNames, Class<?>[] columnTypes, 
                  boolean[] editableColumns, double[] columnWidths, int[] alignments, 
                  boolean includeCheckbox, TableActionManager actionManager) {
        super(new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return editableColumns[columnIndex];
            }
        });
        
        this.hasCheckbox = includeCheckbox;
        this.columnProportions = columnWidths.clone();
        this.columnAlignments = alignments.clone();
        this.actionManager = actionManager;
        
        configureTable();
        applyColumnWidths();
        applyColumnAlignments();
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (isRowSelected(row)) {
            c.setBackground(UIManager.getColor("TableHeader.hoverBackground"));
            c.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            c.setBackground(UIManager.getColor("Table.background"));
            c.setForeground(UIManager.getColor("Table.foreground"));
        }
        return c;
    }

    private void configureTable() {
        setShowVerticalLines(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowHeight(35);
        setFont(new Font("Tahoma", Font.PLAIN, 12));
        getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        
        putClientProperty(FlatClientProperties.STYLE,
                "showHorizontalLines:true;" +
                "intercellSpacing:0,1;" +
                "cellFocusColor:$TableHeader.hoverBackground;" +
                "selectionBackground:$TableHeader.hoverBackground;" +
                "selectionForeground:$Table.foreground");

        getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "height:30;" +
                "hoverBackground:null;" +
                "pressedBackground:null;" +
                "separatorColor:$TableHeader.background;" +
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

    public void setColumnWidths(double[] widths) {
        if (widths.length != getColumnCount()) {
            throw new IllegalArgumentException("Width array must match column count");
        }
        this.columnProportions = widths.clone();
        applyColumnWidths();
    }

    private void applyColumnWidths() {
        int totalWidth = getParent() != null ? getParent().getWidth() : 800;
        if (totalWidth <= 0) totalWidth = 800;
        
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (hasCheckbox && i == 0) continue;
            if (actionManager != null && i == getColumnCount() - 1) {
                // Skip width adjustment for Actions column to respect actionManager's width
                continue;
            }
            int width = (int) (totalWidth * columnProportions[i]);
            columnModel.getColumn(i).setPreferredWidth(width);
        }
    }

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
        int endIndex = actionManager != null ? getColumnCount() - 2 : getColumnCount() - 1; // Skip Actions column
        for (int i = startIndex; i <= endIndex; i++) {
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(columnAlignments[i]);
            columnModel.getColumn(i).setCellRenderer(renderer);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        applyColumnWidths();
    }
}