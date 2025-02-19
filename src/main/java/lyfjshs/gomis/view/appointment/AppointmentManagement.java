package lyfjshs.gomis.view.appointment;

import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JPanel;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import net.miginfocom.swing.MigLayout;

public class AppointmentManagement extends JPanel {
    private Connection connection;
    private AppointmentCalendar appointmentCalendar;

    public AppointmentManagement(Connection connection) {
        this.connection = connection;
        setLayout(new MigLayout("fill, insets 0"));

        // Initialize AppointmentDAO
        AppointmentDAO appointmentDAO = new AppointmentDAO();

        // Create the AppointmentCalendar
        appointmentCalendar = new AppointmentCalendar(appointmentDAO, connection);
        add(appointmentCalendar, "grow");
        
        // Add other components like buttons if needed
        JButton addAppointmentButton = new JButton("Add Appointment");
        addAppointmentButton.addActionListener(e -> createAppointment());
        add(addAppointmentButton, "wrap");
    }

    private void createAppointment() {
        // Logic to create a new appointment
    }
} 