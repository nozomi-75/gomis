/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students.create;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Database.DAO.SchoolFormDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.AllForms;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.utils.ErrorDialogUtils;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.utils.ValidationUtils;
import lyfjshs.gomis.view.students.schoolForm.ImportSF;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.Toast;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class CreateStudentData extends Form {
    private static final Logger logger = LogManager.getLogger(CreateStudentData.class);
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
    private JTextField semesterField, schoolYearField;
    private JComboBox<String> gradeLevelComboBox;
    private JTextField sectionField, trackField, courseField;
    private Connection connection;
    private JScrollPane scrollPane;
    private JPanel formContainer;
    private JButton importSFbtn;
    private JComboBox<String> schoolNameComboBox;
    private JComboBox<String> schoolIdComboBox;
    private JComboBox<String> trackStrandComboBox;
    private JComboBox<String> courseComboBox;
    private JComboBox<String> schoolYearComboBox;
    private SchoolFormDAO schoolFormDAO;

    public CreateStudentData(Connection conn) {
        this.connection = conn;
        this.schoolFormDAO = new SchoolFormDAO(connection);
        setLayout(new MigLayout("fill, insets 10", "[center]", "[]"));

        // Initialize birthDatePicker first to avoid NullPointerException
        birthDatePicker = new DatePicker();
        birthDatePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        birthDatePicker.setDateFormat("yyyy-MM-dd");
        birthDatePicker.addDateSelectionListener(dateEvent -> updateAgeField());

        JLabel pageTitle = new JLabel("Create Student Data", SwingConstants.CENTER);
        pageTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

        formContainer = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][][][][][][]"));
        formContainer.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:shade($Panel.background,5%)");
        formContainer.add(pageTitle, "flowx,growx,gapbottom 20");

        scrollPane = new JScrollPane(formContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(50);
        scrollPane.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        add(scrollPane, "cell 0 0,grow");

        // Create and add all panels
        JPanel studentPanel = createStudentPanel();
        formContainer.add(studentPanel, "span, growx");

        JPanel addressPanel = createAddressPanel();
        formContainer.add(addressPanel, "span, growx");
        
        JPanel parentPanel = createParentPanel();
        formContainer.add(parentPanel, "span, growx");
        
        JPanel guardianPanel = createGuardianPanel();
        formContainer.add(guardianPanel, "span, growx");
        
        JPanel schoolFormPanel = createSchoolFormPanel();
        formContainer.add(schoolFormPanel, "span, growx");

        JButton submitButton = new JButton("Submit");
        JButton clearButton = new JButton("Clear Form");
        
        // Create a button panel
        JPanel buttonPanel = new JPanel(new MigLayout("insets 10", "[grow][][]", "[]"));
        buttonPanel.putClientProperty(FlatClientProperties.STYLE, "background:$Panel.background");
        
        clearButton.putClientProperty(FlatClientProperties.STYLE, 
            "background:shade($Panel.background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");
        clearButton.addActionListener(e -> clearForm());
        
        submitButton.putClientProperty(FlatClientProperties.STYLE,
            "background:tint($Panel.background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");
        submitButton.addActionListener(e -> submitFormAsync(submitButton));
        
        buttonPanel.add(new JLabel(), "grow");
        buttonPanel.add(clearButton, "gapright 10");
        buttonPanel.add(submitButton);
        formContainer.add(buttonPanel, "span, growx");
        importSFbtn = new JButton("Import SF");
        importSFbtn.addActionListener(e -> switchToImportSF());
        formContainer.add(importSFbtn, "cell 0 0");

        initializeValidation();
    }

    private JLabel createRequiredLabel(String text) {
        JLabel label = new JLabel("<html>" + text + " <font color='red'>*</font></html>");
        return label;
    }

    private void initializeValidation() {
        // LRN field - numbers only, max 12 digits
        lrnField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || lrnField.getText().length() >= 12) {
                    e.consume();
                }
            }
        });

        // Name fields - letters, spaces, hyphens, and apostrophes only
        KeyAdapter nameValidator = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '\'') {
                    e.consume();
                }
            }
        };
        firstNameField.addKeyListener(nameValidator);
        lastNameField.addKeyListener(nameValidator);
        middleNameField.addKeyListener(nameValidator);

        // Phone number fields - numbers only, max 11 digits
        KeyAdapter phoneValidator = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                JTextField source = (JTextField) e.getComponent();
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || source.getText().length() >= 11) {
                    e.consume();
                }
            }
        };
        guardianPhoneField.addKeyListener(phoneValidator);
        fatherPhoneNumberField.addKeyListener(phoneValidator);
        motherPhoneNumberField.addKeyListener(phoneValidator);
    }
    
    private boolean validateAndHighlightFields() {
        boolean isValid = true;
        // Use ValidationUtils for required fields
        if (ValidationUtils.isFieldEmpty(lrnField)) {
            ErrorDialogUtils.showError(this, "LRN is required.");
            isValid = false;
        }
        if (ValidationUtils.isFieldEmpty(firstNameField)) {
            ErrorDialogUtils.showError(this, "First name is required.");
            isValid = false;
        }
        if (ValidationUtils.isFieldEmpty(lastNameField)) {
            ErrorDialogUtils.showError(this, "Last name is required.");
            isValid = false;
        }
        if (ValidationUtils.isFieldEmpty(birthDateEditor)) {
            ErrorDialogUtils.showError(this, "Birth Date is required.");
            isValid = false;
        }
        if (ValidationUtils.isFieldEmpty(motherTongueField)) {
            ErrorDialogUtils.showError(this, "Mother Tongue is required.");
            isValid = false;
        }
        if (ValidationUtils.isFieldEmpty(religionField)) {
            ErrorDialogUtils.showError(this, "Religion is required.");
            isValid = false;
        }
        // ComboBox and other non-JTextField checks
        if (schoolNameComboBox.getSelectedItem() == null || schoolNameComboBox.getSelectedIndex() == -1 || schoolNameComboBox.getSelectedItem().toString().trim().isEmpty()) {
            ErrorDialogUtils.showError(this, "School Name is required.");
            isValid = false;
        }
        if (schoolYearComboBox.getSelectedItem() == null || schoolYearComboBox.getSelectedIndex() == -1 || schoolYearComboBox.getSelectedItem().toString().trim().isEmpty()) {
            ErrorDialogUtils.showError(this, "School Year is required.");
            isValid = false;
        }
        if (sectionField.getText().trim().isEmpty()) {
            ErrorDialogUtils.showError(this, "Section is required.");
            isValid = false;
        }
        if (trackStrandComboBox.getSelectedItem() == null || trackStrandComboBox.getSelectedIndex() == -1 || trackStrandComboBox.getSelectedItem().toString().trim().isEmpty()) {
            ErrorDialogUtils.showError(this, "Track & Strand is required.");
            isValid = false;
        }
        return isValid;
    }
    
    private boolean confirmOptionalFields() {
        List<String> emptyOptionalFields = new ArrayList<>();
        if (schoolRegionField.getText().trim().isEmpty()) emptyOptionalFields.add("School Region");
        if (divisionField.getText().trim().isEmpty()) emptyOptionalFields.add("Division");
        if (districtField.getText().trim().isEmpty()) emptyOptionalFields.add("District");
        if (semesterField.getText().trim().isEmpty()) emptyOptionalFields.add("Semester");

        if (!emptyOptionalFields.isEmpty()) {
            StringBuilder message = new StringBuilder("The following fields are empty:\n");
            for (String field : emptyOptionalFields) {
                message.append("- ").append(field).append("\n");
            }
            message.append("\nDo you want to continue saving without them?");

            int response = JOptionPane.showConfirmDialog(this, message.toString(), "Confirm Optional Fields", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            return response == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private JPanel createStudentPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("Student Information");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][][][]"));
        contentPanel.setOpaque(false);

        // Row 0: Labels
        contentPanel.add(createRequiredLabel("LRN"), "cell 0 0");
        contentPanel.add(createRequiredLabel("Sex"), "cell 1 0");
        contentPanel.add(createRequiredLabel("Last Name"), "cell 2 0");
        contentPanel.add(createRequiredLabel("First Name"), "cell 3 0");

        // Row 1: Fields
        lrnField = new JTextField();
        lrnField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter 12-digit LRN");
        contentPanel.add(lrnField, "cell 0 1, growx");

        sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
        contentPanel.add(sexComboBox, "cell 1 1, growx");

        lastNameField = new JTextField();
        lastNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter last name");
        contentPanel.add(lastNameField, "cell 2 1, growx");

        firstNameField = new JTextField();
        firstNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter first name");
        contentPanel.add(firstNameField, "cell 3 1, growx");

        // Row 2: Labels
        contentPanel.add(new JLabel("Middle Name"), "cell 0 2");
        contentPanel.add(createRequiredLabel("Birth Date"), "cell 1 2");
        contentPanel.add(new JLabel("Age"), "cell 2 2");
        contentPanel.add(createRequiredLabel("Mother Tongue"), "cell 3 2");

        // Row 3: Fields
        middleNameField = new JTextField();
        middleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter middle name");
        contentPanel.add(middleNameField, "cell 0 3, growx");

        birthDateEditor = new JFormattedTextField();
        birthDatePicker.setEditor(birthDateEditor);
        contentPanel.add(birthDateEditor, "cell 1 3, growx");

        ageField = new JTextField();
        ageField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Auto-calculated age");
        ageField.setEditable(false);
        contentPanel.add(ageField, "cell 2 3, growx");

        motherTongueField = new JTextField();
        motherTongueField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother tongue");
        contentPanel.add(motherTongueField, "cell 3 3, growx");

        // Row 4: Labels
        contentPanel.add(new JLabel("IP (Ethnic Group)"), "cell 0 4");
        contentPanel.add(createRequiredLabel("Religion"), "cell 1 4");

        // Row 5: Fields
        ipTypeField = new JTextField();
        ipTypeField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter ethnic group if applicable");
        contentPanel.add(ipTypeField, "cell 0 5, growx");

        religionField = new JTextField();
        religionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter religion");
        contentPanel.add(religionField, "cell 1 5, growx");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
    }

    private JPanel createAddressPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("Residential Address (Optional)");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][]"));
        contentPanel.setOpaque(false);

        // Row 0: Labels
        contentPanel.add(new JLabel("House No."), "cell 0 0");
        contentPanel.add(new JLabel("Street"), "cell 1 0");
        contentPanel.add(new JLabel("Region"), "cell 2 0");
        contentPanel.add(new JLabel("Province"), "cell 3 0");

        // Row 1: Fields
        houseNoField = new JTextField();
        houseNoField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter house number");
        contentPanel.add(houseNoField, "cell 0 1, growx");

        streetField = new JTextField();
        streetField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter street");
        contentPanel.add(streetField, "cell 1 1, growx");

        regionField = new JTextField();
        regionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter region (e.g., Region IV-A)");
        contentPanel.add(regionField, "cell 2 1, growx");

        provinceField = new JTextField();
        provinceField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter province (e.g., Cavite)");
        contentPanel.add(provinceField, "cell 3 1, growx");

        // Row 2: Labels
        contentPanel.add(new JLabel("Municipality/City"), "cell 0 2");
        contentPanel.add(new JLabel("Barangay"), "cell 1 2");
        contentPanel.add(new JLabel("Zip Code"), "cell 2 2");

        // Row 3: Fields
        municipalityField = new JTextField();
        municipalityField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter municipality/city");
        contentPanel.add(municipalityField, "cell 0 3, growx");

        barangayField = new JTextField();
        barangayField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter barangay");
        contentPanel.add(barangayField, "cell 1 3, growx");

        zipCodeField = new JTextField();
        zipCodeField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter zip code");
        contentPanel.add(zipCodeField, "cell 2 3, growx");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
    }

    private JPanel createParentPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("Parent's Information (Optional)");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow]", "[]"));
        contentPanel.setOpaque(false);

        // Father's Information Panel
        JPanel fatherPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][]"));
        fatherPanel.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:tint($Panel.background,5%)");
        fatherPanel.setOpaque(false);

        JLabel fatherTitle = new JLabel("Father's Information");
        fatherTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        fatherPanel.add(fatherTitle, "wrap, gapbottom 10");

        JPanel fatherFieldsPanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[][]"));
        fatherFieldsPanel.setOpaque(false);
        fatherFieldsPanel.add(new JLabel("Last Name"), "wrap");
        fatherLastNameField = new JTextField();
        fatherLastNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's last name");
        fatherFieldsPanel.add(fatherLastNameField, "growx, wrap");
        fatherFieldsPanel.add(new JLabel("First Name"), "wrap");
        fatherFirstNameField = new JTextField();
        fatherFirstNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's first name");
        fatherFieldsPanel.add(fatherFirstNameField, "growx, wrap");
        fatherFieldsPanel.add(new JLabel("Middle Name"), "wrap");
        fatherMiddleNameField = new JTextField();
        fatherMiddleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's middle name");
        fatherFieldsPanel.add(fatherMiddleNameField, "growx, wrap");
        fatherFieldsPanel.add(new JLabel("Contact Number"), "wrap");
        fatherPhoneNumberField = new JTextField();
        fatherPhoneNumberField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter father's contact number");
        fatherFieldsPanel.add(fatherPhoneNumberField, "growx");
        fatherPanel.add(fatherFieldsPanel, "grow");

        // Mother's Information Panel
        JPanel motherPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][]"));
        motherPanel.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:tint($Panel.background,5%)");
        motherPanel.setOpaque(false);

        JLabel motherTitle = new JLabel("Mother's Information");
        motherTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        motherPanel.add(motherTitle, "wrap, gapbottom 10");

        JPanel motherFieldsPanel = new JPanel(new MigLayout("fill, insets 0", "[grow]", "[][]"));
        motherFieldsPanel.setOpaque(false);
        motherFieldsPanel.add(new JLabel("Last Name"), "wrap");
        motherLastNameField = new JTextField();
        motherLastNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's last name");
        motherFieldsPanel.add(motherLastNameField, "growx, wrap");
        motherFieldsPanel.add(new JLabel("First Name"), "wrap");
        motherFirstNameField = new JTextField();
        motherFirstNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's first name");
        motherFieldsPanel.add(motherFirstNameField, "growx, wrap");
        motherFieldsPanel.add(new JLabel("Middle Name"), "wrap");
        motherMiddleNameField = new JTextField();
        motherMiddleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's middle name");
        motherFieldsPanel.add(motherMiddleNameField, "growx, wrap");
        motherFieldsPanel.add(new JLabel("Contact Number"), "wrap");
        motherPhoneNumberField = new JTextField();
        motherPhoneNumberField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother's contact number");
        motherFieldsPanel.add(motherPhoneNumberField, "growx");
        motherPanel.add(motherFieldsPanel, "grow");

        contentPanel.add(fatherPanel, "grow");
        contentPanel.add(motherPanel, "grow");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
    }

    private JPanel createGuardianPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("Guardian's Information (Optional)");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow]", "[][]"));
        contentPanel.setOpaque(false);

        contentPanel.add(new JLabel("Guardian's Full Name"), "cell 0 0");
        contentPanel.add(new JLabel("Relation to Student"), "cell 1 0");
        contentPanel.add(new JLabel("Contact Number"), "cell 2 0");

        guardianNameField = new JTextField();
        guardianNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter guardian's full name");
        contentPanel.add(guardianNameField, "cell 0 1, growx");

        relationToStudentField = new JTextField();
        relationToStudentField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter relation to student");
        contentPanel.add(relationToStudentField, "cell 1 1, growx");

        guardianPhoneField = new JTextField();
        guardianPhoneField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter guardian's contact number");
        contentPanel.add(guardianPhoneField, "cell 2 1, growx");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
    }

    private JPanel createSchoolFormPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("School Form Information");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][][][]"));
        contentPanel.setOpaque(false);

        // Row 0: Labels
        contentPanel.add(createRequiredLabel("School Name"), "cell 0 0");
        contentPanel.add(new JLabel("School ID"), "cell 1 0");
        contentPanel.add(new JLabel("Region"), "cell 2 0");
        contentPanel.add(new JLabel("Division"), "cell 3 0");

        // Row 1: Fields
        schoolNameComboBox = new JComboBox<>(getDistinctSchoolNames());
        schoolNameComboBox.setEditable(true);
        schoolNameComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter school name");
        contentPanel.add(schoolNameComboBox, "cell 0 1, growx");

        schoolIdComboBox = new JComboBox<>(getDistinctSchoolIds());
        schoolIdComboBox.setEditable(true);
        schoolIdComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter school ID");
        contentPanel.add(schoolIdComboBox, "cell 1 1, growx");

        schoolRegionField = new JTextField();
        schoolRegionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter region (e.g., Region IV-A)");
        contentPanel.add(schoolRegionField, "cell 2 1, growx");

        divisionField = new JTextField();
        divisionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter division");
        contentPanel.add(divisionField, "cell 3 1, growx");

        // Row 2: Labels
        contentPanel.add(new JLabel("District"), "cell 0 2");
        contentPanel.add(new JLabel("Semester"), "cell 1 2");
        contentPanel.add(createRequiredLabel("School Year"), "cell 2 2");
        contentPanel.add(createRequiredLabel("Grade Level"), "cell 3 2");

        // Row 3: Fields
        districtField = new JTextField();
        districtField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter district");
        contentPanel.add(districtField, "cell 0 3, growx");

        semesterField = new JTextField();
        semesterField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter semester (e.g., 1st Semester)");
        contentPanel.add(semesterField, "cell 1 3, growx");

        schoolYearComboBox = new JComboBox<>(getDistinctSchoolYears());
        schoolYearComboBox.setEditable(true);
        schoolYearComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. 2024-2025");
        contentPanel.add(schoolYearComboBox, "cell 2 3, growx");

        gradeLevelComboBox = new JComboBox<>(new String[]{"Grade 11", "Grade 12"});
        contentPanel.add(gradeLevelComboBox, "cell 3 3, growx");

        // Row 4: Labels
        contentPanel.add(createRequiredLabel("Section"), "cell 0 4");
        contentPanel.add(createRequiredLabel("Track & Strand"), "cell 1 4");
        contentPanel.add(new JLabel("Course"), "cell 2 4");

        // Row 5: Fields
        sectionField = new JTextField();
        sectionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter section");
        contentPanel.add(sectionField, "cell 0 5, growx");

        trackStrandComboBox = new JComboBox<>(getDistinctTrackAndStrands());
        trackStrandComboBox.setEditable(true);
        trackStrandComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter track and strand (e.g., TVL-ICT)");
        contentPanel.add(trackStrandComboBox, "cell 1 5, growx");

        courseComboBox = new JComboBox<>(getDistinctCourses());
        courseComboBox.setEditable(true);
        courseComboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter course (e.g. Programming)");
        contentPanel.add(courseComboBox, "cell 2 5, growx");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
    }

    private String[] getDistinctSchoolNames() {
        List<String> names = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT SF_SCHOOL_NAME FROM SCHOOL_FORM WHERE SF_SCHOOL_NAME IS NOT NULL ORDER BY SF_SCHOOL_NAME";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("SF_SCHOOL_NAME"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching distinct school names", e);
        }
        return names.toArray(new String[0]);
    }

    private String[] getDistinctSchoolIds() {
        List<String> ids = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT SF_SCHOOL_ID FROM SCHOOL_FORM WHERE SF_SCHOOL_ID IS NOT NULL ORDER BY SF_SCHOOL_ID";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString("SF_SCHOOL_ID"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching distinct school IDs", e);
        }
        return ids.toArray(new String[0]);
    }

    private String[] getDistinctTrackAndStrands() {
        List<String> tracks = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT SF_TRACK_AND_STRAND FROM SCHOOL_FORM WHERE SF_TRACK_AND_STRAND IS NOT NULL ORDER BY SF_TRACK_AND_STRAND";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tracks.add(rs.getString("SF_TRACK_AND_STRAND"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching distinct track and strands", e);
        }
        return tracks.toArray(new String[0]);
    }

    private String[] getDistinctCourses() {
        List<String> courses = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT SF_COURSE FROM SCHOOL_FORM WHERE SF_COURSE IS NOT NULL ORDER BY SF_COURSE";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(rs.getString("SF_COURSE"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching distinct courses", e);
        }
        return courses.toArray(new String[0]);
    }

    private String[] getDistinctSchoolYears() {
        List<String> years = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT SF_SCHOOL_YEAR FROM SCHOOL_FORM WHERE SF_SCHOOL_YEAR IS NOT NULL ORDER BY SF_SCHOOL_YEAR";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    years.add(rs.getString("SF_SCHOOL_YEAR"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching distinct school years", e);
        }
        return years.toArray(new String[0]);
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

    private void submitFormAsync(JButton submitButton) {
        submitButton.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    return submitForm();
                } catch (Exception e) {
                    ErrorDialogUtils.showError(CreateStudentData.this, "Error: " + e.getMessage());
                    return false;
                }
            }
            @Override
            protected void done() {
                submitButton.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        Toast.show(CreateStudentData.this, Toast.Type.SUCCESS, "Student information successfully saved!", ToastLocation.BOTTOM_CENTER, createToastOption());
                    }
                } catch (Exception e) {
                    ErrorDialogUtils.showError(CreateStudentData.this, "Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private boolean submitForm() {
        if (!validateAndHighlightFields()) {
            return false;
        }
        if (!confirmOptionalFields()) {
            return false;
        }
        try {
            Address address = new Address(0, houseNoField.getText().trim(), streetField.getText().trim(),
                    regionField.getText().trim(), provinceField.getText().trim(), municipalityField.getText().trim(),
                    barangayField.getText().trim(), zipCodeField.getText().trim());

            Contact contact = new Contact(0, guardianPhoneField.getText().trim());

            Parents parents = new Parents(0, fatherLastNameField.getText().trim(),
                    fatherFirstNameField.getText().trim(), fatherMiddleNameField.getText().trim(),
                    fatherPhoneNumberField.getText().trim(), motherLastNameField.getText().trim(),
                    motherFirstNameField.getText().trim(), motherMiddleNameField.getText().trim(),
                    motherPhoneNumberField.getText().trim());
                    
            String guardianFullName = guardianNameField.getText().trim();
            String lastName = "", firstName = "", middleName = "";
            
            if(!guardianFullName.isEmpty()){
                String[] nameParts = guardianFullName.split("\\s+");
                if (nameParts.length > 0) firstName = nameParts[0];
                if (nameParts.length > 1) lastName = nameParts[nameParts.length-1];
                if (nameParts.length > 2) {
                    StringBuilder mnBuilder = new StringBuilder();
                    for(int i = 1; i < nameParts.length - 1; i++){
                        mnBuilder.append(nameParts[i]).append(" ");
                    }
                    middleName = mnBuilder.toString().trim();
                }
            }

            Guardian guardian = new Guardian(0, lastName, firstName, middleName, 
                    relationToStudentField.getText().trim(), guardianPhoneField.getText().trim());

            SchoolForm schoolForm = new SchoolForm(0, 
                    schoolNameComboBox.getSelectedItem().toString().trim(),
                    schoolIdComboBox.getSelectedItem().toString().trim(),
                    districtField.getText().trim(),
                    divisionField.getText().trim(),
                    schoolRegionField.getText().trim(),
                    semesterField.getText().trim(),
                    schoolYearComboBox.getSelectedItem().toString().trim(),
                    gradeLevelComboBox.getSelectedItem().toString().replace("Grade ", ""),
                    sectionField.getText().trim(),
                    trackStrandComboBox.getSelectedItem().toString().trim(),
                    courseComboBox.getSelectedItem().toString().trim());

            int age = 0;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException e) {
                // This should not happen if birthdate is selected, but as a fallback
                JOptionPane.showMessageDialog(this, "Invalid age value.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            String uiSexValue = sexComboBox.getSelectedItem().toString();
            String dbSexValue = uiSexValue.equals("Male") ? "M" : "F";

            Student student = new Student(0, 0, 0, 0, 0,
                    sectionField.getText().trim(),
                    lrnField.getText().trim(), lastNameField.getText().trim(), firstNameField.getText().trim(),
                    middleNameField.getText().trim(), dbSexValue,
                    java.sql.Date.valueOf(birthDatePicker.getSelectedDate()), motherTongueField.getText().trim(),
                    age, ipTypeField.getText().trim(), religionField.getText().trim(),
                    address, contact, parents, guardian, schoolForm);

            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
            boolean success = studentsDataDAO.createStudentWithRelations(student, address, contact, parents, guardian,
                    schoolForm);

            if (success) {
                // Publish event for student creation
                EventBus.publish("studentCreated", student);
                clearForm();
            }
            return success;
        } catch (SQLException e) {
            ErrorDialogUtils.showError(this, "Database error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            ErrorDialogUtils.showError(this, "An error occurred: " + e.getMessage());
            return false;
        }
    }
    
    private ToastOption createToastOption(){
        ToastOption toastOption = Toast.createOption();
        toastOption.getLayoutOption()
                .setMargin(0, 0, 10, 0)
                .setDirection(ToastDirection.TOP_TO_BOTTOM);
        return toastOption;
    }
    
    private void highlightFieldError(JTextField field, String message) {
        field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message);
        field.setBackground(new Color(255, 200, 200));
        field.requestFocus();
    }

    private void highlightFieldError(JFormattedTextField field, String message) {
        field.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message);
        field.setBackground(new Color(255, 200, 200));
        field.requestFocus();
    }

    private void highlightFieldError(JComboBox<?> comboBox, String message) {
        comboBox.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        comboBox.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, message);
        comboBox.setBackground(new Color(255, 200, 200));
        comboBox.requestFocus();
    }
    
    private void resetFieldStyles() {
        JTextField[] fields = {
            lrnField, firstNameField, lastNameField, middleNameField,
            motherTongueField, ipTypeField, religionField,
            houseNoField, streetField, regionField, provinceField, municipalityField, barangayField,
            zipCodeField, fatherLastNameField, fatherFirstNameField, fatherMiddleNameField,
            fatherPhoneNumberField, motherLastNameField, motherFirstNameField, motherMiddleNameField,
            motherPhoneNumberField, guardianNameField, relationToStudentField, guardianPhoneField,
            schoolNameField, schoolIdField, districtField, divisionField, schoolRegionField,
            semesterField, schoolYearField, sectionField, trackField, courseField
        };
        
        for (JTextField field : fields) {
            if (field != null) {
                field.putClientProperty(FlatClientProperties.STYLE, "");
                field.setToolTipText(null);
            }
        }
        birthDateEditor.putClientProperty(FlatClientProperties.STYLE, "");
        birthDateEditor.setToolTipText(null);
    }

    private void clearForm() {
        resetFieldStyles();
        
        lrnField.setText("");
        lastNameField.setText("");
        firstNameField.setText("");
        middleNameField.setText("");
        sexComboBox.setSelectedIndex(0);
        birthDatePicker.clearSelectedDate();
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
        schoolNameComboBox.setSelectedIndex(-1);
        schoolIdComboBox.setSelectedIndex(-1);
        districtField.setText("");
        divisionField.setText("");
        schoolRegionField.setText("");
        semesterField.setText("");
        schoolYearComboBox.setSelectedIndex(-1);
        gradeLevelComboBox.setSelectedIndex(0);
        sectionField.setText("");
        trackStrandComboBox.setSelectedIndex(-1);
        courseComboBox.setSelectedIndex(-1);
    }

    private void switchToImportSF() {
        ImportSF importSFForm = (ImportSF) AllForms.getForm(ImportSF.class, connection);
        FormManager.showForm(importSFForm);
    }
}
