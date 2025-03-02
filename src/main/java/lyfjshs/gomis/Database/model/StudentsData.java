package lyfjshs.gomis.Database.model;

import java.sql.Date;

import lyfjshs.gomis.Database.entity.Parents;

public class StudentsData {
    private int studentUid;
    private int parentId;
    private int guardianId;
    private int addressId; // Updated to reflect ADDRESS_ID foreign key
    private int contactId; // Updated to reflect CONTACT_ID foreign key
    private String lrn;
    private String lastName;
    private String firstName;
    private String middleName;
    private String sex; // Renamed from SEX to follow Java naming conventions
    private Date birthDate;
    private String motherTongue;
    private int age;
    private String ipType;
    private String religion;
    private Address address; // Nested Address object
    private Contact contact; // Nested Contact object
    private PARENTS PARENTS; // Nested PARENTS object
    private Guardian guardian; // Nested Guardian object
    

    // Constructors
    public StudentsData() {}

    public StudentsData(int studentUid, int parentId, int guardianId, int addressId, int contactId,
                        String lrn, String lastName, String firstName, String middleName, String sex, Date birthDate,
                        String motherTongue, int age, String ipType, String religion, Address address, Contact contact,
                        PARENTS parent, Guardian guardian) {
        this.studentUid = studentUid;
        this.parentId = parentId;
        this.guardianId = guardianId;
        this.addressId = addressId;
        this.contactId = contactId;
        this.lrn = lrn;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.sex = sex;
        this.birthDate = birthDate;
        this.motherTongue = motherTongue;
        this.age = age;
        this.ipType = ipType;
        this.religion = religion;
        this.address = address;
        this.contact = contact;
        this.PARENTS = parent;
        this.guardian = guardian;
    }

    // Getters and Setters
    public int getStudentUid() { return studentUid; }
    public void setStudentUid(int studentUid) { this.studentUid = studentUid; }
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }
    public int getGuardianId() { return guardianId; }
    public void setGuardianId(int guardianId) { this.guardianId = guardianId; }
   public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }
    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }
    public String getLrn() { return lrn; }
    public void setLrn(String lrn) { this.lrn = lrn; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    public String getMotherTongue() { return motherTongue; }
    public void setMotherTongue(String motherTongue) { this.motherTongue = motherTongue; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getIpType() { return ipType; }
    public void setIpType(String ipType) { this.ipType = ipType; }
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }
    public PARENTS getParent() { return PARENTS; }
    public void setParent(PARENTS parent) { this.PARENTS = parent; }
    public Guardian getGuardian() { return guardian; }
    public void setGuardian(Guardian guardian) { this.guardian = guardian; }

    @Override
    public String toString() {
        return "StudentsData{" +
                "studentUid=" + studentUid +
                ", parentId=" + parentId +
                ", guardianId=" + guardianId +
                                ", addressId=" + addressId +
                ", contactId=" + contactId +
                ", lrn='" + lrn + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", sex='" + sex + '\'' +
                ", birthDate=" + birthDate +
                ", motherTongue='" + motherTongue + '\'' +
                ", age=" + age +
                ", ipType='" + ipType + '\'' +
                ", religion='" + religion + '\'' +
                ", address=" + address +
                ", contact=" + contact +
                ", parent=" + PARENTS +
                ", guardian=" + guardian +
                '}';
    }

    public Parents getParents() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getParents'");
    }
}


