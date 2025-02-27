package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lyfjshs.gomis.Database.model.Contact;

public class ContactDAO {
    private static final String URL = "jdbc:mariadb://localhost:3306/your_database";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    // Create (Insert a new contact)
    public boolean insertContact(String contact) {
        String query = "INSERT INTO CONTACT (CONTACT) VALUES (?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, contact);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Read (Retrieve a contact by C_ID)
    public Contact getContactById(int c_id) {
        String query = "SELECT * FROM CONTACT WHERE C_ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, c_id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Contact(rs.getInt("C_ID"), rs.getString("CONTACT"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update (Modify an existing contact)
    public boolean updateContact(int c_id, String newContact) {
        String query = "UPDATE CONTACT SET CONTACT = ? WHERE C_ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newContact);
            stmt.setInt(2, c_id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete (Remove a contact)
    public boolean deleteContact(int c_id) {
        String query = "DELETE FROM CONTACT WHERE C_ID = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, c_id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    } 
}
