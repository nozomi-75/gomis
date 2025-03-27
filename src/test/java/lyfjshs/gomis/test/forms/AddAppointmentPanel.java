package lyfjshs.gomis.test.forms;

import java.awt.Color;
import java.awt.Component;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import raven.modal.component.Modal;

public class AddAppointmentPanel extends Modal {

	public AddAppointmentPanel() {
		this.setLayout(new MigLayout("fill, insets 10", "[grow]", "[grow]"));
		initializeComponents();
	}

	private void initializeComponents() {

		// Body Panel with MigLayout
		JPanel bodyPanel = new JPanel(new MigLayout("wrap 1", "[grow][grow]", "[][][][][][][][][][][]"));

		// Title Field
		bodyPanel.add(new JLabel("Title:"), "cell 0 0,alignx label");
		JTextField titleField = new JTextField();
		bodyPanel.add(titleField, "cell 0 1 2 1,growx");

		// Date Field
		bodyPanel.add(new JLabel("Date:"), "flowx,cell 0 2,alignx left");
		JFormattedTextField dateField = new JFormattedTextField();
		bodyPanel.add(dateField, "cell 0 2,growx");

		// Time Field
		JLabel label = new JLabel("Time:");
		bodyPanel.add(label, "flowx,cell 1 2,alignx label");
		JFormattedTextField timeField = new JFormattedTextField();
		bodyPanel.add(timeField, "cell 1 2,growx");

		// Appointment Type Dropdown
		JLabel label_1 = new JLabel("Appointment Type:");
		bodyPanel.add(label_1, "cell 0 3 2 1,growx");
		JComboBox<String> typeComboBox = new JComboBox<>(new String[] { "Academic Consultation", "Career Guidance",
				"Personal Consultation", "Behavioral Consultation", "Group Consultation" });
		bodyPanel.add(typeComboBox, "cell 0 4 2 1,growx");

		// Guidance Counselor Details
		JLabel counselorLabel = new JLabel("Guidance Counselor:");
		bodyPanel.add(counselorLabel, "cell 0 5,alignx label");
		JTextField counselorDetailsField = new JTextField();
		counselorDetailsField.setEditable(false);
		bodyPanel.add(counselorDetailsField, "cell 0 6 2 1,growx");

		// Status Field
		JLabel statusLabel = new JLabel("Status:");
		bodyPanel.add(statusLabel, "cell 0 7,alignx label");
		JTextField statusField = new JTextField();
		bodyPanel.add(statusField, "cell 0 8 2 1,growx");

		// Participants Section
		JPanel participantsPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[]"));
		participantsPanel.setBorder(BorderFactory.createTitledBorder("Participants"));
		addParticipantPanel(participantsPanel); // Add initial participant
		bodyPanel.add(participantsPanel, "cell 0 9 2 1,grow");

		// Add Participant Button
		JButton addParticipantButton = new JButton("Add Another Participant");
		addParticipantButton.setBackground(new Color(33, 150, 243));
		addParticipantButton.setForeground(Color.WHITE);
		addParticipantButton.setFocusPainted(false);
		bodyPanel.add(addParticipantButton, "cell 0 10 2 1,alignx center");

		// Notes Field
		JLabel label_2 = new JLabel("Notes:");
		bodyPanel.add(label_2, "cell 0 11,alignx label");
		JTextArea notesArea = new JTextArea();
		notesArea.setRows(3);
		notesArea.setLineWrap(true);
		JScrollPane scrollPane_1 = new JScrollPane(notesArea);
		bodyPanel.add(scrollPane_1, "cell 0 12 2 1,growx");

		JScrollPane scrollPane = new JScrollPane(bodyPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, "growx");
	}

	private void addParticipantPanel(JPanel parentPanel) {
		JPanel participantPanel = new JPanel(new MigLayout("hidemode 3", "[right][grow]", "[][]"));
		participantPanel.setBorder(BorderFactory.createEtchedBorder());

		// Participant Type
		JLabel typeLabel = new JLabel("Type:");
		JComboBox<String> typeComboBox = new JComboBox<>(new String[] { "Student", "Non-Student" });
		participantPanel.add(typeLabel);
		participantPanel.add(typeComboBox, "wrap");

		// Create panels for both types
		JPanel studentPanel = createStudentPanel();
		JPanel nonStudentPanel = createNonStudentPanel();

		// Add both panels to the same cell but initially show only student panel
		participantPanel.add(studentPanel, "cell 0 1 2 1,grow");
		participantPanel.add(nonStudentPanel, "cell 0 1 2 1,grow");
		studentPanel.setVisible(true);
		nonStudentPanel.setVisible(false);

		// Add action listener to typeComboBox to switch between panels
		typeComboBox.addActionListener(e -> {
			String selectedType = (String) typeComboBox.getSelectedItem();
			studentPanel.setVisible("Student".equals(selectedType));
			nonStudentPanel.setVisible("Non-Student".equals(selectedType));
			participantPanel.revalidate();
			participantPanel.repaint();
		});

		// Remove Button in its own row
		JButton removeButton = new JButton("Remove Participant");
		removeButton.setBackground(new Color(244, 67, 54));
		removeButton.setForeground(Color.WHITE);
		removeButton.setFocusPainted(false);
		participantPanel.add(removeButton, "cell 0 2 2 1, alignx center");

		parentPanel.add(participantPanel, "growx, wrap");
	}

	private JPanel createStudentPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow]", "[]"));

		// LRN Field
		JLabel lrnLabel = new JLabel("LRN:");
		JTextField studentLrnField = new JTextField();
		panel.add(lrnLabel);
		panel.add(studentLrnField, "grow,wrap");

		// First Name
		JLabel firstNameLabel = new JLabel("First Name:");
		JTextField studentFirstNameField = new JTextField();
		panel.add(firstNameLabel);
		panel.add(studentFirstNameField, "growx, wrap");

		// Last Name
		JLabel lastNameLabel = new JLabel("Last Name:");
		JTextField studentLastNameField = new JTextField();
		panel.add(lastNameLabel);
		panel.add(studentLastNameField, "growx, wrap");

		// Sex
		JLabel sexLabel = new JLabel("Sex:");
		JComboBox<String> studentSexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
		panel.add(sexLabel);
		panel.add(studentSexComboBox, "growx, wrap");

		// Search buttons
		JPanel searchPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
		JButton searchLrnButton = new JButton("Search by LRN");
		JButton searchNameButton = new JButton("Search by Name");
		searchPanel.add(searchLrnButton, "growx");
		searchPanel.add(searchNameButton, "growx");
		panel.add(searchPanel, "span 2, growx");

		return panel;
	}

	private JPanel createNonStudentPanel() {
		JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow]", "[]"));

		// First Name
		JLabel firstNameLabel = new JLabel("First Name:");
		JTextField nonStudentFirstNameField = new JTextField();
		panel.add(firstNameLabel);
		panel.add(nonStudentFirstNameField, "growx, wrap");

		// Last Name
		JLabel lastNameLabel = new JLabel("Last Name:");
		JTextField nonStudentLastNameField = new JTextField();
		panel.add(lastNameLabel);
		panel.add(nonStudentLastNameField, "growx, wrap");

		// Contact Number
		JLabel contactLabel = new JLabel("Contact Number:");
		JTextField nonStudentContactField = new JTextField();
		panel.add(contactLabel);
		panel.add(nonStudentContactField, "growx, wrap");

		// Email
		JLabel emailLabel = new JLabel("Email:");
		JTextField nonStudentEmailField = new JTextField();
		panel.add(emailLabel);
		panel.add(nonStudentEmailField, "growx, wrap");

		// Add validation for required fields
		nonStudentFirstNameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { validateFields(); }
			public void removeUpdate(DocumentEvent e) { validateFields(); }
			public void insertUpdate(DocumentEvent e) { validateFields(); }
		});

		nonStudentLastNameField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { validateFields(); }
			public void removeUpdate(DocumentEvent e) { validateFields(); }
			public void insertUpdate(DocumentEvent e) { validateFields(); }
		});

		nonStudentContactField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { validateFields(); }
			public void removeUpdate(DocumentEvent e) { validateFields(); }
			public void insertUpdate(DocumentEvent e) { validateFields(); }
		});

		return panel;
	}

	private void validateFields() {
		// Get all text fields from the non-student panel
		Component[] components = ((JPanel) getComponent(0)).getComponents();
		for (Component comp : components) {
			if (comp instanceof JPanel) {
				JPanel panel = (JPanel) comp;
				for (Component field : panel.getComponents()) {
					if (field instanceof JTextField) {
						JTextField textField = (JTextField) field;
						if (textField.getText().trim().isEmpty()) {
							textField.setBorder(BorderFactory.createLineBorder(Color.RED));
						} else {
							textField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
						}
					}
				}
			}
		}
	}
}
