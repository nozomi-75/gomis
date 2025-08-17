/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package docPrinter.droppingForm;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class DroppingFormStudentInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel studentNameLabel;
	private JTextField studentNameField;
	private JTextField adviserField;
	private JTextField trackStrandField;
	private JTextField gradeSectionField;
	private JTextField inclusiveDatesField;
	private JTextField actionTakenField;
	private JTextField reasonForDroppingField;
	private JTextField effectiveDateField;
	private JSeparator separator;

	public DroppingFormStudentInfoPanel(String name, String adviser, String trackStrand, String gradeSection) {		
		this.setLayout(new MigLayout("", "[grow][][grow][]", "[][][][][][][][][][][][]"));
		
		// Student name section
		studentNameLabel = new JLabel("Name: " + name);
		studentNameField = new JTextField(name, 20);
		this.add(studentNameLabel, "cell 0 0");
		this.add(studentNameField, "cell 0 1,growx");
		
		// Adviser section
		adviserField = new JTextField(adviser, 20);
		this.add(new JLabel("Adviser"), "cell 0 2");
		this.add(adviserField, "cell 0 3,growx");

		// Track & Strand section
		trackStrandField = new JTextField(trackStrand, 20);
		this.add(new JLabel("Track/Strand"), "cell 2 2");
		this.add(trackStrandField, "cell 2 3,growx");
		
		// Grade & Section section
		gradeSectionField = new JTextField(gradeSection, 20);
		this.add(new JLabel("Grade & Section"), "cell 0 4");
		this.add(gradeSectionField, "cell 0 5,growx");

		// Inclusive Dates section
		inclusiveDatesField = new JTextField(20);
		this.add(new JLabel("Inclusive Dates of Absences"), "cell 2 4");
		this.add(inclusiveDatesField, "cell 2 5,growx");
		
		// Action Taken section
		actionTakenField = new JTextField(20);
		this.add(new JLabel("Action Taken"), "cell 0 6");
		this.add(actionTakenField, "cell 0 7,growx");
		
		// Reason for Dropping section
		reasonForDroppingField = new JTextField(20);
		this.add(new JLabel("Reason for Dropping"), "cell 2 6");
		this.add(reasonForDroppingField, "cell 2 7,growx");
		
		// Effective Date section
		effectiveDateField = new JTextField(20);
		this.add(new JLabel("Effective Date"), "cell 0 8");
		this.add(effectiveDateField, "cell 0 9,growx");
		
		separator = new JSeparator();
		separator.setPreferredSize(new java.awt.Dimension(1, 8));
		add(separator, "cell 0 10 4 1, growx, gapy 8");
	}

	/**
	 * Get the edited student name value
	 */
	public String getStudentNameValue() {
		return studentNameField.getText().trim();
	}
	
	/**
	 * Get the edited adviser value
	 */
	public String getAdviserValue() {
		return adviserField.getText().trim();
	}
	
	/**
	 * Get the edited track and strand value
	 */
	public String getTrackStrandValue() {
		return trackStrandField.getText().trim();
	}
	
	/**
	 * Get the edited grade and section value
	 */
	public String getGradeSectionValue() {
		return gradeSectionField.getText().trim();
	}
	
	/**
	 * Get the edited inclusive dates value
	 */
	public String getInclusiveDatesValue() {
		return inclusiveDatesField.getText().trim();
	}
	
	/**
	 * Get the edited action taken value
	 */
	public String getActionTakenValue() {
		return actionTakenField.getText().trim();
	}
	
	/**
	 * Get the edited reason for dropping value
	 */
	public String getReasonForDroppingValue() {
		return reasonForDroppingField.getText().trim();
	}
	
	/**
	 * Get the edited effective date value
	 */
	public String getEffectiveDateValue() {
		return effectiveDateField.getText().trim();
	}
	
	/**
	 * Set the adviser value
	 */
	public void setAdviserValue(String adviser) {
		adviserField.setText(adviser);
	}
	
	/**
	 * Set the track and strand value
	 */
	public void setTrackStrandValue(String trackStrand) {
		trackStrandField.setText(trackStrand);
	}
	
	/**
	 * Set the grade and section value
	 */
	public void setGradeSectionValue(String gradeSection) {
		gradeSectionField.setText(gradeSection);
	}
} 