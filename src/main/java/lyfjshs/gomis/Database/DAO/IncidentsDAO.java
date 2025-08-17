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

import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;

public class IncidentsDAO {
    private final Connection connection;

    public IncidentsDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ Insert a new incident and return the generated ID
    public int createIncident(Incident incident) throws SQLException {
        String sql = "INSERT INTO INCIDENTS (PARTICIPANT_ID, INCIDENT_DATE, INCIDENT_DESCRIPTION, " +
                     "ACTION_TAKEN, RECOMMENDATION, STATUS, UPDATED_AT) " +
                     "VALUES (?, ?, ?, ?, ?, COALESCE(?, 'PENDING'), NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, incident.getParticipantId());
            stmt.setTimestamp(2, incident.getIncidentDate());
            stmt.setString(3, incident.getIncidentDescription());
            stmt.setString(4, incident.getActionTaken());
            stmt.setString(5, incident.getRecommendation());
            stmt.setString(6, incident.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Adds multiple participants to an incident.
     * This assumes a linking table named INCIDENTS_PARTICIPANTS exists.
     * @param incidentId The ID of the incident.
     * @param participantIds A list of participant IDs to associate with the incident.
     * @throws SQLException If a database access error occurs.
     */
    public void addParticipantsToIncident(int incidentId, List<Integer> participantIds) throws SQLException {
        String sql = "INSERT INTO INCIDENTS_PARTICIPANTS (INCIDENT_ID, PARTICIPANT_ID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Integer participantId : participantIds) {
                stmt.setInt(1, incidentId);
                stmt.setInt(2, participantId);
                stmt.addBatch(); // Add to batch for efficient insertion
            }
            stmt.executeBatch(); // Execute all batched inserts
        }
    }

    // Helper method to standardize status values
    private String standardizeStatus(String status) {
        if (status == null) return "PENDING";
        switch (status.toUpperCase()) {
            case "RESOLVED":
                return "RESOLVED";
            case "IN PROGRESS":
                return "IN PROGRESS";
            case "PENDING":
            default:
                return "PENDING";
        }
    }

    // Update incident status
    public boolean updateIncidentStatus(int incidentId, String status) throws SQLException {
        String sql = "UPDATE INCIDENTS SET STATUS = ?, UPDATED_AT = NOW() WHERE INCIDENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, standardizeStatus(status));
            stmt.setInt(2, incidentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ✅ Retrieve an incident by ID
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

    // ✅ Update an existing incident
    public boolean updateIncident(Incident incident) throws SQLException {
        String sql = "UPDATE INCIDENTS SET PARTICIPANT_ID = ?, INCIDENT_DATE = ?, INCIDENT_DESCRIPTION = ?, " +
                     "ACTION_TAKEN = ?, RECOMMENDATION = ?, STATUS = ?, UPDATED_AT = NOW() WHERE INCIDENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incident.getParticipantId());
            stmt.setTimestamp(2, incident.getIncidentDate());
            stmt.setString(3, incident.getIncidentDescription());
            stmt.setString(4, incident.getActionTaken());
            stmt.setString(5, incident.getRecommendation());
            stmt.setString(6, incident.getStatus());
            stmt.setInt(7, incident.getIncidentId());
            return stmt.executeUpdate() > 0;
        }
    }

    // ✅ Delete an incident
    public boolean deleteIncident(int incidentId) throws SQLException {
        String sql = "DELETE FROM INCIDENTS WHERE INCIDENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ✅ Retrieve all incidents with participant details
    public List<Incident> getAllIncidents() throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT i.*, p.* FROM INCIDENTS i " +
                     "LEFT JOIN PARTICIPANTS p ON i.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                     "ORDER BY i.INCIDENT_DATE DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Incident incident = mapResultSetToIncident(rs);
                incident.setParticipants(mapResultSetToParticipant(rs));
                incidents.add(incident);
            }
        }
        return incidents;
    }

    // ✅ Retrieve incidents by participant ID
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

    // ✅ Retrieve a complete incident with participant and student details
    public Incident getCompleteIncidentDetails(int incidentId) throws SQLException {
        String sql = "SELECT i.*, p.*, " +
                    "s.*, a.*, c.*, par.*, g.*, sf.* " +
                    "FROM INCIDENTS i " +
                    "LEFT JOIN PARTICIPANTS p ON i.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "LEFT JOIN STUDENT s ON p.STUDENT_UID = s.STUDENT_UID " +
                    "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                    "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                    "LEFT JOIN PARENTS par ON s.PARENT_ID = par.PARENT_ID " +
                    "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                    "LEFT JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID " + // Updated join condition
                    "WHERE i.INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Incident incident = mapResultSetToIncident(rs);
                    Participants participant = mapResultSetToParticipant(rs);
                    
                    // Only set student data if participant is a student
                    if (rs.getObject("STUDENT_UID") != null) {
                        StudentsDataDAO studentDAO = new StudentsDataDAO(connection);
                        Student student = studentDAO.getStudentById(rs.getInt("STUDENT_UID"));
                        participant.setStudent(student);
                    }
                    
                    incident.setParticipants(participant);
                    return incident;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all participants associated with a specific incident.
     * This assumes a linking table named INCIDENTS_PARTICIPANTS exists.
     * @param incidentId The ID of the incident.
     * @return A list of Participants associated with the incident.
     * @throws SQLException If a database access error occurs.
     */
    public List<Participants> getParticipantsByIncidentId(int incidentId) throws SQLException {
        List<Participants> participants = new ArrayList<>();
        String sql = "SELECT p.*, ip.INCIDENT_ID " +
                     "FROM PARTICIPANTS p " +
                     "JOIN INCIDENTS_PARTICIPANTS ip ON p.PARTICIPANT_ID = ip.PARTICIPANT_ID " +
                     "WHERE ip.INCIDENT_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participants.add(mapResultSetToParticipant(rs));
                }
            }
        }
        return participants;
    }

    // ✅ Map ResultSet to an Incident object
    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        return new Incident(
                rs.getInt("INCIDENT_ID"),
                rs.getInt("PARTICIPANT_ID"),
                rs.getTimestamp("INCIDENT_DATE"),
                rs.getString("INCIDENT_DESCRIPTION"),
                rs.getString("ACTION_TAKEN"),
                rs.getString("RECOMMENDATION"),
                rs.getString("STATUS"),
                rs.getTimestamp("UPDATED_AT"));
    }

    // ✅ Map ResultSet to a Participants object
    private Participants mapResultSetToParticipant(ResultSet rs) throws SQLException {
        Participants participant = new Participants(
                rs.getObject("STUDENT_UID", Integer.class),
                rs.getString("PARTICIPANT_TYPE"),
                rs.getString("PARTICIPANT_LASTNAME"),
                rs.getString("PARTICIPANT_FIRSTNAME"),
                rs.getString("PARTICIPANT_SEX"),
                rs.getString("CONTACT_NUMBER"));
        participant.setParticipantId(rs.getInt("PARTICIPANT_ID"));
        return participant;
    }

    // ✅ Map ResultSet to a Student object
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
            rs.getString("SF_SECTION"),
            rs.getString("SF_TRACK_AND_STRAND"),
            rs.getString("SF_COURSE")
        );

        return new Student(
            rs.getInt("STUDENT_UID"),
            rs.getInt("PARENT_ID"),
            rs.getInt("GUARDIAN_ID"),
            rs.getInt("ADDRESS_ID"),
            rs.getInt("CONTACT_ID"),
            rs.getString("SF_SECTION"),
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
            address,
            contact,
            parents,
            guardian,
            schoolForm
        );
    }
}
