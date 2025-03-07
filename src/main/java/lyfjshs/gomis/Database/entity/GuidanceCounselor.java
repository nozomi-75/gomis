package lyfjshs.gomis.Database.entity;

public class GuidanceCounselor {
    private int guidanceCounselorId;
    private String lastName;
    private String firstName;
    private String middleName;  // Renamed from middleInitial
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
        this.middleName = middleName;  // Updated
        this.suffix = suffix;
        this.gender = gender;
        this.specialization = specialization;
        this.contactNum = contactNum;
        this.email = email;
        this.position = position;
        this.profilePicture = profilePicture;
    }

    public int getGuidanceCounselorId() {
        return guidanceCounselorId;
    }

    public void setGuidanceCounselorId(int guidanceCounselorId) {
        this.guidanceCounselorId = guidanceCounselorId;
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

    public String getMiddleName() {  // Renamed method
        return middleName;
    }

    public void setMiddleName(String middleName) {  // Renamed method
        this.middleName = middleName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
}
