package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.SQLExceptionPane;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;

public class AppointmentDAO {
    private final Connection connection;

    // Constructor to initialize the database connection
    public AppointmentDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE: Insert a new appointment and return the generated ID
    public int insertAppointment(Integer guidanceCounselorId, String appointmentTitle,
            String appointmentType, Timestamp appointmentDateTime, String appointmentNotes,
            String appointmentStatus, List<Integer> participantIds) throws SQLException {
        String query = "INSERT INTO APPOINTMENTS (guidance_counselor_id, appointment_title, appointment_type, " +
                "APPOINTMENT_DATE_TIME, appointment_notes, appointment_status, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, guidanceCounselorId); // Nullable guidanceCounselorId
            stmt.setString(2, appointmentTitle);
            stmt.setString(3, appointmentType);
            stmt.setTimestamp(4, appointmentDateTime);
            stmt.setString(5, appointmentNotes);
            stmt.setString(6, appointmentStatus);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int appointmentId = generatedKeys.getInt(1);
                        // Add participants to the junction table
                        if (participantIds != null) {
                            for (int participantId : participantIds) {
                                addParticipantToAppointment(appointmentId, participantId);
                            }
                        }
                        return appointmentId; // Return the generated appointment ID
                    }
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Inserting Appointment");
        }
        return 0;
    }

    // Helper method to add participants to the junction table
    private void addParticipantToAppointment(int appointmentId, int participantId) throws SQLException {
        String query = "INSERT INTO APPOINTMENT_PARTICIPANTS (appointment_id, participant_id) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            stmt.setInt(2, participantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e,
                    "Adding participant to appointment (appointment_id=" + appointmentId + ")");
            throw e; // Re-throw to handle in the calling method if needed
        }
    }

    // READ: Retrieve an appointment by its unique ID with all participants
    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        String query = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.appointment_id = ap.appointment_id " +
                "LEFT JOIN PARTICIPANTS p ON ap.participant_id = p.participant_id " +
                "WHERE a.appointment_id = ?";
        Appointment appointment = null;
        List<Participants> participants = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (appointment == null) {
                        appointment = mapResultSetToAppointment(rs);
                    }
                    if (rs.getObject("participant_id") != null) {
                        Participants participant = new Participants();
                        participant.setParticipantId(rs.getInt("participant_id"));
                        participant.setStudentUid(rs.getInt("student_uid"));
                        participant.setParticipantType(rs.getString("participant_type"));
                        participant.setParticipantLastName(rs.getString("participant_lastname"));
                        participant.setParticipantFirstName(rs.getString("participant_firstname"));
                        participant.setEmail(rs.getString("email"));
                        participant.setContactNumber(rs.getString("contact_number"));
                        participants.add(participant);
                    }
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching Appointment by ID (appointment_id=" + appointmentId + ")");
            throw e; // Re-throw to handle in the calling method if needed
        }
        if (appointment != null) {
            appointment.setParticipants(participants);
        }
        return appointment;
    }

    // READ ALL: Retrieve a list of all appointments with their participants
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.appointment_id = ap.appointment_id " +
                "LEFT JOIN PARTICIPANTS p ON ap.participant_id = p.participant_id";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            int currentAppointmentId = -1;
            Appointment currentAppointment = null;
            while (rs.next()) {
                int appointmentId = rs.getInt("appointment_id");
                if (currentAppointmentId != appointmentId) {
                    currentAppointmentId = appointmentId;
                    currentAppointment = mapResultSetToAppointment(rs);
                    currentAppointment.setParticipants(new ArrayList<>());
                    appointments.add(currentAppointment);
                }
                if (rs.getObject("participant_id") != null) {
                    Participants participant = new Participants();
                    participant.setParticipantId(rs.getInt("participant_id"));
                    participant.setStudentUid(rs.getInt("student_uid"));
                    participant.setParticipantType(rs.getString("participant_type"));
                    participant.setParticipantLastName(rs.getString("participant_lastname"));
                    participant.setParticipantFirstName(rs.getString("participant_firstname"));
                    participant.setEmail(rs.getString("email"));
                    participant.setContactNumber(rs.getString("contact_number"));
                    currentAppointment.getParticipants().add(participant);
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching All Appointments");
            throw e; // Re-throw to handle in the calling method if needed
        }
        return appointments;
    }

    // UPDATE: Modify an existing appointment (excluding participants for now)
    public boolean updateAppointment(Appointment appointment) throws SQLException {
        String query = "UPDATE APPOINTMENTS SET guidance_counselor_id = ?, appointment_title = ?, " +
                "appointment_type = ?, APPOINTMENT_DATE_TIME = ?, appointment_notes = ?, appointment_status = ?, updated_at = NOW() "
                +
                "WHERE appointment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, appointment.getGuidanceCounselorId());
            stmt.setString(2, appointment.getAppointmentTitle());
            stmt.setString(3, appointment.getAppointmentType());
            stmt.setTimestamp(4, appointment.getAppointmentDateTime());
            stmt.setString(5, appointment.getAppointmentNotes());
            stmt.setString(6, appointment.getAppointmentStatus());
            stmt.setInt(7, appointment.getAppointmentId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // Update participants if needed (e.g., delete old and insert new)
                updateAppointmentParticipants(appointment.getAppointmentId(), appointment.getParticipants());
            }
            return affectedRows > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e,
                    "Updating Appointment (appointment_id=" + appointment.getAppointmentId() + ")");
            throw e; // Re-throw to handle in the calling method if needed
        }
    }

    // Helper method to update participants in the junction table
    private void updateAppointmentParticipants(int appointmentId, List<Participants> participants) throws SQLException {
        // First, delete existing participants for this appointment
        String deleteQuery = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE appointment_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
        }

        // Then, insert new participants
        if (participants != null) {
            for (Participants p : participants) {
                addParticipantToAppointment(appointmentId, p.getParticipantId());
            }
        }
    }

    // DELETE: Remove an appointment and its participants
    public boolean deleteAppointment(int appointmentId) throws SQLException {
        String deleteParticipantsQuery = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE appointment_id = ?";
        String deleteAppointmentQuery = "DELETE FROM APPOINTMENTS WHERE appointment_id = ?";

        try (PreparedStatement stmt1 = connection.prepareStatement(deleteParticipantsQuery);
                PreparedStatement stmt2 = connection.prepareStatement(deleteAppointmentQuery)) {
            connection.setAutoCommit(false); // Start transaction
            stmt1.setInt(1, appointmentId);
            stmt1.executeUpdate();

            stmt2.setInt(1, appointmentId);
            boolean deleted = stmt2.executeUpdate() > 0;

            if (deleted) {
                connection.commit(); // Commit transaction
            } else {
                connection.rollback(); // Rollback if deletion fails
            }
            return deleted;
        } catch (SQLException e) {
            connection.rollback(); // Rollback on error
            SQLExceptionPane.showSQLException(e, "Deleting Appointment (appointment_id=" + appointmentId + ")");
            throw e; // Re-throw to handle in the calling method if needed
        } finally {
            connection.setAutoCommit(true); // Reset auto-commit
        }
    }

    // READ: Retrieve appointments scheduled for a specific date
    public List<Appointment> getAppointmentsForDate(LocalDate date) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.appointment_id = ap.appointment_id " +
                "LEFT JOIN PARTICIPANTS p ON ap.participant_id = p.participant_id " +
                "WHERE DATE(a.APPOINTMENT_DATE_TIME) = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                int currentAppointmentId = -1;
                Appointment currentAppointment = null;
                while (rs.next()) {
                    int appointmentId = rs.getInt("appointment_id");
                    if (currentAppointmentId != appointmentId) {
                        currentAppointmentId = appointmentId;
                        currentAppointment = mapResultSetToAppointment(rs);
                        currentAppointment.setParticipants(new ArrayList<>());
                        appointments.add(currentAppointment);
                    }
                    if (rs.getObject("participant_id") != null) {
                        Participants participant = new Participants();
                        participant.setParticipantId(rs.getInt("participant_id"));
                        participant.setStudentUid(rs.getInt("student_uid"));
                        participant.setParticipantType(rs.getString("participant_type"));
                        participant.setParticipantLastName(rs.getString("participant_lastname"));
                        participant.setParticipantFirstName(rs.getString("participant_firstname"));
                        participant.setEmail(rs.getString("email"));
                        participant.setContactNumber(rs.getString("contact_number"));
                        currentAppointment.getParticipants().add(participant);
                    }
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching Appointments for Date (date=" + date + ")");
            throw e; // Re-throw to handle in the calling method if needed
        }
        return appointments;
    }

    // Utility: Map a database result set to an Appointment object
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(
                rs.getInt("appointment_id"),
                rs.getObject("guidance_counselor_id", Integer.class), // Updated to match schema
                rs.getString("appointment_title"),
                rs.getString("appointment_type"),
                rs.getTimestamp("APPOINTMENT_DATE_TIME"),
                rs.getString("appointment_status"),
                rs.getString("appointment_notes"),
                rs.getTimestamp("updated_at"));
    }
}