package lyfjshs.gomis.view.students.schoolForm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.AddressDAO;
import lyfjshs.gomis.Database.DAO.ContactDAO;
import lyfjshs.gomis.Database.DAO.GuardianDAO;
import lyfjshs.gomis.Database.DAO.ParentsDAO;
import lyfjshs.gomis.Database.DAO.RemarksDAO;
import lyfjshs.gomis.Database.DAO.SchoolFormDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.LoadingDialog;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.utils.SchoolFormsReader;
import lyfjshs.gomis.utils.TableEditHistory;
import lyfjshs.gomis.utils.TableEditHistory.TableEdit;
import raven.modal.Toast;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

/**
 * ImportSF: All methods in this class are used by the UI or internal logic.
 * Any warnings about unused methods can be safely ignored, as they are invoked
 * via event listeners, SwingWorkers, or UI actions.
 */
public class ImportSF extends Form {
    private static final Logger logger = LogManager.getLogger(ImportSF.class);
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
    private JFrame parentFrame;

    private StudentsDataDAO studentsDAO;
    private GuardianDAO guardianDAO;
    private ContactDAO contactDAO;
    private ParentsDAO parentsDAO;
    private AddressDAO addressDAO;
    private RemarksDAO remarksDAO;
    private SchoolFormDAO schoolFormDAO;

    // Add these fields
    private TableEditHistory editHistory;
    private Object lastCellValue;

    public ImportSF(Connection conn) {
        this.connection = conn;
        this.parentFrame = Main.gFrame;
        editHistory = new TableEditHistory();
        initializePanel();
        initializeDAOs();
        setupKeyBindings();
              // Add resize listener to preserve and restore state
              this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    refreshStateOnResize();
                }
            });
    }

    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel topPanel = createMetadataPanel();
        JPanel centerPanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        loadingDialog = new LoadingDialog(parentFrame, "Processing");
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        JPanel progressPanel = createProgressPanel();
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(progressPanel, BorderLayout.SOUTH);
        return bottomPanel;
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
        panel.add(field);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        String[] columnNames = {
            "LRN", "Last Name", "First Name", "Name Extension", "Middle Name", // Updated column order
            "Sex", "Birth Date", "Age", "Religion",
            "House #/ Street/ Sitio/ Purok", "Barangay", "Municipality/City", "Province",
            "Father's Name", "Mother's Name", "Guardian Name", "Relationship",
            "Contact Number", "Learning Modality", "Remarks"
        };
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Make all cells editable
            }
        };
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (row >= 0 && column >= 0) {
                    Object newValue = model.getValueAt(row, column);
                    table.repaint();
                }
            }
        });
        int[] columnWidths = {
            100, // LRN
            150, // Last Name
            150, // First Name
            80,  // Name Extension (reduced width since it's shorter)
            150, // Middle name
            50,  // Sex
            100, // Birth Date
            50,  // Age
            100, // Religion
            200, // House #/ Street/ Sitio/ Purok
            150, // Barangay
            150, // Municipality/City
            150, // Province
            200, // Father's Name
            200, // Mother's Name
            200, // Guardian Name
            100, // Relationship
            100, // Contact Number
            120, // Learning Modality
            150  // Remarks
        };
        for (int i = 0; i < columnWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Modify the table listener to track changes
        table.addPropertyChangeListener("tableCellEditor", evt -> {
            if (table.isEditing()) {
                // Store the value before editing
                int row = table.getEditingRow();
                int col = table.getEditingColumn();
                // Add null and bounds checking
                if (row >= 0 && col >= 0 && row < model.getRowCount() && col < model.getColumnCount()) {
                    lastCellValue = model.getValueAt(row, col);
                }
            }
        });

        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                // Add null and bounds checking
                if (row >= 0 && column >= 0 && row < model.getRowCount() && column < model.getColumnCount()) {
                    Object newValue = model.getValueAt(row, column);
                    if (lastCellValue != null && !lastCellValue.equals(newValue)) {
                        // Add edit to history
                        editHistory.pushEdit(new TableEdit(row, column, lastCellValue, newValue));
                        lastCellValue = null;
                    }
                    table.repaint();
                }
            }
        });

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        saveButton = new JButton("Save to Database");
        importButton = new JButton("Import SF Forms");
        
        Dimension buttonSize = new Dimension(200, 40);
        Font buttonFont = new Font(Font.DIALOG, Font.BOLD, 14);
        
        saveButton.setPreferredSize(buttonSize);
        importButton.setPreferredSize(buttonSize);
        
        importButton.setFont(buttonFont);
        saveButton.setFont(buttonFont);

        importButton.addActionListener(e -> importFiles());
        saveButton.addActionListener(e -> saveToDatabase());
        
        panel.add(importButton);
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
            if (selectedFiles.length == 0) {
                showToast(Toast.Type.WARNING, "No files selected", ToastLocation.BOTTOM_CENTER);
                return;
            }
            
            // Validate file extensions
            for (File file : selectedFiles) {
                String fileName = file.getName().toLowerCase();
                if (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls")) {
                    showToast(Toast.Type.ERROR, "Invalid file format: " + fileName + ". Only Excel files (.xlsx, .xls) are supported.", ToastLocation.BOTTOM_CENTER);
                    return;
                }
            }
            
            model.setRowCount(0);
            cancelButton.setEnabled(true);
            currentWorker = new SwingWorker<Void, ProgressUpdate>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int totalFiles = selectedFiles.length;
                    int currentFile = 0;
                    for (File file : selectedFiles) {
                        if (isCancelled()) break;
                        publish(new ProgressUpdate(
                            "Processing file " + currentFile + " of " + totalFiles + ": " + file.getName(),
                            (currentFile * 100) / totalFiles
                        ));
                        
                        try {
                            SchoolFormsReader reader = new SchoolFormsReader(file, connection);
                            List<List<String>> records = reader.readSF1Data();
                            if (records.isEmpty()) {
                                throw new Exception("No data found in file: " + file.getName());
                            }
                            
                            SwingUtilities.invokeLater(() -> {
                                updateMetadataFields(reader);
                                for (List<String> record : records) {
                                    model.addRow(record.toArray());
                                }
                            });
                        } catch (Exception ex) {
                            // Log the error but continue with next file
                            System.err.println("Error processing file " + file.getName() + ": " + ex.getMessage());
                            ex.printStackTrace();
                            publish(new ProgressUpdate(
                                "Error processing file: " + file.getName() + " - " + ex.getMessage(),
                                (currentFile * 100) / totalFiles
                            ));
                            Thread.sleep(1000); // Show the error message for a moment
                        }
                        
                        Thread.sleep(100);
                        currentFile++;
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
                            if (model.getRowCount() > 0) {
                                showToast(Toast.Type.SUCCESS, "Import completed successfully! Loaded " + model.getRowCount() + " records.", ToastLocation.BOTTOM_CENTER);
                            } else {
                                showToast(Toast.Type.WARNING, "No records were imported. Please check the files and try again.", ToastLocation.BOTTOM_CENTER);
                            }
                        }
                    } catch (Exception e) {
                        showToast(Toast.Type.ERROR, "Error during import: " + e.getMessage(), ToastLocation.BOTTOM_CENTER);
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
                    showToast(Toast.Type.INFO, "Import operation cancelled", ToastLocation.BOTTOM_CENTER);
                }
            });
            currentWorker.execute();
        }
    }

    /**
     * Shows a toast notification with the specified message
     * 
     * @param type     The type of toast (SUCCESS, ERROR, WARNING, INFO)
     * @param message  The message to display
     * @param location The location where the toast should appear
     */
    private void showToast(Toast.Type type, String message, ToastLocation location) {
        // Create toast option
        ToastOption toastOption = Toast.createOption();
        
        // Set layout options
        toastOption.getLayoutOption()
            .setMargin(0, 0, 50, 0)
            .setDirection(ToastDirection.TOP_TO_BOTTOM);
        
        // Show toast
        Toast.show(this, type, message, location, toastOption);
    }

    private void showErrorMessage(String message) {
        showToast(Toast.Type.ERROR, message, ToastLocation.BOTTOM_CENTER);
    }

    private void saveToDatabase() {
        if (model.getRowCount() <= 0) {
            showToast(Toast.Type.WARNING, "No records to save. Please import data first.", ToastLocation.BOTTOM_CENTER);
            return;
        }

        StringBuilder message = new StringBuilder("Do you want to save the following data to the database?\n\n");
        message.append("School Name: ").append(schoolNameField.getText()).append("\n");
        message.append("School ID: ").append(schoolIDField.getText()).append("\n");
        message.append("District: ").append(districtField.getText()).append("\n");
        message.append("School Year: ").append(schoolYearField.getText()).append("\n");
        message.append("Semester: ").append(semesterField.getText()).append("\n");
        message.append("Section: ").append(sectionField.getText()).append("\n");
        message.append("\nStudent Records: ").append(model.getRowCount()).append(" total\n");

        // Use JOptionPane for confirmation
        int choice = JOptionPane.showConfirmDialog(
            this, 
            message.toString(), 
            "Confirm Save", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            proceedWithSave();
        } else {
            showInfoMessage("Save operation cancelled by user.");
        }
    }

    private void proceedWithSave() {
        progressBar.setValue(0);
        cancelButton.setEnabled(true);
        currentWorker = new SwingWorker<Void, ProgressUpdate>() {
            private int successCount = 0;
            private int errorCount = 0;
            private List<String> errorMessages = new ArrayList<>();
            
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish(new ProgressUpdate("Creating school form record...", 0));
                    
                    // Create school form first and ensure it succeeds
                    int sfId = saveSchoolForm();
                    if (sfId <= 0) {
                        throw new SQLException("Failed to create school form record");
                    }
                    
                    // Create student records
                    int totalRows = model.getRowCount();
                    for (int i = 0; i < totalRows; i++) {
                        if (isCancelled()) break;
                        
                        publish(new ProgressUpdate(
                            "Saving student " + (i + 1) + " of " + totalRows,
                            (i + 1) * 100 / totalRows
                        ));
                        
                        try {
                            saveStudentData(i, sfId);
                            successCount++;
                        } catch (SQLException e) {
                            // Log the error but continue with next student
                            errorCount++;
                            String errorMsg = "Error saving student at row " + (i + 1) + ": " + e.getMessage();
                            System.err.println(errorMsg);
                            errorMessages.add(errorMsg);
                        }
                        Thread.sleep(50);
                    }
                } catch (SQLException e) {
                    throw new Exception("Database error: " + e.getMessage(), e);
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
                        StringBuilder resultMessage = new StringBuilder();
                        resultMessage.append("Successfully saved ").append(successCount).append(" students");
                        
                        if (errorCount > 0) {
                            resultMessage.append(". ").append(errorCount).append(" students failed to save.");
                            
                            // Show detailed errors in a separate dialog if there were any
                            if (!errorMessages.isEmpty()) {
                                StringBuilder errorDetails = new StringBuilder("The following errors occurred:\n\n");
                                for (int i = 0; i < Math.min(errorMessages.size(), 5); i++) {
                                    errorDetails.append("- ").append(errorMessages.get(i)).append("\n");
                                }
                                if (errorMessages.size() > 5) {
                                    errorDetails.append("- And ").append(errorMessages.size() - 5).append(" more errors...");
                                }
                                
                                // Show error details using JOptionPane
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(
                                        parentFrame, 
                                        errorDetails.toString(), 
                                        "Save Errors", 
                                        JOptionPane.ERROR_MESSAGE
                                    );
                                });
                            }
                            
                            showToast(Toast.Type.WARNING, resultMessage.toString(), ToastLocation.BOTTOM_CENTER);
                        } else {
                            showToast(Toast.Type.SUCCESS, resultMessage.toString(), ToastLocation.BOTTOM_CENTER);
                        }
                    }
                } catch (Exception e) {
                    showToast(Toast.Type.ERROR, "Error saving to database: " + e.getMessage(), ToastLocation.BOTTOM_CENTER);
                }
                progressBar.setValue(0);
                progressBar.setString("");
            }
        };
        currentWorker.execute();
    }

    private void saveStudentData(int rowIndex, int sfId) throws SQLException {
        if (sfId <= 0) {
            throw new SQLException("Invalid school form ID");
        }

        Map<String, String> rowData = cleanRowData(rowIndex);
        
        try {
            connection.setAutoCommit(false); // Start transaction
            
            try {
                // Create all related records
                Address address = new Address(
                    0,
                    rowData.get("address").split(",")[0].trim(),
                    rowData.get("address").split(",").length > 1 ? rowData.get("address").split(",")[1].trim() : "",
                    regionField.getText(),
                    rowData.get("province"),
                    rowData.get("municipality"),
                    rowData.get("barangay"),
                    ""  // zip code
                );
                int addressId = addressDAO.createAddress(address);
                
                Contact contact = new Contact(0, rowData.get("contactNumber"));
                int contactId = contactDAO.createContact(contact);
                
                Parents parents = new Parents(
                    0,
                    rowData.get("fatherFirstName"),
                    rowData.get("fatherLastName"),
                    rowData.get("fatherMiddleName"),
                    "",  // father contact
                    rowData.get("motherFirstName"),
                    rowData.get("motherLastName"),
                    rowData.get("motherMiddleName"),
                    ""  // mother contact
                );
                int parentId = parentsDAO.createParents(parents);
                
                Guardian guardian = new Guardian(
                    0,
                    rowData.get("guardianLastName"),
                    rowData.get("guardianFirstName"),
                    rowData.get("guardianMiddleName"),
                    rowData.get("relationship"),
                    ""  // guardian contact
                );
                int guardianId = guardianDAO.createGuardian(guardian);

                // Create Student with the correct school form ID
                Student student = new Student(
                    0, // studentUid (will be generated)
                    parentId,
                    guardianId,
                    addressId,
                    contactId,
                    sectionField.getText(), // schoolSection
                    rowData.get("lrn"), // studentLrn
                    rowData.get("lastName"), // studentLastname
                    rowData.get("firstName"), // studentFirstname
                    rowData.get("middleName"), // studentMiddlename
                    rowData.get("sex"), // studentSex
                    null, // studentBirthdate - No birth date parsing implemented
                    rowData.get("mothertongue"), // studentMothertongue
                    Integer.parseInt(rowData.get("age")), // studentAge
                    "", // studentIpType
                    rowData.get("religion"), // studentReligion
                    address, // address object
                    contact, // contact object
                    parents, // parents object
                    guardian, // guardian object
                    new lyfjshs.gomis.Database.entity.SchoolForm() // Create empty SchoolForm and set ID later
                );
                
                // Set SchoolForm ID directly
                student.setSF_ID(sfId);
                
                try {
                    // Save the student record
                    studentsDAO.createStudentData(student);
                    connection.commit(); // Commit transaction
                } catch (SQLException e) {
                    // Check if this is a duplicate LRN error
                    if (e.getMessage().contains("already exists")) {
                        throw new SQLException("Student with LRN " + rowData.get("lrn") + " already exists in the database");
                    }
                    throw e; // Re-throw other SQL exceptions
                }
                
            } catch (SQLException e) {
                connection.rollback(); // Rollback on error
                throw e; // Re-throw the exception
            } finally {
                connection.setAutoCommit(true); // Reset auto-commit
            }
            
        } catch (SQLException e) {
            throw new SQLException("Error saving student data: " + e.getMessage(), e);
        }
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
        lyfjshs.gomis.Database.entity.SchoolForm schoolForm = new lyfjshs.gomis.Database.entity.SchoolForm(
            0, // ID will be generated
            schoolNameField.getText(),
            schoolIDField.getText(),
            districtField.getText(),
            divisionField.getText(),
            regionField.getText(),
            semesterField.getText(),
            schoolYearField.getText(),
            gradeLevelField.getText(),
            sectionField.getText(),
            trackStrandField.getText(),
            courseField.getText()
        );

        return schoolFormDAO.createSchoolForm(schoolForm);
    }

    private Map<String, String> parseName(String fullName) {
        Map<String, String> nameParts = new HashMap<>();
        nameParts.put("lastName", "");
        nameParts.put("firstName", "");
        nameParts.put("nameExtension", "");
        nameParts.put("middleName", "");

        if (fullName == null || fullName.trim().isEmpty()) {
            return nameParts;
        }

        String[] parts = fullName.trim().split(",");
        if (parts.length > 0) {
            String lastNamePart = parts[0].trim();
            String lastName = lastNamePart;
            String suffix = "";
            
            // Enhanced suffix detection with standardized patterns
            String[] suffixPatterns = {"JR\\.", "SR\\.", "II", "III", "IV", "V", "VI"};
            for (String pattern : suffixPatterns) {
                if (lastName.toUpperCase().endsWith(" " + pattern.toUpperCase())) {
                    int lastSpaceIndex = lastName.lastIndexOf(" ");
                    suffix = lastName.substring(lastSpaceIndex + 1).trim();
                    lastName = lastName.substring(0, lastSpaceIndex).trim();
                    break;
                }
            }
            
            nameParts.put("lastName", lastName);
            nameParts.put("nameExtension", suffix);
            
            if (parts.length > 1) {
                String[] remainingParts = parts[1].trim().split("\\s+");
                StringBuilder firstName = new StringBuilder();
                
                // Handle remaining parts
                if (remainingParts.length > 0) {
                    // Last part is middle name
                    nameParts.put("middleName", remainingParts[remainingParts.length - 1]);
                    
                    // Everything else is first name
                    for (int i = 0; i < remainingParts.length - 1; i++) {
                        if (i > 0) firstName.append(" ");
                        firstName.append(remainingParts[i]);
                    }
                    nameParts.put("firstName", firstName.toString().trim());
                }
            }
        }
        return nameParts;
    }

    private String cleanString(String value) {
        return value == null ? "" : value.trim();
    }

    private Map<String, String> cleanRowData(int rowIndex) {
        Map<String, String> rowData = new HashMap<>();
        rowData.put("lrn", cleanString(model.getValueAt(rowIndex, 0).toString()));
        rowData.put("lastName", cleanString(model.getValueAt(rowIndex, 1).toString()));
        rowData.put("firstName", cleanString(model.getValueAt(rowIndex, 2).toString()));
        rowData.put("nameExtension", cleanString(model.getValueAt(rowIndex, 3).toString())); // Name Extension
        rowData.put("middleName", cleanString(model.getValueAt(rowIndex, 4).toString())); // Middle name
        rowData.put("sex", cleanString(model.getValueAt(rowIndex, 5).toString())); // Sex moved to index 5
        rowData.put("birthDate", cleanString(model.getValueAt(rowIndex, 6).toString()));
        String ageStr = cleanString(model.getValueAt(rowIndex, 7).toString());
        try {
            int age = Integer.parseInt(ageStr.replaceAll("[^0-9]", ""));
            rowData.put("age", String.valueOf(age));
        } catch (NumberFormatException e) {
            rowData.put("age", "0");
        }
        rowData.put("religion", cleanString(model.getValueAt(rowIndex, 8).toString()));
        rowData.put("address", cleanString(model.getValueAt(rowIndex, 9).toString()));
        rowData.put("barangay", cleanString(model.getValueAt(rowIndex, 10).toString()));
        rowData.put("municipality", cleanString(model.getValueAt(rowIndex, 11).toString()));
        rowData.put("province", cleanString(model.getValueAt(rowIndex, 12).toString()));
        Map<String, String> fatherName = parseName(cleanString(model.getValueAt(rowIndex, 13).toString()));
        Map<String, String> motherName = parseName(cleanString(model.getValueAt(rowIndex, 14).toString()));
        Map<String, String> guardianName = parseName(cleanString(model.getValueAt(rowIndex, 15).toString()));
        rowData.putAll(prefixKeys(fatherName, "father"));
        rowData.putAll(prefixKeys(motherName, "mother"));
        rowData.putAll(prefixKeys(guardianName, "guardian"));
        rowData.put("relationship", cleanString(model.getValueAt(rowIndex, 16).toString()));
        rowData.put("contactNumber", cleanString(model.getValueAt(rowIndex, 17).toString()));
        return rowData;
    }

    private Map<String, String> prefixKeys(Map<String, String> map, String prefix) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result.put(prefix + entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void cleanup() {
        if (connection != null) {
            DBConnection.releaseConnection(connection);
        }
    }

    private void showSearchDialog() {
        // Only proceed if there's data to search
        if (model.getRowCount() == 0) {
            showToast(Toast.Type.WARNING, "Please import a school form first before searching.", ToastLocation.BOTTOM_CENTER);
            return;
        }
        
        // Create and show the Excel highlighter dialog - passing the current table
        SFExcelHighlighter excelHighlighter = new SFExcelHighlighter(parentFrame, connection, table);
        excelHighlighter.setLocationRelativeTo(parentFrame);
        
        // Add a cell selected listener to handle when a cell is selected
        excelHighlighter.addCellSelectedListener((row, col, value) -> {
            // Search for the value in the current table
            searchValueInTable(value);
        });
        
        excelHighlighter.setVisible(true);
    }

    private void searchValueInTable(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return;
        }
        
        searchValue = searchValue.trim().toLowerCase();
        boolean found = false;
        
        // Search all cells in the table
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                String cellValue = String.valueOf(model.getValueAt(row, col)).toLowerCase();
                if (cellValue.contains(searchValue)) {
                    // Select and scroll to the matching cell
                    table.setRowSelectionInterval(row, row);
                    table.setColumnSelectionInterval(col, col);
                    table.scrollRectToVisible(table.getCellRect(row, col, true));
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        
        if (!found) {
            showToast(Toast.Type.INFO, "Value '" + searchValue + "' not found in current table view", ToastLocation.BOTTOM_CENTER);
        }
    }

    private void showInfoMessage(String message) {
        showToast(Toast.Type.INFO, message, ToastLocation.BOTTOM_CENTER);
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Setup Undo (Ctrl+Z)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editHistory.canUndo()) {
                    editHistory.undo(model);
                }
            }
        });

        // Setup Redo (Ctrl+Y)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editHistory.canRedo()) {
                    editHistory.redo(model);
                }
            }
        });
    }

    private void refreshStateOnResize() {
        // FIRST. Extract current form data
      

        // 3. Revalidate and repaint to update layout
        this.removeAll();
        //ADD AGAIN ALL COMPONENTS
        initializePanel();
        initializeDAOs();

        this.revalidate();
        this.repaint();

        // LAST. Re-apply data to all panels
        
    }

    @Override
    public void onParentFrameResized(int width, int height) {
        logger.debug("resized to: {}x{}", width, height);
        super.onParentFrameResized(width, height);
    }
}