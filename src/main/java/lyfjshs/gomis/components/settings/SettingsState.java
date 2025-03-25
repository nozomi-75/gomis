package lyfjshs.gomis.components.settings;

public class SettingsState {
	public String theme;
	public String fontStyle;
	public int fontSize;
	public boolean notifications;

	public SettingsState(String theme, String fontStyle, int fontSize, boolean notifications) {
		this.theme = theme;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.notifications = notifications;
	}
}