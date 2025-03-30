package lyfjshs.gomis.components.notification;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class ClickableNotification extends JPanel {
	private static final Color FB_BLUE = new Color(24, 119, 242);
	private static final Color WARNING_COLOR = new Color(255, 152, 0);
	private static final Color TEXT_GRAY = new Color(101, 103, 107);
	private static final Color HOVER_GRAY = new Color(242, 242, 242);
	private static final int PROFILE_PIC_SIZE = 32;
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("h:mm a, MMMM d");
	
	private final JLabel unreadDot;
	private final NotificationManager.Notification notification;
	private final NotificationPopup popup;
	private final NotificationCallback callback;

	public ClickableNotification(NotificationManager.Notification notification, NotificationPopup popup, NotificationCallback callback) {
		this.notification = notification;
		this.popup = popup;
		this.callback = callback;
		
		setLayout(new MigLayout("insets 12 16 12 16, fillx", "[]12[grow]", "[]"));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 229, 229)));
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Profile picture with circular mask
		ImageIcon profileIcon = callback.getNotificationIcon(notification);
		if (profileIcon == null) {
			profileIcon = createDefaultProfileIcon();
		}
		JLabel profilePic = new CircularImageLabel(profileIcon.getImage(), PROFILE_PIC_SIZE);
		add(profilePic, "cell 0 0");

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
		unreadDot.setBackground(FB_BLUE);
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
				setBackground(Color.WHITE);
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

	// Custom JLabel for circular profile picture
	private static class CircularImageLabel extends JLabel {
		private final Image image;
		private final int size;

		public CircularImageLabel(Image image, int size) {
			this.image = image;
			this.size = size;
			setPreferredSize(new Dimension(size, size));
		}

		@Override
		protected void paintComponent(java.awt.Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Create circular clipping path
			java.awt.geom.Ellipse2D.Double circle = new java.awt.geom.Ellipse2D.Double(0, 0, size, size);
			g2.setClip(circle);
			
			// Draw the image
			g2.drawImage(image, 0, 0, size, size, this);
			g2.dispose();
		}
	}

	private ImageIcon createDefaultProfileIcon() {
		BufferedImage defaultIcon = new BufferedImage(
			PROFILE_PIC_SIZE, PROFILE_PIC_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = defaultIcon.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(FB_BLUE);
		g2d.fillOval(0, 0, PROFILE_PIC_SIZE - 1, PROFILE_PIC_SIZE - 1);
		g2d.dispose();
		return new ImageIcon(defaultIcon);
	}

	public void setUnread(boolean unread) {
		unreadDot.setVisible(unread);
	}

}