package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import lyfjshs.gomis.Database.DBConnection;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class AppointmentsListView extends JPanel {
    private JButton closeButton;
    private Connection connection;

    public AppointmentsListView(Connection connection) {
        this.connection = connection;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(636, 555));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel dateLabel = new JLabel("March 10, 2025");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(dateLabel, BorderLayout.WEST);
        
        // Use theme-aware colors for borders
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, 
            UIManager.getColor("Separator.foreground")));
        add(headerPanel, BorderLayout.NORTH);

        // Body Panel with MigLayout
        JPanel bodyPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][][pref!][]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel countLabel = new JLabel("2 appointments scheduled");
        countLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        countLabel.setForeground(UIManager.getColor("Label.disabledText"));
        bodyPanel.add(countLabel, "align center");

        // Create appointment panels with theme-aware colors
        createAppointmentPanel1(bodyPanel);
        
        // Separator with theme-aware color
        JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
        separator1.setForeground(UIManager.getColor("Separator.foreground"));
        bodyPanel.add(separator1, "growx");

        createAppointmentPanel2(bodyPanel);

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

    private void createAppointmentPanel1(JPanel bodyPanel) {
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
        JPanel headerPanel = createHeaderPanel("10:00", "Team Meeting");
        appointmentPanel.add(headerPanel, BorderLayout.NORTH);

        // Details
        JPanel detailsPanel = createDetailsPanel(
            "Weekly sync with project team",
            "Confirmed",
            "Academic Consultation"
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
                showAppointmentDetails(LocalDate.of(2025, 3, 10));
            }
        });

        bodyPanel.add(appointmentPanel, "growx");
    }

    private void createAppointmentPanel2(JPanel bodyPanel) {
        JPanel appointmentPanel2 = new JPanel(new BorderLayout());
        boolean isDarkTheme = UIManager.getBoolean("dark");
        
        // Theme-aware colors
        Color bgColor = isDarkTheme ? new Color(50, 50, 50) : new Color(245, 245, 245);
        Color hoverColor = isDarkTheme ? new Color(60, 60, 60) : new Color(235, 235, 235);
        Color borderColor = isDarkTheme ? new Color(70, 70, 70) : new Color(208, 208, 208);
        
        appointmentPanel2.setBorder(BorderFactory.createLineBorder(borderColor, 1, true));
        appointmentPanel2.setBackground(bgColor);
        appointmentPanel2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Header
        JPanel headerPanel2 = createHeaderPanel("14:00", "Doctor Appointment");
        appointmentPanel2.add(headerPanel2, BorderLayout.NORTH);

        // Details
        JPanel detailsPanel2 = createDetailsPanel(
            "Annual check-up with Dr. Smith",
            "Scheduled",
            ""
        );
        appointmentPanel2.add(detailsPanel2, BorderLayout.CENTER);

        // Hover effect
        appointmentPanel2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                appointmentPanel2.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                appointmentPanel2.setBackground(bgColor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showAppointmentDetails(LocalDate.of(2025, 3, 10));
            }
        });

        bodyPanel.add(appointmentPanel2, "growx");
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
        
        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(UIManager.getColor("Label.disabledText"));
        
        JLabel statusLabel = new JLabel("<html><b>Status:</b> " + status + "</html>");
        statusLabel.setForeground(UIManager.getColor("Label.disabledText"));

        detailsPanel.add(typeLabel);
        detailsPanel.add(descLabel);
        detailsPanel.add(statusLabel);
        
        return detailsPanel;
    }

    private void showAppointmentDetails(LocalDate date) {
        if (ModalDialog.isIdExist("appointment_details")) {
            return;
        }

        try {
            AppointmentDayDetails appointmentDetails = new AppointmentDayDetails(connection, null, null); // Pass null since we don't need selection here
            appointmentDetails.loadAppointmentsForDate(date);

            // Configure default modal options
            ModalDialog.getDefaultOption()
                    .setOpacity(0f) // Transparent background
                    .setAnimationOnClose(false) // No close animation
                    .getBorderOption()
                    .setBorderWidth(0.5f) // Thin border
                    .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM); // Medium shadow

            // Show the modal dialog
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

            // Set size
            ModalDialog.getDefaultOption().getLayoutOption().setSize(600, 400);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error opening appointment details: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLaf.setup(new FlatMacDarkLaf());
            JFrame frame = new JFrame("Appointment Dialog");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Connection connection = null;
            try {
                connection = DBConnection.getConnection();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                return;
            }
            AppointmentsListView panel = new AppointmentsListView(connection);
            frame.getContentPane().add(panel);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}