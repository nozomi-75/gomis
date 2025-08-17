/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package docPrinter.goodMoral;

import java.awt.Component;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.view.students.StudentsListMain;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.Toast;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class goodMoralModalPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JLabel lblNewLabel_8;
	private JTextArea purposeTextArea;
	private JLabel lblNewLabel_9;
	private JFormattedTextField dateGivenField;
	private JLabel lblNewLabel_10;
	private JComboBox<String> certificateSignerDrop;
	private JLabel templateUsedLabel;
	private JLabel lblNewLabel_2;
	private JPanel SP_panel;
	private JScrollPane SP_selectedStud;
	private JSeparator separator;
	private List<Student> selectedStudents;
	private File templateFile;
	private File outputFolder;
	private List<GoodMoralStudentInfoPanel> studentPanels = new ArrayList<>();
	private JTextField fullNameField;
	private JTextField workPositionField;
	private JPanel otherSignerPanel;
	private DatePicker datePicker;
	private JLabel formattedDateLabel;
	private final goodMoralGenarate generator;

	/**
	 * Create the panel.
	 */
	public goodMoralModalPanel(goodMoralGenarate generator) {
		this.generator = generator;
		setLayout(new MigLayout("", "[][grow][]", "[][][][][][:100px:100px][][][][][][][][]"));
		lblNewLabel_2 = new JLabel("Generating certificates for 0 students with shared details below");
		add(lblNewLabel_2, "cell 0 1 3 1,alignx center");
		SP_selectedStud = new JScrollPane();
		add(SP_selectedStud, "cell 1 2,grow");
		SP_panel = new JPanel();
		SP_panel.setLayout(new BoxLayout(SP_panel, BoxLayout.Y_AXIS));
		SP_selectedStud.setViewportView(SP_panel);
		// Add info label below the student info panel
		JLabel infoLabel = new JLabel("Note: Changes made here only affect the generated certificate and do not update the database.");
		infoLabel.setFont(infoLabel.getFont().deriveFont(10f));
		add(infoLabel, "cell 1 3, alignx left");
		
		separator = new JSeparator();
		add(separator, "cell 0 3 3 1");
		lblNewLabel_8 = new JLabel("Purpose");
		add(lblNewLabel_8, "cell 1 4");
		purposeTextArea = new JTextArea();
		add(purposeTextArea, "cell 1 5,grow");
		lblNewLabel_9 = new JLabel("Date to be Given");
		add(lblNewLabel_9, "cell 1 6");
		// DatePicker setup
		datePicker = new DatePicker();
		datePicker.setSelectedDate(LocalDate.now());
		dateGivenField = new JFormattedTextField();
		datePicker.setEditor(dateGivenField);
		add(dateGivenField, "cell 1 7,growx");
		// Formatted date label
		formattedDateLabel = new JLabel();
		updateFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
		datePicker.addDateSelectionListener(e -> updateFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate()));
		add(formattedDateLabel, "cell 1 8,alignx left");
		lblNewLabel_10 = new JLabel("Certificate Signer");
		add(lblNewLabel_10, "cell 1 9");
		certificateSignerDrop = new JComboBox<String>();
		setupSignerDropdown();
		add(certificateSignerDrop, "cell 1 10,growx");
		
		// Other signer panel
		otherSignerPanel = new JPanel(new MigLayout("", "[][grow]", "[][]"));
		otherSignerPanel.setVisible(false);
		fullNameField = new JTextField(20);
		workPositionField = new JTextField(20);
		otherSignerPanel.add(new JLabel("Full Name:"), "cell 0 0,alignx trailing");
		otherSignerPanel.add(fullNameField, "cell 1 0,growx");
		otherSignerPanel.add(new JLabel("Position:"), "cell 0 1,alignx trailing");
		otherSignerPanel.add(workPositionField, "cell 1 1,growx");
		add(otherSignerPanel, "cell 1 11,span 2,growx");
		
		// Add signer combo box listener
		certificateSignerDrop.addActionListener(e -> {
			boolean isOther = "Other".equals(certificateSignerDrop.getSelectedItem());
			otherSignerPanel.setVisible(isOther);
		});
		
		templateUsedLabel = new JLabel("Template: NAME_OF_TEMPLATE");
		add(templateUsedLabel, "cell 1 13");
	}

	private void setupSignerDropdown() {
		String currentSigner = "TEST 123, TEST 12345";
		if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
			GuidanceCounselor counselor = Main.formManager.getCounselorObject();
			currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
		}

		String[] signersAndPosition = new String[] { 
			"-Select Who to Sign-", 
			currentSigner,
			"SALLY P. GENUINO, Principal II", 
			"RACQUEL D. COMANDANTE, Guidance Designate",
			"Other" 
		};
		certificateSignerDrop.setModel(new javax.swing.DefaultComboBoxModel<>(signersAndPosition));
	}

	public void setSelectedStudents(List<Student> students) {
		this.selectedStudents = students;
		lblNewLabel_2.setText("Generating certificates for " + students.size() + " student" + (students.size() > 1 ? "s" : "") + " with shared details below");
		SP_panel.removeAll();
		studentPanels.clear();
		for (Student s : students) {
			String name = s.getStudentLastname() + ", " + s.getStudentFirstname();
			if (s.getStudentMiddlename() != null && !s.getStudentMiddlename().isEmpty()) {
				name += " " + s.getStudentMiddlename().charAt(0) + ".";
			}
			String lrn = s.getStudentLrn();
			String schoolYear = s.getSchoolForm() != null ? s.getSchoolForm().getSF_SCHOOL_YEAR() : "";
			String gradeNsection = s.getSchoolForm() != null ? s.getSchoolForm().getSF_GRADE_LEVEL() + "-" + s.getSchoolForm().getSF_SECTION() : "";
			String trackNstrand = s.getSchoolForm() != null ? s.getSchoolForm().getSF_TRACK_AND_STRAND() : "";
			String specialization = s.getSchoolForm() != null ? (s.getSchoolForm().getSF_COURSE() != null ? s.getSchoolForm().getSF_COURSE() : "") : "";
			
			GoodMoralStudentInfoPanel panel = new GoodMoralStudentInfoPanel(name, lrn, schoolYear, gradeNsection, trackNstrand, specialization);
			studentPanels.add(panel);
			SP_panel.add(panel);
		}
		SP_panel.revalidate();
		SP_panel.repaint();
	}

	public void setTemplateAndOutput(File templateFile, File outputFolder) {
		this.templateFile = templateFile;
		this.outputFolder = outputFolder;
		templateUsedLabel.setText("Template: " + (templateFile != null ? templateFile.getName() : "None"));
	}

	public void showModal(StudentsDataDAO dbManager, Component parent, Runnable onPrint) {
		try {
			Option option = ModalDialog.getDefaultOption();
			option.setOpacity(0f).setAnimationOnClose(false).getBorderOption().setBorderWidth(0.5f)
					.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);
			option.getLayoutOption().setMargin(0, 0, 0, 0).setSize(Math.max(800, parent.getSize().getWidth()-200), Math.max(600, parent.getSize().getHeight()-100));
			
			// Define options for the SimpleModalBorder - similar to reference implementations
			SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
					new SimpleModalBorder.Option("Print " + (selectedStudents != null ? selectedStudents.size() : 0) + " Certificate(s)", SimpleModalBorder.YES_OPTION),
					new SimpleModalBorder.Option("Export as DOCX", SimpleModalBorder.CANCEL_OPTION),
					new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
			};
			
			ModalDialog.showModal(parent,
					new SimpleModalBorder(this, "Good Moral Certificate Generator",
							modalOptions,
							(controller, action) -> {
								boolean success = false;
								if (action == SimpleModalBorder.YES_OPTION) {
									success = processCertificates("print", null, parent, onPrint);
									if (success) controller.close();
									else controller.consume();
								} else if (action == SimpleModalBorder.CANCEL_OPTION) {
									success = processCertificates("docx", null, parent, onPrint);
									if (success) controller.close();
									else controller.consume();
								} else if (action == SimpleModalBorder.CLOSE_OPTION) {
									controller.close();
								}
							}),
					option, "goodMoralGenerator");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean processCertificates(String action, String outputDirectory, Component parent, Runnable onPrint) {
		if (templateFile == null || outputFolder == null) {
			showToast("Template or output folder not set!", Toast.Type.ERROR, parent);
			return false;
		}
		
		// Validate inputs
		if (!validateInputs(parent)) {
			return false;
		}
		
		// Process each student with their edited values
		for (int i = 0; i < selectedStudents.size(); i++) {
			Student student = selectedStudents.get(i);
			GoodMoralStudentInfoPanel studentPanel = studentPanels.get(i);
			
			// Prepare the complete map of values for the template.
			Map<String, String> values = new HashMap<>();
			values.put("purpose", purposeTextArea.getText());
			values.put("formatDateGiven", formattedDateLabel.getText());
			
			// Get edited values from the student panel
			values.put("studentName", studentPanel.getStudentNameValue());
			values.put("schoolYear", studentPanel.getSchoolYearValue());
			values.put("gradeAndSection", studentPanel.getGradeAndSectionValue());
			values.put("trackAndStrand", studentPanel.getTrackAndStrandValue());
			values.put("specialization", studentPanel.getSpecializationValue());
			
			// Handle LRN value
			if (studentPanel.isIncludeLrnChecked()) {
				values.put("withLRN", "LRN: " + studentPanel.getLrnValue());
			} else {
				values.put("withLRN", "");
			}
			
			// Handle signer information
			String selectedSigner = (String) certificateSignerDrop.getSelectedItem();
			if ("Other".equals(selectedSigner)) {
				values.put("certificateSigner", fullNameField.getText().trim());
				values.put("signerPosition", workPositionField.getText().trim());
			} else if (selectedSigner != null && !selectedSigner.equals("-Select Who to Sign-")) {
				String[] signerParts = selectedSigner.split(", ");
				values.put("certificateSigner", signerParts.length > 0 ? signerParts[0] : "");
				values.put("signerPosition", signerParts.length > 1 ? signerParts[1] : "");
			}
			
			// Call the generator with the complete map
			generator.generateGoodMoralDocx(student, outputFolder, templateFile, values, action);
		}
		
		if (onPrint != null) onPrint.run();
		if (parent instanceof StudentsListMain) {
			((StudentsListMain) parent).loadData();
		}
		return true;
	}
	
	private boolean validateInputs(Component parent) {
		if (purposeTextArea.getText().trim().isEmpty()) {
			showToast("Please enter a purpose for the certificate.", Toast.Type.ERROR, parent);
			return false;
		}
		
		if (formattedDateLabel.getText().trim().isEmpty()) {
			showToast("Please enter a date to be given.", Toast.Type.ERROR, parent);
			return false;
		}
		
		String selectedSigner = (String) certificateSignerDrop.getSelectedItem();
		if (selectedSigner == null || selectedSigner.equals("-Select Who to Sign-")) {
			showToast("Please select a certificate signer.", Toast.Type.ERROR, parent);
			return false;
		}
		
		if ("Other".equals(selectedSigner)) {
			if (fullNameField.getText().trim().isEmpty()) {
				showToast("Please enter the full name for the custom signer.", Toast.Type.ERROR, parent);
				return false;
			}
			if (workPositionField.getText().trim().isEmpty()) {
				showToast("Please enter the position for the custom signer.", Toast.Type.ERROR, parent);
				return false;
			}
		}
		
		return true;
	}

	private void updateFormattedDateLabel(JLabel label, LocalDate date) {
		int day = date.getDayOfMonth();
		String suffix = getOrdinalSuffix(day);
		label.setText(day + suffix + " of " + date.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
	}

	private String getOrdinalSuffix(int day) {
		if (day >= 11 && day <= 13) return "th";
		switch (day % 10) {
			case 1: return "st";
			case 2: return "nd";
			case 3: return "rd";
			default: return "th";
		}
	}

	private void showToast(String message, Toast.Type type, Component parent) {
		ToastOption toastOption = Toast.createOption();
		toastOption.getLayoutOption()
				.setMargin(0, 0, 50, 0)
				.setDirection(ToastDirection.TOP_TO_BOTTOM);
		Toast.show(parent, type, message, ToastLocation.BOTTOM_CENTER, toastOption);
	}
}
