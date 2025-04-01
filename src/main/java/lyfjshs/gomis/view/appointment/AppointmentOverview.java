package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class AppointmentOverview extends JPanel {
    private final DatePicker datePicker;
    private final JPanel appointmentsPanel;
    private final List<Appointment> appointments;
    private Connection connection;
    private final AppointmentDAO appointmentDAO;

    public AppointmentOverview(AppointmentDAO appointmentDAO, Connection conn) {
        this.appointmentDAO = appointmentDAO;
        this.connection = conn;
        this.appointments = new ArrayList<>();
        
        // Set panel to be opaque and use system background
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[][grow]"));

        // Date picker section with improved styling
        JPanel datePickerPanel = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
        datePickerPanel.setOpaque(true);
        datePickerPanel.setBackground(UIManager.getColor("Panel.background"));
        datePickerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, 
            UIManager.getColor("Separator.foreground")));
        
        datePicker = createDatePicker();
        datePicker.setPreferredSize(new Dimension(0, 35));
        datePickerPanel.add(datePicker, "grow");
        add(datePickerPanel, "growx, wrap");

        // Appointments list section with improved styling
        appointmentsPanel = new JPanel(new MigLayout("insets 5, gap 5", "[grow]", "[]5[]"));
        appointmentsPanel.setOpaque(true);
        appointmentsPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(true);
        scrollPane.setBackground(UIManager.getColor("Panel.background"));
        scrollPane.getViewport().setOpaque(true);
        scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
        
        add(scrollPane, "grow");

        // Load today's appointments
        loadAppointments(LocalDate.now());
    }

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.setBackground(UIManager.getColor("Panel.background"));
        picker.setForeground(UIManager.getColor("Label.foreground"));
        picker.setOpaque(true);
        picker.addDateSelectionListener(dateEvent -> {
            if (picker.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
                LocalDate selectedDate = picker.getSelectedDate();
                if (selectedDate != null) {
                    loadAppointments(selectedDate);
                }
            }
        });
        picker.now();
        picker.setAnimationEnabled(true);
        return picker;
    }

    private JPanel createAppointmentCard(Appointment appt) {
        JPanel card = new JPanel(new MigLayout("insets 8", "[grow]", "[]3[]3[]"));
        boolean isDarkTheme = FlatLaf.isLafDark();
        
        // Theme-aware colors with improved contrast
        Color bgColor = isDarkTheme ? new Color(45, 45, 45) : new Color(250, 250, 250);
        Color borderColor = isDarkTheme ? new Color(60, 60, 60) : new Color(220, 220, 220);
        Color accentColor = getTypeColor(appt.getConsultationType(), isDarkTheme);
        
        card.setOpaque(true);
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accentColor),
            BorderFactory.createLineBorder(borderColor, 1)));

        // Header with time and title
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow][]"));
        headerPanel.setOpaque(false);
        
        String timeStr = appt.getAppointmentDateTime().toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("h:mm a"));
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLabel.setForeground(UIManager.getColor("Label.foreground"));
        
        JLabel titleLabel = new JLabel(appt.getAppointmentTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        
        headerPanel.add(timeLabel);
        headerPanel.add(titleLabel, "growx");
        card.add(headerPanel, "growx, wrap");

        // Type and status
        JLabel typeLabel = new JLabel(appt.getConsultationType());
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        typeLabel.setForeground(isDarkTheme ? new Color(200, 200, 200) : new Color(100, 100, 100));
        card.add(typeLabel, "growx, wrap");

        // Participants count
        int count = appt.getParticipants() != null ? appt.getParticipants().size() : 0;
        JLabel participantsLabel = new JLabel(count + " participant(s)");
        participantsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        participantsLabel.setForeground(isDarkTheme ? new Color(180, 180, 180) : new Color(120, 120, 120));
        card.add(participantsLabel, "growx");

        // Add hover and click effects
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(isDarkTheme ? new Color(60, 60, 60) : new Color(235, 235, 235));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(bgColor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showAppointmentDetailsPopup(appt);
            }
        });

        return card;
    }

    private Color getTypeColor(String type, boolean isDarkTheme) {
        if (isDarkTheme) {
            switch (type) {
                case "Academic Consultation": return new Color(59, 130, 246);  // Bright blue
                case "Career Guidance": return new Color(147, 51, 234);       // Purple
                case "Personal Counseling": return new Color(16, 185, 129);   // Green
                case "Behavioral Counseling": return new Color(202, 138, 4);  // Orange
                case "Group Counseling": return new Color(239, 68, 68);       // Red
                default: return new Color(156, 163, 175);                     // Gray
            }
        } else {
            switch (type) {
                case "Academic Consultation": return new Color(37, 99, 235);   // Slightly darker blue
                case "Career Guidance": return new Color(126, 34, 206);       // Darker purple
                case "Personal Counseling": return new Color(5, 150, 105);    // Darker green
                case "Behavioral Counseling": return new Color(180, 120, 10); // Darker orange
                case "Group Counseling": return new Color(220, 38, 38);       // Darker red
                default: return new Color(107, 114, 128);                     // Darker gray
            }
        }
    }

    private void showAppointmentDetailsPopup(Appointment appt) {
        // Prevent multiple instances of the same modal
        if (ModalDialog.isIdExist("appointment_details_" + appt.getAppointmentId())) {
            return;
        }

        try {
            // Create and load the appointment details panel
            AppointmentDayDetails detailsPanel = new AppointmentDayDetails(connection, null, null); // Pass null since we don't need selection here
            detailsPanel.loadAppointmentDetails(appt);

            // Configure modal options to match the desired style
            ModalDialog.getDefaultOption()
                    .setOpacity(0f) // Transparent background
                    .setAnimationOnClose(false) // No close animation
                    .getBorderOption()
                    .setBorderWidth(0.5f) // Thin border
                    .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM); // Medium shadow

            // Show the modal dialog with a "Close" button
            ModalDialog.showModal(this,
                    new SimpleModalBorder(detailsPanel, "Appointment Details",
                            new SimpleModalBorder.Option[] {
                                    new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                            },
                            (controller, action) -> {
                                if (action == SimpleModalBorder.CLOSE_OPTION) {
                                    controller.close();
                                }
                            }),
                    "appointment_details_" + appt.getAppointmentId());

            // Set a larger size to accommodate the detailed layout
            ModalDialog.getDefaultOption().getLayoutOption().setSize(700, this.getHeight() - 50);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening appointment details: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    
private void loadAppointments(LocalDate date) {
    SwingUtilities.invokeLater(() -> {
        appointmentsPanel.removeAll();
        List<Appointment> loadedAppointments = null;
        try {
            loadedAppointments = appointmentDAO.getAppointmentsForDate(date);
            // Filter out ended appointments
            if (loadedAppointments != null) {
                loadedAppointments = loadedAppointments.stream()
                    .filter(appt -> !"Ended".equals(appt.getAppointmentStatus()))
                    .toList();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        appointments.clear();
        if (loadedAppointments != null) {
            appointments.addAll(loadedAppointments);
        }

        // Add a header for the date
        JLabel dateHeaderLabel = new JLabel(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        appointmentsPanel.add(dateHeaderLabel, "growx, gapbottom 10, wrap");

        if (appointments.isEmpty()) {
            JLabel noAppointmentsLabel = new JLabel("No appointments for this date", SwingConstants.CENTER);
            noAppointmentsLabel.setForeground(UIManager.getColor("Label.foreground"));
            appointmentsPanel.add(noAppointmentsLabel, "growx, gaptop 10");
        } else {
            // Sort appointments by time
            appointments.sort((a, b) -> a.getAppointmentDateTime().compareTo(b.getAppointmentDateTime()));
            
            // Add each appointment card with proper spacing to ensure full visibility
            for (Appointment appt : appointments) {
                JPanel card = createAppointmentCard(appt);
                appointmentsPanel.add(card, "growx, gapbottom 10, wrap");
            }
        }

        appointmentsPanel.revalidate();
        appointmentsPanel.repaint();
    });
}


    }
