package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Address;
import lyfjshs.gomis.Database.entity.Contact;
import lyfjshs.gomis.Database.entity.Guardian;
import lyfjshs.gomis.Database.entity.Parents;
import lyfjshs.gomis.Database.entity.Student;

public class StudentSearchTest {
    public static void main(String[] args) {
        Connection connection = null;
        Scanner scanner = new Scanner(System.in);

        try {
            connection = DBConnection.getConnection();
            StudentsDataDAO dao = new StudentsDataDAO(connection);

            System.out.println("🔍 Student Search System");
            System.out.println("1. Search by LRN");
            System.out.println("2. Search by Name and Gender");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            List<Student> students;

            if (choice == 1) {
                System.out.print("Enter Student LRN: ");
                String lrn = scanner.nextLine();
                students = dao.getStudentsByFilters(lrn, null, null, null);
                
            } else {
                System.out.print("Enter First Name (or leave blank): ");
                String firstName = scanner.nextLine();
                System.out.print("Enter Last Name (or leave blank): ");
                String lastName = scanner.nextLine();
                System.out.print("Enter Gender (or leave blank): ");
                String sex = scanner.nextLine();

                students = dao.getStudentsByFilters(null, firstName, lastName, sex);
            }

            if (students.isEmpty()) {
                System.out.println("No students found.");
            } else {
                for (Student student : students) {
                    displayStudentDetails(student);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void displayStudentDetails(Student student) {
        System.out.println("\n📌 STUDENT INFORMATION");
        System.out.println("──────────────────────────────────");
        System.out.println("👤 Name: " + student.getStudentFirstname() + " " + student.getStudentMiddlename() + " "
                + student.getStudentLastname());
        System.out.println("📖 LRN: " + student.getStudentLrn());
        System.out.println("👫 Sex: " + student.getStudentSex());
        System.out.println("🎂 Birthdate: " + student.getStudentBirthdate());
        System.out.println("🗣 Mother Tongue: " + student.getStudentMothertongue());
        System.out.println("🌍 IP Type: " + student.getStudentIpType());
        System.out.println("🛐 Religion: " + student.getStudentReligion());

        System.out.println("\n🏡 ADDRESS");
        System.out.println("──────────────────────────────────");
        Address address = student.getAddress();
        if (address != null) {
            System.out.println("📍 " + address.getAddressHouseNumber() + ", " + address.getAddressStreetSubdivision());
            System.out.println("🏙 " + address.getAddressMunicipality() + ", " + address.getAddressProvince());
            System.out.println("📌 ZIP Code: " + address.getAddressZipCode());
        } else {
            System.out.println("No Address Found.");
        }

        System.out.println("\n📞 CONTACT");
        System.out.println("──────────────────────────────────");
        Contact contact = student.getContact();
        if (contact != null) {
            System.out.println("📱 Phone: " + contact.getContactNumber());
        } else {
            System.out.println("No Contact Info.");
        }

        System.out.println("\n👨‍👩‍👧 PARENTS");
        System.out.println("──────────────────────────────────");
        Parents parents = student.getParents();
        if (parents != null) {
            System.out.println("👨 Father: " + parents.getFatherFirstname() + " " + parents.getFatherLastname() + " ("
                    + parents.getFatherContactNumber() + ")");
            System.out.println("👩 Mother: " + parents.getMotherFirstname() + " " + parents.getMotherLastname() + " ("
                    + parents.getMotherContactNumber() + ")");
        } else {
            System.out.println("No Parent Info.");
        }

        System.out.println("\n👨‍⚖️ GUARDIAN");
        System.out.println("──────────────────────────────────");
        Guardian guardian = student.getGuardian();
        if (guardian != null) {
            System.out.println("🧑 " + guardian.getGuardianFirstname() + " " + guardian.getGuardianLastname() + " ("
                    + guardian.getGuardianRelationship() + ")");
            System.out.println("📞 Contact: " + guardian.getGuardianContactNumber());
        } else {
            System.out.println("No Guardian Info.");
        }

        System.out.println("──────────────────────────────────\n");
    }

}
