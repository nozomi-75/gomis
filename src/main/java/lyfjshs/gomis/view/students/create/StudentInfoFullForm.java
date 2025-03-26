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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class StudentInfoFullForm extends Form {
    private JTextField lrnField, lastNameField, firstNameField, middleNameField;
    private JComboBox<String> sexComboBox;
    private DatePicker birthDatePicker;
    private JFormattedTextField birthDateEditor;
    private JTextField ageField, motherTongueField, ipTypeField, religionField;
    private JTextField houseNoField, streetField, regionField, provinceField, municipalityField, barangayField,
            zipCodeField;
    private JTextField fatherLastNameField, fatherFirstNameField, fatherMiddleNameField, fatherPhoneNumberField;
    private JTextField motherLastNameField, motherFirstNameField, motherMiddleNameField, motherPhoneNumberField;
    private JTextField guardianNameField, relationToStudentField, guardianPhoneField;
    private JTextField schoolNameField, schoolIdField, districtField, divisionField, schoolRegionField;
    private JTextField semesterField, schoolYearField, gradeLevelField, sectionField, trackField, courseField;
    private Connection connect;
    private JScrollPane scrollPane;
    private JPanel panel_1;

    public StudentInfoFullForm(Connection conn) {
        this.connect = conn;
        setLayout(new MigLayout("wrap 1, fillx, insets 10", "[grow]", "[]"));

        // Initialize birthDatePicker first to avoid NullPointerException
        birthDatePicker = new DatePicker();
        birthDatePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        birthDatePicker.setDateFormat("yyyy-MM-dd");
        birthDatePicker.addDateSelectionListener(dateEvent -> updateAgeField());

        scrollPane = new JScrollPane();
        add(scrollPane, "cell 0 0,grow");

        panel_1 = new JPanel(new MigLayout("wrap 1, fillx, insets 10", "[grow]", "[]10[]10[]10[][][]"));

        scrollPane.setViewportView(panel_1);

        // Create and add all panels
        JPanel studentPanel = createStudentPanel();
        panel_1.add(studentPanel, "growx");

        JPanel addressPanel = createAddressPanel();
        panel_1.add(addressPanel, "growx");
        
        JPanel parentPanel = createParentPanel();
        panel_1.add(parentPanel, "growx");
        
        JPanel guardianPanel = createGuardianPanel();
        panel_1.add(guardianPanel, "growx");
        
        JPanel schoolFormPanel = createSchoolFormPanel();
        panel_1.add(schoolFormPanel, "growx");

        JButton submitButton = new JButton("Submit");
        panel_1.add(submitButton, "alignx right");
        submitButton.addActionListener(e -> submitForm());
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new MigLayout("", "[right][grow,fill][right][grow,fill][right][grow]", "[][][][]"));
        panel.setBorder(new TitledBorder("STUDENT INFORMATION"));

        lrnField = new JTextField(15);
        lastNameField = new JTextField(15);
        firstNameField = new JTextField(15);
        middleNameField = new JTextField(15);
        motherTongueField = new JTextField(15);
        ipTypeField = new JTextField(15);
        religionField = new JTextField(15);
        ageField = new JTextField(5);
        ageField.setEditable(false);

        lrnField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        panel.add(new JLabel("LRN:"), "cell 0 0");
        panel.add(lrnField, "cell 1 0 2 1");
        panel.add(new JLabel("Sex:"), "cell 4 0,gapx 15");
        sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
        panel.add(sexComboBox, "cell 5 0");

        panel.add(new JLabel("Last Name:"), "cell 0 1");
        panel.add(lastNameField, "cell 1 1");
        panel.add(new JLabel("First Name:"), "cell 2 1,alignx center,gapx 15,growy");
        panel.add(firstNameField, "cell 3 1");
        panel.add(new JLabel("Middle Name:"), "cell 4 1");
        panel.add(middleNameField, "cell 5 1,growx");

        panel.add(new JLabel("Birthdate:"), "cell 0 2,gapx 15");
        birthDateEditor = new JFormattedTextField();
        birthDatePicker.setEditor(birthDateEditor);
        panel.add(birthDateEditor, "cell 1 2");
        panel.add(new JLabel("Age:"), "cell 2 2,alignx right");
        panel.add(ageField, "cell 3 2");

        panel.add(new JLabel("Mother Tongue:"), "cell 0 3");
        panel.add(motherTongueField, "cell 1 3,growx");
        panel.add(new JLabel("IP (Ethnic Group):"), "cell 2 3,alignx right,gapx 15");
        panel.add(ipTypeField, "cell 3 3,growx");
        panel.add(new JLabel("Religion:"), "cell 4 3");
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
        panel.add(streetField, "growx");
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
        panel.add(new JLabel("Father's Contact Number:"));
        panel.add(fatherPhoneNumberField);
        panel.add(new JLabel("Mother's Contact Number:"));
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

    private JPanel createSchoolFormPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));
        panel.setBorder(new TitledBorder("SCHOOL FORM INFORMATION"));

        schoolNameField = new JTextField(20);
        schoolIdField = new JTextField(10);
        districtField = new JTextField(15);
        divisionField = new JTextField(15);
        schoolRegionField = new JTextField(15);
        semesterField = new JTextField(10);
        schoolYearField = new JTextField(10);
        gradeLevelField = new JTextField(5);
        sectionField = new JTextField(15);
        trackField = new JTextField(20);
        courseField = new JTextField(20);

        panel.add(new JLabel("School Name:"));
        panel.add(schoolNameField, "growx");
        panel.add(new JLabel("School ID:"), "gapleft 15");
        panel.add(schoolIdField);

        panel.add(new JLabel("District:"));
        panel.add(districtField);
        panel.add(new JLabel("Division:"), "gapleft 15");
        panel.add(divisionField);

        panel.add(new JLabel("Region:"));
        panel.add(schoolRegionField);
        panel.add(new JLabel("Semester:"), "gapleft 15");
        panel.add(semesterField);

        panel.add(new JLabel("School Year:"));
        panel.add(schoolYearField);
        panel.add(new JLabel("Grade Level:"), "gapleft 15");
        panel.add(gradeLevelField);

        panel.add(new JLabel("Section:"));
        panel.add(sectionField);
        panel.add(new JLabel("Track & Strand:"), "gapleft 15");
        panel.add(trackField);

        panel.add(new JLabel("Course:"));
        panel.add(courseField, "span");

        return panel;
    }

    private void updateAgeField() {
        LocalDate selectedDate = birthDatePicker.getSelectedDate();
        if (selectedDate != null) {
            int age = Period.between(selectedDate, LocalDate.now()).getYears();
            ageField.setText(String.valueOf(age));
        } else {
            ageField.setText("");
        }
    }

    private void submitForm() {
        try {
            // Validate required fields
            if (lrnField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty() || 
                firstNameField.getText().trim().isEmpty() || birthDatePicker.getSelectedDate() == null ||
                ageField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            Address address = new Address(0, houseNoField.getText().trim(), streetField.getText().trim(),
                    regionField.getText().trim(), provinceField.getText().trim(), municipalityField.getText().trim(),
                    barangayField.getText().trim(), zipCodeField.getText().trim());

            Contact contact = new Contact(0, guardianPhoneField.getText().trim());

            Parents parents = new Parents(0, fatherLastNameField.getText().trim(),
                    fatherFirstNameField.getText().trim(), fatherMiddleNameField.getText().trim(),
                    fatherPhoneNumberField.getText().trim(), motherLastNameField.getText().trim(),
                    motherFirstNameField.getText().trim(), motherMiddleNameField.getText().trim(),
                    motherPhoneNumberField.getText().trim());

            // Improved guardian name parsing
            String guardianFullName = guardianNameField.getText().trim();
            String lastName = "", firstName = "", middleName = "";
            
            String[] nameParts = guardianFullName.split("\\s+");
            if (nameParts.length >= 1) {
                firstName = nameParts[0];
            }
            if (nameParts.length >= 2) {
                if (nameParts.length == 2) {
                    lastName = nameParts[1];
                } else {
                    middleName = nameParts[1];
                    // Combine remaining parts as last name
                    StringBuilder lastNameBuilder = new StringBuilder();
                    for (int i = 2; i < nameParts.length; i++) {
                        if (i > 2) lastNameBuilder.append(" ");
                        lastNameBuilder.append(nameParts[i]);
                    }
                    lastName = lastNameBuilder.toString();
                }
            }

            Guardian guardian = new Guardian(0, lastName, firstName, middleName, 
                    relationToStudentField.getText().trim(), guardianPhoneField.getText().trim());

            SchoolForm schoolForm = new SchoolForm(0, schoolNameField.getText().trim(), schoolIdField.getText().trim(),
                    districtField.getText().trim(), divisionField.getText().trim(), schoolRegionField.getText().trim(),
                    semesterField.getText().trim(), schoolYearField.getText().trim(), gradeLevelField.getText().trim(),
                    sectionField.getText().trim(), trackField.getText().trim(), courseField.getText().trim());

            // Create Student object with proper age parsing
            int age = 0;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid age value.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student student = new Student(0, 0, 0, 0, 0, sectionField.getText().trim(), 
                    lrnField.getText().trim(), lastNameField.getText().trim(), firstNameField.getText().trim(),
                    middleNameField.getText().trim(), sexComboBox.getSelectedItem().toString(),
                    java.sql.Date.valueOf(birthDatePicker.getSelectedDate()), motherTongueField.getText().trim(),
                    age, ipTypeField.getText().trim(), religionField.getText().trim(),
                    address, contact, parents, guardian, schoolForm);

            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connect);
            boolean success = studentsDataDAO.createStudentWithRelations(student, address, contact, parents, guardian,
                    schoolForm);

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
        schoolNameField.setText("");
        schoolIdField.setText("");
        districtField.setText("");
        divisionField.setText("");
        schoolRegionField.setText("");
        semesterField.setText("");
        schoolYearField.setText("");
        gradeLevelField.setText("");
        sectionField.setText("");
        trackField.setText("");
        courseField.setText("");
    }
}