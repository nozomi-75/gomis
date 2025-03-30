package lyfjshs.gomis.components;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.components.notification.NotificationCallback;
import lyfjshs.gomis.components.notification.NotificationManager;
import lyfjshs.gomis.components.notification.NotificationPopup;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;

/**
 * The {@code GFrame} class extends {@code JFrame} with FlatLaf enhancements.
 * It includes system tray support, notifications, and animated UI updates.
 */
public class GFrame extends JFrame implements FlatStyleableComponent, NotificationCallback {
	private static final long serialVersionUID = 1L;
	private SystemTray systemTray;
	private TrayIcon trayIcon;
	private Timer notificationTimer;
	private boolean trayIconAdded = false;
	private NotificationManager notificationManager;
	private NotificationPopup notificationPopup;
	private SettingsManager settingsManager;

	/**
	 * Constructs a {@code GFrame} with specified properties.
	 * 
	 * @param width   The width of the frame.
	 * @param height  The height of the frame.
	 * @param visible {@code true} to make the frame visible, {@code false}
	 *                otherwise.
	 * @param title   The title of the frame.
	 * @param icon    The icon of the frame.
	 */
	public GFrame(int width, int height, boolean visible, String title, ImageIcon icon, Connection conn) {
		super(title);
		this.settingsManager = new SettingsManager(conn);

		// Basic frame setup first
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(width, height);
		setLocationRelativeTo(null);
		setTitle(title);

		// Create and set the main content panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setOpaque(true);
		setContentPane(mainPanel);

		// Initialize system tray
		setupSystemTray();

		// Add window listeners
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				handleWindowClosing();
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				setMinimumSize(new Dimension(1250, 700));
			}
		});

		// Apply FlatLaf system properties
		System.setProperty("flatlaf.menuBarEmbedded", "true");
		System.setProperty("flatlaf.useWindowDecorations", "true");

		// Ensure frame is packed before applying settings
		pack();

		// Now apply settings after frame is fully initialized
		SwingUtilities.invokeLater(() -> {
			try {
				settingsManager.initializeAppSettings();
				if (getRootPane() != null) {
					putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
					putClientProperty(FlatClientProperties.USE_WINDOW_DECORATIONS, true);
					getRootPane().putClientProperty("JRootPane.roundedCorners", true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Handles window closing behavior (prompt user for minimize or exit).
	 */
	private void handleWindowClosing() {
		int confirm = JOptionPane.showConfirmDialog(null,
				"Minimize to tray instead of closing?", "Exit Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			minimizeToTray();
		} else if (confirm == JOptionPane.NO_OPTION) {
			exitApplication();
		}
	}

	/**
	 * Initializes the system tray and adds a tray icon with a menu.
	 */
	private void setupSystemTray() {
		if (!SystemTray.isSupported())
			return;

		systemTray = SystemTray.getSystemTray();
		Image trayImage = loadTrayIcon();

		PopupMenu popupMenu = new PopupMenu();
		MenuItem restoreItem = new MenuItem("Restore");
		restoreItem.addActionListener(e -> restoreFromTray());
		popupMenu.add(restoreItem);

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(e -> exitApplication());
		popupMenu.add(exitItem);

		trayIcon = new TrayIcon(trayImage, "GOMIS", popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(e -> restoreFromTray());
	}

	private Image loadTrayIcon() {
		String[] iconPaths = {
			"/icons/app_icon.png",
			"/images/GOMIS Logo.png",
			"/GOMIS Logo.png",
			"/GOMIS_Circle.png"
		};
		
		for (String path : iconPaths) {
			java.net.URL iconUrl = getClass().getResource(path);
			if (iconUrl != null) {
				return new ImageIcon(iconUrl).getImage();
			}
		}
		
		// Create fallback icon
		java.awt.image.BufferedImage fallbackImage = new java.awt.image.BufferedImage(
			16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g2 = fallbackImage.createGraphics();
		g2.setColor(new Color(0x004aad));
		g2.fillRect(0, 0, 16, 16);
		g2.dispose();
		return fallbackImage;
	}

	/**
	 * Minimizes the application to the system tray.
	 */
	private void minimizeToTray() {
		if (!SystemTray.isSupported() || trayIconAdded) {
			setVisible(false);
			return;
		}

		try {
			systemTray.add(trayIcon);
			trayIconAdded = true;
			setVisible(false);
			if (notificationManager != null) {
				notificationManager.showInfoNotification("GOMIS", "Application minimized to tray");
			}
		} catch (AWTException e) {
			e.printStackTrace();
			// If we can't add to tray, just minimize
			setVisible(false);
		}
	}

	/**
	 * Restores the application from the system tray.
	 */
	private void restoreFromTray() {
		setVisible(true);
		setExtendedState(JFrame.NORMAL);
		if (trayIconAdded && systemTray != null) {
			systemTray.remove(trayIcon);
			trayIconAdded = false;
		}
	}

	/**
	 * Exits the application safely.
	 */
	private void exitApplication() {
		if (notificationManager != null) {
			notificationManager.cleanup();
		}
		if (trayIconAdded) {
			systemTray.remove(trayIcon);
		}
		DBConnection.closeAllConnections();
		System.exit(0);
	}

	/**
	 * Refreshes the frame with FlatLaf animation.
	 */
	public void refresh() {
		FlatAnimatedLafChange.showSnapshot();
		getContentPane().revalidate();
		getContentPane().repaint();
		FlatAnimatedLafChange.hideSnapshotWithAnimation();
	}

	/**
	 * Replaces the content panel with a new one, updating the layout and title.
	 * 
	 * @param contentPanel The new content panel.
	 * @param manager      The layout manager.
	 * @param title        The new frame title.
	 */
	public void replaceContentPanel(JComponent contentPanel, LayoutManager manager, String title) {
		SwingUtilities.invokeLater(() -> {
			FlatAnimatedLafChange.showSnapshot();
			JPanel mainPanel = (JPanel) getContentPane();
			mainPanel.removeAll();
			mainPanel.setLayout(manager != null ? manager : new BorderLayout());
			mainPanel.add(contentPanel, BorderLayout.CENTER);
			setTitle(title);
			mainPanel.revalidate();
			mainPanel.repaint();
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		});
	}

	/**
	 * Updates settings dynamically when a change is made.
	 */
	public void updateSettings() {
		SwingUtilities.invokeLater(() -> {
			try {
				settingsManager.initializeAppSettings();
				if (getRootPane() != null) {
					putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
					putClientProperty(FlatClientProperties.USE_WINDOW_DECORATIONS, true);
					getRootPane().putClientProperty("JRootPane.roundedCorners", true);
				}
				refresh();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public Object getClientProperty(Object key) {
		if (getRootPane() != null) {
			return getRootPane().getClientProperty(key);
		}
		return null;
	}

	@Override
	public void putClientProperty(Object key, Object value) {
		if (getRootPane() != null) {
			getRootPane().putClientProperty(key, value);
		}
	}

	public void initializeNotifications(Connection connection) {
		// Initialize NotificationManager as a singleton
		notificationManager = NotificationManager.getInstance();
		
		// Create NotificationPopup with the main content panel
		notificationPopup = new NotificationPopup((JPanel) getContentPane(), this);
		
		// Register the popup with the manager
		notificationManager.registerPopup(notificationPopup);
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public NotificationPopup getNotificationPopup() {
		return notificationPopup;
	}

	// NotificationCallback implementation
	@Override
	public void onNotificationClicked(NotificationManager.Notification notification) {
		// Handle notification click
		System.out.println("Notification clicked: " + notification.getMessage());
		if (notificationPopup != null) {
			notificationPopup.hide();
		}
	}

	@Override
	public void onNotificationDisplayed(NotificationManager.Notification notification) {
		// Handle notification display
		System.out.println("Notification displayed: " + notification.getMessage());
	}

	@Override
	public ImageIcon getNotificationIcon(NotificationManager.Notification notification) {
		// Return custom icon based on notification type
		return new ImageIcon(loadTrayIcon());
	}

	@Override
	public void dispose() {
		if (notificationManager != null) {
			notificationManager.cleanup();
		}
		if (trayIconAdded && systemTray != null) {
			systemTray.remove(trayIcon);
			trayIconAdded = false;
		}
		super.dispose();
	}

	// Override setVisible to minimize to tray
	@Override
	public void setVisible(boolean visible) {
		if (!visible && SystemTray.isSupported()) {
			minimizeToTray();
		} else {
			super.setVisible(visible);
		}
	}
}
