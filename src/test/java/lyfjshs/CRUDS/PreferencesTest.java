package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.DAO.PreferencesDAO;
import lyfjshs.gomis.Database.entity.PreferencesEntity;

public class PreferencesTest {
    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Initialize PreferencesDAO
            PreferencesDAO preferencesDAO = new PreferencesDAO(connection);

            // ✅ Test Insert Preference
            System.out.println("Inserting Preference...");
            PreferencesEntity newPreference = new PreferencesEntity(
                    0, // ID (auto-generated)
                    1, // user_id
                    "Theme", // category
                    "Color", // key
                    "Dark Mode", // value
                    "String", // data_type
                    null, // file
                    null, // created_at
                    null // updated_at
            );
            boolean insertSuccess = preferencesDAO.createPreference(newPreference);
            System.out.println("Insert Success: " + insertSuccess);

            // ✅ Retrieve all preferences for user ID 1
            System.out.println("\nRetrieving preferences for user ID 1...");
            List<PreferencesEntity> preferences = preferencesDAO.getPreferencesByUserId(1);
            preferences.forEach(p -> System.out.println(
                    "ID: " + p.getId() + ", Category: " + p.getCategory() + ", Key: " + p.getKey() + ", Value: " + p.getValue()
            ));

            // ✅ Find a Preference ID before updating or deleting
            System.out.println("\nFinding preference ID...");
            int preferenceId = preferencesDAO.findPreferenceId(1, "Theme", "Color");
            System.out.println("Found Preference ID: " + preferenceId);

            if (preferenceId != -1) {
                // ✅ Update preference
                System.out.println("\nUpdating PREFERENCE ID " + preferenceId + "...");
                PreferencesEntity updatedPreference = new PreferencesEntity(
                        preferenceId, 1, "Theme", "Color", "Light Mode", "String", null, null, null);
                boolean updateSuccess = preferencesDAO.updatePreference(updatedPreference);
                System.out.println("Update Success: " + updateSuccess);

                // ✅ Delete preference
                System.out.println("\nDeleting PREFERENCE ID " + preferenceId + "...");
                boolean deleteSuccess = preferencesDAO.deletePreference(preferenceId);
                System.out.println("Delete Success: " + deleteSuccess);
            } else {
                System.out.println("Preference not found.");
            }

            // ✅ Retrieve all preferences after deletion
            System.out.println("\nRetrieving all preferences after deletion...");
            preferences = preferencesDAO.getPreferencesByUserId(1);
            preferences.forEach(p -> System.out.println(
                    "ID: " + p.getId() + ", Category: " + p.getCategory() + ", Key: " + p.getKey() + ", Value: " + p.getValue()
            ));
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
}
