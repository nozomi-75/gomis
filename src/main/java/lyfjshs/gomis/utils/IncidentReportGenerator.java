package kotlin.jasper;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class IncidentReportGenerator {
    private static final String DEFAULT_JASPER_TEMPLATE = "src/main/resources/jasperTemplates/templates/IncidentReport_Final.jasper";

    public static void createIncidentReport(Component parent) {
        // Define options for the SimpleModalBorder
        SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
            new SimpleModalBorder.Option("Print", SimpleModalBorder.YES_OPTION),
            new SimpleModalBorder.Option("Export as DOCX", SimpleModalBorder.NO_OPTION),
            new SimpleModalBorder.Option("Export as PDF", SimpleModalBorder.CANCEL_OPTION),
            new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
        };

        // Create the panel for input fields
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[][][][][][]"));

        // Input fields
        JTextField nameField = new JTextField(20);
        JTextField gradeSectionField = new JTextField(20);
        JTextField dateField = new JTextField(20);
        JTextField timeVisitField = new JTextField(20);
        JTextArea incidentReportField = new JTextArea(10, 30);
        JTextField outputNameField = new JTextField("IncidentReport", 20);

        // Add scroll pane for the incident report text area
        JScrollPane incidentScrollPane = new JScrollPane(incidentReportField);

        // Add fields to the panel
        panel.add(new JLabel("Name:"));
        panel.add(nameField, "wrap");
        panel.add(new JLabel("Grade & Section:"));
        panel.add(gradeSectionField, "wrap");
        panel.add(new JLabel("Date of Visit:"));
        panel.add(dateField, "wrap");
        panel.add(new JLabel("Time of Visit:"));
        panel.add(timeVisitField, "wrap");
        panel.add(new JLabel("Initial Incident Report:"));
        panel.add(incidentScrollPane, "wrap, growx");
        panel.add(new JLabel("Output File Name:"));
        panel.add(outputNameField, "wrap");

        // Create and show the ModalDialog
        ModalDialog.showModal(parent, new SimpleModalBorder(panel, "Incident Report Generator", modalOptions,
            (controller, action) -> {
                if (action == SimpleModalBorder.YES_OPTION || 
                    action == SimpleModalBorder.NO_OPTION || 
                    action == SimpleModalBorder.CANCEL_OPTION) {
                    
                    String name = nameField.getText().trim();
                    String gradeSection = gradeSectionField.getText().trim();
                    String date = dateField.getText().trim();
                    String timeVisit = timeVisitField.getText().trim();
                    String incidentReport = incidentReportField.getText().trim();
                    String outputName = outputNameField.getText().trim();

                    // Validate inputs
                    if (name.isEmpty() || gradeSection.isEmpty() || date.isEmpty() || 
                        timeVisit.isEmpty() || incidentReport.isEmpty()) {
                        JOptionPane.showMessageDialog(parent, 
                            "All fields except Output File Name must be filled.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (outputName.isEmpty()) {
                        outputName = "IncidentReport";
                    }

                    if (action == SimpleModalBorder.YES_OPTION) { // Print
                        generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, 
                                     incidentReport, "print", null);
                        controller.close();
                    } else if (action == SimpleModalBorder.NO_OPTION) { // Export as DOCX
                        JFileChooser fileChooser = setupFileChooser("docx");
                        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                            String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
                            generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, 
                                         incidentReport, "docx", baseName);
                            controller.close();
                        }
                    } else if (action == SimpleModalBorder.CANCEL_OPTION) { // Export as PDF
                        JFileChooser fileChooser = setupFileChooser("pdf");
                        if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                            String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
                            generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, 
                                         incidentReport, "pdf", baseName);
                            controller.close();
                        }
                    }
                } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }), "incident_report_modal");

        // Optional: Set dialog size
        ModalDialog.getDefaultOption().getLayoutOption().setSize(500, 500);
    }

    /**
     * Sets up a JFileChooser for saving files with the specified extension.
     */
    private static JFileChooser setupFileChooser(String extension) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Incident Report");
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
        fileChooser.setSelectedFile(new File("IncidentReport." + extension));
        return fileChooser;
    }

    /**
     * Generates the Incident Report using ReportGenerator.
     */
    private static void generateReport(String jasperTemplate, String name, String gradeSection, 
                                     String date, String timeVisit, String incidentReport, 
                                     String action, String outputName) {
        try {
            // Default output name if not provided
            outputName = (outputName != null) ? outputName : "IncidentReport";

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("VName", name);
            parameters.put("VGrSec", gradeSection);
            parameters.put("Date", date);
            parameters.put("Tvisit", timeVisit);
            parameters.put("IniReport", incidentReport);
            parameters.put("IS_PDF_EXPORT", action.equals("pdf"));

            ReportGenerator reportGenerator = new ReportGenerator(jasperTemplate);
            reportGenerator.processReport(parameters, outputName, action);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to generate the report: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}