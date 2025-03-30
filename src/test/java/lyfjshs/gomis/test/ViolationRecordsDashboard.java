package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

public class ViolationRecordsDashboard extends JFrame {

    private JPanel contentPane;
    private List<JPanel> recordPanels = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ViolationRecordsDashboard().setVisible(true);
        });
    }

    public ViolationRecordsDashboard() {
        setTitle("Violation Records Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new MigLayout("fillx, wrap 1", "[grow]"));
        JScrollPane scrollPane = new JScrollPane(contentPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Sample Violation Records
        addViolationRecord("Juan Carlos Dela Cruz", "136752180058", "10 - Einstein", 16, "Male",
                "Academic", "Caught cheating during midterm examination", "Pending",
                "Feb 15, 2025", "Academic counseling",
                "Student admitted to the violation. First offense.",
                new String[]{"Feb 18, 2025 - Initial counseling (Completed)"},
                new String[]{"Feb 15, 2025 - Reported by Math teacher"});

        addViolationRecord("Maria Santos Reyes", "136752180042", "9 - Curie", 15, "Female",
                "Behavioral", "Involved in verbal altercation with classmate", "Resolved",
                "Jan 27, 2025", "Conflict resolution training, parent conference",
                "Student showed remorse and apologized to the affected party.",
                new String[]{"Jan 29, 2025 - Initial counseling (Completed)", "Feb 05, 2025 - Follow-up with parent (Completed)"},
                new String[]{"Jan 27, 2025 - Reported by classroom advisor"});

        setContentPane(scrollPane);
    }

    private void addViolationRecord(String name, String lrn, String gradeSection, int age, String sex,
                                    String violationType, String violationDesc, String status, String date,
                                    String reinforcement, String anecdotalRecord, String[] sessions, String[] incidents) {

        JPanel panel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]"));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Header Panel (Clickable)
        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 5", "[grow]"));
        headerPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(status));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel detailsLabel = new JLabel("LRN: " + lrn + " | " + violationType + " | " + date);
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsLabel.setForeground(Color.DARK_GRAY);

        // Expandable Details Panel (Initially Hidden)
        JPanel detailsPanel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]"));
        detailsPanel.setVisible(false);
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        detailsPanel.add(new JLabel("<html><b>Student Information</b></html>"), "growx");
        detailsPanel.add(new JLabel("Grade & Section: " + gradeSection), "growx");
        detailsPanel.add(new JLabel("Age: " + age + " | Sex: " + sex), "growx");

        detailsPanel.add(new JLabel("<html><b>Violation Details</b></html>"), "growx");
        detailsPanel.add(new JLabel("Type: " + violationType), "growx");
        detailsPanel.add(new JLabel("Description: " + violationDesc), "growx");
        detailsPanel.add(new JLabel("Reinforcement: " + reinforcement), "growx");

        detailsPanel.add(new JLabel("<html><b>Anecdotal Record</b></html>"), "growx");
        detailsPanel.add(new JLabel(anecdotalRecord), "growx");

        detailsPanel.add(new JLabel("<html><b>Related Sessions</b></html>"), "growx");
        if (sessions.length == 0) detailsPanel.add(new JLabel("No sessions recorded"), "growx");
        for (String session : sessions) {
            detailsPanel.add(new JLabel(session), "growx");
        }

        detailsPanel.add(new JLabel("<html><b>Related Incidents</b></html>"), "growx");
        if (incidents.length == 0) detailsPanel.add(new JLabel("No incidents recorded"), "growx");
        for (String incident : incidents) {
            detailsPanel.add(new JLabel(incident), "growx");
        }

        // Click to Expand/Collapse
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean isVisible = detailsPanel.isVisible();
                detailsPanel.setVisible(!isVisible);
                panel.revalidate();
                panel.repaint();
            }
        });

        headerPanel.add(nameLabel, "split 2, growx");
        headerPanel.add(statusLabel, "right");
        headerPanel.add(detailsLabel, "span, growx");

        panel.add(headerPanel, "growx");
        panel.add(detailsPanel, "growx, hidemode 3"); // Ensures it collapses properly

        recordPanels.add(panel);
        contentPane.add(panel, "growx");
    }


    private Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending": return new Color(255, 230, 128); // Yellow
            case "resolved": return new Color(144, 238, 144); // Green
            case "escalated": return new Color(255, 102, 102); // Red
            default: return Color.GRAY;
        }
    }
}
