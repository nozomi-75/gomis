package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.PreferencesEntity;

public class PreferencesDAO {

    private final Connection connection;

    public PreferencesDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * CREATE - Insert a new preference record into the database.
     * @param preference The PreferencesEntity object containing the preference details.
     * @return true if insertion is successful, false otherwise.
     */
    public boolean createPreference(PreferencesEntity preference) {
        String sql = "INSERT INTO PREFERENCES (user_id, category, PREFERENCES_KEY, value, data_type, file) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, preference.getUserId(), Types.INTEGER);
            ps.setString(2, preference.getCategory());
            ps.setString(3, preference.getKey());
            ps.setString(4, preference.getValue());
            ps.setString(5, preference.getDataType());
            ps.setBytes(6, preference.getFile());

            return ps.executeUpdate() > 0; // Returns true if insertion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * READ - Retrieve a preference by its ID.
     * @param id The ID of the preference.
     * @return PreferencesEntity object if found, otherwise null.
     */
    public PreferencesEntity getPreferenceById(int id) {
        String sql = "SELECT * FROM PREFERENCES WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PreferencesEntity(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("category"),
                        rs.getString("PREFERENCES_KEY"),
                        rs.getString("value"),
                        rs.getString("data_type"),
                        rs.getBytes("file"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * READ - Retrieve all preferences for a specific user.
     * @param userId The ID of the user.
     * @return List of PreferencesEntity objects.
     */
    public List<PreferencesEntity> getPreferencesByUserId(int userId) {
        List<PreferencesEntity> preferences = new ArrayList<>();
        String sql = "SELECT * FROM PREFERENCES WHERE user_id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                preferences.add(new PreferencesEntity(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("category"),
                        rs.getString("PREFERENCES_KEY"),
                        rs.getString("value"),
                        rs.getString("data_type"),
                        rs.getBytes("file"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("updated_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preferences;
    }

    /**
     * SEARCH - Find a preference ID based on user_id, category, and key.
     * @param userId The ID of the user.
     * @param category The category of the preference.
     * @param key The key of the preference.
     * @return The preference ID if found, otherwise -1.
     */
    public int findPreferenceId(int userId, String category, String key) {
        String sql = "SELECT id FROM PREFERENCES WHERE user_id = ? AND category = ? AND PREFERENCES_KEY = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ps.setString(3, key);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if no matching preference is found
    }


    /**
     * UPDATE - Update an existing preference record.
     * @param preference The PreferencesEntity object containing updated data.
     * @return true if update is successful, false otherwise.
     */
    public boolean updatePreference(PreferencesEntity preference) {
        String sql = "UPDATE PREFERENCES SET user_id = ?, category = ?, PREFERENCES_KEY= ?, value = ?, data_type = ?, file = ? WHERE id = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, preference.getUserId(), Types.INTEGER);
            ps.setString(2, preference.getCategory());
            ps.setString(3, preference.getKey());
            ps.setString(4, preference.getValue());
            ps.setString(5, preference.getDataType());
            ps.setBytes(6, preference.getFile());
            ps.setInt(7, preference.getId());

            return ps.executeUpdate() > 0; // Returns true if update was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * DELETE - Remove a preference by its ID.
     * @param id The ID of the preference.
     * @return true if deletion is successful, false otherwise.
     */
    public boolean deletePreference(int id) {
        String sql = "DELETE FROM PREFERENCES WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0; // Returns true if deletion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
