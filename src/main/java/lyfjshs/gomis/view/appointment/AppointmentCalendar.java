package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;
import net.miginfocom.swing.MigLayout;

public class AppointmentCalendar extends JPanel {
	private AppointmentDAO appointmentDao;
	private Connection connection;
	private LocalDate currentDate;
	
	public AppointmentCalendar(AppointmentDAO appointDAO, Connection conn) {
		this.appointmentDao = appointDAO;
		this.connection = conn;
		currentDate = LocalDate.now();
		setLayout(new MigLayout("wrap 7, fill, insets 5",
				"[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
				"[]5[grow, fill]"));

		// Add day headers
		String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		for (String day : dayNames) {
			JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
			add(dayLabel, "alignx center");
		}

		populateCalendarDays();
	}

	private void populateCalendarDays() {
		YearMonth yearMonth = YearMonth.from(currentDate);
		LocalDate firstOfMonth = yearMonth.atDay(1);
		int daysInMonth = yearMonth.lengthOfMonth();
		int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

		// Add empty cells for days before the first of the month
		for (int i = 0; i < startDayOfWeek; i++) {
			add(new JPanel(), "grow"); // Empty panel that will maintain square shape
		}

		// Add day panels
		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate currentDay = firstOfMonth.plusDays(day - 1);
			JPanel dayPanel = createDayPanel(currentDay);
			add(dayPanel, "grow");
		}
	}

	private JPanel createDayPanel(LocalDate date) {
		JPanel dayPanel = new JPanel(new MigLayout("wrap 1, insets 2", "[grow,fill]", "[][grow]")) {
			@Override
			public Dimension getPreferredSize() {
				Container parent = getParent();
				if (parent != null) {
					int parentWidth = parent.getWidth();
					int baseSize = (parentWidth - 30) / 7;
					return new Dimension(parentWidth / 7, baseSize);
				}
				return new Dimension(100, 100);
			}
		};

		dayPanel.putClientProperty("arc", "8");
		dayPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		dayPanel.setBackground(new Color(230, 240, 250));

		// Add date number
		JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
		dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		dayPanel.add(dayLabel, "align right");

		// Add appointments if any
		List<Appointment> appointments = appointmentDao.getAppointmentsForDate(connection, date);
		if (!appointments.isEmpty()) {
			JPanel appointmentsContainer = new JPanel(new MigLayout("wrap 1, insets 0, gap 2", "[grow,fill]"));
			appointmentsContainer.setOpaque(false);
			dayPanel.add(appointmentsContainer, "grow");

			Map<String, Color> appointmentColors = new HashMap<>();
			appointmentColors.put("Career", new Color(46, 204, 113));
			appointmentColors.put("Counseling", new Color(255, 118, 117));
			appointmentColors.put("Personal Counseling", new Color(241, 196, 15));
			appointmentColors.put("Group Counseling", new Color(181, 255, 140));

			for (Appointment appt : appointments) {
				JPanel appointmentPanel = new JPanel(new MigLayout("fill, insets 2", "[grow]"));
				appointmentPanel.setBackground(appointmentColors.getOrDefault(appt.getAppointmentType(), Color.GRAY));

				JLabel appLabel = new JLabel(appt.getAppointmentType() + " - " + appt.getAppointmentDateTime());
				appLabel.setForeground(Color.BLACK);
				appointmentPanel.add(appLabel, "grow");

				appointmentsContainer.add(appointmentPanel, "grow");
			}
		}

		return dayPanel;
	}

	private String formatAppointmentTime(LocalDateTime dateTime) {
		return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"));
	}
}
