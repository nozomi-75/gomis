package lyfjshs.gomis.view.sessions;

import lyfjshs.gomis.Database.entity.Participants;

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

    public Integer getStudentUid() {
        return studentUid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getType() {
        return type;
    }

    public String getSex() {
        return sex;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public boolean isStudent() {
        return isStudent;
    }

    public Integer getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Integer participantId) {
        this.participantId = participantId;
    }

    public boolean isViolator() {
        return isViolator;
    }

    public void setViolator(boolean isViolator) {
        this.isViolator = isViolator;
    }

    public boolean isReporter() {
        return isReporter;
    }

    public void setReporter(boolean isReporter) {
        this.isReporter = isReporter;
    }

    public Participants toParticipant() {
        Participants participant = new Participants();
        participant.setParticipantId(this.participantId != null ? this.participantId : 0);
        participant.setStudentUid(this.studentUid);
        participant.setParticipantFirstName(this.firstName);
        participant.setParticipantLastName(this.lastName);
        participant.setParticipantType(this.type);
        participant.setSex(this.sex);
        participant.setContactNumber(this.contactNumber);
        participant.setReporter(this.isReporter);
        return participant;
    }
}