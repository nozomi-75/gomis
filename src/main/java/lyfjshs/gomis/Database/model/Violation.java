package lyfjshs.gomis.Database.model;

import java.sql.Timestamp;

public class Violation {
    private int violationId;
    private int studentUid;
    private String violationType;
    private String violationDescription;
    private String anecdotalRecord;
    private String reinforcement;
    private String status;
    private Timestamp updatedAt;

    // Constructor (Empty)
    public Violation() {}

    // Constructor (Full)
    public Violation(int violationId, int studentUid, String violationType, String violationDescription,
                     String anecdotalRecord, String reinforcement, String status, Timestamp updatedAt) {
        this.violationId = violationId;
        this.studentUid = studentUid;
        this.violationType = violationType;
        this.violationDescription = violationDescription;
        this.anecdotalRecord = anecdotalRecord;
        this.reinforcement = reinforcement;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public int getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(int studentUid) {
        this.studentUid = studentUid;
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

    public String getAnecdotalRecord() {
        return anecdotalRecord;
    }

    public void setAnecdotalRecord(String anecdotalRecord) {
        this.anecdotalRecord = anecdotalRecord;
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

    // ToString Method
    @Override
    public String toString() {
        return "Violation{" +
                "violationId=" + violationId +
                ", studentUid=" + studentUid +
                ", violationType='" + violationType + '\'' +
                ", violationDescription='" + violationDescription + '\'' +
                ", anecdotalRecord='" + anecdotalRecord + '\'' +
                ", reinforcement='" + reinforcement + '\'' +
                ", status='" + status + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
