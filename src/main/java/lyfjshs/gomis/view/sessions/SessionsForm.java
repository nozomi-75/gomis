package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.appointment.AppointmentSearchPanel;
import lyfjshs.gomis.view.appointment.add.NonStudentPanel;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class SessionsForm extends Form {
	private JComboBox<String> violationField;
	private JFormattedTextField timeField, dateField; // Declare the missing dateField
	private JTextArea sessionSummaryArea, notesArea;
	private JButton saveButton;
	private JComboBox<String> consultationTypeComboBox;
	private JComboBox<String> appointmentTypeComboBox;
	private JComboBox<String> sessionStatusComboBox;
	private Connection connect;
	private JPanel mainPanel;
	private JPanel contentPanel;
	private JTable participantTable;
	private DefaultTableModel participantTableModel;
	private Map<Integer, Map<String, String>> participantDetails = new HashMap<>();
	private Map<Integer, Participants> pendingParticipants = new HashMap<>();
	private int tempIdCounter = -1;
	private JButton searchBtn;
	private Runnable saveCallback;
	private JTextField customViolationField;
	private Integer selectedAppointmentId = null;
	private DatePicker sessionDatePicker;
	private TimePicker sessionTimePicker;
	private Sessions currentSession;
	private boolean isEditing = false;
	private DropPanel studentDropPanel;
	private DropPanel nonStudentDropPanel;
	private JButton searchStudentButton;
	private List<TempParticipant> tempParticipants = new ArrayList<>();
	private static final Logger logger = Logger.getLogger(SessionsForm.class.getName());

	// Violation type arrays
	private String[] violations = { "Absence/Late", "Minor Property Damage", "Threatening/Intimidating",
			"Pornographic Materials", "Gadget Use in Class", "Cheating", "Stealing", "No Pass", "Bullying",
			"Sexual Abuse", "Illegal Drugs", "Alcohol", "Smoking/Vaping", "Gambling", "Public Display of Affection",
			"Fighting/Weapons", "Severe Property Damage", "Others" };

	private JTextField recordedByField;
	private JFormattedTextField sessionDateField;
	private JFormattedTextField reSchedField;
	private DatePicker reSchedulePicker;
	private TimePicker reScheduleTimePicker;
	private JFormattedTextField reSchedTimeField;
	private JPanel reschedPanel;

	public SessionsForm(Connection conn) {
		try {
			this.connect = conn;
			this.mainPanel = new JPanel(); // Initialize mainPanel here
			initializeComponents();
			layoutComponents();
			populateRecordedByField(); // Call the method to populate the recordedByField
		} catch (Exception e) {
			Logger.getLogger(SessionsForm.class.getName()).log(Level.SEVERE, "Error initializing SessionsForm", e);
			JOptionPane.showMessageDialog(null, 
				"Error initializing session form: " + e.getMessage(), 
				"Initialization Error", 
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initializeComponents() {
		// Initialize the table model
		participantTableModel = new DefaultTableModel(
				new Object[] { "#", "Participant Name", "Participant Type", "Actions", "ID" }, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false; // Make all cells non-editable
			}
		};

		// Initialize date and time pickers with better styling
		sessionTimePicker = new TimePicker();
		sessionDatePicker = new DatePicker();

		// Set up formatted fields
		sessionDateField = new JFormattedTextField();
		sessionDatePicker.setSelectedDate(LocalDate.now());
		sessionDatePicker.setEditor(sessionDateField);

		dateField = new JFormattedTextField();
		sessionDatePicker.setEditor(dateField);

		timeField = new JFormattedTextField();
		sessionTimePicker.setSelectedTime(LocalTime.now());
		sessionTimePicker.setEditor(timeField);

		// Appointment type dropdown with better options
		appointmentTypeComboBox = new JComboBox<>(new String[] { "Walk-in", "Scheduled", "Emergency" });

		// Create search button with better styling
		searchBtn = new JButton("Search Appointment");
		searchBtn.addActionListener(e -> openAppointmentSearchDialog());

		// Consultation type dropdown
		consultationTypeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance",
				"Personal Consultation", "Behavioral Consultation", "Group Consultation" });

		// Session status dropdown
		sessionStatusComboBox = new JComboBox<>(new String[] { "Select Status", "Active", "Ended" });
		sessionStatusComboBox.addActionListener(e -> {
			String newStatus = (String) sessionStatusComboBox.getSelectedItem();

			if (isEditing && currentSession != null) {
				String currentStatus = currentSession.getSessionStatus();

				// If changing from Active to Ended, keep save button enabled
				if ("Active".equals(currentStatus) && "Ended".equals(newStatus)) {
					saveButton.setEnabled(true);
					saveButton.setText("Save Status Change");
					
					// Hide reschedule panel when ending a session
					if (reschedPanel != null) {
						reschedPanel.setVisible(false);
					}
					return;
				}

				// If already Ended, disable everything
				if ("Ended".equals(currentStatus)) {
					disableFieldsForEndedSession();
					return;
				}
			}

			// For Active status or new sessions
			boolean isActive = !"Select Status".equals(newStatus);
			boolean isActiveStatus = "Active".equals(newStatus);
            
            // Show reschedule panel only for Active status and when it comes from an appointment
            if (reschedPanel != null) {
                boolean shouldShowReschedPanel = isActiveStatus && selectedAppointmentId != null;
                reschedPanel.setVisible(shouldShowReschedPanel);
            }
            
			enableFieldsBasedOnStatus(isActive);
		});

		// Initialize violation field with "No Violation" option
		violationField = new JComboBox<>();
		violationField.addItem("-- Select Violation --");
		violationField.addItem("No Violation"); // Add "No Violation" option
		for (String violation : violations) {
			violationField.addItem(violation);
		}

		// Initialize text areas with better styling
		notesArea = new JTextArea(4, 20);
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);

		// Create student and non-student buttons with better styling
		searchStudentButton = new JButton("Add Student");
		searchStudentButton.setBackground(new Color(0, 123, 255));
		searchStudentButton.setForeground(Color.WHITE);
		searchStudentButton.setFocusPainted(false);
	}

	private void disableFieldsForEndedSession() {
		notesArea.setEditable(false);
		sessionSummaryArea.setEditable(false);
		participantTable.setEnabled(false);
		saveButton.setEnabled(false);
		searchStudentButton.setEnabled(false);
		violationField.setEnabled(false);
		customViolationField.setEnabled(false);
		consultationTypeComboBox.setEnabled(false);
		appointmentTypeComboBox.setEnabled(false);
		searchBtn.setEnabled(false);
		saveButton.setText("Session Ended");
	}

	private void enableFieldsBasedOnStatus(boolean isActive) {
		// For walk-in sessions, always keep save button enabled when status is "Ended"
		String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
		String status = (String) sessionStatusComboBox.getSelectedItem();
		boolean isWalkIn = "Walk-in".equals(appointmentType);
		boolean isEnded = "Ended".equals(status);
		boolean isActiveStatus = "Active".equals(status);

		notesArea.setEditable(isActive);
		sessionSummaryArea.setEditable(isActive);
		participantTable.setEnabled(isActive);
		searchStudentButton.setEnabled(isActive);
		violationField.setEnabled(isActive);
		customViolationField.setEnabled(isActive && "Others".equals(violationField.getSelectedItem()));
		consultationTypeComboBox.setEnabled(isActive);
		appointmentTypeComboBox.setEnabled(isActive);
		searchBtn.setEnabled(isActive);
		
		// Show/hide reschedule panel based on status
		if (reschedPanel != null) {
			reschedPanel.setVisible(isActiveStatus);
		}

		// Keep save button enabled for walk-in sessions even when ended
		if (isWalkIn && isEnded) {
			saveButton.setEnabled(true);
			saveButton.setText("Save Session");
		} else {
			saveButton.setEnabled(isActive);
			saveButton.setText(isEditing ? "Update Session" : "SAVE");
		}
	}

	public void setSaveCallback(Runnable saveCallback) {
		this.saveCallback = saveCallback;
	}

	private void clearFields() {
		notesArea.setText("");
		sessionSummaryArea.setText("");
		timeField.setText("");
		dateField.setText("");

		consultationTypeComboBox.setSelectedIndex(0);
		appointmentTypeComboBox.setSelectedIndex(0);
		violationField.setSelectedIndex(0);
		sessionStatusComboBox.setSelectedIndex(0);
		
		// Reset reschedule field and hide panel
		if (reSchedulePicker != null) {
			reSchedulePicker.setSelectedDate(LocalDate.now().plusDays(7));
		}
		if (reschedPanel != null) {
			reschedPanel.setVisible(false);
		}

		participantTableModel.setRowCount(0);
		participantDetails.clear();
		pendingParticipants.clear();
		tempParticipants.clear();
		tempIdCounter = -1; // Reset for next session
		selectedAppointmentId = null;
	}

	private void updateParticipantsTable() {
		participantTableModel.setRowCount(0);
		int rowNumber = 1;

		for (TempParticipant participant : tempParticipants) {
			participantTableModel.addRow(new Object[] { rowNumber++, participant.getFullName(), participant.getType(),
					"View | Remove", participant.isStudent() ? participant.getStudentUid() : -rowNumber });
		}
	}

	private void addStudentParticipant(Student student) {
		TempParticipant participant = new TempParticipant(student.getStudentUid(), student.getStudentFirstname(),
				student.getStudentLastname(), "Student", student.getStudentSex(),
				student.getContact() != null ? student.getContact().getContactNumber() : "", true);

		// Check for duplicates by student ID and by name
		boolean isDuplicate = tempParticipants.stream().anyMatch(p -> 
			(p.isStudent() && p.getStudentUid() != null && p.getStudentUid().equals(participant.getStudentUid())) ||
			(!p.isStudent() && p.getFullName().equalsIgnoreCase(participant.getFullName()))
		);

		if (!isDuplicate) {
			tempParticipants.add(participant);
			updateParticipantsTable();
		} else {
			JOptionPane.showMessageDialog(this, 
				"This participant is already added to the session.", 
				"Duplicate Entry",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	private void setupParticipantTableListener() {
		participantTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = participantTable.rowAtPoint(evt.getPoint());
				int col = participantTable.columnAtPoint(evt.getPoint());

				if (col == 3) { // Actions column
					int viewIndex = participantTable.convertRowIndexToModel(row);
					int id = (int) participantTableModel.getValueAt(viewIndex, 4);

					int clickX = evt.getX();
					int cellX = participantTable.getCellRect(row, col, false).x;
					int relativeX = clickX - cellX;
					int viewWidth = 40; // Approximate width of "View"

					if (relativeX <= viewWidth) {
						// View
						showParticipantDetails(id);
					} else {
						// Remove
						int option = JOptionPane.showConfirmDialog(SessionsForm.this,
								"Are you sure you want to remove this participant?", "Confirm Remove",
								JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION) {
							// If working with TempParticipants
							if (id < 0) {
								// Find the index in tempParticipants that corresponds to this row
								int tempIndex = -(id + 1); // Adjust for row offset used in addRow
								if (tempIndex >= 0 && tempIndex < tempParticipants.size()) {
									tempParticipants.remove(tempIndex);
									updateParticipantsTable();
								}
							} else {
								// Handle existing participants
								participantDetails.remove(id);
								pendingParticipants.remove(id);
								participantTableModel.removeRow(viewIndex);

								// Renumber rows
								for (int i = 0; i < participantTableModel.getRowCount(); i++) {
									participantTableModel.setValueAt(i + 1, i, 0);
								}
							}
						}
					}
				}
			}
		});
	}

	private void layoutComponents() {
		// Set main layout
		this.setLayout(new MigLayout("gap 10", "[grow]", "[grow]"));
		contentPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		add(contentPanel, "cell 0 0, grow");

		// === Title Bar ===
		JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
		headerPanel.setBackground(new Color(5, 117, 230));
		JLabel headerLabel = new JLabel("Session Documentation Form");
		headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
		headerLabel.setForeground(Color.WHITE);
		headerPanel.add(headerLabel, "cell 0 0, center");
		contentPanel.add(headerPanel, "cell 0 0, growx");

		// === Main Panel ===
		mainPanel = new JPanel(new MigLayout("insets 20", "[grow]", "[][grow][]"));
		contentPanel.add(mainPanel, "cell 0 1, grow");
		mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
				BorderFactory.createLineBorder(new Color(200, 200, 200))));

		// === Session Details Panel ===
		JPanel sessionDetailsPanel = new JPanel(new MigLayout("insets 0, fill", "[grow,fill]15[grow]", "[]"));
		mainPanel.add(sessionDetailsPanel, "cell 0 0, growx");

		// === Left Section (Session Info) ===
		JPanel sessionInfoCard = new JPanel(new MigLayout("insets 10", "[grow]", "[][]"));
		sessionInfoCard.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

		// Create top row of fields
		JPanel topFields = new JPanel(new MigLayout("insets 0", "[grow]10[grow]10[grow]10[grow]", "[][]"));

		// Session Date
		topFields.add(new JLabel("Session Date"), "cell 0 0");
		topFields.add(dateField, "cell 0 1, growx");

		// Session Time
		topFields.add(new JLabel("Session Time"), "cell 1 0");
		topFields.add(timeField, "cell 1 1, growx");

		// Appointment Type
		topFields.add(new JLabel("Appointment Type"), "cell 2 0");
		JPanel apptPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
		apptPanel.add(appointmentTypeComboBox, "cell 0 0, growx");
		apptPanel.add(searchBtn, "cell 1 0");
		topFields.add(apptPanel, "cell 2 1, growx");

		// Consultation Type
		topFields.add(new JLabel("Consultation Type"), "cell 3 0");
		topFields.add(consultationTypeComboBox, "cell 3 1, growx");

		// Bottom row of fields
		JPanel bottomFields = new JPanel(new MigLayout("insets 0", "[grow]10[grow]", "[][][grow]"));

		// Violation Type
		bottomFields.add(new JLabel("Violation Type"), "cell 0 0");
		bottomFields.add(violationField, "cell 0 1, growx");

		// Session Status
		bottomFields.add(new JLabel("Session Status"), "cell 1 0");
		bottomFields.add(sessionStatusComboBox, "cell 1 1, growx");
		JPanel otherViolationPanel = new JPanel();
		bottomFields.add(otherViolationPanel, "cell 0 2,grow");
		otherViolationPanel.setLayout(new MigLayout("", "[grow]", "[]"));

		// Other Violation
		JLabel label_1 = new JLabel("Other Violation");
		otherViolationPanel.add(label_1, "flowx,cell 0 0");

		// Initialize custom violation field
		customViolationField = new JTextField(15);
		otherViolationPanel.add(customViolationField, "cell 0 0,growx");
		otherViolationPanel.setVisible(false); // Initially hide the panel

		// Add violation type change listener
		violationField.addActionListener(e -> {
			String selected = (String) violationField.getSelectedItem();
			otherViolationPanel.setVisible("Others".equals(selected));
			customViolationField.setEnabled("Others".equals(selected));
			if (!"Others".equals(selected)) {
				customViolationField.setText(""); // Clear the text when hidden
			}
		});

		sessionInfoCard.add(topFields, "cell 0 0, growx");
		sessionInfoCard.add(bottomFields, "cell 0 1, growx");
		reschedPanel = new JPanel();
		reschedPanel.setVisible(false); // Hide by default
		bottomFields.add(reschedPanel, "cell 1 2,grow");
		reschedPanel.setLayout(new MigLayout("", "[][grow]", "[][]"));
		JLabel lblNewLabel = new JLabel("Reschedule Date:");
		reschedPanel.add(lblNewLabel, "cell 0 0,alignx trailing");
		reSchedulePicker = new DatePicker();
		reSchedField = new JFormattedTextField();
		reSchedulePicker.setEditor(reSchedField);
		reschedPanel.add(reSchedField, "cell 1 0,growx");
		
		// Add time picker for rescheduling
		JLabel lblRescheduleTime = new JLabel("Reschedule Time:");
		reschedPanel.add(lblRescheduleTime, "cell 0 1,alignx trailing");
		reScheduleTimePicker = new TimePicker();
		reSchedTimeField = new JFormattedTextField();
		reScheduleTimePicker.setEditor(reSchedTimeField);
		reschedPanel.add(reSchedTimeField, "cell 1 1,growx");

		sessionDetailsPanel.add(sessionInfoCard, "cell 0 0, grow");

		// === Right Section (Notes Panel) ===
		JPanel sessionNotes = new JPanel(new MigLayout("insets 10", "[grow]", "[][grow]"));
		sessionNotes.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

		// Notes area
		sessionNotes.add(new JLabel("Notes"), "cell 0 0");
		sessionNotes.add(new JScrollPane(notesArea), "cell 0 1, grow");

		sessionDetailsPanel.add(sessionNotes, "cell 1 0, grow");
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		mainPanel.add(tabbedPane, "cell 0 1,grow");

		// Initialize student and non-student drop panels
		studentDropPanel = new DropPanel();
		nonStudentDropPanel = new DropPanel();

		// === Participants Panel ===
		JPanel participantsPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[][grow][grow]"));
		JScrollPane participantScroll = new JScrollPane(participantsPanel);		
		//set participantScroll SPEED
		tabbedPane.addTab("Participants", null, participantScroll, "Click to view/add to Participants List");

		// Button Panel for adding participants
		JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));

		// Add Student Button
		JButton addStudentBtn = new JButton("Add Student");
		addStudentBtn.setBackground(new Color(0, 123, 255));
		addStudentBtn.setForeground(Color.WHITE);
		addStudentBtn.addActionListener(e -> {
			if (nonStudentDropPanel != null) {
				nonStudentDropPanel.setDropdownVisible(false);
			}
			if (studentDropPanel != null) {
				studentDropPanel.setDropdownVisible(!studentDropPanel.isDropdownVisible());
			}
		});
		buttonPanel.add(addStudentBtn, "growx");

		// Add Non-Student Button
		JButton addNonStudentBtn = new JButton("Add Non-Student");
		addNonStudentBtn.setBackground(new Color(40, 167, 69));
		addNonStudentBtn.setForeground(Color.WHITE);
		addNonStudentBtn.addActionListener(e -> {
			if (studentDropPanel != null) {
				studentDropPanel.setDropdownVisible(false);
			}
			if (nonStudentDropPanel != null) {
				nonStudentDropPanel.setDropdownVisible(!nonStudentDropPanel.isDropdownVisible());
			}
		});
		buttonPanel.add(addNonStudentBtn, "growx");

		participantsPanel.add(buttonPanel, "cell 0 0, growx");

		// Create dropdown panels container with proper constraints
		JPanel dropdownContainer = new JPanel(new MigLayout("insets 0, hidemode 3", "[grow]", "[grow][grow]"));
		dropdownContainer.setOpaque(false);

		// Configure student search dropdown panel
		studentDropPanel = new DropPanel();
		studentDropPanel.setDropdownPadding(10, 10, 10, 10);
		StudentSearchPanel studentSearch = new StudentSearchPanel(connect, null) {
			@Override
			protected void onStudentSelected(Student student) {
				addStudentParticipant(student);
				studentDropPanel.setDropdownVisible(false);
			}
		};
		studentDropPanel.setContent(studentSearch);
		dropdownContainer.add(studentDropPanel, "cell 0 0, grow");

		// Configure non-student dropdown panel
		nonStudentDropPanel = new DropPanel();
		nonStudentDropPanel.setDropdownPadding(10, 10, 10, 10);
		NonStudentPanel nonStudentForm = new NonStudentPanel();
		nonStudentForm.setNonStudentListener(participant -> {
			// First check if this non-student already exists by name
			String fullName = participant.getFirstName() + " " + participant.getLastName();
			boolean isDuplicate = tempParticipants.stream().anyMatch(p -> 
				p.getFullName().equalsIgnoreCase(fullName)
			);

			if (isDuplicate) {
				JOptionPane.showMessageDialog(this, 
					"This participant is already added to the session.", 
					"Duplicate Entry",
					JOptionPane.WARNING_MESSAGE);
				return;
			}

			TempParticipant sessionParticipant = new TempParticipant(null,
					participant.getFirstName(),
					participant.getLastName(),
					"Non-Student",
					participant.getSex(),
					participant.getContactNumber(),
					false
			);
			tempParticipants.add(sessionParticipant);
			updateParticipantsTable();
			nonStudentDropPanel.setDropdownVisible(false);
		});
		nonStudentDropPanel.setContent(nonStudentForm);
		dropdownContainer.add(nonStudentDropPanel, "cell 0 1, grow");

		participantsPanel.add(dropdownContainer, "cell 0 1, grow");

		// Participant Table
		participantTable = new JTable(participantTableModel);
		JScrollPane tableScrollPane = new JScrollPane(participantTable);
		tableScrollPane.setPreferredSize(new Dimension(0, 200));
		participantsPanel.add(tableScrollPane, "cell 0 2, grow");

		sessionSummaryArea = new JTextArea(4, 20);
		sessionSummaryArea.setLineWrap(true);
		sessionSummaryArea.setWrapStyleWord(true);

		// === Summary Panel ===
		JPanel summaryPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[grow]"));
		tabbedPane.addTab("Session Summary", null, summaryPanel, null);

		JScrollPane summaryScrollPane = new JScrollPane(sessionSummaryArea);
		summaryPanel.add(summaryScrollPane, "cell 0 0,grow");
		// Hide ID column
		if (participantTable.getColumnCount() > 4) {
			participantTable.getColumnModel().removeColumn(participantTable.getColumnModel().getColumn(4));
		}

		// Setup listener for participant table actions
		setupParticipantTableListener();
		JPanel recordedByPanel = new JPanel();
		mainPanel.add(recordedByPanel, "flowx,cell 0 2,alignx right");
		recordedByPanel.setLayout(new MigLayout("", "[][fill]", "[]"));

		// Recorded By
		JLabel label = new JLabel("Recorded By");
		recordedByPanel.add(label, "cell 0 0");

		// Initialize recorded by field as JTextField
		recordedByField = new JTextField();
		recordedByField.setColumns(20);
		recordedByPanel.add(recordedByField, "cell 1 0");
		recordedByField.setEditable(false); // Make it read-only

		// Create save button with better styling
		saveButton = new JButton("SAVE");
		mainPanel.add(saveButton, "cell 0 2,alignx right");
		saveButton.setBackground(new Color(70, 130, 180));
		saveButton.setForeground(Color.WHITE);
		saveButton.setFocusPainted(false);
		saveButton.addActionListener(e -> saveSessionToDatabase());
		saveButton.setPreferredSize(new Dimension(120, 40));
	}

	private void showParticipantDetails(int id) {
		Map<String, String> details = participantDetails.get(id);

		if (details == null) {
			JOptionPane.showMessageDialog(this, "Participant details not found", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFrame detailFrame = new JFrame("Participant Details");
		detailFrame.setSize(300, 200);
		detailFrame.setLocationRelativeTo(this);

		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(0, 2, 10, 10));
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		detailsPanel.add(new JLabel("Full Name:"));
		detailsPanel.add(new JLabel(details.get("fullName")));
		detailsPanel.add(new JLabel("Contact Number:"));
		detailsPanel.add(new JLabel(details.get("contact")));
		detailsPanel.add(new JLabel("Email:"));
		detailsPanel.add(new JLabel(details.get("email")));
		detailsPanel.add(new JLabel("Type:"));
		detailsPanel.add(new JLabel(details.get("type")));

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> detailFrame.dispose());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(closeButton);

		detailFrame.getContentPane().setLayout(new BorderLayout());
		detailFrame.getContentPane().add(detailsPanel, BorderLayout.CENTER);
		detailFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		detailFrame.setVisible(true);
	}

	private void populateRecordedByField() {
		try {
			if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
				GuidanceCounselor counselor = Main.formManager.getCounselorObject();
				String counselorName = String.format("%s %s %s", counselor.getFirstName(), counselor.getMiddleName(),
						counselor.getLastName()).trim();

				if (!counselorName.isEmpty()) {
					recordedByField.setText(counselorName);
				}
			} else {
				System.out.println("No counselor logged in. Skipping population of 'Recorded By' field.");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error retrieving counselor information: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void openAppointmentSearchDialog() {
		if (ModalDialog.isIdExist("search_appointment")) {
			return;
		}

		try {
			AppointmentSearchPanel searchPanel = new AppointmentSearchPanel(connect);

			ModalDialog.getDefaultOption().setOpacity(0f).setAnimationOnClose(false).getBorderOption()
					.setBorderWidth(0.5f).setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

			ModalDialog.showModal(this,
					new SimpleModalBorder(searchPanel, "Search Appointment",
							new SimpleModalBorder.Option[] {
									new SimpleModalBorder.Option("Select", SimpleModalBorder.YES_OPTION),
									new SimpleModalBorder.Option("Cancel", SimpleModalBorder.NO_OPTION) },
							(controller, action) -> {
								if (action == SimpleModalBorder.YES_OPTION) {
									Integer selectedId = searchPanel.getSelectedAppointmentId();
									if (selectedId != null) {
										try {
											AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
											Appointment selectedAppointment = appointmentDAO.getAppointmentById(selectedId);
											
											if (selectedAppointment != null && selectedAppointment.getAppointmentDateTime() != null) {
												// Set the date and time in the pickers
												LocalDateTime appointmentDateTime = selectedAppointment.getAppointmentDateTime().toLocalDateTime();
												sessionDatePicker.setSelectedDate(appointmentDateTime.toLocalDate());
												sessionTimePicker.setSelectedTime(appointmentDateTime.toLocalTime());
											}
											
											String consultationType = searchPanel.getSelectedConsultationType();
											appointmentTypeComboBox.setSelectedItem("From Appointment");
											consultationTypeComboBox.setSelectedItem(consultationType);
											selectedAppointmentId = selectedId;
											populateParticipantsFromAppointment(selectedId);
											controller.close();
										} catch (SQLException e) {
											JOptionPane.showMessageDialog(SessionsForm.this,
												"Error loading appointment details: " + e.getMessage(),
												"Database Error",
												JOptionPane.ERROR_MESSAGE);
										}
									} else {
										JOptionPane.showMessageDialog(this, "Please select an appointment.", "Warning",
												JOptionPane.WARNING_MESSAGE);
									}
								} else if (action == SimpleModalBorder.NO_OPTION) {
									controller.close();
								}
							}),
					"search_appointment");

			ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 500);

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error opening appointment search: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void populateParticipantsFromAppointment(int appointmentId) {
		try {
			AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
			Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);

			if (appointment != null) {
				participantTableModel.setRowCount(0);

				if (appointment.getParticipants() != null) {
					Set<String> addedParticipants = new HashSet<>(); // Track added participants
					
					for (Participants participant : appointment.getParticipants()) {
						String fullName = participant.getParticipantFirstName() + " " + participant.getParticipantLastName();
						
						// Skip if this participant is already added
						if (addedParticipants.contains(fullName.toLowerCase())) {
							continue;
						}
						
						Map<String, String> details = new HashMap<>();
						details.put("firstName", participant.getParticipantFirstName());
						details.put("lastName", participant.getParticipantLastName());
						details.put("fullName", fullName);
						String participantType = participant.getStudentUid() != null && participant.getStudentUid() > 0
								? "Student"
								: "Non-Student";
						details.put("type", participantType);
						details.put("contact", participant.getContactNumber());
						details.put("sex", participant.getSex());
						participantDetails.put(participant.getParticipantId(), details);

						int rowNumber = participantTableModel.getRowCount() + 1;
						participantTableModel.addRow(new Object[] { rowNumber, details.get("fullName"), participantType,
								"View | Remove", participant.getParticipantId() });
								
						addedParticipants.add(fullName.toLowerCase()); // Track this participant
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error populating participants: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveSessionToDatabase() {
		try {
			if (!validateInputs()) {
				return;
			}

			String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
			String consultationType = (String) consultationTypeComboBox.getSelectedItem();
			String violation = (String) violationField.getSelectedItem();
			String notes = notesArea.getText();
			String summary = sessionSummaryArea.getText().trim();
			String sessionStatus = (String) sessionStatusComboBox.getSelectedItem();

			Integer appointmentId = "Walk-in".equals(appointmentType) ? null : selectedAppointmentId;
			String violationText = "Others".equals(violation) ? customViolationField.getText().trim() : violation;

			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);

			// Start transaction
			connect.setAutoCommit(false);
			try {
				int sessionId;
				if (isEditing && currentSession != null) {
					// Update existing session
					currentSession.setAppointmentType(appointmentType);
					currentSession.setConsultationType(consultationType);
					currentSession.setSessionDateTime(parseDateTime());
					currentSession.setSessionNotes(notes);
					currentSession.setSessionSummary(summary);
					currentSession.setSessionStatus(sessionStatus);

					sessionsDAO.updateSession(currentSession);
					sessionId = currentSession.getSessionId();

					// Clear existing participants for the session
					participantsDAO.removeAllParticipantsFromSession(sessionId);
				} else {
					// Create new session
					int guidanceCounselorId;
					if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
						guidanceCounselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
					} else {
						throw new SQLException("No guidance counselor is currently logged in.");
					}

					Sessions session = new Sessions(0, appointmentId, guidanceCounselorId, null, appointmentType,
							consultationType, parseDateTime(), notes, summary, sessionStatus,
							new java.sql.Timestamp(System.currentTimeMillis()));

					sessionId = sessionsDAO.addSession(session);
					if (sessionId <= 0) {
						throw new SQLException("Failed to create new session.");
					}
				}

				// Process all participants
				for (TempParticipant tempParticipant : tempParticipants) {
					int participantId;
					
					if (tempParticipant.isStudent() && tempParticipant.getStudentUid() != null) {
						// Check if a participant record already exists for this student
						Participants existingParticipant = participantsDAO.getParticipantByStudentId(tempParticipant.getStudentUid());
						if (existingParticipant != null) {
							participantId = existingParticipant.getParticipantId();
						} else {
							// Create new participant record for student
							Participants newParticipant = new Participants();
							newParticipant.setStudentUid(tempParticipant.getStudentUid());
							newParticipant.setParticipantFirstName(tempParticipant.getFirstName());
							newParticipant.setParticipantLastName(tempParticipant.getLastName());
							newParticipant.setParticipantType("Student");
							newParticipant.setSex(tempParticipant.getSex());
							newParticipant.setContactNumber(tempParticipant.getContactNumber());
							participantId = participantsDAO.createParticipant(newParticipant);
						}
					} else {
						// Create new participant record for non-student
						Participants newParticipant = new Participants();
						newParticipant.setParticipantFirstName(tempParticipant.getFirstName());
						newParticipant.setParticipantLastName(tempParticipant.getLastName());
						newParticipant.setParticipantType("Non-Student");
						newParticipant.setSex(tempParticipant.getSex());
						newParticipant.setContactNumber(tempParticipant.getContactNumber());
						participantId = participantsDAO.createParticipant(newParticipant);
					}

					if (participantId <= 0) {
						throw new SQLException("Failed to create/get participant record.");
					}

					// Add participant to session
					sessionsDAO.addParticipantToSession(sessionId, participantId);

					// Create violation record if needed
					if (violation != null && !violation.equals("-- Select Violation --") && !violation.equals("No Violation")) {
						ViolationDAO violationDAO = new ViolationDAO(connect);
						boolean success = violationDAO.addViolation(participantId, violationText, violationText,
								summary, notes, "Active", new java.sql.Timestamp(System.currentTimeMillis()));
						if (!success) {
							throw new SQLException("Failed to create violation record for participant ID: " + participantId);
						}
					}
				}

				// Update appointment status if needed
				if (appointmentId != null) {
					AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
					Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
					if (appointment != null) {
						updateAppointmentStatus(appointment, sessionStatus);
						appointmentDAO.updateAppointment(appointment);
						EventBus.publish("appointment_status_changed", appointmentId);
					}
				}

				// Commit transaction
				connect.commit();

				JOptionPane.showMessageDialog(this, 
					isEditing ? "Session updated successfully!" : "Session and participants saved successfully!", 
					"Success", 
					JOptionPane.INFORMATION_MESSAGE);

				refreshViolations();

				if (saveCallback != null) {
					saveCallback.run();
				}

				if (!isEditing) {
					clearFields();
				}

			} catch (Exception e) {
				connect.rollback();
				throw e;
			} finally {
				connect.setAutoCommit(true);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void updateAppointmentStatus(Appointment appointment, String sessionStatus) {
		if ("Ended".equals(sessionStatus)) {
			appointment.setAppointmentStatus("Completed");
		} else if ("Active".equals(sessionStatus)) {
			if (reschedPanel != null && reschedPanel.isVisible() && reSchedulePicker.getSelectedDate() != null) {
				appointment.setAppointmentStatus("Rescheduled");
				
				LocalDate reschedDate = reSchedulePicker.getSelectedDate();
				LocalTime reschedTime = reScheduleTimePicker.getSelectedTime();
				LocalDateTime newDateTime = reschedDate.atTime(reschedTime != null ? reschedTime : 
					appointment.getAppointmentDateTime().toLocalDateTime().toLocalTime());
				
				appointment.setAppointmentDateTime(java.sql.Timestamp.valueOf(newDateTime));
				
				String existingNotes = appointment.getAppointmentNotes();
				String rescheduleNote = "Session conducted on " + 
					java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd").format(parseDateTime().toLocalDateTime()) + 
					". Rescheduled to " + 
					java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(newDateTime) + ".";
				
				appointment.setAppointmentNotes(existingNotes != null && !existingNotes.isEmpty() ? 
					existingNotes + "\n\n" + rescheduleNote : rescheduleNote);
			} else {
				appointment.setAppointmentStatus("In Progress");
			}
		}
	}

	private boolean validateInputs() {
		StringBuilder errors = new StringBuilder();

		String status = (String) sessionStatusComboBox.getSelectedItem();
		if ("Select Status".equals(status)) {
			errors.append("- Please select a session status\n");
		}

		String consultationType = (String) consultationTypeComboBox.getSelectedItem();
		if (consultationType == null || "".equals(consultationType)) {
			errors.append("- Please select a consultation type\n");
		}

		String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
		if (appointmentType == null || "".equals(appointmentType)) {
			errors.append("- Please select an appointment type\n");
		}

		// Validate session date and time
		try {
			LocalDate selectedDate = sessionDatePicker.getSelectedDate();
			LocalTime selectedTime = sessionTimePicker.getSelectedTime();
			
			if (selectedDate == null) {
				errors.append("- Please select a valid session date\n");
			}
			
			if (selectedTime == null) {
				errors.append("- Please select a valid session time\n");
			}
		} catch (Exception e) {
			errors.append("- Please enter a valid session date and time\n");
		}
		
		// Validate reschedule date if visible and status is Active
		if ("Active".equals(status) && reschedPanel != null && reschedPanel.isVisible()) {
			try {
				LocalDate reschedDate = reSchedulePicker.getSelectedDate();
				LocalTime reschedTime = reScheduleTimePicker.getSelectedTime();
				
				if (reschedDate == null) {
					errors.append("- Please select a valid reschedule date\n");
				} else {
					// Ensure reschedule date is in the future
					LocalDate today = LocalDate.now();
					if (reschedDate.isBefore(today)) {
						errors.append("- Reschedule date must be in the future\n");
					}
				}
				
				if (reschedTime == null) {
					errors.append("- Please select a valid reschedule time\n");
				}
			} catch (Exception e) {
				errors.append("- Please enter a valid reschedule date and time\n");
			}
		}

		String summary = sessionSummaryArea.getText().trim();
		if (summary.isEmpty()) {
			errors.append("- Please enter a session summary\n");
		} else if (summary.length() < 10) {
			errors.append("- Session summary is too short (minimum 10 characters)\n");
		}

		// Check notes
		String notes = notesArea.getText().trim();
		if (notes.isEmpty()) {
			errors.append("- Please enter session notes\n");
		}

		String violation = (String) violationField.getSelectedItem();
		if (violation == null || "-- Select Violation --".equals(violation)) {
			errors.append("- Please select a violation type (or 'No Violation')\n");
		} else if ("Others".equals(violation)) {
			String customViolation = customViolationField.getText().trim();
			if (customViolation.isEmpty()) {
				errors.append("- Please enter a custom violation type\n");
			}
		}

		if (participantTableModel.getRowCount() == 0) {
			errors.append("- Please add at least one participant\n");
		}

		if (errors.length() > 0) {
			JOptionPane.showMessageDialog(this, "Please fix the following errors:\n" + errors.toString(),
					"Validation Error", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	private void refreshViolations() {
		try {
			if (Main.formManager != null) {
				Form[] forms = FormManager.getForms();
				for (Form form : forms) {
					try {
						if (form.getClass().getName().equals("lyfjshs.gomis.view.violation.Violation_Record")) {
							// Use reflection to call refreshViolations to avoid direct dependency
							form.getClass().getMethod("refreshViolations").invoke(form);
						}
					} catch (Exception e) {
						// Log the error but continue processing other forms
						System.err.println("Error refreshing violation record: " + e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error in refreshViolations: " + e.getMessage());
		}
	}

	private java.sql.Timestamp parseDateTime() {
		try {
			LocalDate date = sessionDatePicker.getSelectedDate();
			LocalTime time = sessionTimePicker.getSelectedTime();
			if (date != null && time != null) {
				return java.sql.Timestamp.valueOf(date.atTime(time));
			}
			return new java.sql.Timestamp(System.currentTimeMillis());
		} catch (Exception e) {
			return new java.sql.Timestamp(System.currentTimeMillis());
		}
	}

	public void populateFromAppointment(Appointment appointment) {
		if (appointment != null) {
			// Reset the form
			clearFields();
			
			// Set appointment type to "Scheduled" for appointments
			appointmentTypeComboBox.setSelectedItem("Scheduled");
			
			// Store the appointment ID for reference
			selectedAppointmentId = appointment.getAppointmentId();
			
			// Transfer consultation type
			consultationTypeComboBox.setSelectedItem(appointment.getConsultationType());
			
			// Set the session date and time to match the appointment
			if (appointment.getAppointmentDateTime() != null) {
				java.time.LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
				sessionDatePicker.setSelectedDate(dateTime.toLocalDate());
				sessionTimePicker.setSelectedTime(dateTime.toLocalTime());
				
				// Set a default reschedule date and time (7 days from appointment date, same time)
				LocalDate suggestedRescheduleDate = dateTime.toLocalDate().plusDays(7);
				reSchedulePicker.setSelectedDate(suggestedRescheduleDate);
				reScheduleTimePicker.setSelectedTime(dateTime.toLocalTime());
			} else {
				// If no appointment date, set reschedule to 7 days from today, current time
				reSchedulePicker.setSelectedDate(LocalDate.now().plusDays(7));
				reScheduleTimePicker.setSelectedTime(LocalTime.now());
			}
			
			// Transfer notes
			if (appointment.getAppointmentNotes() != null && !appointment.getAppointmentNotes().isEmpty()) {
				notesArea.setText(appointment.getAppointmentNotes());
			}
			
			// Set default session status to "Active"
			sessionStatusComboBox.setSelectedItem("Active");
			
			// Show reschedule panel for appointments
			if (reschedPanel != null) {
				reschedPanel.setVisible(true);
			}
			
			// Transfer participants
			transferParticipantsFromAppointment(appointment);
			
			// Prepare session summary template
			String summaryTemplate = "Session created from appointment: " + appointment.getAppointmentTitle() + 
					"\nConsultation Type: " + appointment.getConsultationType() +
					"\nDate: " + appointment.getAppointmentDateTime();
			sessionSummaryArea.setText(summaryTemplate);
		}
	}
	
	private void transferParticipantsFromAppointment(Appointment appointment) {
		// Clear existing participants
		participantTableModel.setRowCount(0);
		participantDetails.clear();
		tempParticipants.clear();
		pendingParticipants.clear();
		
		// Add all participants from the appointment
		if (appointment.getParticipants() != null && !appointment.getParticipants().isEmpty()) {
			for (Participants participant : appointment.getParticipants()) {
				Map<String, String> details = new HashMap<>();
				details.put("firstName", participant.getParticipantFirstName());
				details.put("lastName", participant.getParticipantLastName());
				details.put("fullName", participant.getFullName());
				details.put("type", participant.getParticipantType());
				details.put("contact", participant.getContactNumber());
				
				// Store in participant details map
				if (participant.getParticipantId() > 0) {
					participantDetails.put(participant.getParticipantId(), details);
				}
				
				// Create temp participant for table
				TempParticipant tempParticipant = new TempParticipant(
					participant.getStudentUid(),
					participant.getParticipantFirstName(),
					participant.getParticipantLastName(),
					participant.getParticipantType(),
					participant.getSex(),
					participant.getContactNumber(),
					participant.getStudentUid() != null
				);
				tempParticipants.add(tempParticipant);
				
				// Add to table
				int rowNumber = participantTableModel.getRowCount() + 1;
				participantTableModel.addRow(new Object[] { 
					rowNumber, 
					participant.getFullName(),
					participant.getParticipantType(), 
					"View | Remove", 
					participant.getParticipantId() 
				});
			}
		}
	}

	public void setEditingSession(Sessions session) {
		this.currentSession = session;
		this.isEditing = true;
		populateFormFromSession(session);
	}

	private void populateFormFromSession(Sessions session) {
		try {
			appointmentTypeComboBox.setSelectedItem(session.getAppointmentType());
			consultationTypeComboBox.setSelectedItem(session.getConsultationType());

			if (session.getSessionDateTime() != null) {
				LocalDateTime dateTime = session.getSessionDateTime().toLocalDateTime();
				sessionDatePicker.setSelectedDate(dateTime.toLocalDate());
				sessionTimePicker.setSelectedTime(dateTime.toLocalTime());
			}

			notesArea.setText(session.getSessionNotes());
			sessionSummaryArea.setText(session.getSessionSummary());

			ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
			List<Participants> participants = participantsDAO.getParticipantsBySessionId(session.getSessionId());

			participantTableModel.setRowCount(0);
			for (Participants participant : participants) {
				Map<String, String> details = new HashMap<>();
				details.put("firstName", participant.getParticipantFirstName());
				details.put("lastName", participant.getParticipantLastName());
				details.put("fullName",
						participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
				details.put("type", participant.getParticipantType());
				details.put("contact", participant.getContactNumber());
				participantDetails.put(participant.getParticipantId(), details);

				int rowNumber = participantTableModel.getRowCount() + 1;
				participantTableModel.addRow(new Object[] { rowNumber, details.get("fullName"),
						participant.getParticipantType(), "View | Remove", participant.getParticipantId() });
			}

			sessionStatusComboBox.setSelectedItem(session.getSessionStatus());
			boolean isActive = "Active".equals(session.getSessionStatus());
			enableFieldsBasedOnStatus(isActive);
			saveButton.setText("Save Appointment");

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading session data: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// TempParticipant class for managing participants before they're saved to the
	// database
	public class TempParticipant {
		private Integer studentUid;
		private String firstName;
		private String lastName;
		private String type;
		private String sex;
		private String contactNumber;
		private boolean isStudent;

		public TempParticipant(Integer studentUid, String firstName, String lastName, String type, String sex,
				String contactNumber, boolean isStudent) {
			this.studentUid = studentUid;
			this.firstName = firstName;
			this.lastName = lastName;
			this.type = type;
			this.sex = sex;
			this.contactNumber = contactNumber;
			this.isStudent = isStudent;
		}

		public Integer getStudentUid() {
			return studentUid;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public String getFullName() {
			return firstName + " " + lastName;
		}

		public String getType() {
			return type;
		}

		public String getSex() {
			return sex;
		}

		public String getContactNumber() {
			return contactNumber;
		}

		public boolean isStudent() {
			return isStudent;
		}
	}

	/**
	 * Override the dispose method to warn about unsaved changes
	 */
	@Override
	public void dispose() {
		// Check if there are unsaved changes
		if (hasUnsavedChanges()) {
			int option = JOptionPane.showConfirmDialog(
				this,
				"You have unsaved changes in this session. What would you like to do?",
				"Unsaved Changes",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			
			if (option == JOptionPane.YES_OPTION) {
				// Save changes and then dispose
				saveSessionToDatabase();
				super.dispose();
			} else if (option == JOptionPane.NO_OPTION) {
				// Discard changes and dispose
				super.dispose();
			}
			// If CANCEL_OPTION, do nothing and return to the form
			return;
		}
		
		// No unsaved changes, proceed with normal disposal
		super.dispose();
	}

	/**
	 * Checks if there are unsaved changes in the form
	 */
	private boolean hasUnsavedChanges() {
		// If creating a new session
		if (!isEditing && selectedAppointmentId != null) {
			return true;
		}
		
		// If editing an existing session
		if (isEditing && currentSession != null) {
			String currentStatus = (String) sessionStatusComboBox.getSelectedItem();
			String currentNotes = notesArea.getText();
			String currentSummary = sessionSummaryArea.getText();
			
			return !currentStatus.equals(currentSession.getSessionStatus()) ||
				   !currentNotes.equals(currentSession.getSessionNotes()) ||
				   !currentSummary.equals(currentSession.getSessionSummary()) ||
				   participantTableModel.getRowCount() > 0;  // Any participants added
		}
		
		return false;
	}

	/**
	 * Sets the session to edit and populates form fields from the session data
	 * 
	 * @param sessionId The session ID to edit
	 */
	public void setSessionToEdit(Integer sessionId) {
		try {
			if (sessionId == null) {
				return;
			}
			
			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			Sessions session = sessionsDAO.getSessionById(sessionId);
			
			if (session != null) {
				isEditing = true;
				currentSession = session;
				populateFormFromSession(session);
				
				// If the session has an appointment, store its ID
				if (session.getAppointmentId() != null) {
					selectedAppointmentId = session.getAppointmentId();
				}
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, 
				"Error loading session data: " + e.getMessage(), 
				"Database Error", 
				JOptionPane.ERROR_MESSAGE);
		}
	}
}