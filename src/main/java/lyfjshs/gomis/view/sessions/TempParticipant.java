package lyfjshs.gomis.view.sessions;

public class TempParticipant {
    private Integer studentUid;
    private String firstName;
    private String lastName;
    private String type;
    private String sex;
    private String contactNumber;
    private boolean isStudent;

    public TempParticipant(Integer studentUid, String firstName, String lastName, String type, String sex,
            String contactNumber, boolean isStudent) {
        this.studentUid = studentUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.sex = sex;
        this.contactNumber = contactNumber;
        this.isStudent = isStudent;
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
}