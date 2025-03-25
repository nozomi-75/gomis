package lyfjshs.gomis.test;
import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ViolationRecordForm extends JFrame {

    public ViolationRecordForm() {
        // Set FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Frame setup
        setTitle("Violation Record Details");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main container with padding and shadow effect
        JPanel mainPanel = new JPanel(new MigLayout("wrap, insets 30", "[grow]", "[]"));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Section 1: Violation Record Details
        JLabel sectionTitle1 = new JLabel("Violation Record Details");
        sectionTitle1.setFont(new Font("Arial", Font.BOLD, 16));
        sectionTitle1.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle1, "spanx,growx,wrap 15");

        JPanel violationGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
        violationGrid.setBackground(Color.WHITE);

        addLabelValuePair(violationGrid, "Violation ID:", "VR-2024-001");
        addLabelValuePair(violationGrid, "Violation Type:", "Classroom Disruption");
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(new Color(85, 85, 85));
        JLabel statusValue = new JLabel("Pending Resolution");
        statusValue.setForeground(new Color(220, 53, 69)); // Red for pending
        statusValue.setFont(new Font("Arial", Font.BOLD, 12));
        violationGrid.add(statusLabel);
        violationGrid.add(statusValue);
        addLabelValuePair(violationGrid, "Updated At:", "2024-03-25 14:30:00");

        mainPanel.add(violationGrid, "span, wrap 15");

        // Section 2: Violation Description
        JLabel sectionTitle2 = new JLabel("Violation Description");
        sectionTitle2.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle2.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle2, "spanx,growx,wrap 10");

        JTextArea description = new JTextArea(
                "Student repeatedly interrupted class, used inappropriate language, and displayed disrespectful behavior towards the teacher and fellow students. Multiple warnings were issued before this formal violation record.");
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setBackground(Color.WHITE);
        mainPanel.add(description, "span, growx, wrap 15");

        // Section 3: Anecdotal Record
        JLabel sectionTitle3 = new JLabel("Anecdotal Record");
        sectionTitle3.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle3.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle3, "spanx,growx,wrap 10");

        JTextArea anecdotal = new JTextArea(
                "On March 20, 2024, during Mathematics class, the student was observed making disruptive comments, using profanity, and preventing other students from focusing on the lesson. The teacher attempted to redirect the behavior multiple times without success.");
        anecdotal.setLineWrap(true);
        anecdotal.setWrapStyleWord(true);
        anecdotal.setEditable(false);
        anecdotal.setFont(new Font("Arial", Font.PLAIN, 12));
        anecdotal.setBackground(Color.WHITE);
        mainPanel.add(anecdotal, "span, growx, wrap 15");

        // Section 4: Student Information
        JLabel sectionTitle4 = new JLabel("Student Information");
        sectionTitle4.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle4.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle4, "spanx,growx,wrap 10");

        JPanel studentGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
        studentGrid.setBackground(Color.WHITE);

        addLabelValuePair(studentGrid, "Full Name:", "Juan Miguel Santos");
        addLabelValuePair(studentGrid, "Student LRN:", "2024-0987-6543");
        addLabelValuePair(studentGrid, "Age:", "16");
        addLabelValuePair(studentGrid, "Sex:", "Male");
        addLabelValuePair(studentGrid, "Grade/Section:", "11-Rizal");
        addLabelValuePair(studentGrid, "Contact Number:", "+63 912 345 6789");

        mainPanel.add(studentGrid, "span, wrap 15");

        // Section 5: Reinforcement & Recommendations
        JLabel sectionTitle5 = new JLabel("Reinforcement & Recommendations");
        sectionTitle5.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle5.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle5, "spanx,growx,wrap 10");

        JPanel reinforcementGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
        reinforcementGrid.setBackground(Color.WHITE);

        addLabelValuePair(reinforcementGrid, "Reinforcement Type:", "Disciplinary Counseling");
        addLabelValuePair(reinforcementGrid, "Action Recommended:", "Mandatory Guidance Counseling");

        mainPanel.add(reinforcementGrid, "span, wrap 10");

        JLabel recommendedActionsLabel = new JLabel("Recommended actions include:");
        recommendedActionsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(recommendedActionsLabel, "span, wrap 5");

        JTextArea actions = new JTextArea(
                "• Conduct a one-on-one counseling session\n" +
                        "• Develop a behavior improvement plan\n" +
                        "• Involve parents/guardians in the intervention\n" +
                        "• Monitor progress and provide ongoing support");
        actions.setEditable(false);
        actions.setFont(new Font("Arial", Font.PLAIN, 12));
        actions.setBackground(Color.WHITE);
        mainPanel.add(actions, "span, growx");

        // Add main panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        getContentPane().add(scrollPane);

        setVisible(true);
    }

    // Helper method to add label-value pairs to a grid
    private void addLabelValuePair(JPanel panel, String labelText, String valueText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(85, 85, 85));
        JLabel value = new JLabel(valueText);
        value.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(label);
        panel.add(value);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ViolationRecordForm::new);
    }
}