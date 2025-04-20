package lyfjshs.gomis.utils.jasper;

import java.awt.Component;
import java.awt.Image;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jnafilechooser.api.JnaFileChooser;
import lyfjshs.gomis.Database.entity.Student;
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


    public void createGoodMoralReport(Component parent) {
        // Define options for the SimpleModalBorder
        SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
                new SimpleModalBorder.Option("Print", SimpleModalBorder.YES_OPTION),
                new SimpleModalBorder.Option("Export as PDF", SimpleModalBorder.NO_OPTION),
                new SimpleModalBorder.Option("Export as DOCX", SimpleModalBorder.CANCEL_OPTION),
                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
        };

        // Create the preview panel
        GoodMoralPreview previewPanel = new GoodMoralPreview(parent);

        // Show modal
        ModalDialog.showModal(parent, new SimpleModalBorder(previewPanel, "Good Moral Certificate", modalOptions,
                (controller, action) -> {
                    if (validateInputs(previewPanel.getPurpose(), previewPanel.getFormattedDate(),
                            previewPanel.getSelectedSigner(), parent)) {
                        String jasperTemplate = "src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper";

                        if (action == SimpleModalBorder.YES_OPTION) { // Print
                            PRINT_GOOD_MORAL(jasperTemplate, student, previewPanel.getSelectedSigner(),
                                    previewPanel.getPurpose(), previewPanel.getFormattedDate(),
                                    "print", null);
                            controller.close();
                        } else if (action == SimpleModalBorder.NO_OPTION) { // Export as PDF
                            JnaFileChooser fileChooser = setupFileChooser("pdf");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
                                PRINT_GOOD_MORAL(jasperTemplate, student, previewPanel.getSelectedSigner(),
                                        previewPanel.getPurpose(), previewPanel.getFormattedDate(),
                                        "pdf", outputName);
                                controller.close();
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION) { // Export as DOCX
                            JnaFileChooser fileChooser = setupFileChooser("docx");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
                                PRINT_GOOD_MORAL(jasperTemplate, student, previewPanel.getSelectedSigner(),
                                        previewPanel.getPurpose(), previewPanel.getFormattedDate(),
                                        "docx", outputName);
                                controller.close();
                            }
                        } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                            controller.close();
                        }
                    }
                }));
    }

    private boolean validateInputs(String purpose, String dateGiven, String selectedSigner, Component parent) {
        if (parent == null) {
            JOptionPane.showMessageDialog(null, "Parent component must not be null.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            showToast("Please enter a purpose", Toast.Type.WARNING, parent);
            return false;
        }
        if (dateGiven == null || dateGiven.trim().isEmpty()) {
            showToast("Please select a date", Toast.Type.WARNING, parent);
            return false;
        }
        if (selectedSigner == null || selectedSigner.trim().isEmpty() || "-Select Who to Sign-".equals(selectedSigner)) {
            showToast("Please select or enter a signer", Toast.Type.WARNING, parent);
            return false;
        }
        return true;
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

    public void createBatchGoodMoralReport(Component parent, List<Student> students) {
        // Define options for the SimpleModalBorder
        SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
                new SimpleModalBorder.Option("Print All", SimpleModalBorder.YES_OPTION),
                new SimpleModalBorder.Option("Export All as PDF", SimpleModalBorder.NO_OPTION),
                new SimpleModalBorder.Option("Export All as DOCX", SimpleModalBorder.CANCEL_OPTION),
                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
        };

        // Create the batch panel
        GoodMoralBatchPanel batchPanel = new GoodMoralBatchPanel(parent, students);

        // Show modal
        ModalDialog.showModal(parent, new SimpleModalBorder(batchPanel, "Batch Good Moral Certificates", modalOptions,
                (controller, action) -> {
                    if (validateBatchInputs(batchPanel.getPurpose(), batchPanel.getFormattedDate(),
                            batchPanel.getSelectedSigner(), parent)) {
                        String jasperTemplate = "src/main/resources/jasperTemplates/templates/GoodMoral_Final.jasper";

                        if (action == SimpleModalBorder.YES_OPTION) { // Print All
                            processBatchCertificates(students, jasperTemplate, batchPanel.getSelectedSigner(),
                                    batchPanel.getPurpose(), batchPanel.getFormattedDate(),
                                    "print", null, batchPanel.getImages(), parent);
                            controller.close();
                        } else if (action == SimpleModalBorder.NO_OPTION) { // Export All as PDF
                            JnaFileChooser fileChooser = new JnaFileChooser();
                            fileChooser.setTitle("Select Output Directory for Good Moral Certificates");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputDirectory = fileChooser.getSelectedFile().getParent();
                                processBatchCertificates(students, jasperTemplate, batchPanel.getSelectedSigner(),
                                        batchPanel.getPurpose(), batchPanel.getFormattedDate(),
                                        "pdf", outputDirectory, batchPanel.getImages(), parent);
                                controller.close();
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION) { // Export All as DOCX
                            JnaFileChooser fileChooser = new JnaFileChooser();
                            fileChooser.setTitle("Select Output Directory for Good Moral Certificates");
                            if (fileChooser.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
                                String outputDirectory = fileChooser.getSelectedFile().getParent();
                                processBatchCertificates(students, jasperTemplate, batchPanel.getSelectedSigner(),
                                        batchPanel.getPurpose(), batchPanel.getFormattedDate(),
                                        "docx", outputDirectory, batchPanel.getImages(), parent);
                                controller.close();
                            }
                        } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                            controller.close();
                        }
                    }
                }), "batch_good_moral_modal");

        // Set dialog size
        ModalDialog.getDefaultOption().getLayoutOption().setSize(1000, 800);
    }

    private boolean validateBatchInputs(String purpose, String dateGiven, String selectedSigner, Component parent) {
        if (purpose.isEmpty() || dateGiven.isEmpty()) {
            showToast("Purpose and Date Given cannot be empty.", Toast.Type.ERROR, parent);
            return false;
        }

        if (selectedSigner == null || selectedSigner.equals("-Select Who to Sign-")) {
            showToast("Please select a signer.", Toast.Type.ERROR, parent);
            return false;
        }

        return true;
    }

    private void showToast(String message, Toast.Type type, Component parent) {
        if (parent == null) {
            System.err.println("Parent component is null. Cannot show toast.");
            return;
        }
        ToastOption toastOption = Toast.createOption();
        toastOption.getLayoutOption()
                .setMargin(0, 0, 50, 0)
                .setDirection(ToastDirection.TOP_TO_BOTTOM);
        Toast.show(parent, type, message, ToastLocation.BOTTOM_CENTER, toastOption);
    }

    private void processBatchCertificates(List<Student> students, String jasperTemplate, String selectedSigner,
                                               String purpose, String dateGiven, String action, String outputDirectory,
                                               Image[] images, Component parent) {
        for (Student student : students) {
            try {
                String outputName = "Good Moral Certificate - " + student.getStudentFirstname() + " " + student.getStudentLastname();
                if (outputDirectory != null) {
                    outputName = outputDirectory + File.separator + outputName;
                }

                String TRACK_AND_STRAND = student.getSchoolForm().getSF_TRACK_AND_STRAND();
                String track = TRACK_AND_STRAND.split(" - ")[0];
                String specialization = student.getSchoolForm().getSF_COURSE();

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("Name", student.getStudentFirstname() + " " + student.getStudentMiddlename() + " " + student.getStudentLastname());
                parameters.put("SchoolYear", student.getSchoolForm().getSF_SCHOOL_YEAR());
                parameters.put("Strand", track);
                parameters.put("TrackAndSpecialization", specialization);
                parameters.put("purpose", purpose);
                parameters.put("DateGiven", dateGiven);
                String[] signerParts = selectedSigner.split(", ");
                parameters.put("nameToSign", signerParts[0]);
                parameters.put("workPosition", signerParts.length > 1 ? signerParts[1] : "Unknown");
                parameters.put("deped_seal", images[0]);
                parameters.put("deped_matatag", images[1]);
                parameters.put("LYFJSHS_logo", images[2]);

                ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
                reportGenerator.processReport(parameters, outputName, action);

            } catch (Exception e) {
                showToast("Error processing certificate for " + student.getStudentFirstname() + ": " + e.getMessage(), Toast.Type.ERROR, parent);
            }
        }
    }
}