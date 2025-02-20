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

import lyfjshs.gomis.Database.model.Appointment;

public class AppointmentDAO {

    // Create - Add a new appointment
    public boolean addAppointment(Connection conn,
                                 int participantId,
                                 Integer counselorsId,
                                 String appointmentTitle,
                                 String appointmentType,
                                 Timestamp appointmentDateTime,
                                 String appointmentNotes,
                                 String appointmentStatus) {
        String query = "INSERT INTO APPOINTMENTS (participant_id, counselors_id, appointment_title, " +
                      "appointment_type, appointment_date_time, appointment_notes, appointment_status) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setInt(1, participantId);
            
            // Handle nullable counselorsId
            if (counselorsId != null) {
                preparedStatement.setInt(2, counselorsId);
            } else {
                preparedStatement.setNull(2, Types.INTEGER);
            }
            
            preparedStatement.setString(3, appointmentTitle);
            preparedStatement.setString(4, appointmentType);
            preparedStatement.setTimestamp(5, appointmentDateTime);
            preparedStatement.setString(6, appointmentNotes);
            preparedStatement.setString(7, appointmentStatus);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Appointment added successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Read - Get appointment by ID
    public Appointment getAppointmentById(Connection conn, int appointmentId) {
        String query = "SELECT * FROM APPOINTMENTS WHERE appointment_id = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, appointmentId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentId(rs.getInt("appointment_id"));
                    appointment.setParticipantId(rs.getInt("participant_id"));
                    appointment.setCounselorsId(rs.getObject("counselors_id", Integer.class));
                    appointment.setAppointmentTitle(rs.getString("appointment_title"));
                    appointment.setAppointmentType(rs.getString("appointment_type"));
                    appointment.setAppointmentDateTime(rs.getTimestamp("appointment_date_time").toLocalDateTime());
                    appointment.setAppointmentNotes(rs.getString("appointment_notes"));
                    appointment.setAppointmentStatus(rs.getString("appointment_status"));
                    appointment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return appointment;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointment: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Read - Get appointments for a specific date
    public List<Appointment> getAppointmentsForDate(Connection conn, LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM APPOINTMENTS WHERE DATE(appointment_date_time) = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentId(rs.getInt("appointment_id"));
                    appointment.setParticipantId(rs.getInt("participant_id"));
                    appointment.setCounselorsId(rs.getObject("counselors_id", Integer.class));
                    appointment.setAppointmentTitle(rs.getString("appointment_title"));
                    appointment.setAppointmentType(rs.getString("appointment_type"));
                    appointment.setAppointmentDateTime(rs.getTimestamp("appointment_date_time").toLocalDateTime());
                    appointment.setAppointmentNotes(rs.getString("appointment_notes"));
                    appointment.setAppointmentStatus(rs.getString("appointment_status"));
                    appointment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    appointments.add(appointment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving appointments for date: " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    // Update - Update an existing appointment
    public boolean updateAppointment(Connection conn, Appointment appointment) {
        String query = "UPDATE APPOINTMENTS SET participant_id = ?, counselors_id = ?, " +
                      "appointment_title = ?, appointment_type = ?, appointment_date_time = ?, " +
                      "appointment_notes = ?, appointment_status = ? WHERE appointment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, appointment.getParticipantId());
            
            if (appointment.getCounselorsId() != null) {
                ps.setInt(2, appointment.getCounselorsId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            
            ps.setString(3, appointment.getAppointmentTitle());
            ps.setString(4, appointment.getAppointmentType());
            ps.setTimestamp(5, Timestamp.valueOf(appointment.getAppointmentDateTime()));
            ps.setString(6, appointment.getAppointmentNotes());
            ps.setString(7, appointment.getAppointmentStatus());
            ps.setInt(8, appointment.getAppointmentId());

            int rowsAffected = ps.executeUpdate();
            System.out.println("Updated " + rowsAffected + " appointment record(s)");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Delete - Delete an appointment
    public boolean deleteAppointment(Connection conn, int appointmentId) {
        String query = "DELETE FROM APPOINTMENTS WHERE appointment_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, appointmentId);

            int rowsAffected = ps.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " appointment record(s)");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}