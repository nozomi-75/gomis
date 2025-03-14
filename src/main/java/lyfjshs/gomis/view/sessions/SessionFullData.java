package lyfjshs.gomis.view.sessions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Participants;
import lyfjshs.gomis.Database.entity.Sessions;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;
import lyfjshs.gomis.components.table.TableActionManager;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;

public class SessionFullData extends Form {
	// Fields for session details
	private JTextField sessionDateTimeField;
	private JTextArea sessionNotesArea;
	private JTextField sessionStatusField;
	private JTextField updatedAtField;
	private JTextField appointmentTypeField;
	private JTextField counselorNameField;
	private JButton printSessionReportBtn;
	private JTable participantsTable;

	public SessionFullData(Sessions sessionData, GuidanceCounselor counselor, List<Participants> participants) {
		this.setLayout(new MigLayout("", "[][grow][]", "[][]"));
		initComponents();

		// Populate fields with session data
		if (sessionData.getSessionDateTime() != null) {
			sessionDateTimeField.setText(sessionData.getSessionDateTime().toString());
		} else {
			sessionDateTimeField.setText("N/A");
		}

		sessionNotesArea.setText(sessionData.getSessionNotes());
		sessionStatusField.setText(sessionData.getSessionStatus());
		updatedAtField.setText(sessionData.getUpdatedAt() != null ? sessionData.getUpdatedAt().toString() : "N/A");
		appointmentTypeField.setText(sessionData.getAppointmentType());
		counselorNameField.setText(counselor.getFirstName() + " " + counselor.getLastName());

		// Initialize participants table
		participantsTable = new JTable();
		setupParticipantsTable(participants);

		JPanel mainPanel = new JPanel(new MigLayout("", "[40px:n,grow 70,fill][100px:n,grow]", "[271.00,fill][]"));
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
		sessionDateTimeField = new JTextField();
		sessionNotesArea = new JTextArea(5, 20);
		sessionStatusField = new JTextField();
		updatedAtField = new JTextField();
		appointmentTypeField = new JTextField();
		counselorNameField = new JTextField();
	}

	private JPanel createSessionInfoPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[140px][grow,fill]", "[][][][][]"));
		panel.setBorder(
				new TitledBorder(null, "Session Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		panel.add(new JLabel("Session Date/Time:"));
		panel.add(sessionDateTimeField, "growx");
		panel.add(new JLabel("Session Notes:"));
		panel.add(new JScrollPane(sessionNotesArea), "growx, span");
		panel.add(new JLabel("Session Status:"));
		panel.add(sessionStatusField, "growx");
		panel.add(new JLabel("Updated At:"));
		panel.add(updatedAtField, "growx");
		panel.add(new JLabel("Appointment Type:"));
		panel.add(appointmentTypeField, "growx");
		panel.add(new JLabel("Counselor Name:"));
		panel.add(counselorNameField, "growx");

		return panel;
	}

	private JPanel createRelatedInfoPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[140px,grow]", "[]"));
		panel.setBorder(
				new TitledBorder(null, "Related Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		// Add the participants table to the related info panel
		JScrollPane scrollPane = new JScrollPane(participantsTable);
		panel.add(scrollPane, "cell 0 0,grow");

		return panel;
	}

	private void setupParticipantsTable(List<Participants> participants) {
		String[] columnNames = {"#", "Participant Name", "Participant Type", "Actions"};
		DefaultTableModel model = new DefaultTableModel(columnNames, 0);

		for (int i = 0; i < participants.size(); i++) {
			Participants participant = participants.get(i);
			Object[] rowData = {
					i + 1,
					participant.getParticipantFirstName() + " " + participant.getParticipantLastName(),
					participant.getParticipantType(),
					"Actions" // Placeholder for actions column
			};
			model.addRow(rowData);
		}

		participantsTable.setModel(model);

		// Setup table actions
		setupTableActions();
	}

	private void setupTableActions() {
		TableActionManager actionManager = new TableActionManager();
		actionManager.addAction("View", (table, row) -> {
			Participants participant = (Participants) table.getValueAt(row, 1);
			viewParticipantDetails(participant);
		}, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));

		actionManager.applyTo(participantsTable, 3); // Apply actions to the "Actions" column
	}

	private void viewParticipantDetails(Participants participant) {
		JOptionPane.showMessageDialog(this, "Viewing details for: " + participant.getParticipantFirstName() + " " + participant.getParticipantLastName());
	}

	private void printSessionReport() {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d'th' 'day of' MMMM, yyyy");
			String formattedDate = LocalDateTime.now().format(formatter);
			JOptionPane.showMessageDialog(this,
					"Printing Session Report for Session Date/Time: " + sessionDateTimeField.getText() + " on " + formattedDate);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed to print session report: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
