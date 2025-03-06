package lyfjshs.gomis.view.appointment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import lyfjshs.gomis.Database.entity.Appointment;
import net.miginfocom.swing.MigLayout;

public class AppointmentsListView extends JPanel {
    private JButton closeButton;

    public AppointmentsListView() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(636, 555));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel dateLabel = new JLabel("March 10, 2025"); // Static date for testing
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(dateLabel, BorderLayout.WEST);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        add(headerPanel, BorderLayout.NORTH);

        // Body Panel with MigLayout
        JPanel bodyPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][][pref!][]"));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Static appointment count
        JLabel countLabel = new JLabel("2 appointments scheduled");
        countLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        countLabel.setForeground(Color.GRAY);
        bodyPanel.add(countLabel, "align center");

        // Static Appointment 1
        JPanel appointmentPanel1 = new JPanel(new BorderLayout());
        // Make Appointment Panel Clickable
        appointmentPanel1.setBorder(BorderFactory.createLineBorder(new Color(208, 208, 208), 1, true));
        appointmentPanel1.setBackground(new Color(245, 245, 245));
        // Make Appointment Panel Clickable
        appointmentPanel1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        appointmentPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                appointmentPanel1.setBackground(new Color(220, 220, 220)); // Lighter shade on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                appointmentPanel1.setBackground(new Color(245, 245, 245)); // Original color
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Appointment clicked: Team Meeting at 10:00 AM");
            }
        });

        JPanel headerPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel timeLabel1 = new JLabel("10:00");
        timeLabel1.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel titleLabel1 = new JLabel("Team Meeting");
        titleLabel1.setFont(new Font("Arial", Font.PLAIN, 13));
        headerPanel1.add(timeLabel1);
        headerPanel1.add(titleLabel1);
        appointmentPanel1.add(headerPanel1, BorderLayout.NORTH);
        JPanel detailsPanel1 = new JPanel();
        detailsPanel1.setLayout(new BoxLayout(detailsPanel1, BoxLayout.Y_AXIS));
        detailsPanel1.setBorder(BorderFactory.createEmptyBorder(0, 70, 0, 0));
        JLabel appoNotesLabel = new JLabel("Weekly sync with project team");
        appoNotesLabel.setForeground(Color.GRAY);
        JLabel statusLabel1 = new JLabel("<html><b>Status:</b> Confirmed</html>");
        statusLabel1.setForeground(Color.GRAY);

        JLabel appoTypeLabel = new JLabel("Academic Consultation");
        detailsPanel1.add(appoTypeLabel);
        detailsPanel1.add(appoNotesLabel);
        detailsPanel1.add(statusLabel1);
        appointmentPanel1.add(detailsPanel1, BorderLayout.CENTER);
        bodyPanel.add(appointmentPanel1, "growx");

        // Separator
        JSeparator separator1 = new JSeparator(SwingConstants.HORIZONTAL);
        separator1.setForeground(Color.LIGHT_GRAY);
        separator1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        bodyPanel.add(separator1, "growx");

        // Static Appointment 2
        JPanel appointmentPanel2 = new JPanel(new BorderLayout());
        appointmentPanel2.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        appointmentPanel2.setBackground(new Color(249, 249, 249));
        JPanel headerPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel timeLabel2 = new JLabel("14:00");
        timeLabel2.setFont(new Font("Arial", Font.BOLD, 12));
        JLabel titleLabel2 = new JLabel("Doctor Appointment");
        titleLabel2.setFont(new Font("Arial", Font.PLAIN, 13));
        headerPanel2.add(timeLabel2);
        headerPanel2.add(titleLabel2);
        appointmentPanel2.add(headerPanel2, BorderLayout.NORTH);
        JPanel detailsPanel2 = new JPanel();
        detailsPanel2.setLayout(new BoxLayout(detailsPanel2, BoxLayout.Y_AXIS));
        detailsPanel2.setBorder(BorderFactory.createEmptyBorder(0, 70, 0, 0));
        JLabel descriptionLabel2 = new JLabel("Annual check-up with Dr. Smith");
        descriptionLabel2.setForeground(Color.GRAY);
        JLabel statusLabel2 = new JLabel("<html><b>Status:</b> Scheduled</html>");
        statusLabel2.setForeground(Color.GRAY);
        detailsPanel2.add(descriptionLabel2);
        detailsPanel2.add(statusLabel2);
        appointmentPanel2.add(detailsPanel2, BorderLayout.CENTER);
        bodyPanel.add(appointmentPanel2, "growx");

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Close");
        closeButton.setBackground(new Color(244, 67, 54));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        footerPanel.add(closeButton);
        add(footerPanel, BorderLayout.SOUTH);
    }

    // Placeholder method (not used statically)
    private JPanel createAppointmentPanel(Appointment app) {
        return new JPanel(); // Empty for now, as we're using static panels
    }

    private String getFormattedDate() {
        return "March 10, 2025"; // Static date for testing
    }

    // Example usage in a JFrame
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLaf.setup(new FlatLightLaf());
            JFrame frame = new JFrame("Appointment Dialog");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            AppointmentsListView panel = new AppointmentsListView();
            frame.getContentPane().add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}