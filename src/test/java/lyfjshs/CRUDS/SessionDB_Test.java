package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.model.Session;
import lyfjshs.gomis.Database.model.SessionParticipant;

public class SessionDB_Test {
    private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Step 1: Insert a violation record
            ViolationCRUD violationCRUD = new ViolationCRUD(conn);
            int participantId = 1; // Example participant ID
            String violationType = "Academic";
            String description = "Cheating during midterm exam";
            String anecdotalRecord = "Caught using unauthorized materials";
            String reinforcement = "Warning";
            String status = "Active";

            violationCRUD.addViolation(conn, participantId, violationType, description, anecdotalRecord, reinforcement, status);

            // Step 2: Test SessionsDAO methods
            testAddSession(conn);
            testGetAllSessions(conn);
            testUpdateSession(conn);
            testDeleteSession(conn);
            testAddParticipantToSession(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void testAddSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            Session session = new Session(0, 1, 1, 1, 1, "Individual Counseling", 
                                           Timestamp.valueOf(LocalDateTime.now()), 
                                           "Initial assessment of academic performance.", 
                                           "Scheduled", 
                                           Timestamp.valueOf(LocalDateTime.now()));
            sessionsDAO.addSession(session);
            System.out.println("Session added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding session: " + e.getMessage());
        }
    }

    private static void testGetAllSessions(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Session> sessions = sessionsDAO.getAllSessions();
            System.out.println("Sessions List:");
            for (Session session : sessions) {
                System.out.println(session);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving sessions: " + e.getMessage());
        }
    }

    private static void testUpdateSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Session> sessions = sessionsDAO.getAllSessions();
            if (!sessions.isEmpty()) {
                Session sessionToUpdate = sessions.get(0); // Just take the first session
                sessionToUpdate.setSessionNotes("Updated session notes");
                sessionToUpdate.setSessionStatus("Completed");
                sessionsDAO.updateSession(sessionToUpdate);
                System.out.println("Session updated successfully!");
            } else {
                System.out.println("No sessions available to update.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating session: " + e.getMessage());
        }
    }

    private static void testDeleteSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Session> sessions = sessionsDAO.getAllSessions();
            if (!sessions.isEmpty()) {
                int sessionIdToDelete = sessions.get(0).getSessionId(); // Delete the first session
                sessionsDAO.deleteSession(sessionIdToDelete);
                System.out.println("Session with ID " + sessionIdToDelete + " deleted successfully!");
            } else {
                System.out.println("No sessions available to delete.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
        }
    }

    private static void testAddParticipantToSession(Connection conn) {
        try {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);
            List<Session> sessions = sessionsDAO.getAllSessions();
            if (!sessions.isEmpty()) {
                SessionParticipant participant = new SessionParticipant(sessions.get(0).getSessionId(), 1); // Example participant
                sessionsDAO.addParticipantToSession(participant);
                System.out.println("Participant added to session successfully!");
            } else {
                System.out.println("No sessions available to add a participant.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding participant to session: " + e.getMessage());
        }
    }
}

