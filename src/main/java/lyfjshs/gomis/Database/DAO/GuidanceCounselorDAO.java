package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.entity.GuidanceCounselor;

public class GuidanceCounselorDAO {
    private final Connection connection;

    // Constructor to initialize the database connection
    public GuidanceCounselorDAO(Connection conn) {
        this.connection = conn;
    }

    // CREATE Method: Inserts a new guidance counselor into the database
    public boolean createGuidanceCounselor(GuidanceCounselor counselor) {
        String sql = "INSERT INTO GUIDANCE_COUNSELORS (GUIDANCE_COUNSELORS_ID, LAST_NAME, FIRST_NAME, MIDDLE_INITIAL, SUFFIX, " +
                     "GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, counselor.getGuidanceCounselorId());
            pstmt.setString(2, counselor.getLastName());
            pstmt.setString(3, counselor.getFirstName());
            pstmt.setString(4, counselor.getMiddleInitial());
            pstmt.setString(5, counselor.getSuffix());
            pstmt.setString(6, counselor.getGender());
            pstmt.setString(7, counselor.getSpecialization());
            pstmt.setString(8, counselor.getContactNum());
            pstmt.setString(9, counselor.getEmail());
            pstmt.setString(10, counselor.getPosition());
            pstmt.setBytes(11, counselor.getProfilePicture());  // Stores profile picture as byte array

            pstmt.executeUpdate();
            System.out.println("Guidance counselor added successfully.");
            return true;
        } catch (SQLException e) {
            handleSQLException(e, "createGuidanceCounselor");
        }
        return false;
    }

    // READ Method: Retrieves a guidance counselor's details by ID
    public GuidanceCounselor readGuidanceCounselor(int id) {
        String sql = "SELECT * FROM GUIDANCE_COUNSELORS WHERE GUIDANCE_COUNSELORS_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new GuidanceCounselor(
                        rs.getInt("GUIDANCE_COUNSELORS_ID"),
                        rs.getString("LAST_NAME"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("MIDDLE_INITIAL"),
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
            handleSQLException(e, "readGuidanceCounselor");
        }
        return null;    // Returns null if no matching counselor is found
    }

    // Batch Processing: Inserts multiple guidance counselors in one transaction
    public void createGuidanceCounselorsBatch(List<GuidanceCounselor> counselors) {
        String sql = "INSERT INTO GUIDANCE_COUNSELORS (GUIDANCE_COUNSELORS_ID, LAST_NAME, FIRST_NAME, MIDDLE_INITIAL, SUFFIX, " +
                     "GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (GuidanceCounselor counselor : counselors) {
                pstmt.setInt(1, counselor.getGuidanceCounselorId());
                pstmt.setString(2, counselor.getLastName());
                pstmt.setString(3, counselor.getFirstName());
                pstmt.setString(4, counselor.getMiddleInitial());
                pstmt.setString(5, counselor.getSuffix());
                pstmt.setString(6, counselor.getGender());
                pstmt.setString(7, counselor.getSpecialization());
                pstmt.setString(8, counselor.getContactNum());
                pstmt.setString(9, counselor.getEmail());
                pstmt.setString(10, counselor.getPosition());
                pstmt.setBytes(11, counselor.getProfilePicture());  // Stores image data
                
                pstmt.addBatch();    // Adds statement to batch processing
            }
            int[] result = pstmt.executeBatch();    // Executes batch insertion
            System.out.println("Batch insert completed: " + result.length + " rows added.");
        } catch (SQLException e) {
            handleSQLException(e, "createGuidanceCounselorsBatch");
        }
    }

    // DELETE Method: Removes a guidance counselor record based on ID
    public boolean deleteGuidanceCounselor( int id) {
        String sql = "DELETE FROM GUIDANCE_COUNSELORS WHERE GUIDANCE_COUNSELORS_ID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();   // Executes deletion query
            if (affectedRows > 0) {
                System.out.println("Guidance counselor deleted successfully.");
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(e, "deleteGuidanceCounselor");
        }
        return false;   // Returns false if deletion fails
    }

    // Error Handling Method: Logs SQL errors with additional details
    private void handleSQLException(SQLException e, String operation) {
        System.err.println("Error during " + operation + ": " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }
}
