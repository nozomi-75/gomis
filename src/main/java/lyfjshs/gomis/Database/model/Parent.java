package lyfjshs.gomis.Database.model;

public class Parent {
    private int parentId;
    private String lastName;
    private String firstName;
    private String middleName;

    // Constructor
    public Parent(int parentId, String lastName, String firstName, String middleName) {
        this.parentId = parentId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
    }

    // Getters
    public int getParentId() {
        return parentId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    // Setters
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    // toString() method for easy display
    @Override
    public String toString() {
        return "Parent{" +
                "parentId=" + parentId +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", middleName='" + middleName + '\'' +
                '}';
    }
}
