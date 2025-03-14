package lyfjshs.CRUDS;

import lyfjshs.gomis.Database.DAO.SchoolFormDAO;
import lyfjshs.gomis.Database.entity.SchoolForm;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class SfForm_Test {
    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            SchoolFormDAO schoolFormDAO = new SchoolFormDAO(conn);

            testCreateSchoolForm(schoolFormDAO);
            testGetAllSchoolForms(schoolFormDAO);
            testGetSchoolFormById(schoolFormDAO, 1);
            testUpdateSchoolForm(schoolFormDAO, 1);
            testDeleteSchoolForm(schoolFormDAO, 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testCreateSchoolForm(SchoolFormDAO dao) {
        SchoolForm schoolForm = new SchoolForm(0, "Sample School", "S12345", "District 1", "Division 1", "Region 1", "1st", "2024-2025", "Grade 11", "Section A", "ICT", "Programming");
        int id = dao.createSchoolForm(schoolForm);
        System.out.println(id > 0 ? "✔ School Form created with ID: " + id : "❌ Failed to create School Form");
    }

    private static void testGetAllSchoolForms(SchoolFormDAO dao) {
        List<SchoolForm> schoolForms = dao.getAllSchoolForms();
        System.out.println(schoolForms.isEmpty() ? "❌ No School Forms found" : "✔ Retrieved " + schoolForms.size() + " School Forms");
        schoolForms.forEach(System.out::println);
    }

    private static void testGetSchoolFormById(SchoolFormDAO dao, int id) {
        SchoolForm schoolForm = dao.getSchoolFormById(id);
        System.out.println(schoolForm != null ? "✔ Retrieved School Form: " + schoolForm : "❌ School Form not found");
    }

    private static void testUpdateSchoolForm(SchoolFormDAO dao, int id) {
        SchoolForm schoolForm = dao.getSchoolFormById(id);
        if (schoolForm != null) {
            schoolForm.setSF_SCHOOL_NAME("Updated School Name");
            boolean updated = dao.updateSchoolForm(schoolForm);
            System.out.println(updated ? "✔ School Form updated successfully" : "❌ Failed to update School Form");
        } else {
            System.out.println("❌ School Form not found for update");
        }
    }

    private static void testDeleteSchoolForm(SchoolFormDAO dao, int id) {
        boolean deleted = dao.deleteSchoolForm(id);
        System.out.println(deleted ? "✔ School Form deleted successfully" : "❌ Failed to delete School Form");
    }
}
