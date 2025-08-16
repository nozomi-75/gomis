package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.IncidentFillUpFormPanel;
import net.miginfocom.swing.MigLayout;

public class IncidentRecords extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(IncidentRecords.class);
	
	private final Connection conn;
	private GTable incidentsTable;
	private IncidentsDAO incidentsDAO;
	private ParticipantsDAO participantsDAO;
	
	public IncidentRecords(Connection conn) {
		this.conn = conn;
		this.incidentsDAO = new IncidentsDAO(conn);
		this.participantsDAO = new ParticipantsDAO(conn);
		initializeComponents();
		loadIncidents();
	}
	
	private void initializeComponents() {
		setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow]"));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Create column configuration
		String[] columnNames = {"#", "Reporter", "Date", "Description", "Status", "Actions"};
		Class<?>[] columnTypes = {Integer.class, String.class, String.class, String.class, String.class, Object.class};
		boolean[] editableColumns = {false, false, false, false, false, true};
		double[] columnWidths = {0.05, 0.2, 0.15, 0.3, 0.1, 0.2};
		int[] alignments = {
			SwingConstants.CENTER, 
			SwingConstants.LEFT, 
			SwingConstants.CENTER, 
			SwingConstants.LEFT, 
			SwingConstants.CENTER, 
			SwingConstants.CENTER
		};
		
		// Create action manager for the table
		TableActionManager actionManager = new DefaultTableActionManager();
		
		// Add View action
		((DefaultTableActionManager) actionManager).addAction("View", 
			(table, row) -> viewIncident(getIncidentId(row)), 
			new Color(0x518b6f), 
			new FlatSVGIcon("icons/view.svg", 0.5f)
		);
		
		// Add Resolve action only for Active incidents
		((DefaultTableActionManager) actionManager).addAction("Resolve",
			(table, row) -> {
				String status = (String) table.getValueAt(row, 4);
				if ("Active".equals(status)) {
					resolveIncident(getIncidentId(row));
				}
			},
			new Color(0x28a745),
			new FlatSVGIcon("icons/check.svg", 0.5f)
		);
		
		// Initialize table with empty data
		incidentsTable = new GTable(new Object[0][6], columnNames, columnTypes, editableColumns, columnWidths, alignments, false, actionManager);
		
		// Style the table
		incidentsTable.setRowHeight(40);
		incidentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
		
		// Add components to panel
		add(incidentsTable.getTableHeader(), "wrap, growx");
		add(incidentsTable, "grow");
	}
	
	private void loadIncidents() {
		try {
			List<Incident> incidents = incidentsDAO.getAllIncidents();
			Object[][] data = new Object[incidents.size()][6];
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			
			for (int i = 0; i < incidents.size(); i++) {
				Incident incident = incidents.get(i);
				// Get the reporter name from the participant ID
				String reporterName = "N/A";
				try {
					int participantId = incident.getParticipantId();
					if (participantId > 0) {
						Participants reporter = participantsDAO.getParticipantById(participantId);
						if (reporter != null) {
							reporterName = reporter.getParticipantFirstName() + " " + reporter.getParticipantLastName();
						}
					}
				} catch (Exception e) {
					logger.warn("Could not fetch reporter name for incident " + incident.getIncidentId(), e);
				}
				
				data[i][0] = i + 1;
				data[i][1] = reporterName;
				data[i][2] = incident.getIncidentDate().toLocalDateTime().format(formatter);
				data[i][3] = truncateText(incident.getIncidentDescription(), 50);
				data[i][4] = incident.getStatus();
				data[i][5] = incident.getIncidentId();
			}
			
			incidentsTable.setData(data);
		} catch (SQLException e) {
			logger.error("Error loading incident records", e);
			JOptionPane.showMessageDialog(this, "Error loading incident records: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void resolveIncident(int incidentId) {
		int confirm = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to mark this incident as resolved?",
			"Confirm Resolution",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
			
		if (confirm == JOptionPane.YES_OPTION) {
			try {
				// Start transaction
				conn.setAutoCommit(false);
				
				try {
					// Get the incident
					Incident incident = incidentsDAO.getIncidentById(incidentId);
					if (incident != null) {
						// Update status to Ended
						incident.setStatus("Ended");
						incident.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
						
						// Save changes
						incidentsDAO.updateIncident(incident);
						
						// Commit transaction
						conn.commit();
						
						// Show success message
						JOptionPane.showMessageDialog(this,
							"Incident has been marked as resolved.",
							"Success",
							JOptionPane.INFORMATION_MESSAGE);
							
						// Refresh the table
						loadIncidents();
					}
				} catch (SQLException e) {
					// Rollback on error
					conn.rollback();
					throw e;
				} finally {
					// Reset auto-commit
					conn.setAutoCommit(true);
				}
			} catch (SQLException e) {
				logger.error("Error resolving incident", e);
				JOptionPane.showMessageDialog(this,
					"Error resolving incident: " + e.getMessage(),
					"Database Error",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void viewIncident(int incidentId) {
		try {
			Incident incident = incidentsDAO.getIncidentById(incidentId);
			if (incident != null) {
				IncidentFillUpFormPanel viewForm = new IncidentFillUpFormPanel(conn);
				// Implement view-only mode in IncidentFillUpFormPanel
				// viewForm.setViewMode(incident);
				// FormManager.showForm(viewForm);
			}
		} catch (SQLException e) {
			logger.error("Error viewing incident", e);
			JOptionPane.showMessageDialog(this,
				"Error viewing incident: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private int getIncidentId(int row) {
		return ((Number) incidentsTable.getValueAt(row, 5)).intValue();
	}
	
	private String truncateText(String text, int maxLength) {
		if (text == null || text.length() <= maxLength) {
			return text;
		}
		return text.substring(0, maxLength - 3) + "...";
	}
	
	public void refreshIncidents() {
		loadIncidents();
	}
}
