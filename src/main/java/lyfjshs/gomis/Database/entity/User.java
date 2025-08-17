/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

public class User {
    private int userId;
    private String uName;
    private String uPass;
    private int guidanceCounselorId;

    // Getters and Setters...
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getuName() { return uName; }
    public void setuName(String uName) { this.uName = uName; }
    public String getuPass() { return uPass; }
    public void setuPass(String uPass) { this.uPass = uPass; }
    public int getGuidanceCounselorId() { return guidanceCounselorId; }
    public void setGuidanceCounselorId(int guidanceCounselorId) { this.guidanceCounselorId = guidanceCounselorId; }
}
