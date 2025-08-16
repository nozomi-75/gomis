package lyfjshs.gomis.components.table;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
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

	private int pageSize = 10; // Default page size
	private int currentPage = 1;
	private int totalRows = 0;
	private List<Object[]> allData = new ArrayList<>(); // Holds all data rows
	private boolean paginationEnabled = false;
	private int totalPages = 0;

	public GTable(Object[][] data, String[] columnNames, Class<?>[] columnTypes, boolean[] editableColumns,
			double[] columnWidths, int[] alignments, boolean includeCheckbox, TableActionManager actionManager) {

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
		this.columnProportions = columnWidths != null ? columnWidths.clone() : new double[0];
		this.columnAlignments = alignments != null ? alignments.clone() : new int[0];
		this.actionManager = actionManager;

		// Store all data for pagination if provided initially
		if (data != null) {
			for (Object[] row : data) {
				allData.add(row);
			}
			totalRows = allData.size();
		}

		configureTable();
		applyColumnWidths();
		applyColumnAlignments();

		// Add listener to update column widths on parent resize
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				applyColumnWidths();
			}
		});

		// Initial calculation of total pages
		recalculateTotalPages();
	}

	

	/**
	 * Enables or disables pagination.
	 * 
	 * @param enabled  Whether pagination should be enabled.
	 * @param pageSize Number of rows per page (if enabled).
	 */
	public void setPaginationEnabled(boolean enabled, int pageSize) {
		this.paginationEnabled = enabled;
		if (enabled) {
			this.pageSize = Math.max(1, pageSize); // Ensure page size is at least 1
		}
		// Reset to page 1 if pagination state or page size changes significantly
		this.currentPage = 1;
		recalculateTotalPages();
		displayCurrentPage(); // Refresh table view
	}

	/**
	 * Recalculates the total number of pages based on totalRows and pageSize.
	 */
	private void recalculateTotalPages() {
		if (pageSize > 0 && totalRows > 0) {
			totalPages = (int) Math.ceil((double) totalRows / pageSize);
		} else {
			totalPages = 0; // Or 1 if you prefer to always show 1 page even if empty
		}
		if (totalRows == 0)
			totalPages = 0; // Explicitly handle no data case for total pages
	}

	/**
	 * Displays the current page of data in the table.
	 */
	private void displayCurrentPage() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setRowCount(0); // Clear existing rows

		if (!paginationEnabled || allData.isEmpty()) {
			// If pagination is disabled or no data, show all data (or nothing if empty)
			for (Object[] row : allData) {
				model.addRow(row);
			}
			return;
		}

		int startIndex = (currentPage - 1) * pageSize;
		int endIndex = Math.min(startIndex + pageSize, totalRows);

		if (startIndex < totalRows) { // Ensure startIndex is valid
			for (int i = startIndex; i < endIndex; i++) {
				model.addRow(allData.get(i));
			}
		}
	}

	/**
	 * Displays all data without pagination constraints. This is typically called if
	 * pagination is disabled.
	 */
	private void displayAllData() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.setRowCount(0);
		for (Object[] row : allData) {
			model.addRow(row);
		}
	}

	/**
	 * Sets the data for the table. This replaces all existing data.
	 * 
	 * @param data The new data to display (array of row arrays).
	 */
	public void setData(Object[][] data) {
		allData.clear();
		if (data != null) {
			for (Object[] row : data) {
				allData.add(row.clone()); // Clone to avoid external modification issues
			}
		}
		totalRows = allData.size();
		currentPage = 1; // Reset to first page
		recalculateTotalPages();

		if (paginationEnabled) {
			displayCurrentPage();
		} else {
			displayAllData();
		}
	}

	/**
	 * Adds a single row to the table's underlying data.
	 * 
	 * @param rowData The data for the new row.
	 */
	public void addRow(Object[] rowData) {
		allData.add(rowData.clone());
		totalRows = allData.size();
		recalculateTotalPages();

		// If pagination is enabled, and the new row would be on the current page
		// or if it causes the current page to now have data, refresh.
		// Or simply, if the current page is the last page and it's not full.
		// For simplicity, we can just call displayCurrentPage, or for more
		// optimization:
		if (paginationEnabled) {
			// If the added row makes the current page valid or is on the current page
			int startIndex = (currentPage - 1) * pageSize;
			if (totalRows > startIndex) { // Check if current page is still valid
				displayCurrentPage(); // Could optimize to just add if on current page & not full
			} else {
				// If the current page became invalid (e.g., was empty, now has data on page 1)
				// It's generally safer to just redisplay, or go to the page of the new item
				goToPage(totalPages); // Go to the last page where the new item is
			}
		} else {
			DefaultTableModel model = (DefaultTableModel) getModel();
			model.addRow(rowData);
		}
	}

	/**
	 * Clears all data from the table.
	 */
	public void clearData() {
		allData.clear();
		totalRows = 0;
		currentPage = 1;
		recalculateTotalPages();
		displayCurrentPage(); // Clears the table view
	}

	/**
	 * Navigates to the first page.
	 */
	public void goToFirstPage() {
		if (currentPage > 1) {
			currentPage = 1;
			displayCurrentPage();
		}
	}

	/**
	 * Navigates to the previous page.
	 */
	public void goToPreviousPage() {
		if (currentPage > 1) {
			currentPage--;
			displayCurrentPage();
		}
	}

	/**
	 * Navigates to the next page.
	 */
	public void goToNextPage() {
		recalculateTotalPages(); // Ensure totalPages is up-to-date
		if (currentPage < totalPages) {
			currentPage++;
			displayCurrentPage();
		}
	}

	/**
	 * Navigates to the last page.
	 */
	public void goToLastPage() {
		recalculateTotalPages(); // Ensure totalPages is up-to-date
		if (totalPages > 0 && currentPage < totalPages) {
			currentPage = totalPages;
			displayCurrentPage();
		} else if (totalPages == 0) {
			currentPage = 1; // Or 0 if you represent no pages as 0
			displayCurrentPage();
		}
	}

	/**
	 * Navigates to a specific page number.
	 * 
	 * @param pageNumber The page number to go to (1-indexed).
	 */
	public void goToPage(int pageNumber) {
		recalculateTotalPages();
		if (pageNumber >= 1 && pageNumber <= totalPages) {
			currentPage = pageNumber;
			displayCurrentPage();
		} else if (pageNumber < 1 && totalPages > 0) {
			currentPage = 1;
			displayCurrentPage();
		} else if (pageNumber > totalPages && totalPages > 0) {
			currentPage = totalPages;
			displayCurrentPage();
		}
		// If totalPages is 0, currentPage will remain 1 (or as set), and
		// displayCurrentPage will show empty.
	}

	// --- Getters for Pagination State ---
	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		if (pageSize > 0) {
			this.pageSize = pageSize;
			this.currentPage = 1; // Reset to page 1 when page size changes
			recalculateTotalPages();
			if (paginationEnabled) {
				displayCurrentPage();
			}
		}
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public int getTotalPages() {
		recalculateTotalPages(); // Ensure it's fresh before returning
		return totalPages;
	}

	public boolean isPaginationEnabled() {
		return paginationEnabled;
	}

	// --- Core Table Configuration (largely unchanged) ---
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
		setShowVerticalLines(false); // Personal preference, can be true
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Or MULTIPLE_INTERVAL_SELECTION

		FlatTableHeader header = new FlatTableHeader();
		header.setColumnModel(getColumnModel());
		setTableHeader(header);

		putClientProperty(FlatClientProperties.STYLE,
				"showHorizontalLines:true;" + "intercellSpacing:0,1;" + "cellFocusColor:$TableHeader.hoverBackground;" + // Or
																															// a
																															// custom
																															// color
						"selectionBackground:$Table.selectionBackground;"
						+ "selectionForeground:$Table.selectionForeground");

		getTableHeader().putClientProperty(FlatClientProperties.STYLE, "height:25;" + 
				"hoverBackground:$TableHeader.hoverBackground;" + "pressedBackground:$TableHeader.pressedBackground;"
				+ "separatorColor:$TableHeader.separatorColor;" + "font:bold");

		if (hasCheckbox) {
			if (getColumnCount() > 0) { // Ensure columns exist
				JCheckBox checkBox = new JCheckBox();
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				TableColumn checkColumn = getColumnModel().getColumn(0);
				checkColumn.setCellRenderer(new BooleanRenderer()); // Assumes BooleanRenderer exists
				checkColumn.setCellEditor(new DefaultCellEditor(checkBox));
				checkColumn.setMaxWidth(50); // Adjust as needed
				checkColumn.setResizable(false);
			}
		}
		if (actionManager != null) {
			actionManager.applyTo(this); // Assumes applyTo correctly finds the action column
		}
	}

	public void setColumnWidths(double[] widths) {
		if (widths == null || widths.length != getColumnCount()) {
			// Allow null for auto-sizing or throw error if counts don't match and not null
			if (widths != null) {
				System.err.println(
						"Warning: Column widths array length does not match column count. Widths not applied.");
				// throw new IllegalArgumentException("Width array must match column count");
			}
			this.columnProportions = null; // Indicate no proportional widths
			return;
		}
		this.columnProportions = widths.clone();
		applyColumnWidths();
	}

	private void applyColumnWidths() {
		if (columnProportions == null || columnProportions.length == 0)
			return; // No proportional widths to apply

		SwingUtilities.invokeLater(() -> { // Defer to ensure parent is sized
			if (getParent() == null || getParent().getWidth() <= 0)
				return;
			int totalWidth = getParent().getWidth();
			// Subtract fixed width columns from totalWidth if they are part of
			// columnProportions logic
			int fixedWidthUsed = 0;
			if (hasCheckbox && columnProportions.length > 0) { // Checkbox is fixed
				// If checkbox has its own proportion, it's handled by loop.
				// If it's always fixed outside proportions, subtract its width.
				// Current GTable sets MaxWidth, so it's "fixed" but let's assume proportions
				// are for remaining.
			}

			TableColumnModel columnModel = getColumnModel();
			for (int i = 0; i < columnModel.getColumnCount() && i < columnProportions.length; i++) {
				TableColumn column = columnModel.getColumn(i);

				if (hasCheckbox && i == 0) { // Checkbox column is special
					column.setPreferredWidth(50); // Already set in configure, but good to be explicit
					column.setMaxWidth(50);
					continue;
				}

				// Handle Actions column (last column, often)
				if (actionManager != null && i == getColumnCount() - 1) {
					// Let action manager decide, or use proportion.
					// Current DefaultTableActionManager sets preferred width.
					// If columnProportions includes a value for action column, it will be used
					// here.
					// For more robust, check if actionManager.getPreferredWidth() > 0 or similar.
				}

				int width = (int) (totalWidth * columnProportions[i]);
				column.setPreferredWidth(width);
			}
		});
	}

	public void setColumnAlignments(int[] alignments) {
		if (alignments == null || alignments.length != getColumnCount()) {
			if (alignments != null) {
				System.err.println(
						"Warning: Column alignments array length does not match column count. Alignments not applied.");
			}
			this.columnAlignments = new int[0]; // Reset or clear
			return;
		}
		this.columnAlignments = alignments.clone();
		applyColumnAlignments();
	}

	private void applyColumnAlignments() {
		if (columnAlignments == null || columnAlignments.length == 0)
			return;

		TableColumnModel columnModel = getColumnModel();
		// Ensure we don't go out of bounds for columnAlignments
		for (int i = 0; i < columnModel.getColumnCount() && i < columnAlignments.length; i++) {
			// Skip checkbox column if it has its own renderer that handles alignment
			// (BooleanRenderer centers it)
			if (hasCheckbox && i == 0) {
				continue;
			}
			// Skip action column if ActionColumnRenderer handles its own internal alignment
			if (actionManager != null && i == getColumnCount() - 1) {
				// The buttons within the ActionColumnPanel are centered by its FlowLayout.
				// Setting alignment on the column itself might not be what's desired.
				continue;
			}

			DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) getCellRenderer(0, i);
			if (renderer == null) { // Possible if no default renderer set for type
				renderer = new DefaultTableCellRenderer();
			}
			renderer.setHorizontalAlignment(columnAlignments[i]);
			columnModel.getColumn(i).setCellRenderer(renderer);
		}
	}

    public void updateRowHeightFromSettings() {
        int fontSize = Main.settings.getSettingsState().fontSize;
        int buttonHeight = new JButton("Sample").getPreferredSize().height;
        int rowHeight = Math.max(fontSize + 12, buttonHeight + 6); // Reduced padding from 20/10 to 12/6
        setRowHeight(rowHeight);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (Main.settings != null) {
            SwingUtilities.invokeLater(this::updateRowHeightFromSettings);
        }
    }
    
	@Override
	public void doLayout() {
		super.doLayout();
		// Applying column widths on doLayout can be expensive if it happens too often.
		// ComponentResized is generally better.
		// However, if initial sizing is an issue, a one-time call after table is
		// visible might be needed.
		// applyColumnWidths(); // Consider if this is truly needed here or only on
		// resize.
	}
}	