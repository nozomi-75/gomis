package lyfjshs.gomis.view.sessions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.model.Session;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class SessionFullData extends Form {
	// Fields for session details
	private JTextField sessionIdField;
	private JTextField appointmentIdField;
	private JTextField counselorIdField;
	private JTextField sessionTypeField;
	private JTextField sessionDateTimeField;
	private JTextArea sessionNotesArea;
	private JTextField sessionStatusField;
	private JTextField updatedAtField;

	// Related entities
	private JTextField participantIdField;
	private JTextField violationIdField;
	private JButton printSessionReportBtn;

	public SessionFullData(Session sessionData) {
		this.setLayout(new MigLayout("", "[][grow][]", "[][]"));
		initComponents();

		// Populate fields with session data
		sessionIdField.setText(String.valueOf(sessionData.getSessionId()));
		appointmentIdField.setText(String.valueOf(sessionData.getAppointmentId()));
		counselorIdField.setText(String.valueOf(sessionData.getCounselorsId()));
		sessionTypeField.setText(sessionData.getSessionType());
		
		// Handle potential null for sessionDateTime
		if (sessionData.getSessionDateTime() != null) {
			sessionDateTimeField.setText(sessionData.getSessionDateTime().toString());
		} else {
			sessionDateTimeField.setText("N/A"); // Or handle as needed
		}

		sessionNotesArea.setText(sessionData.getSessionNotes());
		sessionStatusField.setText(sessionData.getSessionStatus());
		updatedAtField.setText(sessionData.getUpdatedAt() != null ? sessionData.getUpdatedAt().toString() : "N/A");
		participantIdField.setText(String.valueOf(sessionData.getParticipantId()));
		violationIdField.setText(String.valueOf(sessionData.getViolationId()));

		JPanel mainPanel = new JPanel(new MigLayout("", "[40px:n,grow 70,fill][100px:n,grow]", "[fill][grow,fill][]"));
		JScrollPane scroll = new JScrollPane(mainPanel);

		mainPanel.add(createSessionInfoPanel(), "cell 0 0,grow");
		mainPanel.add(createRelatedInfoPanel(), "cell 1 0,growx,aligny center");

		add(scroll, "cell 1 0,grow");

		JPanel panel = new JPanel(new MigLayout("", "[grow][][]", "[]"));
		add(panel, "cell 1 1,growx");

		printSessionReportBtn = new JButton("Print Session Report");
		printSessionReportBtn.addActionListener(e -> printSessionReport());
		panel.add(printSessionReportBtn, "cell 1 0,grow");
	}

	private void initComponents() {
		sessionIdField = new JTextField();
		appointmentIdField = new JTextField();
		counselorIdField = new JTextField();
		sessionTypeField = new JTextField();
		sessionDateTimeField = new JTextField();
		sessionNotesArea = new JTextArea(5, 20);
		sessionStatusField = new JTextField();
		updatedAtField = new JTextField();
		participantIdField = new JTextField();
		violationIdField = new JTextField();
	}

	private JPanel createSessionInfoPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][][][][][][]"));
		panel.setBorder(
				new TitledBorder(null, "Session Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		panel.add(new JLabel("Session ID:"));
		panel.add(sessionIdField, "growx");
		panel.add(new JLabel("Appointment ID:"));
		panel.add(appointmentIdField, "growx");
		panel.add(new JLabel("Counselor ID:"));
		panel.add(counselorIdField, "growx");
		panel.add(new JLabel("Session Type:"));
		panel.add(sessionTypeField, "growx");
		panel.add(new JLabel("Session Date/Time:"));
		panel.add(sessionDateTimeField, "growx");
		panel.add(new JLabel("Session Notes:"));
		panel.add(new JScrollPane(sessionNotesArea), "growx, span");
		panel.add(new JLabel("Session Status:"));
		panel.add(sessionStatusField, "growx");
		panel.add(new JLabel("Updated At:"));
		panel.add(updatedAtField, "growx");

		return panel;
	}

	private JPanel createRelatedInfoPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][]"));
		panel.setBorder(
				new TitledBorder(null, "Related Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		panel.add(new JLabel("Participant ID:"));
		panel.add(participantIdField, "growx");
		panel.add(new JLabel("Violation ID:"));
		panel.add(violationIdField, "growx");

		return panel;
	}

	private void printSessionReport() {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d'th' 'day of' MMMM, yyyy");
			String formattedDate = LocalDateTime.now().format(formatter);
			JOptionPane.showMessageDialog(this,
					"Printing Session Report for Session ID: " + sessionIdField.getText() + " on " + formattedDate);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed to print session report: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
