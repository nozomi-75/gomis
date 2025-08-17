/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.settings;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class SettingsState {
	public String theme;
	public int fontSize;
	public boolean notifications;
	
	// New settings for appointment notifications
	public int notificationTimeMinutes; // minutes before appointment to show notification
	public boolean notifyOnMissed; // whether to notify for missed appointments
	public boolean notifyOnStart; // whether to notify when appointment starts

	// Sound settings
	public boolean soundEnabled; // whether sound notifications are enabled
	public String soundFile; // path to the sound file to play
	public List<String> availableSoundFiles; // List of available sound file names

	// Good Moral Certificate image settings
	public Image depedSealImage;
	public Image depedMatatagImage;
	public Image lyfjshsLogoImage;
	public String goodMoralSigner;
	public String goodMoralPosition;

	/**
	 * Whether to notify when an appointment is created.
	 */
	public boolean notifyOnAppointmentCreated = true;
	/**
	 * Whether to notify for appointment reminders.
	 */
	public boolean notifyOnAppointmentReminder = true;

	public SettingsState(String theme, int fontSize, boolean notifications) {
		this.theme = theme;
		this.fontSize = fontSize;
		this.notifications = notifications;
		
		// Default values
		this.notificationTimeMinutes = 10;
		this.notifyOnMissed = true;
		this.notifyOnStart = true;
		this.soundEnabled = true;
		this.soundFile = "/sounds/Homecoming.mp3";
		this.availableSoundFiles = new ArrayList<>();
	}
	
	public SettingsState(String theme, int fontSize, boolean notifications,
			int notificationTimeMinutes, boolean notifyOnMissed, boolean notifyOnStart) {
		this.theme = theme;
		this.fontSize = fontSize;
		this.notifications = notifications;
		this.notificationTimeMinutes = notificationTimeMinutes;
		this.notifyOnMissed = notifyOnMissed;
		this.notifyOnStart = notifyOnStart;
		this.soundEnabled = true;
		this.soundFile = "/sounds/Homecoming.mp3";
		this.availableSoundFiles = new ArrayList<>();
	}

	// New constructor including Good Moral Certificate settings
	public SettingsState(String theme, int fontSize, boolean notifications,
			int notificationTimeMinutes, boolean notifyOnMissed, boolean notifyOnStart,
			Image depedSeal, Image depedMatatag, Image lyfjshsLogo,
			String goodMoralSigner, String goodMoralPosition) {
		this(theme, fontSize, notifications, notificationTimeMinutes, notifyOnMissed, notifyOnStart);
		this.depedSealImage = depedSeal;
		this.depedMatatagImage = depedMatatag;
		this.lyfjshsLogoImage = lyfjshsLogo;
		this.goodMoralSigner = goodMoralSigner;
		this.goodMoralPosition = goodMoralPosition;
	}
}