package lyfjshs.gomis.components.FormManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.components.notification.NotificationCallback;
import lyfjshs.gomis.components.notification.NotificationManager;
import lyfjshs.gomis.components.notification.NotificationPopup;
import lyfjshs.gomis.view.appointment.AppointmentDayDetails;
import lyfjshs.gomis.view.sessions.SessionsForm;
import net.miginfocom.swing.MigLayout;
import raven.modal.Drawer;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;
import raven.modal.option.Option;
import lyfjshs.gomis.Database.DBConnection;

public class MainForm extends JPanel implements NotificationCallback {
	private List<Form> activeForms = new ArrayList<>();
	private NotificationPopup notificationPopup;
	private static JButton buttonNotification;
	private static final String MODAL_ID = "notifications";

	public MainForm() {
		init();
	}

	private void init() {
		setLayout(new MigLayout("fillx,wrap,insets 0,gap 0", "[fill]", "[][grow]"));
		add(createHeader());
		add(createMain(), "grow");
		
		// Initialize notification popup
		notificationPopup = new NotificationPopup(this, this);
	}

	private JPanel createHeader() {
		JPanel panel = new JPanel(new MigLayout("insets 3", "[]push[]push", "[fill]"));
		JToolBar toolBar = new JToolBar();

		JButton buttonDrawer = new JButton(new FlatSVGIcon("drawer/icon/menu.svg", 0.5f));

		URL iconUrl = getClass().getClassLoader().getResource("drawer/icon/menu.svg");
		System.out.println(iconUrl != null ? "Icon found: " + iconUrl : "Icon not found!");
		buttonDrawer.addActionListener(e -> {
			if (Drawer.isOpen()) {
				Drawer.showDrawer();
			} else {
				Drawer.toggleMenuOpenMode();
			}
		});
		toolBar.add(buttonDrawer);

		buttonNotification = new JButton(new FlatSVGIcon("drawer/icon/notification.svg", 0.5f));
		buttonNotification.addActionListener(e -> showNotificationPopup());
		toolBar.add(buttonNotification);
		
		panel.add(toolBar);
		return panel;
	}

	private void showNotificationPopup() {
		if (notificationPopup != null) {
			// Close existing modal if it exists
			if (ModalDialog.isIdExist(MODAL_ID)) {
				ModalDialog.closeModal(MODAL_ID);
				return;
			}

			// Get the location of the notification button
			Point location = buttonNotification.getLocationOnScreen();
			
			// Configure modal options specifically for notifications
			Option option = new Option();
			option.setOpacity(0.2f);
			option.setAnimationEnabled(false);
			
			// Set the position relative to the notification button
			option.getLayoutOption()
				.setMargin(0, 0, 0, 0)
				.setLocation(
					location.x - notificationPopup.getPreferredSize().width + buttonNotification.getWidth(),
					location.y + buttonNotification.getHeight() - 10)
				.setSize(400, 500); // Set specific size for notification popup
			
			// Show the modal with the notification popup
			SwingUtilities.invokeLater(() -> {
				ModalDialog.showModal(this, notificationPopup, option, MODAL_ID);
			});
		}
	}

	private Component createMain() {
		mainPanel = new JPanel(new BorderLayout());
		return mainPanel;
	}

	public void setForm(Form form) {
		mainPanel.removeAll();
		mainPanel.add(form);
		mainPanel.repaint();
		mainPanel.revalidate();
		activeForms.add(form);
	}

	public Form[] getAllForms() {
		return activeForms.toArray(new Form[0]);
	}

	private JPanel mainPanel;

	@Override
	public void onNotificationClicked(NotificationManager.Notification notification) {
		System.out.println("Clicked notification: " + notification.getMessage());
		
		// Hide the notification popup
		if (notificationPopup != null) {
			notificationPopup.hide();
		}

		// Try to extract appointment ID from the notification
		try {
			AppointmentDAO appointmentDAO = new AppointmentDAO(DBConnection.getConnection());
			List<Appointment> appointments = appointmentDAO.getUpcomingAppointments();
			LocalDateTime now = LocalDateTime.now();

			// Find the matching appointment from the notification message
			for (Appointment appointment : appointments) {
				String appointmentTitle = appointment.getAppointmentTitle();
				if (notification.getMessage().contains(appointmentTitle)) {
					showAppointmentDetailsModal(appointment);
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
				"Error loading appointment details: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void showAppointmentDetailsModal(Appointment appointment) {
		if (ModalDialog.isIdExist("appointment_details_" + appointment.getAppointmentId())) {
			return;
		}

		try {
			// Create and configure the appointment details panel
			AppointmentDayDetails detailsPanel = new AppointmentDayDetails(
				DBConnection.getConnection(), 
				null, 
				null,
				// Add refresh callback
				refreshedAppointment -> {
					// Refresh notifications if needed
					if (notificationPopup != null) {
						notificationPopup.updateNotifications();
					}
				}
			);
			detailsPanel.loadAppointmentDetails(appointment);

			// Configure modal options specifically for appointment details
			Option option = new Option();
			option.setOpacity(0.3f)
				.setAnimationOnClose(false)
				.getBorderOption()
				.setBorderWidth(0.5f)
				.setShadow(BorderOption.Shadow.MEDIUM);
			
			// Set specific size for appointment details modal
			option.getLayoutOption().setSize(700, 700);

			// Show modal with "Set a Session" option
			ModalDialog.showModal(
				this,
				new SimpleModalBorder(detailsPanel, "Appointment Details",
					new SimpleModalBorder.Option[] {
						new SimpleModalBorder.Option("Set a Session", SimpleModalBorder.YES_OPTION),
						new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
					},
					(controller, action) -> {
						if (action == SimpleModalBorder.YES_OPTION) {
							// Switch to sessions form and populate with appointment data
							DrawerBuilder.switchToSessionsForm();
							Form[] forms = FormManager.getForms();
							for (Form form : forms) {
								if (form instanceof SessionsForm) {
									SessionsForm sessionsForm = (SessionsForm) form;
									try {
										AppointmentDAO appointmentDAO = new AppointmentDAO(DBConnection.getConnection());
										Appointment fullAppointment = appointmentDAO.getAppointmentById(
											appointment.getAppointmentId());
										sessionsForm.populateFromAppointment(fullAppointment);
									} catch (SQLException e) {
										e.printStackTrace();
										JOptionPane.showMessageDialog(this, 
											"Error loading appointment details: " + e.getMessage());
									}
									break;
								}
							}
							controller.close();
						} else if (action == SimpleModalBorder.CLOSE_OPTION) {
							controller.close();
						}
					}),
				option,
				"appointment_details_" + appointment.getAppointmentId()
			);

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
				"Error showing appointment details: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onNotificationDisplayed(NotificationManager.Notification notification) {
		// Optional: Handle when notification is displayed
	}

	@Override
	public ImageIcon getNotificationIcon(NotificationManager.Notification notification) {
		// Return null to use default icon
		return null;
	}

    public static JButton getNotificationButton() {
		return buttonNotification;
	}
}
