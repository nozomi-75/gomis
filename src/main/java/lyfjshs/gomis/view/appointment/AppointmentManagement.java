package lyfjshs.gomis.view.appointment;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.notification.NotificationManager;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;

public class AppointmentManagement extends Form {
	private Connection connection;
	private SlidePane slidePane;
	private AppointmentDailyOverview appointmentDaily;
	private AppointmentCalendar appointmentCalendar;
	public AppointmentDAO appointmentDAO;
	private Timer notificationTimer;
	private static final long NOTIFICATION_CHECK_INTERVAL = 30000; // Check every 30 seconds
	private static final long NOTIFICATION_THRESHOLD = 10; // Notify 10 minutes before appointment

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

		JPanel viewButtons = new JPanel(new MigLayout("nogrid, gap 5"));
		JButton dayBtn = new JButton("Day");
		dayBtn.addActionListener(e -> switchToDayView());
		viewButtons.add(dayBtn);

		JButton monthBtn = new JButton("Month");
		monthBtn.addActionListener(e -> switchToMonthView());
		viewButtons.add(monthBtn);
		headerPanel.add(viewButtons, "cell 2 0, alignx right");

		// Initialize SlidePane with content area
		slidePane = new SlidePane();
		slidePane.setOpaque(true);

		// Wrap SlidePane in JScrollPane
		JScrollPane scrollPane = new JScrollPane(slidePane);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, "grow");

		// Start with month view
		appointmentCalendar = new AppointmentCalendar(appointmentDAO, connection);
		slidePane.addSlide(appointmentCalendar, SlidePaneTransition.Type.FORWARD);

		// Start notification timer
		startNotificationTimer();
	}

	private void startNotificationTimer() {
		if (notificationTimer != null) {
			notificationTimer.cancel();
		}
		notificationTimer = new Timer(true); // Run as daemon thread
		notificationTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				checkUpcomingAppointments();
			}
		}, 0, NOTIFICATION_CHECK_INTERVAL);
	}

	private void checkUpcomingAppointments() {
		try {
			// Get all upcoming appointments
			List<Appointment> appointments = appointmentDAO.getUpcomingAppointments();
			LocalDateTime now = LocalDateTime.now();

			// Filter for today's appointments only
			List<Appointment> todayAppointments = appointments.stream()
				.filter(appointment -> {
					LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
					return appointmentTime.toLocalDate().equals(now.toLocalDate()) &&
						   !appointment.getAppointmentStatus().equals("Ended");
				})
				.toList();

			for (Appointment appointment : todayAppointments) {
				LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
				long minutesUntilAppointment = ChronoUnit.MINUTES.between(now, appointmentTime);

				// Check if appointment is within notification threshold (10 minutes)
				if (minutesUntilAppointment > 0 && minutesUntilAppointment <= 10) {
					// Show notification only if it's close to 10 minutes (between 9.5 and 10.5 minutes)
					// This prevents multiple notifications for the same appointment
					if (minutesUntilAppointment >= 9.5 && minutesUntilAppointment <= 10.5) {
						NotificationManager notificationManager = Main.gFrame.getNotificationManager();
						if (notificationManager != null) {
							String timeStr = appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
							String title = "⚠️ Appointment Starting Soon";
							String message = String.format("%s starting at %s (in 10 minutes)", 
								appointment.getAppointmentTitle(), timeStr);
							notificationManager.showWarningNotification(title, message);
						}
					}
				}

				// Check if appointment is exactly at current time
				if (minutesUntilAppointment == 0) {
					NotificationManager notificationManager = Main.gFrame.getNotificationManager();
					if (notificationManager != null) {
						String timeStr = appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
						String title = "🔔 Appointment Starting Now";
						String message = String.format("%s is starting now at %s", 
							appointment.getAppointmentTitle(), timeStr);
						notificationManager.showWarningNotification(title, message);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void switchToMonthView() {
		if (!(slidePane.getSlideComponent() instanceof AppointmentCalendar)) {
			slidePane.addSlide(appointmentCalendar, SlidePaneTransition.Type.BACK, 300);
		}
	}

	private void switchToDayView() {
		if (!(slidePane.getSlideComponent() instanceof AppointmentDailyOverview)) {
			appointmentDaily = new AppointmentDailyOverview(appointmentDAO, connection);
			slidePane.addSlide(appointmentDaily, SlidePaneTransition.Type.FORWARD, 300);
		}
	}

	private void createAppointment() {
		Appointment newAppointment = new Appointment();
		newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now())); // Set default date and time

		// Set the guidanceCounselorId from FormManager
		newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());

		AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDAO, connection);

		// Use AddAppointmentModal to show the dialog
		AddAppointmentModal.getInstance().showModal(connection, this, addAppointmentPanel, appointmentDAO, 700, 650, () -> refreshViews());

		// Update both views after modal closes
		refreshViews();
	}

	public void refreshViews() {
		// Update calendar view
		if (appointmentCalendar != null) {
			appointmentCalendar.updateCalendar();
		}
		
		// Update daily view if it exists
		if (appointmentDaily != null) {
			appointmentDaily.updateAppointmentsDisplay();
		}
		
		// Force a repaint of the entire panel
		revalidate();
		repaint();
	}

	@Override
	public void dispose() {
		if (notificationTimer != null) {
			notificationTimer.cancel();
			notificationTimer = null;
		}
		super.dispose();
	}
}