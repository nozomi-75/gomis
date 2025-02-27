package lyfjshs.gomis.Database.model;

public class Guardian {

    int GUARDIAN_ID;
    String STUDENT_ID;
    String GUARIAN_FIRSTNAME;
    String GUARDIAN_MIDDLENAME;
    String GUARDIAN_LASTNAME;
    String GUARDIAN_RELATIONSHIP;

    public Guardian(int GUARDIAN_ID,
            String STUDENT_ID,
            String GUARIAN_FIRSTNAME,
            String GUARDIAN_MIDDLENAME,
            String GUARDIAN_LASTNAME,
            String GUARDIAN_RELATIONSHIP) {
    }

    public String getGUARDIAN_RELATIONSHIP() {
        return GUARDIAN_RELATIONSHIP;
    }

    public void setGUARDIAN_RELATIONSHIP(String GUARDIAN_RELATIONSHIP) {
        this.GUARDIAN_RELATIONSHIP = GUARDIAN_RELATIONSHIP;
    }

    public String getGUARDIAN_LASTNAME() {
        return GUARDIAN_LASTNAME;
    }

    public void setGUARDIAN_LASTNAME(String GUARDIAN_LASTNAME) {
        this.GUARDIAN_LASTNAME = GUARDIAN_LASTNAME;
    }

    public String getGUARDIAN_MIDDLENAME() {
        return GUARDIAN_MIDDLENAME;
    }

    public void setGUARDIAN_MIDDLENAME(String GUARDIAN_MIDDLENAME) {
        this.GUARDIAN_MIDDLENAME = GUARDIAN_MIDDLENAME;
    }

    public String getGUARIAN_FIRSTNAME() {
        return GUARIAN_FIRSTNAME;
    }

    public void setGUARIAN_FIRSTNAME(String GUARIAN_FIRSTNAME) {
        this.GUARIAN_FIRSTNAME = GUARIAN_FIRSTNAME;
    }

    public int getGUARDIAN_ID() {
        return GUARDIAN_ID;
    }

    public void setGUARDIAN_ID(int GUARDIAN_ID) {
        this.GUARDIAN_ID = GUARDIAN_ID;
    }

    public String getSTUDENT_ID() {
        return STUDENT_ID;
    }

    public void setSTUDENT_ID(String STUDENT_ID) {
        this.STUDENT_ID = STUDENT_ID;
    }

}
