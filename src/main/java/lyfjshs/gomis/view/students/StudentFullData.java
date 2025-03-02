package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

import lyfjshs.gomis.Database.model.Address;
import lyfjshs.gomis.Database.model.Contact;
import lyfjshs.gomis.Database.model.Guardian;
import lyfjshs.gomis.Database.model.PARENTS;
import lyfjshs.gomis.Database.model.StudentsData;
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
	private JTextField ageField;
	private JTextField motherTongueField;
	private JTextField guardianNameField;
	private JTextField guardianEmailField;
	private JTextField guardianContactField;
	private JTextField addressField;
	private JTextField violationTypeField;
	private JTextField violationDescriptionField;
	private JPanel panel;
	private final JButton printGMBtn;
	private boolean violationResolve;
	private FlatSVGIcon sagisagIcon, logoIcon;
	private JTextField ipField;
	private JTextField textField;
	private JTextField houseNoField;
	private JTextField streetField;
	private JTextField regionField;
	private JTextField provinceField;
	private JTextField municipalityField;
	private JTextField barangayField;
	private JTextField zipCodeField;
	private JTextField fatherLastNameField;
	private JTextField fatherFirstNameField;
	private JTextField fatherMiddleNameField;
	private JTextField fatherPhoneNumberField;
	private JTextField fatherOccupationField;
	private JTextField motherLastNameField;
	private JTextField motherFirstNameField;
	private JTextField motherMiddleNameField;
	private JTextField motherPhoneNumberField;
	private JTextField motherOccupationField;
	private JPanel violationPanel;
	private JTextField parentNameField;
	private JTextField appointmentTitleField;
	private JTextField incidentDescriptionField;
	private JTextField sessionNotesField;

	public StudentFullData(StudentsData studentData) {
		this.setLayout(new MigLayout("", "[][grow][]", "[][]"));
		initComponents();

		// Set fields with student data
		lrnField.setText(studentData.getLrn());
		lastNameField.setText(studentData.getLastName());
		firstNameField.setText(studentData.getFirstName());
		middleNameField.setText(studentData.getMiddleName());
		sexComboBox.setSelectedItem(studentData.getSex());
		dobField.setText(studentData.getBirthDate().toString());
		ageField.setText(String.valueOf(studentData.getAge()));
		motherTongueField.setText(studentData.getMotherTongue());
		ipField.setText(studentData.getIpType());
		textField.setText(studentData.getReligion());

		// Set address fields
		Address address = studentData.getAddress();
		houseNoField.setText(address.getHouseNumber());
		streetField.setText(address.getStreetSubdivision());
		regionField.setText(address.getRegion());
		provinceField.setText(address.getProvince());
		municipalityField.setText(address.getMunicipality());
		barangayField.setText(address.getBarangay());
		zipCodeField.setText(address.getZipCode());

		// Set contact fields
		Contact contact = studentData.getContact();
		guardianContactField.setText(contact.getCONTACT_NUMBER());

		// Set PARENTS fields
		PARENTS parent = studentData.getParent();
		if (parent != null) {
			fatherLastNameField.setText(parent.getFatherLastName());
			fatherFirstNameField.setText(parent.getFatherFirstName());
			fatherMiddleNameField.setText(parent.getFatherMiddleName());
			motherLastNameField.setText(parent.getMotherLastName());
			motherFirstNameField.setText(parent.getMotherFirstName());
			motherMiddleNameField.setText(parent.getMotherMiddleName());
		}

		// Set guardian fields
		Guardian guardian = studentData.getGuardian();
		if (guardian != null) {
			guardianNameField.setText(guardian.getGUARIAN_FIRSTNAME() + " " + guardian.getGUARDIAN_MIDDLENAME() + " " + guardian.getGUARDIAN_LASTNAME());
			guardianEmailField.setText(guardian.getGUARDIAN_RELATIONSHIP());
		}

		// Add components to the panel
		JPanel mainPanel = new JPanel(
				new MigLayout("", "[40px:n,grow,fill][100px:n,grow]", "[fill][grow][grow,fill][][grow]"));
		JScrollPane scroll = new JScrollPane(mainPanel);

		mainPanel.add(createPersonalInfoPanel(), "cell 0 0 2 1,grow");
		mainPanel.add(createAddressPanel(), "cell 0 1 2 1,grow");
		mainPanel.add(createParentPanel(), "cell 0 2 2 1,grow");
		mainPanel.add(createGuardianPanel(), "cell 0 3,grow");
		mainPanel.add(createAppointmentPanel(), "cell 0 4,grow");
		mainPanel.add(createViolationPanel(), "cell 0 5,grow");
		mainPanel.add(createIncidentPanel(), "cell 0 6,grow");
		mainPanel.add(createSessionPanel(), "cell 0 7,grow");

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

	private void initComponents() {
		// Initialize ALL components here
		sagisagIcon = new FlatSVGIcon("DepEd_Sagisag.svg");
		logoIcon = new FlatSVGIcon("DepEd_Logo.svg");
		lrnField = new JTextField();
		lastNameField = new JTextField();
		firstNameField = new JTextField();
		middleNameField = new JTextField();
		sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
		dobField = new JTextField();
		ageField = new JTextField();
		motherTongueField = new JTextField();
		guardianNameField = new JTextField();
		guardianEmailField = new JTextField();
		guardianContactField = new JTextField();
		addressField = new JTextField();
		violationTypeField = new JTextField();
		violationDescriptionField = new JTextField();
		ipField = new JTextField();
		textField = new JTextField();
		houseNoField = new JTextField();
		streetField = new JTextField();
		regionField = new JTextField();
		provinceField = new JTextField();
		municipalityField = new JTextField();
		barangayField = new JTextField();
		zipCodeField = new JTextField();
		fatherLastNameField = new JTextField();
		fatherFirstNameField = new JTextField();
		fatherMiddleNameField = new JTextField();
		fatherPhoneNumberField = new JTextField();
		fatherOccupationField = new JTextField();
		motherLastNameField = new JTextField();
		motherFirstNameField = new JTextField();
		motherMiddleNameField = new JTextField();
		motherPhoneNumberField = new JTextField();
		motherOccupationField = new JTextField();
		houseNoField = new JTextField(10);
		streetField = new JTextField(15);
		regionField = new JTextField(10);
		provinceField = new JTextField(15);
		municipalityField = new JTextField(15);
		barangayField = new JTextField(15);
		zipCodeField = new JTextField(5);
		parentNameField = new JTextField();
		appointmentTitleField = new JTextField();
		incidentDescriptionField = new JTextField();
		sessionNotesField = new JTextField();
	}

	private JPanel createPersonalInfoPanel() {
		JPanel personalInfoPanel = new JPanel(
				new MigLayout("wrap 2", "[140px][grow,fill][][140px,leading][grow,fill]", "[]5[]5[]5[]5[]5[]5[]5[]"));
		personalInfoPanel.setBorder(
				new TitledBorder(null, "Personal Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		personalInfoPanel.add(new JLabel("LRN:"), "cell 0 0, leading");
		personalInfoPanel.add(lrnField, "cell 1 0, growx");

		personalInfoPanel.add(new JLabel("IP (Ethnic Group): "), "cell 3 0, leading");
		personalInfoPanel.add(ipField, "cell 4 0, growx");

		personalInfoPanel.add(new JLabel("Last Name:"), "cell 0 1, leading");
		personalInfoPanel.add(lastNameField, "cell 1 1, growx");

		personalInfoPanel.add(new JLabel("Religion: "), "cell 3 1, leading");
		personalInfoPanel.add(textField, "cell 4 1, growx");

		personalInfoPanel.add(new JLabel("First Name:"), "cell 0 2, leading");
		personalInfoPanel.add(firstNameField, "cell 1 2, growx");

		personalInfoPanel.add(new JLabel("Middle Name:"), "cell 0 3, leading");
		personalInfoPanel.add(middleNameField, "cell 1 3, growx");

		personalInfoPanel.add(new JLabel("Sex:"), "cell 0 4, leading");
		personalInfoPanel.add(sexComboBox, "cell 1 4, growx");

		personalInfoPanel.add(new JLabel("Date of Birth:"), "cell 0 5, leading");
		personalInfoPanel.add(dobField, "cell 1 5, growx");

		personalInfoPanel.add(new JLabel("AGE as of 1st Friday June"), "cell 0 6, leading");
		personalInfoPanel.add(ageField, "cell 1 6, growx");

		personalInfoPanel.add(new JLabel("Mother Tongue"), "cell 0 7, leading");
		personalInfoPanel.add(motherTongueField, "cell 1 7, growx");

		return personalInfoPanel;
	}

	private JPanel createAddressPanel() {
		JPanel addressPanel = new JPanel(new MigLayout("wrap 2", "[][grow]", "[]5[]5[]5[]5[]5[]5[]"));
		addressPanel.setBorder(
				new TitledBorder(null, "RESIDENTIAL ADDRESS", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		zipCodeField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (!Character.isDigit(e.getKeyChar())) {
					e.consume();
				}
			}
		});

		addressPanel.add(new JLabel("House No:"), "cell 0 0, leading");
		addressPanel.add(houseNoField, "cell 1 0, growx");
		addressPanel.add(new JLabel("Street/Subdivision:"), "cell 0 1, leading");
		addressPanel.add(streetField, "cell 1 1, growx");
		addressPanel.add(new JLabel("Region:"), "cell 0 2, leading");
		addressPanel.add(regionField, "cell 1 2, growx");
		addressPanel.add(new JLabel("Province:"), "cell 0 3, leading");
		addressPanel.add(provinceField, "cell 1 3, growx");
		addressPanel.add(new JLabel("Municipality:"), "cell 0 4, leading");
		addressPanel.add(municipalityField, "cell 1 4, growx");
		addressPanel.add(new JLabel("Barangay:"), "cell 0 5, leading");
		addressPanel.add(barangayField, "cell 1 5, growx");
		addressPanel.add(new JLabel("Zip Code:"), "cell 0 6, leading");
		addressPanel.add(zipCodeField, "cell 1 6, growx");

		return addressPanel;
	}

	private JPanel createGuardianPanel() {
		JPanel guardianPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[]5[]5[]"));
		guardianPanel.setBorder(
				new TitledBorder(null, "Guardian Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		guardianPanel.add(new JLabel("Guardian Name:"), "cell 0 0, leading");
		guardianPanel.add(guardianNameField, "cell 1 0, growx");

		guardianPanel.add(new JLabel("Relation to Student:"), "cell 0 1, leading");
		guardianPanel.add(guardianEmailField, "cell 1 1, growx");

		guardianPanel.add(new JLabel("Guardian Contact Number:"), "cell 0 2, leading");
		guardianPanel.add(guardianContactField, "cell 1 2, growx");

		return guardianPanel;
	}

	private JPanel createParentPanel() {
		JPanel parentPanel = new JPanel(new MigLayout("wrap 4", "[][grow]15[][grow]", "[]5[]5[]5[]5[]"));
		parentPanel.setBorder(
				new TitledBorder(null, "PARENT'S INFORMATION", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		fatherFirstNameField = new JTextField();
		parentPanel.add(fatherFirstNameField, "cell 1 0,growx");

		JLabel label_1 = new JLabel("Mother's First Name:");
		parentPanel.add(label_1, "cell 2 0,alignx leading");
		motherFirstNameField = new JTextField();
		parentPanel.add(motherFirstNameField, "cell 3 0,growx");

		parentPanel.add(new JLabel("Father's Middle Name:"), "cell 0 1, leading");
		parentPanel.add(fatherMiddleNameField, "cell 1 1, growx");

		JLabel label_3 = new JLabel("Mother's Middle Name:");
		parentPanel.add(label_3, "cell 2 1,alignx leading");
		motherMiddleNameField = new JTextField();
		parentPanel.add(motherMiddleNameField, "cell 3 1,growx");

		JLabel label_6 = new JLabel("Father's Last Name:");
		parentPanel.add(label_6, "cell 0 2,alignx leading");
		fatherLastNameField = new JTextField();
		parentPanel.add(fatherLastNameField, "cell 1 2,growx");

		parentPanel.add(new JLabel("Mother's Last Name:"), "cell 2 2, leading");
		parentPanel.add(motherLastNameField, "cell 3 2, growx");

		JLabel label_2 = new JLabel("Father's Phone Number:");
		parentPanel.add(label_2, "cell 0 3,alignx leading");
		fatherPhoneNumberField = new JTextField();
		parentPanel.add(fatherPhoneNumberField, "cell 1 3,growx");

		JLabel label_4 = new JLabel("Mother's Phone Number:");
		parentPanel.add(label_4, "cell 2 3,alignx leading");
		motherPhoneNumberField = new JTextField();
		parentPanel.add(motherPhoneNumberField, "cell 3 3,growx");

		JLabel label_5 = new JLabel("Father's Occupation:");
		parentPanel.add(label_5, "cell 0 4,alignx leading");
		fatherOccupationField = new JTextField();
		parentPanel.add(fatherOccupationField, "cell 1 4,growx");

		parentPanel.add(new JLabel("Mother's Occupation:"), "cell 2 4, leading");
		parentPanel.add(motherOccupationField, "cell 3 4, growx");

		JLabel label = new JLabel("Father's First Name:");
		parentPanel.add(label, "flowx,cell 0 0,alignx leading");

		return parentPanel;
	}

	private JPanel createAppointmentPanel() {
		JPanel appointmentPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[]5[]"));
		appointmentPanel.setBorder(new TitledBorder(null, "Appointment Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		appointmentPanel.add(new JLabel("Appointment Title:"), "cell 0 0, leading");
		appointmentPanel.add(appointmentTitleField, "cell 1 0, growx");

		return appointmentPanel;
	}

	private JPanel createViolationPanel() {
		JPanel violationPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[]5[]5[]"));
		violationPanel.setBorder(new TitledBorder(null, "Violation Record", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		violationPanel.add(new JLabel("Violation Type:"), "cell 0 0, leading");
		violationPanel.add(violationTypeField, "cell 1 0, growx");

		violationPanel.add(new JLabel("Violation Description:"), "cell 0 1, leading");
		violationPanel.add(violationDescriptionField, "cell 1 1, growx");

		return violationPanel;
	}

	private JPanel createIncidentPanel() {
		JPanel incidentPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[]5[]"));
		incidentPanel.setBorder(new TitledBorder(null, "Incident Record", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		incidentPanel.add(new JLabel("Incident Description:"), "cell 0 0, leading");
		incidentPanel.add(incidentDescriptionField, "cell 1 0, growx");

		return incidentPanel;
	}

	private JPanel createSessionPanel() {
		JPanel sessionPanel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[]5[]"));
		sessionPanel.setBorder(new TitledBorder(null, "Session Notes", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		sessionPanel.add(new JLabel("Session Notes:"), "cell 0 0, leading");
		sessionPanel.add(sessionNotesField, "cell 1 0, growx");

		return sessionPanel;
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
			String fullName = firstNameField.getText() + " " + middleNameField.getText() + " "
					+ lastNameField.getText();
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
			PrintingReport.generateReport(FormManager.getFrame(), compiledTemplate, parameters,
					fullName + "_GoodMoral_Certificate", "GoodMoral Cert, Save as PDF");

		} catch (Exception e) {
			JOptionPane.showMessageDialog(FormManager.getFrame(),
					"Failed to load or generate the report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}