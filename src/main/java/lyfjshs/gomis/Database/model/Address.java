package lyfjshs.gomis.Database.model;

public class Address {
    private int addressId;
    private String houseNumber;
    private String streetSubdivision;
    private String region;
    private String province;
    private String municipality;
    private String barangay;
    private String zipCode;



    public Address(int addressId, String houseNumber, String streetSubdivision, String region, String province,
                   String municipality, String barangay, String zipCode) {
        this.addressId = addressId;
        this.houseNumber = houseNumber;
        this.streetSubdivision = streetSubdivision;
        this.region = region;
        this.province = province;
        this.municipality = municipality;
        this.barangay = barangay;
        this.zipCode = zipCode;
    }

    // Getters and Setters
    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }
    public String getHouseNumber() { return houseNumber; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
    public String getStreetSubdivision() { return streetSubdivision; }
    public void setStreetSubdivision(String streetSubdivision) { this.streetSubdivision = streetSubdivision; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    @Override
    public String toString() {
        return "Address{" +
                "addressId=" + addressId +
                ", houseNumber='" + houseNumber + '\'' +
                ", streetSubdivision='" + streetSubdivision + '\'' +
                ", region='" + region + '\'' +
                ", province='" + province + '\'' +
                ", municipality='" + municipality + '\'' +
                ", barangay='" + barangay + '\'' +
                ", zipCode='" + zipCode + '\'' +
                '}';
    }
}