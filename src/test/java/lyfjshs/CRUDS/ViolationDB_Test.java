package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.model.Violation;

public class ViolationDB_Test {
    public static void main(String[] args) {
        // Database connection parameters
        String url = "jdbc:mariadb://localhost:3306/gomisDB";
        String user = "root";
        String password = "YourRootPassword123!"; // Update with actual password

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Pass the connection to ViolationCRUD constructor
            ViolationCRUD violationCRUD = new ViolationCRUD(conn);

            // Test CREATE operation
            System.out.println("Testing CREATE operation:");
            boolean createSuccess = violationCRUD.addViolation(
                    1, // participantId
                    "Bullying", // violationType
                    "Physical bullying", // violationDescription
                    "Note", // anecdotalRecord
                    "Counseling", // reinforcement
                    "Pending" // V_status
            );
            System.out.println("Create operation successful: " + createSuccess);
            System.out.println("------------------------");

            // Test READ operation - Get all violations
            System.out.println("Testing READ operation (getAllViolations):");
            List<Violation> violations = violationCRUD.getAllViolations();
            System.out.println("Violations found: " + violations.size());
            for (Violation v : violations) {
                System.out.println("Violation: " + v.toString());
            }
            System.out.println("------------------------");

            // Test READ operation - Get violation by ID (assuming ID 1 exists)
            System.out.println("Testing READ operation (getViolationById):");
            Violation violation = violationCRUD.getViolationById(1);
            if (violation != null) {
                System.out.println("Found violation: " + violation.toString());
            } else {
                System.out.println("No violation found with ID 1");
            }
            System.out.println("------------------------");

            // Test UPDATE operation (assuming ID 1 exists)
            if (violation != null) {
                System.out.println("Testing UPDATE operation:");
                boolean updateSuccess = violationCRUD.updateViolationStatus(1, "Resolved");
                System.out.println("Update operation successful: " + updateSuccess);

                // Verify update
                Violation updatedViolation = violationCRUD.getViolationById(1);
                if (updatedViolation != null) {
                    System.out.println("Updated violation: " + updatedViolation.toString());
                }
                System.out.println("------------------------");
            }

            // Test DELETE operation (assuming ID 1 exists)
            System.out.println("Testing DELETE operation:");
            boolean deleteSuccess = violationCRUD.deleteViolation(1);
            System.out.println("Delete operation successful: " + deleteSuccess);

            // Verify deletion
            Violation deletedViolation = violationCRUD.getViolationById(1);
            System.out.println("Violation after deletion: "
                    + (deletedViolation == null ? "Not found" : deletedViolation.toString()));
            System.out.println("------------------------");

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
