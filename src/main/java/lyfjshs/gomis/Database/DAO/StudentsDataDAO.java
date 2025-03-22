package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;

public class StudentsDataDAO {
    private final Connection connection;

    public StudentsDataDAO(Connection connection) {
        this.connection = connection;
    }

    // ✅ Create Student with all related entities
    public boolean createStudentWithRelations(Student student, Address address, Contact contact,
            Parents parents, Guardian guardian, SchoolForm schoolForm) throws SQLException {
        connection.setAutoCommit(false);
        try {
            AddressDAO addressDAO = new AddressDAO(connection);
            ContactDAO contactDAO = new ContactDAO(connection);
            ParentsDAO parentsDAO = new ParentsDAO(connection);
            GuardianDAO guardianDAO = new GuardianDAO(connection);
            SchoolFormDAO schoolFormDAO = new SchoolFormDAO(connection);

            // 🔹 Insert related entities
            int addressId = addressDAO.createAddress(address);
            int contactId = contactDAO.createContact(contact);
            int parentId = parentsDAO.createParents(parents);
            int guardianId = guardianDAO.createGuardian(guardian);
            int schoolFormId = schoolFormDAO.createSchoolForm(schoolForm);

            if (addressId == 0 || contactId == 0 || parentId == 0 || schoolFormId == 0) {
                throw new SQLException("Failed to insert one or more related entities.");
            }

            student.setAddressId(addressId);
            student.setContactId(contactId);
            student.setParentId(parentId);
            student.setGuardianId(guardianId);
            student.setSchoolSection(schoolForm.getSF_SECTION()); // Use school section instead of schoolFormId

            boolean success = createStudentData(student);
            if (success) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException e) {
            connection.rollback();
            handleSQLException(e, "createStudentWithRelations");
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // ✅ Insert Student Data
    public boolean createStudentData(Student student) {
        String sql = "INSERT INTO STUDENT (STUDENT_UID, Parent_ID, GUARDIAN_ID, ADDRESS_ID, CONTACT_ID, SF_SECTION, " +
                "STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, " +
                "STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, student.getStudentUid());
            pstmt.setInt(2, student.getParentId());
            pstmt.setObject(3, student.getGuardianId() == 0 ? null : student.getGuardianId(), Types.INTEGER);
            pstmt.setInt(4, student.getAddressId());
            pstmt.setInt(5, student.getContactId());
            pstmt.setString(6, student.getSchoolSection()); // 🔹 Reference school section
            pstmt.setString(7, student.getStudentLrn());
            pstmt.setString(8, student.getStudentLastname());
            pstmt.setString(9, student.getStudentFirstname());
            pstmt.setString(10, student.getStudentMiddlename());
            pstmt.setString(11, student.getStudentSex());
            pstmt.setDate(12, student.getStudentBirthdate());
            pstmt.setString(13, student.getStudentMothertongue());
            pstmt.setInt(14, student.getStudentAge());
            pstmt.setString(15, student.getStudentIpType());
            pstmt.setString(16, student.getStudentReligion());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e, "createStudentData");
            return false;
        }
    }

    public List<Student> getStudentsByFilters(String lrn, String firstName, String lastName, String sex)
            throws SQLException {
        List<Student> students = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT s.*, a.*, c.*, p.*, g.*, sf.* FROM STUDENT s " +
                "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID " +
                "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                "LEFT JOIN SCHOOL_FORM sf ON s.SF_SECTION = sf.SF_SECTION WHERE 1=1");

        // Dynamically add filters
        List<Object> params = new ArrayList<>();
        if (lrn != null && !lrn.trim().isEmpty()) {
            query.append(" AND s.STUDENT_LRN LIKE ?");
            params.add(lrn + "%"); // Searching by LRN prefix
        }
        if (firstName != null && !firstName.trim().isEmpty()) {
            query.append(" AND s.STUDENT_FIRSTNAME LIKE ?");
            params.add("%" + firstName + "%");
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            query.append(" AND s.STUDENT_LASTNAME LIKE ?");
            params.add("%" + lastName + "%");
        }
        if (sex != null && !sex.trim().isEmpty()) {
            query.append(" AND s.STUDENT_SEX = ?");
            params.add(sex);
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudentWithRelations(rs));
                }
            }
        }
        return students;
    }

    // ✅ Get Student by UID
    public Student getStudentById(int studentUid) throws SQLException {
        String query = "SELECT s.*, a.*, c.*, p.*, g.*, sf.* " +
                "FROM STUDENT s " +
                "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID " +
                "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                "LEFT JOIN SCHOOL_FORM sf ON s.SF_SECTION = sf.SF_SECTION " +
                "WHERE s.STUDENT_UID = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudentWithRelations(rs);
                }
            }
        }
        return null;
    }

    public Student getStudentDataByLrn(String lrn) throws SQLException {
        String query = "SELECT s.*, a.*, c.*, p.*, g.*, sf.* " +
                "FROM STUDENT s " +
                "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID " +
                "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                "LEFT JOIN SCHOOL_FORM sf ON s.SF_SECTION = sf.SF_SECTION " +
                "WHERE s.STUDENT_LRN = ?";

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

    // ✅ Get All Students
    public List<Student> getAllStudentsData() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, a.*, c.*, p.*, g.*, sf.* FROM STUDENT s " +
                "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                "LEFT JOIN PARENTS p ON s.Parent_ID = p.PARENT_ID " +
                "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                "LEFT JOIN SCHOOL_FORM sf ON s.SF_SECTION = sf.SF_SECTION";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                students.add(mapResultSetToStudentWithRelations(rs));
            }
        }
        return students;
    }

    private Student mapResultSetToStudentWithRelations(ResultSet rs) throws SQLException {
        Address address = new Address(rs.getInt("ADDRESS_ID"), rs.getString("ADDRESS_HOUSE_NUMBER"),
                rs.getString("ADDRESS_STREET_SUBDIVISION"), rs.getString("ADDRESS_REGION"),
                rs.getString("ADDRESS_PROVINCE"), rs.getString("ADDRESS_MUNICIPALITY"),
                rs.getString("ADDRESS_BARANGAY"), rs.getString("ADDRESS_ZIP_CODE"));

        Contact contact = new Contact(rs.getInt("CONTACT_ID"), rs.getString("CONTACT_NUMBER"));

        Parents parents = new Parents(rs.getInt("PARENT_ID"), rs.getString("FATHER_LASTNAME"),
                rs.getString("FATHER_FIRSTNAME"), rs.getString("FATHER_MIDDLENAME"),
                rs.getString("FATHER_CONTACT_NUMBER"), rs.getString("MOTHER_LASTNAME"),
                rs.getString("MOTHER_FIRSTNAME"), rs.getString("MOTHER_MIDDLE_NAME"),
                rs.getString("MOTHER_CONTACT_NUMBER"));

        Guardian guardian = new Guardian(rs.getInt("GUARDIAN_ID"), rs.getString("GUARDIAN_LASTNAME"),
                rs.getString("GUARDIAN_FIRST_NAME"), rs.getString("GUARDIAN_MIDDLE_NAME"),
                rs.getString("GUARDIAN_RELATIONSHIP"), rs.getString("GUARDIAN_CONTACT_NUMBER"));

        SchoolForm schoolForm = new SchoolForm(rs.getInt("SF_ID"), rs.getString("SF_SCHOOL_NAME"),
                rs.getString("SF_SCHOOL_ID"), rs.getString("SF_DISTRICT"), rs.getString("SF_DIVISION"),
                rs.getString("SF_REGION"), rs.getString("SF_SEMESTER"), rs.getString("SF_SCHOOL_YEAR"),
                rs.getString("SF_GRADE_LEVEL"), rs.getString("SF_SECTION"),
                rs.getString("SF_TRACK_AND_STRAND"), rs.getString("SF_COURSE"));

        return new Student(rs.getInt("STUDENT_UID"), rs.getInt("Parent_ID"), rs.getInt("Guardian_ID"),
                rs.getInt("ADDRESS_ID"), rs.getInt("CONTACT_ID"), rs.getString("SF_SECTION"),
                rs.getString("STUDENT_LRN"), rs.getString("STUDENT_LASTNAME"), rs.getString("STUDENT_FIRSTNAME"),
                rs.getString("STUDENT_MIDDLENAME"), rs.getString("STUDENT_SEX"), rs.getDate("STUDENT_BIRTHDATE"),
                rs.getString("STUDENT_MOTHERTONGUE"), rs.getInt("STUDENT_AGE"), rs.getString("STUDENT_IP_TYPE"),
                rs.getString("STUDENT_RELIGION"), address, contact, parents, guardian, schoolForm);
    }

    private void handleSQLException(SQLException e, String operation) {
        System.err.println("Error during " + operation + ": " + e.getMessage());
    }

    // ✅ Update Student Data
    public boolean updateStudentData(Student student) throws SQLException {
        String sql = "UPDATE STUDENT SET STUDENT_LRN = ?, STUDENT_LASTNAME = ?, STUDENT_FIRSTNAME = ?, " +
                "STUDENT_MIDDLENAME = ?, STUDENT_SEX = ?, STUDENT_BIRTHDATE = ?, STUDENT_MOTHERTONGUE = ?, " +
                "STUDENT_AGE = ?, STUDENT_IP_TYPE = ?, STUDENT_RELIGION = ?, SF_SECTION = ? " +
                "WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, student.getStudentLrn());
            stmt.setString(2, student.getStudentLastname());
            stmt.setString(3, student.getStudentFirstname());
            stmt.setString(4, student.getStudentMiddlename());
            stmt.setString(5, student.getStudentSex());
            stmt.setDate(6, student.getStudentBirthdate());
            stmt.setString(7, student.getStudentMothertongue());
            stmt.setInt(8, student.getStudentAge());
            stmt.setString(9, student.getStudentIpType());
            stmt.setString(10, student.getStudentReligion());
            stmt.setString(11, student.getSchoolSection()); // 🔹 Ensure section update
            stmt.setInt(12, student.getStudentUid()); // 🔹 Where clause for update

            return stmt.executeUpdate() > 0;
        }
    }

    // ✅ Delete Student Data
    public boolean deleteStudentData(int studentUid) throws SQLException {
        String sql = "DELETE FROM STUDENT WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            return stmt.executeUpdate() > 0;
        }
    }

}
