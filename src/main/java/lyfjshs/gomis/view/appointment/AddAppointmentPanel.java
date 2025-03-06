package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ContactDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class AddAppointmentPanel extends JPanel {
    private JPanel participantsPanel;
    private int participantCount;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private boolean confirmed;
    private Appointment appointment;
    private AppointmentDAO appointmentDao;
    private Connection connection;
    private List<Integer> participantIds; // To store participant IDs

    // Fields for student panel
    private JTextField studentLrnField;
    private JTextField studentFirstNameField;
    private JTextField studentLastNameField;
    private JComboBox<String> studentSexComboBox;

    // Fields for non-student panel
    private JTextField nonStudentFirstNameField;
    private JTextField nonStudentLastNameField;
    private JTextField nonStudentContactField;
    private JTextField nonStudentEmailField;

    public AddAppointmentPanel(Appointment appointment, AppointmentDAO appointmentDao, Connection connection) {
        this.appointment = appointment;
        this.appointmentDao = appointmentDao;
        this.connection = connection;
        this.participantIds = new ArrayList<>(); // Initialize participant IDs list
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow]"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
    }

    private void initializeComponents() {
        participantCount = 0;
        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        JLabel titleLabel = new JLabel("Add New Appointment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        closeButton.setPreferredSize(new java.awt.Dimension(30, 30));
        closeButton.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        headerPanel.add(titleLabel, "align center");
        headerPanel.add(closeButton, "align right");
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        add(headerPanel, "north");

        // Body Panel with MigLayout
        JPanel bodyPanel = new JPanel(new MigLayout("wrap 1", "[grow][grow]", "[][][][][][][][][][][][][][]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Title Field
        bodyPanel.add(new JLabel("Title:"), "cell 0 0,alignx label");
        JTextField titleField = new JTextField(appointment.getAppointmentTitle());
        bodyPanel.add(titleField, "cell 0 1 2 1,growx");

        // Date Field
        bodyPanel.add(new JLabel("Date:"), "flowx,cell 0 2,alignx left");

        // Time Field
        JLabel label = new JLabel("Time:");
        bodyPanel.add(label, "flowx,cell 1 2,alignx label");
        String[] appointmentTypes = {
                "Academic Consultation",
                "Career Guidance",
                "Personal Consultation",
                "Behavioral Consultation",
                "Group Consultation"
        };

        // Appointment Type Dropdown
        JLabel label_1 = new JLabel("Appointment Type:");
        bodyPanel.add(label_1, "cell 0 3 2 1,growx");
        JComboBox<String> typeComboBox = new JComboBox<>(appointmentTypes);
        typeComboBox.setSelectedItem(appointment.getAppointmentType());
        bodyPanel.add(typeComboBox, "cell 0 4 2 1,growx");

        // Participants Section
        participantsPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]"));
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
        addParticipantPanel(participantsPanel); // Add initial participant
        bodyPanel.add(participantsPanel, "cell 0 5 2 1,grow");

        // Add Participant Button
        JButton addParticipantButton = new JButton("Add Another Participant");
        addParticipantButton.setBackground(new Color(33, 150, 243));
        addParticipantButton.setForeground(Color.WHITE);
        addParticipantButton.setFocusPainted(false);
        addParticipantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addParticipantPanel(participantsPanel);
            }
        });
        bodyPanel.add(addParticipantButton, "cell 0 6 2 1,alignx center");

        // Notes Field
        JLabel label_2 = new JLabel("Notes:");
        bodyPanel.add(label_2, "cell 0 7,alignx label");

        // Form Actions
        JPanel actionsPanel = new JPanel(new MigLayout("insets 0", "[grow, right]"));
        JTextArea notesArea = new JTextArea(appointment.getAppointmentNotes());
        notesArea.setRows(3);
        notesArea.setLineWrap(true);
        JScrollPane scrollPane_1 = new JScrollPane(notesArea);
        bodyPanel.add(scrollPane_1, "cell 0 8 2 1,growx");
        JButton cancelButton = new JButton("Cancel");
        bodyPanel.add(cancelButton, "flowx,cell 1 9");
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        bodyPanel.add(actionsPanel, "cell 0 12,alignx right");

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        datePicker = new DatePicker();
        JFormattedTextField dateField = new JFormattedTextField();
        datePicker.setEditor(dateField);

        // Set current date if appointment date is null
        if (appointment.getAppointmentDateTime() != null) {
            datePicker.setSelectedDate(appointment.getAppointmentDateTime().toLocalDateTime().toLocalDate());
        } else {
            datePicker.setSelectedDate(java.time.LocalDate.now());
        }

        bodyPanel.add(dateField, "cell 0 2,growx");
        timePicker = new TimePicker();
        JFormattedTextField timeField = new JFormattedTextField();
        timePicker.setEditor(timeField);

        // Set current time if appointment time is null
        if (appointment.getAppointmentDateTime() != null) {
            timePicker.setSelectedTime(appointment.getAppointmentDateTime().toLocalDateTime().toLocalTime());
        } else {
            timePicker.setSelectedTime(java.time.LocalTime.now());
        }

        bodyPanel.add(timeField, "cell 1 2,growx");
        JButton submitButton = new JButton("Add Appointment");
        bodyPanel.add(submitButton, "cell 1 9");
        submitButton.setBackground(new Color(76, 175, 80));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(e -> {
            if (validateInputs(titleField, datePicker, timePicker)) {
                appointment.setAppointmentTitle(titleField.getText());
                appointment.setAppointmentType((String) typeComboBox.getSelectedItem());
                appointment.setAppointmentDateTime(java.sql.Timestamp
                        .valueOf(LocalDateTime.of(datePicker.getSelectedDate(), timePicker.getSelectedTime())));
                appointment.setAppointmentNotes(notesArea.getText());
                extractAndSaveParticipants(); // Save participants and set IDs
                setConfirmed(true);
                SwingUtilities.getWindowAncestor(this).dispose();
            }
        });
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "growx");
    }

    private boolean validateInputs(JTextField titleField, DatePicker datePicker, TimePicker timePicker) {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!datePicker.isDateSelected()) {
            JOptionPane.showMessageDialog(this, "Date is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!timePicker.isTimeSelected()) {
            JOptionPane.showMessageDialog(this, "Time is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // Validate participants
        if (participantCount > 0 && participantIds.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "At least one participant must have valid data (first name and last name are required).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void addParticipantPanel(JPanel parentPanel) {
        JPanel participantPanel = new JPanel(new MigLayout("hidemode 3", "[right][grow]", "[][]"));
        participantPanel.setBorder(BorderFactory.createEtchedBorder());

        // Participant Type
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[] { "Student", "Non-Student" });
        participantPanel.add(typeLabel);
        participantPanel.add(typeComboBox, "wrap");

        // Create panels for both types
        JPanel studentPanel = createStudentPanel();
        JPanel nonStudentPanel = createNonStudentPanel();

        // Add both panels to the same cell but initially show only student panel
        participantPanel.add(studentPanel, "cell 0 1 2 1, growx");
        participantPanel.add(nonStudentPanel, "cell 0 1 2 1, growx");
        studentPanel.setVisible(true);
        nonStudentPanel.setVisible(false);

        // Remove Button in its own row
        JButton removeButton = new JButton("Remove Participant");
        removeButton.setBackground(new Color(244, 67, 54));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(e -> {
            parentPanel.remove(participantPanel);
            parentPanel.revalidate();
            parentPanel.repaint();
            participantCount--;
        });
        participantPanel.add(removeButton, "cell 0 2 2 1, alignx center");

        typeComboBox.addActionListener(e -> {
            boolean isStudent = typeComboBox.getSelectedItem().equals("Student");
            studentPanel.setVisible(isStudent);
            nonStudentPanel.setVisible(!isStudent);

            // Ensure proper revalidation and animation
            FlatAnimatedLafChange.showSnapshot();
            participantPanel.revalidate();
            participantPanel.repaint();
            FlatLaf.updateUI();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });

        parentPanel.add(participantPanel, "growx, wrap");
        parentPanel.revalidate();
        parentPanel.repaint();
        participantCount++;
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow]", "[]"));

        // LRN Field
        JLabel lrnLabel = new JLabel("LRN:");
        studentLrnField = new JTextField();
        panel.add(lrnLabel);
        panel.add(studentLrnField, "growx, wrap");

        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        studentFirstNameField = new JTextField();
        panel.add(firstNameLabel);
        panel.add(studentFirstNameField, "growx, wrap");

        // Last Name
        JLabel lastNameLabel = new JLabel("Last Name:");
        studentLastNameField = new JTextField();
        panel.add(lastNameLabel);
        panel.add(studentLastNameField, "growx, wrap");

        // Sex
        JLabel sexLabel = new JLabel("Sex:");
        studentSexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
        panel.add(sexLabel);
        panel.add(studentSexComboBox, "growx, wrap");

        // Search buttons
        JPanel searchPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
        JButton searchLrnButton = new JButton("Search by LRN");
        JButton searchNameButton = new JButton("Search by Name");

        searchLrnButton
                .addActionListener(e -> searchStudent(studentLrnField.getText(), studentFirstNameField,
                        studentLastNameField, studentSexComboBox));
        searchNameButton.addActionListener(
                e -> searchStudent("", studentFirstNameField, studentLastNameField, studentSexComboBox));

        searchPanel.add(searchLrnButton, "growx");
        searchPanel.add(searchNameButton, "growx");
        panel.add(searchPanel, "span 2, growx");

        return panel;
    }

    private JPanel createNonStudentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow]", "[]"));

        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        nonStudentFirstNameField = new JTextField();
        panel.add(firstNameLabel);
        panel.add(nonStudentFirstNameField, "growx, wrap");

        // Last Name
        JLabel lastNameLabel = new JLabel("Last Name:");
        nonStudentLastNameField = new JTextField();
        panel.add(lastNameLabel);
        panel.add(nonStudentLastNameField, "growx, wrap");

        // Contact Number
        JLabel contactLabel = new JLabel("Contact Number:");
        nonStudentContactField = new JTextField();
        panel.add(contactLabel);
        panel.add(nonStudentContactField, "growx, wrap");

        // Email
        JLabel emailLabel = new JLabel("Email:");
        nonStudentEmailField = new JTextField();
        panel.add(emailLabel);
        panel.add(nonStudentEmailField, "growx");

        return panel;
    }

    private void searchStudent(String lrn, JTextField firstNameField, JTextField lastNameField,
            JComboBox<String> sexComboBox) {
        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
        Student student = null;

        // First attempt: Search by LRN if provided
        if (!lrn.trim().isEmpty()) {
            try {
                student = studentsDataDAO.getStudentDataByLrn(lrn);
                if (student != null) {
                    firstNameField.setText(student.getStudentFirstname());
                    lastNameField.setText(student.getStudentLastname());
                    sexComboBox.setSelectedItem(student.getStudentSex());
                    return; // Exit if found by LRN
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error searching by LRN: " + ex.getMessage(),
                        "Search Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }

        // Fallback: Search by First Name, Last Name, and Sex if LRN is not available or
        // no match
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String sex = (String) sexComboBox.getSelectedItem();

        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            try {
                List<Student> students = studentsDataDAO.getStudentDataByNameAndSex(firstName, "", lastName, sex);
                if (!students.isEmpty()) {
                    student = students.get(0); // Take the first match
                    firstNameField.setText(student.getStudentFirstname());
                    lastNameField.setText(student.getStudentLastname());
                    sexComboBox.setSelectedItem(student.getStudentSex());
                    JOptionPane.showMessageDialog(this, "Student found using name and sex.", "Search Result",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No student found with the provided name and sex.",
                            "Search Result", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error searching by name and sex: " + ex.getMessage(),
                        "Search Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else if (lrn.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter LRN or both first name and last name to search.",
                    "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudentByNameAndSex(String firstName, String lastName, String sex, JTextField lrnField) {
        if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both first and last name",
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
        try {
            List<Student> students = studentsDataDAO.getStudentDataByNameAndSex(firstName, "", lastName, sex);
            if (!students.isEmpty()) {
                Student student = students.get(0);
                lrnField.setText(student.getStudentLrn());
            } else {
                JOptionPane.showMessageDialog(this, "Student not found",
                        "Search Result", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching student",
                    "Search Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudent(String firstName, String lastName, JTextField contactField) {
        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
        ContactDAO contactDAO = new ContactDAO(connection);
        try {
            Student student = studentsDataDAO.getStudentByFirstNLastName(firstName, lastName);
            if (student != null) {
                Contact contact = contactDAO.getContactByStudentId(student.getStudentUid());
                if (contact != null) {
                    contactField.setText(contact.getContactNumber());
                } else {
                    JOptionPane.showMessageDialog(this, "Contact not found for the student.", "Search Result",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Student not found.", "Search Result",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error occurred while searching for the student.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    // Method to extract and save participants
    private void extractAndSaveParticipants() {
        ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
        participantIds.clear(); // Clear existing IDs
        boolean hasValidParticipant = false;
        for (int i = 0; i < participantsPanel.getComponentCount(); i++) {
            if (participantsPanel.getComponent(i) instanceof JPanel) {
                JPanel participantPanel = (JPanel) participantsPanel.getComponent(i);
                for (int j = 0; j < participantPanel.getComponentCount(); j++) {
                    if (participantPanel.getComponent(j) instanceof JPanel) {
                        JPanel innerPanel = (JPanel) participantPanel.getComponent(j);
                        if (innerPanel.isVisible()) {
                            Participants participant = createParticipantFromPanel(innerPanel);
                            if (isParticipantValid(participant, innerPanel)) {
                                try {
                                    participantsDAO.createParticipant(participant); // Save to DB and set participantId
                                    participantIds.add(participant.getParticipantId()); // Add to list
                                    hasValidParticipant = true;
                                } catch (SQLException e) {
                                    JOptionPane.showMessageDialog(this, "Error saving participant: " + e.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                    e.printStackTrace();
                                    return; // Stop processing on error
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!hasValidParticipant && participantCount > 0) {
            JOptionPane.showMessageDialog(this,
                    "At least one participant must have valid data (first name and last name are required).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            participantIds.clear(); // Clear invalid participant IDs
        }
    }

    // Helper method to validate participant data with panel context
    private boolean isParticipantValid(Participants participant, JPanel panel) {
        boolean isValid = false;

        // Check the type of participant and validate accordingly
        if (participant.getParticipantType().equals("Student")) {
            // Validate student fields
            JTextField firstNameField = null;
            JTextField lastNameField = null;

            for (Component comp : panel.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if ("First Name:".equals(label.getText())) {
                        for (int k = 0; k < panel.getComponentCount(); k++) {
                            if (panel.getComponent(k) == comp && k + 1 < panel.getComponentCount()) {
                                lastNameField = (JTextField) panel.getComponent(k + 1);
                                break;
                            }
                        }
                    } else if ("Last Name:".equals(label.getText())) {
                        for (int k = 0; k < panel.getComponentCount(); k++) {
                            if (panel.getComponent(k) == comp && k + 1 < panel.getComponentCount()) {
                                lastNameField = (JTextField) panel.getComponent(k + 1);
                                break;
                            }
                        }
                    }
                }
            }

            if (firstNameField != null && lastNameField != null) {
                System.out.println("Validating Student: " + firstNameField.getText() + " " + lastNameField.getText());
                isValid = !firstNameField.getText().trim().isEmpty() && !lastNameField.getText().trim().isEmpty();
            }
        } else if (participant.getParticipantType().equals("Non-Student")) {
            // Validate non-student fields
            JTextField firstNameField = null;
            JTextField lastNameField = null;

            for (Component comp : panel.getComponents()) {
                if (comp instanceof JLabel) {
                    JLabel label = (JLabel) comp;
                    if ("First Name:".equals(label.getText())) {
                        for (int k = 0; k < panel.getComponentCount(); k++) {
                            if (panel.getComponent(k) == comp && k + 1 < panel.getComponentCount()) {
                                firstNameField = (JTextField) panel.getComponent(k + 1);
                                break;
                            }
                        }
                    } else if ("Last Name:".equals(label.getText())) {
                        for (int k = 0; k < panel.getComponentCount(); k++) {
                            if (panel.getComponent(k) == comp && k + 1 < panel.getComponentCount()) {
                                lastNameField = (JTextField) panel.getComponent(k + 1);
                                break;
                            }
                        }
                    }
                }
            }

            if (firstNameField != null && lastNameField != null) {
                System.out
                        .println("Validating Non-Student: " + firstNameField.getText() + " " + lastNameField.getText());
                isValid = !firstNameField.getText().trim().isEmpty() && !lastNameField.getText().trim().isEmpty();
            }
        }

        if (!isValid) {
            JOptionPane.showMessageDialog(this,
                    "Participant data is incomplete (first name and last name are required).",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        } else {
            panel.setBorder(BorderFactory.createEtchedBorder()); // Reset border if valid
        }
        return isValid;
    }

    // Helper method to create a Participant from a panel
    private Participants createParticipantFromPanel(JPanel panel) {
        Participants participant = new Participants();
        for (int i = 0; i < panel.getComponentCount(); i++) {
            if (panel.getComponent(i) instanceof JLabel) {
                JLabel label = (JLabel) panel.getComponent(i);
                int nextIndex = i + 1;
                if (nextIndex < panel.getComponentCount()) {
                    Component nextComponent = panel.getComponent(nextIndex);
                    if (nextComponent instanceof JTextField) {
                        JTextField field = (JTextField) nextComponent;
                        String text = field.getText().trim();
                        if (label.getText().equals("First Name:")) {
                            participant.setParticipantFirstName(text);
                        } else if (label.getText().equals("Last Name:")) {
                            participant.setParticipantLastName(text);
                        } else if (label.getText().equals("Contact Number:")) {
                            participant.setContactNumber(text);
                        } else if (label.getText().equals("Email:")) {
                            participant.setEmail(text);
                        }
                        i = nextIndex; // Skip the field
                    } else if (nextComponent instanceof JComboBox) {
                        JComboBox<?> combo = (JComboBox<?>) nextComponent;
                        if (label.getText().equals("Type:")) {
                            participant.setParticipantType((String) combo.getSelectedItem());
                        }
                        i = nextIndex; // Skip the combo box
                    }
                }
            }
        }
        return participant;
    }

    // Method to get participant IDs
    public List<Integer> getParticipantIds() {
        return participantIds;
    }
}