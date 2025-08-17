/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

public class TempIncidentParticipant {
    private Integer participantId;
    private Integer studentUid;
    private String firstName;
    private String lastName;
    private String type;
    private String sex;
    private String contactNumber;
    private boolean isStudent;

    public TempIncidentParticipant(Integer participantId, Integer studentUid, String firstName, String lastName, String type, String sex, String contactNumber, boolean isStudent) {
        this.participantId = participantId;
        this.studentUid = studentUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.sex = sex;
        this.contactNumber = contactNumber;
        this.isStudent = isStudent;
    }

    public TempIncidentParticipant(Integer studentUid, String firstName, String lastName, String type, String sex, String contactNumber, boolean isStudent) {
        this(null, studentUid, firstName, lastName, type, sex, contactNumber, isStudent);
    }

    public Integer getParticipantId() { return participantId; }
    public void setParticipantId(Integer participantId) { this.participantId = participantId; }
    public Integer getStudentUid() { return studentUid; }
    public void setStudentUid(Integer studentUid) { this.studentUid = studentUid; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public boolean isStudent() { return isStudent; }
    public void setStudent(boolean student) { isStudent = student; }
    public String getFullName() { return (firstName + " " + lastName).trim(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TempIncidentParticipant that = (TempIncidentParticipant) o;
        if (isStudent && that.isStudent) {
            return studentUid != null && studentUid.equals(that.studentUid);
        } else if (!isStudent && !that.isStudent) {
            return getFullName().equalsIgnoreCase(that.getFullName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return isStudent && studentUid != null ? studentUid.hashCode() : getFullName().toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return getFullName() + " (" + type + ")";
    }

    public static TempIncidentParticipant createStudent(Integer studentUid, String firstName, String lastName, String sex, String contactNumber) {
        return new TempIncidentParticipant(null, studentUid, firstName, lastName, "Student", sex, contactNumber, true);
    }

    public static TempIncidentParticipant createNonStudent(String firstName, String lastName, String type, String sex, String contactNumber) {
        return new TempIncidentParticipant(null, null, firstName, lastName, type, sex, contactNumber, false);
    }
}
