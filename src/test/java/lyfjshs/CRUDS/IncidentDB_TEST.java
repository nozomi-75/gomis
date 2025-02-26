package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.model.Incident;

public class IncidentDB_TEST {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://localhost:3306/gomisDB";
        String user = "root"; 	
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            IncidentsDAO incidentsDAO = new IncidentsDAO(conn); // Passing connection to DAO class if needed

                        // Test createIncident using default constructor and setters
            Incident newIncident = new Incident(); // Using the default constructor
            newIncident.setIncidentId(0); // Set a valid incidentId (0 or any valid int)
            newIncident.setStudentUid(1);
            newIncident.setParticipantId(1);
            newIncident.setViolationId(1);
            newIncident.setIncidentDate(Timestamp.valueOf(LocalDateTime.now()));
            newIncident.setIncidentDescription("Description of incident");
            newIncident.setActionTaken("Action taken");
            newIncident.setRecommendation("Recommendation");
            newIncident.setStatus("Pending");
            newIncident.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

            int incidentId = incidentsDAO.createIncident(newIncident);
            System.out.println("Created Incident with ID: " + incidentId);

            // Test getIncidentById
            Incident retrievedIncident = incidentsDAO.getIncidentById(incidentId);
            System.out.println("Retrieved Incident: " + (retrievedIncident != null ? retrievedIncident.getIncidentDescription() : "Not found"));

            // Test updateIncident
            if (retrievedIncident != null) {
                retrievedIncident.setIncidentDescription("Updated description");
                boolean updated = incidentsDAO.updateIncident(retrievedIncident);
                System.out.println("Updated Incident: " + updated);
            }

            // Test getAllIncidents
            List<Incident> allIncidents = incidentsDAO.getAllIncidents();
            System.out.println("Total Incidents: " + allIncidents.size());

            // Test deleteIncident
            boolean deleted = incidentsDAO.deleteIncident(incidentId);
            System.out.println("Deleted Incident: " + deleted);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
