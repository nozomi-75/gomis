package lyfjshs.gomis.test;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SFtoDB extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SF1 Data Table");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);

            String[] columnNames = { "LRN", "Name", "Sex", "Birth Date", "Age", "Religion",
                    "House #/ Street/ Sitio/ Purok", "Barangay", "Municipality/City", "Province", "Father's Name",
                    "Mother's Name", "Guardian Name", "Relationship", "Contact Number", "Learning Modality",
                    "Remarks" };

            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files", "xlsx", "xls"));
            
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                
                SchoolFormsReader reader = new SchoolFormsReader(selectedFile);
                List<List<String>> records = reader.readSF1Data();
                for (List<String> record : records) {
                    model.addRow(record.toArray());
                }

                // Print metadata
                System.out.println("School Name: " + reader.readSchoolName());
                System.out.println("Semester: " + reader.readSemester());
                System.out.println("School ID: " + reader.readSchoolID());
                System.out.println("School Year: " + reader.readSchoolYear());
                System.out.println("Section: " + reader.readSection());
                System.out.println("Course: " + reader.readCourse());
                System.out.println("Track and Strand: " + reader.readTrackAndStrand());
                System.out.println("Grade Level: " + reader.readGradeLevel());
                System.out.println("Division: " + reader.readDivision());
                System.out.println("Region: " + reader.readRegion());

                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);
                frame.add(scrollPane, BorderLayout.CENTER);

                frame.setVisible(true);
            } else {
                frame.dispose();
            }
        });
    }
}

class SchoolFormsReader {
    private final File excelFile;

    public SchoolFormsReader(File excelFile) {
        this.excelFile = excelFile;
    }

    public List<List<String>> readSF1Data() {
        List<List<String>> records = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            int startRow = 19;
            int endMaleRow = findRow(sheet, startRow, "<=== TOTAL MALE");
            int startFemaleRow = endMaleRow + 1;
            int endFemaleRow = findRow(sheet, startFemaleRow, "<=== TOTAL FEMALE");
            
            int[][] columnRanges = {
                {0, 1}, {2, 9}, {10, 10}, {11, 13}, {14, 15}, {16, 19}, {20, 24},
                {25, 29}, {30, 31}, {32, 35}, {36, 40}, {41, 42}, {43, 46}, {47, 48},
                {49, 50}, {51, 52}, {53, 60}
            };
            
            String[] columnNames = {"LRN", "Name", "Sex", "Birth Date", "Age", "Religion", "Address",
                "Barangay", "Municipality", "Province", "Father's Name", "Mother's Name",
                "Guardian Name", "Relationship", "Contact Number", "Learning Modality", "Remarks"};
            
            extractData(sheet, startRow, endMaleRow, columnRanges, columnNames, records);
            extractData(sheet, startFemaleRow, endFemaleRow, columnRanges, columnNames, records);
        } catch (IOException e) {
            System.err.println("Error reading Excel file: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

    private void extractData(Sheet sheet, int startRow, int endRow, int[][] columnRanges, 
                            String[] columnNames, List<List<String>> records) {
        for (int rowIndex = startRow; rowIndex < endRow; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            List<String> record = new ArrayList<>();
            for (int i = 0; i < columnRanges.length; i++) {
                if (i == 0) {
                    record.add(getCellValueAsString(row.getCell(columnRanges[i][0])));
                } else {
                    record.add(getMergedCellValue(row, columnRanges[i]));
                }
            }
            records.add(record);
        }
    }

    private int findRow(Sheet sheet, int startRow, String targetValue) {
        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                for (int colIndex = 2; colIndex <= 61; colIndex++) {
                    Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    if (cell.toString().trim().equals(targetValue)) {
                        return rowIndex;
                    }
                }
            }
        }
        return sheet.getLastRowNum();
    }

    private String getMergedCellValue(Row row, int[] columnRange) {
        StringBuilder value = new StringBuilder();
        for (int col = columnRange[0]; col <= columnRange[1]; col++) {
            Cell cell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            value.append(cell.toString().trim()).append(" ");
        }
        return value.toString().trim();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        // Always return as string, preserving the cell's displayed value
        switch (cell.getCellType()) {
            case NUMERIC:
                // Use toString() to get the raw numeric value as displayed
                return cell.toString().trim();
            case STRING:
                return cell.getStringCellValue().trim();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue()).trim();
            case FORMULA:
                return cell.getCellFormula().trim();
            case BLANK:
                return "";
            default:
                return cell.toString().trim();
        }
    }

    // Enhanced helper method to search next row for values
    private String findValueNextToLabel(Sheet sheet, String label) {
        for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String cellValue = cell.toString().trim();
                if (cellValue.equalsIgnoreCase(label.trim())) {
                    System.out.println("Found label '" + label + "' at row " + rowIndex + ", col " + colIndex);
                    int startValueRow = rowIndex;
                    int startValueCol = colIndex + 1;
                    // Check if the label is in a merged region
                    CellRangeAddress labelMergedRegion = null;
                    for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                        CellRangeAddress region = sheet.getMergedRegion(i);
                        if (region.isInRange(rowIndex, colIndex)) {
                            labelMergedRegion = region;
                            startValueCol = labelMergedRegion.getLastColumn() + 1;
                            break;
                        }
                    }
                    // Search the same row first
                    for (int valueColIndex = startValueCol; valueColIndex < Math.min(startValueCol + 5, row.getLastCellNum()); valueColIndex++) {
                        Cell valueCell = row.getCell(valueColIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        CellRangeAddress valueMergedRegion = getMergedRegion(sheet, rowIndex, valueColIndex);
                        if (valueMergedRegion != null) {
                            valueCell = sheet.getRow(valueMergedRegion.getFirstRow())
                                    .getCell(valueMergedRegion.getFirstColumn(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        }
                        String value = valueCell.toString().trim();
                        if (!value.isEmpty()) {
                            System.out.println("Value for '" + label + "' at row " + rowIndex + ", col " + valueColIndex + ": " + value);
                            return value;
                        }
                    }
                    // If no value found in the same row, search the next row
                    Row nextRow = sheet.getRow(rowIndex + 1);
                    if (nextRow != null) {
                        for (int valueColIndex = startValueCol; valueColIndex < Math.min(startValueCol + 5, nextRow.getLastCellNum()); valueColIndex++) {
                            Cell valueCell = nextRow.getCell(valueColIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            CellRangeAddress valueMergedRegion = getMergedRegion(sheet, rowIndex + 1, valueColIndex);
                            if (valueMergedRegion != null) {
                                valueCell = sheet.getRow(valueMergedRegion.getFirstRow())
                                        .getCell(valueMergedRegion.getFirstColumn(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            }
                            String value = valueCell.toString().trim();
                            if (!value.isEmpty()) {
                                System.out.println("Value for '" + label + "' at row " + (rowIndex + 1) + ", col " + valueColIndex + ": " + value);
                                return value;
                            }
                        }
                    }
                    System.out.println("No non-empty value found after label '" + label + "' starting at row " + startValueRow + ", col " + startValueCol);
                    return "";
                }
            }
        }
        System.out.println("Label '" + label + "' not found in sheet.");
        return "";
    }

    // Helper method to get the merged region for a cell
    private CellRangeAddress getMergedRegion(Sheet sheet, int rowIndex, int colIndex) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowIndex, colIndex)) {
                return region;
            }
        }
        return null;
    }

    public String readSchoolName() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "School Name");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readSemester() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Semester");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readSchoolID() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "School ID");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readSchoolYear() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "School Year");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readSection() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Section");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readCourse() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Course (for TVL only)");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readTrackAndStrand() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Track and Strand");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readGradeLevel() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Grade Level");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readDivision() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Division");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String readRegion() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "Region");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}