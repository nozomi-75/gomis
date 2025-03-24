package lyfjshs.gomis.utils;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class GoodMoralGenerator {

    private final Student student;

    public GoodMoralGenerator(Student stud) {
        this.student = stud;
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
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[][][]"));

        // Input fields
        JTextField purposeField = new JTextField(20);
        JTextField dateGivenField = new JTextField(20);
        JComboBox<String> signerComboBox = new JComboBox<>(
            new String[] { "SALLY P. GENUINO, Principal II", "RACQUEL D. COMANDANTE, Guidance Designate" });

        // Add fields to the panel
        panel.add(new JLabel("Purpose:"));
        panel.add(purposeField, "wrap");
        panel.add(new JLabel("Date Given:"));
        panel.add(dateGivenField, "wrap");
        panel.add(new JLabel("Signer and Position:"));
        panel.add(signerComboBox, "wrap");

        // Create and show the ModalDialog
        ModalDialog.showModal(parent, new SimpleModalBorder(panel, "Good Moral Certificate", modalOptions,
            (controller, action) -> {
                // Only validate and proceed if the user selects a generation option
                if (action == SimpleModalBorder.YES_OPTION || 
                    action == SimpleModalBorder.NO_OPTION || 
                    action == SimpleModalBorder.CANCEL_OPTION) {
                    
                    String jasperTemplate = "src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper";
                    String purpose = purposeField.getText().trim();
                    String dateGiven = dateGivenField.getText().trim();

                    // Validate inputs only when generating the report
                    if (purpose.isEmpty() || dateGiven.isEmpty()) {
                        JOptionPane.showMessageDialog(parent, 
                            "Purpose and Date Given cannot be empty.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (action == SimpleModalBorder.YES_OPTION) { // Print
                        PRINT_GOOD_MORAL(jasperTemplate, student, signerComboBox, purpose, dateGiven, "print", null);
                        controller.close();
                    } else if (action == SimpleModalBorder.NO_OPTION) { // Export as DOCX
                        JFileChooser fileChooser = setupFileChooser("docx");
                        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                            String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
                            PRINT_GOOD_MORAL(jasperTemplate, student, signerComboBox, purpose, dateGiven, "docx", outputName);
                            controller.close();
                        }
                    } else if (action == SimpleModalBorder.CANCEL_OPTION) { // Export as PDF
                        JFileChooser fileChooser = setupFileChooser("pdf");
                        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                            String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
                            PRINT_GOOD_MORAL(jasperTemplate, student, signerComboBox, purpose, dateGiven, "pdf", outputName);
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
    private JFileChooser setupFileChooser(String extension) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Good Moral Certificate");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith("." + extension);
            }

            @Override
            public String getDescription() {
                return extension.toUpperCase() + " Files (*." + extension + ")";
            }
        });
        fileChooser.setSelectedFile(new File("Good Moral Certificate - " + student.getStudentFirstname() + " " +
                                             student.getStudentLastname() + "." + extension));
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

}