package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;

public class SessionsForm extends Form implements Printable {
	private JComboBox<String> violationField;
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
	private JTextField customViolationField; // Add this field for custom violations

	// Violation type arrays
	private String[] violations = {
		"Absence/Late",
		"Minor Property Damage",
		"Threatening/Intimidating",
		"Pornographic Materials",
		"Gadget Use in Class",
		"Cheating",
		"Stealing",
		"No Pass",
		"Bullying",
		"Sexual Abuse",
		"Illegal Drugs",
		"Alcohol",
		"Smoking/Vaping",
		"Gambling",
		"Public Display of Affection",
		"Fighting/Weapons",
		"Severe Property Damage",
		"Others"
	};

	private JTextField recordedByField;
	private JLabel otherViolationLabel;

	public SessionsForm(Connection conn) {
		this.connect = conn;
		initializeComponents();
		layoutComponents();
		populateRecordedByField(); // Call the method to populate the recordedByField
	}

	private void initializeComponents() {
		// Initialize the table model
		participantTableModel = new DefaultTableModel(
				new Object[] { "#", "Participant Name", "Participant Type", "Actions" }, 0);
		

		
		// Initialize custom violation field
		customViolationField = new JTextField(15);
		customViolationField.setVisible(false);
		
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
				customViolationField.setText(""); // Clear the text when hidden
			} else if (selected.equals("Others")) {
				customViolationField.setEnabled(true);

			} else {
				customViolationField.setEnabled(false);
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
    if (ModalDialog.isIdExist("search")) {
        return;
    }

    try {
        StudentSearchPanel searchPanel = new StudentSearchPanel(connect);
        
        // Set the default option properties before showing modal
        ModalDialog.getDefaultOption()
            .setOpacity(1f)  // Make fully opaque
            .setAnimationOnClose(true)
            .getBorderOption()
                .setBorderWidth(1f)
                .setShadow(raven.modal.option.BorderOption.Shadow.MEDIUM);

        // Create the modal dialog
        ModalDialog.showModal(
            this, 
            new SimpleModalBorder(
                searchPanel, 
                "Search Student", 
                new SimpleModalBorder.Option[] {
                    new SimpleModalBorder.Option("Add Selected Student", SimpleModalBorder.YES_OPTION),
                    new SimpleModalBorder.Option("Cancel", SimpleModalBorder.NO_OPTION)
                },
                (controller, action) -> {
                    if (action == SimpleModalBorder.YES_OPTION) {
                        Student selectedStudent = searchPanel.getSelectedStudent();
                        if (selectedStudent != null) {
                            try {
                                // Create new participant from student
                                Participants participant = new Participants();
                                participant.setStudentUid(selectedStudent.getStudentUid());
                                participant.setParticipantFirstName(selectedStudent.getStudentFirstname());
                                participant.setParticipantLastName(selectedStudent.getStudentLastname());
                                participant.setParticipantType("Student");

                                // Save participant to database
                                ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
                                participantsDAO.createParticipant(participant);

                                // Add to table
                                String fullName = selectedStudent.getStudentFirstname() + " " + selectedStudent.getStudentLastname();
                                participantTableModel.addRow(new Object[] {
                                    participant.getParticipantId(),
                                    fullName,
                                    "Student",
                                    "View | Remove"
                                });

                                // Store details for later use
                                Map<String, String> details = new HashMap<>();
                                details.put("firstName", selectedStudent.getStudentFirstname());
                                details.put("lastName", selectedStudent.getStudentLastname());
                                details.put("fullName", fullName);
                                details.put("type", "Student");
                                details.put("studentUid", String.valueOf(selectedStudent.getStudentUid()));
                                participantDetails.put(participant.getParticipantId(), details);

                                controller.close();
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(this,
                                    "Error adding student as participant: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                        } else {
                            JOptionPane.showMessageDialog(this,
                                "Please select a student first.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        controller.close();
                    }
                }
            ),
            "search"
        );

        // Configure layout after showing modal
        ModalDialog.getDefaultOption().getLayoutOption()
            .setSize(700, 500)
            .setLocation(raven.modal.option.Location.CENTER, raven.modal.option.Location.TOP);

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Error opening student search: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
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
        // First verify that we have a valid guidance counselor ID
        int guidanceCounselorId;
        if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
            guidanceCounselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
        } else {
            JOptionPane.showMessageDialog(this, 
                "No guidance counselor is currently logged in.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
        Integer appointmentId = null; // Default value for Walk-in
        if (!"Walk-in".equals(appointmentType)) {
            // Retrieve the actual appointment ID if not Walk-in
            appointmentId = getAppointmentId();
        }

        // Validate if participants exist
        if (participantTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, 
                "Please add at least one participant before saving the session.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a new Session object using the constructor
        Sessions session = new Sessions(
                0, // Auto-incremented session ID
                appointmentId, // Appointment ID (null for walk-in)
                guidanceCounselorId, // Verified counselor ID
                null, // Violation ID (optional)
                appointmentType,
                (String) consultationTypeComboBox.getSelectedItem(),
                new java.sql.Timestamp(System.currentTimeMillis()), // Current timestamp
                notesArea.getText(),
                "Scheduled", // Initial status
                new java.sql.Timestamp(System.currentTimeMillis())
        );

        // Retrieve participants from the table
        List<Participants> participants = new ArrayList<>();
        for (int i = 0; i < participantTableModel.getRowCount(); i++) {
            int participantId = Integer.parseInt(participantTableModel.getValueAt(i, 0).toString());
            String participantName = participantTableModel.getValueAt(i, 1).toString();
            String participantType = participantTableModel.getValueAt(i, 2).toString();

            Participants participant = new Participants();
            participant.setParticipantId(participantId);
            participant.setParticipantFirstName(participantName.split(" ")[0]);
            participant.setParticipantLastName(participantName.split(" ")[1]);
            participant.setParticipantType(participantType);
            participants.add(participant);
        }

        // Store the participants in the session object
        session.setParticipants(participants);

        // Use SessionsDAO to save the session
        SessionsDAO sessionsDAO = new SessionsDAO(connect);
        int sessionId = sessionsDAO.addSession(session);

        if (sessionId > 0) {
            // Store all participants in the SESSIONS_PARTICIPANTS table
            for (Participants participant : participants) {
                sessionsDAO.addParticipantToSession(sessionId, participant.getParticipantId());
            }
            
            JOptionPane.showMessageDialog(this, 
                "Session and participants saved successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            if (saveCallback != null) {
                saveCallback.run();
            }
            
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to save session.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
            "Error saving session: " + e.getMessage(), 
            "Error",
            JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}


	private void clearFields(){
		// Clear text fields
		firstNameField.setText("");
		lastNameField.setText("");
		contactNumberField.setText("");
		notesArea.setText("");
		sessionSummaryArea.setText("");
		sessionDateTimeField.setText("");
		updatedATField.setText("");
		dateField.setText("");

		// Reset combo boxes to first item
		participantsComboBox.setSelectedIndex(0);
		consultationTypeComboBox.setSelectedIndex(0);
		appointmentTypeComboBox.setSelectedIndex(0);
		sexCBox.setSelectedIndex(0);
		violationField.setSelectedIndex(1);

		// Clear participant table
		participantTableModel.setRowCount(0);
		participantDetails.clear();
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
		mainPanel = new JPanel(
				new MigLayout("wrap, gap 15, hidemode 3, insets 20", "[][grow][20px][][]", "[][][][][][fill][][grow][][]"));
		contentPanel.add(mainPanel, "cell 0 1,grow");
		mainPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(10, 10, 10, 10),
			BorderFactory.createLineBorder(new Color(200, 200, 200))
		));

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

		// Violation type with label and custom field
		JLabel violationLabel = new JLabel("Violation Type: ");
		violationLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		mainPanel.add(violationLabel, "flowx,cell 1 4,aligny top");
		
		// Create a panel to hold both the combo box and custom field
		JPanel violationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		violationPanel.add(violationField);
		
		otherViolationLabel = new JLabel("Others");
		violationPanel.add(otherViolationLabel);
		violationPanel.add(customViolationField);
		mainPanel.add(violationPanel, "cell 1 4,growx,aligny top");

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

		// Replace JComboBox with JTextField for Recorded By
		mainPanel.remove(recordedByField);
		JLabel recordedByLabel = new JLabel("Recorded By:");
		recordedByLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
		mainPanel.add(recordedByLabel, "flowx,cell 3 8");
		mainPanel.add(recordedByField, "cell 3 8,growx");

		// Buttons
		mainPanel.add(printButton, "flowx,cell 4 8,growx");
		mainPanel.add(saveButton, "cell 4 8,growx");

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

		searchBtn.addActionListener(e -> openAppointmentSearchDialog());

		JButton addWalkInParticipantButton = new JButton("Add Walk-In Participant");
		addWalkInParticipantButton.addActionListener(e -> addParticipantForWalkIn());
		mainPanel.add(addWalkInParticipantButton, "cell 4 3,alignx center");
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
			JOptionPane.showMessageDialog(this, "Error retrieving counselor information: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void openAppointmentSearchDialog() {
		JDialog dialog = new JDialog((JFrame) null, "Search Appointment", true);
		dialog.setSize(600, 400);
		dialog.setLocationRelativeTo(this);
		dialog.getContentPane().setLayout(new BorderLayout());

		JPanel searchPanel = new JPanel(new FlowLayout());
		JTextField searchField = new JTextField(20);
		JButton searchButton = new JButton("Search");
		searchPanel.add(new JLabel("Search by Title:"));
		searchPanel.add(searchField);
		searchPanel.add(searchButton);

		DefaultTableModel appointmentTableModel = new DefaultTableModel(
				new Object[] { "ID", "Title", "Date & Time", "Status" }, 0);
		JTable appointmentTable = new JTable(appointmentTableModel);
		JScrollPane tableScrollPane = new JScrollPane(appointmentTable);

		searchButton.addActionListener(e -> {
			String searchText = searchField.getText();
			if (searchText.trim().isEmpty()) {
				JOptionPane.showMessageDialog(dialog, "Please enter a search term.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			searchAppointments(searchText, appointmentTableModel);
		});

		JButton selectButton = new JButton("Select");
		selectButton.addActionListener(e -> {
			int selectedRow = appointmentTable.getSelectedRow();
			if (selectedRow == -1) {
				JOptionPane.showMessageDialog(dialog, "Please select an appointment.", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			int appointmentId = (int) appointmentTableModel.getValueAt(selectedRow, 0);
			populateParticipantsFromAppointment(appointmentId);
			dialog.dispose();
		});

		dialog.getContentPane().add(searchPanel, BorderLayout.NORTH);
		dialog.getContentPane().add(tableScrollPane, BorderLayout.CENTER);
		dialog.getContentPane().add(selectButton, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	private void searchAppointments(String searchText, DefaultTableModel tableModel) {
		try {
			AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
			List<Appointment> appointments = appointmentDAO.getAllAppointments();
			tableModel.setRowCount(0);
			for (Appointment appointment : appointments) {
				if (appointment.getAppointmentTitle().toLowerCase().contains(searchText.toLowerCase())) {
					tableModel.addRow(new Object[] {
							appointment.getAppointmentId(),
							appointment.getAppointmentTitle(),
							appointment.getAppointmentDateTime(),
							appointment.getAppointmentStatus()
					});
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error searching appointments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void populateParticipantsFromAppointment(int appointmentId) {
		try {
			AppointmentDAO appointmentDAO = new AppointmentDAO(connect);
			Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
			if (appointment != null && appointment.getParticipants() != null) {
				participantTableModel.setRowCount(0); // Clear existing participants
				int rowNum = 1;
				for (Participants participant : appointment.getParticipants()) {
					participantTableModel.addRow(new Object[] {
							rowNum++,
							participant.getParticipantFirstName() + " " + participant.getParticipantLastName(),
							participant.getParticipantType(),
							"View | Remove"
					});
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error populating participants: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void addParticipantForWalkIn() {
		String firstName = firstNameField.getText();
		String lastName = lastNameField.getText();
		String contact = contactNumberField.getText();
		String email = (String) sexCBox.getSelectedItem();

		if (firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter at least first and last name.", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
			Participants participant = new Participants();
			participant.setParticipantFirstName(firstName);
			participant.setParticipantLastName(lastName);
			participant.setContactNumber(contact);
			participant.setEmail(email);
			participant.setParticipantType("Non-Student");
			participantsDAO.createParticipant(participant);

			participantTableModel.addRow(new Object[] {
					participantTableModel.getRowCount() + 1,
					firstName + " " + lastName,
					"Non-Student",
					"View | Remove"
			});

			firstNameField.setText("");
			lastNameField.setText("");
			contactNumberField.setText("");
			sexCBox.setSelectedIndex(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error adding participant: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void saveSessionToDatabase() {
	    try {
	        // Verify if a guidance counselor is logged in
	        int guidanceCounselorId;
	        if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
	            guidanceCounselorId = Main.formManager.getCounselorObject().getGuidanceCounselorId();
	        } else {
	            JOptionPane.showMessageDialog(this, "No guidance counselor is currently logged in.", 
	                "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        // Validate if participants exist
	        if (participantTableModel.getRowCount() == 0) {
	            JOptionPane.showMessageDialog(this, "Please add at least one participant before saving the session.", 
	                "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        // Retrieve session details
	        String appointmentType = (String) appointmentTypeComboBox.getSelectedItem();
	        String consultationType = (String) consultationTypeComboBox.getSelectedItem();
	        String violation = (String) violationField.getSelectedItem();
	        String notes = notesArea.getText();

	        // Handle appointment ID
	        Integer appointmentId = null; // Default for Walk-in
	        if (!"Walk-in".equals(appointmentType)) {
	            appointmentId = getAppointmentId();
	        }

	        // Handle violation
	        String violationText = null;
	        if (violation != null && violation.equals("Others")) {
	            violationText = customViolationField.getText().trim();
	        } else {
	            violationText = violation;
	        }

	        // Create a new Session object
	        Sessions session = new Sessions(
	            0, // Auto-incremented SESSION_ID
	            appointmentId, // Appointment ID (null for Walk-in)
	            guidanceCounselorId, // Verified counselor ID
	            null, // Violation ID (linked separately)
	            appointmentType,
	            consultationType,
	            new java.sql.Timestamp(System.currentTimeMillis()), // SESSION_DATE_TIME
	            notes,
	            "Active", // SESSION_STATUS
	            new java.sql.Timestamp(System.currentTimeMillis()) // UPDATED_AT
	        );

	        // Save session to the database
	        SessionsDAO sessionsDAO = new SessionsDAO(connect);
	        int sessionId = sessionsDAO.addSession(session);

	        if (sessionId > 0) {
	            ParticipantsDAO participantsDAO = new ParticipantsDAO(connect);
	            // Save participants related to the session
	            for (int i = 0; i < participantTableModel.getRowCount(); i++) {
	                int participantId = Integer.parseInt(participantTableModel.getValueAt(i, 0).toString());

	                // Link participant to the session
	                sessionsDAO.addParticipantToSession(sessionId, participantId);
	            }

	            JOptionPane.showMessageDialog(this, "Session and related data saved successfully!", 
	                "Success", JOptionPane.INFORMATION_MESSAGE);

	            if (saveCallback != null) {
	                saveCallback.run();
	            }

	            // Clear fields after saving
	            clearFields();
	        } else {
	            JOptionPane.showMessageDialog(this, "Failed to save session.", 
	                "Error", JOptionPane.ERROR_MESSAGE);
	        }

	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(this, "Error saving session: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	        e.printStackTrace();
	    }
	}

}