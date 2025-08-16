package lyfjshs.gomis.view.sessions;

import java.util.List;

import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.utils.ErrorDialogUtils;

public class SessionValidator {
    
    public boolean validateSession(Sessions session, List<TempParticipant> participants, Violation violation) {
        StringBuilder errors = new StringBuilder();

        // Validate session flow
        if (!validateSessionFlow(session, errors)) {
            showErrors(errors);
            return false;
        }

        // Validate participants (now passes the session object AND violation object)
        if (!validateParticipants(session, participants, violation, errors)) {
            showErrors(errors);
            return false;
        }

        // Validate session details
        if (!validateSessionDetails(session, errors)) {
            showErrors(errors);
            return false;
        }

        // Validate violation data if a violation object exists and is not null
        if (violation != null) {
            if (!validateViolationData(violation, errors)) {
                showErrors(errors);
                return false;
            }
        }

        return true;
    }

    private boolean validateSessionFlow(Sessions session, StringBuilder errors) {
        if (session.getAppointmentId() == null && session.getSessionId() == null) {
            errors.append("- Session must be created from either an incident report or an appointment\n");
            return false;
        }
        return true;
    }

    private boolean validateParticipants(Sessions session, List<TempParticipant> participants, Violation violation, StringBuilder errors) {
        if (participants.isEmpty()) {
            errors.append("- At least one participant is required\n");
            return false;
        }

        boolean hasReporter = participants.stream()
            .anyMatch(p -> p.isReporter());
        boolean hasViolator = participants.stream()
            .anyMatch(p -> p.isViolator());

        // Only require a violator if a violation object is provided
        if (violation != null) {
            if (!hasViolator) {
                errors.append("- At least one participant must be marked as a violator when a violation is recorded\n");
            }
        } else { // No violation object, meaning 'No Violation' was selected or no violation happened
            if (hasViolator) {
                errors.append("- Cannot mark a participant as violator when no violation is recorded\n");
            }
        }

        // Reporter is optional, so no error for missing reporter here
        return errors.length() == 0; // Return true if no new errors were added by this method
    }

    private boolean validateSessionDetails(Sessions session, StringBuilder errors) {
        if (session.getSessionSummary() == null || session.getSessionSummary().trim().isEmpty()) {
            errors.append("- Session summary is required\n");
        }

        if (session.getSessionNotes() == null || session.getSessionNotes().trim().isEmpty()) {
            errors.append("- Session notes are required\n");
        }

        if (session.getSessionStatus() == null || session.getSessionStatus().trim().isEmpty()) {
            errors.append("- Session status is required\n");
        }

        return errors.length() == 0;
    }

    private boolean validateViolationData(Violation violation, StringBuilder errors) {
        if (violation.getViolationType() == null || violation.getViolationType().trim().isEmpty()) {
            errors.append("- Violation type is required\n");
        }

        if ("Bullying".equals(violation.getViolationType())) {
            lyfjshs.gomis.Database.entity.ViolationCategory category = violation.getCategory();
            if (category == null || category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
                errors.append("- Category is required for bullying violations\n");
            }
        }

        // Add validation for description and reinforcement
        if (violation.getViolationDescription() == null || violation.getViolationDescription().trim().isEmpty()) {
            errors.append("- Violation description is required\n");
        }
        if (violation.getReinforcement() == null || violation.getReinforcement().trim().isEmpty()) {
            errors.append("- Reinforcement / Intervention details are required\n");
        }

        return errors.length() == 0;
    }

    private void showErrors(StringBuilder errors) {
        ErrorDialogUtils.showError(null, "Please correct the following errors:\n\n" + errors.toString());
    }
} 