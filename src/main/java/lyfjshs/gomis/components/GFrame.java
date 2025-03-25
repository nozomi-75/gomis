package lyfjshs.gomis.components;

import java.awt.AWTException;
import java.awt.Dimension;
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
import javax.swing.SwingUtilities;

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
		if (systemTray == null || trayIconAdded)
			return;

		try {
			systemTray.add(trayIcon);
			trayIconAdded = true;
			setVisible(false);
			showNotification("Minimized to Tray", "The application is still running in the background.",
					TrayIcon.MessageType.INFO);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Restores the application from the system tray.
	 */
	private void restoreFromTray() {
		setVisible(true);
		if (trayIconAdded) {
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
}
