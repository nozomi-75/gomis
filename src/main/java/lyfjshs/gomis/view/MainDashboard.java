package lyfjshs.gomis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.model.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.appointment.AppointmentOverview;
import lyfjshs.gomis.view.violation.ViolationFullData;
import net.miginfocom.swing.MigLayout;	

public class MainDashboard extends Form {

	private Connection connection;
	private JPanel contentPanel;
	private JPanel centralTablePanel;
	
	public MainDashboard(Connection conn) {
		this.connection = conn;
		this.setLayout(new BorderLayout());

		contentPanel = new JPanel(new MigLayout("fill, gap 10", "[grow 65][]", "[grow]"));
		this.add(contentPanel, BorderLayout.CENTER); // Center the content panel

		centralTablePanel = new JPanel(new MigLayout("", "[386.00,grow]", "[30px][grow]"));
		contentPanel.add(centralTablePanel, "grow");

		JPanel headerTablePanel = new JPanel(new MigLayout("", "[40px:70px][40px:70px][grow]", "[grow][]"));
		centralTablePanel.add(headerTablePanel, "cell 0 0,grow");

		JLabel lblNewLabel = new JLabel("List of Violations:");
		headerTablePanel.add(lblNewLabel, "cell 0 1 2 1");

		JScrollPane tableScrollPane = createTablePanel();
		centralTablePanel.add(tableScrollPane, "cell 0 1,grow");

		JPanel sideRPanel = new JPanel(new MigLayout("", "[center]", "[][grow][]"));
		contentPanel.add(sideRPanel, "grow");
		
		JLabel lblNewLabel_1 = new JLabel("Appointments overview");
		sideRPanel.add(lblNewLabel_1, "cell 0 0");

		AppointmentOverview appointmentOverview = new AppointmentOverview(conn);
		sideRPanel.add(appointmentOverview, "cell 0 1,grow");

		JPanel actionPanel = new JPanel(new MigLayout("wrap 4, insets 20, gap 20", "[grow][grow][grow][grow]", "[][]"));
		contentPanel.add(actionPanel, "cell 0 2,grow");

		// Create panels for each section
		JPanel violationPanel = createActionPanel("VIOLATION", "VIEW");
		JPanel appointmentPanel = createActionPanel("APPOINTMENT", "SET SCHEDULE");
		JPanel sessionsPanel = createActionPanel("SESSIONS", "VIEW");
		JPanel incidentPanel = createActionPanel("INCIDENT REPORT", "PRINT");

		// Add panels to the action panel
		actionPanel.add(violationPanel, "grow");
		actionPanel.add(appointmentPanel, "grow");
		actionPanel.add(sessionsPanel, "grow");
		actionPanel.add(incidentPanel, "grow");

		// Add this debug code temporarily in MainDashboard constructor
		try {
			Connection testConn = DBConnection.getConnection();
			if (testConn != null && !testConn.isClosed()) {
				System.out.println("Database connected successfully");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.5f);
	FlatSVGIcon resolveIcon = new FlatSVGIcon("icons/resolve.svg", 0.5f); 

	private JScrollPane createTablePanel() {
		String[] columnNames = { "LRN", "Full Name", "Grade & Strand", "Status", "Actions" };
		
		// Create table model that doesn't allow direct editing
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 4; // Only allow editing in Actions column
			}
		};
		
		JTable table = new JTable(model);
		table.setRowHeight(30); // Set a fixed height for rows to shorten the panel
		table.getColumnModel().getColumn(0).setPreferredWidth(50); // LRN
		table.getColumnModel().getColumn(1).setPreferredWidth(150); // Full Name
		table.getColumnModel().getColumn(2).setPreferredWidth(100); // Grade & Strand
		table.getColumnModel().getColumn(3).setPreferredWidth(80); // Status
		table.getColumnModel().getColumn(4).setPreferredWidth(100); // Actions
		
		// Load violation data
		try {
			List<Violation> violations = ViolationCRUD.getAllViolations(connection);
			for (Violation violation : violations) {
				String fullName = String.format("%s %s", violation.getFIRST_NAME(), violation.getLAST_NAME());
				String gradeStrand = getGradeAndStrand(violation.getParticipantId(), connection);
				
				Object[] rowData = {
					violation.getStudentLRN(),
					fullName,
					gradeStrand,
					violation.getStatus(),
					""  // Actions column
				};
				model.addRow(rowData);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
				"Error loading violations: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}

		// Configure action column
		TableActionManager actionsColumn = new TableActionManager();
		
		// View action
		actionsColumn.addAction("View", (t, row) -> {
			String lrn = (String) t.getValueAt(row, 0);
			showViolationDetails(lrn);
		}, new Color(0, 150, 136), viewIcon);

		// Resolve action
		actionsColumn.addAction("Resolve", (t, row) -> {
			String lrn = (String) t.getValueAt(row, 0);
			resolveViolation(lrn);
		}, new Color(0, 150, 136), resolveIcon);

		actionsColumn.applyTo(table, 4);

		return new JScrollPane(table);
	}

	private void showViolationDetails(String lrn) {
		try {
			// Get violation data
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			Violation violation = violationCRUD.getViolationByLRN(connection, lrn);
			
			if (violation != null) {
				// Create and show violation details panel
				JPanel violationDetailPanel = new ViolationFullData(violation);
				
				// Show in dialog
				JDialog dialog = new JDialog();
				dialog.setTitle("Violation Details");
				dialog.setModal(true);
				dialog.setSize(800, 600);
				dialog.setLocationRelativeTo(null);
				dialog.add(violationDetailPanel);
				dialog.setVisible(true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error retrieving violation details: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void resolveViolation(String lrn) {
		try {
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			Violation violation = violationCRUD.getViolationByLRN(connection, lrn);
			
			if (violation != null) {
				int confirm = JOptionPane.showConfirmDialog(this,
					"Are you sure you want to mark this violation as resolved?",
					"Confirm Resolution",
					JOptionPane.YES_NO_OPTION);
					
				if (confirm == JOptionPane.YES_OPTION) {
					violationCRUD.updateViolationStatus(connection, violation.getViolationId(), "Resolved");
					refreshTable();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				"Error resolving violation: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private String getGradeAndStrand(int participantId, Connection conn) throws SQLException {
		String query = "SELECT sr.year_level, sr.strand " +
					  "FROM STUDENT_RECORD sr " +
					  "JOIN PARTICIPANTS p ON p.student_uid = sr.student_uid " +
					  "WHERE p.participant_id = ?";
					  
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			stmt.setInt(1, participantId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return String.format("Grade %d - %s", 
					rs.getInt("year_level"), 
					rs.getString("strand"));
			}
		}
		return "N/A";
	}

	private void refreshTable() {
		// Remove the existing table from the panel
		Component[] components = contentPanel.getComponents();
		for (Component component : components) {
			if (component instanceof JScrollPane) {
				contentPanel.remove(component);
				break;
			}
		}
		
		// Create and add the new table
		JScrollPane newTableScrollPane = createTablePanel();
		centralTablePanel.add(newTableScrollPane, "cell 0 1,grow");
		
		// Revalidate and repaint the panel
		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private JPanel createActionPanel(String title, String buttonText) {
		JPanel panel = new JPanel(new MigLayout("wrap 1, insets 10, gap 5", "[center]", "[][]"));
		panel.setBackground(new Color(240, 240, 240));
		
		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(titleLabel.getFont().deriveFont(12f));
		
		JButton actionButton = new JButton(buttonText);
		actionButton.setBackground(new Color(220, 220, 220));
		actionButton.setForeground(new Color(60, 60, 60));
		actionButton.setFocusPainted(false);
		actionButton.setBorderPainted(false);
		actionButton.setOpaque(true);
		
		panel.add(titleLabel);
		panel.add(actionButton, "w 120!, h 30!");
		
		return panel;
	}

} 