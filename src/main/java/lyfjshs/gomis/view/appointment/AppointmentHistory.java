package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import lyfjshs.gomis.Database.SQLExceptionPane;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class AppointmentHistory extends JPanel {
    private JPanel appointmentsListPanel;
    private String activeFilter = "all";
    private AppointmentDAO appointmentDAO;
    private Connection connection;
    private Appointment selectedAppointment;
    private JButton deleteButton;
    private JComboBox<String> statusFilterComboBox;

    public AppointmentHistory(AppointmentDAO appointmentDAO, Connection connection) {
        this.appointmentDAO = appointmentDAO;
        this.connection = connection;
        
        setLayout(new MigLayout("fill, wrap 1", "[grow, fill]", "[][][grow]"));
        putClientProperty("FlatLaf.style", "arc: 8");
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][right]", "[]"));
        headerPanel.putClientProperty("FlatLaf.style", "arc: 8");
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIManager.getColor("Component.borderColor")));
        
        JLabel titleLabel = new JLabel("Appointment History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        // Status Filter
        statusFilterComboBox = new JComboBox<>(new String[] { "All", "Scheduled", "In Progress", "Completed", "Missed", "Cancelled", "Draft" });
        statusFilterComboBox.addActionListener(e -> filterAppointments((String) statusFilterComboBox.getSelectedItem()));
        headerPanel.add(statusFilterComboBox, "wrap");

        add(headerPanel, "growx");
        
        // Action buttons panel
        JPanel actionPanel = new JPanel(new MigLayout("fillx, insets 5", "[grow][right]", "[]"));
        deleteButton = new JButton("Delete Selected");
        deleteButton.setIcon(UIManager.getIcon("Actions.Red"));
        deleteButton.putClientProperty("FlatLaf.style", "foreground: #ff3333;");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedAppointment());
        actionPanel.add(deleteButton, "cell 1 0");
        add(actionPanel, "growx");

        // Appointments List Panel
        appointmentsListPanel = new JPanel(new MigLayout("fill, wrap 1, hidemode 3", "[grow]", "[grow]10[grow]"));
        appointmentsListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(appointmentsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "grow");

        // Initial load with all history
        filterAppointments("all");
    }

    private JPanel createAppointmentCard(Appointment appointment) {
        JPanel cardPanel = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[][][grow]"));
        cardPanel.setName(appointment.getAppointmentStatus().toLowerCase());
        cardPanel.putClientProperty("FlatLaf.style", "arc: 8");
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, getStatusColor(appointment.getAppointmentStatus())),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Setup selection behavior
        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Set this as the selected appointment
                selectedAppointment = appointment;
                
                // Highlight the selected card and dehighlight others
                for (Component comp : appointmentsListPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        comp.setBackground(UIManager.getColor("Panel.background"));
                    }
                }
                cardPanel.setBackground(UIManager.getColor("List.selectionBackground"));
                
                // Enable the delete button
                deleteButton.setEnabled(true);

                // Show details on double click
                if (e.getClickCount() == 2) {
                    showAppointmentDetailsDialog(appointment);
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (selectedAppointment != appointment) {
                    cardPanel.setBackground(UIManager.getColor("Table.highlightOuter"));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (selectedAppointment != appointment) {
                    cardPanel.setBackground(UIManager.getColor("Panel.background"));
                }
            }
        });

        // Header with Title and Status
        JPanel headerPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][right]", "[align center]"));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(appointment.getAppointmentTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel);

        JLabel statusLabel = new JLabel(appointment.getAppointmentStatus());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(appointment.getAppointmentStatus()));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(statusLabel);

        // Add View Details button
        JButton viewButton = new JButton("View Details");
        viewButton.putClientProperty("FlatLaf.style", "arc: 8");
        viewButton.addActionListener(e -> showAppointmentDetailsDialog(appointment));
        headerPanel.add(viewButton);

        cardPanel.add(headerPanel, "growx");

        // Details Section
        JPanel detailsPanel = new JPanel(new MigLayout("fill, wrap 2, gap 10", "[grow][grow]", "[][]"));
        detailsPanel.setOpaque(false);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        detailsPanel.add(new JLabel("<html><strong>Consultation Type:</strong> " + 
            appointment.getConsultationType() + "</html>"));
        detailsPanel.add(new JLabel("<html><strong>Date:</strong> " + 
            dateFormat.format(appointment.getAppointmentDateTime()) + "</html>"));
        
        if (appointment.getAppointmentNotes() != null && !appointment.getAppointmentNotes().isEmpty()) {
            JLabel notesLabel = new JLabel("<html><strong>Notes:</strong> " + 
                appointment.getAppointmentNotes() + "</html>");
            notesLabel.setPreferredSize(new Dimension(0, 0));
            detailsPanel.add(notesLabel, "span 2");
        }
        
        cardPanel.add(detailsPanel, "growx");

        return cardPanel;
    }

    private Color getStatusColor(String status) {
        return switch (status.toLowerCase()) {
            case "ended", "completed" -> new Color(40, 167, 69);
            case "cancelled" -> new Color(220, 53, 69);
            case "missed" -> new Color(255, 193, 7);
            case "rescheduled" -> new Color(255, 165, 0); // Orange color for rescheduled
            default -> Color.GRAY;
        };
    }

    private void filterAppointments(String status) {
        try {
            List<Appointment> appointments;
            if ("all".equalsIgnoreCase(status)) {
                appointments = appointmentDAO.getAppointmentsByStatus("all");
            } else {
                appointments = appointmentDAO.getAppointmentsByStatus(status);
            }
            appointmentsListPanel.removeAll();

            if (appointments.isEmpty()) {
                JLabel noAppointmentsLabel = new JLabel(
                    "No appointments found for this filter.", 
                    SwingConstants.CENTER
                );
                noAppointmentsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                appointmentsListPanel.add(noAppointmentsLabel, "grow, push");
            } else {
                for (Appointment appointment : appointments) {
                    appointmentsListPanel.add(createAppointmentCard(appointment), "growx");
                }
            }

            appointmentsListPanel.revalidate();
            appointmentsListPanel.repaint();
            
            // Reset selected appointment
            selectedAppointment = null;
            deleteButton.setEnabled(false);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading appointments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedAppointment() {
        if (selectedAppointment == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this appointment?\nThis cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // First, delete any related sessions
                SessionsDAO sessionsDAO = new SessionsDAO(connection);
                sessionsDAO.deleteSessionsByAppointmentId(selectedAppointment.getAppointmentId());
                
                // Then delete the appointment
                boolean success = appointmentDAO.deleteAppointment(selectedAppointment.getAppointmentId());
                
                if (success) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Appointment deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    // Refresh the view
                    updateAppointmentsDisplay();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to delete appointment.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (SQLException e) {
                SQLExceptionPane.showSQLException(e, "Deleting Appointment");
            }
        }
    }

    public void updateAppointmentsDisplay() {
        filterAppointments(activeFilter);
    }

    private void showAppointmentDetailsDialog(Appointment appointment) {
        // Create the details dialog
        JPanel detailsPanel = new JPanel(new MigLayout("fillx, wrap 1, insets 20", "[grow]", "[]10[]10[]10[]"));
        
        // Title and Status
        JPanel headerPanel = new JPanel(new MigLayout("fillx", "[grow][]", "[]"));
        JLabel titleLabel = new JLabel(appointment.getAppointmentTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel, "grow");
        
        JLabel statusLabel = new JLabel(appointment.getAppointmentStatus());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(appointment.getAppointmentStatus()));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(statusLabel);
        
        detailsPanel.add(headerPanel, "growx");
        
        // Details
        JPanel infoPanel = new JPanel(new MigLayout("fillx, wrap 2", "[][grow]", "[]5[]5[]5[]"));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Appointment Details"));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
        infoPanel.add(new JLabel("Date & Time:"));
        infoPanel.add(new JLabel(dateFormat.format(appointment.getAppointmentDateTime())));
        
        infoPanel.add(new JLabel("Consultation Type:"));
        infoPanel.add(new JLabel(appointment.getConsultationType()));
        
        infoPanel.add(new JLabel("Notes:"), "top");
        JTextArea notesArea = new JTextArea(appointment.getAppointmentNotes());
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setRows(3);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        infoPanel.add(notesScroll, "grow");
        
        detailsPanel.add(infoPanel, "growx");
        
        // Participants
        JPanel participantsPanel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]", "[]5[]"));
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
        
        if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
            for (Participants participant : appointment.getParticipants()) {
                participantsPanel.add(new JLabel(String.format("%s %s (%s)",
                    participant.getParticipantFirstName(),
                    participant.getParticipantLastName(),
                    participant.getParticipantType())));
            }
        } else {
            participantsPanel.add(new JLabel("No participants"));
        }
        
        detailsPanel.add(participantsPanel, "growx");
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new MigLayout("fillx", "[grow][grow][grow]", "[]"));
        
        JButton editButton = new JButton("Edit");
        editButton.putClientProperty("FlatLaf.style", "arc: 8");
        editButton.addActionListener(e -> editAppointment(appointment));
        
        JButton rescheduleButton = new JButton("Reschedule");
        rescheduleButton.putClientProperty("FlatLaf.style", "arc: 8");
        rescheduleButton.addActionListener(e -> rescheduleAppointment(appointment));
        
        JButton closeButton = new JButton("Close");
        closeButton.putClientProperty("FlatLaf.style", "arc: 8");
        
        buttonPanel.add(editButton);
        buttonPanel.add(rescheduleButton);
        buttonPanel.add(closeButton);
        
        detailsPanel.add(buttonPanel, "growx");
        
        // Show dialog
        Object[] options = {};
        JOptionPane optionPane = new JOptionPane(detailsPanel, JOptionPane.PLAIN_MESSAGE, 
            JOptionPane.DEFAULT_OPTION, null, options, null);
        
        javax.swing.JDialog dialog = optionPane.createDialog("Appointment Details");
        dialog.setResizable(true);
        dialog.setSize(600, 500);
        
        // Add action listener to close button
        closeButton.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    }

    private void editAppointment(Appointment appointment) {
        try {
            // Create edit panel using AddAppointmentPanel
            AddAppointmentPanel editPanel = new AddAppointmentPanel(appointment, appointmentDAO, connection);
            
            // Show in modal
            AddAppointmentModal.getInstance().showModal(
                connection,
                this,
                editPanel,
                appointmentDAO,
                800,
                600,
                this::updateAppointmentsDisplay
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error editing appointment: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows a dialog to reschedule an appointment.
     * - Allows selecting any date/time (including past dates)
     * - Validates the date/time only when the OK button is clicked
     * - Warns the user if they select a past date/time but allows proceeding if confirmed
     * 
     * @param appointment The appointment to reschedule
     */
    private void rescheduleAppointment(Appointment appointment) {
        // Create date picker for new date/time
        JPanel reschedulePanel = new JPanel(new MigLayout("wrap 2", "[][grow]", "[][]"));
        
        DatePicker datePicker = new DatePicker();
        // Allow selection of any date, including past dates, validation will happen on save
        datePicker.setDateSelectionAble(date -> true);
        
        TimePicker timePicker = new TimePicker();
        
        // Set current appointment date/time as default
        LocalDateTime currentDateTime = appointment.getAppointmentDateTime().toLocalDateTime();
        datePicker.setSelectedDate(currentDateTime.toLocalDate());
        timePicker.setSelectedTime(currentDateTime.toLocalTime());
        
        reschedulePanel.add(new JLabel("New Date:"));
        reschedulePanel.add(datePicker);
        reschedulePanel.add(new JLabel("New Time:"));
        reschedulePanel.add(timePicker);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            reschedulePanel,
            "Reschedule Appointment",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                // Validate the date and time inputs before processing
                LocalDate selectedDate = datePicker.getSelectedDate();
                LocalTime selectedTime = timePicker.getSelectedTime();
                
                if (selectedDate == null) {
                    JOptionPane.showMessageDialog(this,
                        "Please select a valid date.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                if (selectedTime == null) {
                    JOptionPane.showMessageDialog(this,
                        "Please select a valid time.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Create the new date time and validate it
                LocalDateTime newDateTime = LocalDateTime.of(selectedDate, selectedTime);
                
                // Only validate that it's not in the past when actually saving
                if (newDateTime.isBefore(LocalDateTime.now())) {
                    int confirmPast = JOptionPane.showConfirmDialog(
                        this,
                        "The selected date and time is in the past. Are you sure you want to proceed?",
                        "Confirm Past Date",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    
                    if (confirmPast != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                
                // Update appointment date/time
                appointment.setAppointmentDateTime(Timestamp.valueOf(newDateTime));
                appointment.setAppointmentStatus("Rescheduled");
                
                // Add note about rescheduling
                String rescheduleNote = "\n[Rescheduled] from " + 
                    currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) +
                    " to " + newDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                
                if (appointment.getAppointmentNotes() != null && !appointment.getAppointmentNotes().isEmpty()) {
                    appointment.setAppointmentNotes(appointment.getAppointmentNotes() + rescheduleNote);
                } else {
                    appointment.setAppointmentNotes(rescheduleNote);
                }
                
                // Save changes
                appointmentDAO.updateAppointment(appointment);
                
                // Refresh display
                updateAppointmentsDisplay();
                
                JOptionPane.showMessageDialog(
                    this,
                    "Appointment rescheduled successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error rescheduling appointment: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}