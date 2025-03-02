package lyfjshs.gomis.view.appointment;

import java.awt.Color;
import java.awt.Dimension;
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
import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

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
        setLayout(new MigLayout("wrap 1", "[grow]", "[grow]"));

        appointmentsPanel = new JPanel(new MigLayout("wrap 1, fill, gap 10", "[grow]"));
        
        // Setup UI components
        datePicker = createDatePicker();
        datePicker.setPreferredSize(new Dimension(datePicker.getPreferredSize().width, 200)); // Adjust height as needed
        
        this.add(datePicker, "cell 0 0, grow");
        this.add(createAppointmentsView(), "cell 0 1, grow");

        // Load today's appointments
        loadAppointments(LocalDate.now());
    }

    private DatePicker createDatePicker() {
        DatePicker picker = new DatePicker();
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

    private JScrollPane createAppointmentsView() {
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Appointments:"));
        JScrollPane scrollPane = new JScrollPane(appointmentsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Disable horizontal scrolling
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Enable vertical scrolling
        return scrollPane;
    }

    private JPanel createAppointmentCard(Appointment appt) {
        JPanel card = new JPanel(new MigLayout("", "[grow][grow]"));
        card.setBorder(new LineBorder(Color.GRAY, 1, true));

        JLabel nameLabel = new JLabel(appt.getAppointmentTitle());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));

        String timeStr = appt.getAppointmentDateTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("h:mm a"));
        JLabel timeLabel = new JLabel(timeStr);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel typeLabel = new JLabel(appt.getAppointmentType());
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.ITALIC));

        JLabel statusLabel = new JLabel("Status: " + appt.getAppointmentStatus());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));

        JButton viewButton = new JButton("View");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        viewButton.addActionListener(e -> {
            StringBuilder details = new StringBuilder();
            details.append("Appointment Details:\n")
                   .append("ID: ").append(appt.getAppointmentId()).append("\n")
                   .append("Participant ID: ").append(appt.getParticipantId()).append("\n")
                   .append("Counselor ID: ").append(appt.getGuidanceCounselorId() != null ? appt.getGuidanceCounselorId() : "Not assigned").append("\n")
                   .append("Title: ").append(appt.getAppointmentTitle()).append("\n")
                   .append("Type: ").append(appt.getAppointmentType()).append("\n")
                   .append("Date/Time: ").append(appt.getAppointmentDateTime().toLocalDateTime().format(
                           DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a"))).append("\n")
                   .append("Notes: ").append(appt.getAppointmentNotes()).append("\n")
                   .append("Status: ").append(appt.getAppointmentStatus());

            JOptionPane.showMessageDialog(this, details.toString(), 
                "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
        });

        editButton.addActionListener(e -> {
            AppointmentDialog dialog = new AppointmentDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this), appt);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                try {
                    Appointment updatedAppt = dialog.getAppointment();
                    if (appointmentDAO.updateAppointment(connection, updatedAppt)) {
                        loadAppointments(appt.getAppointmentDateTime().toLocalDateTime().toLocalDate());
                        JOptionPane.showMessageDialog(this, "Appointment updated successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error updating appointment: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this appointment?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (appointmentDAO.deleteAppointment(appt.getAppointmentId())) {
                        loadAppointments(appt.getAppointmentDateTime().toLocalDateTime().toLocalDate());
                        JOptionPane.showMessageDialog(this, "Appointment deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting appointment: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        card.add(nameLabel, "cell 0 0");
        card.add(timeLabel, "cell 1 0, align right");
        card.add(typeLabel, "cell 0 1, span 2");
        card.add(statusLabel, "cell 0 2, span 2");
        card.add(viewButton, "cell 0 3, split 3, align left");
        card.add(editButton, "align left");
        card.add(deleteButton, "align left");

        return card;
    }

    private void loadAppointments(LocalDate date) {
        SwingUtilities.invokeLater(() -> {
            appointmentsPanel.removeAll();
            List<Appointment> loadedAppointments = appointmentDAO.getAppointmentsForDate(connection, date);
            appointments.clear();
            appointments.addAll(loadedAppointments);

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