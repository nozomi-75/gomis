package lyfjshs.gomis.view.incident;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;

public class IncidentList extends Form {
    private GTable table;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private static final String TABLE_VIEW = "table";
    private static final String DETAILS_VIEW = "details";
    private Connection connection;
    private IncidentsDAO incidentsDAO;

    public IncidentList(Connection conn) {
        this.connection = conn;
        this.incidentsDAO = new IncidentsDAO(connection);
        setLayout(new BorderLayout(0, 0));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel tablePanel = createTablePanel();
        cardPanel.add(tablePanel, TABLE_VIEW);

        add(cardPanel);
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"#", "Participant", "Date", "Description", "Status", "Actions"};
        Class<?>[] columnTypes = {Integer.class, String.class, String.class, String.class, String.class, Object.class};
        boolean[] editableColumns = {false, false, false, false, false, true};
        double[] columnWidths = {0.05, 0.25, 0.15, 0.3, 0.1, 0.15};
        int[] alignments = {
            SwingConstants.CENTER, 
            SwingConstants.LEFT, 
            SwingConstants.CENTER,
            SwingConstants.LEFT,
            SwingConstants.CENTER,
            SwingConstants.CENTER
        };

        TableActionManager actionManager = new DefaultTableActionManager();
        ((DefaultTableActionManager)actionManager)
            .addAction("View", (t, row) -> {
                int incidentId = (Integer) t.getValueAt(row, 0);
                showIncidentDetails(incidentId);
            }, new java.awt.Color(100, 149, 237), null)
            .addAction("Delete", (t, row) -> {
                int incidentId = (Integer) t.getValueAt(row, 0);
                deleteIncident(incidentId);
            }, new java.awt.Color(220, 53, 69), null);

        Object[][] tableData = fetchIncidentData();
        table = new GTable(
            tableData,
            columnNames,
            columnTypes,
            editableColumns,
            columnWidths,
            alignments,
            false,
            actionManager
        );

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private Object[][] fetchIncidentData() {
        try {
            List<Incident> incidents = incidentsDAO.getAllIncidents();
            Object[][] data = new Object[incidents.size()][6];
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (int i = 0; i < incidents.size(); i++) {
                Incident incident = incidents.get(i);
                data[i][0] = incident.getIncidentId();
                String participantName = incident.getParticipants() != null ? 
                    incident.getParticipants().getParticipantFirstName() + " " + 
                    incident.getParticipants().getParticipantLastName() : 
                    "Unknown";
                data[i][1] = participantName;
                data[i][2] = dateFormat.format(incident.getIncidentDate());
                data[i][3] = incident.getIncidentDescription();
                data[i][4] = incident.getStatus();
                data[i][5] = "";
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading incident data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return new Object[0][6];
        }
    }

    private void showIncidentDetails(int incidentId) {
        try {
            Incident incident = incidentsDAO.getCompleteIncidentDetails(incidentId);
            if (incident == null) {
                JOptionPane.showMessageDialog(this, "Incident not found!");
                return;
            }

            IncidentFullData detailsPanel = new IncidentFullData(
                incident,
                incidentsDAO,
                () -> cardLayout.show(cardPanel, TABLE_VIEW),
                () -> refreshTable()
            );
            cardPanel.add(detailsPanel, DETAILS_VIEW);
            cardLayout.show(cardPanel, DETAILS_VIEW);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading incident details: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteIncident(int incidentId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this incident?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (incidentsDAO.deleteIncident(incidentId)) {
                    JOptionPane.showMessageDialog(this, "Incident deleted successfully!");
                    refreshTable();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error deleting incident: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        Object[][] newData = fetchIncidentData();
        table.setModel(new javax.swing.table.DefaultTableModel(
            newData,
            new String[]{"#", "Participant", "Date", "Description", "Status", "Actions"}
        ));
    }
}