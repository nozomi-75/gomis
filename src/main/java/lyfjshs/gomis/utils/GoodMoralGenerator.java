package lyfjshs.gomis.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import jnafilechooser.api.JnaFileChooser;
import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.Toast;
import raven.modal.component.SimpleModalBorder;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class GoodMoralGenerator {

    private final Student student;

    public GoodMoralGenerator(Student stud) {
        this.student = stud;
    }

    private static String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

    public void createGoodMoralReport(Component parent) {
        // Define options for the SimpleModalBorder, including a "Close" option
        SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
                new SimpleModalBorder.Option("Print", SimpleModalBorder.YES_OPTION),
                new SimpleModalBorder.Option("Export as DOCX", SimpleModalBorder.NO_OPTION),
                new SimpleModalBorder.Option("Export as PDF", SimpleModalBorder.CANCEL_OPTION),
                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
        };

        // Create the panel for input fields
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[100][][][][]"));
        JFormattedTextField dateGivenField = new JFormattedTextField();
        
        // Get current counselor's information
        String currentSigner = "SALLY P. GENUINO, Principal II"; // Default to principal
        if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
            GuidanceCounselor counselor = Main.formManager.getCounselorObject();
            currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
        }
        
        String[] signersAndPosition = new String[] { "-Select Who to Sign-", currentSigner , "SALLY P. GENUINO, Principal II" , "Other"};
        DropPanel dropDownPanel = new DropPanel();
        panel.add(dropDownPanel, "cell 0 4 2 1,grow");

        JPanel otherSignerPanel = new JPanel();
        dropDownPanel.setContent(otherSignerPanel);
        otherSignerPanel.setLayout(new MigLayout("", "[][grow]", "[][]"));
        otherSignerPanel.add( new JLabel("Full Name:"), "cell 0 0,alignx trailing");
        JTextField fullNameField = new JTextField(10);
        otherSignerPanel.add(fullNameField, "cell 0 1,growx");
        otherSignerPanel.add(new JLabel("Position:"), "cell 0 1,alignx trailing");
        JTextField workPositionField = new JTextField(10);
        otherSignerPanel.add(workPositionField, "cell 1 1,growx");
        
        
        // Create combo box with current counselor and principal as options
        JComboBox<String> signerComboBox = new JComboBox<>(signersAndPosition);
        signerComboBox.addActionListener(e -> {
            if (signerComboBox.getSelectedItem().equals("Other")) {
                dropDownPanel.setDropdownVisible(true);
            } else {
                dropDownPanel.setDropdownVisible(false);
            }
        });
        
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(dateGivenField);
        datePicker.setSelectedDate(java.time.LocalDate.now());

        // Label to display formatted date
        JLabel formattedDateLabel = new JLabel();
        
        // Update the formatted date label
        updateFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
        
        // Add date selection listener to update the formatted date label
        datePicker.addDateSelectionListener(e -> {
            updateFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
        });

        // Add fields to the panel
        panel.add(new JLabel("Purpose:"), "cell 0 0");

        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, "cell 1 0,grow");

        // Input fields
        JTextArea purposeField = new JTextArea();
        scrollPane.setViewportView(purposeField);
        purposeField.setColumns(1);
        purposeField.setRows(5);
        panel.add(new JLabel("Date Given:"), "cell 0 1");
        panel.add(dateGivenField, "cell 1 1");
        
        panel.add(formattedDateLabel, "cell 0 2 2 1,alignx center");
        panel.add(new JLabel("Signer and Position:"), "cell 0 3");
        panel.add(signerComboBox, "cell 1 3");

        // Create and show the ModalDialog
        ModalDialog.showModal(parent, new SimpleModalBorder(panel, "Good Moral Certificate", modalOptions,
                (controller, action) -> {
                    // Only validate and proceed if the user selects a generation option
                    if (action == SimpleModalBorder.YES_OPTION ||
                            action == SimpleModalBorder.NO_OPTION ||
                            action == SimpleModalBorder.CANCEL_OPTION) {

                        String jasperTemplate = "src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper";
                        String purpose = purposeField.getText().trim();
                        String dateGiven = formattedDateLabel.getText();

                        // Validate inputs only when generating the report
                        if (purpose.isEmpty() || dateGiven.isEmpty()) {
                            // Create toast option
                            ToastOption toastOption = Toast.createOption();
                            toastOption.getLayoutOption()
                                    .setMargin(0, 0, 50, 0)
                                    .setDirection(ToastDirection.TOP_TO_BOTTOM);
                            
                            // Show toast
                            Toast.show(parent, Toast.Type.ERROR, 
                                "Purpose and Date Given cannot be empty.", 
                                ToastLocation.BOTTOM_CENTER, 
                                toastOption);
                            return;
                        }

                        // Get the selected signer based on combo box selection
                        String selectedSigner;
                        if (signerComboBox.getSelectedItem().equals("Other")) {
                            String fullName = fullNameField.getText().trim();
                            String position = workPositionField.getText().trim();
                            if (fullName.isEmpty() || position.isEmpty()) {
                                // Create toast option
                                ToastOption toastOption = Toast.createOption();
                                toastOption.getLayoutOption()
                                        .setMargin(0, 0, 50, 0)
                                        .setDirection(ToastDirection.TOP_TO_BOTTOM);
                                
                                // Show toast
                                Toast.show(parent, Toast.Type.ERROR, 
                                    "Please enter both name and position for the signer.", 
                                    ToastLocation.BOTTOM_CENTER, 
                                    toastOption);
                                return;
                            }
                            selectedSigner = fullName + ", " + position;
                        } else if (signerComboBox.getSelectedItem().equals("-Select Who to Sign-")) {
                            // Create toast option
                            ToastOption toastOption = Toast.createOption();
                            toastOption.getLayoutOption()
                                    .setMargin(0, 0, 50, 0)
                                    .setDirection(ToastDirection.TOP_TO_BOTTOM);
                            
                            // Show toast
                            Toast.show(parent, Toast.Type.ERROR, 
                                "Please select a signer.", 
                                ToastLocation.BOTTOM_CENTER, 
                                toastOption);
                            return;
                        } else {
                            selectedSigner = (String) signerComboBox.getSelectedItem();
                        }

                        if (action == SimpleModalBorder.YES_OPTION) { // Print
                            PRINT_GOOD_MORAL(jasperTemplate, student, selectedSigner, purpose, dateGiven, "print",
                                    null);
                            controller.close();
                        } else if (action == SimpleModalBorder.NO_OPTION) { // Export as DOCX
                            JnaFileChooser fileChooser = setupFileChooser("docx");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
                                PRINT_GOOD_MORAL(jasperTemplate, student, selectedSigner, purpose, dateGiven, "docx",
                                        outputName);
                                controller.close();
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION) { // Export as PDF
                            JnaFileChooser fileChooser = setupFileChooser("pdf");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
                                PRINT_GOOD_MORAL(jasperTemplate, student, selectedSigner, purpose, dateGiven, "pdf",
                                        outputName);
                                controller.close();
                            }
                        }
                    } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                        // Close the dialog without validation
                        controller.close();
                    }
                }), "good_moral_modal");

        // Optional: Set dialog size
        ModalDialog.getDefaultOption().getLayoutOption().setSize(500, 300);
    }

    // Helper method to update the formatted date label
    private void updateFormattedDateLabel(JLabel label, LocalDate date) {
        int day = date.getDayOfMonth();
        String suffix = getOrdinalSuffix(day);
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        String formattedDate = day + suffix + " day of " + date.format(monthYearFormatter);
        label.setText(formattedDate);
    }

    /**
     * Sets up a JFileChooser for saving files with the specified extension.
     */
    private JnaFileChooser setupFileChooser(String extension) {
        JnaFileChooser fileChooser = new JnaFileChooser();
        fileChooser.setTitle("Save Good Moral Certificate");
        fileChooser.addFilter(extension.toUpperCase() + " Files (*." + extension + ")", "*." + extension);
        fileChooser.setDefaultFileName("Good Moral Certificate - " + student.getStudentFirstname() + " " +
                student.getStudentLastname() + "." + extension);
        return fileChooser;
    }

    /**
     * Generates the Good Moral Certificate report using ReportGenerator.
     * Updated to include an optional outputName parameter for file exports.
     */
    private static void PRINT_GOOD_MORAL(String jasperTemplate, Student student, String selectedSigner,
            String purpose, String dateGiven, String action, String outputName) {
        try {
            String[] signerParts = selectedSigner.split(", ");
            String nameToSign = signerParts.length > 0 ? signerParts[0] : "Unknown";
            String workPosition = signerParts.length > 1 ? signerParts[1] : "Unknown";

            // Default output name if not provided
            String defaultOutputName = "Good Moral Certificate - " + student.getStudentFirstname() + " " +
                    student.getStudentLastname();
            outputName = (outputName != null) ? outputName : defaultOutputName;

            String TRACK_AND_STRAND = student.getSchoolForm().getSF_TRACK_AND_STRAND();
            String track = TRACK_AND_STRAND.split(" - ")[0];
            String specialization = student.getSchoolForm().getSF_COURSE();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Name", student.getStudentFirstname() + " " + student.getStudentMiddlename() + " " +
                    student.getStudentLastname());
            parameters.put("SchoolYear", student.getSchoolForm().getSF_SCHOOL_YEAR());
            parameters.put("Strand", track);
            parameters.put("TrackAndSpecialization", specialization);
            parameters.put("purpose", purpose);
            parameters.put("DateGiven", dateGiven);
            parameters.put("nameToSign", nameToSign);
            parameters.put("workPosition", workPosition);
            parameters.put("IS_PDF_EXPORT", action.equals("pdf"));

            ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
            reportGenerator.processReport(parameters, outputName, action);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to generate the report: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void createBatchGoodMoralReport(Component parent, List<Student> students) {
        // Define options for the SimpleModalBorder
        SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
                new SimpleModalBorder.Option("Print All", SimpleModalBorder.YES_OPTION),
                new SimpleModalBorder.Option("Export All as DOCX", SimpleModalBorder.NO_OPTION),
                new SimpleModalBorder.Option("Export All as PDF", SimpleModalBorder.CANCEL_OPTION),
                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
        };

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create content panel with MigLayout
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 0", "[right][grow,fill]", "[]10[]10[]10[]10[]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create smooth scrolling pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Purpose section
        contentPanel.add(new JLabel("Purpose:"), "cell 0 0");
        JTextArea purposeField = new JTextArea(5, 20);
        purposeField.setLineWrap(true);
        purposeField.setWrapStyleWord(true);
        JScrollPane purposeScrollPane = new JScrollPane(purposeField);
        purposeScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPanel.add(purposeScrollPane, "cell 1 0,grow");

        // Date section
        JFormattedTextField dateGivenField = new JFormattedTextField();
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(dateGivenField);
        datePicker.setSelectedDate(java.time.LocalDate.now());

        contentPanel.add(new JLabel("Date Given:"), "cell 0 1");
        contentPanel.add(dateGivenField, "cell 1 1");

        // Format date with ordinal suffix
        JLabel formattedDateLabel = new JLabel();
        formattedDateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formattedDateLabel.setFont(formattedDateLabel.getFont().deriveFont(Font.ITALIC));
        
        // Update the formatted date label initially
        updateBatchFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
        
        // Add date selection listener
        datePicker.addDateSelectionListener(e -> {
            updateBatchFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
        });
        
        contentPanel.add(formattedDateLabel, "cell 0 2 2 1,alignx center");

        // Signer section
        String currentSigner = "SALLY P. GENUINO, Principal II";
        if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
            GuidanceCounselor counselor = Main.formManager.getCounselorObject();
            currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
        }

        String[] signersAndPosition = new String[] {
            "-Select Who to Sign-",
            currentSigner,
            "SALLY P. GENUINO, Principal II",
            "Other"
        };

        contentPanel.add(new JLabel("Signer and Position:"), "cell 0 3");
        JComboBox<String> signerComboBox = new JComboBox<>(signersAndPosition);
        contentPanel.add(signerComboBox, "cell 1 3");

        // Other signer panel
        DropPanel dropDownPanel = new DropPanel();
        JPanel otherSignerPanel = new JPanel(new MigLayout("fillx, insets 5", "[][grow]", "[]5[]"));
        otherSignerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextField fullNameField = new JTextField(10);
        JTextField workPositionField = new JTextField(10);

        otherSignerPanel.add(new JLabel("Full Name:"), "cell 0 0");
        otherSignerPanel.add(fullNameField, "cell 1 0,growx");
        otherSignerPanel.add(new JLabel("Position:"), "cell 0 1");
        otherSignerPanel.add(workPositionField, "cell 1 1,growx");

        dropDownPanel.setContent(otherSignerPanel);
        contentPanel.add(dropDownPanel, "cell 0 4 2 1,growx");

        signerComboBox.addActionListener(e -> {
            dropDownPanel.setDropdownVisible(signerComboBox.getSelectedItem().equals("Other"));
        });

        // Selected students section
        JPanel studentsPanel = new JPanel(new MigLayout("fillx, insets 10", "[grow]", "[][grow]"));
        studentsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Selected Students (" + students.size() + ")"
        ));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Student student : students) {
            listModel.addElement(student.getStudentFirstname() + " " + student.getStudentLastname() +
                               " (LRN: " + student.getStudentLrn() + ")");
        }
        JList<String> studentList = new JList<>(listModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(studentList);
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        studentsPanel.add(listScrollPane, "cell 0 0,grow,height 150:200:250");

        mainPanel.add(studentsPanel, BorderLayout.SOUTH);

        // Show modal with unique ID
        ModalDialog.showModal(parent, 
            new SimpleModalBorder(mainPanel, "Print Multiple Good Moral Certificates", modalOptions,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION ||
                            action == SimpleModalBorder.NO_OPTION ||
                            action == SimpleModalBorder.CANCEL_OPTION) {

                        String jasperTemplate = "src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper";
                        String purpose = purposeField.getText().trim();
                        String dateGiven = formattedDateLabel.getText();

                        if (purpose.isEmpty() || dateGiven.isEmpty()) {
                            ToastOption toastOption = Toast.createOption();
                            toastOption.getLayoutOption()
                                    .setMargin(0, 0, 50, 0)
                                    .setDirection(ToastDirection.TOP_TO_BOTTOM);
                            
                            Toast.show(parent, Toast.Type.ERROR, 
                                "Purpose and Date Given cannot be empty.", 
                                ToastLocation.BOTTOM_CENTER, 
                                toastOption);
                            return;
                        }

                        String selectedSigner;
                        if (signerComboBox.getSelectedItem().equals("Other")) {
                            String fullName = fullNameField.getText().trim();
                            String position = workPositionField.getText().trim();
                            if (fullName.isEmpty() || position.isEmpty()) {
                                ToastOption toastOption = Toast.createOption();
                                toastOption.getLayoutOption()
                                        .setMargin(0, 0, 50, 0)
                                        .setDirection(ToastDirection.TOP_TO_BOTTOM);
                                
                                Toast.show(parent, Toast.Type.ERROR, 
                                    "Please enter both name and position for the signer.", 
                                    ToastLocation.BOTTOM_CENTER, 
                                    toastOption);
                                return;
                            }
                            selectedSigner = fullName + ", " + position;
                        } else if (signerComboBox.getSelectedItem().equals("-Select Who to Sign-")) {
                            ToastOption toastOption = Toast.createOption();
                            toastOption.getLayoutOption()
                                    .setMargin(0, 0, 50, 0)
                                    .setDirection(ToastDirection.TOP_TO_BOTTOM);
                            
                            Toast.show(parent, Toast.Type.ERROR, 
                                "Please select a signer.", 
                                ToastLocation.BOTTOM_CENTER, 
                                toastOption);
                            return;
                        } else {
                            selectedSigner = (String) signerComboBox.getSelectedItem();
                        }

                        // For export options, get the output directory once
                        String outputDirectory = null;
                        if (action == SimpleModalBorder.NO_OPTION || action == SimpleModalBorder.CANCEL_OPTION) {
                            JnaFileChooser fileChooser = new JnaFileChooser();
                            fileChooser.setTitle("Select Output Directory for Good Moral Certificates");
                            if (!fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                controller.close();
                                return;
                            }
                            outputDirectory = fileChooser.getSelectedFile().getParent();
                        }

                        // Process all students
                        for (Student student : students) {
                            try {
                                String TRACK_AND_STRAND = student.getSchoolForm().getSF_TRACK_AND_STRAND();
                                String track = TRACK_AND_STRAND.split(" - ")[0];
                                String specialization = student.getSchoolForm().getSF_COURSE();

                                Map<String, Object> parameters = new HashMap<>();
                                parameters.put("Name", student.getStudentFirstname() + " " + student.getStudentMiddlename() + " " +
                                        student.getStudentLastname());
                                parameters.put("SchoolYear", student.getSchoolForm().getSF_SCHOOL_YEAR());
                                parameters.put("Strand", track);
                                parameters.put("TrackAndSpecialization", specialization);
                                parameters.put("purpose", purpose);
                                parameters.put("DateGiven", dateGiven);
                                parameters.put("nameToSign", selectedSigner);
                                parameters.put("workPosition", "Principal");
                                parameters.put("IS_PDF_EXPORT", action == SimpleModalBorder.CANCEL_OPTION);

                                String outputName = "Good Moral Certificate - " + student.getStudentFirstname() + " " +
                                        student.getStudentLastname();

                                if (action == SimpleModalBorder.YES_OPTION) {
                                    // Print directly without asking for printer settings each time
                                    ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
                                    reportGenerator.processReport(parameters, outputName, "print");
                                } else if (action == SimpleModalBorder.NO_OPTION) {
                                    // Export as DOCX to the selected directory
                                    String fullOutputPath = outputDirectory + File.separator + outputName;
                                    ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
                                    reportGenerator.processReport(parameters, fullOutputPath, "docx");
                                } else if (action == SimpleModalBorder.CANCEL_OPTION) {
                                    // Export as PDF to the selected directory
                                    String fullOutputPath = outputDirectory + File.separator + outputName;
                                    ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
                                    reportGenerator.processReport(parameters, fullOutputPath, "pdf");
                                }
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(parent,
                                        "Error processing certificate for " + student.getStudentFirstname() + ": " + e.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                        }
                        controller.close();
                    } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                        controller.close();
                    }
                }), "batch_good_moral_modal");

        // Set dialog size
        ModalDialog.getDefaultOption().getLayoutOption().setSize(600, 600);
    }

    // Helper method for batch certificate formatting
    private static void updateBatchFormattedDateLabel(JLabel label, LocalDate date) {
        int day = date.getDayOfMonth();
        String suffix = getOrdinalSuffix(day);
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        String formattedDate = day + suffix + " day of " + date.format(monthYearFormatter);
        label.setText(formattedDate);
    }

}