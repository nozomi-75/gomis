package lyfjshs.gomis.Database.entity;

import java.sql.Date;

public class Student {
    private int studentUid;
    private int parentId;
    private int guardianId;
    private int addressId;
    private int contactId;
    private String studentLrn;
    private String studentLastname;
    private String studentFirstname;
    private String studentMiddlename;
    private String studentSex;
    private Date studentBirthdate;
    private String studentMothertongue;
    private int studentAge;
    private String studentIpType;
    private String studentReligion;
    private Address address;
    private Contact contact;
    private Parents parents;
    private Guardian guardian;

    public Student(int studentUid, int parentId, int guardianId, int addressId, int contactId, String studentLrn,
            String studentLastname, String studentFirstname, String studentMiddlename, String studentSex,
            Date studentBirthdate, String studentMothertongue, int studentAge, String studentIpType,
            String studentReligion) {
        this.studentUid = studentUid;
        this.parentId = parentId;
        this.guardianId = guardianId;
        this.addressId = addressId;
        this.contactId = contactId;
        this.studentLrn = studentLrn;
        this.studentLastname = studentLastname;
        this.studentFirstname = studentFirstname;
        this.studentMiddlename = studentMiddlename;
        this.studentSex = studentSex;
        this.studentBirthdate = studentBirthdate;
        this.studentMothertongue = studentMothertongue;
        this.studentAge = studentAge;
        this.studentIpType = studentIpType;
        this.studentReligion = studentReligion;

    }

    public Student(int studentUid, int parentId, int guardianId, int addressId, int contactId, String studentLrn,
            String studentLastname, String studentFirstname, String studentMiddlename, String studentSex,
            Date studentBirthdate, String studentMothertongue, int studentAge, String studentIpType,
            String studentReligion,
            Address address, Contact contact, Parents parents, Guardian guardian) {
        this.studentUid = studentUid;
        this.parentId = parentId;
        this.guardianId = guardianId;
        this.addressId = addressId;
        this.contactId = contactId;
        this.studentLrn = studentLrn;
        this.studentLastname = studentLastname;
        this.studentFirstname = studentFirstname;
        this.studentMiddlename = studentMiddlename;
        this.studentSex = studentSex;
        this.studentBirthdate = studentBirthdate;
        this.studentMothertongue = studentMothertongue;
        this.studentAge = studentAge;
        this.studentIpType = studentIpType;
        this.studentReligion = studentReligion;
        this.address = address;
        this.contact = contact;
        this.parents = parents;
        this.guardian = guardian;
    }

    // Getters and Setters
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

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getStudentLrn() {
        return studentLrn;
    }

    public void setStudentLrn(String studentLrn) {
        this.studentLrn = studentLrn;
    }

    public String getStudentLastname() {
        return studentLastname;
    }

    public void setStudentLastname(String studentLastname) {
        this.studentLastname = studentLastname;
    }

    public String getStudentFirstname() {
        return studentFirstname;
    }

    public void setStudentFirstname(String studentFirstname) {
        this.studentFirstname = studentFirstname;
    }

    public String getStudentMiddlename() {
        return studentMiddlename;
    }

    public void setStudentMiddlename(String studentMiddlename) {
        this.studentMiddlename = studentMiddlename;
    }

    public String getStudentSex() {
        return studentSex;
    }

    public void setStudentSex(String studentSex) {
        this.studentSex = studentSex;
    }

    public Date getStudentBirthdate() {
        return studentBirthdate;
    }

    public void setStudentBirthdate(Date studentBirthdate) {
        this.studentBirthdate = studentBirthdate;
    }

    public String getStudentMothertongue() {
        return studentMothertongue;
    }

    public void setStudentMothertongue(String studentMothertongue) {
        this.studentMothertongue = studentMothertongue;
    }

    public int getStudentAge() {
        return studentAge;
    }

    public void setStudentAge(int studentAge) {
        this.studentAge = studentAge;
    }

    public String getStudentIpType() {
        return studentIpType;
    }

    public void setStudentIpType(String studentIpType) {
        this.studentIpType = studentIpType;
    }

    public String getStudentReligion() {
        return studentReligion;
    }

    public void setStudentReligion(String studentReligion) {
        this.studentReligion = studentReligion;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Parents getParents() {
        return parents;
    }

    public void setParents(Parents parents) {
        this.parents = parents;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public void setGuardian(Guardian guardian) {
        this.guardian = guardian;
    }

}
