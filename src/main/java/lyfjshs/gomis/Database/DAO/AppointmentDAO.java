package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Appointment;

public class AppointmentDAO {
    private Connection connection;

    // Constructor to initialize connection
    public AppointmentDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE (Insert a new appointment)
    public boolean insertAppointment(int participantId, Integer counselorsId, String appointmentTitle,
                                     String appointmentType, Timestamp appointmentDateTime, String appointmentNotes,
                                     String appointmentStatus) {
        String query = "INSERT INTO APPOINTMENTS (participant_id, counselors_id, appointment_title, appointment_type, " +
                       "appointment_date_time, appointment_notes, appointment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            if (counselorsId != null) {
                stmt.setInt(2, counselorsId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, appointmentTitle);
            stmt.setString(4, appointmentType);
            stmt.setTimestamp(5, appointmentDateTime);
            stmt.setString(6, appointmentNotes);
            stmt.setString(7, appointmentStatus);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting appointment: " + e.getMessage());
        }
        return false;
    }

    // READ (Retrieve an appointment by ID)
    public Appointment getAppointmentById(int appointmentId) {
        String query = "SELECT * FROM APPOINTMENTS WHERE appointment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("participant_id"),
                        rs.getObject("counselors_id", Integer.class),
                        rs.getString("appointment_title"),
                        rs.getString("appointment_type"),
                        rs.getTimestamp("appointment_date_time"),
                        rs.getString("appointment_status"),
                        rs.getTimestamp("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment: " + e.getMessage());
        }
        return null;
    }

    // READ ALL (Retrieve all appointments)
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM APPOINTMENTS";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getInt("participant_id"),
                    rs.getObject("counselors_id", Integer.class),
                    rs.getString("appointment_title"),
                    rs.getString("appointment_type"),
                    rs.getTimestamp("appointment_date_time"),
                    rs.getString("appointment_status"),
                    rs.getTimestamp("updated_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all appointments: " + e.getMessage());
        }
        return appointments;
    }

    // UPDATE (Modify an existing appointment)
    public boolean updateAppointment(int appointmentId, int participantId, Integer counselorsId, String appointmentTitle,
                                     String appointmentType, Timestamp appointmentDateTime, String appointmentNotes,
                                     String appointmentStatus) {
        String query = "UPDATE APPOINTMENTS SET participant_id = ?, counselors_id = ?, appointment_title = ?, " +
                       "appointment_type = ?, appointment_date_time = ?, appointment_notes = ?, appointment_status = ? " +
                       "WHERE appointment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            if (counselorsId != null) {
                stmt.setInt(2, counselorsId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, appointmentTitle);
            stmt.setString(4, appointmentType);
            stmt.setTimestamp(5, appointmentDateTime);
            stmt.setString(6, appointmentNotes);
            stmt.setString(7, appointmentStatus);
            stmt.setInt(8, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
        }
        return false;
    }

    // DELETE (Remove an appointment)
    public boolean deleteAppointment(int appointmentId) {
        String query = "DELETE FROM APPOINTMENTS WHERE appointment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
        }
        return false;
    }

    // Retrieve appointments for a specific date
    public List<Appointment> getAppointmentsForDate(Connection connection, LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM APPOINTMENTS WHERE DATE(appointment_date_time) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("participant_id"),
                        rs.getObject("counselors_id", Integer.class),
                        rs.getString("appointment_title"),
                        rs.getString("appointment_type"),
                        rs.getTimestamp("appointment_date_time"),
                        rs.getString("appointment_status"),
                        rs.getTimestamp("updated_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments for date: " + e.getMessage());
        }
        return appointments;
    }

    // Add a new appointment
    public boolean addAppointment(Connection connection, int participantId, Integer counselorsId, String appointmentTitle,
                                  String appointmentType, Timestamp appointmentDateTime, String appointmentNotes,
                                  String appointmentStatus) {
        String query = "INSERT INTO APPOINTMENTS (participant_id, counselors_id, appointment_title, appointment_type, " +
                       "appointment_date_time, appointment_notes, appointment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            if (counselorsId != null) {
                stmt.setInt(2, counselorsId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, appointmentTitle);
            stmt.setString(4, appointmentType);
            stmt.setTimestamp(5, appointmentDateTime);
            stmt.setString(6, appointmentNotes);
            stmt.setString(7, appointmentStatus);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding appointment: " + e.getMessage());
        }
        return false;
    }

    // Update an existing appointment
    public boolean updateAppointment(Connection connection, Appointment appointment) {
        String query = "UPDATE APPOINTMENTS SET participant_id = ?, counselors_id = ?, appointment_title = ?, " +
                       "appointment_type = ?, appointment_date_time = ?, appointment_notes = ?, appointment_status = ? " +
                       "WHERE appointment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointment.getParticipantId());
            if (appointment.getGuidanceCounselorId() != null) {
                stmt.setInt(2, appointment.getGuidanceCounselorId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, appointment.getAppointmentTitle());
            stmt.setString(4, appointment.getAppointmentType());
            stmt.setTimestamp(5, appointment.getAppointmentDateTime());
            stmt.setString(6, appointment.getAppointmentNotes());
            stmt.setString(7, appointment.getAppointmentStatus());
            stmt.setInt(8, appointment.getAppointmentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
        }
        return false;
    }
}