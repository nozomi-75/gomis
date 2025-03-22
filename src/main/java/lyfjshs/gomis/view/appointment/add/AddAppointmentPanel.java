package lyfjshs.gomis.view.appointment.add;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Appointment;
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

    // Fields for appointment details
    private JTextField titleField;
    private JComboBox<String> typeComboBox;
    private JTextField statusField;
    private JTextArea notesArea;

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

        // Body Panel with MigLayout
        JPanel bodyPanel = new JPanel(new MigLayout("wrap 1", "[grow][grow]", "[][][][][][][][][][][][][][]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Title Field
        bodyPanel.add(new JLabel("Title:"), "cell 0 0,alignx label");
        titleField = new JTextField(appointment.getAppointmentTitle());
        bodyPanel.add(titleField, "cell 0 1 2 1,growx");

        // Date Field
        bodyPanel.add(new JLabel("Date:"), "flowx,cell 0 2,alignx left");

        // Time Field
        JLabel label = new JLabel("Time:");
        bodyPanel.add(label, "flowx,cell 1 2,alignx label");
        String[] consultationTypes = {
                "Academic Consultation",
                "Career Guidance",
                "Personal Consultation",
                "Behavioral Consultation",
                "Group Consultation"
        };

        // Appointment Type Dropdown
        JLabel lblConsultationType = new JLabel("Consultation Type:");
        bodyPanel.add(lblConsultationType, "cell 0 3 2 1,growx");
        typeComboBox = new JComboBox<>(consultationTypes);
        typeComboBox.setSelectedItem(appointment.getConsultationType());
        bodyPanel.add(typeComboBox, "cell 0 4 2 1,growx");

        // Guidance Counselor Details
        JLabel counselorLabel = new JLabel("Guidance Counselor:");
        bodyPanel.add(counselorLabel, "cell 0 5,alignx label");
        JTextField counselorDetailsField = new JTextField();
        counselorDetailsField.setEditable(false);

        // Retrieve counselor details from FormManager
        String counselorFullName = Main.formManager.getCounselorFullName();
        if (counselorFullName != null && !counselorFullName.isEmpty()) {
            counselorDetailsField.setText(counselorFullName);
        } else {
            counselorDetailsField.setText("No counselor assigned");
        }

        bodyPanel.add(counselorDetailsField, "cell 0 6 2 1,growx");

        // Status Field
        JLabel statusLabel = new JLabel("Status:");
        bodyPanel.add(statusLabel, "cell 0 7,alignx label");
        statusField = new JTextField(appointment.getAppointmentStatus());
        bodyPanel.add(statusField, "cell 0 8 2 1,growx");

        // Participants Section
        participantsPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]"));
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
        addParticipantPanel(participantsPanel); // Add initial participant
        bodyPanel.add(participantsPanel, "cell 0 9 2 1,grow");

        // Add Participant Button
        JButton addParticipantButton = new JButton("Add Another Participant");
        addParticipantButton.setBackground(new Color(33, 150, 243));
        addParticipantButton.setForeground(Color.WHITE);
        addParticipantButton.setFocusPainted(false);
        addParticipantButton.addActionListener(e -> addParticipantPanel(participantsPanel));
        bodyPanel.add(addParticipantButton, "cell 0 10 2 1,alignx center");

        // Notes Field
        JLabel label_2 = new JLabel("Notes:");
        bodyPanel.add(label_2, "cell 0 11,alignx label");
        notesArea = new JTextArea(appointment.getAppointmentNotes());
        notesArea.setRows(3);
        notesArea.setLineWrap(true);
        JScrollPane scrollPane_1 = new JScrollPane(notesArea);
        bodyPanel.add(scrollPane_1, "cell 0 12 2 2,growx");

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
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "growx");
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
        participantPanel.add(studentPanel, "cell 0 1 2 1,grow");
        participantPanel.add(nonStudentPanel, "cell 0 1 2 1,grow");
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
        panel.add(studentLrnField, "grow,wrap");

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
                List<Student> students = studentsDataDAO.getStudentsByFilters(null, firstName, lastName, sex);
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

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    private void extractAndSaveParticipants() {
        ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);

        for (Component comp : participantsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel participantPanel = (JPanel) comp;
                JPanel innerPanel = getVisibleParticipantPanel(participantPanel);

                if (innerPanel != null) {
                    Participants participant = createParticipantFromPanel(innerPanel);
                    try {
                        participantsDAO.createParticipant(participant); // Save to DB and set participantId
                        participantIds.add(participant.getParticipantId()); // Add to list
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

    private Participants createParticipantFromPanel(JPanel panel) {
        Participants participant = new Participants();
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                int nextIndex = panel.getComponentZOrder(comp) + 1;
                if (nextIndex < panel.getComponentCount()) {
                    Component nextComponent = panel.getComponent(nextIndex);
                    if (nextComponent instanceof JTextField) {
                        JTextField field = (JTextField) nextComponent;
                        String text = field.getText().trim();
                        switch (label.getText()) {
                            case "First Name:":
                                participant.setParticipantFirstName(text);
                                break;
                            case "Last Name:":
                                participant.setParticipantLastName(text);
                                break;
                            case "Contact Number:":
                                participant.setContactNumber(text);
                                break;
                            case "Email:":
                                participant.setEmail(text);
                                break;
                        }
                    } else if (nextComponent instanceof JComboBox) {
                        JComboBox<?> combo = (JComboBox<?>) nextComponent;
                        if (label.getText().equals("Type:")) {
                            participant.setParticipantType((String) combo.getSelectedItem());
                        }
                    }
                }
            }
        }
        return participant;
    }

    private JPanel getVisibleParticipantPanel(JPanel participantPanel) {
        for (Component comp : participantPanel.getComponents()) {
            if (comp instanceof JPanel && comp.isVisible()) {
                return (JPanel) comp;
            }
        }
        return null;
    }

    // Method to get participant IDs
    public List<Integer> getParticipantIds() {
        return participantIds;
    }

    public void validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Validate title
        if (titleField.getText().trim().isEmpty()) {
            errors.append("- Title is required.\n");
        }

        // Validate date
        if (datePicker.getSelectedDate() == null) {
            errors.append("- Date is required.\n");
        }

        // Validate time
        if (timePicker.getSelectedTime() == null) {
            errors.append("- Time is required.\n");
        }

        // Validate consultation type
        if (typeComboBox.getSelectedItem() == null || typeComboBox.getSelectedItem().toString().trim().isEmpty()) {
            errors.append("- Consultation type is required.\n");
        }

        // Validate participants
        if (participantCount == 0) {
            errors.append("- At least one participant is required.\n");
        } else {
            for (Component comp : participantsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel participantPanel = (JPanel) comp;
                    JPanel innerPanel = getVisibleParticipantPanel(participantPanel);

                    if (innerPanel != null) {
                        boolean isValid = validateParticipantPanel(innerPanel);
                        if (!isValid) {
                            errors.append("- Invalid participant details.\n");
                            break;
                        }
                    }
                }
            }
        }
        // notes are not required

        // Show errors if any
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please fix the following errors:\n" + errors.toString(),
                    "Validation Errors", JOptionPane.ERROR_MESSAGE);
            throw new IllegalArgumentException("Validation failed.");
        }
    }

    private boolean validateParticipantPanel(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JTextField) {
                JTextField field = (JTextField) comp;
                if (field.getText().trim().isEmpty()) {
                    return false; // Empty field found
                }
            }
        }
        return true; // All fields are valid
    }

    public boolean saveAppointment() {
        try {
            // Validate inputs
            validateInputs();

            // Extract participants and save them
            extractAndSaveParticipants();

            // Set appointment details
            appointment.setAppointmentTitle(titleField.getText().trim());
            appointment.setConsultationType((String) typeComboBox.getSelectedItem());
            appointment.setAppointmentDateTime(Timestamp.valueOf(
                    datePicker.getSelectedDate().atTime(timePicker.getSelectedTime())));
            appointment.setAppointmentStatus(statusField.getText().trim());
            appointment.setAppointmentNotes(notesArea.getText().trim());

            // Debugging: Print appointment details
            System.out.println("Appointment Details:");
            System.out.println("Title: " + appointment.getAppointmentTitle());
            System.out.println("Type: " + appointment.getConsultationType());
            System.out.println("Date/Time: " + appointment.getAppointmentDateTime());
            System.out.println("Status: " + appointment.getAppointmentStatus());
            System.out.println("Notes: " + appointment.getAppointmentNotes());
            System.out.println("Participants: " + participantIds);

            // Save the appointment using AppointmentDAO
            int generatedId = appointmentDao.insertAppointment(
                    appointment.getGuidanceCounselorId(),
                    appointment.getAppointmentTitle(),
                    appointment.getConsultationType(),
                    appointment.getAppointmentDateTime(),
                    appointment.getAppointmentNotes(),
                    appointment.getAppointmentStatus(),
                    participantIds);

            if (generatedId > 0) {
                appointment.setAppointmentId(generatedId);
                this.setConfirmed(true);
                return true; // Successfully saved
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save the appointment.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            // Validation errors are already shown in validateInputs()
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false; // Failed to save
    }
}
