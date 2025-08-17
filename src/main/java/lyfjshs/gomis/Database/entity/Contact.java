/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

public class Contact {
    private int contactId;
    private String contactNumber;

      // Constructor
      public Contact(int contactId, String contactNumber) {
        this.contactId = contactId;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters...
    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
}
