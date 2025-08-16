package lyfjshs.gomis.components;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
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
import lyfjshs.gomis.utils.SystemOutCapture;

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
		setIconImage(icon.getImage());


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
				setMinimumSize(new Dimension(1024, 600));
			}
            @Override
            public void componentResized(ComponentEvent e) {
                int contentWidth = getContentPane().getWidth();
                int contentHeight = getContentPane().getHeight();
                lyfjshs.gomis.components.FormManager.FormManager.notifyActiveFormOfResize(contentWidth, contentHeight);
            }
		});

		// Apply FlatLaf system properties
		System.setProperty("flatlaf.menuBarEmbedded", "true");
		System.setProperty("flatlaf.useWindowDecorations", "true");

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
	}

	private Image loadTrayIcon() {
		String[] iconPaths = {
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
		if (!SystemTray.isSupported()) {
			super.setVisible(false);
			return;
		}

		try {
			// When using NotificationManager, we just need to hide the window
			// and don't need to add another tray icon
			if (notificationManager != null) {
				// Get reference to the existing tray icon
				trayIcon = notificationManager.getMainTrayIcon();
				
				// Just update the popup menu of the existing tray icon to include app controls
				if (trayIcon != null) {
					PopupMenu popupMenu = trayIcon.getPopupMenu();
					if (popupMenu == null) {
						popupMenu = new PopupMenu();
						trayIcon.setPopupMenu(popupMenu);
					}
					
					// Add restore and exit items if they don't already exist
					boolean hasRestoreItem = false;
					boolean hasExitItem = false;
					
					for (int i = 0; i < popupMenu.getItemCount(); i++) {
						MenuItem item = popupMenu.getItem(i);
						if (item.getLabel().equals("Restore")) {
							hasRestoreItem = true;
						} else if (item.getLabel().equals("Exit")) {
							hasExitItem = true;
						}
					}
					
					if (!hasRestoreItem) {
						MenuItem restoreItem = new MenuItem("Restore");
						restoreItem.addActionListener(e -> restoreFromTray());
						popupMenu.add(restoreItem);
					}
					
					if (!hasExitItem) {
						MenuItem exitItem = new MenuItem("Exit");
						exitItem.addActionListener(e -> exitApplication());
						popupMenu.add(exitItem);
					}
					
					// We're using the existing tray icon
					trayIconAdded = true;
				}
			} else {
				// Fallback if notification manager isn't initialized - create our own tray icon
				Image trayImage = loadTrayIcon();
				trayIcon = new TrayIcon(trayImage, "GOMIS");
				trayIcon.setImageAutoSize(true);
				
				PopupMenu popupMenu = new PopupMenu();
				MenuItem restoreItem = new MenuItem("Restore");
				restoreItem.addActionListener(e -> restoreFromTray());
				popupMenu.add(restoreItem);

				MenuItem exitItem = new MenuItem("Exit");
				exitItem.addActionListener(e -> exitApplication());
				popupMenu.add(exitItem);
				
				trayIcon.setPopupMenu(popupMenu);
				
				// Only add our own icon if notification manager isn't available
				try {
					systemTray.add(trayIcon);
					trayIconAdded = true;
				} catch (IllegalArgumentException ex) {
					System.err.println("Warning: Could not add tray icon, it may already be present: " + ex.getMessage());
					trayIconAdded = false;
				}
			}
			
			// Hide the window
			super.setVisible(false);
			
			// Show notification
			if (notificationManager != null && notificationManager.isNotificationsEnabled()) {
				notificationManager.showInfoNotification("GOMIS", "Application minimized to tray");
			}
		} catch (AWTException e) {
			e.printStackTrace();
			// If we can't add to tray, just minimize
			super.setVisible(false);
		}
	}

	/**
	 * Restores the application from the system tray.
	 */
	private void restoreFromTray() {
		setVisible(true);
		setExtendedState(JFrame.NORMAL);
		
		// Only remove our own tray icon, not the NotificationManager's
		if (trayIconAdded && systemTray != null && trayIcon != null && notificationManager == null) {
			try {
				systemTray.remove(trayIcon);
			} catch (Exception e) {
				System.err.println("Error removing tray icon: " + e.getMessage());
			}
			trayIconAdded = false;
		}
	}

	/**
	 * Exits the application safely.
	 */
	private void exitApplication() {
		// NotificationManager cleanup will handle its own tray icon
		if (notificationManager != null) {
			notificationManager.cleanup(false); // Pass false to indicate not to exit
		} else if (trayIconAdded && systemTray != null && trayIcon != null) {
			// Only remove our own tray icon if we're not using NotificationManager
			try {
				systemTray.remove(trayIcon);
			} catch (Exception e) {
				System.err.println("Error removing tray icon during exit: " + e.getMessage());
			}
		}
		
		// Simple cleanup before exit
		try {
			// Restore original System.out streams
			SystemOutCapture.restoreSystemOut();
			
			// Close database connections
			DBConnection.closeAllConnections();
			
		} catch (Exception e) {
			// If cleanup fails, just print to console
			System.err.println("Error during cleanup: " + e.getMessage());
		}
		
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

	/**
	 * Initializes the notification system for the frame.
	 */
	public void initializeNotifications(Connection connection) {
		// Initialize NotificationManager as a singleton
		notificationManager = NotificationManager.getInstance();
		
		// Create NotificationPopup with the main content panel
		notificationPopup = new NotificationPopup((JPanel) getContentPane(), this);
		
		// Register the popup with the manager
		notificationManager.registerPopup(notificationPopup);
		
		// Disable automatic notifications upon initialization
		// This prevents the automatic "minimized to tray" notification
		if (notificationManager != null) {
			try {
				// Don't show automatic notifications for the first 10 seconds after startup
				Timer delayStartupNotifications = new Timer(true);
				delayStartupNotifications.schedule(new TimerTask() {
					@Override
					public void run() {
						// Do nothing - this just delays the first notification
					}
				}, 10000); // 10 second delay
			} catch (Exception e) {
				System.err.println("Error configuring notification delay: " + e.getMessage());
			}
		}
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
		// Don't exit the application when login view is closed
		// Just call super.dispose() for proper cleanup
		
		// Clean up notifications if needed
		if (notificationManager != null) {
			notificationManager.cleanup(false); // Pass false to indicate not to exit
		}
		
		// Remove tray icon if we added one
		if (trayIconAdded && systemTray != null && trayIcon != null) {
			try {
				systemTray.remove(trayIcon);
			} catch (Exception e) {
				// Just log the error and continue with dispose
				System.err.println("Error removing tray icon during dispose: " + e.getMessage());
			}
			trayIconAdded = false;
		}
		
		super.dispose();
	}

	// Override setVisible to prevent automatic minimization to tray
	@Override
	public void setVisible(boolean visible) {
		// If we're making the frame visible, always show it
		if (visible) {
			super.setVisible(true);
			super.setExtendedState(JFrame.NORMAL);
			super.toFront();
			super.requestFocus();
		} 
		// Only minimize to tray if explicitly requested with a specific flag
		else if (!visible && SystemTray.isSupported() && trayIconAdded) {
			// This is a normal close, let it happen
			super.setVisible(false);
		}
		// The default behavior should be to just set visibility
		else {
			super.setVisible(visible);
		}
	}

	/**
	 * Explicitly minimizes the application to the system tray.
	 * This should be called only when the user specifically requests to minimize to tray.
	 */
	public void minimizeToSystemTray() {
		minimizeToTray();
	}
}
