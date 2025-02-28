package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.model.Violation;

public class ViolationDB_Test {
    public static void main(String[] args) {
        // Database connection parameters
        String url = "jdbc:mariadb://localhost:3306/gomisDB";
        String user = "root";
        String password = "YourRootPassword123!"; // Update with actual password

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ViolationCRUD violationCRUD = new ViolationCRUD(conn); //Create an instance

            // Test CREATE operation
            System.out.println("Testing CREATE operation:");
            boolean createSuccess = ViolationCRUD.createViolation(conn, "1234567890", "Bullying", "Physical bullying", "John", "Doe", "john.doe@example.com", "123-456-7890", "Student", "Counseling", "Pending");
            System.out.println("Create operation successful: " + createSuccess);
            System.out.println("------------------------");


            // Test READ operation - Get all violations
            System.out.println("Testing READ operation (getAllViolations):");
            List<Violation> violations = ViolationCRUD.getAllViolations(conn);
            System.out.println("Violations found: " + violations.size());
            for (Violation v : violations) {
                System.out.println("Violation: " + v);
            }
            System.out.println("------------------------");

            // Test READ operation - Get violation by ID (assuming ID 1 exists)  -  This needs to be adjusted based on the create operation.  It will not find it with ID 1.
            int lastViolationId = violations.isEmpty() ? 0 : violations.get(violations.size()-1).getViolationId();
            if (lastViolationId > 0) {
                System.out.println("Testing READ operation (getViolationById):");
                Violation violation = violationCRUD.getViolationById(conn, lastViolationId); // Use getViolationById that takes a connection
                if (violation != null) {
                    System.out.println("Found violation: " + violation);
                } else {
                    System.out.println("No violation found with ID " + lastViolationId);
                }
                System.out.println("------------------------");
            }

            // Test UPDATE operation (assuming ID exists)
            if (lastViolationId > 0) {
                System.out.println("Testing UPDATE operation:");
                violationCRUD.updateViolationStatus(conn, lastViolationId, "Resolved");
                System.out.println("Update operation successful");

                // Verify update
                Violation updatedViolation = violationCRUD.getViolationById(conn, lastViolationId); // Use getViolationById that takes a connection
                if (updatedViolation != null) {
                    System.out.println("Updated violation: " + updatedViolation);
                }
                System.out.println("------------------------");
            }

            // Test DELETE operation (assuming ID exists)
            if (lastViolationId > 0) {
                System.out.println("Testing DELETE operation:");
                boolean deleteSuccess = violationCRUD.deleteViolation(conn, lastViolationId);
                System.out.println("Delete operation successful: " + deleteSuccess);

                // Verify deletion
                Violation deletedViolation = violationCRUD.getViolationById(conn, lastViolationId); // Use getViolationById that takes a connection
                System.out.println("Violation after deletion: " + (deletedViolation == null ? "Not found" : deletedViolation));
                System.out.println("------------------------");
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
