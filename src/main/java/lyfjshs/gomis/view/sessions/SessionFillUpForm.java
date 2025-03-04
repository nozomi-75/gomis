package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

public class SessionFillUpForm extends JFrame {

	private JTextField dateField;
	private JComboBox<String> participantComboBox;
	private JComboBox<String> appointmentTypeComboBox;
	private JTextField startTimeField;
	private JTextField endTimeField;
	private JTextArea notesArea;
	private JTextField violationField;
	private JTextField recordedByField;
	private JTextArea summaryArea;
	private JComboBox<String> consultationTypeComboBox;
	private JPanel participantPanel;
	private JTextField firstNameField;
	private JTextField lastNameField;
	private JTextField contactNumberField;
	private JPanel mainPanel;
	private JTextField emailField;

	public SessionFillUpForm() {
		setTitle("Session Documentation Form");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(784, 715);
		getContentPane().setLayout(new BorderLayout());

		// Set FlatLaf
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		// Header
		JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel headerLabel = new JLabel("Session Documentation Form");
		headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		headerPanel.add(headerLabel);
		headerPanel.setBackground(new Color(5, 117, 230));
		headerPanel.setForeground(Color.WHITE);
		getContentPane().add(headerPanel, BorderLayout.NORTH);

		// Main Panel
		mainPanel = new JPanel(new MigLayout("wrap, gap 10, hidemode 3", "[][][][]", "[][][][][][][][grow 40][][][]"));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Participant
		JLabel label_3 = new JLabel("Participant");
		mainPanel.add(label_3, "flowx,cell 0 0,alignx right");

		// Date
		JLabel label = new JLabel("Date");
		mainPanel.add(label, "flowx,cell 2 0,alignx left");

		// Participant Panel
		JPanel createParticipantPanel = new JPanel(new MigLayout("gap 10", "[][grow][][][]", "[][][][]"));
		createParticipantPanel.setBorder(BorderFactory.createTitledBorder("Non-Student Participant"));

		createParticipantPanel.add(new JLabel("First Name"), "cell 0 0");
		firstNameField = new JTextField(10);
		createParticipantPanel.add(firstNameField, "cell 1 0,growx");

		JLabel label_10 = new JLabel("Last Name");
		createParticipantPanel.add(label_10, "cell 3 0");
		lastNameField = new JTextField(10);
		createParticipantPanel.add(lastNameField, "cell 4 0");

		JLabel label_11 = new JLabel("Contact Number");
		createParticipantPanel.add(label_11, "cell 0 1");
		contactNumberField = new JTextField(10);
		createParticipantPanel.add(contactNumberField, "cell 1 1 3 1,growx");

		participantPanel = createParticipantPanel;
		mainPanel.add(participantPanel, "cell 0 1 2 4,growx"); // Initially visible

		JLabel lblNewLabel = new JLabel("Email:");
		createParticipantPanel.add(lblNewLabel, "cell 0 2");

		emailField = new JTextField();
		createParticipantPanel.add(emailField, "cell 1 2 3 1,growx");
		emailField.setColumns(10);

		JButton saveParticipantButton = new JButton("Save Participant");
		createParticipantPanel.add(saveParticipantButton, "cell 1 3 4 1,alignx center");

		// Violation
		JLabel label_7 = new JLabel("Violation");
		mainPanel.add(label_7, "flowx,cell 2 1");

		// Appointment Type
		JLabel label_4 = new JLabel("Appointment Type");
		mainPanel.add(label_4, "flowx,cell 2 2,alignx left");

		// Start Time
		JLabel label_2 = new JLabel("Start Session Time");
		mainPanel.add(label_2, "flowx,cell 2 3");

		// End Time
		JLabel label_5 = new JLabel("End Session Time");
		mainPanel.add(label_5, "flowx,cell 3 3");

		// Consultation Type
		JLabel label_6 = new JLabel("Consultation Type");
		mainPanel.add(label_6, "flowx,cell 2 4,aligny top");

		// Notes
		JLabel label_9 = new JLabel("Notes");
		mainPanel.add(label_9, "cell 1 5");

		// Summary
		JLabel label_1 = new JLabel("Session Summary");
		mainPanel.add(label_1, "cell 0 6,aligny bottom");
		JButton printButton = new JButton("Print");
		JButton saveButton = new JButton("Save");
		notesArea = new JTextArea(4, 20);
		notesArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(notesArea);
		mainPanel.add(scrollPane, "cell 1 6 3 1,growx");
		summaryArea = new JTextArea(4, 20);
		summaryArea.setLineWrap(true);
		JScrollPane scrollPane_1 = new JScrollPane(summaryArea);
		mainPanel.add(scrollPane_1, "cell 0 7 4 1,grow");

		// Recorded By
		JLabel label_8 = new JLabel("Recorded By");
		mainPanel.add(label_8, "cell 2 8");
		recordedByField = new JTextField(10);
		mainPanel.add(recordedByField, "cell 3 8,growx");
		mainPanel.add(printButton, "cell 2 9,growx");
		mainPanel.add(saveButton, "cell 3 9,growx");

		getContentPane().add(mainPanel, BorderLayout.CENTER);
		dateField = new JTextField(10);
		mainPanel.add(dateField, "cell 2 0,alignx right");
		JButton participantButton = new JButton("Search");

		mainPanel.add(participantButton, "cell 1 0");
		participantComboBox = new JComboBox<>(new String[] { "Student", "Non-Student" });
		mainPanel.add(participantComboBox, "cell 0 0,growx");
		violationField = new JTextField(10);
		mainPanel.add(violationField, "cell 2 1");
		appointmentTypeComboBox = new JComboBox<>(
				new String[] { "From Appointment", "Walk-in", "Scheduled", "Follow-up" });
		mainPanel.add(appointmentTypeComboBox, "cell 2 2,growx");
		startTimeField = new JTextField(10);
		mainPanel.add(startTimeField, "cell 2 3,growx");
		endTimeField = new JTextField(10);
		mainPanel.add(endTimeField, "cell 3 3,growx");
		consultationTypeComboBox = new JComboBox<>(
				new String[] { "Academic Consultation", "Career Guidance", "Personal Counseling", "Administrative" });
		mainPanel.add(consultationTypeComboBox, "cell 2 4,growx,aligny top");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			SessionFillUpForm form = new SessionFillUpForm();
			form.setVisible(true);
		});
	}
}
