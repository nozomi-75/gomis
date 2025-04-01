package lyfjshs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.AddressDAO;
import lyfjshs.gomis.Database.DAO.ContactDAO;
import lyfjshs.gomis.Database.DAO.GuardianDAO;
import lyfjshs.gomis.Database.DAO.ParentsDAO;
import lyfjshs.gomis.Database.DAO.RemarksDAO;
import lyfjshs.gomis.Database.DAO.SchoolFormDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.components.LoadingDialog;

public class SFtoDB extends JFrame {
    private DefaultTableModel model;
    private JTable table;
    private JTextField schoolNameField, semesterField, schoolIDField, schoolYearField;
    private JTextField sectionField, courseField, trackStrandField, gradeLevelField;
    private JTextField divisionField, regionField, districtField; 
    private JButton importButton, saveButton;
    private Connection connection;
    private LoadingDialog loadingDialog;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private SwingWorker<Void, ProgressUpdate> currentWorker;

    private StudentsDataDAO studentsDAO;
    private GuardianDAO guardianDAO;
    private ContactDAO contactDAO;
    private ParentsDAO parentsDAO;
    private AddressDAO addressDAO;
    private RemarksDAO remarksDAO;
    private SchoolFormDAO schoolFormDAO;

    public SFtoDB() {
        initializeFrame();
        initializeDatabase();
        initializeDAOs();
        setLocationRelativeTo(null);
    }

    private void initializeFrame() {
        setTitle("School Forms Data Import");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create panels
        JPanel topPanel = createMetadataPanel();
        JPanel centerPanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();

        // Add all panels to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Set the main panel as content pane
        setContentPane(mainPanel);
        
        loadingDialog = new LoadingDialog(this, "Processing");
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        // Create and configure buttons panel
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Create and configure progress panel
        JPanel progressPanel = createProgressPanel();
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        // Add panels to bottom section
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(progressPanel, BorderLayout.SOUTH);
        
        return bottomPanel;
    }

    private void initializeDatabase() {
        try {
            connection = DBConnection.getConnection();
            if (connection == null) {
                throw new SQLException("Failed to get database connection");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Failed to connect to database: " + e.getMessage() + 
                "\nMake sure MariaDB is running and configured correctly.", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeDAOs() {
        studentsDAO = new StudentsDataDAO(connection);
        guardianDAO = new GuardianDAO(connection);
        contactDAO = new ContactDAO(connection);
        parentsDAO = new ParentsDAO(connection);
        addressDAO = new AddressDAO(connection);
        remarksDAO = new RemarksDAO(connection);
        schoolFormDAO = new SchoolFormDAO(connection);
    }

    private JPanel createMetadataPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 4, 10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("School Information"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Create text fields
        schoolNameField = new JTextField(20);
        districtField = new JTextField(20);  // Added district field
        semesterField = new JTextField(20);
        schoolIDField = new JTextField(20);
        schoolYearField = new JTextField(20);
        sectionField = new JTextField(20);
        courseField = new JTextField(20);
        trackStrandField = new JTextField(20);
        gradeLevelField = new JTextField(20);
        divisionField = new JTextField(20);
        regionField = new JTextField(20);

        // Add labels and fields
        addLabelAndField(panel, "School Name:", schoolNameField);
        addLabelAndField(panel, "School ID:", schoolIDField);
        addLabelAndField(panel, "District:", districtField);  // Added district field
        addLabelAndField(panel, "School Year:", schoolYearField);
        addLabelAndField(panel, "Semester:", semesterField);
        addLabelAndField(panel, "Section:", sectionField);
        addLabelAndField(panel, "Grade Level:", gradeLevelField);
        addLabelAndField(panel, "Track/Strand:", trackStrandField);
        addLabelAndField(panel, "Course:", courseField);
        addLabelAndField(panel, "Division:", divisionField);
        addLabelAndField(panel, "Region:", regionField);

        return panel;
    }

    private void addLabelAndField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panel.add(label);
        field.setEditable(false);
        panel.add(field);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] columnNames = {
            "LRN", 
            "Last Name", "First Name", "Middle Name", // Split name into three columns
            "Sex", "Birth Date", "Age", "Religion",
            "House #/ Street/ Sitio/ Purok", "Barangay", "Municipality/City", "Province",
            "Father's Name", "Mother's Name", "Guardian Name", "Relationship",
            "Contact Number", "Learning Modality", "Remarks"
        };

        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Updated column widths to accommodate split name columns
        int[] columnWidths = {
            100, // LRN
            150, 150, 150, // Last, First, Middle name
            50, 100, 50, 100, // Sex, Birth Date, Age, Religion
            200, 150, 150, 150, // Address fields
            200, 200, 200, 100, // Parent/Guardian fields
            100, 100, 150 // Contact, Modality, Remarks
        };

        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        importButton = new JButton("Import SF Forms");
        saveButton = new JButton("Save to Database");
        
        // Configure button sizes and fonts
        Dimension buttonSize = new Dimension(200, 40);
        Font buttonFont = new Font(Font.DIALOG, Font.BOLD, 14);
        
        importButton.setPreferredSize(buttonSize);
        saveButton.setPreferredSize(buttonSize);
        importButton.setFont(buttonFont);
        saveButton.setFont(buttonFont);
        
        // Add action listeners
        importButton.addActionListener(e -> importFiles());
        saveButton.addActionListener(e -> saveToDatabase());
        
        // Add buttons with spacing
        panel.add(importButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(saveButton);
        
        return panel;
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 25));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(cancelButton, BorderLayout.EAST);
        
        return panel;
    }

    private void updateProgress(ProgressUpdate update) {
        loadingDialog.setStatus(update.message);
        loadingDialog.setProgress(false, update.progress);
        progressBar.setValue(update.progress);
        progressBar.setString(update.message);
    }

    private void importFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx, *.xls)", "xlsx", "xls"));
        fileChooser.setDialogTitle("Select School Form Files");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            model.setRowCount(0);
            
            cancelButton.setEnabled(true);
            currentWorker = new SwingWorker<Void, ProgressUpdate>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int totalFiles = selectedFiles.length;
                    int currentFile = 0;

                    for (File file : selectedFiles) {
                        if (isCancelled()) break;
                        
                        currentFile++;
                        publish(new ProgressUpdate(
                            "Processing file " + currentFile + " of " + totalFiles + ": " + file.getName(),
                            (currentFile * 100) / totalFiles
                        ));
                        
                        SchoolFormsReader reader = new SchoolFormsReader(file);
                        List<List<String>> records = reader.readSF1Data();
                        
                        SwingUtilities.invokeLater(() -> {
                            updateMetadataFields(reader);
                            for (List<String> record : records) {
                                model.addRow(record.toArray());
                            }
                        });
                        
                        // Add small delay to prevent UI freeze
                        Thread.sleep(100);
                    }
                    return null;
                }

                @Override
                protected void process(List<ProgressUpdate> chunks) {
                    updateProgress(chunks.get(chunks.size() - 1));
                }

                @Override
                protected void done() {
                    cancelButton.setEnabled(false);
                    try {
                        get();
                        if (!isCancelled()) {
                            showSuccessMessage("Import completed successfully!");
                        }
                    } catch (Exception e) {
                        showErrorMessage("Error during import: " + e.getMessage());
                    }
                    progressBar.setValue(0);
                    progressBar.setString("");
                }
            };

            cancelButton.addActionListener(e -> {
                if (currentWorker != null) {
                    currentWorker.cancel(true);
                    progressBar.setValue(0);
                    progressBar.setString("Operation cancelled");
                    cancelButton.setEnabled(false);
                }
            });

            currentWorker.execute();
        }
    }

    private void saveToDatabase() {
        if (model.getRowCount() == 0) {
            showErrorMessage("No data to save. Please import school forms first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Save " + model.getRowCount() + " records to database?",
            "Confirm Save",
            JOptionPane.YES_NO_OPTION);
            
        if (choice != JOptionPane.YES_OPTION) return;

        cancelButton.setEnabled(true);
        currentWorker = new SwingWorker<Void, ProgressUpdate>() {
            @Override
            protected Void doInBackground() throws Exception {
                publish(new ProgressUpdate("Starting database save...", 0));
                int sfId = saveSchoolForm();
                
                int totalRows = model.getRowCount();
                for (int i = 0; i < totalRows; i++) {
                    if (isCancelled()) break;
                    
                    publish(new ProgressUpdate(
                        "Saving student " + (i + 1) + " of " + totalRows,
                        (i + 1) * 100 / totalRows
                    ));
                    
                    saveStudentData(i, sfId);
                    Thread.sleep(50); // Small delay to prevent UI freeze
                }
                return null;
            }

            @Override
            protected void process(List<ProgressUpdate> chunks) {
                updateProgress(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                cancelButton.setEnabled(false);
                try {
                    get();
                    if (!isCancelled()) {
                        showSuccessMessage("Data saved successfully!");
                    }
                } catch (Exception e) {
                    showErrorMessage("Error saving to database: " + e.getMessage());
                }
                progressBar.setValue(0);
                progressBar.setString("");
            }
        };

        currentWorker.execute();
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void updateMetadataFields(SchoolFormsReader reader) {
        schoolNameField.setText(reader.readSchoolName());
        districtField.setText(reader.readDistrict());  // Added district
        semesterField.setText(reader.readSemester());
        schoolIDField.setText(reader.readSchoolID());
        schoolYearField.setText(reader.readSchoolYear());
        sectionField.setText(reader.readSection());
        courseField.setText(reader.readCourse());
        trackStrandField.setText(reader.readTrackAndStrand());
        gradeLevelField.setText(reader.readGradeLevel());
        divisionField.setText(reader.readDivision());
        regionField.setText(reader.readRegion());
    }

    private int saveSchoolForm() throws SQLException {
        String sql = "INSERT INTO SCHOOL_FORM (SF_SCHOOL_NAME, SF_SCHOOL_ID, SF_DISTRICT, SF_DIVISION, SF_REGION, " +
                     "SF_SEMESTER, SF_SCHOOL_YEAR, SF_GRADE_LEVEL, SF_SECTION, SF_TRACK_AND_STRAND, SF_COURSE) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, schoolNameField.getText());
            stmt.setString(2, schoolIDField.getText());
            stmt.setString(3, districtField.getText());  // Added district
            stmt.setString(4, divisionField.getText());
            stmt.setString(5, regionField.getText());
            stmt.setString(6, semesterField.getText());
            stmt.setString(7, schoolYearField.getText());
            stmt.setString(8, gradeLevelField.getText());
            stmt.setString(9, sectionField.getText());
            stmt.setString(10, trackStrandField.getText());
            stmt.setString(11, courseField.getText());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save school form data");
    }

    // Add utility method to parse names
    private Map<String, String> parseName(String fullName) {
        Map<String, String> nameParts = new HashMap<>();
        nameParts.put("lastName", "");
        nameParts.put("firstName", "");
        nameParts.put("middleName", "");

        if (fullName == null || fullName.trim().isEmpty()) {
            return nameParts;
        }

        // Split by comma first
        String[] parts = fullName.trim().split(",");
        if (parts.length > 0) {
            nameParts.put("lastName", parts[0].trim());
            
            if (parts.length > 1) {
                // Handle first and middle name
                String[] firstMiddle = parts[1].trim().split("\\s+", 2);
                nameParts.put("firstName", firstMiddle[0].trim());
                
                if (firstMiddle.length > 1) {
                    nameParts.put("middleName", firstMiddle[1].trim());
                }
            }
        }

        return nameParts;
    }

    // Add data cleaning methods
    private String cleanString(String value) {
        return value == null ? "" : value.trim();
    }

    // Modify the saveStudentData method
    private void saveStudentData(int rowIndex, int sfId) throws SQLException {
        // Get row data with proper cleaning
        Map<String, String> rowData = cleanRowData(rowIndex);
        
        connection.setAutoCommit(false);
        try {
            // Save address
            int addressId = saveAddress(rowData);
            
            // Save contact
            int contactId = saveContact(rowData.get("contactNumber"));
            
            // Save parents
            int parentId = saveParents(rowData);
            
            // Save guardian
            int guardianId = saveGuardian(rowData);
            
            // Save student
            saveStudent(rowData, sfId, addressId, contactId, parentId, guardianId);
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int saveAddress(Map<String, String> data) throws SQLException {
        String sql = "INSERT INTO ADDRESS (ADDRESS_HOUSE_NUMBER, ADDRESS_STREET_SUBDIVISION, " +
                     "ADDRESS_REGION, ADDRESS_PROVINCE, ADDRESS_MUNICIPALITY, ADDRESS_BARANGAY) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            String[] addressParts = data.get("address").split(",");
            stmt.setString(1, addressParts[0].trim());
            stmt.setString(2, addressParts.length > 1 ? addressParts[1].trim() : "");
            stmt.setString(3, regionField.getText());
            stmt.setString(4, data.get("province"));
            stmt.setString(5, data.get("municipality"));
            stmt.setString(6, data.get("barangay"));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save address");
    }

    private int saveContact(String contactNumber) throws SQLException {
        String sql = "INSERT INTO CONTACT (CONTACT_NUMBER) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, contactNumber);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save contact");
    }

    private int saveParents(Map<String, String> data) throws SQLException {
        String sql = "INSERT INTO PARENTS (FATHER_FIRSTNAME, FATHER_LASTNAME, FATHER_MIDDLENAME, " +
                     "MOTHER_FIRSTNAME, MOTHER_LASTNAME, MOTHER_MIDDLE_NAME) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, data.get("fatherFirstName"));
            stmt.setString(2, data.get("fatherLastName"));
            stmt.setString(3, data.get("fatherMiddleName"));
            stmt.setString(4, data.get("motherFirstName"));
            stmt.setString(5, data.get("motherLastName"));
            stmt.setString(6, data.get("motherMiddleName"));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save parents");
    }

    private int saveGuardian(Map<String, String> data) throws SQLException {
        String sql = "INSERT INTO GUARDIAN (GUARDIAN_FIRST_NAME, GUARDIAN_LASTNAME, GUARDIAN_MIDDLE_NAME, " +
                     "GUARDIAN_RELATIONSHIP) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, data.get("guardianFirstName"));
            stmt.setString(2, data.get("guardianLastName"));
            stmt.setString(3, data.get("guardianMiddleName"));
            stmt.setString(4, data.get("relationship"));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to save guardian");
    }

    private void saveStudent(Map<String, String> data, int sfId, int addressId, int contactId, 
                           int parentId, int guardianId) throws SQLException {
        String sql = "INSERT INTO STUDENT (SF_ID, ADDRESS_ID, CONTACT_ID, PARENT_ID, GUARDIAN_ID, " +
                     "STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, " +
                     "STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_AGE, STUDENT_RELIGION) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, STR_TO_DATE(?, '%m/%d/%Y'), ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sfId);
            stmt.setInt(2, addressId);
            stmt.setInt(3, contactId);
            stmt.setInt(4, parentId);
            stmt.setInt(5, guardianId);
            stmt.setString(6, data.get("lrn"));
            stmt.setString(7, data.get("lastName"));
            stmt.setString(8, data.get("firstName"));
            stmt.setString(9, data.get("middleName"));
            stmt.setString(10, data.get("sex"));
            stmt.setString(11, data.get("birthDate")); // Date will be converted by STR_TO_DATE
            stmt.setInt(12, Integer.parseInt(data.get("age")));
            stmt.setString(13, data.get("religion"));
            
            stmt.executeUpdate();
        }
    }

    // Add cleanup method
    private void cleanup() {
        if (connection != null) {
            DBConnection.releaseConnection(connection);
        }
    }

    private Map<String, String> cleanRowData(int rowIndex) {
        Map<String, String> rowData = new HashMap<>();
        
        // Get basic student info
        rowData.put("lrn", cleanString(model.getValueAt(rowIndex, 0).toString()));
        rowData.put("lastName", cleanString(model.getValueAt(rowIndex, 1).toString()));
        rowData.put("firstName", cleanString(model.getValueAt(rowIndex, 2).toString()));
        rowData.put("middleName", cleanString(model.getValueAt(rowIndex, 3).toString()));
        rowData.put("sex", cleanString(model.getValueAt(rowIndex, 4).toString()));
        rowData.put("birthDate", cleanString(model.getValueAt(rowIndex, 5).toString()));
        
        // Handle age value
        String ageStr = cleanString(model.getValueAt(rowIndex, 6).toString());
        try {
            int age = Integer.parseInt(ageStr.replaceAll("[^0-9]", ""));
            rowData.put("age", String.valueOf(age));
        } catch (NumberFormatException e) {
            rowData.put("age", "0");
        }
        
        rowData.put("religion", cleanString(model.getValueAt(rowIndex, 7).toString()));
        rowData.put("address", cleanString(model.getValueAt(rowIndex, 8).toString()));
        rowData.put("barangay", cleanString(model.getValueAt(rowIndex, 9).toString()));
        rowData.put("municipality", cleanString(model.getValueAt(rowIndex, 10).toString()));
        rowData.put("province", cleanString(model.getValueAt(rowIndex, 11).toString()));
        
        // Handle parent names
        Map<String, String> fatherName = parseName(cleanString(model.getValueAt(rowIndex, 12).toString()));
        Map<String, String> motherName = parseName(cleanString(model.getValueAt(rowIndex, 13).toString()));
        Map<String, String> guardianName = parseName(cleanString(model.getValueAt(rowIndex, 14).toString()));
        
        rowData.putAll(prefixKeys(fatherName, "father"));
        rowData.putAll(prefixKeys(motherName, "mother"));
        rowData.putAll(prefixKeys(guardianName, "guardian"));
        
        rowData.put("relationship", cleanString(model.getValueAt(rowIndex, 15).toString()));
        rowData.put("contactNumber", cleanString(model.getValueAt(rowIndex, 16).toString()));
        
        return rowData;
    }

    private Map<String, String> prefixKeys(Map<String, String> map, String prefix) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.put(prefix + entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatMacLightLaf.setup();    
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            SFtoDB frame = new SFtoDB();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Add window closing listener for cleanup
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    frame.cleanup();
                    DBConnection.closeAllConnections();
                }
            });
        });
    }
}