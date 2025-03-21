package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class Sessions {
    private int sessionId;
    private Integer appointmentId;
    private int guidanceCounselorId;
    private int participantId;
    private Integer violationId; // Use Integer to handle potential null values
    private String appointmentType;
    private String consultationType;
    private Timestamp sessionDateTime;
    private String sessionNotes;
    private String sessionStatus;
    private Timestamp updatedAt;
    private int participantCount; // To store participant count if needed
    private Timestamp appointmentDateTime; // To store related appointment date time

    // Full Constructor
    public Sessions(int sessionId, Integer appointmentId, int guidanceCounselorId, int participantId, Integer violationId,
                    String appointmentType, String consultationType, Timestamp sessionDateTime, String sessionNotes, String sessionStatus,
                    Timestamp updatedAt) {
        this.sessionId = sessionId;
        this.appointmentId = appointmentId;
        this.guidanceCounselorId = guidanceCounselorId;
        this.participantId = participantId;
        this.violationId = violationId;
        this.appointmentType = appointmentType;
        this.consultationType = consultationType;
        this.sessionDateTime = sessionDateTime;
        this.sessionNotes = sessionNotes;
        this.sessionStatus = sessionStatus;
        this.updatedAt = updatedAt;
    }

    // Default Constructor
    public Sessions() {}

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getGuidanceCounselorId() {
        return guidanceCounselorId;
    }

    public void setGuidanceCounselorId(int guidanceCounselorId) {
        this.guidanceCounselorId = guidanceCounselorId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Integer getViolationId() {
        return violationId;
    }

    public void setViolationId(Integer violationId) {
        this.violationId = violationId;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
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

    @Override
    public String toString() {
        return "Sessions{" +
                "sessionId=" + sessionId +
                ", appointmentId=" + appointmentId +
                ", guidanceCounselorId=" + guidanceCounselorId +
                ", participantId=" + participantId +
                ", violationId=" + violationId +
                ", appointmentType='" + appointmentType + '\'' +
                ", consultationType='" + consultationType + '\'' +
                ", sessionDateTime=" + sessionDateTime +
                ", sessionNotes='" + sessionNotes + '\'' +
                ", sessionStatus='" + sessionStatus + '\'' +
                ", updatedAt=" + updatedAt +
                ", participantCount=" + participantCount +
                ", appointmentDateTime=" + appointmentDateTime +
                '}';
    }
}