package lyfjshs.gomis.Database.model;

import java.sql.Date;

public class StudentsData {
    private int studentUid;
    private int parentId;
    private int guardianId;
    private int appointmentsId;
    private int contactId;
    private String lrn;
    private String lastName;
    private String firstName;
    private String middleName;
    private String SEX;
    private Date birthDate;
    private String motherTongue;
    private int age;
    private String ipType;
    private String religion;

    // Constructors (both with and without arguments)
    public StudentsData() {
    }

    public StudentsData(int studentUid, int parentId, int guardianId, int appointmentsId, int contactId, String lrn,
                        String lastName, String firstName, String middleName, String SEX, Date birthDate, String motherTongue,
                        int age, String ipType, String religion) {
        this.studentUid = studentUid;
        this.parentId = parentId;
        this.guardianId = guardianId;
        this.appointmentsId = appointmentsId;
        this.contactId = contactId;
        this.lrn = lrn;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.SEX = SEX;
        this.birthDate = birthDate;
        this.motherTongue = motherTongue;
        this.age = age;
        this.ipType = ipType;
        this.religion = religion;
    }

    // Getters and setters for all fields
    public int getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(int studentUid) {
        this.studentUid = studentUid;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public int getAppointmentsId() {
        return appointmentsId;
    }

    public void setAppointmentsId(int appointmentsId) {
        this.appointmentsId = appointmentsId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getLrn() {
        return lrn;
    }

    public void setLrn(String lrn) {
        this.lrn = lrn;
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getMotherTongue() {
        return motherTongue;
    }

    public void setMotherTongue(String motherTongue) {
        this.motherTongue = motherTongue;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getIpType() {
        return ipType;
    }

    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    @Override
    public String toString() {
        return "StudentsData{" +
                "studentUid=" + studentUid +
                ", parentId=" + parentId +
                ", guardianId=" + guardianId +
                ", appointmentsId=" + appointmentsId +
                ", contactId=" + contactId +
                ", lrn='" + lrn + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", birthDate=" + birthDate +
                ", motherTongue='" + motherTongue + '\'' +
                ", age=" + age +
                ", ipType='" + ipType + '\'' +
                ", religion='" + religion + '\'' +
                '}';
    }

    public String getSEX() {
        return SEX;
    }

    public void setSEX(String SEX) {
        this.SEX = SEX;
    }
}