package lyfjshs.gomis.Database.entity;

public class GuidanceCounselor {
    private int guidanceCounselorId;
    private String lastName;
    private String firstName;
    private String middleInitial;
    private String suffix;
    private String gender;
    private String specialization;
    private int contactNum;
    private String email;
    private String position;
    private byte[] profilePicture;

    // Getters and Setters...
    public int getGuidanceCounselorId() { return guidanceCounselorId; }
    public void setGuidanceCounselorId(int guidanceCounselorId) { this.guidanceCounselorId = guidanceCounselorId; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleInitial() { return middleInitial; }
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public int getContactNum() { return contactNum; }
    public void setContactNum(int contactNum) { this.contactNum = contactNum; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public byte[] getProfilePicture() { return profilePicture; }
    public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }
}
