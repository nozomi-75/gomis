package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import lyfjshs.gomis.Database.entity.Contact;

public class ContactDAO {
    private final Connection connection;

    public ContactDAO(Connection connection) {
        this.connection = connection;
    }

        // CREATE: Insert a new contact and return the generated ID
    public int createContact(Contact contact) throws SQLException {
        String sql = "INSERT INTO CONTACT (CONTACT_NUMBER) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, contact.getContactNumber());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

     // CREATE: Insert a contact with an existing connection
    public boolean insertContact(Connection conn, Contact contact) {
        String query = "INSERT INTO CONTACT (CONTACT_NUMBER) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, contact.getContactNumber());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return false;
    }

    // READ: Retrieve a contact by CONTACT_ID
    public Contact getContactById(Connection conn, int CONTACT_ID) {
        String query = "SELECT * FROM CONTACT WHERE CONTACT_ID = ?";
        try (
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, CONTACT_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Contact(rs.getInt("CONTACT_ID"), rs.getString("CONTACT"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return null;
    }

    public Contact getContactByStudentId(int studentId) throws SQLException {
        String query = "SELECT c.* FROM CONTACT c "
                + "INNER JOIN STUDENT s ON s.CONTACT_ID = c.CONTACT_ID "
                + "WHERE s.STUDENT_UID = ?";
                
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Contact(
                        rs.getInt("CONTACT_ID"),
                        rs.getString("CONTACT_NUMBER")
                    );
                }
            }
        }
        return null;
    }

    // UPDATE: Modify an existing contact
    public boolean updateContact(Connection conn,int CONTACT_ID, Contact newContact) {
        String query = "UPDATE CONTACT SET CONTACT_NUMBER = ? WHERE CONTACT_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newContact.getContactNumber());
            stmt.setInt(2, CONTACT_ID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return false;
    }

    // DELETE: Remove a contact by CONTACT_ID
    public boolean deleteContact(Connection conn, int CONTACT_ID) {
        String query = "DELETE FROM CONTACT WHERE CONTACT_ID = ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, CONTACT_ID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Consider using a logging framework
        }
        return false;
    }
}
