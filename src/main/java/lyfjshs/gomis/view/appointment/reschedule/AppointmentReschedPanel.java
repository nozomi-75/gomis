package lyfjshs.gomis.view.appointment.reschedule;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.appointment.add.NonStudentPanel;
import lyfjshs.gomis.view.appointment.add.TempParticipant;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;

public class AppointmentReschedPanel extends JPanel {
	private final Connection connection;
	private final AppointmentDAO appointmentDAO;
	private final Appointment appointment;
	private List<TempParticipant> tempParticipants = new ArrayList<>();
	private GTable participantsTable;
	private JTextField titleField;
	private JComboBox<String> typeComboBox;
	private JTextArea notesArea;
	private JFormattedTextField dateField, timeField;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private JPanel contentPanel;
	private DropPanel studentDropPanel;
	private DropPanel nonStudentDropPanel;
	private DropPanel otherTypeDropPanel;
	private JComboBox<String> otherTypeComboBox;
	private JTextField customTypeField;
	private JComboBox<String> appointmentStatusComboBox;

	public AppointmentReschedPanel(Appointment appointment, AppointmentDAO appointmentDAO, Connection connection) {
		this.appointment = appointment;
		this.appointmentDAO = appointmentDAO;
		this.connection = connection;
		initializeComponents();
	}

	private void initializeComponents() {
		setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));

		// Create main content panel
		contentPanel = new JPanel(new MigLayout("insets 5", "[grow]", "[][][][][]"));

		// Create scroll pane with smooth scrolling
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);
		scrollPane.setBorder(null);
		add(scrollPane, "grow");

		// Title
		JPanel titleSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[]"));
		titleSection.setBorder(javax.swing.BorderFactory.createTitledBorder(
			javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), 
			"Appointment Details"
		));
		
		JPanel titlePanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		titlePanel.add(new JLabel("Title:"), "wrap");
		titleField = new JTextField(appointment.getAppointmentTitle());
		titlePanel.add(titleField, "growx");
		
		titleSection.add(titlePanel, "grow");
		contentPanel.add(titleSection, "cell 0 0,growx");

		// Type
		JPanel typeSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[][]"));
		typeSection.setBorder(javax.swing.BorderFactory.createTitledBorder(
			javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), 
			"Consultation Type"
		));
		
		JPanel typePanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		typePanel.add(new JLabel("Type:"), "wrap");
		typeComboBox = new JComboBox<>(new String[] { 
				"Academic Consultation", 
				"Career Guidance", 
				"Personal Counseling",
				"Behavioral Counseling", 
				"Group Counseling", 
				"Other" 
		});
		typePanel.add(typeComboBox, "growx");
		
		typeSection.add(typePanel, "cell 0 0,grow");
		
		// Create DropPanel for "Other" type options
		otherTypeDropPanel = new DropPanel();
		otherTypeDropPanel.setDropdownPadding(10, 10, 10, 10);
		
		// Create "Other" type panel
		JPanel otherTypePanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[][]"));
		
		// ComboBox for predefined other types
		otherTypeComboBox = new JComboBox<>(new String[] { 
				"Good Moral Request", 
				"Home Visitation", 
				"Custom..."
		});
		otherTypePanel.add(new JLabel("Specific Type:"), "wrap");
		otherTypePanel.add(otherTypeComboBox, "growx, wrap");
		
		// TextField for custom type
		customTypeField = new JTextField();
		otherTypePanel.add(new JLabel("Custom Type:"), "wrap");
		otherTypePanel.add(customTypeField, "growx");
		customTypeField.setEnabled(false);
		
		// Set the otherTypePanel as the content of the DropPanel
		otherTypeDropPanel.setContent(otherTypePanel);
		
		// Add action listener to the type combobox
		typeComboBox.addActionListener(e -> {
			String selected = (String) typeComboBox.getSelectedItem();
			otherTypeDropPanel.setDropdownVisible("Other".equals(selected));
		});
		
		// Add a change listener to the otherTypeComboBox
		otherTypeComboBox.addActionListener(e -> {
			String selected = (String) otherTypeComboBox.getSelectedItem();
			if ("Custom...".equals(selected)) {
				customTypeField.setEnabled(true);
				customTypeField.requestFocus();
			} else {
				customTypeField.setEnabled(false);
				customTypeField.setText("");
			}
		});
		
		typeSection.add(otherTypeDropPanel, "cell 0 1,grow");
		contentPanel.add(typeSection, "cell 0 1,growx");
		
		// Add a helper method to get the actual consultation type
		if (appointment.getConsultationType() != null) {
			// Check if it's one of the predefined types
			boolean found = false;
			for (int i = 0; i < typeComboBox.getItemCount(); i++) {
				if (typeComboBox.getItemAt(i).equals(appointment.getConsultationType())) {
					typeComboBox.setSelectedIndex(i);
					found = true;
					break;
				}
			}
			
			// If not found, it might be an "Other" type
			if (!found) {
				typeComboBox.setSelectedItem("Other");
				otherTypeDropPanel.setDropdownVisible(true);
				
				// Check if it's one of the predefined "Other" types
				boolean otherFound = false;
				for (int i = 0; i < otherTypeComboBox.getItemCount() - 1; i++) { // -1 to exclude "Custom..."
					if (otherTypeComboBox.getItemAt(i).equals(appointment.getConsultationType())) {
						otherTypeComboBox.setSelectedIndex(i);
						otherFound = true;
						break;
					}
				}
				
				// If not found, it's a custom type
				if (!otherFound) {
					otherTypeComboBox.setSelectedItem("Custom...");
					customTypeField.setEnabled(true);
					customTypeField.setText(appointment.getConsultationType());
				}
			}
		}

		// Date and Time Panel
		JPanel dateTimeSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[][]"));
		dateTimeSection.setBorder(javax.swing.BorderFactory.createTitledBorder(
			javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), 
			"Schedule"
		));
		
		JPanel dateTimePanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[][]"));
		dateTimePanel.add(new JLabel("Date:"), "cell 0 0");
		dateField = new JFormattedTextField();
		datePicker = new DatePicker();

		JLabel label = new JLabel("Time:");
		dateTimePanel.add(label, "cell 1 0");
		datePicker.setEditor(dateField);
		dateTimePanel.add(dateField, "cell 0 1,growx");
		timePicker = new TimePicker();

		if (appointment.getAppointmentDateTime() != null) {
			LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
			datePicker.setSelectedDate(dateTime.toLocalDate());
			timePicker.setSelectedTime(dateTime.toLocalTime());
		}
		timeField = new JFormattedTextField();
		timePicker.setEditor(timeField);
		dateTimePanel.add(timeField, "cell 1 1,growx");
		
		dateTimeSection.add(dateTimePanel, "cell 0 0,grow");
		
		// Add status panel
		JPanel statusPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		dateTimeSection.add(statusPanel, "cell 0 1,grow");
		
		statusPanel.add(new JLabel("Appointment Status:"), "cell 0 0");
		appointmentStatusComboBox = new JComboBox<>(new String[] { 
			"Scheduled", "Rescheduled"
		});
		
		// Set the default status or the current status if updating
		if (appointment.getAppointmentStatus() != null && !appointment.getAppointmentStatus().isEmpty()) {
			appointmentStatusComboBox.setSelectedItem(appointment.getAppointmentStatus());
		} else {
			appointmentStatusComboBox.setSelectedItem("Scheduled");
		}
		
		statusPanel.add(appointmentStatusComboBox, "cell 0 1,growx");
		
		contentPanel.add(dateTimeSection, "cell 0 2,growx");

		// Participants Table
		JPanel participantsSection = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[90px][grow][grow]"));
		participantsSection.setBorder(javax.swing.BorderFactory.createTitledBorder(
			javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), 
			"Participants"
		));
		
		JScrollPane tableScrollPane = new JScrollPane(participantsTable);
		tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		tableScrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);
		tableScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		participantsSection.add(tableScrollPane, "grow, wrap");

		// Participant Buttons Panel with Dropdown Area
		JPanel participantButtonsSection = new JPanel(new MigLayout("insets 0, wrap 1", "[grow]", "[][grow]"));
		
		// Button Panel - Keeps buttons side by side
		JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
		
		// Create student button
		JButton addStudentBtn = new JButton("Add Student");
		addStudentBtn.setBackground(new Color(0, 123, 255));
		addStudentBtn.setForeground(Color.WHITE);
		addStudentBtn.addActionListener(e -> {
			nonStudentDropPanel.setDropdownVisible(false);
			studentDropPanel.setDropdownVisible(!studentDropPanel.isDropdownVisible());
		});
		buttonPanel.add(addStudentBtn, "growx");

		// Create non-student button
		JButton addNonStudentBtn = new JButton("Add Non-Student");
		addNonStudentBtn.setBackground(new Color(40, 167, 69));
		addNonStudentBtn.setForeground(Color.WHITE);
		addNonStudentBtn.addActionListener(e -> {
			studentDropPanel.setDropdownVisible(false);
			nonStudentDropPanel.setDropdownVisible(!nonStudentDropPanel.isDropdownVisible());
		});
		buttonPanel.add(addNonStudentBtn, "growx");
		
		// Add button panel to the participant section
		participantButtonsSection.add(buttonPanel, "growx");

		// Create dropdown panels container
		JPanel dropdownContainer = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		
		// Create student search dropdown panel
		studentDropPanel = new DropPanel();
		studentDropPanel.setDropdownPadding(10, 10, 10, 10);
		
		StudentSearchPanel studentSearch = new StudentSearchPanel(connection, null) {
			@Override
			protected void onStudentSelected(Student student) {
//				addStudentParticipant(student);
				studentDropPanel.setDropdownVisible(false);
			}
		};
		studentDropPanel.setContent(studentSearch);
		dropdownContainer.add(studentDropPanel, "cell 0 0,grow");

		// Create non-student dropdown panel
		nonStudentDropPanel = new DropPanel();
		nonStudentDropPanel.setDropdownPadding(10, 10, 10, 10);
		
		NonStudentPanel nonStudentForm = new NonStudentPanel();
		nonStudentForm.setNonStudentListener(participant -> {
			tempParticipants.add(participant);
//			updateParticipantsTable();
			nonStudentDropPanel.setDropdownVisible(false);
		});
		
				// Notes
				JPanel notesSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[]"));
				notesSection.setBorder(javax.swing.BorderFactory.createTitledBorder(
					javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), 
					"Additional Information"
				));
				
				JPanel notesPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
				notesPanel.add(new JLabel("Notes:"), "wrap");
				notesArea = new JTextArea(4, 20);
				notesArea.setLineWrap(true);
				notesArea.setWrapStyleWord(true);
				JScrollPane notesScrollPane = new JScrollPane(notesArea);
				notesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
				notesScrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);
				notesScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)));
				notesPanel.add(notesScrollPane, "grow");
				
				notesSection.add(notesPanel, "grow");
				contentPanel.add(notesSection, "cell 0 3,growx");
		nonStudentDropPanel.setContent(nonStudentForm);
		dropdownContainer.add(nonStudentDropPanel, "cell 0 1,grow");
		
		// Add dropdown container to participant section
		participantButtonsSection.add(dropdownContainer, "grow");
		
		// Add both sections to the participants panel
		participantsSection.add(participantButtonsSection, "grow");
		
		// Add the complete participant section to content panel
		contentPanel.add(participantsSection, "cell 0 4,growx");
		if (appointment.getAppointmentNotes() != null) {
			notesArea.setText(appointment.getAppointmentNotes());
		}

		setupParticipantsTable();
	}

	private void setupParticipantsTable() {
		// Define table structure
		String[] columnNames = { "Name", "Type", "Actions" };
		Class<?>[] columnTypes = { String.class, String.class, Object.class };
		boolean[] editableColumns = { false, false, true };
		double[] columnWidths = { 0.5, 0.3, 0.2 };
		int[] alignments = { SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER };

		// Create action manager for the delete button
		TableActionManager actionManager = new DefaultTableActionManager() {
			@Override
			public void setupTableColumn(GTable table, int actionColumnIndex) {
				table.getColumnModel().getColumn(actionColumnIndex)
					.setCellRenderer((t, value, isSelected, hasFocus, row, column) -> {
						JPanel panel = new JPanel(new MigLayout("insets 0", "[]", "[]"));
						panel.setOpaque(false);
						
						JButton removeBtn = new JButton("Remove");
						removeBtn.setBackground(new Color(220, 53, 69));
						removeBtn.setForeground(Color.WHITE);
						
						panel.add(removeBtn);
						
						return panel;
					});
			}

			@Override
			public void onTableAction(GTable table, int row) {
				// This will be handled by the mouse listener
			}
		};

		// Create table
		participantsTable = new GTable(new Object[0][3], columnNames, columnTypes, editableColumns, columnWidths,
				alignments, false, actionManager);

		// Add mouse listener for button actions
		participantsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = participantsTable.rowAtPoint(e.getPoint());
				int col = participantsTable.columnAtPoint(e.getPoint());
				
				if (col == 2 && row >= 0) { // Actions column
					removeParticipant(row);
				}
			}
		});

		// Set minimum width for Actions column
		participantsTable.getColumnModel().getColumn(2).setMinWidth(100);
		participantsTable.getColumnModel().getColumn(2).setMaxWidth(120);

		// Update table if there are existing participants
		updateParticipantsTable();
	}

	public boolean saveRescheduledAppointment() throws SQLException {
		if (!validateFields()) {
			return false;
		}

		// Get the consultation type from the UI
		String consultationType = getConsultationType();
			
		// Set the values from form to appointment
		appointment.setAppointmentTitle(titleField.getText().trim());
		appointment.setConsultationType(consultationType);
		
		// Set appointment date time
		appointment.setAppointmentDateTime(
				java.sql.Timestamp.valueOf(datePicker.getSelectedDate().atTime(timePicker.getSelectedTime())));
		
		// Ensure the appointment is not in the past
		if (appointment.getAppointmentDateTime().toLocalDateTime().isBefore(java.time.LocalDateTime.now())) {
			showToast("Appointment date and time cannot be in the past.", raven.modal.Toast.Type.ERROR);
			return false;
		}
		
		// Set status to Rescheduled
		appointment.setAppointmentStatus("Rescheduled");
		
		appointment.setAppointmentNotes(notesArea.getText().trim());
		
		// Save appointment
		boolean success = appointmentDAO.updateAppointment(appointment);
		
		if (success) {
			// Format date and time for display
			java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
			java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
			
			String dateStr = appointment.getAppointmentDateTime().toLocalDateTime().format(dateFormatter);
			String timeStr = appointment.getAppointmentDateTime().toLocalDateTime().format(timeFormatter);
			
			// Create a more detailed success message
			String successMsg = "Appointment rescheduled successfully:\n" + 
							   appointment.getAppointmentTitle() + "\n" +
							   dateStr + " at " + timeStr + "\n" +
							   "Type: " + consultationType;
								
			showToast(successMsg, raven.modal.Toast.Type.SUCCESS);
			
			// Notify AppointmentManagement about the updated appointment
			lyfjshs.gomis.utils.EventBus.publish("appointment_updated", appointment.getAppointmentId());
		} else {
			showToast("Failed to reschedule appointment", raven.modal.Toast.Type.ERROR);
		}
		
		return success;
	}

	private boolean validateFields() {
		boolean isValid = true;
		// Reset all borders first
		titleField.putClientProperty("JTextField.borderColor", null);
		dateField.putClientProperty("JTextField.borderColor", null);
		timeField.putClientProperty("JTextField.borderColor", null);
		
		// Also reset validation for custom type field if visible
		if (customTypeField.isEnabled()) {
			customTypeField.putClientProperty("JTextField.borderColor", null);
		}

		// Validate title
		if (titleField.getText().trim().isEmpty()) {
			titleField.putClientProperty("JTextField.borderColor", new java.awt.Color(220, 53, 69));
			showToast("Please enter an appointment title.", raven.modal.Toast.Type.ERROR);
			isValid = false;
		}

		// Validate date and time
		if (datePicker.getSelectedDate() == null || timePicker.getSelectedTime() == null) {
			dateField.putClientProperty("JTextField.borderColor", new java.awt.Color(220, 53, 69));
			timeField.putClientProperty("JTextField.borderColor", new java.awt.Color(220, 53, 69));
			showToast("Please select both date and time.", raven.modal.Toast.Type.ERROR);
			isValid = false;
		} else {
			// Validate that date is not in the past
			java.time.LocalDateTime selectedDateTime = datePicker.getSelectedDate().atTime(timePicker.getSelectedTime());
			java.time.LocalDateTime now = java.time.LocalDateTime.now();
			
			// Give a 1-minute buffer to account for the time it takes to fill the form
			if (selectedDateTime.isBefore(now.minusMinutes(1))) {
				dateField.putClientProperty("JTextField.borderColor", new java.awt.Color(220, 53, 69));
				timeField.putClientProperty("JTextField.borderColor", new java.awt.Color(220, 53, 69));
				showToast("Appointment date and time cannot be in the past.", raven.modal.Toast.Type.ERROR);
				isValid = false;
			}
		}
		
		// Validate consultation type
		if ("Other".equals(typeComboBox.getSelectedItem())) {
			if ("Custom...".equals(otherTypeComboBox.getSelectedItem()) && 
				customTypeField.getText().trim().isEmpty()) {
				customTypeField.putClientProperty("JTextField.borderColor", new java.awt.Color(220, 53, 69));
				showToast("Please enter a custom consultation type.", raven.modal.Toast.Type.ERROR);
				isValid = false;
			}
		}

		return isValid;
	}

	private void showToast(String message, raven.modal.Toast.Type type) {
		raven.modal.toast.option.ToastOption toastOption = raven.modal.Toast.createOption();
		
		// Configure toast appearance and behavior
		toastOption.getLayoutOption()
				.setMargin(0, 0, 50, 0)
				.setDirection(raven.modal.toast.option.ToastDirection.TOP_TO_BOTTOM);
		
		// Set toast delay based on message length
		if (type == raven.modal.Toast.Type.SUCCESS) {
			// Success messages stay visible longer
			toastOption.setDelay(5000);  // 5 seconds
		} else if (type == raven.modal.Toast.Type.ERROR) {
			// Error messages stay visible a bit longer too
			toastOption.setDelay(4500);  // 4.5 seconds
		} else {
			// Default delay for warnings and info
			toastOption.setDelay(3500);  // 3.5 seconds
		}
		
		// For success messages with multiple lines, show at top for better visibility
		if (type == raven.modal.Toast.Type.SUCCESS && message.contains("\n")) {
			// Show at top center for better visibility of important info
			raven.modal.Toast.show(this, type, message, raven.modal.toast.option.ToastLocation.TOP_CENTER, toastOption);
		} else {
			// Standard position for other messages
			raven.modal.Toast.show(this, type, message, raven.modal.toast.option.ToastLocation.BOTTOM_CENTER, toastOption);
		}
	}

	// Simplified method to get consultation type
	private String getConsultationType() {
		String selectedType = (String) typeComboBox.getSelectedItem();
		
		if ("Other".equals(selectedType)) {
			String otherType = (String) otherTypeComboBox.getSelectedItem();
			
			if ("Custom...".equals(otherType)) {
				String customType = customTypeField.getText().trim();
				return customType.isEmpty() ? "Other" : customType;
			} else {
				return otherType;
			}
		} else {
			return selectedType;
		}
	}

	private void removeParticipant(int row) {
		// Implementation of removeParticipant method
	}

	private void updateParticipantsTable() {
		// Implementation of updateParticipantsTable method
	}
}
