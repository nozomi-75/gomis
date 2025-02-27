package lyfjshs.gomis.Database.model;

public class Contact {
    private int c_id;
    private String contact;

    // Constructor
    public Contact(int c_id, String contact) {
        this.c_id = c_id;
        this.contact = contact;
    }

    // Getter and Setter for C_ID
    public int getC_id() {
        return c_id;
    }

    public void setC_id(int c_id) {
        this.c_id = c_id;
    }

    // Getter and Setter for Contact
    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    // Override toString for debugging and logging
    @Override
    public String toString() {
        return "Contact{" +
                "c_id=" + c_id +
                ", contact='" + contact + '\'' +
                '}';
    }
}
