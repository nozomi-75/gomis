package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.SchoolForm;
import lyfjshs.gomis.Database.entity.Student;

public class StudentDataDb_test {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            // Establish database connection
            connection = DBConnection.getConnection();
            System.out.println("✅ Successfully connected to the database!");

            // Create DAO instance
            StudentsDataDAO dao = new StudentsDataDAO(connection);

            // Test student LRN
            String testLrn = "LRN123456790";

            // Check if student exists, delete if found
            Student existingStudent = dao.getStudentDataByLrn(testLrn);
            if (existingStudent != null) {
                System.out.println("⚠️ Test student already exists. Deleting...");
                dao.deleteStudentData(existingStudent.getStudentUid());
            }

            // Create Address, Contact, Parents, Guardian, and SchoolForm
            Address newAddress = new Address(0, "789", "Test St.", "NCR", "Metro Manila", "Manila City", "Barangay Test", "1000");
            Contact newContact = new Contact(0, "09171234567");
            Parents newParents = new Parents(0, "John", "Doe", "C", "09696912345", "Jenna", "Doe", "G", "09261128000");
            Guardian newGuardian = new Guardian(0, "Reyes", "Carlo", "T.", "Uncle", "09112223344");
            SchoolForm newSchoolForm = new SchoolForm(0, "Test High School", "THS-001", "District 1", "Division A", "Region IV", "First Semester", "2025-2026", "Grade 12", "ICT-101", "ICT", "Programming");

            // Create new student object (without IDs initially)
            Student newStudent = new Student(
                    0, // Auto-generated UID
                    0, 0, 0, 0, // Parent, Guardian, Address, Contact (to be updated)
                    newSchoolForm.getSF_SECTION(), // School section reference
                    testLrn, "Doe", "John", "Smith", "Male",
                    new Date(System.currentTimeMillis()), "Filipino", 17, "None", "Catholic",
                    null, null, null, null, null // Related entities set after insertion
            );

            // Insert into DB with relations
            boolean created = dao.createStudentWithRelations(newStudent, newAddress, newContact, newParents, newGuardian, newSchoolForm);
            if (created) {
                System.out.println("✅ Student added successfully.");
            } else {
                System.err.println("❌ Failed to add student.");
                return;
            }

            // Retrieve student from DB
            Student retrievedStudent = dao.getStudentDataByLrn(testLrn);
            if (retrievedStudent != null) {
                System.out.println("🔍 Retrieved Student Data: " + retrievedStudent);

                // Update student information
                System.out.println("✏️ Updating student last name...");
                retrievedStudent.setStudentLastname("Smith-Updated");
                boolean updated = dao.updateStudentData(retrievedStudent);
                if (updated) {
                    System.out.println("✅ Student updated successfully.");
                } else {
                    System.err.println("❌ Failed to update student.");
                }

                // Verify update
                Student updatedStudent = dao.getStudentDataByLrn(testLrn);
                if (updatedStudent != null) {
                    System.out.println("🔄 Updated Student Data: " + updatedStudent);

                    // Delete student
                    System.out.println("🗑️ Deleting student...");
                    boolean deleted = dao.deleteStudentData(updatedStudent.getStudentUid());
                    if (deleted) {
                        System.out.println("✅ Student record deleted successfully.");
                    } else {
                        System.err.println("❌ Failed to delete student record.");
                    }
                } else {
                    System.err.println("❌ Failed to retrieve updated student data.");
                }
            } else {
                System.err.println("❌ Failed to retrieve student data after creation.");
            }

        } catch (Exception e) {
            System.err.println("❌ An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("🔌 Database connection closed.");
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing database connection: " + e.getMessage());
            }
        }
    }
}
