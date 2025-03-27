package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.SQLExceptionPane;
import lyfjshs.gomis.Database.entity.ViolationRecord;

public class ViolationCRUD {

    private final Connection connection;

    public ViolationCRUD(Connection connect) {
        this.connection = connect;
    }

    // CREATE (Insert a new violation)

    // Modified addViolation() to check for participant existence
    public boolean addViolation(
            int participantId,
            String violationType,
            String description,
            String anecdotalRecord,
            String reinforcement,
            String status,
            java.sql.Timestamp updatedAt) {
        
        // First, check if PARTICIPANT_ID exists
        String checkParticipantSQL = "SELECT COUNT(*) FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkParticipantSQL)) {
            checkStmt.setInt(1, participantId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Error: PARTICIPANT_ID " + participantId + " does not exist. Please check the PARTICIPANTS table.");
                return false; // Prevent insertion
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Checking Participant Existence");
            return false;
        }

        // Now, insert the violation
        String sql = "INSERT INTO VIOLATION_RECORD (PARTICIPANT_ID, VIOLATION_TYPE, VIOLATION_DESCRIPTION, "
                + "ANECDOTAL_RECORD, REINFORCEMENT, STATUS, UPDATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, participantId);
            stmt.setString(2, violationType);
            stmt.setString(3, description);
            stmt.setString(4, anecdotalRecord);
            stmt.setString(5, reinforcement);
            stmt.setString(6, status);
            stmt.setTimestamp(7, updatedAt);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Adding Violation");
            return false;
        }
    }


    // READ (Retrieve all violations)
    public List<ViolationRecord> getAllViolations() {
        List<ViolationRecord> violations = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_RECORD";

        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching All Violations");
        }
        return violations;
    }

    // READ (Retrieve a single violation by ID)
    public ViolationRecord getViolationById(int violationId) {
        String sql = "SELECT * FROM VIOLATION_RECORD WHERE VIOLATION_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToViolation(rs);
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching Violation by ID");
        }
        return null;
    }

    // READ (Retrieve a single violation by LRN)
    public ViolationRecord getViolationByLRN(String lrn) throws SQLException {
        String sql = "SELECT vr.* FROM VIOLATION_RECORD vr " +
                     "JOIN PARTICIPANTS p ON vr.PARTICIPANT_ID = p.PARTICIPANT_ID " +
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

    // UPDATE (Modify an existing violation)
    public boolean updateViolation(
            int violationId, String violationType, String description,
            String anecdotalRecord, String reinforcement, String status, java.sql.Timestamp updatedAt) {
        String sql = "UPDATE VIOLATION_RECORD SET VIOLATION_TYPE = ?, VIOLATION_DESCRIPTION = ?, "
                + "ANECDOTAL_RECORD = ?, REINFORCEMENT = ?, STATUS = ?, UPDATED_AT = ? "
                + "WHERE VIOLATION_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, violationType);
            stmt.setString(2, description);
            stmt.setString(3, anecdotalRecord);
            stmt.setString(4, reinforcement);
            stmt.setString(5, status);
            stmt.setTimestamp(6, updatedAt);
            stmt.setInt(7, violationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Updating Violation");
            return false;
        }
    }

    // UPDATE (Modify the status of an existing violation)
    public boolean updateViolationStatus(int violationId, String status) {
    String sql = "UPDATE VIOLATION_RECORD SET STATUS = ? WHERE VIOLATION_ID = ?";
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setString(1, status);
        stmt.setInt(2, violationId);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        SQLExceptionPane.showSQLException(e, "Updating Violation Status");
        return false; 
    }
}


    // DELETE (Remove a violation by ID)
    public boolean deleteViolation(int violationId) {
        String sql = "DELETE FROM VIOLATION_RECORD WHERE VIOLATION_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, violationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Deleting Violation");
            return false;
        }
    }

    // Helper method to map a ResultSet to a Violation object
    private ViolationRecord mapResultSetToViolation(ResultSet rs) throws SQLException {
        ViolationRecord violation = new ViolationRecord();
        violation.setViolationId(rs.getInt("VIOLATION_ID"));
        violation.setParticipantId(rs.getInt("PARTICIPANT_ID"));
        violation.setViolationType(rs.getString("VIOLATION_TYPE"));
        violation.setViolationDescription(rs.getString("VIOLATION_DESCRIPTION"));
        violation.setAnecdotalRecord(rs.getString("ANECDOTAL_RECORD"));
        violation.setReinforcement(rs.getString("REINFORCEMENT"));
        violation.setStatus(rs.getString("STATUS"));
        violation.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return violation;
    }

    // Search violations
    public List<ViolationRecord> searchViolations(String searchTerm) throws SQLException {
    List<ViolationRecord> violations = new ArrayList<>();
    String query = "SELECT * FROM VIOLATION_RECORD WHERE VIOLATION_TYPE LIKE CONCAT('%', ?, '%') OR VIOLATION_DESCRIPTION LIKE CONCAT('%', ?, '%')";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, searchTerm);
        stmt.setString(2, searchTerm);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                violations.add(mapResultSetToViolation(rs));
            }
        }
    }
    return violations;
}

    // New method to retrieve violations by student UID
    public List<ViolationRecord> getViolationsByStudentUID(int studentUID) throws Exception {
        List<ViolationRecord> violations = new ArrayList<>();
        String query = "SELECT vr.* FROM VIOLATION_RECORD vr " +
                       "JOIN PARTICIPANTS p ON vr.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                       "WHERE p.STUDENT_UID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, studentUID);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ViolationRecord violation = new ViolationRecord(
                    resultSet.getInt("VIOLATION_ID"),
                    resultSet.getInt("PARTICIPANT_ID"),
                    resultSet.getString("VIOLATION_TYPE"),
                    resultSet.getString("VIOLATION_DESCRIPTION"),
                    resultSet.getString("ANECDOTAL_RECORD"),
                    resultSet.getString("REINFORCEMENT"),
                    resultSet.getString("STATUS"),
                    resultSet.getTimestamp("UPDATED_AT")
                );
                violations.add(violation);
            }
        }
        return violations;
    }

    public List<ViolationRecord> getViolationsByParticipantName(String participantName) throws SQLException {
        List<ViolationRecord> violations = new ArrayList<>();
        String sql = "SELECT vr.* FROM VIOLATION_RECORD vr " +
                    "JOIN PARTICIPANTS p ON vr.PARTICIPANT_ID = p.PARTICIPANT_ID " +
                    "WHERE CONCAT(p.PARTICIPANT_FIRSTNAME, ' ', p.PARTICIPANT_LASTNAME) = ? " +
                    "ORDER BY vr.UPDATED_AT DESC";
                    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, participantName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ViolationRecord violation = new ViolationRecord(
                        rs.getInt("VIOLATION_ID"),
                        rs.getInt("PARTICIPANT_ID"),
                        rs.getString("VIOLATION_TYPE"),
                        rs.getString("VIOLATION_DESCRIPTION"),
                        rs.getString("ANECDOTAL_RECORD"),
                        rs.getString("REINFORCEMENT"),
                        rs.getString("STATUS"),
                        rs.getTimestamp("UPDATED_AT")
                    );
                    violations.add(violation);
                }
            }
        }
        return violations;
    }

    // Get active violations that need follow-up
    public List<ViolationRecord> getActiveViolations() throws SQLException {
        List<ViolationRecord> violations = new ArrayList<>();
        String sql = "SELECT * FROM VIOLATION_RECORD WHERE STATUS = 'Active' " +
                    "AND DATEDIFF(NOW(), UPDATED_AT) >= 7 " +
                    "ORDER BY UPDATED_AT";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                ViolationRecord violation = new ViolationRecord(
                    rs.getInt("VIOLATION_ID"),
                    rs.getInt("PARTICIPANT_ID"),
                    rs.getString("VIOLATION_TYPE"),
                    rs.getString("VIOLATION_DESCRIPTION"),
                    rs.getString("ANECDOTAL_RECORD"),
                    rs.getString("REINFORCEMENT"),
                    rs.getString("STATUS"),
                    rs.getTimestamp("UPDATED_AT")
                );
                violations.add(violation);
            }
        }
        return violations;
    }
}
