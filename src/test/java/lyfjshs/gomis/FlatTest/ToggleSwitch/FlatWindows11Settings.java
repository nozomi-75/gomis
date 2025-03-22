package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.DAO.PreferencesDAO;
import lyfjshs.gomis.Database.entity.PreferencesEntity;
import net.miginfocom.swing.MigLayout;

public class FlatWindows11Settings extends JFrame {
	private static final String DB_URL = "jdbc:mariadb://localhost:3306/gomisDB";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "YourRootPassword123!";

	private FlatToggleSwitch darkModeToggle;
	private JComboBox<String> themeComboBox;
	private PreferencesDAO preferencesDAO;
	private int userId = 1; // Assume user ID 1 for now
	private JComboBox<String> fontComboBox;
	private JSlider fontSizeSlider;
	private JCheckBox notificationsCheckBox;

	public FlatWindows11Settings() {
		// Initialize database connection
		try {
			Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			preferencesDAO = new PreferencesDAO(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(706, 300);
		this.getContentPane().setLayout(new MigLayout("fill, insets 10", "[grow]", "[pref!]"));

		// Main panel
		JPanel panel = new JPanel();
		panel.setBorder(
				new TitledBorder(null, "Theme & Appearance", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[pref!][][grow][][]"));

		// Dark mode panel
		JPanel darkModePanel = new JPanel(new MigLayout("", "[][][][][grow]", "[][][]"));
		panel.add(darkModePanel, "cell 0 0,grow");
		darkModePanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;"
				+ "[light]background:shade($Panel.background,10%);" + "[dark]background:tint($Panel.background,10%);");

		JLabel lblDarkMode = new JLabel("Dark Mode");
		lblDarkMode.setFont(new Font("SansSerif", Font.BOLD, 14));
		darkModePanel.add(lblDarkMode, "cell 0 0 2 1");

		JLabel lblDarkModeDesc = new JLabel("Enable or disable dark mode.");
		darkModePanel.add(lblDarkModeDesc, "cell 0 1 4 2");

		// Dark Mode Toggle Switch
		darkModeToggle = new FlatToggleSwitch(new Color(0, 122, 255));
		darkModeToggle.addActionListener(e -> applySelectedTheme());
		darkModePanel.add(darkModeToggle, "cell 4 0 1 3,alignx right,aligny center");

		// Theme selection panel
		JPanel themePanel = new JPanel(new MigLayout("", "[30.00][][][][grow][100px,fill]", "[][]"));
		panel.add(themePanel, "cell 0 1,grow");
		themePanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;"
				+ "[light]background:shade($Panel.background,10%);" + "[dark]background:tint($Panel.background,10%);");

		JLabel lblThemeTitle = new JLabel("Select Theme");
		lblThemeTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		themePanel.add(lblThemeTitle, "cell 0 0 2 1");

		// Theme Selection ComboBox
		themeComboBox = new JComboBox<>();
		themeComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "FlatLaf", "macOS", "Darcula" }));

		themeComboBox.addActionListener(e -> applySelectedTheme());

		themePanel.add(themeComboBox, "cell 5 0 1 2,alignx right,aligny center");

		JLabel lblSelectTheme = new JLabel("Choose a theme for the application:");
		themePanel.add(lblSelectTheme, "cell 0 1 4 1");

		// Font settings panel
		JPanel fontPanel = new JPanel(new MigLayout("", "[30.00][][][][100px,grow,fill]", "[][]"));
		panel.add(fontPanel, "cell 0 2,grow");
		fontPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;"
				+ "[light]background:shade($Panel.background,10%);" + "[dark]background:tint($Panel.background,10%);");

		JLabel lblFontTitle = new JLabel("Font Settings");
		lblFontTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		fontPanel.add(lblFontTitle, "cell 0 0 2 1");

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		fontPanel.add(panel_1, "flowx,cell 4 0 1 2,grow");
		panel_1.setLayout(new MigLayout("", "[grow][200px]", "[20px]"));

		JLabel lblNewLabel = new JLabel("Size:");
		panel_1.add(lblNewLabel, "flowx,cell 0 0,aligny center");

		fontSizeSlider = new JSlider(8, 24, 12);
		panel_1.add(fontSizeSlider, "cell 0 0,growx,aligny center");
		fontSizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				applyFontSettings();
			}
		});

		fontComboBox = new JComboBox<>(new String[] { "SansSerif", "Serif", "Monospaced" });
		panel_1.add(fontComboBox, "cell 1 0,growx,aligny center");

		JLabel lblFontDesc = new JLabel("Select font and size for the application.");
		fontPanel.add(lblFontDesc, "flowx,cell 0 1 2 1");

		// Notification settings panel
		JPanel notificationPanel = new JPanel(new MigLayout("", "[][][][][grow][]", "[][20]"));
		panel.add(notificationPanel, "cell 0 4,grow");
		notificationPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;"
				+ "[light]background:shade($Panel.background,10%);" + "[dark]background:tint($Panel.background,10%);");

		JLabel lblNotificationTitle = new JLabel("Notifications");
		lblNotificationTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		notificationPanel.add(lblNotificationTitle, "cell 0 0 2 1");

		notificationsCheckBox = new JCheckBox("Enable Notifications");
		notificationPanel.add(notificationsCheckBox, "cell 5 0 1 2");

		// Load saved preferences on startup
		loadPreferences();
		applySelectedTheme(); // Apply all settings after loading preferences

		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void applyFontSettings() {
		String selectedFont = (String) fontComboBox.getSelectedItem();
		int fontSize = fontSizeSlider.getValue();
		Font font = new Font(selectedFont, Font.PLAIN, fontSize);

		// Apply the font to all components in the frame
		updateFontRecursively(getContentPane(), font);
		System.out.println("Font applied: " + selectedFont + ", Size: " + fontSize); // Debug statement
	}

	private void updateFontRecursively(Container container, Font font) {
		for (Component component : container.getComponents()) {
			component.setFont(font);
			if (component instanceof Container) {
				updateFontRecursively((Container) component, font);
			}
		}
	}

	/**
	 * Loads user preferences from the database.
	 */
	private void loadPreferences() {
	    List<PreferencesEntity> preferences = preferencesDAO.getPreferencesByUserId(userId);
	    for (PreferencesEntity pref : preferences) {
	        if ("Theme".equals(pref.getCategory()) && "SelectedTheme".equals(pref.getKey())) {
	            themeComboBox.setSelectedItem(pref.getValue());
	        } else if ("Theme".equals(pref.getCategory()) && "DarkMode".equals(pref.getKey())) {
	            darkModeToggle.setSelected(Boolean.parseBoolean(pref.getValue()));
	        } else if ("Font".equals(pref.getCategory()) && "SelectedFont".equals(pref.getKey())) {
	            fontComboBox.setSelectedItem(pref.getValue());
	        } else if ("Font".equals(pref.getCategory()) && "FontSize".equals(pref.getKey())) {
	            fontSizeSlider.setValue(Integer.parseInt(pref.getValue()));
	        } else if ("Notifications".equals(pref.getCategory()) && "Enabled".equals(pref.getKey())) {
	            notificationsCheckBox.setSelected(Boolean.parseBoolean(pref.getValue()));
	        }
	    }
	}

	private void applyNotificationSettings() {
	    boolean areNotificationsEnabled = notificationsCheckBox.isSelected();
	    // Implement notification settings logic here
	    System.out.println("Notifications Enabled: " + areNotificationsEnabled);
	}

	/**
	 * Applies the selected theme based on the current toggle state and saves the
	 * preference.
	 */
	private void applySelectedTheme() {
	    boolean isDarkMode = darkModeToggle.isSelected();
	    String selectedTheme = (String) themeComboBox.getSelectedItem();
	    String selectedFont = (String) fontComboBox.getSelectedItem();
	    int fontSize = fontSizeSlider.getValue();
	    boolean areNotificationsEnabled = notificationsCheckBox.isSelected();

	    SwingUtilities.invokeLater(() -> {
	        FlatAnimatedLafChange.showSnapshot();

	        switch (selectedTheme) {
	            case "FlatLaf":
	                if (isDarkMode) FlatDarkLaf.setup();
	                else FlatLightLaf.setup();
	                break;
	            case "Darcula":
	                FlatDarculaLaf.setup(); // Always dark
	                darkModeToggle.setSelected(true); // Ensure toggle remains ON
	                break;
	            case "macOS":
	                if (isDarkMode) FlatMacDarkLaf.setup();
	                else FlatMacLightLaf.setup();
	                break;
	            default:
	                FlatLightLaf.setup();
	        }

	        // Save preferences to the database
	        savePreference("Theme", "SelectedTheme", selectedTheme);
	        savePreference("Theme", "DarkMode", String.valueOf(isDarkMode));
	        savePreference("Font", "SelectedFont", selectedFont);
	        savePreference("Font", "FontSize", String.valueOf(fontSize));
	        savePreference("Notifications", "Enabled", String.valueOf(areNotificationsEnabled));

	        int previousStep = darkModeToggle.getAnimationStep(); // Preserve animation progress

	        SwingUtilities.updateComponentTreeUI(this);

	        // Apply font settings after updating the UI
	        applyFontSettings();
	        applyNotificationSettings();

	        // Restore animation progress and repaint
	        darkModeToggle.setAnimationStep(previousStep);
	        darkModeToggle.setUI(new CustomButtonUI());
	        darkModeToggle.repaint();
	        FlatAnimatedLafChange.hideSnapshotWithAnimation();
	    });
	}

	/**
	 * Saves a user preference to the database.
	 */
	private void savePreference(String category, String key, String value) {
		int prefId = preferencesDAO.findPreferenceId(userId, category, key);
		PreferencesEntity preference = new PreferencesEntity(prefId == -1 ? 0 : prefId, // Use existing ID if found,
																						// else create new
				userId, category, key, value, "String", null, null, null);

		if (prefId == -1) {
			preferencesDAO.createPreference(preference); // Insert new preference
		} else {
			preferencesDAO.updatePreference(preference); // Update existing preference
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FlatLightLaf.setup(); // Default theme
			new FlatWindows11Settings();
		});
	}
}
