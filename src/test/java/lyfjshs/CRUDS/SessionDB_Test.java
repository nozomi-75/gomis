package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;

public class SessionDB_Test {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/gomisdb";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YourRootPassword123!";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            SessionsDAO sessionsDAO = new SessionsDAO(conn);

            while (true) {
                System.out.println("\n===== SESSION PARTICIPANT MANAGEMENT SYSTEM =====");
                System.out.println("1. Create New Session with Participants");
                System.out.println("2. View Session Details with Participants");
                System.out.println("3. View All Sessions with Participants");
                System.out.println("4. Exit");
                System.out.print("Select an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        createNewSession(sessionsDAO);
                        break;
                    case 2:
                        viewSessionWithParticipants(sessionsDAO);
                        break;
                    case 3:
                        viewAllSessionsWithParticipants(sessionsDAO);
                        break;
                    case 4:
                        System.out.println("Exiting system...");
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createNewSession(SessionsDAO sessionsDAO) {
        try {
            // Get input from user
            System.out.print("Enter Appointment ID (or 0 for Walk-in): ");
            Integer appointmentId = scanner.nextInt();
            if (appointmentId == 0) {
                appointmentId = null;
            }
    
            System.out.print("Enter Guidance Counselor ID: ");
            int guidanceCounselorId = scanner.nextInt();
    
            System.out.print("Enter Violation ID (or 0 if none): ");
            Integer violationId = scanner.nextInt();
            if (violationId == 0) {
                violationId = null;
            }
            scanner.nextLine();
    
            // Collect session details
            System.out.print("Enter Appointment Type: ");
            String appointmentType = scanner.nextLine();
    
            System.out.print("Enter Consultation Type: ");
            String consultationType = scanner.nextLine();
    
            System.out.print("Enter Session Date and Time (YYYY-MM-DD HH:MM:SS): ");
            String sessionDateTimeStr = scanner.nextLine();
            Timestamp sessionDateTime = Timestamp.valueOf(sessionDateTimeStr);
    
            System.out.print("Enter Session Notes: ");
            String sessionNotes = scanner.nextLine();
    
            System.out.print("Enter Session Status: ");
            String sessionStatus = scanner.nextLine();
    
            // Collect participants
            List<Participants> participants = new ArrayList<>();
            while (true) {
                System.out.print("Enter Participant ID (or 0 to finish adding): ");
                int participantId = scanner.nextInt();
                if (participantId == 0) {
                    break;
                }
                if (!sessionsDAO.participantExists(participantId)) {
                    System.out.println("Error: Participant ID " + participantId + " not found.");
                } else if (isDuplicateParticipant(participants, participantId)) {
                    System.out.println("Error: Participant ID " + participantId + " is already added.");
                } else {
                    participants.add(new Participants(participantId, null, null, null, null, null));
                }
            }
            // // Create new session
            // Sessions newSession = new Sessions(appointmentId, guidanceCounselorId, violationId,
            //         violationId, appointmentType, consultationType, sessionDateTime, sessionNotes, sessionStatus, 
            //                             sessionStatus, new Timestamp(System.currentTimeMillis()));
    
            // // ✅ Set participants
            // newSession.setParticipants(participants);
            // // Insert session into database
            // int sessionId = sessionsDAO.addSession(newSession);
            // if (sessionId > 0) {
            //     System.out.println("✅ Session created successfully with ID: " + sessionId);
            //     System.out.println("✅ Participants added successfully.");
            // } else {
            //     System.out.println("❌ Failed to create session.");
            // }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
       
    // Helper method to check for duplicate participant in the list
    private static boolean isDuplicateParticipant(List<Participants> participants, int participantId) {
        return participants.stream().anyMatch(p -> p.getParticipantId() == participantId);
    }

    // 2. View a specific session with full participant details
    private static void viewSessionWithParticipants(SessionsDAO sessionsDAO) {
        System.out.print("Enter Session ID: ");
        int sessionId = scanner.nextInt();

        try {
            Sessions session = sessionsDAO.getSessionWithParticipants(sessionId);
            if (session == null) {
                System.out.println("No session found with ID " + sessionId);
                return;
            }
            displaySessionWithParticipants(session);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. View all sessions with full participant details
    private static void viewAllSessionsWithParticipants(SessionsDAO sessionsDAO) {
        try {
            List<Sessions> sessions = sessionsDAO.getSessionDataWithParticipantCount();
            if (sessions.isEmpty()) {
                System.out.println("No sessions found.");
                return;
            }
            for (Sessions session : sessions) {
                displaySessionWithParticipants(session);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to display a session and its participants
    private static void displaySessionWithParticipants(Sessions session) {
        System.out.println("\n===== SESSION ID: " + session.getSessionId() + " =====");
        System.out.println("Appointment Type: " + session.getAppointmentType());
        System.out.println("Consultation Type: " + session.getConsultationType());
        System.out.println("Date/Time: " + session.getSessionDateTime());
        System.out.println("Status: " + session.getSessionStatus());
        System.out.println("Notes: " + session.getSessionNotes());
        System.out.println("Participants (" + session.getParticipants().size() + "):");

        for (Participants p : session.getParticipants()) {
            String category = (p.getStudentUid() != null) ? "Student" : "Non-Student";
            System.out.println(" - ID: " + p.getParticipantId() +
                    " | Name: " + p.getParticipantFirstName() + " " + p.getParticipantLastName() +
                    " | Type: " + p.getParticipantType() +
                    " | Category: " + category +
                    " | Sex: " + p.getSex() +
                    " | Contact: " + p.getContactNumber());
        }
        System.out.println("===============================================");
    }
}
