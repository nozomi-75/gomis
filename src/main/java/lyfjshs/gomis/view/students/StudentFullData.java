package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.model.StudentsData;
import lyfjshs.gomis.Database.model.StudentsRecord;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.utils.PrintingReport;
import net.miginfocom.swing.MigLayout;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
@SuppressWarnings(value = "serial")

public class StudentFullData extends Form {
	// Text fields for easy access and validation
	private JTextField lrnField;
	private JTextField lastNameField;
	private JTextField firstNameField;
	private JTextField middleNameField;
	private JComboBox<String> sexComboBox;
	private JTextField dobField;
	private JTextField emailField;
	private JTextField contactNumberField;
	private JTextField guardianNameField;
	private JTextField guardianEmailField;
	private JTextField guardianContactField;
	private JTextField addressField;

	// Academic and Violation Fields
	private JTextField academicYearField;
	private JTextField semesterField;
	private JTextField strandField;
	private JTextField trackField;
	private JTextField yearLevelField;
	private JTextField adviserField;
	private JTextField sectionField;
	private JTextField statusField;
	private JTextField violationTypeField;
	private JTextField violationDescriptionField;
	private JPanel panel;
	private JButton printGMBtn;
	private JPanel panel_1;
	private boolean violationResolve;
    private FlatSVGIcon sagisagIcon, logoIcon;

	public StudentFullData(StudentsData studentData, StudentsRecord studentRecord) {
		this.setLayout(new MigLayout("", "[][grow][]", "[][]"));
		initComponents();

		// Set fields with student data
		lrnField.setText(studentData.getLrn());
		lastNameField.setText(studentData.getLAST_NAME());
		firstNameField.setText(studentData.getFIRST_NAME());
		middleNameField.setText(studentData.getMiddleInitial());
		sexComboBox.setSelectedItem(studentData.getGender());
		dobField.setText(studentData.getDob().toString());
		emailField.setText(studentData.getEmail());
		contactNumberField.setText(studentData.getContactNumber());
		guardianNameField.setText(studentData.getGuardianName());
		guardianEmailField.setText(studentData.getGuardianEmail());
		guardianContactField.setText(studentData.getGuardianContactNumber());
		addressField.setText(studentData.getAddress());

		// Set fields with student record data
		academicYearField.setText(studentRecord.getAcademicYear());
		semesterField.setText(studentRecord.getSemester());
		strandField.setText(studentRecord.getStrand());
		trackField.setText(studentRecord.getTrack());
		yearLevelField.setText(String.valueOf(studentRecord.getYearLevel()));
		adviserField.setText(studentRecord.getAdviser());
		sectionField.setText(studentRecord.getSection());
		statusField.setText(studentRecord.getStatus());

		// Add components to the panel
		JPanel mainPanel = new JPanel(new MigLayout("", "[40px:n,grow 70,fill][100px:n,grow]", "[fill][grow,fill][]"));
		JScrollPane scroll = new JScrollPane(mainPanel);

		mainPanel.add(createPersonalInfoPanel(), "cell 0 0,grow");
		mainPanel.add(createAcademicInfoPanel(), "cell 1 0,growx,aligny center");
		mainPanel.add(createGuardianPanel(), "cell 0 1,grow");
		mainPanel.add(creteViolationRecordPanel(), "cell 1 1,grow");

		add(scroll, "cell 1 0,grow");

		panel = new JPanel(new MigLayout("", "[grow][][]", "[]"));
		add(panel, "cell 1 1,growx");

		printGMBtn = new JButton("Print Good Moral");
		printGMBtn.addActionListener(e -> createGoodMoralReport()); // Attach event

		panel.add(printGMBtn, "cell 1 0,grow");
	}

	private void testViolation() {
		violationResolve = true; // Example: assume violation is resolved

		if (violationResolve) {
			// Handle resolved violations (if needed)
		} else {
			JPanel noViolationPanel = new JPanel();
			noViolationPanel.setLayout(new BorderLayout());
		}
	}

	private JPanel creteViolationRecordPanel() {
		JPanel violationPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][]"));
		violationPanel.setBorder(
				new TitledBorder(null, "Violation Record", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		return violationPanel;
	}

	private void initComponents() {
		// Initialize ALL components here
		sagisagIcon = new FlatSVGIcon("DepEd_Sagisag.svg");
        logoIcon = new FlatSVGIcon("DepEd_Logo.svg");
        
		// Personal Information
		lrnField = new JTextField();
		lastNameField = new JTextField();
		firstNameField = new JTextField();
		middleNameField = new JTextField();
		sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
		dobField = new JTextField();
		emailField = new JTextField();
		contactNumberField = new JTextField();
		guardianNameField = new JTextField();
		guardianEmailField = new JTextField();
		guardianContactField = new JTextField();
		addressField = new JTextField();

		// Academic and Violation Information
		academicYearField = new JTextField();
		semesterField = new JTextField();
		strandField = new JTextField();
		trackField = new JTextField();
		yearLevelField = new JTextField();
		adviserField = new JTextField();
		sectionField = new JTextField();
		statusField = new JTextField();
		violationTypeField = new JTextField();
		violationDescriptionField = new JTextField();
	}

	private JPanel createPersonalInfoPanel() {
		JPanel personalInfoPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][][][][][][]"));
		personalInfoPanel.setBorder(
				new TitledBorder(null, "Personal Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		personalInfoPanel.add(new JLabel("LRN:"));
		personalInfoPanel.add(lrnField, "growx");

		personalInfoPanel.add(new JLabel("Last Name:"));
		personalInfoPanel.add(lastNameField, "growx");

		personalInfoPanel.add(new JLabel("First Name:"));
		personalInfoPanel.add(firstNameField, "growx");

		personalInfoPanel.add(new JLabel("Middle Name:"));
		personalInfoPanel.add(middleNameField, "growx");

		personalInfoPanel.add(new JLabel("Sex:"));
		personalInfoPanel.add(sexComboBox, "growx");

		personalInfoPanel.add(new JLabel("Date of Birth:"));
		personalInfoPanel.add(dobField, "growx");

		personalInfoPanel.add(new JLabel("Email:"));
		personalInfoPanel.add(emailField, "growx");

		personalInfoPanel.add(new JLabel("Contact Number:"));
		personalInfoPanel.add(contactNumberField, "growx");

		return personalInfoPanel;
	}

	private JPanel createAcademicInfoPanel() {
		JPanel academicInfoPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][][][][]"));
		academicInfoPanel.setBorder(
				new TitledBorder(null, "Academic Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		academicInfoPanel.add(new JLabel("Academic Year:"));
		academicInfoPanel.add(academicYearField, "growx");

		academicInfoPanel.add(new JLabel("Semester:"));
		academicInfoPanel.add(semesterField, "growx");

		academicInfoPanel.add(new JLabel("Strand:"));
		academicInfoPanel.add(strandField, "growx");

		academicInfoPanel.add(new JLabel("Track:"));
		academicInfoPanel.add(trackField, "growx");

		academicInfoPanel.add(new JLabel("Year Level:"));
		academicInfoPanel.add(yearLevelField, "growx");

		academicInfoPanel.add(new JLabel("Adviser:"));
		academicInfoPanel.add(adviserField, "growx");

		academicInfoPanel.add(new JLabel("Section:"));
		academicInfoPanel.add(sectionField, "growx");

		academicInfoPanel.add(new JLabel("Status:"));
		academicInfoPanel.add(statusField, "growx");

		return academicInfoPanel;
	}

	private JPanel createGuardianPanel() {
		JPanel guardianPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][]"));
		guardianPanel.setBorder(
				new TitledBorder(null, "Guardian Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		guardianPanel.add(new JLabel("Guardian Name:"));
		guardianPanel.add(guardianNameField, "growx");

		guardianPanel.add(new JLabel("Guardian Email:"));
		guardianPanel.add(guardianEmailField, "growx");

		guardianPanel.add(new JLabel("Guardian Contact:"));
		guardianPanel.add(guardianContactField, "growx");

		return guardianPanel;
	}

	/**
	 * Loads and compiles the Good Moral Certificate JRXML template from the
	 * resources folder.
	 *
	 * @return A compiled JasperReport object.
	 * @throws Exception If the template file is not found or fails to compile.
	 */
	private JasperReport loadGoodMoralTemplate() throws Exception {
		// Load the JRXML template from the resources folder
		InputStream templateStream = getClass().getClassLoader()
				.getResourceAsStream("templates/GoodMoral Template.jrxml");

		if (templateStream == null) {
			throw new IllegalArgumentException("Good Moral template not found in resources/templates/");
		}

		// Compile the JRXML template into a JasperReport object
		return JasperCompileManager.compileReport(templateStream);
	}

	
	/**
	 * Loads, compiles, and generates the Good Moral Certificate report.
	 */
	public void createGoodMoralReport() {
	    try {
	        // Load and compile the JRXML template
	        JasperReport compiledTemplate = loadGoodMoralTemplate();

	        // Prepare parameters for the report
	        Map<String, Object> parameters = new HashMap<>();
	        
	        // Generate full name
	        String fullName = firstNameField.getText() + " " + middleNameField.getText() + " " + lastNameField.getText();
	        parameters.put("fullName", fullName);
	        
	        // Format the date
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d'th' 'day of' MMMM, yyyy");
	        String formattedDate = LocalDateTime.now().format(formatter);
	        parameters.put("dateTodayText", formattedDate);

	        // Principal's name
	        String principalName = "Testing"; // This can be dynamic
	        parameters.put("principalName", principalName);

	        // Convert icons
	        parameters.put("DepEdLogo", PrintingReport.convertSvgToBufferedImage(sagisagIcon));
	        parameters.put("DepEdSagisag", PrintingReport.convertSvgToBufferedImage(logoIcon));

	        // Generate the report
	        PrintingReport.generateReport(FormManager.getFrame(), compiledTemplate, parameters, fullName + "_GoodMoral_Certificate", "GoodMoral Cert, Save as PDF");

	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(FormManager.getFrame(), "Failed to load or generate the report: " + e.getMessage(),
	                "Error", JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}
}