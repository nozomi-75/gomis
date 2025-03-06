package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.SessionParticipant;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;

public class SessionDB_Test {

    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            createSessionWithParticipantsAndAppointments(conn, scanner);
            testAddPaticipantForSession(conn);
            testAddSession(conn);
            testGetAllSessions(conn);
            testUpdateSession(conn);
            testDeleteSession(conn);
            testAddParticipantToSession(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createSessionWithParticipantsAndAppointments(Connection conn, Scanner scanner) {
        try {
            ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);
            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(conn);
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            AppointmentDAO appointmentDAO = new AppointmentDAO(conn);

            System.out.println("Enter participant type (1: student, 2: non-student): ");
            int participantTypeChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            int participantId;
            if (participantTypeChoice == 2) {
                Participants participant = new Participants(
                        null, // for non-student, set to 0 or NULL
                        "non-student", // participant type
                        "Doe", // last name
                        "Smith", // first name
                        "john@email.com", // email
                        "1234567890" // contact number
                );
                participantsDAO.createParticipant(participant);
                participantId = participant.getParticipantId();
            } else {
                System.out.println("Enter student LRN: ");
                String studentLrn = scanner.nextLine();
                Student student = studentsDataDAO.getStudentDataByLrn(studentLrn);
                if (student == null) {
                    System.out.println("❌ Student not found.");
                    return;
                }
                participantId = student.getStudentUid();
            }

            System.out.println("Enter appointment type (1: walk-in, 2: appointed): ");
            int appointmentTypeChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            int appointmentId = 0;
            if (appointmentTypeChoice == 2) {
                System.out.println("Enter appointment date and time (YYYY-MM-DD HH:MM:SS): ");
                String appointmentDateTimeStr = scanner.nextLine();
                Timestamp appointmentDateTime = Timestamp.valueOf(appointmentDateTimeStr);
                List<Appointment> appointments = appointmentDAO.getAppointmentsForDate(appointmentDateTime.toLocalDateTime().toLocalDate());
                for (Appointment appointment : appointments) {
                    if (appointment.getAppointmentDateTime().equals(appointmentDateTime)) {
                        appointmentId = appointment.getAppointmentId();
                        break;
                    }
                }
                if (appointmentId == 0) {
                    System.out.println("❌ Appointment not found.");
                    return;
                }
            }

            // Check if GUIDANCE_COUNSELOR_ID exists
            int guidanceCounselorId = 1; // example ID
            if (!guidanceCounselorExists(conn, guidanceCounselorId)) {
                System.out.println("❌ Guidance Counselor ID does not exist.");
                return;
            }

            Sessions session = new Sessions(
                    0, // session ID (auto-increment)
                    appointmentId,
                    guidanceCounselorId, // guidance counselor ID
                    participantId,
                    null, // violation ID (example)
                    "Individual Counseling", // session type
                    Timestamp.valueOf(LocalDateTime.now()), // session date and time
                    "Initial assessment of academic performance.", // session notes
                    "Scheduled", // session status
                    Timestamp.valueOf(LocalDateTime.now()) // updated at
            );
            sessionsDAO.addSession(session);
            System.out.println("✔ Session created successfully!");

        } catch (SQLException e) {
            System.err.println("❌ Error creating session: " + e.getMessage());
        }
    }

    private static boolean guidanceCounselorExists(Connection conn, int guidanceCounselorId) throws SQLException {
        String query = "SELECT COUNT(*) FROM GUIDANCE_COUNSELORS WHERE GUIDANCE_COUNSELOR_ID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, guidanceCounselorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private static void testAddPaticipantForSession(Connection conn) {
        // create a participant in Participant table
        ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);
        Participants participant = new Participants(
                null, // for non-student, set to 0 or NULL
                "non-student", // participant type
                "Doe", // last name
                "Smith", // first name
                "john@email.com", // email
                "1234567890" // contact number
        );
        try {
            participantsDAO.createParticipant(participant);
        } catch (SQLException e) {
            System.err.println("❌ Error adding participant: " + e.getMessage());
        }
        // then retrieve the participant id
        int participantId = participant.getParticipantId();
        System.out.println("✔ Participant added successfully with ID: " + participantId);
        // then add the participant to the session
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Sessions> sessions = sessionsDAO.getAllSessions();
            if (sessions != null && !sessions.isEmpty()) {
                int sessionId = sessions.get(0).getSessionId();
                SessionParticipant sessionParticipant = new SessionParticipant(sessionId, participantId);
                sessionsDAO.addParticipantToSession(sessionParticipant);
                System.out.println("✔ Participant added to session successfully!");
            } else {
                System.out.println("❌ No sessions available to add a participant.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding participant to session: " + e.getMessage());
        }

    }

    private static boolean participantExists(Connection conn, int participantId) throws SQLException {
        String query = "SELECT COUNT(*) FROM PARTICIPANTS WHERE participant_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, participantId);
            return stmt.executeQuery().next();
        }
    }

    private static void testAddSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            Sessions session = new Sessions(0, 1, 1, 1, 1, "Individual Counseling",
                    Timestamp.valueOf(LocalDateTime.now()),
                    "Initial assessment of academic performance.",
                    "Scheduled",
                    Timestamp.valueOf(LocalDateTime.now()));
            sessionsDAO.addSession(session);
            System.out.println("✔ Session added successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error adding session: " + e.getMessage());
        }
    }

    private static void testGetAllSessions(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Sessions> sessions = sessionsDAO.getAllSessions();
            if (sessions != null && !sessions.isEmpty()) {
                System.out.println("✔ Sessions List:");
                for (Sessions session : sessions) {
                    System.out.println(session);
                }
            } else {
                System.out.println("❌ No sessions found.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving sessions: " + e.getMessage());
        }
    }

    private static void testUpdateSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Sessions> sessions = sessionsDAO.getAllSessions();
            if (sessions != null && !sessions.isEmpty()) {
                Sessions sessionToUpdate = sessions.get(0);
                sessionToUpdate.setSessionNotes("Updated session notes");
                sessionToUpdate.setSessionStatus("Completed");
                sessionsDAO.updateSession(sessionToUpdate);
                System.out.println("✔ Session updated successfully!");
            } else {
                System.out.println("❌ No sessions available to update.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating session: " + e.getMessage());
        }
    }

    private static void testDeleteSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Sessions> sessions = sessionsDAO.getAllSessions();
            if (sessions != null && !sessions.isEmpty()) {
                int sessionIdToDelete = sessions.get(0).getSessionId();
                sessionsDAO.deleteSession(sessionIdToDelete);
                System.out.println("✔ Session with ID " + sessionIdToDelete + " deleted successfully!");
            } else {
                System.out.println("❌ No sessions available to delete.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting session: " + e.getMessage());
        }
    }

    private static void testAddParticipantToSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Sessions> sessions = sessionsDAO.getAllSessions();
            if (sessions != null && !sessions.isEmpty()) {
                int sessionId = sessions.get(0).getSessionId();
                int participantId = 1;
                SessionParticipant participant = new SessionParticipant(sessionId, participantId);
                sessionsDAO.addParticipantToSession(participant);
                System.out.println("✔ Participant added to session successfully!");
            } else {
                System.out.println("❌ No sessions available to add a participant.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding participant to session: " + e.getMessage());
        }
    }
}
