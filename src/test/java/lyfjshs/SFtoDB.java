package lyfjshs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
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
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.LoadingDialog;
import lyfjshs.gomis.utils.TableEditHistory;
import lyfjshs.gomis.utils.TableEditHistory.TableEdit;
import net.miginfocom.swing.MigLayout;

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

    // Add these fields
    private TableEditHistory editHistory;
    private Object lastCellValue;

    public SFtoDB() {
        editHistory = new TableEditHistory();
        initializeFrame();
        initializeDatabase();
        initializeDAOs();
        setupKeyBindings();
        setLocationRelativeTo(null);
    }

    private void initializeFrame() {
        setTitle("School Forms Data Import");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel topPanel = createMetadataPanel();
        JPanel centerPanel = createTablePanel();
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        loadingDialog = new LoadingDialog(this, "Processing");
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
        JButton searchButton = new JButton("Search"); // Add search button
        
        Dimension buttonSize = new Dimension(200, 40);
        Font buttonFont = new Font(Font.DIALOG, Font.BOLD, 14);
        
        saveButton.setPreferredSize(buttonSize);
        importButton.setPreferredSize(buttonSize);
        searchButton.setPreferredSize(buttonSize);
        
        importButton.setFont(buttonFont);
        saveButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
        
        importButton.addActionListener(e -> importFiles());
        saveButton.addActionListener(e -> saveToDatabase());
        searchButton.addActionListener(e -> showSearchDialog());
        
        panel.add(importButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(saveButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(searchButton);
        
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

        // Create and show the confirmation dialog with modified data
        StringBuilder message = new StringBuilder();
        message.append("The following modifications will be saved:\n\n");
        message.append("School Information:\n");
        message.append("School Name: ").append(schoolNameField.getText()).append("\n");
        message.append("School ID: ").append(schoolIDField.getText()).append("\n");
        message.append("District: ").append(districtField.getText()).append("\n");
        message.append("School Year: ").append(schoolYearField.getText()).append("\n");
        message.append("Semester: ").append(semesterField.getText()).append("\n");
        message.append("Section: ").append(sectionField.getText()).append("\n");
        message.append("\nStudent Records: ").append(model.getRowCount()).append(" total\n");

        // Create a custom dialog with a scrollable text area
        JDialog confirmDialog = new JDialog(this, "Confirm Save", true);
        confirmDialog.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea(message.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");
        
        confirmButton.addActionListener(e -> {
            confirmDialog.dispose();
            proceedWithSave();
        });
        
        cancelButton.addActionListener(e -> {
            confirmDialog.dispose();
        });
        
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        
        confirmDialog.add(scrollPane, BorderLayout.CENTER);
        confirmDialog.add(buttonPanel, BorderLayout.SOUTH);
        confirmDialog.pack();
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setVisible(true);
    }

    private void proceedWithSave() {
        progressBar.setValue(0);
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
                    Thread.sleep(50);
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

    private void saveStudentData(int rowIndex, int sfId) throws SQLException {
        Map<String, String> rowData = cleanRowData(rowIndex);
        
        try {
            // Create entities using the DAO pattern
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
            
            Contact contact = new Contact(0, rowData.get("contactNumber"));
            
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
            
            Guardian guardian = new Guardian(
                0,
                rowData.get("guardianLastName"),
                rowData.get("guardianFirstName"),
                rowData.get("guardianMiddleName"),
                rowData.get("relationship"),
                ""  // guardian contact
            );

            // Create records using DAOs first to get the IDs
            int addressId = addressDAO.createAddress(address);
            int contactId = contactDAO.createContact(contact);
            int parentId = parentsDAO.createParents(parents);
            int guardianId = guardianDAO.createGuardian(guardian);

            // Create SchoolForm object for the student
            SchoolForm schoolForm = new SchoolForm(
                sfId,
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

            // Create Student using the full constructor
            Student student = new Student(
                0, // studentUid will be generated
                parentId,
                guardianId,
                addressId,
                contactId,
                sectionField.getText(), // schoolSection
                rowData.get("lrn"),
                rowData.get("lastName"),
                rowData.get("firstName"),
                rowData.get("middleName"),
                rowData.get("sex"),
                null, // birthDate - you might want to parse this from rowData
                rowData.get("mothertongue"),
                Integer.parseInt(rowData.get("age")),
                "", // ipType - not in SF form
                rowData.get("religion"),
                address,
                contact,
                parents,
                guardian,
                schoolForm
            );
            
            // Save the student record
            studentsDAO.createStudentData(student);
            
        } catch (SQLException e) {
            throw new SQLException("Error saving student data: " + e.getMessage());
        }
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
        SchoolForm schoolForm = new SchoolForm(
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
        SearchDialog dialog = new SearchDialog(this);
        dialog.setVisible(true);
    }

    private class SearchDialog extends JDialog {
        private JComboBox<String> columnComboBox;
        private JTextField searchField;
        private JButton searchButton;
        private JComboBox<String> additionalColumnComboBox;  // Added
        private JTextField additionalSearchField;  // Added
        private JCheckBox enableSecondSearch;  // Added

        public SearchDialog(JFrame parent) {
            super(parent, "Search Records", true);
            initializeComponents();
            setupLayout();
            setLocationRelativeTo(parent);
            pack();
        }

        private void initializeComponents() {
            String[] columnNames = new String[model.getColumnCount()];
            for (int i = 0; i < model.getColumnCount(); i++) {
                columnNames[i] = model.getColumnName(i);
            }

            columnComboBox = new JComboBox<>(columnNames);
            searchField = new JTextField(20);
            
            // Add components for second search criteria
            additionalColumnComboBox = new JComboBox<>(columnNames);
            additionalSearchField = new JTextField(20);
            enableSecondSearch = new JCheckBox("Add second search criteria");
            
            // Initially disable second search components
            additionalColumnComboBox.setEnabled(false);
            additionalSearchField.setEnabled(false);
            
            // Enable/disable second search components based on checkbox
            enableSecondSearch.addActionListener(e -> {
                boolean isEnabled = enableSecondSearch.isSelected();
                additionalColumnComboBox.setEnabled(isEnabled);
                additionalSearchField.setEnabled(isEnabled);
            });

            searchButton = new JButton("Search");
            searchButton.addActionListener(e -> performSearch());
        }

        private void setupLayout() {
            setLayout(new MigLayout("fillx, insets 10", "[right][grow]", "[][]"));
            
            // First search criteria
            add(new JLabel("Search Column:"));
            add(columnComboBox, "growx, wrap");
            
            add(new JLabel("Search Value:"));
            add(searchField, "growx, wrap");
            
            // Checkbox for enabling second search
            add(enableSecondSearch, "skip 1, wrap");
            
            // Second search criteria
            add(new JLabel("Additional Column:"));
            add(additionalColumnComboBox, "growx, wrap");
            
            add(new JLabel("Additional Value:"));
            add(additionalSearchField, "split 2, growx");
            add(searchButton, "wrap");
            
            // Cancel button
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            add(cancelButton, "skip 1, align right");
        }

        private void performSearch() {
            String searchValue = searchField.getText().trim().toLowerCase();
            int selectedColumn = columnComboBox.getSelectedIndex();
            boolean useSecondCriteria = enableSecondSearch.isSelected();
            String additionalValue = additionalSearchField.getText().trim().toLowerCase();
            int additionalColumn = additionalColumnComboBox.getSelectedIndex();
            boolean found = false;

            // Clear any existing selection
            table.clearSelection();

            // Search through all rows
            for (int row = 0; row < model.getRowCount(); row++) {
                String cellValue = model.getValueAt(row, selectedColumn).toString().toLowerCase();
                boolean matchesFirst = cellValue.contains(searchValue);
                
                boolean matchesSecond = true;
                if (useSecondCriteria) {
                    String additionalCellValue = model.getValueAt(row, additionalColumn).toString().toLowerCase();
                    matchesSecond = additionalCellValue.contains(additionalValue);
                }

                if (matchesFirst && matchesSecond) {
                    // Select and scroll to the matching row
                    table.addRowSelectionInterval(row, row);
                    table.scrollRectToVisible(table.getCellRect(row, selectedColumn, true));
                    found = true;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this,
                    "No matches found for the specified criteria",
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                dispose(); // Close dialog if matches were found
            }
        }
    }

    private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

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