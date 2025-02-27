package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lyfjshs.gomis.Database.DAO.ContactDAO;
import lyfjshs.gomis.Database.model.Contact;

public class ContactDB_Test {
    public static void main(String[] args) {
        // Database connection parameters
        String url = "jdbc:mariadb://localhost:3306/your_database";
        String user = "root";
        String password = "YourRootPassword123!"; 

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ContactDAO contactDAO = new ContactDAO();

            // Test CREATE operation
            System.out.println("Testing CREATE operation:");
            boolean createSuccess = contactDAO.insertContact("test@example.com");
            System.out.println("Create operation successful: " + createSuccess);
            System.out.println("------------------------");

            // Test READ operation - Get contact by ID
            System.out.println("Testing READ operation:");
            Contact contact = contactDAO.getContactById(1);
            if (contact != null) {
                System.out.println("Found contact: " + contact.toString());
            } else {
                System.out.println("No contact found with ID 1");
            }
            System.out.println("------------------------");

            // Test UPDATE operation
            if (contact != null) {
                System.out.println("Testing UPDATE operation:");
                boolean updateSuccess = contactDAO.updateContact(1, "updated@example.com");
                System.out.println("Update operation successful: " + updateSuccess);
                Contact updatedContact = contactDAO.getContactById(1);
                if (updatedContact != null) {
                    System.out.println("Updated contact: " + updatedContact.toString());
                }
                System.out.println("------------------------");
            }

            // Test DELETE operation
            System.out.println("Testing DELETE operation:");
            boolean deleteSuccess = contactDAO.deleteContact(1);
            System.out.println("Delete operation successful: " + deleteSuccess);
            Contact deletedContact = contactDAO.getContactById(1);
            System.out.println("Contact after deletion: " + (deletedContact == null ? "Not found" : deletedContact.toString()));
            System.out.println("------------------------");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

