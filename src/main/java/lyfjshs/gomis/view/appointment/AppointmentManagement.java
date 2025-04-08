package lyfjshs.gomis.view.appointment;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.notification.NotificationManager;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.components.settings.SettingsState;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.sessions.SessionsForm;
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
	private Timer notificationTimer;
	private JButton actionButton;
	private static final long NOTIFICATION_CHECK_INTERVAL = 30000; // Check every 30 seconds
	private static final long NOTIFICATION_THRESHOLD = 10; // Notify 10 minutes before appointment
	private SettingsManager settingsManager;
	private static final Logger logger = Logger.getLogger(AppointmentManagement.class.getName());
	private ScheduledExecutorService scheduler;
	private final Map<Integer, AppointmentAlarm> activeAlarms;
	private static AppointmentAlarm currentActiveAlarm; // Track the currently active alarm

	public AppointmentManagement(Connection connection) {
		this.connection = connection;
		this.appointmentDAO = new AppointmentDAO(connection);
		this.settingsManager = Main.settings;
		this.activeAlarms = new HashMap<>();
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
		appointmentCalendar = new AppointmentCalendar(appointmentDAO, connection);
		cancelledAppointmentsPanel = createCancelledAppointmentsPanel();

		// Start with month view
		slidePane.addSlide(appointmentCalendar, SlidePaneTransition.Type.FORWARD);

		// Initialize the toolbar with buttons for managing appointments
		initializeToolbar();
		
		// Subscribe to appointment events
		EventBus.subscribe(this, "appointment_created", this::handleAppointmentCreated);
		EventBus.subscribe(this, "appointment_updated", this::handleAppointmentUpdated);
		EventBus.subscribe(this, "appointment_deleted", this::handleAppointmentDeleted);
		EventBus.subscribe(this, "counselor_logged_in", data -> {
			startNotificationTimer();
			startStatusUpdateScheduler();
		});
		
		// Initialize alarms for existing appointments
		initializeExistingAppointments();
	}

	private void startNotificationTimer() {
		// Skip if no counselor is logged in
		if (Main.formManager == null || Main.formManager.getCounselorObject() == null) {
			logger.info("No counselor logged in. Skipping notification timer start.");
			return;
		}
		
		// Skip if timer is already running
		if (notificationTimer != null) {
			logger.info("Notification timer already running. Skipping start.");
			return;
		}
		
		// Start a new timer
		notificationTimer = new Timer("AppointmentNotifications", true); // Run as daemon thread
		notificationTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkUpcomingAppointments();
				checkForMissedAppointments();
			}
		}, 0, NOTIFICATION_CHECK_INTERVAL);
		
		logger.info("Notification timer started successfully");
	}

	private void checkForMissedAppointments() {
		// Check if a counselor is logged in first
		if (Main.formManager == null || Main.formManager.getCounselorObject() == null) {
			logger.info("No counselor logged in. Skipping missed appointment check.");
			return;
		}
		
		try {
			// First check for appointments in grace period
			List<Appointment> graceAppointments = appointmentDAO.getAppointmentsInGracePeriod();
			
			// Show urgent reminders for appointments in grace period
			for (Appointment appointment : graceAppointments) {
				// Only show notification if this appointment hasn't been notified yet
				Integer appointmentId = appointment.getAppointmentId();
				if (!NotificationManager.isAppointmentNotified(appointmentId)) {
					String title = "Appointment About to be Marked as Missed";
					String message = String.format(
						"Appointment '%s' is past its scheduled time. Create a session now to prevent it from being marked as missed.",
						appointment.getAppointmentTitle()
					);
					
					// Show dialog with option to start session
					int response = JOptionPane.showConfirmDialog(
						this,
						message + "\n\nWould you like to start a session for this appointment now?",
						title,
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE
					);
					
					if (response == JOptionPane.YES_OPTION) {
						openSessionForm(appointment);
					} else {
						// If they said no, show a notification
						NotificationManager notificationManager = Main.gFrame.getNotificationManager();
						if (notificationManager != null) {
							notificationManager.showWarningNotification(title, message);
						}
					}
					
					// Mark as notified to prevent spam
					NotificationManager.markAppointmentAsNotified(appointmentId);
				}
			}
			
			// Then check and update missed appointments (those beyond grace period)
			int updatedCount = appointmentDAO.checkAndUpdateMissedWithoutSessions();
			
			// Refresh the UI if any appointments were updated
			if (updatedCount > 0) {
				logger.info("Marked " + updatedCount + " missed appointments");
				// Refresh the appointment table
				refreshViews();
			}
		} catch (SQLException ex) {
			logger.log(Level.SEVERE, "Error checking for missed appointments: " + ex.getMessage(), ex);
		}
	}

	private void showReminderDialog(Appointment appointment) {
		String message = "Reminder: " + appointment.getAppointmentTitle() + " is starting soon!";
		playReminderSound();
		JOptionPane.showMessageDialog(this, message, "Appointment Reminder", JOptionPane.INFORMATION_MESSAGE);

	}

	private void playReminderSound() {
		try {
			File soundFile = new File(getClass().getClassLoader().getResource("sounds/reminder.wav").toURI());
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkUpcomingAppointments() {
		// Check if a counselor is logged in first
		if (Main.formManager == null || Main.formManager.getCounselorObject() == null) {
			logger.info("No counselor logged in. Skipping upcoming appointment check.");
			return;
		}
		
		try {
			List<Appointment> appointments = appointmentDAO.getUpcomingAppointments();
			LocalDateTime now = LocalDateTime.now();
			SettingsState state = SettingsManager.getCurrentState();
			boolean notificationsEnabled = state != null && state.notifications;
			if (!notificationsEnabled) {
				return;
			}
			int notificationMinutes = state.notificationTimeMinutes;
			boolean notifyOnStart = state.notifyOnStart;
			
			List<Appointment> todayAppointments = appointments.stream()
				.filter(appointment -> {
					LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
					return appointmentTime.toLocalDate().equals(now.toLocalDate()) &&
						   ("Scheduled".equals(appointment.getAppointmentStatus()) ||
							"In Progress".equals(appointment.getAppointmentStatus()) ||
							"Rescheduled".equals(appointment.getAppointmentStatus()));
				})
				.toList();
				
			for (Appointment appointment : todayAppointments) {
				LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
				long minutesUntilAppointment = ChronoUnit.MINUTES.between(now, appointmentTime);
				
				// Only show notification if this appointment hasn't been notified yet
				Integer appointmentId = appointment.getAppointmentId();
				if (!NotificationManager.isAppointmentNotified(appointmentId)) {
					// Show reminders in specific cases:
					// 1. If appointment is within the notification window
					// 2. If appointment is starting right now (0-1 minutes) and notification on start is enabled
					boolean shouldShowReminder = 
						(minutesUntilAppointment > 0 && minutesUntilAppointment <= notificationMinutes) ||
						(minutesUntilAppointment >= 0 && minutesUntilAppointment < 1 && notifyOnStart);
						
					if (shouldShowReminder) {
						// Show appropriate notification based on time to appointment
						if (minutesUntilAppointment < 5) {
							// For imminent appointments (less than 5 minutes away)
							String title = "⏰ Urgent Appointment Reminder";
							String message = String.format(
								"Appointment '%s' is starting %s. Click here to start a session.",
								appointment.getAppointmentTitle(),
								minutesUntilAppointment <= 0 ? "now" : "in " + minutesUntilAppointment + " minutes"
							);
							
							// Show dialog with option to start session
							int response = JOptionPane.showConfirmDialog(
								this,
								message + "\n\nWould you like to start a session for this appointment now?",
								title,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE
							);
							
							if (response == JOptionPane.YES_OPTION) {
								openSessionForm(appointment);
							} else {
								// If they said no, still show a notification
								NotificationManager notificationManager = Main.gFrame.getNotificationManager();
								if (notificationManager != null) {
									notificationManager.showWarningNotification(title, message);
								}
							}
						} else {
							// For appointments further away
							String title = "🔔 Appointment Reminder";
							String message = String.format(
								"Appointment '%s' is scheduled in %d minutes",
								appointment.getAppointmentTitle(),
								minutesUntilAppointment
							);
							
							// Use the notification manager
							NotificationManager notificationManager = Main.gFrame.getNotificationManager();
							if (notificationManager != null) {
								notificationManager.showInfoNotification(title, message);
							} else {
								// Fallback to dialog
						showReminderDialog(appointment);
							}
						}
						
						// Play sound for better notification
						playReminderSound();
						
						// Mark as notified in NotificationManager
						NotificationManager.markAppointmentAsNotified(appointmentId);
						
						// Also update the appointment's reminderShown flag
						appointment.setReminderShown(true);
						appointmentDAO.updateAppointment(appointment);
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "Error checking upcoming appointments: " + e.getMessage(), e);
		}
	}

	/**
	 * Opens a session form pre-filled with the specified appointment data
	 */
	private void openSessionForm(Appointment appointment) {
		try {
			DrawerBuilder.switchToSessionsForm();
			Form[] forms = FormManager.getForms();
			for (Form form : forms) {
				if (form instanceof SessionsForm) {
					SessionsForm sessionsForm = (SessionsForm) form;
					sessionsForm.populateFromAppointment(appointment);
					
					// Update appointment status to "In Progress"
					appointment.setAppointmentStatus("In Progress");
					appointmentDAO.updateAppointment(appointment);
					
					// Refresh views to show updated status
					refreshViews();
					break;
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error opening session form", e);
			JOptionPane.showMessageDialog(this,
				"Error opening session form: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void toggleView() {
		if (slidePane.getSlideComponent() instanceof AppointmentCalendar) {
			// Switch to cancelled appointments view
			slidePane.addSlide(cancelledAppointmentsPanel, SlidePaneTransition.Type.FORWARD);
			actionButton.setText("Back");
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
		// Update calendar view
		if (appointmentCalendar != null) {
			appointmentCalendar.updateCalendar();
		}
		
		// Update history view if it exists
		if (appointmentHistory != null) {
			appointmentHistory.updateAppointmentsDisplay();
		}
		
		// Check for any cancelled appointments panel that might need updating
		if (cancelledAppointmentsPanel != null && cancelledAppointmentsPanel.isVisible()) {
		    // This will update any components in the cancelled appointments panel
		    for (java.awt.Component comp : cancelledAppointmentsPanel.getComponents()) {
		        if (comp instanceof AppointmentHistory) {
		            ((AppointmentHistory) comp).updateAppointmentsDisplay();
		        }
		    }
		}
		
		// Force a repaint of the entire panel and all its components
		invalidate();
		revalidate();
		repaint();
		
		// Update current slide in slidePane
		if (slidePane != null) {
		    java.awt.Component currentComponent = slidePane.getSlideComponent();
		    if (currentComponent != null) {
		        currentComponent.revalidate();
		        currentComponent.repaint();
		    }
		}
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
					java.awt.EventQueue.invokeLater(this::refreshViews);
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "Error updating appointment statuses", e);
			}
		}, 0, 30, TimeUnit.MINUTES); // Check every 30 minutes
	}

	@Override
	public void dispose() {
		// Cancel timers
		if (notificationTimer != null) {
			notificationTimer.cancel();
			notificationTimer = null;
		}
		if (scheduler != null) {
			scheduler.shutdown();
			scheduler = null;
		}
		
		// Clean up alarms
		shutdown();
		
		// Unsubscribe from EventBus
		EventBus.unsubscribeAll(this);
		
		super.dispose();
	}

	public void onAppointmentUpdated(int appointmentId) {
		logger.info("Appointment updated with ID: " + appointmentId);
		
		// Refresh all views to reflect the updated appointment
		refreshViews();
		
		// Show confirmation message
		JOptionPane.showMessageDialog(
			this,
			"Appointment has been successfully updated.",
			"Update Successful",
			JOptionPane.INFORMATION_MESSAGE
		);
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
		
		// Add the button to header panel
		JPanel headerPanel = (JPanel) getComponent(0); // Get the header panel
		if (headerPanel != null) {
			headerPanel.add(createSessionButton, "cell 2 0, split 2");
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
			
			// Check if this appointment already has a session
			String query = "SELECT SESSION_ID FROM SESSIONS WHERE APPOINTMENT_ID = ?";
			PreparedStatement pst = connection.prepareStatement(query);
			pst.setInt(1, appointmentId);
			ResultSet rs = pst.executeQuery();
			
			if (rs.next()) {
				int sessionId = rs.getInt("SESSION_ID");
				rs.close();
				pst.close();
				
				// Appointment already has a session, ask if they want to open it
				int response = JOptionPane.showConfirmDialog(
					this,
					"This appointment already has a session. Would you like to open it?",
					"Session Exists",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE
				);
				
				if (response == JOptionPane.YES_OPTION) {
					// Open the existing session
					openExistingSession(sessionId);
				}
				return;
			}
			
			rs.close();
			pst.close();
			
			// If we reach here, no session exists yet, so create a new one
			SessionsForm sessionsForm = new SessionsForm(connection);
			
			// Get full appointment details with participants
			Appointment fullAppointment = appointmentDAO.getAppointmentById(appointmentId);
			
			// Populate the form with the appointment
			sessionsForm.populateFromAppointment(fullAppointment);
			
			FormManager.showForm(sessionsForm);
			
			// Update the appointment status to "In Progress"
			String updateQuery = "UPDATE APPOINTMENTS SET APPOINTMENT_STATUS = 'In Progress' WHERE APPOINTMENT_ID = ?";
			PreparedStatement updatePst = connection.prepareStatement(updateQuery);
			updatePst.setInt(1, appointmentId);
			updatePst.executeUpdate();
			updatePst.close();
			
			// Refresh the views
			refreshViews();
			
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error creating session from appointment: " + ex.getMessage(), ex);
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
			SessionsForm sessionsForm = new SessionsForm(connection);
			
			// Set the session ID for editing
			try {
				sessionsForm.getClass().getMethod("setSessionToEdit", Integer.class)
					.invoke(sessionsForm, sessionId);
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Could not set session ID: " + ex.getMessage());
			}
			
			FormManager.showForm(sessionsForm);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Error opening existing session: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this,
				"Error opening session: " + ex.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initializeExistingAppointments() {
		try {
			// Cancel any existing alarms first
			shutdown();
			
			List<Appointment> upcomingAppointments = appointmentDAO.getUpcomingAppointments();
			// Sort appointments by date/time to ensure we handle them in order
			upcomingAppointments.sort((a1, a2) -> 
				a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime()));
			
			// Only set alarm for the next upcoming appointment
			if (!upcomingAppointments.isEmpty()) {
				Appointment nextAppointment = upcomingAppointments.get(0);
				createAlarmForAppointment(nextAppointment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void handleAppointmentCreated(Object appointmentId) {
		try {
			int id = (Integer) appointmentId;
			Appointment appointment = appointmentDAO.getAppointmentById(id);
			if (appointment != null) {
				// Check if this should be the next active alarm
				if (currentActiveAlarm == null || 
					appointment.getAppointmentDateTime().before(
						currentActiveAlarm.getAppointment().getAppointmentDateTime())) {
					// Cancel current alarm if it exists
					if (currentActiveAlarm != null) {
						currentActiveAlarm.stop();
					}
					createAlarmForAppointment(appointment);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void handleAppointmentUpdated(Object appointmentId) {
		try {
			int id = (Integer) appointmentId;
			// Remove existing alarm if any
			removeAlarmForAppointment(id);
			
			// Create new alarm with updated appointment data
			Appointment appointment = appointmentDAO.getAppointmentById(id);
			if (appointment != null) {
				// Only create new alarm if this is the next upcoming appointment
				if (currentActiveAlarm == null || 
					appointment.getAppointmentDateTime().before(
						currentActiveAlarm.getAppointment().getAppointmentDateTime())) {
					createAlarmForAppointment(appointment);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void handleAppointmentDeleted(Object appointmentId) {
		int id = (Integer) appointmentId;
		removeAlarmForAppointment(id);
		
		// Find and set the next upcoming appointment alarm
		try {
			List<Appointment> upcomingAppointments = appointmentDAO.getUpcomingAppointments();
			if (!upcomingAppointments.isEmpty()) {
				// Sort by date/time
				upcomingAppointments.sort((a1, a2) -> 
					a1.getAppointmentDateTime().compareTo(a2.getAppointmentDateTime()));
				createAlarmForAppointment(upcomingAppointments.get(0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void createAlarmForAppointment(Appointment appointment) {
		// Only create alarm for future appointments
		if (appointment.getAppointmentDateTime().toLocalDateTime().isAfter(LocalDateTime.now())) {
			// Cancel any existing alarm first
			if (currentActiveAlarm != null) {
				currentActiveAlarm.stop();
				currentActiveAlarm = null;
			}

			AppointmentAlarm alarm = new AppointmentAlarm(appointment, () -> {
				try {
					// Switch to sessions form
					DrawerBuilder.switchToSessionsForm();
					Form[] forms = FormManager.getForms();
					for (Form form : forms) {
						if (form instanceof SessionsForm) {
							SessionsForm sessionsForm = (SessionsForm) form;
							sessionsForm.populateFromAppointment(appointment);
							break;
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Error creating session from alarm", e);
				}
			});
			
			activeAlarms.put(appointment.getAppointmentId(), alarm);
			currentActiveAlarm = alarm;
			logger.info("Created alarm for appointment: " + appointment.getAppointmentId());
		}
	}

	private void removeAlarmForAppointment(int appointmentId) {
		AppointmentAlarm alarm = activeAlarms.remove(appointmentId);
		if (alarm != null) {
			alarm.stop();
			if (currentActiveAlarm == alarm) {
				currentActiveAlarm = null;
			}
		}
	}

	public void shutdown() {
		// Clean up all active alarms
		for (AppointmentAlarm alarm : activeAlarms.values()) {
			alarm.stop();
		}
		activeAlarms.clear();
		currentActiveAlarm = null;
	}
}