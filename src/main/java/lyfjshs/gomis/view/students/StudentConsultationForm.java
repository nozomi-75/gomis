package lyfjshs.gomis.view.students;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.miginfocom.swing.MigLayout;
import com.formdev.flatlaf.FlatLightLaf;

public class StudentConsultationForm extends JFrame {
    private Map<String, Object> formData;
    private JTextField studentNameField, gradeField, schoolField, consultantNameField, witnessesField;
    private JTextField receivedField;
    private JTextArea descStudentConcernArea, summaryOfGuidancePlanArea, studentStatementArea;
    private JCheckBox frequentAbsencesBox, bullyingOrFightingBox, academicPerfBox, failedAssignmentsBox;
    private JCheckBox misbehaviorBox, schoolRuleViolationBox, disrespectfulBox, othersBox;
    private JSpinner dateTimeSpinner, dateConSpinner, followUpDateSpinner;
    private JSpinner dateStudentSpinner, dateGuardianSpinner, dateAdminSpinner;

    public static void main(String[] args) {
        try {
            // Set the FlatLaf look and feel
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new StudentConsultationForm().setVisible(true);
        });
    }

    public StudentConsultationForm() {
        // Initialize form data
        formData = new HashMap<>();

        // Set up the frame
        setTitle("Student Consultation Form");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Create the main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        // Add the form components
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Add the header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Add the main panel to the frame
        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(30, 144, 255));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel titleLabel = new JLabel("Students Consultation Form", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]10[]10[]"));

        // Add Basic Information Section
        panel.add(createBasicInfoSection(), "span, grow, wrap");

        // Add the main content with two columns
        JPanel mainContentPanel = new JPanel(new MigLayout("fillx", "[50%][50%]", "[]"));
        mainContentPanel.setBackground(Color.WHITE);

        // Left column
        JPanel leftPanel = new JPanel(new MigLayout("fillx", "[grow]", "[]10[]10[]"));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(createConsultationReasonsSection(), "span, grow, wrap");
        leftPanel.add(createStudentConcernSection(), "span, grow, wrap");
        leftPanel.add(createStudentStatementSection(), "span, grow, wrap");

        // Right column
        JPanel rightPanel = new JPanel(new MigLayout("fillx", "[grow]", "[]10[]10[]"));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(createGuidancePlanSection(), "span, grow, wrap");
        rightPanel.add(createFollowUpSection(), "span, grow, wrap");
        rightPanel.add(createSignaturesSection(), "span, grow, wrap");

        mainContentPanel.add(leftPanel, "grow");
        mainContentPanel.add(rightPanel, "grow");

        panel.add(mainContentPanel, "span, grow, wrap");

        // Add Form Actions
        panel.add(createActionButtonsPanel(), "span, grow, right");

        return panel;
    }

    private JPanel createBasicInfoSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);

        // Section Title
        JLabel titleLabel = new JLabel("Basic Information", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBackground(new Color(230, 230, 230));
        titleLabel.setOpaque(true);
        titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(titleLabel, "span, grow, wrap");

        // First row
        studentNameField = createTextField();
        gradeField = createTextField();
        schoolField = createTextField();
        consultantNameField = createTextField();

        panel.add(createLabeledComponent("Student Name:", studentNameField), "cell 0 1, growx");
        panel.add(createLabeledComponent("Grade Level/Section:", gradeField), "cell 1 1, growx");
        panel.add(createLabeledComponent("School:", schoolField), "cell 2 1, growx");
        panel.add(createLabeledComponent("Consultant Name:", consultantNameField), "cell 3 1, growx");

        // Second row
        SpinnerDateModel dateTimeModel = new SpinnerDateModel();
        dateTimeSpinner = new JSpinner(dateTimeModel);
        JSpinner.DateEditor dateTimeEditor = new JSpinner.DateEditor(dateTimeSpinner, "yyyy-MM-dd HH:mm");
        dateTimeSpinner.setEditor(dateTimeEditor);

        SpinnerDateModel dateConModel = new SpinnerDateModel();
        dateConSpinner = new JSpinner(dateConModel);
        JSpinner.DateEditor dateConEditor = new JSpinner.DateEditor(dateConSpinner, "yyyy-MM-dd");
        dateConSpinner.setEditor(dateConEditor);

        witnessesField = createTextField();

        panel.add(createLabeledComponent("Incident Date/Time:", dateTimeSpinner), "cell 0 2, growx");
        panel.add(createLabeledComponent("Date of Counseling:", dateConSpinner), "cell 1 2, growx");
        panel.add(createLabeledComponent("Names of Witnesses:", witnessesField), "cell 2 2, growx");

        return panel;
    }

    private JPanel createConsultationReasonsSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);

        // Section Title
        JLabel titleLabel = new JLabel("Reason for Consultation", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBackground(new Color(230, 230, 230));
        titleLabel.setOpaque(true);
        titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(titleLabel, "span, grow, wrap");

        // Checkboxes in a grid
        frequentAbsencesBox = new JCheckBox("Frequent Absences/Tardiness");
        bullyingOrFightingBox = new JCheckBox("Bullying/Fighting");
        academicPerfBox = new JCheckBox("Academic Performance");
        failedAssignmentsBox = new JCheckBox("Failed to Submit Assignments");
        misbehaviorBox = new JCheckBox("Misbehavior in Class");
        schoolRuleViolationBox = new JCheckBox("School Rule Violation");
        disrespectfulBox = new JCheckBox("Disrespectful Behavior");
        othersBox = new JCheckBox("Other");

        panel.add(frequentAbsencesBox, "cell 0 1");
        panel.add(misbehaviorBox, "cell 1 1");
        panel.add(academicPerfBox, "cell 0 2");
        panel.add(schoolRuleViolationBox, "cell 1 2");
        panel.add(failedAssignmentsBox, "cell 0 3");
        panel.add(othersBox, "cell 1 3");
        panel.add(disrespectfulBox, "cell 0 4");
        panel.add(bullyingOrFightingBox, "cell 1 4");

        return panel;
    }

    private JPanel createStudentConcernSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Description of Student Concern:");
        label.setFont(new Font("Arial", Font.BOLD, 12));

        descStudentConcernArea = new JTextArea();
        descStudentConcernArea.setRows(5);
        descStudentConcernArea.setLineWrap(true);
        descStudentConcernArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(descStudentConcernArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(label, "wrap");
        panel.add(scrollPane, "span, grow, h 100");

        return panel;
    }

    private JPanel createStudentStatementSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Student Statement:");
        label.setFont(new Font("Arial", Font.BOLD, 12));

        studentStatementArea = new JTextArea();
        studentStatementArea.setRows(5);
        studentStatementArea.setLineWrap(true);
        studentStatementArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(studentStatementArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(label, "wrap");
        panel.add(scrollPane, "span, grow, h 100");

        return panel;
    }

    private JPanel createGuidancePlanSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Summary of Guidance Plan or Action Taken:");
        label.setFont(new Font("Arial", Font.BOLD, 12));

        summaryOfGuidancePlanArea = new JTextArea();
        summaryOfGuidancePlanArea.setRows(5);
        summaryOfGuidancePlanArea.setLineWrap(true);
        summaryOfGuidancePlanArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(summaryOfGuidancePlanArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        panel.add(label, "wrap");
        panel.add(scrollPane, "span, grow, h 100");

        return panel;
    }

    private JPanel createFollowUpSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]"));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("Follow-up Date:");
        label.setFont(new Font("Arial", Font.BOLD, 12));

        SpinnerDateModel followUpModel = new SpinnerDateModel();
        followUpDateSpinner = new JSpinner(followUpModel);
        JSpinner.DateEditor followUpEditor = new JSpinner.DateEditor(followUpDateSpinner, "yyyy-MM-dd");
        followUpDateSpinner.setEditor(followUpEditor);

        panel.add(label, "wrap");
        panel.add(followUpDateSpinner, "span, grow");

        return panel;
    }

    private JPanel createSignaturesSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[grow]", "[]5[]5[]"));
        panel.setBackground(Color.WHITE);

        // Section Title
        JLabel titleLabel = new JLabel("Signatures", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBackground(new Color(230, 230, 230));
        titleLabel.setOpaque(true);
        titleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(titleLabel, "span, grow, wrap");

        // First row
        SpinnerDateModel dateStudentModel = new SpinnerDateModel();
        dateStudentSpinner = new JSpinner(dateStudentModel);
        JSpinner.DateEditor dateStudentEditor = new JSpinner.DateEditor(dateStudentSpinner, "yyyy-MM-dd");
        dateStudentSpinner.setEditor(dateStudentEditor);

        SpinnerDateModel dateConSignModel = new SpinnerDateModel();
        JSpinner dateConSignSpinner = new JSpinner(dateConSignModel);
        JSpinner.DateEditor dateConSignEditor = new JSpinner.DateEditor(dateConSignSpinner, "yyyy-MM-dd");
        dateConSignSpinner.setEditor(dateConSignEditor);

        panel.add(createLabeledComponent("Student Signature Date:", dateStudentSpinner), "cell 0 1, growx");
        panel.add(createLabeledComponent("Consultant Signature Date:", dateConSignSpinner), "cell 1 1, growx");

        // Second row
        SpinnerDateModel dateGuardianModel = new SpinnerDateModel();
        dateGuardianSpinner = new JSpinner(dateGuardianModel);
        JSpinner.DateEditor dateGuardianEditor = new JSpinner.DateEditor(dateGuardianSpinner, "yyyy-MM-dd");
        dateGuardianSpinner.setEditor(dateGuardianEditor);

        receivedField = createTextField();

        SpinnerDateModel dateAdminModel = new SpinnerDateModel();
        dateAdminSpinner = new JSpinner(dateAdminModel);
        JSpinner.DateEditor dateAdminEditor = new JSpinner.DateEditor(dateAdminSpinner, "yyyy-MM-dd");
        dateAdminSpinner.setEditor(dateAdminEditor);

        panel.add(createLabeledComponent("Parent/Guardian Date:", dateGuardianSpinner), "cell 0 2, growx");
        panel.add(createLabeledComponent("School Administrator:", receivedField), "cell 1 2, growx");
        panel.add(createLabeledComponent("Administrator Date:", dateAdminSpinner), "cell 2 2, growx");

        return panel;
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Color.WHITE);

        JButton resetButton = new JButton("Reset");
        resetButton.setBackground(new Color(200, 200, 200));
        resetButton.setForeground(Color.BLACK);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });

        JButton submitButton = new JButton("Submit");
        submitButton.setBackground(new Color(30, 144, 255));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitForm();
            }
        });

        panel.add(resetButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(submitButton);

        return panel;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return textField;
    }

    private JPanel createLabeledComponent(String labelText, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[]2[]"));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(label, "wrap");
        panel.add(component, "growx");

        return panel;
    }

    private void resetForm() {
        // Clear all text fields
        studentNameField.setText("");
        gradeField.setText("");
        schoolField.setText("");
        consultantNameField.setText("");
        witnessesField.setText("");
        receivedField.setText("");

        // Clear all text areas
        descStudentConcernArea.setText("");
        summaryOfGuidancePlanArea.setText("");
        studentStatementArea.setText("");

        // Uncheck all checkboxes
        frequentAbsencesBox.setSelected(false);
        bullyingOrFightingBox.setSelected(false);
        academicPerfBox.setSelected(false);
        failedAssignmentsBox.setSelected(false);
        misbehaviorBox.setSelected(false);
        schoolRuleViolationBox.setSelected(false);
        disrespectfulBox.setSelected(false);
        othersBox.setSelected(false);

        // Reset all date spinners to current date
        Date currentDate = new Date();
        dateTimeSpinner.setValue(currentDate);
        dateConSpinner.setValue(currentDate);
        followUpDateSpinner.setValue(currentDate);
        dateStudentSpinner.setValue(currentDate);
        dateGuardianSpinner.setValue(currentDate);
        dateAdminSpinner.setValue(currentDate);

        JOptionPane.showMessageDialog(this, "Form has been reset", "Reset", JOptionPane.INFORMATION_MESSAGE);
    }

    private void submitForm() {
        // Collect form data
        collectFormData();

        // Print to console (for testing)
        System.out.println("Form Data Submitted:");
        for (Map.Entry<String, Object> entry : formData.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // Show success message
        JOptionPane.showMessageDialog(this, "Form submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void collectFormData() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        formData.put("studentName", studentNameField.getText());
        formData.put("grade", gradeField.getText());
        formData.put("school", schoolField.getText());
        formData.put("conName", consultantNameField.getText());
        formData.put("dateTime", dateTimeFormat.format(dateTimeSpinner.getValue()));
        formData.put("dateCon", dateFormat.format(dateConSpinner.getValue()));
        formData.put("witnesses", witnessesField.getText());

        formData.put("frequentAbsences", frequentAbsencesBox.isSelected() ? "X" : "");
        formData.put("bullyingOrFighting", bullyingOrFightingBox.isSelected() ? "X" : "");
        formData.put("academicPerf", academicPerfBox.isSelected() ? "X" : "");
        formData.put("failedAssignments", failedAssignmentsBox.isSelected() ? "X" : "");
        formData.put("misbehavior", misbehaviorBox.isSelected() ? "X" : "");
        formData.put("schoolRuleViolation", schoolRuleViolationBox.isSelected() ? "X" : "");
        formData.put("disrespectful", disrespectfulBox.isSelected() ? "X" : "");
        formData.put("others", othersBox.isSelected() ? "X" : "");

        formData.put("descStudentConcern", descStudentConcernArea.getText());
        formData.put("summaryOfGuidancePlan", summaryOfGuidancePlanArea.getText());
        formData.put("studentStatement", studentStatementArea.getText());

        formData.put("followUpDate", dateFormat.format(followUpDateSpinner.getValue()));
        formData.put("dateStudent", dateFormat.format(dateStudentSpinner.getValue()));
        formData.put("dateGuardian", dateFormat.format(dateGuardianSpinner.getValue()));
        formData.put("received", receivedField.getText());
        formData.put("dateAdmin", dateFormat.format(dateAdminSpinner.getValue()));
    }
}
