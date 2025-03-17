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
    public int addSession(Sessions session) throws SQLException {
        String sql = "INSERT INTO SESSIONS (APPOINTMENT_ID, GUIDANCE_COUNSELOR_ID, PARTICIPANT_ID, VIOLATION_ID, APPOINTMENT_TYPE, CONSULTATION_TYPE, SESSION_DATE_TIME, SESSION_NOTES, SESSION_STATUS, UPDATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, session.getAppointmentId());
            stmt.setInt(2, session.getGuidanceCounselorId());
            stmt.setInt(3, session.getParticipantId());
            stmt.setInt(4, session.getViolationId());
            stmt.setString(5, session.getAppointmentType());
            stmt.setString(6, session.getConsultationType());
            stmt.setTimestamp(7, session.getSessionDateTime());
            stmt.setString(8, session.getSessionNotes());
            stmt.setString(9, session.getSessionStatus());
            stmt.setTimestamp(10, session.getUpdatedAt());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // Return the generated SESSION_ID
                }
            }
            return 0; // Return 0 if no ID was generated
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
                rs.getString("APPOINTMENT_TYPE"),
                rs.getString("CONSULTATION_TYPE"),
                rs.getTimestamp("SESSION_DATE_TIME"),
                rs.getString("SESSION_NOTES"),
                rs.getString("SESSION_STATUS"),
                rs.getTimestamp("UPDATED_AT"));
    }

    // Update session
    public void updateSession(Sessions session) throws SQLException {
        String sql = "UPDATE SESSIONS SET APPOINTMENT_ID = ?, GUIDANCE_COUNSELOR_ID = ?, PARTICIPANT_ID = ?, VIOLATION_ID = ?, APPOINTMENT_TYPE = ?, CONSULTATION_TYPE = ?, SESSION_DATE_TIME = ?, SESSION_NOTES = ?, SESSION_STATUS = ?, UPDATED_AT = ? WHERE SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (session.getAppointmentId() == 0) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, session.getAppointmentId());
            }

            stmt.setInt(2, session.getGuidanceCounselorId());
            stmt.setInt(3, session.getParticipantId());
            stmt.setInt(4, session.getViolationId());
            stmt.setString(5, session.getAppointmentType());
            stmt.setString(6, session.getConsultationType());
            stmt.setTimestamp(7, session.getSessionDateTime());
            stmt.setString(8, session.getSessionNotes());
            stmt.setString(9, session.getSessionStatus());
            stmt.setTimestamp(10, session.getUpdatedAt());
            stmt.setInt(11, session.getSessionId()); // WHERE condition
            stmt.executeUpdate();
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
        String sql = "SELECT s.SESSION_ID, s.APPOINTMENT_ID, s.GUIDANCE_COUNSELOR_ID, s.PARTICIPANT_ID, " +
                     "s.VIOLATION_ID, s.APPOINTMENT_TYPE, s.CONSULTATION_TYPE, s.SESSION_DATE_TIME, " +
                     "s.SESSION_NOTES, s.SESSION_STATUS, s.UPDATED_AT, COUNT(sp.PARTICIPANT_ID) AS participant_count " +
                     "FROM SESSIONS s " +
                     "LEFT JOIN SESSIONS_PARTICIPANTS sp ON s.SESSION_ID = sp.SESSION_ID " +
                     "GROUP BY s.SESSION_ID";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                Sessions sessionData = new Sessions();
                sessionData.setSessionId(rs.getInt("SESSION_ID"));
                sessionData.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                sessionData.setGuidanceCounselorId(rs.getInt("GUIDANCE_COUNSELOR_ID"));
                sessionData.setParticipantId(rs.getInt("PARTICIPANT_ID"));
                sessionData.setViolationId(rs.getInt("VIOLATION_ID"));
                sessionData.setAppointmentType(rs.getString("APPOINTMENT_TYPE")); // Ensure this matches the query
                sessionData.setConsultationType(rs.getString("CONSULTATION_TYPE"));
                sessionData.setSessionDateTime(rs.getTimestamp("SESSION_DATE_TIME"));
                sessionData.setSessionNotes(rs.getString("SESSION_NOTES"));
                sessionData.setSessionStatus(rs.getString("SESSION_STATUS"));
                sessionData.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
                sessionData.setParticipantCount(rs.getInt("participant_count"));
                sessionDataList.add(sessionData);
            }
        }
        return sessionDataList;
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
                        rs.getBytes("PROFILE_PICTURE")
                    );
                }
            }
        }
        return null;
    }

    public List<Participants> getParticipantsBySessionId(int sessionId) throws SQLException {
        List<Participants> participants = new ArrayList<>();
        String sql = "SELECT p.* FROM PARTICIPANTS p JOIN SESSIONS_PARTICIPANTS sp ON p.PARTICIPANT_ID = sp.PARTICIPANT_ID WHERE sp.SESSION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participants.add(new Participants(
                        rs.getInt("PARTICIPANT_ID"),
                        rs.getObject("STUDENT_UID", Integer.class), // Handle potential null values
                        rs.getString("PARTICIPANT_TYPE"),
                        rs.getString("PARTICIPANT_LASTNAME"),
                        rs.getString("PARTICIPANT_FIRSTNAME"),
                        rs.getString("EMAIL"),
                        rs.getString("CONTACT_NUMBER")
                    ));
                }
            }
        }
        return participants;
    }
}
