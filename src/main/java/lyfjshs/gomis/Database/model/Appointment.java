package lyfjshs.gomis.Database.model;

import java.time.LocalDateTime;

public class Appointment {
    private int appointmentId;
    private int participantId;
    private Integer counselorsId;
    private String appointmentTitle;
    private String appointmentType;
    private LocalDateTime appointmentDateTime;
    private String appointmentNotes;
    private String appointmentStatus;
    private LocalDateTime updatedAt;

    // Constructor
    public Appointment() {}

    public Appointment(int appointmentId, int participantId, Integer counselorsId, String appointmentTitle,
                       String appointmentType, LocalDateTime appointmentDateTime, String appointmentNotes,
                       String appointmentStatus, LocalDateTime updatedAt) {
        this.appointmentId = appointmentId;
        this.participantId = participantId;
        this.counselorsId = counselorsId;
        this.appointmentTitle = appointmentTitle;
        this.appointmentType = appointmentType;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentNotes = appointmentNotes;
        this.appointmentStatus = appointmentStatus;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
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

    public Integer getCounselorsId() {
        return counselorsId;
    }

    public void setCounselorsId(Integer counselorsId) {
        this.counselorsId = counselorsId;
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

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getAppointmentNotes() {
        return appointmentNotes;
    }

    public void setAppointmentNotes(String appointmentNotes) {
        this.appointmentNotes = appointmentNotes;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
