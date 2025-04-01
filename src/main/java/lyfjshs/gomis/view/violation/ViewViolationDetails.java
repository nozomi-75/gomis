package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
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
    private IncidentsDAO incidentsDAO;
    private SessionsDAO sessionsDAO;
    private List<Incident> incidents;
    private List<Sessions> sessions;

    public ViewViolationDetails(Connection conn,
                                Violation violation,
                                StudentsDataDAO studentsDataDAO,
                                ParticipantsDAO participantsDAO) {
        this.violation = violation;
        this.studentsDataDAO = studentsDataDAO;
        this.participantsDAO = participantsDAO;
        this.incidentsDAO = new IncidentsDAO(conn);
        this.sessionsDAO = new SessionsDAO(conn);
        
        // Fetch incidents and sessions data
        try {
            this.incidents = incidentsDAO.getIncidentsByParticipant(violation.getParticipantId());
            this.sessions = sessionsDAO.getAllSessions().stream()
                .filter(s -> s.getViolationId() != null && s.getViolationId() == violation.getViolationId())
                .collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Main panel layout with single column that grows
        setLayout(new MigLayout("insets 0, fill", "[grow]", "[grow]"));
        putClientProperty(FlatClientProperties.STYLE, "arc: 8");

        // Content panel with proper spacing
        JPanel mainPanel = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]15[]15[]15[]15[]"));
        mainPanel.setOpaque(false);

        // Status and Date Panel with flexible layout
        JPanel statusDatePanel = new JPanel(new MigLayout("insets 0", "[grow][]", ""));
        statusDatePanel.setOpaque(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String formattedDate = dateFormat.format(violation.getUpdatedAt());
        
        JLabel dateLabel = new JLabel("Date Recorded: " + formattedDate);
        dateLabel.putClientProperty("FlatLaf.style", "font: 14 $medium.font");

        JLabel statusLabel = new JLabel("Status: " + violation.getStatus());
        statusLabel.putClientProperty("FlatLaf.style", "font: 14 $medium.font");
        statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UIManager.getColor("Component.accentColor"), 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(UIManager.getColor("Component.accentColor"));
        statusLabel.setForeground(Color.WHITE);

        statusDatePanel.add(dateLabel, "growx");
        statusDatePanel.add(statusLabel);
        
        mainPanel.add(statusDatePanel, "growx");
        mainPanel.add(createStudentInfoSection(), "growx");
        mainPanel.add(createViolationInfoSection(), "growx");
        mainPanel.add(createIncidentDetailsSection(), "growx");
        mainPanel.add(createCounselingSessionsSection(), "growx");

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        add(scrollPane, "grow");
    }

    private JPanel createStudentInfoSection() {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]15[]"));
        section.setOpaque(false);

        JLabel title = new JLabel("Student Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(58, 86, 167));
        section.add(title, "growx");

        JPanel line = new JPanel();
        line.setBackground(new Color(58, 86, 167));
        section.add(line, "growx, height 2!");

        try {
            Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
            if (participant != null) {
                String fullName = participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
                String contactNumber = participant.getContactNumber();

                JPanel namePanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]5[]"));
                namePanel.setOpaque(false);

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
                section.add(namePanel, "growx");

                JPanel gridPanel = new JPanel(new MigLayout("wrap 2, insets 0", "[grow,fill][grow,fill]", "[]15[]"));
                gridPanel.setOpaque(false);

                if ("Student".equals(participant.getParticipantType())) {
                    Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                    if (student != null) {
                        gridPanel.add(createInfoGroup("Grade & Section", student.getSchoolSection()));
                        gridPanel.add(createInfoGroup("Sex", student.getStudentSex()));
                        gridPanel.add(createInfoGroup("Age", String.valueOf(calculateAge(student.getStudentBirthdate()))));
                        gridPanel.add(createInfoGroup("Contact Number", contactNumber));
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
        group.setOpaque(false);

        JLabel infoLabel = new JLabel(label);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoLabel.setForeground(UIManager.getColor("Label.foreground"));
        group.add(infoLabel);

        JPanel valuePanel = new JPanel(new MigLayout("insets 10", "[grow]", "[]"));
        valuePanel.setBackground(UIManager.getColor("TextField.background"));
        valuePanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, UIManager.getColor("Component.accentColor")));
        
        JLabel valueLabel = new JLabel(value != null ? value : "");
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        valueLabel.setForeground(UIManager.getColor("Label.foreground"));
        valuePanel.add(valueLabel, "growx");
        group.add(valuePanel, "growx");

        return group;
    }

    private JPanel createViolationInfoSection() {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]15[]"));
        section.setOpaque(false);

        JLabel title = new JLabel("Violation Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIManager.getColor("Component.accentColor"));
        section.add(title, "growx");

        JPanel line = new JPanel();
        line.setBackground(UIManager.getColor("Component.accentColor"));
        section.add(line, "growx, height 2!");

        JLabel typeLabel = new JLabel("Violation Type");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        typeLabel.setForeground(UIManager.getColor("Label.foreground"));
        section.add(typeLabel);

        JPanel badgePanel = new JPanel(new MigLayout("insets 10", "[grow]", "[]"));
        badgePanel.setOpaque(false);
        badgePanel.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, UIManager.getColor("Component.accentColor")));
        
        JLabel violationBadge = new JLabel(violation.getViolationType());
        violationBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        violationBadge.setForeground(Color.WHITE);
        violationBadge.setBackground(UIManager.getColor("Component.accentColor"));
        violationBadge.setOpaque(true);
        violationBadge.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        badgePanel.add(violationBadge, "growx");
        section.add(badgePanel, "growx");

        return section;
    }

    private JPanel createIncidentDetailsSection() {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]15[]"));
        section.setOpaque(false);

        JLabel title = new JLabel("Incident Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIManager.getColor("Component.accentColor"));
        section.add(title, "growx");

        JPanel line = new JPanel();
        line.setBackground(UIManager.getColor("Component.accentColor"));
        section.add(line, "growx, height 2!");

        if (incidents != null && !incidents.isEmpty()) {
            for (Incident incident : incidents) {
                JPanel incidentBox = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]10[]10[]"));
                incidentBox.setOpaque(false);
                incidentBox.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, UIManager.getColor("Component.accentColor")));

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                String formattedDate = dateFormat.format(incident.getIncidentDate());
                
                JLabel dateLabel = new JLabel("Incident Date: " + formattedDate);
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                dateLabel.setForeground(UIManager.getColor("Label.foreground"));
                incidentBox.add(dateLabel, "growx");

                JLabel descLabel = new JLabel("Incident Description");
                descLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                descLabel.setForeground(UIManager.getColor("Label.foreground"));
                incidentBox.add(descLabel, "growx 5");

                JTextArea descText = new JTextArea(incident.getIncidentDescription());
                descText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                descText.setLineWrap(true);
                descText.setWrapStyleWord(true);
                descText.setEditable(false);
                descText.setBackground(UIManager.getColor("TextField.background"));
                descText.setForeground(UIManager.getColor("TextField.foreground"));
                descText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                incidentBox.add(descText, "growx 10");

                JLabel actionLabel = new JLabel("Action Taken");
                actionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                actionLabel.setForeground(UIManager.getColor("Label.foreground"));
                incidentBox.add(actionLabel, "growx 5");

                JTextArea actionText = new JTextArea(incident.getActionTaken());
                actionText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                actionText.setLineWrap(true);
                actionText.setWrapStyleWord(true);
                actionText.setEditable(false);
                actionText.setBackground(UIManager.getColor("TextField.background"));
                actionText.setForeground(UIManager.getColor("TextField.foreground"));
                actionText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                incidentBox.add(actionText, "growx 10");

                section.add(incidentBox, "growx 15");
            }
        } else {
            JLabel noIncidentLabel = new JLabel("No incident reports found for this violation.");
            noIncidentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noIncidentLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            section.add(noIncidentLabel, "growx 15");
        }

        return section;
    }

    private JPanel createCounselingSessionsSection() {
        JPanel section = new JPanel(new MigLayout("wrap, insets 0", "[grow]", "[]5[]15[]"));
        section.setOpaque(false);

        JLabel title = new JLabel("Counseling Sessions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIManager.getColor("Component.accentColor"));
        section.add(title, "growx");

        JPanel line = new JPanel();
        line.setBackground(UIManager.getColor("Component.accentColor"));
        section.add(line, "growx, height 2!");

        if (sessions != null && !sessions.isEmpty()) {
            for (Sessions session : sessions) {
                JPanel sessionBox = new JPanel(new MigLayout("wrap, insets 15", "[grow]", "[]10[]10[]10[]"));
                sessionBox.setOpaque(false);
                sessionBox.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, UIManager.getColor("Component.accentColor")));

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                String formattedDate = dateFormat.format(session.getSessionDateTime());
                
                JLabel dateLabel = new JLabel("Session Date: " + formattedDate);
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                dateLabel.setForeground(UIManager.getColor("Label.foreground"));
                sessionBox.add(dateLabel, "growx");

                JLabel summaryLabel = new JLabel("Session Summary");
                summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                summaryLabel.setForeground(UIManager.getColor("Label.foreground"));
                sessionBox.add(summaryLabel, "growx 5");

                JTextArea summaryText = new JTextArea(session.getSessionSummary());
                summaryText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                summaryText.setLineWrap(true);
                summaryText.setWrapStyleWord(true);
                summaryText.setEditable(false);
                summaryText.setBackground(UIManager.getColor("TextField.background"));
                summaryText.setForeground(UIManager.getColor("TextField.foreground"));
                summaryText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                sessionBox.add(summaryText, "growx 10");

                JLabel notesLabel = new JLabel("Session Notes");
                notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                notesLabel.setForeground(UIManager.getColor("Label.foreground"));
                sessionBox.add(notesLabel, "growx 5");

                JTextArea notesText = new JTextArea(session.getSessionNotes());
                notesText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                notesText.setLineWrap(true);
                notesText.setWrapStyleWord(true);
                notesText.setEditable(false);
                notesText.setBackground(UIManager.getColor("TextField.background"));
                notesText.setForeground(UIManager.getColor("TextField.foreground"));
                notesText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                sessionBox.add(notesText, "growx 10");

                section.add(sessionBox, "growx 15");
            }
        } else {
            JLabel noSessionLabel = new JLabel("No counseling sessions found for this violation.");
            noSessionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            noSessionLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            section.add(noSessionLabel, "growx 15");
        }

        return section;
    }

    private int calculateAge(Date birthdate) {
        if (birthdate == null) return 0;
        LocalDate birthDate = birthdate.toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public static void showDialog(Component parent,
                                  Connection conn,
                                  Violation violation,
                                  StudentsDataDAO studentsDataDAO,
                                  ParticipantsDAO participantsDAO) {
        ViewViolationDetails detailsPanel = new ViewViolationDetails(
                conn, violation, studentsDataDAO, participantsDAO
        );

        Option option = ModalDialog.createOption();
        option.setOpacity(0.3f)
              .setAnimationOnClose(false)
              .getBorderOption()
              .setBorderWidth(0f)
              .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

        String modalId = "violation_details_" + violation.getViolationId();
        if (ModalDialog.isIdExist(modalId)) {
            ModalDialog.closeModal(modalId);
        }

        // Set dialog size with proper dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(600, screenSize.width - 100);
        int height = Math.min(700, screenSize.height - 100);
        option.getLayoutOption().setSize(width, height);

        ModalDialog.showModal(
                parent,
                new SimpleModalBorder(
                        detailsPanel,
                        "Violation Details",
                        new SimpleModalBorder.Option[]{
                                new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
                        },
                        (controller, action) -> {
                            if (action == SimpleModalBorder.CLOSE_OPTION) {
                                controller.close();
                            }
                        }
                ),
                modalId
        );
    }
}
