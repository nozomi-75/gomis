package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class Appointment {
    private int appointmentId;
    private int participantId;
    private Integer guidanceCounselorId; // Change to Integer
    private String appointmentTitle;
    private String appointmentType;
    private Timestamp appointmentDateTime;
    private String appointmentStatus;
    private String appointmentNotes;
    private Timestamp updatedAt;

    public Appointment() {
    }

    public Appointment(int appointmentId,
            int participantId,
            Integer guidanceCounselorId, // Change to Integer
            String appointmentTitle,
            String appointmentType,
            Timestamp appointmentDateTime,
            String appointmentStatus,
            Timestamp updatedAt) {
        this.appointmentId = appointmentId;
        this.participantId = participantId;
        this.guidanceCounselorId = guidanceCounselorId;
        this.appointmentTitle = appointmentTitle;
        this.appointmentType = appointmentType;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentStatus = appointmentStatus;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters...
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Integer getGuidanceCounselorId() { // Change to Integer
        return guidanceCounselorId;
    }

    public void setGuidanceCounselorId(Integer guidanceCounselorId) { // Change to Integer
        this.guidanceCounselorId = guidanceCounselorId;
    }

    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    public void setAppointmentTitle(String appointmentTitle) {
        this.appointmentTitle = appointmentTitle;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public Timestamp getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(Timestamp appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getAppointmentNotes() {
        return appointmentNotes;
    }

    public void setAppointmentNotes(String appointmentNotes) {
        this.appointmentNotes = appointmentNotes;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}