package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.sessions.SessionsForm;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;

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
        
        // Ensure panel is opaque and has background color
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        
        // Use proper layout constraints
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
        dayPanel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        dayPanel.setPreferredSize(new Dimension(DAY_SIZE, DAY_SIZE));

        // Create and style the day label
        JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
        dayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        // Enhanced current day highlighting
        if (date.equals(LocalDate.now())) {
            // Create a circular background for current date
            JPanel dateHighlight = new JPanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    super.paintComponent(g);
                    java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                      java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Use theme-aware colors
                    boolean isDarkTheme = dayPanel.getBackground() != null && 
                                        dayPanel.getBackground().getRed() < 128;
                    
                    if (isDarkTheme) {
                        g2.setColor(new Color(65, 105, 225)); // Royal Blue for dark theme
                    } else {
                        g2.setColor(new Color(30, 144, 255)); // Dodger Blue for light theme
                    }
                    
                    int size = Math.min(getWidth(), getHeight()) - 4;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;
                    g2.fillOval(x, y, size, size);
                    g2.dispose();
                }
            };
            dateHighlight.setOpaque(false);
            dateHighlight.setPreferredSize(new Dimension(24, 24));
            
            // Style the day label for current date
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // Add the highlight panel with the label
            JPanel highlightWrapper = new JPanel(new BorderLayout());
            highlightWrapper.setOpaque(false);
            highlightWrapper.add(dateHighlight, BorderLayout.CENTER);
            dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dateHighlight.add(dayLabel);
            dayPanel.add(highlightWrapper, "align right");
        } else {
            // Regular day styling
            dayPanel.add(dayLabel, "align right");
        }

        List<Appointment> appointments = null;
        try {
            // Get all appointments for this date and filter client-side
            appointments = appointmentDao.getAppointmentsForDate(date);
            
            // Filter for active-like statuses only
            if (appointments != null) {
                appointments = appointments.stream()
                    .filter(a -> "Scheduled".equals(a.getAppointmentStatus()) || 
                                "In Progress".equals(a.getAppointmentStatus()) ||
                                "Rescheduled".equals(a.getAppointmentStatus()))  // Add Rescheduled status
                    .toList();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if (appointments != null && !appointments.isEmpty()) {
            JPanel appointmentsContainer = new JPanel(new MigLayout("wrap 1, insets 0, gap 2", "[grow,fill]"));
            appointmentsContainer.setOpaque(false);
            dayPanel.add(new JScrollPane(appointmentsContainer) {
                {
                    setBorder(null);
                    getViewport().setOpaque(false);
                    setOpaque(false);
                }
            }, "grow");

            // Updated color scheme for better visibility in dark theme
            Map<String, Color[]> appointmentColors = new HashMap<>();
            // Each array contains [background color, text color]
            appointmentColors.put("Academic Consultation", new Color[]{new Color(25, 95, 210), Color.WHITE});
            appointmentColors.put("Career Guidance", new Color[]{new Color(120, 40, 190), Color.WHITE});
            appointmentColors.put("Personal Counseling", new Color[]{new Color(15, 145, 100), Color.WHITE});
            appointmentColors.put("Behavioral Counseling", new Color[]{new Color(180, 120, 10), Color.WHITE});
            appointmentColors.put("Group Counseling", new Color[]{new Color(190, 45, 45), Color.WHITE});

            for (Appointment appt : appointments) {
                JPanel appointmentPanel = new JPanel(new MigLayout("fill, insets 2", "[grow][]"));
                
                // Get the base colors for the appointment type
                Color[] baseColors = appointmentColors.getOrDefault(appt.getConsultationType(), 
                    new Color[]{new Color(100, 100, 100), Color.WHITE});
                
                // Determine final colors based on status
                final Color[] colors;
                if ("Rescheduled".equals(appt.getAppointmentStatus())) {
                    // Add a slight orange tint to indicate rescheduled status
                    colors = new Color[]{
                        new Color(
                            Math.min(255, baseColors[0].getRed() + 40),
                            Math.min(255, baseColors[0].getGreen() + 20),
                            baseColors[0].getBlue()
                        ),
                        Color.WHITE
                    };
                } else {
                    colors = baseColors;
                }
                
                appointmentPanel.setBackground(colors[0]);
                appointmentPanel.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
                String timeDisplay = appt.getAppointmentDateTime().toLocalDateTime().format(timeFormatter);
                
                // Add rescheduled indicator to the title if needed
                String titleText = timeDisplay + " - " + appt.getAppointmentTitle();
                if ("Rescheduled".equals(appt.getAppointmentStatus())) {
                    titleText = "🔄 " + titleText;  // Add rescheduled icon
                }

                JLabel appLabel = new JLabel(titleText);
                appLabel.setForeground(colors[1]);
                appointmentPanel.add(appLabel, "grow");

                // Add hover effect
                appointmentPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        appointmentPanel.setBackground(colors[0].brighter());
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        appointmentPanel.setBackground(colors[0]);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        showAppointmentDetailsPopup(appt);
                    }
                });

                appointmentsContainer.add(appointmentPanel, "grow");
            }
        }

        // Add hover effect for the entire day panel
        dayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!date.equals(LocalDate.now())) {
                    boolean isDarkTheme = dayPanel.getBackground() != null && 
                                        dayPanel.getBackground().getRed() < 128;
                    dayPanel.setBackground(isDarkTheme ? 
                        new Color(50, 50, 50) : 
                        new Color(240, 240, 240));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!date.equals(LocalDate.now())) {
                    dayPanel.setBackground(null);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showAppointmentDetailsPopup(date);
            }
        });

        return dayPanel;
    }

    private void showAppointmentDetailsPopup(Appointment appointment) {
        if (ModalDialog.isIdExist("appointment_details_" + appointment.getAppointmentId())) {
            return;
        }

        try {
            // Check if the appointment is active-like status
            boolean isActiveStatus = "Scheduled".equals(appointment.getAppointmentStatus()) || 
                                    "In Progress".equals(appointment.getAppointmentStatus()) ||
                                    "Rescheduled".equals(appointment.getAppointmentStatus());
            
            // If it's not active-like, don't show details
            if (!isActiveStatus) {
                return;
            }
            
            // Get full appointment with participants
            Appointment currentAppointment = appointmentDao.getAppointmentById(appointment.getAppointmentId());
            
            if (currentAppointment == null) {
                // If the appointment is not found, don't show it
                return;
            }

            // Check if appointment is today and within 30 minutes of current time
            LocalDateTime appointmentTime = currentAppointment.getAppointmentDateTime().toLocalDateTime();
            LocalDateTime now = LocalDateTime.now();
            boolean isToday = appointmentTime.toLocalDate().equals(now.toLocalDate());
            long minutesDifference = java.time.temporal.ChronoUnit.MINUTES.between(now, appointmentTime);
            
            // If appointment is today and within 30 minutes (before or after scheduled time)
            if (isToday && Math.abs(minutesDifference) <= 30) {
                int response = JOptionPane.showConfirmDialog(
                    this,
                    "This appointment is scheduled for today and is within 30 minutes of the current time.\n" +
                    "Would you like to start a session for this appointment now?",
                    "Start Session",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (response == JOptionPane.YES_OPTION) {
                    // Switch to sessions form and populate it with this appointment
                    DrawerBuilder.switchToSessionsForm();
                    Form[] forms = FormManager.getForms();
                    for (Form form : forms) {
                        if (form instanceof SessionsForm) {
                            SessionsForm sessionsForm = (SessionsForm) form;
                            sessionsForm.populateFromAppointment(currentAppointment);
                            // Set the appointment status to "In Progress"
                            currentAppointment.setAppointmentStatus("In Progress");
                            appointmentDao.updateAppointment(currentAppointment);
                            // Refresh views
                            updateCalendar();
                            if (getParent() instanceof AppointmentManagement) {
                                ((AppointmentManagement) getParent()).refreshViews();
                            }
                            return;
                        }
                    }
                }
            }

            AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(
                connection, 
                null, 
                null,
                // Add refresh callback
                refreshedAppointment -> {
                    updateCalendar();
                    if (getParent() instanceof AppointmentManagement) {
                        ((AppointmentManagement) getParent()).refreshViews();
                    }
                }
            );
            appointmentDetails.loadAppointmentDetails(currentAppointment);
            createAndShowModalDialog(appointmentDetails, "appointment_details_" + currentAppointment.getAppointmentId(), 
                currentAppointment.getAppointmentDateTime().toLocalDateTime().toLocalDate());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading appointment details: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAppointmentDetailsPopup(LocalDate selectedDate) {
        AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(connection, null, null);
        try {
            // Get all appointments for the selected date
            List<Appointment> appointments = appointmentDao.getAppointmentsForDate(selectedDate);
            
            // Filter for active-like statuses
            List<Appointment> activeAppointments = appointments.stream()
                .filter(a -> "Active".equals(a.getAppointmentStatus()) || 
                           "Scheduled".equals(a.getAppointmentStatus()) || 
                           "Rescheduled".equals(a.getAppointmentStatus()))
                .toList();
            
            // If there are no active appointments, show empty state
            if (activeAppointments == null || activeAppointments.isEmpty()) {
                appointmentDetails = new AppointmentDayDetails(connection, null, null);
                // Initialize with empty state
                appointmentDetails.loadAppointmentDetails(null);
            } else {
                // Load the first active appointment
                appointmentDetails.loadAppointmentDetails(activeAppointments.get(0));
            }
            
            createAndShowModalDialog(appointmentDetails, "appointment_details_date_" + selectedDate.toString(), selectedDate);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading appointments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createAndShowModalDialog(AppointmentDayDetails detailsPanel, String modalId, LocalDate selectedDate) {
        if (ModalDialog.isIdExist(modalId)) {
            return;
        }

        try {
            // Find the proper parent window
            java.awt.Window parentWindow = javax.swing.SwingUtilities.getWindowAncestor(this);
            if (parentWindow == null) {
                parentWindow = Main.gFrame;
            }

            // Configure modal options
            ModalDialog.getDefaultOption()
                    .setOpacity(0f)
                    .setAnimationOnClose(false)
                    .getBorderOption()
                    .setBorderWidth(0.5f)
                    .setShadow(BorderOption.Shadow.MEDIUM);

            // Check if there is a current appointment
            Appointment currentAppointment = detailsPanel.getCurrentAppointment();
            
            // Set options based on whether there's an appointment with active-like status
            SimpleModalBorder.Option[] options;
            boolean isActiveAppointment = currentAppointment != null && 
                                         ("Active".equals(currentAppointment.getAppointmentStatus()) || 
                                          "Scheduled".equals(currentAppointment.getAppointmentStatus()) || 
                                          "Rescheduled".equals(currentAppointment.getAppointmentStatus()));
            
            if (isActiveAppointment) {
                // If there's an active appointment, show "Set a Session" option
                options = new SimpleModalBorder.Option[] {
                    new SimpleModalBorder.Option("Set a Session", SimpleModalBorder.YES_OPTION),
                    new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                };
            } else {
                // If no appointment or ended appointment, show "Add a Appointment" option
                options = new SimpleModalBorder.Option[] {
                    new SimpleModalBorder.Option("Add a Appointment", SimpleModalBorder.YES_OPTION),
                    new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                };
            }

            // Show modal with conditional action handling
            ModalDialog.showModal(this,
                    new SimpleModalBorder(detailsPanel, "Appointment Details",
                            options,
                            (controller, action) -> {
                                if (action == SimpleModalBorder.YES_OPTION) {
                                    if (isActiveAppointment) {
                                        // Handle "Set a Session" action
                                        DrawerBuilder.switchToSessionsForm();
                                        Form[] forms = FormManager.getForms();
                                        for (Form form : forms) {
                                            if (form instanceof SessionsForm) {
                                                SessionsForm sessionsForm = (SessionsForm) form;
                                                try {
                                                    AppointmentDAO appointmentDAO = new AppointmentDAO(connection);
                                                    Appointment fullAppointment = appointmentDAO.getAppointmentById(
                                                        currentAppointment.getAppointmentId());
                                                    if (fullAppointment != null) {
                                                        sessionsForm.populateFromAppointment(fullAppointment);
                                                    }
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                    JOptionPane.showMessageDialog(this, 
                                                        "Error loading appointment details: " + e.getMessage());
                                                }
                                                break;
                                            }
                                        }
                                    } else {
                                        // Handle "Add a Appointment" action
                                        showAddAppointmentDialog(selectedDate);
                                    }
                                    controller.close();
                                } else if (action == SimpleModalBorder.CLOSE_OPTION) {
                                    controller.close();
                                }
                                // Refresh views
                                updateCalendar();
                                if (getParent() instanceof AppointmentManagement) {
                                    ((AppointmentManagement) getParent()).refreshViews();
                                }
                            }),
                    modalId);

            // Set modal size
            ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 700);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error opening appointment details: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddAppointmentDialog(LocalDate selectedDate) {
        try {
            // Verify counselor is logged in
            if (Main.formManager == null || Main.formManager.getCounselorObject() == null) {
                JOptionPane.showMessageDialog(this,
                    "Please log in as a counselor to create appointments.",
                    "Authentication Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create new appointment with selected date
            Appointment newAppointment = new Appointment();
            // Set the appointment date to the selected date at current time
            newAppointment.setAppointmentDateTime(Timestamp.valueOf(selectedDate.atTime(LocalDateTime.now().toLocalTime())));
            newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());

            // Create and configure the appointment panel
            AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDao, connection);

            // Show modal with proper size and validation
            AddAppointmentModal.getInstance().showModal(connection,
                this, 
                addAppointmentPanel,
                appointmentDao,
                750,  // width 
                800,   // height
                () -> {
                    // Callback for successful appointment creation
                    try {
                        // Reload appointments for the selected date
                        AppointmentDayDetails detailsPanel = new AppointmentDayDetails(connection, null, null);
                        detailsPanel.loadAppointmentsForDate(selectedDate);
                        
                        // Close and recreate modal with updated content if it exists
                        String modalId = "appointment_details_date_" + selectedDate.toString();
                        if (ModalDialog.isIdExist(modalId)) {
                            ModalDialog.closeModal(modalId);
                            createAndShowModalDialog(detailsPanel, modalId, selectedDate);
                        }
                        
                        // Refresh calendar view
                        updateCalendar();
                        
                        // Refresh parent views if applicable
                        if (getParent() != null) {
                            java.awt.Component parent = getParent();
                            while (parent != null && !(parent instanceof AppointmentManagement)) {
                                parent = parent.getParent();
                            }
                            if (parent instanceof AppointmentManagement) {
                                ((AppointmentManagement) parent).refreshViews();
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this,
                            "Error updating calendar: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }   
            );

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error creating appointment: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}