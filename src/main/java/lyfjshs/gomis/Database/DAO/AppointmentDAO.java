package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;

public class AppointmentDAO {
    private static final Logger logger = LogManager.getLogger(AppointmentDAO.class);
    private final Connection connection;

    public AppointmentDAO() throws SQLException {
        this.connection = DBConnection.getConnection();
    }

    public AppointmentDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new appointment with participants
    public boolean insertAppointment(Appointment appointment) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // First insert the appointment
            String sql = "INSERT INTO APPOINTMENTS (GUIDANCE_COUNSELOR_ID, APPOINTMENT_TITLE, CONSULTATION_TYPE, " +
                    "APPOINTMENT_DATE_TIME, APPOINTMENT_STATUS, APPOINTMENT_NOTES, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

            int appointmentId;
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, appointment.getGuidanceCounselorId());
                stmt.setString(2, appointment.getAppointmentTitle());
                stmt.setString(3, appointment.getConsultationType());
                stmt.setTimestamp(4, appointment.getAppointmentDateTime());
                stmt.setString(5, appointment.getAppointmentStatus());
                stmt.setString(6, appointment.getAppointmentNotes());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        appointmentId = generatedKeys.getInt(1);
                        appointment.setAppointmentId(appointmentId); // Set the generated ID back to the appointment object
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            // Clear existing participant relationships first
            String deleteSql = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE APPOINTMENT_ID = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, appointmentId);
                deleteStmt.executeUpdate();
            }

            // Then insert the participant relationships, checking for duplicates
            if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
                String checkSql = "SELECT COUNT(*) FROM APPOINTMENT_PARTICIPANTS WHERE APPOINTMENT_ID = ? AND PARTICIPANT_ID = ?";
                String insertSql = "INSERT INTO APPOINTMENT_PARTICIPANTS (APPOINTMENT_ID, PARTICIPANT_ID) VALUES (?, ?)";
                
                try (PreparedStatement checkStmt = connection.prepareStatement(checkSql);
                     PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    
                    for (Participants participant : appointment.getParticipants()) {
                        // Check if relationship already exists
                        checkStmt.setInt(1, appointmentId);
                        checkStmt.setInt(2, participant.getParticipantId());
                        ResultSet rs = checkStmt.executeQuery();
                        rs.next();
                        int count = rs.getInt(1);
                        
                        // Only insert if relationship doesn't exist
                        if (count == 0) {
                            insertStmt.setInt(1, appointmentId);
                            insertStmt.setInt(2, participant.getParticipantId());
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // Add participants to an appointment
    public void addParticipantsToAppointment(int appointmentId, List<Integer> participantIds) throws SQLException {
        boolean wasAutoCommit = connection.getAutoCommit();
        try {
            if (wasAutoCommit) {
                connection.setAutoCommit(false);
            }

            // First delete existing participants
            String deleteSql = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE APPOINTMENT_ID = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, appointmentId);
                deleteStmt.executeUpdate();
            }

            // Then add new participants
            if (!participantIds.isEmpty()) {
                String insertSql = "INSERT INTO APPOINTMENT_PARTICIPANTS (APPOINTMENT_ID, PARTICIPANT_ID) VALUES (?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    for (Integer participantId : participantIds) {
                        insertStmt.setInt(1, appointmentId);
                        insertStmt.setInt(2, participantId);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

            if (wasAutoCommit) {
                connection.commit();
            }
        } catch (SQLException e) {
            if (wasAutoCommit) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (wasAutoCommit) {
                connection.setAutoCommit(true);
            }
        }
    }

    // Get appointment by ID with participants
    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE APPOINTMENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    // Fetch and set participants
                    appointment.setParticipants(getParticipantsForAppointment(appointmentId));
                    return appointment;
                }
            }
        }
        return null;
    }

    // Get appointments for a specific date
    public List<Appointment> getAppointmentsForDate(LocalDate date) throws SQLException {
        String sql = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                    "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.APPOINTMENT_ID = ap.APPOINTMENT_ID " +
                    "LEFT JOIN PARTICIPANTS p ON ap.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE DATE(a.APPOINTMENT_DATE_TIME) = ? " +
                    "ORDER BY a.APPOINTMENT_DATE_TIME";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            return fetchAppointmentsWithParticipants(stmt);
        }
    }

    // Get appointments for a specific date excluding ended ones
    public List<Appointment> getAppointmentsForDateExcludingEnded(LocalDate date) throws SQLException {
        String sql = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                    "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.APPOINTMENT_ID = ap.APPOINTMENT_ID " +
                    "LEFT JOIN PARTICIPANTS p ON ap.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE DATE(a.APPOINTMENT_DATE_TIME) = ? " +
                    "AND a.APPOINTMENT_STATUS = 'Active' " +
                    "ORDER BY a.APPOINTMENT_DATE_TIME";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(date));
            return fetchAppointmentsWithParticipants(stmt);
        }
    }

    // Get a single appointment excluding ended ones
    public Appointment getAppointmentByIdExcludingEnded(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE APPOINTMENT_ID = ? AND APPOINTMENT_STATUS NOT IN ('Ended', 'Cancelled')";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    // Fetch and set participants
                    List<Participants> participants = getParticipantsForAppointment(appointmentId);
                    appointment.setParticipants(participants);
                    return appointment;
                }
            }
        }
        return null;
    }

    // Update appointment
    public boolean updateAppointment(Appointment appointment) throws SQLException {
        try {
            connection.setAutoCommit(false);
            
            // Use parameterized query for better security and performance
            String sql = "UPDATE APPOINTMENTS SET GUIDANCE_COUNSELOR_ID = ?, APPOINTMENT_TITLE = ?, " +
                        "APPOINTMENT_DATE_TIME = ?, CONSULTATION_TYPE = ?, APPOINTMENT_NOTES = ?, " +
                        "APPOINTMENT_STATUS = ?, UPDATED_AT = CURRENT_TIMESTAMP " +
                        "WHERE APPOINTMENT_ID = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, appointment.getGuidanceCounselorId());
                stmt.setString(2, appointment.getAppointmentTitle());
                stmt.setTimestamp(3, appointment.getAppointmentDateTime());
                stmt.setString(4, appointment.getConsultationType());
                stmt.setString(5, appointment.getAppointmentNotes());
                stmt.setString(6, appointment.getAppointmentStatus());
                stmt.setInt(7, appointment.getAppointmentId());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    connection.commit();
                    logger.debug("Appointment updated successfully. Affected rows: {}", affectedRows);
                    return true;
                } else {
                    connection.rollback();
                    logger.warn("No rows affected when updating appointment ID: {}", appointment.getAppointmentId());
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
            }
            logger.error("Error updating appointment: {}", e.getMessage(), e);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error resetting auto-commit: {}", e.getMessage(), e);
            }
        }
    }

    // Update appointment status
    public boolean updateAppointmentStatus(Integer appointmentId, String status) throws SQLException {
        String sql = "UPDATE APPOINTMENTS SET APPOINTMENT_STATUS = ?, UPDATED_AT = CURRENT_TIMESTAMP WHERE APPOINTMENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, appointmentId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Delete appointment
    public boolean deleteAppointment(int appointmentId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // First, delete any associated sessions
            SessionsDAO sessionsDAO = new SessionsDAO(connection);
            sessionsDAO.deleteSessionsByAppointmentId(appointmentId);

            // Then, delete participants related to this appointment
            String deleteParticipantsSql = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE APPOINTMENT_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteParticipantsSql)) {
                stmt.setInt(1, appointmentId);
                stmt.executeUpdate();
            }

            // Then delete the appointment itself
            String deleteAppointmentSql = "DELETE FROM APPOINTMENTS WHERE APPOINTMENT_ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteAppointmentSql)) {
                stmt.setInt(1, appointmentId);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // Helper methods
    private List<Appointment> fetchAppointmentsWithParticipants(PreparedStatement stmt) throws SQLException {
        Map<Integer, Appointment> appointmentMap = new HashMap<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Appointment appointment = mapResultSetToAppointment(rs);
                appointmentMap.put(appointment.getAppointmentId(), appointment);
            }
        }

        // Fetch participants for all appointments
        for (Appointment appointment : appointmentMap.values()) {
            appointment.setParticipants(getParticipantsForAppointment(appointment.getAppointmentId()));
        }

        return new ArrayList<>(appointmentMap.values());
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
        appointment.setGuidanceCounselorId(rs.getInt("GUIDANCE_COUNSELOR_ID"));
        appointment.setAppointmentTitle(rs.getString("APPOINTMENT_TITLE"));
        appointment.setConsultationType(rs.getString("CONSULTATION_TYPE"));
        appointment.setAppointmentDateTime(rs.getTimestamp("APPOINTMENT_DATE_TIME"));
        appointment.setAppointmentStatus(rs.getString("APPOINTMENT_STATUS"));
        appointment.setAppointmentNotes(rs.getString("APPOINTMENT_NOTES"));
        appointment.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return appointment;
    }

    public List<Participants> getParticipantsForAppointment(int appointmentId) throws SQLException {
        String sql = "SELECT p.* FROM PARTICIPANTS p " +
                    "INNER JOIN APPOINTMENT_PARTICIPANTS ap ON p.PARTICIPANT_ID = ap.PARTICIPANT_ID " +
                    "WHERE ap.APPOINTMENT_ID = ?";
        List<Participants> participants = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Participants participant = new Participants();
                    participant.setParticipantId(rs.getInt("PARTICIPANT_ID"));
                    participant.setStudentUid(rs.getObject("STUDENT_UID", Integer.class));
                    participant.setParticipantType(rs.getString("PARTICIPANT_TYPE"));
                    participant.setParticipantFirstName(rs.getString("PARTICIPANT_FIRSTNAME"));
                    participant.setParticipantLastName(rs.getString("PARTICIPANT_LASTNAME"));
                    participant.setSex(rs.getString("PARTICIPANT_SEX"));
                    participant.setContactNumber(rs.getString("CONTACT_NUMBER"));
                    participants.add(participant);
                }
            }
        }
        return participants;
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
        String sql = "SELECT * FROM APPOINTMENTS ORDER BY APPOINTMENT_DATE_TIME DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            return fetchAppointmentsWithParticipants(stmt);
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

    // Get today's appointments
    public List<Appointment> getTodayAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM APPOINTMENTS WHERE DATE(APPOINTMENT_DATE_TIME) = CURDATE() " +
                    "AND APPOINTMENT_STATUS = 'Active' ORDER BY APPOINTMENT_DATE_TIME";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            return fetchAppointmentsWithParticipants(stmt);
        }
    }

    /**
     * Gets all upcoming appointments that haven't happened yet.
     * 
     * @return List of upcoming appointments
     * @throws SQLException if a database error occurs
     */
    public List<Appointment> getUpcomingAppointments() throws SQLException {
        String sql = "SELECT a.*, p.* FROM APPOINTMENTS a " +
                    "LEFT JOIN APPOINTMENT_PARTICIPANTS ap ON a.APPOINTMENT_ID = ap.APPOINTMENT_ID " +
                    "LEFT JOIN PARTICIPANTS p ON ap.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE a.APPOINTMENT_DATE_TIME > NOW() AND a.APPOINTMENT_STATUS = 'Active' " +
                    "ORDER BY a.APPOINTMENT_DATE_TIME ASC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            return fetchAppointmentsWithParticipants(stmt);
        }
    }

    public List<Appointment> getAppointmentsByStatus(String status) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql;
        
        if ("all".equalsIgnoreCase(status)) {
            sql = "SELECT * FROM APPOINTMENTS WHERE APPOINTMENT_STATUS IN ('Ended', 'Cancelled', 'Missed') ORDER BY APPOINTMENT_DATE_TIME DESC";
        } else {
            sql = "SELECT * FROM APPOINTMENTS WHERE APPOINTMENT_STATUS = ? ORDER BY APPOINTMENT_DATE_TIME DESC";
        }
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (!"all".equalsIgnoreCase(status)) {
                stmt.setString(1, status);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    // Load participants for this appointment
                    appointment.setParticipants(getParticipantsForAppointment(appointment.getAppointmentId()));
                    appointments.add(appointment);
                }
            }
        }
        
        return appointments;
    }

    public List<Appointment> getAppointmentsByGuidanceCounselor(int guidanceCounselorId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE GUIDANCE_COUNSELOR_ID = ? ORDER BY APPOINTMENT_DATE_TIME DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, guidanceCounselorId);
            return fetchAppointmentsWithParticipants(stmt);
        }
    }

    public List<Appointment> getAppointmentsByParticipant(String participantName) throws SQLException {
        String sql = "SELECT a.* FROM APPOINTMENTS a " +
                     "JOIN APPOINTMENT_PARTICIPANTS ap ON a.APPOINTMENT_ID = ap.APPOINTMENT_ID " +
                     "JOIN PARTICIPANTS p ON ap.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                     "WHERE CONCAT(p.PARTICIPANT_FIRSTNAME, ' ', p.PARTICIPANT_LASTNAME) = ?";
        List<Appointment> appointments = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, participantName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(mapResultSetToAppointment(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Checks for and updates appointments that should be marked as missed.
     * This method finds appointments that are in the past and still marked as Scheduled, Rescheduled, or Active,
     * and updates their status to Missed.
     * 
     * @return The number of appointments that were marked as missed
     * @throws SQLException if a database error occurs
     */
    public int checkAndUpdateMissedAppointments() throws SQLException {
        int updatedCount = 0;
        
        // Find appointments that are in the past, still scheduled, rescheduled, or active
        String query = "SELECT * FROM APPOINTMENTS WHERE APPOINTMENT_DATE_TIME < ? " +
                       "AND (APPOINTMENT_STATUS = 'Scheduled' OR APPOINTMENT_STATUS = 'Rescheduled' OR APPOINTMENT_STATUS = 'Active')";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setTimestamp(1, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            
            try (ResultSet rs = pst.executeQuery()) {
                // Update query to mark appointments as missed
                String updateQuery = "UPDATE APPOINTMENTS SET APPOINTMENT_STATUS = 'Missed' WHERE APPOINTMENT_ID = ?";
                
                try (PreparedStatement updatePst = connection.prepareStatement(updateQuery)) {
                    while (rs.next()) {
                        int appointmentId = rs.getInt("APPOINTMENT_ID");
                        updatePst.setInt(1, appointmentId);
                        updatePst.executeUpdate();
                        updatedCount++;
                    }
                }
            }
        }
        
        return updatedCount;
    }

    /**
     * Gets appointments that are about to be marked as missed (within the 2-minute grace period)
     * @return List of appointments in their grace period
     */
    public List<Appointment> getAppointmentsInGracePeriod() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        
        // Find appointments that are 0-2 minutes past their scheduled time and still active
        String query = "SELECT * FROM APPOINTMENTS WHERE " +
                      "APPOINTMENT_DATE_TIME BETWEEN DATE_SUB(NOW(), INTERVAL 2 MINUTE) AND NOW() " +
                      "AND (APPOINTMENT_STATUS = 'Scheduled' OR APPOINTMENT_STATUS = 'Rescheduled')";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Appointment appointment = mapResultSetToAppointment(rs);
                    appointment.setParticipants(getParticipantsForAppointment(appointment.getAppointmentId()));
                    appointments.add(appointment);
                }
            }
        }
        
        return appointments;
    }

    /**
     * Checks for and updates appointment statuses, with a 2-minute grace period
     * @return The number of appointments that were marked as missed
     */
    public int checkAndUpdateMissedWithoutSessions() throws SQLException {
        int updatedCount = 0;
        
        // Get appointments that are more than 2 minutes past their scheduled time
        String query = "SELECT * FROM APPOINTMENTS WHERE " +
                      "APPOINTMENT_DATE_TIME < DATE_SUB(NOW(), INTERVAL 2 MINUTE) " +
                      "AND (APPOINTMENT_STATUS = 'Scheduled' OR APPOINTMENT_STATUS = 'Rescheduled' OR APPOINTMENT_STATUS = 'Active')";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int appointmentId = rs.getInt("APPOINTMENT_ID");
                    
                    // Check if there's a session associated with this appointment
                    boolean hasSession = false;
                    try (PreparedStatement sessionCheck = connection.prepareStatement(
                            "SELECT COUNT(*) FROM SESSIONS WHERE APPOINTMENT_ID = ?")) {
                        sessionCheck.setInt(1, appointmentId);
                        ResultSet sessionRs = sessionCheck.executeQuery();
                        if (sessionRs.next() && sessionRs.getInt(1) > 0) {
                            hasSession = true;
                        }
                    }
                    
                    // Only mark as missed if no session is associated
                    if (!hasSession) {
                        try (PreparedStatement updateStmt = connection.prepareStatement(
                                "UPDATE APPOINTMENTS SET APPOINTMENT_STATUS = 'Missed' WHERE APPOINTMENT_ID = ?")) {
                            updateStmt.setInt(1, appointmentId);
                            updateStmt.executeUpdate();
                            updatedCount++;
                        }
                    }
                }
            }
        }
        
        return updatedCount;
    }

    /**
     * Deletes appointment history older than 10 days
     * Only deletes appointments with status 'Completed', 'Cancelled', or 'Missed'
     */
    public void cleanupOldAppointments() throws SQLException {
        String query = "DELETE FROM APPOINTMENTS WHERE " +
                      "APPOINTMENT_STATUS IN ('Completed', 'Cancelled', 'Missed') " +
                      "AND APPOINTMENT_DATE_TIME < DATE_SUB(NOW(), INTERVAL 10 DAY)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int deletedCount = stmt.executeUpdate();
            if (deletedCount > 0) {
                logger.info("Deleted {} old appointments", deletedCount);
            }
        }
    }

    // New method to remove a participant from an appointment
    public void removeParticipantFromAppointment(int appointmentId, int participantId) throws SQLException {
        String sql = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE APPOINTMENT_ID = ? AND PARTICIPANT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            stmt.setInt(2, participantId);
            stmt.executeUpdate();
        }
    }
}


