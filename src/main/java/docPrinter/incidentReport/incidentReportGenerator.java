/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package docPrinter.incidentReport;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import docPrinter.templateManager;

public class incidentReportGenerator {
    private static final Logger logger = LogManager.getLogger(incidentReportGenerator.class);
    
    public incidentReportGenerator() {
	}

	/**
	 * Generate incident report template without any data (clean template)
	 */
	public boolean generateIncidentReport(File outputFolder, File templateFile, String action) {
		return generateIncidentReport(outputFolder, templateFile, null, action);
	}

	/**
	 * Generate incident report with actual incident data
	 */
	public boolean generateIncidentReport(File outputFolder, File templateFile, Map<String, String> incidentData, String action) {
		try {
			// Use templateManager to get the active incident report template
			File activeTemplate = templateManager.getActiveTemplate(templateManager.TemplateType.INCIDENT_REPORT);
			
			if (!activeTemplate.exists()) {
				logger.error("Incident report template not found: " + activeTemplate.getAbsolutePath());
				return false;
			}
			
			// Load the template
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(activeTemplate);
			MainDocumentPart documentPart = pkg.getMainDocumentPart();

			// If incident data is provided, replace placeholders
			if (incidentData != null && !incidentData.isEmpty()) {
				replacePlaceholders(documentPart, incidentData);
			}

			// Generate appropriate filename based on action and data
			String fileName = incidentData != null ? 
				"IncidentReport_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".docx" :
				"IncidentReport_Template.docx";
			File outputFile = new File(outputFolder, fileName);
			
			// Handle different export types
			switch (action.toLowerCase()) {
				case "print":
					pkg.save(outputFile);
					openFileForPrinting(outputFile);
					break;
				case "docx":
				default:
					pkg.save(outputFile);
					break;
			}
			
			logger.info("Incident report generated successfully: " + outputFile.getAbsolutePath());
			return true;
		} catch (Exception e) {
			logger.error("Error generating incident report", e);
			return false;
		}
	}

	/**
	 * Replace placeholders in the document with actual data
	 */
	private void replacePlaceholders(MainDocumentPart documentPart, Map<String, String> data) {
		try {
			// Use docx4j's built-in placeholder replacement with Map
			documentPart.variableReplace(data);
		} catch (Exception e) {
			logger.error("Error replacing placeholders in incident report", e);
		}
	}

	/**
	 * Create a sample incident data map for testing
	 */
	public static Map<String, String> createSampleIncidentData() {
		Map<String, String> data = new HashMap<>();
		data.put("REPORTED_BY", "John Doe");
		data.put("DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
		data.put("TIME", LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
		data.put("GRADE_SECTION", "Grade 10 - Section A");
		data.put("NARRATIVE_REPORT", "Sample narrative report describing the incident...");
		data.put("ACTIONS_TAKEN", "Sample actions taken in response to the incident...");
		data.put("RECOMMENDATIONS", "Sample recommendations for future prevention...");
		data.put("STATUS", "Active");
		return data;
	}

	private void openFileForPrinting(File file) {
		try {
			java.awt.Desktop.getDesktop().open(file);
		} catch (Exception e) {
			logger.error("Could not open file for printing", e);
		}
	}
}

