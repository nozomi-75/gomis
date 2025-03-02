package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.model.StudentsData;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class ViolationFillUpForm extends Form {
    private JComboBox<String> participantTypeDropdown;
    private JTextField lrnField, FIRST_NAMEField, LAST_NAMEField, contactField;
    private JTextArea descriptionArea;
    private JButton addButton, searchStudentButton;
    private JComboBox<String> violationTypeDropdown;
    private JComboBox<String> reinforcementDropdown;
    private JComboBox<String> statusDropdown;
    private JComboBox<String> parentGuardianDropdown;
    private JTextField motherField, fatherField, guardianField;
    private ViolationCRUD violationCRUD;
    private Connection connect;

    public ViolationFillUpForm(Connection conn) {
        this.connect = conn;
        violationCRUD = new ViolationCRUD(connect);
        setLayout(new MigLayout("", "[grow]", "[][grow][pref]"));

        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
        JLabel lblTitle = new JLabel("VIOLATION MANAGEMENT");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(lblTitle, "grow");
        add(headerPanel, "cell 0 0, growx");

        // Main Input Panel
        JPanel inputPanel = new JPanel(new MigLayout("", "[][grow][][grow]", "[][][][][][][20.00px][grow][]"));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 177)),
            "VIOLATION DETAILS",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 12),
            new Color(100, 149, 177)));

        // Basic Information
        inputPanel.add(new JLabel("Participant Type:"), "cell 0 0");
        participantTypeDropdown = new JComboBox<>(new String[]{"Student", "Teacher", "Other"});
        inputPanel.add(participantTypeDropdown, "cell 1 0, growx");

        inputPanel.add(new JLabel("LRN:"), "cell 2 0");
        lrnField = new JTextField();
        inputPanel.add(lrnField, "cell 3 0,growx");

        // Add Search Student Button
        searchStudentButton = new JButton("Search Student");
        searchStudentButton.addActionListener(e -> searchStudent());
        inputPanel.add(searchStudentButton, "cell 3 0");

        inputPanel.add(new JLabel("First Name:"), "cell 0 1");
        FIRST_NAMEField = new JTextField();
        inputPanel.add(FIRST_NAMEField, "cell 1 1, growx");

        inputPanel.add(new JLabel("Last Name:"), "cell 2 1");
        LAST_NAMEField = new JTextField();
        inputPanel.add(LAST_NAMEField, "cell 3 1,growx");

        // Parent/Guardian Selection
        inputPanel.add(new JLabel("Select:"), "cell 0 2");
        parentGuardianDropdown = new JComboBox<>(new String[]{"Parent", "Guardian"});
        parentGuardianDropdown.addActionListener(e -> updateParentGuardianFields());
        inputPanel.add(parentGuardianDropdown, "cell 1 2, growx");

        // TextFields for Parent/Guardian Names
        inputPanel.add(new JLabel("Mother Name:"), "cell 0 3");
        motherField = new JTextField();
        inputPanel.add(motherField, "cell 1 3, growx");

        inputPanel.add(new JLabel("Father Name:"), "cell 2 3");
        fatherField = new JTextField();
        inputPanel.add(fatherField, "cell 3 3, growx");

        inputPanel.add(new JLabel("Guardian Name:"), "cell 0 4");
        guardianField = new JTextField();
        inputPanel.add(guardianField, "cell 1 4, growx");

        inputPanel.add(new JLabel("Contact:"), "cell 2 4");
        contactField = new JTextField();
        inputPanel.add(contactField, "cell 3 4,growx");

        // Violation Information
        inputPanel.add(new JLabel("Violation Type:"), "cell 0 5");
        violationTypeDropdown = new JComboBox<>(new String[]{
            "Minor", "Major", "Academic Dishonesty", "Bullying", "Dress Code",
            "Tardiness", "Cutting Classes", "Vandalism"
        });
        inputPanel.add(violationTypeDropdown, "cell 1 5, growx");

        inputPanel.add(new JLabel("Status:"), "cell 2 5");
        statusDropdown = new JComboBox<>(new String[]{
            "Pending", "Under Investigation", "Resolved"
        });
        inputPanel.add(statusDropdown, "cell 3 5,growx");

        inputPanel.add(new JLabel("Reinforcement:"), "cell 0 6");
        reinforcementDropdown = new JComboBox<>(new String[]{
            "Warning", "Counseling", "Parent Conference", "Suspension",
            "Community Service", "Written Warning", "Verbal Warning"
        });
        inputPanel.add(reinforcementDropdown, "cell 1 6,growx");

        // Description Area
        JLabel label = new JLabel("Description:");
        label.setFont(new Font("Tahoma", Font.BOLD, 13));
        inputPanel.add(label, "cell 0 7");
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 149, 177)),
            "Description",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", Font.BOLD, 14),
            new Color(0, 0, 0)));
        inputPanel.add(scrollPane, "cell 1 7 3 1,grow");

        add(inputPanel, "cell 0 1, grow");

        // Button Panel
        JPanel buttonPanel = new JPanel(new MigLayout("", "[center, grow]", "[]"));
        addButton = new JButton("Add");
        addButton.addActionListener(e -> addViolation());
        buttonPanel.add(addButton, "cell 0 0");
        add(buttonPanel, "cell 0 2");

        lrnField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || lrnField.getText().length() >= 12) {
                    e.consume();
                }
            }
        });

        contactField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || contactField.getText().length() >= 11) {
                    e.consume();
                }
            }
        });

        FIRST_NAMEField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && !Character.isSpaceChar(c)) {
                    e.consume();
                }
            }
        });

        LAST_NAMEField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && !Character.isSpaceChar(c)) {
                    e.consume();
                }
            }
        });

        // Add this after creating participantTypeDropdown
        participantTypeDropdown.addActionListener(e -> updateFields());
        updateParentGuardianFields();
    }

    private void updateParentGuardianFields() {
        String selected = (String) parentGuardianDropdown.getSelectedItem();
        boolean isParent = "Parent".equals(selected);
        boolean isGuardian = "Guardian".equals(selected);

        motherField.setEnabled(isParent);
        fatherField.setEnabled(isParent);
        guardianField.setEnabled(isGuardian);
    }

    private void searchStudent() {
        String lrn = lrnField.getText().trim();
        if (lrn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an LRN to search.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StudentsData student = fetchStudentByLrn(lrn);
        if (student != null) {
            FIRST_NAMEField.setText(student.getFirstName());
            LAST_NAMEField.setText(student.getLastName());
            contactField.setText(student.getContact().getCONTACT_NUMBER());
            if (student.getParents() != null) {
                Parents parents = student.getParents();
                motherField.setText(parents.getMotherFirstname() + " " + parents.getMotherLastname());
                fatherField.setText(parents.getFatherFirstname() + " " + parents.getFatherLastname());
                guardianField.setText("");
            }
        } else {
            JOptionPane.showMessageDialog(this, "No student found with the provided LRN.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private StudentsData fetchStudentByLrn(String lrn) {
        // Implement the logic to fetch student data from the database using the LRN
        // This is a placeholder for the actual implementation
        return null; // Replace with actual student data retrieval
    }

    private void updateFields() {
        String participantType = (String) participantTypeDropdown.getSelectedItem();
        boolean isStudent = "Student".equals(participantType);

        lrnField.setEnabled(isStudent);
        lrnField.setVisible(isStudent);
        lrnField.setText(isStudent ? lrnField.getText() : "");

        Component[] components = ((JPanel) lrnField.getParent()).getComponents();
        for (Component c : components) {
            if (c instanceof JLabel && ((JLabel) c).getText().equals("LRN:")) {
                c.setVisible(isStudent);
                break;
            }
        }
    }

    private void addViolation() {
        String participantType = (String) participantTypeDropdown.getSelectedItem();
        String lrn = lrnField.getText().trim();
        String FIRST_NAME = FIRST_NAMEField.getText().trim();
        String LAST_NAME = LAST_NAMEField.getText().trim();
        String contact = contactField.getText().trim();
        String violationType = (String) violationTypeDropdown.getSelectedItem();
        String reinforcement = (String) reinforcementDropdown.getSelectedItem();
        String status = (String) statusDropdown.getSelectedItem();
        String description = descriptionArea.getText().trim();

        if (FIRST_NAME.isEmpty() || LAST_NAME.isEmpty() || contact.isEmpty() ||
            violationType.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = violationCRUD.addViolation(
            Integer.parseInt(lrn),
            violationType,
            description,
            null,
            reinforcement,
            status,
            new Timestamp(System.currentTimeMillis())
        );

        if (success) {
            JOptionPane.showMessageDialog(this, "Violation added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            // Create new violation record
            Violation_Record violationRecord = new Violation_Record(connect);
            violationRecord.addViolationRecord(FIRST_NAME, LAST_NAME, contact, violationType, description);
        } else {
            JOptionPane.showMessageDialog(this, "Error adding violation to database", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        lrnField.setText("");
        FIRST_NAMEField.setText("");
        LAST_NAMEField.setText("");
        contactField.setText("");
        descriptionArea.setText("");
        motherField.setText("");
        fatherField.setText("");
        guardianField.setText("");
    }
}
