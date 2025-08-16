package lyfjshs.gomis.Database.entity;

public class Participants {
    private int participantId;
    private Integer studentUid;
    private String participantType;
    private String participantLastName;
    private String participantFirstName;
    private String sex;
    private String contactNumber;
    private Student student;
    private boolean isReporter;

    public Participants() {
    }

    // Constructor with fields
    public Participants(Integer studentUid, String participantType, String participantLastName,
            String participantFirstName, String sex, String contactNumber) {
        this.studentUid = studentUid;
        this.participantType = participantType;
        this.participantLastName = participantLastName;
        this.participantFirstName = participantFirstName;
        this.sex = sex;
        this.contactNumber = contactNumber;
        this.isReporter = false;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
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

    public boolean isReporter() {
        return isReporter;
    }

    public void setReporter(boolean reporter) {
        isReporter = reporter;
    }

    public String getFullName() {
        return participantFirstName + " " + participantLastName;
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
                ", sex='" + sex + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", isReporter=" + isReporter +
                '}';
    }
}
