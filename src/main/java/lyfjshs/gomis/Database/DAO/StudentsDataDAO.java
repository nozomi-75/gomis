package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.StudentsData;
import lyfjshs.gomis.Database.model.StudentsRecord;

public class StudentsDataDAO {
	private Connection connection;

	public StudentsDataDAO(Connection connection) {
		this.connection = connection;
	}
	// CREATE Student Data
	public void createStudentData(Connection conn, StudentsData student) {
		String sql = "INSERT INTO STUDENTS_DATA (student_uid, LRN, LAST_NAME, FIRST_NAME, MIDDLE_INITIAL, gender, DATE_OF_BIRTH, EMAIL, CONTACT_NUMBER, GUARDIAN_NAME, GUARDIAN_EMAIL, GUARDIAN_CELLPHONE_NUM, ADDRESS) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, student.getStudentUid());
			pstmt.setString(2, student.getLrn());
			pstmt.setString(3, student.getLAST_NAME());
			pstmt.setString(4, student.getFIRST_NAME());
			pstmt.setString(5, student.getMiddleInitial());
			pstmt.setString(6, student.getGender());
			pstmt.setDate(7, student.getDob());
			pstmt.setString(8, student.getEmail());
			pstmt.setString(9, student.getContactNumber());
			pstmt.setString(10, student.getGuardianName());
			pstmt.setString(11, student.getGuardianEmail());
			pstmt.setString(12, student.getGuardianContactNumber());
			pstmt.setString(13, student.getAddress());
			pstmt.executeUpdate();
			System.out.println("Student data added successfully.");
		} catch (SQLException e) {
			handleSQLException(e, "createStudentData");
		}
	}

	// READ Student Record
	public StudentsRecord getStudentRecord(Connection conn, int studentUid) {
		String sql = "SELECT * FROM STUDENTS_RECORD WHERE student_uid = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, studentUid);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					StudentsRecord studentRecord = new StudentsRecord();
					studentRecord.setStudentRecordId(rs.getInt("student_record_id"));
					studentRecord.setStudentUid(rs.getInt("student_uid"));
					studentRecord.setViolationId(rs.getInt("violation_id"));
					studentRecord.setTypeOfStudent(rs.getString("type_of_student"));
					studentRecord.setAcademicYear(rs.getString("academic_year"));
					studentRecord.setSemester(rs.getString("semester"));
					studentRecord.setStrand(rs.getString("strand"));
					studentRecord.setTrack(rs.getString("track"));
					studentRecord.setYearLevel(rs.getString("year_level"));
					studentRecord.setAdviser(rs.getString("adviser"));
					studentRecord.setSection(rs.getString("section"));
					studentRecord.setStatus(rs.getString("STATUS"));
					studentRecord.setUpdatedAt(rs.getTimestamp("updated_at"));

					return studentRecord;
				}
			}
		} catch (SQLException e) {
			handleSQLException(e, "getStudentRecord");
		}
		return null;
	}

	public List<StudentsData> getAllStudentsData(Connection conn) throws SQLException {
        List<StudentsData> students = new ArrayList<>();
        String sql = "SELECT * FROM STUDENTS_DATA";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StudentsData student = new StudentsData();
                    student.setStudentUid(rs.getInt("student_uid"));
                    student.setLrn(rs.getString("LRN"));
                    student.setLAST_NAME(rs.getString("LAST_NAME"));
                    student.setFIRST_NAME(rs.getString("FIRST_NAME"));
                    student.setMiddleInitial(rs.getString("MIDDLE_INITIAL"));
                    student.setSuffix(rs.getString("SUFFIX"));
                    student.setGender(rs.getString("GENDER"));
                    student.setDob(rs.getDate("DATE_OF_BIRTH"));
                    student.setEmail(rs.getString("email"));
                    student.setContactNumber(rs.getString("contact_number"));
                    student.setGuardianName(rs.getString("guardian_name"));
                    student.setGuardianEmail(rs.getString("guardian_email"));
                    student.setGuardianContactNumber(rs.getString("GUARDIAN_CELLPHONE_NUM"));
                    student.setAddress(rs.getString("ADDRESS"));
                    students.add(student);
                }
            }
        }
        return students;
    }
	
	
	public static void readStudent(Connection conn, String LRN) {
		String sql = "SELECT * FROM STUDENTS_DATA WHERE LRN = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, LRN);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				System.out.println("LRN: " + rs.getString("LRN"));
				System.out.println("Name: " + rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME"));
				System.out.println("Gender: " + rs.getString("gender"));
				System.out.println("Email: " + rs.getString("email"));
				// Add other columns as needed
			} else {
				System.out.println("No student found with LRN: " + LRN);
			}
		} catch (SQLException e) {
			System.err.println("Error occurred during the SELECT operation: " + e.getMessage());
			e.printStackTrace();
		}
	}
	public void updateViolationRecord(Connection conn, int violationId, String status) {
		String sql = "UPDATE VIOLATION_RECORD SET V_status = ? WHERE violation_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setInt(2, violationId);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Violation record updated successfully.");
			} else {
				System.out.println("No violation found with the given ID.");
			}
		} catch (SQLException e) {
			handleSQLException(e, "updateViolationRecord");
		}
	}

	
	public void deleteStudentData(Connection conn, int studentUid) {
		String sql = "DELETE FROM STUDENTS_DATA WHERE student_uid = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, studentUid);
			int rowsAffected = pstmt.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println("Student record deleted successfully.");
			} else {
				System.out.println("No student found with the given UID.");
			}
		} catch (SQLException e) {
			handleSQLException(e, "deleteStudentData");
		}
	}

	public void handleSQLException(SQLException e, String operation) {
		System.err.println("Error during " + operation + ": " + e.getMessage());
		System.err.println("SQL State: " + e.getSQLState());
		System.err.println("Error Code: " + e.getErrorCode());
	}

	public StudentsData getStudentDataByLrn(Connection connection, String lrn) throws SQLException {
		String query = "SELECT * FROM STUDENTS_DATA WHERE LRN = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, lrn);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new StudentsData(
					rs.getInt("student_uid"),
					rs.getString("LRN"),
					rs.getString("LAST_NAME"),
					rs.getString("FIRST_NAME"),
					rs.getString("middle_initial"),
					rs.getString("SUFFIX"),
					rs.getString("GENDER"),
					rs.getDate("DATE_OF_BIRTH"),
					rs.getString("email"),
					rs.getString("contact_number"),
					rs.getString("guardian_name"),
					rs.getString("guardian_email"),
					rs.getString("GUARDIAN_CELLPHONE_NUM"),
					rs.getString("ADDRESS")
				);
			}
		}
		return null;
	}

	public StudentsRecord getStudentRecordByUid(Connection connection, int studentUid) throws SQLException {
		String query = "SELECT * FROM STUDENTS_RECORD WHERE STUDENT_UID = ?";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, studentUid);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new StudentsRecord(
					rs.getInt("STUDENT_RECORD_ID"),
					rs.getInt("STUDENT_UID"),
					rs.getInt("VIOLATION_ID"),
					rs.getString("TYPE_OF_STUDENT"),
					rs.getString("ACADEMIC_YEAR"),
					rs.getString("SEMESTER"),
					rs.getString("STRAND"),
					rs.getString("TRACK"),
					rs.getString("YEAR_LEVEL"),
					rs.getString("ADVISER"),
					rs.getString("SECTION"),
					rs.getString("STATUS"),
					rs.getTimestamp("UPDATED_AT")
				);
			}
		}
		return null;
	}

	public StudentsData getStudentById(int studentUid) throws SQLException {
		String sql = "SELECT * FROM STUDENTS WHERE student_uid = ?";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, studentUid);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToStudent(rs);
				}
			}
		}
		return null;
	}

	private StudentsData mapResultSetToStudent(ResultSet rs) throws SQLException {
		StudentsData student = new StudentsData();
		student.setStudentUid(rs.getInt("student_uid"));
		student.setLrn(rs.getString("LRN"));
		student.setFIRST_NAME(rs.getString("FIRST_NAME"));
		student.setLAST_NAME(rs.getString("LAST_NAME"));
		student.setMiddleInitial(rs.getString("middle_initial"));
		student.setSuffix(rs.getString("SUFFIX"));
		student.setDob(rs.getDate("DATE_OF_BIRTH"));
		student.setGender(rs.getString("GENDER"));
		student.setEmail(rs.getString("email"));
		student.setContactNumber(rs.getString("contact_number"));
		student.setGuardianName(rs.getString("guardian_name"));
		student.setGuardianEmail(rs.getString("guardian_email"));
		student.setGuardianContactNumber(rs.getString("GUARDIAN_CELLPHONE_NUM"));
		student.setAddress(rs.getString("ADDRESS"));
		
		return student;


	}
}
