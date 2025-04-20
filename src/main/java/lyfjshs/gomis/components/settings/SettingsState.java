package lyfjshs.gomis.components.settings;

import java.awt.Image;

public class SettingsState {
	public String theme;
	public String fontStyle;
	public int fontSize;
	public boolean notifications;
	
	// New settings for appointment notifications
	public int notificationTimeMinutes; // minutes before appointment to show notification
	public boolean notifyOnMissed; // whether to notify for missed appointments
	public boolean notifyOnStart; // whether to notify when appointment starts

	// Good Moral Certificate image settings
	public Image depedSealImage;
	public Image depedMatatagImage;
	public Image lyfjshsLogoImage;
	public String goodMoralSigner;
	public String goodMoralPosition;

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

	// New constructor including Good Moral Certificate settings
	public SettingsState(String theme, String fontStyle, int fontSize, boolean notifications,
			int notificationTimeMinutes, boolean notifyOnMissed, boolean notifyOnStart,
			Image depedSeal, Image depedMatatag, Image lyfjshsLogo,
			String goodMoralSigner, String goodMoralPosition) {
		this(theme, fontStyle, fontSize, notifications, notificationTimeMinutes, notifyOnMissed, notifyOnStart);
		this.depedSealImage = depedSeal;
		this.depedMatatagImage = depedMatatag;
		this.lyfjshsLogoImage = lyfjshsLogo;
		this.goodMoralSigner = goodMoralSigner;
		this.goodMoralPosition = goodMoralPosition;
	}
}