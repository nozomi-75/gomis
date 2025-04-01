package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.utils.DroppingFormGenerator;
import lyfjshs.gomis.utils.GoodMoralGenerator;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

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
	private JButton printGMBtn;
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
	private JTextField parentNameField;
	private GTable violationTable;
	private Connection connect;
	private JTextField fullAddField;
	private JButton dropStudBtn;
	private FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.4f);
	private FlatSVGIcon resolveIcon = new FlatSVGIcon("icons/resolve.svg", 0.4f);
	private JPanel noViolationPanel;
	private JPanel containerPanel; // Add this field
	private JTextField schoolNameField;
	private JTextField schoolIdField;
	private JTextField districtField;
	private JTextField divisionField;
	private JTextField schoolRegionField;
	private JTextField semesterField;
	private JTextField schoolYearField;
	private JTextField gradeLevelField;
	private JTextField sectionField;
	private JTextField trackField;
	private JTextField courseField;

	public StudentFullData(Connection connection, Student studentData) {
		this.setLayout(new MigLayout("", "[][grow][]", "[][]"));
		this.connect = connection;

		// Initialize all components first
		initComponents();

		// Initialize the violation table before using it
		initializeViolationTable();

		// Create the panels before setting data
		setupMainPanel(studentData);

		// Set student data after all components are initialized
		setStudentData(studentData);

		// Now load violations
		loadViolations(studentData.getStudentUid(), connect);
	}

	private void initComponents() {
		// Initialize sexComboBox first before using it
		sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
		sexComboBox.setEnabled(false);

		// Initialize all text fields
		lrnField = new JTextField();
		lrnField.setEditable(false);

		lastNameField = new JTextField();
		lastNameField.setEditable(false);

		firstNameField = new JTextField();
		firstNameField.setEditable(false);

		middleNameField = new JTextField();
		middleNameField.setEditable(false);

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

	private void initializeViolationTable() {
		// Initialize GTable with proper configuration
		String[] columnNames = { "Type", "Description", "Reinforcement", "Status", "Actions" };
		Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, Object.class };
		boolean[] editableColumns = { false, false, false, false, true };
		double[] columnWidths = { 0.2, 0.3, 0.2, 0.15, 0.15 };
		int[] alignments = { SwingConstants.LEFT, // Type
				SwingConstants.LEFT, // Description
				SwingConstants.LEFT, // Reinforcement
				SwingConstants.CENTER, // Status
				SwingConstants.CENTER // Actions
		};

		// Create action manager
		TableActionManager actionManager = new DefaultTableActionManager();
		((DefaultTableActionManager)actionManager)
			.addAction("View", (table, row) -> {
				viewViolation(row);
			}, new Color(0, 150, 136), viewIcon)
			.addAction("Resolve", (table, row) -> {
				Violation violation = (Violation) table.getValueAt(row, -1);
				resolveViolation(violation);
			}, new Color(255, 150, 136), resolveIcon);

		violationTable = new GTable(new Object[0][5], columnNames, columnTypes, editableColumns, columnWidths,
				alignments, false, // no checkbox
				actionManager // actions configuration
		);
	}

	private void setStudentData(Student studentData) {
		if (studentData == null) {
			System.out.println("Warning: studentData is null");
			return;
		}

		try {
			// Debug prints to check data
			System.out.println("Setting student data for: " + studentData.getStudentFirstname());
			
			// Basic info
			lrnField.setText(studentData.getStudentLrn() != null ? studentData.getStudentLrn() : "");
			lastNameField.setText(studentData.getStudentLastname() != null ? studentData.getStudentLastname() : "");
			firstNameField.setText(studentData.getStudentFirstname() != null ? studentData.getStudentFirstname() : "");
			middleNameField.setText(studentData.getStudentMiddlename() != null ? studentData.getStudentMiddlename() : "");
			sexComboBox.setSelectedItem(studentData.getStudentSex() != null ? studentData.getStudentSex() : "");
			dobField.setText(studentData.getStudentBirthdate() != null ? studentData.getStudentBirthdate().toString() : "");
			ageField.setText(String.valueOf(studentData.getStudentAge()));
			motherTongueField.setText(studentData.getStudentMothertongue() != null ? studentData.getStudentMothertongue() : "");
			ipField.setText(studentData.getStudentIpType() != null ? studentData.getStudentIpType() : "");
			textField.setText(studentData.getStudentReligion() != null ? studentData.getStudentReligion() : "");

			// Address
			Address address = studentData.getAddress();
			if (address != null) {
				String fullAddress = String.format("%s %s %s %s %s %s",
					nullToEmpty(address.getAddressHouseNumber()),
					nullToEmpty(address.getAddressStreetSubdivision()),
					nullToEmpty(address.getAddressBarangay()),
					nullToEmpty(address.getAddressMunicipality()),
					nullToEmpty(address.getAddressProvince()),
					nullToEmpty(address.getAddressZipCode())
				).trim();
				fullAddField.setText(fullAddress);
			}

			// Contact
			Contact contact = studentData.getContact();
			if (contact != null) {
				guardianContactField.setText(contact.getContactNumber() != null ? contact.getContactNumber() : "");
			}

			// Parents
			Parents parent = studentData.getParents();
			if (parent != null) {
				fatherLastNameField.setText(nullToEmpty(parent.getFatherLastname()));
				fatherFirstNameField.setText(nullToEmpty(parent.getFatherFirstname()));
				fatherMiddleNameField.setText(nullToEmpty(parent.getFatherMiddlename()));
				fatherPhoneNumberField.setText(nullToEmpty(parent.getFatherContactNumber()));
				motherLastNameField.setText(nullToEmpty(parent.getMotherLastname()));
				motherFirstNameField.setText(nullToEmpty(parent.getMotherFirstname()));
				motherMiddleNameField.setText(nullToEmpty(parent.getMotherMiddlename()));
				motherPhoneNumberField.setText(nullToEmpty(parent.getMotherContactNumber()));
			}

			// Guardian
			Guardian guardian = studentData.getGuardian();
			if (guardian != null) {
				String guardianFullName = String.format("%s %s %s",
					nullToEmpty(guardian.getGuardianFirstname()),
					nullToEmpty(guardian.getGuardianMiddlename()),
					nullToEmpty(guardian.getGuardianLastname())
				).trim();
				guardianNameField.setText(guardianFullName);
				guardianEmailField.setText(nullToEmpty(guardian.getGuardianRelationship()));
			}

			// School Form
			SchoolForm schoolForm = studentData.getSchoolForm();
			if (schoolForm != null) {
				schoolNameField.setText(nullToEmpty(schoolForm.getSF_SCHOOL_NAME()));
				schoolIdField.setText(nullToEmpty(schoolForm.getSF_SCHOOL_ID()));
				districtField.setText(nullToEmpty(schoolForm.getSF_DISTRICT()));
				divisionField.setText(nullToEmpty(schoolForm.getSF_DIVISION()));
				schoolRegionField.setText(nullToEmpty(schoolForm.getSF_REGION()));
				semesterField.setText(nullToEmpty(schoolForm.getSF_SEMESTER()));
				schoolYearField.setText(nullToEmpty(schoolForm.getSF_SCHOOL_YEAR()));
				gradeLevelField.setText(nullToEmpty(schoolForm.getSF_GRADE_LEVEL()));
				sectionField.setText(nullToEmpty(schoolForm.getSF_SECTION()));
				trackField.setText(nullToEmpty(schoolForm.getSF_TRACK_AND_STRAND()));
				courseField.setText(nullToEmpty(schoolForm.getSF_COURSE()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error setting student data: " + e.getMessage());
		}
	}

	private String nullToEmpty(String value) {
		return value != null ? value : "";
	}

	private void setupMainPanel(Student student) {
		// Add components to the panel
		JPanel mainPanel = new JPanel(
				new MigLayout("", "[40px:n,grow,fill][100px:n,grow]", "[fill][grow,fill][grow,fill][grow,fill][250px]"));
		JScrollPane scroll = new JScrollPane(mainPanel);
		scroll.getVerticalScrollBar().setUnitIncrement(20);  // Increase single scroll unit (mouse wheel)
        scroll.getVerticalScrollBar().setBlockIncrement(100); // Increase scroll bar click increment
        scroll	.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);

		mainPanel.add(createPersonalInfoPanel(), "cell 0 0 2 1,grow");
		mainPanel.add(createParentPanel(), "cell 0 1 2 1,grow");
		mainPanel.add(createGuardianPanel(), "cell 0 2 2 1,grow");
		mainPanel.add(createSchoolFormPanel(), "cell 0 3 2 1,grow");
		mainPanel.add(createViolationTablePanel(), "cell 0 4 2 1,grow");

		add(scroll, "cell 1 0,grow");

		panel = new JPanel(new MigLayout("", "[grow][][][]", "[]"));
		add(panel, "cell 1 1,growx");

		printGMBtn = new JButton("Print Good Moral");
		printGMBtn.addActionListener(e -> createGoodMoralReport(student));

		dropStudBtn = new JButton("DROP Student");
		dropStudBtn.addActionListener(e -> dropStudentModal());
		panel.add(dropStudBtn, "cell 1 0");
		panel.add(printGMBtn, "cell 2 0,grow");
	}

	private void createGoodMoralReport(Student student) {
		try {
			// Check for active violations
			ViolationDAO ViolationDAO = new ViolationDAO(connect);
			List<Violation> violations = ViolationDAO.getViolationsByStudentUID(student.getStudentUid());
			
			// Check if there are any active violations
			boolean hasActiveViolations = false;
			if (violations != null && !violations.isEmpty()) {
				for (Violation violation : violations) {
					if ("Active".equalsIgnoreCase(violation.getStatus())) {
						hasActiveViolations = true;
						break;
					}
				}
			}
			
			if (hasActiveViolations) {
				JOptionPane.showMessageDialog(this, 
					"Cannot print Good Moral Certificate. Student has active violations that need to be resolved first.",
					"Active Violations Found",
					JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			// If no active violations, proceed with creating the good moral
			GoodMoralGenerator generator = new GoodMoralGenerator(student);
			generator.createGoodMoralReport(this);
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error checking violations: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
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

		personalInfoPanel.add(new JLabel("Mother Tongue"), "cell 3 2,alignx leading");

		personalInfoPanel.add(motherTongueField, "cell 4 2,growx");

		personalInfoPanel.add(new JLabel("Middle Name:"), "cell 0 3, leading");
		personalInfoPanel.add(middleNameField, "cell 1 3, growx");

		personalInfoPanel.add(new JLabel("Full Address:"), "cell 3 3,alignx left");

		personalInfoPanel.add(fullAddField, "cell 4 3,growx");

		personalInfoPanel.add(new JLabel("Sex:"), "cell 0 4, leading");
		personalInfoPanel.add(sexComboBox, "cell 1 4, growx");

		personalInfoPanel.add(new JLabel("Date of Birth:"), "cell 0 5, leading");
		personalInfoPanel.add(dobField, "flowx,cell 1 5,growx");

		personalInfoPanel.add(new JLabel("AGE "), "cell 1 5,alignx leading");
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

	private JPanel createSchoolFormPanel() {
		JPanel schoolFormPanel = new JPanel(new MigLayout("wrap 4", "[][grow]15[][grow]", "[]5[]5[]5[]5[]5[]"));
		schoolFormPanel.setBorder(
				new TitledBorder(null, "SCHOOL FORM INFORMATION", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		// Initialize text fields if not already done
		if (schoolNameField == null) {
			schoolNameField = new JTextField();
			schoolNameField.setEditable(false);
		}
		if (schoolIdField == null) {
			schoolIdField = new JTextField();
			schoolIdField.setEditable(false);
		}
		if (districtField == null) {
			districtField = new JTextField();
			districtField.setEditable(false);
		}
		if (divisionField == null) {
			divisionField = new JTextField();
			divisionField.setEditable(false);
		}
		if (schoolRegionField == null) {
			schoolRegionField = new JTextField();
			schoolRegionField.setEditable(false);
		}
		if (semesterField == null) {
			semesterField = new JTextField();
			semesterField.setEditable(false);
		}
		if (schoolYearField == null) {
			schoolYearField = new JTextField();
			schoolYearField.setEditable(false);
		}
		if (gradeLevelField == null) {
			gradeLevelField = new JTextField();
			gradeLevelField.setEditable(false);
		}
		if (sectionField == null) {
			sectionField = new JTextField();
			sectionField.setEditable(false);
		}
		if (trackField == null) {
			trackField = new JTextField();
			trackField.setEditable(false);
		}
		if (courseField == null) {
			courseField = new JTextField();
			courseField.setEditable(false);
		}

		// Add components to panel
		schoolFormPanel.add(new JLabel("School Name:"), "cell 0 0");
		schoolFormPanel.add(schoolNameField, "cell 1 0,growx");
		schoolFormPanel.add(new JLabel("School ID:"), "cell 2 0");
		schoolFormPanel.add(schoolIdField, "cell 3 0,growx");

		schoolFormPanel.add(new JLabel("District:"), "cell 0 1");
		schoolFormPanel.add(districtField, "cell 1 1,growx");
		schoolFormPanel.add(new JLabel("Division:"), "cell 2 1");
		schoolFormPanel.add(divisionField, "cell 3 1,growx");

		schoolFormPanel.add(new JLabel("Region:"), "cell 0 2");
		schoolFormPanel.add(schoolRegionField, "cell 1 2,growx");
		schoolFormPanel.add(new JLabel("Semester:"), "cell 2 2");
		schoolFormPanel.add(semesterField, "cell 3 2,growx");

		schoolFormPanel.add(new JLabel("School Year:"), "cell 0 3");
		schoolFormPanel.add(schoolYearField, "cell 1 3,growx");
		schoolFormPanel.add(new JLabel("Grade Level:"), "cell 2 3");
		schoolFormPanel.add(gradeLevelField, "cell 3 3,growx");

		schoolFormPanel.add(new JLabel("Section:"), "cell 0 4");
		schoolFormPanel.add(sectionField, "cell 1 4,growx");
		schoolFormPanel.add(new JLabel("Track & Strand:"), "cell 2 4");
		schoolFormPanel.add(trackField, "cell 3 4,growx");

		schoolFormPanel.add(new JLabel("Course:"), "cell 0 5");
		schoolFormPanel.add(courseField, "cell 1 5 3 1,growx");

		return schoolFormPanel;
	}

	private JPanel createViolationTablePanel() {
		JPanel violationTablePanel = new JPanel(new BorderLayout());
		violationTablePanel.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Violation Records", TitledBorder.LEADING, TitledBorder.TOP, null));

		// Initialize the table first
		initializeViolationTable();

		// Create container panel to hold both table and message
		containerPanel = new JPanel(new CardLayout());

		// Create "No Violations" panel with centered message
		noViolationPanel = new JPanel(new GridBagLayout());
		JLabel noViolationLabel = new JLabel("No violation records found");
		noViolationLabel.setFont(new Font("Arial", Font.BOLD, 14));
		noViolationLabel.setForeground(new Color(128, 128, 128));
		noViolationPanel.add(noViolationLabel);

		// Add components to container
		containerPanel.add(new JScrollPane(violationTable), "TABLE");
		containerPanel.add(noViolationPanel, "NO_DATA");

		violationTablePanel.add(containerPanel, BorderLayout.CENTER);
		return violationTablePanel;
	}

	private void loadViolations(int studentUID, Connection connection) {
		try {
			ViolationDAO ViolationDAO = new ViolationDAO(connection);
			List<Violation> violations = ViolationDAO.getViolationsByStudentUID(studentUID);
			CardLayout cardLayout = (CardLayout) containerPanel.getLayout();

			if (violations == null || violations.isEmpty()) {
				cardLayout.show(containerPanel, "NO_DATA");
			} else {
				cardLayout.show(containerPanel, "TABLE");
				DefaultTableModel model = (DefaultTableModel) violationTable.getModel();
				model.setRowCount(0);

				for (int i = 0; i < violations.size(); i++) {
					Violation violation = violations.get(i);
					model.addRow(new Object[] {
							violation.getViolationType(),
							violation.getViolationDescription(),
							violation.getReinforcement(),
							violation.getStatus(),
							"actions"  // This column will be handled by TableActionManager
					});
					// Store violation object directly in table
					violationTable.putClientProperty("violation_" + i, violation);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to load violations: " + e.getMessage());
		}
	}

	private void viewViolation(int row) {
		Violation violation = (Violation) violationTable.getClientProperty("violation_" + row);
		if (violation != null) {
			String modalId = "violation_details_" + violation.getViolationId();
			
			// Check if modal is already open
			if (ModalDialog.isIdExist(modalId)) {
				return;
			}

			JPanel violationDetailsPanel = new JPanel(new MigLayout("wrap 2", "[][grow]", "[]10[]10[]10[]10[]"));
			violationDetailsPanel.add(new JLabel("Violation Type:"), "");
			violationDetailsPanel.add(new JLabel(violation.getViolationType()), "growx");
			violationDetailsPanel.add(new JLabel("Description:"), "");
			violationDetailsPanel.add(new JLabel(violation.getViolationDescription()), "growx");
			violationDetailsPanel.add(new JLabel("Session Summary:"), "");
			violationDetailsPanel.add(new JLabel(violation.getSessionSummary()), "growx");
			violationDetailsPanel.add(new JLabel("Reinforcement:"), "");
			violationDetailsPanel.add(new JLabel(violation.getReinforcement()), "growx");
			violationDetailsPanel.add(new JLabel("Status:"), "");
			violationDetailsPanel.add(new JLabel(violation.getStatus()), "growx");

			// Configure modal options
			ModalDialog.getDefaultOption()
				.setOpacity(0f)
				.setAnimationOnClose(false)
				.getBorderOption()
				.setBorderWidth(0.5f)
				.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

			// Show modal with unique ID
			ModalDialog.showModal(this,
					new SimpleModalBorder(violationDetailsPanel, "Violation Details",
							new SimpleModalBorder.Option[] {
									new SimpleModalBorder.Option("Close", SimpleModalBorder.CANCEL_OPTION) },
							(controller, action) -> {
								if (action == SimpleModalBorder.CANCEL_OPTION || 
									action == SimpleModalBorder.CLOSE_OPTION) {
									controller.close();
								}
							}),
					modalId);

			// Set size for the modal
			ModalDialog.getDefaultOption().getLayoutOption().setSize(600, 400);
		}
	}

	private void resolveViolation(Violation violation) {
		try {
			ViolationDAO ViolationDAO = new ViolationDAO(connect);

			int choice = JOptionPane.showConfirmDialog(this, "Do you want to mark this violation as resolved?",
					"Resolve Violation", JOptionPane.YES_NO_OPTION);

			if (choice == JOptionPane.YES_OPTION) {
				if (ViolationDAO.updateViolationStatus(violation.getViolationId(), "RESOLVED")) {
					JOptionPane.showMessageDialog(this, "Violation resolved successfully", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					// Reload violations to refresh the table
					loadViolations(Integer.parseInt(lrnField.getText()), connect);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to resolve violation: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void dropStudentModal() {
		try {
			// Fetch student data
			StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connect);
			Student student = studentsDataDAO.getStudentDataByLrn(lrnField.getText());

			if (student == null) {
				JOptionPane.showMessageDialog(this, "Student not found.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Fetch guidance counselor data
			GuidanceCounselorDAO guidanceCounselorDAO = new GuidanceCounselorDAO(connect);
			GuidanceCounselor counselor = guidanceCounselorDAO.readGuidanceCounselor(1); // Assuming ID 1 for now

			// Create a modal dialog for dropping a student
			DroppingForm droppingForm = new DroppingForm(connect, student, counselor);

			String modalId = "drop_student_" + student.getStudentUid();
			
			// Check if modal is already open
			if (ModalDialog.isIdExist(modalId)) {
				return;
			}

			// Configure modal options
			ModalDialog.getDefaultOption()
				.setOpacity(0f)
				.setAnimationOnClose(false)
				.getBorderOption()
				.setBorderWidth(0.5f)
				.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

			ModalDialog.showModal(this, new SimpleModalBorder(droppingForm, "Drop Confirmation",
					new SimpleModalBorder.Option[] {
							new SimpleModalBorder.Option("Print & Drop", SimpleModalBorder.YES_OPTION),
							new SimpleModalBorder.Option("Cancel", SimpleModalBorder.NO_OPTION)
					},
					(controller, action) -> {
						if (action == SimpleModalBorder.YES_OPTION) {
							controller.consume();
							try {
								// First print the dropping form
								DroppingFormGenerator.createDroppingForm(
									this,
									droppingForm.getDateField().getText(),
									student.getStudentFirstname() + " " + student.getStudentLastname(),
									counselor.getFirstName() + " " + counselor.getLastName(),
									student.getSchoolForm().getSF_TRACK_AND_STRAND(),
									student.getSchoolForm().getSF_GRADE_LEVEL() + " " + student.getSchoolForm().getSF_SECTION(),
									droppingForm.getAbsencesField().getText(),
									droppingForm.getActionTextArea().getText(),
									droppingForm.getReasonTextArea().getText(),
									droppingForm.getEffectiveDateField().getText()
								);

								// Then drop the student
								boolean success = studentsDataDAO.dropStudentWithRelations(student.getStudentUid());

								if (success) {
									JOptionPane.showMessageDialog(this, 
										"Student has been dropped successfully.",
										"Success", 
										JOptionPane.INFORMATION_MESSAGE);
									
									// Close the modal
									controller.close();
									
									// Navigate back or refresh UI
									FormManager.showForm(new StudentMangementGUI(connect));
								} else {
									JOptionPane.showMessageDialog(this, 
										"Failed to drop student records.",
										"Error", 
										JOptionPane.ERROR_MESSAGE);
								}
							} catch (SQLException e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(this, 
									"An error occurred while dropping the student: " + e.getMessage(),
									"Error", 
									JOptionPane.ERROR_MESSAGE);
							}
						} else if (action == SimpleModalBorder.NO_OPTION || 
								 action == SimpleModalBorder.CLOSE_OPTION || 
								 action == SimpleModalBorder.CANCEL_OPTION) {
							controller.close();
						}
					}), 
					modalId);

			ModalDialog.getDefaultOption().getLayoutOption().setSize(950, 600);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
				"Failed to load student or counselor data.", 
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}