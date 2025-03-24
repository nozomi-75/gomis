package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class AppointmentSearchPanel extends JPanel {
    private GTable appointmentTable;
    private Connection connect;
    private DatePicker datePicker;
    private FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.5f);
    
    public AppointmentSearchPanel(Connection connect) {
        this.connect = connect;
        setLayout(new MigLayout("fill", "[grow]", "[][grow]"));

        // Search panel
        JPanel searchPanel = new JPanel(new MigLayout("insets 10", "[][][grow][][][]", "[]"));
        
        // Add current month label with year
        java.time.LocalDate now = java.time.LocalDate.now();
        JLabel monthLabel = new JLabel(String.format("Current Month: %s %d", 
            now.getMonth().toString(), 
            now.getYear()));
        monthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        searchPanel.add(monthLabel, "cell 1 0");

        // Add DatePicker with proper configuration
        datePicker = new DatePicker();
        datePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        datePicker.setCloseAfterSelected(true);
        datePicker.now(); // Set to current date
        
        JFormattedTextField dateEditor = new JFormattedTextField();
        datePicker.setEditor(dateEditor);
        searchPanel.add(new JLabel("Select Date:"), "cell 3 0");
        searchPanel.add(dateEditor, "cell 4 0,width 150!");
        
        add(searchPanel, "cell 0 0, growx");

        // Initialize table with GTable
        initializeTable();

        // Add event listeners
        setupEventListeners();

        // Initial load of appointments
        SwingUtilities.invokeLater(this::loadCurrentMonthAppointments);
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

        // Create action manager
        TableActionManager actionManager = new TableActionManager();
        actionManager.addAction("View", (table, row) -> {
            Integer appointmentId = (Integer) table.getValueAt(row, 0);
            viewAppointment(appointmentId);
        }, new Color(0, 150, 136), viewIcon);

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

        add(new JScrollPane(appointmentTable), "cell 0 1, grow");
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
            
            // Check if a date is selected (though in this setup, it's always set to now initially)
            if (selectedDate != null) {
                // Fetch appointments for the selected date by setting startDate and endDate to the same date
                List<Appointment> appointments = appointmentDAO.searchAppointments(
                    null,          // title (not filtering by title)
                    null,          // consultationType (not filtering by type)
                    selectedDate,  // startDate
                    selectedDate,  // endDate
                    null,          // status (not filtering by status)
                    counselorId    // filter by the current counselor
                );
                
                // Update the table with the filtered appointments
                updateAppointmentTableWithCounselor(appointments);
            } else {
                // Inform the user if no date is selected (unlikely due to datePicker.now())
                JOptionPane.showMessageDialog(this, 
                    "Please select a date.", 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
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
        try {
            AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
            int counselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
            
            // Get first and last day of current month
            java.time.LocalDate now = java.time.LocalDate.now();
            java.time.LocalDate firstDay = now.withDayOfMonth(1);
            java.time.LocalDate lastDay = now.withDayOfMonth(now.lengthOfMonth());
            
            // Debug print
            System.out.println("Loading appointments from " + firstDay + " to " + lastDay);
            System.out.println("Counselor ID: " + counselorId);
            
            // Get all appointments for current month
            List<Appointment> appointments = appointmentDAO.searchAppointments(
                null,  // title
                null,  // consultationType
                firstDay, 
                lastDay, 
                null,  // status
                counselorId
            );
            
            System.out.println("Found " + appointments.size() + " appointments");
            
            // Update table with found appointments
            if (appointments.isEmpty()) {
                System.out.println("No appointments found for the current month");
            }
            
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
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0); // Clear existing rows

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
                    new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a")
                        .format(fullAppointment.getAppointmentDateTime()) : "N/A";

                // Format counselor name
                String counselorName = counselor != null ? 
                    counselor.getFirstName() + " " + counselor.getLastName() : "Not Assigned";

                // Ensure we have participants list
                List<Participants> participants = fullAppointment.getParticipants();
                int participantCount = participants != null ? participants.size() : 0;

                model.addRow(new Object[] {
                    fullAppointment.getAppointmentId(),
                    fullAppointment.getAppointmentTitle(),
                    fullAppointment.getConsultationType(),
                    participantCount,
                    fullAppointment.getAppointmentStatus(),
                    dateTimeStr,
                    counselorName,
                    null // Actions column handled by TableActionManager
                });
            } catch (Exception e) {
                System.err.println("Error processing appointment ID " + appointment.getAppointmentId() + ": " + e.getMessage());
            }
        }
    }

    private void viewAppointment(Integer appointmentId) {
        // Implement view appointment details logic
        JOptionPane.showMessageDialog(this, "Viewing appointment " + appointmentId);
        // TODO: Add your view implementation
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
}