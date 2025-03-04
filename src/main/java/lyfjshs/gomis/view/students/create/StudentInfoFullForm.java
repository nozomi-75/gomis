package lyfjshs.gomis.view.students.create;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class StudentInfoFullForm extends Form {
	private JTextField lrnField, lastNameField, firstNameField, middleNameField;
	private JComboBox<String> sexComboBox;
	private DatePicker birthDatePicker;
	private JFormattedTextField birthDateEditor;
	private JTextField ageField, birthPlaceField, motherTongueField, ipTypeField, religionField;;
	private JTextField houseNoField, streetField, regionField, provinceField, municipalityField, barangayField,
			zipCodeField;
	private JTextField fatherLastNameField, fatherFirstNameField, fatherMiddleNameField, fatherPhoneNumberField;
	private JTextField motherLastNameField, motherFirstNameField, motherMiddleNameField, motherPhoneNumberField;
	private JTextField guardianNameField, relationToStudentField, guardianPhoneField;
	private Connection connect;

	public StudentInfoFullForm(Connection conn) {
		this.connect = conn;
		setLayout(new MigLayout("wrap 1, fillx, insets 10", "[grow]", "[]10[]10[]10[][][]"));

		add(createStudentPanel(), "cell 0 0, growx");
		add(createAddressPanel(), "cell 0 1, growx");
		add(createParentPanel(), "cell 0 2,growx");

		add(createGuardianPanel(), "cell 0 3,growx");

		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(e -> submitForm());
		add(submitButton, "width 100,alignx center");
	}

	private JPanel createStudentPanel() {
		JPanel panel = new JPanel(new MigLayout("", "[right][grow,fill][right][grow,fill][right][grow]", "[][][][]"));
		panel.setBorder(new TitledBorder("STUDENT INFORMATION"));

		lrnField = new JTextField(15);
		lastNameField = new JTextField(15);
		firstNameField = new JTextField(15);

		birthDatePicker = new DatePicker();
		birthDatePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
		birthDatePicker.setDateFormat("yyyy-MM-dd");
		birthDatePicker.addDateSelectionListener(dateEvent -> updateAgeField());

		motherTongueField = new JTextField(15);

		lrnField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (!Character.isDigit(e.getKeyChar())) {
					e.consume();
				}
			}
		});

		panel.add(new JLabel("LRN:"), "cell 0 0");
		panel.add(lrnField, "cell 1 0 2 1");
		JLabel label_4 = new JLabel("Sex:");
		panel.add(label_4, "cell 4 0,gapx 15");
		sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
		panel.add(sexComboBox, "cell 5 0");

		panel.add(new JLabel("Last Name:"), "cell 0 1");
		panel.add(lastNameField, "cell 1 1");
		panel.add(new JLabel("First Name:"), "cell 2 1,alignx center,gapx 15,growy");
		panel.add(firstNameField, "cell 3 1");
		JLabel label = new JLabel("Middle Name:");
		panel.add(label, "cell 4 1");
		middleNameField = new JTextField(15);
		panel.add(middleNameField, "cell 5 1,growx");

		JLabel label_1 = new JLabel("Birthdate:");
		panel.add(label_1, "cell 0 2,gapx 15");

		birthDateEditor = new JFormattedTextField();
		birthDatePicker.setEditor(birthDateEditor);
		panel.add(birthDateEditor, "cell 1 2");
		JLabel label_2 = new JLabel("Age:");
		panel.add(label_2, "cell 2 2,alignx right");

		ageField = new JTextField(5);
		ageField.setEditable(false);
		panel.add(ageField, "cell 3 2");
		JLabel label_3 = new JLabel("Birthplace:");
		panel.add(label_3, "cell 4 2,alignx right,gapx 15");
		birthPlaceField = new JTextField(20);
		panel.add(birthPlaceField, "cell 5 2,growx");

		// New Fields
		panel.add(new JLabel("Mother Tongue:"), "cell 0 3");
		panel.add(motherTongueField, "cell 1 3,growx");
		JLabel lblIpethnicGroup = new JLabel("IP (Ethnic Group):");
		panel.add(lblIpethnicGroup, "cell 2 3,alignx right,gapx 15");
		ipTypeField = new JTextField(15);
		panel.add(ipTypeField, "cell 3 3,growx");

		JLabel label_5 = new JLabel("Religion:");
		panel.add(label_5, "cell 4 3");
		religionField = new JTextField(15);
		panel.add(religionField, "cell 5 3,growx");

		return panel;
	}

	private JPanel createAddressPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));
		panel.setBorder(new TitledBorder("RESIDENTIAL ADDRESS"));

		houseNoField = new JTextField(15);
		streetField = new JTextField(25);
		regionField = new JTextField(15);
		provinceField = new JTextField(15);
		municipalityField = new JTextField(15);
		barangayField = new JTextField(15);
		zipCodeField = new JTextField(8);
		zipCodeField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if (!Character.isDigit(e.getKeyChar())) {
					e.consume();
				}
			}
		});

		panel.add(new JLabel("House No:"));
		panel.add(houseNoField);
		panel.add(new JLabel("Street:"), "gapleft 15");
		panel.add(streetField);
		panel.add(new JLabel("Region:"));
		panel.add(regionField);
		panel.add(new JLabel("Province:"), "gapleft 15");
		panel.add(provinceField);
		panel.add(new JLabel("Municipality:"));
		panel.add(municipalityField);
		panel.add(new JLabel("Barangay:"), "gapleft 15");
		panel.add(barangayField);
		panel.add(new JLabel("Zip Code:"));
		panel.add(zipCodeField, "span");

		return panel;
	}

	private JPanel createParentPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));
		panel.setBorder(new TitledBorder("PARENT'S INFORMATION"));

		fatherLastNameField = new JTextField(15);
		fatherFirstNameField = new JTextField(15);
		fatherMiddleNameField = new JTextField(15);
		fatherPhoneNumberField = new JTextField(15);
		motherLastNameField = new JTextField(15);
		motherFirstNameField = new JTextField(15);
		motherMiddleNameField = new JTextField(15);
		motherPhoneNumberField = new JTextField(15);

		panel.add(new JLabel("Father's Last Name:"));
		panel.add(fatherLastNameField);
		panel.add(new JLabel("Mother's Last Name:"));
		panel.add(motherLastNameField);
		panel.add(new JLabel("Father's First Name:"));
		panel.add(fatherFirstNameField);
		panel.add(new JLabel("Mother's First Name:"));
		panel.add(motherFirstNameField);
		panel.add(new JLabel("Father's Middle Name:"));
		panel.add(fatherMiddleNameField);
		panel.add(new JLabel("Mother's Middle Name:"));
		panel.add(motherMiddleNameField);
		panel.add(new JLabel("Father's Contact Numeber:"));
		panel.add(fatherPhoneNumberField);
		panel.add(new JLabel("Mother's Contact Numeber:"));
		panel.add(motherPhoneNumberField);

		return panel;
	}

	private JPanel createGuardianPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow]", "[]5[]5[]"));
		panel.setBorder(new TitledBorder("GUARDIAN'S INFORMATION"));

		guardianNameField = new JTextField(15);
		relationToStudentField = new JTextField(15);
		guardianPhoneField = new JTextField(15);

		panel.add(new JLabel("Name:"));
		panel.add(guardianNameField, "growx");
		panel.add(new JLabel("Relation to Student:"));
		panel.add(relationToStudentField, "growx");
		panel.add(new JLabel("Contact Number:"));
		panel.add(guardianPhoneField, "growx");

		return panel;
	}

	private void updateAgeField() {
		LocalDate selectedDate = birthDatePicker.getSelectedDate();
		ageField.setText(
				selectedDate != null ? String.valueOf(Period.between(selectedDate, LocalDate.now()).getYears()) : "");
	}

	private void submitForm() {
		try {
			Address address = new Address(0, houseNoField.getText().trim(), streetField.getText().trim(),
					regionField.getText().trim(), provinceField.getText().trim(), municipalityField.getText().trim(),
					barangayField.getText().trim(), zipCodeField.getText().trim());

			Contact contact = new Contact(0, guardianPhoneField.getText().trim());

			Parents parents = new Parents(0, // ID will be auto-generated
					fatherLastNameField.getText().trim(),
					fatherFirstNameField.getText().trim(),
					fatherMiddleNameField.getText().trim(),
					fatherPhoneNumberField.getText().trim(),
					motherLastNameField.getText().trim(),
					motherFirstNameField.getText().trim(),
					motherMiddleNameField.getText().trim(),
					motherPhoneNumberField.getText().trim());
			parents.setFatherContactNumber(fatherPhoneNumberField.getText().trim());
			parents.setMotherContactNumber(motherPhoneNumberField.getText().trim());

			String[] guardianNameParts = guardianNameField.getText().trim().split(" ", 3);
			Guardian guardian = new Guardian(0,
					guardianNameParts.length > 2 ? guardianNameParts[2] : "",
					guardianNameParts.length > 0 ? guardianNameParts[0] : "",
					guardianNameParts.length > 1 ? guardianNameParts[1] : "",
					relationToStudentField.getText().trim(),
					guardianPhoneField.getText().trim());

			Student student = new Student(0, 0, 0, 0, 0, lrnField.getText().trim(),
					lastNameField.getText().trim(), firstNameField.getText().trim(), middleNameField.getText().trim(),
					sexComboBox.getSelectedItem().toString(), java.sql.Date.valueOf(birthDatePicker.getSelectedDate()),
					motherTongueField.getText().trim(), Integer.parseInt(ageField.getText().trim()),
					ipTypeField.getText().trim(), religionField.getText().trim());

			StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connect);
			boolean success = studentsDataDAO.createStudentWithRelations(student, address, contact, parents, guardian);
			if (success) {
				JOptionPane.showMessageDialog(this, "Student information successfully saved!", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				clearForm();
			} else {
				JOptionPane.showMessageDialog(this, "Failed to save student information.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void clearForm() {
		lrnField.setText("");
		lastNameField.setText("");
		firstNameField.setText("");
		middleNameField.setText("");
		sexComboBox.setSelectedIndex(0);
		birthDateEditor.setText("");
		ageField.setText("");
		birthPlaceField.setText("");
		motherTongueField.setText("");
		ipTypeField.setText("");
		religionField.setText("");
		houseNoField.setText("");
		streetField.setText("");
		regionField.setText("");
		provinceField.setText("");
		municipalityField.setText("");
		barangayField.setText("");
		zipCodeField.setText("");
		fatherLastNameField.setText("");
		fatherFirstNameField.setText("");
		fatherMiddleNameField.setText("");
		fatherPhoneNumberField.setText("");
		motherLastNameField.setText("");
		motherFirstNameField.setText("");
		motherMiddleNameField.setText("");
		motherPhoneNumberField.setText("");
		guardianNameField.setText("");
		relationToStudentField.setText("");
		guardianPhoneField.setText("");
	}

}
