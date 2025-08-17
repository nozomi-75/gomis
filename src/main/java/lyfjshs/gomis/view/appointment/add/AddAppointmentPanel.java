/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment.add;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.utils.ErrorDialogUtils;
import lyfjshs.gomis.utils.EventBus;
import lyfjshs.gomis.utils.ValidationUtils;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.Toast;
import raven.modal.component.Modal;
import raven.modal.toast.option.ToastOption;

public class AddAppointmentPanel extends Modal {
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

	public AddAppointmentPanel(Appointment appointment, AppointmentDAO appointmentDAO, Connection connection) {
		this.appointment = (appointment != null) ? appointment : new Appointment();
		this.appointmentDAO = appointmentDAO;
		this.connection = connection;
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
				javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), "Appointment Details"));

		JPanel titlePanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		titlePanel.add(new JLabel("Title:"), "wrap");
		titleField = new JTextField();
		titlePanel.add(titleField, "growx");

		titleSection.add(titlePanel, "grow");
		contentPanel.add(titleSection, "growx, wrap");

		// Type and Status
		JPanel typeSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[][]"));
		typeSection.setBorder(javax.swing.BorderFactory.createTitledBorder(
				javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), "Consultation Type"));

		JPanel typePanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[][]"));
		typePanel.add(new JLabel("Type:"), "cell 0 0");
		typeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance", "Personal Counseling",
				"Behavioral Counseling", "Group Counseling", "Other" });
		typePanel.add(typeComboBox, "cell 0 1,growx");

		// Add Status ComboBox
		typePanel.add(new JLabel("Status:"), "cell 1 0");
		appointmentStatusComboBox = new JComboBox<>(
				new String[] { "Scheduled", "Active", "Completed", "Cancelled", "No Show", "Rescheduled" });
		typePanel.add(appointmentStatusComboBox, "cell 1 1,growx");

		typeSection.add(typePanel, "cell 0 0,grow");

		// Create DropPanel for "Other" type options
		otherTypeDropPanel = new DropPanel();
		otherTypeDropPanel.setDropdownPadding(10, 10, 10, 10);

		// Create "Other" type panel
		JPanel otherTypePanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[][]"));

		// ComboBox for predefined other types
		otherTypeComboBox = new JComboBox<>(new String[] { "Good Moral Request", "Home Visitation", "Custom..." });
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
		contentPanel.add(typeSection, "growx, wrap");

		// Date and Time Panel
		JPanel dateTimeSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[][]"));
		dateTimeSection.setBorder(javax.swing.BorderFactory
				.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new Color(200, 200, 200)), "Schedule"));

		JPanel dateTimePanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[][]"));
		dateTimePanel.add(new JLabel("Date:"), "cell 0 0");
		dateField = new JFormattedTextField();
		datePicker = new DatePicker();

		JLabel label = new JLabel("Time:");
		dateTimePanel.add(label, "cell 1 0");
		datePicker.setEditor(dateField);
		dateTimePanel.add(dateField, "cell 0 1,growx");
		timePicker = new TimePicker();

		timeField = new JFormattedTextField();
		timePicker.setEditor(timeField);
		dateTimePanel.add(timeField, "cell 1 1,growx");

		dateTimeSection.add(dateTimePanel, "cell 0 0,grow");

		contentPanel.add(dateTimeSection, "growx, wrap");

		// Notes
		JPanel notesSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[][]"));
		notesSection.setBorder(
				BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), "Notes"));

		JPanel notesPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		notesPanel.add(new JLabel("Notes:"), "wrap");
		notesArea = new JTextArea(5, 20);
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesPanel.add(new JScrollPane(notesArea), "growx");

		notesSection.add(notesPanel, "grow");
		contentPanel.add(notesSection, "growx, wrap");

		// Participants
		JPanel participantsSection = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[][][][120px][]"));
		participantsSection.setBorder(BorderFactory
				.createTitledBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), "Participants"));

		// Create buttons panel
		JPanel buttonsPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));

		JButton addStudentBtn = new JButton("Add Student");
		JButton addNonStudentBtn = new JButton("Add Non-Student");

		buttonsPanel.add(addStudentBtn, "cell 0 0,growx");
		buttonsPanel.add(addNonStudentBtn, "cell 1 0,growx");

		participantsSection.add(buttonsPanel, "growx, wrap");

		// Create DropPanels for student and non-student
		studentDropPanel = new DropPanel();
		nonStudentDropPanel = new DropPanel();

		// Set up student search panel
		AppointmentStudentSearch studentSearchPanel = new AppointmentStudentSearch(connection, student -> {
			if (student != null) {
				addStudentParticipant(student);
				studentDropPanel.setDropdownVisible(false);
			}
		});
		studentDropPanel.setContent(studentSearchPanel);

		// Set up non-student panel
		NonStudentPanel nonStudentPanel = new NonStudentPanel();
		nonStudentPanel.setNonStudentListener(participant -> {
			addNonStudentParticipant(participant);
			nonStudentDropPanel.setDropdownVisible(false);
		});
		nonStudentDropPanel.setContent(nonStudentPanel);

		// Add action listeners to buttons
		addStudentBtn.addActionListener(e -> {
			studentDropPanel.setDropdownVisible(true);
			nonStudentDropPanel.setDropdownVisible(false);
		});

		addNonStudentBtn.addActionListener(e -> {
			nonStudentDropPanel.setDropdownVisible(true);
			studentDropPanel.setDropdownVisible(false);
		});

		participantsSection.add(studentDropPanel, "growx, wrap");
		participantsSection.add(nonStudentDropPanel, "growx, wrap");

		// Add participants table
		setupParticipantsTable();
		participantsSection.add(new JScrollPane(participantsTable), "growx, wrap");

		// Load appointment details if in edit mode
		loadAppointmentDetails();

		contentPanel.add(participantsSection, "growx, wrap");

	}

	private void loadAppointmentDetails() {
		// Set initial values for fields if updating an existing appointment
		if (appointment != null) {
			titleField.setText(appointment.getAppointmentTitle() != null ? appointment.getAppointmentTitle() : "");

			// Set consultation type
			if (appointment.getConsultationType() != null) {
				boolean found = false;
				for (int i = 0; i < typeComboBox.getItemCount(); i++) {
					if (typeComboBox.getItemAt(i).equals(appointment.getConsultationType())) {
						typeComboBox.setSelectedIndex(i);
						found = true;
						break;
					}
				}
				if (!found) {
					typeComboBox.setSelectedItem("Academic Consultation");
					otherTypeDropPanel.setDropdownVisible(true);
					boolean otherFound = false;
					for (int i = 0; i < otherTypeComboBox.getItemCount() - 1; i++) {
						if (otherTypeComboBox.getItemAt(i).equals(appointment.getConsultationType())) {
							otherTypeComboBox.setSelectedIndex(i);
							otherFound = true;
							break;
						}
					}
					if (!otherFound) {
						otherTypeComboBox.setSelectedItem("Custom...");
						customTypeField.setEnabled(true);
						customTypeField.setText(appointment.getConsultationType());
					}
				}
			}

			// Set date and time
			if (appointment.getAppointmentDateTime() != null) {
				LocalDateTime dateTime = appointment.getAppointmentDateTime().toLocalDateTime();
				datePicker.setSelectedDate(dateTime.toLocalDate());
				timePicker.setSelectedTime(dateTime.toLocalTime());
			}

			// Set notes
			notesArea.setText(appointment.getAppointmentNotes() != null ? appointment.getAppointmentNotes() : "");

			// Set appointment status
			if (appointment.getAppointmentStatus() != null && !appointment.getAppointmentStatus().isEmpty()) {
				appointmentStatusComboBox.setSelectedItem(appointment.getAppointmentStatus());
			} else {
				appointmentStatusComboBox.setSelectedItem("Scheduled");
			}

			// Load existing participants into the table
			loadParticipants(appointment.getParticipants());
		}
	}

	private void loadParticipants(List<Participants> participants) {
		tempParticipants.clear();
		if (participants != null) {
			for (Participants p : participants) {
				tempParticipants.add(
						new TempParticipant(Integer.valueOf(p.getParticipantId()),
											p.getStudentUid(),
											p.getParticipantFirstName(),
											p.getParticipantLastName(),
											p.getParticipantType(),
											p.getSex(),
											p.getContactNumber(),
											p.getStudentUid() != null,
											false,
											false));
			}
		}
		updateParticipantsTable();
	}

	private void removeParticipant(int row) {
		if (row >= 0 && row < tempParticipants.size()) {
			int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this participant?",
					"Confirm Removal", JOptionPane.YES_NO_OPTION);

			if (confirm == JOptionPane.YES_OPTION) {
				tempParticipants.remove(row);
				updateParticipantsTable();
			}
		}
	}

	private boolean checkForConflicts() throws SQLException {
		for (TempParticipant participant : tempParticipants) {
			List<Appointment> appointments = new AppointmentDAO(connection)
					.getAppointmentsByParticipant(participant.getFullName());
			for (Appointment existingAppointment : appointments) {
				// Skip checking conflict with the appointment currently being edited
				if (this.appointment.getAppointmentId() != null
						&& existingAppointment.getAppointmentId().equals(this.appointment.getAppointmentId())) {
					continue;
				}

				// Only consider 'Scheduled' or 'Active' appointments for conflict checks
				String status = existingAppointment.getAppointmentStatus();
				if (!"Scheduled".equals(status) && !"Active".equals(status)) {
					continue;
				}

				LocalDateTime existingStart = existingAppointment.getAppointmentDateTime().toLocalDateTime();
				LocalDateTime newStart = datePicker.getSelectedDate().atTime(timePicker.getSelectedTime());
				if (Math.abs(ChronoUnit.HOURS.between(existingStart, newStart)) < 1) {
					JOptionPane.showMessageDialog(this,
							"Participant " + participant.getFullName() + " is already booked within 1 hour.",
							"Booking Conflict", JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
		}
		return true;
	}

	public boolean saveAppointmentAsync(JButton saveButton) {
		saveButton.setEnabled(false);
		new SwingWorker<Boolean, Void>() {
			@Override
			protected Boolean doInBackground() {
				try {
					return saveAppointment();
				} catch (SQLException e) {
					ErrorDialogUtils.showError(AddAppointmentPanel.this, "Database error: " + e.getMessage());
					return false;
				}
			}
			@Override
			protected void done() {
				saveButton.setEnabled(true);
				try {
					boolean success = get();
					if (success) {
						// Show success toast/dialog
						showToast("Appointment saved successfully!", Toast.Type.SUCCESS);
					}
				} catch (Exception e) {
					ErrorDialogUtils.showError(AddAppointmentPanel.this, "Error: " + e.getMessage());
				}
			}
		}.execute();
		return true;
	}

	private boolean validateFields() {
		boolean isValid = true;
		// Reset all borders first
		titleField.putClientProperty("JTextField.borderColor", null);
		dateField.putClientProperty("JTextField.borderColor", null);
		timeField.putClientProperty("JTextField.borderColor", null);
		participantsTable.putClientProperty("JTable.borderColor", null);

		// Also reset validation for custom type field if visible
		if (customTypeField.isEnabled()) {
			customTypeField.putClientProperty("JTextField.borderColor", null);
		}

		// Use ValidationUtils for field checks
		if (ValidationUtils.isFieldEmpty(titleField)) {
			ErrorDialogUtils.showError(this, "Please enter an appointment title.");
			isValid = false;
		}

		// Validate date and time
		if (datePicker.getSelectedDate() == null || timePicker.getSelectedTime() == null) {
			dateField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
			timeField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
			showToast("Please select both date and time.", Toast.Type.ERROR);
			isValid = false;
		} else {
			// Validate that date is not in the past
			LocalDateTime selectedDateTime = datePicker.getSelectedDate().atTime(timePicker.getSelectedTime());
			LocalDateTime now = LocalDateTime.now();

			// Give a 1-minute buffer to account for the time it takes to fill the form
			if (selectedDateTime.isBefore(now.minusMinutes(1))) {
				dateField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
				timeField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
				showToast("Appointment date and time cannot be in the past.", Toast.Type.ERROR);
				isValid = false;
			}
		}

		// Validate consultation type
		if ("Other".equals(typeComboBox.getSelectedItem())) {
			if ("Custom...".equals(otherTypeComboBox.getSelectedItem()) && customTypeField.getText().trim().isEmpty()) {
				customTypeField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
				showToast("Please enter a custom consultation type.", Toast.Type.ERROR);
				isValid = false;
			}
		}

		// Validate participants
		if (tempParticipants.isEmpty()) {
			participantsTable.putClientProperty("JTable.borderColor", new Color(220, 53, 69));
			showToast("Please add at least one participant.", Toast.Type.ERROR);
			isValid = false;
		}

		return isValid;
	}

	private void showToast(String message, Toast.Type type) {
		ToastOption toastOption = Toast.createOption();
		Toast.show(this, type, message, toastOption);
	}

	public Appointment getAppointment() {
		return appointment;
	}

	private String getConsultationType() {
		String selected = (String) typeComboBox.getSelectedItem();
		if ("Other".equals(selected)) {
			String otherSelected = (String) otherTypeComboBox.getSelectedItem();
			if ("Custom...".equals(otherSelected)) {
				return customTypeField.getText().trim();
			} else {
				return otherSelected;
			}
		} else {
			return selected;
		}
	}

	private void addStudentParticipant(Student student) {
		boolean isDuplicate = tempParticipants.stream()
				.anyMatch(p -> p.getStudentUid() != null && p.getStudentUid().equals(student.getStudentUid()));

		if (!isDuplicate) {
			tempParticipants.add(new TempParticipant(null,
					student.getStudentUid(),
					student.getStudentFirstname(),
					student.getStudentLastname(),
					"Student",
					student.getStudentSex(),
					student.getContact() != null ? student.getContact().getContactNumber() : "",
					true,
					false,
					false));
			updateParticipantsTable();
		} else {
			showToast("Student already added.", Toast.Type.WARNING);
		}
	}

	private void addNonStudentParticipant(TempParticipant participant) {
		boolean isDuplicate = tempParticipants.stream()
				.anyMatch(p -> p.getFirstName().equalsIgnoreCase(participant.getFirstName())
						&& p.getLastName().equalsIgnoreCase(participant.getLastName())
						&& p.getType().equalsIgnoreCase(participant.getType()));

		if (!isDuplicate) {
			tempParticipants.add(participant);
			updateParticipantsTable();
		} else {
			showToast("Participant already added.", Toast.Type.WARNING);
		}
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
		participantsTable = new GTable(new Object[0][columnNames.length], columnNames, columnTypes, editableColumns,
				columnWidths, alignments, false, actionManager);

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

	private void updateParticipantsTable() {
		DefaultTableModel model = (DefaultTableModel) participantsTable.getModel();
		model.setRowCount(0);

		for (TempParticipant participant : tempParticipants) {
			model.addRow(new Object[] { participant.getFullName(), participant.getType(), "Remove" });
		}
	}

	public boolean saveAppointment() throws SQLException {
		if (!validateFields() || !checkForConflicts()) {
			return false;
		}

		// Set appointment details
		appointment.setAppointmentTitle(titleField.getText().trim());
		appointment.setConsultationType(getConsultationType());
		appointment.setAppointmentDateTime(
				Timestamp.valueOf(datePicker.getSelectedDate().atTime(timePicker.getSelectedTime())));
		appointment.setAppointmentNotes(notesArea.getText().trim());
		appointment.setAppointmentStatus((String) appointmentStatusComboBox.getSelectedItem());

		// Get the current logged-in counselor's ID
		GuidanceCounselor counselor = Main.formManager.getCounselorObject();
		if (counselor == null) {
			showToast("No counselor is currently logged in.", Toast.Type.ERROR);
			return false;
		}
		appointment.setGuidanceCounselorId(counselor.getGuidanceCounselorId());

		// Special handling for Good Moral Request and Home Visitation
		// These appointments don't need a session later, so mark them appropriately
		boolean isSpecialType = appointment.getConsultationType().equals("Good Moral Request")
				|| appointment.getConsultationType().equals("Home Visitation");

		if (isSpecialType) {
			appointment.setAppointmentNotes(notesArea.getText().trim() + "\n[This is a "
					+ appointment.getConsultationType() + " appointment that doesn't require a counseling session.]");
		}

		// Clear existing participants
		appointment.setParticipants(new ArrayList<>());

		// Add participants from temp list
		for (TempParticipant tempParticipant : tempParticipants) {
			// Create and save participant
			Participants participant = new Participants();
			participant.setStudentUid(tempParticipant.getStudentUid());
			participant.setParticipantType(tempParticipant.getType());
			participant.setParticipantFirstName(tempParticipant.getFirstName());
			participant.setParticipantLastName(tempParticipant.getLastName());
			participant.setSex(tempParticipant.getSex());
			participant.setContactNumber(tempParticipant.getContactNumber());

			// Save the participant to database
			try {
				ParticipantsDAO dao = new ParticipantsDAO(connection);
				int id = dao.createParticipant(participant);
				if (id > 0) {
					participant.setParticipantId(id);
					appointment.addParticipant(participant);
				}
			} catch (SQLException e) {
				// If participant already exists, fetch and use that one
				ParticipantsDAO dao = new ParticipantsDAO(connection);
				Participants existingParticipant = dao.getParticipantByDetails(tempParticipant.getFirstName(),
						tempParticipant.getLastName(), tempParticipant.getType());
				if (existingParticipant != null) {
					appointment.addParticipant(existingParticipant);
				} else {
					throw e;
				}
			}
		}

		// Save or update appointment
		boolean success;
		String actionType;
		if (appointment.getAppointmentId() == null) {
			success = appointmentDAO.insertAppointment(appointment);
			actionType = "created";
		} else {
			success = appointmentDAO.updateAppointment(appointment);
			actionType = "updated";
		}

		if (success) {
			// Format date and time for display
			java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter
					.ofPattern("EEEE, MMMM d, yyyy");
			java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a");

			String dateStr = appointment.getAppointmentDateTime().toLocalDateTime().format(dateFormatter);
			String timeStr = appointment.getAppointmentDateTime().toLocalDateTime().format(timeFormatter);

			// Create a more detailed success message
			String successMsg = "Appointment " + actionType + " successfully:\n" + appointment.getAppointmentTitle()
					+ "\n" + dateStr + " at " + timeStr + "\n" + "Type: " + appointment.getConsultationType();

			showToast(successMsg, Toast.Type.SUCCESS);

			// Publish event with context
			EventBus.publish("appointment_" + actionType, appointment);
		} else {
			showToast("Failed to " + (actionType.equals("created") ? "create" : "update") + " appointment",
					Toast.Type.ERROR);
		}

		return success;
	}
}
