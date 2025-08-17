/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import docPrinter.templateManager;
import docPrinter.incidentReport.incidentReportGenerator;
import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.CirclePanel;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.utils.ErrorDialogUtils;
import net.miginfocom.swing.MigLayout;

public class IncidentFillUpFormPanel extends Form {
    private static final Logger logger = LogManager.getLogger(IncidentFillUpFormPanel.class);
    private IncidentDetailsPanel detailsPanel;
    private IncidentNarrativePanel narrativePanel;
    private IncidentActionsPanel actionsPanel;
    private IncidentParticipantsPanel participantsPanel;
    private JButton submitBtn;
    private JButton printBtn;
    private Connection connection;

    public IncidentFillUpFormPanel(Connection connection) {
        this.connection = connection;

        setComponents();

        setupListeners();
    }

    private void setComponents() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        JPanel container = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
        container.setOpaque(false);

        // Header
        JPanel header = new JPanel(new MigLayout("", "[grow][]", "[]10[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Incident Fill-Up Form");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, "flowx,cell 0 0 2 1,alignx center");
        container.add(header, "cell 0 0,growx");
        printBtn = new JButton("Print Initial Report");
        header.add(printBtn, "cell 1 1,alignx right");
        printBtn.setFocusPainted(false);

        // Scrollable form area
        JPanel formPanel = new JPanel(
                
                
                new MigLayout("fill, insets 10, wrap 1", "[grow,fill]", "[]10[]10[]10[]10[grow,fill]"));
        formPanel.setOpaque(false);

        detailsPanel = new IncidentDetailsPanel(connection);
        narrativePanel = new IncidentNarrativePanel();
        actionsPanel = new IncidentActionsPanel();
        participantsPanel = new IncidentParticipantsPanel(connection);

        CirclePanel detailsSection = new CirclePanel("1", "Incident Details", detailsPanel);
        CirclePanel narrativeSection = new CirclePanel("2", "Narrative Report", narrativePanel);
        CirclePanel actionsSection = new CirclePanel("3", "Initial Actions & Recommendations", actionsPanel);
        CirclePanel participantsSection = new CirclePanel("4", "Participants", participantsPanel);

        formPanel.add(detailsSection, "growx, pushx");
        formPanel.add(narrativeSection, "growx, pushx");
        formPanel.add(actionsSection, "growx, pushx");
        formPanel.add(participantsSection, "grow, push");

        // Sticky footer
        JPanel footer = new JPanel(new MigLayout("insets 25, fillx", "[grow][][grow]", ""));
        formPanel.add(footer, "cell 0 4,growx,aligny center");
        footer.setOpaque(false);
        submitBtn = new JButton("Submit Incident Report");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        submitBtn.setBackground(new Color(102, 126, 234));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        footer.add(Box.createHorizontalGlue(), "grow");

        
        
        container.add(formPanel, "cell 0 1,grow");

        add(container, BorderLayout.CENTER);
    }

    public boolean validateAll() {
        if (!detailsPanel.isValidPanel()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please complete the Incident Details section.");
            return false;
        }
        if (!narrativePanel.isValidPanel()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Narrative report must be at least 10 characters.");
            return false;
        }
        if (!actionsPanel.isValidPanel()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Actions and recommendations must be at least 10 characters.");
            return false;
        }
        if (!participantsPanel.isValidPanel()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please add at least one participant.");
            return false;
        }
        return true;
    }

    public void clearAll() {
        detailsPanel.clearFields();
        narrativePanel.clearFields();
        actionsPanel.clearFields();
        participantsPanel.clearFields();
    }

    public IncidentDetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    public IncidentNarrativePanel getNarrativePanel() {
        return narrativePanel;
    }

    public IncidentActionsPanel getActionsPanel() {
        return actionsPanel;
    }

    public IncidentParticipantsPanel getParticipantsPanel() {
        return participantsPanel;
    }

    private void setupListeners() {
        submitBtn.addActionListener(e -> {
            logger.info("Submit button clicked. Validating form...");
            if (validateAll()) {
                logger.info("Validation passed. Saving incident report...");
                saveIncidentReportAsync(submitBtn);
            } else {
                logger.warn("Validation failed. Form not submitted.");
            }
        });
        printBtn.addActionListener(e -> {
            logger.info("Print Initial Report button clicked.");
            printIncidentReport();
        });
    }

    private void saveIncidentReportAsync(JButton saveButton) {
        saveButton.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    logger.info("Starting async save of incident report...");
                    return saveIncidentReport();
                } catch (Exception e) {
                    logger.error("Error during async save: {}", e.getMessage(), e);
                    ErrorDialogUtils.showError(IncidentFillUpFormPanel.this, "Error: " + e.getMessage());
                    return false;
                }
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    boolean success = get();
                    if (success) {
                        logger.info("Incident report saved successfully.");
                        javax.swing.JOptionPane.showMessageDialog(IncidentFillUpFormPanel.this,
                                "Incident Report Submitted Successfully!");
                        clearAll();
                    } else {
                        logger.warn("Incident report save failed.");
                    }
                } catch (Exception e) {
                    logger.error("Error after async save: {}", e.getMessage(), e);
                    ErrorDialogUtils.showError(IncidentFillUpFormPanel.this, "Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    private boolean saveIncidentReport() {
        try {
            logger.info("Saving incident report to database...");
            // Validate required fields (already checked in validateAll, but double-check)
            if (!validateAll()) {
                logger.warn("Validation failed during saveIncidentReport().");
                return false;
            }
            IncidentsDAO incidentsDAO = new IncidentsDAO(connection);
            ViolationDAO violationDAO = new ViolationDAO(connection);
            ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
            // Create new incident
            Incident incident = new Incident();
            // Reporter is not auto-filled, so skip participantId for now
            incident.setIncidentDate(java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            incident.setStatus(detailsPanel.getStatus());
            incident.setIncidentDescription(narrativePanel.getNarrativeReport());
            incident.setActionTaken(actionsPanel.getActionsTaken());
            incident.setRecommendation(actionsPanel.getRecommendations());
            incident.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            // Save incident to database
            int incidentId = incidentsDAO.createIncident(incident);
            if (incidentId != -1) {
                logger.info("Incident record created with ID: {}", incidentId);
                // Create violation records for each participant
                for (TempIncidentParticipant tempParticipant : participantsPanel.getParticipants()) {
                    Violation violation = new Violation();
                    violation.setParticipantId(tempParticipant.getParticipantId());
                    violation.setViolationType(narrativePanel.getNarrativeReport());
                    violation.setViolationDescription(narrativePanel.getNarrativeReport());
                    violation.setSessionSummary(narrativePanel.getNarrativeReport());
                    violation.setReinforcement(actionsPanel.getRecommendations());
                    violation.setStatus("Active");
                    violation.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    int violationId = violationDAO.createViolation(violation);
                    if (violationId != -1) {
                        logger.info("Violation record created with ID: {} for participant {}", violationId,
                                tempParticipant.getParticipantId());
                        String linkSql = "INSERT INTO INCIDENT_VIOLATIONS (INCIDENT_ID, VIOLATION_ID) VALUES (?, ?)";
                        try (java.sql.PreparedStatement pstmt = connection.prepareStatement(linkSql)) {
                            pstmt.setInt(1, incidentId);
                            pstmt.setInt(2, violationId);
                            pstmt.executeUpdate();
                        }
                    } else {
                        logger.warn("Failed to create violation for participant {}",
                                tempParticipant.getParticipantId());
                    }
                }
                // Success
                return true;
            } else {
                logger.error("Failed to create incident record in database.");
                ErrorDialogUtils.showError(this, "Failed to save incident report.");
                return false;
            }
        } catch (Exception ex) {
            logger.error("Exception while saving incident: {}", ex.getMessage(), ex);
            ErrorDialogUtils.showError(this, "Error saving incident: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

 

    public void getData(IncidentFullData data) {
        data.reportedBy = detailsPanel.getReportedBy();
        data.gradeSection = detailsPanel.getGradeSection();
        data.date = detailsPanel.getDate();
        data.time = detailsPanel.getTime();
        data.status = detailsPanel.getStatus();
        data.narrative = narrativePanel.getNarrativeReport();
        data.actions = actionsPanel.getActionsTaken();
        data.recommendations = actionsPanel.getRecommendations();
        data.participants = new java.util.ArrayList<>(participantsPanel.getParticipants());
    }

    public void setData(IncidentFullData data) {
        detailsPanel.setReportedBy(data.reportedBy);
        detailsPanel.setGradeSection(data.gradeSection);
        detailsPanel.setDate(data.date);
        detailsPanel.setTime(data.time);
        detailsPanel.setStatus(data.status);
        narrativePanel.setNarrativeReport(data.narrative);
        actionsPanel.setActionsTaken(data.actions);
        actionsPanel.setRecommendations(data.recommendations);
        participantsPanel.setParticipants(data.participants != null ? data.participants : new java.util.ArrayList<>());
    }

    private void printIncidentReport() {
        try {
            logger.info("Generating incident report for print/export...");
            incidentReportGenerator generator = new incidentReportGenerator();
            File outputFolder = templateManager.getDefaultOutputFolder();
            Map<String, String> incidentData = new HashMap<>();
            // Gather data from panels
            incidentData.put("REPORTED_BY", detailsPanel.getReportedBy().trim());
            incidentData.put("DATE", detailsPanel.getDate() != null ? detailsPanel.getDate().toString() : "");
            incidentData.put("TIME", detailsPanel.getTime() != null ? detailsPanel.getTime().toString() : "");
            incidentData.put("GRADE_SECTION", detailsPanel.getGradeSection().trim());
            incidentData.put("NARRATIVE_REPORT", narrativePanel.getNarrativeReport().trim());
            incidentData.put("ACTIONS_TAKEN", actionsPanel.getActionsTaken().trim());
            incidentData.put("RECOMMENDATIONS", actionsPanel.getRecommendations().trim());
            incidentData.put("STATUS", detailsPanel.getStatus());
            // Participants
            java.util.List<TempIncidentParticipant> participants = participantsPanel.getParticipants();
            if (!participants.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (TempIncidentParticipant p : participants) {
                    sb.append(p.getFullName()).append(" (").append(p.getType()).append(")").append(", ");
                }
                if (sb.length() > 2)
                    sb.setLength(sb.length() - 2);
                incidentData.put("PARTICIPANTS", sb.toString());
            }
            boolean success = generator.generateIncidentReport(outputFolder, null, incidentData, "print");
            if (success) {
                logger.info("Incident report generated successfully for print/export.");
                javax.swing.JOptionPane.showMessageDialog(this, "Incident report generated successfully.");
            } else {
                logger.error("Failed to generate incident report for print/export.");
                javax.swing.JOptionPane.showMessageDialog(this, "Failed to generate incident report.", "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            logger.error("Exception during incident report print/export: {}", ex.getMessage(), ex);
            javax.swing.JOptionPane.showMessageDialog(this, "Error generating incident report: " + ex.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Populate the incident form from a session and its participants (for quick
     * incident creation from a session record).
     */
    public void populateFromSession(Sessions session, List<Participants> participants) {
        // Clear existing data
        clearAll();
        // Set the date and time from session
        if (session.getSessionDateTime() != null) {
            java.time.LocalDateTime sessionDateTime = session.getSessionDateTime().toLocalDateTime();
            detailsPanel.setDate(sessionDateTime.toLocalDate());
            detailsPanel.setTime(sessionDateTime.toLocalTime());
        }
        // Set the narrative report from session summary
        if (session.getSessionSummary() != null && !session.getSessionSummary().isEmpty()) {
            narrativePanel.setNarrativeReport(session.getSessionSummary());
        }
        // Set the actions taken from session notes
        if (session.getSessionNotes() != null && !session.getSessionNotes().isEmpty()) {
            actionsPanel.setActionsTaken(session.getSessionNotes());
        }
        // Clear reporter field - must be set manually
        detailsPanel.setReportedBy("");
        detailsPanel.setGradeSection("");
        // Set initial status as Active
        detailsPanel.setStatus("Active");
        // Clear existing temporary participant data
        participantsPanel.clearFields();
        // Add participants to the temporary list and then update the table
        if (participants != null && !participants.isEmpty()) {
            java.util.List<TempIncidentParticipant> tempList = new java.util.ArrayList<>();
            for (Participants participant : participants) {
                TempIncidentParticipant tempIncidentParticipant = new TempIncidentParticipant(
                        participant.getParticipantId(), participant.getStudentUid(),
                        participant.getParticipantFirstName(), participant.getParticipantLastName(),
                        participant.getParticipantType(), participant.getSex(), participant.getContactNumber(),
                        "Student".equals(participant.getParticipantType()));
                tempList.add(tempIncidentParticipant);
            }
            participantsPanel.setParticipants(tempList);
        }
    }

    private void refreshStateOnResize() {
        // if its a fill up Form Store data somewhere
        IncidentFullData data = new IncidentFullData();
        getData(data);

        // remove all components
        this.removeAll();

        // set all components
        setComponents();
        // revalidate and repaint main Form
        this.revalidate();
        this.repaint();
        this.formRefresh(); // For any lightweight updates

        // Set Data (IF FILL-UP FORM)
        setData(data);
    }

    @Override
    public void onParentFrameResized(int width, int height) {
        logger.debug("IncidentFillUpFormPanel resized to: {}x{}", width, height);
        super.onParentFrameResized(width, height);
        refreshStateOnResize();
    }
}