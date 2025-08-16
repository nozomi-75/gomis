package lyfjshs.gomis.view.appointment;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.components.FlatButton;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;
import raven.modal.option.Option;

public class AppointmentCalendar extends JPanel {
    private static final Logger logger = LogManager.getLogger(AppointmentCalendar.class);
    private final AppointmentDAO appointmentDao;
    private final Connection connection;
    private final AppointmentCalendarAdapter adapter;
    private final SessionsDAO sessionsDAO;

    // Calendar state
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private Map<Integer, List<Appointment>> monthlyAppointments;

    // Listeners
    private final List<Consumer<LocalDate>> dateSelectionListeners = new ArrayList<>();
    private final List<Consumer<Appointment>> appointmentClickListeners = new ArrayList<>();

    // Fonts
    private final Font weekDayFont = new Font("SansSerif", Font.BOLD, 12);
    private final Font dayNumberFont = new Font("SansSerif", Font.BOLD, 13);
    private final Font appointmentFont = new Font("SansSerif", Font.PLAIN, 16);
    private final Font overflowFont = new Font("SansSerif", Font.ITALIC, 11);

    private Map<Rectangle, Appointment> appointmentBounds;
    private Appointment hoveredAppointment;

    // Constants for drawing
    private final int PADDING = 5;
    private final int BOTTOM_MARGIN_FOR_APPOINTMENTS = 3;
    private final int DOT_SIZE = 6;

    public AppointmentCalendar(AppointmentDAO appointDAO, Connection conn, SessionsDAO sessionsDAO) {
        this.appointmentDao = appointDAO;
        this.connection = conn;
        this.sessionsDAO = sessionsDAO;
        this.adapter = new AppointmentCalendarAdapter(appointDAO);
        
        // Initialize calendar state
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        this.monthlyAppointments = Collections.emptyMap();
        this.appointmentBounds = new HashMap<>();
        
        setLayout(new BorderLayout(5, 5));
        initUI();
        refreshCalendar();
    }

    private void initUI() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        FlatButton prevButton = new FlatButton();
        prevButton.setText("<");
        FlatButton nextButton = new FlatButton();
        nextButton.setText(">");
        prevButton.addActionListener(e -> changeMonth(-1));
        nextButton.addActionListener(e -> changeMonth(1));
        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Calendar Drawing Panel
        calendarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawCalendar((Graphics2D) g);
            }
        };
        add(calendarPanel, BorderLayout.CENTER);
        
        // Mouse Listeners
        calendarPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleDateOrAppointmentClick(e.getPoint());
            }
        });
        calendarPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoved(e.getPoint());
            }
        });
    }

    private void changeMonth(int amount) {
        currentMonth = currentMonth.plusMonths(amount);
        refreshCalendar();
    }

    public void refreshCalendar() {
        SwingUtilities.invokeLater(() -> {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")) + " (Loading...)");

                Map<Integer, List<Appointment>> appointments = 
                    adapter.getAppointmentsForMonth(currentMonth.getYear(), currentMonth.getMonthValue());
                
                monthlyAppointments = appointments;
                monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
                calendarPanel.repaint();
            } catch (Exception e) {
                logger.error("Error loading appointments: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(this,
                    "Error loading appointments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    public void updateCalendar() {
        refreshCalendar();
    }

    private void drawCalendar(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        appointmentBounds = new HashMap<>();

        // Layout Constants
        final int HEADER_HEIGHT = 30;

        int width = calendarPanel.getWidth();
        int height = calendarPanel.getHeight();
        int cellWidth = width / 7;
        int cellHeight = (height - HEADER_HEIGHT) / 6;

        // Draw Weekday Headers
        g2d.setFont(weekDayFont);
        FontMetrics weekDayFm = g2d.getFontMetrics();
        String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            int strWidth = weekDayFm.stringWidth(weekdays[i]);
            g2d.setColor(UIManager.getColor("Label.foreground"));
            g2d.drawString(weekdays[i], (i * cellWidth) + (cellWidth - strWidth) / 2, HEADER_HEIGHT / 2 + weekDayFm.getAscent() / 2);
        }

        // Draw Day Cells
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int dayOfWeekOfFirst = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            int row = (day + dayOfWeekOfFirst - 1) / 7;
            int col = (day + dayOfWeekOfFirst - 1) % 7;
            int x = col * cellWidth;
            int y = row * cellHeight + HEADER_HEIGHT;

            // Draw cell background for selected and today
            if (date.equals(selectedDate)) {
                g2d.setColor(new Color(UIManager.getColor("Component.accentColor").getRed(),
                                        UIManager.getColor("Component.accentColor").getGreen(),
                                        UIManager.getColor("Component.accentColor").getBlue(),
                                        50)); // 50 is the alpha value for translucency
                g2d.fillRect(x, y, cellWidth, cellHeight);
            } else if (date.equals(LocalDate.now())) { // Only highlight today if not selected
                g2d.setColor(UIManager.getColor("Component.focusColor"));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x + 1, y + 1, cellWidth - 3, cellHeight - 3);
                g2d.setStroke(new BasicStroke(1));
            }
            
            // Draw grid lines
            g2d.setColor(UIManager.getColor("Separator.foreground"));
            g2d.drawRect(x, y, cellWidth, cellHeight);

            // Draw Day Number
            g2d.setFont(dayNumberFont);
            FontMetrics dayFm = g2d.getFontMetrics();
            String dayText = String.valueOf(day);
            int dayTextWidth = dayFm.stringWidth(dayText);
            
            if (date.equals(selectedDate)) {
                g2d.setColor(Color.WHITE);
        } else {
                g2d.setColor(UIManager.getColor("Label.foreground"));
            }
            g2d.drawString(dayText, x + cellWidth - dayTextWidth - PADDING, y + dayFm.getAscent() + PADDING);

            // Draw Appointments
            List<Appointment> dayAppointments = monthlyAppointments.getOrDefault(day, Collections.emptyList());
            if (!dayAppointments.isEmpty()) {
                g2d.setFont(appointmentFont); // Ensure appointment font is set here
                FontMetrics appFm = g2d.getFontMetrics();
                int appY = y + PADDING;

                for (int i = 0; i < dayAppointments.size(); i++) {
                    if (appY + appFm.getHeight() > y + cellHeight - BOTTOM_MARGIN_FOR_APPOINTMENTS) {
                        g2d.setFont(overflowFont);
                        g2d.setColor(Color.GRAY);
                        g2d.drawString(String.format("+ %d more...", dayAppointments.size() - i), x + PADDING, appY + appFm.getAscent());
                        break;
                    }
                    
                    Appointment app = dayAppointments.get(i);
                    g2d.setFont(appointmentFont); // Re-ensure appointment font is set for each app
                    
                    // Draw indicator dot
                    g2d.setColor(UIManager.getColor("Component.accentColor"));
                    g2d.fillOval(x + PADDING, appY + (appFm.getAscent() - DOT_SIZE) / 2, DOT_SIZE, DOT_SIZE);

                    // Draw appointment title
                    g2d.setColor(UIManager.getColor("Label.foreground"));
                    // Calculate textX relative to the cell's x, accounting for padding and dot
                    int textXInCell = PADDING + DOT_SIZE + PADDING; 
                    // Calculate maxWidth based on cellWidth, left offset, and right padding
                    int availableWidthForText = cellWidth - (PADDING * 2) - DOT_SIZE;
                    String title = truncateText(app.getAppointmentTitle(), g2d, availableWidthForText);
                    g2d.drawString(title, x + textXInCell, appY + appFm.getAscent());
                    
                    // Store the bounds for click detection
                    Rectangle bounds = new Rectangle(x, appY, cellWidth, appFm.getHeight());
                    appointmentBounds.put(bounds, app);

                    appY += appFm.getHeight(); // Move to next line
                }
            }
        }
    }

    private String truncateText(String text, Graphics2D g2d, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String ellipsis = "...";
        int ellipsisWidth = g2d.getFontMetrics().stringWidth(ellipsis);
        
        // If the text fits, return as is
        if (g2d.getFontMetrics().stringWidth(text) <= maxWidth) {
            return text;
        }

        // Try to truncate and add ellipsis
        int currentWidth = g2d.getFontMetrics().stringWidth(text);
        while (currentWidth + ellipsisWidth > maxWidth && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
            currentWidth = g2d.getFontMetrics().stringWidth(text);
        }
        // If after truncation, text is empty, just return ellipsis
        if (text.isEmpty()) {
            return ellipsis;
        }

        return text + ellipsis;
    }

    private void handleDateOrAppointmentClick(Point clickPoint) {
        // First, check if the click was on a specific appointment
        for (Map.Entry<Rectangle, Appointment> entry : appointmentBounds.entrySet()) {
            if (entry.getKey().contains(clickPoint)) {
                // Show details for the clicked appointment
                showAppointmentDetailsPopup(entry.getValue());
                return; // Click handled, do not proceed.
            }
        }

        // If not, it was a click on the general day cell
        int headerHeight = 30;
        if (clickPoint.y < headerHeight) return; 

        int cellWidth = calendarPanel.getWidth() / 7;
        int cellHeight = (calendarPanel.getHeight() - headerHeight) / 6;
        int col = clickPoint.x / cellWidth;
        int row = (clickPoint.y - headerHeight) / cellHeight;
        
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int dayOfWeekOfFirst = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        int dayOfMonth = (row * 7 + col) - dayOfWeekOfFirst + 1;

        if (dayOfMonth >= 1 && dayOfMonth <= currentMonth.lengthOfMonth()) {
            LocalDate clickedDate = currentMonth.atDay(dayOfMonth);
            if (clickedDate.equals(selectedDate)) {
                // If already selected, deselect it
                selectedDate = null;
            } else {
                // Otherwise, select the new date
                selectedDate = clickedDate;
            }
            calendarPanel.repaint();
            
            // Only show appointments list if a date is selected
            if (selectedDate != null) {
                showAppointmentsListForDate(selectedDate);
            }
        }
    }

    // Method to show appointments list for a specific date
    private void showAppointmentsListForDate(LocalDate date) {
        try {
            // Create and show AppointmentsListPanel as a modal
            AppointmentsListPanel listPanel = new AppointmentsListPanel(
                connection,
                date,
                appointment -> {
                    // When an appointment is selected, show its details
                    showAppointmentDetailsPopup(appointment);
                }
            );

            Option listOption = new Option();
            listOption.setOpacity(0f)
                    .setAnimationOnClose(false)
                    .getBorderOption().setBorderWidth(0.5f).setShadow(BorderOption.Shadow.MEDIUM);
            listOption.getLayoutOption().setSize(700, 500); // Set a default size for the list modal

            ModalDialog.showModal(
                Main.gFrame,
                new SimpleModalBorder(
                    listPanel,
                    "Appointments for " + date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    new SimpleModalBorder.Option[] { 
                        new SimpleModalBorder.Option("Add New", SimpleModalBorder.YES_OPTION),
                        new SimpleModalBorder.Option("Close", SimpleModalBorder.CANCEL_OPTION) 
                    },
                            (controller, action) -> {
                                if (action == SimpleModalBorder.YES_OPTION) {
                            // Only close if we can show the add dialog
                            try {
                                controller.close();
                                showAddAppointmentDialog(date);
                            } catch (Exception e) {
                                controller.consume();
                                                    JOptionPane.showMessageDialog(this, 
                                    "Error showing add appointment dialog: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                            controller.close();
                        } else {
                            // Consume any other actions to prevent unwanted closing
                            controller.consume();
                        }
                    }
                ),
                listOption, // Pass the specific option for this modal
                "appointments_list_popup"
            );
        } catch (Exception e) {
            logger.error("Error showing appointments: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, 
                "Error showing appointments: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMouseMoved(Point mousePoint) {
        Appointment newHoveredAppointment = null;
        for (Map.Entry<Rectangle, Appointment> entry : appointmentBounds.entrySet()) {
            if (entry.getKey().contains(mousePoint)) {
                newHoveredAppointment = entry.getValue();
                break;
            }
        }

        if (newHoveredAppointment != hoveredAppointment) {
            hoveredAppointment = newHoveredAppointment;
            if (hoveredAppointment != null) {
                // Set a detailed tooltip for the hovered appointment
                String counselorName = "Unknown";
                try {
                    GuidanceCounselor c = Main.formManager.getCounselorObject(); // Use Main.formManager
                    if (c != null) counselorName = c.getFirstName() + " " + c.getLastName();
                } catch (Exception e) { /* ignore for tooltip */ }
                
                String tooltip = String.format("<html><b>%s</b><br>Time: %s<br>Counselor: %s</html>",
                    hoveredAppointment.getAppointmentTitle(),
                    hoveredAppointment.getAppointmentDateTime().toLocalDateTime().toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    counselorName);
                calendarPanel.setToolTipText(tooltip);
            } else {
                calendarPanel.setToolTipText(null);
            }
        }
    }

    // --- Listener registration methods ---
    public void addDateSelectionListener(Consumer<LocalDate> listener) {
        dateSelectionListeners.add(listener);
    }
    public void addAppointmentClickListener(Consumer<Appointment> listener) {
        appointmentClickListeners.add(listener);
    }

    public LocalDate getSelectedDate() {
        return this.selectedDate;
    }

    public YearMonth getCurrentMonth() {
        return this.currentMonth;
    }

    // Method to show AddAppointmentPanel as a modal when a date is clicked
    private void showAddAppointmentDialog(LocalDate selectedDate) {
        // Create a new Appointment object and set the selected date
            Appointment newAppointment = new Appointment();
        newAppointment.setAppointmentDateTime(Timestamp.valueOf(selectedDate.atStartOfDay()));
        newAppointment.setAppointmentStatus("Pending"); // Set a default status
        newAppointment.setConsultationType("New"); // Set a default type
        newAppointment.setAppointmentTitle(""); // Set an empty title

        AddAppointmentModal.getInstance().showModal(
            connection,
            Main.gFrame, // Use Main.gFrame as parent component
            new AddAppointmentPanel(newAppointment, appointmentDao, connection), // Pass the new Appointment object
                appointmentDao,
            800, // Width
            600, // Height
            this::refreshCalendar // Refresh calendar on success
        );
    }

    // Method to show details of an existing appointment as a modal
    private void showAppointmentDetailsPopup(Appointment appointment) {
        // Create and show AppointmentDayDetails panel as a modal
        AppointmentDayDetails detailsPanel = new AppointmentDayDetails(
            connection,
            participant -> { /* handle participant selection if needed */ },
            appointmentToRedirect -> {
                // Callback from AppointmentDayDetails to open session from alarm/direct click
                // This is currently handled by AppointmentManagement.openSessionFromAlarm
                // which then sets appointment status to In Progress.
                // We need to re-evaluate this flow if the appointment is already rescheduled.

                // Check if a session already exists for this appointment
                try {
                    // Use getSessionsByAppointmentId which returns a list
                    List<lyfjshs.gomis.Database.entity.Sessions> existingSessions = sessionsDAO.getSessionsByAppointmentId(appointmentToRedirect.getAppointmentId());

                    if (existingSessions != null && !existingSessions.isEmpty()) {
                        // If session exists, open the first one directly
                        lyfjshs.gomis.Database.entity.Sessions sessionToOpen = existingSessions.get(0);
                        // Find the SessionsFillUpFormPanel instance and populate it
                        lyfjshs.gomis.components.FormManager.Form[] forms = lyfjshs.gomis.components.FormManager.FormManager.getForms();
                        for (lyfjshs.gomis.components.FormManager.Form form : forms) {
                            if (form instanceof lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel) {
                                lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel SessionsFillUpFormPanel = (lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel) form;
                                SessionsFillUpFormPanel.setEditingSession(sessionToOpen); // Use setEditingSession with the Session object
                                lyfjshs.gomis.components.FormManager.FormManager.showForm(SessionsFillUpFormPanel);
                                break;
                            }
                        }
                    } else {
                        // If no session exists, proceed with the original logic (create new session)
                        // This means going to the SessionsFillUpFormPanel and populating it from the appointment
                        lyfjshs.gomis.components.DrawerBuilder.switchToSessionsFillUpFormPanel();
                        lyfjshs.gomis.components.FormManager.Form[] forms = lyfjshs.gomis.components.FormManager.FormManager.getForms();
                        for (lyfjshs.gomis.components.FormManager.Form form : forms) {
                            if (form instanceof lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel) {
                                lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel SessionsFillUpFormPanel = (lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel) form;
                                SessionsFillUpFormPanel.populateFromAppointment(appointmentToRedirect);

                                // Update status to In Progress when creating new session from appointment
                                appointmentToRedirect.setAppointmentStatus("In Progress");
                                appointmentDao.updateAppointment(appointmentToRedirect);
                                EventBus.publish("appointment_status_changed", appointmentToRedirect.getAppointmentId());
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    logger.error("Error checking or opening session: " + e.getMessage(), e);
                    JOptionPane.showMessageDialog(Main.gFrame, "Error checking or opening session: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            },
            // Callback for editing appointment
            editedAppointment -> {
                try {
                    appointmentDao.updateAppointment(editedAppointment);
                    refreshCalendar(); // Refresh calendar after edit
                    } catch (SQLException e) {
                        logger.error("Error updating appointment: " + e.getMessage(), e);
                        JOptionPane.showMessageDialog(this,
                        "Error updating appointment: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            );

        try {
            detailsPanel.loadAppointmentDetails(appointment);

            Option detailsOption = new Option();
            detailsOption.setOpacity(0f)
                .setAnimationOnClose(false)
                .getBorderOption().setBorderWidth(0.5f).setShadow(BorderOption.Shadow.MEDIUM);
            detailsOption.getLayoutOption().setSize(700, 500); // Set a default size for the details modal

            // Determine button text and action based on appointment status and existence of session
            String sessionButtonText = "Create Session";
            SimpleModalBorder.Option sessionButtonOption;
            SessionsDAO sessionCheckDAO = new SessionsDAO(connection);
            // Check if there are any sessions associated with this appointment
            List<lyfjshs.gomis.Database.entity.Sessions> associatedSessions = sessionCheckDAO.getSessionsByAppointmentId(appointment.getAppointmentId());
            boolean hasExistingSession = (associatedSessions != null && !associatedSessions.isEmpty());

            if ("Rescheduled".equalsIgnoreCase(appointment.getAppointmentStatus()) && hasExistingSession) {
                sessionButtonText = "Go to Session";
                sessionButtonOption = new SimpleModalBorder.Option(sessionButtonText, SimpleModalBorder.YES_OPTION);
            } else if (hasExistingSession) {
                // If a session already exists for a non-rescheduled appointment, also offer to go to session
                sessionButtonText = "Go to Session";
                sessionButtonOption = new SimpleModalBorder.Option(sessionButtonText, SimpleModalBorder.YES_OPTION);
            } else {
                // For other statuses or no existing session, offer to create
                sessionButtonOption = new SimpleModalBorder.Option(sessionButtonText, SimpleModalBorder.YES_OPTION);
            }

            ModalDialog.showModal(
                Main.gFrame,
                new SimpleModalBorder(
                    detailsPanel,
                    "Appointment Details",
                    new SimpleModalBorder.Option[] {
                        sessionButtonOption, // Dynamically set session button
                        new SimpleModalBorder.Option("Edit", SimpleModalBorder.NO_OPTION), // Changed to NO_OPTION to differentiate
                        new SimpleModalBorder.Option("Delete", 3), // Arbitrary option value for delete
                        new SimpleModalBorder.Option("Close", SimpleModalBorder.CANCEL_OPTION)
                    },
                    (controller, action) -> {
                        if (action == SimpleModalBorder.YES_OPTION) { // This is now for session action
                            controller.close();
                            // Trigger the session redirect logic in AppointmentDayDetails
                            detailsPanel.triggerSessionRedirect(appointment);
                        } else if (action == SimpleModalBorder.NO_OPTION) { // This is now for Edit
                            controller.close();
                            showEditAppointmentDialog(appointment);
                        } else if (action == 3) { // This is for Delete
                            // Handle delete appointment
                            int confirmResult = JOptionPane.showConfirmDialog(Main.gFrame,
                                "Are you sure you want to delete this appointment? This cannot be undone.",
                                "Confirm Delete",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);

                            if (confirmResult == JOptionPane.YES_OPTION) {
                                try {
                                    appointmentDao.deleteAppointment(appointment.getAppointmentId());
                                    refreshCalendar();
                                    controller.close(); // Close the modal after deletion
                                    JOptionPane.showMessageDialog(Main.gFrame, "Appointment deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                } catch (SQLException ex) {
                                    logger.error("Error deleting appointment: " + ex.getMessage(), ex);
                                    JOptionPane.showMessageDialog(Main.gFrame, "Error deleting appointment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (action == SimpleModalBorder.CANCEL_OPTION || action == SimpleModalBorder.CLOSE_OPTION) {
                            controller.close();
                        } else {
                            controller.consume();
                        }
                    }
                ),
                detailsOption, // Pass the specific option for this modal
                "appointment_details_popup"
            );
        } catch (Exception e) {
            logger.error("Error showing appointment details: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Error showing appointment details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // New method to show details of an existing appointment for editing
    private void showEditAppointmentDialog(Appointment appointmentToEdit) {
        AddAppointmentModal.getInstance().showModal(
            connection,
            Main.gFrame, // Use Main.gFrame as parent component
            new AddAppointmentPanel(appointmentToEdit, appointmentDao, connection), // Pass the existing Appointment object
            appointmentDao,
            800, // Width
            600, // Height
            this::refreshCalendar // Refresh calendar on success
        );
    }

    // // Method to handle rescheduling a missed appointment
    // private void rescheduleMissedAppointment(Appointment appointment) {
    //     // Set initial status to Rescheduled
    //     appointment.setAppointmentStatus("Rescheduled");
    //     try {
    //         appointmentDao.updateAppointment(appointment);
    //         refreshCalendar(); // Refresh calendar after update
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         JOptionPane.showMessageDialog(this, "Error rescheduling appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    //     }
    // }
}