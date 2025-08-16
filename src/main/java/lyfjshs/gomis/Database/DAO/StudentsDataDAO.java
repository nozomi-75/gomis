package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Participants;
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
            // Check if student with same LRN already exists
            String checkLrnSql = "SELECT COUNT(*) FROM STUDENT WHERE STUDENT_LRN = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkLrnSql)) {
                checkStmt.setString(1, student.getStudentLrn());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("A student with LRN " + student.getStudentLrn() + " already exists");
                    }
                }
            }

            // Check if a school form with the same section exists
            String checkSectionSql = "SELECT SF_ID FROM SCHOOL_FORM WHERE SF_SECTION = ? AND SF_SCHOOL_YEAR = ? AND SF_GRADE_LEVEL = ?";
            int existingSchoolFormId = 0;
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSectionSql)) {
                checkStmt.setString(1, schoolForm.getSF_SECTION());
                checkStmt.setString(2, schoolForm.getSF_SCHOOL_YEAR());
                checkStmt.setString(3, schoolForm.getSF_GRADE_LEVEL());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        existingSchoolFormId = rs.getInt("SF_ID");
                    }
                }
            }

            // If school form exists, use it; otherwise create new one
            int schoolFormId;
            if (existingSchoolFormId > 0) {
                schoolFormId = existingSchoolFormId;
            } else {
                SchoolFormDAO schoolFormDAO = new SchoolFormDAO(connection);
                schoolFormId = schoolFormDAO.createSchoolForm(schoolForm);
                if (schoolFormId == 0) {
                    throw new SQLException("Failed to create school form");
                }
            }

            // Set the school form ID in the student object
            student.setSF_ID(schoolFormId);

            // Create other related entities
            AddressDAO addressDAO = new AddressDAO(connection);
            ContactDAO contactDAO = new ContactDAO(connection);
            ParentsDAO parentsDAO = new ParentsDAO(connection);
            GuardianDAO guardianDAO = new GuardianDAO(connection);

            int addressId = addressDAO.createAddress(address);
            int contactId = contactDAO.createContact(contact);
            int parentId = parentsDAO.createParents(parents);
            int guardianId = guardianDAO.createGuardian(guardian);

            if (addressId == 0 || contactId == 0 || parentId == 0) {
                throw new SQLException("Failed to insert one or more related entities.");
            }

            student.setAddressId(addressId);
            student.setContactId(contactId);
            student.setParentId(parentId);
            student.setGuardianId(guardianId);

            // Now create the student record
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
        String sql = "INSERT INTO STUDENT (PARENT_ID, GUARDIAN_ID, ADDRESS_ID, " +
                "CONTACT_ID, SF_ID, STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, " +
                "STUDENT_MIDDLENAME, STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, " +
                "STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, student.getParentId());
            pstmt.setObject(2, student.getGuardianId() == 0 ? null : student.getGuardianId(), Types.INTEGER);
            pstmt.setInt(3, student.getAddressId());
            pstmt.setInt(4, student.getContactId());
            pstmt.setInt(5, student.getSF_ID());
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

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Get the generated ID and set it in the student object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentUid(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            handleSQLException(e, "createStudentData");
            return false;
        }
    }

    public List<Student> getStudentsByFilters(String lrn, String firstName, String lastName, String sex)
            throws SQLException {
        List<Student> students = new ArrayList<>();
        StringBuilder query = new StringBuilder(getBaseQuery() + " WHERE 1=1");

        // Dynamically add filters
        List<Object> params = new ArrayList<>();
        
        // LRN search - exact prefix match
        if (lrn != null && !lrn.trim().isEmpty()) {
            query.append(" AND s.STUDENT_LRN LIKE ?");
            params.add(lrn + "%");
        }
        
        // First name search - case insensitive partial match
        if (firstName != null && !firstName.trim().isEmpty()) {
            query.append(" AND LOWER(s.STUDENT_FIRSTNAME) LIKE LOWER(?)");
            params.add("%" + firstName.trim() + "%");
        }
        
        // Last name search - case insensitive partial match
        if (lastName != null && !lastName.trim().isEmpty()) {
            query.append(" AND LOWER(s.STUDENT_LASTNAME) LIKE LOWER(?)");
            params.add("%" + lastName.trim() + "%");
        }
        
        // Gender/Sex search - exact match (case sensitive)
        if (sex != null && !sex.trim().isEmpty()) {
            query.append(" AND s.STUDENT_SEX = ?");
            params.add(sex.trim());
        }

        // Add ORDER BY clause for consistent results
        query.append(" ORDER BY s.STUDENT_LASTNAME, s.STUDENT_FIRSTNAME");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            // Set all parameters
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
        String query = getBaseQuery() + " WHERE s.STUDENT_UID = ?";

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
        String query = getBaseQuery() + " WHERE s.STUDENT_LRN = ?";

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
        String sql = "SELECT s.*, a.*, c.*, p.*, g.*, sf.* " +
                "FROM STUDENT s " +
                "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
                "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
                "LEFT JOIN PARENTS p ON s.PARENT_ID = p.PARENT_ID " +
                "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
                "LEFT JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID " + // Changed join condition
                "ORDER BY s.STUDENT_LASTNAME";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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

                    SchoolForm schoolForm = new SchoolForm(
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

                    Student student = new Student(
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

                    students.add(student);
                }
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
        String errorMessage;
        if (e.getErrorCode() == 1062) { // Duplicate entry error
            if (e.getMessage().contains("STUDENT_LRN")) {
                errorMessage = "A student with this LRN already exists.";
            } else {
                errorMessage = "A duplicate entry was found in the database.";
            }
        } else {
            errorMessage = "Database error during " + operation + ": " + e.getMessage();
        }
        System.err.println(errorMessage);
        // You might want to log this error to a proper logging system
    }

    // ✅ Update Student Data
    public boolean updateStudentData(Student student) throws SQLException {
        String sql = "UPDATE STUDENT SET STUDENT_LRN = ?, STUDENT_LASTNAME = ?, STUDENT_FIRSTNAME = ?, " +
                "STUDENT_MIDDLENAME = ?, STUDENT_SEX = ?, STUDENT_BIRTHDATE = ?, STUDENT_MOTHERTONGUE = ?, " +
                "STUDENT_AGE = ?, STUDENT_IP_TYPE = ?, STUDENT_RELIGION = ?, SF_SECTION = ? " + // Use SF_SECTION
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
            stmt.setString(11, student.getSchoolSection()); // Set SF_SECTION value
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

    // Add method to update student's school form ID
    public boolean updateStudentSchoolFormId(int studentUid, Integer sfId) throws SQLException {
        String sql = "UPDATE STUDENT SET SF_ID = ? WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (sfId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, sfId);
            }
            stmt.setInt(2, studentUid);
            return stmt.executeUpdate() > 0;
        }
    }

    // Add method to update student references to null
    public boolean updateStudentReferences(int studentUid) throws SQLException {
        String sql = "UPDATE STUDENT SET PARENT_ID = NULL, GUARDIAN_ID = NULL, ADDRESS_ID = NULL, " +
                    "CONTACT_ID = NULL, SF_ID = NULL WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            return stmt.executeUpdate() > 0;
        }
    }

    // Add method to delete student related records
    public boolean deleteStudentRelatedRecords(int studentUid) throws SQLException {
        Student student = getStudentById(studentUid);
        if (student == null) {
            return false;
        }

        // Delete records in reverse order of dependencies
        try {
            // Get any participant records that reference this student
            ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
            List<Participants> participants = participantsDAO.getParticipantByStudentUid(studentUid);
            
            for (Participants participant : participants) {
                int participantId = participant.getParticipantId();
                
                // First delete from APPOINTMENT_PARTICIPANTS
                String deleteAppointmentParticipantsSQL = "DELETE FROM APPOINTMENT_PARTICIPANTS WHERE PARTICIPANT_ID = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteAppointmentParticipantsSQL)) {
                    stmt.setInt(1, participantId);
                    stmt.executeUpdate();
                }
                
                // Delete from SESSIONS_PARTICIPANTS
                String deleteSessionParticipantsSQL = "DELETE FROM SESSIONS_PARTICIPANTS WHERE PARTICIPANT_ID = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteSessionParticipantsSQL)) {
                    stmt.setInt(1, participantId);
                    stmt.executeUpdate();
                }
                
                // Delete from VIOLATION_RECORD
                String deleteViolationSQL = "DELETE FROM VIOLATION_RECORD WHERE PARTICIPANT_ID = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteViolationSQL)) {
                    stmt.setInt(1, participantId);
                    stmt.executeUpdate();
                }
                
                // Delete from INCIDENTS
                String deleteIncidentsSQL = "DELETE FROM INCIDENTS WHERE PARTICIPANT_ID = ?";
                try (PreparedStatement stmt = connection.prepareStatement(deleteIncidentsSQL)) {
                    stmt.setInt(1, participantId);
                    stmt.executeUpdate();
                }
                
                // Finally delete the participant record itself
                participantsDAO.deleteParticipant(participantId);
            }

            // Delete guardian if exists and not referenced by other students
            if (student.getGuardianId() != 0) {
                GuardianDAO guardianDAO = new GuardianDAO(connection);
                if (!isGuardianReferencedByOtherStudents(student.getGuardianId(), studentUid)) {
                    guardianDAO.deleteGuardian(student.getGuardianId());
                }
            }

            // Delete parents if exists and not referenced by other students
            if (student.getParentId() != 0) {
                ParentsDAO parentsDAO = new ParentsDAO(connection);
                if (!isParentReferencedByOtherStudents(student.getParentId(), studentUid)) {
                    parentsDAO.deleteParents(student.getParentId());
                }
            }

            // Delete contact if exists and not referenced by other students
            if (student.getContactId() != 0) {
                ContactDAO contactDAO = new ContactDAO(connection);
                if (!isContactReferencedByOtherStudents(student.getContactId(), studentUid)) {
                    contactDAO.deleteContact(student.getContactId());
                }
            }

            // Delete address if exists and not referenced by other students
            if (student.getAddressId() != 0) {
                AddressDAO addressDAO = new AddressDAO(connection);
                if (!isAddressReferencedByOtherStudents(student.getAddressId(), studentUid)) {
                    addressDAO.deleteAddress(student.getAddressId());
                }
            }

            return true;
        } catch (SQLException e) {
            throw new SQLException("Error deleting related records: " + e.getMessage());
        }
    }

    // Helper methods to check if records are referenced by other students
    private boolean isGuardianReferencedByOtherStudents(int guardianId, int excludeStudentUid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE GUARDIAN_ID = ? AND STUDENT_UID != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, guardianId);
            stmt.setInt(2, excludeStudentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean isParentReferencedByOtherStudents(int parentId, int excludeStudentUid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE PARENT_ID = ? AND STUDENT_UID != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, parentId);
            stmt.setInt(2, excludeStudentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean isContactReferencedByOtherStudents(int contactId, int excludeStudentUid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE CONTACT_ID = ? AND STUDENT_UID != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contactId);
            stmt.setInt(2, excludeStudentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private boolean isAddressReferencedByOtherStudents(int addressId, int excludeStudentUid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE ADDRESS_ID = ? AND STUDENT_UID != ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            stmt.setInt(2, excludeStudentUid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Method to delete student record
    public boolean deleteStudent(int studentUid) throws SQLException {
        String sql = "DELETE FROM STUDENT WHERE STUDENT_UID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentUid);
            return stmt.executeUpdate() > 0;
        }
    }

    // Updated dropStudentWithRelations method
    public boolean dropStudentWithRelations(int studentUid) throws SQLException {
        connection.setAutoCommit(false);
        try {
            // First update student to remove all foreign key references
            if (!updateStudentReferences(studentUid)) {
                throw new SQLException("Failed to update student references");
            }

            // Then delete related records
            if (!deleteStudentRelatedRecords(studentUid)) {
                throw new SQLException("Failed to delete related records");
            }

            // Finally delete the student record
            if (!deleteStudent(studentUid)) {
                throw new SQLException("Failed to delete student record");
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException("Error during student deletion: " + e.getMessage());
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private String getBaseQuery() {
        return "SELECT s.*, a.*, c.*, p.*, g.*, sf.* " +
               "FROM STUDENT s " +
               "LEFT JOIN ADDRESS a ON s.ADDRESS_ID = a.ADDRESS_ID " +
               "LEFT JOIN CONTACT c ON s.CONTACT_ID = c.CONTACT_ID " +
               "LEFT JOIN PARENTS p ON s.PARENT_ID = p.PARENT_ID " +
               "LEFT JOIN GUARDIAN g ON s.GUARDIAN_ID = g.GUARDIAN_ID " +
               "LEFT JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID";
    }

    // New methods for filtering functionality
    public int[] getMinMaxAge() throws SQLException {
        String sql = "SELECT MIN(STUDENT_AGE), MAX(STUDENT_AGE) FROM STUDENT";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int minAge = rs.getInt(1);
                int maxAge = rs.getInt(2);
                // Provide sensible defaults if DB values are 0
                return new int[]{minAge == 0 ? 12 : minAge, maxAge == 0 ? 22 : maxAge};
            }
        }
        return new int[]{12, 22}; // Default fallback
    }

    public List<String> getDistinctGradeLevels() throws SQLException {
        List<String> gradeLevels = new ArrayList<>();
        String sql = "SELECT DISTINCT SF_GRADE_LEVEL FROM SCHOOL_FORM " +
                    "WHERE SF_GRADE_LEVEL IS NOT NULL AND SF_GRADE_LEVEL != '' " +
                    "AND SF_SCHOOL_YEAR = (SELECT MAX(SF_SCHOOL_YEAR) FROM SCHOOL_FORM) " + // Get current school year
                    "ORDER BY CAST(SF_GRADE_LEVEL AS UNSIGNED) ASC"; // Sort numerically
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String gradeLevel = rs.getString("SF_GRADE_LEVEL");
                if (gradeLevel != null && !gradeLevel.trim().isEmpty()) {
                    gradeLevels.add("Grade " + gradeLevel); // Format as "Grade X"
                }
            }
        }
        return gradeLevels;
    }

    public List<String> getDistinctSections() throws SQLException {
        List<String> sections = new ArrayList<>();
        String sql = "SELECT DISTINCT SF_SECTION FROM SCHOOL_FORM " +
                    "WHERE SF_SECTION IS NOT NULL AND SF_SECTION != '' " +
                    "AND SF_SCHOOL_YEAR = (SELECT MAX(SF_SCHOOL_YEAR) FROM SCHOOL_FORM) " + // Get current school year
                    "ORDER BY SF_SECTION ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String section = rs.getString("SF_SECTION");
                if (section != null && !section.trim().isEmpty()) {
                    sections.add(section);
                }
            }
        }
        return sections;
    }

    public List<String> getDistinctTrackStrands() throws SQLException {
        List<String> trackStrands = new ArrayList<>();
        String sql = "SELECT DISTINCT SF_TRACK_AND_STRAND FROM SCHOOL_FORM " +
                    "WHERE SF_TRACK_AND_STRAND IS NOT NULL " +
                    "AND SF_TRACK_AND_STRAND != '' " +
                    "AND SF_TRACK_AND_STRAND != 'N/A' " +
                    "AND SF_SCHOOL_YEAR = (SELECT MAX(SF_SCHOOL_YEAR) FROM SCHOOL_FORM) " + // Get current school year
                    "ORDER BY SF_TRACK_AND_STRAND ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String trackStrand = rs.getString("SF_TRACK_AND_STRAND");
                if (trackStrand != null && !trackStrand.trim().isEmpty()) {
                    trackStrands.add(trackStrand);
                }
            }
        }
        return trackStrands;
    }

    public List<Student> getStudentsByFilterCriteria(String searchTerm, String firstName, String lastName, 
            String middleName, boolean middleInitialOnly, String gradeLevel, String section, 
            String trackStrand, boolean filterMale, boolean filterFemale, int minAge, int maxAge) throws SQLException {
        
        List<Student> students = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(getBaseQuery());
        StringBuilder whereClause = new StringBuilder();

        // Search Term (LRN or Name parts)
        if (searchTerm != null && !searchTerm.isEmpty()) {
            addCondition(whereClause,
                "(s.STUDENT_LRN LIKE ? OR " +
                "LOWER(s.STUDENT_FIRSTNAME) LIKE ? OR " +
                "LOWER(s.STUDENT_LASTNAME) LIKE ? OR " +
                "LOWER(s.STUDENT_MIDDLENAME) LIKE ? OR " +
                "LOWER(CONCAT(s.STUDENT_FIRSTNAME, ' ', s.STUDENT_LASTNAME)) LIKE ? OR " +
                "LOWER(CONCAT(s.STUDENT_LASTNAME, ', ', s.STUDENT_FIRSTNAME)) LIKE ?)"
            );
            String searchTermParam = "%" + searchTerm.toLowerCase() + "%";
            for(int i=0; i<6; i++) params.add(searchTermParam);
        }

        // Name Filters
        if (firstName != null && !firstName.isEmpty()) {
            addCondition(whereClause, "LOWER(s.STUDENT_FIRSTNAME) LIKE ?");
            params.add("%" + firstName.toLowerCase() + "%");
        }
        if (lastName != null && !lastName.isEmpty()) {
            addCondition(whereClause, "LOWER(s.STUDENT_LASTNAME) LIKE ?");
            params.add("%" + lastName.toLowerCase() + "%");
        }
        if (middleName != null && !middleName.isEmpty()) {
            if (middleInitialOnly) {
                addCondition(whereClause, "LOWER(s.STUDENT_MIDDLENAME) LIKE ?");
                params.add(middleName.toLowerCase().charAt(0) + "%");
            } else {
                addCondition(whereClause, "LOWER(s.STUDENT_MIDDLENAME) LIKE ?");
                params.add("%" + middleName.toLowerCase() + "%");
            }
        }

        // Academic Filters - Using SchoolForm fields
        if (gradeLevel != null && !gradeLevel.equals("All")) {
            // Remove "Grade " prefix if present
            String gradeLevelValue = gradeLevel.replace("Grade ", "").trim();
            addCondition(whereClause, "sf.SF_GRADE_LEVEL = ?");
            params.add(gradeLevelValue);
        }
        if (section != null && !section.equals("All")) {
            addCondition(whereClause, "sf.SF_SECTION = ?");
            params.add(section);
        }
        if (trackStrand != null && !trackStrand.equals("All")) {
            addCondition(whereClause, "sf.SF_TRACK_AND_STRAND = ?");
            params.add(trackStrand);
        }

        // Add current school year filter
        // addCondition(whereClause, "sf.SF_SCHOOL_YEAR = (SELECT MAX(SF_SCHOOL_YEAR) FROM SCHOOL_FORM)");

        // Sex Filter - Using M/F to match Student entity
        if (!filterMale && filterFemale) {
            addCondition(whereClause, "s.STUDENT_SEX = ?");
            params.add("F");
        } else if (filterMale && !filterFemale) {
            addCondition(whereClause, "s.STUDENT_SEX = ?");
            params.add("M");
        } else if (!filterMale && !filterFemale) {
            addCondition(whereClause, "1 = 0");
        }

        // Age Range Filter
        addCondition(whereClause, "s.STUDENT_AGE BETWEEN ? AND ?");
        params.add(minAge);
        params.add(maxAge);

        if (whereClause.length() > 0) {
            sqlBuilder.append(" WHERE ").append(whereClause);
        }

        sqlBuilder.append(" ORDER BY s.STUDENT_LASTNAME ASC, s.STUDENT_FIRSTNAME ASC");

        try (PreparedStatement stmt = connection.prepareStatement(sqlBuilder.toString())) {
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

    private void addCondition(StringBuilder whereClause, String condition) {
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(condition);
    }
}
