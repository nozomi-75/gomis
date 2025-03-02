package lyfjshs.gomis.Database.entity;

import java.util.Date;

public class Appointment {
    private int appointmentId;
    private int participantId;
    private int guidanceCounselorId;
    private String appointmentTitle;
    private String appointmentType;
    private Date appointmentDateTime;
    private String appointmentStatus;
    private Date updatedAt;

    // Getters and Setters...
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public int getGuidanceCounselorId() { return guidanceCounselorId; }
    public void setGuidanceCounselorId(int guidanceCounselorId) { this.guidanceCounselorId = guidanceCounselorId; }
    public String getAppointmentTitle() { return appointmentTitle; }
    public void setAppointmentTitle(String appointmentTitle) { this.appointmentTitle = appointmentTitle; }
    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }
    public Date getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(Date appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public String getAppointmentStatus() { return appointmentStatus; }
    public void setAppointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
