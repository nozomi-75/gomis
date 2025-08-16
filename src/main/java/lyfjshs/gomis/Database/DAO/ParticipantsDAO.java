package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;

public class ParticipantsDAO {
    private static final Logger logger = LogManager.getLogger(ParticipantsDAO.class);
    private final Connection connection;

    public ParticipantsDAO(Connection connection) {
        this.connection = connection;
    }

    // Create participant
    public int createParticipant(Participants participant) throws SQLException {
        // Only check for existing participants if studentUid is not null
        if (participant.getStudentUid() != null) {
            List<Participants> existingParticipants = getParticipantByStudentUid(participant.getStudentUid());
            if (!existingParticipants.isEmpty()) {
                Participants existingParticipant = existingParticipants.get(0);
                logger.info("Using existing participant with ID: {}", existingParticipant.getParticipantId());
                return existingParticipant.getParticipantId();
            }
        } else {
            // For non-students, check by name and type
            Participants existing = findParticipantByNameAndType(
                participant.getParticipantFirstName(),
                participant.getParticipantLastName(),
                participant.getParticipantType()
            );
            if (existing != null) {
                logger.info("Using existing participant with ID: {}", existing.getParticipantId());
                return existing.getParticipantId();
            }
        }

        String sql = "INSERT INTO PARTICIPANTS (STUDENT_UID, PARTICIPANT_FIRSTNAME, PARTICIPANT_LASTNAME, " +
                    "PARTICIPANT_TYPE, PARTICIPANT_SEX, CONTACT_NUMBER, IS_REPORTER) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (participant.getStudentUid() != null) {
                stmt.setObject(1, participant.getStudentUid());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, participant.getParticipantFirstName());
            stmt.setString(3, participant.getParticipantLastName());
            stmt.setString(4, participant.getParticipantType());
            stmt.setString(5, participant.getSex());
            stmt.setString(6, participant.getContactNumber());
            stmt.setBoolean(7, participant.isReporter());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        participant.setParticipantId(rs.getInt(1));
                        logger.info("New participant created with ID: {}", participant.getParticipantId());
                        return participant.getParticipantId();
                    }
                }
            }
            logger.error("Creating participant failed, no rows affected.");
            return -1;
        }
    }

    // Get participant by ID
    public Participants getParticipantById(int participantId) throws SQLException {
        String sql = "SELECT * FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParticipant(rs);
                }
            }
        }
        logger.warn("No participant found with ID: {}", participantId);
        return null;
    }

    // Update an existing participant
    public void updateParticipant(Participants participant) throws SQLException {
        String sql = "UPDATE PARTICIPANTS SET STUDENT_UID = ?, PARTICIPANT_TYPE = ?, PARTICIPANT_LASTNAME = ?, " +
                "PARTICIPANT_FIRSTNAME = ?, PARTICIPANT_SEX = ?, CONTACT_NUMBER = ?, IS_REPORTER = ? WHERE PARTICIPANT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, participant.getStudentUid());
            stmt.setString(2, participant.getParticipantType());
            stmt.setString(3, participant.getParticipantLastName());
            stmt.setString(4, participant.getParticipantFirstName());
            stmt.setString(5, participant.getSex());
            stmt.setString(6, participant.getContactNumber());
            stmt.setBoolean(7, participant.isReporter());
            stmt.setInt(8, participant.getParticipantId());
            stmt.executeUpdate();
        }
    }

    // Delete a participant by ID
    public void deleteParticipant(int participantId) throws SQLException {
        String sql = "DELETE FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            stmt.executeUpdate();
        }
    }

    // Retrieve all participants
    public List<Participants> getAllParticipants() throws SQLException {
        List<Participants> participants = new ArrayList<>();
        String sql = "SELECT * FROM PARTICIPANTS";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                participants.add(mapResultSetToParticipant(rs));
            }
        }
        return participants;
    }

    // Helper method to map ResultSet to Participant object
    private Participants mapResultSetToParticipant(ResultSet rs) throws SQLException {
        Participants participant = new Participants();
        participant.setParticipantId(rs.getInt("PARTICIPANT_ID"));
        participant.setStudentUid(rs.getInt("STUDENT_UID"));
        participant.setParticipantType(rs.getString("PARTICIPANT_TYPE"));
        participant.setParticipantLastName(rs.getString("PARTICIPANT_LASTNAME"));
        participant.setParticipantFirstName(rs.getString("PARTICIPANT_FIRSTNAME"));
        participant.setSex(rs.getString("PARTICIPANT_SEX"));
        participant.setContactNumber(rs.getString("CONTACT_NUMBER"));
        participant.setReporter(rs.getBoolean("IS_REPORTER"));
        return participant;
    }

    // Retrieve participant with related student data
    public Participants getParticipantWithStudentData(int participantId) throws SQLException {
        Participants participant = getParticipantById(participantId);
        if (participant != null && participant.getStudentUid() != null) {
            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
            Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
            participant.setStudent(student);
        }
        return participant;
    }

    public List<Participants> getParticipantByStudentUid(int studentUid) throws SQLException {
        List<Participants> participants = new ArrayList<>();
        String sql = "SELECT * FROM participants WHERE student_uid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Participants participant = new Participants();
                participant.setParticipantId(rs.getInt("PARTICIPANT_ID"));
                participant.setStudentUid(rs.getInt("STUDENT_UID"));
                participant.setParticipantType(rs.getString("PARTICIPANT_TYPE"));
                participant.setParticipantFirstName(rs.getString("PARTICIPANT_FIRSTNAME"));
                participant.setParticipantLastName(rs.getString("PARTICIPANT_LASTNAME"));
                participant.setSex(rs.getString("PARTICIPANT_SEX"));
                participant.setContactNumber(rs.getString("CONTACT_NUMBER"));
                participant.setReporter(rs.getBoolean("IS_REPORTER"));
                participants.add(participant);
            }
        }
        return participants;
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
                    participant.setReporter(rs.getBoolean("IS_REPORTER"));
                    participants.add(participant);
                }
            }
        }
        return participants;
    }

    public Participants findParticipantByNameAndType(String firstName, String lastName, String participantType) throws SQLException {
        String sql = "SELECT * FROM PARTICIPANTS WHERE PARTICIPANT_FIRSTNAME = ? AND PARTICIPANT_LASTNAME = ? AND PARTICIPANT_TYPE = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, participantType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParticipant(rs);
                }
            }
        }
        return null;
    }
    
    /**
     * Gets a participant by their details
     * 
     * @param firstName The participant's first name
     * @param lastName The participant's last name
     * @param type The participant's type
     * @return The participant, or null if not found
     * @throws SQLException If there is a database error
     */
    public Participants getParticipantByDetails(String firstName, String lastName, String type) throws SQLException {
        return findParticipantByNameAndType(firstName, lastName, type);
    }

    /**
     * Gets a participant by their student ID
     * 
     * @param studentUid The student's UID to search for
     * @return The participant record if found, null otherwise
     * @throws SQLException If there is a database error
     */
    public Participants getParticipantByStudentId(Integer studentUid) throws SQLException {
        if (studentUid == null) {
            return null;
        }
        
        String sql = "SELECT * FROM PARTICIPANTS WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParticipant(rs);
                }
            }
        }
        return null;
    }

    /**
     * Removes all participants from a session
     * @param sessionId The ID of the session
     * @throws SQLException If there is a database error
     */
    public void removeAllParticipantsFromSession(int sessionId) throws SQLException {
        String sql = "DELETE FROM SESSIONS_PARTICIPANTS WHERE SESSION_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Counts how many sessions a participant is associated with.
     * @param participantId The ID of the participant.
     * @return The count of sessions.
     * @throws SQLException If a database error occurs.
     */
    public int countSessionAssociations(int participantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM SESSIONS_PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, participantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Counts how many appointments a participant is associated with.
     * @param participantId The ID of the participant.
     * @return The count of appointments.
     * @throws SQLException If a database error occurs.
     */
    public int countAppointmentAssociations(int participantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM APPOINTMENT_PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, participantId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Checks if a participant is associated with any session.
     * @param participantId The ID of the participant.
     * @return true if associated with any session, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean isAssociatedWithAnySession(int participantId) throws SQLException {
        return countSessionAssociations(participantId) > 0;
    }

    /**
     * Checks if a participant is associated with any appointment.
     * @param participantId The ID of the participant.
     * @return true if associated with any appointment, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean isAssociatedWithAnyAppointment(int participantId) throws SQLException {
        return countAppointmentAssociations(participantId) > 0;
    }
}
