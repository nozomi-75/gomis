package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Student;

public class StudentsDataDAO {
    private final Connection connection;

    public StudentsDataDAO(Connection connection) {
        this.connection = connection;
    }

    // CREATE Student Data
    public boolean createStudentData(Student student) {
        String sql = "INSERT INTO STUDENT (STUDENT_UID, Parent_ID, GUARDIAN_ID, ADDRESS_ID, CONTACT_ID, "
                + "STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, "
                + "STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, student.getStudentUid());
            pstmt.setInt(2, student.getParentId());
            pstmt.setInt(3, student.getGuardianId() == 0 ? java.sql.Types.INTEGER : student.getGuardianId());
            pstmt.setInt(4, student.getAddressId());
            pstmt.setInt(5, student.getContactId());
            pstmt.setString(6, student.getStudentLrn());
            pstmt.setString(7, student.getStudentLastname());
            pstmt.setString(8, student.getStudentFirstname());
            pstmt.setString(9, student.getStudentMiddlename());
            pstmt.setString(10, student.getStudentSex());
            pstmt.setDate(11, student.getStudentBirthdate());
            pstmt.setString(12, student.getStudentMothertongue());
            pstmt.setInt(13, student.getStudentAge());
            pstmt.setString(14, student.getStudentIpType());
            pstmt.setString(15, student.getStudentReligion());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e, "createStudentData");
            return false;
        }
    }

    // READ Student Data by LRN
    public Student getStudentDataByLrn(String lrn) throws SQLException {
        String query = "SELECT s.*, a.*, c.*, p.*, g.* FROM STUDENT s "
                + "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID "
                + "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID "
                + "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID "
                + "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID "
                + "WHERE s.STUDENT_LRN = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, lrn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentWithRelations(rs);
                }
            }
        }
        return null;
    }

    // READ Student by ID
    public Student getStudentById(int studentUid) throws SQLException {
        String sql = "SELECT s.*, a.*, c.*, p.*, g.* FROM STUDENT s "
                + "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID "
                + "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID "
                + "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID "
                + "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID "
                + "WHERE s.STUDENT_UID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentWithRelations(rs);
                }
            }
        }
        return null;
    }

    // READ All Students Data with Relations
    public List<Student> getAllStudentsData() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, a.*, c.*, p.*, g.* FROM STUDENT s "
                + "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID "
                + "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID "
                + "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID "
                + "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                students.add(mapResultSetToStudentWithRelations(rs));
            }
        }
        return students;
    }

    // New Method: Retrieve All Related Data for a Student
    public Student getStudentWithAllRelatedData(int studentUid) throws SQLException {
        String sql = "SELECT s.*, a.*, c.*, p.*, g.* FROM STUDENT s "
                + "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID "
                + "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID "
                + "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID "
                + "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID "
                + "WHERE s.STUDENT_UID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentWithAllRelations(rs);
                }
            }
        }
        return null;
    }

    // Helper method to map ResultSet to StudentsData with Address and Contact
    private Student mapResultSetToStudentWithRelations(ResultSet rs) throws SQLException {
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
            rs.getString("MOTHER_LASTNAME"),
            rs.getString("MOTHER_FIRSTNAME"),
            rs.getString("MOTHER_MIDDLE_NAME")
        );
        Guardian guardian = new Guardian(
            rs.getInt("GUARDIAN_ID"),
            rs.getString("GUARDIAN_FIRSTNAME"),
            rs.getString("GUARDIAN_MIDDLENAME"),
            rs.getString("GUARDIAN_FIRSTNAME"),
            rs.getString("GUARDIAN_RELATIONSHIP")
        );

        return new Student(
            rs.getInt("STUDENT_UID"),
            rs.getInt("Parent_ID"),
            rs.getInt("Guardian_ID"),
            rs.getInt("ADDRESS_ID"),
            rs.getInt("CONTACT_ID"),
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
            guardian
        );
    }

    // Helper method to map ResultSet to StudentsData with all related data
    private Student mapResultSetToStudentWithAllRelations(ResultSet rs) throws SQLException {
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
        Parents PARENTS = new Parents(
            rs.getInt("PARENT_ID"),
            rs.getString("FATHER_LASTNAME"),
            rs.getString("FATHER_FIRSTNAME"),
            rs.getString("FATHER_MIDDLENAME"),
            rs.getString("MOTHER_LASTNAME"),
            rs.getString("MOTHER_FIRSTNAME"),
            rs.getString("MOTHER_MIDDLE_NAME")
        );
        Guardian guardian = new Guardian(
            rs.getInt("GUARDIAN_ID"),
            rs.getString("GUARDIAN_FIRSTNAME"),
            rs.getString("GUARDIAN_MIDDLENAME"),
            rs.getString("GUARDIAN_FIRSTNAME"),
            rs.getString("GUARDIAN_RELATIONSHIP")
        );

        return new Student(
            rs.getInt("STUDENT_UID"),
            rs.getInt("Parent_ID"),
            rs.getInt("Guardian_ID"),
            rs.getInt("ADDRESS_ID"),
            rs.getInt("CONTACT_ID"),
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
            PARENTS,
            guardian
        );
    }

    // UPDATE Student Data
    public boolean updateStudentData(Student student) {
        String sql = "UPDATE STUDENT SET Parent_ID = ?, Guardian_ID = ?, ADDRESS_ID = ?, CONTACT_ID = ?, "
                + "STUDENT_LRN = ?, STUDENT_LASTNAME = ?, STUDENT_FIRSTNAME = ?, STUDENT_MIDDLENAME = ?, STUDENT_SEX = ?, "
                + "STUDENT_BIRTHDATE = ?, STUDENT_MOTHERTONGUE = ?, STUDENT_AGE = ?, STUDENT_IP_TYPE = ?, STUDENT_RELIGION = ? "
                + "WHERE STUDENT_UID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, student.getParentId());
            pstmt.setInt(2, student.getGuardianId() == 0 ? java.sql.Types.INTEGER : student.getGuardianId());
            pstmt.setInt(3, student.getAddressId());
            pstmt.setInt(4, student.getContactId());
            pstmt.setString(5, student.getStudentLrn());
            pstmt.setString(6, student.getStudentLastname());
            pstmt.setString(7, student.getStudentFirstname());
            pstmt.setString(8, student.getStudentMiddlename());
            pstmt.setString(9, student.getStudentSex());
            pstmt.setDate(10, student.getStudentBirthdate());
            pstmt.setString(11, student.getStudentMothertongue());
            pstmt.setInt(12, student.getStudentAge());
            pstmt.setString(13, student.getStudentIpType());
            pstmt.setString(14, student.getStudentReligion());
            pstmt.setInt(15, student.getStudentUid());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e, "updateStudentData");
            return false;
        }
    }

    // DELETE Student Data
    public boolean deleteStudentData(int studentUid) {
        String sql = "DELETE FROM STUDENT WHERE STUDENT_UID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentUid);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e, "deleteStudentData");
            return false;
        }
    }

    // Handle SQL Exceptions
    private void handleSQLException(SQLException e, String operation) {
        System.err.println("Error during " + operation + ": " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }
}