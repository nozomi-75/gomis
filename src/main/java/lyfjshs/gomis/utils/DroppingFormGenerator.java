package lyfjshs.gomis.utils;

import java.awt.Component;
import java.io.File;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class DroppingFormGenerator {
	private static final String DEFAULT_JASPER_TEMPLATE = "src/main/resources/jasperTemplates/templates/DroppingForm_Final.jasper";

	public static void createDroppingForm(Component parent, String date, String nameOfStudent, String adviser,
			String trackStrand, String gradeSection, String inclusiveDates, String actionTaken,
			String reasonForDropping, String effectiveDate) {

		generateReport(DEFAULT_JASPER_TEMPLATE, date, nameOfStudent, adviser, trackStrand, gradeSection, inclusiveDates,
				actionTaken, reasonForDropping, effectiveDate, "print", null);

//		EXPORT AS DOCX
//		JFileChooser fileChooser = setupFileChooser("docx");
//		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
//			String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(".docx", "");
//			generateReport(DEFAULT_JASPER_TEMPLATE, date, nameOfStudent, adviser, trackStrand, gradeSection,
//					inclusiveDates, actionTaken, reasonForDropping, effectiveDate, "docx", baseName);
//		}
//		EXPORT AS PDF
//		JFileChooser fileChooser = setupFileChooser("pdf");
//		if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
//			String baseName = fileChooser.getSelectedFile().getAbsolutePath().replace(".pdf", "");
//			generateReport(DEFAULT_JASPER_TEMPLATE, date, nameOfStudent, adviser, trackStrand, gradeSection,
//					inclusiveDates, actionTaken, reasonForDropping, effectiveDate, "pdf", baseName);
//		}

	}

	/**
	 * Sets up a JFileChooser for saving files with the specified extension.
	 */
	private static JFileChooser setupFileChooser(String extension) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Dropping Form");
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
		fileChooser.setSelectedFile(new File("DroppingForm." + extension));
		return fileChooser;
	}

	/**
	 * Generates the Dropping Form using ReportGenerator.
	 */
	private static void generateReport(String jasperTemplate, String date, String nameOfStudent, String adviser,
			String trackStrand, String gradeSection, String inclusiveDates, String actionTaken,
			String reasonForDropping, String effectiveDate, String action, String outputName) {
		try {
			// Default output name if not provided
			outputName = (outputName != null) ? outputName : "DroppingForm";

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("Date", date);
			parameters.put("NameofStudent", nameOfStudent);
			parameters.put("Adviser", adviser);
			parameters.put("Trank/Strand Specialization", trackStrand);
			parameters.put("Grade/Section", gradeSection);
			parameters.put("InclusiveDateofAbsences", inclusiveDates);
			parameters.put("ActionTaken", actionTaken);
			parameters.put("ReasonforDropping", reasonForDropping);
			parameters.put("EffectiveDate", effectiveDate);
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