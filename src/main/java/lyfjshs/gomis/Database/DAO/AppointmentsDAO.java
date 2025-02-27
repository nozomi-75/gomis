package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.Appointments;

public class AppointmentsDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/your_database";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    // CREATE (Insert a new appointment)
    public boolean insertAppointment(int participantId, Integer counselorsId, String title, String type, Timestamp dateTime, String notes, String status) {
        String query = "INSERT INTO APPOINTMENTS (participant_id, counselors_id, appointment_title, appointment_type, appointment_date_time, appointment_notes, appointment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            
            if (counselorsId != null) {
                stmt.setInt(2, counselorsId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setString(3, title);
            stmt.setString(4, type);
            stmt.setTimestamp(5, dateTime);
            stmt.setString(6, notes);
            stmt.setString(7, status);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ (Retrieve an appointment by ID)
    public Appointments getAppointmentById(int appointmentId) {
        String query = "SELECT * FROM APPOINTMENTS WHERE appointment_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Appointments(
                        rs.getInt("appointment_id"),
                        rs.getInt("participant_id"),
                        rs.getInt("counselors_id"),
                        rs.getString("appointment_title"),
                        rs.getString("appointment_type"),
                        rs.getTimestamp("appointment_date_time"),
                        rs.getString("appointment_notes"),
                        rs.getString("appointment_status"),
                        rs.getTimestamp("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ ALL (Retrieve all appointments)
    public List<Appointments> getAllAppointments() {
        List<Appointments> appointments = new ArrayList<>();
        String query = "SELECT * FROM APPOINTMENTS";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                appointments.add(new Appointments(
                    rs.getInt("appointment_id"),
                    rs.getInt("participant_id"),
                    rs.getInt("counselors_id"),
                    rs.getString("appointment_title"),
                    rs.getString("appointment_type"),
                    rs.getTimestamp("appointment_date_time"),
                    rs.getString("appointment_notes"),
                    rs.getString("appointment_status"),
                    rs.getTimestamp("updated_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }

    // UPDATE (Modify an existing appointment)
    public boolean updateAppointment(int appointmentId, Integer counselorsId, String title, String type, Timestamp dateTime, String notes, String status) {
        String query = "UPDATE APPOINTMENTS SET counselors_id = ?, appointment_title = ?, appointment_type = ?, appointment_date_time = ?, appointment_notes = ?, appointment_status = ?, updated_at = CURRENT_TIMESTAMP WHERE appointment_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (counselorsId != null) {
                stmt.setInt(1, counselorsId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, title);
            stmt.setString(3, type);
            stmt.setTimestamp(4, dateTime);
            stmt.setString(5, notes);
            stmt.setString(6, status);
            stmt.setInt(7, appointmentId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE (Remove an appointment)
    public boolean deleteAppointment(int appointmentId) {
        String query = "DELETE FROM APPOINTMENTS WHERE appointment_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
