package docPrinter.droppingForm;

import java.awt.Component;
import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.Toast;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class droppingFormModalPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField nameField, adviserField, absencesField, gradeSecField, trackField, specializationField;
	private JTextArea actionTextArea, reasonTextArea;
	private JFormattedTextField dateField, effectiveDateField;
	private DatePicker datePicker, effectiveDatePicker;
	private JComboBox<String> formSignerDrop;
	private JTextField fullNameField, workPositionField;
	private DropPanel otherSignerPanel;
	private JLabel templateUsedLabel;
	private File templateFile;
	private File outputFolder;
	private List<Student> selectedStudents;
	private final droppingFormGenerator generator;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public droppingFormModalPanel(droppingFormGenerator generator) {
		this.generator = generator;
		setLayout(new MigLayout("wrap 6, insets 20", "[150]10[100px:100px:200px]10[150]10[100px:100px:200px]",
				"[][][][][][][100px:150px:200px][][][][]"));

		// --- Student Info (show first selected student, or blank if none) ---
		add(new JLabel("Date:"), "cell 0 0");
		datePicker = new DatePicker();
		dateField = new JFormattedTextField();
		datePicker.setEditor(dateField);
		datePicker.setSelectedDate(LocalDate.now());
		add(dateField, "cell 1 0,growx");

		add(new JLabel("Name of Student:"), "cell 0 1");
		nameField = new JTextField(20);
		add(nameField, "cell 1 1,growx");
		add(new JLabel("Track/Strand:"), "cell 2 1");
		trackField = new JTextField(20);
		add(trackField, "cell 3 1,growx");
		JLabel label_2 = new JLabel("Grade & Section:");
		add(label_2, "cell 0 2");
		gradeSecField = new JTextField(20);
		add(gradeSecField, "cell 1 2,growx");
		JLabel label = new JLabel("Specialization:");
		add(label, "cell 2 2");
		specializationField = new JTextField(20);
		add(specializationField, "cell 3 2,growx");

		JLabel lblAdviserFullName = new JLabel("Adviser Full Name:");
		add(lblAdviserFullName, "cell 0 3");
		adviserField = new JTextField(20);
		add(adviserField, "cell 1 3,growx");
		JLabel label_3 = new JLabel("Effective Date:");
		add(label_3, "cell 2 3");
		effectiveDatePicker = new DatePicker();
		effectiveDateField = new JFormattedTextField();
		effectiveDatePicker.setEditor(effectiveDateField);
		effectiveDatePicker.setSelectedDate(LocalDate.now());
		add(effectiveDateField, "cell 3 3,growx");
		panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Inclusive Dates of Absences:"));
		add(panel, "cell 0 4 4 1,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[]"));
		absencesField = new JTextField(20);
		panel.add(absencesField, "cell 0 0,growx");

		add(new JLabel("Action Taken"), "cell 0 5");
		add(new JLabel("Reason for Dropping"), "cell 2 5");
		actionTextArea = new JTextArea(5, 40);
		actionTextArea.setLineWrap(true);
		actionTextArea.setWrapStyleWord(true);
		add(new JScrollPane(actionTextArea), "cell 0 6 2 1,grow");
		reasonTextArea = new JTextArea(5, 40);
		reasonTextArea.setLineWrap(true);
		reasonTextArea.setWrapStyleWord(true);
		add(new JScrollPane(reasonTextArea), "cell 2 6 2 1,grow");

		// --- Note ---
		JLabel infoLabel = new JLabel(
				"Note: Changes made here only affect the generated form and do not update the database.");
		infoLabel.setFont(infoLabel.getFont().deriveFont(10f));
		add(infoLabel, "cell 0 7 4 1,alignx center");

		// --- Signer selection ---
		add(new JLabel("Guidance Designate:"), "cell 0 8");
		formSignerDrop = new JComboBox<>();
		setupSignerDropdown();
		add(formSignerDrop, "cell 1 8 2 1,growx");

		otherSignerPanel = new DropPanel();
		JPanel signerFieldsPanel = new JPanel(new net.miginfocom.swing.MigLayout("", "[][grow]", "[][]"));
		fullNameField = new JTextField(20);
		workPositionField = new JTextField(20);
		signerFieldsPanel.add(new JLabel("Full Name:"), "cell 0 0,alignx trailing");
		signerFieldsPanel.add(fullNameField, "cell 1 0,growx");
		signerFieldsPanel.add(new JLabel("Position:"), "cell 0 1,alignx trailing");
		signerFieldsPanel.add(workPositionField, "cell 1 1,growx");
		otherSignerPanel.setContent(signerFieldsPanel);
		add(otherSignerPanel, "cell 0 10 4 1,growx");

		formSignerDrop.addActionListener(e -> {
			if ("Other".equals(formSignerDrop.getSelectedItem())) {
				otherSignerPanel.setDropdownVisible(true);
			} else {
				otherSignerPanel.setDropdownVisible(false);
			}
		});

		templateUsedLabel = new JLabel("Template: NAME_OF_TEMPLATE");
		add(templateUsedLabel, "cell 0 10 4 1");
	}

	private void setupSignerDropdown() {
		String currentSigner = "TEST 123, TEST 12345";
		if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
			GuidanceCounselor counselor = Main.formManager.getCounselorObject();
			currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
		}
		String[] signersAndPosition = new String[] { "-Select Who to Sign-", currentSigner,
				"SALLY P. GENUINO, Principal II", "RACQUEL D. COMANDANTE, Guidance Designate", "Other" };
		formSignerDrop.setModel(new javax.swing.DefaultComboBoxModel<>(signersAndPosition));
	}

	public void setSelectedStudents(List<Student> students) {
		this.selectedStudents = students;
		if (students == null || students.isEmpty())
			return;
		Student s = students.get(0); // For now, just show the first student
		String fullName = s.getStudentLastname() + ", " + s.getStudentFirstname()
				+ (s.getStudentMiddlename() != null && !s.getStudentMiddlename().isEmpty()
						? " " + s.getStudentMiddlename().charAt(0) + "."
						: "");
		nameField.setText(fullName);
		gradeSecField.setText(s.getSchoolForm() != null
				? s.getSchoolForm().getSF_GRADE_LEVEL() + "-" + s.getSchoolForm().getSF_SECTION()
				: "");
		trackField.setText(s.getSchoolForm() != null ? s.getSchoolForm().getSF_TRACK_AND_STRAND() : "");
		specializationField.setText(
				s.getSchoolForm() != null && s.getSchoolForm().getSF_COURSE() != null ? s.getSchoolForm().getSF_COURSE()
						: "");
		adviserField.setText(""); // Let user enter
		absencesField.setText("");
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
			option.getLayoutOption().setMargin(0, 0, 0, 0)
				.setSize(800, 800);
			SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
					new SimpleModalBorder.Option("Print Form", SimpleModalBorder.YES_OPTION),
					new SimpleModalBorder.Option("Export as DOCX", SimpleModalBorder.CANCEL_OPTION),
					new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION) };
			ModalDialog.showModal(parent,
					new SimpleModalBorder(this, "Dropping Form Generator", modalOptions, (controller, action) -> {
						boolean success = false;
						if (action == SimpleModalBorder.YES_OPTION) {
							success = processForm("print", parent, onPrint);
							if (success)
								controller.close();
							else
								controller.consume();
						} else if (action == SimpleModalBorder.CANCEL_OPTION) {
							success = processForm("docx", parent, onPrint);
							if (success)
								controller.close();
							else
								controller.consume();
						} else if (action == SimpleModalBorder.CLOSE_OPTION) {
							controller.close();
						}
					}), option, "droppingFormGenerator");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean processForm(String action, Component parent, Runnable onPrint) {
		if (templateFile == null || outputFolder == null) {
			showToast("Template or output folder not set!", Toast.Type.ERROR, parent);
			return false;
		}
		if (!validateInputs(parent)) {
			return false;
		}
		Map<String, String> values = new HashMap<>();
		values.put("Date", dateField.getText());
		values.put("Name", nameField.getText());
		values.put("LRN",
				selectedStudents != null && !selectedStudents.isEmpty() ? selectedStudents.get(0).getStudentLrn() : "");
		values.put("Adviser", adviserField.getText());
		values.put("TrackNStrand", trackField.getText());
		values.put("Specialization", specializationField.getText());
		values.put("GradeNSection", gradeSecField.getText());
		values.put("Inclusive", absencesField.getText());
		values.put("ActionTaken", actionTextArea.getText());
		values.put("ReasonForDropping", reasonTextArea.getText());
		values.put("EffectiveDate", effectiveDateField.getText());
		// Guidance Designate (UserAccount)
		String userAccount = null;
		if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
			var counselor = Main.formManager.getCounselorObject();
			userAccount = counselor.getFirstName() + " " + counselor.getLastName();
		} else if (fullNameField.getText() != null && !fullNameField.getText().trim().isEmpty()) {
			userAccount = fullNameField.getText().trim();
		} else {
			userAccount = "Guidance Designate";
		}
		values.put("UserAccount", userAccount);
		String selectedSigner = (String) formSignerDrop.getSelectedItem();
		if ("Other".equals(selectedSigner)) {
			values.put("formSigner", fullNameField.getText().trim());
			values.put("signerPosition", workPositionField.getText().trim());
		} else if (selectedSigner != null && !selectedSigner.equals("-Select Who to Sign-")) {
			String[] signerParts = selectedSigner.split(", ");
			values.put("formSigner", signerParts.length > 0 ? signerParts[0] : "");
			values.put("signerPosition", signerParts.length > 1 ? signerParts[1] : "");
		}
		generator.generateDroppingFormDocx(
				selectedStudents != null && !selectedStudents.isEmpty() ? selectedStudents.get(0) : null, outputFolder,
				templateFile, values, action);
		if (onPrint != null)
			onPrint.run();
		return true;
	}

	private boolean validateInputs(Component parent) {
		if (nameField.getText().trim().isEmpty()) {
			showToast("Please enter the student's name.", Toast.Type.ERROR, parent);
			return false;
		}
		if (adviserField.getText().trim().isEmpty()) {
			showToast("Please enter the adviser.", Toast.Type.ERROR, parent);
			return false;
		}
		if (dateField.getText().trim().isEmpty()) {
			showToast("Please enter a date.", Toast.Type.ERROR, parent);
			return false;
		}
		if (effectiveDateField.getText().trim().isEmpty()) {
			showToast("Please enter an effective date.", Toast.Type.ERROR, parent);
			return false;
		}
		if (actionTextArea.getText().trim().isEmpty()) {
			showToast("Please enter action taken.", Toast.Type.ERROR, parent);
			return false;
		}
		if (reasonTextArea.getText().trim().isEmpty()) {
			showToast("Please enter a reason for dropping.", Toast.Type.ERROR, parent);
			return false;
		}
		String selectedSigner = (String) formSignerDrop.getSelectedItem();
		if (selectedSigner == null || selectedSigner.equals("-Select Who to Sign-")) {
			showToast("Please select a form signer.", Toast.Type.ERROR, parent);
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

	private void showToast(String message, Toast.Type type, Component parent) {
		ToastOption toastOption = Toast.createOption();
		toastOption.getLayoutOption().setMargin(0, 0, 50, 0).setDirection(ToastDirection.TOP_TO_BOTTOM);
		Toast.show(parent, type, message, ToastLocation.BOTTOM_CENTER, toastOption);
	}
}