package lyfjshs.gomis.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout; // Add this import

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

public class ViolationRecordForm extends JPanel {

    private JTextField txtViolationType;
    private JTextField txtStatus;
    private JTextField txtUpdatedAt;
    private JTextArea txtDescription;
    private JTextArea txtAnecdotal;
    private JTextField txtFullName;
    private JTextField txtStudentLRN;
    private JTextField txtAge;
    private JTextField txtSex;
    private JTextField txtGradeSection;
    private JTextField txtContactNumber;
    private JTextField txtReinforcementType;
    private JTextField txtActionRecommended;
    private JTextArea txtActions;

    public ViolationRecordForm() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new MigLayout("wrap, insets 30", "[grow]", "[]"));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel sectionTitle1 = new JLabel("Violation Record Details");
        sectionTitle1.setFont(new Font("Arial", Font.BOLD, 16));
        sectionTitle1.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle1, "spanx,growx,wrap 15");

        JPanel violationGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
        violationGrid.setBackground(Color.WHITE);

 
        txtViolationType = new JTextField("Classroom Disruption", 20);
        txtStatus = new JTextField("Pending Resolution", 20);
        txtUpdatedAt = new JTextField("2024-03-25 14:30:00", 20);

        addLabelValuePair(violationGrid, "Violation Type:", txtViolationType);
        addLabelValuePair(violationGrid, "Status:", txtStatus);
        addLabelValuePair(violationGrid, "Updated At:", txtUpdatedAt);

        mainPanel.add(violationGrid, "span, wrap 15");

        JLabel sectionTitle2 = new JLabel("Violation Description");
        sectionTitle2.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle2.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle2, "spanx,growx,wrap 10");

        txtDescription = new JTextArea(
                "Student repeatedly interrupted class, used inappropriate language, and displayed disrespectful behavior towards the teacher and fellow students. Multiple warnings were issued before this formal violation record.");
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDescription.setBackground(Color.WHITE);
        mainPanel.add(new JScrollPane(txtDescription), "span, growx, wrap 15");

        JLabel sectionTitle3 = new JLabel("Anecdotal Record");
        sectionTitle3.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle3.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle3, "spanx,growx,wrap 10");

        txtAnecdotal = new JTextArea(
                "On March 20, 2024, during Mathematics class, the student was observed making disruptive comments, using profanity, and preventing other students from focusing on the lesson. The teacher attempted to redirect the behavior multiple times without success.");
        txtAnecdotal.setLineWrap(true);
        txtAnecdotal.setWrapStyleWord(true);
        txtAnecdotal.setEditable(false);
        txtAnecdotal.setFont(new Font("Arial", Font.PLAIN, 12));
        txtAnecdotal.setBackground(Color.WHITE);
        mainPanel.add(new JScrollPane(txtAnecdotal), "span, growx, wrap 15");

        JLabel sectionTitle4 = new JLabel("Student Information");
        sectionTitle4.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle4.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle4, "spanx,growx,wrap 10");

        JPanel studentGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
        studentGrid.setBackground(Color.WHITE);

        txtFullName = new JTextField("Juan Miguel Santos", 20);
        txtStudentLRN = new JTextField("2024-0987-6543", 20);
        txtAge = new JTextField("16", 20);
        txtSex = new JTextField("Male", 20);
        txtGradeSection = new JTextField("11-Rizal", 20);
        txtContactNumber = new JTextField("+63 912 345 6789", 20);

        addLabelValuePair(studentGrid, "Full Name:", txtFullName);
        addLabelValuePair(studentGrid, "Student LRN:", txtStudentLRN);
        addLabelValuePair(studentGrid, "Age:", txtAge);
        addLabelValuePair(studentGrid, "Sex:", txtSex);
        addLabelValuePair(studentGrid, "Grade/Section:", txtGradeSection);
        addLabelValuePair(studentGrid, "Contact Number:", txtContactNumber);

        mainPanel.add(studentGrid, "span, wrap 15");

        JLabel sectionTitle5 = new JLabel("Reinforcement & Recommendations");
        sectionTitle5.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle5.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 123, 255)));
        mainPanel.add(sectionTitle5, "spanx,growx,wrap 10");

        JPanel reinforcementGrid = new JPanel(new MigLayout("wrap 2", "[right]10[left]", "[]5[]"));
        reinforcementGrid.setBackground(Color.WHITE);

        txtReinforcementType = new JTextField("Disciplinary Counseling", 20);
        txtActionRecommended = new JTextField("Mandatory Guidance Counseling", 20);

        addLabelValuePair(reinforcementGrid, "Reinforcement Type:", txtReinforcementType);
        addLabelValuePair(reinforcementGrid, "Action Recommended:", txtActionRecommended);

        mainPanel.add(reinforcementGrid, "span, wrap 10");

        JLabel recommendedActionsLabel = new JLabel("Recommended actions include:");
        recommendedActionsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(recommendedActionsLabel, "span, wrap 5");

        txtActions = new JTextArea(
                "• Conduct a one-on-one counseling session\n" +
                        "• Develop a behavior improvement plan\n" +
                        "• Involve parents/guardians in the intervention\n" +
                        "• Monitor progress and provide ongoing support");
        txtActions.setEditable(false);
        txtActions.setFont(new Font("Arial", Font.PLAIN, 12));
        txtActions.setBackground(Color.WHITE);
        mainPanel.add(new JScrollPane(txtActions), "span, growx");

        setLayout(new BorderLayout());
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }

    private void addLabelValuePair(JPanel panel, String labelText, JTextField valueField) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(85, 85, 85));
        valueField.setEditable(false);
        valueField.setFont(new Font("Arial", Font.PLAIN, 12));
        valueField.setBackground(Color.WHITE);
        panel.add(label);
        panel.add(valueField, "growx");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Violation Record Details");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new ViolationRecordForm());
            frame.setVisible(true);
        });
    }
}