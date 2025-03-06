package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;

public class IncidentsDAO {
    private final Connection connection;    // Database connection instance

    // Constructor to initialize DAO with a database connection
    public IncidentsDAO(Connection connection) {
        this.connection = connection;
    }

     // Inserts a new incident into the database and returns the generated ID
    public int createIncident(Incident incident) throws SQLException {
        String sql = "INSERT INTO INCIDENTS (PARTICIPANT_ID, INCIDENT_DATE, " +
                "INCIDENT_DESCRIPTION, ACTION_TAKEN, RECOMMENDATION, STATUS, UPDATED_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, incident.getParticipantId());
            stmt.setTimestamp(2, incident.getIncidentDate());
            stmt.setString(3, incident.getIncidentDescription());
            stmt.setString(4, incident.getActionTaken());
            stmt.setString(5, incident.getRecommendation());
            stmt.setString(6, incident.getStatus());
            stmt.setTimestamp(7, incident.getUpdatedAt());

            stmt.executeUpdate();

            // Retrieves the generated ID of the inserted incident
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1; // Returns -1 if insertion failed
    }

    // Retrieves an incident by its ID
    public Incident getIncidentById(int incidentId) throws SQLException {
        String sql = "SELECT * FROM INCIDENTS WHERE INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToIncident(rs);
                }
            }
        }
        return null;    // Returns null if no incident is found
    }

     // Updates an existing incident in the database
    public boolean updateIncident(Incident incident) throws SQLException {
        String sql = "UPDATE INCIDENTS SET PARTICIPANT_ID = ?, INCIDENT_DATE = ?, " +
                "INCIDENT_DESCRIPTION = ?, ACTION_TAKEN = ?, RECOMMENDATION = ?, STATUS = ?, UPDATED_AT = ? " +
                "WHERE INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incident.getParticipantId());
            stmt.setTimestamp(2, incident.getIncidentDate());
            stmt.setString(3, incident.getIncidentDescription());
            stmt.setString(4, incident.getActionTaken());
            stmt.setString(5, incident.getRecommendation());
            stmt.setString(6, incident.getStatus());
            stmt.setTimestamp(7, incident.getUpdatedAt());
            stmt.setInt(8, incident.getIncidentId());

            return stmt.executeUpdate() > 0;    // Returns true if update was successful
        }
    }

    // Deletes an incident by its ID
    public boolean deleteIncident(int incidentId) throws SQLException {
        String sql = "DELETE FROM INCIDENTS WHERE INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            return stmt.executeUpdate() > 0;    // Returns true if deletion was successful
        }
    }

    // Retrieves all incidents from the database
    public List<Incident> getAllIncidents() throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENTS ORDER BY INCIDENT_DATE DESC";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }
        }
        return incidents;
    }

    // Retrieves incidents based on participant ID
    public List<Incident> getIncidentsByParticipant(int participantId) throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENTS WHERE PARTICIPANT_ID = ? ORDER BY INCIDENT_DATE DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incidents.add(mapResultSetToIncident(rs));
                }
            }
        }
        return incidents;
    }

    // Converts a ResultSet to an Incident object
    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        Incident incident = new Incident();
        incident.setIncidentId(rs.getInt("INCIDENT_ID"));
        incident.setParticipantId(rs.getInt("PARTICIPANT_ID"));
        incident.setIncidentDate(rs.getTimestamp("INCIDENT_DATE"));
        incident.setIncidentDescription(rs.getString("INCIDENT_DESCRIPTION"));
        incident.setActionTaken(rs.getString("ACTION_TAKEN"));
        incident.setRecommendation(rs.getString("RECOMMENDATION"));
        incident.setStatus(rs.getString("STATUS"));
        incident.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return incident;
    }

    // Retrieves an incident with related participant and student details
    public Incident getCompleteIncidentDetails(int incidentId) throws SQLException {
        Incident incident = getIncidentById(incidentId);
        if (incident == null) {
            return null;
        }

        // Fetches the participant details
        Participants participant = getParticipantById(incident.getParticipantId());
        incident.setParticipants(participant);

        // If the participant is a student, retrieves student details
        if (participant.getStudentUid() != null) {
            Student student = getStudentById(participant.getStudentUid());
            incident.setStudent(student);
        }

        return incident;
    }

    // Retrieve participant by ID
    private Participants getParticipantById(int participantId) throws SQLException {
        String sql = "SELECT * FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToParticipant(rs);
                }
            }
        }
        return null;
    }

    // Converts a ResultSet to a Participant object
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

    // Retrieves a student by ID
    private Student getStudentById(int studentUid) throws SQLException {
        String sql = "SELECT * FROM STUDENT WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        }
        return null;
    }

    // Converts a ResultSet to a Student object
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student(
            rs.getInt("STUDENT_UID"),
            rs.getInt("PARENT_ID"),
            rs.getInt("GUARDIAN_ID"),
            rs.getInt("ADDRESS_ID"),
            rs.getInt("CONTACT_ID"),
            rs.getString("STUDENT_LRN"),
            rs.getString("STUDENT_LASTNAME"),
            rs.getString("STUDENT_FIRSTNAME"),
            rs.getString("STUDENT_MIDDLENAME"),
            rs.getString("STUDENT_SEX"),
            rs.getDate("STUDENT_BIRTHDATE"),
            rs.getString("STUDENT_MOTHERTONGUE"),
            rs.getInt("STUDENT_AGE"),
            rs.getString("STUDENT_IP_TYPE"),
            rs.getString("STUDENT_RELIGION")
        );
        return student;
    }
}
