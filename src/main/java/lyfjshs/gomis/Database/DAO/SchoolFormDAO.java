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
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;

public class SchoolFormDAO {
    private final Connection connection;

    // Constructor to initialize the DAO with a database connection
    public SchoolFormDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Student> getStudentsBySection(String section) throws SQLException {
        String sql = "SELECT s.*, sf.*, a.*, c.*, p.*, g.* FROM STUDENT s " +
                    "JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID " +
                    "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                    "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                    "LEFT JOIN PARENTS p ON s.PARENT_ID = p.PARENT_ID " +
                    "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                    "WHERE sf.SF_SECTION = ?";
        
        List<Student> students = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, section);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        }
        return students;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
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

        Contact contact = new Contact(
            rs.getInt("CONTACT_ID"),
            rs.getString("CONTACT_NUMBER")
        );

        Parents parents = new Parents(
            rs.getInt("PARENT_ID"),
            rs.getString("FATHER_LASTNAME"),
            rs.getString("FATHER_FIRSTNAME"),
            rs.getString("FATHER_MIDDLENAME"),
            rs.getString("FATHER_CONTACT_NUMBER"),
            rs.getString("MOTHER_LASTNAME"),
            rs.getString("MOTHER_FIRSTNAME"),
            rs.getString("MOTHER_MIDDLE_NAME"),
            rs.getString("MOTHER_CONTACT_NUMBER")
        );

        Guardian guardian = new Guardian(
            rs.getInt("GUARDIAN_ID"),
            rs.getString("GUARDIAN_LASTNAME"),
            rs.getString("GUARDIAN_FIRST_NAME"),
            rs.getString("GUARDIAN_MIDDLE_NAME"),
            rs.getString("GUARDIAN_RELATIONSHIP"),
            rs.getString("GUARDIAN_CONTACT_NUMBER")
        );

        SchoolForm schoolForm = mapResultSetToSchoolForm(rs);

        return new Student(
            rs.getInt("STUDENT_UID"),
            rs.getInt("PARENT_ID"),
            rs.getInt("GUARDIAN_ID"),
            rs.getInt("ADDRESS_ID"),
            rs.getInt("CONTACT_ID"),
            rs.getString("SF_SECTION"),
            rs.getString("STUDENT_LRN"),
            rs.getString("STUDENT_LASTNAME"),
            rs.getString("STUDENT_FIRSTNAME"),
            rs.getString("STUDENT_MIDDLENAME"),
            rs.getString("STUDENT_SEX"),
            rs.getDate("STUDENT_BIRTHDATE"),
            rs.getString("STUDENT_MOTHERTONGUE"),
            rs.getInt("STUDENT_AGE"),
            rs.getString("STUDENT_IP_TYPE"),
            rs.getString("STUDENT_RELIGION"),
            address,
            contact,
            parents,
            guardian,
            schoolForm
        );
    }

    // CREATE: Inserts a new school form and returns the generated ID
    public int createSchoolForm(SchoolForm schoolForm) throws SQLException {
        String sql = "INSERT INTO SCHOOL_FORM (SF_SCHOOL_NAME, SF_SCHOOL_ID, SF_DISTRICT, " +
                    "SF_DIVISION, SF_REGION, SF_SEMESTER, SF_SCHOOL_YEAR, SF_GRADE_LEVEL, " +
                    "SF_SECTION, SF_TRACK_AND_STRAND, SF_COURSE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating school form failed, no rows affected.");
            }
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating school form failed, no ID obtained.");
                }
            }
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



