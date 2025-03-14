package lyfjshs.gomis.view.sessions;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.view.appointment.StudentSearchUI;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Rectangle;
import java.awt.Dimension;
import javax.swing.JFrame;
import java.awt.GridLayout;
import java.awt.BorderLayout;

public class SessionsForm extends Form implements Printable {
	private JComboBox<String> violationField, recordedByField;
	private JFormattedTextField sessionDateTimeField, updatedATField;
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
	private JButton searchBtn;
	private JFormattedTextField dateField; // Declare the missing dateField
	private Runnable saveCallback;

	public SessionsForm(Connection conn) {
		this.connect = conn;
		initializeComponents();
		layoutComponents();
	}

	private void initializeComponents() {
		// Initialize the table model
		participantTableModel = new DefaultTableModel(
				new Object[] { "#", "Participant Name", "Participant Type", "Actions" }, 0);
	}

	private void toggleSearchStudentButton() {
		searchStudentButton.setEnabled("Student".equals(participantsComboBox.getSelectedItem()));
	}

	private void openStudentSearchUI() {
		StudentSearchUI studentSearchUI = new StudentSearchUI();
		studentSearchUI.createAndShowGUI();
	}

	private int getAppointmentId() {
		// Implement logic to retrieve the actual appointment ID
		// This is a placeholder implementation and should be replaced with actual logic
		// For example, you might query the database or get the ID from a selected
		// appointment
		return 1; // Replace with actual appointment ID retrieval logic
	}

	public void setSaveCallback(Runnable saveCallback) {
		this.saveCallback = saveCallback;
	}

	private void saveSession() {
		try {
			String date = dateField.getText();
			String participants = (String) participantsComboBox.getSelectedItem();
			String violation = (String) violationField.getSelectedItem();
			String recordedBy = (String) recordedByField.getSelectedItem();
			String notes = notesArea.getText();
			String summary = sessionSummaryArea.getText();
			String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();

			int appointmentId = 0; // Default value for Walk-in
			if (!"Walk-in".equals(appointmentType)) {
				// Retrieve the actual appointment ID if not Walk-in
				appointmentId = getAppointmentId();
			}

			// Create a new Session object using the constructor
			Sessions session = new Sessions(0, appointmentId, // appointmentId
					0, // counselorsId (not retrieved in the query)
					participants.equals("Student") ? 1 : 0, // Use 1 for Student, 0 for Non-Student
					0, // violationId (not retrieved in the query)
					violation, // sessionType
					appointmentType, 
					new java.sql.Timestamp(System.currentTimeMillis()), // sessionDateTime (current time)
					notes, // sessionNotes
					"Active", // sessionStatus
					new java.sql.Timestamp(System.currentTimeMillis()) // updatedAt
			);

			// Use SessionsDAO to save the session
			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			sessionsDAO.addSession(session); // Assuming you have an addSession method in SessionsDAO

			JOptionPane.showMessageDialog(this, "Session saved successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
			
			if (saveCallback != null) {
				saveCallback.run();
			}
			//after saving, clear the fields
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void clearFields(){
		// Clear all fields after saving
		
		
	}

	private void addParticipant() {
		String firstName = firstNameField.getText();
		String lastName = lastNameField.getText();
		String contact = contactNumberField.getText();
		String email = (String) sexCBox.getSelectedItem();

		// Validate that at least name fields are filled
		if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter at least first and last name", "Missing Information",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Add the new participant to the table
		String fullName = firstName + " " + lastName;
		int rowNum = participantTableModel.getRowCount() + 1; // Get the next row number
		participantTableModel.addRow(new Object[] { rowNum, fullName, "Non-Student", "View | Remove" }); // Add to the
																											// table

		// Store all participant details for later viewing
		Map<String, String> details = new HashMap<>();
		details.put("firstName", firstName);
		details.put("lastName", lastName);
		details.put("fullName", fullName);
		details.put("contact", contact);
		details.put("email", email);
		details.put("type", "Non-Student");

		// Store using row index as key
		participantDetails.put(rowNum, details);

		// Clear the input fields after saving
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

				// Check if the "Actions" column was clicked
				if (col == 3) {
					String action = (String) participantTable.getValueAt(row, col);
					int rowNumber = (int) participantTable.getValueAt(row, 0);

					// Parse click position to determine if View or Remove was clicked
					int clickX = evt.getX();
					int cellX = participantTable.getCellRect(row, col, false).x;
					int relativeX = clickX - cellX;

					// Approximate width of "View" text - adjust as needed
					int viewWidth = 40;

					if (relativeX <= viewWidth) {
						// "View" part was clicked
						showParticipantDetails(rowNumber);
					} else {
						// "Remove" part was clicked
						int option = JOptionPane.showConfirmDialog(SessionsForm.this,
								"Are you sure you want to remove this participant?", "Confirm Remove",
								JOptionPane.YES_NO_OPTION);
						if (option == JOptionPane.YES_OPTION) {
							// Remove participant from the table
							participantTableModel.removeRow(row);
							// Also remove from our details map
							participantDetails.remove(rowNumber);
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
		saveButton.addActionListener(e -> saveSession());

		// Header
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contentPanel.add(headerPanel, "cell 0 0,growx");
		JLabel headerLabel = new JLabel("Session Documentation Form");
		headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		headerPanel.add(headerLabel);
		headerPanel.setBackground(new Color(5, 117, 230));
		headerPanel.setForeground(Color.WHITE);

		firstNameField = new JTextField(10);
		lastNameField = new JTextField(10);

		// Main Panel
		mainPanel = new JPanel(
				new MigLayout("wrap, gap 10, hidemode 3", "[][grow][20px][][]", "[][][][][][fill][][grow][][]"));
		contentPanel.add(mainPanel, "cell 0 1,grow");
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		separator = new JSeparator();
		separator.setSize(new Dimension(20, 20));
		separator.setBounds(new Rectangle(0, 0, 5, 5));
		separator.setBackground(Color.GRAY);
		separator.setForeground(Color.DARK_GRAY);
		separator.setOrientation(SwingConstants.VERTICAL);
		mainPanel.add(separator, "cell 2 0 1 6");

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

		mainPanel.add(participantPanel, "cell 1 0 1 4,growx"); // Initially visible

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
		JLabel startTimeLabel = new JLabel("Session Date & Time");
		mainPanel.add(startTimeLabel, "flowx,cell 3 1");

		// End Time
		JLabel endTimeLabel = new JLabel("Last Updated:");
		mainPanel.add(endTimeLabel, "flowx,cell 4 1");

		// Appointment Type
		JLabel appointmentTypeLabel = new JLabel("Appointment Type");
		mainPanel.add(appointmentTypeLabel, "flowx,cell 3 2,alignx left");

		searchBtn = new JButton("Search Appointment");
		mainPanel.add(searchBtn, "cell 4 2");

		// Consultation Type
		JLabel consultationTypeLabel = new JLabel("Consultation Type");
		mainPanel.add(consultationTypeLabel, "flowx,cell 3 3,aligny top");

		// Violation
		JLabel violationLabel = new JLabel("Violation Type: ");
		mainPanel.add(violationLabel, "flowx,cell 1 4,aligny top");

		// Participant Table
		participantTable = new JTable(participantTableModel);
		participantTable.setPreferredScrollableViewportSize(new Dimension(400, 100)); // Set preferred size for the
																						// table
		JScrollPane tableScrollPane = new JScrollPane(participantTable); // Wrap the table in a scroll pane
		tableScrollPane.setBorder(BorderFactory.createTitledBorder("Participant Table")); // Set border for the scroll
																							// pane

		panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout()); // Use BorderLayout for better control
		panel_1.add(tableScrollPane, BorderLayout.CENTER); // Add the scroll pane to the panel
		mainPanel.add(panel_1, "cell 1 5,grow"); // Adjust the cell position to align with the participant panel

		// Setup listener for participant table actions
		setupParticipantTableListener();

		panel = new JPanel();
		mainPanel.add(panel, "cell 3 5 2 1,grow");
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
		mainPanel.add(summaryLabel, "cell 1 6 4 1,alignx left,aligny bottom");
		JScrollPane summaryScrollPane = new JScrollPane(sessionSummaryArea);
		mainPanel.add(summaryScrollPane, "cell 1 7 4 1,grow");

		printButton = new JButton("PRINT");
		printButton.setBackground(new Color(70, 130, 180));
		printButton.setForeground(Color.WHITE);
		printButton.setFocusPainted(false);
		printButton.addActionListener(e -> printSessionDetails());

		// Recorded By
		JLabel recordedByLabel = new JLabel("Recorded By");
		mainPanel.add(recordedByLabel, "flowx,cell 3 8");

		// Buttons
		mainPanel.add(printButton, "flowx,cell 4 8,growx");
		mainPanel.add(saveButton, "cell 4 8,growx");
		recordedByField = new JComboBox<>();
		mainPanel.add(recordedByField, "cell 3 8,growx");
		violationField = new JComboBox<>();
		mainPanel.add(violationField, "cell 1 4,growx,aligny top");

		consultationTypeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance",
				"Personal Consultation", "Behavioral Consultation", "Group Consultation" });
		mainPanel.add(consultationTypeComboBox, "cell 3 3,growx,aligny top");

		appointmentTypeComboBox = new JComboBox<>(new String[] { "Walk-in", "From Appointment" });
		mainPanel.add(appointmentTypeComboBox, "cell 3 2,growx");
		sessionDateTimeField = new JFormattedTextField();
		sessionDateTimeField.setColumns(10);
		mainPanel.add(sessionDateTimeField, "cell 3 1,growx");
		updatedATField = new JFormattedTextField();
		updatedATField.setColumns(10);
		mainPanel.add(updatedATField, "cell 4 1,growx");

		// Initialize the dateField
		dateField = new JFormattedTextField();
		dateField.setColumns(10);
		mainPanel.add(dateField, "cell 3 1,growx");
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
	private void showParticipantDetails(int rowNumber) {
		// Get the stored details for this participant
		Map<String, String> details = participantDetails.get(rowNumber);

		if (details == null) {
			JOptionPane.showMessageDialog(this, "Participant details not found", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Create a new JFrame for showing the details
		JFrame detailFrame = new JFrame("Participant Details");
		detailFrame.setSize(300, 200);
		detailFrame.setLocationRelativeTo(this); // Center on parent frame

		// Create a panel to show details with a more organized layout
		JPanel detailsPanel = new JPanel();
		detailsPanel.setLayout(new GridLayout(0, 2, 10, 10)); // Rows adjust automatically, 2 columns
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding

		// Add all available details
		detailsPanel.add(new JLabel("Full Name:"));
		detailsPanel.add(new JLabel(details.get("fullName")));

		detailsPanel.add(new JLabel("Contact Number:"));
		detailsPanel.add(new JLabel(details.get("contact")));

		detailsPanel.add(new JLabel("Email:"));
		detailsPanel.add(new JLabel(details.get("email")));

		// Add a close button
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> detailFrame.dispose());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(closeButton);

		// Add panels to the frame
		detailFrame.getContentPane().setLayout(new BorderLayout());
		detailFrame.getContentPane().add(detailsPanel, BorderLayout.CENTER);
		detailFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// Show the frame
		detailFrame.setVisible(true);
	}

}
