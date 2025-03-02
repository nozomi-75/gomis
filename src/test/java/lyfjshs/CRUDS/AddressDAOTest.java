package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lyfjshs.gomis.Database.DAO.AddressDAO;
import lyfjshs.gomis.Database.entity.Address;
public class AddressDAOTest {

    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "YourRootPassword123!"; // Replace with your MySQL password

    public static void main(String[] args) {
        Connection connection = null;

        try {
            // Establish database connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Successfully connected to the database!");

            // Initialize AddressDAO
            AddressDAO addressDAO = new AddressDAO(connection);

            // Test CREATE (Add a new address)
            System.out.println("\nTesting CREATE...");
            boolean created = addressDAO.addAddress(
                "123",
                "Main Street",
                "Region I",
                "Ilocos Sur",
                "Vigan City",
                "Barangay I",
                "2700" // 4-digit zip code, fits VARCHAR(5)
            );
            System.out.println("Address created: " + created);
            Address addedAddress = addressDAO.getAllAddresses().get(addressDAO.getAllAddresses().size() - 1);
            System.out.println("Added Address: " + addedAddress);

            // Test READ (Retrieve the added address by ID)
            System.out.println("\nTesting READ...");
            int newAddressId = addedAddress.getAddressId();
            Address retrievedAddress = addressDAO.getAddressById(newAddressId);
            System.out.println("Retrieved Address: " + retrievedAddress);

            // Test UPDATE (Modify the retrieved address)
            System.out.println("\nTesting UPDATE...");
            boolean updated = addressDAO.updateAddress(
                newAddressId,
                "456",
                "Updated Street",
                "Region II",
                "Nueva Ecija",
                "Cabanatuan City",
                "Barangay II",
                "3100" // 4-digit zip code, fits VARCHAR(5)
            );
            System.out.println("Address updated: " + updated);
            Address updatedAddress = addressDAO.getAddressById(newAddressId);
            System.out.println("Updated Address: " + updatedAddress);

            // Test DELETE (Remove the address)
            System.out.println("\nTesting DELETE...");
            boolean deleted = addressDAO.deleteAddress(newAddressId);
            System.out.println("Address deleted: " + deleted);
            Address deletedAddress = addressDAO.getAddressById(newAddressId);
            System.out.println("Address after delete (should be null): " + deletedAddress);

        } catch (SQLException e) {
            System.err.println("Database connection or operation failed: " + e.getMessage());
            // Replace with SQLExceptionPane.showSQLException(e, "Database Test") if available
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed.");
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }
}