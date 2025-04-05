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

	// Violation type arrays
	private String[] violations = { "Absence/Late", "Minor Property Damage", "Threatening/Intimidating",
			"Pornographic Materials", "Gadget Use in Class", "Cheating", "Stealing", "No Pass", "Bullying",
			"Sexual Abuse", "Illegal Drugs", "Alcohol", "Smoking/Vaping", "Gambling", "Public Display of Affection",
			"Fighting/Weapons", "Severe Property Damage", "Others" };

	private JTextField recordedByField;
	private JFormattedTextField sessionDateField;

	public SessionsForm(Connection conn) {
		this.connect = conn;
		this.mainPanel = new JPanel(); // Initialize mainPanel here
		initializeComponents();
		layoutComponents();
		populateRecordedByField(); // Call the method to populate the recordedByField
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
			enableFieldsBasedOnStatus(isActive);
		});

		// Initialize violation field with "No Violation" option
		violationField = new JComboBox<>();
		violationField.addItem("-- Select Violation --");
		violationField.addItem("No Violation"); // Add "No Violation" option
		for (String violation : violations) {
			violationField.addItem(violation);
		}

		// Add listener to update violation type label and show/hide custom field
		violationField.addActionListener(e -> {
			String selected = (String) violationField.getSelectedItem();
			if (selected == null || selected.equals("-- Select Violation --") || selected.equals("No Violation")) {
			} else if (selected.equals("Others")) {
			} else {
			}
		});

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

		notesArea.setEditable(isActive);
		sessionSummaryArea.setEditable(isActive);
		participantTable.setEnabled(isActive);
		searchStudentButton.setEnabled(isActive);
		violationField.setEnabled(isActive);
		customViolationField.setEnabled(isActive && "Others".equals(violationField.getSelectedItem()));
		consultationTypeComboBox.setEnabled(isActive);
		appointmentTypeComboBox.setEnabled(isActive);
		searchBtn.setEnabled(isActive);

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

		participantTableModel.setRowCount(0);
		participantDetails.clear();
		pendingParticipants.clear();
		tempIdCounter = -1; // Reset for next session
		selectedAppointmentId = null;
		sessionSummaryArea.setText("");
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

		// Check for duplicates
		boolean isDuplicate = tempParticipants.stream().anyMatch(p -> p.isStudent() && p.getStudentUid() != null
				&& p.getStudentUid().equals(participant.getStudentUid()));

		if (!isDuplicate) {
			tempParticipants.add(participant);
			updateParticipantsTable();
		} else {
			JOptionPane.showMessageDialog(this, "This student is already added as a participant.", "Duplicate Entry",
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
		mainPanel = new JPanel(new MigLayout("insets 20", "[grow]", "[][][]"));
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
		JPanel bottomFields = new JPanel(new MigLayout("insets 0", "[grow]10[grow]", "[][][]"));

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
			if (!"Others".equals(selected)) {
				customViolationField.setText(""); // Clear the text when hidden
			}
		});

		sessionInfoCard.add(topFields, "cell 0 0, growx");
		sessionInfoCard.add(bottomFields, "cell 0 1, growx");

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
		tabbedPane.addTab("Participants", null, participantsPanel, "Click to view/add to Participants List");

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
										String consultationType = searchPanel.getSelectedConsultationType();
										appointmentTypeComboBox.setSelectedItem("From Appointment");
										consultationTypeComboBox.setSelectedItem(consultationType);
										selectedAppointmentId = selectedId;
										populateParticipantsFromAppointment(selectedId);
										controller.close();
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
					for (Participants participant : appointment.getParticipants()) {
						Map<String, String> details = new HashMap<>();
						details.put("firstName", participant.getParticipantFirstName());
						details.put("lastName", participant.getParticipantLastName());
						details.put("fullName",
								participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
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

			if (isEditing && currentSession != null) {
				currentSession.setAppointmentType(appointmentType);
				currentSession.setConsultationType(consultationType);
				currentSession.setSessionDateTime(parseDateTime());
				currentSession.setSessionNotes(notes);
				currentSession.setSessionSummary(summary);
				currentSession.setSessionStatus(sessionStatus);

				sessionsDAO.updateSession(currentSession);

				if ("Ended".equals(sessionStatus) && currentSession.getAppointmentId() != null) {
					AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
					Appointment appointment = appointmentDAO.getAppointmentById(currentSession.getAppointmentId());
					if (appointment != null) {
						appointment.setAppointmentStatus("Ended");
						appointmentDAO.updateAppointment(appointment);
					}
				}

				JOptionPane.showMessageDialog(this, "Session updated successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);

				if (saveCallback != null) {
					saveCallback.run();
				}

				if ("Ended".equals(sessionStatus)) {
					disableFieldsForEndedSession();
				}
			} else {
				int guidanceCounselorId;
				if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
					guidanceCounselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
				} else {
					JOptionPane.showMessageDialog(this, "No guidance counselor is currently logged in.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				Sessions session = new Sessions(0, appointmentId, guidanceCounselorId, null, appointmentType,
						consultationType, parseDateTime(), notes, summary, sessionStatus,
						new java.sql.Timestamp(System.currentTimeMillis()));

				int sessionId = sessionsDAO.addSession(session);

				if (sessionId > 0) {
					ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
					Map<Integer, Integer> idMapping = new HashMap<>();

					// Handle both old pendingParticipants and new tempParticipants
					// First, process the table model entries
					for (int i = 0; i < participantTableModel.getRowCount(); i++) {
						int id = (int) participantTableModel.getValueAt(i, 4);

						if (id < 0) {
							// This could be from pendingParticipants or derived from tempParticipants
							Participants participant = pendingParticipants.get(id);
							if (participant != null) {
								int realId = participantsDAO.createParticipant(participant);
								sessionsDAO.addParticipantToSession(sessionId, realId);
								idMapping.put(id, realId);
							} else {
								// Try to find the corresponding tempParticipant and create a Participants
								// object
								for (TempParticipant tempParticipant : tempParticipants) {
									if (!tempParticipant.isStudent()) { // For non-students with negative IDs
										Participants newParticipant = new Participants();
										newParticipant.setParticipantFirstName(tempParticipant.getFirstName());
										newParticipant.setParticipantLastName(tempParticipant.getLastName());
										newParticipant.setParticipantType(tempParticipant.getType());
										newParticipant.setSex(tempParticipant.getSex());
										newParticipant.setContactNumber(tempParticipant.getContactNumber());

										int realId = participantsDAO.createParticipant(newParticipant);
										sessionsDAO.addParticipantToSession(sessionId, realId);
										idMapping.put(id, realId);
										break;
									}
								}
							}
						} else {
							// This is an existing participant
							sessionsDAO.addParticipantToSession(sessionId, id);
							idMapping.put(id, id);
						}
					}

					// Now handle tempParticipants that are students (they have positive IDs)
					for (TempParticipant tempParticipant : tempParticipants) {
						if (tempParticipant.isStudent() && tempParticipant.getStudentUid() != null) {
							// Check if this student is already processed
							boolean alreadyProcessed = false;
							for (int i = 0; i < participantTableModel.getRowCount(); i++) {
								int id = (int) participantTableModel.getValueAt(i, 4);
								if (id > 0 && id == tempParticipant.getStudentUid()) {
									alreadyProcessed = true;
									break;
								}
							}

							if (!alreadyProcessed) {
								// Create a new participant for this student
								Participants newParticipant = new Participants();
								newParticipant.setStudentUid(tempParticipant.getStudentUid());
								newParticipant.setParticipantFirstName(tempParticipant.getFirstName());
								newParticipant.setParticipantLastName(tempParticipant.getLastName());
								newParticipant.setParticipantType(tempParticipant.getType());
								newParticipant.setSex(tempParticipant.getSex());
								newParticipant.setContactNumber(tempParticipant.getContactNumber());

								int realId = participantsDAO.createParticipant(newParticipant);
								sessionsDAO.addParticipantToSession(sessionId, realId);
								idMapping.put(tempParticipant.getStudentUid(), realId);
							}
						}
					}

					if ("Ended".equals(sessionStatus) && appointmentId != null) {
						AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
						Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
						if (appointment != null) {
							appointment.setAppointmentStatus("Ended");
							appointmentDAO.updateAppointment(appointment);
						}
					}

					if (violation != null && !violation.equals("-- Select Violation --")
							&& !violation.equals("No Violation")) {
						ViolationDAO ViolationDAO = new ViolationDAO(connect);

						// Process violations for all participants
						for (Integer participantId : idMapping.values()) {
							boolean success = ViolationDAO.addViolation(participantId, violationText, violationText,
									summary, notes, "Active", new java.sql.Timestamp(System.currentTimeMillis()));

							if (!success) {
								throw new Exception(
										"Failed to save violation record for participant ID: " + participantId);
							}
						}
					}

					JOptionPane.showMessageDialog(this, "Session and participants saved successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);

					refreshViolations();

					if (saveCallback != null) {
						saveCallback.run();
					}

					clearFields();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to save session.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private boolean validateInputs() {
		StringBuilder errors = new StringBuilder();

		String status = (String) sessionStatusComboBox.getSelectedItem();
		if ("Select Status".equals(status)) {
			errors.append("- Please select a session status\n");
		}

		String consultationType = (String) consultationTypeComboBox.getSelectedItem();
		if (consultationType == null || consultationType.trim().isEmpty()) {
			errors.append("- Please select a consultation type\n");
		}

		String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
		if (appointmentType == null || appointmentType.trim().isEmpty()) {
			errors.append("- Please select an appointment type\n");
		}

		String summary = sessionSummaryArea.getText().trim();
		if (summary.isEmpty()) {
			errors.append("- Please enter a session summary\n");
		}

		String violation = (String) violationField.getSelectedItem();
		if ("Others".equals(violation)) {
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
			appointmentTypeComboBox.setSelectedItem("Scheduled");
			selectedAppointmentId = appointment.getAppointmentId();
			consultationTypeComboBox.setSelectedItem(appointment.getConsultationType());
			populateParticipantsFromAppointment(appointment.getAppointmentId());

			if (appointment.getAppointmentDateTime() != null) {
				java.time.LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
				sessionDatePicker.setSelectedDate(dateTime.toLocalDate());
				sessionTimePicker.setSelectedTime(dateTime.toLocalTime());
			}

			if (appointment.getAppointmentNotes() != null) {
				notesArea.setText(appointment.getAppointmentNotes());
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
}