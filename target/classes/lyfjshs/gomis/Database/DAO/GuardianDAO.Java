package lyfjshs.gomis.Database.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lyfjshs.gomis.Database.model.Guardian;  

public class GuardianDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/your_database";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    // CREATE (Insert a new guardian)
    public boolean insertGuardian(String studentId, String lastName, String firstName, String middleName, String relationship) {
        String query = "INSERT INTO GUARDIAN (STUDENT_ID, GUARDIAN_LASTNAME, GUARDIAN_FIRSTNAME, GUARDIAN_MIDDLENAME, GUARDIAN_RELATIONSHIP) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, studentId);
            stmt.setString(2, lastName);
            stmt.setString(3, firstName);
            
            // Handle NULL values for middleName
            if (middleName != null && !middleName.isEmpty()) {
                stmt.setString(4, middleName);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setString(5, relationship);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // READ (Retrieve a guardian by ID)
    public Guardian getGuardianById(int guardianId) {
        String query = "SELECT * FROM GUARDIAN WHERE GUARDIAN_ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, guardianId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Guardian(
                        rs.getInt("GUARDIAN_ID"),
                        rs.getString("STUDENT_ID"),
                        rs.getString("GUARDIAN_LASTNAME"),
                        rs.getString("GUARDIAN_FIRSTNAME"),
                        rs.getString("GUARDIAN_MIDDLENAME"),
                        rs.getString("GUARDIAN_RELATIONSHIP")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // READ ALL (Retrieve all guardians)
    public List<Guardian> getAllGuardians() {
        List<Guardian> guardians = new ArrayList<>();
        String query = "SELECT * FROM GUARDIAN";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                guardians.add(new Guardian(
                    rs.getInt("GUARDIAN_ID"),
                    rs.getString("STUDENT_ID"),
                    rs.getString("GUARDIAN_LASTNAME"),
                    rs.getString("GUARDIAN_FIRSTNAME"),
                    rs.getString("GUARDIAN_MIDDLENAME"),
                    rs.getString("GUARDIAN_RELATIONSHIP")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guardians;
    }

    // UPDATE (Modify an existing guardian)
    public boolean updateGuardian(int guardianId, String lastName, String firstName, String middleName, String relationship) {
        String query = "UPDATE GUARDIAN SET GUARDIAN_LASTNAME = ?, GUARDIAN_FIRSTNAME = ?, GUARDIAN_MIDDLENAME = ?, GUARDIAN_RELATIONSHIP = ? WHERE GUARDIAN_ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, lastName);
            stmt.setString(2, firstName);
            
            // Handle NULL values for middleName
            if (middleName != null && !middleName.isEmpty()) {
                stmt.setString(3, middleName);
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setString(4, relationship);
            stmt.setInt(5, guardianId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // DELETE (Remove a guardian)
    public boolean deleteGuardian(int guardianId) {
        String query = "DELETE FROM GUARDIAN WHERE GUARDIAN_ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, guardianId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
