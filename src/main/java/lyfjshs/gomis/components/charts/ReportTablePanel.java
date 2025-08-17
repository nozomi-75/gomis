/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.charts;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.miginfocom.swing.MigLayout;

public class ReportTablePanel extends JPanel {
    private final JLabel summaryLabel;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton exportButton;

    public ReportTablePanel() {
        super(new MigLayout("fillx, insets 15", "[grow,fill]", "[][][grow][]"));
        summaryLabel = new JLabel("Showing all records");
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(summaryLabel, "wrap");

        String[] columnNames = { "Participant Name", "Category", "Violation Type", "Description", "Status", "Date" };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);
        add(tableScrollPane, "grow, h 200!, wrap");

        exportButton = new JButton("Export to Excel");
        add(exportButton, "right, wrap");

        exportButton.addActionListener(e -> exportTableToExcel());
    }

    public void updateTableData(java.util.List<Object[]> rows, String summaryText) {
        tableModel.setRowCount(0);
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        summaryLabel.setText(summaryText);
        table.getColumn("Status").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if ("Active".equalsIgnoreCase(status)) {
                    c.setForeground(new java.awt.Color(0, 128, 0));
                } else if ("Resolved".equalsIgnoreCase(status)) {
                    c.setForeground(java.awt.Color.GRAY);
                } else {
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        });
    }

    private void exportTableToExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
            }
            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                Sheet sheet = workbook.createSheet("Violations Report");
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    headerRow.createCell(i).setCellValue(tableModel.getColumnName(i));
                }
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        row.createCell(j).setCellValue(value != null ? value.toString() : "");
                    }
                }
                workbook.write(fileOut);
                JOptionPane.showMessageDialog(this, "Table exported successfully to " + fileToSave.getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error exporting table to Excel.", "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 