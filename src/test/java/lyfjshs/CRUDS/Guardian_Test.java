package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.GuardianDAO;
import lyfjshs.gomis.Database.entity.Guardian;

public class Guardian_Test {
	public static void main(String[] args) {
		Connection connection = null;
		try {
			connection = DBConnection.getConnection();
			GuardianDAO guardianDAO = new GuardianDAO(connection);

			// Insert a new guardian
			System.out.println("Inserting a new guardian...");
			boolean insertSuccess = guardianDAO.insertGuardian("Doe", "John", "M", "Father", "1234567");
			System.out.println("Insert successful: " + insertSuccess);

			if (!insertSuccess) {
				System.out.println("Insertion failed. Exiting test.");
				return;
			}

			// Retrieve all guardians to get the latest inserted ID
			List<Guardian> guardians = guardianDAO.getAllGuardians();
			if (guardians.isEmpty()) {
				System.out.println("No guardians found. Exiting test.");
				return;
			}
			Guardian lastInsertedGuardian = guardians.get(guardians.size() - 1);
			int lastGuardianId = lastInsertedGuardian.getGuardianId();

			// Retrieve a guardian by ID
			System.out.println("Retrieving guardian with ID: " + lastGuardianId);
			Guardian guardian = guardianDAO.getGuardianById(lastGuardianId);
			if (guardian != null) {
				System.out.println("Guardian found: " + guardian);
			} else {
				System.out.println("Guardian not found.");
			}

			// Update a guardian
			System.out.println("Updating guardian with ID: " + lastGuardianId);
			boolean updateSuccess = guardianDAO.updateGuardian(lastGuardianId, "Doe", "Jonathan", "M", "Father",
					"12345678910");
			System.out.println("Update successful: " + updateSuccess);

			// Retrieve all guardians
			System.out.println("Retrieving all guardians...");
			guardians = guardianDAO.getAllGuardians();
			for (Guardian g : guardians) {
				System.out.println(g);
			}

			// Delete a guardian
			System.out.println("Deleting guardian with ID: " + lastGuardianId);
			boolean deleteSuccess = guardianDAO.deleteGuardian(lastGuardianId);
			System.out.println("Delete successful: " + deleteSuccess);

		} catch (SQLException e) {
			System.err.println("A database error occurred: " + e.getMessage());
			e.printStackTrace();
		} finally {
			// Close the connection in the finally block
			if (connection != null) {
				try {
					connection.close();
					System.out.println("Database connection closed.");
				} catch (SQLException e) {
					System.err.println("Error closing the database connection: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
}
