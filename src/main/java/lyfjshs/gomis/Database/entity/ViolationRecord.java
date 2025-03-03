package lyfjshs.gomis.Database.entity;

import java.util.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ViolationRecord {
    private int violationId;
    private int participantId;
    private String violationType;
    private String violationDescription;
    private String anecdotalRecord;
    private String reinforcement;
    private String status;
    private Date updatedAt;

    // Full Constructor
    public ViolationRecord(int violationId, int participantId, String violationType, String violationDescription,
                            String anecdotalRecord, String reinforcement, String status, Date updatedAt) {
        this.violationId = violationId;
        this.participantId = participantId;
        this.violationType = violationType;
        this.violationDescription = violationDescription;
        this.anecdotalRecord = anecdotalRecord;
        this.reinforcement = reinforcement;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Default Constructor
    public ViolationRecord() {}

    // Getters and Setters
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
