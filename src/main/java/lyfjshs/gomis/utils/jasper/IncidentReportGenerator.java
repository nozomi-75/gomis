package lyfjshs.gomis.utils.jasper;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

public class IncidentReportGenerator {
	private static final String DEFAULT_JASPER_TEMPLATE = "src/main/resources/jasperTemplates/templates/IncidentReport_Final.jasper";

	public static void createINITIALIncidentReport(Component parent) {
		String name = "";
		String gradeSection = "";
		String date = "";
		String timeVisit = "";
		String incidentReport = "";
		
		generateReport(DEFAULT_JASPER_TEMPLATE, name, gradeSection, date, timeVisit, incidentReport, "print", null);

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