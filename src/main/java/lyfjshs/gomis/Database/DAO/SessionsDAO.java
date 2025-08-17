/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;

public class SessionsDAO {

    private Connection connection;

    public SessionsDAO(Connection connection) {
        this.connection = connection;
    }

    // Method to check if an appointment exists
    private boolean appointmentExists(int appointmentId) throws SQLException {
        String sql = "SELECT 1 FROM APPOINTMENTS WHERE APPOINTMENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if at least one row exists
            }
        }
    }

    // ✅ Add session without participantId (use SESSIONS_PARTICIPANTS instead)
    public int addSession(Sessions session) throws SQLException {
        String sql = "INSERT INTO SESSIONS (APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, VIOLATION_ID, " +
                "APPOINTMENT_TYPE, CONSULTATION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_SUMMARY, SESSION_STATUS, UPDATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, session.getAppointmentId());
            stmt.setInt(2, session.getGuidanceCounselorId());
            stmt.setObject(3, session.getViolationId());
            stmt.setString(4, session.getAppointmentType());
            stmt.setString(5, session.getConsultationType());
            stmt.setTimestamp(6, session.getSessionDateTime());
            stmt.setString(7, session.getSessionNotes());
            
            // Properly handle session summary
            String summary = session.getSessionSummary();
            if (summary != null && !summary.trim().isEmpty()) {
                stmt.setString(8, summary);
            } else {
                stmt.setNull(8, java.sql.Types.VARCHAR);
            }
            
            stmt.setString(9, session.getSessionStatus());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        }
    }

    // Method to get all sessions
    public List<Sessions> getAllSessions() throws SQLException {
        List<Sessions> sessions = new ArrayList<>();
        String sql = "SELECT * FROM SESSIONS";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sessions.add(mapRowToSession(rs));
            }
        }
        return sessions;
    }

    // Method to get a session by ID
    public Sessions getSessionById(int id) throws SQLException {
        String sql = "SELECT * FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sessions session = mapRowToSession(rs);
                    // Ensure summary is never null
                    if (session.getSessionSummary() == null) {
                        session.setSessionSummary("");
                    }
                    return session;
                }
            }
        }
        return null; // Return null if no session is found with the given ID
    }

    // ✅ Get session with participants
    public Sessions getSessionWithParticipants(int sessionId) throws SQLException {
        Sessions session = null;
        String sql = "SELECT s.*, " +
                    "(SELECT COUNT(*) FROM SESSIONS_PARTICIPANTS sp WHERE sp.SESSION_ID = s.SESSION_ID) as participant_count " +
                    "FROM SESSIONS s WHERE s.SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    session = mapRowToSession(rs);
                    session.setParticipantCount(rs.getInt("participant_count"));
                }
            }
        }
        if (session != null) {
            session.setParticipants(getParticipantsBySessionId(sessionId));
        }
        return session;
    }

    // ✅ Add participant to a session
    public void addParticipantToSession(int sessionId, int participantId) throws SQLException {
        String sql = "INSERT INTO SESSIONS_PARTICIPANTS (SESSION_ID, PARTICIPANT_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, participantId);
            stmt.executeUpdate();
        }
    }

    // ✅ Map ResultSet to Session
    private Sessions mapRowToSession(ResultSet rs) throws SQLException {
        String sessionSummary = rs.getString("SESSION_SUMMARY");
        // Ensure summary is never null
        if (sessionSummary == null) {
            sessionSummary = "";
        }
        
        Sessions session = new Sessions(
                rs.getInt("SESSION_ID"),
                rs.getObject("APPOINTMENT_ID", Integer.class),
                rs.getInt("GUIDANCE_COUNSELOR_ID"),
                rs.getObject("VIOLATION_ID", Integer.class),
                rs.getString("APPOINTMENT_TYPE"),
                rs.getString("CONSULTATION_TYPE"),
                rs.getTimestamp("SESSION_DATE_TIME"),
                rs.getString("SESSION_NOTES"),
                sessionSummary,
                rs.getString("SESSION_STATUS"),
                rs.getTimestamp("UPDATED_AT"));
        return session;
    }

    // Update session
    public boolean updateSession(Sessions session) throws SQLException {
        String sql = "UPDATE SESSIONS SET " +
                    "APPOINTMENT_ID = ?, " +
                    "GUIDANCE_COUNSELOR_ID = ?, " +
                    "VIOLATION_ID = ?, " +
                    "APPOINTMENT_TYPE = ?, " +
                    "CONSULTATION_TYPE = ?, " +
                    "SESSION_DATE_TIME = ?, " +
                    "SESSION_NOTES = ?, " +
                    "SESSION_SUMMARY = ?, " +
                    "SESSION_STATUS = ?, " +
                    "UPDATED_AT = CURRENT_TIMESTAMP " +
                    "WHERE SESSION_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, session.getAppointmentId());
            pstmt.setInt(2, session.getGuidanceCounselorId());
            pstmt.setObject(3, session.getViolationId());
            pstmt.setString(4, session.getAppointmentType());
            pstmt.setString(5, session.getConsultationType());
            pstmt.setTimestamp(6, session.getSessionDateTime());
            pstmt.setString(7, session.getSessionNotes());
            pstmt.setString(8, session.getSessionSummary());
            pstmt.setString(9, session.getSessionStatus());
            pstmt.setInt(10, session.getSessionId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public int createSession(Sessions session) throws SQLException {
        String sql = "INSERT INTO SESSIONS (" +
                    "APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, VIOLATION_ID, " +
                    "APPOINTMENT_TYPE, CONSULTATION_TYPE, SESSION_DATE_TIME, " +
                    "SESSION_NOTES, SESSION_SUMMARY, SESSION_STATUS, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setObject(1, session.getAppointmentId());
            pstmt.setInt(2, session.getGuidanceCounselorId());
            pstmt.setObject(3, session.getViolationId());
            pstmt.setString(4, session.getAppointmentType());
            pstmt.setString(5, session.getConsultationType());
            pstmt.setTimestamp(6, session.getSessionDateTime());
            pstmt.setString(7, session.getSessionNotes());
            pstmt.setString(8, session.getSessionSummary());
            pstmt.setString(9, session.getSessionStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    // New method for inserting a session with participants
    public boolean insertSession(Sessions session, List<Integer> participantIds, Integer appointmentId) throws SQLException {
        String sessionSql = "INSERT INTO SESSIONS (APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, VIOLATION_ID, " +
                            "APPOINTMENT_TYPE, CONSULTATION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_SUMMARY, SESSION_STATUS, UPDATED_AT) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        try (PreparedStatement sessionStmt = connection.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS)) {
            sessionStmt.setObject(1, appointmentId); // Use provided appointmentId
            sessionStmt.setInt(2, session.getGuidanceCounselorId());
            sessionStmt.setObject(3, session.getViolationId());
            sessionStmt.setString(4, session.getAppointmentType());
            sessionStmt.setString(5, session.getConsultationType());
            sessionStmt.setTimestamp(6, session.getSessionDateTime());
            sessionStmt.setString(7, session.getSessionNotes());
            sessionStmt.setString(8, session.getSessionSummary());
            sessionStmt.setString(9, session.getSessionStatus());

            int affectedRows = sessionStmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = sessionStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int newSessionId = generatedKeys.getInt(1);
                    session.setSessionId(newSessionId); // Set the generated ID back to the session object

                    // Link participants to the new session
                    if (participantIds != null && !participantIds.isEmpty()) {
                        addParticipantsToSession(newSessionId, participantIds);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // New method to remove all participants from a session
    public void removeParticipantsFromSession(int sessionId) throws SQLException {
        String sql = "DELETE FROM SESSIONS_PARTICIPANTS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        }
    }

    // New method to add multiple participants to a session
    public void addParticipantsToSession(int sessionId, List<Integer> participantIds) throws SQLException {
        String sql = "INSERT INTO SESSIONS_PARTICIPANTS (SESSION_ID, PARTICIPANT_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Integer participantId : participantIds) {
                stmt.setInt(1, sessionId);
                stmt.setInt(2, participantId);
                stmt.addBatch(); // Add to batch for efficiency
            }
            stmt.executeBatch(); // Execute all inserts
        }
    }

    // Delete session
    public void deleteSession(int sessionId) throws SQLException {
        String sql = "DELETE FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        }
    }

    // New method to get session data with participant count
    public List<Sessions> getSessionDataWithParticipantCount() throws SQLException {
        List<Sessions> sessionDataList = new ArrayList<>();
        String sql = "SELECT s.*, " +
                "(SELECT COUNT(*) FROM SESSIONS_PARTICIPANTS sp WHERE sp.SESSION_ID = s.SESSION_ID) as participant_count "
                +
                "FROM SESSIONS s " +
                "ORDER BY s.SESSION_ID";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Sessions sessionData = mapRowToSession(rs);
                sessionData.setParticipantCount(rs.getInt("participant_count"));
                sessionDataList.add(sessionData);
            }
        }
        return sessionDataList;
    }

    // Add this method to get participants count for a specific session
    public int getParticipantsCount(int sessionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SESSIONS_PARTICIPANTS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public GuidanceCounselor getCounselorById(int counselorId) throws SQLException {
        String sql = "SELECT * FROM GUIDANCE_COUNSELORS WHERE GUIDANCE_COUNSELOR_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, counselorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new GuidanceCounselor(
                            rs.getInt("GUIDANCE_COUNSELOR_ID"),
                            rs.getString("LAST_NAME"),
                            rs.getString("FIRST_NAME"),
                            rs.getString("MIDDLE_NAME"),
                            rs.getString("SUFFIX"),
                            rs.getString("GENDER"),
                            rs.getString("SPECIALIZATION"),
                            rs.getString("CONTACT_NUM"),
                            rs.getString("EMAIL"),
                            rs.getString("POSITION"),
                            rs.getBytes("PROFILE_PICTURE"));
                }
            }
        }
        return null;
    }

    public boolean participantExists(int participantId) throws SQLException {
        String query = "SELECT COUNT(*) FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Participants> getParticipantsBySessionId(int sessionId) throws SQLException {
        List<Participants> participants = new ArrayList<>();
        String sql = "SELECT p.* FROM PARTICIPANTS p " +
                     "JOIN SESSIONS_PARTICIPANTS sp ON p.PARTICIPANT_ID = sp.PARTICIPANT_ID " +
                     "WHERE sp.SESSION_ID = ?";
                     
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Participants participant = new Participants(
                        rs.getObject("STUDENT_UID", Integer.class),
                        rs.getString("PARTICIPANT_TYPE"),
                        rs.getString("PARTICIPANT_LASTNAME"),
                        rs.getString("PARTICIPANT_FIRSTNAME"),
                        rs.getString("PARTICIPANT_SEX"),
                        rs.getString("CONTACT_NUMBER")
                    );
                    participant.setParticipantId(rs.getInt("PARTICIPANT_ID"));
                    participants.add(participant);
                }
            }
        }
        return participants;
    }

    public void deleteSessionsByAppointmentId(int appointmentId) throws SQLException {
        // First, delete the session participants
        String deleteParticipantsSQL = "DELETE sp FROM SESSIONS_PARTICIPANTS sp " +
                                      "INNER JOIN SESSIONS s ON sp.SESSION_ID = s.SESSION_ID " +
                                      "WHERE s.APPOINTMENT_ID = ?";
        
        // Then delete the sessions
        String deleteSessionsSQL = "DELETE FROM SESSIONS WHERE APPOINTMENT_ID = ?";
        
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            // Delete participants first
            try (PreparedStatement stmt = connection.prepareStatement(deleteParticipantsSQL)) {
                stmt.setInt(1, appointmentId);
                stmt.executeUpdate();
            }
            
            // Then delete sessions
            try (PreparedStatement stmt = connection.prepareStatement(deleteSessionsSQL)) {
                stmt.setInt(1, appointmentId);
                stmt.executeUpdate();
            }
            
            // Commit transaction
            connection.commit();
        } catch (SQLException e) {
            // Rollback on error
            connection.rollback();
            throw e;
        } finally {
            // Reset auto-commit
            connection.setAutoCommit(true);
        }
    }

    /**
     * Gets all sessions related to a specific violation
     * 
     * @param violationId The ID of the violation
     * @return List of sessions related to the violation
     * @throws SQLException If there is a database error
     */
    public List<Sessions> getSessionsByViolationId(int violationId) throws SQLException {
        List<Sessions> sessions = new ArrayList<>();
        String sql = "SELECT * FROM SESSIONS WHERE VIOLATION_ID = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sessions session = new Sessions(
                        rs.getInt("SESSION_ID"),
                        rs.getObject("APPOINTMENT_ID", Integer.class),
                        rs.getInt("GUIDANCE_COUNSELOR_ID"),
                        rs.getObject("VIOLATION_ID", Integer.class),
                        rs.getString("APPOINTMENT_TYPE"),
                        rs.getString("CONSULTATION_TYPE"),
                        rs.getTimestamp("SESSION_DATE_TIME"),
                        rs.getString("SESSION_NOTES"),
                        rs.getString("SESSION_SUMMARY"),
                        rs.getString("SESSION_STATUS"),
                        rs.getTimestamp("UPDATED_AT")
                    );
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    /**
     * Gets all sessions for a specific participant
     * 
     * @param participantId The ID of the participant
     * @return List of sessions the participant was involved in
     * @throws SQLException If there is a database error
     */
    public List<Sessions> getSessionsByParticipantId(int participantId) throws SQLException {
        List<Sessions> sessions = new ArrayList<>();
        String sql = "SELECT s.* FROM SESSIONS s " +
                    "JOIN SESSIONS_PARTICIPANTS sp ON s.SESSION_ID = sp.SESSION_ID " +
                    "WHERE sp.PARTICIPANT_ID = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sessions session = new Sessions(
                        rs.getInt("SESSION_ID"),
                        rs.getObject("APPOINTMENT_ID", Integer.class),
                        rs.getInt("GUIDANCE_COUNSELOR_ID"),
                        rs.getObject("VIOLATION_ID", Integer.class),
                        rs.getString("APPOINTMENT_TYPE"),
                        rs.getString("CONSULTATION_TYPE"),
                        rs.getTimestamp("SESSION_DATE_TIME"),
                        rs.getString("SESSION_NOTES"),
                        rs.getString("SESSION_SUMMARY"),
                        rs.getString("SESSION_STATUS"),
                        rs.getTimestamp("UPDATED_AT")
                    );
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    /**
     * Saves a new session with participants and updates appointment status
     * 
     * @param session The session to save
     * @param participantIds List of participant IDs to add to the session
     * @param appointmentId The appointment ID to update (can be null)
     * @return true if successful, false otherwise
     * @throws SQLException If there is a database error
     */
    public boolean saveSessionWithParticipants(Sessions session, List<Integer> participantIds, Integer appointmentId) throws SQLException {
        try {
            // Start transaction
            connection.setAutoCommit(false);
            
            try {
                // Insert session record
                int sessionId = addSession(session);
                if (sessionId == 0) {
                    throw new SQLException("Failed to create session");
                }
                
                // Add participants
                if (participantIds != null && !participantIds.isEmpty()) {
                    for (Integer participantId : participantIds) {
                        // Validate participant exists before adding
                        if (!participantExists(participantId)) {
                            throw new SQLException("Participant with ID " + participantId + " does not exist");
                        }
                        addParticipantToSession(sessionId, participantId);
                    }
                }
                
                // Update appointment status if appointmentId is provided
                if (appointmentId != null) {
                    String updateAppointmentQuery = "UPDATE APPOINTMENTS SET APPOINTMENT_STATUS = ? WHERE APPOINTMENT_ID = ?";
                    try (PreparedStatement stmt = connection.prepareStatement(updateAppointmentQuery)) {
                        stmt.setString(1, "Completed");
                        stmt.setInt(2, appointmentId);
                        stmt.executeUpdate();
                    }
                }
                
                // Commit transaction
                connection.commit();
                return true;
                
            } catch (SQLException e) {
                // Rollback transaction on error
                connection.rollback();
                throw e;
            }
            
        } finally {
            // Reset auto-commit
            connection.setAutoCommit(true);
        }
    }

    public List<Sessions> getSessionsByAppointmentId(Integer appointmentId) throws SQLException {
        List<Sessions> sessions = new ArrayList<>();
        String query = "SELECT * FROM sessions WHERE appointment_id = ? ORDER BY session_date_time DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Sessions session = new Sessions();
                session.setSessionId(rs.getInt("SESSION_ID"));
                session.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                session.setGuidanceCounselorId(rs.getInt("GUIDANCE_COUNSELOR_ID"));
                session.setViolationId(rs.getInt("VIOLATION_ID"));
                session.setAppointmentType(rs.getString("APPOINTMENT_TYPE"));
                session.setConsultationType(rs.getString("CONSULTATION_TYPE"));
                session.setSessionDateTime(rs.getTimestamp("SESSION_DATE_TIME"));
                session.setSessionNotes(rs.getString("SESSION_NOTES"));
                session.setSessionSummary(rs.getString("SESSION_SUMMARY"));
                session.setSessionStatus(rs.getString("SESSION_STATUS"));
                session.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
                sessions.add(session);
            }
        }
        
        return sessions;
    }

    // New method to remove a participant from a session
    public void removeParticipantFromSession(int sessionId, int participantId) throws SQLException {
        String sql = "DELETE FROM SESSIONS_PARTICIPANTS WHERE SESSION_ID = ? AND PARTICIPANT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, participantId);
            stmt.executeUpdate();
        }
    }
}
