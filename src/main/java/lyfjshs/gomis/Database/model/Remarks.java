package lyfjshs.gomis.Database.model;

import java.sql.Timestamp;

public class Remarks {
    private int remarkId;
    private String studentId;
    private String remarkText;
    private Timestamp remarkDate;

    // Constructor
    public Remarks(int remarkId, String studentId, String remarkText, Timestamp remarkDate) {
        this.remarkId = remarkId;
        this.studentId = studentId;
        this.remarkText = remarkText;
        this.remarkDate = remarkDate;
    }

    // Getters and Setters
    public int getRemarkId() {
        return remarkId;
    }

    public void setRemarkId(int remarkId) {
        this.remarkId = remarkId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getRemarkText() {
        return remarkText;
    }

    public void setRemarkText(String remarkText) {
        this.remarkText = remarkText;
    }

    public Timestamp getRemarkDate() {
        return remarkDate;
    }

    public void setRemarkDate(Timestamp remarkDate) {
        this.remarkDate = remarkDate;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Remarks{" +
                "remarkId=" + remarkId +
                ", studentId='" + studentId + '\'' +
                ", remarkText='" + remarkText + '\'' +
                ", remarkDate=" + remarkDate +
                '}';
    }
}
