package lyfjshs.gomis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
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

		contentPanel = new JPanel(new MigLayout("fill", "[grow][300]", "[grow 70][grow]"));
		this.add(contentPanel, BorderLayout.CENTER); // Center the content panel

		centralTablePanel = new JPanel(new MigLayout("", "[386.00,grow]", "[30px][grow]"));
		contentPanel.add(centralTablePanel, "cell 0 0,grow");

		JPanel headerTablePanel = new JPanel(new MigLayout("", "[40px:70px][40px:70px][grow]", "[grow][]"));
		centralTablePanel.add(headerTablePanel, "cell 0 0,grow");

		JLabel lblNewLabel = new JLabel("List of Violations:");
		lblNewLabel.putClientProperty("FlatLaf.styleClass", "large");

		headerTablePanel.add(lblNewLabel, "cell 0 1 2 1");

		JScrollPane tableScrollPane = createTablePanel();
		centralTablePanel.add(tableScrollPane, "cell 0 1,grow");

		JPanel sideRPanel = new JPanel(new MigLayout("", "[center]", "[][grow][]"));
		contentPanel.add(sideRPanel, "cell 1 0 1 2,grow");

		JLabel lblNewLabel_1 = new JLabel("Appointments overview");
		lblNewLabel_1.putClientProperty("FlatLaf.styleClass", "large");

		sideRPanel.add(lblNewLabel_1, "cell 0 0");
		AppointmentDAO appointmentDAO = new AppointmentDAO(conn);
		AppointmentOverview appointmentOverview = new AppointmentOverview(appointmentDAO, conn);
		sideRPanel.add(appointmentOverview, "cell 0 1,grow");

		JPanel actionPanel = new JPanel(new MigLayout("fill", "[grow]", "[]"));
		contentPanel.add(actionPanel, "cell 0 1,grow");

		// Create panels for each section
		JPanel violationPanel = createActionPanel();

		actionPanel.add(violationPanel, "grow");

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
		String[] columnNames = { "LRN", "Full Name", "Violation Type", "Violation Status", "Actions" };

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
		table.getColumnModel().getColumn(2).setPreferredWidth(150); // Violation Type
		table.getColumnModel().getColumn(3).setPreferredWidth(100); // Violation Status
		table.getColumnModel().getColumn(4).setPreferredWidth(100); // Actions

		// Load violation data
		try {
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
			StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);

			List<ViolationRecord> violations = violationCRUD.getAllViolations();
			for (ViolationRecord violation : violations) {
				// Fetch participant details
				Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());

				// If participant is a student, fetch student details
				if (participant.getStudentUid() != null) {
					Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
					String fullName = String.format("%s %s", student.getStudentFirstname(),
							student.getStudentLastname());

					Object[] rowData = { student.getStudentLrn(), fullName, violation.getViolationType(),
							violation.getStatus(), "" // Actions column
					};
					model.addRow(rowData);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading violations: " + e.getMessage(), "Error",
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
			ViolationRecord violation = violationCRUD.getViolationByLRN(connection, lrn);

			if (violation != null) {
				// Create and show violation details panel
				JPanel violationDetailPanel = new ViolationFullData(violation, connection);

				// Show in dialog
				JDialog dialog = new JDialog();
				dialog.setTitle("Violation Details");
				dialog.setModal(true);
				dialog.setSize(800, 600);
				dialog.setLocationRelativeTo(null);
				dialog.getContentPane().add(violationDetailPanel);
				dialog.setVisible(true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error retrieving violation details: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void resolveViolation(String lrn) {
		try {
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			ViolationRecord violation = violationCRUD.getViolationByLRN(connection, lrn);

			if (violation != null) {
				int confirm = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to mark this violation as resolved?", "Confirm Resolution",
						JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION) {
					violationCRUD.updateViolationStatus(connection, violation.getViolationId(), "Resolved");
					refreshTable();
					contentPanel.revalidate();
					contentPanel.repaint();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error resolving violation: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void refreshTable() {
		// Remove the existing table from the centralTablePanel
		for (Component component : centralTablePanel.getComponents()) {
			if (component instanceof JScrollPane) {
				centralTablePanel.remove(component);
				break; // Exit after removing the first JScrollPane
			}
		}

		// Create and add the new table
		JScrollPane newTableScrollPane = createTablePanel();
		centralTablePanel.add(newTableScrollPane, "cell 0 1,grow");

		// Revalidate and repaint the panel
		centralTablePanel.revalidate();
		centralTablePanel.repaint();
	}

	private JPanel createActionPanel() {
		JPanel panel = new JPanel(new MigLayout("", "[grow][][grow][][grow][][grow]", "[][][][][]"));

		JLabel titleLabel = new JLabel("Management");
		titleLabel.putClientProperty("FlatLaf.styleClass", "h1");
		panel.add(titleLabel, "cell 0 0 4 1,growx,aligny center");

		JLabel violationLabel = new JLabel("Violation");
		violationLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(violationLabel, "cell 1 2,alignx center");

		JLabel appointmentLabel = new JLabel("Appointment");
		appointmentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(appointmentLabel, "cell 3 2,alignx center");

		JLabel studentLabel = new JLabel("Student");
		studentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(studentLabel, "cell 5 2,alignx center");

		FlatButton standardButton_1 = new FlatButton();
		standardButton_1.setText("View Violation");
		standardButton_1.setButtonType(ButtonType.none);
		panel.add(standardButton_1, "cell 1 3");

		FlatButton standardButton_2 = new FlatButton();
		standardButton_2.setText("Set Appointment");
		standardButton_2.setButtonType(ButtonType.none);
		panel.add(standardButton_2, "flowx,cell 3 3");

		FlatButton standardButton_3 = new FlatButton();
		standardButton_3.setText("View Appointments");
		standardButton_3.setButtonType(ButtonType.none);
		panel.add(standardButton_3, "cell 3 3");

		FlatButton standardButton_4 = new FlatButton();
		standardButton_4.setText("View Students");
		standardButton_4.setButtonType(ButtonType.none);
		panel.add(standardButton_4, "cell 5 3");
		return panel;
	}

}