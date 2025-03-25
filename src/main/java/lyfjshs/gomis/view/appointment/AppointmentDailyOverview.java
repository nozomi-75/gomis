package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.toedter.calendar.JDateChooser;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class AppointmentDailyOverview extends JPanel {
    private LocalDate selectedDate;
    private JPanel appointmentsPanel;
    private JDateChooser dateChooser;
    private final AppointmentDAO appointmentDAO;
    private final Connection connection;

    public AppointmentDailyOverview(AppointmentDAO appointmentDAO, Connection connection) {
        this.appointmentDAO = appointmentDAO;
        this.connection = connection;
        setLayout(new BorderLayout());
        selectedDate = LocalDate.now();
        initUI();
        updateAppointmentsDisplay();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new MigLayout("wrap, fill", "[grow]", "[][][grow]"));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel titleLabel = new JLabel("Daily Appointments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel subtitleLabel = new JLabel("View and manage your schedule");
        subtitleLabel.setForeground(Color.GRAY);

        JPanel headerPanel = new JPanel(new MigLayout("wrap 2", "[][right]", ""));
        dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setDate(java.sql.Date.valueOf(selectedDate));
        dateChooser.addPropertyChangeListener("date", evt -> {
            if (evt.getNewValue() != null) {
                selectedDate = ((java.util.Date) evt.getNewValue()).toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                updateAppointmentsDisplay();
            }
        });

        JButton addButton = new JButton("Add Appointment");
        addButton.setPreferredSize(new Dimension(120, 25));
        addButton.addActionListener(e -> createAppointment());

        headerPanel.add(titleLabel, "span 2");
        headerPanel.add(subtitleLabel, "span 2");
        headerPanel.add(dateChooser);
        headerPanel.add(addButton, "gapy 5");

        // Appointments display
        appointmentsPanel = new JPanel(new MigLayout("wrap 1", "[grow]", ""));
        appointmentsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        mainPanel.add(headerPanel, "growx");
        mainPanel.add(new JSeparator(), "growx, gapy 5");
        mainPanel.add(appointmentsPanel, "grow");

        add(mainPanel);
    }

    public void updateAppointmentsDisplay() {
        appointmentsPanel.removeAll();

        List<Appointment> appointments = null;
        try {
            appointments = appointmentDAO.getAppointmentsForDate(selectedDate);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching appointments: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        if (appointments == null || appointments.isEmpty()) {
            JPanel emptyPanel = new JPanel(new MigLayout("wrap, align center", "[grow]", "[]"));
            emptyPanel.add(new JLabel("No appointments scheduled for this day"));
            JButton addButton = new JButton("Add Your First Appointment");
            addButton.setPreferredSize(new Dimension(150, 25));
            addButton.addActionListener(e -> createAppointment());
            emptyPanel.add(addButton, "gapy 5");
            appointmentsPanel.add(emptyPanel, "growx");
        } else {
            for (Appointment app : appointments) {
                appointmentsPanel.add(createAppointmentCard(app), "growx");
            }
        }

        appointmentsPanel.revalidate();
        appointmentsPanel.repaint();
    }

    private JPanel createAppointmentCard(Appointment app) {
        JPanel card = new JPanel(new MigLayout("wrap 2", "[grow][]", ""));
        
        // Detect if we're in dark mode
        boolean isDarkTheme = card.getBackground() != null && 
                            card.getBackground().getRed() < 128;

        // Set border and background based on theme
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, getTypeColor(app.getConsultationType(), isDarkTheme)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        card.setBackground(getTypeBackground(app.getConsultationType(), isDarkTheme));

        JLabel title = new JLabel(app.getAppointmentTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));

        card.add(title, "span 2");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        LocalDateTime appointmentDateTime = app.getAppointmentDateTime().toLocalDateTime();
        String timeSlot = appointmentDateTime.format(timeFormatter);
        card.add(new JLabel(timeSlot + " • " + app.getAppointmentStatus()) {
            {
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }
        }, "span 2");

        if (app.getGuidanceCounselorId() != null) {
            card.add(new JLabel("Counselor ID: " + app.getGuidanceCounselorId()) {
                {
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
            }, "span 2");
        }

        if (app.getAppointmentNotes() != null && !app.getAppointmentNotes().isEmpty()) {
            String truncatedNotes = app.getAppointmentNotes().length() > 100
                    ? app.getAppointmentNotes().substring(0, 100) + "..."
                    : app.getAppointmentNotes();
            JLabel notesLabel = new JLabel("<html>Notes: " + truncatedNotes + "</html>") {
                {
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    setPreferredSize(new Dimension(300, 50));
                    setVerticalAlignment(SwingConstants.TOP);
                }
            };
            card.add(notesLabel, "span 2");
        }

        // Display participants if available
        if (app.getParticipants() != null && !app.getParticipants().isEmpty()) {
            StringBuilder participantsText = new StringBuilder("Participants: ");
            for (int i = 0; i < app.getParticipants().size(); i++) {
                Participants p = app.getParticipants().get(i);
                participantsText.append(p.getParticipantFirstName()).append(" ").append(p.getParticipantLastName());
                if (i < app.getParticipants().size() - 1) {
                    participantsText.append(", ");
                }
            }
            card.add(new JLabel(participantsText.toString()) {
                {
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
            }, "span 2");
        }

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showAppointmentDetailsPopup(app);
            }
        });

        return card;
    }

private void showAppointmentDetailsPopup(Appointment appointment) {
    // Prevent multiple instances of the same modal
    if (ModalDialog.isIdExist("appointment_details")) {
        return;
    }

    try {
        // Create and load the appointment details panel
        AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(connection, null, null); // Pass null since we don't need selection here
        LocalDate appointmentDate = appointment.getAppointmentDateTime().toLocalDateTime().toLocalDate();
        appointmentDetails.loadAppointmentsForDate(appointmentDate);

        // Configure modal options to match the desired style
        ModalDialog.getDefaultOption()
                .setOpacity(0f) // Transparent background
                .setAnimationOnClose(false) // No close animation
                .getBorderOption()
                .setBorderWidth(0.5f) // Thin border
                .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM); // Medium shadow

        // Show the modal dialog with a "Close" button
        ModalDialog.showModal(this,
                new SimpleModalBorder(appointmentDetails, "Appointment Details",
                        new SimpleModalBorder.Option[] {
                                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                        },
                        (controller, action) -> {
                            if (action == SimpleModalBorder.CLOSE_OPTION) {
                                controller.close();
                            }
                        }),
                "appointment_details");

        // Set the size to match the original dialog
        ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 700);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error opening appointment details: " + e.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    private void createAppointment() {
        try {
            Appointment newAppointment = new Appointment();
            newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now())); // Set default date and time

            // Check if the guidance counselor is logged in
            if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
                newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());
            } else {
                JOptionPane.showMessageDialog(this, "No counselor logged in. Please log in first.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDAO, connection);

            // Use AddAppointmentModal to show the dialog
            AddAppointmentModal.getInstance().showModal(this, addAppointmentPanel, appointmentDAO, 750, 800);

            // Update the current view after the modal is closed
            updateAppointmentsDisplay();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating appointment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private Color getTypeColor(String type, boolean isDarkTheme) {
        if (isDarkTheme) {
            switch (type) {
                case "Academic Consultation":
                    return new Color(59, 130, 246);
                case "Career Guidance":
                    return new Color(147, 51, 234);
                case "Personal Counseling":
                    return new Color(16, 185, 129);
                case "Behavioral Counseling":
                    return new Color(202, 138, 4);
                case "Group Counseling":
                    return new Color(239, 68, 68);
                default:
                    return new Color(156, 163, 175);
            }
        } else {
            switch (type) {
                case "Academic Consultation":
                    return new Color(37, 99, 235);
                case "Career Guidance":
                    return new Color(126, 34, 206);
                case "Personal Counseling":
                    return new Color(5, 150, 105);
                case "Behavioral Counseling":
                    return new Color(180, 120, 10);
                case "Group Counseling":
                    return new Color(220, 38, 38);
                default:
                    return Color.GRAY;
            }
        }
    }

    private Color getTypeBackground(String type, boolean isDarkTheme) {
        if (isDarkTheme) {
            switch (type) {
                case "Academic Consultation":
                    return new Color(30, 58, 138, 100);
                case "Career Guidance":
                    return new Color(91, 33, 182, 100);
                case "Personal Counseling":
                    return new Color(6, 95, 70, 100);
                case "Behavioral Counseling":
                    return new Color(146, 64, 14, 100);
                case "Group Counseling":
                    return new Color(153, 27, 27, 100);
                default:
                    return new Color(55, 65, 81, 100);
            }
        } else {
            // Keep existing light theme colors
            switch (type) {
                case "Academic Consultation":
                    return new Color(219, 234, 254);
                case "Career Guidance":
                    return new Color(233, 213, 255);
                case "Personal Counseling":
                    return new Color(209, 250, 229);
                case "Behavioral Counseling":
                    return new Color(254, 243, 199);
                case "Group Counseling":
                    return new Color(254, 226, 226);
                default:
                    return new Color(243, 244, 246);
            }
        }
    }
}