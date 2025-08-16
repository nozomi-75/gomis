package lyfjshs.gomis.view.appointment.add;

public class TempParticipant {
    private Integer studentUid;
    private String firstName;
    private String lastName;
    private String type;
    private String sex;
    private String contactNumber;
    private boolean isStudent;
    private Integer participantId;
    private boolean isViolator;
    private boolean isReporter;

    // Single, comprehensive constructor
    public TempParticipant(Integer participantId, Integer studentUid, String firstName, String lastName, String type, String sex,
            String contactNumber, boolean isStudent, boolean isViolator, boolean isReporter) {
        this.participantId = participantId;
        this.studentUid = studentUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.sex = sex;
        this.contactNumber = contactNumber;
        this.isStudent = isStudent;
        this.isViolator = isViolator;
        this.isReporter = isReporter;
    }

    // Getters and setters
    public Integer getStudentUid() { return studentUid; }
    public void setStudentUid(Integer studentUid) { this.studentUid = studentUid; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public boolean isStudent() { return isStudent; }
    public void setStudent(boolean isStudent) { this.isStudent = isStudent; }
    public Integer getParticipantId() { return participantId; }
    public void setParticipantId(Integer participantId) { this.participantId = participantId; }
    public boolean isViolator() { return isViolator; }
    public void setViolator(boolean isViolator) { this.isViolator = isViolator; }
    public boolean isReporter() { return isReporter; }
    public void setReporter(boolean isReporter) { this.isReporter = isReporter; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
} 