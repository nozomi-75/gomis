package lyfjshs.gomis.Database.model;

public class Participants {
    private int participantId;
    private int studentId;
    private String participantType;
    private String participantName;
    private String participantContact;

    // Full Constructor
    public Participants(int participantId, int studentId, String participantType, String participantName, String participantContact) {
        this.participantId = participantId;
        this.studentId = studentId;
        this.participantType = participantType;
        this.participantName = participantName;
        this.participantContact = participantContact;
    }


    // Default Constructor
    public Participants() {}

    // Getters and Setters
    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getParticipantType() {
        return participantType;
    }

    public void setParticipantType(String participantType) {
        this.participantType = participantType;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantContact() {
        return participantContact;
    }

    public void setParticipantContact(String participantContact) {
        this.participantContact = participantContact;
    }

    // Overriding toString() for easy debugging
    @Override
    public String toString() {
        return "Participant{" +
                "participantId=" + participantId +
                ", studentId=" + studentId +
                ", participantType='" + participantType + '\'' +
                ", participantName='" + participantName + '\'' +
                ", participantContact='" + participantContact + '\'' +
                '}';
    }
}
