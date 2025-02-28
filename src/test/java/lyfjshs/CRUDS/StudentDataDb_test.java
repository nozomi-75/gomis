package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.model.StudentsData;

public class StudentDataDb_test {

    public static void main(String[] args) {
        Connection connection = null;
        try {
            // Establish database connection
            connection = DBConnection.getConnection();
            
            // Create DAO instance
            StudentsDataDAO dao = new StudentsDataDAO(connection);
            
            // Test student LRN
            String testLrn = "LRN123456790";
            
            // Check if test student already exists and delete if found
            try {
                StudentsData existingStudent = dao.getStudentDataByLrn(connection, testLrn);
                if (existingStudent != null) {
                    System.out.println("Test student already exists, deleting first...");
                    dao.deleteStudentData(connection, existingStudent.getStudentUid());
                }
            } catch (SQLException e) {
                System.out.println("Error checking for existing student: " + e.getMessage());
            }
            
            // Create a new student
            System.out.println("Creating new student record...");
            StudentsData newStudent = new StudentsData(
                    4, // New STUDENT_UID (assuming 1, 2, 3 already exist)
                    1, // Parent_ID (use existing parent from DB)
                    0, // Set Guardian_ID to null initially
                    1, // APPOINTMENTS_ID (use existing appointment from DB)
                    1, // CONTACT_ID (use existing address from DB)
                    testLrn, // Unique LRN
                    "Doe", "John", "Smith", "Male",
                    Date.valueOf("2005-06-15"), "English", 20, "IP Type Example", "Christianity"
            );
            
            // Create the student
            dao.createStudentData(connection, newStudent);
            
            // Read the student data to verify creation
            System.out.println("Reading student data...");
            try {
                StudentsData student = dao.getStudentDataByLrn(connection, testLrn);
                if (student != null) {
                    System.out.println("Retrieved Student Data: " + student);
                
                    // Create a guardian for the student if needed
                    ensureGuardianExists(connection, student.getStudentUid());
                    
                    // Update student with a guardian ID
                    System.out.println("Updating student data...");
                    student.setLastName("Smith-Updated");
                    student.setGuardianId(getGuardianId(connection, student.getStudentUid()));
                    dao.updateStudentData(connection, student);
                
                    // Verify update
                    StudentsData updatedStudent = dao.getStudentDataByLrn(connection, testLrn);
                    if (updatedStudent != null) {
                        System.out.println("Updated Student Data: " + updatedStudent);
                        
                        // Delete student data
                        System.out.println("Deleting student data...");
                        dao.deleteStudentData(connection, student.getStudentUid());
                        
                        // Verify deletion
                        try {
                            StudentsData deletedStudent = dao.getStudentDataByLrn(connection, testLrn);
                            if (deletedStudent == null) {
                                System.out.println("Student record deleted successfully.");
                            } else {
                                System.out.println("Failed to delete student record.");
                            }
                        } catch (SQLException e) {
                            System.out.println("Error checking deletion: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Failed to retrieve updated student data.");
                    }
                } else {
                    System.out.println("Failed to retrieve student data after creation.");
                }
            } catch (SQLException e) {
                System.out.println("Error retrieving student data: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                System.out.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Ensures a guardian exists for the given student ID
     */
    private static void ensureGuardianExists(Connection connection, int studentId) {
        // Check if guardian already exists
        if (getGuardianId(connection, studentId) > 0) {
            System.out.println("Guardian already exists for student ID: " + studentId);
            return;
        }
        
        // Create a new guardian
        String insertGuardian = "INSERT INTO GUARDIAN (STUDENT_ID, GUARDIAN_LASTNAME, GUARDIAN_FIRSTNAME, " +
                             "GUARDIAN_MIDDLENAME, GUARDIAN_RELATIONSHIP) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertGuardian)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, "Test");
            stmt.setString(3, "Guardian");
            stmt.setString(4, "T");
            stmt.setString(5, "Uncle");
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Created new guardian for student ID: " + studentId);
            } else {
                System.out.println("Failed to create guardian for student ID: " + studentId);
            }
        } catch (SQLException e) {
            System.out.println("Error creating guardian: " + e.getMessage());
        }
    }
    
    /**
     * Gets the guardian ID for a given student ID
     * @return the guardian ID or 0 if not found
     */
    private static int getGuardianId(Connection connection, int studentId) {
        String query = "SELECT GUARDIAN_ID FROM GUARDIAN WHERE STUDENT_ID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("GUARDIAN_ID");
            }
        } catch (SQLException e) {
            System.out.println("Error getting guardian ID: " + e.getMessage());
        }
        return 0;
    }
}