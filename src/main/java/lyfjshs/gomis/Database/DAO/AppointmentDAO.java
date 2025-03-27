package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.Date;
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

    public AppointmentDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ Insert a new appointment and return its ID
    public int insertAppointment(Integer guidanceCounselorId, String appointmentTitle, String consultationType,
            Timestamp appointmentDateTime, String appointmentNotes, String appointmentStatus,
            List<Integer> participantIds)
            throws SQLException {
        String query = "INSERT INTO APPOINTMENTS (guidance_counselor_id, appointment_title, CONSULTATION_TYPE, " +
                "APPOINTMENT_DATE_TIME, appointment_notes, appointment_status, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, guidanceCounselorId); // Nullable FK
            stmt.setString(2, appointmentTitle);
            stmt.setString(3, consultationType);
            stmt.setTimestamp(4, appointmentDateTime);
            stmt.setString(5, appointmentNotes);
            stmt.setString(6, appointmentStatus);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int appointmentId = generatedKeys.getInt(1);
                        addParticipantsToAppointment(appointmentId, participantIds);
                        return appointmentId;
                    }
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Inserting Appointment");
        }
        return 0;
    }

    // ✅ Add multiple participants to an appointment
    public void addParticipantsToAppointment(int appointmentId, List<Integer> participantIds) throws SQLException {
        String query = "INSERT INTO APPOINTMENT_PARTICIPANTS (appointment_id, participant_id) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int participantId : participantIds) {
                stmt.setInt(1, appointmentId);
                stmt.setInt(2, participantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Adding participants to appointment");
            throw e;
        }
    }

    // ✅ Get appointment by ID (with participants)
    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        String query = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.appointment_id = ap.appointment_id " +
                "LEFT JOIN PARTICIPANTS p ON ap.participant_id = p.participant_id " +
                "WHERE a.appointment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            return fetchAppointmentWithParticipants(stmt);
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching Appointment by ID");
            throw e;
        }
    }

    public List<Appointment> searchAppointments(String title, String consultationType, LocalDate startDate, LocalDate endDate, String status, Integer counselorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT a.* FROM APPOINTMENTS a WHERE 1=1"); // Base query
    
        List<Object> params = new ArrayList<>();
    
        if (title != null && !title.trim().isEmpty()) {
            query.append(" AND a.APPOINTMENT_TITLE LIKE ?");
            params.add("%" + title + "%"); // Search using LIKE for partial match
        }
        if (consultationType != null && !consultationType.trim().isEmpty()) {
            query.append(" AND a.CONSULTATION_TYPE = ?");
            params.add(consultationType);
        }
        if (startDate != null) {
            query.append(" AND DATE(a.APPOINTMENT_DATE_TIME) >= ?");
            params.add(java.sql.Date.valueOf(startDate));
        }
        if (endDate != null) {
            query.append(" AND DATE(a.APPOINTMENT_DATE_TIME) <= ?");
            params.add(java.sql.Date.valueOf(endDate));
        }
        if (status != null && !status.trim().isEmpty()) {
            query.append(" AND a.APPOINTMENT_STATUS = ?");
            params.add(status);
        }
        if (counselorId != null) {
            query.append(" AND a.GUIDANCE_COUNSELOR_ID = ?");
            params.add(counselorId);
        }
    
        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return appointments;
    }    

    // ✅ Get all appointments
    public List<Appointment> getAllAppointments() throws SQLException {
        String query = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.appointment_id = ap.appointment_id " +
                "LEFT JOIN PARTICIPANTS p ON ap.participant_id = p.participant_id " +
                "ORDER BY a.APPOINTMENT_DATE_TIME";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            return fetchAppointmentsWithParticipants(stmt);
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching All Appointments");
            throw e;
        }
    }

    public List<Appointment> searchAppointmentsByParticipant(String participantName) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT DISTINCT a.* FROM APPOINTMENTS a " +
                       "JOIN APPOINTMENT_PARTICIPANTS ap ON a.APPOINTMENT_ID = ap.APPOINTMENT_ID " +
                       "JOIN PARTICIPANTS p ON ap.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                       "WHERE CONCAT(p.PARTICIPANT_FIRSTNAME, ' ', p.PARTICIPANT_LASTNAME) LIKE ?";
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + participantName + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return appointments;
    }    

    // ✅ Get appointments by date (reuses fetch logic)
    public List<Appointment> getAppointmentsForDate(LocalDate date) throws SQLException {
        String query = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.appointment_id = ap.appointment_id " +
                "LEFT JOIN PARTICIPANTS p ON ap.participant_id = p.participant_id " +
                "WHERE DATE(a.APPOINTMENT_DATE_TIME) = ? " +
                "ORDER BY a.APPOINTMENT_DATE_TIME";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(date));
            return fetchAppointmentsWithParticipants(stmt);
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching Appointments for Date");
            throw e;
        }
    }

    // ✅ Update an appointment (includes participant handling)
    public boolean updateAppointment(Appointment appointment) throws SQLException {
        String query = "UPDATE APPOINTMENTS SET guidance_counselor_id = ?, appointment_title = ?, " +
                "CONSULTATION_TYPE = ?, APPOINTMENT_DATE_TIME = ?, appointment_notes = ?, appointment_status = ?, updated_at = NOW() "
                +
                "WHERE appointment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, appointment.getGuidanceCounselorId());
            stmt.setString(2, appointment.getAppointmentTitle());
            stmt.setString(3, appointment.getConsultationType());
            stmt.setTimestamp(4, appointment.getAppointmentDateTime());
            stmt.setString(5, appointment.getAppointmentNotes());
            stmt.setString(6, appointment.getAppointmentStatus());
            stmt.setInt(7, appointment.getAppointmentId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                updateAppointmentParticipants(appointment.getAppointmentId(), appointment.getParticipants());
            }
            
            return affectedRows > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Updating Appointment");
            throw e;
        }
    }

    // ✅ Update participants for an appointment
    private void updateAppointmentParticipants(int appointmentId, List<Participants> newParticipants)
            throws SQLException {
        List<Integer> existingParticipantIds = getExistingParticipantsForAppointment(appointmentId);

        List<Integer> newParticipantIds = new ArrayList<>();
        for (Participants participant : newParticipants) {
            newParticipantIds.add(participant.getParticipantId());
        }

        // Determine which participants need to be removed
        List<Integer> participantsToRemove = new ArrayList<>(existingParticipantIds);
        participantsToRemove.removeAll(newParticipantIds);

        // Determine which participants need to be added
        List<Integer> participantsToAdd = new ArrayList<>(newParticipantIds);
        participantsToAdd.removeAll(existingParticipantIds);

        // Remove old participants
        if (!participantsToRemove.isEmpty()) {
            removeParticipantsFromAppointment(appointmentId, participantsToRemove);
        }

        // Add new participants
        if (!participantsToAdd.isEmpty()) {
            addParticipantsToAppointment(appointmentId, participantsToAdd);
        }
    }

    // ✅ Fetch existing participant IDs for an appointment
    private List<Integer> getExistingParticipantsForAppointment(int appointmentId) throws SQLException {
        List<Integer> participantIds = new ArrayList<>();
        String query = "SELECT participant_id FROM APPOINTMENT_PARTICIPANTS WHERE appointment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participantIds.add(rs.getInt("participant_id"));
                }
            }
        }
        return participantIds;
    }

    // ✅ Remove participants from an appointment
    private void removeParticipantsFromAppointment(int appointmentId, List<Integer> participantIds)
            throws SQLException {
        String query = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE appointment_id = ? AND participant_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (int participantId : participantIds) {
                stmt.setInt(1, appointmentId);
                stmt.setInt(2, participantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // ✅ Delete appointment
    public boolean deleteAppointment(int appointmentId) throws SQLException {
        String query = "DELETE FROM APPOINTMENTS WHERE appointment_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Deleting Appointment");
            throw e;
        }
    }

    // 🟢 Utility Methods
    private List<Appointment> fetchAppointmentsWithParticipants(PreparedStatement stmt) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        try (ResultSet rs = stmt.executeQuery()) {
            int currentId = -1;
            Appointment currentAppointment = null;
            while (rs.next()) {
                int appointmentId = rs.getInt("appointment_id");
                if (currentId != appointmentId) {
                    currentId = appointmentId;
                    currentAppointment = mapResultSetToAppointment(rs);
                    currentAppointment.setParticipants(new ArrayList<>());
                    appointments.add(currentAppointment);
                }
                addParticipantToAppointmentList(rs, currentAppointment);
            }
        }
        return appointments;
    }

    private Appointment fetchAppointmentWithParticipants(PreparedStatement stmt) throws SQLException {
        Appointment appointment = null;
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                if (appointment == null) {
                    appointment = mapResultSetToAppointment(rs);
                    appointment.setParticipants(new ArrayList<>());
                }
                addParticipantToAppointmentList(rs, appointment);
            }
        }
        return appointment;
    }

    private void addParticipantToAppointmentList(ResultSet rs, Appointment appointment) throws SQLException {
        if (rs.getObject("participant_id") != null) {
            Participants participant = new Participants(
                rs.getObject("student_uid", Integer.class),
                rs.getString("participant_type"),
                rs.getString("participant_lastname"),
                rs.getString("participant_firstname"),
                rs.getString("participant_sex"),
                rs.getString("contact_number")
            );
            participant.setParticipantId(rs.getInt("participant_id"));
            appointment.getParticipants().add(participant);
        }
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        return new Appointment(rs.getInt("appointment_id"), rs.getObject("guidance_counselor_id", Integer.class),
                rs.getString("appointment_title"), rs.getString("CONSULTATION_TYPE"),
                rs.getTimestamp("APPOINTMENT_DATE_TIME"), rs.getString("appointment_status"),
                rs.getString("appointment_notes"), rs.getTimestamp("updated_at"));
    }

    // Get today's appointments
    public List<Appointment> getTodayAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENTS WHERE DATE(APPOINTMENT_DATE_TIME) = CURDATE() " +
                    "AND APPOINTMENT_STATUS = 'Active' ORDER BY APPOINTMENT_DATE_TIME";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Appointment appointment = mapResultSetToAppointment(rs);
                appointments.add(appointment);
            }
        }
        return appointments;
    }
}
