package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.model.GuidanceCounselor;

public class GuidanceCouncilorDAO {

    // CREATE Method
    public static void createGuidanceCounselor(Connection conn, int id, String LAST_NAME, String FIRST_NAME,
            String middleInitial, String suffix, String gender,
            String specialization, String contactNum, String email, String position,
            byte[] profilePicture) {
        String sql = "INSERT INTO GUIDANCE_COUNSELORS (GUIDANCE_COUNSELORS_ID, LAST_NAME, FIRST_NAME, MIDDLE_INITIAL, SUFFIX, "
                + "GENDER, SPECIALIZATION, CONTACT_NUM, EMAIL, POSITION, PROFILE_PICTURE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, LAST_NAME);
            pstmt.setString(3, FIRST_NAME);
            pstmt.setString(4, middleInitial);
            pstmt.setString(5, suffix);
            pstmt.setString(6, gender);
            pstmt.setString(7, specialization);
            pstmt.setString(8, contactNum);
            pstmt.setString(9, email);
            pstmt.setString(10, position);
            pstmt.setBytes(11, profilePicture);

            pstmt.executeUpdate();
            System.out.println("Guidance counselor added successfully.");
        } catch (SQLException e) {
            handleSQLException(e, "createGuidanceCounselor");
        }
    }

    // READ Method
    public static void readGuidanceCounselor(Connection conn, int id) {
        String sql = "SELECT * FROM GUIDANCE_COUNSELORS WHERE GUIDANCE_COUNSELORS_ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("guidance_COUNSELORS_ID"));
                    System.out.println("Name: " + rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME"));
                    System.out.println("Specialization: " + rs.getString("specialization"));
                    System.out.println("Email: " + rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, "readGuidanceCounselor");
        }
    }

    // Batch Processing Example
    public static void createGuidanceCounselorsBatch(Connection connection, List<GuidanceCounselor> counselors) {
        String sql = "INSERT INTO GUIDANCE_COUNSELORS (guidance_COUNSELORS_ID, LAST_NAME, FIRST_NAME, middle_initial, suffix, gender, specialization, contact_num, email, position, profile_picture) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (GuidanceCounselor counselor : counselors) {
                pstmt.setInt(1, counselor.getId());
                pstmt.setString(2, counselor.getLAST_NAME());
                pstmt.setString(3, counselor.getFIRST_NAME());
                pstmt.setString(4, counselor.getMiddleInitial());
                pstmt.setString(5, counselor.getSuffix());
                pstmt.setString(6, counselor.getGender());
                pstmt.setString(7, counselor.getSpecialization());
                pstmt.setString(8, counselor.getContactNumber());
                pstmt.setString(9, counselor.getEmail());
                pstmt.setString(10, counselor.getPosition());
                pstmt.setBytes(11, counselor.getProfilePicture());
                pstmt.addBatch();
            }
            int[] result = pstmt.executeBatch();
            System.out.println("Batch insert completed: " + result.length + " rows added.");
        } catch (SQLException e) {
            handleSQLException(e, "createGuidanceCounselorsBatch");
        }
    }

    // Error Handling Method
    public static void handleSQLException(SQLException e, String operation) {
        System.err.println("Error during " + operation + ": " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }
}