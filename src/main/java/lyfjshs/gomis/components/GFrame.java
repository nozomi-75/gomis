package lyfjshs.gomis.components;

import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.components.FlatStyleableComponent;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;

/**
 * The {@code GFrame} class extends {@code JFrame} with FlatLaf enhancements.
 * It includes system tray support, notifications, and animated UI updates.
 */
public class GFrame extends JFrame implements FlatStyleableComponent {
	private static final long serialVersionUID = 1L;
	private SystemTray systemTray;
	private TrayIcon trayIcon;
	private Timer notificationTimer;
	private boolean trayIconAdded = false;
	private NotificationManager notificationManager;

	private SettingsManager settingsManager; // 🔹 Reference to SettingsManager

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
		this.settingsManager = new SettingsManager(conn);
		SettingsState state = settingsManager.getSettingsState(); // 🔹 Load saved settings

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(width, height);
		setLocationRelativeTo(null);
		setTitle(title);

		applySettings(state); // 🔹 Apply theme & font from settings

		setupSystemTray();
		startNotificationTask();

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
	}

	/**
	 * Applies settings such as theme, font, and window properties.
	 */
	private void applySettings(SettingsState state) {
		settingsManager.initializeAppSettings(); // 🔹 Apply the theme & font

		// Enable full window content styling
		putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
		putClientProperty(FlatClientProperties.USE_WINDOW_DECORATIONS, true);
		getRootPane().putClientProperty("JRootPane.roundedCorners", true);

		// Apply FlatLaf system properties for better integration
		System.setProperty("flatlaf.menuBarEmbedded", "true");
		System.setProperty("flatlaf.useWindowDecorations", "true");

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
		Image trayImage = Toolkit.getDefaultToolkit().getImage("GOMIS_Circle.png"); // Update path

		PopupMenu popupMenu = new PopupMenu();
		MenuItem restoreItem = new MenuItem("Restore");
		restoreItem.addActionListener(e -> restoreFromTray());
		popupMenu.add(restoreItem);

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(e -> exitApplication());
		popupMenu.add(exitItem);

		trayIcon = new TrayIcon(trayImage, "GFrame Running", popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(e -> restoreFromTray());
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
			showNotification("Minimized to Tray", "The application is still running in the background.",
					TrayIcon.MessageType.INFO);
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
	 * Displays a system tray notification.
	 * 
	 * @param title   The notification title.
	 * @param message The notification message.
	 * @param type    The notification type (INFO, WARNING, ERROR).
	 */
	public void showNotification(String title, String message, TrayIcon.MessageType type) {
		if (trayIcon != null) {
			trayIcon.displayMessage(title, message, type);
		}
	}

	/**
	 * Starts a background task that triggers notifications at intervals.
	 */
	private void startNotificationTask() {
		notificationTimer = new Timer();
		notificationTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				showNotification("Appointment Reminder", "You have pending appointments.",
						TrayIcon.MessageType.INFO);
			}
		}, 5000, 30000); // First notification in 5 sec, then every 30 sec
	}

	/**
	 * Exits the application safely.
	 */
	private void exitApplication() {
		if (notificationTimer != null) {
			notificationTimer.cancel();
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
			getContentPane().removeAll();
			getContentPane().setLayout(manager);
			setTitle(title);
			setContentPane(contentPanel);
			getContentPane().revalidate();
			getContentPane().repaint();
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		});
	}

	/**
	 * Updates settings dynamically when a change is made.
	 */
	public void updateSettings() {
		SettingsState state = settingsManager.getSettingsState();
		applySettings(state); // 🔹 Reapply theme & font dynamically
		refresh();
	}

	@Override
	public Object getClientProperty(Object key) {
		return getRootPane().getClientProperty(key);
	}

	@Override
	public void putClientProperty(Object key, Object value) {
		getRootPane().putClientProperty(key, value);
	}

	public void initializeTrayIcon() {
		if (!SystemTray.isSupported()) {
			System.err.println("System tray is not supported");
			return;
		}

		if (trayIcon != null) {
			// Clean up existing tray icon if any
			if (trayIconAdded && systemTray != null) {
				systemTray.remove(trayIcon);
				trayIconAdded = false;
			}
			trayIcon = null;
		}

		systemTray = SystemTray.getSystemTray();
		
		// Try multiple possible icon locations
		Image image = null;
		String[] iconPaths = {
			"/icons/app_icon.png",
			"/images/GOMIS Logo.png",
			"/GOMIS Logo.png",
			"/GOMIS_Circle.png"
		};
		
		for (String path : iconPaths) {
			java.net.URL iconUrl = getClass().getResource(path);
			if (iconUrl != null) {
				image = new ImageIcon(iconUrl).getImage();
				break;
			}
		}
		
		// If no icon found, create a default one
		if (image == null) {
			// Create a simple colored square as fallback
			java.awt.image.BufferedImage fallbackImage = new java.awt.image.BufferedImage(
				16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g2 = fallbackImage.createGraphics();
			g2.setColor(new Color(0x004aad));
			g2.fillRect(0, 0, 16, 16);
			g2.dispose();
			image = fallbackImage;
			System.err.println("Warning: Could not load tray icon, using fallback");
		}
		
		PopupMenu popup = new PopupMenu();
		MenuItem showItem = new MenuItem("Show");
		showItem.addActionListener(e -> {
			setVisible(true);
			setExtendedState(JFrame.NORMAL);
		});
		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(e -> {
			if (notificationManager != null) {
				notificationManager.stopNotificationServices();
			}
			System.exit(0);
		});
		
		popup.add(showItem);
		popup.addSeparator();
		popup.add(exitItem);
		
		trayIcon = new TrayIcon(image, "GOMIS", popup);
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(e -> {
			setVisible(true);
			setExtendedState(JFrame.NORMAL);
		});
	}

	public void initializeNotifications(Connection connection) {
		if (trayIcon != null) {
			notificationManager = new NotificationManager(connection, trayIcon);
			// Only start notifications if enabled in settings
			if (SettingsManager.getCurrentState().notifications) {
				notificationManager.startNotificationServices();
			}
		}
	}

	@Override
	public void dispose() {
		if (notificationManager != null) {
			notificationManager.stopNotificationServices();
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
			// Minimize to tray
			super.setVisible(false);
		} else {
			super.setVisible(visible);
		}
	}
}
