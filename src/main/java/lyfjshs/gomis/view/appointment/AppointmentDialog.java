package lyfjshs.gomis.view.appointment;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import net.miginfocom.swing.MigLayout;
import lyfjshs.gomis.Database.model.Appointment;

public class AppointmentDialog extends JDialog {
    private JComboBox<String> participantTypeCombo;
    private JTextField participantIdField;
    private JTextField counselorIdField;
    private JTextField titleField;
    private JComboBox<String> appointmentTypeCombo;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private JTextField notesField;
    private JComboBox<String> statusCombo;
    private boolean confirmed;
    private Appointment appointment;

    public AppointmentDialog(Frame parent) {
        this(parent, new Appointment());
    }

    public AppointmentDialog(Frame parent, Appointment existingAppt) {
        super(parent, existingAppt.getAppointmentId() == 0 ? "Create Appointment" : "Edit Appointment", true);
        this.appointment = existingAppt;
        initComponents();
        if (existingAppt.getAppointmentId() != 0) {
            populateFields();
        }
        setSize(450, 450);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap 2", "[][grow]", "[]10[]"));

        // Participant Type Selection
        participantTypeCombo = new JComboBox<>(new String[]{"Student", "Non-Student"});
        participantTypeCombo.addActionListener(e -> {
            participantIdField.setEnabled(participantTypeCombo.getSelectedItem().equals("Student"));
        });

        // Participant ID field
        participantIdField = new JTextField(10);

        // Counselor ID field
        counselorIdField = new JTextField(10);

        // Title field
        titleField = new JTextField(20);

        // Appointment Type field
        appointmentTypeCombo = new JComboBox<>(new String[]{
            "Academic Consultation",
            "Career Guidance",
            "Personal Counseling",
            "Behavioral Counseling",
            "Group Counseling"
        });

        // Date Picker
        datePicker = new DatePicker();
        datePicker.now();
        JFormattedTextField dateEditor = new JFormattedTextField();
        datePicker.setEditor(dateEditor);

        // Time Picker
        timePicker = new TimePicker();
        timePicker.now();
        JFormattedTextField timeEditor = new JFormattedTextField();
        timePicker.setEditor(timeEditor);

        // Notes field
        notesField = new JTextField(20);

        // Status field
        statusCombo = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});

        // Add components
        add(new JLabel("Participant Type:"));
        add(participantTypeCombo, "growx");
        
        add(new JLabel("Participant ID:"));
        add(participantIdField, "growx");
        
        add(new JLabel("Counselor ID:"));
        add(counselorIdField, "growx");
        
        add(new JLabel("Title:"));
        add(titleField, "growx");
        
        add(new JLabel("Appointment Type:"));
        add(appointmentTypeCombo, "growx");
        
        add(new JLabel("Date:"));
        add(dateEditor, "growx");
        
        add(new JLabel("Time:"));
        add(timeEditor, "growx");
        
        add(new JLabel("Notes:"));
        add(notesField, "growx");
        
        add(new JLabel("Status:"));
        add(statusCombo, "growx");

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            if (validateInputs()) {
                confirmed = true;
                updateAppointment();
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));

        add(confirmButton, "span 2, split 2, align center");
        add(cancelButton);
    }

    private void populateFields() {
        participantTypeCombo.setSelectedItem(
            appointment.getParticipantId() > 0 ? "Student" : "Non-Student");
        participantIdField.setText(String.valueOf(appointment.getParticipantId()));
        counselorIdField.setText(appointment.getCounselorsId() != null ? 
            String.valueOf(appointment.getCounselorsId()) : "");
        titleField.setText(appointment.getAppointmentTitle());
        appointmentTypeCombo.setSelectedItem(appointment.getAppointmentType());
        datePicker.setSelectedDate(appointment.getAppointmentDateTime().toLocalDate());
        timePicker.setSelectedTime(appointment.getAppointmentDateTime().toLocalTime());
        notesField.setText(appointment.getAppointmentNotes());
        statusCombo.setSelectedItem(appointment.getAppointmentStatus());
    }

    private boolean validateInputs() {
        // Validate participant ID
        String participantIdText = participantIdField.getText().trim();
        if (participantIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Participant ID");
            return false;
        }
        try {
            Integer.parseInt(participantIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Participant ID must be a valid number");
            return false;
        }

        // Validate counselor ID if provided
        String counselorIdText = counselorIdField.getText().trim();
        if (!counselorIdText.isEmpty()) {
            try {
                Integer.parseInt(counselorIdText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Counselor ID must be a valid number");
                return false;
            }
        }

        // Validate title
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter appointment title");
            return false;
        }

        // Validate date and time
        if (!datePicker.isDateSelected() || !timePicker.isTimeSelected()) {
            JOptionPane.showMessageDialog(this, "Please select both date and time");
            return false;
        }

        return true;
    }

    private void updateAppointment() {
        appointment.setParticipantId(Integer.parseInt(participantIdField.getText().trim()));
        
        String counselorIdText = counselorIdField.getText().trim();
        appointment.setCounselorsId(counselorIdText.isEmpty() ? null : Integer.parseInt(counselorIdText));
        
        appointment.setAppointmentTitle(titleField.getText().trim());
        appointment.setAppointmentType((String)appointmentTypeCombo.getSelectedItem());
        
        LocalDate date = datePicker.getSelectedDate();
        LocalTime time = timePicker.getSelectedTime();
        appointment.setAppointmentDateTime(LocalDateTime.of(date, time));
        
        appointment.setAppointmentNotes(notesField.getText().trim());
        appointment.setAppointmentStatus((String)statusCombo.getSelectedItem());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    // Legacy methods for backward compatibility
    public Integer getStudentUid() {
        return participantTypeCombo.getSelectedItem().equals("Student") ? 
            Integer.parseInt(participantIdField.getText().trim()) : null;
    }

    public String getAppointmentType() {
        return (String)appointmentTypeCombo.getSelectedItem();
    }

    public LocalDateTime getAppointmentDateTime() {
        return LocalDateTime.of(datePicker.getSelectedDate(), timePicker.getSelectedTime());
    }
}