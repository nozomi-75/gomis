package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.entity.GuidanceCounselor;

public class GuidanceCounselorDAO {
    private Connection connection;

    // Constructor to initialize the database connection
    public GuidanceCounselorDAO(Connection conn) {
        this.connection = conn;
    }

    // Modified CREATE Method to return the generated ID
    public int createGuidanceCounselor(GuidanceCounselor counselor) {
        String sql = "INSERT INTO GUIDANCE_COUNSELORS (LAST_NAME, FIRST_NAME, MIDDLE_NAME, SUFFIX, " +
                     "GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, counselor.getLastName());
            pstmt.setString(2, counselor.getFirstName());
            pstmt.setString(3, counselor.getMiddleName());
            pstmt.setString(4, counselor.getSuffix());
            pstmt.setString(5, counselor.getGender());
            pstmt.setString(6, counselor.getSpecialization());
            pstmt.setString(7, counselor.getContactNum());
            pstmt.setString(8, counselor.getEmail());
            pstmt.setString(9, counselor.getPosition());
            pstmt.setBytes(10, counselor.getProfilePicture());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        counselor.setGuidanceCounselorId(generatedId); // Update the counselor object
                        System.out.println("Guidance counselor added successfully with ID: " + generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "createGuidanceCounselor");
        }
        return -1;
    }

    // READ Method: Retrieves a guidance counselor's details by ID
    public GuidanceCounselor readGuidanceCounselor(int guidanceCounselorId) {
        String sql = "SELECT * FROM GUIDANCE_COUNSELORS WHERE guidance_counselor_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, guidanceCounselorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GuidanceCounselor(
                        rs.getInt("guidance_counselor_id"),
                        rs.getString("LAST_NAME"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("MIDDLE_NAME"),
                        rs.getString("SUFFIX"),
                        rs.getString("GENDER"),
                        rs.getString("SPECIALIZATION"),
                        rs.getString("CONTACT_NUM"),
                        rs.getString("EMAIL"),
                        rs.getString("POSITION"),
                        rs.getBytes("PROFILE_PICTURE")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Batch Processing: Inserts multiple guidance counselors in one transaction
    public void createGuidanceCounselorsBatch(List<GuidanceCounselor> counselors) {
        String sql = "INSERT INTO GUIDANCE_COUNSELORS (GUIDANCE_COUNSELOR_ID, LAST_NAME, FIRST_NAME, MIDDLE_NAME, SUFFIX, " +
                     "GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (GuidanceCounselor counselor : counselors) {
                pstmt.setInt(1, counselor.getGuidanceCounselorId());
                pstmt.setString(2, counselor.getLastName());
                pstmt.setString(3, counselor.getFirstName());
                pstmt.setString(4, counselor.getMiddleName()); // Updated method call
                pstmt.setString(5, counselor.getSuffix());
                pstmt.setString(6, counselor.getGender());
                pstmt.setString(7, counselor.getSpecialization());
                pstmt.setString(8, counselor.getContactNum());
                pstmt.setString(9, counselor.getEmail());
                pstmt.setString(10, counselor.getPosition());
                pstmt.setBytes(11, counselor.getProfilePicture());

                pstmt.addBatch();
            }
            int[] result = pstmt.executeBatch();
            pstmt.clearBatch();  // Ensure batch is cleared
            System.out.println("Batch insert completed: " + result.length + " rows added.");
        } catch (SQLException e) {
            handleSQLException(e, "createGuidanceCounselorsBatch");
        }
    }

    // DELETE Method: Removes a guidance counselor record based on ID
    public boolean deleteGuidanceCounselor(int id) {
        String sql = "DELETE FROM GUIDANCE_COUNSELORS WHERE GUIDANCE_COUNSELOR_ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Guidance counselor deleted successfully.");
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(e, "deleteGuidanceCounselor");
        }
        return false;
        }

    // Error Handling Method
    private void handleSQLException(SQLException e, String operation) {
        System.err.println("Error during " + operation + ": " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }
}