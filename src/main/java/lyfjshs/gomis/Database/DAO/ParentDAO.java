package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ParentDAO {

    // Insert a new parent record
    public boolean insertParent(Connection connection, String fatherLastName, String fatherFirstName, String fatherMiddleName, String motherLastName, String motherFirstName) {
        String sql = "INSERT INTO PARENT (father_lastname, father_firstname, father_middlename, mother_lastname, mother_firstname) VALUES (?, ?, ?, ?, ?)";
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
        String sql = "UPDATE PARENT SET father_lastname = ?, father_firstname = ?, father_middlename = ?, mother_lastname = ?, mother_firstname = ? WHERE parent_id = ?";
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

    // Delete a parent record by ID
    public boolean deleteParent(Connection connection, int parentId) {
        String sql = "DELETE FROM PARENT WHERE parent_id = ?";
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
                String parent = String.format(
                        "ID: %d, Last Name: %s, First Name: %s, Middle Name: %s",
                        resultSet.getInt("parent_id"),
                        resultSet.getString("father_lastname"),
                        resultSet.getString("father_firstname"),
                        resultSet.getString("father_middlename")
                );
                parents.add(parent);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving parents: " + e.getMessage());
        }
        return parents;
    }
}