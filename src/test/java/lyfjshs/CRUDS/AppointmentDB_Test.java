package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;

public class AppointmentDB_Test {

    public static void main(String[] args) {
        // Database connection parameters
        String url = "jdbc:mysql://localhost:3306/gomisDB";
        String user = "root";
        String password = ""; // Update with your actual password

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            AppointmentDAO appointmentDAO = new AppointmentDAO();

            // Test CREATE operation
            System.out.println("Testing CREATE operation:");
            boolean createSuccess = appointmentDAO.addAppointment(
                    conn,
                    1, // participantId
                    1, // counselorsId
                    "Counseling Session", // appointmentTitle
                    "Individual", // appointmentType
                    Timestamp.valueOf(LocalDateTime.now().plusDays(1)), // appointmentDateTime
                    "Initial notes", // appointmentNotes
                    "Scheduled" // appointmentStatus
            );
            System.out.println("Create operation successful: " + createSuccess);
            System.out.println("------------------------");

            // Test READ operation - Get appointments for today
            System.out.println("Testing READ operation (getAppointmentsForDate):");
            List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(
                    conn,
                    LocalDate.now().plusDays(1));
            System.out.println("Appointments found: " + appointments.size());
            for (Appointment apt : appointments) {
                System.out.println("Appointment: " + apt.toString());
            }
            System.out.println("------------------------");

            // Test READ operation - Get appointment by ID (assuming ID 1 exists)
            System.out.println("Testing READ operation (getAppointmentById):");
            Appointment appointment = appointmentDAO.getAppointmentById(conn, 1);
            if (appointment != null) {
                System.out.println("Found appointment: " + appointment.toString());
            } else {
                System.out.println("No appointment found with ID 1");
            }
            System.out.println("------------------------");

            // Test UPDATE operation (assuming ID 1 exists)
            if (appointment != null) {
                System.out.println("Testing UPDATE operation:");
                appointment.setAppointmentType("Group");
                appointment.setAppointmentStatus("Completed");
                appointment.setAppointmentNotes("Updated notes");
                boolean updateSuccess = appointmentDAO.updateAppointment(conn, appointment);
                System.out.println("Update operation successful: " + updateSuccess);

                // Verify update
                Appointment updatedAppointment = appointmentDAO.getAppointmentById(conn, 1);
                if (updatedAppointment != null) {
                    System.out.println("Updated appointment: " + updatedAppointment.toString());
                }
                System.out.println("------------------------");
            }

            // Test DELETE operation (assuming ID 1 exists)
            System.out.println("Testing DELETE operation:");
            boolean deleteSuccess = appointmentDAO.deleteAppointment(conn, 1);
            System.out.println("Delete operation successful: " + deleteSuccess);

            // Verify deletion
            Appointment deletedAppointment = appointmentDAO.getAppointmentById(conn, 1);
            System.out.println("Appointment after deletion: "
                    + (deletedAppointment == null ? "Not found" : deletedAppointment.toString()));
            System.out.println("------------------------");

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}