package lyfjshs.gomis.utils;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class IncidentReportGenerator {
	private static final String DEFAULT_JASPER_TEMPLATE = "src/main/resources/jasperTemplates/templates/IncidentReport_Final.jasper";

	public static void createINITIALIncidentReport(Component parent) {
		String name = "";
		String gradeSection = "";
		String date = "";
		String timeVisit = "";
		String incidentReport = "";
		
//		generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, incidentReport, "print", null);

//		JFileChooser fileChooser = setupFileChooser("docx");
//		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
//			String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
//			generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, incidentReport, "docx",
//					baseName);
//		}

		JFileChooser fileChooser = setupFileChooser("pdf");
		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
			generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, incidentReport, "pdf",
					baseName);
		}
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
	private static void generateReport(String jasperTemplate, String name, String gradeSection, String date,
			String timeVisit, String incidentReport, String action, String outputName) {
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
			JOptionPane.showMessageDialog(null, "Failed to generate the report: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}