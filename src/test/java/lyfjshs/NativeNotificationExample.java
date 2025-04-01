package lyfjshs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.components.notification.NotificationCallback;
import lyfjshs.gomis.components.notification.NotificationManager;
import lyfjshs.gomis.components.notification.NotificationPopup;
import net.miginfocom.swing.MigLayout;

/**
 * Example application demonstrating the NotificationManager functionality.
 */
public class NativeNotificationExample extends JFrame implements NotificationCallback {
	private static final String WINDOW_TITLE = "GOMIS Notification Demo";
	private final NotificationManager notificationManager;
	private NotificationPopup notificationPopup;
	private JButton notificationButton;
	private final JPanel mainPanel;

	public NativeNotificationExample() {
		super(WINDOW_TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);

		// Initialize notification manager
		notificationManager = NotificationManager.getInstance();

		// Create main panel
		mainPanel = new JPanel(new MigLayout("fill", "[grow][][grow]", "[grow][][][][][][grow]"));
		mainPanel.setBackground(Color.DARK_GRAY);
		setContentPane(mainPanel);

		// Create the floating notification window
		notificationPopup = new NotificationPopup(mainPanel, this);

		// Add some test notifications
		addTestNotifications();
	}

	private void toggleNotificationWindow() {
		if (notificationPopup == null) {
			notificationPopup = new NotificationPopup(mainPanel, this);
		}
		notificationPopup.show();
	}

	private void showError(Exception ex) {
		JOptionPane.showMessageDialog(this, "Error showing notification: " + ex.getMessage(), "Notification Error",
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void onNotificationClicked(NotificationManager.Notification notification) {
		System.out.println("Clicked notification: " + notification.getMessage());
		if (notificationPopup != null) {
			notificationPopup.hide();
		}
	}

	@Override
	public void onNotificationDisplayed(NotificationManager.Notification notification) {
		// Optional: Handle when notification is displayed
	}

	@Override
	public ImageIcon getNotificationIcon(NotificationManager.Notification notification) {
		// Optional: Return custom icon for notification
            return null;
        }

	private void openAppointmentDetails(NotificationManager.Notification notification) {
		// Example method to open appointment details
		// You would implement this to open your appointment UI
		JOptionPane.showMessageDialog(this,
			"Opening appointment details...\n" + notification.getMessage(),
			"Appointment Details",
			JOptionPane.INFORMATION_MESSAGE);
	}

	private void openInformationScreen(NotificationManager.Notification notification) {
		// Example method to open information screen
		// You would implement this to open your information UI
		JOptionPane.showMessageDialog(this,
			"Opening information screen...\n" + notification.getMessage(),
			"Information Details",
			JOptionPane.INFORMATION_MESSAGE);
	}

	private void addTestNotifications() {
		// Interactive Notification Button
		JButton interactiveButton = new JButton("Interactive Notification");
		interactiveButton.addActionListener(e -> {
			try {
				notificationManager.showInteractiveNotification("GOMIS Interactive",
						"Click this notification to interact!");
			} catch (Exception ex) {
				showError(ex);
			}
		});

		// Custom Notification Button
		JButton customButton = new JButton("Custom Notification");
		customButton.addActionListener(e -> {
			try {
				notificationManager.showCustomNotification("GOMIS Custom",
						"This is a custom notification with the GOMIS icon!");
			} catch (Exception ex) {
				showError(ex);
			}
		});

		// Error Notification Button
		JButton errorButton = new JButton("Error Notification");
		errorButton.addActionListener(e -> {
			try {
				notificationManager.showErrorNotification("GOMIS Error", "This is an error message!");
			} catch (Exception ex) {
				showError(ex);
			}
		});

		// Warning Notification Button
		JButton warningButton = new JButton("Warning Notification");
		warningButton.addActionListener(e -> {
			try {
				notificationManager.showWarningNotification("GOMIS Warning", "This is a warning message!");
			} catch (Exception ex) {
				showError(ex);
			}
		});

		// Info Notification Button
		JButton infoButton = new JButton("Info Notification");
		infoButton.addActionListener(e -> {
			try {
				notificationManager.showInfoNotification("GOMIS Info", "This is an informational message.");
			} catch (Exception ex) {
				showError(ex);
			}
		});
		
				// Interactive Notification Button
				notificationButton = new JButton("N");
				notificationButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
				notificationButton.setForeground(Color.WHITE);
				notificationButton.setBackground(new Color(59, 89, 152));
				notificationButton.setFocusPainted(false);
				notificationButton.setBorderPainted(false);
				notificationButton.setPreferredSize(new Dimension(40, 40));
				notificationButton.addActionListener(e -> toggleNotificationWindow());
				
						// Add button to top-right corner
						mainPanel.add(notificationButton, "cell 2 0,alignx right,aligny top");

		// Add buttons to main panel
		mainPanel.add(infoButton, "cell 1 1,growx");
		mainPanel.add(warningButton, "cell 1 2,growx");
		mainPanel.add(errorButton, "cell 1 3,growx");
		mainPanel.add(customButton, "cell 1 4,growx");
		mainPanel.add(interactiveButton, "cell 1 5,growx");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				// Set system look and feel first
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				// Then apply FlatLaf
				if (!FlatMacLightLaf.setup()) {
					System.err.println("Failed to initialize FlatLaf, using system look and feel");
				}

				JFrame frame = new NativeNotificationExample();
				frame.setVisible(true);

				// Add window listener to clean up when closing
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						NotificationManager.getInstance().cleanup();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to start application: " + e.getMessage(), "Startup Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		});
    }
}