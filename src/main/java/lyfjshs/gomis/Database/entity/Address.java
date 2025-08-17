/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.entity;

public class Address {
    private int addressId;
    private String addressHouseNumber;
    private String addressStreetSubdivision;
    private String addressRegion;
    private String addressProvince;
    private String addressMunicipality;
    private String addressBarangay;
    private String addressZipCode;

      // Constructor
      public Address(int addressId, String addressHouseNumber, String addressStreetSubdivision, String addressRegion, String addressProvince, String addressMunicipality, String addressBarangay, String addressZipCode) {
        this.addressId = addressId;
        this.addressHouseNumber = addressHouseNumber;
        this.addressStreetSubdivision = addressStreetSubdivision;
        this.addressRegion = addressRegion;
        this.addressProvince = addressProvince;
        this.addressMunicipality = addressMunicipality;
        this.addressBarangay = addressBarangay;
        this.addressZipCode = addressZipCode;
    }

    // Getters and Setters
    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getAddressHouseNumber() {
        return addressHouseNumber;
    }

    public void setAddressHouseNumber(String addressHouseNumber) {
        this.addressHouseNumber = addressHouseNumber;
    }

    public String getAddressStreetSubdivision() {
        return addressStreetSubdivision;
    }

    public void setAddressStreetSubdivision(String addressStreetSubdivision) {
        this.addressStreetSubdivision = addressStreetSubdivision;
    }

    public String getAddressRegion() {
        return addressRegion;
    }

    public void setAddressRegion(String addressRegion) {
        this.addressRegion = addressRegion;
    }

    public String getAddressProvince() {
        return addressProvince;
    }

    public void setAddressProvince(String addressProvince) {
        this.addressProvince = addressProvince;
    }

    public String getAddressMunicipality() {
        return addressMunicipality;
    }

    public void setAddressMunicipality(String addressMunicipality) {
        this.addressMunicipality = addressMunicipality;
    }

    public String getAddressBarangay() {
        return addressBarangay;
    }

    public void setAddressBarangay(String addressBarangay) {
        this.addressBarangay = addressBarangay;
    }

    public String getAddressZipCode() {
        return addressZipCode;
    }

    public void setAddressZipCode(String addressZipCode) {
        this.addressZipCode = addressZipCode;
    }
}
