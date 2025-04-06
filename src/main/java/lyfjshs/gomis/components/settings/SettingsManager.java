package lyfjshs.gomis.components.settings;

import java.awt.Font;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class SettingsManager {

	private static Connection connection;
	private static SettingsState currentState;
	private static List<Consumer<SettingsState>> settingsListeners = new ArrayList<>();

	public SettingsManager(Connection connection) {
		this.connection = connection;
		loadSettings();
	}

	private final Map<String, String> settings = new HashMap<>();

	public void initializeAppSettings() {
		try {
			// Load settings from DB first
			loadSettings();

			// Apply the loaded settings
			if (currentState != null) {
				applyUIChanges(currentState);
				notifyListeners();
			} else {
				// Set defaults if no settings found
				currentState = new SettingsState("FlatLightLaf", "Segoe UI", 14, true);
				saveSettingsToDB(currentState);
				applyUIChanges(currentState);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void saveSettingsToDB(SettingsState state) {
		try {
			connection.setAutoCommit(false);
			String[] queries = {
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?",
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?",
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?",
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?",
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?",
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?",
				"INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?"
			};

			String[][] values = {
				{"theme", state.theme},
				{"font_style", state.fontStyle},
				{"font_size", String.valueOf(state.fontSize)},
				{"notifications", String.valueOf(state.notifications)},
				{"notification_time_minutes", String.valueOf(state.notificationTimeMinutes)},
				{"notify_on_missed", String.valueOf(state.notifyOnMissed)},
				{"notify_on_start", String.valueOf(state.notifyOnStart)}
			};

			for (int i = 0; i < queries.length; i++) {
				try (PreparedStatement stmt = connection.prepareStatement(queries[i])) {
					stmt.setString(1, values[i][0]); // key
					stmt.setString(2, values[i][1]); // value
					stmt.setString(3, values[i][1]); // value for update
					stmt.executeUpdate();
				}
			}
			connection.commit();
			
			// After saving, reload settings
			loadSettings();
			
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public SettingsState getSettingsState() {
		String theme = settings.getOrDefault("theme", "FlatLightLaf");
		String fontStyle = settings.getOrDefault("font_style", "FlatRobotoFont");
		int fontSize = Integer.parseInt(settings.getOrDefault("font_size", "13"));
		boolean notifications = Boolean.parseBoolean(settings.getOrDefault("notifications", "true"));
		
		// Add new notification settings
		int notificationTimeMinutes = Integer.parseInt(settings.getOrDefault("notification_time_minutes", "10"));
		boolean notifyOnMissed = Boolean.parseBoolean(settings.getOrDefault("notify_on_missed", "true"));
		boolean notifyOnStart = Boolean.parseBoolean(settings.getOrDefault("notify_on_start", "true"));
		
		return new SettingsState(theme, fontStyle, fontSize, notifications, 
			notificationTimeMinutes, notifyOnMissed, notifyOnStart);
	}
	
	   /**
     * Updates a setting in the database and applies changes immediately.
     * @param key   The setting key.
     * @param value The new value.
     */
	public static SettingsState updateSetting(String key, String value) {
		if (currentState == null) {
			loadSettings();
		}

		// Update current state
		switch (key) {
			case "theme":
				currentState.theme = value;
				break;
			case "font_style":
				currentState.fontStyle = value;
				break;
			case "font_size":
				currentState.fontSize = Integer.parseInt(value);
				break;
			case "notifications":
				currentState.notifications = Boolean.parseBoolean(value);
				break;
			case "notification_time_minutes":
				currentState.notificationTimeMinutes = Integer.parseInt(value);
				break;
			case "notify_on_missed":
				currentState.notifyOnMissed = Boolean.parseBoolean(value);
				break;
			case "notify_on_start":
				currentState.notifyOnStart = Boolean.parseBoolean(value);
				break;
		}

		// Save immediately to DB
		saveSettingsToDB(currentState);
		
		// Apply changes
		applySettings();
		notifyListeners();
		
		return currentState;
	}


    public static SettingsState getCurrentState() {
        return currentState;
    }

	public String getSetting(String key, String defaultValue) {
		return settings.getOrDefault(key, defaultValue);
	}

	private void applyUIChanges(SettingsState state) {
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
				// Main.gFrame.FULL_WINDOW_CONTENT(true);
				SwingUtilities.updateComponentTreeUI(window);
			}
		});
	}

	/**
	 * Loads settings from the database and caches them.
	 */
	private static void loadSettings() {
		currentState = getSettingsStateFromDB();
		notifyListeners(); // Notify listeners when settings are loaded
	}

	/**
	 * Retrieves settings from the database.
	 * 
	 * @return SettingsState object with loaded settings.
	 */
	private static SettingsState getSettingsStateFromDB() {
		String query = "SELECT PREF_KEY, PREF_VALUE FROM PREFERENCES";
		SettingsState state = new SettingsState("FlatLightLaf", "Segoe UI", 14, true);
		
		try (PreparedStatement stmt = connection.prepareStatement(query);
			 ResultSet rs = stmt.executeQuery()) {
				 
			while (rs.next()) {
				String key = rs.getString("PREF_KEY");
				String value = rs.getString("PREF_VALUE");
				
				switch (key) {
					case "theme":
						state.theme = value;
						break;
					case "font_style":
						state.fontStyle = value;
						break;
					case "font_size":
						state.fontSize = Integer.parseInt(value);
						break;
					case "notifications":
						state.notifications = Boolean.parseBoolean(value);
						break;
					case "notification_time_minutes":
						state.notificationTimeMinutes = Integer.parseInt(value);
						break;
					case "notify_on_missed":
						state.notifyOnMissed = Boolean.parseBoolean(value);
						break;
					case "notify_on_start":
						state.notifyOnStart = Boolean.parseBoolean(value);
						break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error loading settings from database: " + e.getMessage());
		}
		return state;
	}

	/**
	 * Applies theme, font, and UI updates globally.
	 */
	public static void applySettings() {
		if (currentState == null)
			loadSettings(); // Ensure settings are loaded

		try {
			switch (currentState.theme) {
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
				UIManager.setLookAndFeel(new FlatLightLaf());
				break; // Default fallback
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Apply font settings
		Font appFont = new Font(currentState.fontStyle, Font.PLAIN, currentState.fontSize);
		UIManager.put("defaultFont", appFont);

		// Update all open UI components
		SwingUtilities.invokeLater(() -> {
			for (Window window : Window.getWindows()) {
				SwingUtilities.updateComponentTreeUI(window);
			}
		});
	}

	public static void addSettingsListener(Consumer<SettingsState> listener) {
		settingsListeners.add(listener);
	}

	private static void notifyListeners() {
		for (Consumer<SettingsState> listener : settingsListeners) {
			listener.accept(currentState);
		}
	}

}