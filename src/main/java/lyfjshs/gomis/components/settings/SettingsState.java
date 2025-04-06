package lyfjshs.gomis.components.settings;

public class SettingsState {
	public String theme;
	public String fontStyle;
	public int fontSize;
	public boolean notifications;
	
	// New settings for appointment notifications
	public int notificationTimeMinutes; // minutes before appointment to show notification
	public boolean notifyOnMissed; // whether to notify for missed appointments
	public boolean notifyOnStart; // whether to notify when appointment starts

	public SettingsState(String theme, String fontStyle, int fontSize, boolean notifications) {
		this.theme = theme;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.notifications = notifications;
		
		// Default values
		this.notificationTimeMinutes = 10;
		this.notifyOnMissed = true;
		this.notifyOnStart = true;
	}
	
	public SettingsState(String theme, String fontStyle, int fontSize, boolean notifications,
			int notificationTimeMinutes, boolean notifyOnMissed, boolean notifyOnStart) {
		this.theme = theme;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.notifications = notifications;
		this.notificationTimeMinutes = notificationTimeMinutes;
		this.notifyOnMissed = notifyOnMissed;
		this.notifyOnStart = notifyOnStart;
	}
}