package lyfjshs.gomis.view;


import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.components.FlatToggleSwitch;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings({ "unused", "serial" })
public class SettingsPanel extends Form {
	private final FlatToggleSwitch notificationsToggle;
	private final JComboBox<Integer> fontSizeComboBox;
	private final JComboBox<String> fontComboBox;
	private final JComboBox<String> themeComboBox;
	private final Map<String, String> themeMap = Map.of("Dark Theme", "FlatDarkLaf", "Light Theme", "FlatLightLaf",
			"Mac Dark Theme", "FlatMacDarkLaf", "Mac Light Theme", "FlatMacLightLaf", "Darcula Theme", "FlatDarculaLaf",
			"IntelliJ Theme", "FlatIntelliJLaf");
	private final Map<String, String> themeIdToDisplayName = Map.of("FlatDarkLaf", "Dark Theme", "FlatLightLaf",
			"Light Theme", "FlatMacDarkLaf", "Mac Dark Theme", "FlatMacLightLaf", "Mac Light Theme", "FlatDarculaLaf",
			"Darcula Theme", "FlatIntelliJLaf", "IntelliJ Theme");

	public SettingsPanel(Connection conn) {
		this.setLayout(new MigLayout("fill", "[grow]", "[grow]"));

		// Main Panel
		JPanel panel = new JPanel(new MigLayout("", "[grow]", "[][][][][]"));
		panel.setBorder(BorderFactory.createTitledBorder("Theme & Appearance"));
		this.add(panel, "cell 0 0,grow");

		// Dark Mode Panel
		JPanel darkModePanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		darkModePanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(darkModePanel, "cell 0 0,grow");

		JLabel lblNotifications = new JLabel("Notifications");
		lblNotifications.setFont(new Font("SansSerif", Font.BOLD, 14));
		darkModePanel.add(lblNotifications, "cell 0 0");

		JPanel panel_1 = new JPanel(new MigLayout("insets 0", "[][]", "[grow]"));
		panel_1.setOpaque(false);
		darkModePanel.add(panel_1, "cell 1 0 1 2,grow");

		JLabel lblOnOff = new JLabel("Off");
		panel_1.add(lblOnOff, "cell 0 0,growx,aligny center");

		// Create our custom toggle
		notificationsToggle = new FlatToggleSwitch();
        // If you want to override the color just for this one instance:
        // toggleSwitch.setTrackOnColor(new Color(76, 175, 80)); // green
        // toggleSwitch.setAnimationDuration(200);               // e.g. 200ms
		panel_1.add(notificationsToggle, "cell 1 0,growx,aligny center");

		JLabel lblDarkModeDesc = new JLabel("Turn on or off Notifications");
		darkModePanel.add(lblDarkModeDesc, "cell 0 1");

		// Theme Selection Panel
		JPanel themePanel = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
		themePanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(themePanel, "cell 0 1,growx");

		JLabel lblThemeTitle = new JLabel("Select Theme");
		lblThemeTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		themePanel.add(lblThemeTitle, "cell 0 0");

		JLabel lblSelectTheme = new JLabel("Choose a theme:");
		themePanel.add(lblSelectTheme, "cell 0 1");

		themeComboBox = new JComboBox<>(new String[] { "Dark Theme", "Light Theme", "Mac Dark Theme", "Mac Light Theme",
				"Darcula Theme", "IntelliJ Theme" });
		themePanel.add(themeComboBox, "cell 1 0 1 2,alignx right,aligny center");

		// Text Size Panel
		JPanel textSizePanel = new JPanel(new MigLayout("fill", "[grow][]", "[][][]"));
		textSizePanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(textSizePanel, "cell 0 2,growx");

		JLabel lblTextSize = new JLabel("Text Size");
		lblTextSize.setFont(new Font("SansSerif", Font.BOLD, 14));
		textSizePanel.add(lblTextSize, "cell 0 0");

		fontSizeComboBox = new JComboBox<>(new Integer[] { 10, 12, 14, 16, 18, 20, 24 });
		textSizePanel.add(fontSizeComboBox, "cell 1 0 1 3,alignx right,aligny center");

		JLabel lblNewLabel = new JLabel("Select a Font Size");
		textSizePanel.add(lblNewLabel, "cell 0 1");

		// Font Selection Panel
		JPanel fontPanel = new JPanel(new MigLayout("fill", "[grow]", "[][]"));
		fontPanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(fontPanel, "cell 0 3,growx");

		JLabel lblFontTitle = new JLabel("Font Selection");
		lblFontTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		fontPanel.add(lblFontTitle, "cell 0 0");

		JLabel lblFontDesc = new JLabel("Choose a font:");
		fontPanel.add(lblFontDesc, "cell 0 1");

		fontComboBox = new JComboBox<>(new String[] { "SansSerif", "Serif", "Monospaced" });
		fontPanel.add(fontComboBox, "cell 1 0 1 2,alignx right,aligny center");

		// Initialize UI with current settings
		initializeSettings();

		// Add listeners to update settings
		themeComboBox.addActionListener(e -> applySelectedTheme());
		fontSizeComboBox.addActionListener(
				e -> updateSettings("font_size", String.valueOf(fontSizeComboBox.getSelectedItem())));
		fontComboBox.addActionListener(e -> updateSettings("font_style", (String) fontComboBox.getSelectedItem()));
	}

	private void applySelectedTheme() {
		String selectedName = (String) themeComboBox.getSelectedItem();
		String themeId = themeMap.get(selectedName);
		updateSettings("theme", themeId); // Persist the theme via SettingsManager
	}

	private void initializeSettings() {
		SettingsState state = Main.settings.getSettingsState();
		String displayName = themeIdToDisplayName.getOrDefault(state.theme, "Light Theme");
		themeComboBox.setSelectedItem(displayName);
		fontSizeComboBox.setSelectedItem(state.fontSize);
		fontComboBox.setSelectedItem(state.fontStyle);
	}

	private void updateSettings(String key, String value) {
		SettingsState state = Main.settings.updateSetting(key, value);
		if ("theme".equals(key)) {
			String displayName = themeIdToDisplayName.getOrDefault(state.theme, "Light Theme");
			themeComboBox.setSelectedItem(displayName);
		} else if ("font_size".equals(key)) {
			fontSizeComboBox.setSelectedItem(state.fontSize);
		} else if ("font_style".equals(key)) {
			fontComboBox.setSelectedItem(state.fontStyle);
		} else if ("notifications".equals(key)) {
			notificationsToggle.setSelected(state.notifications);
		}
	}
}