package lyfjshs.gomis.view.incident;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.IncidentsDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.entity.Incident;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.utils.IncidentReportGenerator;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class IncidentFillUpForm extends Form {

	private static final long serialVersionUID = 1L;
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

	public IncidentFillUpForm(Connection connectDB) {
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
		detailsPanel.add(DateField, "grow");

		detailsPanel.add(new JLabel("Grade & Section: "), "cell 0 1,alignx left");
		detailsPanel.add(GradeSectionField = new JTextField(), "cell 1 1,growx");

		detailsPanel.add(new JLabel("Time: "), "cell 4 1,alignx left");
		TimeField = new JFormattedTextField();
		timePicker.setSelectedTime(LocalTime.now());
		timePicker.setEditor(TimeField);
		TimeField.setColumns(10);
		detailsPanel.add(TimeField, "cell 5 1,growx");

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
		submitButton.addActionListener(e -> saveIncidentReport());
		footPanel.add(submitButton, "cell 1 0");

		JButton printBtn = new JButton("Print INTIAL Report");
		printBtn.addActionListener( e -> IncidentReportGenerator. createINITIALIncidentReport(this));
		detailsPanel.add(printBtn, "cell 0 2");

	}

	private void initializeComponents() {
		// Initialize the table model with proper columns
		participantTableModel = new DefaultTableModel(
				new Object[] { "#", "Participant Name", "Participant Type", "Actions" }, 0);

		participantsDAO = new ParticipantsDAO(conn);

		// ...existing initialization code...
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

		try {
			// Create new participant
			Participants participant = new Participants();
			participant.setStudentUid(null); // For non-student participant
			participant.setParticipantType(participantType);
			participant.setParticipantLastName(lastName);
			participant.setParticipantFirstName(firstName);
			participant.setSex(sex); // Using email field to store sex temporarily
			participant.setContactNumber(contact);

			// Save to database
			participantsDAO.createParticipant(participant);

			// Store participant details
			Map<String, String> details = new HashMap<>();
			details.put("firstName", firstName);
			details.put("lastName", lastName);
			details.put("fullName", firstName + " " + lastName);
			details.put("sex", sex);
			details.put("contact", contact);
			details.put("type", participantType);
			participantDetails.put(participant.getParticipantId(), details);

			// Add to table
			participantTableModel.addRow(new Object[] { participant.getParticipantId(), details.get("fullName"),
					participantType, "View | Remove" });

			// Clear input fields
			clearParticipantFields();

		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "Error adding participant: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void removeParticipant(int row, int participantId) {
		int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this participant?",
				"Confirm Remove", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			participantTableModel.removeRow(row);
			participantDetails.remove(participantId);
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

	private void saveIncidentReport() {
		try {
			// Validate required fields
			if (!validateFields()) {
				return;
			}

			// Validate reporter student
			if (reporterStudent == null) {
				JOptionPane.showMessageDialog(this,
					"Please select a reporter student first",
					"Validation Error",
					JOptionPane.ERROR_MESSAGE);
				return;
			}

			IncidentsDAO incidentsDAO = new IncidentsDAO(conn);

			// Create new incident
			Incident incident = new Incident();
			incident.setParticipantId(getOrCreateParticipant());
			incident.setIncidentDate(parseDateTime(DateField.getText(), TimeField.getText()));
			incident.setIncidentDescription(narrativeReportField.getText());
			incident.setActionTaken(actionsTakenField.getText());
			incident.setRecommendation(recommendationsField.getText());
			incident.setStatus("Pending");
			incident.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

			// Save to database
			int incidentId = incidentsDAO.createIncident(incident);
			if (incidentId != -1) {
				JOptionPane.showMessageDialog(this, "Incident report saved successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);
				clearForm();
			}

		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, 
				"Validation error: " + ex.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, 
				"Error saving incident: " + ex.getMessage(), 
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

	private boolean validateFields() {
		// Check reporter info
		if (reportedByField.getText().trim().isEmpty()) {
			showError("Please select a reporter");
			return false;
		}

		// Check date and time
		if (DateField.getText().trim().isEmpty() || TimeField.getText().isEmpty()) {
			showError("Please enter date and time of the incident");
			return false;
		}

		// Check narrative
		if (narrativeReportField.getText().trim().isEmpty()) {
			showError("Please enter narrative report");
			return false;
		}

		// Check actions taken
		if (actionsTakenField.getText().trim().isEmpty()) {
			showError("Please enter actions taken");
			return false;
		}

		return true;
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
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
		} else {
			narrativeReportField.setText("No summary available from the session.");
		}

		// Set the actions taken from session notes
		if (session.getSessionNotes() != null && !session.getSessionNotes().isEmpty()) {
			actionsTakenField.setText(session.getSessionNotes());
		} else {
			actionsTakenField.setText("No notes available from the session.");
		}

		// Clear existing table data
		participantTableModel.setRowCount(0);
		participantDetails.clear();

		// Add participants to the table
		if (participants != null && !participants.isEmpty()) {
			for (int i = 0; i < participants.size(); i++) {
				Participants participant = participants.get(i);
				Map<String, String> details = new HashMap<>();
				details.put("firstName", participant.getParticipantFirstName());
				details.put("lastName", participant.getParticipantLastName());
				details.put("fullName", participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
				details.put("type", participant.getParticipantType());
				details.put("sex", participant.getSex());
				details.put("contact", participant.getContactNumber());
				
				// Store participant details
				participantDetails.put(participant.getParticipantId(), details);

				// Add to table with proper row number
				participantTableModel.addRow(new Object[] {
					i + 1, // Use 1-based index for row numbers
					details.get("fullName"),
					participant.getParticipantType(),
					"View | Remove"
				});
			}

			// Set the first participant as reporter if available
			Participants firstParticipant = participants.get(0);
			String reporterName = firstParticipant.getParticipantFirstName() + " " + firstParticipant.getParticipantLastName();
			reportedByField.setText(reporterName);
		}
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
		detailsPanel.add(studentReporterBtn, "cell 1 0 3 1");

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
				try {
					addStudentParticipant(student);
					ModalDialog.closeModal("participantSearch");
				} catch (SQLException ex) {
					JOptionPane.showMessageDialog(IncidentFillUpForm.this, 
						"Error adding student as participant: " + ex.getMessage(),
						"Database Error", 
						JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		Option option = ModalDialog.createOption();
		option.setAnimationEnabled(true);
		option.getLayoutOption().setMargin(40, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);
		ModalDialog.showModal(this, searchPanel, option, "participantSearch");
	}

	// Add this helper method to handle student participant creation
	private void addStudentParticipant(Student student) throws SQLException {
		Participants participant = new Participants();
		participant.setStudentUid(student.getStudentUid());
		participant.setParticipantType("Student");
		participant.setParticipantLastName(student.getStudentLastname());
		participant.setParticipantFirstName(student.getStudentFirstname());
		participant.setSex(student.getStudentSex());
		participant.setContactNumber(student.getContact() != null ? student.getContact().getContactNumber() : "");

		// Save participant to database
		participantsDAO.createParticipant(participant);

		// Store participant details
		Map<String, String> details = new HashMap<>();
		details.put("firstName", student.getStudentFirstname());
		details.put("lastName", student.getStudentLastname());
		details.put("fullName", student.getStudentFirstname() + " " + student.getStudentLastname());
		details.put("sex", student.getStudentSex());
		details.put("type", "Student");
		details.put("lrn", student.getStudentLrn());
		participantDetails.put(participant.getParticipantId(), details);

		// Add to participants table
		participantTableModel.addRow(new Object[] { 
			participant.getParticipantId(), 
			details.get("fullName"),
			"Student", 
			"View | Remove" 
		});
	}
}
