package lyfjshs.gomis.view.students;

import java.awt.Color;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.utils.PrintingReport;
import net.miginfocom.swing.MigLayout;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

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
	private JPanel panel;
	private final JButton printGMBtn;
	private boolean violationResolve;
	private FlatSVGIcon sagisagIcon, logoIcon;
	private JTextField ipField;
	private JTextField textField;
	private JTextField fatherLastNameField;
	private JTextField fatherFirstNameField;
	private JTextField fatherMiddleNameField;
	private JTextField fatherPhoneNumberField;
	private JTextField fatherOccupationField;
	private JTextField motherLastNameField;
	private JTextField motherFirstNameField;
	private JTextField motherMiddleNameField;
	private JTextField motherPhoneNumberField;
	private JPanel violationPanel;
	private JTextField parentNameField;
	private JPanel violationTablePanel;
	private JScrollPane scrollPane;
	private JTable violationTable;
	private Connection connect;
	private JLabel lblNewLabel;
	private JTextField fullAddField;

	public StudentFullData(Connection connection, Student studentData) {
		this.setLayout(new MigLayout("", "[][grow][]", "[][]"));
		initComponents();
		this.connect = connection;

		loadViolations(studentData.getStudentUid(), connect);

		// Set fields with student data
		setStudentData(studentData);

		// Add components to the panel
		JPanel mainPanel = new JPanel(new MigLayout("", "[40px:n,grow,fill][100px:n,grow]", "[fill][grow,fill][]"));
		JScrollPane scroll = new JScrollPane(mainPanel);

		mainPanel.add(createPersonalInfoPanel(), "cell 0 0 2 1,grow");
		mainPanel.add(createParentPanel(), "cell 0 1 2 1,grow");
		mainPanel.add(createGuardianPanel(), "cell 0 2,grow");
		mainPanel.add(createViolationTablePanel(), "cell 1 2,grow");

		add(scroll, "cell 1 0,grow");

		panel = new JPanel(new MigLayout("", "[grow][][]", "[]"));
		add(panel, "cell 1 1,growx");

		printGMBtn = new JButton("Print Good Moral");
		printGMBtn.addActionListener(e -> createGoodMoralReport()); // Attach event

		panel.add(printGMBtn, "cell 1 0,grow");
	}

	private void initComponents() {
		// Initialize ALL components here
		sagisagIcon = new FlatSVGIcon("DepEd_Sagisag.svg");
		logoIcon = new FlatSVGIcon("DepEd_Logo.svg");

		lrnField = new JTextField();
		lrnField.setEditable(false);

		lastNameField = new JTextField();
		lastNameField.setEditable(false);

		firstNameField = new JTextField();
		firstNameField.setEditable(false);

		middleNameField = new JTextField();
		middleNameField.setEditable(false);

		sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
		sexComboBox.setEnabled(false);

		dobField = new JTextField();
		dobField.setEditable(false);

		ageField = new JTextField();
		ageField.setEditable(false);

		motherTongueField = new JTextField();
		motherTongueField.setEditable(false);

		guardianNameField = new JTextField();
		guardianNameField.setEditable(false);

		guardianEmailField = new JTextField();
		guardianEmailField.setEditable(false);

		guardianContactField = new JTextField();
		guardianContactField.setEditable(false);

		addressField = new JTextField();
		addressField.setEditable(false);

		ipField = new JTextField();
		ipField.setEditable(false);

		textField = new JTextField();
		textField.setEditable(false);

		fatherLastNameField = new JTextField();
		fatherLastNameField.setEditable(false);

		fatherFirstNameField = new JTextField();
		fatherFirstNameField.setEditable(false);

		fatherMiddleNameField = new JTextField();
		fatherMiddleNameField.setEditable(false);

		fatherPhoneNumberField = new JTextField();
		fatherPhoneNumberField.setEditable(false);

		fatherOccupationField = new JTextField();
		fatherOccupationField.setEditable(false);

		motherLastNameField = new JTextField();
		motherLastNameField.setEditable(false);

		motherFirstNameField = new JTextField();
		motherFirstNameField.setEditable(false);

		motherMiddleNameField = new JTextField();
		motherMiddleNameField.setEditable(false);

		motherPhoneNumberField = new JTextField();
		motherPhoneNumberField.setEditable(false);

		parentNameField = new JTextField();
		parentNameField.setEditable(false);

		fullAddField = new JTextField();
		fullAddField.setEditable(false);
	}

	private void setStudentData(Student studentData) {
		lrnField.setText(studentData.getStudentLrn());
		lastNameField.setText(studentData.getStudentLastname());
		firstNameField.setText(studentData.getStudentFirstname());
		middleNameField.setText(studentData.getStudentMiddlename());
		sexComboBox.setSelectedItem(studentData.getStudentSex());
		dobField.setText(studentData.getStudentBirthdate().toString());
		ageField.setText(String.valueOf(studentData.getStudentAge()));
		motherTongueField.setText(studentData.getStudentMothertongue());
		ipField.setText(studentData.getStudentIpType());
		textField.setText(studentData.getStudentReligion());

		// Set address fields
		Address address = studentData.getAddress();
		System.out.println(address.getAddressHouseNumber() + " " + address.getAddressStreetSubdivision() + " "
				+ address.getAddressBarangay() + " " + address.getAddressMunicipality() + " "
				+ address.getAddressProvince() + " " + address.getAddressZipCode());
		fullAddField.setText(address.getAddressHouseNumber() + " " + address.getAddressStreetSubdivision() + " "
				+ address.getAddressBarangay() + " " + address.getAddressMunicipality() + " "
				+ address.getAddressProvince() + " " + address.getAddressZipCode());

		// Set contact fields
		Contact contact = studentData.getContact();
		guardianContactField.setText(contact.getContactNumber());

		// Set PARENTS fields
		Parents parent = studentData.getParents();
		fatherLastNameField.setText(parent.getFatherLastname());
		fatherFirstNameField.setText(parent.getFatherFirstname());
		fatherMiddleNameField.setText(parent.getFatherMiddlename());
		fatherPhoneNumberField.setText(parent.getFatherContactNumber());
		motherLastNameField.setText(parent.getMotherLastname());
		motherFirstNameField.setText(parent.getMotherFirstname());
		motherMiddleNameField.setText(parent.getMotherMiddlename());
		motherPhoneNumberField.setText(parent.getMotherContactNumber());

		// Set guardian fields
		Guardian guardian = studentData.getGuardian();
		guardianNameField.setText(guardian.getGuardianFirstname() + " " + guardian.getGuardianMiddlename() + " "
				+ guardian.getGuardianLastname());
		guardianEmailField.setText(guardian.getGuardianRelationship());
	}

	private JPanel createPersonalInfoPanel() {
		JPanel personalInfoPanel = new JPanel(
				new MigLayout("wrap 2", "[140px][grow,fill][][140px,leading][grow,fill]", "[]5[]5[]5[]5[]5[]"));
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

		JLabel label = new JLabel("Mother Tongue");
		personalInfoPanel.add(label, "cell 3 2,alignx leading");

		motherTongueField = new JTextField();
		motherTongueField.setEditable(false);
		personalInfoPanel.add(motherTongueField, "cell 4 2,growx");

		personalInfoPanel.add(new JLabel("Middle Name:"), "cell 0 3, leading");
		personalInfoPanel.add(middleNameField, "cell 1 3, growx");

		lblNewLabel = new JLabel("Full Address:");
		personalInfoPanel.add(lblNewLabel, "cell 3 3,alignx left");

		fullAddField = new JTextField();
		fullAddField.setEditable(false);
		personalInfoPanel.add(fullAddField, "cell 4 3,growx");
		fullAddField.setColumns(10);

		personalInfoPanel.add(new JLabel("Sex:"), "cell 0 4, leading");
		personalInfoPanel.add(sexComboBox, "cell 1 4, growx");

		personalInfoPanel.add(new JLabel("Date of Birth:"), "cell 0 5, leading");
		personalInfoPanel.add(dobField, "flowx,cell 1 5,growx");

		JLabel label_1 = new JLabel("AGE as of 1st Friday June");
		personalInfoPanel.add(label_1, "cell 1 5,alignx leading");

		ageField = new JTextField();
		ageField.setEditable(false);
		personalInfoPanel.add(ageField, "cell 1 5,alignx center");

		return personalInfoPanel;
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
		JPanel parentPanel = new JPanel(new MigLayout("wrap 4", "[][grow]15[][grow]", "[]5[]5[]5[]"));
		parentPanel.setBorder(
				new TitledBorder(null, "PARENT'S INFORMATION", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		parentPanel.add(fatherFirstNameField, "cell 1 0,growx");

		JLabel label_1 = new JLabel("Mother's First Name:");
		parentPanel.add(label_1, "cell 2 0,alignx leading");
		parentPanel.add(motherFirstNameField, "cell 3 0,growx");

		parentPanel.add(new JLabel("Father's Middle Name:"), "cell 0 1, leading");
		parentPanel.add(fatherMiddleNameField, "cell 1 1, growx");

		JLabel label_3 = new JLabel("Mother's Middle Name:");
		parentPanel.add(label_3, "cell 2 1,alignx leading");
		parentPanel.add(motherMiddleNameField, "cell 3 1,growx");

		JLabel label_6 = new JLabel("Father's Last Name:");
		parentPanel.add(label_6, "cell 0 2,alignx leading");
		parentPanel.add(fatherLastNameField, "cell 1 2,growx");

		parentPanel.add(new JLabel("Mother's Last Name:"), "cell 2 2, leading");
		parentPanel.add(motherLastNameField, "cell 3 2, growx");

		JLabel label_2 = new JLabel("Father's Phone Number:");
		parentPanel.add(label_2, "cell 0 3,alignx leading");
		parentPanel.add(fatherPhoneNumberField, "cell 1 3,growx");

		JLabel label_4 = new JLabel("Mother's Phone Number:");
		parentPanel.add(label_4, "cell 2 3,alignx leading");
		parentPanel.add(motherPhoneNumberField, "cell 3 3,growx");

		JLabel label = new JLabel("Father's First Name:");
		parentPanel.add(label, "flowx,cell 0 0,alignx leading");

		return parentPanel;
	}

	private JPanel createViolationTablePanel() {
		JPanel violationTablePanel = new JPanel();
		violationTablePanel.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Violation Table", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		violationTablePanel.setLayout(new MigLayout("", "[grow]", "[]"));

		// Initialize the violation table with an empty model
		violationTable = new JTable(
				new DefaultTableModel(new String[] { "Violation Type", "Reinforcement", "Status", "Actions" }, 0));
		scrollPane = new JScrollPane(violationTable);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS); // Ensure vertical scroll bar is
																						// always visible
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Add horizontal scroll
																								// bar if needed

		// Constrain the height of the scroll pane to prevent it from growing too large
		violationTablePanel.add(scrollPane, "cell 0 0,grow,h 150!"); // Set a maximum height for the scroll pane

		return violationTablePanel;
	}

	private void loadViolations(int studentUID, Connection connection) {
		try {
			// Ensure violationTable is initialized
			if (violationTable == null) {
				violationTable = new JTable(new DefaultTableModel(
						new String[] { "Violation Type", "Reinforcement", "Status", "Actions" }, 0));
			}
			List<ViolationRecord> violations = ViolationCRUD.getViolationsByStudentUID(connection, studentUID);
			createViolationTable(violations);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createViolationTable(List<ViolationRecord> violations) {
		DefaultTableModel model = (DefaultTableModel) violationTable.getModel();
		model.setRowCount(0); // Clear existing rows

		for (ViolationRecord violation : violations) {
			Object[] row = { violation.getViolationType(), violation.getReinforcement(), violation.getStatus(), "" // Placeholder
																													// for
																													// actions
			};
			model.addRow(row);
		}

		TableActionManager actionManager = new TableActionManager();
		actionManager.addAction("View", (table, row) -> viewViolation(violations.get(row)), null, null)
				.addAction("Resolve", (table, row) -> resolveViolation(violations.get(row)), null, null);
		actionManager.applyTo(violationTable, 3); // Assuming actions are in the 4th column
	}

	private void viewViolation(ViolationRecord violation) {
		// Implement view logic
	}

	private void resolveViolation(ViolationRecord violation) {
		// Implement resolve logic
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