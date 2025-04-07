package lyfjshs.gomis.view.students.schoolForm;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

public class SFExcelHighlighter extends JDialog {

    private JTextField nameBoxField;
    private JTextArea infoArea;
    private JTable table;
    private ExcelTableModel tableModel;
    private File selectedFile;
    private int highlightRow = -1;
    private int highlightCol = -1;
    private Connection connection;

    @FunctionalInterface
    public interface CellSelectedListener {
        void onCellSelected(int row, int col, String value);
    }

    private CellSelectedListener cellSelectedListener;

    public void addCellSelectedListener(CellSelectedListener listener) {
        this.cellSelectedListener = listener;
    }

    // Original constructor for direct Excel file import
    public SFExcelHighlighter(JFrame parent, Connection connection) {
        super(parent, "SF Excel Viewer with Cell Highlight", true);
        this.connection = connection;
        FlatLightLaf.setup();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[][][grow][100!]"));

        // File Import Button
        JButton importBtn = new JButton("Import SF Excel File");
        importBtn.addActionListener(e -> importFile());
        add(importBtn, "wrap");

        // Cell Input
        nameBoxField = new JTextField(10);
        JButton viewBtn = new JButton("Go to Cell");
        viewBtn.addActionListener(e -> highlightCell());
        add(new JLabel("Name Box (e.g. C20):"), "split 3");
        add(nameBoxField, "growx");
        add(viewBtn, "wrap");

        // JTable for Excel
        tableModel = new ExcelTableModel();
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new ExcelCellHighlighter());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);
        add(new JScrollPane(table), "grow, wrap");

        // Info Area
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        add(new JScrollPane(infoArea), "grow, span");
    }
    
    // New constructor that accepts table data directly from ImportSF
    public SFExcelHighlighter(JFrame parent, Connection connection, JTable sourceTable) {
        super(parent, "SF Excel Viewer with Cell Search", true);
        this.connection = connection;
        FlatLightLaf.setup();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[][grow][100!]"));

        // Cell Input
        nameBoxField = new JTextField(10);
        JButton viewBtn = new JButton("Search for Value");
        viewBtn.addActionListener(e -> searchValue());
        add(new JLabel("Search Value:"), "split 3");
        add(nameBoxField, "growx");
        add(viewBtn, "wrap");

        // Import data from source table to our table model
        tableModel = new ExcelTableModel();
        importDataFromTable(sourceTable);
        
        // JTable for Excel
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new ExcelCellHighlighter());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(25);
        add(new JScrollPane(table), "grow, wrap");

        // Info Area
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        add(new JScrollPane(infoArea), "grow, span");
        
        infoArea.setText("Data loaded from current form. Enter a search term and click 'Search for Value'.");
    }
    
    private void importDataFromTable(JTable sourceTable) {
        if (sourceTable == null || sourceTable.getModel().getRowCount() == 0) {
            return;
        }
        
        // Get data from the source table
        List<List<String>> data = new ArrayList<>();
        int maxColumns = sourceTable.getColumnCount();
        
        for (int row = 0; row < sourceTable.getModel().getRowCount(); row++) {
            List<String> rowData = new ArrayList<>();
            for (int col = 0; col < maxColumns; col++) {
                Object value = sourceTable.getModel().getValueAt(row, col);
                rowData.add(value != null ? value.toString() : "");
            }
            data.add(rowData);
        }
        
        // Set the data to our table model
        tableModel.setData(data, maxColumns);
    }
    
    private void searchValue() {
        String searchTerm = nameBoxField.getText().trim();
        if (searchTerm.isEmpty()) {
            infoArea.setText("Please enter a search term.");
            return;
        }
        
        boolean found = false;
        StringBuilder results = new StringBuilder();
        results.append("Search results for: ").append(searchTerm).append("\n\n");
        int matchCount = 0;
        
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                String cellValue = String.valueOf(tableModel.getValueAt(row, col)).toLowerCase();
                if (cellValue.contains(searchTerm.toLowerCase())) {
                    if (!found) {
                        // For the first match, highlight and scroll to it
                        highlightRow = row;
                        highlightCol = col;
                        table.repaint();
                        table.scrollRectToVisible(table.getCellRect(row, col, true));
                        found = true;
                    }
                    
                    matchCount++;
                    results.append("Match #").append(matchCount).append(": Row ").append(row + 1)
                           .append(", Column ").append(CellReference.convertNumToColString(col))
                           .append(" - Value: ").append(cellValue).append("\n");
                    
                    // Notify listener if one is registered
                    if (cellSelectedListener != null) {
                        cellSelectedListener.onCellSelected(row, col, cellValue);
                    }
                }
            }
        }
        
        if (found) {
            results.append("\nTotal matches: ").append(matchCount);
            infoArea.setText(results.toString());
        } else {
            infoArea.setText("No matches found for '" + searchTerm + "'");
        }
    }

    private void importFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select SF Excel File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            loadExcelToTable();
        }
    }

    private void loadExcelToTable() {
        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<List<String>> data = new ArrayList<>();

            int maxColumns = 0;
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowData.add(getCellValue(cell));
                }
                data.add(rowData);
                maxColumns = Math.max(maxColumns, rowData.size());
            }

            tableModel.setData(data, maxColumns);
            highlightRow = -1;
            highlightCol = -1;
            infoArea.setText("Excel file loaded. You may now enter a cell reference.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(getParent(), "Error loading file: " + ex.getMessage());
        }
    }

    private void highlightCell() {
        try {
            CellAddress address = new CellAddress(nameBoxField.getText().trim().toUpperCase());
            highlightRow = address.getRow();
            highlightCol = address.getColumn();
            table.repaint();

            table.scrollRectToVisible(table.getCellRect(highlightRow, highlightCol, true));

            // Get cell value
            String cellValue = (String) tableModel.getValueAt(highlightRow, highlightCol);

            // Build info
            StringBuilder sb = new StringBuilder();
            sb.append("📌 Cell ").append(nameBoxField.getText().toUpperCase())
                    .append(" Value: ").append(cellValue).append("\n\n");

            // Full row
            sb.append("📘 Row ").append(highlightRow + 1).append(": ");
            for (int col = 0; col < table.getColumnCount(); col++) {
                sb.append(tableModel.getValueAt(highlightRow, col)).append(" | ");
            }
            sb.append("\n\n");

            // Full column
            sb.append("📗 Column ").append(CellReference.convertNumToColString(highlightCol)).append(":\n");
            for (int row = 0; row < table.getRowCount(); row++) {
                sb.append(tableModel.getValueAt(row, highlightCol)).append("\n");
            }

            infoArea.setText(sb.toString());

            // Notify listener if one is registered
            if (cellSelectedListener != null) {
                cellSelectedListener.onCellSelected(
                    highlightRow,
                    highlightCol,
                    (String)tableModel.getValueAt(highlightRow, highlightCol)
                );
            }

        } catch (Exception ex) {
            infoArea.setText("❌ Invalid cell reference. Try something like C20.");
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> "=" + cell.getCellFormula();
            default -> "";
        };
    }

    // Table model to handle dynamic Excel data
    static class ExcelTableModel extends AbstractTableModel {
        private List<List<String>> data = new ArrayList<>();
        private int columns = 0;

        public void setData(List<List<String>> data, int maxColumns) {
            this.data = data;
            this.columns = maxColumns;
            fireTableStructureChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < data.size() && columnIndex < data.get(rowIndex).size()) {
                return data.get(rowIndex).get(columnIndex);
            }
            return "";
        }

        @Override
        public String getColumnName(int column) {
            return CellReference.convertNumToColString(column);
        }
    }

    // Renderer for cell highlight
    class ExcelCellHighlighter extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row == highlightRow && column == highlightCol) {
                c.setBackground(Color.YELLOW);
            } else if (row == highlightRow || column == highlightCol) {
                c.setBackground(new Color(255, 255, 200));
            } else {
                c.setBackground(Color.WHITE);
            }
            return c;
        }
    }
}
