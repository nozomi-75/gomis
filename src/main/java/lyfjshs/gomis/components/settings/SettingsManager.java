package lyfjshs.gomis.components.settings;

import java.awt.Font;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class SettingsManager {

    private  Connection connection;

    public SettingsManager(Connection connection) {
    	this.connection = connection;
        loadSettings();
    }

    private final  Map<String, String> settings = new HashMap<>();

    private void loadSettings() {
        String query = "SELECT * FROM PREFERENCES";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                settings.put(rs.getString("PREF_KEY"), rs.getString("PREF_VALUE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public  void initializeAppSettings() {
        SettingsState state = getSettingsState();
        applyUIChanges(state);
    }

    public  SettingsState getSettingsState() {	
        String theme = settings.getOrDefault("theme", "FlatLightLaf");
        String fontStyle = settings.getOrDefault("font_style", "FlatRobotoFont");
        int fontSize = Integer.parseInt(settings.getOrDefault("font_size", "13"));
        boolean notifications = Boolean.parseBoolean(settings.getOrDefault("notifications", "true"));
        return new SettingsState(theme, fontStyle, fontSize, notifications);
    }

    public  SettingsState updateSetting(String key, String value) {
        settings.put(key, value);
        String query = "INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) " +
                       "ON DUPLICATE KEY UPDATE PREF_VALUE = ?";
        try (
             PreparedStatement stmt = connection.prepareStatement(query);
             ) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, value);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SettingsState state = getSettingsState();
        applyUIChanges(state);
        return state;
    }

    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    private  void applyUIChanges(SettingsState state) {
        try {
            // Directly set the look and feel without animation
            switch (state.theme) {
                case "FlatDarkLaf":
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    break;
                case "FlatLightLaf":
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    break;
                case "FlatMacDarkLaf":
                    UIManager.setLookAndFeel(new FlatMacDarkLaf());
                    break;
                case "FlatMacLightLaf":
                    UIManager.setLookAndFeel(new FlatMacLightLaf());
                    break;
                case "FlatDarculaLaf":
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    break;
                case "FlatIntelliJLaf":
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
                default:
                    UIManager.setLookAndFeel(new FlatLightLaf()); // Fallback
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Font appFont = new Font(state.fontStyle, Font.PLAIN, state.fontSize);
        UIManager.put("defaultFont", appFont);
        
        // Update all UI components immediately
        SwingUtilities.invokeLater(() -> {
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        });
    }
}