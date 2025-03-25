package lyfjshs.gomis.view.students;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class DroppingForm extends JPanel {
	private JFormattedTextField dateField, effectiveDateField;
	private JTextField lrnField, nameField, adviserField, absencesField, gradeSecField;
	private JTextArea reasonTextArea;
	private JTextField trackField;
	private DatePicker dateChooser, effectiveDateChooser;

//	private final String[] trackOptions = { 
//			"Accountancy, Business, and Management (ABM)",
//			"Humanities and Social Sciences (HUMSS)", 
//			"Science, Technology, Engineering, and Mathematics (STEM)",
//			"Technical-Vocational-Livelihood (TVL) Home Economics",
//			"Technical-Vocational-Livelihood (TVL) Industrial Arts",
//			"Technical-Vocational-Livelihood (TVL) ICT - Programming",
//			"Technical-Vocational-Livelihood (TVL) ICT - Technical Drafting" };

	private Connection connection;
	private JLabel lblNewLabel;
	private JTextArea actionTextArea;

	public DroppingForm(Connection conn, Student studee, GuidanceCounselor counselor) {
		this.connection = conn;

		// Initialize components first
		initializeComponents();
		// Then setup layout
		setupLayout();
		// Finally populate fields
		populateFields(studee, counselor);
	}

	private void initializeComponents() {
		dateField = new JFormattedTextField(new SimpleDateFormat("MM/dd/yyyy"));
		lrnField = new JTextField(20);
		nameField = new JTextField(20);
		adviserField = new JTextField(20);
		absencesField = new JTextField(20);
		gradeSecField = new JTextField(20);
		reasonTextArea = new JTextArea(5, 40);
		reasonTextArea.setLineWrap(true);
		reasonTextArea.setWrapStyleWord(true);
		actionTextArea = new JTextArea(5, 40);
		actionTextArea.setLineWrap(true);
		actionTextArea.setWrapStyleWord(true);
		trackField = new JTextField();
		dateChooser = new DatePicker();
		dateChooser.setEditor(dateField);
		effectiveDateChooser = new DatePicker();
	}

	private void setupLayout() {
		setLayout(new MigLayout("wrap 4, insets 20", "[150]10[250]10[]10[150]10[250]", "[][][][][][187.00,grow][]"));

		this.add(new JLabel("Date:"), "cell 0 0");
		this.add(dateField, "cell 1 0,growx");
		this.add(new JLabel("LRN:"), "cell 3 0");
		this.add(lrnField, "cell 4 0,growx");

		this.add(new JLabel("Name of Student:"), "cell 0 1");
		this.add(nameField, "cell 1 1,growx");
		this.add(new JLabel("Track/Strand-Specialization:"), "cell 3 1");
		this.add(trackField, "cell 4 1,growx");

		this.add(new JLabel("Adviser:"), "cell 0 2");
		this.add(adviserField, "cell 1 2,growx");
		this.add(new JLabel("Grade & Section:"), "cell 3 2");
		this.add(gradeSecField, "cell 4 2,growx");

		this.add(new JLabel("Inclusive date of absences:"), "cell 0 3");
		this.add(absencesField, "cell 1 3,growx");

		JLabel label_1 = new JLabel("Effective Date:");
		this.add(label_1, "cell 3 3");
		effectiveDateField = new JFormattedTextField();
		effectiveDateChooser.setEditor(effectiveDateField);
		this.add(effectiveDateField, "cell 4 3,grow");

		lblNewLabel = new JLabel("Action Taken");
		this.add(lblNewLabel, "cell 0 4");

		JLabel label = new JLabel("Reason for dropping:");
		this.add(label, "cell 3 4");

		
		this.add(new JScrollPane(actionTextArea), "cell 0 5 2 1,grow");
		
		this.add(new JScrollPane(reasonTextArea), "cell 3 5 2 1,grow");
	}

	public void resetForm() {
		dateField.setText("");
		lrnField.setText("");
		nameField.setText("");
		adviserField.setText("");
		absencesField.setText("");
		gradeSecField.setText("");
		effectiveDateField.setText("");
		reasonTextArea.setText("");
		actionTextArea.setText("");
		trackField.setText("");
	}

	private void populateFields(Student studee, GuidanceCounselor counselor) {
		if (studee != null) {
			String studentName = String.format("%s %s %s",
				studee.getStudentLastname() != null ? studee.getStudentLastname() : "",
				studee.getStudentFirstname() != null ? studee.getStudentFirstname() : "",
				studee.getStudentMiddlename() != null ? studee.getStudentMiddlename() : "").trim();
			
			nameField.setText(studentName);
			lrnField.setText(studee.getStudentLrn());	
			gradeSecField.setText(studee.getSchoolForm().getSF_GRADE_LEVEL() + " " + studee.getSchoolSection());
			
			if (studee.getSchoolForm() != null) {
				trackField.setText(studee.getSchoolForm().getSF_TRACK_AND_STRAND());
			}
		}
		
		if (counselor != null) {
			String counselorName = String.format("%s %s",
				counselor.getFirstName() != null ? counselor.getFirstName() : "",
				counselor.getLastName() != null ? counselor.getLastName() : "").trim();
			
			adviserField.setText(counselorName);
		}
	}

	private void DropStudent(Connection connend) {
		// method to drop student
	}
}
