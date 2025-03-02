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
        ParentsDAO parentDAO = new ParentsDAO();
        
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Test Insert Parent
            System.out.println("Inserting Parent...");
            boolean insertSuccess = parentDAO.insertParent(connection, "Doe", "John", "A.", "Smith", "Jane");
            System.out.println("Insert Success: " + insertSuccess);

            // Retrieve all parents
            System.out.println("Retrieving all parents...");
            List<String> parents = parentDAO.getAllParents(connection);
            parents.forEach(System.out::println);

            // Test Update PARENTS (assuming ID 1 exists)
            System.out.println("Updating PARENTS ID 1...");
            boolean updateSuccess = parentDAO.updateParent(connection, 1, "Doe", "Johnny", "B.", "Smith", "Janet");
            System.out.println("Update Success: " + updateSuccess);

            // Test Delete PARENTS (assuming ID 1 exists)
            System.out.println("Deleting PARENTS ID 1...");
            boolean deleteSuccess = parentDAO.deleteParent(connection, 1);
            System.out.println("Delete Success: " + deleteSuccess);

            // Retrieve all parents again to confirm deletion
            System.out.println("Retrieving all parents after deletion...");
            parents = parentDAO.getAllParents(connection);
            parents.forEach(System.out::println);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}