package lyfjshs.gomis.test;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;
import com.formdev.flatlaf.FlatLightLaf;

public class AppointmentCompletedORCancelledPanelTEST extends JPanel {
    private JPanel appointmentsListPanel;
    private JButton completedButton, cancelledButton, allButton;
    private String activeFilter = "all appointments"; // Track the active filter

    public AppointmentCompletedORCancelledPanelTEST() {
        setLayout(new MigLayout("fill, wrap 1", "[grow, fill]", "[][][grow]"));
        setBackground(new Color(244, 244, 244));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][right]", "[]"));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        JLabel titleLabel = new JLabel("Guidance Counseling Appointments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(titleLabel);

        // Filter Buttons
        JPanel filterButtonsPanel = new JPanel(new MigLayout("insets 0, gap 10", "[]", "[]"));
        filterButtonsPanel.setBackground(Color.WHITE);
        completedButton = createFilterButton("Completed");
        cancelledButton = createFilterButton("Cancelled");
        allButton = createFilterButton("All Appointments");
        allButton.setBackground(new Color(0, 100, 200)); // Highlight "All" as default active filter
        filterButtonsPanel.add(completedButton);
        filterButtonsPanel.add(cancelledButton);
        filterButtonsPanel.add(allButton);
        headerPanel.add(filterButtonsPanel, "wrap");

        add(headerPanel, "growx");

        // Appointments List Panel with hidemode 3
        appointmentsListPanel = new JPanel(new MigLayout("fill, wrap 1, hidemode 3", "[grow]", "[grow]10[grow]")); // Add gap of 10 between cards
        appointmentsListPanel.setBackground(Color.WHITE);
        appointmentsListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Sample Appointments
        appointmentsListPanel.add(getAppointmentCard("Academic Guidance Session", "Completed", "Academic Counseling", "2024-03-15 10:00 AM", "Alice Smith", "Follow-up on academic performance and support strategies", new String[][]{
                {"John Doe", "Student", "11th Grade"},
                {"Jane Smith", "Parent", "Mother"}
        }, "Discussed student's academic challenges and developed an improvement plan.", "Additional tutoring, study skills workshop", "Academic Performance", "Consecutive low grades in mathematics", "Academic counseling and support"), "growx");

        appointmentsListPanel.add(getAppointmentCard("Personal Counseling Session", "Cancelled", "Personal Counseling", "2024-03-20 02:00 PM", "Alice Smith", "Rescheduled due to conflict", new String[][]{
                {"Michael Johnson", "Student", "10th Grade"}
        }, "", "", "", "", ""), "growx");

        JScrollPane scrollPane = new JScrollPane(appointmentsListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Disable horizontal scrollbar
        add(scrollPane, "grow");
    }

    private JButton createFilterButton(String label) {
        JButton filterButton = new JButton(label);
        filterButton.setBackground(new Color(0, 123, 255));
        filterButton.setForeground(Color.WHITE);
        filterButton.setFocusPainted(false);
        filterButton.setBorderPainted(false);
        filterButton.setOpaque(true);
        filterButton.setPreferredSize(new Dimension(150, 30)); // Consistent button size
        filterButton.addActionListener(e -> {
            activeFilter = label.toLowerCase();
            updateFilterButtonStyles();
            filterAppointments(activeFilter);
        });
        return filterButton;
    }

    private void updateFilterButtonStyles() {
        completedButton.setBackground("completed".equals(activeFilter) ? new Color(0, 100, 200) : new Color(0, 123, 255));
        cancelledButton.setBackground("cancelled".equals(activeFilter) ? new Color(0, 100, 200) : new Color(0, 123, 255));
        allButton.setBackground("all appointments".equals(activeFilter) ? new Color(0, 100, 200) : new Color(0, 123, 255));
    }

    private JPanel getAppointmentCard(String sessionTitle, String status, String consultationType, String date, String counselor, String notes, String[][] participants, String sessionSummary, String recommendations, String violationType, String violationDesc, String reinforcement) {
        JPanel cardPanel = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[][][grow]"));
        cardPanel.setName(status.toLowerCase());
        cardPanel.setBackground(new Color(248, 249, 250));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, getStatusColor(status)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                cardPanel.getBorder()
        ));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, Color.WHITE, Color.LIGHT_GRAY),
                cardPanel.getBorder()
        ));

        // Header with Title and Status
        JPanel headerPanel = new JPanel(new MigLayout("fill, insets 0", "[grow][right]", "[align center]"));
        headerPanel.setBackground(new Color(248, 249, 250));
        JLabel titleLabel = new JLabel(sessionTitle);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerPanel.add(titleLabel);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(status));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(statusLabel);

        cardPanel.add(headerPanel, "growx");

        // Details Section
        JPanel detailsPanel = new JPanel(new MigLayout("fill, wrap 2, gap 10", "[grow][grow]", "[][]"));
        detailsPanel.setBackground(new Color(248, 249, 250));
        detailsPanel.add(new JLabel("<html><strong>Consultation Type:</strong> " + consultationType + "</html>"));
        detailsPanel.add(new JLabel("<html><strong>Date:</strong> " + date + "</html>"));
        detailsPanel.add(new JLabel("<html><strong>Counselor:</strong> " + counselor + "</html>"));
        JLabel notesLabel = new JLabel("<html><strong>Appointment Notes:</strong> " + notes + "</html>");
        notesLabel.setPreferredSize(new Dimension(0, 0)); // Allow wrapping
        detailsPanel.add(notesLabel);
        cardPanel.add(detailsPanel, "growx");

        // Participants Section
        if (participants != null && participants.length > 0) {
            JPanel participantsPanel = new JPanel(new MigLayout("fill, wrap 1, gap 10", "[grow]", "[]")); // Stack vertically
            participantsPanel.setBackground(new Color(248, 249, 250));
            participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
            for (String[] participant : participants) {
                JPanel participantCard = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[]"));
                participantCard.setBackground(new Color(233, 236, 239));
                participantCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                participantCard.add(new JLabel("<html><strong>" + participant[0] + "</strong></html>"));
                participantCard.add(new JLabel("<html><strong>Type:</strong> " + participant[1] + "</html>"));
                participantCard.add(new JLabel("<html><strong>Grade Level:</strong> " + participant[2] + "</html>"));
                participantsPanel.add(participantCard, "growx");
            }
            cardPanel.add(participantsPanel, "growx");
        }

        // Session Summary Section
        if (!sessionSummary.isEmpty()) {
            JPanel sessionPanel = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[]10[]")); // Add gap between summary and recommendations
            sessionPanel.setBackground(new Color(241, 243, 245));
            sessionPanel.setBorder(BorderFactory.createTitledBorder("Session Summary"));
            JLabel summaryLabel = new JLabel("<html>" + sessionSummary + "</html>");
            summaryLabel.setPreferredSize(new Dimension(0, 0)); // Allow wrapping
            sessionPanel.add(summaryLabel);
            sessionPanel.add(new JLabel("<html><strong>Recommendations:</strong> " + recommendations + "</html>"));
            cardPanel.add(sessionPanel, "growx");
        }

        // Related Violations Section
        if (!violationType.isEmpty()) {
            JPanel violationPanel = new JPanel(new MigLayout("fill, wrap 1", "[grow]", "[][][]"));
            violationPanel.setBackground(new Color(255, 243, 205));
            violationPanel.setBorder(BorderFactory.createTitledBorder("Related Violations"));
            violationPanel.add(new JLabel("<html><strong>Violation Type:</strong> " + violationType + "</html>"));
            violationPanel.add(new JLabel("<html><strong>Description:</strong> " + violationDesc + "</html>"));
            violationPanel.add(new JLabel("<html><strong>Reinforcement:</strong> " + reinforcement + "</html>"));
            cardPanel.add(violationPanel, "growx");
        }

        return cardPanel;
    }

    private Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "completed":
                return new Color(40, 167, 69);
            case "cancelled":
                return new Color(220, 53, 69);
            default:
                return Color.GRAY;
        }
    }

    private void filterAppointments(String status) {
        boolean hasVisibleCards = false;
        Component[] components = appointmentsListPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel cardPanel = (JPanel) component;
                String cardStatus = cardPanel.getName();
                if (cardStatus != null) {
                    if ("all appointments".equals(status) || cardStatus.equals(status)) {
                        cardPanel.setVisible(true);
                        hasVisibleCards = true;
                    } else {
                        cardPanel.setVisible(false);
                    }
                }
            }
        }

        // Add "No Appointments" message if no cards are visible
        appointmentsListPanel.removeAll();
        if (!hasVisibleCards) {
            JLabel noAppointmentsLabel = new JLabel("No appointments found for this filter.", SwingConstants.CENTER);
            noAppointmentsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            noAppointmentsLabel.setForeground(Color.GRAY);
            appointmentsListPanel.add(noAppointmentsLabel, "grow, push");
        } else {
            // Re-add the appointment cards
            for (Component component : components) {
                appointmentsListPanel.add(component, "growx");
            }
        }

        appointmentsListPanel.revalidate();
        appointmentsListPanel.repaint();
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        JFrame frame = new JFrame("Guidance Counseling Appointments");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setContentPane(new AppointmentCompletedORCancelledPanelTEST());
        frame.setVisible(true);
    }
}