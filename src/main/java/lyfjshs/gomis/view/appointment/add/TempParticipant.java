package lyfjshs.gomis.view.appointment.add;

public class TempParticipant {
    private Integer studentUid;
    private String firstName;
    private String lastName;
    private String type;
    private String sex;
    private String contactNumber;
    private boolean isStudent;

    public TempParticipant(Integer studentUid, String firstName, String lastName, String type, String sex, String contactNumber, boolean isStudent) {
        this.studentUid = studentUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.sex = sex;
        this.contactNumber = contactNumber;
        this.isStudent = isStudent;
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

    public String getFullName() {
        return firstName + " " + lastName;
    }
} 