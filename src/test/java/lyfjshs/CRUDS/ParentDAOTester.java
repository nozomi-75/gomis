package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.DAO.ParentsDAO;

public class ParentDAOTester {
    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Initialize ParentsDAO
            ParentsDAO parentsDAO = new ParentsDAO(connection);

            // ✅ Test Insert Parent
            System.out.println("Inserting Parent...");
            boolean insertSuccess = parentsDAO.insertParent(
                connection, 
                "Doe", "John", "C.", "123456789", // Father's details
                "Doe", "Jane", "D", "1023456438"  // Mother's details
            );
            System.out.println("Insert Success: " + insertSuccess);

            // ✅ Retrieve all parents
            System.out.println("\nRetrieving all parents...");
            List<String> parents = parentsDAO.getAllParents();
            parents.forEach(System.out::println);

            // ✅ Test Update Parent (assuming ID 1 exists)
            System.out.println("\nUpdating PARENT ID 1...");
            boolean updateSuccess = parentsDAO.updateParent(
                connection, 
                1, 
                "Doe", "Johnny", "B.", "987654321", // Updated Father's details
                "Doe", "Janet", "D", "1023456438"   // Updated Mother's details
            );
            System.out.println("Update Success: " + updateSuccess);

            // ✅ Test Delete Parent (assuming ID 1 exists)
            System.out.println("\nDeleting PARENT ID 1...");
            boolean deleteSuccess = parentsDAO.deleteParent(connection, 1);
            System.out.println("Delete Success: " + deleteSuccess);

            // ✅ Retrieve all parents after deletion
            System.out.println("\nRetrieving all parents after deletion...");
            parents = parentsDAO.getAllParents();
            parents.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}
