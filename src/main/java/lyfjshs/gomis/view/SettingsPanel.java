package lyfjshs.gomis.view;

import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import docPrinter.templateManager;
import docPrinter.templateManager.TemplateType;
import lyfjshs.gomis.Main;
import lyfjshs.gomis.components.FlatToggleSwitch;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.alarm.AlarmManagement;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsManager.TemplateMetadata;
import lyfjshs.gomis.components.settings.SettingsState;
import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;

public class SettingsPanel extends Form {
	private static final Logger logger = LogManager.getLogger(SettingsPanel.class);
	private final FlatToggleSwitch notificationsToggle = new FlatToggleSwitch();
	private final FlatToggleSwitch notifyOnMissedToggle = new FlatToggleSwitch();
	private final FlatToggleSwitch notifyOnStartToggle = new FlatToggleSwitch();
	private JComboBox<String> themeComboBox;
	private JSpinner notificationTimeSpinner;
	private JCheckBox soundEnabledCheckBox;
	private JComboBox<String> soundComboBox;
	private JButton testSoundButton;
	private JButton stopSoundButton;
	private String selectedSoundFile;
	private boolean isInitializing = true;
	private JSpinner fontSizeSpinner;


	private final Map<String, String> themeMap = Map.of(
			"Dark Theme", "FlatDarkLaf",
			"Light Theme", "FlatLightLaf",
			"Mac Dark Theme", "FlatMacDarkLaf",
			"Mac Light Theme", "FlatMacLightLaf",
			"Darcula Theme", "FlatDarculaLaf",
			"IntelliJ Theme", "FlatIntelliJLaf"
	);

	private final Map<String, String> themeIdToDisplayName = Map.of(
			"FlatDarkLaf", "Dark Theme",
			"FlatLightLaf", "Light Theme",
			"FlatMacDarkLaf", "Mac Dark Theme",
			"FlatMacLightLaf", "Mac Light Theme",
			"FlatDarculaLaf", "Darcula Theme",
			"FlatIntelliJLaf", "IntelliJ Theme"
	);
	private JButton saveButton;

	public SettingsPanel(Connection conn) {
		// Use a vertical, single-column layout with compact insets
		this.setLayout(new MigLayout("fill, insets 4", "[grow]", "[grow]"));
		this.selectedSoundFile = "";

		// Main content panel with vertical stacking
		JPanel contentPanel = new JPanel(new MigLayout("wrap 1, fillx, insets 4", "[grow]", ""));
		// All sections will be added to contentPanel

		// Initialize all components
		initializeComponents(contentPanel);
		saveButton = new JButton("Save Settings");
		contentPanel.add(saveButton, "growx, gapy 8");

		// Wrap contentPanel in a scroll pane for small screens
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setBorder(null);
		this.add(scrollPane, "grow");

		// Load settings
		loadSettings();
		// Add listeners
		addComponentListeners();
		// Initialization complete
		isInitializing = false;
	}

	private void initializeComponents(JPanel panel) {
		// Dark Mode Panel
		JPanel darkModePanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		darkModePanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(darkModePanel, "growx, gapy 4");

		JLabel lblNotifications = new JLabel("Notifications");
		lblNotifications.setFont(new Font("SansSerif", Font.BOLD, 14));
		darkModePanel.add(lblNotifications, "cell 0 0");

		JPanel panel_1 = new JPanel(new MigLayout("insets 0", "[][][grow]", "[grow]"));
		panel_1.setOpaque(false);
		darkModePanel.add(panel_1, "cell 1 0 1 2,grow");

		JLabel lblOnOff = new JLabel("Off");
		panel_1.add(lblOnOff, "cell 0 0,growx,aligny center");
		panel_1.add(notificationsToggle, "cell 1 0,growx,aligny center");

		notificationsToggle.addActionListener(e -> {
			boolean isOn = notificationsToggle.isSelected();
			lblOnOff.setText(isOn ? "On" : "Off");
		});

		JLabel lblDarkModeDesc = new JLabel("Turn on or off Notifications");
		darkModePanel.add(lblDarkModeDesc, "cell 0 1");

		// Add notification time spinner
		JPanel notificationTimePanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		notificationTimePanel.setOpaque(false);
		darkModePanel.add(notificationTimePanel, "cell 0 2,growx");

		JLabel lblNotificationTime = new JLabel("Notification Time (minutes)");
		notificationTimePanel.add(lblNotificationTime, "cell 0 0");

		SpinnerNumberModel notificationTimeModel = new SpinnerNumberModel(10, 1, 60, 1);
		notificationTimeSpinner = new JSpinner(notificationTimeModel);
		notificationTimePanel.add(notificationTimeSpinner, "cell 1 0,alignx right,aligny center");

		// Theme Selection Panel
		JPanel themePanel = new JPanel(new MigLayout("fillx, insets 4", "[grow]", "[]"));
		themePanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(themePanel, "growx, gapy 4");

		JLabel lblThemeTitle = new JLabel("Select Theme");
		lblThemeTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
		themePanel.add(lblThemeTitle, "cell 0 0");

		JLabel lblSelectTheme = new JLabel("Choose a theme:");
		themePanel.add(lblSelectTheme, "cell 0 1");

		themeComboBox = new JComboBox<>(new String[] { 
				"Dark Theme", "Light Theme", "Mac Dark Theme", "Mac Light Theme",
				"Darcula Theme", "IntelliJ Theme" 
		});
		themePanel.add(themeComboBox, "cell 1 0 1 2,alignx right,aligny center");

		// Add appointment notification settings
		addAppointmentNotificationSettings(panel);

		// Sound Settings Panel
		JPanel soundPanel = new JPanel(new MigLayout("insets 8, fillx", "[right]15[grow,fill]"));
		soundPanel.setBorder(BorderFactory.createTitledBorder("Alarm & Notification Settings"));
		soundEnabledCheckBox = new JCheckBox("Enable Sound");
		soundPanel.add(soundEnabledCheckBox, "wrap");

		// Dynamically load sound files from resources
		List<String> soundFiles = new ArrayList<>();
		try {
			// We only want MP3s from alarm_tone for the user-selectable sound
			String alarmToneSubdir = "alarm_tone/";

			java.net.URL alarmToneDirUrl = getClass().getResource("/sounds/" + alarmToneSubdir);
			if (alarmToneDirUrl != null) {
				// If running from JAR
				if (alarmToneDirUrl.getProtocol().equals("jar")) {
					String jarPath = alarmToneDirUrl.getPath().substring(5, alarmToneDirUrl.getPath().indexOf("!"));
					java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath);
					java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						String name = entries.nextElement().getName();
						// Check if the entry is within the alarm_tone subdirectory and is an MP3 file
						if (name.startsWith("sounds/" + alarmToneSubdir) && name.endsWith(".mp3")) {
							soundFiles.add(name.substring(name.indexOf("sounds/")));
						}
					}
					jar.close();
				} else {
					// If running from file system
					java.io.File alarmToneDirFile = new java.io.File(alarmToneDirUrl.toURI());
					if (alarmToneDirFile.exists() && alarmToneDirFile.isDirectory()) {
						java.io.File[] files = alarmToneDirFile.listFiles((dir, name) -> name.endsWith(".mp3"));
						if (files != null) {
							for (java.io.File file : files) {
								// Add the full relative path from /sounds/ to the list
								soundFiles.add("sounds/" + alarmToneSubdir + file.getName());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error loading sound files", e);
			// Fallback to default MP3 alarm sound
			soundFiles.add("sounds/alarm_tone/Homecoming.mp3");
		}

		// Sort the sound files alphabetically
		Collections.sort(soundFiles);

		// Create combo box with found sound files
		soundComboBox = new JComboBox<>(soundFiles.toArray(new String[0]));
		soundPanel.add(new JLabel("")); // Spacer
		soundPanel.add(soundComboBox, "split 2");
		
		// Create test and stop buttons
		testSoundButton = new JButton("Test Sound");
		stopSoundButton = new JButton("Stop Sound");
		stopSoundButton.setEnabled(false);
		
		soundPanel.add(testSoundButton, "split 2");
		soundPanel.add(stopSoundButton, "wrap");

		panel.add(soundPanel, "growx, gapy 4");

		// Add template manager section after sound panel
		addTemplateManagerSettings(panel);
	}

	private void addAppointmentNotificationSettings(JPanel panel) {
		JPanel appointmentNotificationsPanel = new JPanel(new MigLayout("fill", "[grow]", "[]10[]10[]"));
		appointmentNotificationsPanel.putClientProperty("FlatLaf.style", 
			"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		appointmentNotificationsPanel.setBorder(BorderFactory.createTitledBorder("Appointment Notifications"));
		
		SpinnerNumberModel fontSizeModel = new SpinnerNumberModel(12, 8, 24, 1);
		// After theme panel, add font size panel
		JPanel fontSizePanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		fontSizePanel.putClientProperty("FlatLaf.style",
		"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		panel.add(fontSizePanel, "growx, gapy 4");
		
		JLabel lblFontSize = new JLabel("Font Size");
		lblFontSize.setFont(new Font("SansSerif", Font.BOLD, 14));
		fontSizePanel.add(lblFontSize, "cell 0 0");
		fontSizeSpinner = new JSpinner(fontSizeModel);
		fontSizePanel.add(fontSizeSpinner, "cell 1 0 1 2,alignx right,aligny center");
		
		JLabel lblFontSizeDesc = new JLabel("Adjust the application font size");
		fontSizePanel.add(lblFontSizeDesc, "cell 0 1");
		
		// Move the existing appointment notification settings panel to cell 0 3
		// Update the existing line:
		panel.add(appointmentNotificationsPanel, "growx, gapy 4");
		
		// Notify on missed toggle
		JPanel notifyMissedPanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		notifyMissedPanel.setOpaque(false);
		
		JLabel lblNotifyMissed = new JLabel("Notify on Missed Appointments");
		lblNotifyMissed.setFont(new Font("SansSerif", Font.BOLD, 14));
		notifyMissedPanel.add(lblNotifyMissed, "cell 0 0");
		
		JPanel missedTogglePanel = new JPanel(new MigLayout("insets 0", "[][grow]", "[grow]"));
		missedTogglePanel.setOpaque(false);
		
		JLabel lblMissedOnOff = new JLabel("Off");
		missedTogglePanel.add(lblMissedOnOff, "cell 0 0, growx, aligny center");
		
		notifyOnMissedToggle.addActionListener(e -> {
			boolean isOn = notifyOnMissedToggle.isSelected();
			lblMissedOnOff.setText(isOn ? "On" : "Off");
		});
		
		missedTogglePanel.add(notifyOnMissedToggle, "cell 1 0, growx, aligny center");
		notifyMissedPanel.add(missedTogglePanel, "cell 1 0 1 2, grow");
		
		JLabel lblNotifyMissedDesc = new JLabel("Show notifications when appointments are missed");
		notifyMissedPanel.add(lblNotifyMissedDesc, "cell 0 1");
		
		appointmentNotificationsPanel.add(notifyMissedPanel, "growx, wrap");
		
		// Notify on start toggle
		JPanel notifyStartPanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		notifyStartPanel.setOpaque(false);
		
		JLabel lblNotifyStart = new JLabel("Notify on Appointment Start");
		lblNotifyStart.setFont(new Font("SansSerif", Font.BOLD, 14));
		notifyStartPanel.add(lblNotifyStart, "cell 0 0");
		
		JPanel startTogglePanel = new JPanel(new MigLayout("insets 0", "[][grow]", "[grow]"));
		startTogglePanel.setOpaque(false);
		
		JLabel lblStartOnOff = new JLabel("Off");
		startTogglePanel.add(lblStartOnOff, "cell 0 0, growx, aligny center");
		
		notifyOnStartToggle.addActionListener(e -> {
			boolean isOn = notifyOnStartToggle.isSelected();
			lblStartOnOff.setText(isOn ? "On" : "Off");
		});
		
		startTogglePanel.add(notifyOnStartToggle, "cell 1 0, growx, aligny center");
		notifyStartPanel.add(startTogglePanel, "cell 1 0 1 2, grow");
		
		JLabel lblNotifyStartDesc = new JLabel("Show notifications when appointments start");
		notifyStartPanel.add(lblNotifyStartDesc, "cell 0 1");
		
		appointmentNotificationsPanel.add(notifyStartPanel, "growx");
	}

	private void loadSettings() {
		SettingsState currentState = Main.settings.getSettingsState();
		if (currentState != null) {
			updateUIComponents(currentState);
		}
	}

	private void updateUIComponents(SettingsState state) {
		if (state == null) return;
		
		SwingUtilities.invokeLater(() -> {
			removeAllListeners();
			
			String displayName = themeIdToDisplayName.getOrDefault(state.theme, "Light Theme");
			themeComboBox.setSelectedItem(displayName);
			
			notificationsToggle.setSelected(state.notifications);
			notificationTimeSpinner.setValue(state.notificationTimeMinutes);
			notifyOnMissedToggle.setSelected(state.notifyOnMissed);
			notifyOnStartToggle.setSelected(state.notifyOnStart);
			soundEnabledCheckBox.setSelected(state.soundEnabled);

			// Update sound controls state
			boolean soundEnabled = state.soundEnabled;
			soundComboBox.setEnabled(soundEnabled);
			testSoundButton.setEnabled(soundEnabled);
			stopSoundButton.setEnabled(false);

			// Update sound combo box
			soundComboBox.removeAllItems();
			for (String soundFile : state.availableSoundFiles) {
				soundComboBox.addItem(soundFile);
			}
			soundComboBox.setSelectedItem(state.soundFile);

			addComponentListeners();
			
			updateToggleLabel(notificationsToggle, state.notifications);
			updateToggleLabel(notifyOnMissedToggle, state.notifyOnMissed);
			updateToggleLabel(notifyOnStartToggle, state.notifyOnStart);

			// Reset save button state
			saveButton.setEnabled(false);
		});
	}

	private void updateToggleLabel(FlatToggleSwitch toggle, boolean isOn) {
		Component[] components = ((JPanel)toggle.getParent()).getComponents();
		for (Component c : components) {
			if (c instanceof JLabel && (((JLabel)c).getText().equals("On") || ((JLabel)c).getText().equals("Off"))) {
				((JLabel)c).setText(isOn ? "On" : "Off");
				break;
			}
		}
	}

	private void removeAllListeners() {
		for (java.awt.event.ActionListener al : themeComboBox.getActionListeners()) {
			themeComboBox.removeActionListener(al);
		}
		for (java.awt.event.ActionListener al : notificationsToggle.getActionListeners()) {
			notificationsToggle.removeActionListener(al);
		}
		for (java.awt.event.ActionListener al : notifyOnMissedToggle.getActionListeners()) {
			notifyOnMissedToggle.removeActionListener(al);
		}
		for (java.awt.event.ActionListener al : notifyOnStartToggle.getActionListeners()) {
			notifyOnStartToggle.removeActionListener(al);
		}
		for (ChangeListener cl : notificationTimeSpinner.getChangeListeners()) {
			notificationTimeSpinner.removeChangeListener(cl);
		}
		for (ChangeListener cl : fontSizeSpinner.getChangeListeners()) {
			fontSizeSpinner.removeChangeListener(cl);
		}
	}

	private void addComponentListeners() {
		// Theme selection listener
		themeComboBox.addActionListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
		});

		notificationsToggle.addActionListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
		});

		notificationTimeSpinner.addChangeListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
		});
		
		notifyOnMissedToggle.addActionListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
		});
		
		notifyOnStartToggle.addActionListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
		});
		
		soundEnabledCheckBox.addActionListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
			// Update sound controls state
			boolean soundEnabled = soundEnabledCheckBox.isSelected();
			soundComboBox.setEnabled(soundEnabled);
			testSoundButton.setEnabled(soundEnabled);
			stopSoundButton.setEnabled(false);
		});
		
		soundComboBox.addActionListener(e -> {
			if (isInitializing) return;
			saveButton.setEnabled(true);
		});

		testSoundButton.addActionListener(e -> {
			// Get the currently selected sound file from the combo box
			String selectedSoundPath = (String) soundComboBox.getSelectedItem();
			if (selectedSoundPath != null && soundEnabledCheckBox.isSelected()) {
				selectedSoundFile = selectedSoundPath;
				AlarmManagement alarmManager = AlarmManagement.getInstance();
				if (alarmManager.isPlayingSound()) {
					alarmManager.stopSound();
					testSoundButton.setText("Test Sound");
					stopSoundButton.setEnabled(false);
				} else {
					alarmManager.playSound(selectedSoundPath);
					testSoundButton.setText("Stop Sound");
					stopSoundButton.setEnabled(true);
				}
			} else if (!soundEnabledCheckBox.isSelected()) {
				ToastOption toastOption = Toast.createOption();
				toastOption.getLayoutOption().setMargin(0, 0, 50, 0).setDirection(ToastDirection.TOP_TO_BOTTOM);
				Toast.show(this, Toast.Type.INFO, "Sound is disabled in settings.", ToastLocation.BOTTOM_CENTER, toastOption);
			}
		});

		stopSoundButton.addActionListener(e -> {
			AlarmManagement alarmManager = AlarmManagement.getInstance();
			if (alarmManager.isPlayingSound()) {
				alarmManager.stopSound();
				testSoundButton.setText("Test Sound");
				stopSoundButton.setEnabled(false);
			}
		});

		// Add save button listener
		saveButton.addActionListener(e -> {
			saveSettings();
			saveButton.setEnabled(false);
		});
	}

	private void saveSettings() {
		String selectedThemeDisplayName = (String) themeComboBox.getSelectedItem();
		String themeId = themeMap.getOrDefault(selectedThemeDisplayName, "FlatLightLaf");
		
		SettingsManager.updateSetting("theme", themeId);
		SettingsManager.updateSetting("notifications", String.valueOf(notificationsToggle.isSelected()));
		SettingsManager.updateSetting("notification_time_minutes", String.valueOf(notificationTimeSpinner.getValue()));
		SettingsManager.updateSetting("notify_on_missed", String.valueOf(notifyOnMissedToggle.isSelected()));
		SettingsManager.updateSetting("notify_on_start", String.valueOf(notifyOnStartToggle.isSelected()));
		SettingsManager.updateSetting("sound_enabled", String.valueOf(soundEnabledCheckBox.isSelected()));
		
		String selectedSound = (String) soundComboBox.getSelectedItem();
		if (selectedSound != null) {
			SettingsManager.updateSetting("sound_file", selectedSound);
		}

		showSettingsSavedToast();
	}

	private void showSettingsSavedToast() {
		ToastOption toastOption = Toast.createOption();
		toastOption.getLayoutOption()
			.setMargin(0, 0, 50, 0)
			.setDirection(ToastDirection.TOP_TO_BOTTOM);
		Toast.show(this, Toast.Type.SUCCESS, "Settings saved", ToastLocation.BOTTOM_CENTER, toastOption);
	}

	@Override
	public void formOpen() {
		super.formOpen();
		SettingsState currentState = Main.settings.getSettingsState();
		if (currentState != null) {
			updateUIComponents(currentState);
		}
		// Reset save button state when form opens
		saveButton.setEnabled(false);
	}

	/**
	 * Adds the Document Template Management section to the settings panel.
	 */
	private void addTemplateManagerSettings(JPanel panel) {
		JPanel templatePanel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]", ""));
		templatePanel.setBorder(BorderFactory.createTitledBorder("Document Templates"));
		templatePanel.putClientProperty("FlatLaf.style",
				"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");

		for (TemplateType type : templateManager.getAllTemplateTypes()) {
			JPanel row = new JPanel(new MigLayout("fillx, insets 0", "[grow]10[]10[]10[]", ""));
			String status = templateManager.getTemplateStatus(type);
			File activeFile = templateManager.getActiveTemplate(type);
			JLabel nameLabel = new JLabel(type.name().replace('_', ' '));
			JLabel statusLabel = new JLabel(status);
			JLabel pathLabel = new JLabel(activeFile != null ? activeFile.getAbsolutePath() : "");
			// Fetch metadata
			TemplateMetadata meta = lyfjshs.gomis.components.settings.SettingsManager.getTemplateMetadata(type.name());
			String metaInfo = "";
			if (meta != null) {
				metaInfo = String.format("<html><small>Last Modified: %s<br>User: %s</small></html>",
						meta.lastModified != null ? meta.lastModified : "-",
						meta.user != null ? meta.user : "-");
			}
			JLabel metaLabel = new JLabel(metaInfo);
			row.add(nameLabel, "growx");
			row.add(statusLabel);
			row.add(pathLabel, "growx, pushx");
			row.add(metaLabel);

			JButton importBtn = new JButton("Import");
			JButton exportBtn = new JButton("Export Default");
			JButton resetBtn = new JButton("Reset");

			// Import custom template
			importBtn.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Select DOCX Template");
				int result = chooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					boolean ok = templateManager.importCustomTemplate(type, selected);
					if (ok) {
						JOptionPane.showMessageDialog(this, "Template imported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "Failed to import template.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					refreshTemplateManagerSettings(templatePanel);
				}
			});

			// Export default template
			exportBtn.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Export Default Template");
				chooser.setSelectedFile(new File(type.getFileName()));
				int result = chooser.showSaveDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					boolean ok = templateManager.exportDefaultTemplate(type, chooser.getSelectedFile());
					if (ok) {
						JOptionPane.showMessageDialog(this, "Default template exported!", "Success", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "Failed to export default template.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			// Reset to default
			resetBtn.addActionListener(e -> {
				boolean ok = templateManager.resetToDefaultTemplate(type);
				if (ok) {
					JOptionPane.showMessageDialog(this, "Template reset to default.", "Success", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "Failed to reset template.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				refreshTemplateManagerSettings(templatePanel);
			});

			row.add(importBtn);
			row.add(exportBtn);
			row.add(resetBtn);
			templatePanel.add(row, "growx");
		}
		panel.add(templatePanel, "growx, gapy 4");
	}

	/**
	 * Refreshes the template manager section to update status and paths.
	 */
	private void refreshTemplateManagerSettings(JPanel templatePanel) {
		templatePanel.removeAll();
		for (TemplateType type : templateManager.getAllTemplateTypes()) {
			JPanel row = new JPanel(new MigLayout("fillx, insets 0", "[grow]10[]10[]10[]", ""));
			String status = templateManager.getTemplateStatus(type);
			File activeFile = templateManager.getActiveTemplate(type);
			JLabel nameLabel = new JLabel(type.name().replace('_', ' '));
			JLabel statusLabel = new JLabel(status);
			JLabel pathLabel = new JLabel(activeFile != null ? activeFile.getAbsolutePath() : "");
			// Fetch metadata
			TemplateMetadata meta = lyfjshs.gomis.components.settings.SettingsManager.getTemplateMetadata(type.name());
			String metaInfo = "";
			if (meta != null) {
				metaInfo = String.format("<html><small>Last Modified: %s<br>User: %s</small></html>",
						meta.lastModified != null ? meta.lastModified : "-",
						meta.user != null ? meta.user : "-");
			}
			JLabel metaLabel = new JLabel(metaInfo);
			row.add(nameLabel, "growx");
			row.add(statusLabel);
			row.add(pathLabel, "growx, pushx");
			row.add(metaLabel);

			JButton importBtn = new JButton("Import");
			JButton exportBtn = new JButton("Export Default");
			JButton resetBtn = new JButton("Reset");

			importBtn.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Select DOCX Template");
				int result = chooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selected = chooser.getSelectedFile();
					boolean ok = templateManager.importCustomTemplate(type, selected);
					if (ok) {
						JOptionPane.showMessageDialog(this, "Template imported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "Failed to import template.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					refreshTemplateManagerSettings(templatePanel);
				}
			});
			exportBtn.addActionListener(e -> {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Export Default Template");
				chooser.setSelectedFile(new File(type.getFileName()));
				int result = chooser.showSaveDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					boolean ok = templateManager.exportDefaultTemplate(type, chooser.getSelectedFile());
					if (ok) {
						JOptionPane.showMessageDialog(this, "Default template exported!", "Success", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "Failed to export default template.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			resetBtn.addActionListener(e -> {
				boolean ok = templateManager.resetToDefaultTemplate(type);
				if (ok) {
					JOptionPane.showMessageDialog(this, "Template reset to default.", "Success", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "Failed to reset template.", "Error", JOptionPane.ERROR_MESSAGE);
				}
				refreshTemplateManagerSettings(templatePanel);
			});
			row.add(importBtn);
			row.add(exportBtn);
			row.add(resetBtn);
			templatePanel.add(row, "growx");
		}
		templatePanel.revalidate();
		templatePanel.repaint();
	}
}