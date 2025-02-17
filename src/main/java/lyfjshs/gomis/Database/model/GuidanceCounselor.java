package lyfjshs.gomis.Database.model;

import java.util.Arrays;

public class GuidanceCounselor {
    private int id;
    private String LAST_NAME;
    private String FIRST_NAME;
    private String middleInitial;
    private String suffix;
    private String gender;
    private String specialization;
    private String contactNumber;
    private String email;
    private String position;
    private byte[] profilePicture;

    // Constructor
    public GuidanceCounselor(int id, String LAST_NAME, String FIRST_NAME, String middleInitial, String suffix,
                             String gender, String specialization, String contactNumber, String email,
                             String position, byte[] profilePicture) {
        this.id = id;
        this.LAST_NAME = LAST_NAME;
        this.FIRST_NAME = FIRST_NAME;
        this.middleInitial = middleInitial;
        this.suffix = suffix;
        this.gender = gender;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.email = email;
        this.position = position;
        this.profilePicture = profilePicture;
    }

    // Default Constructor
    public GuidanceCounselor() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLAST_NAME() {
        return LAST_NAME;
    }

    public void setLAST_NAME(String LAST_NAME) {
        this.LAST_NAME = LAST_NAME;
    }

    public String getFIRST_NAME() {
        return FIRST_NAME;
    }

    public void setFIRST_NAME(String FIRST_NAME) {
        this.FIRST_NAME = FIRST_NAME;
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

    // Overriding toString() for easier debugging and logging
    @Override
    public String toString() {
        return "GuidanceCounselor{" +
                "id=" + id +
                ", LAST_NAME='" + LAST_NAME + '\'' +
                ", FIRST_NAME='" + FIRST_NAME + '\'' +
                ", middleInitial='" + middleInitial + '\'' +
                ", suffix='" + suffix + '\'' +
                ", gender='" + gender + '\'' +
                ", specialization='" + specialization + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", email='" + email + '\'' +
                ", position='" + position + '\'' +
                ", profilePicture=" + Arrays.toString(profilePicture) +
                '}';
    }
}
