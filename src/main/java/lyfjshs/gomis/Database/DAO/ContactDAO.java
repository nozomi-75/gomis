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

        // CREATE Contact
    public int createContact(Contact contact) throws SQLException {
        String sql = "INSERT INTO CONTACT (CONTACT_NUMBER) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, contact.getContactNumber());
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean insertContact(Connection conn, Contact contact) {
        String query = "INSERT INTO CONTACT (CONTACT_NUMBER) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, contact.getContactNumber());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Read (Retrieve a contact by CONTACT_ID)
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
            e.printStackTrace();
        }
        return null;
    }

    // Update (Modify an existing contact)
    public boolean updateContact(Connection conn,int CONTACT_ID, Contact newContact) {
        String query = "UPDATE CONTACT SET CONTACT_NUMBER = ? WHERE CONTACT_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newContact.getContactNumber());
            stmt.setInt(2, CONTACT_ID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete (Remove a contact)
    public boolean deleteContact(Connection conn, int CONTACT_ID) {
        String query = "DELETE FROM CONTACT WHERE CONTACT_ID = ?";
        try ( PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, CONTACT_ID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
