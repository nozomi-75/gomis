package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Violation;

public class ViolationDAO {
    private final Connection connection;

    public ViolationDAO(Connection connection) {
        this.connection = connection;
    }

    // Create a new violation and return the generated ID
    public int createViolation(Violation violation) throws SQLException {
        String sql = "INSERT INTO VIOLATION_RECORD (PARTICIPANT_ID, VIOLATION_TYPE, VIOLATION_DESCRIPTION, " +
                    "SESSION_SUMMARY, REINFORCEMENT, STATUS, UPDATED_AT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // First, check if participant exists
            if (!doesParticipantExist(violation.getParticipantId())) {
                throw new SQLException("Participant ID " + violation.getParticipantId() + " does not exist");
            }

            stmt.setInt(1, violation.getParticipantId());
            stmt.setString(2, violation.getViolationType());
            stmt.setString(3, violation.getViolationDescription());
            stmt.setString(4, violation.getSessionSummary());
            stmt.setString(5, violation.getReinforcement());
            stmt.setString(6, violation.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new SQLException("Creating violation failed, no ID obtained.");
        }
    }

    // Add violation with individual parameters
    public boolean addViolation(int participantId, String violationType, String violationDescription,
                              String sessionSummary, String reinforcement, String status, Timestamp updatedAt) throws SQLException {
        Violation violation = new Violation(0, participantId, violationType, violationDescription,
                                         sessionSummary, reinforcement, status, updatedAt);
        return createViolation(violation) > 0;
    }

    // Get violations by student ID (active only)
    public List<Violation> getViolationsByStudentId(int studentId) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE p.STUDENT_UID = ? AND v.STATUS = 'Active'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Get violation by LRN
    public Violation getViolationByLRN(String lrn) throws SQLException {
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "JOIN STUDENT s ON p.STUDENT_UID = s.STUDENT_UID " +
                    "WHERE s.STUDENT_LRN = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, lrn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToViolation(rs);
                }
            }
        }
        return null;
    }

    // Get violations by student UID
    public List<Violation> getViolationsByStudentUID(int studentUID) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE p.STUDENT_UID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Get all violations
    public List<Violation> getAllViolations() throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_RECORD ORDER BY UPDATED_AT DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        }
        return violations;
    }

    // Update violation
    public void updateViolation(Violation violation) throws SQLException {
        String sql = "UPDATE VIOLATION_RECORD SET VIOLATION_TYPE = ?, VIOLATION_DESCRIPTION = ?, " +
                    "SESSION_SUMMARY = ?, REINFORCEMENT = ?, STATUS = ?, UPDATED_AT = NOW() " +
                    "WHERE VIOLATION_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, violation.getViolationType());
            stmt.setString(2, violation.getViolationDescription());
            stmt.setString(3, violation.getSessionSummary());
            stmt.setString(4, violation.getReinforcement());
            stmt.setString(5, violation.getStatus());
            stmt.setInt(6, violation.getViolationId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating violation failed, no rows affected.");
            }
        }
    }

    // Helper method to standardize status values
    private String standardizeStatus(String status) {
        if (status == null) return "ACTIVE";
        switch (status.toUpperCase()) {
            case "RESOLVED":
            case "RESOLVE":
                return "RESOLVED";
            case "ACTIVE":
            default:
                return "ACTIVE";
        }
    }

    // Update violation status with standardized values
    public boolean updateViolationStatus(int violationId, String status) throws SQLException {
        String sql = "UPDATE VIOLATION_RECORD SET STATUS = ? WHERE VIOLATION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, standardizeStatus(status));
            stmt.setInt(2, violationId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Delete violation
    public void deleteViolation(int violationId) throws SQLException {
        // First check if there are any related sessions
        String checkSessionsSql = "SELECT COUNT(*) FROM SESSIONS WHERE VIOLATION_ID = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSessionsSql)) {
            checkStmt.setInt(1, violationId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete violation: It has associated sessions");
                }
            }
        }

        // If no sessions exist, proceed with deletion
        String sql = "DELETE FROM VIOLATION_RECORD WHERE VIOLATION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting violation failed, no rows affected.");
            }
        }
    }

    // Search violations
    public List<Violation> searchViolations(String searchTerm) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "WHERE v.VIOLATION_TYPE LIKE ? OR v.VIOLATION_DESCRIPTION LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Get violations by participant name
    public List<Violation> getViolationsByParticipantName(String firstName, String lastName) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT v.* FROM VIOLATION_RECORD v " +
                    "JOIN PARTICIPANTS p ON v.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE p.PARTICIPANT_FIRSTNAME LIKE ? AND p.PARTICIPANT_LASTNAME LIKE ? " +
                    "ORDER BY v.UPDATED_AT DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + firstName + "%");
            stmt.setString(2, "%" + lastName + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    violations.add(mapResultSetToViolation(rs));
                }
            }
        }
        return violations;
    }

    // Overloaded method for full name
    public List<Violation> getViolationsByParticipantName(String fullName) throws SQLException {
        String[] names = fullName.split(" ", 2);
        String firstName = names[0];
        String lastName = names.length > 1 ? names[1] : "";
        return getViolationsByParticipantName(firstName, lastName);
    }

    // Get active violations that need follow-up
    public List<Violation> getActiveViolations() throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_RECORD WHERE STATUS = 'Active' " +
                    "ORDER BY UPDATED_AT DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        }
        return violations;
    }

    // Helper method to check if participant exists
    private boolean doesParticipantExist(int participantId) throws SQLException {
        String sql = "SELECT 1 FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Helper method to map ResultSet to Violation object
    private Violation mapResultSetToViolation(ResultSet rs) throws SQLException {
        return new Violation(
            rs.getInt("VIOLATION_ID"),
            rs.getInt("PARTICIPANT_ID"),
            rs.getString("VIOLATION_TYPE"),
            rs.getString("VIOLATION_DESCRIPTION"),
            rs.getString("SESSION_SUMMARY"),
            rs.getString("REINFORCEMENT"),
            rs.getString("STATUS"),
            rs.getTimestamp("UPDATED_AT")
        );
    }
}