package lyfjshs.gomis.components.settings;

import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
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

	// Constants for Good Moral Certificate settings
	private static final String PREF_DEPED_SEAL = "good_moral_deped_seal";
	private static final String PREF_DEPED_MATATAG = "good_moral_deped_matatag";
	private static final String PREF_LYFJSHS_LOGO = "good_moral_lyfjshs_logo";
	private static final String PREF_GOOD_MORAL_SIGNER = "good_moral_signer";
	private static final String PREF_GOOD_MORAL_POSITION = "good_moral_position";

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
		loadGoodMoralSettings(); // Load Good Moral Certificate settings
		notifyListeners();
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

	private static void loadGoodMoralSettings() {
		try {
			// Load images
			String[] imageKeys = {PREF_DEPED_SEAL, PREF_DEPED_MATATAG, PREF_LYFJSHS_LOGO};
			String sql = "SELECT PREF_KEY, PREF_FILE FROM PREFERENCES WHERE PREF_KEY = ?";
			
			for (String key : imageKeys) {
				try (PreparedStatement stmt = connection.prepareStatement(sql)) {
					stmt.setString(1, key);
					try (ResultSet rs = stmt.executeQuery()) {
						if (rs.next()) {
							byte[] imageBytes = rs.getBytes("PREF_FILE");
							if (imageBytes != null) {
								BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
								switch (key) {
									case PREF_DEPED_SEAL:
										currentState.depedSealImage = image;
										break;
									case PREF_DEPED_MATATAG:
										currentState.depedMatatagImage = image;
										break;
									case PREF_LYFJSHS_LOGO:
										currentState.lyfjshsLogoImage = image;
										break;
								}
							}
						}
					}
				}
			}

			// Load signer and position
			sql = "SELECT PREF_KEY, PREF_VALUE FROM PREFERENCES WHERE PREF_KEY IN (?, ?)";
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, PREF_GOOD_MORAL_SIGNER);
				stmt.setString(2, PREF_GOOD_MORAL_POSITION);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						String key = rs.getString("PREF_KEY");
						String value = rs.getString("PREF_VALUE");
						if (PREF_GOOD_MORAL_SIGNER.equals(key)) {
							currentState.goodMoralSigner = value;
						} else if (PREF_GOOD_MORAL_POSITION.equals(key)) {
							currentState.goodMoralPosition = value;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveGoodMoralImage(String key, BufferedImage image) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			byte[] imageBytes = baos.toByteArray();

			String sql = "INSERT INTO PREFERENCES (PREF_KEY, PREF_FILE) VALUES (?, ?) " +
						"ON DUPLICATE KEY UPDATE PREF_FILE = ?";
			
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, key);
				stmt.setBytes(2, imageBytes);
				stmt.setBytes(3, imageBytes);
				stmt.executeUpdate();
			}

			// Update current state
			switch (key) {
				case PREF_DEPED_SEAL:
					currentState.depedSealImage = image;
					break;
				case PREF_DEPED_MATATAG:
					currentState.depedMatatagImage = image;
					break;
				case PREF_LYFJSHS_LOGO:
					currentState.lyfjshsLogoImage = image;
					break;
			}
			notifyListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveGoodMoralSigner(String signer, String position) {
		try {
			connection.setAutoCommit(false);
			String sql = "INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) " +
						"ON DUPLICATE KEY UPDATE PREF_VALUE = ?";
			
			// Save signer
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, PREF_GOOD_MORAL_SIGNER);
				stmt.setString(2, signer);
				stmt.setString(3, signer);
				stmt.executeUpdate();
			}

			// Save position
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, PREF_GOOD_MORAL_POSITION);
				stmt.setString(2, position);
				stmt.setString(3, position);
				stmt.executeUpdate();
			}

			connection.commit();

			// Update current state
			currentState.goodMoralSigner = signer;
			currentState.goodMoralPosition = position;
			notifyListeners();
		} catch (Exception e) {
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

	// Method to get Good Moral Certificate settings
	public static Image getGoodMoralImage(String key) {
		switch (key) {
			case PREF_DEPED_SEAL:
				return currentState.depedSealImage;
			case PREF_DEPED_MATATAG:
				return currentState.depedMatatagImage;
			case PREF_LYFJSHS_LOGO:
				return currentState.lyfjshsLogoImage;
			default:
				return null;
		}
	}

	public static String getGoodMoralSigner() {
		return currentState.goodMoralSigner;
	}

	public static String getGoodMoralPosition() {
		return currentState.goodMoralPosition;
	}

}