package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;

public class AppointmentsDB_Test {
    
    private static Connection connection;
    private static AppointmentDAO appointmentDAO;

    public static void main(String[] args) {
        try {
            setUp();
            
            // Run Tests
            testAddAppointment();
            testGetAppointmentById();
            testGetAllAppointments();
            testUpdateAppointment();
            testDeleteAppointment();
            
            tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void setUp() throws SQLException {
        // Set up the database connection
        String url = "jdbc:mariadb://localhost:3306/gomis";
        String user = "root";
        String password = "YourRootPassword123!";

        connection = DriverManager.getConnection(url, user, password);
        appointmentDAO = new AppointmentDAO(connection);

        System.out.println("✔ Database connection established.");
    }

    static void testAddAppointment() {
        try {
            Timestamp appointmentDateTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

            boolean result = appointmentDAO.insertAppointment(
                1, // participant_id
                2, // counselors_id (nullable)
                "Counseling Session",
                "Mental Health",
                appointmentDateTime,
                "Follow-up session",
                "Scheduled"
            );

            System.out.println(result ? "✔ testAddAppointment Passed" : "❌ testAddAppointment Failed");
        } catch (Exception e) {
            System.err.println("❌ testAddAppointment Failed: " + e.getMessage());
        }
    }

    static void testGetAppointmentById() {
        try {
            int testAppointmentId = 1; // Make sure this ID exists in your test database
            Appointment appointment = appointmentDAO.getAppointmentById(testAppointmentId);

            if (appointment != null && appointment.getAppointmentId() == testAppointmentId) {
                System.out.println("✔ testGetAppointmentById Passed");
            } else {
                System.out.println("❌ testGetAppointmentById Failed");
            }
        } catch (Exception e) {
            System.err.println("❌ testGetAppointmentById Failed: " + e.getMessage());
        }
    }

    static void testGetAllAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            if (appointments != null && !appointments.isEmpty()) {
                System.out.println("✔ testGetAllAppointments Passed");
            } else {
                System.out.println("❌ testGetAllAppointments Failed");
            }
        } catch (Exception e) {
            System.err.println("❌ testGetAllAppointments Failed: " + e.getMessage());
        }
    }

    static void testUpdateAppointment() {
        try {
            int testAppointmentId = 1; // Ensure this appointment exists
            Appointment appointment = appointmentDAO.getAppointmentById(testAppointmentId);

            if (appointment == null) {
                System.out.println("❌ testUpdateAppointment Failed: Appointment not found");
                return;
            }

            boolean updated = appointmentDAO.updateAppointment(
                testAppointmentId,
                appointment.getParticipantId(),
                appointment.getCounselorsId(),
                "Updated Title",
                appointment.getAppointmentType(),
                Timestamp.valueOf(appointment.getAppointmentDateTime()),
                appointment.getAppointmentNotes(),
                "Completed"
            );

            System.out.println(updated ? "✔ testUpdateAppointment Passed" : "❌ testUpdateAppointment Failed");
        } catch (Exception e) {
            System.err.println("❌ testUpdateAppointment Failed: " + e.getMessage());
        }
    }

    static void testDeleteAppointment() {
        try {
            int appointmentIdToDelete = 2; // Ensure this appointment exists
            boolean deleted = appointmentDAO.deleteAppointment(appointmentIdToDelete);

            System.out.println(deleted ? "✔ testDeleteAppointment Passed" : "❌ testDeleteAppointment Failed");
        } catch (Exception e) {
            System.err.println("❌ testDeleteAppointment Failed: " + e.getMessage());
        }
    }

    static void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
            System.out.println("✔ Database connection closed.");
        }
    }
}
