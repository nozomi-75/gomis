package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;

/* CREATE TABLE PARTICIPANTS (
    PARTICIPANT_ID INT PRIMARY KEY AUTO_INCREMENT,
    STUDENT_UID INT NULL,
    PARTICIPANT_TYPE VARCHAR(50),
    PARTICIPANT_LASTNAME VARCHAR(100),
    PARTICIPANT_FIRSTNAME VARCHAR(100),
    EMAIL VARCHAR(100),
    CONTACT_NUMBER VARCHAR(20),
    FOREIGN KEY (STUDENT_UID) REFERENCES STUDENT (STUDENT_UID)
); */
public class ParticipantsDAO {
    private final Connection connection;

    public ParticipantsDAO(Connection connection) {
            if (connection == null) {
        throw new IllegalArgumentException("❌ Database connection is null. Ensure DBConnection.getConnection() is called properly.");
    }
        this.connection = connection;
    }

    
    // Create a new participant
    public void createParticipant(Participants participant) throws SQLException {
    String sql = "INSERT INTO PARTICIPANTS (STUDENT_UID, PARTICIPANT_TYPE, PARTICIPANT_LASTNAME, " +
            "PARTICIPANT_FIRSTNAME, EMAIL, CONTACT_NUMBER) VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setObject(1, participant.getStudentUid());
        stmt.setString(2, participant.getParticipantType());
        stmt.setString(3, participant.getParticipantLastName());
        stmt.setString(4, participant.getParticipantFirstName());
        stmt.setString(5, participant.getEmail());
        stmt.setString(6, participant.getContactNumber());

        int affectedRows = stmt.executeUpdate();

        if (affectedRows > 0) {
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participant.setParticipantId(generatedKeys.getInt(1));
                    System.out.println("✔ Participant created with ID: " + participant.getParticipantId());
                }
            }
        } else {
            System.err.println("❌ Creating participant failed, no rows affected.");
        }
    }
}

    // Retrieve a participant by ID
    public Participants getParticipantById(int participantId) {
        String sql = "SELECT * FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParticipant(rs);
                } else {
                    System.err.println("❌ No participant found with ID: " + participantId);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getParticipantById: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Update an existing participant
    public void updateParticipant(Participants participant) throws SQLException {
        String sql = "UPDATE PARTICIPANTS SET STUDENT_UID = ?, PARTICIPANT_TYPE = ?, PARTICIPANT_LASTNAME = ?, " +
                "PARTICIPANT_FIRSTNAME = ?, EMAIL = ?, CONTACT_NUMBER = ? WHERE PARTICIPANT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participant.getStudentUid());
            stmt.setString(2, participant.getParticipantType());
            stmt.setString(3, participant.getParticipantLastName());
            stmt.setString(4, participant.getParticipantFirstName());
            stmt.setString(5, participant.getEmail());
            stmt.setString(6, participant.getContactNumber());
            stmt.setInt(7, participant.getParticipantId());
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
        participant.setEmail(rs.getString("EMAIL"));
        participant.setContactNumber(rs.getString("CONTACT_NUMBER"));
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
}
