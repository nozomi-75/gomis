package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class AppointmentDayDetails extends JPanel {
    private Connection connection;
    private JPanel bodyPanel;
    private Consumer<Participants> onParticipantSelect;
    private Consumer<Appointment> onRedirectToSession;
    private Appointment currentAppointment;
    private Consumer<Appointment> onEditAppointment;
    private AppointmentDAO appointmentDAO;

    // Editable components for appointment details
    private JTextField titleField;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> statusComboBox;
    private JTextArea notesArea;

    // Replace JTextField declarations with DatePicker and TimePicker
    private DatePicker datePicker;
    private TimePicker timePicker;

    // Add JFormattedTextField declarations
    private JFormattedTextField dateField;
    private JFormattedTextField timeField;

    private boolean editMode = false;
    private JButton editButton;
    private JButton updateButton;

    /**
     * @wbp.parser.constructor
     */
    public AppointmentDayDetails(Connection connection, Consumer<Participants> onParticipantSelect,
            Consumer<Appointment> onRedirectToSession) {
        this(connection, onParticipantSelect, onRedirectToSession, null);
    }

    public AppointmentDayDetails(Connection connection, Consumer<Participants> onParticipantSelect,
            Consumer<Appointment> onRedirectToSession, Consumer<Appointment> onEditAppointment) {
        this.connection = connection;
        this.onParticipantSelect = onParticipantSelect;
        this.onRedirectToSession = onRedirectToSession;
        this.onEditAppointment = onEditAppointment;
        this.appointmentDAO = new AppointmentDAO(connection);

        setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));
        bodyPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
        add(bodyPanel, "grow");

        // Show empty state initially
        JLabel emptyLabel = new JLabel("No appointment selected");
        emptyLabel.setHorizontalAlignment(JLabel.CENTER);
        bodyPanel.add(emptyLabel, "center");

        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    public void loadAppointmentsForDate(LocalDate date) throws SQLException {
        List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(date);
        bodyPanel.removeAll();

        if (appointments != null && !appointments.isEmpty()) {
            // Take the first appointment as current
            this.currentAppointment = appointments.get(0);
            displayAppointmentDetails(currentAppointment);
        } else {
            // Show empty state
            JLabel emptyLabel = new JLabel("No appointments for this date");
            emptyLabel.setHorizontalAlignment(JLabel.CENTER);
            bodyPanel.add(emptyLabel, "center");
        }

        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    public Appointment getCurrentAppointment() {
        return currentAppointment;
    }

    public void loadAppointmentDetails(Appointment appointment) {
        this.currentAppointment = appointment;
        bodyPanel.removeAll();
        displayAppointmentDetails(appointment);
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    /**
     * Displays appointment details using editable fields for title, consultation
     * type,
     * status, and notes. Date and time are shown as non-editable fields.
     */
    private void displayAppointmentDetails(Appointment appointment) {
        // Initialize timePicker and datePicker at the start
        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setDateFormat("yyyy-MM-dd");
        
        timePicker = new TimePicker();

        // Main container with smooth scrolling
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        scrollPane.setBorder(null);

        // Content panel
        JPanel contentPanel = new JPanel(new MigLayout("wrap 2, fillx", "[grow][grow]", "[]"));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        if (appointment == null) {
            // Show empty state
            JLabel emptyLabel = new JLabel("No active appointments for this date");
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 14));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(emptyLabel, "span 2, align center");
        } else {
            // Appointment Title
            JLabel titleLabel = new JLabel("Appointment Title");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
            titleField = new JTextField(appointment.getAppointmentTitle());
            titleField.setEditable(false);
            contentPanel.add(titleLabel, "growx");
            contentPanel.add(titleField, "growx");

            // Consultation Type
            JLabel typeLabel = new JLabel("Consultation Type");
            typeLabel.setFont(new Font("Arial", Font.BOLD, 12));
            String[] consultationTypes = {
                    "Academic Consultation",
                    "Career Guidance",
                    "Personal Consultation",
                    "Behavioral Consultation",
                    "Group Consultation"
            };
            typeComboBox = new JComboBox<>(consultationTypes);
            typeComboBox.setSelectedItem(appointment.getConsultationType());
            typeComboBox.setEnabled(false);
            contentPanel.add(typeLabel, "growx");
            contentPanel.add(typeComboBox, "growx");

            // Date Field
            JLabel dateLabel = new JLabel("Date");
            dateLabel.setFont(new Font("Arial", Font.BOLD, 12));

            dateField = new JFormattedTextField();
            dateField.setEditable(false);
            datePicker.setEditor(dateField);
            datePicker.setEnabled(false);
            
            // Set a custom DateSelectionAble to prevent selecting past dates
            datePicker.setDateSelectionAble(date -> !date.isBefore(LocalDate.now()));
            
            // Add a date selection listener to validate time when date changes
            datePicker.addDateSelectionListener(event -> {
                validateDate();
            });

            // Set the appointment date
            LocalDate appointmentDate = appointment.getAppointmentDateTime()
                    .toLocalDateTime().toLocalDate();
            datePicker.setSelectedDate(appointmentDate);

            contentPanel.add(dateLabel, "growx");
            contentPanel.add(dateField, "growx");

            // Time Field
            JLabel timeLabel = new JLabel("Time");
            timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
            timeField = new JFormattedTextField();
            timeField.setEditable(false);
            timePicker.setEditor(timeField);
            timePicker.setEnabled(false);
            
            // Add a time selection listener to validate time when it changes
            timePicker.addTimeSelectionListener(event -> {
                validateDate();
            });

            // Set the appointment time
            LocalTime appointmentTime = appointment.getAppointmentDateTime()
                    .toLocalDateTime().toLocalTime();
            timePicker.setSelectedTime(appointmentTime);

            contentPanel.add(timeLabel, "growx");
            contentPanel.add(timeField, "growx");

            // Status
            JLabel statusLabel = new JLabel("Status");
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            String[] statuses = { "On-going", "Ended", "Rescheduled", "Cancelled" };
            statusComboBox = new JComboBox<>(statuses);
            statusComboBox.setSelectedItem(appointment.getAppointmentStatus());
            statusComboBox.setEnabled(false);
            contentPanel.add(statusLabel, "growx");
            contentPanel.add(statusComboBox, "growx");

            // Participants Section
            if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
                JPanel participantsHeader = createSectionHeader("Participants", null);
                contentPanel.add(participantsHeader, "span, growx");

                // Participants table
                String[] columnNames = { "Name", "Participant Type", "Contact Number", "Gender" };
                DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                for (Participants participant : appointment.getParticipants()) {
                    model.addRow(new Object[] {
                            participant.getParticipantFirstName() + " " + participant.getParticipantLastName(),
                            participant.getParticipantType(),
                            participant.getContactNumber(),
                            participant.getSex()
                    });
                }

                JTable participantsTable = new JTable(model);
                participantsTable.setRowHeight(40);
                participantsTable.setShowGrid(true);
                participantsTable.setGridColor(new Color(221, 221, 221));
                styleTable(participantsTable);
                JScrollPane tableScrollPane = new JScrollPane(participantsTable);
                contentPanel.add(tableScrollPane, "span, growx");
            }

            // Notes Section
            JPanel notesHeader = createSectionHeader("Notes", null);
            contentPanel.add(notesHeader, "span, growx");

            notesArea = new JTextArea(
                    appointment.getAppointmentNotes() != null ? appointment.getAppointmentNotes() : "");
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);
            notesArea.setRows(5);
            notesArea.setEditable(false);
            JScrollPane notesScrollPane = new JScrollPane(notesArea);
            contentPanel.add(notesScrollPane, "span, growx");
            
            // Buttons panel
            JPanel buttonsPanel = new JPanel(new MigLayout("fillx", "[grow,fill]", "[]"));
            editButton = new JButton("Edit");
            editButton.addActionListener(e -> toggleEditMode());

            updateButton = new JButton("Save Changes");
            updateButton.setVisible(false);
            updateButton.addActionListener(e -> saveChanges(appointment));

            JButton cancelButton = new JButton("Cancel Appointment");
            cancelButton.addActionListener(e -> handleCancelAppointment(appointment));

            // Only show cancel button if appointment is not already cancelled
            if (!"Cancelled".equals(appointment.getAppointmentStatus())) {
                buttonsPanel.add(editButton, "growx");
                buttonsPanel.add(updateButton, "growx, gapleft 15");
                buttonsPanel.add(cancelButton, "growx, gapleft 15");
            }
            contentPanel.add(buttonsPanel, "span, growx, wrap");
        }

        // Add content panel to scroll pane and then to bodyPanel
        scrollPane.setViewportView(contentPanel);
        bodyPanel.removeAll();
        bodyPanel.add(scrollPane, "grow");
        bodyPanel.revalidate();
        bodyPanel.repaint();
    }

    private void handleCancelAppointment(Appointment appointment) {
        try {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel this appointment?",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Update the appointment status to "Cancelled"
                    appointment.setAppointmentStatus("Cancelled");
                    if (appointmentDAO.updateAppointment(appointment)) {
                        JOptionPane.showMessageDialog(this,
                                "Appointment cancelled successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        
                        // Update the status in the UI
                        if (statusComboBox != null) {
                            statusComboBox.setSelectedItem("Cancelled");
                        }
                        
                        // Notify any listeners about the change
                        if (onEditAppointment != null) {
                            onEditAppointment.accept(appointment);
                        }
                    }
                } catch (SQLException ex) {
                    String message = "Error cancelling appointment: " + ex.getMessage();
                    JOptionPane.showMessageDialog(this,
                            message,
                            "Cannot Cancel Appointment",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleEditMode() {
        editMode = !editMode;

        // Enable/disable all editable fields if they are initialized
        if (titleField != null) titleField.setEditable(editMode);
        if (typeComboBox != null) typeComboBox.setEnabled(editMode);
        if (statusComboBox != null) statusComboBox.setEnabled(editMode);
        if (notesArea != null) notesArea.setEditable(editMode);

        // Handle date and time pickers if they are initialized
        if (datePicker != null) {
            datePicker.setEnabled(editMode);
            // Update date selection validation when toggling edit mode
            datePicker.setDateSelectionAble(date -> !date.isBefore(LocalDate.now()));
        }
        if (dateField != null) dateField.setEditable(editMode);
        if (timePicker != null) {
            timePicker.setEnabled(editMode);
        }
        if (timeField != null) timeField.setEditable(editMode);

        if (!editMode && currentAppointment != null) {
            // Restore original values when canceling edit mode
            LocalDateTime originalDateTime = currentAppointment.getAppointmentDateTime().toLocalDateTime();
            if (datePicker != null) datePicker.setSelectedDate(originalDateTime.toLocalDate());
            if (timePicker != null) timePicker.setSelectedTime(originalDateTime.toLocalTime());
        }

        // Toggle button visibility
        if (editButton != null) editButton.setVisible(!editMode);
        if (updateButton != null) updateButton.setVisible(editMode);
    }

    private void validateDate() {
        LocalDate selectedDate = datePicker.getSelectedDate();
        LocalTime selectedTime = timePicker.getSelectedTime();
        
        if (selectedDate != null && selectedTime != null) {
            LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);
            LocalDateTime now = LocalDateTime.now();
            
            if (selectedDateTime.isBefore(now)) {
                // If selected date is today, set time to current time + 1 hour
                if (selectedDate.equals(LocalDate.now())) {
                    LocalTime newTime = LocalTime.now().plusHours(1);
                    timePicker.setSelectedTime(newTime);
                    JOptionPane.showMessageDialog(this,
                            "Cannot select a time in the past. Time has been adjusted to " + newTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                            "Invalid Time",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    // If selected date is in the past, set to today
                    datePicker.setSelectedDate(LocalDate.now());
                    JOptionPane.showMessageDialog(this,
                            "Cannot select a date in the past. Date has been adjusted to today.",
                            "Invalid Date",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void saveChanges(Appointment appointment) {
        // Validate date and time
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

        LocalDateTime newDateTime = LocalDateTime.of(selectedDate, selectedTime);
        if (newDateTime.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                    "Cannot set appointment date/time in the past.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Update appointment object
        appointment.setAppointmentDateTime(Timestamp.valueOf(newDateTime));
        // ...rest of existing save logic...
        appointment.setAppointmentTitle(titleField.getText());
        appointment.setConsultationType((String) typeComboBox.getSelectedItem());
        appointment.setAppointmentStatus((String) statusComboBox.getSelectedItem());
        appointment.setAppointmentNotes(notesArea.getText());

        try {
            if (appointmentDAO.updateAppointment(appointment)) {
                JOptionPane.showMessageDialog(this,
                        "Appointment updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                if (onEditAppointment != null) {
                    onEditAppointment.accept(appointment);
                }
                // Switch back to view mode
                toggleEditMode();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating appointment: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createSectionHeader(String title, String badge) {
        JPanel headerPanel = new JPanel(new MigLayout("insets 12 15, fillx"));
        headerPanel.putClientProperty("FlatLaf.style", "arc: 8");
        headerPanel.setBackground(UIManager.getColor("Button.default.startBackground"));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));

        headerPanel.add(titleLabel, "grow, pushx");

        if (badge != null) {
            JLabel badgeLabel = new JLabel(badge);
            badgeLabel.setForeground(UIManager.getColor("Label.foreground"));
            Color bgColor = UIManager.getColor("Button.default.startBackground");
            if (bgColor != null) {
                badgeLabel.setBackground(bgColor.darker());
            } else {
                badgeLabel.setBackground(new Color(60, 60, 60));
            }
            badgeLabel.setOpaque(true);
            badgeLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            headerPanel.add(badgeLabel);
        }

        return headerPanel;
    }

    private void styleTable(JTable table) {
        // Custom header renderer
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                label.setBackground(new Color(241, 243, 245));
                label.setForeground(new Color(44, 62, 80));
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                return label;
            }
        });

        // Custom cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 221, 221)),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                return label;
            }
        });
    }
}
