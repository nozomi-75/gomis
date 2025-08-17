/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.notification;

/**
 * Interface for handling notification actions and customization.
 */
public interface NotificationCallback {
    /**
     * Called when a notification is clicked.
     * @param notification The notification that was clicked
     */
    void onNotificationClicked(NotificationManager.Notification notification);
    
    /**
     * Called when a notification is displayed.
     * @param notification The notification being displayed
     */
    void onNotificationDisplayed(NotificationManager.Notification notification);
    
    /**
     * Provides a custom icon for the notification based on its type.
     * @param notification The notification needing an icon
     * @return The icon to use, or null to use default
     */
    javax.swing.ImageIcon getNotificationIcon(NotificationManager.Notification notification);
} 