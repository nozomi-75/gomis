package lyfjshs.gomis.Database.model;

import java.sql.Timestamp;

public class Incident {
    private int incidentId;
    private Integer studentUid; // Nullable
    private int participantId;
    private int violationId;
    private Timestamp incidentDate;
    private String incidentDescription;
    private String actionTaken;
    private String recommendation;
    private String status;
    private Timestamp updatedAt;

    // Constructor
    public Incident(int incidentId, Integer studentUid, int participantId, int violationId,
            Timestamp incidentDate, String incidentDescription, String actionTaken,
            String recommendation, String status, Timestamp updatedAt) {
        this.incidentId = incidentId;
        this.studentUid = studentUid;
        this.participantId = participantId;
        this.violationId = violationId;
        this.incidentDate = incidentDate;
        this.incidentDescription = incidentDescription;
        this.actionTaken = actionTaken;
        this.recommendation = recommendation;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Default constructor
    public Incident() {
    }

    // Getters and Setters
    public int getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(int incidentId) {
        this.incidentId = incidentId;
    }

    public Integer getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(Integer studentUid) {
        this.studentUid = studentUid;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public Timestamp getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(Timestamp incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getIncidentDescription() {
        return incidentDescription;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
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