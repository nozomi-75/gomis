package lyfjshs.gomis.utils;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import jnafilechooser.api.JnaFileChooser;

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
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[100][][][]"));
        JFormattedTextField dateGivenField = new JFormattedTextField();
        JComboBox<String> signerComboBox = new JComboBox<>(
                new String[] { "SALLY P. GENUINO, Principal II", "RACQUEL D. COMANDANTE, Guidance Designate" });
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(dateGivenField);
        datePicker.setSelectedDate(java.time.LocalDate.now());

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
        
        // Format the date with proper ordinal suffix
        LocalDate selectedDate = datePicker.getSelectedDate();
        int day = selectedDate.getDayOfMonth();
        String suffix = getOrdinalSuffix(day);
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        String formatDateSelected = day + suffix + " day of " + selectedDate.format(monthYearFormatter);
        
        panel.add(new JLabel(formatDateSelected), "cell 0 2 2 1,alignx center");
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
                        String dateGiven = formatDateSelected;

                        // Validate inputs only when generating the report
                        if (purpose.isEmpty() || dateGiven.isEmpty()) {
                            JOptionPane.showMessageDialog(parent,
                                    "Purpose and Date Given cannot be empty.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if (action == SimpleModalBorder.YES_OPTION) { // Print
                            PRINT_GOOD_MORAL(jasperTemplate, student, signerComboBox, purpose, dateGiven, "print",
                                    null);
                            controller.close();
                        } else if (action == SimpleModalBorder.NO_OPTION) { // Export as DOCX
                            JnaFileChooser fileChooser = setupFileChooser("docx");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
                                PRINT_GOOD_MORAL(jasperTemplate, student, signerComboBox, purpose, dateGiven, "docx",
                                        outputName);
                                controller.close();
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION) { // Export as PDF
                            JnaFileChooser fileChooser = setupFileChooser("pdf");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
                                PRINT_GOOD_MORAL(jasperTemplate, student, signerComboBox, purpose, dateGiven, "pdf",
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
    private static void PRINT_GOOD_MORAL(String jasperTemplate, Student student, JComboBox<String> signerComboBox,
            String purpose, String dateGiven, String action, String outputName) {
        try {
            String selectedSigner = (String) signerComboBox.getSelectedItem();
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

        // Create the panel for input fields
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[100][][][][grow]"));
        JFormattedTextField dateGivenField = new JFormattedTextField();
        JComboBox<String> signerComboBox = new JComboBox<>(
                new String[] { "SALLY P. GENUINO, Principal II", "RACQUEL D. COMANDANTE, Guidance Designate" });
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(dateGivenField);
        datePicker.setSelectedDate(java.time.LocalDate.now());

        // Add fields to the panel
        panel.add(new JLabel("Purpose:"), "cell 0 0");

        JScrollPane purposeScrollPane = new JScrollPane();
        panel.add(purposeScrollPane, "cell 1 0,grow");

        JTextArea purposeField = new JTextArea();
        purposeScrollPane.setViewportView(purposeField);
        purposeField.setColumns(1);
        purposeField.setRows(5);
        
        panel.add(new JLabel("Date Given:"), "cell 0 1");
        panel.add(dateGivenField, "cell 1 1");
        
        // Format the date with proper ordinal suffix
        LocalDate selectedDate = datePicker.getSelectedDate();
        int day = selectedDate.getDayOfMonth();
        String suffix = getOrdinalSuffix(day);
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        String formatDateSelected = day + suffix + " day of " + selectedDate.format(monthYearFormatter);
        
        panel.add(new JLabel(formatDateSelected), "cell 0 2 2 1,alignx center");
        panel.add(new JLabel("Signer and Position:"), "cell 0 3");
        panel.add(signerComboBox, "cell 1 3");

        // Add selected students list
        panel.add(new JLabel("Selected Students (" + students.size() + "):"), "cell 0 4");
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (Student student : students) {
            listModel.addElement(student.getStudentFirstname() + " " + student.getStudentLastname() + 
                               " (LRN: " + student.getStudentLrn() + ")");
        }
        JList<String> studentList = new JList<>(listModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(studentList);
        panel.add(listScrollPane, "cell 1 4,grow");

        // Show modal with unique ID
        ModalDialog.showModal(parent, new SimpleModalBorder(panel, "Print Multiple Good Moral Certificates", modalOptions,
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION ||
                            action == SimpleModalBorder.NO_OPTION ||
                            action == SimpleModalBorder.CANCEL_OPTION) {

                        String jasperTemplate = "src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper";
                        String purpose = purposeField.getText().trim();
                        String dateGiven = formatDateSelected;

                        if (purpose.isEmpty() || dateGiven.isEmpty()) {
                            JOptionPane.showMessageDialog(parent,
                                    "Purpose and Date Given cannot be empty.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        String selectedSigner = (String) signerComboBox.getSelectedItem();
                        String[] signerParts = selectedSigner.split(", ");
                        String nameToSign = signerParts.length > 0 ? signerParts[0] : "Unknown";
                        String workPosition = signerParts.length > 1 ? signerParts[1] : "Unknown";

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
                                parameters.put("nameToSign", nameToSign);
                                parameters.put("workPosition", workPosition);
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
        ModalDialog.getDefaultOption().getLayoutOption().setSize(600, 500);
    }

}