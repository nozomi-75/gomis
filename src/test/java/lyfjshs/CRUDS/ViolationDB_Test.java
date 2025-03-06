package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.ViolationRecord;

public class ViolationDB_Test {

    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("? Database connection established.");
            ViolationCRUD crud = new ViolationCRUD(conn);

            setupTestData(conn);  // ✅ Ensure test participant exists before running tests

            testAddViolation(crud);
            testGetViolationById(crud);
            testGetAllViolations(crud);
            testUpdateViolation(crud);
            testDeleteViolation(crud);

            System.out.println("? Database connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ Ensures PARTICIPANT_ID = 1 exists before running tests.
     */
    private static void setupTestData(Connection conn) {
        String checkSQL = "SELECT COUNT(*) FROM PARTICIPANTS WHERE PARTICIPANT_ID = ?";
        String insertSQL = "INSERT INTO PARTICIPANTS (PARTICIPANT_ID, STUDENT_UID) VALUES (?, ?)";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, 1);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("✔ Test Participant already exists.");
                return; // ✅ Participant exists, no need to insert
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // ✅ Insert participant if not exists
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
            insertStmt.setInt(1, 1);
            insertStmt.setInt(2, 1001); // Use an appropriate `STUDENT_UID`
            insertStmt.executeUpdate();
            System.out.println("✔ Test Participant inserted.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testAddViolation(ViolationCRUD crud) {
        boolean added = crud.addViolation(
                1, // ✅ Ensure this matches the PARTICIPANT_ID inserted in setupTestData()
                "Late", "Arrived late", "Explained delay", "Warning", "Pending",
                new java.sql.Timestamp(System.currentTimeMillis())
        );
        System.out.println(added ? "✔ testAddViolation Passed" : "❌ testAddViolation Failed");
    }

    static void testGetViolationById(ViolationCRUD violationCRUD) {
        try {
            int testViolationId = getLatestViolationId(violationCRUD);
            if (testViolationId == -1) {
                System.out.println("❌ testGetViolationById Failed: No violations exist.");
                return;
            }

            ViolationRecord violation = violationCRUD.getViolationById(testViolationId);
            System.out.println((violation != null) ? "✔ testGetViolationById Passed" : "❌ testGetViolationById Failed");
        } catch (Exception e) {
            System.err.println("❌ testGetViolationById Failed: " + e.getMessage());
        }
    }

    static void testGetAllViolations(ViolationCRUD violationCRUD) {
        try {
            List<ViolationRecord> violations = violationCRUD.getAllViolations();
            if (violations != null && !violations.isEmpty()) {
                System.out.println("✔ testGetAllViolations Passed");
            } else {
                System.out.println("❌ testGetAllViolations Failed");
            }
        } catch (Exception e) {
            System.err.println("❌ testGetAllViolations Failed: " + e.getMessage());
        }
    }

    static void testUpdateViolation(ViolationCRUD violationCRUD) {
        try {
            int testViolationId = getLatestViolationId(violationCRUD);
            if (testViolationId == -1) {
                System.out.println("❌ testUpdateViolation Failed: No violations exist.");
                return;
            }

            boolean updated = violationCRUD.updateViolation(
                    testViolationId,
                    "Updated Violation Type",
                    "Updated violation description.",
                    "Updated anecdotal record.",
                    "Updated reinforcement.",
                    "Resolved",
                    Timestamp.valueOf(LocalDateTime.now())
            );
            System.out.println(updated ? "✔ testUpdateViolation Passed" : "❌ testUpdateViolation Failed");
        } catch (Exception e) {
            System.err.println("❌ testUpdateViolation Failed: " + e.getMessage());
        }
    }

    static void testDeleteViolation(ViolationCRUD violationCRUD) {
        try {
            int violationIdToDelete = getLatestViolationId(violationCRUD);
            if (violationIdToDelete == -1) {
                System.out.println("❌ testDeleteViolation Failed: No violations exist.");
                return;
            }

            boolean deleted = violationCRUD.deleteViolation(violationIdToDelete);
            System.out.println(deleted ? "✔ testDeleteViolation Passed" : "❌ testDeleteViolation Failed");
        } catch (Exception e) {
            System.err.println("❌ testDeleteViolation Failed: " + e.getMessage());
        }
    }

    /**
     * ✅ Retrieves the most recent Violation ID.
     * Prevents using hardcoded `VIOLATION_ID = 1`, which may not exist.
     */
    private static int getLatestViolationId(ViolationCRUD crud) {
        List<ViolationRecord> violations = crud.getAllViolations();
        if (violations.isEmpty()) {
            return -1;
        }
        return violations.get(violations.size() - 1).getViolationId(); // Get the latest ID
    }

    static void tearDown(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
            System.out.println("✔ Database connection closed.");
        }
    }
}
