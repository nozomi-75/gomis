package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.DAO.LoginController;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;

public class SignUp_Test {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Test the complete sign-up process
            testSignUpAndLogin(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testSignUpAndLogin(Connection conn) {
        GuidanceCounselorDAO counselorDAO = new GuidanceCounselorDAO(conn);
        LoginController loginController = new LoginController(conn);

        // Step 1: Create a new Guidance Counselor
        GuidanceCounselor newCounselor = new GuidanceCounselor(
            0, // Auto-increment ID
            "Johnson",  // lastName
            "Robert",   // firstName
            "Michael",  // middleName
            "Sr",      // suffix
            "Male",    // gender
            "Behavioral Counseling", // specialization
            "0912345678", // contactNum
            "robert.johnson@school.edu", // email
            "Junior Counselor", // position
            null  // profilePicture
        );

        try {
            // Step 2: Create the counselor
            System.out.println("\nTesting Guidance Counselor Creation...");
            int counselorId = counselorDAO.createGuidanceCounselor(newCounselor);
            
            if (counselorId > 0) {
                System.out.println("✔ Guidance Counselor created successfully with ID: " + counselorId);
                newCounselor.setGuidanceCounselorId(counselorId); // Assign the generated ID
            } else {
                System.out.println("❌ Failed to create Guidance Counselor");
                return; // Stop further execution if counselor creation fails
            }

            // Step 3: Create user account
            System.out.println("\nTesting User Account Creation...");
            String username = "robert.johnson";
            String password = "admin";
            try {
                loginController.createUser(username, password, counselorId).executeUpdate();
                System.out.println("✔ User account created successfully");
            } catch (SQLException e) {
                System.out.println("❌ Failed to create user account: " + e.getMessage());
                return; // Stop further execution if user creation fails
            }

            // Step 4: Test login with new credentials
            System.out.println("\nTesting Login Validation...");
            boolean loginValid = loginController.isValidUser(username, password);
            if (loginValid) {
                System.out.println("✔ Login successful for user: " + username);
            } else {
                System.out.println("❌ Login failed for user: " + username);
            }

            // Step 5: Verify counselor details
            System.out.println("\nVerifying Counselor Details...");
            GuidanceCounselor retrievedCounselor = counselorDAO.readGuidanceCounselor(counselorId);
            if (retrievedCounselor != null) {
                System.out.println("✔ Counselor retrieval successful");
                System.out.println("Name: " + retrievedCounselor.getFirstName() + " " + retrievedCounselor.getLastName());
                System.out.println("Email: " + retrievedCounselor.getEmail());
                System.out.println("Position: " + retrievedCounselor.getPosition());
            } else {
                System.out.println("❌ Failed to retrieve counselor details");
            }

        } catch (Exception e) {
            System.out.println("❌ Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
