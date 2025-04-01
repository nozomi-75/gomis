package lyfjshs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SchoolFormsReader {
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
            
            // Handle LRN (first column)
            record.add(getCellValueAsString(row.getCell(columnRanges[0][0])));
            
            // Handle Name - split into Last, First, Middle
            String fullName = getMergedCellValue(row, columnRanges[1]);
            String[] nameParts = fullName.split(",", 2);
            // Last name
            record.add(nameParts[0].trim());
            if (nameParts.length > 1) {
                // Split first and middle name
                String[] firstMiddle = nameParts[1].trim().split("\\s+", 2);
                record.add(firstMiddle[0].trim()); // First name
                record.add(firstMiddle.length > 1 ? firstMiddle[1].trim() : ""); // Middle name
            } else {
                record.add(""); // Empty first name
                record.add(""); // Empty middle name
            }
            
            // Add remaining fields starting from Sex (index 2 in columnRanges)
            for (int i = 2; i < columnRanges.length; i++) {
                record.add(getMergedCellValue(row, columnRanges[i]));
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
            String cellValue = getCellValueAsString(cell); // Use the updated getCellValueAsString method
            if (!cellValue.isEmpty()) {
                value.append(cellValue).append(" ");
            }
        }
        return value.toString().trim();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        try {
            // Try to get the formatted value first
            String formattedValue = cell.toString().trim();
            
            // For numeric cells, preserve the raw number format
            if (cell.getCellType() == CellType.NUMERIC) {
                // Check if it's a date
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("MM/dd/yyyy").format(cell.getDateCellValue());
                }
                // Get the raw number without scientific notation
                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    return String.format("%.0f", value); // Remove decimal for whole numbers
                } else {
                    return String.valueOf(value); // Keep decimals if present
                }
            }
            
            return formattedValue;
        } catch (Exception e) {
            // Fallback to basic string conversion
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
                    int startValueCol = colIndex + 1;
                    
                    // Check for merged regions containing the label
                    CellRangeAddress labelMergedRegion = getMergedRegion(sheet, rowIndex, colIndex);
                    if (labelMergedRegion != null) {
                        startValueCol = labelMergedRegion.getLastColumn() + 1;
                    }

                    // Search in the same row first
                    String sameRowValue = searchForValue(sheet, rowIndex, startValueCol);
                    if (!sameRowValue.isEmpty()) {
                        return sameRowValue;
                    }

                    // If no value found in the same row, search the next row
                    String nextRowValue = searchForValue(sheet, rowIndex + 1, startValueCol);
                    if (!nextRowValue.isEmpty()) {
                        return nextRowValue;
                    }
                }
            }
        }
        return "";
    }

    private String searchForValue(Sheet sheet, int rowIndex, int startCol) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) return "";

        for (int col = startCol; col < Math.min(startCol + 5, row.getLastCellNum()); col++) {
            Cell valueCell = row.getCell(col, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            
            // Check for merged regions
            CellRangeAddress mergedRegion = getMergedRegion(sheet, rowIndex, col);
            if (mergedRegion != null) {
                valueCell = sheet.getRow(mergedRegion.getFirstRow())
                    .getCell(mergedRegion.getFirstColumn(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            }

            // Convert cell value to string based on cell type
            String value = "";
            switch (valueCell.getCellType()) {
                case STRING:
                    value = valueCell.getStringCellValue();
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(valueCell)) {
                        value = new SimpleDateFormat("MM/dd/yyyy").format(valueCell.getDateCellValue());
                    } else {
                        // Format numeric values without scientific notation and unnecessary decimal places
                        double numericValue = valueCell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            value = String.format("%.0f", numericValue);
                        } else {
                            value = String.valueOf(numericValue);
                        }
                    }
                    break;
                case BOOLEAN:
                    value = String.valueOf(valueCell.getBooleanCellValue());
                    break;
                case FORMULA:
                    try {
                        value = String.valueOf(valueCell.getNumericCellValue());
                    } catch (Exception e) {
                        value = valueCell.getStringCellValue();
                    }
                    break;
                default:
                    value = valueCell.toString();
            }

            value = value.trim();
            if (!value.isEmpty()) {
                return value;
            }
        }
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

    public String readDistrict() {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            return findValueNextToLabel(sheet, "District");
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}