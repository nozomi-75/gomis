package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
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
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.awt.Cursor;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JSeparator;
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
	private static final Map<String, JPanel> dayPanels = new HashMap<>();
	private JPanel weekViewPanel;
	private JPanel mainViewPanel;
	private JPanel dayViewPanel;
	private final Color PRIMARY_COLOR = new Color(99, 102, 241);
	private final Color LIGHT_PURPLE = new Color(237, 238, 252);
	private final Color TEXT_GRAY = new Color(107, 114, 128);

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
		dayBtn.addActionListener(e -> switchToDayView());
		headerPanel.add(dayBtn, "flowx,cell 2 0");
		
		JButton weekBtn = new JButton("Week");
		weekBtn.addActionListener(e -> switchToWeekView());
		headerPanel.add(weekBtn, "cell 2 0");
		
		JButton monthBtn = new JButton("Month");
		monthBtn.addActionListener(e -> switchToMonthView());
		headerPanel.add(monthBtn, "cell 2 0");

		// Navigation panel
		add(createNavigationPanel(), "cell 0 1, growx");

		// Replace the direct calendar panel add with mainViewPanel
		mainViewPanel = new JPanel(new BorderLayout());
		calendarPanel = createCalendarPanel();
		mainViewPanel.add(calendarPanel);
		add(mainViewPanel, "cell 0 2, grow");
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
				// Show options dialog
				String[] options = {"View Appointments", "Add Appointment"};
				int choice = JOptionPane.showOptionDialog(
					dayPanel,
					"What would you like to do?",
					date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
					JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,
					options,
					options[0]
				);

				if (choice == 0) {
					showDayAppointments(date);
				} else if (choice == 1) {
					AppointmentDialog dialog = new AppointmentDialog(Main.jFrame);
					dialog.setVisible(true);
					
					if (dialog.isConfirmed()) {
						try {
							Integer studentUid = dialog.getStudentUid();
							int counselorId = 1; // TODO: Replace with actual getCurrentLoggedInCounselorId()
							String appointmentType = dialog.getAppointmentType();
							LocalDateTime appointmentDateTime = dialog.getAppointmentDateTime();
							String appointmentStatus = "Scheduled";

							AppointmentDAO appointmentCRUD = new AppointmentDAO();
							appointmentCRUD.addAppointment(connection, studentUid, counselorId, appointmentType,
									java.sql.Timestamp.valueOf(appointmentDateTime), appointmentStatus);

							JOptionPane.showMessageDialog(AppointmentMangement.this, 
								"Appointment scheduled successfully!", "Success",
								JOptionPane.INFORMATION_MESSAGE);

							// Refresh calendar
							mainViewPanel.removeAll();
							calendarPanel = createCalendarPanel();
							mainViewPanel.add(calendarPanel);
							mainViewPanel.revalidate();
							mainViewPanel.repaint();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(AppointmentMangement.this, 
								"Error creating appointment: " + e.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
						}
					}
				}
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

		mainViewPanel.removeAll();
		calendarPanel = createCalendarPanel();
		mainViewPanel.add(calendarPanel);

		mainViewPanel.revalidate();
		mainViewPanel.repaint();
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
				mainViewPanel.removeAll();
				calendarPanel = createCalendarPanel();
				mainViewPanel.add(calendarPanel);
				
				revalidate();
				repaint();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error creating appointment: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void switchToWeekView() {
		mainViewPanel.removeAll();
		weekViewPanel = createWeekViewPanel();
		mainViewPanel.add(weekViewPanel);
		
		mainViewPanel.revalidate();
		mainViewPanel.repaint();
	}

	private void switchToMonthView() {
		mainViewPanel.removeAll();
		calendarPanel = createCalendarPanel();
		mainViewPanel.add(calendarPanel);
		
		mainViewPanel.revalidate();
		mainViewPanel.repaint();
	}

	private void switchToDayView() {
		mainViewPanel.removeAll();
		dayViewPanel = createDayViewPanel();
		mainViewPanel.add(dayViewPanel);
		
		mainViewPanel.revalidate();
		mainViewPanel.repaint();
	}

	private JPanel createWeekViewPanel() {
		// Main content panel
		JPanel mainContent = new JPanel(new MigLayout("wrap 1, fill", "[grow]", "[grow][]"));

		// Appointments overview section
		JPanel overviewPanel = new JPanel(new MigLayout("wrap 6, fill", "[grow][grow][grow][grow][grow][grow]", "[grow]"));
		overviewPanel.setBackground(Color.WHITE);
		overviewPanel.setBorder(BorderFactory.createTitledBorder("Appointments Overview"));

		// Get current date and calculate week
		LocalDate startOfWeek = currentDate.minusDays(currentDate.getDayOfWeek().getValue() - 1);

		String[] columns = new String[6];
		LocalDate[] dates = new LocalDate[6]; // Add this array to store the actual dates
		
		for (int i = 0; i < 6; i++) {
			LocalDate day = startOfWeek.plusDays(i);
			dates[i] = day; // Store the actual date
			columns[i] = day.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
		}

		for (int i = 0; i < columns.length; i++) {
			JPanel dayPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow]", ""));
			dayPanel.setBackground(new Color(245, 245, 245));
			dayPanel.setBorder(BorderFactory.createTitledBorder(columns[i]));

			// Use the stored date directly instead of parsing
			LocalDate date = dates[i];
			List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(connection, date);
			
			for (Appointment appointment : appointments) {
				String appointmentDetails = appointment.getAppointmentDateTime().toLocalTime()
						.format(DateTimeFormatter.ofPattern("HH:mm")) + 
						" - " + appointment.getAppointmentType();
				
				JButton appointmentButton = new JButton(appointmentDetails);
				appointmentButton.setBackground(new Color(220, 245, 220));
				appointmentButton.setFocusPainted(false);
				appointmentButton.addActionListener(event -> {
					showAppointmentDetails(appointment);
				});
				
				dayPanel.add(appointmentButton, "growx");
			}

			JScrollPane scrollPane = new JScrollPane(dayPanel);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			overviewPanel.add(scrollPane, "grow");

			dayPanels.put(columns[i], dayPanel);
		}

		mainContent.add(overviewPanel, "grow");
		return mainContent;
	}

	private void showAppointmentDetails(Appointment appointment) {
		String details = String.format("Time: %s\nStudent ID: %d\nType: %s\nStatus: %s",
			appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
			appointment.getStudentUid(),
			appointment.getAppointmentType(),
			appointment.getAppointmentStatus());
			
		JOptionPane.showMessageDialog(this,
			details,
			"Appointment Details",
			JOptionPane.INFORMATION_MESSAGE);
	}

	private JPanel createDayViewPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Top panel with date navigation
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(Color.WHITE);

		// Date navigation
		JPanel dateNavPanel = new JPanel(new 
		FlowLayout(FlowLayout.LEFT));
		dateNavPanel.setBackground(Color.WHITE);

		JButton prevDayBtn = new JButton("←");
		JButton nextDayBtn = new JButton("→");
		JLabel dateLabel = new JLabel(currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
		dateLabel.setFont(new Font("Inter", Font.BOLD, 18));
		dateLabel.setForeground(TEXT_GRAY);

		styleNavigationButton(prevDayBtn);
		styleNavigationButton(nextDayBtn);

		prevDayBtn.addActionListener(e -> {
			currentDate = currentDate.minusDays(1);
			dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
			updateDayViewAppointments();
		});

		nextDayBtn.addActionListener(e -> {
			currentDate = currentDate.plusDays(1);
			dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
			updateDayViewAppointments();
		});

		dateNavPanel.add(dateLabel);
		dateNavPanel.add(prevDayBtn);
		dateNavPanel.add(nextDayBtn);
		topPanel.add(dateNavPanel, BorderLayout.WEST);

		// Schedule panel
		JPanel schedulePanel = new JPanel(new MigLayout("insets 0", "[50][grow,fill]"));
		schedulePanel.setBackground(Color.WHITE);

		// Time slots panel
		JPanel timePanel = new JPanel(new MigLayout("wrap 1, insets 0", "[50]"));
		timePanel.setBackground(Color.WHITE);

		// Events panel
		JPanel eventsPanel = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]"));
		eventsPanel.setBackground(Color.WHITE);

		// Add time slots (9 AM to 5 PM)
		for (int hour = 9; hour <= 17; hour++) {
			// Time label
			String timeText = String.format("%d:00 %s", hour > 12 ? hour - 12 : hour, hour >= 12 ? "PM" : "AM");
			JPanel timeSlot = createTimeSlot(timeText);
			timePanel.add(timeSlot, "grow");

			// Event slot
			JPanel eventSlot = createEventSlot();
			eventsPanel.add(eventSlot, "grow");
		}

		schedulePanel.add(timePanel, "dock west");
		schedulePanel.add(eventsPanel, "grow");

		// Add components to main panel
		mainPanel.add(topPanel, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(schedulePanel);
		scrollPane.setBorder(null);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		// Update the dayViewPanel reference
		dayViewPanel = mainPanel;
		updateDayViewAppointments();

		return mainPanel;
	}

	private JPanel createTimeSlot(String time) {
		JPanel slot = new JPanel(new BorderLayout());
		slot.setPreferredSize(new Dimension(50, 60));
		slot.setBackground(Color.WHITE);
		slot.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));

		JLabel timeLabel = new JLabel(time);
		timeLabel.setFont(new Font("Inter", Font.PLAIN, 12));
		timeLabel.setForeground(TEXT_GRAY);
		timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		slot.add(timeLabel, BorderLayout.CENTER);

		return slot;
	}

	private JPanel createEventSlot() {
		JPanel slot = new JPanel(new MigLayout("insets 0"));
		slot.setPreferredSize(new Dimension(0, 60));
		slot.setBackground(Color.WHITE);
		slot.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
		return slot;
	}

	private void updateDayViewAppointments() {
		// Clear existing appointments
		Component[] components = dayViewPanel.getComponents();
		JScrollPane scrollPane = (JScrollPane) components[1];
		JPanel schedulePanel = (JPanel) scrollPane.getViewport().getView();
		JPanel eventsPanel = (JPanel) schedulePanel.getComponent(1);
		
		// Get appointments for current date
		List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(connection, currentDate);
		
		// Clear existing events
		eventsPanel.removeAll();
		
		// Recreate empty slots
		for (int hour = 9; hour <= 17; hour++) {
			JPanel eventSlot = createEventSlot();
			eventsPanel.add(eventSlot, "grow");
		}
		
		// Add appointments
		for (Appointment appointment : appointments) {
			LocalTime time = appointment.getAppointmentDateTime().toLocalTime();
			int hour = time.getHour();
			int slotIndex = hour - 9;
			
			if (slotIndex >= 0 && slotIndex < eventsPanel.getComponentCount()) {
				JPanel eventSlot = (JPanel) eventsPanel.getComponent(slotIndex);
				addAppointmentToSlot(eventSlot, appointment);
			}
		}
		
		eventsPanel.revalidate();
		eventsPanel.repaint();
	}

	private void addAppointmentToSlot(JPanel slot, Appointment appointment) {
		JPanel appointmentPanel = new JPanel(new MigLayout("fillx, insets 5"));
		appointmentPanel.setBackground(new Color(135, 206, 250));
		appointmentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		String timeStr = appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("h:mm a"));
		JLabel timeLabel = new JLabel(timeStr);
		timeLabel.setFont(new Font("Inter", Font.BOLD, 12));
		
		JLabel typeLabel = new JLabel(appointment.getAppointmentType());
		typeLabel.setFont(new Font("Inter", Font.PLAIN, 12));

		appointmentPanel.add(timeLabel, "split 2");
		appointmentPanel.add(typeLabel);
		
		slot.add(appointmentPanel, "grow");
	}

	private void styleNavigationButton(JButton button) {
		button.setFont(new Font("Inter", Font.BOLD, 16));
		button.setForeground(TEXT_GRAY);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
}
