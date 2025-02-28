package lyfjshs.gomis.Database.model;

public class GuidanceCounselor {
    private int GUIDANCE_COUNSELORS_ID;
    private String lastName;
    private String firstName;
    private String middleInitial;
    private String suffix;
    private String gender;
    private String specialization;
    private String contactNumber;
    private String email;
    private String position;
    private byte[] profilePicture;

    // Constructor
    public GuidanceCounselor(int GUIDANCE_COUNSELORS_ID, String lastName, String firstName, String middleInitial, String suffix,
                             String gender, String specialization, String contactNumber, String email,
                             String position, byte[] profilePicture) {
        this.GUIDANCE_COUNSELORS_ID = GUIDANCE_COUNSELORS_ID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.suffix = suffix;
        this.gender = gender;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.email = email;
        this.position = position;
        this.profilePicture = profilePicture;
    }

    // Getters and Setters
    public int getGUIDANCE_COUNSELORS_ID() {
        return GUIDANCE_COUNSELORS_ID;
    }

    public void setGUIDANCE_COUNSELORS_ID(int GUIDANCE_COUNSELORS_ID) {
        this.GUIDANCE_COUNSELORS_ID = GUIDANCE_COUNSELORS_ID;
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

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
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
