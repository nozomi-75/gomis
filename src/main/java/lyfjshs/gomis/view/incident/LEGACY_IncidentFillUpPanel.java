// LEGACY: This file is now replaced by IncidentFillUpFormPanel and modular subpanels.
package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import docPrinter.templateManager;
import docPrinter.incidentReport.incidentReportGenerator;
import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.utils.ErrorDialogUtils;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.utils.ValidationUtils;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.TempIncidentParticipant;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class LEGACY_IncidentFillUpPanel extends Form {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(LEGACY_IncidentFillUpPanel.class);
	private JFormattedTextField DateField;
	private JTextArea narrativeReportField;
	private JTextArea actionsTakenField;
	private JTextArea recommendationsField;
	private JTextField reportedByField;
	private JFormattedTextField TimeField;
	private JTextField GradeSectionField;
	private Connection conn;
	private JTable table;
	private JPanel detailsPanel;
	private DatePicker datePicker;
	private TimePicker timePicker;

	// Add these fields at the class level
	private JTextField firstNameField, lastNameField, contactNumberField;
	private JComboBox<String> sexCBox, participantsComboBox;
	private DefaultTableModel participantTableModel;
	private Map<Integer, Map<String, String>> participantDetails = new HashMap<>();
	private ParticipantsDAO participantsDAO;
	private Student reporterStudent;
	private JComboBox<String> statusCombo;
	private List<TempIncidentParticipant> tempIncidentParticipants = new ArrayList<>();
	private Integer tempIdCounter = -1;

	public LEGACY_IncidentFillUpPanel(Connection connectDB) {
		this.conn = connectDB;
		
		// Initialize DatePicker and TimePicker
		datePicker = new DatePicker();
		timePicker = new TimePicker();
		
		setLayout(new MigLayout("", "[grow][grow]", "[38px][][200px][200px][170px][pref]"));

		// Header
		JPanel headerPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
		JLabel lblTitle = new JLabel("INCIDENT Fill-Up Form");
		lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
		headerPanel.add(lblTitle, "grow");
		add(headerPanel, "cell 0 0 2 1,grow");

		detailsPanel = new JPanel(new MigLayout("", "[][grow 80][][][][grow]", "[][][]"));
		detailsPanel
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
						"INCIDENT DETAILS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12), new Color(100, 149, 177)));

		JLabel label_1 = new JLabel("Reported By:");
		detailsPanel.add(label_1, "cell 0 0,alignx left");
		detailsPanel.add(reportedByField = new JTextField(), "cell 1 0,growx");

		JLabel label_2 = new JLabel("Date: ");
		detailsPanel.add(label_2, "cell 4 0");
		DateField = new JFormattedTextField();
		datePicker.setSelectedDate(LocalDate.now());
		datePicker.setEditor(DateField);
		DateField.setColumns(10);
		detailsPanel.add(DateField, "cell 5 0,grow");

		detailsPanel.add(new JLabel("Grade & Section: "), "cell 0 1,alignx left");
		detailsPanel.add(GradeSectionField = new JTextField(), "cell 1 1,growx");

		detailsPanel.add(new JLabel("Time: "), "cell 4 1,alignx left");
		TimeField = new JFormattedTextField();
		timePicker.setSelectedTime(LocalTime.now());
		timePicker.setEditor(TimeField);
		TimeField.setColumns(10);
		detailsPanel.add(TimeField, "cell 5 1,growx");

		// Add status combo box
		detailsPanel.add(new JLabel("Status: "), "cell 4 2,alignx trailing");
		statusCombo = new JComboBox<>(new String[]{"Select Status", "Active", "Ended"});
		statusCombo.setSelectedItem("Select Status");
		detailsPanel.add(statusCombo, "cell 5 2,growx");

		add(detailsPanel, "cell 0 1 2 1,grow");

		// Narrative Report Panel
		JPanel narrativePanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		narrativePanel.add(new JLabel("Narrative Report"), "cell 0 0");
		narrativeReportField = new JTextArea();
		narrativeReportField.setLineWrap(true);
		narrativeReportField.setWrapStyleWord(true);
		JScrollPane narrativeScrollPane = new JScrollPane(narrativeReportField);
		narrativeScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(100, 149, 177)), "Narrative Report:",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 17)));
		add(narrativeScrollPane, "cell 0 2 2 1,grow");

		// Actions Taken Panel
		JPanel actionsPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		actionsPanel.add(new JLabel("Action Taken"), "cell 0 0");
		actionsTakenField = new JTextArea();
		actionsTakenField.setLineWrap(true);
		actionsTakenField.setWrapStyleWord(true);
		JScrollPane actionsScrollPane = new JScrollPane(actionsTakenField);
		actionsScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(100, 149, 177)), "Action Taken:",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 17)));
		add(actionsScrollPane, "cell 0 3,grow");

		// Recommendations Panel
		JPanel recommendationsPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		recommendationsPanel.add(new JLabel("Recommendation"), "cell 0 0");
		recommendationsField = new JTextArea();
		recommendationsField.setLineWrap(true);
		recommendationsField.setWrapStyleWord(true);
		JScrollPane recommendationsScrollPane = new JScrollPane(recommendationsField);
		recommendationsScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(100, 149, 177)), "Recommendation:",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Tahoma", Font.BOLD, 17)));
		add(recommendationsScrollPane, "cell 1 3,grow");

		initializeComponents();
		setupParticipantPanel();
		setupDetailsPanel();

		// Footer Panel
		JPanel footPanel = new JPanel(new MigLayout("", "[grow,fill][center]", "[]"));
		add(footPanel, "cell 0 5 2 1,grow");

		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(e -> saveIncidentReportAsync(submitButton));
		footPanel.add(submitButton, "cell 1 0");

		JButton printBtn = new JButton("Print INTIAL Report");
		printBtn.addActionListener( e -> printIncidentReport());
		detailsPanel.add(printBtn, "cell 0 2");

	}

	private void initializeComponents() {
		// Initialize the table model with proper columns
		participantTableModel = new DefaultTableModel(
				new Object[] { "#", "Participant Name", "Participant Type", "Actions" }, 0);

		participantsDAO = new ParticipantsDAO(conn);

		// Initialize status combo box with values matching SessionsFillUpFormPanel
		statusCombo = new JComboBox<>(new String[]{"Select Status", "Active", "Ended"});
		statusCombo.setSelectedItem("Select Status");
	}

	private void setupParticipantPanel() {
		// Create participant panel with proper styling
		JPanel participantPanel = new JPanel(new MigLayout("gap 10", "[][grow][][grow]", "[][][][]"));
		participantPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")), "Add Participant",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 12),
				UIManager.getColor("Label.foreground")));

		// Participant Type Selector
		JLabel participantLabel = new JLabel("Participant Type: ");
		participantPanel.add(participantLabel, "cell 0 0");
		participantsComboBox = new JComboBox<>(new String[] { "SELECT PARTICIPANT", "Student", "Non-Student" });
		participantPanel.add(participantsComboBox, "cell 1 0,growx");

		// Student Search Button for participants
		JButton studentParticipantSearchButton = new JButton("Search Student Participant");
		studentParticipantSearchButton.setEnabled(false);
		participantPanel.add(studentParticipantSearchButton, "cell 2 0 2 1");

		// Add action listener to the Student Participant Search button
		studentParticipantSearchButton.addActionListener(e -> openStudentParticipantSearchUI());

		// Name Fields
		participantPanel.add(new JLabel("First Name"), "cell 0 1");
		firstNameField = new JTextField();
		participantPanel.add(firstNameField, "cell 1 1,growx");

		participantPanel.add(new JLabel("Last Name"), "cell 2 1");
		lastNameField = new JTextField();
		participantPanel.add(lastNameField, "cell 3 1,growx");

		// Sex and Contact Fields
		participantPanel.add(new JLabel("Sex: "), "cell 0 2");
		sexCBox = new JComboBox<>(new String[] { "Male", "Female" });
		participantPanel.add(sexCBox, "cell 1 2,growx");

		participantPanel.add(new JLabel("Contact Number"), "cell 2 2");
		contactNumberField = new JTextField();
		participantPanel.add(contactNumberField, "cell 3 2,growx");

		// Add Participant Button
		JButton addButton = new JButton("Add Participant");
		addButton.addActionListener(e -> addParticipant());
		participantPanel.add(addButton, "cell 3 3,alignx center");

		// Add listener for participant type selection
		participantsComboBox.addActionListener(e -> {

			studentParticipantSearchButton.setEnabled("Student".equals(participantsComboBox.getSelectedItem()));
		});

		// Replace existing participant panel
		add(participantPanel, "cell 0 4,grow");

		// Setup participant table
		setupParticipantTable();
	}

	private void setupParticipantTable() {
		JPanel listPanel = new JPanel(new MigLayout("", "[grow]", "[grow]"));
		listPanel.setBorder(BorderFactory.createTitledBorder("List of Participants"));

		table = new JTable(participantTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(400, 100));

		JScrollPane scrollPane = new JScrollPane(table);
		listPanel.add(scrollPane, "grow");

		// Add mouse listener for actions
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());

				if (col == 3 && row >= 0) { // Actions column
					handleTableAction(row, e.getX() - table.getCellRect(row, col, false).x);
				}
			}
		});

		add(listPanel, "cell 1 4,grow");
	}

	private void handleTableAction(int row, int xOffset) {
		int viewWidth = 60; // Approximate width of "View" button
		int participantId = (int) table.getValueAt(row, 0);

		if (xOffset <= viewWidth) {
			showParticipantDetails(participantId);
		} else {
			removeParticipant(row, participantId);
		}
	}

	private void addParticipant() {
		String firstName = firstNameField.getText().trim();
		String lastName = lastNameField.getText().trim();
		String contact = contactNumberField.getText().trim();
		String sex = (String) sexCBox.getSelectedItem();
		String participantType = (String) participantsComboBox.getSelectedItem();

		// Validate required fields
		if (firstName.isEmpty() || lastName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter at least first and last name", "Missing Information",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Create new TempIncidentParticipant (DO NOT save to DB immediately)
		TempIncidentParticipant newParticipant = new TempIncidentParticipant(
			null, // studentUid is null for non-students
			firstName,
			lastName,
			participantType,
			sex,
			contact,
			false // isStudent
		);

		// Check for duplicates in the temporary list
		boolean isDuplicate = tempIncidentParticipants.stream().anyMatch(p ->
			(!p.isStudent() && p.getFullName().equalsIgnoreCase(newParticipant.getFullName()))
		);

		if (isDuplicate) {
			JOptionPane.showMessageDialog(this,
				"This non-student participant is already added to the incident.",
				"Duplicate Entry",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

		tempIncidentParticipants.add(newParticipant); // Add to the temporary list
		updateIncidentParticipantsTable(); // Refresh the table from the temporary list

		// Clear input fields
		clearParticipantFields();
	}

	private void removeParticipant(int row, int participantId) {
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this participant?",
				"Confirm Remove", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			// Find the TempIncidentParticipant to remove based on its ID
			TempIncidentParticipant participantToRemove = null;
			for (TempIncidentParticipant tip : tempIncidentParticipants) {
				if (tip.getParticipantId() != null && tip.getParticipantId().equals(participantId)) {
					participantToRemove = tip;
					break;
				}
			}

			if (participantToRemove != null) {
				tempIncidentParticipants.remove(participantToRemove);
				updateIncidentParticipantsTable(); // Refresh the table from the temporary list
			} else {
				logger.warn("Attempted to remove participant with ID " + participantId + ", but not found in tempIncidentParticipants.");
			}
		}
	}

	private void updateIncidentParticipantsTable() {
		participantTableModel.setRowCount(0);
		int rowNumber = 1;

		for (TempIncidentParticipant participant : tempIncidentParticipants) {
			Integer idToUseInTable = participant.getParticipantId();

			if (idToUseInTable == null || idToUseInTable < 0) { // Check for null or existing negative temporary IDs
				idToUseInTable = tempIdCounter--; // Assign a new temporary negative ID
				participant.setParticipantId(idToUseInTable); // Store temporary ID in TempIncidentParticipant
			}

			// Store details in participantDetails for ALL participants, using the ID used in the table
			Map<String, String> details = new HashMap<>();
			details.put("firstName", participant.getFirstName());
			details.put("lastName", participant.getLastName());
			details.put("fullName", participant.getFullName());
			details.put("type", participant.getType());
			details.put("contact", participant.getContactNumber());
			details.put("sex", participant.getSex());
			if (participant.isStudent() && participant.getStudentUid() != null) {
				details.put("studentUid", String.valueOf(participant.getStudentUid()));
			}
			participantDetails.put(idToUseInTable, details);

			participantTableModel.addRow(new Object[] { rowNumber++, participant.getFullName(), participant.getType(),
					"View | Remove", idToUseInTable });
		}
		// Hide ID column
		if (table.getColumnCount() > 4) {
			table.getColumnModel().removeColumn(table.getColumnModel().getColumn(4));
		}
	}

	private void clearParticipantFields() {
		firstNameField.setText("");
		lastNameField.setText("");
		contactNumberField.setText("");
		sexCBox.setSelectedIndex(0);
		participantsComboBox.setSelectedIndex(0);
	}

	private int getOrCreateParticipant() throws SQLException {
		if (reporterStudent == null) {
			throw new IllegalArgumentException("Please select a reporter student first");
		}

		ParticipantsDAO participantsDAO = new ParticipantsDAO(conn);

		// Create participant from reporter student
		Participants participant = new Participants();
		participant.setStudentUid(reporterStudent.getStudentUid());
		participant.setParticipantType("Reporter");
		participant.setParticipantLastName(reporterStudent.getStudentLastname());
		participant.setParticipantFirstName(reporterStudent.getStudentFirstname());
		participant.setSex(reporterStudent.getStudentSex());
		
		// Safely get contact number
		String contactNumber = "";
		if (reporterStudent.getContact() != null) {
			contactNumber = reporterStudent.getContact().getContactNumber();
		}
		participant.setContactNumber(contactNumber);

		return participantsDAO.createParticipant(participant);
	}

	private void saveIncidentReportAsync(JButton saveButton) {
		saveButton.setEnabled(false);
		new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() {
				try {
					return saveIncidentReport();
				} catch (Exception e) {
					ErrorDialogUtils.showError(LEGACY_IncidentFillUpPanel.this, "Error: " + e.getMessage());
					return false;
				}
			}
			@Override
			protected void done() {
				saveButton.setEnabled(true);
				try {
					boolean success = get();
					if (success) {
						// Success toast or dialog can be shown here if needed
					}
				} catch (Exception e) {
					ErrorDialogUtils.showError(LEGACY_IncidentFillUpPanel.this, "Error: " + e.getMessage());
				}
			}
		}.execute();
	}

	private boolean saveIncidentReport() {
		try {
			// Validate required fields
			if (!validateFields()) {
				return false;
			}
			// Validate reporter student
			if (reporterStudent == null) {
				ErrorDialogUtils.showError(this, "Please select a reporter student first");
				return false;
			}
			IncidentsDAO incidentsDAO = new IncidentsDAO(conn);
			ViolationDAO violationDAO = new ViolationDAO(conn);
			// Create new incident
			Incident incident = new Incident();
			incident.setParticipantId(getOrCreateParticipant()); // Reporter participant ID
			incident.setIncidentDate(new java.sql.Timestamp(System.currentTimeMillis()));
			incident.setStatus((String) statusCombo.getSelectedItem());
			incident.setIncidentDescription(narrativeReportField.getText());
			incident.setActionTaken(actionsTakenField.getText());
			incident.setRecommendation(recommendationsField.getText());
			incident.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
			// Save incident to database
			int incidentId = incidentsDAO.createIncident(incident);
			if (incidentId != -1) {
				// Create violation records for each participant
				for (TempIncidentParticipant tempParticipant : tempIncidentParticipants) {
					Violation violation = new Violation();
					violation.setParticipantId(tempParticipant.getParticipantId());
					violation.setViolationType(narrativeReportField.getText());
					violation.setViolationDescription(narrativeReportField.getText());
					violation.setSessionSummary(narrativeReportField.getText());
					violation.setReinforcement(recommendationsField.getText());
					violation.setStatus("Active");
					violation.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
					int violationId = violationDAO.createViolation(violation);
					if (violationId != -1) {
						String linkSql = "INSERT INTO INCIDENT_VIOLATIONS (INCIDENT_ID, VIOLATION_ID) VALUES (?, ?)";
						try (PreparedStatement pstmt = conn.prepareStatement(linkSql)) {
							pstmt.setInt(1, incidentId);
							pstmt.setInt(2, violationId);
							pstmt.executeUpdate();
						}
					}
				}
				// Publish event for incident creation
				EventBus.publish("incidentCreated", incident);
				clearForm();
				return true;
			} else {
				ErrorDialogUtils.showError(this, "Failed to save incident report.");
				return false;
			}
		} catch (IllegalArgumentException ex) {
			ErrorDialogUtils.showError(this, "Validation error: " + ex.getMessage());
			return false;
		} catch (SQLException ex) {
			logger.error("Error saving incident: " + ex.getMessage(), ex);
			ErrorDialogUtils.showError(this, "Error saving incident: " + ex.getMessage());
			return false;
		} catch (Exception ex) {
			logger.error("Error processing incident form", ex);
			ErrorDialogUtils.showError(this, "Error processing incident form: " + ex.getMessage());
			return false;
		}
	}

	private boolean validateFields() {
		// Use ValidationUtils for field checks where possible
		if (reporterStudent == null || ValidationUtils.isFieldEmpty(reportedByField)) {
			ErrorDialogUtils.showError(this, "Please select a reporter for this incident");
			return false;
		}
		String selectedStatus = (String) statusCombo.getSelectedItem();
		if (selectedStatus == null || "Select Status".equals(selectedStatus)) {
			ErrorDialogUtils.showError(this, "Please select a valid status");
			return false;
		}
		if (ValidationUtils.isFieldEmpty(DateField) || ValidationUtils.isFieldEmpty(TimeField)) {
			ErrorDialogUtils.showError(this, "Please enter date and time of the incident");
			return false;
		}
		if (ValidationUtils.isFieldEmpty(narrativeReportField)) {
			ErrorDialogUtils.showError(this, "Please enter narrative report");
			return false;
		} else if (narrativeReportField.getText().trim().length() < 10) {
			ErrorDialogUtils.showError(this, "Narrative report is too short (minimum 10 characters)");
			return false;
		}
		if (ValidationUtils.isFieldEmpty(actionsTakenField)) {
			ErrorDialogUtils.showError(this, "Please enter actions taken");
			return false;
		} else if (actionsTakenField.getText().trim().length() < 10) {
			ErrorDialogUtils.showError(this, "Actions taken is too short (minimum 10 characters)");
			return false;
		}
		if (ValidationUtils.isFieldEmpty(recommendationsField)) {
			ErrorDialogUtils.showError(this, "Please enter recommendations");
			return false;
		} else if (recommendationsField.getText().trim().length() < 10) {
			ErrorDialogUtils.showError(this, "Recommendations is too short (minimum 10 characters)");
			return false;
		}
		if (participantTableModel.getRowCount() == 0) {
			ErrorDialogUtils.showError(this, "Please add at least one participant");
			return false;
		}
		return true;
	}

	private java.sql.Timestamp parseDateTime(String date, String time) {
		try {
			LocalDate selectedDate = datePicker.getSelectedDate();
			LocalTime selectedTime = timePicker.getSelectedTime();
			LocalDateTime dateTime = LocalDateTime.of(selectedDate, selectedTime);
			return java.sql.Timestamp.valueOf(dateTime);
		} catch (Exception e) {
			return new java.sql.Timestamp(System.currentTimeMillis());
		}
	}

	private void clearForm() {
		reportedByField.setText("");
		DateField.setText("");
		TimeField.setText("");
		GradeSectionField.setText("");
		narrativeReportField.setText("");
		actionsTakenField.setText("");
		recommendationsField.setText("");
		reporterStudent = null;
		
		// Reset date and time pickers to current date/time
		datePicker.setSelectedDate(LocalDate.now());
		timePicker.setSelectedTime(LocalTime.now());
	}

	public void populateFromSession(Sessions session, List<Participants> participants) {
		// Clear existing data
		clearForm();
		
		// Set the date and time from session
		if (session.getSessionDateTime() != null) {
			LocalDateTime sessionDateTime = session.getSessionDateTime().toLocalDateTime();
			datePicker.setSelectedDate(sessionDateTime.toLocalDate());
			timePicker.setSelectedTime(sessionDateTime.toLocalTime());
		}

		// Set the narrative report from session summary
		if (session.getSessionSummary() != null && !session.getSessionSummary().isEmpty()) {
			narrativeReportField.setText(session.getSessionSummary());
		}

		// Set the actions taken from session notes
		if (session.getSessionNotes() != null && !session.getSessionNotes().isEmpty()) {
			actionsTakenField.setText(session.getSessionNotes());
		}

		// Clear reporter field - must be set manually
		reporterStudent = null;
		reportedByField.setText("");

		// Set initial status as Active
		statusCombo.setSelectedItem("Active");

		// Clear existing temporary participant data
		tempIncidentParticipants.clear();

		// Add participants to the temporary list and then update the table
		if (participants != null && !participants.isEmpty()) {
			for (Participants participant : participants) {
				// Create TempIncidentParticipant from database Participant, preserving the participantId
				TempIncidentParticipant tempIncidentParticipant = new TempIncidentParticipant(
						participant.getParticipantId(),
						participant.getStudentUid(), // This might be null for non-students
						participant.getParticipantFirstName(),
						participant.getParticipantLastName(),
						participant.getParticipantType(),
						participant.getSex(),
						participant.getContactNumber(),
						"Student".equals(participant.getParticipantType()) // Determine if it's a student
				);
				tempIncidentParticipants.add(tempIncidentParticipant);
			}
		}
		updateIncidentParticipantsTable(); // Refresh the table from the temporary list
	}

	private void populateReporterFields(Student student) {
		String fullName = String.format("%s %s %s", student.getStudentFirstname(), student.getStudentMiddlename(),
				student.getStudentLastname());
		reportedByField.setText(fullName);
		GradeSectionField.setText(student.getSchoolForm().getSF_SECTION());
	}

	// Update showParticipantDetails to include LRN for students
	private void showParticipantDetails(int participantId) {
		Map<String, String> details = participantDetails.get(participantId);
		if (details == null) {
			JOptionPane.showMessageDialog(this, "Participant details not found", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		StringBuilder detailsText = new StringBuilder();
		detailsText.append("<html><body style='width: 300px; padding: 10px;'>");
		detailsText.append("<h2>Participant Details</h2>");
		detailsText.append("<p><b>Name:</b> ").append(details.get("fullName")).append("</p>");
		detailsText.append("<p><b>Type:</b> ").append(details.get("type")).append("</p>");
		detailsText.append("<p><b>Sex:</b> ").append(details.get("sex")).append("</p>");

		// Add LRN for student participants
		if ("Student".equals(details.get("type")) && details.get("lrn") != null) {
			detailsText.append("<p><b>LRN:</b> ").append(details.get("lrn")).append("</p>");
		}

		if (details.get("contact") != null && !details.get("contact").isEmpty()) {
			detailsText.append("<p><b>Contact:</b> ").append(details.get("contact")).append("</p>");
		}
		detailsText.append("</body></html>");

		JOptionPane.showMessageDialog(this, detailsText.toString(), "Participant Details",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void setupDetailsPanel() {
		// ...existing setup code...

		// Add Student Search Button for reporter
		JButton studentReporterBtn = new JButton("Search Reporter Student");
		detailsPanel.add(studentReporterBtn, "cell 1 0");

		// Add action listener to the Student Search button
		studentReporterBtn.addActionListener(e -> openStudentReporterSearchUI());
	}

	private void openStudentReporterSearchUI() {
		if (ModalDialog.isIdExist("reporterSearch")) {
			return;
		}

		StudentSearchPanel searchPanel = new StudentSearchPanel(conn, "reporterSearch") {
			@Override
			protected void onStudentSelected(Student student) {
				reporterStudent = student;
				populateReporterFields(student);
				ModalDialog.closeModal("reporterSearch");
			}
		};

		Option option = ModalDialog.createOption();
		option.setAnimationEnabled(true);
		option.getLayoutOption().setMargin(40, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);
		ModalDialog.showModal(this, searchPanel, option, "reporterSearch");
	}

	private void openStudentParticipantSearchUI() {
		if (ModalDialog.isIdExist("participantSearch")) {
			return;
		}

		StudentSearchPanel searchPanel = new StudentSearchPanel(conn, "participantSearch") {
			@Override
			protected void onStudentSelected(Student student) {
				addStudentParticipant(student);
				ModalDialog.closeModal("participantSearch");
			}
		};

		Option option = ModalDialog.createOption();
		option.setAnimationEnabled(true);
		option.getLayoutOption().setMargin(40, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);
		ModalDialog.showModal(this, searchPanel, option, "participantSearch");
	}

	// Add this helper method to handle student participant creation
	private void addStudentParticipant(Student student) {
		TempIncidentParticipant participant = new TempIncidentParticipant(
			null, // participantId is null for new temporary participants
			student.getStudentUid(),
			student.getStudentFirstname(),
			student.getStudentLastname(),
			"Student",
			student.getStudentSex(),
			student.getContact() != null ? student.getContact().getContactNumber() : "",
			true
		);

		// Check for duplicates by student ID
		boolean isDuplicate = tempIncidentParticipants.stream().anyMatch(p -> 
			(p.isStudent() && p.getStudentUid() != null && p.getStudentUid().equals(participant.getStudentUid()))
		);

		if (!isDuplicate) {
			tempIncidentParticipants.add(participant);
			updateIncidentParticipantsTable();
		} else {
			JOptionPane.showMessageDialog(this, 
				"This student participant is already added to the incident.", 
				"Duplicate Entry",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	private void configureScrollSpeed(JScrollPane scrollPane) {
		JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
		verticalScrollBar.setUnitIncrement(16);
		verticalScrollBar.setBlockIncrement(64);
	}


	private void printIncidentReport() {
		try {
			// Use docPrinter incident report system with actual incident data
			incidentReportGenerator generator = new incidentReportGenerator();
			File outputFolder = templateManager.getDefaultOutputFolder();
			
			// Create incident data map from form fields
			Map<String, String> incidentData = new HashMap<>();
			incidentData.put("REPORTED_BY", reportedByField.getText().trim());
			incidentData.put("DATE", DateField.getText().trim());
			incidentData.put("TIME", TimeField.getText().trim());
			incidentData.put("GRADE_SECTION", GradeSectionField.getText().trim());
			incidentData.put("NARRATIVE_REPORT", narrativeReportField.getText().trim());
			incidentData.put("ACTIONS_TAKEN", actionsTakenField.getText().trim());
			incidentData.put("RECOMMENDATIONS", recommendationsField.getText().trim());
			incidentData.put("STATUS", statusCombo.getSelectedItem().toString());
			
			// Add participant information if available
			if (!tempIncidentParticipants.isEmpty()) {
				StringBuilder participants = new StringBuilder();
				for (TempIncidentParticipant participant : tempIncidentParticipants) {
					participants.append(participant.getFullName()).append(" (").append(participant.getType()).append("), ");
				}
				if (participants.length() > 2) {
					participants.setLength(participants.length() - 2); // Remove last ", "
				}
				incidentData.put("PARTICIPANTS", participants.toString());
			}
			
			boolean success = generator.generateIncidentReport(outputFolder, null, incidentData, "print");
			
			if (success) {
				logger.info("Incident report generated successfully with form data");
			} else {
				logger.error("Failed to generate incident report");
				JOptionPane.showMessageDialog(this, "Failed to generate incident report", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			logger.error("Error generating incident report", e);
			JOptionPane.showMessageDialog(this, "Error generating incident report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}	
}
