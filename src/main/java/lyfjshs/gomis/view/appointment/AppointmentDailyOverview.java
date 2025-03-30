package lyfjshs.gomis.view.appointment;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import net.miginfocom.swing.MigLayout;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;

public class AppointmentDailyOverview extends JPanel {
    private JPanel appointmentsListPanel;
    private JButton completedButton, cancelledButton, allButton;
    private String activeFilter = "all appointments";
    private AppointmentDAO appointmentDAO;
    private Connection connection;

    public AppointmentDailyOverview(AppointmentDAO appointmentDAO, Connection connection) {
        this.appointmentDAO = appointmentDAO;
        this.connection = connection;
        
        setLayout(new MigLayout("fill, wrap 1", "[grow, fill]", "[][][grow]"));
        putClientProperty("FlatLaf.style", "arc: 8");
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][right]", "[]"));
        headerPanel.putClientProperty("FlatLaf.style", "arc: 8");
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIManager.getColor("Component.borderColor")));
        
        JLabel titleLabel = new JLabel("Completed & Cancelled Appointments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        // Filter Buttons
        JPanel filterButtonsPanel = new JPanel(new MigLayout("insets 0, gap 10", "[]", "[]"));
        completedButton = createFilterButton("Ended");
        cancelledButton = createFilterButton("Cancelled");
        allButton = createFilterButton("All Appointments");
        allButton.putClientProperty("FlatLaf.styleClass", "accent");
        
        filterButtonsPanel.add(completedButton);
        filterButtonsPanel.add(cancelledButton);
        filterButtonsPanel.add(allButton);
        headerPanel.add(filterButtonsPanel, "wrap");

        add(headerPanel, "growx");

        // Appointments List Panel
        appointmentsListPanel = new JPanel(new MigLayout("fill, wrap 1, hidemode 3", "[grow]", "[grow]10[grow]"));
        appointmentsListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(appointmentsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "grow");

        // Initial load
        updateAppointmentsDisplay();
    }

    private JButton createFilterButton(String label) {
        JButton filterButton = new JButton(label);
        filterButton.putClientProperty("FlatLaf.style", "arc: 8");
        filterButton.setPreferredSize(new Dimension(150, 30));
        filterButton.addActionListener(e -> {
            activeFilter = label.toLowerCase();
            updateFilterButtonStyles();
            filterAppointments(activeFilter);
        });
        return filterButton;
    }

    private void updateFilterButtonStyles() {
        completedButton.putClientProperty("FlatLaf.styleClass", "ended".equals(activeFilter) ? "accent" : "");
        cancelledButton.putClientProperty("FlatLaf.styleClass", "cancelled".equals(activeFilter) ? "accent" : "");
        allButton.putClientProperty("FlatLaf.styleClass", "all appointments".equals(activeFilter) ? "accent" : "");
    }

    private JPanel createAppointmentCard(Appointment appointment) {
        JPanel cardPanel = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[][][grow]"));
        cardPanel.setName(appointment.getAppointmentStatus().toLowerCase());
        cardPanel.putClientProperty("FlatLaf.style", "arc: 8");
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, getStatusColor(appointment.getAppointmentStatus())),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

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
            case "ended" -> new Color(40, 167, 69);
            case "cancelled" -> new Color(220, 53, 69);
            default -> Color.GRAY;
        };
    }

    private void filterAppointments(String status) {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByStatus(status);
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
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading appointments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateAppointmentsDisplay() {
        filterAppointments(activeFilter);
    }
}