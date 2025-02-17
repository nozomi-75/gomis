package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class AppointmentOverview extends JPanel {
    private final DatePicker datePicker;
    private final JPanel appointmentsPanel;
    private final List<Appointment> appointments;
    private Connection connection;

    private AppointmentDAO appointmentCRUD;

    public AppointmentOverview(Connection conn) {
        this.connection = conn;
        appointmentCRUD = new AppointmentDAO();

       this.setLayout(new MigLayout("", "[]", "[top][grow]"));

        appointmentsPanel = new JPanel(new MigLayout("wrap 1, fill, gap 10", "[grow]"));
        appointments = new ArrayList<>();

        // Setup UI components
        datePicker = createDatePicker();
        this.add(datePicker, "cell 0 0, grow");
        this.add(createAppointmentsView(), "cell 0 1, grow");

        // Load today's appointments
        updateAppointmentsForDate(LocalDate.now());
    }

    /**
     * Creates and configures the DatePicker component.
     */
    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
        picker.addDateSelectionListener(dateEvent -> {
            if (picker.getDateSelectionMode() == DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED) {
                LocalDate selectedDate = picker.getSelectedDate();
                if (selectedDate != null) {
                    updateAppointmentsForDate(selectedDate);
                }
            }
        });
        picker.now();
        picker.setAnimationEnabled(true);
        return picker;
    }

    /**
     * Creates a scrollable appointments view.
     */
    private JScrollPane createAppointmentsView() {
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Appointments:"));
        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    /**
     * Creates a stylized appointment card.
     *
     * @param appt Appointment object
     * @return JPanel containing appointment details
     */
    private JPanel createAppointmentCard(Appointment appt) {
        JPanel card = new JPanel(new MigLayout("", "[grow][grow]"));
        card.setBorder(new LineBorder(Color.GRAY, 1, true));

        // Display student UID or "Non-Student" if null
        String displayName = appt.getStudentUid() != null ? "Student ID: " + appt.getStudentUid()
                : "Non-Student Appointment";
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        // Format time from appointmentDateTime
        String timeStr = appt.getAppointmentDateTime().format(
                DateTimeFormatter.ofPattern("h:mm a"));
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // Use appointmentType instead of purpose
        JLabel typeLabel = new JLabel(appt.getAppointmentType());
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.ITALIC));

        // Add status label
        JLabel statusLabel = new JLabel("Status: " + appt.getAppointmentStatus());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));

        JButton viewButton = new JButton("View");
        JButton editButton = new JButton("Edit");

        // Action listeners with updated information
        viewButton.addActionListener(e -> {
            StringBuilder details = new StringBuilder();
            details.append("Appointment Details:\n");
            details.append("ID: ").append(appt.getAppointmentId()).append("\n");
            details.append("Student ID: ").append(appt.getStudentUid() != null ? appt.getStudentUid() : "Non-Student")
                    .append("\n");
            details.append("Type: ").append(appt.getAppointmentType()).append("\n");
            details.append("Date/Time: ").append(
                    appt.getAppointmentDateTime().format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a")))
                    .append("\n");
            details.append("Status: ").append(appt.getAppointmentStatus());

            JOptionPane.showMessageDialog(this,
                    details.toString(),
                    "Appointment Details",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        editButton.addActionListener(e -> {
            // Create edit dialog
            AppointmentDialog dialog = new AppointmentDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this));

            // Pre-populate with current values
            // Note: The dialog needs to be enhanced to support editing
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                try {
                    appointmentCRUD.updateAppointments(
                            connection,
                            dialog.getStudentUid() != null ? dialog.getStudentUid() : appt.getStudentUid(),
                            appt.getCounselorsId(),
                            dialog.getAppointmentType(),
                            java.sql.Timestamp.valueOf(dialog.getAppointmentDateTime()),
                            appt.getAppointmentStatus());


                    // Refresh the view
                    updateAppointmentsForDate(appt.getAppointmentDateTime().toLocalDate());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error updating appointment: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Layout components
        card.add(nameLabel, "cell 0 0");
        card.add(timeLabel, "cell 1 0, align right");
        card.add(typeLabel, "cell 0 1, span 2");
        card.add(statusLabel, "cell 0 2, span 2");
        card.add(viewButton, "cell 0 3, split 2, align left");
        card.add(editButton, "align left");

        return card;
    }

    /**
     * Updates the appointment panel based on the selected date.
     *
     * @param date Selected date
     */
    private void updateAppointmentsForDate(LocalDate date) {
        SwingUtilities.invokeLater(() -> {
            appointmentsPanel.removeAll();

            List<Appointment> appointments = appointmentCRUD.getAppointmentsForDate(connection, date);

            if (appointments.isEmpty()) {
                JLabel noAppointmentsLabel = new JLabel("No appointments for this date", SwingConstants.CENTER);
                appointmentsPanel.add(noAppointmentsLabel, "grow");
            } else {
                for (Appointment appt : appointments) {
                    appointmentsPanel.add(createAppointmentCard(appt), "grow");
                }
            }

            appointmentsPanel.revalidate();
            appointmentsPanel.repaint();
        });
    }
}
