package lyfjshs.gomis.Database.entity;

import java.util.Date;

public class ViolationRecord {
    private int violationId;
    private int participantId;
    private String violationType;
    private String violationDescription;
    private String anecdotalRecord;
    private String reinforcement;
    private String status;
    private Date updatedAt;

    // Getters and Setters...
    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public String getViolationDescription() { return violationDescription; }
    public void setViolationDescription(String violationDescription) { this.violationDescription = violationDescription; }
    public String getAnecdotalRecord() { return anecdotalRecord; }
    public void setAnecdotalRecord(String anecdotalRecord) { this.anecdotalRecord = anecdotalRecord; }
    public String getReinforcement() { return reinforcement; }
    public void setReinforcement(String reinforcement) { this.reinforcement = reinforcement; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
