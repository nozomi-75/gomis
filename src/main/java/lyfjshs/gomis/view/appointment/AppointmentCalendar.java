package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;

public class AppointmentCalendar extends JPanel {
    private AppointmentDAO appointmentDao;
    private Connection connection;
    private LocalDate currentDate;
    private JPanel calendarPanel;
    private JLabel monthYearLabel;
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final int DAY_SIZE = 100; // Constant size for day boxes

    public AppointmentCalendar(AppointmentDAO appointDAO, Connection conn) {
        this.appointmentDao = appointDAO;
        this.connection = conn;
        currentDate = LocalDate.now();

        setLayout(new MigLayout("wrap 1, fill, insets 5", "[grow]", "[pref!][pref!][grow]"));

        // Navigation panel
        JPanel navigationPanel = new JPanel(new MigLayout("fill", "[]push[]"));
        JButton prevMonthButton = new JButton("Previous Month");
        JButton nextMonthButton = new JButton("Next Month");
        monthYearLabel = new JLabel(currentDate.format(MONTH_YEAR_FORMATTER), SwingConstants.CENTER);

        prevMonthButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            updateCalendar();
        });

        nextMonthButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            updateCalendar();
        });

        navigationPanel.add(prevMonthButton);
        navigationPanel.add(monthYearLabel);
        navigationPanel.add(nextMonthButton);
        add(navigationPanel, "growx");

        // Week day headers
        JPanel weekDaysPanel = new JPanel(new MigLayout("wrap 7, fill, insets 0"));
        String[] dayNames = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (String day : dayNames) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setForeground(Color.DARK_GRAY);
            weekDaysPanel.add(dayLabel, "width " + DAY_SIZE + "!, alignx center");
        }
        add(weekDaysPanel, "growx");

        calendarPanel = new JPanel(new MigLayout("wrap 7, fill, insets 5, gap 5", "[]", "[]"));
        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setBorder(null);
        add(scrollPane, "grow");

        updateCalendar();
    }

    public void updateCalendar() {
        monthYearLabel.setText(currentDate.format(MONTH_YEAR_FORMATTER));
        calendarPanel.removeAll();
        populateCalendarDays();
        revalidate();
        repaint();
    }

    private void populateCalendarDays() {
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        // Empty cells before first day
        for (int i = 0; i < startDayOfWeek; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setPreferredSize(new Dimension(DAY_SIZE, DAY_SIZE));
            calendarPanel.add(emptyPanel, "grow");
        }

        // Day panels
        for (int day = 1; day <= daysInMonth; day++) {
            final LocalDate currentDay = firstOfMonth.plusDays(day - 1);
            JPanel dayPanel = createDayPanel(currentDay);
            calendarPanel.add(dayPanel, "grow");
        }
    }

    private JPanel createDayPanel(LocalDate date) {
        JPanel dayPanel = new JPanel(new MigLayout("wrap 1, insets 2", "[grow,fill]", "[][grow]"));
        dayPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        dayPanel.setBackground(new Color(230, 240, 250));
        dayPanel.setPreferredSize(new Dimension(DAY_SIZE, DAY_SIZE));

        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dayPanel.add(dayLabel, "align right");

        List<Appointment> appointments = null;
        try {
            appointments = appointmentDao.getAppointmentsForDate(date);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!appointments.isEmpty()) {
            JPanel appointmentsContainer = new JPanel(new MigLayout("wrap 1, insets 0, gap 2", "[grow,fill]"));
            appointmentsContainer.setOpaque(false);
            dayPanel.add(new JScrollPane(appointmentsContainer), "grow");

            Map<String, Color> appointmentColors = new HashMap<>();
            appointmentColors.put("Academic Consultation", new Color(59, 130, 246));
            appointmentColors.put("Career Guidance", new Color(147, 51, 234));
            appointmentColors.put("Personal Counseling", new Color(16, 185, 129));
            appointmentColors.put("Behavioral Counseling", new Color(202, 138, 4));
            appointmentColors.put("Group Counseling", new Color(239, 68, 68));

            for (Appointment appt : appointments) {
                JPanel appointmentPanel = new JPanel(new MigLayout("fill, insets 2", "[grow][]"));
                Color bgColor = appointmentColors.getOrDefault(appt.getConsultationType(), Color.GRAY);
                appointmentPanel.setBackground(bgColor);

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String timeDisplay = appt.getAppointmentDateTime().toLocalDateTime().format(timeFormatter);

                JLabel appLabel = new JLabel(timeDisplay + " - " + appt.getAppointmentTitle());
                appLabel.setForeground(Color.BLACK);
                appointmentPanel.add(appLabel, "grow");

                // Make the appointment panel clickable
                appointmentPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showAppointmentDetailsPopup(appt);
                    }
                });

                appointmentsContainer.add(appointmentPanel, "grow");
            }
        }

        dayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showAppointmentDetailsPopup(date);
            }
        });

        return dayPanel;
    }

    private void showAppointmentDetailsPopup(Appointment appointment) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Appointment Details", true);
        AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(connection);
        appointmentDetails.loadAppointmentDetails(appointment);
        dialog.getContentPane().add(appointmentDetails);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showAppointmentDetailsPopup(LocalDate selectedDate) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Appointment Details", true);
        AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(connection);
        try {
            appointmentDetails.loadAppointmentsForDate(selectedDate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        dialog.getContentPane().add(appointmentDetails);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}