/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

public class Guardian {
    private int guardianId;
    private String guardianLastname;
    private String guardianFirstname;
    private String guardianMiddlename;
    private String guardianRelationship;
    private String guardianContactNumber;

    // Constructor
    public Guardian(
            int guardianId,
            String guardianLastname, String guardianFirstname, String guardianMiddlename,
            String guardianRelationship, String guardianContactNumber) {
        this.guardianId = guardianId;
        this.guardianLastname = guardianLastname;
        this.guardianFirstname = guardianFirstname;
        this.guardianMiddlename = guardianMiddlename;
        this.guardianRelationship = guardianRelationship;
        this.guardianContactNumber = guardianContactNumber;
    }

    // Getters and Setters
    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public String getGuardianLastname() {
        return guardianLastname;
    }

    public void setGuardianLastname(String guardianLastname) {
        this.guardianLastname = guardianLastname;
    }

    public String getGuardianFirstname() {
        return guardianFirstname;
    }

    public void setGuardianFirstname(String guardianFirstname) {
        this.guardianFirstname = guardianFirstname;
    }

    public String getGuardianMiddlename() {
        return guardianMiddlename;
    }

    public void setGuardianMiddlename(String guardianMiddlename) {
        this.guardianMiddlename = guardianMiddlename;
    }

    public String getGuardianRelationship() {
        return guardianRelationship;
    }

    public void setGuardianRelationship(String guardianRelationship) {
        this.guardianRelationship = guardianRelationship;
    }

    public String getGuardianContactNumber() {
        return guardianContactNumber;
    }

    public void setGuardianContactNumber(String guardianContactNumber) {
        this.guardianContactNumber = guardianContactNumber;
    }
}
