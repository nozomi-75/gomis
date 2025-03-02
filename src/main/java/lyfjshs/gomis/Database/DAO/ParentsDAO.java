package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ParentsDAO {

    // Insert a new PARENTS record
    public boolean insertParent(Connection connection, String fatherLastName, String fatherFirstName, String fatherMiddleName, String motherLastName, String motherFirstName) {
        String sql = "INSERT INTO PARENTS (father_lastname, father_firstname, father_middlename, mother_lastname, mother_firstname) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fatherLastName);
            statement.setString(2, fatherFirstName);
            statement.setString(3, fatherMiddleName);
            statement.setString(4, motherLastName);
            statement.setString(5, motherFirstName);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting parent: " + e.getMessage());
        }
        return false;
    }

    // Update a parent's details by ID
    public boolean updateParent(Connection connection, int parentId, String fatherLastName, String fatherFirstName, String fatherMiddleName, String motherLastName, String motherFirstName) {
        String sql = "UPDATE PARENTS SET father_lastname = ?, father_firstname = ?, father_middlename = ?, mother_lastname = ?, mother_firstname = ? WHERE parent_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fatherLastName);
            statement.setString(2, fatherFirstName);
            statement.setString(3, fatherMiddleName);
            statement.setString(4, motherLastName);
            statement.setString(5, motherFirstName);
            statement.setInt(6, parentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating parent: " + e.getMessage());
        }
        return false;
    }

    // Delete a PARENTS record by ID
    public boolean deleteParent(Connection connection, int parentId) {
        String sql = "DELETE FROM PARENTS WHERE parent_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parentId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting parent: " + e.getMessage());
        }
        return false;
    }

    // Retrieve all parents
    public List<String> getAllParents(Connection connection) {
        List<String> parents = new ArrayList<>();
        String sql = "SELECT * FROM PARENT";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String PARENTS = String.format(
                        "ID: %d, Last Name: %s, First Name: %s, Middle Name: %s",
                        resultSet.getInt("PARENT_ID"),
                        resultSet.getString("FATHER_LASTNAME"),
                        resultSet.getString("FATHER_FIRSTNAME"),
                        resultSet.getString("FATHER_MIDDLENAME")
                );
                parents.add(PARENTS);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving parents: " + e.getMessage());
        }
        return parents;
    }
}