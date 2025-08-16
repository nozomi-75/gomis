package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class AppointmentsListView extends JPanel {
    private static final Logger logger = LogManager.getLogger(AppointmentsListView.class);
    private JButton closeButton;
    private Connection connection;
    private LocalDate selectedDate;
    private JPanel bodyPanel;
    private JLabel dateLabel;
    private JLabel countLabel;

    public AppointmentsListView(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
    }

    public void setDate(LocalDate date, List<Appointment> appointments) {
        this.selectedDate = date;
        loadAppointments(appointments);
    }

    private void loadAppointments(List<Appointment> appointments) {
        try {
            // Update date label
            dateLabel.setText(selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            
            // Update count label
            countLabel.setText(appointments.size() + " appointment" + (appointments.size() != 1 ? "s" : "") + " scheduled");
            
            // Clear existing appointment panels
            bodyPanel.removeAll();
            bodyPanel.add(countLabel, "align center, wrap");
            
            // Add appointment panels
            for (int i = 0; i < appointments.size(); i++) {
                Appointment appointment = appointments.get(i);
                createAppointmentPanel(bodyPanel, appointment);
                
                // Add separator between appointments
                if (i < appointments.size() - 1) {
                    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                    separator.setForeground(UIManager.getColor("Separator.foreground"));
                    bodyPanel.add(separator, "growx, wrap");
                }
            }
            
            // Refresh the panel
            bodyPanel.revalidate();
            bodyPanel.repaint();
            
        } catch (Exception e) { // Changed from SQLException for broader error handling
            logger.error("Error loading appointments: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                "Error loading appointments: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        dateLabel = new JLabel("", SwingConstants.LEFT);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(dateLabel, BorderLayout.WEST);
        
        // Use theme-aware colors for borders
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, 
            UIManager.getColor("Separator.foreground")));
        add(headerPanel, BorderLayout.NORTH);

        // Body Panel with MigLayout
        bodyPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][][pref!][]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        countLabel = new JLabel("0 appointments scheduled");
        countLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        countLabel.setForeground(UIManager.getColor("Label.disabledText"));
        bodyPanel.add(countLabel, "align center");

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Footer with theme-aware colors
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, 
            UIManager.getColor("Separator.foreground")));
        
        closeButton = new JButton("Close");
        closeButton.putClientProperty("JButton.buttonType", "roundRect");
        closeButton.putClientProperty("JButton.buttonProperties", "color: #f44336"); // Material red color
        footerPanel.add(closeButton);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void createAppointmentPanel(JPanel bodyPanel, Appointment appointment) {
        JPanel appointmentPanel = new JPanel(new BorderLayout());
        boolean isDarkTheme = UIManager.getBoolean("dark");
        
        // Theme-aware colors
        Color bgColor = isDarkTheme ? new Color(50, 50, 50) : new Color(245, 245, 245);
        Color hoverColor = isDarkTheme ? new Color(60, 60, 60) : new Color(235, 235, 235);
        Color borderColor = isDarkTheme ? new Color(70, 70, 70) : new Color(208, 208, 208);
        
        appointmentPanel.setBorder(BorderFactory.createLineBorder(borderColor, 1, true));
        appointmentPanel.setBackground(bgColor);
        appointmentPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Header
        JPanel headerPanel = createHeaderPanel(
            appointment.getAppointmentDateTime().toLocalDateTime().toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm")),
            appointment.getAppointmentTitle()
        );
        appointmentPanel.add(headerPanel, BorderLayout.NORTH);

        // Details
        JPanel detailsPanel = createDetailsPanel(
            appointment.getAppointmentNotes(),
            appointment.getAppointmentStatus(),
            appointment.getConsultationType()
        );
        appointmentPanel.add(detailsPanel, BorderLayout.CENTER);

        // Hover effect
        appointmentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                appointmentPanel.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                appointmentPanel.setBackground(bgColor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showAppointmentDetails(appointment);
            }
        });

        bodyPanel.add(appointmentPanel, "growx");
    }

    private JPanel createHeaderPanel(String time, String title) {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        headerPanel.add(timeLabel);
        headerPanel.add(titleLabel);
        return headerPanel;
    }

    private JPanel createDetailsPanel(String description, String status, String type) {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 70, 0, 0));
        detailsPanel.setOpaque(false);

        JLabel typeLabel = new JLabel(type);
        typeLabel.setForeground(UIManager.getColor("Label.foreground"));
        
        JLabel descLabel = new JLabel(description != null ? description : "");
        descLabel.setForeground(UIManager.getColor("Label.disabledText"));
        
        JLabel statusLabel = new JLabel("<html><b>Status:</b> " + status + "</html>");
        statusLabel.setForeground(UIManager.getColor("Label.disabledText"));

        detailsPanel.add(typeLabel);
        detailsPanel.add(descLabel);
        detailsPanel.add(statusLabel);
        
        return detailsPanel;
    }

    private void showAppointmentDetails(Appointment appointment) {
        AppointmentDayDetails detailsPanel = new AppointmentDayDetails(connection, null, null);
        detailsPanel.loadAppointmentDetails(appointment);
        
        // Configure modal options
        Option detailsOption = new Option();
        detailsOption.setOpacity(0f)
                .setAnimationOnClose(false)
                .getBorderOption()
                .setBorderWidth(0.5f)
                .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

        // Show modal with proper size
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
                detailsOption, // Pass the specific option for this modal
                "appointment_details");

        // Set modal size
        detailsOption.getLayoutOption().setSize(800, 400);
    }
}