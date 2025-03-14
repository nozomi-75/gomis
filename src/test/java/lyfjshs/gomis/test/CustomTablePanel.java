package lyfjshs.gomis.test;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class CustomTablePanel extends JPanel {
	private JTable table;
    private DefaultTableModel model;
    private double[] columnProportions;
    private int[] columnAlignments;
    private JButton toggleCheckboxButton;
    private boolean hasCheckboxColumn = true;
    private Object[][] originalData;

    public CustomTablePanel(Object[][] data, String[] columnNames, Class<?>[] columnTypes, boolean[] editableColumns,
                            double[] columnWidths, int[] alignments) {
        this.originalData = data.clone();
        initComponents();
        initializeTable(data, columnNames, columnTypes, editableColumns, columnWidths, alignments);
        addToggleButton();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow]"));
        setBackground(UIManager.getColor("Panel.background"));
    }

    private void initializeTable(Object[][] data, String[] columnNames, Class<?>[] columnTypes, boolean[] editableColumns,
                                 double[] columnWidths, int[] alignments) {
        model = new DefaultTableModel(data, columnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return editableColumns[columnIndex];
            }
        };

        table = new JTable(model) {
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

            @Override
            public boolean editCellAt(int row, int column, java.util.EventObject e) {
                if (column == 0 && e instanceof java.awt.event.MouseEvent) {
                    return super.editCellAt(row, column, e);
                }
                return false;
            }
        };

        configureTable();
        setTableColumnWidths(columnWidths);
        setColumnAlignments(alignments);
    }
    
    private void configureTable() {
        table.setRowHeight(30);
        table.putClientProperty(FlatClientProperties.STYLE,
                "showHorizontalLines:true;intercellSpacing:0,1;cellFocusColor:$TableHeader.hoverBackground;selectionBackground:$TableHeader.hoverBackground;selectionForeground:$Table.foreground;");

        table.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "height:30;hoverBackground:null;pressedBackground:null;separatorColor:$TableHeader.background;font:bold;");

        updateCheckboxColumn();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.putClientProperty(FlatClientProperties.STYLE, "border:null;");
        add(scrollPane, "cell 0 1,grow");
    }

    private void addToggleButton() {
        toggleCheckboxButton = new JButton("Remove Checkbox");
        toggleCheckboxButton.addActionListener(e -> toggleCheckboxColumn());
        add(toggleCheckboxButton, "cell 0 0");
    }

    private void toggleCheckboxColumn() {
        hasCheckboxColumn = !hasCheckboxColumn;
        toggleCheckboxButton.setText(hasCheckboxColumn ? "Remove Checkbox" : "Add Checkbox");
        
        // Store current data excluding checkbox column if it exists
        Object[][] currentData = new Object[model.getRowCount()][model.getColumnCount() - (hasCheckboxColumn ? 0 : 1)];
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = hasCheckboxColumn ? 0 : 1, k = 0; j < model.getColumnCount(); j++, k++) {
                currentData[i][k] = model.getValueAt(i, j);
            }
        }

        // Create new column names and types
        String[] newColumnNames = hasCheckboxColumn ? 
            new String[]{"Select", "Name", "Age", "Score"} : 
            new String[]{"Name", "Age", "Score"};
        Class<?>[] newColumnTypes = hasCheckboxColumn ?
            new Class<?>[]{Boolean.class, String.class, Integer.class, Double.class} :
            new Class<?>[]{String.class, Integer.class, Double.class};
        boolean[] newEditableColumns = hasCheckboxColumn ?
            new boolean[]{true, false, false, false} :
            new boolean[]{false, false, false};
        double[] newWidths = hasCheckboxColumn ?
            new double[]{0.1, 0.4, 0.25, 0.25} :
            new double[]{0.5, 0.25, 0.25};
        int[] newAlignments = hasCheckboxColumn ?
            new int[]{SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT} :
            new int[]{SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT};

        // Create new model
        model = new DefaultTableModel(hasCheckboxColumn ? originalData : currentData, newColumnNames) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return newColumnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return newEditableColumns[columnIndex];
            }
        };

        table.setModel(model);
        updateCheckboxColumn();
        setTableColumnWidths(newWidths);
        setColumnAlignments(newAlignments);
        table.revalidate();
        table.repaint();
    }

    private void updateCheckboxColumn() {
        if (hasCheckboxColumn) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(0).setCellRenderer(new BooleanRenderer());
            table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(checkBox));
        }
    }
    
    public void setTableColumnWidths(double[] columnProportions) {
        this.columnProportions = columnProportions.clone();
        applyColumnWidths();
    }

    private void applyColumnWidths() {
        if (columnProportions == null) return;
        int totalWidth = getParent() != null ? getParent().getWidth() : 800;
        if (totalWidth <= 0) totalWidth = 800;
        for (int i = 0; i < table.getColumnCount(); i++) {
            int preferredWidth = (int) (totalWidth * columnProportions[i]);
            table.getColumnModel().getColumn(i).setPreferredWidth(preferredWidth);
        }
    }

    public void setColumnAlignment(int columnIndex, int alignment) {
        if (columnIndex == 0) return;
        if (columnAlignments == null) {
            columnAlignments = new int[table.getColumnCount()];
            for (int i = 0; i < columnAlignments.length; i++) {
                columnAlignments[i] = SwingConstants.LEFT;
            }
        }
        columnAlignments[columnIndex] = alignment;
        applyColumnAlignment(columnIndex);
    }

    private void applyColumnAlignment(int columnIndex) {
        if (columnIndex == 0) return;
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(columnAlignments[columnIndex]);
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
    }

    public void setColumnAlignments(int[] alignments) {
        if (alignments == null || alignments.length != table.getColumnCount()) {
            throw new IllegalArgumentException("Alignments array must match the number of columns");
        }
        columnAlignments = alignments.clone();
        applyAllColumnAlignments();
    }

    private void applyAllColumnAlignments() {
        if (columnAlignments == null) return;
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0) continue;
            applyColumnAlignment(i);
        }
    }

    @Override
    public void doLayout() {
        super.doLayout();
        applyColumnWidths();
        applyAllColumnAlignments();
    }

    public JTable getTable() {
        return table;
    }
}

class BooleanRenderer extends JCheckBox implements TableCellRenderer {
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
