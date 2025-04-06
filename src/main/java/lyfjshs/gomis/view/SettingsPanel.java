package lyfjshs.gomis.view;


import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.components.FlatToggleSwitch;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("unused")
public class SettingsPanel extends Form {
	private final FlatToggleSwitch notificationsToggle = new FlatToggleSwitch();
	private final FlatToggleSwitch notifyOnMissedToggle = new FlatToggleSwitch();
	private final FlatToggleSwitch notifyOnStartToggle = new FlatToggleSwitch();
	private JComboBox<Integer> fontSizeComboBox;
	private JComboBox<String> fontComboBox;
	private JComboBox<String> themeComboBox;
	private JSpinner notificationTimeSpinner;
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
		this.add(panel, "cell 0 0, grow, wrap");

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
		panel_1.add(notificationsToggle, "cell 1 0,growx,aligny center");

		// Add listener to update the label text
		notificationsToggle.addActionListener(e -> {
			boolean isOn = notificationsToggle.isSelected();
			lblOnOff.setText(isOn ? "On" : "Off");
		});

		// Update initial state based on settings
		SettingsState currentState = Main.settings.getSettingsState();
		notificationsToggle.setSelected(currentState.notifications);
		lblOnOff.setText(currentState.notifications ? "On" : "Off");

		JLabel lblDarkModeDesc = new JLabel("Turn on or off Notifications");
		darkModePanel.add(lblDarkModeDesc, "cell 0 1");

		// Initialize remaining components
		initializeRemainingComponents(panel);
		
		// Add appointment notification settings panel
		addAppointmentNotificationSettings(panel);

		// Initialize settings and listeners
		removeAllListeners();
		updateUIComponents(Main.settings.getSettingsState());
		SettingsManager.addSettingsListener(this::updateUIComponents);
		addComponentListeners();
	}

	private void addAppointmentNotificationSettings(JPanel panel) {
		// Appointment notifications panel
		JPanel appointmentNotificationsPanel = new JPanel(new MigLayout("fill", "[grow]", "[]10[]10[]"));
		appointmentNotificationsPanel.putClientProperty("FlatLaf.style", 
			"arc:20; [light]background:shade($Panel.background,10%); [dark]background:tint($Panel.background,10%)");
		appointmentNotificationsPanel.setBorder(BorderFactory.createTitledBorder("Appointment Notifications"));
		panel.add(appointmentNotificationsPanel, "cell 0 4, growx");
		
		// Notification time settings
		JPanel notificationTimePanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		notificationTimePanel.setOpaque(false);
		
		JLabel lblNotificationTime = new JLabel("Notification Time");
		lblNotificationTime.setFont(new Font("SansSerif", Font.BOLD, 14));
		notificationTimePanel.add(lblNotificationTime, "cell 0 0");
		
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(10, 1, 60, 1);
		notificationTimeSpinner = new JSpinner(spinnerModel);
		notificationTimePanel.add(notificationTimeSpinner, "cell 1 0");
		
		JLabel lblNotificationTimeDesc = new JLabel("Minutes before appointment to show notification");
		notificationTimePanel.add(lblNotificationTimeDesc, "cell 0 1");
		
		appointmentNotificationsPanel.add(notificationTimePanel, "growx, wrap");
		
		// Notify on missed toggle
		JPanel notifyMissedPanel = new JPanel(new MigLayout("fill", "[grow][]", "[][]"));
		notifyMissedPanel.setOpaque(false);
		
		JLabel lblNotifyMissed = new JLabel("Notify on Missed Appointments");
		lblNotifyMissed.setFont(new Font("SansSerif", Font.BOLD, 14));
		notifyMissedPanel.add(lblNotifyMissed, "cell 0 0");
		
		JPanel missedTogglePanel = new JPanel(new MigLayout("insets 0", "[][]", "[grow]"));
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
		
		JPanel startTogglePanel = new JPanel(new MigLayout("insets 0", "[][]", "[grow]"));
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

	private void initializeRemainingComponents(JPanel panel) {
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
	}

	private void applySelectedTheme() {
		String selectedName = (String) themeComboBox.getSelectedItem();
		String themeId = themeMap.get(selectedName);
		updateSettings("theme", themeId); // Persist the theme via SettingsManager
	}

	private void updateSettings(String key, String value) {
	    SettingsState state = SettingsManager.updateSetting(key, value); 
	    if ("theme".equals(key)) {
	        String displayName = themeIdToDisplayName.getOrDefault(state.theme, "Light Theme");
	        themeComboBox.setSelectedItem(displayName);
	    } else if ("font_size".equals(key)) {
	        fontSizeComboBox.setSelectedItem(state.fontSize);
	    } else if ("font_style".equals(key)) {
	        fontComboBox.setSelectedItem(state.fontStyle);
	    } else if ("notifications".equals(key)) {
	        notificationsToggle.setSelected(state.notifications);
	    } else if ("notification_time_minutes".equals(key)) {
	        notificationTimeSpinner.setValue(state.notificationTimeMinutes);
	    } else if ("notify_on_missed".equals(key)) {
	        notifyOnMissedToggle.setSelected(state.notifyOnMissed);
	    } else if ("notify_on_start".equals(key)) {
	        notifyOnStartToggle.setSelected(state.notifyOnStart);
	    }
	}

	private void updateUIComponents(SettingsState state) {
        if (state == null) return;
        
        SwingUtilities.invokeLater(() -> {
            // Remove listeners temporarily
            removeAllListeners();
            
            // Update UI components
            String displayName = themeIdToDisplayName.getOrDefault(state.theme, "Light Theme");
            themeComboBox.setSelectedItem(displayName);
            fontSizeComboBox.setSelectedItem(state.fontSize);
            fontComboBox.setSelectedItem(state.fontStyle);
            notificationsToggle.setSelected(state.notifications);
            
            // Update appointment notification settings
            notificationTimeSpinner.setValue(state.notificationTimeMinutes);
            notifyOnMissedToggle.setSelected(state.notifyOnMissed);
            notifyOnStartToggle.setSelected(state.notifyOnStart);
            
            // Update the On/Off labels
            updateToggleLabel(notificationsToggle, state.notifications);
            updateToggleLabel(notifyOnMissedToggle, state.notifyOnMissed);
            updateToggleLabel(notifyOnStartToggle, state.notifyOnStart);
            
            // Re-add listeners
            addComponentListeners();
            
            // Force immediate UI update
            revalidate();
            repaint();
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
        for (java.awt.event.ActionListener al : fontSizeComboBox.getActionListeners()) {
            fontSizeComboBox.removeActionListener(al);
        }
        for (java.awt.event.ActionListener al : fontComboBox.getActionListeners()) {
            fontComboBox.removeActionListener(al);
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
    }
    
    @Override
    public void formOpen() {
        super.formOpen();
        // Reload settings when form opens
        SettingsState currentState = SettingsManager.getCurrentState();
        if (currentState != null) {
            updateUIComponents(currentState);
        }
    }

    private void addComponentListeners() {
        themeComboBox.addActionListener(e -> {
            if (e.getSource() == themeComboBox) {
                String selectedName = (String) themeComboBox.getSelectedItem();
                String themeId = themeMap.get(selectedName);
                if (themeId != null) {
                    SettingsManager.updateSetting("theme", themeId);
                }
            }
        });

        fontSizeComboBox.addActionListener(e -> {
            if (e.getSource() == fontSizeComboBox) {
                Object selected = fontSizeComboBox.getSelectedItem();
                if (selected != null) {
                    SettingsManager.updateSetting("font_size", selected.toString());
                }
            }
        });

        fontComboBox.addActionListener(e -> {
            if (e.getSource() == fontComboBox) {
                String selected = (String) fontComboBox.getSelectedItem();
                if (selected != null) {
                    SettingsManager.updateSetting("font_style", selected);
                }
            }
        });

        notificationsToggle.addActionListener(e -> {
            SettingsManager.updateSetting("notifications", 
                String.valueOf(notificationsToggle.isSelected()));
        });
        
        notifyOnMissedToggle.addActionListener(e -> {
            SettingsManager.updateSetting("notify_on_missed", 
                String.valueOf(notifyOnMissedToggle.isSelected()));
        });
        
        notifyOnStartToggle.addActionListener(e -> {
            SettingsManager.updateSetting("notify_on_start", 
                String.valueOf(notifyOnStartToggle.isSelected()));
        });
        
        notificationTimeSpinner.addChangeListener(e -> {
            SettingsManager.updateSetting("notification_time_minutes", 
                String.valueOf(notificationTimeSpinner.getValue()));
        });
    }
}