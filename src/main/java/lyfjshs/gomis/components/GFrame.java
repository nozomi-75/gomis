package lyfjshs.gomis.components;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.Database.DBConnection;

/**
 * The {@code GFrame} class extends {@code JFrame} and provides a customized
 * application window with additional utility methods for refreshing and
 * replacing the content panel.
 */
public class GFrame extends JFrame {

	private SystemTray systemTray;
	private TrayIcon trayIcon;
	private Timer notificationTimer;

	/**
	 * Constructs a {@code GFrame} with a specified width, height, visibility state,
	 * title, and icon.
	 *
	 * @param width   The width of the frame.
	 * @param height  The height of the frame.
	 * @param visible {@code true} to make the frame visible, {@code false}
	 *                otherwise.
	 * @param title   The title of the frame.
	 * @param icon    The icon of the frame.
	 */
	public GFrame(int width, int height, boolean visible, String title, ImageIcon icon) {
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		this.setTitle(title);

		setupSystemTray();
		startNotificationTask();

		this.setVisible(visible);
		// this.setIconImage(icon.getImage());

		// Add a component listener to adjust the minimum size after the frame is shown
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				java.awt.Insets insets = getInsets();
				int minWidth = 1250 + insets.left + insets.right;
				int minHeight = 700 + insets.top + insets.bottom;
				setMinimumSize(new Dimension(minWidth, minHeight));
				// setResizable(false);
			}
		});

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int confirm = JOptionPane.showConfirmDialog(null, "Minimize to tray instead of closing?",
						"Exit Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					minimizeToTray();
				} else if (confirm == JOptionPane.NO_OPTION) {
					exitApplication();
				}
			}
		});
	}

	/**
	 * Initializes the system tray and adds a tray icon with a menu.
	 */
	private void setupSystemTray() {
		if (!SystemTray.isSupported()) {
			JOptionPane.showMessageDialog(this, "System Tray not supported!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		systemTray = SystemTray.getSystemTray();
		Image trayImage = Toolkit.getDefaultToolkit().getImage("GOMIS_Circle.png"); // Replace with actual icon path

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
		try {
			systemTray.add(trayIcon);
			this.setVisible(false);
			showNotification("Minimized to tray", "The application is still running in the background.",
					TrayIcon.MessageType.INFO);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Restores the application from the system tray.
	 */
	private void restoreFromTray() {
		this.setVisible(true);
		systemTray.remove(trayIcon);
	}

	/**
	 * Displays a system tray notification.
	 *
	 * @param title   The notification title.
	 * @param message The notification message.
	 * @param type    The type of notification (INFO, WARNING, ERROR).
	 */
	public void showNotification(String title, String message, TrayIcon.MessageType type) {
		if (systemTray != null && trayIcon != null) {
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
				showNotification("Appointment Reminder", "You have pending appointments.", TrayIcon.MessageType.INFO);
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
		systemTray.remove(trayIcon);
		DBConnection.closeAllConnections();
		System.exit(0);
	}

	/**
	 * Sets the size of the frame along with a preferred size.
	 *
	 * @param width      The actual width of the frame.
	 * @param height     The actual height of the frame.
	 * @param prefWidth  The preferred width of the frame.
	 * @param prefHeight The preferred height of the frame.
	 */
	public void setSize(int width, int height, int prefWidth, int prefHeight) {
		this.setPreferredSize(new Dimension(prefWidth, prefHeight));
		this.setSize(width, height);
	}

	/**
	 * Refreshes the frame by revalidating and repainting the content pane. This
	 * ensures that UI changes are reflected immediately.
	 */
	public void refresh() {
		FlatAnimatedLafChange.showSnapshot();
		this.getContentPane().revalidate();
		this.getContentPane().repaint();
		FlatAnimatedLafChange.hideSnapshotWithAnimation();
	}

	/**
	 * Replaces the current content panel with a new one, updating the layout and
	 * title of the frame.
	 *
	 * @param contentPanel The new content panel to be displayed.
	 * @param manager      The layout manager to be applied to the content pane.
	 * @param title        The title to be set for the frame.
	 */
	public void replaceContentPanel(JComponent contentPanel, LayoutManager manager, String title) {
		EventQueue.invokeLater(() -> {
			FlatAnimatedLafChange.showSnapshot();
			this.getContentPane().removeAll();
			this.getContentPane().revalidate();
			this.getContentPane().repaint();
			this.getContentPane().setLayout(manager);

			this.setTitle(title);
			setContentPane(contentPanel);
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		});
	}
}
