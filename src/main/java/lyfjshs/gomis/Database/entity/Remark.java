package lyfjshs.gomis.Database.entity;

import java.util.Date;

public class Remark {
    private int remarkId;
    private int studentId;
    private String remarkText;
    private Date remarkDate;

    // Getters and Setters...
    public int getRemarkId() { return remarkId; }
    public void setRemarkId(int remarkId) { this.remarkId = remarkId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getRemarkText() { return remarkText; }
    public void setRemarkText(String remarkText) { this.remarkText = remarkText; }
    public Date getRemarkDate() { return remarkDate; }
    public void setRemarkDate(Date remarkDate) { this.remarkDate = remarkDate; }
}
