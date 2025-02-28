package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.StudentsData;

public class StudentsDataDAO {
	private Connection connection;

	public StudentsDataDAO(Connection connection) {
		this.connection = connection;
	}
	// CREATE Student Data
	public void createStudentData(Connection conn, StudentsData student) {
		String sql = "INSERT INTO STUDENT (STUDENT_UID, Parent_ID, Guardian_ID, APPOINTMENTS_ID, CONTACT_ID, STUDENT_LRN, STUDENT_LASTNAME, STUDENT_FIRSTNAME, STUDENT_MIDDLENAME, STUDENT_SEX, STUDENT_BIRTHDATE, STUDENT_MOTHERTONGUE, STUDENT_AGE, STUDENT_IP_TYPE, STUDENT_RELIGION) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, student.getStudentUid());
			pstmt.setInt(2, student.getParentId());
			if (student.getGuardianId() == 0) {
				pstmt.setNull(3, java.sql.Types.INTEGER);
			} else {
				pstmt.setInt(3, student.getGuardianId());
			}
			pstmt.setInt(4, student.getAppointmentsId());
			pstmt.setInt(5, student.getContactId());
			pstmt.setString(6, student.getLrn());
			pstmt.setString(7, student.getLastName());
			pstmt.setString(8, student.getFirstName());
			pstmt.setString(9, student.getMiddleName());
			pstmt.setString(10, student.getSEX());
			pstmt.setDate(11, student.getBirthDate());
			pstmt.setString(12, student.getMotherTongue());
			pstmt.setInt(13, student.getAge());
			pstmt.setString(14, student.getIpType());
			pstmt.setString(15, student.getReligion());
			pstmt.executeUpdate();
			System.out.println("Student data added successfully.");
		} catch (SQLException e) {
			handleSQLException(e, "createStudentData");
		}
	}

	// READ Student Data by LRN
	public StudentsData getStudentDataByLrn(Connection conn, String lrn) throws SQLException {
		String query = "SELECT * FROM STUDENT WHERE STUDENT_LRN = ?";
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setString(1, lrn);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return new StudentsData(
					rs.getInt("STUDENT_UID"),
					rs.getInt("Parent_ID"),
					rs.getInt("Guardian_ID"),
					rs.getInt("APPOINTMENTS_ID"),
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
					rs.getString("STUDENT_RELIGION")
				);
			}
		}
		return null;
	}

	public StudentsData getStudentById(int studentUid) throws SQLException {
		String sql = "SELECT * FROM STUDENT WHERE STUDENT_UID = ?";
		
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
		return new StudentsData(
			rs.getInt("STUDENT_UID"),
			rs.getInt("Parent_ID"),
			rs.getInt("Guardian_ID"),
			rs.getInt("APPOINTMENTS_ID"),
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
			rs.getString("STUDENT_RELIGION")
		);
	}

	// READ All Students Data
	public List<StudentsData> getAllStudentsData(Connection conn) throws SQLException {
		List<StudentsData> students = new ArrayList<>();
		String sql = "SELECT * FROM STUDENT";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					StudentsData student = new StudentsData(
						rs.getInt("STUDENT_UID"),
						rs.getInt("Parent_ID"),
						rs.getInt("Guardian_ID"),
						rs.getInt("APPOINTMENTS_ID"),
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
						rs.getString("STUDENT_RELIGION")
					);
					students.add(student);
				}
			}
		}
		return students;
	}

	// UPDATE Student Data
	public void updateStudentData(Connection conn, StudentsData student) {
		String sql = "UPDATE STUDENT SET Parent_ID = ?, Guardian_ID = ?, APPOINTMENTS_ID = ?, CONTACT_ID = ?, STUDENT_LRN = ?, STUDENT_LASTNAME = ?, STUDENT_FIRSTNAME = ?, STUDENT_MIDDLENAME = ?, STUDENT_SEX = ?, STUDENT_BIRTHDATE = ?, STUDENT_MOTHERTONGUE = ?, STUDENT_AGE = ?, STUDENT_IP_TYPE = ?, STUDENT_RELIGION = ? WHERE STUDENT_UID = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, student.getParentId());
			pstmt.setInt(2, student.getGuardianId());
			pstmt.setInt(3, student.getAppointmentsId());
			pstmt.setInt(4, student.getContactId());
			pstmt.setString(5, student.getLrn());
			pstmt.setString(6, student.getLastName());
			pstmt.setString(7, student.getFirstName());
			pstmt.setString(8, student.getMiddleName());
			pstmt.setString(9, student.getSEX());
			pstmt.setDate(10, student.getBirthDate());
			pstmt.setString(11, student.getMotherTongue());
			pstmt.setInt(12, student.getAge());
			pstmt.setString(13, student.getIpType());
			pstmt.setString(14, student.getReligion());
			pstmt.setInt(15, student.getStudentUid());
			pstmt.executeUpdate();
			System.out.println("Student data updated successfully.");
		} catch (SQLException e) {
			handleSQLException(e, "updateStudentData");
		}
	}

	// DELETE Student Data
	public void deleteStudentData(Connection conn, int studentUid) {
		String sql = "DELETE FROM STUDENT WHERE STUDENT_UID = ?";
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

	// Handle SQL Exceptions
	public void handleSQLException(SQLException e, String operation) {
		System.err.println("Error during " + operation + ": " + e.getMessage());
		System.err.println("SQL State: " + e.getSQLState());
		System.err.println("Error Code: " + e.getErrorCode());
	}


}
