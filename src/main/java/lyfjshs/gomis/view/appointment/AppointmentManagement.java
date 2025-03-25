package lyfjshs.gomis.view.appointment;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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

	private void reloadAppointments() {
		if (appointmentCalendar != null) {
			appointmentCalendar.updateCalendar();
		}
		if (appointmentDaily != null) {
			appointmentDaily.updateAppointmentsDisplay();
		}
	}

	private void createAppointment() {
		Appointment newAppointment = new Appointment();
		newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now())); // Set default date and time

		// Set the guidanceCounselorId from FormManager
		newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());

		AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDAO, connection);

		// Use AddAppointmentModal to show the dialog
		AddAppointmentModal.getInstance().showModal(this, addAppointmentPanel, appointmentDAO, 700,650);

		// Add refresh after modal closes
		reloadAppointments();
	}

	public void refreshViews() {
		reloadAppointments();
	}
}