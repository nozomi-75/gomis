/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

public class Parents {
    private int parentId;
    private String fatherFirstname;
    private String fatherLastname;
    private String fatherMiddlename;
    private String fatherContactNumber;
    private String motherFirstname;
    private String motherLastname;
    private String motherMiddlename;
    private String motherContactNumber;

    // ✅ Default constructor (needed for frameworks like Hibernate)
    public Parents() {
    }

    // ✅ Constructor without ID (useful when inserting a new record)
    public Parents(String fatherFirstname, String fatherLastname, String fatherMiddlename, String fatherContactNumber,
                   String motherFirstname, String motherLastname, String motherMiddlename, String motherContactNumber) {
        this.fatherFirstname = fatherFirstname;
        this.fatherLastname = fatherLastname;
        this.fatherMiddlename = fatherMiddlename;
        this.fatherContactNumber = fatherContactNumber;
        this.motherFirstname = motherFirstname;
        this.motherLastname = motherLastname;
        this.motherMiddlename = motherMiddlename;
        this.motherContactNumber = motherContactNumber;
    }

    // ✅ Constructor with ID (useful when retrieving data from DB)
    public Parents(int parentId, String fatherFirstname, String fatherLastname, String fatherMiddlename, String fatherContactNumber,
                   String motherFirstname, String motherLastname, String motherMiddlename, String motherContactNumber) {
        this.parentId = parentId;
        this.fatherFirstname = fatherFirstname;
        this.fatherLastname = fatherLastname;
        this.fatherMiddlename = fatherMiddlename;
        this.fatherContactNumber = fatherContactNumber;
        this.motherFirstname = motherFirstname;
        this.motherLastname = motherLastname;
        this.motherMiddlename = motherMiddlename;
        this.motherContactNumber = motherContactNumber;
    }

    // ✅ Getters and Setters
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public String getFatherFirstname() { return fatherFirstname; }
    public void setFatherFirstname(String fatherFirstname) { this.fatherFirstname = fatherFirstname; }

    public String getFatherLastname() { return fatherLastname; }
    public void setFatherLastname(String fatherLastname) { this.fatherLastname = fatherLastname; }

    public String getFatherMiddlename() { return fatherMiddlename; }
    public void setFatherMiddlename(String fatherMiddlename) { this.fatherMiddlename = fatherMiddlename; }

    public String getFatherContactNumber() { return fatherContactNumber; }
    public void setFatherContactNumber(String fatherContactNumber) { this.fatherContactNumber = fatherContactNumber; }

    public String getMotherFirstname() { return motherFirstname; }
    public void setMotherFirstname(String motherFirstname) { this.motherFirstname = motherFirstname; }

    public String getMotherLastname() { return motherLastname; }
    public void setMotherLastname(String motherLastname) { this.motherLastname = motherLastname; }

    public String getMotherMiddlename() { return motherMiddlename; }
    public void setMotherMiddlename(String motherMiddlename) { this.motherMiddlename = motherMiddlename; }

    public String getMotherContactNumber() { return motherContactNumber; }
    public void setMotherContactNumber(String motherContactNumber) { this.motherContactNumber = motherContactNumber; }
}
