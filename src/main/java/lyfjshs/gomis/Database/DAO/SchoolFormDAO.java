package lyfjshs.gomis.Database.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import lyfjshs.gomis.Database.SQLExceptionPane;
import lyfjshs.gomis.Database.entity.SchoolForm;

public class SchoolFormDAO {
    private final Connection connection;

    // Constructor to initialize the DAO with a database connection
    public SchoolFormDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE: Inserts a new school form and returns the generated ID
    public int createSchoolForm(SchoolForm schoolForm) {
        String sql = "INSERT INTO SCHOOL_FORM (SF_SCHOOL_NAME, SF_SCHOOL_ID, SF_DISTRICT, SF_DIVISION, SF_REGION, " +
                     "SF_SEMESTER, SF_SCHOOL_YEAR, SF_GRADE_LEVEL, SF_SECTION, SF_TRACK_AND_STRAND, SF_COURSE) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, schoolForm.getSF_SCHOOL_NAME());
            pstmt.setString(2, schoolForm.getSF_SCHOOL_ID());
            pstmt.setString(3, schoolForm.getSF_DISTRICT());
            pstmt.setString(4, schoolForm.getSF_DIVISION());
            pstmt.setString(5, schoolForm.getSF_REGION());
            pstmt.setString(6, schoolForm.getSF_SEMESTER());
            pstmt.setString(7, schoolForm.getSF_SCHOOL_YEAR());
            pstmt.setString(8, schoolForm.getSF_GRADE_LEVEL());
            pstmt.setString(9, schoolForm.getSF_SECTION());
            pstmt.setString(10, schoolForm.getSF_TRACK_AND_STRAND());
            pstmt.setString(11, schoolForm.getSF_COURSE());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Creating School Form");
            return 0;
        }
    }

    // READ: Retrieves all school forms
    public List<SchoolForm> getAllSchoolForms() {
        List<SchoolForm> schoolForms = new ArrayList<>();
        String sql = "SELECT * FROM SCHOOL_FORM";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                schoolForms.add(mapResultSetToSchoolForm(rs));
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching All School Forms");
        }
        return schoolForms;
    }

    // READ: Retrieves a school form by ID
    public SchoolForm getSchoolFormById(int SF_ID) {
        String sql = "SELECT * FROM SCHOOL_FORM WHERE SF_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, SF_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSchoolForm(rs);
                }
            }
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Fetching School Form by ID");
        }
        return null;
    }

    // UPDATE: Updates an existing school form
    public boolean updateSchoolForm(SchoolForm schoolForm) {
        String sql = "UPDATE SCHOOL_FORM SET SF_SCHOOL_NAME = ?, SF_SCHOOL_ID = ?, SF_DISTRICT = ?, SF_DIVISION = ?, " +
                     "SF_REGION = ?, SF_SEMESTER = ?, SF_SCHOOL_YEAR = ?, SF_GRADE_LEVEL = ?, SF_SECTION = ?, " +
                     "SF_TRACK_AND_STRAND = ?, SF_COURSE = ? WHERE SF_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, schoolForm.getSF_SCHOOL_NAME());
            stmt.setString(2, schoolForm.getSF_SCHOOL_ID());
            stmt.setString(3, schoolForm.getSF_DISTRICT());
            stmt.setString(4, schoolForm.getSF_DIVISION());
            stmt.setString(5, schoolForm.getSF_REGION());
            stmt.setString(6, schoolForm.getSF_SEMESTER());
            stmt.setString(7, schoolForm.getSF_SCHOOL_YEAR());
            stmt.setString(8, schoolForm.getSF_GRADE_LEVEL());
            stmt.setString(9, schoolForm.getSF_SECTION());
            stmt.setString(10, schoolForm.getSF_TRACK_AND_STRAND());
            stmt.setString(11, schoolForm.getSF_COURSE());
            stmt.setInt(12, schoolForm.getSF_ID());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Updating School Form");
            return false;
        }
    }

    // DELETE: Deletes a school form by ID
    public boolean deleteSchoolForm(int SF_ID) {
        String sql = "DELETE FROM SCHOOL_FORM WHERE SF_ID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, SF_ID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            SQLExceptionPane.showSQLException(e, "Deleting School Form");
            return false;
        }
    }

    // Helper method to map a ResultSet row to a SchoolForm object
    private SchoolForm mapResultSetToSchoolForm(ResultSet rs) throws SQLException {
        return new SchoolForm(
            rs.getInt("SF_ID"),
            rs.getString("SF_SCHOOL_NAME"),
            rs.getString("SF_SCHOOL_ID"),
            rs.getString("SF_DISTRICT"),
            rs.getString("SF_DIVISION"),
            rs.getString("SF_REGION"),
            rs.getString("SF_SEMESTER"),
            rs.getString("SF_SCHOOL_YEAR"),
            rs.getString("SF_GRADE_LEVEL"),
            rs.getString("SF_SECTION"),
            rs.getString("SF_TRACK_AND_STRAND"),
            rs.getString("SF_COURSE")
        );
    }
}



