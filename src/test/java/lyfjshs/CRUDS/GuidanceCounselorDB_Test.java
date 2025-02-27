package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.model.GuidanceCounselor;

public class GuidanceCounselorDB_Test {
    private static final String URL = "jdbc:mariadb://localhost:3306/your_database";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to the database.");
            
            // Test Insert
            GuidanceCounselor counselor = new GuidanceCounselor(1, "Doe", "John", "A", "Jr.", "Male", "Psychology", "1234567890", "john.doe@example.com", "Senior Counselor", null);
            GuidanceCounselorDAO.createGuidanceCounselor(conn, counselor.getId(), counselor.getLAST_NAME(), 
                    counselor.getFIRST_NAME(), counselor.getMiddleInitial(), counselor.getSuffix(), 
                    counselor.getGender(), counselor.getSpecialization(), counselor.getContactNumber(), 
                    counselor.getEmail(), counselor.getPosition(), counselor.getProfilePicture());
            
            // Test Read
            System.out.println("Reading inserted counselor:");
            GuidanceCounselorDAO.readGuidanceCounselor(conn, 1);
            
            // Test Batch Insert
            List<GuidanceCounselor> counselorList = new ArrayList<>();
            counselorList.add(new GuidanceCounselor(2, "Smith", "Alice", "B", "", "Female", "Counseling", "9876543210", "alice.smith@example.com", "Assistant Counselor", null));
            counselorList.add(new GuidanceCounselor(3, "Brown", "Charlie", "C", "", "Non-Binary", "Career Coaching", "5556667777", "charlie.brown@example.com", "Career Advisor", null));
            
            GuidanceCounselorDAO.createGuidanceCounselorsBatch(conn, counselorList);
            
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}