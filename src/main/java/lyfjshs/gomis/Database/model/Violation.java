package lyfjshs.gomis.Database.model;
import java.sql.Timestamp;

/**
 * Data class representing a Violation record with associated participant information.
 */
public class Violation {
    private int violationId;
    private int participantId;
    private String participantType;
    private String violationType;
    private String violationDescription;
    private String anecdotalRecord;
    private String email;
    private String contact;
    private String reinforcement;
    private String status;
    private Timestamp updatedAt;
    private String FIRST_NAME;
    private String LAST_NAME;
    private String studentLRN;

    // Default constructor
    public Violation() {}

    // Full constructor
    public Violation(int violationId, int participantId, String participantType, 
                    String violationType, String violationDescription, String email, 
                    String contact, String reinforcement, String status) {
        this.violationId = violationId;
        this.participantId = participantId;
        this.participantType = participantType;
        this.violationType = violationType;
        this.violationDescription = violationDescription;
        this.email = email;
        this.contact = contact;
        this.reinforcement = reinforcement;
        this.status = status;
    }

    // Getters
    public int getViolationId() { return violationId; }
    public int getParticipantId() { return participantId; }
    public String getParticipantType() { return participantType; }
    public String getViolationType() { return violationType; }
    public String getViolationDescription() { return violationDescription; }
    public String getAnecdotalRecord() { return anecdotalRecord; }
    public String getEmail() { return email; }
    public String getContact() { return contact; }
    public String getReinforcement() { return reinforcement; }
    public String getStatus() { return status; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public String getFIRST_NAME() { return FIRST_NAME; }
    public String getLAST_NAME() { return LAST_NAME; }
    public String getStudentLRN() { return studentLRN; }

    // Setters
    public void setViolationId(int violationId) { this.violationId = violationId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }
    public void setParticipantType(String participantType) { this.participantType = participantType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public void setViolationDescription(String violationDescription) { this.violationDescription = violationDescription; }
    public void setAnecdotalRecord(String anecdotalRecord) { this.anecdotalRecord = anecdotalRecord; }
    public void setEmail(String email) { this.email = email; }
    public void setContact(String contact) { this.contact = contact; }
    public void setReinforcement(String reinforcement) { this.reinforcement = reinforcement; }
    public void setStatus(String status) { this.status = status; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public void setFIRST_NAME(String FIRST_NAME) { this.FIRST_NAME = FIRST_NAME; }
    public void setLAST_NAME(String LAST_NAME) { this.LAST_NAME = LAST_NAME; }
    public void setStudentLRN(String studentLRN) { this.studentLRN = studentLRN; }

    /**
     * Returns a formatted string representation of the participant information
     */
    public String getFormattedParticipantInfo() {
        if ("Student".equals(participantType) && studentLRN != null) {
            return String.format("Student (LRN: %s) - %s %s", studentLRN, FIRST_NAME, LAST_NAME);
        } else {
            return String.format("%s - %s %s", participantType, FIRST_NAME, LAST_NAME);
        }
    }

    /**
     * Returns a formatted string representation of the violation record
     */
    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s",
            getFormattedParticipantInfo(),
            email,
            contact,
            violationType,
            reinforcement,
            status
        );
    }

    public String getLRN() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLRN'");
    }
}