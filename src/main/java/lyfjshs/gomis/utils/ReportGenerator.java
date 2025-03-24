package lyfjshs.gomis.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;

/**
 * A reusable report generator for compiling and exporting JasperReports templates.
 */
public class ReportGenerator {
    private final String jasperTemplate;
    private static final Map<String, ExportAction> EXPORT_ACTIONS = new HashMap<>();

    // Functional interface for export actions
    @FunctionalInterface
    private interface ExportAction {
        void execute(JasperPrint jasperPrint, String outputName);
    }

    // Static initialization of export actions
    static {
        EXPORT_ACTIONS.put("print", ReportGenerator::printReport);
        EXPORT_ACTIONS.put("docx", ReportGenerator::exportToDocx);
        EXPORT_ACTIONS.put("pdf", ReportGenerator::exportToPdf);
        EXPORT_ACTIONS.put("default", (print, output) -> 
            JOptionPane.showMessageDialog(null, "Invalid action specified!", "Error", JOptionPane.ERROR_MESSAGE));
    }

    public ReportGenerator(String jasperTemplate) {
        this.jasperTemplate = jasperTemplate;
    }

    /**
     * Generates a report based on the provided parameters.
     */
    public JasperPrint generateReport(Map<String, Object> parameters) {
        try {
            return JasperFillManager.fillReport(jasperTemplate, parameters, new JREmptyDataSource());
        } catch (JRException e) {
            showError("Error generating report: " + e.getMessage(), "Report Generation Failed");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Processes the report using the specified action.
     */
    public void processReport(Map<String, Object> parameters, String outputName, String action) {
        JasperPrint jasperPrint = generateReport(parameters);
        
        if (jasperPrint == null) {
            showError("Report generation failed.", "Error");
            return;
        }

        ExportAction exportAction = EXPORT_ACTIONS.getOrDefault(action.toLowerCase(), EXPORT_ACTIONS.get("default"));
        exportAction.execute(jasperPrint, outputName);
    }

    // Private method to print report
    private static void printReport(JasperPrint jasperPrint, String outputName) {
        try {
            JasperPrintManager.printReport(jasperPrint, true);
        } catch (JRException e) {
            showError("Error printing report: " + e.getMessage(), "Print Failed");
            e.printStackTrace();
        }
    }

    // Private method to export to DOCX
    private static void exportToDocx(JasperPrint jasperPrint, String outputName) {
        File outputFile = new File(outputName + ".docx");
        try {
            JRDocxExporter exporter = new JRDocxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));

            SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
            configuration.setFramesAsNestedTables(false);
            configuration.setFlexibleRowHeight(true);
            configuration.setIgnoreHyperlink(true);
            configuration.setNewLineAsParagraph(true);
            configuration.setBackgroundAsHeader(false);
            configuration.setOverrideHints(true);

            exporter.setConfiguration(configuration);
            exporter.exportReport();

            showSuccess("DOCX export successful!\nSaved to: " + outputFile.getAbsolutePath(), "Export Success");
        } catch (JRException e) {
            showError("Error exporting to DOCX: " + e.getMessage(), "Export Failed");
            e.printStackTrace();
        }
    }

    // Private method to export to PDF
    private static void exportToPdf(JasperPrint jasperPrint, String outputName) {
        File outputFile = new File(outputName + ".pdf");
        try {
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputFile));
            exporter.exportReport();

            showSuccess("PDF export successful!\nSaved to: " + outputFile.getAbsolutePath(), "Export Success");
        } catch (JRException e) {
            showError("Error exporting to PDF: " + e.getMessage(), "Export Failed");
            e.printStackTrace();
        }
    }

    // Helper method for error messages
    private static void showError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Helper method for success messages
    private static void showSuccess(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}