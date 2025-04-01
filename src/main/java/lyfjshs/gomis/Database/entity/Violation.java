package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class Violation {
    private int violationId;
    private int participantId;
    private String violationType;
    private String violationDescription;
    private String sessionSummary;
    private String reinforcement;
    private String status;
    private Timestamp updatedAt;

    public Violation(int violationId, int participantId, String violationType, String violationDescription,
            String sessionSummary, String reinforcement, String status, Timestamp updatedAt) {
        this.violationId = violationId;
        this.participantId = participantId;
        this.violationType = violationType;
        this.violationDescription = violationDescription;
        this.sessionSummary = sessionSummary;
        this.reinforcement = reinforcement;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getViolationDescription() {
        return violationDescription;
    }

    public void setViolationDescription(String violationDescription) {
        this.violationDescription = violationDescription;
    }

    public String getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(String sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public String getReinforcement() {
        return reinforcement;
    }

    public void setReinforcement(String reinforcement) {
        this.reinforcement = reinforcement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
} 