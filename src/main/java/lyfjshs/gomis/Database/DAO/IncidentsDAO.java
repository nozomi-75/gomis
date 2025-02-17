package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.Incident;
import lyfjshs.gomis.Database.model.StudentsData;
import lyfjshs.gomis.Database.model.Violation;

public class IncidentsDAO {
    private Connection connection;

    public IncidentsDAO(Connection connection) {
        this.connection = connection;
    }

    // Create new incident
    public int createIncident(Incident incident) throws SQLException {
        String sql = "INSERT INTO INCIDENTS (student_uid, participant_id, violation_id, incident_date, " +
                "incident_description, action_taken, recommendation, status, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set nullable student_uid
            if (incident.getStudentUid() != null) {
                stmt.setInt(1, incident.getStudentUid());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            stmt.setInt(2, incident.getParticipantId());
            stmt.setInt(3, incident.getViolationId());
            stmt.setTimestamp(4, incident.getIncidentDate());
            stmt.setString(5, incident.getIncidentDescription());
            stmt.setString(6, incident.getActionTaken());
            stmt.setString(7, incident.getRecommendation());
            stmt.setString(8, incident.getStatus());
            stmt.setTimestamp(9, incident.getUpdatedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Read incident by ID
    public Incident getIncidentById(int incidentId) throws SQLException {
        String sql = "SELECT * FROM INCIDENTS WHERE incident_id = ?";

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

    // Update incident
    public boolean updateIncident(Incident incident) throws SQLException {
        String sql = "UPDATE INCIDENTS SET student_uid = ?, participant_id = ?, violation_id = ?, " +
                "incident_description = ?, action_taken = ?, recommendation = ?, status = ?, updated_at = ? " +
                "WHERE incident_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (incident.getStudentUid() != null) {
                stmt.setInt(1, incident.getStudentUid());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, incident.getParticipantId());
            stmt.setInt(3, incident.getViolationId());
            stmt.setString(4, incident.getIncidentDescription());
            stmt.setString(5, incident.getActionTaken());
            stmt.setString(6, incident.getRecommendation());
            stmt.setString(7, incident.getStatus());
            stmt.setTimestamp(8, incident.getUpdatedAt());
            stmt.setInt(9, incident.getIncidentId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Delete incident
    public boolean deleteIncident(int incidentId) throws SQLException {
        String sql = "DELETE FROM INCIDENTS WHERE incident_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, incidentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Get all incidents
    public List<Incident> getAllIncidents() throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENTS ORDER BY incident_date DESC";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                incidents.add(mapResultSetToIncident(rs));
            }
        }
        return incidents;
    }

    // Get incidents by student
    public List<Incident> getIncidentsByStudent(int studentUid) throws SQLException {
        List<Incident> incidents = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENTS WHERE student_uid = ? ORDER BY incident_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    incidents.add(mapResultSetToIncident(rs));
                }
            }
        }
        return incidents;
    }

    // Helper method to map ResultSet to Incident object
    private Incident mapResultSetToIncident(ResultSet rs) throws SQLException {
        Incident incident = new Incident();
        incident.setIncidentId(rs.getInt("incident_id"));
        incident.setStudentUid(rs.getInt("student_uid"));
        incident.setParticipantId(rs.getInt("participant_id"));
        incident.setViolationId(rs.getInt("violation_id"));
        incident.setIncidentDate(rs.getTimestamp("incident_date"));
        incident.setIncidentDescription(rs.getString("incident_description"));
        incident.setActionTaken(rs.getString("action_taken"));
        incident.setRecommendation(rs.getString("recommendation"));
        incident.setStatus(rs.getString("status"));
        incident.setUpdatedAt(rs.getTimestamp("updated_at"));
        return incident;
    }

    // Get complete incident details including related data
    public Incident getCompleteIncidentDetails(int incidentId) throws SQLException {
        Incident incident = getIncidentById(incidentId);
        if (incident == null)
            return null;

        StudentsDataDAO studentsCRUD = new StudentsDataDAO(connection);
        ViolationCRUD violationCRUD = new ViolationCRUD(connection);

        // Fetch related data
        StudentsData student = (incident.getStudentUid() != null)
                ? studentsCRUD.getStudentById(incident.getStudentUid())
                : null;
        Violation violation = violationCRUD.getViolationById(incident.getViolationId());

        // Debugging/logging
        System.out.println("Incident Details:");
        System.out.println("Student: " + (student != null ? student.getFIRST_NAME() + student.getLAST_NAME() : "N/A"));
        System.out.println("Violation: " + (violation != null ? violation.getViolationType() : "N/A"));

        return incident;
    }

}