package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

public class ViolationDetailsApp extends JFrame {

    public ViolationDetailsApp() {
        // Set up the JFrame
        setTitle("Violation Details");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);

        // Main container panel with MigLayout
        JPanel container = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[]"));
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 20)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Content panel wrapper for scrollable content
        JPanel contentWrapper = new JPanel(new MigLayout("fill", "[grow]", "[]"));
        contentWrapper.setBackground(Color.WHITE);

        // Content panel for the violation
        JPanel contentPanel = createContentPanel();
        contentWrapper.add(contentPanel, "grow");

        // Configure JScrollPane properly
        JScrollPane scrollPane = new JScrollPane(contentWrapper);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Ensure the contentWrapper doesn't stretch beyond its preferred size
        contentWrapper.setMinimumSize(new Dimension(950, 0)); // Minimum width to prevent shrinking
        contentWrapper.setPreferredSize(new Dimension(950, contentWrapper.getPreferredSize().height));

        container.add(scrollPane, "grow");

        getContentPane().add(container);

        // Initial update of scroll pane size
        updateScrollPane(scrollPane, contentWrapper);
    }

    private void updateScrollPane(JScrollPane scrollPane, JPanel contentWrapper) {
        // Force revalidation of the content wrapper to update its preferred size
        contentWrapper.revalidate();
        // Update the preferred size based on the visible content
        contentWrapper.setPreferredSize(new Dimension(950, contentWrapper.getPreferredSize().height));
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap, insets 20", "[grow, fill]", "[]"));
        contentPanel.setBackground(Color.WHITE);

        // Status and Date Panel (Moved to the top with improved visibility)
        JPanel statusDatePanel = new JPanel(new MigLayout("insets 0, gap 10", "[left, grow][right]", ""));
        statusDatePanel.setBackground(Color.WHITE);
        
        // Date Label
        JLabel dateLabel = new JLabel("Date Recorded: April 1, 2025");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Status Label with improved visibility
        JLabel statusLabel = new JLabel("Status: Ongoing");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(214, 69, 65)); // Red color for visibility
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 69, 65), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(255, 240, 240)); // Light red background
        
        statusDatePanel.add(dateLabel, "growx");
        statusDatePanel.add(statusLabel, "right");
        contentPanel.add(statusDatePanel, "growx, gapbottom 20");

        // Student Information Section
        contentPanel.add(createStudentInfoSection(
            "Juan Dela Cruz", "123456789012", "Grade 10 - Einstein", "Male", "16", "09123456789"
        ), "growx");

        // Violation Information Section
        contentPanel.add(createViolationInfoSection(
            "Bullying", Color.RED,
            "Student was involved in verbal bullying of another student during lunch break. " +
            "Witnesses reported hearing derogatory comments and threats being made. " +
            "This is the second reported incident this semester."
        ), "growx");

        // Incident Details Section
        contentPanel.add(createIncidentDetailsSection(
            "1. Immediate intervention by supervising teacher.\n" +
            "2. Both students separated and brought to guidance office.\n" +
            "3. Parents of both students notified via phone call.\n" +
            "4. Initial counseling session conducted.\n" +
            "5. Incident documented and filed according to school policy."
        ), "growx");

        // Counseling Sessions Section
        contentPanel.add(createCounselingSessionsSection(
            new String[]{"March 25, 2025", "April 1, 2025"},
            new String[]{
                "Student expressed remorse for actions. Discussed underlying issues including academic pressure and family concerns. Student agreed to participate in weekly check-ins and anti-bullying program.",
                "Follow-up session showed improvement in attitude. Student completed first worksheet on empathy building. Parents report improved behavior at home as well. Will continue with scheduled program."
            },
            new String[]{
                "Student was initially resistant to discussing the incident but opened up after establishing rapport. Identified triggers for behavior and developed initial coping strategies. Recommended continued individual counseling and possible group intervention.",
                "Student demonstrated improved self-awareness and showed genuine interest in making amends. Discussed specific strategies for managing anger and frustration. Role-played appropriate responses to triggering situations. Next session will focus on conflict resolution skills."
            }
        ), "growx");

        // Action Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 20", "[right]", ""));
        buttonPanel.setBackground(Color.WHITE);
        JButton resolveButton = new JButton("Resolve");
        resolveButton.setBackground(new Color(58, 86, 167));
        resolveButton.setForeground(Color.WHITE);
        resolveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JButton closeButton = new JButton("Close");
        closeButton.setBackground(new Color(241, 241, 241));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        buttonPanel.add(resolveButton, "gapx 10");
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, "growx");

        return contentPanel;
    }

    private JPanel createStudentInfoSection(String name, String lrn, String gradeSection, String sex, String age, String contact) {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", ""));
        section.setBackground(Color.WHITE);

        JLabel title = new JLabel("Student Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(58, 86, 167));
        section.add(title, "growx");
        
        JPanel line = new JPanel();
        line.setBackground(new Color(58, 86, 167));
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        section.add(line, "growx, wrap 15");

        JPanel namePanel = new JPanel(new MigLayout("insets 0", "[grow]", ""));
        namePanel.setBackground(Color.WHITE);
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        namePanel.add(nameLabel, "wrap");
        JLabel lrnLabel = new JLabel("LRN: " + lrn);
        lrnLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lrnLabel.setForeground(new Color(102, 102, 102));
        namePanel.add(lrnLabel);
        section.add(namePanel, "growx, wrap 20");

        JPanel gridPanel = new JPanel(new MigLayout("wrap 2, insets 0", "[grow][grow]", "[]15[]"));
        gridPanel.setBackground(Color.WHITE);

        // Grade & Section
        gridPanel.add(createInfoGroup("Grade & Section", gradeSection), "growx");
        // Sex
        gridPanel.add(createInfoGroup("Sex", sex), "growx");
        // Age
        gridPanel.add(createInfoGroup("Age", age), "growx");
        // Contact Number
        gridPanel.add(createInfoGroup("Contact Number", contact), "growx");

        section.add(gridPanel, "growx");
        return section;
    }

    private JPanel createInfoGroup(String label, String value) {
        JPanel group = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]"));
        group.setBackground(Color.WHITE);

        JLabel infoLabel = new JLabel(label);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoLabel.setForeground(new Color(85, 85, 85));
        group.add(infoLabel);

        JPanel valuePanel = new JPanel(new MigLayout("insets 10", "[grow]", ""));
        valuePanel.setBackground(new Color(249, 249, 249));
        valuePanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(58, 86, 167)));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valuePanel.add(valueLabel);
        group.add(valuePanel, "growx");

        return group;
    }

    private JPanel createViolationInfoSection(String violationType, Color badgeColor, String descriptionText) {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", ""));
        section.setBackground(Color.WHITE);

        JLabel title = new JLabel("Violation Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(58, 86, 167));
        section.add(title, "growx");
        
        JPanel line = new JPanel();
        line.setBackground(new Color(58, 86, 167));
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        section.add(line, "growx, wrap 15");

        // Violation Type
        JLabel typeLabel = new JLabel("Violation Type");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        typeLabel.setForeground(new Color(85, 85, 85));
        section.add(typeLabel, "wrap 5");

        JPanel badgePanel = new JPanel(new MigLayout("insets 10", "[grow]", ""));
        badgePanel.setBackground(new Color(249, 249, 249));
        badgePanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(58, 86, 167)));
        JLabel violationBadge = new JLabel(violationType);
        violationBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        violationBadge.setForeground(Color.WHITE);
        violationBadge.setBackground(badgeColor);
        violationBadge.setOpaque(true);
        violationBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        badgePanel.add(violationBadge);
        section.add(badgePanel, "growx, wrap 15");

        // Violation Description
        JLabel descLabel = new JLabel("Violation Description");
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descLabel.setForeground(new Color(85, 85, 85));
        section.add(descLabel, "wrap 5");

        JPanel descPanel = new JPanel(new MigLayout("insets 10", "[grow]", ""));
        descPanel.setBackground(new Color(249, 249, 249));
        descPanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(58, 86, 167)));
        JTextArea description = new JTextArea(descriptionText);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setBackground(new Color(249, 249, 249));
        description.setBorder(BorderFactory.createEmptyBorder());
        descPanel.add(description, "grow");
        section.add(descPanel, "growx, wrap 20");

        return section;
    }

    private JPanel createIncidentDetailsSection(String actionText) {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", ""));
        section.setBackground(Color.WHITE);

        JLabel title = new JLabel("Incident Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(58, 86, 167));
        section.add(title, "growx");
        
        JPanel line = new JPanel();
        line.setBackground(new Color(58, 86, 167));
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        section.add(line, "growx, wrap 15");

        JLabel actionLabel = new JLabel("Action Taken");
        actionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionLabel.setForeground(new Color(85, 85, 85));
        section.add(actionLabel, "wrap 5");

        JPanel actionPanel = new JPanel(new MigLayout("insets 10", "[grow]", ""));
        actionPanel.setBackground(new Color(249, 249, 249));
        actionPanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(58, 86, 167)));
        JTextArea actionTaken = new JTextArea(actionText);
        actionTaken.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        actionTaken.setLineWrap(true);
        actionTaken.setWrapStyleWord(true);
        actionTaken.setEditable(false);
        actionTaken.setBackground(new Color(249, 249, 249));
        actionTaken.setBorder(BorderFactory.createEmptyBorder());
        actionPanel.add(actionTaken, "grow");
        section.add(actionPanel, "growx, wrap 20");

        return section;
    }

    private JPanel createCounselingSessionsSection(String[] dates, String[] summaries, String[] notes) {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", ""));
        section.setBackground(Color.WHITE);

        JLabel title = new JLabel("Counseling Sessions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(58, 86, 167));
        section.add(title, "growx");
        
        JPanel line = new JPanel();
        line.setBackground(new Color(58, 86, 167));
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 2));
        section.add(line, "growx, wrap 15");

        for (int i = 0; i < dates.length; i++) {
            JPanel sessionBox = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]15[]"));
            sessionBox.setBackground(new Color(249, 249, 249));
            sessionBox.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(58, 86, 167)));

            JLabel dateLabel = new JLabel("Session Date: " + dates[i]);
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            dateLabel.setForeground(new Color(119, 119, 119));
            sessionBox.add(dateLabel);

            JLabel summaryLabel = new JLabel("Session Summary");
            summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            summaryLabel.setForeground(new Color(85, 85, 85));
            sessionBox.add(summaryLabel, "wrap 5");

            JTextArea summaryText = new JTextArea(summaries[i]);
            summaryText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            summaryText.setLineWrap(true);
            summaryText.setWrapStyleWord(true);
            summaryText.setEditable(false);
            summaryText.setBackground(new Color(249, 249, 249));
            summaryText.setBorder(BorderFactory.createEmptyBorder());
            sessionBox.add(summaryText, "growx, wrap 15");

            JLabel notesLabel = new JLabel("Session Notes");
            notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            notesLabel.setForeground(new Color(85, 85, 85));
            sessionBox.add(notesLabel, "wrap 5");

            JTextArea notesText = new JTextArea(notes[i]);
            notesText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            notesText.setLineWrap(true);
            notesText.setWrapStyleWord(true);
            notesText.setEditable(false);
            notesText.setBackground(new Color(249, 249, 249));
            notesText.setBorder(BorderFactory.createEmptyBorder());
            sessionBox.add(notesText, "growx");

            section.add(sessionBox, "growx, wrap 15");
        }

        return section;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViolationDetailsApp().setVisible(true));
    }
}