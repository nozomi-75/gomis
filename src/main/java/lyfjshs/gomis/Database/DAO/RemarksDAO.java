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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Remarks;
public class RemarksDAO {
    private Connection connection;

    // Constructor to initialize connection
    public RemarksDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE (Insert a new remark)
    public boolean insertRemark(String studentId, String remarkText, Timestamp remarkDate) {
        String query = "INSERT INTO REMARK (STUDENT_ID, REMARK_TEXT, REMARK_DATE) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, remarkText);
            stmt.setTimestamp(3, remarkDate != null ? remarkDate : new Timestamp(System.currentTimeMillis()));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting remark: " + e.getMessage());
        }
        return false;
    }

    // READ (Retrieve a remark by ID)
    public Remarks getRemarkById(int remarkId) {
        String query = "SELECT * FROM REMARK WHERE REMARK_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, remarkId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Remarks(
                        rs.getInt("REMARK_ID"),
                        rs.getInt("STUDENT_ID"),
                        rs.getString("REMARK_TEXT"),
                        rs.getTimestamp("REMARK_DATE")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving remark: " + e.getMessage());
        }
        return null;
    }

    // READ ALL (Retrieve all remarks)
    public List<Remarks> getAllRemarks() {
        List<Remarks> remarks = new ArrayList<>();
        String query = "SELECT * FROM REMARK";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                remarks.add(new Remarks(
                    rs.getInt("REMARK_ID"),
                    rs.getInt("STUDENT_ID"),
                    rs.getString("REMARK_TEXT"),
                    rs.getTimestamp("REMARK_DATE")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all remarks: " + e.getMessage());
        }
        return remarks;
    }

    // UPDATE (Modify an existing remark)
    public boolean updateRemark(int remarkId, String newText, Timestamp newDate) {
        String query = "UPDATE REMARK SET REMARK_TEXT = ?, REMARK_DATE = ? WHERE REMARK_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newText);
            stmt.setTimestamp(2, newDate != null ? newDate : new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, remarkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating remark: " + e.getMessage());
        }
        return false;
    }

    // DELETE (Remove a remark)
    public boolean deleteRemark(int remarkId) {
        String query = "DELETE FROM REMARK WHERE REMARK_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, remarkId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting remark: " + e.getMessage());
        }
        return false;
    }
}
