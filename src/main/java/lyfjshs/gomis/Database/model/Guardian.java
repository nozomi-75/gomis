package lyfjshs.gomis.Database.model;

public class Guardian {
    private int guardianId;
    private String studentId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String relationship;

    // Constructor
    public Guardian(int guardianId, String studentId, String lastName, String firstName, String middleName, String relationship) {
        this.guardianId = guardianId;
        this.studentId = studentId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.relationship = relationship;
    }

    // Default Constructor
    public Guardian() {
    }

    // Getters and Setters
    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    // Overriding toString() for easier debugging
    @Override
    public String toString() {
        return "Guardian{" +
                "guardianId=" + guardianId +
                ", studentId='" + studentId + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
