package lyfjshs.gomis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import javax.swing.BorderFactory;
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

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.utils.IncidentReportGenerator;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.appointment.AppointmentOverview;
import lyfjshs.gomis.view.appointment.add.AddAppointmentModal;
import lyfjshs.gomis.view.appointment.add.AddAppointmentPanel;
import lyfjshs.gomis.view.incident.IncidentFillUpForm;
import lyfjshs.gomis.view.sessions.SessionsForm;
import lyfjshs.gomis.view.students.StudentFullData;
import lyfjshs.gomis.view.students.StudentMangementGUI;
import lyfjshs.gomis.view.students.StudentSearchPanel;
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

	FlatSVGIcon viewIcon = new FlatSVGIcon("icons/view.svg", 0.4f);
	FlatSVGIcon resolveIcon = new FlatSVGIcon("icons/resolve.svg", 0.4f);

	private JScrollPane createTablePanel() {
		String[] columnNames = { "LRN", "Full Name", "Violation Type", "Status", "Actions" }; // Changed "Violation
																								// Status" to "Status"
		Class<?>[] columnTypes = { String.class, String.class, String.class, String.class, Object.class };
		boolean[] editableColumns = { false, false, false, false, true };
		double[] columnWidths = { 0.15, 0.20, 0.17, 0.10, 0.38 }; // Adjusted widths to ensure total is 1 and "Actions"
																	// column is wide enough
		int[] alignments = { SwingConstants.CENTER, // LRN
				SwingConstants.LEFT, // Full Name
				SwingConstants.LEFT, // Violation Type
				SwingConstants.CENTER, // Violation Status
				SwingConstants.CENTER // Actions
		};

		TableActionManager actionsColumn = new DefaultTableActionManager();
		((DefaultTableActionManager)actionsColumn)
			.addAction("View", (t, row) -> {
				String lrn = (String) t.getValueAt(row, 0);
				showViolationDetails(lrn);
			}, new Color(0, 150, 136), viewIcon)
			.addAction("Resolve", (t, row) -> {
				String lrn = (String) t.getValueAt(row, 0);
				resolveViolation(lrn);
			}, new Color(0, 150, 136), resolveIcon);

		Object[][] initialData = new Object[0][columnNames.length];
		GTable table = new GTable(initialData, columnNames, columnTypes, editableColumns, columnWidths, alignments,
				false, // No checkbox column
				actionsColumn);
		// Load violation data - Modified to only show active violations
		try {
			ViolationDAO ViolationDAO = new ViolationDAO(connection);
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
			StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);

			List<Violation> violations = ViolationDAO.getAllViolations();
			System.out.println("Found " + violations.size() + " violations"); // Debug line

			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setRowCount(0); // Clear existing rows

			for (Violation violation : violations) {
				// Debug information
				System.out.println("\nProcessing violation:" +
						"\nID: " + violation.getViolationId() +
						"\nType: " + violation.getViolationType() +
						"\nStatus: " + violation.getStatus() +
						"\nParticipantID: " + violation.getParticipantId());

				// Only process active violations
				if ("Active".equals(violation.getStatus())) {
					Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
					if (participant != null) {
						System.out.println("Found participant:" +
								"\nName: " + participant.getParticipantFirstName() + " "
								+ participant.getParticipantLastName() +
								"\nType: " + participant.getParticipantType() +
								"\nStudentUID: " + participant.getStudentUid());

						if (participant.getStudentUid() != null && participant.getStudentUid() > 0) {
							Student student = studentsDataDAO.getStudentById(participant.getStudentUid());
							if (student != null) {
								String fullName = String.format("%s %s",
										student.getStudentFirstname() != null ? student.getStudentFirstname() : "",
										student.getStudentLastname() != null ? student.getStudentLastname() : "")
										.trim();

								model.addRow(new Object[] {
										student.getStudentLrn(),
										fullName,
										violation.getViolationType(),
										violation.getStatus(),
										"actions"
								});
							} else {
								System.out.println("ERROR: Student not found in database for StudentUID: "
										+ participant.getStudentUid());
							}
						} else {
							System.out.println("ERROR: Invalid StudentUID (null or 0) for participant");
						}
					} else {
						System.out.println("No participant found for ID: " + violation.getParticipantId());
					}
				} else {
					System.out.println("Skipping non-active violation");
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
			ViolationDAO ViolationDAO = new ViolationDAO(connection);
			Violation violation = ViolationDAO.getViolationByLRN(lrn);

			if (violation != null) {
				// Create panel with proper styling
				JPanel detailPanel = new JPanel(new MigLayout("fillx, insets 20", "[30%][70%]", "[][][][][][][][]"));
				detailPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

				// Fetch related data
				ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
				StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
				Participants participant = participantsDAO.getParticipantById(violation.getParticipantId());
				Student student = participant != null && participant.getStudentUid() != null
						? studentsDataDAO.getStudentById(participant.getStudentUid())
						: null;

				// Create header section
				JLabel headerLabel = new JLabel("Violation Information");
				headerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
				detailPanel.add(headerLabel, "span, center, gapbottom 15");

				// Add student information
				if (student != null) {
					createSection(detailPanel, "Student Information", new String[][] {
							{ "Student LRN:", student.getStudentLrn() },
							{ "Student Name:", student.getStudentFirstname() + " " + student.getStudentLastname() }
					});
				}

				// Add violation information
				createSection(detailPanel, "Violation Details", new String[][] {
						{ "Violation Type:", violation.getViolationType() },
						{ "Description:", violation.getViolationDescription() },
						{ "Status:", violation.getStatus() },
						{ "Date:", violation.getUpdatedAt().toString() }
				});

				// Add text areas with improved styling
				createTextAreaField(detailPanel, "Session Summary:", violation.getSessionSummary(), 5);
				createTextAreaField(detailPanel, "Reinforcement:", violation.getReinforcement(), 5);

				// Add resolution status message if resolved
				if ("Resolved".equals(violation.getStatus())) {
					JLabel resolutionLabel = new JLabel("This violation has been resolved.");
					resolutionLabel.setForeground(new Color(0, 150, 136)); // Green color
					resolutionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
					detailPanel.add(resolutionLabel, "span, gaptop 10, gapbottom 5");
				}

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
		panel.add(new JLabel(label), "cell 0 " + panel.getComponentCount() / 2);
		panel.add(new JLabel(value != null ? value : ""), "cell 1 " + panel.getComponentCount() / 2);
	}

	private void resolveViolation(String lrn) {
		try {
			ViolationDAO violationDAO = new ViolationDAO(connection);
			Violation violation = violationDAO.getViolationByLRN(lrn);

			if (violation != null) {
				// Check if violation is already resolved
				if ("Resolved".equals(violation.getStatus())) {
					JOptionPane.showMessageDialog(this,
							"This violation has already been resolved.",
							"Already Resolved",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				int option = JOptionPane.showConfirmDialog(this,
						"Are you sure you want to resolve this violation?",
						"Confirm Resolution",
						JOptionPane.YES_NO_OPTION);

				if (option == JOptionPane.YES_OPTION) {
					boolean success;
					try {
						success = violationDAO.updateViolationStatus(violation.getViolationId(), "Resolved");
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
					}
					if (success) {
						JOptionPane.showMessageDialog(this,
								"Violation has been resolved successfully!",
								"Success",
								JOptionPane.INFORMATION_MESSAGE);
						refreshTable(); // Refresh table after resolving
					} else {
						JOptionPane.showMessageDialog(this,
								"Failed to resolve violation.",
								"Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Violation not found.",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error resolving violation: " + e.getMessage(),
					"Error",
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

		FlatButton printINITIAL = new FlatButton();
		printINITIAL.setText("Print INITIAL Incident");
		printINITIAL.setButtonType(ButtonType.none);
		printINITIAL.addActionListener(e -> printInitialForm());
		panel.add(printINITIAL, "flowx,cell 1 3,growx");

		FlatButton createSessionBtn = new FlatButton();
		createSessionBtn.setText("Create a Session");
		createSessionBtn.setButtonType(ButtonType.none);
		createSessionBtn.addActionListener(e -> SessionForm());
		panel.add(createSessionBtn, "cell 3 3,growx");

		FlatButton setAppointment = new FlatButton();
		setAppointment.setText("Set Appointment");
		setAppointment.setButtonType(ButtonType.none);
		setAppointment.addActionListener(e -> createAppointment());
		panel.add(setAppointment, "flowx,cell 5 3,growx");

		FlatButton createIncident = new FlatButton();
		createIncident.setText("Create a Incident Record");
		createIncident.setButtonType(ButtonType.none);
		createIncident.addActionListener(e -> createIncidentRecord());
		panel.add(createIncident, "cell 1 4");

		FlatButton viewAppointments = new FlatButton();
		viewAppointments.setText("View Appointments");
		viewAppointments.setButtonType(ButtonType.none);
		viewAppointments.addActionListener(e -> viewAppointments());
		panel.add(viewAppointments, "cell 5 4,growx");

		FlatButton viewStudents = new FlatButton();
		viewStudents.setText("View Students");
		viewStudents.setButtonType(ButtonType.none);
		viewStudents.addActionListener(e -> {
			StudentMangementGUI studManagement = new StudentMangementGUI(connection);
			FormManager.showForm(studManagement);
		});
		panel.add(viewStudents, "flowx,cell 7 3,growx");

		FlatButton searchStudent = new FlatButton();
		searchStudent.setText("Search Student");
		searchStudent.setButtonType(ButtonType.none);
		searchStudent.addActionListener(e -> {
			if (ModalDialog.isIdExist("search")) {
				return;
			}
			StudentSearchPanel searchPanel = new StudentSearchPanel(connection, student -> {
				// Callback when student is selected show
				// its full details using StudentFullData.java
				StudentFullData studentFullData = new StudentFullData(connection, student);
				FormManager.showForm(studentFullData);

				ModalDialog.closeModal("search");
			}, "search") {
				@Override
				protected void onStudentSelected(Student student) {
					// Optional: Implement any specific behavior here
					// This method is called when a student is selected
				}
			};

			ModalDialog.getDefaultOption().setOpacity(0.5f).setAnimationOnClose(true).getBorderOption()
					.setBorderWidth(0.5f)
					.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

			ModalDialog.showModal(this, searchPanel, "search");
			ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 500);
		});

		panel.add(searchStudent, "cell 7 4");
		return panel;
	}

	private void SessionForm() {
		try {
			SessionsForm sessionsForm = new SessionsForm(connection);
			FormManager.showForm(sessionsForm);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error opening Sessions form: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void viewAppointments() {
		try {
			AppointmentManagement appointments = new AppointmentManagement(connection);
			FormManager.showForm(appointments);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
					"Error opening Appointments view: " + e.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void createAppointment() {
		Appointment newAppointment = new Appointment();
		newAppointment.setAppointmentDateTime(Timestamp.valueOf(LocalDateTime.now())); // Set default date and time
		AppointmentDAO appointmentDAO = Main.appointmentCalendar.appointmentDAO;

		// Set the guidanceCounselorId from FormManager
		newAppointment.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());

		AddAppointmentPanel addAppointmentPanel = new AddAppointmentPanel(newAppointment, appointmentDAO, connection);

		// Use AddAppointmentModal to show the dialog with correct size
		AddAppointmentModal.getInstance().showModal(connection,
				this,
				addAppointmentPanel,
				appointmentDAO,
				800,  // Increased width
				600,  // Increased height
				() -> Main.appointmentCalendar.refreshViews());
	}

	private void printInitialForm() {
		IncidentReportGenerator.createINITIALIncidentReport(this);
	}

	private void createIncidentRecord() {
		IncidentFillUpForm incidentFillUp = new IncidentFillUpForm(connection);
		FormManager.showForm(incidentFillUp);
	}

	// Add method to refresh table when dashboard is shown
	@Override
	public void formRefresh() {
		super.formRefresh();
		refreshTable();
	}
}