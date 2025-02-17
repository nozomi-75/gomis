package lyfjshs.gomis.Database.model;

import java.sql.Date;

public class ViolationRecord  {

    private int violationId;
    private int participantId;
    private String violationType;
    private String violationDescription;
    private String anecdotalRecord;
    private String reinforcement;
    private String status;
    private Date updatedAt;
    
	public ViolationRecord() {
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the reinforcement
	 */
	public String getReinforcement() {
		return reinforcement;
	}

	/**
	 * @param reinforcement the reinforcement to set
	 */
	public void setReinforcement(String reinforcement) {
		this.reinforcement = reinforcement;
	}

	/**
	 * @return the anecdotalRecord
	 */
	public String getAnecdotalRecord() {
		return anecdotalRecord;
	}

	/**
	 * @param anecdotalRecord the anecdotalRecord to set
	 */
	public void setAnecdotalRecord(String anecdotalRecord) {
		this.anecdotalRecord = anecdotalRecord;
	}

	/**
	 * @return the violationDescription
	 */
	public String getViolationDescription() {
		return violationDescription;
	}

	/**
	 * @param violationDescription the violationDescription to set
	 */
	public void setViolationDescription(String violationDescription) {
		this.violationDescription = violationDescription;
	}

	/**
	 * @return the violationType
	 */
	public String getViolationType() {
		return violationType;
	}

	/**
	 * @param violationType the violationType to set
	 */
	public void setViolationType(String violationType) {
		this.violationType = violationType;
	}

	/**
	 * @return the participantId
	 */
	public int getParticipantId() {
		return participantId;
	}

	/**
	 * @param participantId the participantId to set
	 */
	public void setParticipantId(int participantId) {
		this.participantId = participantId;
	}

	/**
	 * @return the violationId
	 */
	public int getViolationId() {
		return violationId;
	}

	/**
	 * @param violationId the violationId to set
	 */
	public void setViolationId(int violationId) {
		this.violationId = violationId;
	}

}
