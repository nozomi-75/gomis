package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.Session;
import lyfjshs.gomis.Database.model.SessionParticipant;

public class SessionsDAO {

    private Connection connection;

    public SessionsDAO(Connection connection) {
        this.connection = connection;
    }

    // Method to add a session
    public void addSession(Session session) throws SQLException {
        String sql = "INSERT INTO SESSIONS (SESSION_ID,APPOINTMENT_ID, COUNSELORS_ID, PARTICIPANT_ID, VIOLATION_ID, SESSION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS, UPDATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters
            stmt.setInt(1, session.getSessionId());
            stmt.setInt(2, session.getAppointmentId());
            stmt.setInt(3, session.getCounselorsId());
            stmt.setInt(4, session.getParticipantId());
            stmt.setInt(5, session.getViolationId());
            stmt.setString(6, session.getSessionType());
            stmt.setTimestamp(7, session.getSessionDateTime());
            stmt.setString(8, session.getSessionNotes());
            stmt.setString(9, session.getSessionStatus());
            stmt.setTimestamp(10, session.getUpdatedAt());
            stmt.executeUpdate();
        }
    }

    // Method to get all sessions
    public List<Session> getAllSessions() throws SQLException {
        List<Session> sessions = new ArrayList<>();
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
    private Session mapRowToSession(ResultSet rs) throws SQLException {
        return new Session(
                rs.getInt("SESSION_ID"),
                rs.getInt("APPOINTMENT_ID"),
                rs.getInt("COUNSELORS_ID"),
                rs.getInt("PARTICIPANT_ID"),
                rs.getInt("VIOLATION_ID"),
                rs.getString("SESSION_TYPE"),
                rs.getTimestamp("SESSION_DATE_TIME"),
                rs.getString("SESSION_NOTES"),
                rs.getString("SESSION_STATUS"),
                rs.getTimestamp("UPDATED_AT"));
    }

//update session
    public void updateSession(Session session) throws SQLException {
        String sql = "UPDATE SESSIONS SET APPOINTMENT_ID = ?, COUNSELORS_ID = ?, PARTICIPANT_ID = ?, VIOLATION_ID = ?, SESSION_TYPE = ?, SESSION_DATE_TIME = ?, SESSION_NOTES = ?, SESSION_STATUS = ?, UPDATED_AT = ? WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters
            stmt.setInt(1, session.getAppointmentId());
            stmt.setInt(2, session.getCounselorsId());
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


//delete session 

public void deleteSession(int sessionId) throws SQLException {
    String sql = "DELETE FROM SESSIONS WHERE SESSION_ID = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, sessionId);
        stmt.executeUpdate();
    }
}

}