package lyfjshs.gomis.Database.entity;

import java.util.Date;

public class Session {
    private int sessionId;
    private int appointmentId;
    private int guidanceCounselorId;
    private int participantId;
    private int violationId;
    private String sessionType;
    private Date sessionDateTime;
    private String sessionNotes;
    private String sessionStatus;
    private Date updatedAt;

    // Getters and Setters...
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public int getGuidanceCounselorId() { return guidanceCounselorId; }
    public void setGuidanceCounselorId(int guidanceCounselorId) { this.guidanceCounselorId = guidanceCounselorId; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }
    public Date getSessionDateTime() { return sessionDateTime; }
    public void setSessionDateTime(Date sessionDateTime) { this.sessionDateTime = sessionDateTime; }
    public String getSessionNotes() { return sessionNotes; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }
    public String getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(String sessionStatus) { this.sessionStatus = sessionStatus; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
