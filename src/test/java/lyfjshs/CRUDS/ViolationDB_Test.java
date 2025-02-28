package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.model.Violation;

public class ViolationDB_Test {

    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("? Database connection established.");
            ViolationCRUD crud = new ViolationCRUD(conn);
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

    private static void testAddViolation(ViolationCRUD crud) {
        boolean added = crud.addViolation(
                3, 
                "Late", "Arrived late", "Explained delay", "Warning", "Pending",
                new java.sql.Timestamp(System.currentTimeMillis())
        );
        System.out.println(added ? "? testAddViolation Passed" : "? testAddViolation Failed");
    }

    static void testGetViolationById(ViolationCRUD violationCRUD) {
        try {
            int testViolationId = 1; // Ensure this ID exists in your test database
            Violation violation = violationCRUD.getViolationById(testViolationId);

            if (violation != null && violation.getViolationId() == testViolationId) {
                System.out.println("✔ testGetViolationById Passed");
            } else {
                System.out.println("❌ testGetViolationById Failed");
            }
        } catch (Exception e) {
            System.err.println("❌ testGetViolationById Failed: " + e.getMessage());
        }
    }

    static void testGetAllViolations(ViolationCRUD violationCRUD) {
        try {
            List<Violation> violations = violationCRUD.getAllViolations();
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
            int testViolationId = 1; // Ensure this violation exists
            Violation violation = violationCRUD.getViolationById(testViolationId);

            if (violation == null) {
                System.out.println("❌ testUpdateViolation Failed: Violation not found");
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
            int violationIdToDelete = 3; 
            boolean deleted = violationCRUD.deleteViolation(violationIdToDelete);

            System.out.println(deleted ? "✔ testDeleteViolation Passed" : "❌ testDeleteViolation Failed");
        } catch (Exception e) {
            System.err.println("❌ testDeleteViolation Failed: " + e.getMessage());
        }
    }

    static void tearDown(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
            System.out.println("✔ Database connection closed.");
        }
    }
}
