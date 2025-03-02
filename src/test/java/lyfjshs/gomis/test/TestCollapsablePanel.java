package lyfjshs.gomis.test;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class TestCollapsablePanel extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TestCollapsablePanel frame = new TestCollapsablePanel();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TestCollapsablePanel() {
        setTitle("Collapsible Panels Example");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 550, 600);

        contentPane = new JPanel(new MigLayout("fillx, wrap 1", "[grow]"));
        JScrollPane scrollPane = new JScrollPane(contentPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Add student records with collapsible behavior
        addCollapsiblePanel("Juan Carlos Dela Cruz", "LRN: 136752180058 | Academic | Feb 15, 2025", "Pending", contentPane);
        addCollapsiblePanel("Maria Santos Reyes", "LRN: 136752180042 | Behavioral | Jan 27, 2025", "Resolved", contentPane);
        addCollapsiblePanel("Antonio Villanueva", "LRN: 136752180039 | Attendance | Feb 8, 2025", "Escalated", contentPane);
        addCollapsiblePanel("Sofia Mendoza", "LRN: 136752180076 | Dress Code | Jan 15, 2025", "Resolved", contentPane);

        setContentPane(scrollPane);
    }

    private void addCollapsiblePanel(String name, String details, String status, JPanel parent) {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]"));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setBackground(Color.WHITE);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 5", "[grow]"));
        headerPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(status));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsLabel.setForeground(Color.DARK_GRAY);

        JPanel detailsPanel = new JPanel(new MigLayout("fillx, wrap 1", "[grow]"));
        detailsPanel.setVisible(false);
        detailsPanel.setBackground(Color.LIGHT_GRAY);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel infoLabel = new JLabel("<html><b>Violation Details:</b><br>Sample description of the student's violation.</html>");
        detailsPanel.add(infoLabel);

        headerPanel.add(nameLabel, "split 2, growx");
        headerPanel.add(statusLabel, "right");
        headerPanel.add(detailsLabel, "span, growx");

        panel.add(headerPanel, "growx");
        panel.add(detailsPanel, "growx");

        // Add MouseListener to toggle visibility of details panel
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                detailsPanel.setVisible(!detailsPanel.isVisible());
                parent.revalidate();
                parent.repaint();
            }
        });

        parent.add(panel, "growx");
    }

    private Color getStatusColor(String status) {
        switch (status) {
            case "Pending":
                return new Color(255, 230, 128); // Yellow
            case "Resolved":
                return new Color(144, 238, 144); // Light Green
            case "Escalated":
                return new Color(255, 102, 102); // Red
            default:
                return Color.GRAY;
        }
    }
}
