package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private int appointmentId;
    private Integer guidanceCounselorId;
    private String appointmentTitle;
    private String ConsultationType;
    private Timestamp appointmentDateTime;
    private String appointmentStatus;
    private String appointmentNotes;
    private Timestamp updatedAt;
    private List<Participants> participants; // New field for multiple participants

    public Appointment() {
    }

    public Appointment(int appointmentId,
            Integer guidanceCounselorId, // Change to Integer
            String appointmentTitle,
            String ConsultationType,
            Timestamp appointmentDateTime,
            String appointmentStatus,
            String appointmentNotes,
            Timestamp updatedAt) {
        this.appointmentId = appointmentId;
        this.guidanceCounselorId = guidanceCounselorId;
        this.appointmentTitle = appointmentTitle;
        this.ConsultationType = ConsultationType;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentStatus = appointmentStatus;
        this.appointmentNotes = appointmentNotes;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters...
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
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

    public String getConsultationType() {
        return ConsultationType;
    }

    public void setConsultationType(String ConsultationType) {
        this.ConsultationType = ConsultationType;
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

    public List<Participants> getParticipants() {
        return participants != null ? participants : new ArrayList<>();
    }

    public void setParticipants(List<Participants> participants) {
        this.participants = participants;
    }
}