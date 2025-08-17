/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package LEGACY_test_unused;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationCategoryDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.Database.entity.ViolationCategory;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.alarm.AlarmManagement;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.utils.ErrorDialogUtils;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.view.appointment.AppointmentSearchPanel;
import lyfjshs.gomis.view.appointment.add.NonStudentPanel;
import lyfjshs.gomis.view.sessions.SessionController;
import lyfjshs.gomis.view.sessions.SessionValidator;
import lyfjshs.gomis.view.sessions.TempParticipant;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

@SuppressWarnings("unused")
public class SessionFormTEST extends Form {
	private JComboBox<String> violationField;
	private JFormattedTextField timeField, dateField; // Declare the missing dateField
	private JTextArea sessionSummaryArea, notesArea;
	private JTextArea violationDescriptionArea, reinforcementArea; // New declarations
	private JButton saveButton;
	private JComboBox<String> consultationTypeComboBox;
	private JComboBox<String> appointmentTypeComboBox;
	private JComboBox<String> sessionStatusComboBox;
	private Connection connect;
	private JPanel mainPanel;
	private JPanel contentPanel;
	private GTable participantTable; // Change from JTable to GTable
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
	private static final Logger logger = LogManager.getLogger(SessionFormTEST.class);
	private AlarmManagement alarmManagement;
	private String currentSessionStatus;
	private SessionController controller;
	private SessionValidator validator;
	private JLabel statusIndicator;
	private DefaultTableActionManager actionManager; // Declare as a class member

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
	private JComboBox<String> categoryComboBox;
	private JTextField customCategoryField;
	private JPanel otherViolationAndCategoryPanel;
	private JLabel lblNewLabel_1;
	private JPanel violationInfoPanel; // Declare the new panel for Violation Info tab

	public SessionFormTEST(Connection conn) {
		try {
			this.connect = conn;
			this.controller = new SessionController(conn);
			this.validator = new SessionValidator();
			this.mainPanel = new JPanel();
			initializeComponents();

			// Set main layout for SessionFormTEST (this)
			this.setLayout(new MigLayout("gap 10, fill", "[grow]", "[shrink 0][grow]")); // Two rows: non-growing for header, growing for main

			// === Title Bar ===
			JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
			headerPanel.setBackground(new Color(5, 117, 230));
			JLabel headerLabel = new JLabel("Session Documentation Form");
			headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
			headerLabel.setForeground(Color.WHITE);
			headerPanel.add(headerLabel, "cell 0 0, center");
			this.add(headerPanel, "cell 0 0, growx"); // Add headerPanel directly to SessionFormTEST

			// === Main Panel ===
			mainPanel = new JPanel(new MigLayout("insets 20", "[grow]", "[][grow][]"));
			mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
					BorderFactory.createLineBorder(new Color(200, 200, 200))));
			this.add(mainPanel, "cell 0 1, grow"); // Add mainPanel directly to SessionFormTEST

			// Remove contentPanel as it's no longer needed
			// contentPanel was previously initialized and used here. Its contents are now directly added to 'this'.
			// Any components that were added to contentPanel should now be added to mainPanel if they belong there,
			// or directly to 'this' if they are top-level.

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

			// Bottom row of fields (only Session Status and Reschedule Panel remain here)
			JPanel bottomFields = new JPanel(new MigLayout("insets 0", "[grow]10[grow]", "[][][][][grow]"));
			
			// Session Status
			bottomFields.add(new JLabel("Session Status"), "cell 1 0");
			bottomFields.add(sessionStatusComboBox, "cell 1 1, growx");
			
			reschedPanel = new JPanel();
			reschedPanel.setVisible(false); // Hide by default
			bottomFields.add(reschedPanel, "cell 1 2 1 3,grow");
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

			sessionInfoCard.add(topFields, "cell 0 0, growx");
			sessionInfoCard.add(bottomFields, "cell 0 1, growx");

			sessionDetailsPanel.add(sessionInfoCard, "cell 0 0, grow");

			// === Right Section (Notes Panel) ===
			JPanel sessionNotes = new JPanel(new MigLayout("insets 10", "[grow]", "[][grow]"));
			sessionNotes.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

			// Notes area
			sessionNotes.add(new JLabel("Notes"), "cell 0 0");
			JScrollPane notesScrollPane = new JScrollPane(notesArea);
			configureScrollSpeed(notesScrollPane);
			sessionNotes.add(notesScrollPane, "cell 0 1, grow");

			sessionDetailsPanel.add(sessionNotes, "cell 1 0, grow");
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			mainPanel.add(tabbedPane, "cell 0 1,grow");

			// Initialize student and non-student drop panels
			studentDropPanel = new DropPanel();
			nonStudentDropPanel = new DropPanel();

			// === Participants Panel ===
			JPanel participantsPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[][grow][grow]"));
			JScrollPane participantScroll = new JScrollPane(participantsPanel);
			configureScrollSpeed(participantScroll);
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

				TempParticipant sessionParticipant = new TempParticipant(null, // participantId (new)
						null, // studentUid (non-student)
						participant.getFirstName(),
						participant.getLastName(),
						"Non-Student",
						participant.getSex(),
						participant.getContactNumber(),
						false, // isStudent
						false, // isViolator (default for new non-student)
						false); // isReporter (default for new non-student)
				tempParticipants.add(sessionParticipant);
				updateParticipantsTable();
				nonStudentDropPanel.setDropdownVisible(false);
			});
			nonStudentDropPanel.setContent(nonStudentForm);
			dropdownContainer.add(nonStudentDropPanel, "cell 0 1, grow");

			participantsPanel.add(dropdownContainer, "cell 0 1, grow");

			// Participant Table
			JScrollPane tableScrollPane = new JScrollPane(participantTable);
			configureScrollSpeed(tableScrollPane);
			tableScrollPane.setPreferredSize(new Dimension(0, 200));
			participantsPanel.add(tableScrollPane, "cell 0 2, grow");

			sessionSummaryArea = new JTextArea(4, 20);
			sessionSummaryArea.setLineWrap(true);
			sessionSummaryArea.setWrapStyleWord(true);

			// === Summary Panel ===
			JPanel summaryPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[grow]"));
			tabbedPane.addTab("Session Summary", null, summaryPanel, null);

			JScrollPane summaryScrollPane = new JScrollPane(sessionSummaryArea);
			configureScrollSpeed(summaryScrollPane);
			summaryPanel.add(summaryScrollPane, "cell 0 0,grow");
			
			// === Violation Info Panel ===
			violationInfoPanel = new JPanel(new MigLayout("insets 10, fill", "[grow]", "[][][grow][grow]")); // Adjusted layout for violationInfoPanel
			tabbedPane.addTab("Violation Info", null, violationInfoPanel, "Details of violation and intervention");

			// Violation Type and Category (moved from sessionInfoCard/bottomFields)
			JPanel violationTypeCategoryPanel = new JPanel(new MigLayout("insets 0, fill", "[grow]10[grow]", "[][][]")); // Added a row for other violation/category
			violationTypeCategoryPanel.add(new JLabel("Violation Type"), "cell 0 0");
			violationTypeCategoryPanel.add(violationField, "cell 0 1, growx");

			// Other Violation field (moved from `otherViolationAndCategoryPanel` to be directly below Violation Type)
			violationTypeCategoryPanel.add(new JLabel("Other Violation"), "cell 0 2");
			violationTypeCategoryPanel.add(customViolationField, "cell 0 3, growx");

			violationTypeCategoryPanel.add(lblNewLabel_1, "cell 1 0"); // Category Label
			violationTypeCategoryPanel.add(categoryComboBox, "cell 1 1, growx"); // Category ComboBox

			// Other Category field (moved from `otherViolationAndCategoryPanel` to be directly below Category)
			violationTypeCategoryPanel.add(new JLabel("Other Category"), "cell 1 2");
			violationTypeCategoryPanel.add(customCategoryField, "cell 1 3, growx");

			violationInfoPanel.add(violationTypeCategoryPanel, "cell 0 0, growx, wrap");

			// Removed otherViolationAndCategoryPanel as its components are now integrated

			// Violation Description
			JPanel violationDescriptionPanel = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[][grow]"));
			violationDescriptionPanel.add(new JLabel("Violation Description"), "cell 0 0");
			JScrollPane violationDescriptionScrollPane = new JScrollPane(violationDescriptionArea);
			configureScrollSpeed(violationDescriptionScrollPane);
			violationDescriptionPanel.add(violationDescriptionScrollPane, "cell 0 1, grow");
			violationInfoPanel.add(violationDescriptionPanel, "cell 0 1, growx, wrap"); // Adjusted cell

			// Reinforcement / Intervention
			JPanel reinforcementPanel = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[][grow]"));
			reinforcementPanel.add(new JLabel("Reinforcement / Intervention"), "cell 0 0");
			JScrollPane reinforcementScrollPane = new JScrollPane(reinforcementArea);
			configureScrollSpeed(reinforcementScrollPane);
			reinforcementPanel.add(reinforcementScrollPane, "cell 0 1, grow");
			violationInfoPanel.add(reinforcementPanel, "cell 0 2, growx, wrap"); // Adjusted cell
			
			// Setup listener for participant table actions
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
			saveButton.addActionListener(e -> saveSessionAsync(saveButton));
			saveButton.setPreferredSize(new Dimension(120, 40));
			
			populateRecordedByField(); 
			setDefaultDateTime();
			initializeAlarmSystem();
		} catch (Exception e) {
			logger.error("Error initializing SessionFormTEST", e);
			JOptionPane.showMessageDialog(null, 
				"Error initializing session form: " + e.getMessage(), 
				"Initialization Error", 
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void initializeComponents() {
		tempParticipants.clear(); // Ensure clean state before initialization
		// Initialize the table model with Violator and Reporter columns
		String[] columnNames = { "#", "Participant Name", "Participant Type", "Violator", "Reporter", "Actions" };
		Class<?>[] columnTypes = { Integer.class, String.class, String.class, Boolean.class, Boolean.class, Object.class };
		boolean[] editableColumns = { false, false, false, true, true, true };
		double[] columnWidths = { 0.05, 0.30, 0.20, 0.10, 0.10, 0.15 }; // Sum to 1.0
		int[] alignments = {
			SwingConstants.CENTER,  // #
			SwingConstants.LEFT,    // Participant Name
			SwingConstants.LEFT,    // Participant Type
			SwingConstants.CENTER,  // Violator
			SwingConstants.CENTER,  // Reporter
			SwingConstants.CENTER   // Actions
		};

		// Create action manager for the table
		actionManager = new DefaultTableActionManager(); // Initialize the class member
		((DefaultTableActionManager)actionManager)
			.addAction("View", (table, row) -> {
				// Get TempParticipant directly from list using row index
				if (row >= 0 && row < tempParticipants.size()) {
					TempParticipant participant = tempParticipants.get(row);
					showParticipantDetails(participant);
				} else {
					JOptionPane.showMessageDialog(SessionFormTEST.this, "Invalid row selected.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f))
			.addAction("Remove", (table, row) -> {
				int option = JOptionPane.showConfirmDialog(SessionFormTEST.this,
						"Are you sure you want to remove this participant?", "Confirm Remove",
						JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					// Get TempParticipant directly from list using row index
					if (row >= 0 && row < tempParticipants.size()) {
						TempParticipant participant = tempParticipants.get(row);
						if (participant.getParticipantId() != null) {
							removeParticipant(row, participant.getParticipantId());
						} else {
							// For newly added participants without a DB ID, remove directly from tempParticipants
							tempParticipants.remove(row);
							updateParticipantsTable();
						}
					}
				}
			}, new Color(0xdc3545), new FlatSVGIcon("icons/remove.svg", 0.5f));

		// Initialize GTable with proper configuration
		participantTable = new GTable(new Object[0][6], columnNames, columnTypes, editableColumns, columnWidths, alignments, false, null); // Updated to 6 columns
		participantTable.setRowHeight(40);

		// Explicitly set up the ActionColumnRenderer and Editor for the "Actions" column (index 5)
		// This is done after the table is initialized to ensure the column model is ready.
		actionManager.setupTableColumn(participantTable, 5);

		// Add listener for violator and reporter checkbox changes
		participantTable.getModel().addTableModelListener(e -> {
			int column = e.getColumn();
			int row = e.getFirstRow();
			if (row < 0 || row >= tempParticipants.size()) return; // Prevent AIOOBE

			// Get TempParticipant directly from list using row index
			TempParticipant p = tempParticipants.get(row);

			if (column == 3) { // Violator column
				p.setViolator((Boolean) participantTable.getValueAt(row, column));
			} else if (column == 4) { // Reporter column
				p.setReporter((Boolean) participantTable.getValueAt(row, column));
			}
		});

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

		// Initialize custom fields
		customViolationField = new JTextField(15);
		customViolationField.setEditable(false);
		customCategoryField = new JTextField(15);
		customCategoryField.setEditable(false);

		// Initialize other violation and category panel
		otherViolationAndCategoryPanel = new JPanel(new MigLayout("", "[grow]", "[][]"));
		otherViolationAndCategoryPanel.add(new JLabel("Other Violation"), "growx, wrap");
		otherViolationAndCategoryPanel.add(customViolationField, "growx, wrap");
		otherViolationAndCategoryPanel.add(new JLabel("Other Category"), "growx, wrap");
		otherViolationAndCategoryPanel.add(customCategoryField, "growx, wrap");
		otherViolationAndCategoryPanel.setVisible(false);

		// Initialize category label
		lblNewLabel_1 = new JLabel("Category");
		lblNewLabel_1.setVisible(false);

		// Initialize text areas
		sessionSummaryArea = new JTextArea(4, 20);
		sessionSummaryArea.setLineWrap(true);
		sessionSummaryArea.setWrapStyleWord(true);
		notesArea = new JTextArea(4, 20);
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);

		// Initialize new text areas for violation
		violationDescriptionArea = new JTextArea(4, 20);
		violationDescriptionArea.setLineWrap(true);
		violationDescriptionArea.setWrapStyleWord(true);
		reinforcementArea = new JTextArea(4, 20);
		reinforcementArea.setLineWrap(true);
		reinforcementArea.setWrapStyleWord(true);

		// Initialize combo boxes
		appointmentTypeComboBox = new JComboBox<>(new String[] { "Walk-in", "Scheduled", "Emergency" });
		consultationTypeComboBox = new JComboBox<>(new String[] {
			"Academic Consultation",
			"Career Guidance",
			"Personal Consultation",
			"Behavioral Consultation",
			"Group Consultation"
		});
		sessionStatusComboBox = new JComboBox<>(new String[] { "Select Status", "Active", "Ended" });
		violationField = new JComboBox<>();
		violationField.addItem("-- Select Violation --");
		violationField.addItem("No Violation");
		for (String violation : violations) {
			violationField.addItem(violation);
		}
		categoryComboBox = new JComboBox<>(new String[] {
			"-- Select Category --",
			"No Violation",
			"Physical",
			"Verbal",
			"Emotional",
			"Sexual",
			"Cyber",
			"Other"
		});

		// Initialize buttons
		searchBtn = new JButton("Search Appointment");
		searchBtn.addActionListener(e -> openAppointmentSearchDialog());
		searchStudentButton = new JButton("Add Student");
		searchStudentButton.setBackground(new Color(0, 123, 255));
		searchStudentButton.setForeground(Color.WHITE);
		searchStudentButton.setFocusPainted(false);

		// Add listeners
		sessionStatusComboBox.addActionListener(e -> handleStatusChange());
		categoryComboBox.addActionListener(e -> updateOtherCategoryVisibility());
		violationField.addActionListener(e -> updateViolationAndCategoryVisibility());

		// Initialize recorded by field
		recordedByField = new JTextField();
		recordedByField.setEditable(false);
		populateRecordedByField();

		// Initialize status indicator
		statusIndicator = new JLabel();
		statusIndicator.setPreferredSize(new Dimension(20, 20));
		statusIndicator.setOpaque(true);
		statusIndicator.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		updateStatusIndicator("Active"); // Set initial status
	}

	private void handleStatusChange() {
		String newStatus = (String) sessionStatusComboBox.getSelectedItem();
		String originalStatus = currentSession != null ? currentSession.getSessionStatus() : null;

		// If the user selects "Ended"
		if ("Ended".equals(newStatus)) {
			// If it's a new session or an active session being set to "Ended"
			if (originalStatus == null || "Active".equals(originalStatus)) {
				if (!validateSessionEnding()) {
					sessionStatusComboBox.setSelectedItem(originalStatus != null ? originalStatus : "-- Select Status --"); // Revert selection
					return;
				}
				// If validation passes, allow saving
				saveButton.setEnabled(true); // Enable save button to save the "Ended" status
			} else if ("Ended".equals(originalStatus)) {
				// If session is already ended, disable fields
				disableFieldsForEndedSession();
			}
		} else {
			// If status is changed to anything other than "Ended" or "Select Status"
			if ("-- Select Status --".equals(newStatus)) {
				saveButton.setEnabled(false); // Disable if "Select Status"
			} else {
				// Enable fields for active status
				enableFieldsBasedOnStatus(true);
			}
		}

		// Update UI based on new status for other fields
		updateUIForStatus(newStatus);
	}

	private boolean validateSessionEnding() {
		StringBuilder errors = new StringBuilder();

		// Check if session summary is provided
		if (sessionSummaryArea.getText().trim().isEmpty()) {
			errors.append("- Session summary is required before ending\n");
		}

		// Check if reinforcement is provided
		if (notesArea.getText().trim().isEmpty()) {
			errors.append("- Reinforcement details are required before ending\n");
		}

		// Removed participant-related validation from here. It is handled by SessionValidator.

		if (errors.length() > 0) {
			JOptionPane.showMessageDialog(this,
				"Please correct the following errors:\n\n" + errors.toString(),
				"Validation Error",
				JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	private void updateUIForStatus(String status) {		
		// Update participant table
		participantTable.setEnabled(!"Ended".equals(status));
		
		// Show/hide reschedule panel
		if (reschedPanel != null) {
			reschedPanel.setVisible("Active".equals(status));
		}
		
		// Update status indicator
		updateStatusIndicator(status);
	}

	private void updateStatusIndicator(String status) {
		// Update visual indicator of session status
		if (status != null) {
			switch (status) {
				case "Active":
					statusIndicator.setBackground(new Color(46, 204, 113));
					break;
				case "Ended":
					statusIndicator.setBackground(new Color(231, 76, 60));
					break;
				default:
					statusIndicator.setBackground(new Color(149, 165, 166));
			}
		}
	}

	private void updateOtherCategoryVisibility() {
		String selectedCategory = (String) categoryComboBox.getSelectedItem();
		if ("Other".equals(selectedCategory)) {
			customCategoryField.setEditable(true);
			customCategoryField.setVisible(true);
		} else {
			customCategoryField.setText("");
			customCategoryField.setEditable(false);
			customCategoryField.setVisible(false);
		}
	}

	private void updateViolationAndCategoryVisibility() {
		String selectedViolation = (String) violationField.getSelectedItem();
		
		// Show/hide category fields for Bullying
		if ("Bullying".equals(selectedViolation)) {
			lblNewLabel_1.setVisible(true);
			categoryComboBox.setVisible(true);
			updateOtherCategoryVisibility();
		} else {
			lblNewLabel_1.setVisible(false);
			categoryComboBox.setVisible(false);
			customCategoryField.setVisible(false);
		}
		
		// Show/hide custom violation field for Others
		if ("Others".equals(selectedViolation)) {
			customViolationField.setEditable(true);
			customViolationField.setVisible(true);
		} else {
			customViolationField.setText("");
			customViolationField.setEditable(false);
			customViolationField.setVisible(false);
		}
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

		// saveButton state is now primarily handled by handleStatusChange
		// Keep its text consistent with editing mode
		if (!isEnded) {
			saveButton.setText(isEditing ? "Update Session" : "SAVE");
		}
	}

	public void setSaveCallback(Runnable saveCallback) {
		this.saveCallback = saveCallback;
	}

	private void clearFields() {
		// Clear text fields
		sessionSummaryArea.setText("");
		notesArea.setText("");
		customViolationField.setText("");
		customCategoryField.setText("");
		recordedByField.setText("");
		sessionDateField.setText("");
		reSchedField.setText("");
		reSchedTimeField.setText("");
		
		// New: Clear violation description and reinforcement
		violationDescriptionArea.setText("");
		reinforcementArea.setText("");
		
		// Clear combo boxes
		consultationTypeComboBox.setSelectedIndex(0);
		appointmentTypeComboBox.setSelectedIndex(0);
		sessionStatusComboBox.setSelectedIndex(0);
		categoryComboBox.setSelectedIndex(0);
		violationField.setSelectedIndex(0);
		
		// Clear date and time pickers
		sessionDatePicker.setSelectedDate(LocalDate.now());
		sessionTimePicker.setSelectedTime(LocalTime.now());
		reSchedulePicker.setSelectedDate(LocalDate.now());
		reScheduleTimePicker.setSelectedTime(LocalTime.now());
		
		// Clear table
		participantTable.clearData();
		
		// Clear collections
		tempParticipants.clear();
		participantDetails.clear();
		pendingParticipants.clear();
		
		// Reset counters and flags
		tempIdCounter = -1;
		selectedAppointmentId = null;
		isEditing = false;
		currentSession = null;
		
		// Reset status
		currentSessionStatus = "Active";
		enableFieldsBasedOnStatus(true);
		
		// Hide panels
		otherViolationAndCategoryPanel.setVisible(false);
		reschedPanel.setVisible(false);
		
		// Reset drop panels
		studentDropPanel.removeAll();
		nonStudentDropPanel.removeAll();
	}

	private void updateParticipantsTable() {
		Object[][] data = new Object[tempParticipants.size()][6]; // Updated to 6 columns
		for (int i = 0; i < tempParticipants.size(); i++) {
			TempParticipant participant = tempParticipants.get(i);
			if (participant != null) {
				data[i][0] = i + 1; // Row number
				data[i][1] = participant.getFullName() != null ? participant.getFullName() : ""; // Name
				data[i][2] = participant.getType() != null ? participant.getType() : ""; // Type
				data[i][3] = participant.isViolator(); // Violator status
				data[i][4] = participant.isReporter(); // Reporter status
				data[i][5] = "actions"; // Actions column
			} else {
				// Handle null participant with default values
				data[i][0] = i + 1;
				data[i][1] = "";
				data[i][2] = "";
				data[i][3] = false;
				data[i][4] = false;
				data[i][5] = "actions";
			}
		}
		participantTable.setData(data);
	}

	private String determineParticipantRole(TempParticipant participant) {
		if (participant.isStudent()) {
			return "Student";
		} else if ("Teacher".equals(participant.getType())) {
			return "Teacher";
		} else {
			return "Other";
		}
	}

	private void addStudentParticipant(Student student) {
		// Corrected TempParticipant constructor call with all 10 arguments
		TempParticipant participant = new TempParticipant(null, // participantId
			                                            student.getStudentUid(),
			                                            student.getStudentFirstname(),
			                                            student.getStudentLastname(),
			                                            "Student",
			                                            student.getStudentSex(),
			                                            student.getContact() != null ? student.getContact().getContactNumber() : "",
			                                            true, // isStudent
			                                            false, // isViolator
			                                            false); // isReporter

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

	private void showParticipantDetails(TempParticipant participant) {
		if (participant == null) {
			JOptionPane.showMessageDialog(this, "Participant details not available.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JFrame detailFrame = new JFrame("Participant Details");
		detailFrame.setSize(400, 250); // Increased size to accommodate more details
		detailFrame.setLocationRelativeTo(this);

		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(0, 2, 10, 10));
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		detailsPanel.add(new JLabel("Full Name:"));
		detailsPanel.add(new JLabel(participant.getFullName() != null ? participant.getFullName() : "N/A"));
		detailsPanel.add(new JLabel("Type:"));
		detailsPanel.add(new JLabel(participant.getType() != null ? participant.getType() : "N/A"));
		detailsPanel.add(new JLabel("Gender:"));
		detailsPanel.add(new JLabel(participant.getSex() != null ? participant.getSex() : "N/A"));
		detailsPanel.add(new JLabel("Contact Number:"));
		detailsPanel.add(new JLabel(participant.getContactNumber() != null ? participant.getContactNumber() : "N/A"));
		detailsPanel.add(new JLabel("Is Violator:"));
		detailsPanel.add(new JLabel(participant.isViolator() ? "Yes" : "No"));
		detailsPanel.add(new JLabel("Is Reporter:"));
		detailsPanel.add(new JLabel(participant.isReporter() ? "Yes" : "No"));

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
			if (Main.formManager != null && FormManager.staticCounselorObject != null) {
				GuidanceCounselor counselor = FormManager.staticCounselorObject;
				String counselorName = String.format("%s %s %s", 
					counselor.getFirstName(), 
					counselor.getMiddleName() != null ? counselor.getMiddleName() : "",
					counselor.getLastName()).trim().replaceAll("\\s+", " ");

				if (counselorName != null && !counselorName.trim().isEmpty()) {
					recordedByField.setText(counselorName);
				} else {
					logger.info("Counselor name is empty. Skipping population of 'Recorded By' field.");
				}
			} else {
				logger.info("No counselor logged in. Skipping population of 'Recorded By' field.");
			}
		} catch (Exception e) {
			logger.error("Error retrieving counselor information", e);
			JOptionPane.showMessageDialog(this, 
				"Error retrieving counselor information: " + e.getMessage(), 
				"Error",
				JOptionPane.ERROR_MESSAGE);
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
											JOptionPane.showMessageDialog(SessionFormTEST.this,
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
			logger.error("Error opening appointment search", e);
			JOptionPane.showMessageDialog(this, "Error opening appointment search: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void populateParticipantsFromAppointment(int appointmentId) {
		try {
			AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
			Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);

			if (appointment != null) {
				// Clear existing participants in tempParticipants list
				tempParticipants.clear();
				participantDetails.clear(); // Clear map as well
				pendingParticipants.clear(); // Clear pending as well

				if (appointment.getParticipants() != null) {
					for (Participants participant : appointment.getParticipants()) {
						if (participant != null) {
							// Create TempParticipant with existing database ID, handling null values
							TempParticipant tempParticipant = new TempParticipant(
								participant.getParticipantId(), // Existing DB ID
								participant.getStudentUid(),
								participant.getParticipantFirstName() != null ? participant.getParticipantFirstName() : "",
								participant.getParticipantLastName() != null ? participant.getParticipantLastName() : "",
								participant.getParticipantType() != null ? participant.getParticipantType() : "Non-Student",
								participant.getSex() != null ? participant.getSex() : "",
								participant.getContactNumber() != null ? participant.getContactNumber() : "",
								participant.getStudentUid() != null, // isStudent: Determine if it's a student based on studentUid
								false, // isViolator: Participants does not have this info
								participant.isReporter());
							tempParticipants.add(tempParticipant);
						}
					}
				}
				// Update the UI table from the consolidated tempParticipants list
				updateParticipantsTable();
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error populating participants: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private Integer getOrCreateCategoryId(String violationType, String categoryName, String customCategoryName) throws SQLException {
		Integer categoryId = null;
		String targetCategoryName = null;

		if ("Bullying".equals(violationType)) {
			if ("Other".equals(categoryName)) {
				targetCategoryName = customCategoryName;
			} else {
				targetCategoryName = categoryName;
			}
		} else {
			targetCategoryName = "NO CATEGORY"; // Default for non-bullying violations
		}

		// Try to find existing category ID
		String selectSql = "SELECT CATEGORY_ID FROM VIOLATION_CATEGORIES WHERE CATEGORY_NAME = ?";
		try (PreparedStatement pstmt = connect.prepareStatement(selectSql)) {
			pstmt.setString(1, targetCategoryName);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					categoryId = rs.getInt("CATEGORY_ID");
				}
			}
		}

		// If category not found, create it
		if (categoryId == null) {
			String insertSql = "INSERT INTO VIOLATION_CATEGORIES (CATEGORY_NAME, CATEGORY_DESCRIPTION) VALUES (?, ?)";
			try (PreparedStatement insertStmt = connect.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
				insertStmt.setString(1, targetCategoryName);
				insertStmt.setString(2, "Automatically created category"); // Or a more specific description
				insertStmt.executeUpdate();
				try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						categoryId = generatedKeys.getInt(1);
					}
				}
			}
		}
		return categoryId;
	}

	private void saveSessionAsync(JButton saveButton) {
		saveButton.setEnabled(false);
		new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() {
				try {
					return saveSessionToDatabase();
				} catch (Exception e) {
					ErrorDialogUtils.showError(SessionFormTEST.this, "Error: " + e.getMessage());
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
					ErrorDialogUtils.showError(SessionFormTEST.this, "Error: " + e.getMessage());
				}
			}
		}.execute();
	}

	private boolean saveSessionToDatabase() {
		try {
			// Always ask for confirmation before saving
			int confirm = JOptionPane.showConfirmDialog(this,
				"Are you sure you want to save this session?",
				"Confirm Save",
				JOptionPane.YES_NO_OPTION);
			if (confirm != JOptionPane.YES_OPTION) {
				return false;
			}
			// Create session object
			Sessions session = new Sessions();
			session.setAppointmentId(selectedAppointmentId);
			session.setGuidanceCounselorId(Main.formManager.getCounselorObject().getGuidanceCounselorId());
			session.setAppointmentType(appointmentTypeComboBox.getSelectedItem().toString());
			session.setConsultationType(consultationTypeComboBox.getSelectedItem().toString());
			session.setSessionDateTime(parseDateTimeToTimestamp());
			session.setSessionNotes(notesArea.getText());
			session.setSessionSummary(sessionSummaryArea.getText());
			session.setSessionStatus(sessionStatusComboBox.getSelectedItem().toString());
			// Initialize ViolationCategoryDAO here, accessible throughout the method
			ViolationCategoryDAO violationCategoryDAO = new ViolationCategoryDAO(connect);
			// Create violation object if needed
			Violation violation = null;
			String selectedViolation = (String) violationField.getSelectedItem();
			boolean hasViolation = selectedViolation != null && !selectedViolation.equals("-- Select Violation --") && !selectedViolation.equals("No Violation");
			String categoryNameFromComboBox = null;
			String customCategoryText = null;
			Integer finalViolationCategoryId = null; // Will store the determined category ID
			if (hasViolation) {
				// Ensure at least one violator
				boolean hasViolator = tempParticipants.stream().anyMatch(TempParticipant::isViolator);
				if (!hasViolator) {
					ErrorDialogUtils.showError(this,
						"If a violation is selected, at least one participant must be marked as a violator.");
					return false;
				}
				violation = new Violation();
				violation.setViolationType(selectedViolation);
				violation.setViolationDescription(violationDescriptionArea.getText());
				violation.setReinforcement(reinforcementArea.getText());
				violation.setSessionSummary(sessionSummaryArea.getText());
				if ("Bullying".equals(selectedViolation)) {
					categoryNameFromComboBox = (String) categoryComboBox.getSelectedItem();
					customCategoryText = customCategoryField.getText().trim();
					if (categoryNameFromComboBox == null || categoryNameFromComboBox.equals("-- Select Category --")) {
						ErrorDialogUtils.showError(this,
							"Category is required for bullying violations.");
						return false;
					} else if ("Other".equals(categoryNameFromComboBox) && customCategoryText.isEmpty()) {
						ErrorDialogUtils.showError(this,
							"Please enter a custom category for 'Other' category.");
						return false;
					}
					finalViolationCategoryId = getOrCreateCategoryId(selectedViolation, categoryNameFromComboBox, customCategoryText);
					if (finalViolationCategoryId != null) {
						violation.setCategoryId(finalViolationCategoryId);
						ViolationCategory categoryObject = violationCategoryDAO.getCategoryById(finalViolationCategoryId);
						violation.setCategory(categoryObject);
					} else {
						ErrorDialogUtils.showError(this,
							"Internal Error: Could not determine violation category ID. Violation record not saved.");
						return false;
					}
				} else {
					categoryNameFromComboBox = "NO CATEGORY";
					customCategoryText = "";
				}
				finalViolationCategoryId = getOrCreateCategoryId(selectedViolation, categoryNameFromComboBox, customCategoryText);
				if (finalViolationCategoryId != null) {
					violation.setCategoryId(finalViolationCategoryId);
					ViolationCategory categoryObject = violationCategoryDAO.getCategoryById(finalViolationCategoryId);
					violation.setCategory(categoryObject);
				} else {
					ErrorDialogUtils.showError(this,
						"Internal Error: Could not determine violation category ID. Violation record not saved.");
					return false;
				}
			}
			// Validate session using SessionValidator
			if (!validator.validateSession(session, tempParticipants, violation)) {
				return false;
			}
			// Start transaction
			connect.setAutoCommit(false);
			// Save session
			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			int sessionId = sessionsDAO.createSession(session);
			if (sessionId > 0) {
				// Initialize DAOs for participants and violations within the transaction
				ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
				ViolationDAO violationDAO = new ViolationDAO(connect);
				// Save participants and violation records if needed
				for (TempParticipant participant : tempParticipants) {
					int participantId = participantsDAO.createParticipant(participant.toParticipant());
					if (participantId > 0) {
						sessionsDAO.addParticipantToSession(sessionId, participantId);
						if (hasViolation && participant.isViolator()) {
							Violation violatorRecord = new Violation();
							violatorRecord.setParticipantId(participantId);
							violatorRecord.setCategoryId(finalViolationCategoryId);
							violatorRecord.setViolationType(violation.getViolationType());
							violatorRecord.setViolationDescription(violation.getViolationDescription());
							violatorRecord.setReinforcement(violation.getReinforcement());
							violatorRecord.setSessionSummary(sessionSummaryArea.getText());
							violatorRecord.setStatus("Active");
							violatorRecord.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
							violationDAO.createViolation(violatorRecord);
						}
					}
				}
				// Handle appointment status
				if (selectedAppointmentId != null) {
					AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
					Appointment originalAppointment = appointmentDAO.getAppointmentById(selectedAppointmentId);
					if (originalAppointment != null) {
						String sessionStatus = session.getSessionStatus();
						LocalDate rescheduledDate = null;
						LocalTime rescheduledTime = null;
						String dateText = reSchedField.getText();
						if (dateText != null && !dateText.trim().isEmpty()) {
							try {
								rescheduledDate = LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
							} catch (java.time.format.DateTimeParseException e) {
								logger.debug("Error parsing rescheduled date '" + dateText + "': " + e.getMessage());
							}
						}
						String timeText = reSchedTimeField.getText();
						if (timeText != null && !timeText.trim().isEmpty()) {
							try {
								rescheduledTime = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("hh:mm a"));
							} catch (java.time.format.DateTimeParseException e) {
								logger.debug("Error parsing rescheduled time '" + timeText + "': " + e.getMessage());
							}
						}
						if ("Ended".equals(sessionStatus)) {
							if (rescheduledDate != null && rescheduledTime != null) {
								originalAppointment.setAppointmentDateTime(Timestamp.valueOf(rescheduledDate.atTime(rescheduledTime)));
								originalAppointment.setAppointmentStatus("Rescheduled");
								appointmentDAO.updateAppointment(originalAppointment);
								EventBus.publish("appointment_status_changed", originalAppointment.getAppointmentId());
							} else {
								originalAppointment.setAppointmentStatus("Completed");
								appointmentDAO.updateAppointment(originalAppointment);
							}
						} else if ("Active".equals(sessionStatus)) {
							if (rescheduledDate != null && rescheduledTime != null) {
								originalAppointment.setAppointmentStatus("Rescheduled");
								originalAppointment.setAppointmentDateTime(Timestamp.valueOf(rescheduledDate.atTime(rescheduledTime)));
								String rescheduleNote = "\n[Rescheduled from session to: " +
									rescheduledDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " " +
									rescheduledTime.format(DateTimeFormatter.ofPattern("hh:mm a")) + "]";
								String currentNotes = originalAppointment.getAppointmentNotes();
								originalAppointment.setAppointmentNotes(currentNotes == null ? rescheduleNote : currentNotes + rescheduleNote);
							} else {
								originalAppointment.setAppointmentStatus("In Progress");
							}
							appointmentDAO.updateAppointment(originalAppointment);
							EventBus.publish("appointment_updated", selectedAppointmentId);
						}
					}
				}
				connect.commit();
				updateUIForStatus("Active");
				updateStatusIndicator("Active");
				// Publish event for session creation
				EventBus.publish("sessionCreated", session);
				ErrorDialogUtils.showError(this, "Session has been saved successfully"); // Use a toast or info dialog in real UI
				if (saveCallback != null) {
					saveCallback.run();
				}
				return true;
			}
			connect.rollback();
			ErrorDialogUtils.showError(this, "Failed to save session");
			return false;
		} catch (SQLException e) {
			try {
				connect.rollback();
			} catch (SQLException ex) {
				logger.error("Error rolling back transaction", ex);
			}
			logger.error("Error saving session", e);
			ErrorDialogUtils.showError(this,
				"Error saving session: " + e.getMessage());
			return false;
		} finally {
			try {
				connect.setAutoCommit(true);
			} catch (SQLException e) {
				logger.error("Error resetting auto-commit", e);
			}
		}
	}

	private boolean validateViolationData() {
		StringBuilder errors = new StringBuilder();

		// Validate violation type
		String violation = (String) violationField.getSelectedItem();
		if (violation == null || violation.equals("-- Select Violation --")) {
			errors.append("- Please select a violation type\n");
		} else if ("Others".equals(violation)) {
			String customViolation = customViolationField.getText().trim();
			if (customViolation.isEmpty()) {
				errors.append("- Please enter a custom violation\n");
			}
		}

		// Validate category if violation is Bullying
		if ("Bullying".equals(violation)) {
			String category = (String) categoryComboBox.getSelectedItem();
			if (category == null || category.equals("-- Select Category --")) {
				errors.append("- Category is required for bullying violations\n");
			} else if ("Other".equals(category)) {
				String customCategory = customCategoryField.getText().trim();
				if (customCategory.isEmpty()) {
					errors.append("- Please enter a custom category for 'Other' category\n");
				}
			}
		}

		// Validate session summary
		String sessionSummary = sessionSummaryArea.getText().trim();
		if (sessionSummary.isEmpty()) {
			errors.append("- Session Summary is required\n");
		}

		// If there are any errors, show them and return false
		if (errors.length() > 0) {
			JOptionPane.showMessageDialog(this, 
				"Please correct the following errors:\n\n" + errors.toString(), 
				"Validation Error", 
				JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private List<Participants> getSessionParticipants(int sessionId) throws SQLException {
		List<Participants> participants = new ArrayList<>();
		String sql = "SELECT p.* FROM PARTICIPANTS p " +
					 "JOIN SESSIONS_PARTICIPANTS sp ON p.PARTICIPANT_ID = sp.PARTICIPANT_ID " +
					 "WHERE sp.SESSION_ID = ?";
		
		try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
			pstmt.setInt(1, sessionId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Participants participant = new Participants();
					participant.setParticipantId(rs.getInt("PARTICIPANT_ID"));
					participant.setStudentUid(rs.getInt("STUDENT_UID"));
					participant.setParticipantFirstName(rs.getString("PARTICIPANT_FIRSTNAME"));
					participant.setParticipantLastName(rs.getString("PARTICIPANT_LASTNAME"));
					participant.setParticipantType(rs.getString("PARTICIPANT_TYPE"));
					participant.setSex(rs.getString("PARTICIPANT_SEX"));
					participant.setContactNumber(rs.getString("CONTACT_NUMBER"));
					participants.add(participant);
				}
			}
		}
		return participants;
	}

	private void removeParticipant(int row, Integer participantId) {
		// Find the TempParticipant to remove based on its ID
		TempParticipant participantToRemove = null;
		for (TempParticipant tp : tempParticipants) {
			if (tp.getParticipantId() != null && tp.getParticipantId().equals(participantId)) {
				participantToRemove = tp;
				break;
			}
		}

		if (participantToRemove != null) {
			try {
				SessionsDAO sessionsDAO = new SessionsDAO(connect);
				AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
				ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);

				// Remove participant from the current session
				if (currentSession != null && currentSession.getSessionId() != null) {
					sessionsDAO.removeParticipantFromSession(currentSession.getSessionId(), participantId);
				} else if (selectedAppointmentId != null) {
					appointmentDAO.removeParticipantFromAppointment(selectedAppointmentId, participantId);
				}

				// Check if the participant is associated with any other sessions or appointments
				boolean hasOtherSessionAssociations = participantsDAO.isAssociatedWithAnySession(participantId);
				boolean hasOtherAppointmentAssociations = participantsDAO.isAssociatedWithAnyAppointment(participantId);

				// If not associated with any other session or appointment, delete from PARTICIPANTS table
				if (!hasOtherSessionAssociations && !hasOtherAppointmentAssociations) {
					participantsDAO.deleteParticipant(participantId);
					logger.info("Participant " + participantId + " fully deleted from database.");
				} else {
					logger.info("Participant " + participantId + " still associated with other records, not fully deleted.");
				}

				tempParticipants.remove(participantToRemove);
				updateParticipantsTable();
			} catch (SQLException e) {
				logger.error("Error removing participant", e);
				SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Error removing participant: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE));
			}
		}
	}

	private void handleSessionEnding(Sessions session) {
		try {
			// Start transaction
			connect.setAutoCommit(false);
			
			// Update session status
			session.setSessionStatus("Ended");
			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			if (sessionsDAO.updateSession(session)) {
				// Update appointment status to Completed if there's an associated appointment
				if (session.getAppointmentId() != null) {
					AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
					Appointment appointment = appointmentDAO.getAppointmentById(session.getAppointmentId());
					if (appointment != null) {
						appointment.setAppointmentStatus("Completed");
						if (!appointmentDAO.updateAppointment(appointment)) {
							connect.rollback();
							JOptionPane.showMessageDialog(this, 
								"Failed to update appointment status",
								"Error",
								JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}

				// Commit transaction
				connect.commit();
				
				// Update UI
				updateUIForStatus("Ended");
				updateStatusIndicator("Ended");
				
				// Show success message
				JOptionPane.showMessageDialog(this,
					"Session has been ended successfully",
					"Success",
					JOptionPane.INFORMATION_MESSAGE);
			} else {
				connect.rollback();
				JOptionPane.showMessageDialog(this,
					"Failed to update session status",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			}
		} catch (SQLException e) {
			try {
				connect.rollback();
			} catch (SQLException ex) {
				logger.error("Error rolling back transaction", ex);
			}
			logger.error("Error ending session", e);
			JOptionPane.showMessageDialog(this,
				"Error ending session: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				connect.setAutoCommit(true);
			} catch (SQLException e) {
				logger.error("Error resetting auto-commit", e);
			}
		}
	}

	private void setDefaultDateTime() {
		// Set default date and time to current
		sessionDatePicker.setSelectedDate(LocalDate.now());
		sessionTimePicker.setSelectedTime(LocalTime.now());
	}

	private void initializeAlarmSystem() {
		try {
			alarmManagement = AlarmManagement.createInstance(new AlarmManagement.AlarmCallback() {
				@Override
				public void onAlarmTriggered(Appointment appointment) {
					JOptionPane.showMessageDialog(SessionFormTEST.this, 
						"An appointment alarm has been triggered for: " + appointment.getAppointmentTitle(), 
						"Alarm",
							JOptionPane.INFORMATION_MESSAGE);
				}

				@Override
				public void onAlarmScheduled(LocalDateTime dateTime) {
					// Handle alarm scheduled
				}

				@Override
				public void onAlarmStopped() {
					// Handle alarm stopped
				}

				@Override
				public void onAlarmSnoozed(LocalDateTime newDateTime) {
					logger.info("Alarm snoozed until: " + newDateTime);
				}
			}, Main.appointmentCalendar);

			alarmManagement.start();
		} catch (Exception e) {
			logger.error("Error initializing alarm system", e);
			JOptionPane.showMessageDialog(this,
				"Error initializing appointment reminder system: " + e.getMessage(),
				"Initialization Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void configureScrollSpeed(JScrollPane scrollPane) {
		// Configure scroll speed for smoother scrolling
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
	}

	private LocalDateTime parseDateTime() {
		// Parse date and time from pickers
		LocalDate date = sessionDatePicker.getSelectedDate();
		LocalTime time = sessionTimePicker.getSelectedTime();
		return LocalDateTime.of(date, time);
	}

	private Timestamp parseDateTimeToTimestamp() {
		// Parse date and time from pickers and convert to Timestamp
		LocalDateTime dateTime = parseDateTime();
		return Timestamp.valueOf(dateTime);
	}

	public void populateFromAppointment(Appointment appointment) {
		if (appointment == null) {
			return;
		}

		try {
			// Set appointment ID
			selectedAppointmentId = appointment.getAppointmentId();

			// Set date and time
			if (appointment.getAppointmentDateTime() != null) {
				LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
				sessionDatePicker.setSelectedDate(dateTime.toLocalDate());
				sessionTimePicker.setSelectedTime(dateTime.toLocalTime());
			}

			// Set appointment title
			if (appointment.getAppointmentTitle() != null) {
				appointmentTypeComboBox.setSelectedItem(appointment.getAppointmentTitle());
			}

			// Set consultation type
			if (appointment.getConsultationType() != null) {
				consultationTypeComboBox.setSelectedItem(appointment.getConsultationType());
			}

			// Set session status to Active
			sessionStatusComboBox.setSelectedItem("Active");
			currentSessionStatus = "Active";
			updateUIForStatus("Active");

			// Populate participants from appointment
			populateParticipantsFromAppointment(appointment.getAppointmentId());

			// Update UI
			updateParticipantsTable();
			revalidate();
			repaint();

		} catch (Exception e) {
			logger.error("Error populating form from appointment", e);
			JOptionPane.showMessageDialog(this,
				"Error loading appointment data: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setEditingSession(Sessions session) {
		if (session == null) {
			return;
		}

		try {
			// Set current session
			currentSession = session;
			isEditing = true;

			// Set date and time
			if (session.getSessionDateTime() != null) {
				LocalDateTime dateTime = session.getSessionDateTime().toLocalDateTime();
				sessionDatePicker.setSelectedDate(dateTime.toLocalDate());
				sessionTimePicker.setSelectedTime(dateTime.toLocalTime());
			}

			// Set appointment type
			if (session.getAppointmentType() != null) {
				appointmentTypeComboBox.setSelectedItem(session.getAppointmentType());
			}

			// Set consultation type
			if (session.getConsultationType() != null) {
				consultationTypeComboBox.setSelectedItem(session.getConsultationType());
			}

			// Set session status
			if (session.getSessionStatus() != null) {
				sessionStatusComboBox.setSelectedItem(session.getSessionStatus());
				currentSessionStatus = session.getSessionStatus();
				updateUIForStatus(session.getSessionStatus());
			}

			// Set session summary and notes
			if (session.getSessionSummary() != null) {
				sessionSummaryArea.setText(session.getSessionSummary());
			}
			if (session.getSessionNotes() != null) {
				notesArea.setText(session.getSessionNotes());
			}

			// Set appointment ID if exists
			if (session.getAppointmentId() != null) {
				selectedAppointmentId = session.getAppointmentId();
			}

			// Populate participants
			List<Participants> participants = getSessionParticipants(session.getSessionId());
			tempParticipants.clear();
			for (Participants p : participants) {
				TempParticipant tempP = new TempParticipant(
					p.getParticipantId(),
					p.getStudentUid(),
					p.getParticipantFirstName(),
					p.getParticipantLastName(),
					p.getParticipantType(),
					p.getSex(),
					p.getContactNumber(),
					p.getStudentUid() != null,
					false, // isViolator: Participants does not have this info
					p.isReporter());
				tempParticipants.add(tempP);
			}

			// Update UI
			updateParticipantsTable();
			revalidate();
			repaint();

		} catch (Exception e) {
			logger.error("Error setting session for editing", e);
			JOptionPane.showMessageDialog(this,
				"Error loading session data: " + e.getMessage(),
				"Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}
}