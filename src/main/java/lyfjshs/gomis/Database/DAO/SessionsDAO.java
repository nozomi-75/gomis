package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.SessionParticipant;
import lyfjshs.gomis.Database.entity.Sessions;

public class SessionsDAO {

    private Connection connection;

    public SessionsDAO(Connection connection) {
        this.connection = connection;
    }

    // Method to add a session
    public void addSession(Sessions session) throws SQLException {
        String sql = "INSERT INTO SESSIONS (APPOINTMENT_ID, guidance_counselor_id, PARTICIPANT_ID, VIOLATION_ID, SESSION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS, UPDATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, session.getAppointmentId());
            stmt.setInt(2, session.getGuidanceCounselorId());
            stmt.setInt(3, session.getParticipantId());
            stmt.setInt(4, session.getViolationId());
            stmt.setString(5, session.getSessionType());
            stmt.setTimestamp(6, session.getSessionDateTime());
            stmt.setString(7, session.getSessionNotes());
            stmt.setString(8, session.getSessionStatus());
            stmt.setTimestamp(9, session.getUpdatedAt());
            stmt.executeUpdate();
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

    // Method to add a participant to a session
    public void addParticipantToSession(SessionParticipant participant) throws SQLException {
        String sql = "INSERT INTO SESSIONS_PARTICIPANTS (SESSION_ID, PARTICIPANT_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participant.getSessionId());
            stmt.setInt(2, participant.getParticipantId());
            stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet to Session object
    private Sessions mapRowToSession(ResultSet rs) throws SQLException {
        return new Sessions(
                rs.getInt("SESSION_ID"),
                rs.getInt("APPOINTMENT_ID"),
                rs.getInt("guidance_counselor_id"),
                rs.getInt("PARTICIPANT_ID"),
                rs.getInt("VIOLATION_ID"),
                rs.getString("SESSION_TYPE"),
                rs.getTimestamp("SESSION_DATE_TIME"),
                rs.getString("SESSION_NOTES"),
                rs.getString("SESSION_STATUS"),
                rs.getTimestamp("UPDATED_AT"));
    }

    // update session
    public void updateSession(Sessions session) throws SQLException {
        String sql = "UPDATE SESSIONS SET APPOINTMENT_ID = ?, guidance_counselor_id = ?, PARTICIPANT_ID = ?, VIOLATION_ID = ?, SESSION_TYPE = ?, SESSION_DATE_TIME = ?, SESSION_NOTES = ?, SESSION_STATUS = ?, UPDATED_AT = ? WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters
            stmt.setInt(1, session.getAppointmentId());
            stmt.setInt(2, session.getGuidanceCounselorId());
            stmt.setInt(3, session.getParticipantId());
            stmt.setInt(4, session.getViolationId());
            stmt.setString(5, session.getSessionType());
            stmt.setTimestamp(6, session.getSessionDateTime());
            stmt.setString(7, session.getSessionNotes());
            stmt.setString(8, session.getSessionStatus());
            stmt.setTimestamp(9, session.getUpdatedAt());
            stmt.setInt(10, session.getSessionId()); // WHERE condition
            stmt.executeUpdate();
        }
    }

    // delete session

    public void deleteSession(int sessionId) throws SQLException {
        String sql = "DELETE FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        }
    }

    // New method to get session data with participant count
    public List<Sessions> getSessionDataWithParticipantCount() throws SQLException {
        List<Sessions> sessions = new ArrayList<>();
        String sql = "SELECT s.SESSION_ID, COUNT(sp.PARTICIPANT_ID) AS Participants, s.SESSION_TYPE, "
                + "a.APPOINTMENT_DATE_TIME, s.SESSION_STATUS, s.UPDATED_AT "
                + "FROM SESSIONS s "
                + "LEFT JOIN sessions_participants sp ON s.SESSION_ID = sp.SESSION_ID "
                + "LEFT JOIN APPOINTMENTS a ON s.APPOINTMENT_ID = a.APPOINTMENT_ID "
                + "GROUP BY s.SESSION_ID";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Sessions session = new Sessions(
                        rs.getInt("SESSION_ID"),
                        0, // appointmentId (not retrieved in the query)
                        0, // counselorsId (not retrieved in the query)
                        0, // participantId (not retrieved in the query)
                        0, // violationId (not retrieved in the query)
                        rs.getString("SESSION_TYPE"),
                        null, // sessionDateTime (not retrieved in the query)
                        null, // sessionNotes (not retrieved in the query)
                        rs.getString("SESSION_STATUS"),
                        rs.getTimestamp("UPDATED_AT"));
                session.setParticipantCount(rs.getInt("Participants")); // Set participant count
                session.setAppointmentDateTime(rs.getTimestamp("APPOINTMENT_DATE_TIME")); // Set appointment date time
                sessions.add(session);
            }
        }
        return sessions;
    }

    // New method to get a session by ID
    public Sessions getSessionById(int sessionId) throws SQLException {
        String sql = "SELECT * FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Sessions(
                        rs.getInt("SESSION_ID"),
                        rs.getInt("APPOINTMENT_ID"),
                        rs.getInt("guidance_counselor_id"),
                        rs.getInt("PARTICIPANT_ID"),
                        rs.getInt("VIOLATION_ID"),
                        rs.getString("SESSION_TYPE"),
                        rs.getTimestamp("SESSION_DATE_TIME"),
                        rs.getString("SESSION_NOTES"),
                        rs.getString("SESSION_STATUS"),
                        rs.getTimestamp("UPDATED_AT"));
            }
        }
        return null; // Return null if no session found
    }

}