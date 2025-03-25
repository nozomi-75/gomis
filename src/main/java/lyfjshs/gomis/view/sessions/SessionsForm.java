package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.view.appointment.AppointmentSearchPanel;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import lyfjshs.gomis.view.violation.Violation_Record;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class SessionsForm extends Form implements Printable {
	private JComboBox<String> violationField;
	private JFormattedTextField timeField, dateField; // Declare the missing dateField
	private JTextArea sessionSummaryArea, notesArea;
	private JButton saveButton, printButton, searchStudentButton;
	private JComboBox<String> participantsComboBox;
	private JComboBox<String> consultationTypeComboBox;
	private JComboBox<String> appointmentTypeComboBox;
	private Connection connect;
	private JPanel mainPanel;
	private JComboBox<String> sexCBox;
	private JTextField firstNameField, lastNameField, contactNumberField;
	private JPanel contentPanel;
	private JPanel panel;
	private JSeparator separator;
	private JPanel panel_1;
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

	// Violation type arrays
	private String[] violations = { "Absence/Late", "Minor Property Damage", "Threatening/Intimidating",
			"Pornographic Materials", "Gadget Use in Class", "Cheating", "Stealing", "No Pass", "Bullying",
			"Sexual Abuse", "Illegal Drugs", "Alcohol", "Smoking/Vaping", "Gambling", "Public Display of Affection",
			"Fighting/Weapons", "Severe Property Damage", "Others" };

	private JTextField recordedByField;
	private JLabel lblSessionTime;

	public SessionsForm(Connection conn) {
		this.connect = conn;
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

		sessionTimePicker = new TimePicker();
		sessionDatePicker = new DatePicker();

		// Initialize custom violation field
		customViolationField = new JTextField(15);
		customViolationField.setVisible(false);
		customViolationField.setEnabled(false);

		// Initialize violation field with combined offenses
		violationField = new JComboBox<>();
		violationField.addItem("-- Select Violation --");
		for (String violation : violations) {
			violationField.addItem(violation);
		}

		// Add listener to update violation type label and show/hide custom field
		violationField.addActionListener(e -> {
			String selected = (String) violationField.getSelectedItem();
			if (selected == null || selected.equals("-- Select Violation --")) {
				customViolationField.setEnabled(false);
				customViolationField.setVisible(false);
				customViolationField.setText(""); // Clear the text when hidden
			} else if (selected.equals("Others")) {
				customViolationField.setEnabled(true);
				customViolationField.setVisible(true);
			} else {
				customViolationField.setEnabled(false);
				customViolationField.setVisible(false);
				customViolationField.setText(""); // Clear the text when hidden
			}
		});

		// Initialize recorded by field as JTextField
		recordedByField = new JTextField();
		recordedByField.setEditable(false); // Make it read-only
	}

	private void toggleSearchStudentButton() {
		searchStudentButton.setEnabled("Student".equals(participantsComboBox.getSelectedItem()));
	}

	private void openStudentSearchUI() {
		String modalId = "session_student_search"; // Use unique modal ID
		if (ModalDialog.isIdExist(modalId)) {
			return;
		}

		// Create an anonymous class that extends the abstract StudentSearchPanel
		StudentSearchPanel searchPanel = new StudentSearchPanel(connect, student -> {
			// Create new participant
			Participants participant = new Participants();
			participant.setStudentUid(student.getStudentUid());
			participant.setParticipantType("Student");
			participant.setParticipantLastName(student.getStudentLastname());
			participant.setParticipantFirstName(student.getStudentFirstname());
			participant.setSex(student.getStudentSex());
			participant.setContactNumber(student.getContact().getContactNumber());

			// Generate temporary ID
			int tempId = tempIdCounter--;

			// Store in pendingParticipants
			pendingParticipants.put(tempId, participant);

			Map<String, String> details = new HashMap<>();
			details.put("firstName", participant.getParticipantFirstName());
			details.put("lastName", participant.getParticipantLastName());
			details.put("fullName", participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
			details.put("type", "Student");
			details.put("contact", participant.getContactNumber());
			details.put("sex", participant.getSex());
			participantDetails.put(tempId, details);

			// Add to table with row number and hidden tempId
			int rowNumber = participantTableModel.getRowCount() + 1;
			participantTableModel
					.addRow(new Object[] { rowNumber, details.get("fullName"), "Student", "View | Remove", tempId });

			// Close the modal using the correct ID
			ModalDialog.closeModal(modalId);
		}) {
			@Override
			protected void onStudentSelected(Student student) {
				// This is already handled by the callback
			}
		};

		// Configure modal options
		ModalDialog.getDefaultOption()
				.setOpacity(0f)
				.setAnimationOnClose(false)
				.getBorderOption()
				.setBorderWidth(0.5f)
				.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

		// Show modal with the correct ID
		ModalDialog.showModal(this, searchPanel, modalId);
		ModalDialog.getDefaultOption().getLayoutOption().setSize(700, 500);
	}

	public void setSaveCallback(Runnable saveCallback) {
		this.saveCallback = saveCallback;
	}

	private void clearFields() {
		firstNameField.setText("");
		lastNameField.setText("");
		contactNumberField.setText("");
		notesArea.setText("");
		sessionSummaryArea.setText("");
		timeField.setText("");
		dateField.setText("");

		participantsComboBox.setSelectedIndex(0);
		consultationTypeComboBox.setSelectedIndex(0);
		appointmentTypeComboBox.setSelectedIndex(0);
		sexCBox.setSelectedIndex(0);
		violationField.setSelectedIndex(0);

		participantTableModel.setRowCount(0);
		participantDetails.clear();
		pendingParticipants.clear();
		tempIdCounter = -1; // Reset for next session
		selectedAppointmentId = null;
	}

	private void addParticipant() {
		String firstName = firstNameField.getText();
		String lastName = lastNameField.getText();
		String contact = contactNumberField.getText();
		String sex = (String) sexCBox.getSelectedItem();

		// Validate input
		if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter at least first and last name", "Missing Information",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Create new participant
		Participants participant = new Participants();
		participant.setStudentUid(null);
		participant.setParticipantType("Non-Student");
		participant.setParticipantLastName(lastName);
		participant.setParticipantFirstName(firstName);
		participant.setSex(sex);
		participant.setContactNumber(contact);

		// Generate temporary ID
		int tempId = tempIdCounter--;

		// Store in pendingParticipants
		pendingParticipants.put(tempId, participant);

		// Store participant details
		Map<String, String> details = new HashMap<>();
		details.put("firstName", firstName);
		details.put("lastName", lastName);
		details.put("fullName", firstName + " " + lastName);
		details.put("contact", contact);
		details.put("sex", sex);
		details.put("type", "Non-Student");
		participantDetails.put(tempId, details);

		// Add to table with row number and hidden tempId
		int rowNumber = participantTableModel.getRowCount() + 1;
		participantTableModel
				.addRow(new Object[] { rowNumber, details.get("fullName"), "Non-Student", "View | Remove", tempId });

		// Clear input fields
		firstNameField.setText("");
		lastNameField.setText("");
		contactNumberField.setText("");
		sexCBox.setSelectedIndex(0);
	}

	private void setupParticipantTableListener() {
		participantTable.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = participantTable.rowAtPoint(evt.getPoint());
				int col = participantTable.columnAtPoint(evt.getPoint());

				if (col == 3) { // Actions column
					int id = (int) participantTableModel.getValueAt(row, 4); // Get tempId or actual ID from hidden
																				// column

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
							// Remove from maps if new participant
							participantDetails.remove(id);
							pendingParticipants.remove(id); // Only affects new participants with tempId

							// Remove row from table
							participantTableModel.removeRow(row);

							// Update row numbers
							for (int i = 0; i < participantTableModel.getRowCount(); i++) {
								participantTableModel.setValueAt(i + 1, i, 0);
							}
						}
					}
				}
			}
		});
	}

	private void layoutComponents() {
		this.setLayout(new MigLayout("gap 10", "[grow]", "[grow]"));
		contentPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		add(contentPanel, "cell 0 0,grow");
		sessionSummaryArea = new JTextArea(4, 20);

		saveButton = new JButton("SAVE");
		saveButton.setBackground(new Color(70, 130, 180));
		saveButton.setForeground(Color.WHITE);
		saveButton.setFocusPainted(false);
		saveButton.addActionListener(e -> saveSessionToDatabase());

		// Header with improved styling
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contentPanel.add(headerPanel, "cell 0 0,growx");
		JLabel headerLabel = new JLabel("Session Documentation Form");
		headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
		headerLabel.setForeground(Color.WHITE);
		headerPanel.add(headerLabel);
		headerPanel.setBackground(new Color(5, 117, 230));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		firstNameField = new JTextField(10);
		lastNameField = new JTextField(10);

		// Main Panel with improved spacing
		mainPanel = new JPanel(new MigLayout("wrap, gap 15, hidemode 3, insets 20", "[][grow][20px][][]",
				"[][][][][][][fill][][grow][][]"));
		contentPanel.add(mainPanel, "cell 0 1,grow");
		mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),
				BorderFactory.createLineBorder(new Color(200, 200, 200))));

		separator = new JSeparator();
		separator.setSize(new Dimension(20, 20));
		separator.setBackground(Color.GRAY);
		separator.setForeground(Color.DARK_GRAY);
		separator.setOrientation(SwingConstants.VERTICAL);
		mainPanel.add(separator, "cell 2 0 1 7");

		// Participant Panel
		JPanel participantPanel = new JPanel(new MigLayout("gap 10", "[][][][][]", "[][][][]"));
		participantPanel.setBorder(BorderFactory.createTitledBorder("Add Participant"));

		// Participant
		JLabel participantLabel = new JLabel("Participant Type: ");
		participantPanel.add(participantLabel, "cell 0 0");
		participantsComboBox = new JComboBox<>(new String[] { "Student", "Non-Student" });
		participantPanel.add(participantsComboBox, "cell 1 0 2 1,growx");
		participantsComboBox.addActionListener(e -> toggleSearchStudentButton());

		searchStudentButton = new JButton("Search Student");
		participantPanel.add(searchStudentButton, "cell 3 0");
		searchStudentButton.setEnabled(false);
		searchStudentButton.addActionListener(e -> openStudentSearchUI());

		participantPanel.add(new JLabel("First Name"), "cell 0 1");
		participantPanel.add(firstNameField, "cell 1 1,growx");

		participantPanel.add(new JLabel("Last Name"), "cell 3 1");
		participantPanel.add(lastNameField, "cell 4 1");

		JLabel lblSexl = new JLabel("Sex: ");
		participantPanel.add(lblSexl, "flowx,cell 0 2");

		mainPanel.add(participantPanel, "cell 1 0 1 5,growx"); // Initially visible

		JButton saveParticipantButton = new JButton("Add Participant");
		saveParticipantButton.addActionListener(e -> addParticipant());
		sexCBox = new JComboBox<>(new String[] { "Male", "Female" });
		participantPanel.add(sexCBox, "cell 1 2,growx");

		JLabel label = new JLabel("Contact Number");
		participantPanel.add(label, "flowx,cell 3 2");
		contactNumberField = new JTextField(10);
		participantPanel.add(contactNumberField, "cell 4 2,growx");
		participantPanel.add(saveParticipantButton, "cell 4 3,alignx center");

		// Start Time
		JLabel startTimeLabel = new JLabel("Session Date");
		mainPanel.add(startTimeLabel, "flowx,cell 3 1");

		// Initialize the dateField
		dateField = new JFormattedTextField();
		sessionDatePicker.setSelectedDate(LocalDate.now());
		sessionDatePicker.setEditor(dateField);
		dateField.setColumns(10);
		mainPanel.add(dateField, "cell 3 1,growx");

		lblSessionTime = new JLabel("Session Time");
		mainPanel.add(lblSessionTime, "flowx,cell 3 2");

		timeField = new JFormattedTextField();
		sessionTimePicker.setSelectedTime(LocalTime.now());
		sessionTimePicker.setEditor(timeField);
		timeField.setColumns(10);
		mainPanel.add(timeField, "cell 3 2,growx");

		// Appointment Type
		JLabel appointmentTypeLabel = new JLabel("Appointment Type");
		mainPanel.add(appointmentTypeLabel, "flowx,cell 3 3,alignx left");

		searchBtn = new JButton("Search Appointment");
		mainPanel.add(searchBtn, "cell 4 3");

		// Consultation Type
		JLabel consultationTypeLabel = new JLabel("Consultation Type");
		mainPanel.add(consultationTypeLabel, "flowx,cell 3 4,aligny top");

		// Violation type with label and custom field
		JLabel violationLabel = new JLabel("Violation Type: ");
		violationLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		mainPanel.add(violationLabel, "cell 1 5");

		// Create a panel to hold both the combo box and custom field with proper layout
		JPanel violationPanel = new JPanel(new MigLayout("insets 0", "[][grow]", "[]"));
		violationPanel.add(violationField, "cell 0 0");

		// Add custom violation field with label
		JLabel otherLabel = new JLabel("Other:");
		otherLabel.setVisible(false);
		violationPanel.add(otherLabel, "cell 1 0");

		// Setup custom violation field
		customViolationField = new JTextField(20);
		customViolationField.setVisible(false);
		violationPanel.add(customViolationField, "cell 1 0, growx");

		// Add listener to show/hide the "Other:" label and field
		violationField.addActionListener(e -> {
			String selected = (String) violationField.getSelectedItem();
			boolean isOthers = "Others".equals(selected);
			otherLabel.setVisible(isOthers);
			customViolationField.setVisible(isOthers);
			customViolationField.setEnabled(isOthers);
			violationPanel.revalidate();
			violationPanel.repaint();
		});

		mainPanel.add(violationPanel, "cell 1 5 2 1, growx");

		// Participant Table
		participantTable = new JTable(participantTableModel);
		participantTable.setPreferredScrollableViewportSize(new Dimension(400, 100));
		participantTable.getColumnModel().removeColumn(participantTable.getColumnModel().getColumn(4));
		JScrollPane tableScrollPane = new JScrollPane(participantTable);
		tableScrollPane.setBorder(BorderFactory.createTitledBorder("Participant Table")); // pane

		panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout()); // Use BorderLayout for better control
		panel_1.add(tableScrollPane, BorderLayout.CENTER); // Add the scroll pane to the panel
		mainPanel.add(panel_1, "cell 1 6,grow"); // Adjust the cell position to align with the participant panel

		// Setup listener for participant table actions
		setupParticipantTableListener();

		panel = new JPanel();
		mainPanel.add(panel, "cell 3 6 2 1,grow");
		panel.setLayout(new MigLayout("", "[][grow]", "[grow]"));

		// Notes
		JLabel notesLabel = new JLabel("Notes: ");
		panel.add(notesLabel, "cell 0 0,aligny top");
		notesArea = new JTextArea(4, 20);
		JScrollPane notesScrollPane = new JScrollPane(notesArea);
		panel.add(notesScrollPane, "cell 1 0,grow");

		// Session Summary
		JLabel summaryLabel = new JLabel("Summary: ");
		summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
		mainPanel.add(summaryLabel, "cell 1 7 4 1,alignx left,aligny bottom");
		JScrollPane summaryScrollPane = new JScrollPane(sessionSummaryArea);
		mainPanel.add(summaryScrollPane, "cell 1 8 4 1,grow");

		printButton = new JButton("PRINT");
		printButton.setBackground(new Color(70, 130, 180));
		printButton.setForeground(Color.WHITE);
		printButton.setFocusPainted(false);
		printButton.addActionListener(e -> printSessionDetails());

		// Replace JComboBox with JTextField for Recorded By
		mainPanel.remove(recordedByField);
		JLabel recordedByLabel = new JLabel("Recorded By:");
		recordedByLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		mainPanel.add(recordedByLabel, "flowx,cell 3 9");
		mainPanel.add(recordedByField, "cell 3 9,growx");

		// Buttons
		mainPanel.add(printButton, "flowx,cell 4 9,growx");
		mainPanel.add(saveButton, "cell 4 9,growx");

		consultationTypeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance",
				"Personal Consultation", "Behavioral Consultation", "Group Consultation" });
		mainPanel.add(consultationTypeComboBox, "cell 3 4,growx,aligny top");

		appointmentTypeComboBox = new JComboBox<>(new String[] { "Walk-in", "Scheduled" });
		mainPanel.add(appointmentTypeComboBox, "cell 3 3,growx");

		searchBtn.addActionListener(e -> openAppointmentSearchDialog());
	}

	private void printSessionDetails() {
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		printerJob.setPrintable(this);
		if (printerJob.printDialog()) {
			try {
				printerJob.print();
			} catch (PrinterException e) {
				JOptionPane.showMessageDialog(this, "Printing Error: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.translate(pf.getImageableX(), pf.getImageableY());
		this.printAll(g);
		return PAGE_EXISTS;
	}

	// Add this method to display participant details
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
				String counselorName = Main.formManager.getCounselorObject().getFirstName() + " "
						+ Main.formManager.getCounselorObject().getMiddleName() + " "
						+ Main.formManager.getCounselorObject().getLastName();
				if (counselorName != null && !counselorName.isEmpty()) {
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

			// Configure default modal options to match TestModal
			ModalDialog.getDefaultOption().setOpacity(0f) // Transparent background
					.setAnimationOnClose(false) // No close animation
					.getBorderOption().setBorderWidth(0.5f) // Thin border
					.setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM); // Medium shadow

			// Show the modal dialog
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

			// Set size
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
				participantTableModel.setRowCount(0); // Clear existing participants

				if (appointment.getParticipants() != null) {
					for (Participants participant : appointment.getParticipants()) {
						// Store participant details with actual ID
						Map<String, String> details = new HashMap<>();
						details.put("firstName", participant.getParticipantFirstName());
						details.put("lastName", participant.getParticipantLastName());
						details.put("fullName",
								participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
						details.put("type", participant.getParticipantType());
						details.put("contact", participant.getContactNumber());
						details.put("sex", participant.getSex());
						participantDetails.put(participant.getParticipantId(), details);

						// Add to table with row number and actual ID
						int rowNumber = participantTableModel.getRowCount() + 1;
						participantTableModel.addRow(new Object[] { rowNumber, details.get("fullName"),
								participant.getParticipantType(), "View | Remove", participant.getParticipantId() });
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
			int guidanceCounselorId;
			if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
				guidanceCounselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
			} else {
				JOptionPane.showMessageDialog(this, "No guidance counselor is currently logged in.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// First create the session without violation records
			String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
			String consultationType = (String) consultationTypeComboBox.getSelectedItem();
			String violation = (String) violationField.getSelectedItem();
			String notes = notesArea.getText();

			Integer appointmentId = "Walk-in".equals(appointmentType) ? null : selectedAppointmentId;

			String violationText = "Others".equals(violation) ? customViolationField.getText().trim() : violation;

			// Validate custom violation text
			if ("Others".equals(violation) && (violationText == null || violationText.trim().isEmpty())) {
				JOptionPane.showMessageDialog(this,
						"Please enter a custom violation type.",
						"Missing Information",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			Sessions session = new Sessions(0, appointmentId, guidanceCounselorId, null, appointmentType,
					consultationType, new java.sql.Timestamp(System.currentTimeMillis()), notes, "Active",
					new java.sql.Timestamp(System.currentTimeMillis()));

			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			int sessionId = sessionsDAO.addSession(session);

			if (sessionId > 0) {
				ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
				// Map to track temporary IDs to real database IDs
				Map<Integer, Integer> idMapping = new HashMap<>();

				// Process all participants in the table
				for (int i = 0; i < participantTableModel.getRowCount(); i++) {
					int id = (int) participantTableModel.getValueAt(i, 4); // Get ID from hidden column

					if (id < 0) {
						// New participant with temporary ID
						Participants participant = pendingParticipants.get(id);
						int realId = participantsDAO.createParticipant(participant); // Save to DB, get actual ID
						sessionsDAO.addParticipantToSession(sessionId, realId);
						idMapping.put(id, realId); // Store mapping from temp ID to real ID
					} else {
						// Existing participant with actual ID
						sessionsDAO.addParticipantToSession(sessionId, id);
						idMapping.put(id, id); // Map ID to itself for consistency
					}
				}

				// NOW create violation records if there's a violation, using real participant
				// IDs
				if (violation != null && !violation.equals("-- Select Violation --")) {
					ViolationCRUD violationCRUD = new ViolationCRUD(connect);

					// For each participant, create a violation record using real IDs
					for (int i = 0; i < participantTableModel.getRowCount(); i++) {
						int tempId = (int) participantTableModel.getValueAt(i, 4);
						int realParticipantId = idMapping.get(tempId); // Get the real ID from our mapping

						// Create violation record with real participant ID
						boolean success = violationCRUD.addViolation(
								realParticipantId,
								violationText, // Use custom text if "Others" is selected
								violationText, // Use custom text as description
								sessionSummaryArea.getText(), // Use session summary as anecdotal record
								notesArea.getText(), // Use notes as reinforcement
								"Active",
								new java.sql.Timestamp(System.currentTimeMillis()));

						if (!success) {
							throw new Exception(
									"Failed to save violation record for participant ID: " + realParticipantId);
						}
					}
				}

				JOptionPane.showMessageDialog(this, "Session and participants saved successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);

				// Refresh violation records if they exist in the system
				refreshViolationRecords();

				if (saveCallback != null) {
					saveCallback.run();
				}

				clearFields();
			} else {
				JOptionPane.showMessageDialog(this, "Failed to save session.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void refreshViolationRecords() {
		// Find and refresh any open violation record forms
		if (Main.formManager != null) {
			Form[] forms = FormManager.getForms();
			for (Form form : forms) {
				if (form instanceof Violation_Record) {
					((Violation_Record) form).refreshViolations();
				}
			}
		}
	}

	public void populateFromAppointment(Appointment appointment) {
		if (appointment != null) {
			// Set appointment type and ID
			appointmentTypeComboBox.setSelectedItem("Scheduled");
			selectedAppointmentId = appointment.getAppointmentId();

			// Set consultation type
			consultationTypeComboBox.setSelectedItem(appointment.getConsultationType());

			// Populate participants
			populateParticipantsFromAppointment(appointment.getAppointmentId());

			// Set date and time if available
			if (appointment.getAppointmentDateTime() != null) {
				java.time.LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
				sessionDatePicker.setSelectedDate(dateTime.toLocalDate());
				sessionTimePicker.setSelectedTime(dateTime.toLocalTime());
			}

			// If there are notes, populate them
			if (appointment.getAppointmentNotes() != null) {
				notesArea.setText(appointment.getAppointmentNotes());
			}
		}
	}
}