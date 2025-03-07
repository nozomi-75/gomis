package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;

public class AppointmentsDB_Test {

    private static Connection connection;
    private static AppointmentDAO appointmentDAO;

    public static void main(String[] args) {
        try {
            setUp();

            // Run Tests
            testAddAppointment();
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
        String url = "jdbc:mariadb://localhost:3306/gomisdb";
        String user = "root";
        String password = "YourRootPassword123!";

        connection = DriverManager.getConnection(url, user, password);
        appointmentDAO = new AppointmentDAO(connection);

        System.out.println("✔ Database connection established.");
    }

    static void testAddAppointment() {
        try {
            Timestamp appointmentDateTime = Timestamp.valueOf(LocalDateTime.now().plusDays(1));
            List<Integer> participantIds = new ArrayList<>(); // Add participant IDs if needed

            int result = appointmentDAO.insertAppointment(
                    1, // guidanceCounselorId (nullable)
                    "Counseling Session",
                    "Mental Health",
                    appointmentDateTime,
                    "Follow-up session",
                    "Scheduled",
                    participantIds);
            if (result == 0) {
                System.out.println("❌ testAddAppointment Failed");
            } else {
                System.out.println("✔ testAddAppointment Passed (ID: " + result + ")");
            }
        } catch (Exception e) {
            System.err.println("❌ testAddAppointment Failed: " + e.getMessage());
        }
    }

    static void testGetAllAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            if (appointments != null && !appointments.isEmpty()) {
                System.out.println("✔ testGetAllAppointments Passed");
            } else {
                System.out.println("❌ testGetAllAppointments Failed (No appointments found)");
            }
        } catch (Exception e) {
            System.err.println("❌ testGetAllAppointments Failed: " + e.getMessage());
        }
    }

    static void testUpdateAppointment() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            if (appointments.isEmpty()) {
                System.out.println("❌ testUpdateAppointment Failed: No existing appointments to update");
                return;
            }

            Appointment appointment = appointments.get(0); // Take first appointment

            appointment.setAppointmentTitle("Updated Title");
            appointment.setAppointmentNotes("Updated Notes");
            appointment.setAppointmentStatus("Completed");
            appointment.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

            boolean updated = appointmentDAO.updateAppointment(appointment);

            System.out.println(updated ? "✔ testUpdateAppointment Passed" : "❌ testUpdateAppointment Failed");
        } catch (Exception e) {
            System.err.println("❌ testUpdateAppointment Failed: " + e.getMessage());
        }
    }

    static void testDeleteAppointment() {
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointments();
            if (appointments.isEmpty()) {
                System.out.println("❌ testDeleteAppointment Failed: No existing appointments to delete");
                return;
            }

            int appointmentIdToDelete = appointments.get(0).getAppointmentId();
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
