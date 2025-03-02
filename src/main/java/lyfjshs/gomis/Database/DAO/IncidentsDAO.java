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
    private final Connection connection;

    public IncidentsDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new incident
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

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1; // Indicate failure
    }

    // Retrieve an incident by ID
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
        return null;
    }

    // Update an existing incident
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

            return stmt.executeUpdate() > 0;
        }
    }

    // Delete an incident by ID
    public boolean deleteIncident(int incidentId) throws SQLException {
        String sql = "DELETE FROM INCIDENTS WHERE INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Retrieve all incidents
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

    // Retrieve incidents by participant ID
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

    // Map a ResultSet to an Incident object
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

    // Retrieve complete incident details including related data
    public Incident getCompleteIncidentDetails(int incidentId) throws SQLException {
        Incident incident = getIncidentById(incidentId);
        if (incident == null) {
            return null;
        }

        // Fetch participant details
        Participants participant = getParticipantById(incident.getParticipantId());
        incident.setParticipants(participant);

        // If participant is a student, fetch student details
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

    // Map a ResultSet to a Participant object
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

    // Retrieve student by ID
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

    // Map a ResultSet to a Student object
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
