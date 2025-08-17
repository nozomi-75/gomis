/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.SQLExceptionPane;
import lyfjshs.gomis.Database.entity.Address;

public class AddressDAO {

    private final Connection connection;

    // Constructor to initialize the AddressDAO with a database connection
    public AddressDAO(Connection connection) {
        this.connection = connection;
    }

     // CREATE: Inserts a new address into the database and returns the generated ID
    public int createAddress(Address address) {
    String sql = "INSERT INTO ADDRESS (ADDRESS_HOUSE_NUMBER, ADDRESS_STREET_SUBDIVISION, ADDRESS_REGION, " +
            "ADDRESS_PROVINCE, ADDRESS_MUNICIPALITY, ADDRESS_BARANGAY, ADDRESS_ZIP_CODE) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        // Setting values from the Address object
        pstmt.setString(1, address.getAddressHouseNumber());
        pstmt.setString(2, address.getAddressStreetSubdivision());
        pstmt.setString(3, address.getAddressRegion());
        pstmt.setString(4, address.getAddressProvince());
        pstmt.setString(5, address.getAddressMunicipality());
        pstmt.setString(6, address.getAddressBarangay());
        pstmt.setString(7, address.getAddressZipCode());
        pstmt.executeUpdate();

        // Retrieving the generated ID
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    } catch (SQLException e) {
        SQLExceptionPane.showSQLException(e, "Creating Address");
        return 0;
    }
}

    
    // CREATE: Alternative method to add an address using individual parameters
    public boolean addAddress(
        String houseNumber,
        String streetSubdivision,
        String region,
        String province,
        String municipality,
        String barangay,
        String zipCode) {
    String sql = "INSERT INTO ADDRESS (ADDRESS_HOUSE_NUMBER, ADDRESS_STREET_SUBDIVISION, ADDRESS_REGION, " +
                 "ADDRESS_PROVINCE, ADDRESS_MUNICIPALITY, ADDRESS_BARANGAY, ADDRESS_ZIP_CODE) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, houseNumber);
        stmt.setString(2, streetSubdivision);
        stmt.setString(3, region);
        stmt.setString(4, province);
        stmt.setString(5, municipality);
        stmt.setString(6, barangay);
        stmt.setString(7, zipCode);

        int affectedRows = stmt.executeUpdate();
        if (affectedRows > 0) {
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Generated Address ID can be retrieved if needed
                }
            }
            return true;
        }
    } catch (SQLException e) {
        SQLExceptionPane.showSQLException(e, "Adding Address");
    }
    return false;
}


    // READ: Retrieves all addresses from the database
    public List<Address> getAllAddresses() {
    List<Address> addresses = new ArrayList<>();
    String sql = "SELECT * FROM ADDRESS";

    try (PreparedStatement stmt = connection.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
            addresses.add(mapResultSetToAddress(rs));
        }
    } catch (SQLException e) {
        SQLExceptionPane.showSQLException(e, "Fetching All Addresses");
    }
    return addresses;
}


    // READ: Retrieves a single address by ID
    public Address getAddressById(int addressId) {
    String sql = "SELECT * FROM ADDRESS WHERE ADDRESS_ID = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, addressId);
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return mapResultSetToAddress(rs);
            }
        }
    } catch (SQLException e) {
        SQLExceptionPane.showSQLException(e, "Fetching Address by ID");
    }
    return null;
}


    // UPDATE: Updates an existing address by ID
    public boolean updateAddress(
            int addressId,
            String houseNumber,
            String streetSubdivision,
            String region,
            String province,
            String municipality,
            String barangay,
            String zipCode) {
        String sql = "UPDATE ADDRESS SET ADDRESS_HOUSE_NUMBER = ?, ADDRESS_STREET_SUBDIVISION = ?, "
                + "ADDRESS_REGION = ?, ADDRESS_PROVINCE = ?, ADDRESS_MUNICIPALITY = ?, "
                + "ADDRESS_BARANGAY = ?, ADDRESS_ZIP_CODE = ? "
                + "WHERE ADDRESS_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, houseNumber);
            stmt.setString(2, streetSubdivision);
            stmt.setString(3, region);
            stmt.setString(4, province);
            stmt.setString(5, municipality);
            stmt.setString(6, barangay);
            stmt.setString(7, zipCode);
            stmt.setInt(8, addressId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Updating Address");
            return false;
        }
    }

    // DELETE: Deletes an address from the database by ID
    public boolean deleteAddress(int addressId) {
        String sql = "DELETE FROM ADDRESS WHERE ADDRESS_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Deleting Address");
            return false;
        }
    }

    // Helper method to map a ResultSet row to an Address object
    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address(
            rs.getInt("ADDRESS_ID"),
            rs.getString("ADDRESS_HOUSE_NUMBER"),
            rs.getString("ADDRESS_STREET_SUBDIVISION"),
            rs.getString("ADDRESS_REGION"),
            rs.getString("ADDRESS_PROVINCE"),
            rs.getString("ADDRESS_MUNICIPALITY"),
            rs.getString("ADDRESS_BARANGAY"),
            rs.getString("ADDRESS_ZIP_CODE")
        );
        return address;
    }
}