package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Option;

public class ViewViolationDetails extends JPanel {
    private Violation violation;
    private StudentsDataDAO studentsDataDAO;
    private ParticipantsDAO participantsDAO;

    public ViewViolationDetails(Violation violation, StudentsDataDAO studentsDataDAO,
            ParticipantsDAO participantsDAO) {
        this.violation = violation;
        this.studentsDataDAO = studentsDataDAO;
        this.participantsDAO = participantsDAO;
        setLayout(new MigLayout("wrap, insets 20", "[grow]", "[]"));
        putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        // Main container panel
        JPanel mainPanel = new JPanel(new MigLayout("wrap 1, fillx", "[grow][grow]", "[]20[200px]20[][][]"));
        mainPanel.setBackground(UIManager.getColor("Panel.background"));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Status and Date Panel
        JPanel statusDatePanel = new JPanel(new MigLayout("insets 0, gap 10", "[left, grow][right]", ""));
        statusDatePanel.setBackground(Color.WHITE);
        
        JLabel dateLabel = new JLabel("Date Recorded: " + violation.getUpdatedAt());
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel statusLabel = new JLabel("Status: " + violation.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(new Color(214, 69, 65));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(214, 69, 65), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(255, 240, 240));
        
        statusDatePanel.add(dateLabel, "growx");
        statusDatePanel.add(statusLabel, "right");
        mainPanel.add(statusDatePanel, "growx, gapbottom 20");

        // Student Information Section
        mainPanel.add(createStudentInfoSection(), "growx");

        // Violation Information Section
        mainPanel.add(createViolationInfoSection(), "growx");

        // Incident Details Section
        mainPanel.add(createIncidentDetailsSection(), "growx");

        // Counseling Sessions Section
        mainPanel.add(createCounselingSessionsSection(), "growx");

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
        mainPanel.add(buttonPanel, "growx");

        add(mainPanel, "grow");
    }

    private JPanel createStudentInfoSection() {
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

        try {
            Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
            if (participant != null) {
                String fullName = participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
                String contactNumber = participant.getContactNumber();
                
                JPanel namePanel = new JPanel(new MigLayout("insets 0", "[grow]", ""));
                namePanel.setBackground(Color.WHITE);
                JLabel nameLabel = new JLabel(fullName);
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                namePanel.add(nameLabel, "wrap");
                
                if ("Student".equals(participant.getParticipantType())) {
                    Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                    if (student != null) {
                        JLabel lrnLabel = new JLabel("LRN: " + student.getStudentLrn());
                        lrnLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        lrnLabel.setForeground(new Color(102, 102, 102));
                        namePanel.add(lrnLabel);
                    }
                }
                section.add(namePanel, "growx, wrap 20");

                JPanel gridPanel = new JPanel(new MigLayout("wrap 2, insets 0", "[grow][grow]", "[]15[]"));
                gridPanel.setBackground(Color.WHITE);

                if ("Student".equals(participant.getParticipantType())) {
                    Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                    if (student != null) {
                        gridPanel.add(createInfoGroup("Grade & Section", student.getSchoolSection()), "growx");
                        gridPanel.add(createInfoGroup("Sex", student.getStudentSex()), "growx");
                        gridPanel.add(createInfoGroup("Age", String.valueOf(calculateAge(student.getStudentBirthdate()))), "growx");
                        gridPanel.add(createInfoGroup("Contact Number", contactNumber), "growx");
                    }
                }

                section.add(gridPanel, "growx");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        JLabel valueLabel = new JLabel(value != null ? value : "");
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valuePanel.add(valueLabel);
        group.add(valuePanel, "growx");

        return group;
    }

    private JPanel createViolationInfoSection() {
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
        JLabel violationBadge = new JLabel(violation.getViolationType());
        violationBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        violationBadge.setForeground(Color.WHITE);
        violationBadge.setBackground(Color.RED);
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
        JTextArea description = new JTextArea(violation.getViolationDescription());
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

    private JPanel createIncidentDetailsSection() {
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
        JTextArea actionTaken = new JTextArea(violation.getViolationDescription());
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

    private JPanel createCounselingSessionsSection() {
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

        // Create a single session box for the current violation
        JPanel sessionBox = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]15[]"));
        sessionBox.setBackground(new Color(249, 249, 249));
        sessionBox.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(58, 86, 167)));

        JLabel dateLabel = new JLabel("Session Date: " + violation.getUpdatedAt());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(119, 119, 119));
        sessionBox.add(dateLabel);

        JLabel summaryLabel = new JLabel("Session Summary");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        summaryLabel.setForeground(new Color(85, 85, 85));
        sessionBox.add(summaryLabel, "wrap 5");

        JTextArea summaryText = new JTextArea(violation.getSessionSummary());
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

        JTextArea notesText = new JTextArea(violation.getReinforcement());
        notesText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notesText.setLineWrap(true);
        notesText.setWrapStyleWord(true);
        notesText.setEditable(false);
        notesText.setBackground(new Color(249, 249, 249));
        notesText.setBorder(BorderFactory.createEmptyBorder());
        sessionBox.add(notesText, "growx");

        section.add(sessionBox, "growx, wrap 15");

        return section;
    }

    private int calculateAge(Date birthdate) {
        if (birthdate == null) return 0;
        LocalDate birthDate = birthdate.toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public static void showDialog(Component parent, Violation violation, StudentsDataDAO studentsDataDAO,
            ParticipantsDAO participantsDAO) {
        ViewViolationDetails detailsPanel = new ViewViolationDetails(violation, studentsDataDAO, participantsDAO);

        Option option = ModalDialog.createOption();
        option.setOpacity(0.3f).setAnimationOnClose(false).getBorderOption().setBorderWidth(0f)
                .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

        String modalId = "violation_details_" + violation.getViolationId();
        if (ModalDialog.isIdExist(modalId)) {
            ModalDialog.closeModal(modalId);
        }
        option.getLayoutOption().setSize(800, 600);

        ModalDialog.showModal(parent,
                new SimpleModalBorder(detailsPanel, "Violation Details",
                        new SimpleModalBorder.Option[] {
                                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION) },
                        (controller, action) -> {
                            if (action == SimpleModalBorder.CLOSE_OPTION) {
                                controller.close();
                            }
                        }),
                modalId);
    }
} 