/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class Violation {
    private int violationId;
    private int participantId;
    private Integer categoryId;
    private String violationType;
    private String violationDescription;
    private String sessionSummary;
    private String reinforcement;
    private String status;
    private Timestamp updatedAt;
    private Integer sessionId;
    private String resolutionNotes;
    
    // Non-persistent fields for convenience
    private ViolationCategory category;
    private Participants participant;

    public Violation() {
    }

    public Violation(int violationId, int participantId, Integer categoryId, String violationType,
            String violationDescription, String sessionSummary, String reinforcement, String status,
            Timestamp updatedAt) {
        this.violationId = violationId;
        this.participantId = participantId;
        this.categoryId = categoryId;
        this.violationType = violationType;
        this.violationDescription = violationDescription;
        this.sessionSummary = sessionSummary;
        this.reinforcement = reinforcement;
        this.status = status;
        this.updatedAt = updatedAt;
        this.resolutionNotes = null;
    }

    public Violation(int violationId, int participantId, Integer categoryId, String violationType,
            String violationDescription, String sessionSummary, String reinforcement, String status,
            Timestamp updatedAt, Integer sessionId, String resolutionNotes) {
        this.violationId = violationId;
        this.participantId = participantId;
        this.categoryId = categoryId;
        this.violationType = violationType;
        this.violationDescription = violationDescription;
        this.sessionSummary = sessionSummary;
        this.reinforcement = reinforcement;
        this.status = status;
        this.updatedAt = updatedAt;
        this.sessionId = sessionId;
        this.resolutionNotes = resolutionNotes;
    }

    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getViolationDescription() {
        return violationDescription;
    }

    public void setViolationDescription(String violationDescription) {
        this.violationDescription = violationDescription;
    }

    public String getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(String sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public String getReinforcement() {
        return reinforcement;
    }

    public void setReinforcement(String reinforcement) {
        this.reinforcement = reinforcement;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ViolationCategory getCategory() {
        return category;
    }

    public void setCategory(ViolationCategory category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getCategoryId();
        }
    }

    public Participants getParticipant() {
        return participant;
    }

    public void setParticipant(Participants participant) {
        this.participant = participant;
        if (participant != null) {
            this.participantId = participant.getParticipantId();
        }
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
} 