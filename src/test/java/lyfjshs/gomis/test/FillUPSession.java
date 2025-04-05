package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatDarkLaf;

import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.view.appointment.add.NonStudentPanel;
import lyfjshs.gomis.view.appointment.add.TempParticipant;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import net.miginfocom.swing.MigLayout;

public class FillUPSession extends JPanel {

	// === FIELDS ===
	private JTextField otherViolationField;
	private JFormattedTextField sessionDateField;
	private JFormattedTextField sessionTimeField;
	private JComboBox<String> appointmentTypeCombo;
	private JButton btnSearchAppointment;
	private JComboBox<String> consultationTypeCombo;
	private JComboBox<String> violationTypeCombo;
	private JComboBox<String> sessionStatusCombo;
	private JTextArea quickNotesArea;
	private JTextField recordedByField;
	private JButton addStudentBtn;
	private JButton addNonStudentBtn;
	private JTable participantTable;
	private JTextArea summaryArea;
	private JButton saveBtn;
	private JTabbedPane tabs;
	private DropPanel studentDropPanel;
	private DropPanel nonStudentDropPanel;
	private Connection connect;
	// === CONSTRUCTOR ===
	public FillUPSession(Connection conn) {
		this.connect = conn;
		initComponents();
		initLayout();
	}

	// suggestion: instead of
	// .setBorder(BorderFactory.createLineBorder(Color.decode("#e0e0e0"))); use
	// FlatLaf properties arc:20 to make an round shape and set its background to 5%
	// dark or light depends (add some insets 5)
	// sample code:
//	putClientProperty(FlatClientProperties.STYLE, "arc:20; [light]background:darken(@background,10%); [dark]background:lighten(@background,10%);");

	// === COMPONENT INITIALIZATION ===
	private void initComponents() {
		sessionDateField = new JFormattedTextField();
		sessionTimeField = new JFormattedTextField();
		appointmentTypeCombo = new JComboBox<>(new String[] { "Walk-In", "Scheduled", "Emergency" });
		btnSearchAppointment = new JButton("Search Appointment");
		consultationTypeCombo = new JComboBox<>(new String[] { "Academic Consultation", "Career Advising" });
		violationTypeCombo = new JComboBox<>(
				new String[] { "-- Select Violation --", "Academic Dishonesty", "Behavioral Misconduct" });
		sessionStatusCombo = new JComboBox<>(
				new String[] { "-- Select Status --", "Pending", "In Progress", "Completed" });
		otherViolationField = new JTextField(10);
		quickNotesArea = new JTextArea(5, 20);
		recordedByField = new JTextField("Alice B Smith");
		addStudentBtn = new JButton("Add Student");
		addNonStudentBtn = new JButton("Add Non-Student");
		summaryArea = new JTextArea("Enter session summary here...", 5, 20);
		saveBtn = new JButton("SAVE SESSION");

		String[] columns = { "#", "Participant Name", "Participant Type", "Actions" };
		Object[][] data = { { "1", "John Smith", "Student", "Edit | Remove" } };
		participantTable = new JTable(data, columns);

		tabs = new JTabbedPane();

		// Styling
		addStudentBtn.setBackground(new Color(0, 120, 215));
		addStudentBtn.setForeground(Color.WHITE);
		addNonStudentBtn.setBackground(new Color(46, 204, 113));
		addNonStudentBtn.setForeground(Color.WHITE);
		saveBtn.setBackground(new Color(0, 120, 215));
		saveBtn.setForeground(Color.WHITE);

		quickNotesArea.setLineWrap(true);
		quickNotesArea.setWrapStyleWord(true);
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
	}

	// === LAYOUT SETUP ===
	private void initLayout() {
		this.setLayout(new MigLayout("insets 0, fill", "[grow]", "[][100px,grow][grow][]"));
		
		// === Title Bar ===
		JPanel titleBar = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
		titleBar.setBackground(new Color(0, 120, 215));
		JLabel title = new JLabel("Session Documentation Form");
		title.setFont(new Font("Segoe UI", Font.BOLD, 18));
		title.setForeground(Color.WHITE);
		titleBar.add(title, "cell 0 0, center");
		add(titleBar, "cell 0 0, growx");

		// === Main Split Panel ===
		JPanel sessionDetailsPanel = new JPanel(new MigLayout("insets 0, fill", "[grow,fill]15[grow]", "[]"));
		add(sessionDetailsPanel, "cell 0 1, grow");

		// === Left Section (Session Info) ===
		JPanel sessionAttributes = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[]"));
		sessionDetailsPanel.add(sessionAttributes, "cell 0 0, grow");

		JPanel sessionInfoCard = new JPanel(new MigLayout("insets 5", "[grow]", "[][]"));
		sessionInfoCard.setBorder(BorderFactory.createLineBorder(Color.decode("#e0e0e0")));

		JPanel topFields = new JPanel(new MigLayout("insets 0", "[grow]10[grow]10[grow]10[grow]", "[][]"));
		topFields.add(new JLabel("Session Date"), "cell 0 0");
		topFields.add(sessionDateField, "cell 0 1, growx");

		topFields.add(new JLabel("Session Time"), "cell 1 0");
		topFields.add(sessionTimeField, "cell 1 1, growx");

		topFields.add(new JLabel("Appointment Type"), "cell 2 0");
		JPanel apptPanel = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
		apptPanel.add(appointmentTypeCombo, "cell 0 0, growx");
		apptPanel.add(btnSearchAppointment, "cell 1 0");
		topFields.add(apptPanel, "cell 2 1, growx");

		topFields.add(new JLabel("Consultation Type"), "cell 3 0");
		topFields.add(consultationTypeCombo, "cell 3 1, growx");

		JPanel bottomFields = new JPanel(new MigLayout("insets 0", "[grow]10[grow]", "[][]"));
		bottomFields.add(new JLabel("Violation Type"), "cell 0 0");
		bottomFields.add(violationTypeCombo, "cell 0 1, growx");

		bottomFields.add(new JLabel("Session Status"), "cell 1 0");
		bottomFields.add(sessionStatusCombo, "cell 1 1, growx");

		bottomFields.add(new JLabel("Other Violation"), "cell 0 2");
		bottomFields.add(otherViolationField, "cell 0 3, growx");

		sessionInfoCard.add(topFields, "cell 0 0, growx");
		sessionInfoCard.add(bottomFields, "cell 0 1, growx");

		sessionAttributes.add(sessionInfoCard, "cell 0 0,grow");

		// === Right Section (Notes Panel) ===
		JPanel sessionNotes = new JPanel(new MigLayout("insets 5", "[grow]", "[][][][]"));
		sessionNotes.setBorder(BorderFactory.createLineBorder(Color.decode("#e0e0e0")));

		sessionNotes.add(new JLabel("Notes"), "cell 0 0");
		sessionNotes.add(new JScrollPane(quickNotesArea), "cell 0 1, grow");

		sessionNotes.add(new JLabel("Recorded By"), "cell 0 2");
		sessionNotes.add(recordedByField, "cell 0 3, growx");

		sessionDetailsPanel.add(sessionNotes, "cell 1 0, grow");

		// === TABS PANEL ===
		JPanel tabsCard = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[grow]"));
		tabsCard.setBorder(BorderFactory.createLineBorder(Color.decode("#e0e0e0")));

		// === Participants Tab ===
		JPanel participantsPanel = new JPanel(new MigLayout("insets 5", "[grow]", "[][grow][][]"));
		JPanel buttonRow = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
		buttonRow.add(addStudentBtn, "cell 0 0");
		buttonRow.add(addNonStudentBtn, "cell 1 0");
		participantsPanel.add(buttonRow, "cell 0 0, growx");

		JPanel dropdownContainer = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
		participantsPanel.add(dropdownContainer, "cell 0 1, grow");

		// Create student search dropdown panel
		studentDropPanel = new DropPanel();
		studentDropPanel.setDropdownPadding(10, 10, 10, 10);

		StudentSearchPanel studentSearch = new StudentSearchPanel(connect, null) {
			@Override
			protected void onStudentSelected(Student student) {
				// addStudentParticipant(student);
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
			// Convert the NonStudentPanel's TempParticipant to SessionsForm's
			// TempParticipant
			TempParticipant sessionParticipant = new TempParticipant(null, // studentUid is null for non-students
					participant.getFirstName(), participant.getLastName(), "Non-Student", participant.getSex(),
					participant.getContactNumber(), false // isStudent = false
			);
			// tempParticipants.add(sessionParticipant);
			// updateParticipantsTable();
			nonStudentDropPanel.setDropdownVisible(false);
		});
		nonStudentDropPanel.setContent(nonStudentForm);
		dropdownContainer.add(nonStudentDropPanel, "cell 0 1,grow");

		JScrollPane tableScroll = new JScrollPane(participantTable);
		tableScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
		participantsPanel.add(tableScroll, "cell 0 3, grow");

		tabs.addTab("Participants", participantsPanel);

		// === Summary Tab ===
		JPanel summaryPanel = new JPanel(new MigLayout("insets 5, fill", "[grow]", "[grow]"));
		summaryPanel.add(new JScrollPane(summaryArea), "cell 0 0, grow");
		tabs.addTab("Session Summary", summaryPanel);

		tabsCard.add(tabs, "cell 0 0, grow");
		add(tabsCard, "cell 0 2, grow");

		// === Save Button ===
		add(saveBtn, "cell 0 3, growx");
	}

	// === MAIN FOR TESTING ===
	public static void main(String[] args) {
		FlatDarkLaf.setup();
		JFrame frame = new JFrame("Session Documentation Form");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new FillUPSession(null));
		frame.setSize(1400, 800);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
