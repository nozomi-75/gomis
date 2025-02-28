package lyfjshs.gomis.view.appointment;

import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.toedter.calendar.JDateChooser;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.model.Appointment;

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
        addButton.addActionListener(e -> showAppointmentDialog(null));

        headerPanel.add(titleLabel, "span 2");
        headerPanel.add(subtitleLabel, "span 2");
        headerPanel.add(dateChooser);
        headerPanel.add(addButton, "gapy 5");

        // Appointments display
        appointmentsPanel = new JPanel(new MigLayout("wrap 1", "[grow]", ""));
        appointmentsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        mainPanel.add(headerPanel, "growx");
        mainPanel.add(new JSeparator(), "growx, gapy 5");
        mainPanel.add(new JScrollPane(appointmentsPanel), "grow");

        add(mainPanel);
    }

    public void updateAppointmentsDisplay() {
        appointmentsPanel.removeAll();
        
        List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(connection, selectedDate);

        if (appointments.isEmpty()) {
            JPanel emptyPanel = new JPanel(new MigLayout("wrap, align center", "[grow]", "[]"));
            emptyPanel.add(new JLabel("No appointments scheduled for this day"));
            JButton addButton = new JButton("Add Your First Appointment");
            addButton.setPreferredSize(new Dimension(150, 25));
            addButton.addActionListener(e -> showAppointmentDialog(null));
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
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, getTypeColor(app.getAppointmentType())),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        card.setBackground(getTypeBackground(app.getAppointmentType()));

        JLabel title = new JLabel(app.getAppointmentTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton editButton = new JButton("Edit");
        editButton.setPreferredSize(new Dimension(60, 25));
        editButton.addActionListener(e -> showAppointmentDialog(app));
        JButton deleteButton = new JButton("Delete");
        deleteButton.setPreferredSize(new Dimension(80, 25));
        deleteButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this appointment?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (appointmentDAO.deleteAppointment(app.getAppointmentId())) {
                    updateAppointmentsDisplay();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete appointment",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        card.add(title);
        card.add(buttonPanel, "right");
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        String timeSlot = app.getAppointmentDateTime().format(timeFormatter);
        card.add(new JLabel(timeSlot + " • " + app.getAppointmentStatus()) {
            { setFont(new Font("Segoe UI", Font.PLAIN, 12)); }
        }, "span 2");

        if (app.getCounselorsId() != null) {
            card.add(new JLabel("Counselor ID: " + app.getCounselorsId()) {
                { setFont(new Font("Segoe UI", Font.PLAIN, 12)); }
            }, "span 2");
        }
        
        if (app.getAppointmentNotes() != null && !app.getAppointmentNotes().isEmpty()) {
            String truncatedNotes = app.getAppointmentNotes().length() > 100 ? 
                app.getAppointmentNotes().substring(0, 100) + "..." : 
                app.getAppointmentNotes();
            JLabel notesLabel = new JLabel("<html>Notes: " + truncatedNotes + "</html>") {
                { 
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    setPreferredSize(new Dimension(300, 50));
                    setVerticalAlignment(SwingConstants.TOP);
                }
            };
            card.add(notesLabel, "span 2");
        }

        return card;
    }

    private void showAppointmentDialog(Appointment existingApp) {
        AppointmentDialog dialog = new AppointmentDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), 
            existingApp != null ? existingApp : new Appointment()
        );
        
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Appointment app = dialog.getAppointment();
            if (existingApp == null) {
                // Create new appointment
                boolean success = appointmentDAO.addAppointment(
                    connection,
                    app.getParticipantId(),
                    app.getCounselorsId(),
                    app.getAppointmentTitle(),
                    app.getAppointmentType(),
                    java.sql.Timestamp.valueOf(app.getAppointmentDateTime()),
                    app.getAppointmentNotes(),
                    app.getAppointmentStatus()
                );
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Failed to add appointment",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Update existing appointment
                boolean success = appointmentDAO.updateAppointment(connection, app);
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Failed to update appointment",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            updateAppointmentsDisplay();
        }
    }

    private Color getTypeColor(String type) {
        switch (type) {
            case "Academic Consultation": return new Color(59, 130, 246);
            case "Career Guidance": return new Color(147, 51, 234);
            case "Personal Counseling": return new Color(16, 185, 129);
            case "Behavioral Counseling": return new Color(202, 138, 4);
            case "Group Counseling": return new Color(239, 68, 68);
            default: return Color.GRAY;
        }
    }

    private Color getTypeBackground(String type) {
        switch (type) {
            case "Academic Consultation": return new Color(219, 234, 254);
            case "Career Guidance": return new Color(233, 213, 255);
            case "Personal Counseling": return new Color(209, 250, 229);
            case "Behavioral Counseling": return new Color(254, 243, 199);
            case "Group Counseling": return new Color(254, 226, 226);
            default: return new Color(243, 244, 246);
        }
    }
}