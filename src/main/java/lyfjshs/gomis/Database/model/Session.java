package lyfjshs.gomis.Database.model;

import java.sql.Timestamp;

public class Session {
    private int sessionId;
    private int appointmentId;
    private int counselorsId;
    private int participantId;
    private int violationId;
    private String sessionType;
    private Timestamp sessionDateTime;
    private String sessionNotes;
    private String sessionStatus;
    private Timestamp updatedAt;
    private int participantCount;
    private Timestamp appointmentDateTime;

    // Constructor
    public Session(int sessionId, int appointmentId, int counselorsId, int participantId, int violationId,
                   String sessionType, Timestamp sessionDateTime, String sessionNotes, String sessionStatus,
                   Timestamp updatedAt) {
        this.sessionId = sessionId;
        this.appointmentId = appointmentId;
        this.counselorsId = counselorsId;
        this.participantId = participantId;
        this.violationId = violationId;
        this.sessionType = sessionType;
        this.sessionDateTime = sessionDateTime;
        this.sessionNotes = sessionNotes;
        this.sessionStatus = sessionStatus;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getSessionId() { return sessionId; }
    public int getAppointmentId() { return appointmentId; }
    public int getCounselorsId() { return counselorsId; }
    public int getParticipantId() { return participantId; }
    public int getViolationId() { return violationId; }
    public String getSessionType() { return sessionType; }
    public Timestamp getSessionDateTime() { return sessionDateTime; }
    public String getSessionNotes() { return sessionNotes; }
    public String getSessionStatus() { return sessionStatus; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    // Setters
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public void setCounselorsId(int counselorsId) { this.counselorsId = counselorsId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public void setSessionDateTime(Timestamp sessionDateTime) { this.sessionDateTime = sessionDateTime; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    public Timestamp getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(Timestamp appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
}