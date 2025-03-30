package lyfjshs.gomis.components.notification;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.Modal;
import raven.modal.option.Option;

public class NotificationPopup extends Modal {
	private static final Color BORDER_COLOR = new Color(229, 229, 229);
	private final JPanel notificationsContainer;
	private final NotificationManager notificationManager;
	private final JPanel parent;
	private final NotificationCallback callback;
	private static final String MODAL_ID = "notifications";

	public NotificationPopup(JPanel parent, NotificationCallback callback) {
		this.parent = parent;
		this.callback = callback;
		this.notificationManager = NotificationManager.getInstance();
		notificationManager.registerPopup(this);

		// Main content pane with theme-aware background
		setLayout(new MigLayout("insets 0, fillx", "[grow]", "[][grow]"));
		setBackground(UIManager.getColor("Panel.background"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		// Header panel with theme-aware background
		JPanel headerPanel = new JPanel(new MigLayout("insets 15 20 15 20, fillx", "[grow][]", "[]"));
		headerPanel.setBackground(UIManager.getColor("Panel.background"));
		headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));

		// Title with modern font
		JLabel titleLabel = new JLabel("Notifications");
		titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		titleLabel.setForeground(UIManager.getColor("Label.foreground"));
		headerPanel.add(titleLabel, "cell 0 0");

		// Add header to content pane
		add(headerPanel, "cell 0 0, growx, wrap");

		// Notifications container with theme-aware background
		notificationsContainer = new JPanel(new MigLayout("insets 0, fillx", "[grow]", ""));
		notificationsContainer.setBackground(UIManager.getColor("Panel.background"));

		// Load existing notifications
		updateNotifications();

		// Scroll pane for notifications
		JScrollPane scrollPane = new JScrollPane(notificationsContainer);
		scrollPane.setBorder(null);
		scrollPane.setBackground(UIManager.getColor("Panel.background"));
		scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		// Add scroll pane to content
		add(scrollPane, "cell 0 1, grow");

		// Set size
		setPreferredSize(new Dimension(360, 480));

		// Add component listener to parent to update position when parent moves
		parent.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentMoved(java.awt.event.ComponentEvent e) {
				if (isShowing()) {
					updatePosition();
				}
			}
			
			@Override
			public void componentHidden(java.awt.event.ComponentEvent e) {
				hide();
			}
		});
	}

	private void updatePosition() {
		Point location = parent.getLocationOnScreen();
		JButton notificationButton = findNotificationButton(parent);
		if (notificationButton != null) {
			location = notificationButton.getLocationOnScreen();
			
			// Close existing modal if it exists
			if (ModalDialog.isIdExist(MODAL_ID)) {
				ModalDialog.closeModal(MODAL_ID);
			}
			
			Option option = ModalDialog.createOption();
			option.setOpacity(0f);
			option.setAnimationEnabled(false);
			
			option.getLayoutOption()
				.setMargin(0, 0, 0, 0)
				.setLocation(
					location.x - getPreferredSize().width + notificationButton.getWidth(),
					location.y + notificationButton.getHeight());
			
			ModalDialog.showModal(parent, this, option, MODAL_ID);
		}
	}

	private JButton findNotificationButton(Container container) {
		for (Component comp : container.getComponents()) {
			if (comp instanceof JButton && "N".equals(((JButton) comp).getText())) {
				return (JButton) comp;
			}
			if (comp instanceof Container) {
				JButton result = findNotificationButton((Container) comp);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	public void addNotification(NotificationManager.Notification notification) {
		SwingUtilities.invokeLater(() -> {
			// Add new notification at the top
			ClickableNotification clickableNotification = new ClickableNotification(notification, this, callback);
			clickableNotification.setUnread(notification.isUnread());
			notificationsContainer.add(clickableNotification, "wrap, growx", 0);  // Add at index 0
			notificationsContainer.revalidate();
			notificationsContainer.repaint();
		});
	}

	public void updateNotifications() {
		SwingUtilities.invokeLater(() -> {
			notificationsContainer.removeAll();
			for (NotificationManager.Notification notification : notificationManager.getNotifications()) {
				ClickableNotification clickableNotification = new ClickableNotification(notification, this, callback);
				clickableNotification.setUnread(notification.isUnread());
				notificationsContainer.add(clickableNotification, "wrap, growx");
			}
			notificationsContainer.revalidate();
			notificationsContainer.repaint();
		});
	}

	public NotificationCallback getCallback() {
		return callback;
	}

	public void show() {
		updatePosition();
		setVisible(true);
	}

	public void hide() {
		if (ModalDialog.isIdExist(MODAL_ID)) {
			ModalDialog.closeModal(MODAL_ID);
		}
	}
}
