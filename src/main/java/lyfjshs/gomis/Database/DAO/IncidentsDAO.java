package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;

public class IncidentsDAO {
    private final Connection connection; // Database connection instance

    // Constructor to initialize DAO with a database connection
    public IncidentsDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ Insert a new incident into the database and return the generated ID
    public int createIncident(Incident incident) throws SQLException {
        String sql = "INSERT INTO INCIDENTS (PARTICIPANT_ID, INCIDENT_DATE, INCIDENT_DESCRIPTION, " +
                "ACTION_TAKEN, RECOMMENDATION, STATUS, UPDATED_AT) " +
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

            // Retrieve the generated ID of the inserted incident
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1; // Returns -1 if insertion failed
    }

    // ✅ Retrieve an incident by its ID
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
        return null; // Return null if no incident is found
    }

    // ✅ Update an existing incident in the database
    public boolean updateIncident(Incident incident) throws SQLException {
        String sql = "UPDATE INCIDENTS SET PARTICIPANT_ID = ?, INCIDENT_DATE = ?, INCIDENT_DESCRIPTION = ?, " +
                "ACTION_TAKEN = ?, RECOMMENDATION = ?, STATUS = ?, UPDATED_AT = ? WHERE INCIDENT_ID = ?";

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

    // ✅ Delete an incident by its ID
    public boolean deleteIncident(int incidentId) throws SQLException {
        String sql = "DELETE FROM INCIDENTS WHERE INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ✅ Retrieve all incidents from the database
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

    // ✅ Retrieve incidents based on participant ID
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

    // ✅ Retrieve a complete incident with related participant and student details
    public Incident getCompleteIncidentDetails(int incidentId) throws SQLException {
        Incident incident = getIncidentById(incidentId);
        if (incident == null) {
            return null;
        }

        // Fetch participant details
        Participants participant = getParticipantById(incident.getParticipantId());
        incident.setParticipants(participant);

        // If the participant is a student, retrieve student details
        if (participant.getStudentUid() != null) {
            Student student = getStudentById(participant.getStudentUid());
            incident.setStudent(student);
        }

        return incident;
    }

    // ✅ Retrieve participant by ID
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

    // ✅ Retrieve a student by ID (updated for new schema)
    private Student getStudentById(int studentUid) throws SQLException {
        String sql = "SELECT s.*, a.*, c.*, p.*, g.*, sf.* FROM STUDENT s " +
                "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID " +
                "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                "LEFT JOIN SCHOOL_FORM sf ON s.SF_SECTION = sf.SF_SECTION " +
                "WHERE s.STUDENT_UID = ?";

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

    // ✅ Convert a ResultSet to an Incident object
    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        return new Incident(
                rs.getInt("INCIDENT_ID"),
                rs.getInt("PARTICIPANT_ID"),
                rs.getTimestamp("INCIDENT_DATE"),
                rs.getString("INCIDENT_DESCRIPTION"),
                rs.getString("ACTION_TAKEN"),
                rs.getString("RECOMMENDATION"),
                rs.getString("STATUS"),
                rs.getTimestamp("UPDATED_AT")
        );
    }

    // ✅ Convert a ResultSet to a Participant object
    private Participants mapResultSetToParticipant(ResultSet rs) throws SQLException {
        return new Participants(
                rs.getInt("PARTICIPANT_ID"),
                rs.getInt("STUDENT_UID"),
                rs.getString("PARTICIPANT_TYPE"),
                rs.getString("PARTICIPANT_LASTNAME"),
                rs.getString("PARTICIPANT_FIRSTNAME"),
                rs.getString("EMAIL"),
                rs.getString("CONTACT_NUMBER")
        );
    }

    // ✅ Convert a ResultSet to a Student object (updated for School Section)
  // ✅ Convert a ResultSet to a Student object (updated for full entity mapping)
private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
    Address address = new Address(
            rs.getInt("ADDRESS_ID"),
            rs.getString("ADDRESS_HOUSE_NUMBER"),
            rs.getString("ADDRESS_STREET_SUBDIVISION"),
            rs.getString("ADDRESS_REGION"),
            rs.getString("ADDRESS_PROVINCE"),
            rs.getString("ADDRESS_MUNICIPALITY"),
            rs.getString("ADDRESS_BARANGAY"),
            rs.getString("ADDRESS_ZIP_CODE")
    );

    Contact contact = new Contact(
            rs.getInt("CONTACT_ID"),
            rs.getString("CONTACT_NUMBER")
    );

    Parents parents = new Parents(
            rs.getInt("PARENT_ID"),
            rs.getString("FATHER_LASTNAME"),
            rs.getString("FATHER_FIRSTNAME"),
            rs.getString("FATHER_MIDDLENAME"),
            rs.getString("FATHER_CONTACT_NUMBER"),
            rs.getString("MOTHER_LASTNAME"),
            rs.getString("MOTHER_FIRSTNAME"),
            rs.getString("MOTHER_MIDDLE_NAME"),
            rs.getString("MOTHER_CONTACT_NUMBER")
    );

    Guardian guardian = new Guardian(
            rs.getInt("GUARDIAN_ID"),
            rs.getString("GUARDIAN_LASTNAME"),
            rs.getString("GUARDIAN_FIRST_NAME"),
            rs.getString("GUARDIAN_MIDDLE_NAME"),
            rs.getString("GUARDIAN_RELATIONSHIP"),
            rs.getString("GUARDIAN_CONTACT_NUMBER")
    );

    SchoolForm schoolForm = new SchoolForm(
            rs.getInt("SF_ID"), 
            rs.getString("SF_SCHOOL_NAME"),
            rs.getString("SF_SCHOOL_ID"),
            rs.getString("SF_DISTRICT"),
            rs.getString("SF_DIVISION"),
            rs.getString("SF_REGION"),
            rs.getString("SF_SEMESTER"),
            rs.getString("SF_SCHOOL_YEAR"),
            rs.getString("SF_GRADE_LEVEL"),
            rs.getString("SF_SECTION"), // ✅ Ensure SF_SECTION is mapped
            rs.getString("SF_TRACK_AND_STRAND"),
            rs.getString("SF_COURSE")
    );

    return new Student(
            rs.getInt("STUDENT_UID"),
            rs.getInt("PARENT_ID"),
            rs.getInt("GUARDIAN_ID"),
            rs.getInt("ADDRESS_ID"),
            rs.getInt("CONTACT_ID"),
            rs.getString("SF_SECTION"), // ✅ Reference school section
            rs.getString("STUDENT_LRN"),
            rs.getString("STUDENT_LASTNAME"),
            rs.getString("STUDENT_FIRSTNAME"),
            rs.getString("STUDENT_MIDDLENAME"),
            rs.getString("STUDENT_SEX"),
            rs.getDate("STUDENT_BIRTHDATE"),
            rs.getString("STUDENT_MOTHERTONGUE"),
            rs.getInt("STUDENT_AGE"),
            rs.getString("STUDENT_IP_TYPE"),
            rs.getString("STUDENT_RELIGION"),
            address, // ✅ Address entity
            contact, // ✅ Contact entity
            parents, // ✅ Parents entity
            guardian, // ✅ Guardian entity
            schoolForm // ✅ SchoolForm entity
    );
}

}
