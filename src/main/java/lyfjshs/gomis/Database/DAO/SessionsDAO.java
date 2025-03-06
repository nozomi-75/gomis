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

    // Method to check if an appointment exists
    private boolean appointmentExists(int appointmentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM APPOINTMENTS WHERE APPOINTMENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Method to add a session
    public void addSession(Sessions session) throws SQLException {
        if (!"walk-in".equalsIgnoreCase(session.getSessionType()) &&
                !"individual counseling".equalsIgnoreCase(session.getSessionType()) &&
                session.getAppointmentId() != 0 && !appointmentExists(session.getAppointmentId())) {
            throw new SQLException("Appointment ID does not exist.");
        }

        String sql = "INSERT INTO SESSIONS (APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, PARTICIPANT_ID, VIOLATION_ID, SESSION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS, UPDATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if ("walk-in".equalsIgnoreCase(session.getSessionType())
                    || "individual counseling".equalsIgnoreCase(session.getSessionType())) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else if (session.getAppointmentId() == 0) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, session.getAppointmentId());
            }

            stmt.setInt(2, session.getGuidanceCounselorId());
            stmt.setInt(3, session.getParticipantId());

            // Handle null violationId
            if (session.getViolationId() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, session.getViolationId());
            }

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

    // Method to get a session by ID
    public Sessions getSessionById(int id) throws SQLException {
        String sql = "SELECT * FROM SESSIONS WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSession(rs);
                }
            }
        }
        return null; // Return null if no session is found with the given ID
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
                rs.getInt("GUIDANCE_COUNSELOR_ID"),
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
        String sql = "UPDATE SESSIONS SET APPOINTMENT_ID = ?, GUIDANCE_COUNSELOR_ID = ?, PARTICIPANT_ID = ?, VIOLATION_ID = ?, SESSION_TYPE = ?, SESSION_DATE_TIME = ?, SESSION_NOTES = ?, SESSION_STATUS = ?, UPDATED_AT = ? WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if ("walk-in".equalsIgnoreCase(session.getSessionType())
                    || "individual counseling".equalsIgnoreCase(session.getSessionType())) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, session.getAppointmentId());
            }

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
                + "COALESCE(a.APPOINTMENT_DATE_TIME, NULL) AS APPOINTMENT_DATE_TIME, s.SESSION_STATUS, s.UPDATED_AT "
                + "FROM SESSIONS s "
                + "LEFT JOIN SESSIONS_PARTICIPANTS sp ON s.SESSION_ID = sp.SESSION_ID "
                + "LEFT JOIN APPOINTMENTS a ON s.APPOINTMENT_ID = a.APPOINTMENT_ID "
                + "GROUP BY s.SESSION_ID, s.SESSION_TYPE, a.APPOINTMENT_DATE_TIME, s.SESSION_STATUS, s.UPDATED_AT";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Sessions session = new Sessions(
                        rs.getInt("SESSION_ID"),
                        0, 0, 0, 0, // Ignored foreign keys
                        rs.getString("SESSION_TYPE"),
                        null,
                        null,
                        rs.getString("SESSION_STATUS"),
                        rs.getTimestamp("UPDATED_AT"));

                session.setParticipantCount(rs.getInt("Participants"));
                session.setAppointmentDateTime(rs.getTimestamp("APPOINTMENT_DATE_TIME"));
                sessions.add(session);
            }
        }
        return sessions;
    }
}
