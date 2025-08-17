/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.notification;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

public class ClickableNotification extends JPanel {
	private static final Color WARNING_COLOR = new Color(255, 152, 0);
	private static final Color TEXT_GRAY = new Color(101, 103, 107);
	private static final Color HOVER_GRAY = new Color(242, 242, 242);
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("h:mm a, MMMM d");
	
	private final JLabel unreadDot;
	private final NotificationManager.Notification notification;
	private final NotificationCallback callback;

	public ClickableNotification(NotificationManager.Notification notification, NotificationCallback callback) {
		this.notification = notification;
		this.callback = callback;
		
		setLayout(new MigLayout("insets 12 16 12 16, fillx", "[]12[grow]", "[]"));
		setBackground(UIManager.getColor("Panel.background"));
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Notification content panel
		JPanel contentPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]", "[]4[]"));
		contentPanel.setOpaque(false);
		
		// Title in bold
		JLabel titleLabel = new JLabel(notification.getTitle());
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
		if (notification.getTitle().contains("⚠️")) {
			titleLabel.setForeground(WARNING_COLOR);
		}
		contentPanel.add(titleLabel, "cell 0 0, growx, wrap");
		
		// Message
		JLabel messageLabel = new JLabel("<html><body style='width: 200px'>" + 
			notification.getMessage() + "</body></html>");
		messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		contentPanel.add(messageLabel, "cell 0 1, growx, wrap");
		
		// Time in gray
		String timeStr = DATE_FORMAT.format(new Date(notification.getTimestamp()));
		JLabel timeLabel = new JLabel(timeStr);
		timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		timeLabel.setForeground(TEXT_GRAY);
		contentPanel.add(timeLabel, "cell 0 2");
		
		add(contentPanel, "cell 1 0, growx");

		// Unread indicator
		unreadDot = new JLabel();
		unreadDot.setOpaque(true);
		unreadDot.setBackground(new Color(24, 119, 242));
		unreadDot.setPreferredSize(new Dimension(8, 8));
		unreadDot.setBorder(BorderFactory.createEmptyBorder());
		add(unreadDot, "cell 2 0, width 8!, height 8!, gapright 8");

		// Click and hover effects
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setBackground(HOVER_GRAY);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(UIManager.getColor("Panel.background"));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				handleNotificationClick();
			}
		});
	}

	private void handleNotificationClick() {
		// Mark as read
		notification.markAsRead();
		setUnread(false);

		// Call the callback
		callback.onNotificationClicked(notification);
	}

	public void setUnread(boolean unread) {
		unreadDot.setVisible(unread);
	}
}