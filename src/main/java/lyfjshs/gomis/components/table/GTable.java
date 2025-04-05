package lyfjshs.gomis.components.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
    
    // Pagination fields
    private int pageSize = 10;
    private int currentPage = 1;
    private int totalRows = 0;
    private List<Object[]> allData = new ArrayList<>();
    private JPanel paginationPanel;
    private JLabel pageInfoLabel;
    private JButton prevPageButton;
    private JButton nextPageButton;
    private JButton firstPageButton;
    private JButton lastPageButton;
    private boolean paginationEnabled = false;

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
        
        // Store all data for pagination
        if (data != null) {
            for (Object[] row : data) {
                allData.add(row);
            }
            totalRows = allData.size();
        }

        configureTable();
        applyColumnWidths();
        applyColumnAlignments();
        updateRowHeightFromSettings();
        
        // Create pagination panel
        createPaginationPanel();

        // Add listener to update column widths on parent resize
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyColumnWidths();
            }
        });
    }
    
    /**
     * Creates the pagination panel with navigation buttons and page info
     */
    private void createPaginationPanel() {
        paginationPanel = new JPanel(new BorderLayout());
        
        // Create navigation buttons
        firstPageButton = new JButton("<<");
        prevPageButton = new JButton("<");
        nextPageButton = new JButton(">");
        lastPageButton = new JButton(">>");
        pageInfoLabel = new JLabel("Page 1 of 1");
        
        // Add action listeners
        firstPageButton.addActionListener(e -> goToFirstPage());
        prevPageButton.addActionListener(e -> goToPreviousPage());
        nextPageButton.addActionListener(e -> goToNextPage());
        lastPageButton.addActionListener(e -> goToLastPage());
        
        // Add buttons to panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(firstPageButton);
        buttonPanel.add(prevPageButton);
        buttonPanel.add(pageInfoLabel);
        buttonPanel.add(nextPageButton);
        buttonPanel.add(lastPageButton);
        
        paginationPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Initially hide pagination panel
        paginationPanel.setVisible(false);
    }
    
    /**
     * Enables or disables pagination
     * @param enabled Whether pagination should be enabled
     * @param pageSize Number of rows per page (if enabled)
     */
    public void setPaginationEnabled(boolean enabled, int pageSize) {
        this.paginationEnabled = enabled;
        this.pageSize = pageSize;
        
        if (enabled) {
            paginationPanel.setVisible(true);
            updatePagination();
            displayCurrentPage();
        } else {
            paginationPanel.setVisible(false);
            displayAllData();
        }
    }
    
    /**
     * Gets the pagination panel to be added to the parent component
     * @return The pagination panel
     */
    public JPanel getPaginationPanel() {
        return paginationPanel;
    }
    
    /**
     * Updates the pagination controls based on current state
     */
    private void updatePagination() {
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        
        // Update page info
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        
        // Enable/disable buttons based on current page
        firstPageButton.setEnabled(currentPage > 1);
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < totalPages);
        lastPageButton.setEnabled(currentPage < totalPages);
    }
    
    /**
     * Displays the current page of data
     */
    private void displayCurrentPage() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.setRowCount(0);
        
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRows);
        
        for (int i = startIndex; i < endIndex; i++) {
            model.addRow(allData.get(i));
        }
    }
    
    /**
     * Displays all data without pagination
     */
    private void displayAllData() {
        DefaultTableModel model = (DefaultTableModel) getModel();
        model.setRowCount(0);
        
        for (Object[] row : allData) {
            model.addRow(row);
        }
    }
    
    /**
     * Sets the data for the table and updates pagination if enabled
     * @param data The new data to display
     */
    public void setData(Object[][] data) {
        allData.clear();
        if (data != null) {
            for (Object[] row : data) {
                allData.add(row);
            }
        }
        totalRows = allData.size();
        
        if (paginationEnabled) {
            currentPage = 1;
            updatePagination();
            displayCurrentPage();
        } else {
            displayAllData();
        }
    }
    
    /**
     * Adds a row to the table data
     * @param rowData The row data to add
     */
    public void addRow(Object[] rowData) {
        allData.add(rowData);
        totalRows = allData.size();
        
        if (paginationEnabled) {
            updatePagination();
            displayCurrentPage();
        } else {
            DefaultTableModel model = (DefaultTableModel) getModel();
            model.addRow(rowData);
        }
    }
    
    /**
     * Clears all data from the table
     */
    public void clearData() {
        allData.clear();
        totalRows = 0;
        
        if (paginationEnabled) {
            currentPage = 1;
            updatePagination();
            displayCurrentPage();
        } else {
            DefaultTableModel model = (DefaultTableModel) getModel();
            model.setRowCount(0);
        }
    }
    
    /**
     * Navigates to the first page
     */
    private void goToFirstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            updatePagination();
            displayCurrentPage();
        }
    }
    
    /**
     * Navigates to the previous page
     */
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
            displayCurrentPage();
        }
    }
    
    /**
     * Navigates to the next page
     */
    private void goToNextPage() {
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
            displayCurrentPage();
        }
    }
    
    /**
     * Navigates to the last page
     */
    private void goToLastPage() {
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        if (currentPage < totalPages) {
            currentPage = totalPages;
            updatePagination();
            displayCurrentPage();
        }
    }
    
    /**
     * Gets the current page size
     * @return The number of rows per page
     */
    public int getPageSize() {
        return pageSize;
    }
    
    /**
     * Sets the page size
     * @param pageSize The new page size
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        if (paginationEnabled) {
            currentPage = 1;
            updatePagination();
            displayCurrentPage();
        }
    }
    
    /**
     * Gets the current page number
     * @return The current page number
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Gets the total number of rows
     * @return The total number of rows
     */
    public int getTotalRows() {
        return totalRows;
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
            actionManager.applyTo(this);
        }
    }

    public void updateRowHeightFromSettings() {
        int fontSize = Main.settings.getSettingsState().fontSize;
        int buttonHeight = new JButton("Sample").getPreferredSize().height;
        int rowHeight = Math.max(fontSize + 20, buttonHeight + 10);
        setRowHeight(rowHeight);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (Main.settings != null) {
            SwingUtilities.invokeLater(this::updateRowHeightFromSettings);
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
        if (totalWidth <= 0)
            totalWidth = 800;

        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);

            // Handle checkbox column
            if (hasCheckbox && i == 0) {
                column.setPreferredWidth(50);
                column.setMaxWidth(50);
                continue;
            }

            // Handle Actions column (Allow Resizing)
            if (actionManager != null && i == getColumnCount() - 1) {
                int actionColumnWidth = (int) (totalWidth * 0.15);
                column.setPreferredWidth(actionColumnWidth);
                column.setMinWidth(actionColumnWidth);
                continue;
            }

            // Normal columns
            int width = (int) (totalWidth * columnProportions[i]);
            column.setPreferredWidth(width);
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
        applyColumnWidths();
    }
}
