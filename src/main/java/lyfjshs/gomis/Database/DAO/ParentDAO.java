package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ParentDAO {

    private static final String URL = "jdbc:mariadb://localhost:3306/your_database_name";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    // Insert a new parent record
    public boolean insertParent(String lastName, String firstName, String middleName) {
        String sql = "INSERT INTO PARENT (PARENT_LASTNAME, PARENT_FIRSTNAME, PARENT_MIDDLENAME) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lastName);
            statement.setString(2, firstName);
            statement.setString(3, middleName);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting parent: " + e.getMessage());
        }
        return false;
    }

    // Update a parent's details by ID
    public boolean updateParent(int parentId, String lastName, String firstName, String middleName) {
        String sql = "UPDATE PARENT SET PARENT_LASTNAME = ?, PARENT_FIRSTNAME = ?, PARENT_MIDDLENAME = ? WHERE PARENT_ID = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lastName);
            statement.setString(2, firstName);
            statement.setString(3, middleName);
            statement.setInt(4, parentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating parent: " + e.getMessage());
        }
        return false;
    }

    // Delete a parent record by ID
    public boolean deleteParent(int parentId) {
        String sql = "DELETE FROM PARENT WHERE PARENT_ID = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting parent: " + e.getMessage());
        }
        return false;
    }

    // Retrieve all parents
    public List<String> getAllParents() {
        List<String> parents = new ArrayList<>();
        String sql = "SELECT * FROM PARENT";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String parent = String.format(
                        "ID: %d, Last Name: %s, First Name: %s, Middle Name: %s",
                        resultSet.getInt("PARENT_ID"),
                        resultSet.getString("PARENT_LASTNAME"),
                        resultSet.getString("PARENT_FIRSTNAME"),
                        resultSet.getString("PARENT_MIDDLENAME")
                );
                parents.add(parent);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving parents: " + e.getMessage());
        }
        return parents;
    }
}

