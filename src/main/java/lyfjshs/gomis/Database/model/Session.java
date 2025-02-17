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

    // Constructor, getters, and setters
    public Session(int sessionId, int appointmentId, int counselorsId, int participantId, int violationId,
            String sessionType, Timestamp sessionDateTime, String sessionNotes,
            String sessionStatus, Timestamp updatedAt) {
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

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getCounselorsId() {
        return counselorsId;
    }

    public void setCounselorsId(int counselorsId) {
        this.counselorsId = counselorsId;
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

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Timestamp getSessionDateTime() {
        return sessionDateTime;
    }

    public void setSessionDateTime(Timestamp sessionDateTime) {
        this.sessionDateTime = sessionDateTime;
    }

    public String getSessionNotes() {
        return sessionNotes;
    }

    public void setSessionNotes(String sessionNotes) {
        this.sessionNotes = sessionNotes;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}