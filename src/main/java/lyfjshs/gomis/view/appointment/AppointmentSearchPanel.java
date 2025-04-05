package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class AppointmentSearchPanel extends JPanel {
    private GTable appointmentTable;
    private Connection connect;
    private DatePicker datePicker;
    private FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.5f);
    private FlatSVGIcon editIcon = new FlatSVGIcon("icons/edit.svg", 0.5f);
    private FlatSVGIcon deleteIcon = new FlatSVGIcon("icons/delete.svg", 0.5f);
    
    // Simplified UI components
    private JTextField participantFilter;
    private JButton newAppointmentButton;
    private JLabel currentMonthLabel;
    private JButton clearDateButton;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color PRIMARY_DARK = new Color(0, 77, 153);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color LIGHT_COLOR = new Color(248, 249, 250);
    private static final Color DARK_COLOR = new Color(52, 58, 64);
    
    public AppointmentSearchPanel(Connection connect) {
        this.connect = connect;
        setLayout(new BorderLayout());
        
        // Create main container panel
        JPanel containerPanel = new JPanel(new MigLayout("insets 20", "[grow]", "[]"));
        containerPanel.setBackground(Color.WHITE);
        
        // Initialize table first
        initializeTable();
        
        // Add components to container
        containerPanel.add(createSearchControlsPanel(), "grow, wrap");
        containerPanel.add(createParticipantSearchField(), "grow, wrap");
        
        // Create table scroll pane
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        containerPanel.add(scrollPane, "grow, wrap");
        
        // Add modal footer
        containerPanel.add(createModalFooter(), "grow");

        // Add container to main panel
        add(containerPanel, BorderLayout.CENTER);
        
        // Setup event listeners
        setupEventListeners();

        // Initial load of appointments
        SwingUtilities.invokeLater(this::loadCurrentMonthAppointments);
    }

    private JPanel createModalFooter() {
        JPanel footerPanel = new JPanel(new MigLayout("insets 15 0 0 0", "[push][]10[]", "[]"));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(238, 238, 238)));

        JButton cancelButton = new JButton("Cancel");
        styleSecondaryButton(cancelButton);
        
        JButton selectButton = new JButton("Select");
        stylePrimaryButton(selectButton);

        footerPanel.add(cancelButton);
        footerPanel.add(selectButton);

        return footerPanel;
    }

    private void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private void styleSecondaryButton(JButton button) {
        button.setBackground(LIGHT_COLOR);
        button.setForeground(DARK_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(221, 221, 221)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(238, 238, 238)));
        
        // Title with icon
        JLabel titleLabel = new JLabel(" Search Appointments");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setIcon(new FlatSVGIcon("icons/search.svg", 0.8f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Close button
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        closeButton.setForeground(new Color(102, 102, 102));
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            // Close action
            Component parent = SwingUtilities.getWindowAncestor(this);
            if (parent != null) {
                parent.setVisible(false);
            }
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createSearchControlsPanel() {
        JPanel searchControlsPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        searchControlsPanel.setBackground(Color.WHITE);
        
        // Create date filter panel
        JPanel dateFilterPanel = new JPanel(new MigLayout("insets 0", "[]15[]", "[]"));
        dateFilterPanel.setBackground(Color.WHITE);
        
        // Month selector
        JPanel monthSelector = new JPanel(new MigLayout("insets 0", "[]5[]", "[]"));
        monthSelector.setBackground(Color.WHITE);
        
        JLabel monthLabel = new JLabel("Current Month:");
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        monthLabel.setForeground(DARK_COLOR);
        
        currentMonthLabel = new JLabel();
        currentMonthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        currentMonthLabel.setForeground(PRIMARY_COLOR);
        
        // Set current month
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        currentMonthLabel.setText(now.format(formatter).toUpperCase());
        
        monthSelector.add(monthLabel);
        monthSelector.add(currentMonthLabel);
        
        // Date picker
        JPanel datePickerPanel = new JPanel(new MigLayout("insets 0", "[]5[]5[]", "[]"));
        datePickerPanel.setBackground(Color.WHITE);
        
        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JFormattedTextField dateEditor = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        dateEditor.setColumns(12);
        dateEditor.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(221, 221, 221)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setCloseAfterSelected(true);
        datePicker.setEditor(dateEditor);
        
        clearDateButton = new JButton("×");
        clearDateButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        clearDateButton.setForeground(new Color(102, 102, 102));
        clearDateButton.setBackground(LIGHT_COLOR);
        clearDateButton.setBorderPainted(false);
        clearDateButton.setFocusPainted(false);
        clearDateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearDateButton.addActionListener(e -> {
            datePicker.clearSelectedDate();
            loadCurrentMonthAppointments();
        });
        
        datePickerPanel.add(dateLabel);
        datePickerPanel.add(dateEditor);
        datePickerPanel.add(clearDateButton);
        
        // Add components to date filter panel
        dateFilterPanel.add(monthSelector);
        dateFilterPanel.add(datePickerPanel);
        
        // New appointment button
        newAppointmentButton = new JButton("New Appointment");
        newAppointmentButton.setIcon(new FlatSVGIcon("icons/add.svg", 0.5f));
        stylePrimaryButton(newAppointmentButton);
        newAppointmentButton.setBackground(SUCCESS_COLOR);
        newAppointmentButton.addActionListener(e -> {
            // Open new appointment modal
            try {
                AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
                // Create a new empty appointment object
                Appointment newAppointment = new Appointment();
                // Set initial values if needed
                newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());
                newAppointment.setAppointmentStatus("Scheduled");

                AddAppointmentModal.getInstance().showModal(
                    connect, 
                    this, 
                    new AddAppointmentPanel(newAppointment, appointmentDAO, connect),
                    appointmentDAO,
                    800, 
                    600, 
                    this::loadCurrentMonthAppointments
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error opening appointment form: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Add components to search controls panel
        searchControlsPanel.add(dateFilterPanel, "cell 0 0");
        searchControlsPanel.add(newAppointmentButton, "cell 1 0");
        
        return searchControlsPanel;
    }

    private JPanel createParticipantSearchField() {
        JPanel participantSearchPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]5[]"));
        participantSearchPanel.setBackground(Color.WHITE);
        
        JLabel label = new JLabel("Search Participant");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(DARK_COLOR);
        label.setIcon(new FlatSVGIcon("icons/search.svg", 0.4f));
        
        participantFilter = new JTextField();
        participantFilter.setToolTipText("Search by participant name, ID or contact information");
        styleTextField(participantFilter);
        participantFilter.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Type name or ID to search...");
        
        // Add listener to filter on typing
        participantFilter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                performSearch();
            }
        });
        
        participantSearchPanel.add(label, "wrap");
        participantSearchPanel.add(participantFilter, "growx");
        
        return participantSearchPanel;
    }

    private void styleTextField(JTextField textField) {
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(221, 221, 221)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "Title", "Consultation Type", "Participants", "Status", "Date/Time", "Counselor", "Actions"};
        Class<?>[] columnTypes = {Integer.class, String.class, String.class, Integer.class, String.class, String.class, String.class, Object.class};
        boolean[] editableColumns = {false, false, false, false, false, false, false, true};
        double[] columnWidths = {0.05, 0.15, 0.15, 0.10, 0.15, 0.15, 0.15, 0.10};
        int[] alignments = {
            SwingConstants.CENTER, // ID
            SwingConstants.LEFT,   // Title
            SwingConstants.LEFT,   // Consultation Type
            SwingConstants.CENTER, // Participants
            SwingConstants.CENTER, // Status
            SwingConstants.CENTER, // Date/Time
            SwingConstants.LEFT,   // Counselor
            SwingConstants.CENTER  // Actions
        };

        // Create action manager with styled buttons
        TableActionManager actionManager = new DefaultTableActionManager();
        ((DefaultTableActionManager)actionManager).addAction("View", (table, row) -> {
            Integer appointmentId = (Integer) table.getValueAt(row, 0);
            viewAppointment(appointmentId);
        }, INFO_COLOR, viewIcon);
        
        ((DefaultTableActionManager)actionManager).addAction("Edit", (table, row) -> {
            Integer appointmentId = (Integer) table.getValueAt(row, 0);
            // Edit action would be implemented here
            JOptionPane.showMessageDialog(this, "Edit appointment " + appointmentId);
        }, WARNING_COLOR, editIcon);
        
        ((DefaultTableActionManager)actionManager).addAction("Delete", (table, row) -> {
            Integer appointmentId = (Integer) table.getValueAt(row, 0);
            // Delete action would be implemented here
            JOptionPane.showMessageDialog(this, "Delete appointment " + appointmentId);
        }, DANGER_COLOR, deleteIcon);

        appointmentTable = new GTable(
            new Object[0][8], 
            columnNames, 
            columnTypes, 
            editableColumns, 
            columnWidths, 
            alignments,
            false,      // no checkbox
            actionManager
        );
        
        // Style the table
        appointmentTable.putClientProperty(FlatClientProperties.STYLE,
            "showHorizontalLines:true;" +
            "intercellSpacing:0,1;" +
            "cellFocusColor:$TableHeader.hoverBackground;" +
            "selectionBackground:$Table.selectionBackground;" +
            "selectionForeground:$Table.selectionForeground");
            
        appointmentTable.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
            "height:30;" +
            "hoverBackground:$TableHeader.hoverBackground;" +
            "pressedBackground:$TableHeader.pressedBackground;" +
            "separatorColor:$TableHeader.separatorColor;" +
            "font:bold");
    }

    private void setupEventListeners() {
        // Fix date selection listener
        datePicker.addDateSelectionListener(e -> {
            if (datePicker.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
                performSearch();
            }
        });
    }

    private void performSearch() {
        try {
            // Initialize the DAO with the database connection
            AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
            
            // Get the counselor ID from the current session
            int counselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
            
            // Get the selected date from the date picker
            LocalDate selectedDate = datePicker.getSelectedDate();
            
            final String participantFilterText = participantFilter.getText().trim();
            final String participant = participantFilterText.isEmpty() ? null : participantFilterText;
            
            // Check if we have a date selection
            if (selectedDate != null) {
                // Fetch appointments for the selected date
                List<Appointment> appointments = appointmentDAO.searchAppointments(
                    null,          // title (not filtering by title)
                    null,          // consultationType (removed)
                    selectedDate,  // startDate
                    selectedDate,  // endDate
                    null,          // status (removed)
                    counselorId    // filter by the current counselor
                );

                // Filter by participant if specified
                if (participant != null) {
                    appointments = appointments.stream()
                        .filter(appt -> {
                            if (appt.getParticipants() == null || appt.getParticipants().isEmpty()) {
                                return false;
                            }
                            return appt.getParticipants().stream()
                                .anyMatch(p -> 
                                    (p.getParticipantFirstName() + " " + p.getParticipantLastName())
                                        .toLowerCase()
                                        .contains(participant.toLowerCase()) ||
                                    (p.getParticipantId() + "").contains(participant)
                                );
                        })
                        .toList();
                }
                
                // Update the table with the filtered appointments
                updateAppointmentTableWithCounselor(appointments);
            } else {
                // If no date selected, load current month appointments filtered by participant
                loadCurrentMonthAppointments(participant);
            }
        } catch (Exception e) {
            // Handle any errors that occur during the search
            JOptionPane.showMessageDialog(this, 
                "Error searching appointments: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCurrentMonthAppointments() {
        loadCurrentMonthAppointments(null);
    }
    
    private void loadCurrentMonthAppointments(String participantFilter) {
        try {
            AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
            int counselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
            
            // Get first and last day of current month
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate firstDay = now.withDayOfMonth(1);
            java.time.LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
            
            // Get all appointments for current month
            List<Appointment> appointments = appointmentDAO.searchAppointments(
                null,  // title
                null,  // consultationType
                firstDay, 
                lastDay, 
                null,  // status
                counselorId
            );
            
            // Filter by participant if specified
            if (participantFilter != null && !participantFilter.isEmpty()) {
                final String filter = participantFilter.toLowerCase();
                appointments = appointments.stream()
                    .filter(appt -> {
                        if (appt.getParticipants() == null || appt.getParticipants().isEmpty()) {
                            return false;
                        }
                        return appt.getParticipants().stream()
                            .anyMatch(p -> 
                                (p.getParticipantFirstName() + " " + p.getParticipantLastName())
                                    .toLowerCase()
                                    .contains(filter) ||
                                (p.getParticipantId() + "").contains(filter)
                            );
                    })
                    .toList();
            }
            
            // Update table with found appointments
            updateAppointmentTableWithCounselor(appointments);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading appointments: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAppointmentTableWithCounselor(List<Appointment> appointments) {
        appointmentTable.clearData();

        AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
        SessionsDAO sessionsDAO = new SessionsDAO(connect);

        for (Appointment appointment : appointments) {
            try {
                // Get fresh appointment data with participants
                Appointment fullAppointment = appointmentDAO.getAppointmentById(appointment.getAppointmentId());
                if (fullAppointment == null) continue;

                // Get counselor info
                GuidanceCounselor counselor = null;
                if (fullAppointment.getGuidanceCounselorId() != null) {
                    counselor = sessionsDAO.getCounselorById(fullAppointment.getGuidanceCounselorId());
                }

                // Format date/time
                String dateTimeStr = fullAppointment.getAppointmentDateTime() != null ?
                    new SimpleDateFormat("MMM dd, yyyy hh:mm a")
                        .format(fullAppointment.getAppointmentDateTime()) : "N/A";

                // Format counselor name
                String counselorName = counselor != null ? 
                    counselor.getFirstName() + " " + counselor.getLastName() : "Not Assigned";

                // Ensure we have participants list
                List<Participants> participants = fullAppointment.getParticipants();
                int participantCount = participants != null ? participants.size() : 0;

                // Create status badge
                String status = fullAppointment.getAppointmentStatus();
                String statusHtml = createStatusBadge(status);

                appointmentTable.addRow(new Object[] {
                    fullAppointment.getAppointmentId(),
                    fullAppointment.getAppointmentTitle(),
                    fullAppointment.getConsultationType(),
                    participantCount,
                    statusHtml,
                    dateTimeStr,
                    counselorName,
                    null // Actions column handled by TableActionManager
                });
            } catch (Exception e) {
                System.err.println("Error processing appointment ID " + appointment.getAppointmentId() + ": " + e.getMessage());
            }
        }
    }
    
    private String createStatusBadge(String status) {
        if (status == null) return "";
        
        String bgColor;
        String textColor;
        
        switch (status.toLowerCase()) {
            case "scheduled":
                bgColor = "#e3f2fd";
                textColor = "#0d47a1";
                break;
            case "completed":
                bgColor = "#e8f5e9";
                textColor = "#1b5e20";
                break;
            case "cancelled":
                bgColor = "#ffebee";
                textColor = "#b71c1c";
                break;
            case "pending":
                bgColor = "#fff8e1";
                textColor = "#f57f17";
                break;
            default:
                bgColor = "#f8f9fa";
                textColor = "#333333";
        }
        
        return "<html><span style='padding: 5px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; background-color: " + 
               bgColor + "; color: " + textColor + ";'>" + status + "</span></html>";
    }

    public Integer getSelectedAppointmentId() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow != -1) {
            return (Integer) appointmentTable.getValueAt(selectedRow, 0);
        }
        return null;
    }

    public String getSelectedConsultationType() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow != -1) {
            return (String) appointmentTable.getValueAt(selectedRow, 2);
        }
        return null;
    }
    
    /**
     * Displays the details of the selected appointment
     * @param appointmentId The ID of the appointment to view
     */
    private void viewAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            JOptionPane.showMessageDialog(this, 
                "No appointment selected.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Get the appointment details
            AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
            Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
            
            if (appointment == null) {
                JOptionPane.showMessageDialog(this, 
                    "Appointment not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get counselor info
            SessionsDAO sessionsDAO = new SessionsDAO(connect);
            GuidanceCounselor counselor = null;
            if (appointment.getGuidanceCounselorId() != null) {
                counselor = sessionsDAO.getCounselorById(appointment.getGuidanceCounselorId());
            }
            
            // Format date/time
            String dateTimeStr = appointment.getAppointmentDateTime() != null ?
                new SimpleDateFormat("MMM dd, yyyy hh:mm a")
                    .format(appointment.getAppointmentDateTime()) : "N/A";
            
            // Format counselor name
            String counselorName = counselor != null ? 
                counselor.getFirstName() + " " + counselor.getLastName() : "Not Assigned";
            
            // Build participant list
            StringBuilder participantList = new StringBuilder();
            if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
                for (Participants participant : appointment.getParticipants()) {
                    participantList.append(participant.getParticipantFirstName())
                                 .append(" ")
                                 .append(participant.getParticipantLastName())
                                 .append(" (")
                                 .append(participant.getParticipantType())
                                 .append(")\n");
                }
            } else {
                participantList.append("No participants");
            }
            
            // Create and show the details dialog
            String message = String.format(
                "<html><body style='width: 400px;'>" +
                "<h2 style='color: #0066cc;'>Appointment Details</h2>" +
                "<p><b>ID:</b> %d</p>" +
                "<p><b>Title:</b> %s</p>" +
                "<p><b>Consultation Type:</b> %s</p>" +
                "<p><b>Status:</b> %s</p>" +
                "<p><b>Date/Time:</b> %s</p>" +
                "<p><b>Counselor:</b> %s</p>" +
                "<p><b>Participants:</b><br>%s</p>" +
                "<p><b>Notes:</b><br>%s</p>" +
                "</body></html>",
                appointment.getAppointmentId(),
                appointment.getAppointmentTitle(),
                appointment.getConsultationType(),
                createStatusBadge(appointment.getAppointmentStatus()),
                dateTimeStr,
                counselorName,
                participantList.toString().replace("\n", "<br>"),
                appointment.getAppointmentNotes() != null ? appointment.getAppointmentNotes().replace("\n", "<br>") : "No notes"
            );
            
            JOptionPane.showMessageDialog(this, 
                message, 
                "Appointment Details", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error viewing appointment: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
