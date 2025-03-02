package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class Incident {
    private int incidentId;
    private int participantId;
    private Timestamp incidentDate;
    private String incidentDescription;
    private String actionTaken;
    private String recommendation;
    private String status;
    private Timestamp updatedAt;
    private Student student;
    private Participants participants;
    
    // Constructor
    public Incident(int incidentId, int participantId, Timestamp incidentDate, String incidentDescription,
                    String actionTaken, String recommendation, String status, Timestamp updatedAt) {
        this.incidentId = incidentId;
        this.participantId = participantId;
        this.incidentDate = incidentDate;
        this.incidentDescription = incidentDescription;
        this.actionTaken = actionTaken;
        this.recommendation = recommendation;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    // Default constructor
    public Incident() {}

    // Getters and Setters
    public int getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(int incidentId) {
        this.incidentId = incidentId;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Timestamp getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(Timestamp incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getIncidentDescription() {
        return incidentDescription;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
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

    @Override
    public String toString() {
        return "Incident{" +
                "incidentId=" + incidentId +
                ", participantId=" + participantId +
                ", incidentDate=" + incidentDate +
                ", incidentDescription='" + incidentDescription + '\'' +
                ", actionTaken='" + actionTaken + '\'' +
                ", recommendation='" + recommendation + '\'' +
                ", status='" + status + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Incident incident = (Incident) o;
        return incidentId == incident.incidentId && participantId == incident.participantId && Objects.equals(incidentDate, incident.incidentDate) && Objects.equals(incidentDescription, incident.incidentDescription) && Objects.equals(actionTaken, incident.actionTaken) && Objects.equals(recommendation, incident.recommendation) && Objects.equals(status, incident.status) && Objects.equals(updatedAt, incident.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incidentId, participantId, incidentDate, incidentDescription, actionTaken, recommendation, status, updatedAt);
    }

	/**
	 * @return the participants
	 */
	public Participants getParticipants() {
		return participants;
	}

	/**
	 * @param participants the participants to set
	 */
	public void setParticipants(Participants participants) {
		this.participants = participants;
	}

	/**
	 * @return the student
	 */
	public Student getStudent() {
		return student;
	}

	/**
	 * @param student the student to set
	 */
	public void setStudent(Student student) {
		this.student = student;
	}

    public void setViolationId(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setViolationId'");
    }
}