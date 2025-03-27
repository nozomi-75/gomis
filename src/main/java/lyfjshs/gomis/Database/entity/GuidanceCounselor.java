package lyfjshs.gomis.Database.entity;

public class GuidanceCounselor {
    private int guidanceCounselorId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String suffix;
    private String gender;
    private String specialization;
    private String contactNum;
    private String email;
    private String position;
    private byte[] profilePicture;

    public GuidanceCounselor(int guidanceCounselorId, String lastName, String firstName, String middleName,
            String suffix, String gender, String specialization, String contactNum, String email, String position,
            byte[] profilePicture) {
        this.guidanceCounselorId = guidanceCounselorId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.suffix = suffix;
        this.gender = gender;
        this.specialization = specialization;
        this.contactNum = contactNum;
        this.email = email;
        this.position = position;
        this.profilePicture = profilePicture;
    }

    // Getters
    public int getGuidanceCounselorId() {
        return guidanceCounselorId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getGender() {
        return gender;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getContactNum() {
        return contactNum;
    }

    public String getEmail() {
        return email;
    }

    public String getPosition() {
        return position;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    // Setters
    public void setGuidanceCounselorId(int guidanceCounselorId) {
        this.guidanceCounselorId = guidanceCounselorId;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    // Compatibility methods for LoginController
    public int getCounselorId() {
        return guidanceCounselorId;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNum = contactNumber;
    }

    public String getContactNumber() {
        return contactNum;
    }

    public void setPassword(String password) {
        // This is a compatibility method - in the actual system, password is stored in USERS table
    }
}
