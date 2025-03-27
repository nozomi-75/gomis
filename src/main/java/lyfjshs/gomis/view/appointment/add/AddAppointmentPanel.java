package lyfjshs.gomis.view.appointment.add;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
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
    private JComboBox<String> nonStudentSexComboBox;

    // Fields for appointment details
    private JTextField titleField;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> statusComboBox; // Changed from JTextField to JComboBox
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
                "~Please Select~",
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
        typeComboBox.setSelectedItem("~Please Select~"); // Set default value
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

        // Status Field (replace the existing status field code with this)
        JLabel statusLabel = new JLabel("Status:");
        bodyPanel.add(statusLabel, "cell 0 7,alignx label");
        String[] statusOptions = { "~Please Select~", "On-going", "Rescheduled" };
        statusComboBox = new JComboBox<>(statusOptions);
        statusComboBox.setSelectedItem("~Please Select~"); // Set default value
        bodyPanel.add(statusComboBox, "cell 0 8 2 1,growx");

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
        
        // Initialize DatePicker
        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        
        JFormattedTextField dateField = new JFormattedTextField();
        datePicker.setEditor(dateField);

        // Set current date if appointment date is null
        if (appointment.getAppointmentDateTime() != null) {
            LocalDate appointmentDate = appointment.getAppointmentDateTime().toLocalDateTime().toLocalDate();
            // Only set the date if it's not in the past
            if (!appointmentDate.isBefore(LocalDate.now())) {
                datePicker.setSelectedDate(appointmentDate);
            } else {
                datePicker.setSelectedDate(LocalDate.now());
            }
        } else {
            datePicker.setSelectedDate(LocalDate.now());
        }

        // Add listener to prevent selecting past dates
        datePicker.addDateSelectionListener(e -> {
            if (datePicker.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
                LocalDate selectedDate = datePicker.getSelectedDate();
                if (selectedDate != null && selectedDate.isBefore(LocalDate.now())) {
                    datePicker.setSelectedDate(LocalDate.now());
                    JOptionPane.showMessageDialog(this,
                        "Cannot select a date in the past. Date has been reset to today.",
                        "Invalid Date",
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        bodyPanel.add(dateField, "cell 0 2,growx");
        
        // Initialize TimePicker
        timePicker = new TimePicker();
        JFormattedTextField timeField = new JFormattedTextField();
        timePicker.setEditor(timeField);

        // Set current time if appointment time is null
        if (appointment.getAppointmentDateTime() != null) {
            LocalDateTime appointmentDateTime = appointment.getAppointmentDateTime().toLocalDateTime();
            if (!appointmentDateTime.isBefore(LocalDateTime.now())) {
                timePicker.setSelectedTime(appointmentDateTime.toLocalTime());
            } else {
                timePicker.setSelectedTime(LocalDateTime.now().toLocalTime());
            }
        } else {
            timePicker.setSelectedTime(LocalDateTime.now().toLocalTime());
        }

        // Add listener to TimePicker to validate selected time
        timePicker.addTimeSelectionListener(e -> {
            if (datePicker.getSelectedDate() != null && 
                datePicker.getSelectedDate().equals(LocalDate.now()) && 
                timePicker.getSelectedTime() != null && 
                timePicker.getSelectedTime().isBefore(LocalDateTime.now().toLocalTime())) {
                // If selected time is in the past, reset to current time
                timePicker.setSelectedTime(LocalDateTime.now().toLocalTime());
                JOptionPane.showMessageDialog(this,
                    "Cannot select a time in the past. Time has been reset to current time.",
                    "Invalid Time",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        bodyPanel.add(timeField, "cell 1 2,growx");
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "growx");
    }

    private void addParticipantPanel(JPanel parentPanel) {
        JPanel participantPanel = new JPanel(new MigLayout("hidemode 3", "[right][grow]", "[][]"));
        participantPanel.setBorder(BorderFactory.createEtchedBorder());

        // Participant Type
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[] {
                "SELECT TYPE", // Default value
                "Student",
                "Non-Student"
        });
        participantPanel.add(typeLabel);
        participantPanel.add(typeComboBox, "wrap");

        // Create panels for both types
        JPanel studentPanel = createStudentPanel();
        JPanel nonStudentPanel = createNonStudentPanel();

        // Initially hide both panels since SELECT TYPE is default
        studentPanel.setVisible(false);
        nonStudentPanel.setVisible(false);

        // Add both panels to the same cell but initially show only student panel
        participantPanel.add(studentPanel, "cell 0 1 2 1,grow");
        participantPanel.add(nonStudentPanel, "cell 0 1 2 1,grow");

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

        // Add ActionListener to handle type selection
        typeComboBox.addActionListener(e -> {
            String selected = (String) typeComboBox.getSelectedItem();
            if ("SELECT TYPE".equals(selected)) {
                studentPanel.setVisible(false);
                nonStudentPanel.setVisible(false);
            } else {
                boolean isStudent = "Student".equals(selected);
                studentPanel.setVisible(isStudent);
                nonStudentPanel.setVisible(!isStudent);
            }

            // Ensure proper revalidation and animation
            participantPanel.revalidate();
            participantPanel.repaint();
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

        searchLrnButton.addActionListener(e -> searchStudentByLrn(studentLrnField.getText()));
        searchNameButton.addActionListener(e -> searchStudentByNameAndSex(
                studentFirstNameField.getText(),
                studentLastNameField.getText(),
                (String) studentSexComboBox.getSelectedItem()));

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

        // sex
        JLabel sexLabel = new JLabel("Sex:");
        nonStudentSexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
        panel.add(sexLabel);
        panel.add(nonStudentSexComboBox, "growx");

        return panel;
    }

    private void searchStudentByNameAndSex(String firstName, String lastName, String sex) {
        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
        try {
            if (!firstName.trim().isEmpty() && !lastName.trim().isEmpty()) {
                List<Student> students = studentsDataDAO.getStudentsByFilters(null, firstName, lastName, sex);
                if (!students.isEmpty()) {
                    Student student = students.get(0);
                    
                    // Create participant from student
                    Participants participant = new Participants();
                    participant.setStudentUid(student.getStudentUid());
                    participant.setParticipantType("Student");
                    participant.setParticipantFirstName(student.getStudentFirstname());
                    participant.setParticipantLastName(student.getStudentLastname());
                    participant.setSex(student.getStudentSex());
                    participant.setContactNumber(student.getContact() != null ? 
                        student.getContact().getContactNumber() : null);

                    // Save participant to database
                    ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
                    int participantId = participantsDAO.createParticipant(participant);
                    participant.setParticipantId(participantId);
                    
                    // Add to participants list
                    participantIds.add(participantId);

                    // Populate fields
                    studentFirstNameField.setText(student.getStudentFirstname());
                    studentLastNameField.setText(student.getStudentLastname());
                    studentSexComboBox.setSelectedItem(student.getStudentSex());
                    studentLrnField.setText(student.getStudentLrn());
                    
                    JOptionPane.showMessageDialog(this, "Student found and added as participant!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No student found with the provided details.",
                            "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching student: " + ex.getMessage(),
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void searchStudentByLrn(String lrn) {
        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
        try {
            if (!lrn.trim().isEmpty()) {
                Student student = studentsDataDAO.getStudentDataByLrn(lrn);
                if (student != null) {
                    // Create participant from student
                    Participants participant = new Participants();
                    participant.setStudentUid(student.getStudentUid());
                    participant.setParticipantType("Student");
                    participant.setParticipantFirstName(student.getStudentFirstname());
                    participant.setParticipantLastName(student.getStudentLastname());
                    participant.setSex(student.getStudentSex());
                    participant.setContactNumber(student.getContact() != null ? 
                        student.getContact().getContactNumber() : null);

                    // Save participant to database
                    ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
                    int participantId = participantsDAO.createParticipant(participant);
                    participant.setParticipantId(participantId);
                    
                    // Add to participants list
                    participantIds.add(participantId);

                    // Populate fields
                    studentFirstNameField.setText(student.getStudentFirstname());
                    studentLastNameField.setText(student.getStudentLastname());
                    studentSexComboBox.setSelectedItem(student.getStudentSex());
                    
                    JOptionPane.showMessageDialog(this, "Student found and added as participant!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No student found with the provided LRN.",
                            "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error searching by LRN: " + ex.getMessage(),
                    "Search Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
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
        // Clear previous list to avoid duplicates
        participantIds.clear();
        
        for (Component comp : participantsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel participantPanel = (JPanel) comp;
                
                // Get the type selection
                String participantType = null;
                for (Component c : participantPanel.getComponents()) {
                    if (c instanceof JComboBox) {
                        @SuppressWarnings("unchecked")
                        JComboBox<String> typeBox = (JComboBox<String>) c;
                        participantType = (String) typeBox.getSelectedItem();
                        break;
                    }
                }
                
                // Skip if no type selected or if it's "SELECT TYPE"
                if (participantType == null || "SELECT TYPE".equals(participantType)) {
                    continue;
                }

                JPanel innerPanel = getVisibleParticipantPanel(participantPanel);
                if (innerPanel != null) {
                    try {
                        // Validate the panel first
                        validateParticipantPanel(innerPanel);
                        
                        // Create and save the participant
                        Participants participant = createParticipantFromPanel(innerPanel, participantType);
                        if (participant != null) {
                            ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
                            int participantId = participantsDAO.createParticipant(participant);
                            participantIds.add(participantId);
                        }
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(this, 
                            "Error saving participant: " + e.getMessage(),
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(this,
                            "Validation error: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    // Modified createParticipantFromPanel to include participant type
    private Participants createParticipantFromPanel(JPanel panel, String participantType) {
        Participants participant = new Participants();
        participant.setParticipantType(participantType);
        
        if ("Student".equals(participantType)) {
            try {
                StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
                Student student = studentsDataDAO.getStudentDataByLrn(studentLrnField.getText().trim());
                
                if (student != null) {
                    participant.setStudentUid(student.getStudentUid());
                    participant.setParticipantFirstName(student.getStudentFirstname());
                    participant.setParticipantLastName(student.getStudentLastname());
                    participant.setSex(student.getStudentSex());
                    participant.setContactNumber(student.getContact() != null ? 
                        student.getContact().getContactNumber() : "");
                } else {
                    // If student not found, use the form fields
                    participant.setParticipantFirstName(studentFirstNameField.getText().trim());
                    participant.setParticipantLastName(studentLastNameField.getText().trim());
                    participant.setSex((String) studentSexComboBox.getSelectedItem());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // If there's an error, use the form fields
                participant.setParticipantFirstName(studentFirstNameField.getText().trim());
                participant.setParticipantLastName(studentLastNameField.getText().trim());
                participant.setSex((String) studentSexComboBox.getSelectedItem());
            }
        } else if ("Non-Student".equals(participantType)) {
            participant.setParticipantFirstName(nonStudentFirstNameField.getText().trim());
            participant.setParticipantLastName(nonStudentLastNameField.getText().trim());
            participant.setContactNumber(nonStudentContactField.getText().trim());
            participant.setSex((String) nonStudentSexComboBox.getSelectedItem());
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

    public void validateInputs() throws IllegalArgumentException {
        StringBuilder errors = new StringBuilder();

        // Validate title
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            errors.append("- Title is required\n");
        } else if (titleField.getText().trim().length() < 3) {
            errors.append("- Title must be at least 3 characters long\n");
        }

        // Validate date and time
        if (datePicker.getSelectedDate() == null) {
            errors.append("- Date is required\n");
        } else if (datePicker.getSelectedDate().isBefore(LocalDate.now())) {
            errors.append("- Cannot schedule appointments in the past\n");
        }

        if (timePicker.getSelectedTime() == null) {
            errors.append("- Time is required\n");
        }

        // Validate future date/time combination
        if (datePicker.getSelectedDate() != null && timePicker.getSelectedTime() != null) {
            LocalDateTime selectedDateTime = LocalDateTime.of(
                    datePicker.getSelectedDate(),
                    timePicker.getSelectedTime());
            if (selectedDateTime.isBefore(LocalDateTime.now())) {
                errors.append("- Cannot schedule appointments in the past\n");
            }
        }

        // Validate consultation type
        if (typeComboBox.getSelectedItem() == null ||
                typeComboBox.getSelectedItem().toString().equals("~Please Select~")) {
            errors.append("- Consultation type must be selected\n");
        }

        // Validate status
        if (statusComboBox.getSelectedItem() == null ||
                statusComboBox.getSelectedItem().toString().equals("~Please Select~")) {
            errors.append("- Status must be selected\n");
        }

        // Validate counselor assignment
        if (appointment.getGuidanceCounselorId() == null ||
                appointment.getGuidanceCounselorId() <= 0) {
            errors.append("- A guidance counselor must be assigned\n");
        }

        // Validate participants
        boolean hasValidParticipant = false;
        StringBuilder participantErrors = new StringBuilder();

        for (Component comp : participantsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel participantPanel = (JPanel) comp;
                JPanel innerPanel = getVisibleParticipantPanel(participantPanel);

                if (innerPanel != null) {
                    try {
                        validateParticipantPanel(innerPanel);
                        hasValidParticipant = true;
                    } catch (IllegalArgumentException e) {
                        participantErrors.append("  ").append(e.getMessage()).append("\n");
                    }
                }
            }
        }

        if (!hasValidParticipant) {
            errors.append("- At least one valid participant is required\n");
            if (participantErrors.length() > 0) {
                errors.append("Participant validation errors:\n").append(participantErrors);
            }
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException("Please fix the following errors:\n" + errors.toString());
        }
    }

    private void validateParticipantPanel(JPanel panel) throws IllegalArgumentException {
        StringBuilder errors = new StringBuilder();

        // Get the type selection from combo box
        String participantType = null;
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JComboBox) {
                @SuppressWarnings("unchecked")
                JComboBox<String> typeBox = (JComboBox<String>) comp;
                participantType = (String) typeBox.getSelectedItem();
                break;
            }
        }

        if ("SELECT TYPE".equals(participantType)) {
            errors.append("- Please select a participant type\n");
        } else if ("Student".equals(participantType)) {
            // Validate student fields
            if (studentLrnField.getText().trim().isEmpty()) {
                errors.append("- Student LRN is required\n");
            }
            if (studentFirstNameField.getText().trim().isEmpty()) {
                errors.append("- Student first name is required\n");
            }
            if (studentLastNameField.getText().trim().isEmpty()) {
                errors.append("- Student last name is required\n");
            }
            if (studentSexComboBox.getSelectedItem() == null) {
                errors.append("- Student sex is required\n");
            }
        } else if ("Non-Student".equals(participantType)) {
            // Validate non-student fields
            if (nonStudentFirstNameField.getText().trim().isEmpty()) {
                errors.append("- First name is required\n");
            }
            if (nonStudentLastNameField.getText().trim().isEmpty()) {
                errors.append("- Last name is required\n");
            }
            if (nonStudentContactField.getText().trim().isEmpty()) {
                errors.append("- Contact number is required\n");
            } else if (!nonStudentContactField.getText().trim().matches("\\d{11}")) {
                errors.append("- Invalid contact number format (must be 11 digits)\n");
            }
            if (nonStudentSexComboBox.getSelectedItem() == null) {
                errors.append("- Sex is required\n");
            }
        }

        if (errors.length() > 0) {
            throw new IllegalArgumentException(errors.toString());
        }
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
            appointment.setAppointmentStatus((String) statusComboBox.getSelectedItem());
            appointment.setAppointmentNotes(notesArea.getText().trim());

            // Debugging: Print appointment details
            System.out.println("Appointment Details:");
            System.out.println("Title: " + appointment.getAppointmentTitle());
            System.out.println("Type: " + appointment.getConsultationType());
            System.out.println("Date/Time: " + appointment.getAppointmentDateTime());
            System.out.println("Status: " + appointment.getAppointmentStatus());
            System.out.println("Notes: " + appointment.getAppointmentNotes());
            System.out.println("Participants: " + participantIds);

            // Validate that we have at least one participant
            if (participantIds.isEmpty()) {
                throw new IllegalArgumentException("At least one participant is required");
            }

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
                JOptionPane.showMessageDialog(this, 
                    "Appointment saved successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                return true; // Successfully saved
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to save the appointment.", 
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                "Validation Error: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + e.getMessage(), 
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return false; // Failed to save
    }
}
