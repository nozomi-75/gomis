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

import lyfjshs.gomis.Database.DAO.SessionsDAO;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.view.appointment.StudentSearchUI;
import net.miginfocom.swing.MigLayout;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.Rectangle;
import java.awt.Dimension;

public class SessionsForm extends Form implements Printable {
	private JTextField dateField, violationField, recordedByField;
	private JFormattedTextField startSessionTimeField, endSessionTimeField;
	private JTextArea sessionSummaryArea, notesArea;
	private JButton saveButton, printButton, searchStudentButton;
	private JComboBox<String> participantsComboBox;
	private JComboBox<String> consultationTypeComboBox;
	private JComboBox<String> appointmentTypeComboBox;
	private Connection connect;
	private JPanel mainPanel;
	private JTextField firstNameField, lastNameField, contactNumberField, emailField;
	private JPanel contentPanel;
	private JPanel panel;
	private JSeparator separator;
	private JPanel panel_1;

	public SessionsForm(Connection conn) {
		this.connect = conn;
		initializeComponents();
		layoutComponents();
	}

	private void initializeComponents() {
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
		// For example, you might query the database or get the ID from a selected appointment
		return 1; // Replace with actual appointment ID retrieval logic
	}

	private void saveSession() {
		try {
			String date = dateField.getText();
			String participants = (String) participantsComboBox.getSelectedItem();
			String violation = violationField.getText();
			String recordedBy = recordedByField.getText();
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
					null, // sessionDateTime (not retrieved in the query)
					notes, // sessionNotes
					"Active", // sessionStatus
					new java.sql.Timestamp(System.currentTimeMillis()) // updatedAt
			);

			// Use SessionsDAO to save the session
			SessionsDAO sessionsDAO = new SessionsDAO(connect);
			sessionsDAO.addSession(session); // Assuming you have an addSession method in SessionsDAO

			JOptionPane.showMessageDialog(this, "Session saved successfully!", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void layoutComponents() {
		this.setLayout(new MigLayout("gap 10", "[grow]", "[grow]"));
		contentPanel = new JPanel(new MigLayout("", "[grow]", "[][grow]"));
		add(contentPanel, "cell 0 0,grow");
		participantsComboBox = new JComboBox<>(new String[] { "Student", "Non-Student" });
		participantsComboBox.addActionListener(e -> toggleSearchStudentButton());
		sessionSummaryArea = new JTextArea(4, 20);

		saveButton = new JButton("SAVE");
		saveButton.setBackground(new Color(70, 130, 180));
		saveButton.setForeground(Color.WHITE);
		saveButton.setFocusPainted(false);
		saveButton.addActionListener(e -> saveSession());

		searchStudentButton = new JButton("Search Student");
		searchStudentButton.setEnabled(false);
		searchStudentButton.addActionListener(e -> openStudentSearchUI());

		// Header
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contentPanel.add(headerPanel, "cell 0 0,growx");
		JLabel headerLabel = new JLabel("Session Documentation Form");
		headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		headerPanel.add(headerLabel);
		headerPanel.setBackground(new Color(5, 117, 230));
		headerPanel.setForeground(Color.WHITE);

		consultationTypeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance",
				"Personal Consultation", "Behavioral Consultation", "Group Consultation" });

		appointmentTypeComboBox = new JComboBox<>(new String[] { "Walk-in", "From Appointment" });

		firstNameField = new JTextField(10);
		lastNameField = new JTextField(10);
		contactNumberField = new JTextField(10);
		emailField = new JTextField(10);

		// Main Panel
		mainPanel = new JPanel(new MigLayout("wrap, gap 10, hidemode 3", "[][grow][][10][][]", "[][][][][][][][grow][][]"));
		contentPanel.add(mainPanel, "cell 0 1,grow");
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Participant
		JLabel participantLabel = new JLabel("Participant");
		mainPanel.add(participantLabel, "flowx,cell 1 0,alignx right");
		mainPanel.add(participantsComboBox, "cell 1 0,growx");
		mainPanel.add(searchStudentButton, "cell 2 0");
		
		separator = new JSeparator();
		separator.setSize(new Dimension(10, 10));
		separator.setBounds(new Rectangle(0, 0, 5, 5));
		separator.setBackground(Color.GRAY);
		separator.setForeground(Color.DARK_GRAY);
		separator.setOrientation(SwingConstants.VERTICAL);
		mainPanel.add(separator, "cell 3 0 1 6");

		// Participant Panel
		JPanel participantPanel = new JPanel(new MigLayout("gap 10", "[][grow][][][]", "[][][][]"));
		participantPanel.setBorder(BorderFactory.createTitledBorder("Non-Student Participant"));

		participantPanel.add(new JLabel("First Name"), "cell 0 0");
		participantPanel.add(firstNameField, "cell 1 0,growx");

		participantPanel.add(new JLabel("Last Name"), "cell 3 0");
		participantPanel.add(lastNameField, "cell 4 0");

		participantPanel.add(new JLabel("Contact Number"), "cell 0 1");
		participantPanel.add(contactNumberField, "cell 1 1 3 1,growx");

		participantPanel.add(new JLabel("Email:"), "cell 0 2");
		participantPanel.add(emailField, "cell 1 2 3 1,growx");

		JButton saveParticipantButton = new JButton("Save Participant");
		participantPanel.add(saveParticipantButton, "cell 1 3 4 1,alignx center");

		mainPanel.add(participantPanel, "cell 1 1 2 4,growx"); // Initially visible

		// Violation
		JLabel violationLabel = new JLabel("Violation");
		mainPanel.add(violationLabel, "flowx,cell 4 1");
		violationField = new JTextField(10);
		mainPanel.add(violationField, "cell 4 1");

		// Appointment Type
		JLabel appointmentTypeLabel = new JLabel("Appointment Type");
		mainPanel.add(appointmentTypeLabel, "flowx,cell 4 2,alignx left");
		mainPanel.add(appointmentTypeComboBox, "cell 4 2,growx");

		// Start Time
		JLabel startTimeLabel = new JLabel("Start Session Time");
		mainPanel.add(startTimeLabel, "flowx,cell 4 3");
		startSessionTimeField = new JFormattedTextField();
		startSessionTimeField.setColumns(10);
		mainPanel.add(startSessionTimeField, "cell 4 3,growx");

		// End Time
		JLabel endTimeLabel = new JLabel("End Session Time");
		mainPanel.add(endTimeLabel, "flowx,cell 5 3");
		endSessionTimeField = new JFormattedTextField();
		endSessionTimeField.setColumns(10);
		mainPanel.add(endSessionTimeField, "cell 5 3,growx");

		// Consultation Type
		JLabel consultationTypeLabel = new JLabel("Consultation Type");
		mainPanel.add(consultationTypeLabel, "flowx,cell 4 4,aligny top");
		mainPanel.add(consultationTypeComboBox, "cell 4 4,growx,aligny top");
				
						// Date
						JLabel dateLabel = new JLabel("Date");
						mainPanel.add(dateLabel, "flowx,cell 5 4,alignx left,aligny top");
		
		panel_1 = new JPanel();
		mainPanel.add(panel_1, "cell 1 5 2 1,grow");
		
		panel = new JPanel();
		mainPanel.add(panel, "cell 4 5 2 1,grow");
		panel.setLayout(new MigLayout("", "[][grow]", "[]"));
		
				// Notes
				JLabel notesLabel = new JLabel("Notes");
				panel.add(notesLabel, "cell 0 0");
				notesArea = new JTextArea(4, 20);
				JScrollPane notesScrollPane = new JScrollPane(notesArea);
				panel.add(notesScrollPane, "cell 1 0,growx");

		// Session Summary
		JLabel summaryLabel = new JLabel("Session Summary");
		mainPanel.add(summaryLabel, "cell 1 6 5 1,alignx center,aligny bottom");
		JScrollPane summaryScrollPane = new JScrollPane(sessionSummaryArea);
		mainPanel.add(summaryScrollPane, "cell 1 7 5 1,grow");
		
				printButton = new JButton("PRINT");
				printButton.setBackground(new Color(70, 130, 180));
				printButton.setForeground(Color.WHITE);
				printButton.setFocusPainted(false);
				printButton.addActionListener(e -> printSessionDetails());
						
								// Recorded By
								JLabel recordedByLabel = new JLabel("Recorded By");
								mainPanel.add(recordedByLabel, "flowx,cell 4 8");
				
						// Buttons
						mainPanel.add(printButton, "flowx,cell 5 8,growx");
		mainPanel.add(saveButton, "cell 5 8,growx");
		dateField = new JTextField(10);
		mainPanel.add(dateField, "cell 5 4,alignx right,aligny top");
		recordedByField = new JTextField(10);
		mainPanel.add(recordedByField, "cell 4 8 2 1,growx");
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

}
