package lyfjshs.gomis.Database.model;

import java.sql.Date;

public class StudentsData {
    private int studentUid;
    private String lrn;
    private String LAST_NAME;
    private String FIRST_NAME;
    private String middleInitial;
    private String suffix;
    private String gender;
    private Date dob; // Use java.sql.Date
    private String email;
    private String contactNumber;
    private String guardianName;
    private String guardianEmail;
    private String guardianContactNumber;
    private String address;

    // Constructors (both with and without arguments)
    public StudentsData() {
    }

    public StudentsData(int studentUid, String lrn, String LAST_NAME, String FIRST_NAME, String middleInitial,
            String suffix, String gender, Date dob, String email, String contactNumber, String guardianName,
            String guardianEmail, String guardianContactNumber, String address) {
        this.studentUid = studentUid;
        this.lrn = lrn;
        this.LAST_NAME = LAST_NAME;
        this.FIRST_NAME = FIRST_NAME;
        this.middleInitial = middleInitial;
        this.suffix = suffix;
        this.gender = gender;
        this.dob = dob;
        this.email = email;
        this.contactNumber = contactNumber;
        this.guardianName = guardianName;
        this.guardianEmail = guardianEmail;
        this.guardianContactNumber = guardianContactNumber;
        this.address = address;
    }

    // Getters and setters for all fields
    public int getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(int studentUid) {
        this.studentUid = studentUid;
    }

    public String getLrn() {
        return lrn;
    }

    public void setLrn(String lrn) {
        this.lrn = lrn;
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

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianEmail() {
        return guardianEmail;
    }

    public void setGuardianEmail(String guardianEmail) {
        this.guardianEmail = guardianEmail;
    }

    public String getGuardianContactNumber() {
        return guardianContactNumber;
    }

    public void setGuardianContactNumber(String guardianContactNumber) {
        this.guardianContactNumber = guardianContactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "StudentsData{" +
                "studentUid=" + studentUid +
                ", lrn='" + lrn + '\'' +
                ", LAST_NAME='" + LAST_NAME + '\'' +
                ", FIRST_NAME='" + FIRST_NAME + '\'' +
                ", middleInitial='" + middleInitial + '\'' +
                ", suffix='" + suffix + '\'' +
                ", gender='" + gender + '\'' +
                ", dob=" + dob +
                ", email='" + email + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", guardianName='" + guardianName + '\'' +
                ", guardianEmail='" + guardianEmail + '\'' +
                ", guardianContactNumber='" + guardianContactNumber + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}