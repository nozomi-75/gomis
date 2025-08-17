/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private Integer appointmentId;
    private Integer guidanceCounselorId;
    private String appointmentTitle;
    private String consultationType;
    private Timestamp appointmentDateTime;
    private String appointmentStatus;
    private String appointmentNotes;
    private Timestamp updatedAt;
    private List<Participants> participants;
    private boolean reminderShown = false;

    public Appointment() {
        this.participants = new ArrayList<>();
    }

    public Appointment(Integer appointmentId,
            Integer guidanceCounselorId,
            String appointmentTitle,
            String consultationType,
            Timestamp appointmentDateTime,
            String appointmentStatus,
            String appointmentNotes,
            Timestamp updatedAt) {
        this.appointmentId = appointmentId;
        this.guidanceCounselorId = guidanceCounselorId;
        this.appointmentTitle = appointmentTitle;
        this.consultationType = consultationType;
        this.appointmentDateTime = appointmentDateTime;
        this.appointmentStatus = appointmentStatus;
        this.appointmentNotes = appointmentNotes;
        this.updatedAt = updatedAt;
        this.participants = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getGuidanceCounselorId() {
        return guidanceCounselorId;
    }

    public void setGuidanceCounselorId(Integer guidanceCounselorId) {
        this.guidanceCounselorId = guidanceCounselorId;
    }

    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    public void setAppointmentTitle(String appointmentTitle) {
        this.appointmentTitle = appointmentTitle;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
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
        if (participants == null) {
            participants = new ArrayList<>();
        }
        return participants;
    }

    public void setParticipants(List<Participants> participants) {
        this.participants = participants != null ? participants : new ArrayList<>();
    }

    public void addParticipant(Participants participant) {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        if (participant != null && !participants.contains(participant)) {
            participants.add(participant);
        }
    }

    public void removeParticipant(Participants participant) {
        if (participants != null) {
            participants.remove(participant);
        }
    }

    public List<Integer> getParticipantIds() {
        List<Integer> ids = new ArrayList<>();
        if (participants != null) {
            for (Participants participant : participants) {
                Integer participantId = participant.getParticipantId();
                if (participantId != null) {
                    ids.add(participantId);
                }
            }
        }
        return ids;
    }

    public boolean isReminderShown() {
        return reminderShown;
    }

    public void setReminderShown(boolean reminderShown) {
        this.reminderShown = reminderShown;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", guidanceCounselorId=" + guidanceCounselorId +
                ", appointmentTitle='" + appointmentTitle + '\'' +
                ", consultationType='" + consultationType + '\'' +
                ", appointmentDateTime=" + appointmentDateTime +
                ", appointmentStatus='" + appointmentStatus + '\'' +
                ", appointmentNotes='" + appointmentNotes + '\'' +
                ", updatedAt=" + updatedAt +
                ", participantsCount=" + (participants != null ? participants.size() : 0) +
                '}';
    }
}