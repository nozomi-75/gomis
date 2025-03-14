package lyfjshs.gomis.Database.entity;

public class Participants {
    private int participantId;
    private Integer studentUid;
    private String participantType;
    private String participantLastName;
    private String participantFirstName;
    private String email;
    private String contactNumber;
    private Student student;

    public Participants(
            Integer studentUid, 
            String participantType,
            String participantLastName, 
            String participantFirstName,
            String email, String contactNumber) {
        this.studentUid = studentUid;
        this.participantType = participantType;
        this.participantLastName = participantLastName;
        this.participantFirstName = participantFirstName;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    // Default Constructor
    public Participants() {
    }
    
    // Full Constructor
    public Participants(int participantId, Integer studentUid, String participantType, String participantLastName,
            String participantFirstName, String email, String contactNumber) {
        this.participantId = participantId;
        this.studentUid = studentUid;
        this.participantType = participantType;
        this.participantLastName = participantLastName;
        this.participantFirstName = participantFirstName;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public Integer getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(Integer studentUid) {
        this.studentUid = studentUid;
    }

    public String getParticipantType() {
        return participantType;
    }

    public void setParticipantType(String participantType) {
        this.participantType = participantType;
    }

    public String getParticipantLastName() {
        return participantLastName;
    }

    public void setParticipantLastName(String participantLastName) {
        this.participantLastName = participantLastName;
    }

    public String getParticipantFirstName() {
        return participantFirstName;
    }

    public void setParticipantFirstName(String participantFirstName) {
        this.participantFirstName = participantFirstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    // Overriding toString() for easy debugging
    @Override
    public String toString() {
        return "Participants{" +
                "participantId=" + participantId +
                ", studentUid=" + studentUid +
                ", participantType='" + participantType + '\'' +
                ", participantLastName='" + participantLastName + '\'' +
                ", participantFirstName='" + participantFirstName + '\'' +
                ", email='" + email + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}
