/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package docPrinter.goodMoral;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class GoodMoralStudentInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel studentNameLabel;
	private JTextField lrnField;
	private JTextField schoolYearField;
	private JTextField trackNstrandField;
	private JTextField gradeNsectionField;
	private JCheckBox includeLrnCheckBox;
	private JSeparator separator;
	private JTextField specializationField;
	private JTextField studentNameField;

	public GoodMoralStudentInfoPanel(String name, String lrn, String schoolYear, String gradeNsection, String trackNstrand, String specialization) {		
		this.setLayout(new MigLayout("", "[grow][][grow][]", "[][][][][][][][]"));
		
		// Student name section
		studentNameLabel = new JLabel("Name: " + name);
		studentNameField = new JTextField(name, 20);
		this.add(studentNameLabel, "cell 0 0");
		this.add(studentNameField, "cell 0 1,growx");
		
		// LRN section
		lrnField = new JTextField(lrn, 12);
		includeLrnCheckBox = new JCheckBox("Include LRN", true);
		this.add(new JLabel("LRN"), "cell 0 2");
		this.add(lrnField, "cell 0 3,growx");
		this.add(includeLrnCheckBox, "cell 1 3");

		// School Year section
		schoolYearField = new JTextField(schoolYear, 10);
		this.add(new JLabel("School Year"), "cell 2 2");
		this.add(schoolYearField, "cell 2 3,growx");
		
		// Grade & Section section
		gradeNsectionField = new JTextField(gradeNsection);
		this.add(new JLabel("Grade & Section"), "cell 0 4");
		this.add(gradeNsectionField, "cell 0 5,growx");

		// Track & Strand section
		trackNstrandField = new JTextField(trackNstrand);
		this.add(new JLabel("Track & Strand"), "cell 2 4");
		this.add(trackNstrandField, "cell 2 5,growx");
		
		// Specialization section
		specializationField = new JTextField(specialization);
		this.add(new JLabel("Specialization"), "cell 0 6");
		this.add(specializationField, "cell 0 7,growx");
		
		separator = new JSeparator();
		separator.setPreferredSize(new java.awt.Dimension(1, 8));
		add(separator, "cell 0 8 4 1, growx, gapy 8");
	}

	/**
	 * Returns true if the 'Include LRN' checkbox is checked.
	 */
	public boolean isIncludeLrnChecked() {
		return includeLrnCheckBox.isSelected();
	}
	
	/**
	 * Get the edited LRN value
	 */
	public String getLrnValue() {
		return lrnField.getText().trim();
	}
	
	/**
	 * Get the edited school year value
	 */
	public String getSchoolYearValue() {
		return schoolYearField.getText().trim();
	}
	
	/**
	 * Get the edited grade and section value
	 */
	public String getGradeAndSectionValue() {
		return gradeNsectionField.getText().trim();
	}
	
	/**
	 * Get the edited track and strand value
	 */
	public String getTrackAndStrandValue() {
		return trackNstrandField.getText().trim();
	}
	
	/**
	 * Get the edited specialization value
	 */
	public String getSpecializationValue() {
		return specializationField.getText().trim();
	}
	
	/**
	 * Get the edited student name value
	 */
	public String getStudentNameValue() {
		return studentNameField.getText().trim();
	}
	
	/**
	 * Set the specialization value
	 */
	public void setSpecializationValue(String specialization) {
		specializationField.setText(specialization);
	}
}
