package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.model.Session;
import lyfjshs.gomis.Database.model.SessionParticipant;

public class SessionDB_Test {
       public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/gomisDB";
        String user = "root";
        String password = "";
        
             try (Connection conn = DriverManager.getConnection(url, user, password)) {
            // Initialize SessionsDAO with the connection
            SessionsDAO sessionsDAO = new SessionsDAO(conn);

            // Create a new session
            Session newSession = new Session(0, 1, 2, 3, 4, "Counseling", 
                    Timestamp.valueOf(LocalDateTime.now()), "Initial session", "Scheduled", 
                    Timestamp.valueOf(LocalDateTime.now()));
            sessionsDAO.addSession(newSession);
            System.out.println("Session added successfully!");

            // Retrieve all sessions and display them
            List<Session> sessions = sessionsDAO.getAllSessions();
            System.out.println("Sessions List:");
            for (Session session : sessions) {
                System.out.println(session);
            }

            // Update an existing session (use a session ID from your DB)
            if (!sessions.isEmpty()) {
                Session sessionToUpdate = sessions.get(0); // Just take the first session
                sessionToUpdate.setSessionNotes("Updated session notes");
                sessionToUpdate.setSessionStatus("Completed");
                sessionsDAO.updateSession(sessionToUpdate);
                System.out.println("Session updated successfully!");
            }

            // Delete a session (use a session ID from your DB)
            if (!sessions.isEmpty()) {
                int sessionIdToDelete = sessions.get(0).getSessionId(); // Delete the first session
                sessionsDAO.deleteSession(sessionIdToDelete);
                System.out.println("Session with ID " + sessionIdToDelete + " deleted successfully!");
            }

            // Add a participant to a session (use valid session ID and participant ID)
            if (!sessions.isEmpty()) {
                SessionParticipant participant = new SessionParticipant(sessions.get(0).getSessionId(), 2); // Example participant
                sessionsDAO.addParticipantToSession(participant);
                System.out.println("Participant added to session successfully!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        }
       }

