/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.settings;

import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
	// Replace branding keys with generic ones
	private static final String PREF_GENERIC_LOGO_1 = "good_moral_generic_logo_1";
	private static final String PREF_GENERIC_LOGO_2 = "good_moral_generic_logo_2";
	private static final String PREF_GENERIC_LOGO_3 = "good_moral_generic_logo_3";
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
				currentState = new SettingsState("FlatLightLaf", 14, true);
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
				{"font_size", String.valueOf(state.fontSize)},
				{"notifications", String.valueOf(state.notifications)},
				{"notification_time_minutes", String.valueOf(state.notificationTimeMinutes)},
				{"notify_on_missed", String.valueOf(state.notifyOnMissed)},
				{"notify_on_start", String.valueOf(state.notifyOnStart)},
				{"sound_enabled", String.valueOf(state.soundEnabled)},
				{"sound_file", state.soundFile}
			};

			for (String[] entry : values) {
				try (PreparedStatement stmt = connection.prepareStatement(queries[0])) { // Use the first query template
					stmt.setString(1, entry[0]); // key
					stmt.setString(2, entry[1]); // value
					stmt.setString(3, entry[1]); // value for update
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
		int fontSize = Integer.parseInt(settings.getOrDefault("font_size", "13"));
		boolean notifications = Boolean.parseBoolean(settings.getOrDefault("notifications", "true"));
		
		// Add new notification settings
		int notificationTimeMinutes = Integer.parseInt(settings.getOrDefault("notification_time_minutes", "10"));
		boolean notifyOnMissed = Boolean.parseBoolean(settings.getOrDefault("notify_on_missed", "true"));
		boolean notifyOnStart = Boolean.parseBoolean(settings.getOrDefault("notify_on_start", "true"));
		boolean soundEnabled = Boolean.parseBoolean(settings.getOrDefault("sound_enabled", "true"));
		String soundFile = settings.getOrDefault("sound_file", "/sounds/Homecoming.mp3");
		
		SettingsState state = new SettingsState(theme, fontSize, notifications,
			notificationTimeMinutes, notifyOnMissed, notifyOnStart);
		state.soundEnabled = soundEnabled;
		state.soundFile = soundFile;
		state.availableSoundFiles = discoverSoundFiles(); // Populate available sound files

		return state;
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
			case "sound_enabled":
				currentState.soundEnabled = Boolean.parseBoolean(value);
				break;
			case "sound_file":
				currentState.soundFile = value;
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

		Font currentFont = UIManager.getFont("defaultFont");
		if (currentFont == null) {
			currentFont = new Font("Dialog", Font.PLAIN, state.fontSize);
		} else {
			currentFont = currentFont.deriveFont((float) state.fontSize);
		}
		UIManager.put("defaultFont", currentFont);

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
		currentState.availableSoundFiles = discoverSoundFiles(); // Populate available sound files
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
		SettingsState state = new SettingsState("FlatLightLaf", 14, true);
		
		try (PreparedStatement stmt = connection.prepareStatement(query);
			 ResultSet rs = stmt.executeQuery()) {
				 
			while (rs.next()) {
				String key = rs.getString("PREF_KEY");
				String value = rs.getString("PREF_VALUE");
				
				switch (key) {
					case "theme":
						state.theme = value;
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
					case "sound_enabled":
						state.soundEnabled = Boolean.parseBoolean(value);
						break;
					case "sound_file":
						state.soundFile = value;
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

		// Apply just font size to default font
		Font currentFont = UIManager.getFont("defaultFont");
		if (currentFont == null) {
			currentFont = new Font("Dialog", Font.PLAIN, currentState.fontSize);
		} else {
			currentFont = currentFont.deriveFont((float) currentState.fontSize);
		}
		UIManager.put("defaultFont", currentFont);

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

	private static List<String> discoverSoundFiles() {
		List<String> soundFiles = new ArrayList<>();
		try {
			String[] soundSubdirs = {"alarm_tone/", "notification/"}; // Define subdirectories to scan

			for (String subdir : soundSubdirs) {
				java.net.URL soundDirUrl = SettingsManager.class.getResource("/sounds/" + subdir);
				if (soundDirUrl != null) {
					URI uri = soundDirUrl.toURI();
					Path path;
					FileSystem fileSystem = null;

					if ("jar".equals(uri.getScheme())) {
						// If running from a JAR, create a new file system
						fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
						path = fileSystem.getPath("/sounds/" + subdir);
					} else {
						// If running from exploded classes (e.g., in IDE)
						path = Paths.get(uri);
					}

					// List files and filter by extensions (only direct children, as we are iterating subdirs)
					try (Stream<Path> walk = Files.walk(path, 1)) {
						Iterator<Path> it = walk.iterator();
						while(it.hasNext()) {
							Path file = it.next();
							if (Files.isRegularFile(file)) {
								String fileName = file.getFileName().toString();
								if (fileName.toLowerCase().endsWith(".mp3") || fileName.toLowerCase().endsWith(".wav")) {
									soundFiles.add("sounds/" + subdir + fileName); // Store as full relative resource path
								}
							}
						}
					}
					
					// Close the file system if it was created
					if (fileSystem != null) {
						fileSystem.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Fallback to default sounds if there's an error
			soundFiles.add("sounds/alarm_tone/Homecoming.mp3");
			soundFiles.add("sounds/notification/Advanced_Bell.mp3");
		}
		return soundFiles;
	}

	private static void loadGoodMoralSettings() {
		try {
			// Load images
			// Use generic branding keys
			String[] imageKeys = {PREF_GENERIC_LOGO_1, PREF_GENERIC_LOGO_2, PREF_GENERIC_LOGO_3};
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
									case PREF_GENERIC_LOGO_1:
										currentState.depedSealImage = image; // Use generic field
										break;
									case PREF_GENERIC_LOGO_2:
										currentState.depedMatatagImage = image; // Use generic field
										break;
									case PREF_GENERIC_LOGO_3:
										currentState.lyfjshsLogoImage = image; // Use generic field
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

			// Update current state with generic keys
			switch (key) {
				case PREF_GENERIC_LOGO_1:
					currentState.depedSealImage = image;
					break;
				case PREF_GENERIC_LOGO_2:
					currentState.depedMatatagImage = image;
					break;
				case PREF_GENERIC_LOGO_3:
					currentState.lyfjshsLogoImage = image;
					break;
			}
			notifyListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to get Good Moral Certificate settings
	public static Image getGoodMoralImage(String key) {
		switch (key) {
			case PREF_GENERIC_LOGO_1:
				return currentState.depedSealImage;
			case PREF_GENERIC_LOGO_2:
				return currentState.depedMatatagImage;
			case PREF_GENERIC_LOGO_3:
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

	// --- Template Metadata Management ---
	public static void saveTemplateMetadata(String templateKey, String templatePath, String lastModified, String user) {
		try {
			connection.setAutoCommit(false);
			String sql = "INSERT INTO PREFERENCES (PREF_KEY, PREF_VALUE) VALUES (?, ?) ON DUPLICATE KEY UPDATE PREF_VALUE = ?";
			// Save path
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, templateKey + "_path");
				stmt.setString(2, templatePath);
				stmt.setString(3, templatePath);
				stmt.executeUpdate();
			}
			// Save last modified
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, templateKey + "_last_modified");
				stmt.setString(2, lastModified);
				stmt.setString(3, lastModified);
				stmt.executeUpdate();
			}
			// Save user
			try (PreparedStatement stmt = connection.prepareStatement(sql)) {
				stmt.setString(1, templateKey + "_user");
				stmt.setString(2, user);
				stmt.setString(3, user);
				stmt.executeUpdate();
			}
			connection.commit();
		} catch (SQLException e) {
			try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
			e.printStackTrace();
		} finally {
			try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
		}
	}

	public static TemplateMetadata getTemplateMetadata(String templateKey) {
		TemplateMetadata meta = new TemplateMetadata();
		String sql = "SELECT PREF_KEY, PREF_VALUE FROM PREFERENCES WHERE PREF_KEY IN (?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, templateKey + "_path");
			stmt.setString(2, templateKey + "_last_modified");
			stmt.setString(3, templateKey + "_user");
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String key = rs.getString("PREF_KEY");
					String value = rs.getString("PREF_VALUE");
					if (key.endsWith("_path")) meta.path = value;
					else if (key.endsWith("_last_modified")) meta.lastModified = value;
					else if (key.endsWith("_user")) meta.user = value;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return meta;
	}

	public static class TemplateMetadata {
		public String path;
		public String lastModified;
		public String user;
	}

}