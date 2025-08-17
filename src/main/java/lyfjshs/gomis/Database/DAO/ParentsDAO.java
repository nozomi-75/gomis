/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Parents;

public class ParentsDAO {
    private final Connection connection;    // Database connection instance

    // Constructor to initialize DAO with a database connection
    public ParentsDAO(Connection connection) {
        this.connection = connection;
    }

    // Inserts a new parent record into the database and returns the generated ID
    public int createParents(Parents parents) throws SQLException {
        String sql = "INSERT INTO PARENTS (FATHER_FIRSTNAME, FATHER_LASTNAME, FATHER_MIDDLENAME, FATHER_CONTACT_NUMBER, "
                + "MOTHER_FIRSTNAME, MOTHER_LASTNAME, MOTHER_MIDDLE_NAME, MOTHER_CONTACT_NUMBER) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, parents.getFatherFirstname());
            pstmt.setString(2, parents.getFatherLastname());
            pstmt.setString(3, parents.getFatherMiddlename());
            pstmt.setString(4, parents.getFatherContactNumber());
            pstmt.setString(5, parents.getMotherFirstname());
            pstmt.setString(6, parents.getMotherLastname());
            pstmt.setString(7, parents.getMotherMiddlename());
            pstmt.setString(8, parents.getMotherContactNumber());
            
            int affectedRows = pstmt.executeUpdate();
            
            // Checks if the insertion was successful
            if (affectedRows == 0) {
                throw new SQLException("Creating parent failed, no rows affected.");
            }
            
            // Retrieves the generated parent ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating parent failed, no ID obtained.");
                }
            }
        }
    }


    // Inserts a new parent record into the database
    public boolean insertParent(String fatherLastName, String fatherFirstName,
                                String fatherMiddleName, String fatherContactNumber, 
                                String motherLastName, String motherFirstName, 
                                String motherMiddleName, String motherContactNumber) { 
        String sql = "INSERT INTO PARENTS (FATHER_LASTNAME, FATHER_FIRSTNAME, FATHER_MIDDLENAME, FATHER_CONTACT_NUMBER, " +
                    "MOTHER_LASTNAME, MOTHER_FIRSTNAME, MOTHER_MIDDLE_NAME, MOTHER_CONTACT_NUMBER) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fatherLastName);
            statement.setString(2, fatherFirstName);
            statement.setString(3, fatherMiddleName);
            statement.setString(4, fatherContactNumber); 
            statement.setString(5, motherLastName);
            statement.setString(6, motherFirstName);
            statement.setString(7, motherMiddleName);
            statement.setString(8, motherContactNumber); 
            
            
            return statement.executeUpdate() > 0;   // Returns true if insertion was successful
        } catch (SQLException e) {
            System.err.println("Error inserting parent: " + e.getMessage());
        }
        return false;
    }

     // Updates an existing parent's details in the database
    public boolean updateParent(int parentId, 
                                String fatherLastName, String fatherFirstName, String fatherMiddleName, 
                                String fatherContactNumber, 
                                String motherLastName, String motherFirstName, String motherMiddleName,
                                String motherContactNumber) { 
        String sql = "UPDATE PARENTS SET FATHER_LASTNAME = ?, FATHER_FIRSTNAME = ?, FATHER_MIDDLENAME = ?, FATHER_CONTACT_NUMBER = ?, " +
                    "MOTHER_LASTNAME = ?, MOTHER_FIRSTNAME = ?, MOTHER_MIDDLE_NAME = ?, MOTHER_CONTACT_NUMBER = ? " +
                    "WHERE PARENT_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fatherLastName);
            statement.setString(2, fatherFirstName);
            statement.setString(3, fatherMiddleName);
            statement.setString(4, fatherContactNumber); 
            statement.setString(5, motherLastName);
            statement.setString(6, motherFirstName);
            statement.setString(7, motherMiddleName);
            statement.setString(8, motherContactNumber); 
            statement.setInt(9, parentId);
            
            return statement.executeUpdate() > 0;   // Returns true if update was successful
        } catch (SQLException e) {
            System.err.println("Error updating parent: " + e.getMessage());
        }
        return false;
    }

    // Deletes a parent record by its ID
    public boolean deleteParent(int parentId) {
        String sql = "DELETE FROM PARENTS WHERE parent_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parentId);
            return statement.executeUpdate() > 0;   // Returns true if deletion was successful
        } catch (SQLException e) {
            System.err.println("Error deleting parent: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteParents(int parentId) throws SQLException {
        String sql = "DELETE FROM PARENTS WHERE PARENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Retrieves all parent records from the database
    public List<String> getAllParents() {
        List<String> parents = new ArrayList<>();
        String sql = "SELECT * FROM PARENTS"; // Query to retrieve all parents

        // Formats parent details into a readable string
        try (Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String parentDetails = String.format(
                        "ID: %d, Father: %s %s %s (Contact: %s), Mother: %s %s %s (Contact: %s)",
                        resultSet.getInt("PARENT_ID"),
                        resultSet.getString("FATHER_FIRSTNAME"),
                        resultSet.getString("FATHER_MIDDLENAME"),
                        resultSet.getString("FATHER_LASTNAME"),
                        resultSet.getString("FATHER_CONTACT_NUMBER"), // Added Father's contact number
                        resultSet.getString("MOTHER_FIRSTNAME"),
                        resultSet.getString("MOTHER_MIDDLE_NAME"),
                        resultSet.getString("MOTHER_LASTNAME"),
                        resultSet.getString("MOTHER_CONTACT_NUMBER") // Added Mother's contact number
                );
                parents.add(parentDetails);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving parents: " + e.getMessage());
        }
        return parents;
    }

}