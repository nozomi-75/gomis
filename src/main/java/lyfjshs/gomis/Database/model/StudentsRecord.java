package lyfjshs.gomis.Database.model;

import java.sql.Timestamp;

public class StudentsRecord {
    private int studentRecordId;
    private int studentUid;
    private int violationId;
    private String typeOfStudent;
    private String academicYear;
    private String semester;
    private String strand;
    private String track;
    private String yearLevel;
    private String adviser;
    private String section;
    private String status;
    private Timestamp updatedAt;

    public StudentsRecord(
            int studentRecordId,
            int studentUid,
            int violationId,
            String typeOfStudent,
            String academicYear,
            String semester,
            String strand,
            String track,
            String yearLevel,
            String adviser,
            String section,
            String status,
            Timestamp updatedAt) {
        this.studentRecordId = studentRecordId;
        this.studentUid = studentUid;
        this.violationId = violationId;
        this.typeOfStudent = typeOfStudent;
        this.academicYear = academicYear;
        this.semester = semester;
        this.strand = strand;
        this.track = track;
        this.yearLevel = yearLevel;
        this.adviser = adviser;
        this.section = section;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public StudentsRecord() {
    }

    public int getStudentRecordId() {
        return studentRecordId;
    }

    public void setStudentRecordId(int studentRecordId) {
        this.studentRecordId = studentRecordId;
    }

    public int getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(int studentUid) {
        this.studentUid = studentUid;
    }

    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }

    public String getTypeOfStudent() {
        return typeOfStudent;
    }

    public void setTypeOfStudent(String typeOfStudent) {
        this.typeOfStudent = typeOfStudent;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getAdviser() {
        return adviser;
    }

    public void setAdviser(String adviser) {
        this.adviser = adviser;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() { // Useful for debugging
        return "StudentRecord{" +
                "studentRecordId=" + studentRecordId +
                ", studentUid=" + studentUid +
                ", violationId=" + violationId +
                ", typeOfStudent='" + typeOfStudent + '\'' +
                ", academicYear='" + academicYear + '\'' +
                ", semester='" + semester + '\'' +
                ", strand='" + strand + '\'' +
                ", track='" + track + '\'' +
                ", yearLevel=" + yearLevel +
                ", adviser='" + adviser + '\'' +
                ", section='" + section + '\'' +
                ", status='" + status + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}