/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;
import java.util.List;

public class Sessions {
    private Integer sessionId;
    private Integer appointmentId;
    private Integer guidanceCounselorId;
    private Integer violationId; // Use Integer to handle potential null values
    private String appointmentType;
    private String consultationType;
    private Timestamp sessionDateTime;
    private String sessionNotes;
    private String sessionStatus;
    private Timestamp updatedAt;
    private int participantCount; // To store participant count if needed
    private Timestamp appointmentDateTime; // To store related appointment date time
    private List<Participants> participants; // ✅ Store multiple participants
    private String sessionSummary; // New field
    private String violationType;
    private Integer categoryId;

    // Default constructor
    public Sessions() {
    }

    // Full Constructor
    public Sessions(Integer sessionId, Integer appointmentId, Integer guidanceCounselorId, Integer violationId,
                    String appointmentType, String consultationType, Timestamp sessionDateTime, 
                    String sessionNotes, String sessionSummary,
                     String sessionStatus,
                    Timestamp updatedAt) {
        this.sessionId = sessionId;
        this.appointmentId = appointmentId;
        this.guidanceCounselorId = guidanceCounselorId;
        this.violationId = violationId;
        this.appointmentType = appointmentType;
        this.consultationType = consultationType;
        this.sessionDateTime = sessionDateTime;
        this.sessionNotes = sessionNotes;
        this.sessionStatus = sessionStatus;
        this.updatedAt = updatedAt;
        this.sessionSummary = sessionSummary != null ? sessionSummary : ""; // Initialize summary with empty string if null
   }

    // Getters and Setters
    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

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

    public List<Participants> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participants> participants) {
        this.participants = participants;
    }

    public String getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(String sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        return "Sessions{" +
                "sessionId=" + sessionId +
                ", appointmentId=" + appointmentId +
                ", guidanceCounselorId=" + guidanceCounselorId +
                ", violationId=" + violationId +
                ", appointmentType='" + appointmentType + '\'' +
                ", consultationType='" + consultationType + '\'' +
                ", sessionDateTime=" + sessionDateTime +
                ", sessionNotes='" + sessionNotes + '\'' +
                ", sessionStatus='" + sessionStatus + '\'' +
                ", updatedAt=" + updatedAt +
                ", participantCount=" + participantCount +
                ", appointmentDateTime=" + appointmentDateTime +
                ", sessionSummary='" + sessionSummary + '\'' +
                '}';
    }
}