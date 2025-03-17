package lyfjshs.gomis.test.Jasper;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;

public class JasperDocxGenerator {
    private static File selectedTemplate;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JasperDocxGenerator::createUI);
    }

    private static void createUI() {
        JFrame frame = new JFrame("Jasper Report Generator");
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(12, 2, 5, 5));

        JButton chooseTemplateBtn = new JButton("Choose JRXML Template");
        JLabel templateLabel = new JLabel("No template selected", SwingConstants.CENTER);

        JTextField nameField = new JTextField();
        JTextField schoolYearField = new JTextField();
        JTextField strandField = new JTextField();
        JTextField trackField = new JTextField();
        JTextField purposeField = new JTextField();
        JTextField dateGivenField = new JTextField();

        JComboBox<String> signerComboBox = new JComboBox<>(new String[]{
                "SALLY P. GENUINO, Principal II", "RACQUEL D. COMANDANTE, Guidance Designate"
        });

        JLabel depEdSealLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        JLabel depEdMatatagLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        JLabel lyfjshsLogoLabel = new JLabel("No Image Selected", SwingConstants.CENTER);

        JButton depEdSealButton = new JButton("Choose DepEd Seal");
        JButton depEdMatatagButton = new JButton("Choose DepEd MATATAG");
        JButton lyfjshsLogoButton = new JButton("Choose LYFJSHS Logo");
        JButton generateBtn = new JButton("Generate Report");

        final BufferedImage[] depEdSeal = new BufferedImage[1];
        final BufferedImage[] depEdMatatag = new BufferedImage[1];
        final BufferedImage[] lyfjshsLogo = new BufferedImage[1];

        chooseTemplateBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedTemplate = fileChooser.getSelectedFile();
                templateLabel.setText(selectedTemplate.getName());
            }
        });

        depEdSealButton.addActionListener(e -> depEdSeal[0] = selectImage(depEdSealLabel));
        depEdMatatagButton.addActionListener(e -> depEdMatatag[0] = selectImage(depEdMatatagLabel));
        lyfjshsLogoButton.addActionListener(e -> lyfjshsLogo[0] = selectImage(lyfjshsLogoLabel));

        generateBtn.addActionListener(e -> {
            if (selectedTemplate == null) {
                JOptionPane.showMessageDialog(frame, "Please select a JRXML template first.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedSigner = (String) signerComboBox.getSelectedItem();
            String[] parts = selectedSigner.split(", ");
            String nameToSign = parts[0];
            String workPosition = parts[1];

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Name", nameField.getText());
            parameters.put("SchoolYear", schoolYearField.getText());
            parameters.put("Strand", strandField.getText());
            parameters.put("TrackAndSpecialization", trackField.getText());
            parameters.put("purpose", purposeField.getText());
            parameters.put("DateGiven", dateGivenField.getText());
            parameters.put("nameToSign", nameToSign);
            parameters.put("workPosition", workPosition);
            parameters.put("DepEd_Seal", depEdSeal[0]);
            parameters.put("DepEd_MATATAG", depEdMatatag[0]);
            parameters.put("LYFJSHS_logo", lyfjshsLogo[0]);

            generateReport(selectedTemplate, parameters);
        });

        mainPanel.add(chooseTemplateBtn);
        mainPanel.add(templateLabel);
        mainPanel.add(new JLabel("Name:"));
        mainPanel.add(nameField);
        mainPanel.add(new JLabel("School Year:"));
        mainPanel.add(schoolYearField);
        mainPanel.add(new JLabel("Strand:"));
        mainPanel.add(strandField);
        mainPanel.add(new JLabel("Track and Specialization:"));
        mainPanel.add(trackField);
        mainPanel.add(new JLabel("Purpose:"));
        mainPanel.add(purposeField);
        mainPanel.add(new JLabel("Date Given:"));
        mainPanel.add(dateGivenField);
        mainPanel.add(new JLabel("Signer and Position:"));
        mainPanel.add(signerComboBox);
        mainPanel.add(depEdSealButton);
        mainPanel.add(depEdSealLabel);
        mainPanel.add(depEdMatatagButton);
        mainPanel.add(depEdMatatagLabel);
        mainPanel.add(lyfjshsLogoButton);
        mainPanel.add(lyfjshsLogoLabel);
        mainPanel.add(new JLabel(""));
        mainPanel.add(generateBtn);

        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static BufferedImage selectImage(JLabel label) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File imageFile = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(imageFile);
                label.setText(imageFile.getName());
                return image;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error loading image: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private static void generateReport(File jrxmlFile, Map<String, Object> parameters) {
        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlFile.getAbsolutePath());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            int choice = JOptionPane.showOptionDialog(null, "Choose Export Format", "Export Report",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    new String[]{"Print", "Export to DOCX", "Export to PDF"}, "Print");

            JFileChooser fileChooser = new JFileChooser();
            if (choice == 1) { // Export to DOCX
                fileChooser.setSelectedFile(new File("Report.docx"));
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    exportToDocx(jasperPrint, fileChooser.getSelectedFile());
                }
            } else if (choice == 2) { // Export to PDF
                fileChooser.setSelectedFile(new File("Report.pdf"));
                if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    exportToPdf(jasperPrint, fileChooser.getSelectedFile());
                }
            } else {
                JasperPrintManager.printReport(jasperPrint, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void exportToDocx(JasperPrint jasperPrint, File outputFile) {
        try {
            JRDocxExporter exporter = new JRDocxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));

            SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
            
            // Optimize DOCX output for better formatting
            configuration.setFramesAsNestedTables(false); // Prevent excessive nested tables
            configuration.setFlexibleRowHeight(true); // Allow rows to adjust height dynamically
            configuration.setIgnoreHyperlink(true); // Ignore hyperlinks for cleaner output
            configuration.setNewLineAsParagraph(true); // Preserve paragraph formatting
            configuration.setBackgroundAsHeader(false); // Prevent background from being used as header
            configuration.setOverrideHints(true); // Allows the exporter to override report hints

            exporter.setConfiguration(configuration);
            exporter.exportReport();

            JOptionPane.showMessageDialog(null, "DOCX export successful!\nSaved to: " + outputFile.getAbsolutePath(),
                    "Export Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (JRException e) {
            JOptionPane.showMessageDialog(null, "Error exporting to DOCX: " + e.getMessage(),
                    "Export Failed", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private static void exportToPdf(JasperPrint jasperPrint, File outputFile) throws JRException {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));
        exporter.exportReport();
    }
}
