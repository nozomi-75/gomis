package lyfjshs.gomis.components.settings;

public class SettingsState {
	public final String theme;
	public final String fontStyle;
	public final int fontSize;
	public final boolean notifications;

	public SettingsState(String theme, String fontStyle, int fontSize, boolean notifications) {
		this.theme = theme;
		this.fontStyle = fontStyle;
		this.fontSize = fontSize;
		this.notifications = notifications;
	}
}