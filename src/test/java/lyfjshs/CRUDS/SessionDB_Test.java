package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;

public class SessionDB_Test {

    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            createSession(conn, scanner);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createSession(Connection conn, Scanner scanner) {
        try {
            ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            AppointmentDAO appointmentDAO = new AppointmentDAO(conn);

            System.out.println("What participant type are you? (1: student, 2: non-student): ");
            int participantTypeChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            int participantId;
            if (participantTypeChoice == 2) {
                System.out.println("Enter participant last name: ");
                String lastName = scanner.nextLine();
                System.out.println("Enter participant first name: ");
                String firstName = scanner.nextLine();
                System.out.println("Enter participant email: ");
                String email = scanner.nextLine();
                System.out.println("Enter participant contact number: ");
                String contactNumber = scanner.nextLine();

                Participants participant = new Participants(
                        null, "non-student", lastName, firstName, email, contactNumber);
                participantsDAO.createParticipant(participant);
                participantId = participant.getParticipantId();
                System.out.println("✔ Non-student participant created with ID: " + participantId);
            } else {
                System.out.println("Enter student LRN: ");
                String studentLrn = scanner.nextLine();
                Participants studentParticipant = participantsDAO.getParticipantById(Integer.parseInt(studentLrn));
                if (studentParticipant == null) {
                    System.out.println("❌ Student not found.");
                    return;
                }
                participantId = studentParticipant.getParticipantId();
                System.out.println("✔ Student participant found with ID: " + participantId);
            }

            System.out.println("Enter appointment type (1: walk-in, 2: appointed): ");
            int appointmentTypeChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            Integer appointmentId = null;
            if (appointmentTypeChoice == 2) {
                System.out.println("Enter appointment date and time (YYYY-MM-DD HH:MM:SS): ");
                String appointmentDateTimeStr = scanner.nextLine();

                Timestamp appointmentDateTime;
                try {
                    appointmentDateTime = Timestamp.valueOf(appointmentDateTimeStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("❌ Invalid date format! Please use YYYY-MM-DD HH:MM:SS.");
                    return;
                }

                List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(appointmentDateTime.toLocalDateTime().toLocalDate());

                if (appointments.isEmpty()) {
                    System.out.println("❌ No appointments found for this date.");
                    return; // Prevents null pointer exception
                }

                for (Appointment appointment : appointments) {
                    if (appointment.getAppointmentDateTime().equals(appointmentDateTime)) {
                        appointmentId = appointment.getAppointmentId();
                        break;
                    }
                }

                if (appointmentId == null) {
                    System.out.println("❌ Exact appointment not found. Please check the date and time.");
                    return;
                }

                System.out.println("✔ Appointment found with ID: " + appointmentId);
            }
            

            int guidanceCounselorId = 1; // Example guida2nce counselor ID
            String appointmentType = appointmentTypeChoice == 1 ? "walk-in" : "scheduled";
            Sessions session = new Sessions(
                    0, // Auto-incremented session ID
                    appointmentId, // Appointment ID or null for walk-in
                    guidanceCounselorId, // Guidance counselor ID
                    participantId, // Participant ID
                    null, // Violation ID (optional)
                    appointmentType, // Appointment type
                    "General Counseling", // Consultation type
                    Timestamp.valueOf(LocalDateTime.now()), // Session date and time
                    "Initial session notes.", // Session notes
                    "Scheduled", // Session status
                    Timestamp.valueOf(LocalDateTime.now()) // Updated at
            );
            sessionsDAO.addSession(session);
            System.out.println("✔ Session created successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error creating session: " + e.getMessage());
        }
    }
}
