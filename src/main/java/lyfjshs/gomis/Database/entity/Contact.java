package lyfjshs.gomis.Database.entity;

public class Contact {
    private int contactId;
    private String contactNumber;

      // Constructor
      public Contact(int contactId, String contactNumber) {
        this.contactId = contactId;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters...
    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}
