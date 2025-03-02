package lyfjshs.gomis.Database.model;

public class PARENTS {
    private int parentId;
    private String fatherLastName;
    private String fatherFirstName;
    private String fatherMiddleName;
    private String motherLastName;
    private String motherFirstName;
    private String motherMiddleName;

    // Constructor
    public PARENTS(int parentId, String fatherLastName, String fatherFirstName, String fatherMiddleName,
                  String motherLastName, String motherFirstName, String motherMiddleName) {
        this.parentId = parentId;
        this.fatherLastName = fatherLastName;
        this.fatherFirstName = fatherFirstName;
        this.fatherMiddleName = fatherMiddleName;
        this.motherLastName = motherLastName;
        this.motherFirstName = motherFirstName;
        this.motherMiddleName = motherMiddleName;
    }

    // Getters
    public int getParentId() {
        return parentId;
    }

    public String getFatherLastName() {
        return fatherLastName;
    }

    public String getFatherFirstName() {
        return fatherFirstName;
    }

    public String getFatherMiddleName() {
        return fatherMiddleName;
    }

    public String getMotherLastName() {
        return motherLastName;
    }

    public String getMotherFirstName() {
        return motherFirstName;
    }

    public String getMotherMiddleName() {
        return motherMiddleName;
    }

    // Setters
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setFatherLastName(String fatherLastName) {
        this.fatherLastName = fatherLastName;
    }

    public void setFatherFirstName(String fatherFirstName) {
        this.fatherFirstName = fatherFirstName;
    }

    public void setFatherMiddleName(String fatherMiddleName) {
        this.fatherMiddleName = fatherMiddleName;
    }

    public void setMotherLastName(String motherLastName) {
        this.motherLastName = motherLastName;
    }

    public void setMotherFirstName(String motherFirstName) {
        this.motherFirstName = motherFirstName;
    }

    public void setMotherMiddleName(String motherMiddleName) {
        this.motherMiddleName = motherMiddleName;
    }

    // toString() method for easy display
    @Override
    public String toString() {
        return "Parent{" +
                "parentId=" + parentId +
                ", fatherLastName='" + fatherLastName + '\'' +
                ", fatherFirstName='" + fatherFirstName + '\'' +
                ", fatherMiddleName='" + fatherMiddleName + '\'' +
                ", motherLastName='" + motherLastName + '\'' +
                ", motherFirstName='" + motherFirstName + '\'' +
                ", motherMiddleName='" + motherMiddleName + '\'' +
                '}';
    }

    public String getFatherFirstname() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFatherFirstname'");
    }

    public String getFatherLastname() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFatherLastname'");
    }

    public String getMotherFirstname() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMotherFirstname'");
    }

    public String getMotherLastname() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMotherLastname'");
    }
}
