package lyfjshs.gomis.view.appointment.add;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.DAO.AppointmentDAO;
import lyfjshs.gomis.Database.DAO.ParticipantsDAO;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.datetime.TimePicker;
import raven.modal.Toast;
import raven.modal.component.Modal;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
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

	public AddAppointmentPanel(Appointment appointment, AppointmentDAO appointmentDAO, Connection connection) {
		this.appointment = appointment;
		this.appointmentDAO = appointmentDAO;
		this.connection = connection;
		initializeComponents();
	}

	private void initializeComponents() {
		setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow]"));

		// Create main content panel
		contentPanel = new JPanel(new MigLayout("insets 5", "[grow]", "[][][][][][][200px][][][][]"));

		// Create scroll pane with smooth scrolling
		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);
		scrollPane.setBorder(null);
		add(scrollPane, "grow");

		// Title
		contentPanel.add(new JLabel("Title:"), "wrap");
		titleField = new JTextField(appointment.getAppointmentTitle());
		contentPanel.add(titleField, "growx, wrap");

		// Type
		contentPanel.add(new JLabel("Type:"), "wrap");
		typeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance", "Personal Counseling",
				"Behavioral Counseling", "Group Counseling" });
		if (appointment.getConsultationType() != null) {
			typeComboBox.setSelectedItem(appointment.getConsultationType());
		}
		contentPanel.add(typeComboBox, "growx, wrap");

		// Date and Time Panel
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
		contentPanel.add(dateTimePanel, "growx, wrap");
		timeField = new JFormattedTextField();
		timePicker.setEditor(timeField);
		dateTimePanel.add(timeField, "cell 1 1,growx");

		// Participants Table
		contentPanel.add(new JLabel("Participants:"), "wrap");
		setupParticipantsTable();
		JScrollPane tableScrollPane = new JScrollPane(participantsTable);
		tableScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		tableScrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);
		contentPanel.add(tableScrollPane, "grow, wrap");

		// Participant Buttons Panel with Dropdown Area
		JPanel participantSection = new JPanel(new MigLayout("insets 0, wrap 1", "[grow]", "[][grow]"));
		
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
		participantSection.add(buttonPanel, "growx");

		// Create dropdown panels container
		JPanel dropdownContainer = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		
		// Create student search dropdown panel
		studentDropPanel = new DropPanel();
		studentDropPanel.setDropdownPadding(10, 10, 10, 10);
		
		StudentSearchPanel studentSearch = new StudentSearchPanel(connection, null) {
			@Override
			protected void onStudentSelected(Student student) {
				addStudentParticipant(student);
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
			updateParticipantsTable();
			nonStudentDropPanel.setDropdownVisible(false);
		});
		nonStudentDropPanel.setContent(nonStudentForm);
		dropdownContainer.add(nonStudentDropPanel, "cell 0 1,grow");
		
		// Add dropdown container to participant section
		participantSection.add(dropdownContainer, "grow");
		
		// Add the complete participant section to content panel
		contentPanel.add(participantSection, "growx, wrap");

		// Notes
		contentPanel.add(new JLabel("Notes:"), "wrap");
		notesArea = new JTextArea(4, 20);
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		if (appointment.getAppointmentNotes() != null) {
			notesArea.setText(appointment.getAppointmentNotes());
		}
		JScrollPane notesScrollPane = new JScrollPane(notesArea);
		notesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
		notesScrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.smoothScrolling", true);
		contentPanel.add(notesScrollPane, "grow, wrap");
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
			public void onTableAction(GTable table, int row) {
				removeParticipant(row);
			}

			@Override
			public void setupTableColumn(GTable table, int actionColumnIndex) {
				table.getColumnModel().getColumn(actionColumnIndex)
						.setCellRenderer((t, value, isSelected, hasFocus, row, column) -> {
							JButton button = new JButton("Remove");
							button.setBackground(new Color(220, 53, 69));
							button.setForeground(Color.WHITE);
							return button;
						});
			}
		};

		// Create table
		participantsTable = new GTable(new Object[0][3], columnNames, columnTypes, editableColumns, columnWidths,
				alignments, false, actionManager);

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

	private void addStudentParticipant(Student student) {
		TempParticipant participant = new TempParticipant(student.getStudentUid(), student.getStudentFirstname(),
				student.getStudentLastname(), "Student", student.getStudentSex(), "", true);

		// Check for duplicates
		boolean isDuplicate = tempParticipants.stream().anyMatch(p -> p.isStudent() && p.getStudentUid() != null
				&& p.getStudentUid().equals(participant.getStudentUid()));

		if (!isDuplicate) {
			tempParticipants.add(participant);
			updateParticipantsTable();
			// Reset border when valid participant is added
			participantsTable.putClientProperty("JTable.borderColor", null);
		} else {
			showToast("This student is already added as a participant.", Toast.Type.WARNING);
		}
	}

	private void removeParticipant(int row) {
		if (row >= 0 && row < tempParticipants.size()) {
			tempParticipants.remove(row);
			updateParticipantsTable();
		}
	}

	public boolean saveAppointment() throws SQLException {
		if (!validateFields()) {
			return false;
		}

		// Update appointment object
		appointment.setAppointmentTitle(titleField.getText().trim());
		appointment.setConsultationType((String) typeComboBox.getSelectedItem());

		LocalDateTime dateTime = LocalDateTime.of(datePicker.getSelectedDate(), timePicker.getSelectedTime());
		appointment.setAppointmentDateTime(Timestamp.valueOf(dateTime));

		appointment.setAppointmentStatus("Active");
		appointment.setAppointmentNotes(notesArea.getText().trim());

		// Start transaction
		connection.setAutoCommit(false);
		try {
			// Save appointment first
			if (!appointmentDAO.insertAppointment(appointment)) {
				throw new SQLException("Failed to save appointment");
			}

			// Now create and save participants
			ParticipantsDAO participantsDAO = new ParticipantsDAO(connection);
			List<Participants> savedParticipants = new ArrayList<>();

			for (TempParticipant temp : tempParticipants) {
				Participants participant = new Participants();
				participant.setStudentUid(temp.getStudentUid());
				participant.setParticipantType(temp.getType());
				participant.setParticipantFirstName(temp.getFirstName());
				participant.setParticipantLastName(temp.getLastName());
				participant.setSex(temp.getSex());
				participant.setContactNumber(temp.getContactNumber());
				// Save participant and get the generated ID
				int participantId = participantsDAO.createParticipant(participant);
				participant.setParticipantId(participantId);
				savedParticipants.add(participant);
			}

			// Add participants to appointment
			for (Participants participant : savedParticipants) {
				appointment.addParticipant(participant);
			}

			// Update appointment with participants
			appointmentDAO.addParticipantsToAppointment(appointment.getAppointmentId(),
					appointment.getParticipantIds());

			connection.commit();
			showToast("Appointment saved successfully!", Toast.Type.SUCCESS);
			return true;
		} catch (SQLException e) {
			connection.rollback();
			showToast("Failed to save appointment: " + e.getMessage(), Toast.Type.ERROR);
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	private boolean validateFields() {
		boolean isValid = true;
		// Reset all borders first
		titleField.putClientProperty("JTextField.borderColor", null);
		dateField.putClientProperty("JTextField.borderColor", null);
		timeField.putClientProperty("JTextField.borderColor", null);
		participantsTable.putClientProperty("JTable.borderColor", null);

		// Validate title
		if (titleField.getText().trim().isEmpty()) {
			titleField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
			showToast("Please enter an appointment title.", Toast.Type.ERROR);
			isValid = false;
		}

		// Validate date and time
		if (datePicker.getSelectedDate() == null || timePicker.getSelectedTime() == null) {
			dateField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
			timeField.putClientProperty("JTextField.borderColor", new Color(220, 53, 69));
			showToast("Please select both date and time.", Toast.Type.ERROR);
			isValid = false;
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
		toastOption.getLayoutOption()
				.setMargin(0, 0, 50, 0)
				.setDirection(ToastDirection.TOP_TO_BOTTOM);
		
		Toast.show(this, type, message, ToastLocation.BOTTOM_CENTER, toastOption);
	}

	public Appointment getAppointment() {
		return appointment;
	}
}
