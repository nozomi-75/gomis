/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package docPrinter.droppingForm;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;

import jakarta.xml.bind.JAXBElement;
import lyfjshs.gomis.Database.entity.Student;

// a class to generate a dropping form
public class droppingFormGenerator {
	private static final Logger logger = LogManager.getLogger(droppingFormGenerator.class);

	public droppingFormGenerator() {
	}

	/**
	 * Generates a Dropping Form by merging a map of values with a DOCX template.
	 * @param student The student object, used for generating the filename.
	 * @param outputFolder The folder to save the generated file in.
	 * @param templateFile The DOCX template file.
	 * @param values A map containing all the key-value pairs to be replaced in the template.
	 * @param action The action to perform ("print" or "docx").
	 * @return True if successful, false otherwise.
	 */
	public boolean generateDroppingFormDocx(Student student, File outputFolder, File templateFile, Map<String, String> values, String action) {
		try {
			WordprocessingMLPackage pkg = WordprocessingMLPackage.load(templateFile);

			// Use both methods to cover placeholders and content controls.
			pkg.getMainDocumentPart().variableReplace(values);
			replaceContentControls(pkg, values);

			// Replace in headers/footers too:
			pkg.getDocumentModel().getSections().forEach(section -> {
				if (section.getHeaderFooterPolicy() != null) {
					try {
						if (section.getHeaderFooterPolicy().getDefaultHeader() != null)
							section.getHeaderFooterPolicy().getDefaultHeader().variableReplace(values);
					} catch (Exception e) { logger.debug("Error processing default header", e); }
					try {
						if (section.getHeaderFooterPolicy().getFirstHeader() != null)
							section.getHeaderFooterPolicy().getFirstHeader().variableReplace(values);
					} catch (Exception e) { logger.debug("Error processing first header", e); }
					try {
						if (section.getHeaderFooterPolicy().getEvenHeader() != null)
							section.getHeaderFooterPolicy().getEvenHeader().variableReplace(values);
					} catch (Exception e) { logger.debug("Error processing even header", e); }
					try {
						if (section.getHeaderFooterPolicy().getDefaultFooter() != null)
							section.getHeaderFooterPolicy().getDefaultFooter().variableReplace(values);
					} catch (Exception e) { logger.debug("Error processing default footer", e); }
					try {
						if (section.getHeaderFooterPolicy().getFirstFooter() != null)
							section.getHeaderFooterPolicy().getFirstFooter().variableReplace(values);
					} catch (Exception e) { logger.debug("Error processing first footer", e); }
					try {
						if (section.getHeaderFooterPolicy().getEvenFooter() != null)
							section.getHeaderFooterPolicy().getEvenFooter().variableReplace(values);
					} catch (Exception e) { logger.debug("Error processing even footer", e); }
				}
			});

			// Generate appropriate filename based on action
			String fileName = generateFileName(student);
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
			
			return true;
		} catch (Exception e) {
			logger.error("Error generating dropping form", e);
			return false;
		}
	}

	private String generateFileName(Student student) {
		String baseName = student.getStudentLastname() + "_" + student.getStudentFirstname() + "_DroppingForm";
		return baseName + ".docx";
	}

	private void openFileForPrinting(File file) {
		try {
			java.awt.Desktop.getDesktop().open(file);
		} catch (Exception e) {
			logger.error("Could not open file for printing: " + e.getMessage(), e);
		}
	}

	private void replaceContentControls(WordprocessingMLPackage pkg, Map<String, String> values) {
		replaceContentControlsInPart(pkg.getMainDocumentPart(), values);
		pkg.getDocumentModel().getSections().forEach(section -> {
			try {
				if (section.getHeaderFooterPolicy() != null) {
					if (section.getHeaderFooterPolicy().getDefaultHeader() != null)
						replaceContentControlsInPart(section.getHeaderFooterPolicy().getDefaultHeader(), values);
					if (section.getHeaderFooterPolicy().getFirstHeader() != null)
						replaceContentControlsInPart(section.getHeaderFooterPolicy().getFirstHeader(), values);
					if (section.getHeaderFooterPolicy().getEvenHeader() != null)
						replaceContentControlsInPart(section.getHeaderFooterPolicy().getEvenHeader(), values);
					if (section.getHeaderFooterPolicy().getDefaultFooter() != null)
						replaceContentControlsInPart(section.getHeaderFooterPolicy().getDefaultFooter(), values);
					if (section.getHeaderFooterPolicy().getFirstFooter() != null)
						replaceContentControlsInPart(section.getHeaderFooterPolicy().getFirstFooter(), values);
					if (section.getHeaderFooterPolicy().getEvenFooter() != null)
						replaceContentControlsInPart(section.getHeaderFooterPolicy().getEvenFooter(), values);
				}
			} catch (Exception ex) {
				logger.error("Error processing content controls in section", ex);
			}
		});
	}

	private void replaceContentControlsInPart(ContentAccessor part, Map<String, String> values) {
		List<Object> sdtList = getAllElementFromObject(part, SdtElement.class);
		for (Object obj : sdtList) {
			SdtElement sdt = (SdtElement) obj;
			SdtPr sdtPr = sdt.getSdtPr();
			if (sdtPr != null && sdtPr.getTag() != null) {
				String tagVal = sdtPr.getTag().getVal();
				logger.info("Found content control tag: " + tagVal);
				if (values.containsKey(tagVal)) {
					logger.info("Replacing tag '" + tagVal + "' with value: " + values.get(tagVal));
					setContentControlText(sdt, values.get(tagVal));
				} else {
					logger.info("No value found for tag: " + tagVal);
				}
			}
		}
	}

	private List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
		List<Object> result = new java.util.ArrayList<>();
		if (obj instanceof JAXBElement) obj = ((JAXBElement<?>) obj).getValue();
		if (obj == null) return result;
		if (toSearch.isAssignableFrom(obj.getClass())) {
			result.add(obj);
		} else if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}
		}
		return result;
	}

	private void setContentControlText(SdtElement sdt, String value) {
		List<Object> texts = getAllElementFromObject(sdt, Text.class);
		for (Object t : texts) {
			((Text) t).setValue(value);
		}
	}
}