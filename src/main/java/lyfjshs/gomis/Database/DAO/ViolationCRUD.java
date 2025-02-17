package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.Violation;

public class ViolationCRUD {
	private Connection connection;

	public ViolationCRUD(Connection connection) {
		this.connection = connection;
	}

	public void addViolation(Connection connection, int participantId, String violationType, String description,
			String anecdotalRecord,
			String reinforcement, String status) throws SQLException {
		String sql = "INSERT INTO VIOLATION_RECORD (participant_id, violation_type, violation_description, anecdotal_record, reinforcement, V_status) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, participantId);
			stmt.setString(2, violationType);
			stmt.setString(3, description);
			stmt.setString(4, anecdotalRecord);
			stmt.setString(5, reinforcement);
			stmt.setString(6, status);
			stmt.executeUpdate();
		}
	}

	public static List<Violation> getAllViolations(Connection connection) throws SQLException {
		List<Violation> violations = new ArrayList<>();
		String sql = "SELECT v.*, s.LRN, s.LAST_NAME, s.FIRST_NAME, p.PARTICIPANT_TYPE, p.EMAIL, p.CONTACT_NUMBER " +
					"FROM VIOLATION_RECORD v " +
					"JOIN PARTICIPANTS p ON v.participant_id = p.participant_id " +
					"LEFT JOIN STUDENTS_DATA s ON p.student_uid = s.student_uid";

		try (PreparedStatement stmt = connection.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Violation violation = new Violation();
				violation.setViolationId(rs.getInt("VIOLATION_ID"));
				violation.setViolationType(rs.getString("VIOLATION_TYPE"));
				violation.setViolationDescription(rs.getString("VIOLATION_DESCRIPTION"));
				violation.setAnecdotalRecord(rs.getString("ANECDOTAL_RECORD"));
				violation.setReinforcement(rs.getString("REINFORCEMENT"));
				violation.setStatus(rs.getString("V_status"));
				violation.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
				violation.setEmail(rs.getString("EMAIL"));
				violation.setContact(rs.getString("CONTACT_NUMBER"));
				violation.setParticipantType(rs.getString("PARTICIPANT_TYPE"));
				violation.setStudentLRN(rs.getString("LRN"));
				violation.setFIRST_NAME(rs.getString("FIRST_NAME"));
				violation.setLAST_NAME(rs.getString("LAST_NAME"));

				violations.add(violation);
			}
		}
		return violations;
	}

	public List<Violation> getViolationsWithStudentInfo(Connection connection) {
		List<Violation> violations = new ArrayList<>();
		String sql = "SELECT v.*, p.participant_type, p.FIRST_NAME, p.LAST_NAME, p.email, p.contact_number, " +
				"p.student_uid FROM VIOLATION_RECORD v " +
				"JOIN PARTICIPANTS p ON v.participant_id = p.participant_id";

		try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				Violation violation = new Violation(
						rs.getInt("violation_id"),
						rs.getInt("participant_id"),
						formatParticipantType(rs),
						rs.getString("violation_type"),
						rs.getString("violation_description"),
						rs.getString("email"),
						rs.getString("contact_number"),
						rs.getString("reinforcement"),
						rs.getString("V_status"));
				violations.add(violation);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return violations;
	}

	private String formatParticipantType(ResultSet rs) throws SQLException {
		String participantType = rs.getString("participant_type");
		String FIRST_NAME = rs.getString("FIRST_NAME");
		String LAST_NAME = rs.getString("LAST_NAME");
		String studentUid = rs.getString("student_uid");

		if (studentUid != null) {
			return String.format("Student (LRN: %s) - %s %s", studentUid, FIRST_NAME, LAST_NAME);
		} else {
			return String.format("%s - %s %s", participantType, FIRST_NAME, LAST_NAME);
		}
	}

	public void updateViolationStatus(Connection connection, int violationId, String status) throws SQLException {
		String sql = "UPDATE VIOLATION_RECORD SET V_status = ? WHERE violation_id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, status);
			stmt.setInt(2, violationId);
			stmt.executeUpdate();
		}
	}

	public boolean deleteViolation(Connection connection, int violationId) throws SQLException {
		String sql = "DELETE FROM VIOLATION_RECORD WHERE violation_id = ?";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, violationId);
			stmt.executeUpdate();
		}
		return false;
	}

	public Violation getViolationById(Connection connection, int violationId) throws SQLException {
		String sql = "SELECT v.*, p.LAST_NAME, p.FIRST_NAME, s.LRN " +
				"FROM VIOLATION_RECORD v " +
				"JOIN PARTICIPANTS p ON v.participant_id = p.participant_id " +
				"LEFT JOIN STUDENTS_DATA s ON p.student_uid = s.LRN " +
				"WHERE v.violation_id = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, violationId);

			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToViolation(rs);
				}
			}
		}
		return null;
	}

	public List<Violation> searchViolations(Connection connection, String searchTerm) throws SQLException {
		List<Violation> violations = new ArrayList<>();
		String sql = "SELECT v.*, p.LAST_NAME, p.FIRST_NAME, s.LRN " +
				"FROM VIOLATION_RECORD v " +
				"JOIN PARTICIPANTS p ON v.participant_id = p.participant_id " +
				"LEFT JOIN STUDENTS_DATA s ON p.student_uid = s.LRN " +
				"WHERE p.LAST_NAME LIKE ? OR p.FIRST_NAME LIKE ? " +
				"OR v.violation_type LIKE ? OR s.LRN LIKE ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			String searchPattern = "%" + searchTerm + "%";
			stmt.setString(1, searchPattern);
			stmt.setString(2, searchPattern);
			stmt.setString(3, searchPattern);
			stmt.setString(4, searchPattern);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					violations.add(mapResultSetToViolation(rs));
				}
			}
		}
		return violations;
	}

	private static Violation mapResultSetToViolation(ResultSet rs) throws SQLException {
		Violation violation = new Violation();
		violation.setViolationId(rs.getInt("violation_id"));
		violation.setParticipantId(rs.getInt("participant_id"));
		violation.setViolationType(rs.getString("violation_type"));
		violation.setViolationDescription(rs.getString("violation_description"));
		violation.setAnecdotalRecord(rs.getString("anecdotal_record"));
		violation.setReinforcement(rs.getString("reinforcement"));
		violation.setStatus(rs.getString("V_status"));
		violation.setUpdatedAt(rs.getTimestamp("updated_at"));
		violation.setFIRST_NAME(rs.getString("FIRST_NAME"));
		violation.setLAST_NAME(rs.getString("LAST_NAME"));
		violation.setStudentLRN(rs.getString("LRN"));
		return violation;
	}

	public static boolean createViolation(Connection connection, String lrn, String violationType, String description,
			String FIRST_NAME, String LAST_NAME, String email, String contact,
			String participantType, String reinforcement, String status) {

		// First create or get participant
		int participantId;
		String createParticipantSQL = "INSERT INTO PARTICIPANTS (participant_type, FIRST_NAME, LAST_NAME, email, contact_number, student_uid) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try {
			// Start transaction
			connection.setAutoCommit(false);

			// Create participant
			try (PreparedStatement stmt = connection.prepareStatement(createParticipantSQL,
					Statement.RETURN_GENERATED_KEYS)) {
				stmt.setString(1, participantType);
				stmt.setString(2, FIRST_NAME);
				stmt.setString(3, LAST_NAME);
				stmt.setString(4, email);
				stmt.setString(5, contact);
				stmt.setString(6, "Student".equals(participantType) ? lrn : null);

				int rowsAffected = stmt.executeUpdate();
				if (rowsAffected == 0) {
					connection.rollback();
					return false;
				}

				// Get the generated participant ID
				try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						participantId = generatedKeys.getInt(1);
					} else {
						connection.rollback();
						return false;
					}
				}
			}

			// Create violation record
			String createViolationSQL = "INSERT INTO VIOLATION_RECORD (participant_id, violation_type, violation_description, "
					+ "anecdotal_record, reinforcement, V_status, student_uid) VALUES (?, ?, ?, ?, ?, ?, ?)";

			try (PreparedStatement stmt = connection.prepareStatement(createViolationSQL)) {
				stmt.setInt(1, participantId);
				stmt.setString(2, violationType);
				stmt.setString(3, description);
				stmt.setString(4, ""); // anecdotal record
				stmt.setString(5, reinforcement);
				stmt.setString(6, status);
				stmt.setInt(7, participantId); // Assuming participantId is the same as studentUid for this example

				int rowsAffected = stmt.executeUpdate();
				if (rowsAffected != 1) {
					connection.rollback();
					return false;
				}
			}

			// Commit transaction
			connection.commit();
			return true;

		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
			return false;
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean updateViolation(Connection connection, int violationId, String violationType, String description,
			String anecdotalRecord, String reinforcement) {
		String sql = "UPDATE VIOLATION_RECORD SET violation_type = ?, violation_description = ?, " +
				"anecdotal_record = ?, reinforcement = ?, updated_at = CURRENT_TIMESTAMP " +
				"WHERE violation_id = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, violationType);
			stmt.setString(2, description);
			stmt.setString(3, anecdotalRecord);
			stmt.setString(4, reinforcement);
			stmt.setInt(5, violationId);

			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<String> getAllViolationsWithStudents(Connection connection) {
		List<String> violations = new ArrayList<>();
		String sql = "SELECT v.*, p.participant_type, p.FIRST_NAME, p.LAST_NAME, " +
				"p.email, p.contact_number, p.student_uid " +
				"FROM VIOLATION_RECORD v " +
				"JOIN PARTICIPANTS p ON v.participant_id = p.participant_id";

		try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				String participantInfo = formatParticipantInfo(rs);
				violations.add(String.format("%s, %s, %s, %s, %s, %s",
						participantInfo,
						rs.getString("email"),
						rs.getString("contact_number"),
						rs.getString("violation_type"),
						rs.getString("reinforcement"),
						rs.getString("V_status")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return violations;
	}

	private String formatParticipantInfo(ResultSet rs) throws SQLException {
		String participantType = rs.getString("participant_type");
		String FIRST_NAME = rs.getString("FIRST_NAME");
		String LAST_NAME = rs.getString("LAST_NAME");
		String studentUid = rs.getString("student_uid");

		if (studentUid != null) {
			return String.format("Student (LRN: %s) - %s %s", studentUid, FIRST_NAME, LAST_NAME);
		} else {
			return String.format("%s - %s %s", participantType, FIRST_NAME, LAST_NAME);
		}
	}

	public static boolean addViolation(Connection connection, int participantId, String violationType,
			String description,
			String anecdotalRecord, String reinforcement, String status, String updatedAt) {
		String sql = "INSERT INTO VIOLATION_RECORD (participant_id, violation_type, violation_description, " +
				"anecdotal_record, reinforcement, V_status) VALUES (?, ?, ?, ?, ?, ?)";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, participantId);
			stmt.setString(2, violationType);
			stmt.setString(3, description);
			stmt.setString(4, anecdotalRecord);
			stmt.setString(5, reinforcement);
			stmt.setString(6, status);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0; // Return true if the insert was successful
		} catch (SQLException e) {
			e.printStackTrace();
			return false; // Return false if there was an error
		}
	}

	public Violation getViolationById(int violationId) throws SQLException {
		String sql = "SELECT * FROM VIOLATIONS WHERE violation_id = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setInt(1, violationId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return mapResultSetToViolation(rs);
				}
			}
		}
		return null;
	}

	public Violation getViolationByLRN(Connection connection, String lrn) throws SQLException {
		String sql = "SELECT v.*, p.participant_type, p.FIRST_NAME, p.LAST_NAME, p.email, p.contact_number, " +
				"p.student_uid FROM VIOLATION_RECORD v " +
				"JOIN PARTICIPANTS p ON v.participant_id = p.participant_id " +
				"JOIN STUDENTS_DATA s ON p.student_uid = s.LRN " +
				"WHERE s.LRN = ?";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, lrn);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					Violation violation = new Violation();
					violation.setViolationId(rs.getInt("violation_id"));
					violation.setParticipantId(rs.getInt("participant_id"));
					violation.setViolationType(rs.getString("violation_type"));
					violation.setViolationDescription(rs.getString("violation_description"));
					violation.setAnecdotalRecord(rs.getString("anecdotal_record"));
					violation.setReinforcement(rs.getString("reinforcement"));
					violation.setStatus(rs.getString("V_status"));
					violation.setUpdatedAt(rs.getTimestamp("updated_at"));
					violation.setFIRST_NAME(rs.getString("FIRST_NAME"));
					violation.setLAST_NAME(rs.getString("LAST_NAME"));
					violation.setEmail(rs.getString("email"));
					violation.setContact(rs.getString("contact_number"));
					violation.setStudentLRN(rs.getString("student_uid"));
					return violation;
				}
			}
		}
		return null;
	}

}
