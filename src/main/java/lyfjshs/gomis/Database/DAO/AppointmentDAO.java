package lyfjshs.gomis.Database.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lyfjshs.gomis.Database.model.Appointment;

public class AppointmentDAO {
	// Create session for Appointment
	public void addAppointment(Connection conn, 
							Integer participantId, Integer counselorId, 
							  String appointmentType, Timestamp appointmentDateTime,
							  String appointmentStatus) {
		String query = "INSERT INTO appointments (participant_id, counselors_id, " +
					  "appointment_type, appointment_date_time, appointment_status) " +
					  "VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
			// Handle nullable participantId
			if (participantId != null) {
				preparedStatement.setInt(1, participantId);
			} else {
				preparedStatement.setNull(1, Types.INTEGER);
			}
			
			// Handle nullable counselorId
			if (counselorId != null) {
				preparedStatement.setInt(2, counselorId);
			} else {
				preparedStatement.setNull(2, Types.INTEGER);
			}
			
			preparedStatement.setString(3, appointmentType);
			preparedStatement.setTimestamp(4, appointmentDateTime);
			preparedStatement.setString(5, appointmentStatus);

			preparedStatement.executeUpdate();
			System.out.println("Appointment added successfully.");
		} catch (SQLException e) {
			throw new RuntimeException("Error adding appointment: " + e.getMessage(), e);
		}
	}
	
	public List<Appointment> getAppointmentsForDate(Connection conn, LocalDate date) {
		List<Appointment> appointments = new ArrayList<>();
		String query = "SELECT * FROM appointments WHERE DATE(appointment_date_time) = ?";

		try (PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setDate(1, java.sql.Date.valueOf(date));
			
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					appointments.add(new Appointment(
						rs.getInt("appointment_id"),
						rs.getObject("participant_id", Integer.class),
						rs.getObject("counselors_id", Integer.class),
						rs.getString("appointment_type"),
						rs.getTimestamp("appointment_date_time").toLocalDateTime(),
						rs.getString("appointment_status")
					));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error retrieving appointments: " + e.getMessage(), e);
		}
		return appointments;
	}

	// Update session for Appointment
	public void updateAppointments(Connection connection, int participantId, int counselors_id, String appointment_type,
			java.sql.Timestamp appointment_date_time, String appointment_status) {
		String updateSQL = "UPDATE appointments SET participant_id = ?, counselors_id = ?, appointment_type = ?, appointment_date_time = ?, appointment_status = ? ";
		try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
			preparedStatement.setInt(1, participantId);
			preparedStatement.setInt(2, counselors_id);
			preparedStatement.setString(3, appointment_type);
			preparedStatement.setTimestamp(4, appointment_date_time);
			preparedStatement.setString(5, appointment_status);

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Updated " + rowsAffected + " record(s).\n");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Delete sesion for Appointment
	public void deleteAppointments(Connection connection, int participantId) {
		String deleteSQL = "DELETE FROM appointments WHERE participant_id = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
			preparedStatement.setInt(1, participantId);

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Deleted " + rowsAffected + " record(s).\n");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}