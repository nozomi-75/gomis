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
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.SessionParticipant;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;

public class SessionDB_Test {

    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            Scanner scanner = new Scanner(System.in);
            createSessionWithParticipantsAndAppointments(conn, scanner);
            testAddParticipantForSession(conn);
            testAddSession(conn);
            testGetAllSessions(conn);
            testUpdateSession(conn);
            testDeleteSession(conn);
            testAddParticipantToSession(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // FOR WALK IN type of appointment
    // 1ST TEST CASE
    // to create a session with a non-student type of participant
    // with an violation record (depends on what type of violation)

    public void testCreateSessionWithNonStudentViolation(Connection conn) {
        try {
            ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            ViolationCRUD violationCRUD = new ViolationCRUD(conn); // Use ViolationCRUD

            // 1️⃣ Create a non-student participant
            Participants participant = new Participants(
                    null, "non-student", "Doe", "John", "john.doe@email.com", "123456789");
            participantsDAO.createParticipant(participant);
            int participantId = participant.getParticipantId();
            System.out.println("✔ Non-student participant created with ID: " + participantId);

            // 2️⃣ Create a Violation Record for this Participant
            boolean violationAdded = violationCRUD.addViolation(
                    participantId,
                    "Disruptive Behavior", // Example violation type
                    "Was involved in a disturbance in the hallway", // Violation description
                    "Recorded by security", // Anecdotal record
                    "Counseling recommended", // Reinforcement
                    "Pending", // Status
                    Timestamp.valueOf(LocalDateTime.now()) // Updated timestamp
            );

            if (!violationAdded) {
                System.err.println("❌ Failed to create violation record!");
                return;
            }
            System.out.println("✔ Violation recorded for participant ID: " + participantId);

            // 3️⃣ Retrieve the latest violation ID for this participant
            ViolationRecord latestViolation = violationCRUD.getViolationById(participantId);
            if (latestViolation == null) {
                System.err.println("❌ Could not retrieve the newly created violation record!");
                return;
            }
            int violationId = latestViolation.getViolationId();

            // 4️⃣ Create a session for the non-student with a violation
            Sessions session = new Sessions(
                    0, // Auto-incremented session ID
                    0, // No appointment ID since it's walk-in
                    4, // Example guidance counselor ID (change this if needed)
                    participantId, // Non-student participant ID
                    violationId, // Violation ID
                    "walk-in", // Appointment type
                    "Behavioral Counseling", // Consultation type
                    Timestamp.valueOf(LocalDateTime.now()), // Current timestamp
                    "Counseling for behavioral issues.", // Session notes
                    "Scheduled", // Initial session status
                    Timestamp.valueOf(LocalDateTime.now()) // Updated at timestamp
            );
            sessionsDAO.addSession(session);
            System.out.println("✔ Session created for non-student with violation.");

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    // 2ND TEST CASE
    // to create a session with a student type of participant
    // with an violation record (depends on what type of violation)
    public void testCreateSessionWithStudentViolation(Connection conn) {
        try {
            ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            ViolationCRUD violationCRUD = new ViolationCRUD(conn); // Use ViolationCRUD

            // 1️⃣ Create a student participant
            // if a participant is a student then refer to Students table for STUDENT_UID
            // and use the STUDENT_UID as the STUDENT_UID in the Participants table
            // to link the student to the participant
            // ATTRBUTES OF THE PARTICIPANTS TABEL SHOULD CAME FROM THE STUDENTS TABLE

            Participants participant = new Participants(
                    null, "student", "Smith", "Jane", "jane.smith@email.com", "987654321");
            participantsDAO.createParticipant(participant);
            int participantId = participant.getParticipantId();
            System.out.println("✔ Student participant created with ID: " + participantId);

            // 2️⃣ Create a Violation Record for this Student
            boolean violationAdded = violationCRUD.addViolation(
                    participantId,
                    "Cheating", // Example violation type
                    "Student was caught cheating during an exam", // Violation description
                    "Incident reported by the teacher", // Anecdotal record
                    "Parental meeting required", // Reinforcement
                    "Pending", // Status
                    Timestamp.valueOf(LocalDateTime.now()) // Updated timestamp
            );

            if (!violationAdded) {
                System.err.println("❌ Failed to create violation record!");
                return;
            }
            System.out.println("✔ Violation recorded for student ID: " + participantId);

            // 3️⃣ Retrieve the latest violation ID for this student
            ViolationRecord latestViolation = violationCRUD.getViolationById(participantId);
            if (latestViolation == null) {
                System.err.println("❌ Could not retrieve the newly created violation record!");
                return;
            }
            int violationId = latestViolation.getViolationId();

            // 4️⃣ Create a session for the student with a violation
            Sessions session = new Sessions(
                    0, // Auto-incremented session ID
                    0, // No appointment ID since it's walk-in
                    5, // Example guidance counselor ID (change this if needed)
                    participantId, // Student participant ID
                    violationId, // Violation ID
                    "walk-in", // Appointment type
                    "Academic Counseling", // Consultation type
                    Timestamp.valueOf(LocalDateTime.now()), // Current timestamp
                    "Discussion about academic integrity and consequences.", // Session notes
                    "Scheduled", // Initial session status
                    Timestamp.valueOf(LocalDateTime.now()) // Updated at timestamp
            );
            sessionsDAO.addSession(session);
            System.out.println("✔ Session created for student with violation.");

        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    // 3RD TEST CASE
    // to update both of the session status into "completed"

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
                List<Appointment> appointments = appointmentDAO
                        .getAppointmentsForDate(appointmentDateTime.toLocalDateTime().toLocalDate());
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
            int guidanceCounselorId = 4; // example ID
            if (!guidanceCounselorExists(conn, guidanceCounselorId)) {
                System.out.println("❌ Guidance Counselor ID does not exist.");
                return;
            }
            String appointmentType = appointmentTypeChoice == 1 ? "walk-in" : "scheduled";
            Sessions session = new Sessions(
                    0, // session ID (auto-increment)
                    appointmentId,
                    guidanceCounselorId, // guidance counselor ID
                    participantId,
                    0, // violation ID (example)
                    appointmentType, // appointment type
                    "Individual Counseling", // consultation type
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

    // 3rd test case(idk if na ganananana wow fantastic baby dance dance dance)
    private static void updateSessionsToCompleted(Connection conn) {
        System.out.println("🛠 updateSessionsToCompleted called!"); // Debug line
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Sessions> sessions = sessionsDAO.getAllSessions();
            if (sessions != null && !sessions.isEmpty()) {
                for (Sessions session : sessions) {
                    session.setSessionStatus("Completed");
                    sessionsDAO.updateSession(session);
                }
                System.out.println("✔ All sessions updated to 'Completed' status.");
            } else {
                System.out.println("❌ No sessions found to update.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating sessions: " + e.getMessage());
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

    private static void testAddParticipantForSession(Connection conn) {
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
            Sessions session = new Sessions(
                    0,
                    1,
                    1,
                    1,
                    1,
                    "test",
                    "Individual Counseling",
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
