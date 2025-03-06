package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;

public class GuidanceCounselorDB_Test {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            GuidanceCounselorDAO dao = new GuidanceCounselorDAO(conn);

            // Test Create
            testCreateGuidanceCounselor(conn, dao);

            // Test Read
            testReadGuidanceCounselor(conn, dao);

            // Test Delete
            testDeleteGuidanceCounselor(conn, dao);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testCreateGuidanceCounselor(Connection conn, GuidanceCounselorDAO dao) {
        GuidanceCounselor counselor = new GuidanceCounselor(4, "Doe", "John", "A", "Jr.", "Male", 
                "Psychology", "1234567890", "johndoe@example.com", "Senior Counselor", null);
        
        boolean result = dao.createGuidanceCounselor(conn, counselor);
        System.out.println(result ? "✔ testCreateGuidanceCounselor Passed" : "❌ testCreateGuidanceCounselor Failed");
    }

    private static void testReadGuidanceCounselor(Connection conn, GuidanceCounselorDAO dao) {
        int id = 1; // ID to read
        GuidanceCounselor counselor = dao.readGuidanceCounselor(conn, id);
        
        if (counselor != null) {
            System.out.println("✔ testReadGuidanceCounselor Passed: " + counselor.getFirstName() + " " + counselor.getLastName());
        } else {
            System.out.println("❌ testReadGuidanceCounselor Failed");
        }
    }

    private static void testDeleteGuidanceCounselor(Connection conn, GuidanceCounselorDAO dao) {
        int id = 4; // ID to delete
        boolean result = dao.deleteGuidanceCounselor(conn, id);
        
        System.out.println(result ? "✔ testDeleteGuidanceCounselor Passed" : "❌ testDeleteGuidanceCounselor Failed");
    }
}
