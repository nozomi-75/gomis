package lyfjshs.gomis.view.appointment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import net.miginfocom.swing.MigLayout;

public class AppointmentDialog extends JDialog {
    private JComboBox<String> participantTypeCombo;
    private JTextField studentUidField;
    private JComboBox<String> appointmentTypeCombo;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private boolean confirmed;

    public AppointmentDialog(Frame parent) {
        super(parent, "Create Appointment", true);
        setLayout(new MigLayout("wrap 2", "[][grow]", "[]10[]10[]10[]10[]10[]"));
        
        // Participant Type Selection
        participantTypeCombo = new JComboBox<>(new String[]{"Student", "Non-Student"});
        participantTypeCombo.addActionListener(e -> {
            studentUidField.setEnabled(participantTypeCombo.getSelectedItem().equals("Student"));
        });

        // Student UID field
        studentUidField = new JTextField();
        
        // Appointment Type field
        appointmentTypeCombo = new JComboBox<>(new String[]{
            "Academic Consultation",
            "Career Guidance",
            "Personal Counseling",
            "Behavioral Counseling",
            "Group Counseling"
        });

        // Replace date and time fields with pickers
        datePicker = new DatePicker();
        datePicker.now();
        JFormattedTextField dateEditor = new JFormattedTextField();
        datePicker.setEditor(dateEditor);

        timePicker = new TimePicker();
        timePicker.now();
        JFormattedTextField timeEditor = new JFormattedTextField();
        timePicker.setEditor(timeEditor);


        // Add components with MigLayout constraints
        add(new JLabel("Participant Type:"));
        add(participantTypeCombo, "growx");
        add(new JLabel("Student UID:"));
        add(studentUidField, "growx");
        add(new JLabel("Appointment Type:"));
        add(appointmentTypeCombo, "growx");
        add(new JLabel("Date:"));
        add(dateEditor, "growx");
        add(new JLabel("Time:"));
        add(timeEditor, "growx");

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> {
            if (validateInputs()) {
                confirmed = true;
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));

        add(confirmButton);
        add(cancelButton);

        // Initialize state
        studentUidField.setEnabled(true);
        setSize(450, 350);
        setLocationRelativeTo(parent);
    }

    private boolean validateInputs() {
        // Only validate student UID if "Student" is selected
        if (participantTypeCombo.getSelectedItem().equals("Student")) {
            String uidText = studentUidField.getText().trim();
            if (uidText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Student UID");
                return false;
            }
            try {
                Integer.parseInt(uidText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Student UID must be a valid number");
                return false;
            }
        }
        
        if (!datePicker.isDateSelected() || !timePicker.isTimeSelected()) {
            JOptionPane.showMessageDialog(this, "Please select both date and time");
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isStudent() {
        return participantTypeCombo.getSelectedItem().equals("Student");
    }

    public Integer getStudentUid() {
        if (!isStudent() || studentUidField.getText().trim().isEmpty()) {
            return null;
        }
        return Integer.parseInt(studentUidField.getText().trim());
    }

    public String getAppointmentType() {
        return (String) appointmentTypeCombo.getSelectedItem();
    }

    public LocalDateTime getAppointmentDateTime() {
        LocalDate date = datePicker.getSelectedDate();
        LocalTime time = timePicker.getSelectedTime();
        return LocalDateTime.of(date, time);
    }
} 