package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.SQLExceptionPane;
import lyfjshs.gomis.Database.model.Violation;

public class ViolationCRUD {

    private final Connection connection;

    public ViolationCRUD(Connection connection) {
        this.connection = connection;
    }

    // CREATE (Insert a new violation)
    public boolean addViolation(
            int studentUid,
            String violationType,
            String description,
            String anecdotalRecord,
            String reinforcement,
            String vStatus,
            java.sql.Timestamp updatedAt) {
        String sql = "INSERT INTO VIOLATION_RECORD "
                + "(STUDENT_UID, VIOLATION_TYPE, VIOLATION_DESCRIPTION, "
                + "ANECDOTAL_RECORD, REINFORCEMENT, V_status, UPDATED_AT) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            stmt.setString(2, violationType);
            stmt.setString(3, description);
            stmt.setString(4, anecdotalRecord);
            stmt.setString(5, reinforcement);
            stmt.setString(6, vStatus);
            stmt.setTimestamp(7, updatedAt);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Adding Violation");
            return false;
        }
    }

    // READ (Retrieve all violations)
    public List<Violation> getAllViolations() {
        List<Violation> violations = new ArrayList<>();
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
    public Violation getViolationById(int violationId) {
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
    public Violation getViolationByLRN(Connection connection, String lrn) throws SQLException {
        String sql = "SELECT vr.* FROM VIOLATION_RECORD vr " +
                     "JOIN STUDENT s ON vr.student_uid = s.STUDENT_UID " +
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
            String anecdotalRecord, String reinforcement, String vStatus, java.sql.Timestamp updatedAt) {
        String sql = "UPDATE VIOLATION_RECORD SET VIOLATION_TYPE = ?, VIOLATION_DESCRIPTION = ?, "
                + "ANECDOTAL_RECORD = ?, REINFORCEMENT = ?, V_status = ?, UPDATED_AT = ? "
                + "WHERE VIOLATION_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, violationType);
            stmt.setString(2, description);
            stmt.setString(3, anecdotalRecord);
            stmt.setString(4, reinforcement);
            stmt.setString(5, vStatus);
            stmt.setTimestamp(6, updatedAt);
            stmt.setInt(7, violationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Updating Violation");
            return false;
        }
    }

    // UPDATE (Modify the status of an existing violation)
    public boolean updateViolationStatus(Connection connection, int violationId, String status) throws SQLException {
        String sql = "UPDATE VIOLATION_RECORD SET V_status = ? WHERE VIOLATION_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, violationId);
            return stmt.executeUpdate() > 0;
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
    private Violation mapResultSetToViolation(ResultSet rs) throws SQLException {
        Violation violation = new Violation();
        violation.setViolationId(rs.getInt("VIOLATION_ID"));
        violation.setStudentUid(rs.getInt("STUDENT_UID"));
        violation.setViolationType(rs.getString("VIOLATION_TYPE"));
        violation.setViolationDescription(rs.getString("VIOLATION_DESCRIPTION"));
        violation.setAnecdotalRecord(rs.getString("ANECDOTAL_RECORD"));
        violation.setReinforcement(rs.getString("REINFORCEMENT"));
        violation.setStatus(rs.getString("V_status"));
        violation.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return violation;
    }

    public List<Violation> searchViolations(Connection conn, String searchTerm) throws SQLException {
        List<Violation> violations = new ArrayList<>();
        String query = "SELECT * FROM violations WHERE name LIKE ? OR type LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Violation violation = new Violation();
                    // populate violation object with data from result set
                    violations.add(violation);
                }
            }
        }
        return violations;
    }

}
