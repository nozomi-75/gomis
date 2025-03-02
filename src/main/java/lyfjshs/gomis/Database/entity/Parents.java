package lyfjshs.gomis.Database.entity;

public class Parents {
    private int parentId;
    private String fatherFirstname;
    private String fatherLastname;
    private String fatherMiddlename;
    private String motherFirstname;
    private String motherLastname;
    private String motherMiddlename;

    // Getters and Setters...
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public String getFatherFirstname() { return fatherFirstname; }
    public void setFatherFirstname(String fatherFirstname) { this.fatherFirstname = fatherFirstname; }

    public String getFatherLastname() { return fatherLastname; }
    public void setFatherLastname(String fatherLastname) { this.fatherLastname = fatherLastname; }

    public String getFatherMiddlename() { return fatherMiddlename; }
    public void setFatherMiddlename(String fatherMiddlename) { this.fatherMiddlename = fatherMiddlename; }

    public String getMotherFirstname() { return motherFirstname; }
    public void setMotherFirstname(String motherFirstname) { this.motherFirstname = motherFirstname; }

    public String getMotherLastname() { return motherLastname; }
    public void setMotherLastname(String motherLastname) { this.motherLastname = motherLastname; }

    public String getMotherMiddlename() { return motherMiddlename; }
    public void setMotherMiddlename(String motherMiddlename) { this.motherMiddlename = motherMiddlename; }
}
