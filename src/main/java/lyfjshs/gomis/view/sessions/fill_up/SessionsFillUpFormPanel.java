package lyfjshs.gomis.view.sessions.fill_up;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.time.LocalDateTime;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.components.CirclePanel;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class SessionsFillUpFormPanel extends Form {
    private static final Logger logger = LogManager.getLogger(SessionsFillUpFormPanel.class);
    private SessionDetailsPanel detailsPanel;
    private ParticipantsPanel participantsPanel;
    private SessionSummaryPanel summaryPanel;
    private ViolationInfoPanel violationPanel;
    private JButton saveBtn;
    private Runnable saveCallback;
    private Sessions editingSession;
    private boolean isEditing = false;
    private Connection connection;
    private Integer appointmentId = null; // Track appointmentId for linkage

    public SessionsFillUpFormPanel(Connection connection) {
        this.connection = connection;
        setComponents();
        // Add resize listener to preserve and restore state
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshStateOnResize();
            }
        });
    }

    private void setComponents() {
        this.connection = connection;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));

        // Main container (centered, max width)
        JPanel container = new JPanel(new MigLayout("", "[grow]", "[][][]"));
        container.setOpaque(false);

        // Header
        JPanel header = new JPanel(new MigLayout("", "[grow]", "[]10[]"));
        header.setOpaque(false);
        JLabel title = new JLabel("Session Documentation Form");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, "growx, wrap");
        // Progress bar
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(20);
        progressBar.setPreferredSize(new Dimension(200, 8));
        progressBar.setForeground(new Color(76, 175, 80));
        progressBar.setBackground(new Color(224, 224, 224));
        progressBar.setBorderPainted(false);
        header.add(progressBar, "growx");

        container.add(header, "cell 0 0,growx");

        // Scrollable form area
        JPanel formPanel = new JPanel(new MigLayout("", "[grow]", "[][][][][][][][]"));
        formPanel.setOpaque(false);

        // Section panels (modular, as in SessionFormTEST)
        detailsPanel = new SessionDetailsPanel();
        participantsPanel = new ParticipantsPanel(connection);
        summaryPanel = new SessionSummaryPanel();
        violationPanel = new ViolationInfoPanel();

        CirclePanel detailsSection = new CirclePanel("1", "Session Details", detailsPanel.getContentPanel());
        CirclePanel participantsSection = new CirclePanel("2", "Participants", participantsPanel.getContentPanel());
        CirclePanel summarySection = new CirclePanel("3", "Session Summary", summaryPanel.getContentPanel());
        CirclePanel violationSection = new CirclePanel("4", "Violation Information", violationPanel.getContentPanel());

        formPanel.add(detailsSection, "cell 0 0,grow");
        formPanel.add(participantsSection, "cell 0 2,grow");
        formPanel.add(summarySection, "cell 0 4,grow");
        formPanel.add(violationSection, "cell 0 6,grow");

        // Save section (sticky footer)
        JPanel saveSection = new JPanel(new MigLayout("insets 25, fillx", "[grow][][grow]", ""));
        formPanel.add(saveSection, "cell 0 7,growx");
        saveSection.setOpaque(false);
        saveBtn = new JButton("Save Session Record");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        saveBtn.setBackground(new Color(102, 126, 234));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        saveBtn.addActionListener(e -> onSave());
        saveSection.add(Box.createHorizontalGlue(), "grow");
        saveSection.add(saveBtn, "center");
        saveSection.add(Box.createHorizontalGlue(), "grow");

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        container.add(scrollPane, "cell 0 1,grow");

        removeAll();
        add(container, BorderLayout.CENTER);
    }

    // Save logic with input validation and confirmation
    private void onSave() {
        logger.info("Save button clicked. Validating session form...");
        // Input validation
        StringBuilder errors = new StringBuilder();
        // Required fields
        if (detailsPanel.getSessionDate().trim().isEmpty())
            errors.append("- Session Date is required\n");
        if (detailsPanel.getSessionTime().trim().isEmpty())
            errors.append("- Session Time is required\n");
        if (detailsPanel.getAppointmentType() == null || detailsPanel.getAppointmentType().trim().isEmpty())
            errors.append("- Appointment Type is required\n");
        if (detailsPanel.getConsultationType() == null || detailsPanel.getConsultationType().trim().isEmpty())
            errors.append("- Consultation Type is required\n");
        if (detailsPanel.getSessionStatus() == null || detailsPanel.getSessionStatus().trim().isEmpty())
            errors.append("- Session Status is required\n");
        if (summaryPanel.getSummary().trim().isEmpty())
            errors.append("- Session Summary is required\n");
        // Violation logic
        String violationType = violationPanel.getViolationType();
        if (violationType == null || violationType.trim().isEmpty())
            errors.append("- Violation Type is required\n");
        if ("Bullying".equals(violationType)) {
            if (violationPanel.getCategory() == null || violationPanel.getCategory().trim().isEmpty())
                errors.append("- Category is required for Bullying\n");
        }
        if ("Other".equals(violationType) && violationPanel.getViolationType().trim().isEmpty()) {
            errors.append("- Please specify the other violation type\n");
        }
        // Participants validation
        if (participantsPanel.getParticipants().isEmpty())
            errors.append("- At least one participant is required\n");
        // Status/reschedule logic
        if ("Active".equals(detailsPanel.getSessionStatus())) {
            if (detailsPanel.getRescheduleDate().trim().isEmpty()
                    || detailsPanel.getRescheduleTime().trim().isEmpty()) {
                errors.append("- Reschedule date and time are required for Active sessions\n");
            }
        }
        if (errors.length() > 0) {
            logger.warn("Validation failed. Errors: {}", errors.toString().replace("\n", "; "));
            // Center the dialog relative to the top-level window
            javax.swing.JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Please correct the following errors:\n\n" + errors,
                "Validation Error",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        // Confirm dialog
        int confirm = JOptionPane.showConfirmDialog(this, "Save this session record?", "Confirm Save",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            logger.info("User cancelled session save.");
            return;
        }
        // Save to database
        logger.info("Saving session record to database...");
        boolean success = saveSessionToDatabase();
        if (success) {
            logger.info("Session record saved successfully.");
            JOptionPane.showMessageDialog(this, "Session record saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (saveCallback != null)
                saveCallback.run();
        } else {
            logger.error("Session record failed to save.");
        }
    }

    /**
     * Full save-to-database logic: creates/updates session, participants,
     * violations, and updates appointment as needed.
     */
    private boolean saveSessionToDatabase() {
        try {
            logger.info("Beginning database transaction for session save...");
            connection.setAutoCommit(false);
            // --- 1. Gather Data ---
            lyfjshs.gomis.Database.entity.Sessions session = new lyfjshs.gomis.Database.entity.Sessions();
            // Guidance Counselor (Recorded By)
            Integer counselorId = lyfjshs.gomis.Main.formManager != null
                    && lyfjshs.gomis.Main.formManager.getCounselorObject() != null
                            ? lyfjshs.gomis.Main.formManager.getCounselorObject().getGuidanceCounselorId()
                            : null;
            session.setGuidanceCounselorId(counselorId);
            // Appointment linkage
            String apptType = detailsPanel.getAppointmentType();
            session.setAppointmentType(apptType);
            session.setConsultationType(detailsPanel.getConsultationType());
            session.setSessionStatus(detailsPanel.getSessionStatus());
            session.setSessionNotes(detailsPanel.getNotes());
            session.setSessionSummary(summaryPanel.getSummary());
            // Link to appointment if present
            if ("Scheduled".equals(apptType) && appointmentId != null) {
                session.setAppointmentId(appointmentId);
            }
            // Parse session date/time
            java.time.LocalDate date = detailsPanel.getDatePicker().getSelectedDate();
            java.time.LocalTime time = detailsPanel.getTimePicker().getSelectedTime();
            java.sql.Timestamp sessionDateTime = java.sql.Timestamp.valueOf(java.time.LocalDateTime.of(date, time));
            session.setSessionDateTime(sessionDateTime);
            // --- 2. Participants ---
            java.util.List<lyfjshs.gomis.Database.entity.Participants> dbParticipants = new java.util.ArrayList<>();
            java.util.List<Integer> violatorParticipantIds = new java.util.ArrayList<>();
            for (lyfjshs.gomis.view.sessions.TempParticipant p : participantsPanel.getParticipants()) {
                lyfjshs.gomis.Database.entity.Participants dbP = new lyfjshs.gomis.Database.entity.Participants();
                dbP.setParticipantFirstName(p.getFirstName());
                dbP.setParticipantLastName(p.getLastName());
                dbP.setParticipantType(p.getType());
                dbP.setReporter(p.isReporter());
                dbParticipants.add(dbP);
                if (p.isViolator())
                    violatorParticipantIds.add(dbParticipants.size() - 1);
            }
            // --- 3. Violation ---
            String vType = violationPanel.getViolationType();
            String vCategory = violationPanel.getCategory();
            String vDesc = violationPanel.getDescription();
            String vReinforcement = violationPanel.getReinforcement();
            Integer categoryId = null;
            if ("Bullying".equals(vType) && vCategory != null && !vCategory.isEmpty()) {
                lyfjshs.gomis.Database.DAO.ViolationCategoryDAO catDAO = new lyfjshs.gomis.Database.DAO.ViolationCategoryDAO(
                        connection);
                lyfjshs.gomis.Database.entity.ViolationCategory cat = catDAO.getCategoryByName(vCategory);
                if (cat == null) {
                    cat = new lyfjshs.gomis.Database.entity.ViolationCategory();
                    cat.setCategoryName(vCategory);
                    cat.setCategoryDescription("Auto-created");
                    catDAO.addCategory(cat);
                    cat = catDAO.getCategoryByName(vCategory);
                }
                categoryId = cat != null ? cat.getCategoryId() : null;
            }
            // --- 4. Save Session ---
            lyfjshs.gomis.Database.DAO.SessionsDAO sessionsDAO = new lyfjshs.gomis.Database.DAO.SessionsDAO(connection);
            int sessionId = sessionsDAO.createSession(session);
            if (sessionId <= 0)
                throw new Exception("Failed to create session record.");
            session.setSessionId(sessionId);
            logger.info("Session record created with ID: {}", sessionId);
            // --- 5. Save Participants ---
            lyfjshs.gomis.Database.DAO.ParticipantsDAO participantsDAO = new lyfjshs.gomis.Database.DAO.ParticipantsDAO(
                    connection);
            java.util.List<Integer> participantIds = new java.util.ArrayList<>();
            for (lyfjshs.gomis.Database.entity.Participants dbP : dbParticipants) {
                int pid = participantsDAO.createParticipant(dbP);
                participantIds.add(pid);
                sessionsDAO.addParticipantToSession(sessionId, pid);
                logger.info("Participant record created with ID: {} and linked to session {}", pid, sessionId);
            }
            // --- 6. Save Violations (for each violator) ---
            if (!violatorParticipantIds.isEmpty() && vType != null && !vType.isEmpty()) {
                lyfjshs.gomis.Database.DAO.ViolationDAO violationDAO = new lyfjshs.gomis.Database.DAO.ViolationDAO(
                        connection);
                for (int idx : violatorParticipantIds) {
                    lyfjshs.gomis.Database.entity.Violation violation = new lyfjshs.gomis.Database.entity.Violation();
                    violation.setParticipantId(participantIds.get(idx));
                    violation.setCategoryId(categoryId);
                    violation.setViolationType(vType);
                    violation.setViolationDescription(vDesc);
                    violation.setSessionSummary(summaryPanel.getSummary());
                    violation.setReinforcement(vReinforcement);
                    violation.setStatus("Active");
                    violation.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    violationDAO.createViolation(violation);
                    logger.info("Violation record created for participant ID: {}", participantIds.get(idx));
                }
            }
            // --- 7. Appointment Link/Reschedule ---
            if (session.getAppointmentId() != null) {
                lyfjshs.gomis.Database.DAO.AppointmentDAO appointmentDAO = new lyfjshs.gomis.Database.DAO.AppointmentDAO(
                        connection);
                lyfjshs.gomis.Database.entity.Appointment appt = appointmentDAO
                        .getAppointmentById(session.getAppointmentId());
                if (appt != null) {
                    if ("Ended".equals(session.getSessionStatus())) {
                        // Mark appointment as completed
                        appointmentDAO.updateAppointmentStatus(appt.getAppointmentId(), "Completed");
                        logger.info("Appointment {} marked as completed.", appt.getAppointmentId());
                    } else if ("Active".equals(session.getSessionStatus())) {
                        String reschedDate = detailsPanel.getRescheduleDate();
                        String reschedTime = detailsPanel.getRescheduleTime();
                        if (reschedDate != null && !reschedDate.trim().isEmpty() && reschedTime != null
                                && !reschedTime.trim().isEmpty()) {
                            // Parse new date/time
                            java.time.LocalDate rDate = detailsPanel.getRescheduleDatePicker().getSelectedDate();
                            java.time.LocalTime rTime = detailsPanel.getRescheduleTimePicker().getSelectedTime();
                            java.sql.Timestamp newDateTime = java.sql.Timestamp
                                    .valueOf(java.time.LocalDateTime.of(rDate, rTime));
                            appt.setAppointmentDateTime(newDateTime);
                            appt.setAppointmentStatus("Rescheduled");
                            appointmentDAO.updateAppointment(appt);
                            logger.info("Appointment {} rescheduled to {} {}.", appt.getAppointmentId(), rDate, rTime);
                        }
                    }
                }
            }
            connection.commit();
            logger.info("Session save transaction committed successfully.");
            return true;
        } catch (Exception ex) {
            try {
                connection.rollback();
                logger.error("Session save transaction rolled back due to error: {}", ex.getMessage(), ex);
            } catch (Exception rollbackEx) {
                logger.error("Error during rollback: {}", rollbackEx.getMessage(), rollbackEx);
            }
            JOptionPane.showMessageDialog(this, "Error saving session: " + ex.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception autoCommitEx) {
                logger.error("Error resetting auto-commit: {}", autoCommitEx.getMessage(), autoCommitEx);
            }
        }
    }

    // --- Integration Methods ---
    public void setEditingSession(Sessions session) {
        this.editingSession = session;
        this.isEditing = true;
        if (session != null) {
            detailsPanel.setAppointmentType(session.getAppointmentType());
            detailsPanel.setConsultationType(session.getConsultationType());
            if (session.getSessionDateTime() != null) {
                LocalDateTime dateTime = session.getSessionDateTime().toLocalDateTime();
                detailsPanel.setSessionDate(dateTime.toLocalDate().toString());
                detailsPanel.setSessionTime(dateTime.toLocalTime().toString());
            }
            detailsPanel.setNotes(session.getSessionNotes());
            summaryPanel.setSummary(session.getSessionSummary());
            detailsPanel.setSessionStatus(session.getSessionStatus());
            // --- Populate participantsPanel from session ---
            try {
                lyfjshs.gomis.Database.DAO.SessionsDAO sessionsDAO = new lyfjshs.gomis.Database.DAO.SessionsDAO(
                        connection);
                java.util.List<lyfjshs.gomis.Database.entity.Participants> dbParticipants = sessionsDAO
                        .getParticipantsBySessionId(session.getSessionId());
                java.util.List<lyfjshs.gomis.view.sessions.TempParticipant> uiParticipants = new java.util.ArrayList<>();
                for (lyfjshs.gomis.Database.entity.Participants p : dbParticipants) {
                    uiParticipants.add(new lyfjshs.gomis.view.sessions.TempParticipant(
                        p.getParticipantId(),
                        p.getStudentUid(),
                        p.getParticipantFirstName(),
                        p.getParticipantLastName(),
                        p.getParticipantType(),
                        p.getSex(),
                        p.getContactNumber(),
                        p.getStudentUid() != null,
                        false, // Violator info not stored in Participants
                        p.isReporter()
                    ));
                }
                participantsPanel.setParticipants(uiParticipants);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // --- Optionally populate violationPanel if you have violation info in Sessions
            // ---
            // violationPanel.setViolationType(...); // set from session if available
            // violationPanel.setCategory(...);
            // violationPanel.setDescription(...);
            // violationPanel.setReinforcement(...);
        }
    }

    public void setSaveCallback(Runnable callback) {
        this.saveCallback = callback;
    }

    public void populateFromAppointment(Appointment appointment) {
        if (appointment == null)
            return;
        this.appointmentId = appointment.getAppointmentId(); // Track appointmentId
        logger.debug("Populating SessionsFillUpFormPanel from appointment: {}", appointment.getAppointmentId());
        SwingUtilities.invokeLater(() -> {
            detailsPanel.setAppointmentType("Scheduled");
            detailsPanel.setConsultationType(appointment.getConsultationType());
            if (appointment.getAppointmentDateTime() != null) {
                LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
                detailsPanel.setSessionDate(dateTime.toLocalDate().toString());
                detailsPanel.setSessionTime(dateTime.toLocalTime().toString());
            }
            detailsPanel.setNotes(appointment.getAppointmentNotes() != null ? appointment.getAppointmentNotes() : "");
            detailsPanel.setSessionStatus("Active");
            // --- Transfer participants to participantsPanel ---
            try {
                lyfjshs.gomis.Database.DAO.AppointmentDAO appointmentDAO = new lyfjshs.gomis.Database.DAO.AppointmentDAO(
                        connection);
                java.util.List<lyfjshs.gomis.Database.entity.Participants> dbParticipants = appointmentDAO
                        .getParticipantsForAppointment(appointment.getAppointmentId());
                java.util.List<lyfjshs.gomis.view.sessions.TempParticipant> uiParticipants = new java.util.ArrayList<>();
                for (lyfjshs.gomis.Database.entity.Participants p : dbParticipants) {
                    uiParticipants.add(new lyfjshs.gomis.view.sessions.TempParticipant(
                        p.getParticipantId(),
                        p.getStudentUid(),
                        p.getParticipantFirstName(),
                        p.getParticipantLastName(),
                        p.getParticipantType(),
                        p.getSex(),
                        p.getContactNumber(),
                        p.getStudentUid() != null,
                        false, // Violator info not stored in Participants
                        p.isReporter()
                    ));
                }
                participantsPanel.setParticipants(uiParticipants);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // --- Set summary template and clear violation info ---
            summaryPanel.setSummary(
                    "Session created from appointment: " + appointment.getAppointmentTitle() + "\nConsultation Type: "
                            + appointment.getConsultationType() + "\nDate: " + appointment.getAppointmentDateTime());
            violationPanel.setViolationType("");
            violationPanel.setCategory("");
            violationPanel.setDescription("");
            violationPanel.setReinforcement("");
            this.isEditing = false;
            this.editingSession = null;
        });
    }

    public void setSessionToEdit(Integer sessionId) {
        // This method can be implemented to load a session by ID if needed
        // For now, just a stub for compatibility
    }

    public void clearAllFields() {
        detailsPanel.clearFields();
        participantsPanel.clearParticipants();
        summaryPanel.clearSummary();
        violationPanel.clearFields();
        this.isEditing = false;
        this.editingSession = null;
        this.appointmentId = null;
    }

    /**
     * Loads a session by ID and populates all panels. Returns true if successful.
     */
    public boolean reloadFromSessionId(Integer sessionId) {
        if (sessionId == null || connection == null)
            return false;
        try {
            lyfjshs.gomis.Database.DAO.SessionsDAO sessionsDAO = new lyfjshs.gomis.Database.DAO.SessionsDAO(connection);
            lyfjshs.gomis.Database.entity.Sessions session = sessionsDAO.getSessionById(sessionId);
            if (session != null) {
                setEditingSession(session);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // Expose get/set methods for integration with your data model
    public SessionDetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    public ParticipantsPanel getParticipantsPanel() {
        return participantsPanel;
    }

    public SessionSummaryPanel getSummaryPanel() {
        return summaryPanel;
    }

    public ViolationInfoPanel getViolationPanel() {
        return violationPanel;
    }

    private void refreshStateOnResize() {
        // FIRST. Extract current form data
        SessionFormData data = new SessionFormData();
        detailsPanel.getData(data);
        data.participants = participantsPanel.getParticipants();
        summaryPanel.getData(data);
        violationPanel.getData(data);

        // 3. Revalidate and repaint to update layout
        this.removeAll();
        //ADD AGAIN ALL COMPONENTS
        setComponents();

        this.revalidate();
        this.repaint();

        // LAST. Re-apply data to all panels
        detailsPanel.setData(data);
        if (data.participants != null) participantsPanel.setParticipants(data.participants);
        summaryPanel.setData(data);
        violationPanel.setData(data);
    }

    @Override
    public void onParentFrameResized(int width, int height) {
        logger.debug("SessionsFillUpFormPanel resized to: {}x{}", width, height);
        super.onParentFrameResized(width, height);
    }
}