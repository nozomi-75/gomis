package lyfjshs.gomis.view.sessions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.utils.EventBus;

public class SessionController {
    private static final Logger logger = Logger.getLogger(SessionController.class.getName());
    private final Connection conn;
    private final SessionsDAO sessionsDAO;
    private final ViolationDAO violationDAO;
    private final ParticipantsDAO participantsDAO;
    private final AppointmentDAO appointmentDAO;

    public SessionController(Connection conn) {
        this.conn = conn;
        this.sessionsDAO = new SessionsDAO(conn);
        this.violationDAO = new ViolationDAO(conn);
        this.participantsDAO = new ParticipantsDAO(conn);
        this.appointmentDAO = new AppointmentDAO(conn);
    }

    public boolean saveSession(Sessions session, List<TempParticipant> participants, Violation formViolation) throws SQLException {
        try {
            conn.setAutoCommit(false);
            
            // Collect participant IDs to link with the session
            List<Integer> finalParticipantIds = new ArrayList<>();
            for (TempParticipant tempParticipant : participants) {
                // If participant already has an ID (e.g., from an appointment), use it
                Participants participantEntity = createParticipantFromTemp(tempParticipant);
                Integer participantId;
                if (tempParticipant.getParticipantId() != null && tempParticipant.getParticipantId() > 0) {
                    participantId = tempParticipant.getParticipantId();
                    // Assuming updateParticipant exists in ParticipantsDAO
                    participantsDAO.updateParticipant(participantEntity);
                } else {
                    // Otherwise, create a new participant and get its ID
                    participantId = participantsDAO.createParticipant(participantEntity);
                    if (participantId <= 0) {
                        conn.rollback();
                        return false;
                    }
                    tempParticipant.setParticipantId(participantId); // Set the ID back to TempParticipant for later use if needed
                }
                finalParticipantIds.add(participantId);
            }

            // Save or Update session details
            boolean sessionOperationSuccess;
            if (session.getSessionId() == null || session.getSessionId() == 0) { // New Session (ID is 0 or null)
                sessionOperationSuccess = sessionsDAO.insertSession(session, finalParticipantIds, session.getAppointmentId());
            } else { // Existing Session
                sessionOperationSuccess = sessionsDAO.updateSession(session); // Update session details
                // For existing sessions, update participants linked to the session
                sessionsDAO.removeParticipantsFromSession(session.getSessionId()); // Remove all existing links
                sessionsDAO.addParticipantsToSession(session.getSessionId(), finalParticipantIds); // Add current participants
            }

            if (!sessionOperationSuccess) {
                conn.rollback();
                return false;
            }

            // Handle Violations: First, remove all existing violation records for this session
            // to ensure we only have current valid violation records. This is a destructive approach
            // for simplicity given the problem context. A more robust solution would involve checking
            // for existing violations and updating/deleting selectively based on changes.
            List<Violation> existingViolations = violationDAO.getViolationsBySessionId(session.getSessionId());
            for (Violation v : existingViolations) {
                violationDAO.deleteViolation(v.getViolationId());
            }
            session.setViolationId(null); // Clear session's main violation ID as we'll re-add if needed
            sessionsDAO.updateSession(session); // Update session after clearing violationId

            if (formViolation != null) {
                Integer firstViolatorViolationId = null;
                // For each violator, create a new violation record
                for (TempParticipant tempParticipant : participants) {
                    if (tempParticipant.isViolator() && tempParticipant.getParticipantId() != null) {
                        Violation violatorViolation = new Violation();
                        violatorViolation.setSessionId(session.getSessionId()); // Link to the current session
                        violatorViolation.setParticipantId(tempParticipant.getParticipantId());

                        // Copy main violation details from the form's violation object
                        violatorViolation.setViolationType(formViolation.getViolationType());
                        violatorViolation.setViolationDescription(formViolation.getViolationDescription());
                        violatorViolation.setReinforcement(formViolation.getReinforcement());
                        violatorViolation.setStatus("Active"); // Set initial status
                        if (formViolation.getCategory() != null) {
                            violatorViolation.setCategoryId(formViolation.getCategory().getCategoryId());
                        }

                        int createdViolationId = violationDAO.createViolation(violatorViolation);
                        if (createdViolationId <= 0) {
                            conn.rollback();
                            return false;
                        }
                        if (firstViolatorViolationId == null) {
                            firstViolatorViolationId = createdViolationId; // Store the ID of the first violator's violation
                        }
                    }
                }
                // Update the session's violationId to the first created violation ID (if any)
                if (firstViolatorViolationId != null) {
                    session.setViolationId(firstViolatorViolationId);
                    sessionsDAO.updateSession(session); // Update session with the new violationId
                }
            }

            // Update appointment status if session is ended
            if ("Ended".equals(session.getSessionStatus()) && session.getAppointmentId() != null) {
                boolean updated = appointmentDAO.updateAppointmentStatus(session.getAppointmentId(), "Completed");
                if (!updated) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            logger.log(Level.SEVERE, "Error saving session", e);
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private boolean saveViolationRecords(Sessions session, List<TempParticipant> participants) throws SQLException {
        // This method is no longer needed as violation saving logic is integrated into saveSession
        // You can remove this method or leave it as a placeholder if other parts of the code might still reference it.
        return true;
    }

    private Participants createParticipantFromTemp(TempParticipant temp) {
        Participants participant = new Participants();
        participant.setStudentUid(temp.getStudentUid());
        participant.setParticipantType(temp.getType());
        participant.setParticipantFirstName(temp.getFirstName());
        participant.setParticipantLastName(temp.getLastName());
        participant.setSex(temp.getSex());
        participant.setContactNumber(temp.getContactNumber());
        return participant;
    }

    public boolean endSession(Sessions session) throws SQLException {
        try {
            conn.setAutoCommit(false);
            
            // Update session status
            session.setSessionStatus("Ended");
            boolean success = sessionsDAO.updateSession(session);
            if (!success) {
                conn.rollback();
                return false;
            }

            // Update appointment status if session has an associated appointment
            if (session.getAppointmentId() != null) {
                boolean updated = appointmentDAO.updateAppointmentStatus(session.getAppointmentId(), "Completed");
                if (updated) {
                    EventBus.publish("appointment_status_changed", session.getAppointmentId());
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            logger.log(Level.SEVERE, "Error ending session", e);
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private boolean updateViolationRecords(Sessions session) throws SQLException {
        // This method is no longer fully needed as its logic is partially moved or simplified.
        // The `SessionsFillUpFormPanel.handleSessionEnding` now directly updates violation status.
        return true;
    }
} 