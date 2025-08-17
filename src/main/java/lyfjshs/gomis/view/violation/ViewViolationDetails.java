/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

/**
 * A panel that displays detailed information about a specific violation record.
 * This view has been redesigned for better clarity, includes more complete data
 * based on the provided database schema, and ensures correct age calculation.
 */
public class ViewViolationDetails extends JPanel {

    private static final Logger logger = LogManager.getLogger(ViewViolationDetails.class);

    private final Violation violation;
    private final StudentsDataDAO studentsDataDAO;
    private final ParticipantsDAO participantsDAO;
    private final IncidentsDAO incidentsDAO;
    private final SessionsDAO sessionsDAO;
    private final ViolationDAO violationDAO;

    private List<Incident> incidents;
    private List<Sessions> sessions;

    public ViewViolationDetails(Connection conn, Violation violation, StudentsDataDAO studentsDataDAO, ParticipantsDAO participantsDAO) {
        this.violation = violation;
        this.studentsDataDAO = studentsDataDAO;
        this.participantsDAO = participantsDAO;
        // Initialize all DAOs here
        this.incidentsDAO = new IncidentsDAO(conn);
        this.sessionsDAO = new SessionsDAO(conn);
        this.violationDAO = new ViolationDAO(conn);

        fetchRelatedData();
        initUI();
    }

    /**
     * Fetches all data related to the violation from the database.
     */
    private void fetchRelatedData() {
        try {
            this.incidents = incidentsDAO.getIncidentsByParticipant(violation.getParticipantId());
            this.sessions = sessionsDAO.getSessionsByViolationId(violation.getViolationId());
            if (this.sessions == null || this.sessions.isEmpty()) {
                this.sessions = sessionsDAO.getSessionsByParticipantId(violation.getParticipantId());
            }
        } catch (SQLException e) {
            logger.error("Error fetching related data: " + e.getMessage());
        }
    }

    /**
     * Initializes the main user interface of the panel.
     */
    private void initUI() {
        setLayout(new MigLayout("fill, insets 0", "[fill]", "[fill]"));

        JPanel mainPanel = new JPanel(new MigLayout("wrap, fillx, insets 20", "[fill]", "[]"));
        mainPanel.setOpaque(false);

        mainPanel.add(createHeaderPanel(), "growx, wrap 20");
        mainPanel.add(createStudentInfoSection(), "growx, wrap 15");
        mainPanel.add(createViolationInfoSection(), "growx, wrap 15");

        if (incidents != null && !incidents.isEmpty()) {
            mainPanel.add(createTitledSection("Incident Details", createIncidentsPanel()), "growx, wrap 15");
        }

        if (sessions != null && !sessions.isEmpty()) {
            mainPanel.add(createTitledSection("Counseling Sessions", createCounselingSessionsPanel()), "growx, wrap 15");
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, "grow");
    }

    /**
     * Creates the header panel showing the violation status and record date.
     * @return The header JPanel.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new MigLayout("insets 0, fill", "[grow][]", ""));
        headerPanel.setOpaque(false);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        String formattedDate = (violation.getUpdatedAt() != null) ? dateFormat.format(violation.getUpdatedAt()) : "Date not recorded";
        
        JLabel dateLabel = new JLabel("<html><b>Date Recorded:</b> " + formattedDate + "</html>");

        // Create a styled status label
        String status = violation.getStatus() != null ? violation.getStatus() : "Unknown";
        JLabel statusLabel = new JLabel(status.toUpperCase());
        statusLabel.putClientProperty("FlatLaf.style", "font: bold 12; border: 8,12,8,12;");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor(status));
        statusLabel.putClientProperty(FlatClientProperties.STYLE, "arc: 999;"); // Make it a pill shape

        headerPanel.add(dateLabel, "ay center");
        headerPanel.add(statusLabel, "ay center");

        return headerPanel;
    }

    /**
     * Creates the main section for displaying student information.
     * @return A JPanel containing the student's details.
     */
    private JPanel createStudentInfoSection() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]"));
        sectionPanel.setOpaque(false);

        try {
            Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
            if (participant != null) {
                if ("Student".equalsIgnoreCase(participant.getParticipantType()) && participant.getStudentUid() > 0) {
                    Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
                    if (student != null) {
                        // Address address = addressDAO.getAddressById(student.getAddressId());
                        // Parents parents = parentsDAO.getParentById(student.getParentId());
                        // Guardian guardian = guardianDAO.getGuardianById(student.getGuardianId());

                        // Student Name and LRN
                        String fullName = student.getStudentFirstname() + " " + student.getStudentMiddlename().charAt(0) + ". " + student.getStudentLastname();
                        JLabel nameLabel = new JLabel(fullName);
                        nameLabel.putClientProperty("FlatLaf.style", "font: bold 22;");
                        JLabel lrnLabel = new JLabel("LRN: " + student.getStudentLrn());
                        lrnLabel.putClientProperty("FlatLaf.style", "font: 14;");
                        sectionPanel.add(nameLabel, "wrap 0");
                        sectionPanel.add(lrnLabel, "wrap 15");

                        // Details Grid
                        JPanel grid = new JPanel(new MigLayout("wrap 4, fillx, insets 0", "[fill][fill][fill][fill]"));
                        grid.setOpaque(false);
                        grid.add(createInfoCard("Grade & Section", student.getSchoolSection() != null ? student.getSchoolSection() : "N/A"));
                        grid.add(createInfoCard("Age", String.valueOf(calculateAge(student.getStudentBirthdate())) + " years old"));
                        grid.add(createInfoCard("Sex", student.getStudentSex()));
                        grid.add(createInfoCard("Religion", student.getStudentReligion() != null ? student.getStudentReligion() : "N/A"));
                        
                        // Contact and Address
                        grid.add(createInfoCard("Contact Number", participant.getContactNumber() != null ? participant.getContactNumber() : "N/A"), "span 2");
                        // grid.add(createInfoCard("Address", address != null ? formatAddress(address) : "N/A"), "span 2"); // Uncomment when addressDAO is ready
                        
                        // grid.add(createInfoCard("Guardian", guardian != null ? guardian.getFullName() : "N/A"), "span 2"); // Uncomment when guardianDAO is ready
                        // grid.add(createInfoCard("Guardian Contact", guardian != null ? guardian.getContactNumber() : "N/A"), "span 2"); // Uncomment when guardianDAO is ready

                        sectionPanel.add(grid);
                    }
                } else {
                     // Handle non-student participants
                    String fullName = participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
                    sectionPanel.add(new JLabel(fullName), "wrap");
                    sectionPanel.add(createInfoCard("Participant Type", participant.getParticipantType()));
                    sectionPanel.add(createInfoCard("Contact", participant.getContactNumber()));
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading student data.");
            sectionPanel.add(new JLabel("Error loading student data."));
        }

        return createTitledSection("Student Information", sectionPanel);
    }
    
    /**
     * Creates the main section for displaying violation information.
     * @return A JPanel containing the violation details.
     */
    private JPanel createViolationInfoSection() {
        JPanel sectionPanel = new JPanel(new MigLayout("wrap 2, fillx, insets 0", "[fill][fill]"));
        sectionPanel.setOpaque(false);

        // Violation Type & Category
        sectionPanel.add(createInfoCard("Violation Type", violation.getViolationType()), "growx");
        try {
            String categoryName = violationDAO.getCategoryNameById(violation.getCategoryId());
            sectionPanel.add(createInfoCard("Violation Category", categoryName != null ? categoryName : "N/A"), "growx");
        } catch (SQLException e) {
            sectionPanel.add(createInfoCard("Violation Category", "Error loading"), "growx");
            logger.error("Error loading violation category.");
        }

        // Violation Description
        sectionPanel.add(createTextAreaCard("Description of Violation", violation.getViolationDescription()), "span 2, growx");

        // Reinforcement and Resolution
        sectionPanel.add(createTextAreaCard("Reinforcement / Action Plan", violation.getReinforcement()), "span 2, growx");
        sectionPanel.add(createTextAreaCard("Resolution Notes", violation.getResolutionNotes()), "span 2, growx");
        sectionPanel.add(createTextAreaCard("Session Summary", violation.getSessionSummary()), "span 2, growx");

        return createTitledSection("Violation Information", sectionPanel);
    }
    
    /**
     * Creates a panel containing all incident report cards.
     * @return A JPanel with incident details.
     */
    private JPanel createIncidentsPanel() {
        JPanel incidentsPanel = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]"));
        incidentsPanel.setOpaque(false);
        for(Incident incident : incidents) {
            incidentsPanel.add(createIncidentCard(incident), "growx, wrap 10");
        }
        return incidentsPanel;
    }
    
    /**
     * Creates a panel containing all counseling session cards.
     * @return A JPanel with session details.
     */
    private JPanel createCounselingSessionsPanel() {
        JPanel sessionsPanel = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]"));
        sessionsPanel.setOpaque(false);
        for(Sessions session : sessions) {
            sessionsPanel.add(createSessionCard(session), "growx, wrap 10");
        }
        return sessionsPanel;
    }

    /**
     * Creates a styled card to display a single incident's details.
     * @param incident The incident data to display.
     * @return A JPanel representing the incident card.
     */
    private JPanel createIncidentCard(Incident incident) {
        JPanel card = createBaseCard();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
        String dateStr = incident.getIncidentDate() != null ? sdf.format(incident.getIncidentDate()) : "N/A";
        
        card.add(createCardHeader("Incident on " + dateStr, getStatusColor(incident.getStatus())), "span, growx, wrap 10");
        card.add(createTextAreaCard("Description", incident.getIncidentDescription()), "growx, wrap");
        card.add(createTextAreaCard("Action Taken", incident.getActionTaken()), "growx, wrap");
        card.add(createTextAreaCard("Recommendation", incident.getRecommendation()), "growx");
        return card;
    }

    /**
     * Creates a styled card to display a single session's details.
     * @param session The session data to display.
     * @return A JPanel representing the session card.
     */
    private JPanel createSessionCard(Sessions session) {
        JPanel card = createBaseCard();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        String dateStr = session.getSessionDateTime() != null ? sdf.format(session.getSessionDateTime()) : "N/A";

        card.add(createCardHeader("Session on " + dateStr, getStatusColor(session.getSessionStatus())), "span, growx, wrap 10");
        card.add(createInfoCard("Consultation Type", session.getConsultationType()), "growx");
        card.add(createInfoCard("Appointment Type", session.getAppointmentType()), "growx, wrap");
        card.add(createTextAreaCard("Session Summary", session.getSessionSummary()), "span, growx, wrap");
        card.add(createTextAreaCard("Session Notes", session.getSessionNotes()), "span, growx");
        return card;
    }

    // --- HELPER METHODS FOR UI CREATION ---

    /**
     * Creates a generic titled section with a separator line.
     * @param title The title of the section.
     * @param contentPanel The panel containing the content for this section.
     * @return A composed JPanel for the section.
     */
    private JPanel createTitledSection(String title, JComponent contentPanel) {
        JPanel section = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]"));
        section.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold 18;");
        section.add(titleLabel, "wrap 5");
        section.add(new JSeparator(), "growx, wrap 10");
        section.add(contentPanel, "growx");
        return section;
    }

    /**
     * Creates a small display card for a single piece of information (label and value).
     * @param label The label text.
     * @param value The value text.
     * @return A styled JPanel for the info card.
     */
    private JPanel createInfoCard(String label, String value) {
        JPanel card = new JPanel(new MigLayout("wrap, fillx, insets 8 12", "[fill]"));
        JLabel labelLabel = new JLabel(label.toUpperCase());
        JLabel valueLabel = new JLabel(value != null && !value.trim().isEmpty() ? value : "N/A");
        valueLabel.putClientProperty(FlatClientProperties.STYLE, "font: 14;");
        card.add(labelLabel);
        card.add(valueLabel, "gapy 2");
        return card;
    }

    /**
     * Creates a display card for multi-line text content.
     * @param label The label for the text area.
     * @param text The text content.
     * @return A styled JPanel containing the text area.
     */
    private JPanel createTextAreaCard(String label, String text) {
        JPanel card = new JPanel(new MigLayout("wrap, fillx, insets 8 12", "[fill]"));
        
        JLabel labelLabel = new JLabel(label.toUpperCase());
        
        JTextArea textArea = new JTextArea(text != null && !text.trim().isEmpty() ? text : "No information provided.");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setOpaque(false);
        textArea.putClientProperty(FlatClientProperties.STYLE, "font: 14;");

        card.add(labelLabel);
        card.add(textArea, "gapy 2, growx");
        return card;
    }

    private JPanel createBaseCard() {
        JPanel card = new JPanel(new MigLayout("wrap 2, fillx", "[fill][fill]"));
        return card;
    }

    private JComponent createCardHeader(String title, Color statusColor) {
        JPanel header = new JPanel(new MigLayout("insets 8 12", "[grow]"));
        JLabel titleLabel = new JLabel(title);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold 14;");
        header.add(titleLabel);
        return header;
    }

    /**
     * Calculates age based on the birthdate. This is the corrected and more robust version.
     * @param birthdate The student's birthdate.
     * @return The calculated age in years.
     */
    private int calculateAge(Date birthdate) {
        if (birthdate == null) {
            return 0;
        }
        LocalDate birthDateLocal = birthdate.toLocalDate();
        LocalDate today = LocalDate.now();
        if (birthDateLocal.isAfter(today)) {
            return 0; // Birthdate is in the future, invalid.
        }
        return Period.between(birthDateLocal, today).getYears();
    }

    /**
     * Provides a color based on the item's status string.
     * @param status The status string (e.g., "Active", "Closed").
     * @return A Color object for the status.
     */
    private Color getStatusColor(String status) {
        if (status == null) return Color.GRAY;
        switch (status.toLowerCase()) {
            case "active":
            case "ongoing":
                return new Color(0, 123, 255); // Blue
            case "closed":
            case "resolved":
                return new Color(40, 167, 69); // Green
            case "pending":
                return new Color(255, 193, 7); // Yellow
            case "cancelled":
                return new Color(108, 117, 125); // Gray
            default:
                return new Color(220, 53, 69); // Red for other cases like "for review"
        }
    }

    /**
     * Static method to create and show the modal dialog for this panel.
     */
    public static void showDialog(Component parent, Connection conn, Violation violation, StudentsDataDAO studentsDataDAO, ParticipantsDAO participantsDAO) {
        ViewViolationDetails detailsPanel = new ViewViolationDetails(conn, violation, studentsDataDAO, participantsDAO);

        String modalId = "violation_details_" + violation.getViolationId();
        if (ModalDialog.isIdExist(modalId)) {
            ModalDialog.closeModal(modalId);
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(900, screenSize.width - 100);
        int height = Math.min(800, screenSize.height - 100);

        // Configure global modal options based on AddAppointmentModal.java pattern
        ModalDialog.getDefaultOption()
            .setOpacity(0.3f) // Keep opacity from original ViewViolationDetails
            .setAnimationOnClose(false) // Keep animation from original ViewViolationDetails
            .getBorderOption()
            .setBorderWidth(0f) // Keep border from original ViewViolationDetails
            .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM); // Keep shadow from original ViewViolationDetails

        SimpleModalBorder modalBorder = new SimpleModalBorder(
            detailsPanel, 
            "Violation Record Details", 
            new SimpleModalBorder.Option[]{ new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)},
            (controller, action) -> {
                if (action == SimpleModalBorder.CLOSE_OPTION) {
                    controller.close();
                }
            }
        );
        
        ModalDialog.showModal(parent, modalBorder, modalId);

        // Set modal size after showing, using getDefaultOption()
        ModalDialog.getDefaultOption().getLayoutOption().setSize(width,height);
    }
}
