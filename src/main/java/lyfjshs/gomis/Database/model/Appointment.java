package lyfjshs.gomis.Database.model;

import java.time.LocalDateTime;

public class Appointment {
	private Integer appointmentId;
	private Integer studentUid;
	private Integer counselorsId;
	private String appointmentType;
	private LocalDateTime appointmentDateTime;
	private String appointmentStatus;
	private LocalDateTime updatedAt;

	// Default constructor
	public Appointment() {}

	// Constructor without appointmentId (for new appointments)
	public Appointment(Integer studentUid, Integer counselorsId, 
					  String appointmentType, LocalDateTime appointmentDateTime, 
					  String appointmentStatus) {
		this.studentUid = studentUid;
		this.counselorsId = counselorsId;
		this.appointmentType = appointmentType;
		this.appointmentDateTime = appointmentDateTime;
		this.appointmentStatus = appointmentStatus;
	}

	// Constructor for database retrieval
	public Appointment(Integer appointmentId, Integer studentUid, Integer counselorsId,
					  String appointmentType, LocalDateTime appointmentDateTime,
					  String appointmentStatus) {
		this.appointmentId = appointmentId;
		this.studentUid = studentUid;
		this.counselorsId = counselorsId;
		this.appointmentType = appointmentType;
		this.appointmentDateTime = appointmentDateTime;
		this.appointmentStatus = appointmentStatus;
	}

	public Integer getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(Integer appointmentId) {
		this.appointmentId = appointmentId;
	}

	public Integer getStudentUid() {
		return studentUid;
	}

	public void setStudentUid(Integer studentUid) {
		this.studentUid = studentUid;
	}

	public Integer getCounselorsId() {
		return counselorsId;
	}

	public void setCounselorsId(Integer counselorsId) {
		this.counselorsId = counselorsId;
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

	@Override
	public String toString() {
		return String.format(
			"Appointment[id=%d, studentUid=%s, counselorId=%s, type='%s', dateTime=%s, status='%s']",
			appointmentId, 
			studentUid != null ? studentUid : "N/A", 
			counselorsId != null ? counselorsId : "N/A", 
			appointmentType, 
			appointmentDateTime, 
			appointmentStatus
		);
	}
}
