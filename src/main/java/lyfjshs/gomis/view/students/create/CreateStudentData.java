package lyfjshs.gomis.view.students.create;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatClientProperties;

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
import lyfjshs.gomis.view.students.schoolForm.ImportSF;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.Toast;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class CreateStudentData extends Form {
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

    public CreateStudentData(Connection conn) {
        this.connection = conn;
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
        submitButton.addActionListener(e -> submitForm());
        
        buttonPanel.add(new JLabel(), "grow");
        buttonPanel.add(clearButton, "gapright 10");
        buttonPanel.add(submitButton);
        formContainer.add(buttonPanel, "span, growx");
        importSFbtn = new JButton("Import SF");
        importSFbtn.addActionListener(e -> switchToImportSF());
        formContainer.add(importSFbtn, "cell 0 0");

        // Initialize input validation
        initializeValidation();
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

        // Add focus listeners for visual feedback
        FocusAdapter focusAdapter = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JTextField field = (JTextField) e.getComponent();
                field.putClientProperty(FlatClientProperties.STYLE, "background:tint($Panel.background,10%)");
            }

            @Override
            public void focusLost(FocusEvent e) {
                JTextField field = (JTextField) e.getComponent();
                field.putClientProperty(FlatClientProperties.STYLE, "");
                validateField(field);
            }
        };

        // Apply focus listeners to required fields
        lrnField.addFocusListener(focusAdapter);
        firstNameField.addFocusListener(focusAdapter);
        lastNameField.addFocusListener(focusAdapter);
        guardianNameField.addFocusListener(focusAdapter);
        schoolNameField.addFocusListener(focusAdapter);
        schoolIdField.addFocusListener(focusAdapter);
        sectionField.addFocusListener(focusAdapter);
    }

    private void validateField(JTextField field) {
        String text = field.getText().trim();
        
        // Basic field validation (not to highlight errors yet, only warn for incorrect formats)
        if (field == lrnField) {
            if (!text.isEmpty() && !text.matches("\\d{12}")) {
                showFieldWarning(field, "LRN must be exactly 12 digits");
            } else {
                // Reset style
                field.putClientProperty(FlatClientProperties.STYLE, "");
                field.setToolTipText(null);
            }
        } else if (field == firstNameField || field == lastNameField) {
            if (!text.isEmpty() && !text.matches("[a-zA-Z\\s-']{2,50}")) {
                showFieldWarning(field, "Name must be 2-50 characters and contain only letters, spaces, hyphens, and apostrophes");
            } else {
                // Reset style
                field.putClientProperty(FlatClientProperties.STYLE, "");
                field.setToolTipText(null);
            }
        } else if (field == guardianPhoneField || field == fatherPhoneNumberField || field == motherPhoneNumberField) {
            if (!text.isEmpty() && !text.matches("\\d{11}")) {
                showFieldWarning(field, "Phone number must be 11 digits");
            } else {
                // Reset style
                field.putClientProperty(FlatClientProperties.STYLE, "");
                field.setToolTipText(null);
            }
        }
    }

    private void showFieldWarning(JTextField field, String message) {
        // Only set a tooltip with warning message, but don't change appearance
        field.setToolTipText(message);
    }

    private JPanel createStudentPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("Student Information");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][][][]"));
        contentPanel.setOpaque(false);

        // Row 0: Labels - Removed asterisks and red color
        contentPanel.add(new JLabel("LRN"), "cell 0 0");
        contentPanel.add(new JLabel("Sex"), "cell 1 0");
        contentPanel.add(new JLabel("Last Name"), "cell 2 0");
        contentPanel.add(new JLabel("First Name"), "cell 3 0");

        // Row 1: Fields
        lrnField = new JTextField();
        lrnField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter LRN");
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
        contentPanel.add(new JLabel("Birth Date"), "cell 1 2"); // Removed asterisk and red color
        contentPanel.add(new JLabel("Age"), "cell 2 2");
        contentPanel.add(new JLabel("Mother Tongue"), "cell 3 2");

        // Row 3: Fields
        middleNameField = new JTextField();
        middleNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter middle name");
        contentPanel.add(middleNameField, "cell 0 3, growx");

        birthDateEditor = new JFormattedTextField();
        birthDatePicker.setEditor(birthDateEditor);
        contentPanel.add(birthDateEditor, "cell 1 3, growx");

        ageField = new JTextField();
        ageField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Auto-calculated age");
        ageField.setEditable(false); // Age field should not be manually editable
        contentPanel.add(ageField, "cell 2 3, growx");

        motherTongueField = new JTextField();
        motherTongueField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter mother tongue");
        contentPanel.add(motherTongueField, "cell 3 3, growx");

        // Row 4: Labels
        contentPanel.add(new JLabel("IP (Ethnic Group)"), "cell 0 4");
        contentPanel.add(new JLabel("Religion"), "cell 1 4");

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

        JLabel sectionTitle = new JLabel("Residential Address");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow][grow]", "[][][][]"));
        contentPanel.setOpaque(false);

        // Row 0: Labels - Removed asterisks and red color
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

        JLabel sectionTitle = new JLabel("Parent's Information");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        // Create two sub-panels for father and mother information
        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow]", "[]"));
        contentPanel.setOpaque(false);

        // Father's Information Panel
        JPanel fatherPanel = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][]"));
        fatherPanel.putClientProperty(FlatClientProperties.STYLE, "arc:8; background:tint($Panel.background,5%)");
        fatherPanel.setOpaque(false);

        JLabel fatherTitle = new JLabel("Father's Information");
        fatherTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        fatherPanel.add(fatherTitle, "wrap, gapbottom 10");

        // Father's fields panel
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

        // Mother's fields panel
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

        // Add father and mother panels to the content panel
        contentPanel.add(fatherPanel, "grow");
        contentPanel.add(motherPanel, "grow");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
    }

    private JPanel createGuardianPanel() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fill, insets 20", "[grow]", "[][]"));
        sectionPanel.putClientProperty(FlatClientProperties.STYLE, "arc:10; background:tint($Panel.background,10%)");

        JLabel sectionTitle = new JLabel("Guardian's Information");
        sectionTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        sectionPanel.add(sectionTitle, "span, growx, gapbottom 15");

        JPanel contentPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][grow][grow]", "[][]"));
        contentPanel.setOpaque(false);

        // Row 0: Labels
        contentPanel.add(new JLabel("Guardian's Full Name"), "cell 0 0");
        contentPanel.add(new JLabel("Relation to Student"), "cell 1 0");
        contentPanel.add(new JLabel("Contact Number"), "cell 2 0");

        // Row 1: Fields
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
        contentPanel.add(new JLabel("School Name"), "cell 0 0");
        contentPanel.add(new JLabel("School ID"), "cell 1 0");
        contentPanel.add(new JLabel("Region"), "cell 2 0");
        contentPanel.add(new JLabel("Division"), "cell 3 0");

        // Row 1: Fields
        schoolNameField = new JTextField();
        schoolNameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter school name");
        contentPanel.add(schoolNameField, "cell 0 1, growx");

        schoolIdField = new JTextField();
        schoolIdField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter school ID");
        contentPanel.add(schoolIdField, "cell 1 1, growx");

        schoolRegionField = new JTextField();
        schoolRegionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter region (e.g., Region IV-A)");
        contentPanel.add(schoolRegionField, "cell 2 1, growx");

        divisionField = new JTextField();
        divisionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter division");
        contentPanel.add(divisionField, "cell 3 1, growx");

        // Row 2: Labels
        contentPanel.add(new JLabel("District"), "cell 0 2");
        contentPanel.add(new JLabel("Semester"), "cell 1 2");
        contentPanel.add(new JLabel("School Year"), "cell 2 2");
        contentPanel.add(new JLabel("Grade Level"), "cell 3 2");

        // Row 3: Fields
        districtField = new JTextField();
        districtField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter district");
        contentPanel.add(districtField, "cell 0 3, growx");

        semesterField = new JTextField();
        semesterField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter semester (e.g., 1st Semester)");
        contentPanel.add(semesterField, "cell 1 3, growx");

        schoolYearField = new JTextField();
        schoolYearField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. 2024-2025");
        contentPanel.add(schoolYearField, "cell 2 3, growx");

        gradeLevelComboBox = new JComboBox<>(new String[]{"Grade 10", "Grade 11", "Grade 12"});
        contentPanel.add(gradeLevelComboBox, "cell 3 3, growx");

        // Row 4: Labels
        contentPanel.add(new JLabel("Section"), "cell 0 4");
        contentPanel.add(new JLabel("Track & Strand"), "cell 1 4");
        contentPanel.add(new JLabel("Course"), "cell 2 4");

        // Row 5: Fields
        sectionField = new JTextField();
        sectionField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter section");
        contentPanel.add(sectionField, "cell 0 5, growx");

        trackField = new JTextField();
        trackField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter track and strand (e.g., STEM)");
        contentPanel.add(trackField, "cell 1 5, growx");

        courseField = new JTextField();
        courseField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter course");
        contentPanel.add(courseField, "cell 2 5, growx");

        sectionPanel.add(contentPanel, "span, growx");
        return sectionPanel;
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
            // Track missing required fields
            boolean hasErrors = false;
            StringBuilder missingFields = new StringBuilder("Please fill in the following required fields:\n");
            
            // Check required fields based on database schema (Table STUDENT and related tables)
            if (lrnField.getText().trim().isEmpty()) {
                highlightFieldError(lrnField, "LRN is required");
                missingFields.append("- LRN\n");
                hasErrors = true;
            }
            
            if (lastNameField.getText().trim().isEmpty()) {
                highlightFieldError(lastNameField, "Last Name is required");
                missingFields.append("- Last Name\n");
                hasErrors = true;
            }
            
            if (firstNameField.getText().trim().isEmpty()) {
                highlightFieldError(firstNameField, "First Name is required");
                missingFields.append("- First Name\n");
                hasErrors = true;
            }
            
            if (birthDatePicker.getSelectedDate() == null) {
                highlightFieldError(birthDateEditor, "Birth Date is required");
                missingFields.append("- Birth Date\n");
                hasErrors = true;
            }
            
            if (ageField.getText().trim().isEmpty()) {
                // Age is calculated automatically, so this might not happen
                missingFields.append("- Age\n");
                hasErrors = true;
            }
            
            // Required for SCHOOL_FORM table: SF_SECTION is marked as NOT NULL in the schema
            if (sectionField.getText().trim().isEmpty()) {
                highlightFieldError(sectionField, "Section is required");
                missingFields.append("- Section\n");
                hasErrors = true;
            }

            if (hasErrors) {
                // Create toast option for validation error
                ToastOption toastOption = Toast.createOption();
                toastOption.getLayoutOption()
                        .setMargin(0, 0, 10, 0)
                        .setDirection(ToastDirection.TOP_TO_BOTTOM);
                
                Toast.show(this, Toast.Type.WARNING, "Missing required fields", 
                        ToastLocation.BOTTOM_CENTER, toastOption);
                return;
            }

            // Reset field styling 
            resetFieldStyles();

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
                    semesterField.getText().trim(), schoolYearField.getText().trim(), 
                    gradeLevelComboBox.getSelectedItem().toString().replace("Grade ", ""),
                    sectionField.getText().trim(), trackField.getText().trim(), courseField.getText().trim());

            // Create Student object with proper age parsing
            int age = 0;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException e) {
                // Create toast option for number format error
                ToastOption toastOption = Toast.createOption();
                toastOption.getLayoutOption()
                        .setMargin(0, 0, 10, 0)
                        .setDirection(ToastDirection.TOP_TO_BOTTOM);
                
                Toast.show(this, Toast.Type.ERROR, "Invalid age value.", 
                        ToastLocation.BOTTOM_CENTER, toastOption);
                return;
            }

            // Convert UI sex value to database value (M/F)
            String uiSexValue = sexComboBox.getSelectedItem().toString();
            String dbSexValue = uiSexValue.equals("Male") ? "M" : "F";

            Student student = new Student(0, 0, 0, 0, 0, sectionField.getText().trim(), 
                    lrnField.getText().trim(), lastNameField.getText().trim(), firstNameField.getText().trim(),
                    middleNameField.getText().trim(), dbSexValue,
                    java.sql.Date.valueOf(birthDatePicker.getSelectedDate()), motherTongueField.getText().trim(),
                    age, ipTypeField.getText().trim(), religionField.getText().trim(),
                    address, contact, parents, guardian, schoolForm);

            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
            boolean success = studentsDataDAO.createStudentWithRelations(student, address, contact, parents, guardian,
                    schoolForm);

            if (success) {
                // Create toast option for success
                ToastOption toastOption = Toast.createOption();
                toastOption.getLayoutOption()
                        .setMargin(0, 0, 10, 0)
                        .setDirection(ToastDirection.TOP_TO_BOTTOM);
                
                Toast.show(this, Toast.Type.SUCCESS, "Student information successfully saved!", 
                        ToastLocation.BOTTOM_CENTER, toastOption);
                clearForm();
            } else {
                // Create toast option for failure
                ToastOption toastOption = Toast.createOption();
                toastOption.getLayoutOption()
                        .setMargin(0, 0, 10, 0)
                        .setDirection(ToastDirection.TOP_TO_BOTTOM);
                
                Toast.show(this, Toast.Type.ERROR, "Failed to save student information.", 
                        ToastLocation.BOTTOM_CENTER, toastOption);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Create toast option for SQL exception
            ToastOption toastOption = Toast.createOption();
            toastOption.getLayoutOption()
                    .setMargin(0, 0, 10, 0)
                    .setDirection(ToastDirection.TOP_TO_BOTTOM);
            
            Toast.show(this, Toast.Type.ERROR, "Database error: " + e.getMessage(), 
                    ToastLocation.BOTTOM_CENTER, toastOption);
        } catch (Exception e) {
            e.printStackTrace();
            // Create toast option for general exception
            ToastOption toastOption = Toast.createOption();
            toastOption.getLayoutOption()
                    .setMargin(0, 0, 10, 0)
                    .setDirection(ToastDirection.TOP_TO_BOTTOM);
            
            Toast.show(this, Toast.Type.ERROR, "An error occurred: " + e.getMessage(), 
                    ToastLocation.BOTTOM_CENTER, toastOption);
        }
    }
    
    // Method to highlight fields with errors
    private void highlightFieldError(JTextField field, String tooltipText) {
        field.putClientProperty(FlatClientProperties.STYLE, "background:#FFECEC; foreground:#FF5555");
        field.setToolTipText(tooltipText);
    }
    
    // Method to reset field styles
    private void resetFieldStyles() {
        // Reset style for each field
        JTextField[] fields = {
            lrnField, firstNameField, lastNameField, middleNameField, birthDateEditor,
            sectionField, motherTongueField, ipTypeField, religionField,
            houseNoField, streetField, regionField, provinceField, municipalityField, barangayField,
            zipCodeField, fatherLastNameField, fatherFirstNameField, fatherMiddleNameField,
            fatherPhoneNumberField, motherLastNameField, motherFirstNameField, motherMiddleNameField,
            motherPhoneNumberField, guardianNameField, relationToStudentField, guardianPhoneField,
            schoolNameField, schoolIdField, districtField, divisionField, schoolRegionField,
            semesterField, schoolYearField, trackField, courseField
        };
        
        for (JTextField field : fields) {
            if (field != null) {
                field.putClientProperty(FlatClientProperties.STYLE, "");
                field.setToolTipText(null);
            }
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
        gradeLevelComboBox.setSelectedIndex(0);
        sectionField.setText("");
        trackField.setText("");
        courseField.setText("");
    }

    private void switchToImportSF() {
        // Use AllForms to get the ImportSF form
        ImportSF importSFForm = (ImportSF) AllForms.getForm(ImportSF.class, connection);
        FormManager.showForm(importSFForm);
    }
}