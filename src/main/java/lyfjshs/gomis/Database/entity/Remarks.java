package lyfjshs.gomis.Database.entity;

import java.sql.Timestamp;

public class Remarks {
    private int remarkId;
    private int studentId;
    private String remarkText;
    private Timestamp remarkDate;

    public Remarks(int remarkId, int studentId, String remarkText, Timestamp remarkDate) {
        this.remarkId = remarkId;
        this.studentId = studentId;
        this.remarkText = remarkText;
        this.remarkDate = remarkDate;
    }

    // Getters and Setters...
    public int getRemarkId() { return remarkId; }
    public void setRemarkId(int remarkId) { this.remarkId = remarkId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getRemarkText() { return remarkText; }
    public void setRemarkText(String remarkText) { this.remarkText = remarkText; }
    public Timestamp getRemarkDate() { return remarkDate; }
    public void setRemarkDate(Timestamp remarkDate) { this.remarkDate = remarkDate; }
}
