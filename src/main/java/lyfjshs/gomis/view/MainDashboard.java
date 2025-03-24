package lyfjshs.gomis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
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

		contentPanel = new JPanel(new MigLayout("fill", "[grow][300]", "[grow][]"));
		this.add(contentPanel, BorderLayout.CENTER);

		// Left side with violations table
		centralTablePanel = new JPanel(new MigLayout("", "[grow]", "[30px][grow]"));
		contentPanel.add(centralTablePanel, "cell 0 0,grow");

		JPanel headerTablePanel = new JPanel(new MigLayout("", "[40px:70px][40px:70px][grow]", "[grow][]"));
		centralTablePanel.add(headerTablePanel, "cell 0 0,grow");

		JLabel lblNewLabel = new JLabel("List of Violations:");
		lblNewLabel.putClientProperty("FlatLaf.styleClass", "large");

		headerTablePanel.add(lblNewLabel, "cell 0 1 2 1");

		JScrollPane tableScrollPane = createTablePanel();
		centralTablePanel.add(tableScrollPane, "cell 0 1,grow");

		// Right side with appointments overview
		JPanel sideRPanel = new JPanel(new MigLayout("insets 0, gap 0", "[grow]", "[][grow]"));
		sideRPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground")));
		contentPanel.add(sideRPanel, "cell 1 0 1 2,grow");

		// Appointments header
		JPanel headerPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[]"));
		JLabel appointmentsLabel = new JLabel("Appointments");
		appointmentsLabel.putClientProperty("FlatLaf.styleClass", "h2");
		headerPanel.add(appointmentsLabel, "grow");
		sideRPanel.add(headerPanel, "growx, wrap");

		// Appointments content
		AppointmentDAO appointmentDAO = new AppointmentDAO(conn);
		AppointmentOverview appointmentOverview = new AppointmentOverview(appointmentDAO, conn);
		JScrollPane scrollPane = new JScrollPane(appointmentOverview);
		scrollPane.setBorder(null);
		sideRPanel.add(scrollPane, "grow");

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

		actionsColumn.addAction("Resolve", (t, row) -> {
			String lrn = (String) t.getValueAt(row, 0);
			resolveViolation(lrn);
		}, new Color(0, 150, 136), resolveIcon);

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
				if (participant != null && participant.getStudentUid() != null) {
					Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
					if (student != null) {
						String fullName = String.format("%s %s",
								student.getStudentFirstname() != null ? student.getStudentFirstname() : "",
								student.getStudentLastname() != null ? student.getStudentLastname() : "").trim();

						model.addRow(new Object[] { 
							student.getStudentLrn(), 
							fullName,
							violation.getViolationType(),  // Changed from student.getStudentSex()
							violation.getStatus(),         // Changed from student.getSchoolSection()
							"actions"                      // This column will be handled by TableActionManager
						});
					}
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
			ViolationCRUD violationCRUD = new ViolationCRUD(connection);
			ViolationRecord violation = violationCRUD.getViolationByLRN(lrn);

			if (violation != null) {
				// Create panel with proper styling
				JPanel detailPanel = new JPanel(new MigLayout("fillx, insets 20", "[30%][70%]", "[][][][][][][][]"));
				detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

				// Fetch related data
				ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
				StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
				Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
				Student student = participant != null && participant.getStudentUid() != null ? 
					studentsDataDAO.getStudentById(participant.getStudentUid()) : null;

				// Create header section
				JLabel headerLabel = new JLabel("Violation Information");
				headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
				detailPanel.add(headerLabel, "span, center, gapbottom 15");

				// Add student information
				if (student != null) {
					createSection(detailPanel, "Student Information", new String[][] {
						{"Student LRN:", student.getStudentLrn()},
						{"Student Name:", student.getStudentFirstname() + " " + student.getStudentLastname()}
					});
				}

				// Add violation information
				createSection(detailPanel, "Violation Details", new String[][] {
					{"Violation Type:", violation.getViolationType()},
					{"Description:", violation.getViolationDescription()},
					{"Status:", violation.getStatus()},
					{"Date:", violation.getUpdatedAt().toString()}
				});

				// Add text areas with improved styling
				createTextAreaField(detailPanel, "Anecdotal Record:", violation.getAnecdotalRecord(), 5);
				createTextAreaField(detailPanel, "Reinforcement:", violation.getReinforcement(), 5);

				// Configure modal options based on violation status
				SimpleModalBorder.Option[] options;
				if (!"Resolved".equals(violation.getStatus())) {
					options = new SimpleModalBorder.Option[] {
						new SimpleModalBorder.Option("Resolve", SimpleModalBorder.YES_OPTION),
						new SimpleModalBorder.Option("Close", SimpleModalBorder.NO_OPTION)
					};
				} else {
					options = new SimpleModalBorder.Option[] {
						new SimpleModalBorder.Option("Close", SimpleModalBorder.NO_OPTION)
					};
				}

				// Show modal with proper configuration
				ModalDialog.showModal(this,
					new SimpleModalBorder(detailPanel, "Violation Details", options,
						(controller, action) -> {
							if (action == SimpleModalBorder.YES_OPTION) {
								resolveViolation(lrn);
								controller.close();
							} else if (action == SimpleModalBorder.NO_OPTION || 
									 action == SimpleModalBorder.CLOSE_OPTION) {
								controller.close();
							}
							refreshTable();
						}),
					"ViolationDetails");

				// Configure modal appearance
				ModalDialog.getDefaultOption().getLayoutOption()
					.setSize(800, 600);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error retrieving violation details: " + e.getMessage());
		}
	}

	private void createSection(JPanel panel, String title, String[][] fields) {
		JLabel sectionLabel = new JLabel(title);
		sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		panel.add(sectionLabel, "span, gaptop 10, gapbottom 5");

		for (String[] field : fields) {
			addDetailField(panel, field[0], field[1]);
		}
	}

	private void createTextAreaField(JPanel panel, String label, String text, int rows) {
		JLabel fieldLabel = new JLabel(label);
		fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		panel.add(fieldLabel, "span, gaptop 10");

		JTextArea textArea = new JTextArea(text);
		textArea.setRows(rows);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setBackground(new Color(245, 245, 245));
		textArea.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(200, 200, 200)),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(0, rows * 20));
		panel.add(scrollPane, "span, growx, gapbottom 10");
	}

	private void addDetailField(JPanel panel, String label, String value) {
		panel.add(new JLabel(label), "cell 0 " + panel.getComponentCount()/2);
		panel.add(new JLabel(value != null ? value : ""), "cell 1 " + panel.getComponentCount()/2);
	}

	private void resolveViolation(String lrn) {
		try {
			int option = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to resolve this violation?",
				"Confirm Resolution",
				JOptionPane.YES_NO_OPTION);
				
			if (option == JOptionPane.YES_OPTION) {
				ViolationCRUD violationCRUD = new ViolationCRUD(connection);
				ViolationRecord violation = violationCRUD.getViolationByLRN(lrn);
				
				if (violation != null) {
					boolean success = violationCRUD.updateViolationStatus(violation.getViolationId(), "Resolved");
					if (success) {
						JOptionPane.showMessageDialog(this, "Violation has been resolved successfully!");
						refreshTable();
					} else {
						JOptionPane.showMessageDialog(this, "Failed to resolve violation.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error resolving violation: " + e.getMessage());
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
		JPanel panel = new JPanel(new MigLayout("", "[grow][][grow][][grow][][grow][][grow]", "[][][][][][]"));

		JLabel violationLabel = new JLabel("Session Form");
		violationLabel.putClientProperty("FlatLaf.styleClass", "large");

		JLabel titleLabel = new JLabel("Management");
		titleLabel.putClientProperty("FlatLaf.styleClass", "h1");
		panel.add(titleLabel, "cell 0 0 9 1,growx,aligny center");

		JLabel lblNewLabel_1 = new JLabel("Incident Forms");
		panel.add(lblNewLabel_1, "cell 1 2,alignx center");
		panel.add(violationLabel, "cell 3 2,alignx center");

		JLabel appointmentLabel = new JLabel("Appointment");
		appointmentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(appointmentLabel, "cell 5 2,alignx center");

		JLabel studentLabel = new JLabel("Student");
		studentLabel.putClientProperty("FlatLaf.styleClass", "large");
		panel.add(studentLabel, "cell 7 2,alignx center");

		FlatButton standardButton_1_1 = new FlatButton();
		standardButton_1_1.setText("Print INITIAL Incident");
		standardButton_1_1.setButtonType(ButtonType.none);
		panel.add(standardButton_1_1, "flowx,cell 1 3,growx");

		FlatButton standardButton_1 = new FlatButton();
		standardButton_1.setText("Create a Session");
		standardButton_1.setButtonType(ButtonType.none);
		panel.add(standardButton_1, "cell 3 3,growx");

		FlatButton standardButton_2 = new FlatButton();
		standardButton_2.setText("Set Appointment");
		standardButton_2.setButtonType(ButtonType.none);
		panel.add(standardButton_2, "flowx,cell 5 3,growx");

		FlatButton standardButton_4 = new FlatButton();
		standardButton_4.setText("View Students");
		standardButton_4.setButtonType(ButtonType.none);
		panel.add(standardButton_4, "flowx,cell 7 3,growx");

		JButton btnNewButton = new JButton("Create a Incident Record");
		panel.add(btnNewButton, "cell 1 4");

		FlatButton standardButton_3 = new FlatButton();
		standardButton_3.setText("View Appointments");
		standardButton_3.setButtonType(ButtonType.none);
		panel.add(standardButton_3, "cell 5 4,growx");

		JButton btnNewButton_1 = new JButton("Search Student");
		panel.add(btnNewButton_1, "cell 7 4");
		return panel;
	}

}