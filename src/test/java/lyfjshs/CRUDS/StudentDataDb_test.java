package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Student;


public class StudentDataDb_test {

    public static void main(String[] args) {
        Connection connection = null;
        try {
            // Establish database connection
            connection = DBConnection.getConnection();
            System.out.println("Successfully connected to the database!");

            // Create DAO instance
            StudentsDataDAO dao = new StudentsDataDAO(connection);

            // Test student LRN
            String testLrn = "LRN123456790";

            // Check if test student already exists and delete if found
            try {
                Student existingStudent = dao.getStudentDataByLrn(testLrn);
                if (existingStudent != null) {
                    System.out.println("Test student already exists, deleting first...");
                    dao.deleteStudentData(existingStudent.getStudentUid());
                }
            } catch (SQLException e) {
                System.err.println("Error checking for existing student: " + e.getMessage());
            }

            // Create a new address
            System.out.println("Creating new address record...");
            int addressId = createAddress(connection, "789", "Test St.", "NCR", "Metro Manila", "Manila City", "Barangay Test", "1000");
            Address newAddress = new Address(addressId, "789", "Test St.", "NCR", "Metro Manila", "Manila City", "Barangay Test", "1000");

            // Create a new contact
            System.out.println("Creating new contact record...");
            int contactId = createContact(connection, "09171234567");
            Contact newContact = new Contact(contactId, "09171234567");

            // Create a new student
            System.out.println("Creating new student record...");
            // Assuming PARENTS and Guardian objects are created or fetched from the database
            Parents PARENTS = new Parents(1, "John", "Doe", "123 Main St", "john.doe@example.com", "1234567890", "Some additional info");
            Guardian guardian = new Guardian(0, "GuardianFirstName", "", "GuardianMiddleName", "GuardianRelationship");

            Student newStudent = new Student(
                    1, // studentUid
                    1, // parentID
                    1, // guardianID
                    1, // contactID
                    1, // anotherID
                    "LRN123456790", // lrn
                    "Doe", // lastName
                    "John", // firstName
                    "Smith", // middleName
                    "john.doe@example.com", // email
                    new java.sql.Date(new java.util.Date().getTime()), // birthDate
                    "123 Main St", // address
                    1234567890, // phoneNumber
                    "Male", // gender
                    "Single", // civilStatus
                    newAddress, // Include address
                    newContact, // Include contact
                    PARENTS, // Include parent
                    guardian  // Include guardian
            );

            // Create the student
            boolean created = dao.createStudentData(newStudent);
            if (created) {
                System.out.println("Student data added successfully.");
            } else {
                System.out.println("Failed to create student data.");
            }

            // Read the student data to verify creation
            System.out.println("Reading student data...");
            try {
                Student student = dao.getStudentDataByLrn(testLrn);
                if (student != null) {
                    System.out.println("Retrieved Student Data: " + student);

                    // Create a guardian for the student if needed
                    ensureGuardianExists(connection, student.getStudentUid());

                    // Update student with a guardian ID
                    System.out.println("Updating student data...");
                    student.setStudentLastname("Smith-Updated");
                    student.setGuardianId(getGuardianId(connection, student.getStudentUid()));
                    boolean updated = dao.updateStudentData(student);
                    if (updated) {
                        System.out.println("Student data updated successfully.");
                    } else {
                        System.out.println("Failed to update student data.");
                    }

                    // Verify update
                    Student updatedStudent = dao.getStudentDataByLrn(testLrn);
                    if (updatedStudent != null) {
                        System.out.println("Updated Student Data: " + updatedStudent);

                        // Delete student data
                        System.out.println("Deleting student data...");
                        boolean deleted = dao.deleteStudentData(student.getStudentUid());
                        if (deleted) {
                            System.out.println("Student record deleted successfully.");
                        } else {
                            System.out.println("Failed to delete student record.");
                        }

                        // Verify deletion
                        Student deletedStudent = dao.getStudentDataByLrn(testLrn);
                        if (deletedStudent == null) {
                            System.out.println("Student record deleted successfully.");
                        } else {
                            System.out.println("Failed to delete student record.");
                        }
                    } else {
                        System.out.println("Failed to retrieve updated student data.");
                    }
                } else {
                    System.out.println("Failed to retrieve student data after creation.");
                }
            } catch (SQLException e) {
                System.err.println("Error retrieving student data: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Creates a new address record and returns the generated ID
     */
    private static int createAddress(Connection connection, String houseNumber, String streetSubdivision, String region,
                                    String province, String municipality, String barangay, String zipCode) {
        String sql = "INSERT INTO ADDRESS (ADDRESS_HOUSE_NUMBER, ADDRESS_STREET_SUBDIVISION, ADDRESS_REGION, " +
                     "ADDRESS_PROVINCE, ADDRESS_MUNICIPALITY, ADDRESS_BARANGAY, ADDRESS_ZIP_CODE) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, houseNumber);
            stmt.setString(2, streetSubdivision);
            stmt.setString(3, region);
            stmt.setString(4, province);
            stmt.setString(5, municipality);
            stmt.setString(6, barangay);
            stmt.setString(7, zipCode);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating address: " + e.getMessage());
        }
        return 0; // Return 0 if creation fails
    }

    /**
     * Creates a new contact record and returns the generated ID
     */
    private static int createContact(Connection connection, String contactNumber) {
        String sql = "INSERT INTO CONTACT (CONTACT_NUMBER) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, contactNumber);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating contact: " + e.getMessage());
        }
        return 0; // Return 0 if creation fails
    }

    /**
     * Ensures a guardian exists for the given student ID
     */
    private static void ensureGuardianExists(Connection connection, int studentId) {
        int guardianId = getGuardianId(connection, studentId);
        if (guardianId > 0) {
            System.out.println("Guardian already exists for student ID: " + studentId + " with ID: " + guardianId);
            return;
        }

        String insertGuardian = "INSERT INTO GUARDIAN (STUDENT_ID, GUARDIAN_LASTNAME, GUARDIAN_FIRSTNAME, GUARDIAN_MIDDLENAME, GUARDIAN_RELATIONSHIP) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertGuardian, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, studentId);
            stmt.setString(2, "Test");
            stmt.setString(3, "Guardian");
            stmt.setString(4, "T");
            stmt.setString(5, "Uncle");

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        guardianId = generatedKeys.getInt(1);
                        System.out.println("Created new guardian for student ID: " + studentId + " with ID: " + guardianId);
                    }
                }
            } else {
                System.out.println("Failed to create guardian for student ID: " + studentId);
            }
        } catch (SQLException e) {
            System.err.println("Error creating guardian: " + e.getMessage());
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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("GUARDIAN_ID");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting guardian ID: " + e.getMessage());
        }
        return 0;
    }
}