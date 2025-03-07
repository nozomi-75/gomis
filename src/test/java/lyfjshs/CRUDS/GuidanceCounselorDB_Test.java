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
            testCreateGuidanceCounselor(dao);

            // Test Read
            testReadGuidanceCounselor(dao);

            // Test Delete
            // testDeleteGuidanceCounselor(dao);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testCreateGuidanceCounselor(GuidanceCounselorDAO dao) {
        GuidanceCounselor counselor = new GuidanceCounselor(4, "Doe", "John", "Anderson", "Jr.", "Male", 
                "Psychology", "1234567890", "johndoe@example.com", "Senior Counselor", null);

        
        boolean result = dao.createGuidanceCounselor(counselor);
        System.out.println(result ? "✔ testCreateGuidanceCounselor Passed" : "❌ testCreateGuidanceCounselor Failed");
    }

    private static void testReadGuidanceCounselor(GuidanceCounselorDAO dao) {
        int id = 4;
        GuidanceCounselor counselor = dao.readGuidanceCounselor(id);
        
        System.out.println(counselor != null ? "✔ testReadGuidanceCounselor Passed" : "❌ testReadGuidanceCounselor Failed");
    }

    private static void testDeleteGuidanceCounselor(GuidanceCounselorDAO dao) {
        int id = 4;
        boolean result = dao.deleteGuidanceCounselor(id);
        
        System.out.println(result ? "✔ testDeleteGuidanceCounselor Passed" : "❌ testDeleteGuidanceCounselor Failed");
    }
}
