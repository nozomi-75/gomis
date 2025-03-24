package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;

public class AppointmentDayDetails extends JPanel {
    private Connection connection;
    private JPanel bodyPanel;
    private Consumer<Participants> onParticipantSelect;
    private Consumer<Appointment> onRedirectToSession;
    // Add field to store current appointment
    private Appointment currentAppointment;

    public AppointmentDayDetails(Connection connection, Consumer<Participants> onParticipantSelect, Consumer<Appointment> onRedirectToSession) {
        this.connection = connection;
        this.onParticipantSelect = onParticipantSelect;
        this.onRedirectToSession = onRedirectToSession;
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow]"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][right]", "[]"));
        JLabel titleLabel = new JLabel("Appointment Details");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel, "align center");
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        add(headerPanel, "north");

        // Body Panel with MigLayout
        bodyPanel = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "cell 0 0, grow");
    }

    // Method to load appointments for a specific date
    public void loadAppointmentsForDate(LocalDate date) throws SQLException {
        bodyPanel.removeAll(); // Clear existing components
        AppointmentDAO appointmentsDAO = new AppointmentDAO(connection);
        List<Appointment> appointments = appointmentsDAO.getAppointmentsForDate(date);

        if (appointments.isEmpty()) {
            displayNoAppointmentsMessage();
        } else {
            for (Appointment appointment : appointments) {
                displayAppointmentDetails(appointment);
            }
        }
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    // Method to load a specific appointment
    public void loadAppointmentDetails(Appointment appointment) {
        this.currentAppointment = appointment; // Store the current appointment
        bodyPanel.removeAll(); // Clear existing components
        displayAppointmentDetails(appointment);
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    // Add getter for current appointment
    public Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    // Method to display no appointments message
    private void displayNoAppointmentsMessage() {
        JLabel noAppointmentsLabel = new JLabel("No appointments scheduled for this day.");
        noAppointmentsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        bodyPanel.add(noAppointmentsLabel, "cell 0 0, align center");
    }

    // Method to display appointment details
    private void displayAppointmentDetails(Appointment appointment) {
        // Appointment Information Section
        JPanel appointmentSection = new JPanel(new MigLayout("wrap 2, insets 10", "[grow][grow]", "[]"));
        appointmentSection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Appointment Information"));
        
        // Format date and time in 12-hour format
        String dateTimeStr = appointment.getAppointmentDateTime().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        
        appointmentSection.add(new JLabel("Title: "), "cell 0 0, alignx right");
        appointmentSection.add(new JLabel(appointment.getAppointmentTitle()), "cell 1 0");
        appointmentSection.add(new JLabel("Type: "), "cell 0 1, alignx right");
        appointmentSection.add(new JLabel(appointment.getConsultationType()), "cell 1 1");
        appointmentSection.add(new JLabel("Date & Time: "), "cell 0 2, alignx right");
        appointmentSection.add(new JLabel(dateTimeStr), "cell 1 2");
        appointmentSection.add(new JLabel("Status: "), "cell 0 3, alignx right");
        appointmentSection.add(new JLabel(appointment.getAppointmentStatus()), "cell 1 3");
        if (appointment.getAppointmentNotes() != null && !appointment.getAppointmentNotes().isEmpty()) {
            appointmentSection.add(new JLabel("Notes: "), "cell 0 4, alignx right");
            appointmentSection.add(new JLabel(appointment.getAppointmentNotes()), "cell 1 4");
        }
        appointmentSection.add(new JLabel("Last Updated: "), "cell 0 5, alignx right");
        appointmentSection.add(new JLabel(appointment.getUpdatedAt().toString()), "cell 1 5");
        bodyPanel.add(appointmentSection, "growx, wrap");

        // Participants Section
        JPanel participantsSection = new JPanel(new MigLayout("wrap 1, insets 10", "[grow]", "[]"));
        participantsSection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Participants"));
        if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
            for (Participants participant : appointment.getParticipants()) {
                String participantInfo = "Name: " + participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
                if (participant.getStudentUid() != null) {
                    participantInfo += " (Student UID: " + participant.getStudentUid() + ")";
                }
                if (participant.getContactNumber() != null) {
                    participantInfo += ", Contact: " + participant.getContactNumber();
                }
                if (participant.getSex() != null) {
                    participantInfo += ", Email: " + participant.getSex();
                }
                participantsSection.add(new JLabel(participantInfo), "growx");
            }
        } else {
            participantsSection.add(new JLabel("No participants assigned."), "growx");
        }
        bodyPanel.add(participantsSection, "growx, wrap");

        // Guidance Counselor Section
        JPanel counselorSection = new JPanel(new MigLayout("wrap 2, insets 10", "[grow][grow]", "[]"));
        counselorSection.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Guidance Counselor Information"));
        GuidanceCounselorDAO counselorDAO = new GuidanceCounselorDAO(connection);
        GuidanceCounselor counselor = null;
        if (appointment.getGuidanceCounselorId() != null) {
            counselor = counselorDAO.readGuidanceCounselor(appointment.getGuidanceCounselorId());
        }
        if (counselor != null) {
            counselorSection.add(new JLabel("Name: "), "cell 0 0, alignx right");
            counselorSection.add(new JLabel(counselor.getFirstName() + " " + counselor.getLastName()), "cell 1 0");
            counselorSection.add(new JLabel("Gender: "), "cell 0 1, alignx right");
            counselorSection.add(new JLabel(counselor.getGender()), "cell 1 1");
            counselorSection.add(new JLabel("Specialization: "), "cell 0 2, alignx right");
            counselorSection.add(new JLabel(counselor.getSpecialization()), "cell 1 2");
            counselorSection.add(new JLabel("Contact: "), "cell 0 3, alignx right");
            counselorSection.add(new JLabel(counselor.getContactNum()), "cell 1 3");
            counselorSection.add(new JLabel("Email: "), "cell 0 4, alignx right");
            counselorSection.add(new JLabel(counselor.getEmail()), "cell 1 4");
            counselorSection.add(new JLabel("Position: "), "cell 0 5, alignx right");
            counselorSection.add(new JLabel(counselor.getPosition()), "cell 1 5");
        } else {
            counselorSection.add(new JLabel("No guidance counselor information available."), "cell 0 0, span 2");
        }
        bodyPanel.add(counselorSection, "growx, wrap");
    }

}