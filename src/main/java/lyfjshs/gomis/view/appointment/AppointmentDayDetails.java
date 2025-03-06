package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;

public class AppointmentDayDetails extends JPanel {
    private JButton closeButton_1;
    private Connection connection;
    private JPanel bodyPanel;

    public AppointmentDayDetails(Connection connection) {
        this.connection = connection;
        setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow][]"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][right]", "[]"));
        JLabel titleLabel = new JLabel("Appointment Details");
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
        bodyPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "cell 0 0, grow");
        closeButton_1 = new JButton("Close");
        add(closeButton_1, "cell 0 1,alignx right");
        closeButton_1.setBackground(new Color(244, 67, 54));
        closeButton_1.setForeground(Color.WHITE);
        closeButton_1.setFocusPainted(false);
        closeButton_1.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
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

    // Method to display no appointments message
    private void displayNoAppointmentsMessage() {
        JLabel noAppointmentsLabel = new JLabel("No appointments scheduled for this day.");
        noAppointmentsLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        bodyPanel.add(noAppointmentsLabel, "cell 0 0, align center");
    }

    // Method to display appointment details
    private void displayAppointmentDetails(Appointment appointment) {
        // Appointment Information Section
        JPanel appointmentSection = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
        appointmentSection.setBorder(BorderFactory.createTitledBorder("Appointment Information"));
        appointmentSection.add(new JLabel("Title: " + appointment.getAppointmentTitle()), "cell 0 0");
        appointmentSection.add(new JLabel("Type: " + appointment.getAppointmentType()), "cell 0 1");
        appointmentSection.add(new JLabel("Date & Time: " + appointment.getAppointmentDateTime()), "cell 0 2");
        appointmentSection.add(new JLabel("Status: " + appointment.getAppointmentStatus()), "cell 0 3");
        if (appointment.getAppointmentNotes() != null && !appointment.getAppointmentNotes().isEmpty()) {
            appointmentSection.add(new JLabel("Notes: " + appointment.getAppointmentNotes()), "cell 0 4");
        }
        appointmentSection.add(new JLabel("Last Updated: " + appointment.getUpdatedAt()), "cell 0 5");
        bodyPanel.add(appointmentSection, "cell 0 0,growx");

        // Participants Section
        JPanel participantsSection = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
        participantsSection.setBorder(BorderFactory.createTitledBorder("Participants"));
        if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
            for (int i = 0; i < appointment.getParticipants().size(); i++) {
                Participants participant = appointment.getParticipants().get(i);
                String participantInfo = "Name: " + participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
                if (participant.getStudentUid() != null) {
                    participantInfo += " (Student UID: " + participant.getStudentUid() + ")";
                }
                if (participant.getContactNumber() != null) {
                    participantInfo += ", Contact: " + participant.getContactNumber();
                }
                if (participant.getEmail() != null) {
                    participantInfo += ", Email: " + participant.getEmail();
                }
                participantsSection.add(new JLabel(participantInfo), "cell 0 " + i);
            }
        } else {
            participantsSection.add(new JLabel("No participants assigned."), "cell 0 0");
        }
        bodyPanel.add(participantsSection, "cell 0 1,growx");

        // Guidance Counselor Section
        JPanel counselorSection = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
        counselorSection.setBorder(BorderFactory.createTitledBorder("Guidance Counselor Information"));
        GuidanceCounselorDAO counselorDAO = new GuidanceCounselorDAO(connection);
        GuidanceCounselor counselor = null;
        counselor = counselorDAO.readGuidanceCounselor(appointment.getGuidanceCounselorId());
        if (counselor != null) {
            counselorSection.add(new JLabel("Name: " + counselor.getFirstName() + " " + counselor.getLastName()), "cell 0 0");
            counselorSection.add(new JLabel("Gender: " + counselor.getGender()), "cell 0 1");
            counselorSection.add(new JLabel("Specialization: " + counselor.getSpecialization()), "cell 0 2");
            counselorSection.add(new JLabel("Contact: " + counselor.getContactNum()), "cell 0 3");
            counselorSection.add(new JLabel("Email: " + counselor.getEmail()), "cell 0 4");
            counselorSection.add(new JLabel("Position: " + counselor.getPosition()), "cell 0 5");
        } else {
            counselorSection.add(new JLabel("No guidance counselor information available."), "cell 0 0");
        }
        bodyPanel.add(counselorSection, "cell 0 2,growx");
    }

    // Example usage in a JFrame
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Set FlatLaf theme
            FlatMacLightLaf.setup();

            JFrame frame = new JFrame("Appointment Detailed View");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Connection connection = null;
            try {
                connection = DBConnection.getConnection();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
            AppointmentDayDetails panel = new AppointmentDayDetails(connection);
            frame.getContentPane().add(panel);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Load appointments for a specific date
            try {
                panel.loadAppointmentsForDate(LocalDate.of(2025, 3, 13)); // Replace with actual date
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error loading appointments: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}