package lyfjshs.gomis.view.appointment;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.alarm.AlarmManagement;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;

public class AppointmentManagement extends Form {
	private Connection connection;
	private SlidePane slidePane;
	private AppointmentHistory appointmentHistory;
	private AppointmentCalendar appointmentCalendar;
	private JPanel cancelledAppointmentsPanel;
	public AppointmentDAO appointmentDAO;
	private JButton actionButton;
	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AppointmentManagement.class);
	private ScheduledExecutorService scheduler;
	private AlarmManagement alarmManagement;

	public AppointmentManagement(Connection connection) {
		this.connection = connection;
		this.appointmentDAO = new AppointmentDAO(connection);
		setLayout(new MigLayout("wrap 1, fill, insets 0", "[grow]", "[pref!][grow]"));

		// Header panel
		JPanel headerPanel = new JPanel(new MigLayout("fill", "[100px][grow][150px]", "[]"));
		headerPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		add(headerPanel, "growx");

		JButton addAppointBtn = new JButton("Add");
		addAppointBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		addAppointBtn.addActionListener(e -> createAppointment());
		headerPanel.add(addAppointBtn, "cell 0 0");

		JLabel titleLabel = new JLabel("Appointments", SwingConstants.CENTER);
		titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
		titleLabel.setHorizontalAlignment(JLabel.CENTER);
		headerPanel.add(titleLabel, "cell 1 0, growx");

		actionButton = new JButton("Show Cancelled");
		actionButton.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		actionButton.addActionListener(e -> toggleView());
		headerPanel.add(actionButton, "cell 2 0, alignx right");

		// Initialize SlidePane with content area
		slidePane = new SlidePane();
		slidePane.setOpaque(true);

		// Wrap SlidePane in JScrollPane
		JScrollPane scrollPane = new JScrollPane(slidePane);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, "grow");

		// Initialize views
		appointmentCalendar = new AppointmentCalendar(appointmentDAO, connection, new lyfjshs.gomis.Database.DAO.SessionsDAO(connection));
		cancelledAppointmentsPanel = createCancelledAppointmentsPanel();

		// Start with month view
		slidePane.addSlide(appointmentCalendar, SlidePaneTransition.Type.FORWARD);

		// Initialize the toolbar with buttons for managing appointments
		initializeToolbar();
		
		// Subscribe to appointment events
		EventBus.subscribe(this, "appointment_created", this::handleAppointmentCreated);
		EventBus.subscribe(this, "appointment_updated", this::handleAppointmentUpdated);
		EventBus.subscribe(this, "appointment_deleted", this::handleAppointmentDeleted);
		EventBus.subscribe(this, "appointment_status_changed", data -> refreshViews());
		EventBus.subscribe(this, "counselor_logged_in", data -> {
			startStatusUpdateScheduler(); // AlarmManagement will handle notifications and alarms
		});
		
		// Initialize alarm system
		initializeAlarmSystem();
	}

	private void openSessionForm(Appointment appointment) {
				try {
					DrawerBuilder.switchToSessionsFillUpFormPanel();
					Form[] forms = FormManager.getForms();
					for (Form form : forms) {
						if (form instanceof SessionsFillUpFormPanel) {
							SessionsFillUpFormPanel SessionsFillUpFormPanel = (SessionsFillUpFormPanel) form;
							SessionsFillUpFormPanel.populateFromAppointment(appointment);
							
							appointment.setAppointmentStatus("In Progress");
							appointmentDAO.updateAppointment(appointment);
							break;
						}
					}
				} catch (Exception e) {
					logger.error("Error opening session form", (Throwable) e);
			JOptionPane.showMessageDialog(null,
						"Error opening session form: " + e.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
		}
	}

	public void openSessionFromAlarm(Appointment appointment) {
		if (appointment == null) {
			logger.warn("Attempted to open session from alarm with null appointment");
			return;
		}

		// Ensure we're on the EDT
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> openSessionFromAlarm(appointment));
			return;
		}

		try {
			// Switch to sessions form
			DrawerBuilder.switchToSessionsFillUpFormPanel();
			SwingUtilities.invokeLater(() -> {
				Form[] forms = FormManager.getForms();
				for (Form form : forms) {
					if (form instanceof SessionsFillUpFormPanel) {
						SessionsFillUpFormPanel SessionsFillUpFormPanel = (SessionsFillUpFormPanel) form;
						SessionsFillUpFormPanel.populateFromAppointment(appointment);
						SessionsFillUpFormPanel.revalidate();
						SessionsFillUpFormPanel.repaint();
						break;
					}
				}
			});
			// Update appointment status to "In Progress"
			appointment.setAppointmentStatus("In Progress");
			appointmentDAO.updateAppointment(appointment);
			// Refresh views to show updated status
			refreshViews();
		} catch (Exception e) {
			logger.error("Error opening session from alarm", (Throwable) e);
			JOptionPane.showMessageDialog(this,
				"Error opening session from alarm: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void toggleView() {
		if (slidePane.getSlideComponent() instanceof AppointmentCalendar) {
			// Switch to cancelled appointments view
			slidePane.addSlide(cancelledAppointmentsPanel, SlidePaneTransition.Type.FORWARD);
			actionButton.setText("Show Calendar");
		} else {
			// Switch back to calendar view
			slidePane.addSlide(appointmentCalendar, SlidePaneTransition.Type.BACK);
			actionButton.setText("Show Cancelled");
		}
	}

	private JPanel createCancelledAppointmentsPanel() {
		JPanel panel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		appointmentHistory = new AppointmentHistory(appointmentDAO, connection);
		panel.add(appointmentHistory, "grow");
		return panel;
	}

	private void createAppointment() {
		Appointment newAppointment = new Appointment();
		newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now())); // Set default date and time

		// Set the guidanceCounselorId from FormManager
		newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());
		
		// Set default status to Active
		newAppointment.setAppointmentStatus("Active");

		AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDAO, connection);

		// Use AddAppointmentModal to show the dialog
		AddAppointmentModal.getInstance().showModal(connection, this, addAppointmentPanel, appointmentDAO, 700, 650, () -> {
			// After saving the appointment, refresh views to show the new appointment
			refreshViews();
		});
	}

	public void refreshViews() {
		SwingUtilities.invokeLater(() -> {
			if (appointmentCalendar != null) {
				appointmentCalendar.refreshCalendar();
			}
			if (appointmentHistory != null) {
				appointmentHistory.updateAppointmentsDisplay();
			}
			
			// Also update appointment statuses when refreshing views
			try {
				appointmentDAO.checkAndUpdateMissedWithoutSessions();
			} catch (SQLException e) {
				logger.error("Error updating missed appointment statuses during refresh", (Throwable) e);
			}
		});
	}

	private void startStatusUpdateScheduler() {
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
			try {
				// Skip if no counselor is logged in
				if (Main.formManager == null || Main.formManager.getCounselorObject() == null) {
					logger.info("No counselor logged in. Skipping appointment status update.");
					return;
				}
				
				// Check and update appointment statuses using the DAO method
				int updatedCount = appointmentDAO.checkAndUpdateMissedWithoutSessions();
				
				// Refresh views if any appointments were updated
				if (updatedCount > 0) {
					logger.info("Updated status for " + updatedCount + " appointments");
					// java.awt.EventQueue.invokeLater(this::refreshViews); // Removed recursive call
					EventBus.publish("appointment_status_changed", null); // Publish a generic event
				}
			} catch (SQLException e) {
				logger.error("Error updating appointment statuses", (Throwable) e);
			}
		}, 0, 5, TimeUnit.MINUTES); // Check every 5 minutes
	}

	@Override
	public void dispose() {
		// Cancel timers
		if (scheduler != null) {
			scheduler.shutdownNow();
			scheduler = null;
		}
		
		// Unsubscribe from EventBus
		EventBus.unsubscribeAll(this);
		
		super.dispose();
	}

	@Override
	public void formRefresh() {
		refreshViews();
	}

	public void handleAppointmentUpdated(Object appointmentId) {
		logger.info("Appointment updated with ID: " + appointmentId);
		
		// Clear notified status for this appointment in AlarmManagement to allow re-alarming if rescheduled
		if (appointmentId instanceof Integer) {
			AlarmManagement.getInstance().clearNotifiedStatus((Integer) appointmentId);
		}

		// Refresh all views to reflect the updated appointment
		refreshViews();
	}

	public void handleAppointmentDeleted(Object appointmentId) {
		logger.info("Appointment deleted with ID: " + appointmentId);
		
		// Refresh all views to reflect the deleted appointment
		refreshViews();
		
		// Show confirmation message
		/* Removed redundant dialog
		JOptionPane.showMessageDialog(
			this,
			"Appointment has been successfully deleted.",
			"Deletion Successful",
			JOptionPane.INFORMATION_MESSAGE
		);
		*/
	}

	/**
	 * Get the AppointmentDAO instance
	 * @return The AppointmentDAO instance
	 */
	public lyfjshs.gomis.Database.DAO.AppointmentDAO getAppointmentDAO() {
		return appointmentDAO;
	}

	/**
	 * Initialize the toolbar with buttons for managing appointments
	 */
	private void initializeToolbar() {
		// Create a "Create Session" button for the header panel
		JButton createSessionButton = new JButton("Create Session");
		createSessionButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
		createSessionButton.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		createSessionButton.setToolTipText("Create a new counseling session from an existing appointment");
		createSessionButton.addActionListener(e -> createSessionFromAppointment());
		
		// Create an "Edit Appointment" button
		JButton editAppointmentButton = new JButton("Edit Appointment");
		editAppointmentButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_BORDERLESS);
		editAppointmentButton.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		editAppointmentButton.setToolTipText("Edit an existing appointment");
		editAppointmentButton.addActionListener(e -> {
			try {
				AppointmentSearchPanel searchPanel = new AppointmentSearchPanel(connection);
				int option = JOptionPane.showConfirmDialog(
					this,
					searchPanel,
					"Select Appointment to Edit",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE
				);
				if (option == JOptionPane.OK_OPTION) {
					Integer selectedId = searchPanel.getSelectedAppointmentId();
					if (selectedId != null) {
						editSelectedAppointment(selectedId);
					} else {
						JOptionPane.showMessageDialog(this, "Please select an appointment to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
					}
				}
			} catch (Exception ex) {
				logger.error("Error opening appointment search for edit", (Throwable) ex);
				JOptionPane.showMessageDialog(this, "Error editing appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		// Add the buttons to header panel
		JPanel headerPanel = (JPanel) getComponent(0); // Get the header panel
		if (headerPanel != null) {
			headerPanel.add(createSessionButton, "cell 2 0, split 2");
			headerPanel.add(editAppointmentButton, "cell 2 0");
		}
	}

	/**
	 * Creates a session from the currently selected appointment
	 */
	private void createSessionFromAppointment() {
		try {
			// Create an appointment search panel
			AppointmentSearchPanel searchPanel = new AppointmentSearchPanel(connection);
			
			// Show the search panel in a dialog
			int option = JOptionPane.showConfirmDialog(
				this,
				searchPanel,
				"Select an Appointment",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
			);
			
			// If user cancelled, return
			if (option != JOptionPane.OK_OPTION) {
				return;
			}
			
			// Get the selected appointment ID from the search panel
			Integer appointmentId = searchPanel.getSelectedAppointmentId();
			
			// Validate selection
			if (appointmentId == null) {
				JOptionPane.showMessageDialog(this, 
					"Please select an appointment from the list.",
					"No Selection", 
					JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			// Get the selected appointment details
			Appointment selectedAppointment = appointmentDAO.getAppointmentById(appointmentId);
			
			if (selectedAppointment == null) {
				JOptionPane.showMessageDialog(this, 
					"Invalid appointment selection.", 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// Use openSessionForm to handle the session creation
			openSessionForm(selectedAppointment);
			
		} catch (Exception ex) {
			logger.error("Error creating session from appointment", (Throwable) ex);
			JOptionPane.showMessageDialog(this,
				"Error creating session: " + ex.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Opens an existing session with the given ID
	 */
	private void openExistingSession(int sessionId) {
		try {
			SessionsFillUpFormPanel SessionsFillUpFormPanel = new SessionsFillUpFormPanel(connection);
			
			// Set the session ID for editing
			try {
				SessionsFillUpFormPanel.getClass().getMethod("setSessionToEdit", Integer.class)
					.invoke(SessionsFillUpFormPanel, sessionId);
			} catch (Exception ex) {
				logger.warn("Could not set session ID: " + ex.getMessage());
			}
			
			SwingUtilities.invokeLater(() -> {
				FormManager.showForm(SessionsFillUpFormPanel);
			});
		} catch (Exception ex) {
			logger.error("Error opening existing session", (Throwable) ex);
			JOptionPane.showMessageDialog(this,
				"Error opening session: " + ex.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleAppointmentCreated(Object appointmentId) {
		if (appointmentId instanceof Integer) {
			// AlarmManagement will now automatically discover new appointments from the DB
			// and schedule alarms if they are in the future.
			// No direct alarm creation needed here.
			refreshViews(); // Only refresh the view
		}
	}

	public void initializeAlarmSystem() {
		try {
			// Create alarm management instance with callback
			this.alarmManagement = AlarmManagement.createInstance(new AlarmManagement.AlarmCallback() {
				@Override
				public void onAlarmTriggered(Appointment appointment) {
					// This will be called when an appointment is due
					// Use the centralized showAlarmDialog method in AlarmManagement
					AlarmManagement.getInstance().showAlarmDialog(appointment);

					// You might want to refresh the calendar view if an appointment is due
					// to reflect potential status changes (e.g., missed)
					refreshViews();
				}

				@Override
				public void onAlarmScheduled(LocalDateTime dateTime) {
					// Optional: Log or handle scheduled alarm
				}

				@Override
				public void onAlarmStopped() {
					// Optional: Handle alarm stop
				}

				@Override
				public void onAlarmSnoozed(LocalDateTime newDateTime) {
					logger.info("Alarm snoozed until: " + newDateTime);
				}
			}, this); // Pass 'this' (AppointmentManagement instance) here

			// Start checking for appointments when counselor logs in
			alarmManagement.start();
		} catch (Exception e) {
			logger.error("Error initializing appointment reminder system", (Throwable) e);
			JOptionPane.showMessageDialog(this,
				"Error initializing appointment reminder system: " + e.getMessage(),
				"Initialization Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Edits an existing appointment.
	 * @param appointmentId The ID of the appointment to edit.
	 */
	private void editSelectedAppointment(Integer appointmentId) {
		try {
			Appointment selectedAppointment = appointmentDAO.getAppointmentById(appointmentId);
			if (selectedAppointment != null) {
				AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(selectedAppointment, appointmentDAO, connection);
				AddAppointmentModal.getInstance().showModal(connection, this, addAppointmentPanel, appointmentDAO, 700, 650, () -> {
					refreshViews(); // Refresh views after editing
				});
			} else {
				JOptionPane.showMessageDialog(this, "Selected appointment not found.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException e) {
			logger.error("Error retrieving appointment details for edit", (Throwable) e);
			JOptionPane.showMessageDialog(this, "Error loading appointment details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}