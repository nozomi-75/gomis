package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class AppointmentMangement extends Form {
	private LocalDate currentDate;
	private JPanel calendarPanel;
	private JLabel monthYearLabel;
	private AppointmentDAO appointmentDAO;
	private Connection connection;

	public AppointmentMangement(Connection connection) {
		this.connection = connection;
		this.appointmentDAO = new AppointmentDAO();
		currentDate = LocalDate.now();

		FlatLightLaf.setup();

		// Main panel layout - fill the entire space
		setLayout(new MigLayout("fill, insets 0", "[grow, fill]", "[][][grow, fill]"));

		// Header panel
		JPanel headerPanel = new JPanel(new MigLayout("fill", "[100px][grow][100px]", "[]"));
		headerPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		add(headerPanel, "cell 0 0, growx");

		JButton addAppointBtn = new JButton("Add");
		addAppointBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		addAppointBtn.addActionListener(e -> createAppointment());
		headerPanel.add(addAppointBtn, "flowx,cell 0 0");

		JLabel titleLabel = new JLabel("Appointments");
		headerPanel.add(titleLabel, "cell 1 0, alignx center");
		
		JButton dayBtn = new JButton("Day");
		headerPanel.add(dayBtn, "flowx,cell 2 0");
		
		JButton weekBtn = new JButton("Week");
		headerPanel.add(weekBtn, "cell 2 0");
		
		JButton monthBtn = new JButton("Month");
		headerPanel.add(monthBtn, "cell 2 0");

		// Navigation panel
		add(createNavigationPanel(), "cell 0 1, growx");

		// Calendar panel with proper constraints
		calendarPanel = createCalendarPanel();
		add(calendarPanel, "cell 0 2, grow");
	}

	private JPanel createNavigationPanel() {
		JPanel navigationPanel = new JPanel(new MigLayout("", "[grow][grow][grow]"));
		JButton prevButton = new JButton("<");
		prevButton.addActionListener(e -> changeMonth(-1));
		JButton nextButton = new JButton(">");
		nextButton.addActionListener(e -> changeMonth(1));
		monthYearLabel = new JLabel();
		updateMonthYearLabel();
		navigationPanel.add(prevButton, "cell 0 0, align left");
		navigationPanel.add(monthYearLabel, "cell 1 0, align center");
		navigationPanel.add(nextButton, "cell 2 0, align right");
		return navigationPanel;
	}

	private JPanel createCalendarPanel() {
		// Create panel with equal spacing for all cells that grow proportionally
		JPanel panel = new JPanel(new MigLayout("wrap 7, fill, insets 5",
				"[grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill][grow, fill]",
				"[]5[grow, fill]5[grow, fill]5[grow, fill]5[grow, fill]5[grow, fill]"));

		// Add day headers
		String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
		for (String day : dayNames) {
			JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
			panel.add(dayLabel, "alignx center");
		}

		populateCalendarDays(panel);
		return panel;
	}

	private void populateCalendarDays(JPanel panel) {
		YearMonth yearMonth = YearMonth.from(currentDate);
		LocalDate firstOfMonth = yearMonth.atDay(1);
		int daysInMonth = yearMonth.lengthOfMonth();
		int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

		// Add empty cells for days before the first of the month
		for (int i = 0; i < startDayOfWeek; i++) {
			panel.add(new JPanel(), "grow"); // Empty panel that will maintain square shape
		}

		// Add day panels
		for (int day = 1; day <= daysInMonth; day++) {
			LocalDate currentDay = firstOfMonth.plusDays(day - 1);
			JPanel dayPanel = createDayPanel(currentDay);
			panel.add(dayPanel, "grow");
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

		dayPanel.putClientProperty(FlatClientProperties.STYLE, "arc: 8");
		dayPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		dayPanel.setBackground(new Color(230, 240, 250));

		// Add date number
		JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
		dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		dayPanel.add(dayLabel, "align right");

		// Add appointments if any
		List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(connection, date);
		if (!appointments.isEmpty()) {
			JPanel appointmentsContainer = new JPanel(new MigLayout("wrap 1, insets 0, gap 2", "[grow,fill]", // Full
																												// width
					appointments.size() == 1 ? "[30!]" : "[grow]")); // Adjust gap between appointments
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

				String timeStr = formatAppointmentTime(appt.getAppointmentDateTime());

				JLabel appLabel = new JLabel() {
					@Override
					public void setText(String text) {
						Container parent = getParent();
						if (parent != null) {
							Container calendarCell = parent.getParent().getParent();
							if (appointments.size() == 1) {
								setFont(new Font("Arial", Font.BOLD, 14));
								setHorizontalAlignment(SwingConstants.CENTER);
								super.setText(timeStr);
							} else {
								// Larger font for multiple appointments
								setFont(new Font("Arial", Font.BOLD, 12));
								setHorizontalAlignment(SwingConstants.LEFT);
								if (calendarCell.getWidth() > 120) {
									super.setText(timeStr + " - " + appt.getAppointmentType());
								} else {
									super.setText(timeStr);
								}
							}
						}
					}
				};
				appLabel.setForeground(Color.BLACK);

				appointmentPanel.add(appLabel, "grow");

				// Adjust height based on number of appointments
				String constraints;
				if (appointments.size() == 1) {
					constraints = "grow, height 30!";
				} else if (appointments.size() == 2) {
					constraints = "grow, height 25!";
				} else {
					constraints = "grow, height 20!"; // Slightly smaller for 3+ appointments
				}
				appointmentsContainer.add(appointmentPanel, constraints);
			}

			// Add resize listener to the day panel
			dayPanel.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					for (Component c : appointmentsContainer.getComponents()) {
						if (c instanceof JPanel) {
							for (Component label : ((JPanel) c).getComponents()) {
								if (label instanceof JLabel) {
									((JLabel) label).setText(((JLabel) label).getText());
								}
							}
						}
					}
				}
			});
		}

		// Add hover effects
		dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				if (dayPanel.getBackground().equals(new Color(230, 240, 250))) {
					dayPanel.setBackground(new Color(220, 230, 240));
				}
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				if (dayPanel.getBackground().equals(new Color(220, 230, 240))) {
					dayPanel.setBackground(new Color(230, 240, 250));
				}
			}

			public void mouseClicked(java.awt.event.MouseEvent evt) {
				showDayAppointments(date);
			}
		});

		return dayPanel;
	}

	private String formatAppointmentTime(LocalDateTime dateTime) {
		return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("h:mm a"));
	}

	private void showDayAppointments(LocalDate date) {
		List<Appointment> dayAppointments = appointmentDAO.getAppointmentsForDate(connection, date);

		if (dayAppointments.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No appointments on " + date, "Appointments",
					JOptionPane.INFORMATION_MESSAGE);
			return;
			
			
		}
		StringBuilder message = new StringBuilder("Appointments on " + date + "\n");
		for (Appointment appt : dayAppointments) {
			message.append(appt.getAppointmentDateTime()).append(" - ").append(appt.getStudentUid()).append(" (")
					.append(appt.getAppointmentType()).append(")\n");
		}
		JOptionPane.showMessageDialog(this, message.toString(), "Appointments", JOptionPane.PLAIN_MESSAGE);
	}

	private void changeMonth(int delta) {
		currentDate = currentDate.plusMonths(delta);
		updateMonthYearLabel();

		// Remove old calendar and add new one
		remove(calendarPanel);
		calendarPanel = createCalendarPanel();
		add(calendarPanel, "cell 0 2, grow");

		revalidate();
		repaint();
	}

	private void updateMonthYearLabel() {
		String monthName = currentDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault());
		monthYearLabel.setText(monthName + " " + currentDate.getYear());
	}

	private void createAppointment() {
		AppointmentDialog dialog = new AppointmentDialog(Main.jFrame);
		dialog.setVisible(true);

		if (dialog.isConfirmed()) {
			try {
				Integer studentUid = dialog.getStudentUid();
				// Temporarily using counselor ID 1 for testing - you should implement proper
				// user authentication
				int counselorId = 1; // TODO: Replace with actual getCurrentLoggedInCounselorId()
				String appointmentType = dialog.getAppointmentType();
				LocalDateTime appointmentDateTime = dialog.getAppointmentDateTime();
				String appointmentStatus = "Scheduled";

				AppointmentDAO appointmentCRUD = new AppointmentDAO();
				appointmentCRUD.addAppointment(connection, studentUid, counselorId, appointmentType,
						java.sql.Timestamp.valueOf(appointmentDateTime), appointmentStatus);

				JOptionPane.showMessageDialog(this, "Appointment scheduled successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);

				// Refresh calendar
				remove(calendarPanel);
				calendarPanel = createCalendarPanel();
				add(calendarPanel, "cell 0 2, grow");

				revalidate();
				repaint();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error creating appointment: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
