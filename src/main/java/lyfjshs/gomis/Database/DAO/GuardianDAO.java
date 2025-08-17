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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Guardian;

public class GuardianDAO {
    private final Connection connection;
    
    // Constructor that initializes the database connection
    public GuardianDAO(Connection connection) {
        this.connection = connection;
    }
    // CREATE Guardian: Inserts a new guardian record into the database
    public int createGuardian(Guardian guardian) throws SQLException {
        String sql = "INSERT INTO GUARDIAN (GUARDIAN_LASTNAME, GUARDIAN_FIRST_NAME, GUARDIAN_MIDDLE_NAME, " +
                "GUARDIAN_RELATIONSHIP, GUARDIAN_CONTACT_NUMBER) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, guardian.getGuardianLastname());
            pstmt.setString(2, guardian.getGuardianFirstname());
            pstmt.setString(3, guardian.getGuardianMiddlename());
            pstmt.setString(4, guardian.getGuardianRelationship());
            pstmt.setString(5, guardian.getGuardianContactNumber());
            pstmt.executeUpdate();

             // Retrieves the generated ID for the newly inserted record
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // READ: Retrieves a guardian by their unique ID
    public Guardian getGuardianById(int guardianId) {
        String query = "SELECT * FROM GUARDIAN WHERE GUARDIAN_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, guardianId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Guardian(
                            rs.getInt("GUARDIAN_ID"),
                            rs.getString("GUARDIAN_LASTNAME"),
                            rs.getString("GUARDIAN_FIRST_NAME"),
                            rs.getString("GUARDIAN_MIDDLE_NAME"),
                            rs.getString("GUARDIAN_RELATIONSHIP"),
                            rs.getString("GAURDIAN_CONTACT_NUMBER"));                } // Typo in column name
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a proper logging mechanism
        }
        return null; // Returns null if no guardian is found
    }

    // READ ALL: Retrieves all guardian records from the database
    public List<Guardian> getAllGuardians() {
        List<Guardian> guardians = new ArrayList<>();
        String query = "SELECT * FROM GUARDIAN";
        try (PreparedStatement stmt = connection.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                guardians.add(new Guardian(
                        rs.getInt("GUARDIAN_ID"),
                        rs.getString("GUARDIAN_LASTNAME"),
                        rs.getString("GUARDIAN_FIRST_NAME"),
                        rs.getString("GUARDIAN_MIDDLE_NAME"),
                        rs.getString("GUARDIAN_RELATIONSHIP"),
                        rs.getString("GUARDIAN_CONTACT_NUMBER")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guardians;   // Returns an empty list if no records are found
    }

    // INSERT: Adds a new guardian to the database
   public boolean insertGuardian(String lastName, String firstName,
        String middleName, String relationship, String contactNumber) {
    String query = "INSERT INTO GUARDIAN (GUARDIAN_LASTNAME, GUARDIAN_FIRST_NAME, GUARDIAN_MIDDLE_NAME, GUARDIAN_RELATIONSHIP, GUARDIAN_CONTACT_NUMBER) VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, lastName);
        stmt.setString(2, firstName);

        // Handles NULL values for middleName
        if (middleName != null && !middleName.isEmpty()) {
            stmt.setString(3, middleName);
        } else {
            stmt.setNull(3, Types.VARCHAR);
        }

        stmt.setString(4, relationship);

        // Sets contact number
        if (contactNumber != null && !contactNumber.isEmpty()) {
            stmt.setString(5, contactNumber);
        } else {
            stmt.setNull(5, Types.VARCHAR);
        }

        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

// UPDATE: Modifies an existing guardian's details
public boolean updateGuardian(int guardianId, String lastName, String firstName,
        String middleName, String relationship, String contactNumber) {
    String query = "UPDATE GUARDIAN SET GUARDIAN_LASTNAME = ?, GUARDIAN_FIRST_NAME = ?, GUARDIAN_MIDDLE_NAME = ?, GUARDIAN_RELATIONSHIP = ?, GUARDIAN_CONTACT_NUMBER = ? WHERE GUARDIAN_ID = ?";
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setString(1, lastName);
        stmt.setString(2, firstName);

        // Handles NULL values for middleName
        if (middleName != null && !middleName.isEmpty()) {
            stmt.setString(3, middleName);
        } else {
            stmt.setNull(3, Types.VARCHAR);
        }

        stmt.setString(4, relationship);

        // Sets contact number
        if (contactNumber != null && !contactNumber.isEmpty()) {
            stmt.setString(5, contactNumber);
        } else {
            stmt.setNull(5, Types.VARCHAR);
        }

        stmt.setInt(6, guardianId);
        return stmt.executeUpdate() > 0;    // Returns true if update is successful
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}


    // DELETE: Removes a guardian from the database
    public boolean deleteGuardian(int guardianId) {
        String query = "DELETE FROM GUARDIAN WHERE GUARDIAN_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, guardianId);
            return stmt.executeUpdate() > 0;    // Returns true if deletion is successful
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
