package lyfjshs.gomis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.ViolationRecord;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.appointment.AppointmentOverview;
import lyfjshs.gomis.view.violation.ViolationFullData;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class MainDashboard extends Form {

	private Connection connection;
	private JPanel contentPanel;
	private JPanel centralTablePanel;

	public MainDashboard(Connection conn) {
		this.connection = conn;
		this.setLayout(new BorderLayout());

		contentPanel = new JPanel(new MigLayout("fill", "[grow][290]", "[grow 70][grow]"));
		this.add(contentPanel, BorderLayout.CENTER); // Center the content panel

		centralTablePanel = new JPanel(new MigLayout("", "[grow]", "[30px][grow]"));
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

		refreshTable();
	}

	FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.5f);
	FlatSVGIcon resolveIcon = new FlatSVGIcon("icons/resolve.svg", 0.5f);

	private JScrollPane createTablePanel() {
		String[] columnNames = { "LRN", "Full Name", "Violation Type", "Violation Status", "Actions" };
		Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, Object.class };
		boolean[] editableColumns = { false, false, false, false, true };
		double[] columnWidths = { 0.15, 0.30, 0.19, 0.19, 0.18 };
		int[] alignments = { SwingConstants.CENTER, // LRN
				SwingConstants.LEFT, // Full Name
				SwingConstants.LEFT, // Violation Type
				SwingConstants.CENTER, // Violation Status
				SwingConstants.CENTER // Actions
		};

		TableActionManager actionsColumn = new TableActionManager();
		actionsColumn.addAction("View", (t, row) -> {
			String lrn = (String) t.getValueAt(row, 0);
			showViolationDetails(lrn);
		}, new Color(0, 150, 136), viewIcon);

//		actionsColumn.addAction("Resolve", (t, row) -> {
//			String lrn = (String) t.getValueAt(row, 0);
//			resolveViolation(lrn);
//		}, new Color(0, 150, 136), resolveIcon);

		Object[][] initialData = new Object[0][columnNames.length];
		GTable table = new GTable(initialData, columnNames, columnTypes, editableColumns, columnWidths, alignments,
				false, // No checkbox column
				actionsColumn);

		// Load violation data
		try {
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
			StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);

			List<ViolationRecord> violations = violationCRUD.getAllViolations();
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			for (ViolationRecord violation : violations) {
				Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
				if (participant.getStudentUid() != null) {
					Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
					String fullName = String.format("%s %s", student.getStudentFirstname(),
							student.getStudentLastname());
					model.addRow(new Object[] { student.getStudentLrn(), fullName, violation.getViolationType(),
							violation.getStatus(), null // Actions column handled by TableActionManager
					});
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading violations: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		return new JScrollPane(table);
	}

	private void showViolationDetails(String lrn) {
		try {
			// Get violation data
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			ViolationRecord violation = violationCRUD.getViolationByLRN(lrn);

			if (violation != null) {
				// Create and show violation details panel
				JPanel violationDetailPanel = new ViolationFullData(violation, connection);

				ModalDialog.showModal(this,
						new SimpleModalBorder(violationDetailPanel, "Violation Details", new SimpleModalBorder.Option[] {
							new SimpleModalBorder.Option("View in Violation Record", SimpleModalBorder.YES_OPTION),
						}, (controller, action) -> {
							if (action == SimpleModalBorder.YES_OPTION) {
								controller.consume();
								// actions todo next after Confirm
							} else if (action == SimpleModalBorder.NO_OPTION|| action == SimpleModalBorder.CLOSE_OPTION
									|| action == SimpleModalBorder.CANCEL_OPTION) {
								controller.close();
								refreshTable();
								// actions todo next after Close or Cancel
							}
						}),
						"ViolationDetails");
					// set size of modal dialog to 800x800
					ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 800);
			} else if (violation == null) {
				JOptionPane.showMessageDialog(this, "Violation not found", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error retrieving violation details: " + e.getMessage(), "Error",
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

		JLabel violationLabel = new JLabel("Create a Session");
		violationLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(violationLabel, "cell 1 2,alignx center");

		JLabel appointmentLabel = new JLabel("Appointment");
		appointmentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(appointmentLabel, "cell 3 2,alignx center");

		JLabel studentLabel = new JLabel("Student");
		studentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(studentLabel, "cell 5 2,alignx center");

		FlatButton standardButton_1 = new FlatButton();
		standardButton_1.setText("Session Form");
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