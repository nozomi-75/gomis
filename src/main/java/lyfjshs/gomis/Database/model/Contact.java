package lyfjshs.gomis.Database.model;

public class Contact {
    private int c_id;
    private String CONTACT_NUMBER;

    // Constructor
    public Contact(int c_id, String CONTACT_NUMBER) {
        this.c_id = c_id;
        this.CONTACT_NUMBER = CONTACT_NUMBER;
    }

    // Getter and Setter for C_ID
    public int getCONTACT_id() {
        return c_id;
    }

    public void setC_id(int c_id) {
        this.c_id = c_id;
    }

    // Getter and Setter for Contact
    public String getCONTACT_NUMBER() {
        return CONTACT_NUMBER;
    }

    public void setCONTACT_NUMBER(String CONTACT_NUMBER) {
        this.CONTACT_NUMBER = CONTACT_NUMBER;
    }

    // Override toString for debugging and logging
    @Override
    public String toString() {
        return "Contact{" +
                "c_id=" + c_id +
                ", contact='" + CONTACT_NUMBER + '\'' +
                '}';
    }
}
