package lyfjshs.gomis.view.violation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class ViolationFillUpForm extends Form {
    private JComboBox<String> participantTypeDropdown;
    private JTextField lrnField, FIRST_NAMEField, LAST_NAMEField, emailField, contactField;
    private JTextArea descriptionArea;
    private JButton addButton;
    private JComboBox<String> violationTypeDropdown;
    private JComboBox<String> reinforcementDropdown;
    private JComboBox<String> statusDropdown;
    private ViolationCRUD violationCRUD;
    private Connection connect;

	public ViolationFillUpForm(Connection conn ) {
		this.connect = conn;
		violationCRUD = new ViolationCRUD(connect);
		setLayout(new MigLayout("", "[grow]", "[][grow][pref]"));

		// Header Panel
		JPanel headerPanel = new JPanel(new MigLayout("", "[grow]", "[]"));
		JLabel lblTitle = new JLabel("VIOLATION MANAGEMENT");
		lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
		headerPanel.add(lblTitle, "grow");
		add(headerPanel, "cell 0 0, growx");

		// Main Input Panel
		JPanel inputPanel = new JPanel(new MigLayout("", "[][grow][][grow]", "[][][][][][grow 60][100px]"));
		inputPanel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(new Color(100, 149, 177)),
			"VIOLATION DETAILS",
			TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION,
			new Font("Arial", Font.BOLD, 12),
			new Color(100, 149, 177)));

		// Basic Information
		inputPanel.add(new JLabel("Participant Type:"), "cell 0 0");
		participantTypeDropdown = new JComboBox<>(new String[]{"Student", "Teacher", "Other"});
		inputPanel.add(participantTypeDropdown, "cell 1 0, growx");
		
		inputPanel.add(new JLabel("LRN:"), "cell 2 0");
		lrnField = new JTextField();
		inputPanel.add(lrnField, "cell 3 0, growx, wrap");

		inputPanel.add(new JLabel("First Name:"), "cell 0 1");
		FIRST_NAMEField = new JTextField();
		inputPanel.add(FIRST_NAMEField, "cell 1 1, growx");
		
		inputPanel.add(new JLabel("Last Name:"), "cell 2 1");
		LAST_NAMEField = new JTextField();
		inputPanel.add(LAST_NAMEField, "cell 3 1, growx, wrap");

		inputPanel.add(new JLabel("Email:"), "cell 0 2");
		emailField = new JTextField();
		inputPanel.add(emailField, "cell 1 2, growx");
		
		inputPanel.add(new JLabel("Contact:"), "cell 2 2");
		contactField = new JTextField();
		inputPanel.add(contactField, "cell 3 2, growx, wrap");

		// Violation Information
		inputPanel.add(new JLabel("Violation Type:"), "cell 0 3");
		violationTypeDropdown = new JComboBox<>(new String[]{
			"Minor", "Major", "Academic Dishonesty", "Bullying", "Dress Code", 
			"Tardiness", "Cutting Classes", "Vandalism"
		});
		inputPanel.add(violationTypeDropdown, "cell 1 3, growx");
		
		inputPanel.add(new JLabel("Status:"), "cell 2 3");
		statusDropdown = new JComboBox<>(new String[]{
			"Pending", "Under Investigation", "Resolved"
		});
		inputPanel.add(statusDropdown, "cell 3 3, growx, wrap");

		inputPanel.add(new JLabel("Reinforcement:"), "cell 0 4");
		reinforcementDropdown = new JComboBox<>(new String[]{
			"Warning", "Counseling", "Parent Conference", "Suspension", 
			"Community Service", "Written Warning", "Verbal Warning"
		});
		inputPanel.add(reinforcementDropdown, "cell 1 4, growx, wrap");

		// Description Area
		JLabel label = new JLabel("Description:");
		label.setFont(new Font("Tahoma", Font.BOLD, 13));
		inputPanel.add(label, "cell 0 5");
		descriptionArea = new JTextArea(4, 20);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(descriptionArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(new Color(100, 149, 177)),
			"Description",
			TitledBorder.DEFAULT_JUSTIFICATION,
			TitledBorder.DEFAULT_POSITION,
			new Font("Tahoma", Font.BOLD, 14),
			new Color(0, 0, 0)));
		inputPanel.add(scrollPane, "cell 1 5 3 1, grow, wrap");

		add(inputPanel, "cell 0 1, grow");

		// Button Panel
		JPanel buttonPanel = new JPanel(new MigLayout("", "[center, grow]", "[]"));
		addButton = new JButton("Add");
		addButton.addActionListener(e -> addViolation());
		buttonPanel.add(addButton, "cell 0 0");
		add(buttonPanel, "cell 0 2");

		lrnField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) || lrnField.getText().length() >= 12) {
					e.consume();
				}
			}
		});

		contactField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) || contactField.getText().length() >= 11) {
					e.consume();
				}
			}
		});

		FIRST_NAMEField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isLetter(c) && !Character.isSpaceChar(c)) {
					e.consume();
				}
			}
		});

		LAST_NAMEField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isLetter(c) && !Character.isSpaceChar(c)) {
					e.consume();
				}
			}
		});

		// Add this after creating participantTypeDropdown
		participantTypeDropdown.addActionListener(e -> updateFields());
	}

	private void updateFields() {
		String participantType = (String) participantTypeDropdown.getSelectedItem();
		boolean isStudent = "Student".equals(participantType);
		
		// Show/hide LRN field and label based on participant type
		lrnField.setEnabled(isStudent);
		lrnField.setVisible(isStudent);
		lrnField.setText(isStudent ? lrnField.getText() : "");  // Clear LRN if not student
		
		// Find and update LRN label visibility
		Component[] components = ((JPanel)lrnField.getParent()).getComponents();
		for (Component c : components) {
			if (c instanceof JLabel && ((JLabel)c).getText().equals("LRN:")) {
				c.setVisible(isStudent);
				break;
			}
		}
	}

	private void addViolation() {
		String participantType = (String) participantTypeDropdown.getSelectedItem();
		String lrn = lrnField.getText().trim();
		String FIRST_NAME = FIRST_NAMEField.getText().trim();
		String LAST_NAME = LAST_NAMEField.getText().trim();
		String email = emailField.getText().trim();
		String contact = contactField.getText().trim();
		String violationType = (String) violationTypeDropdown.getSelectedItem();
		String reinforcement = (String) reinforcementDropdown.getSelectedItem();
		String status = (String) statusDropdown.getSelectedItem();
		String description = descriptionArea.getText().trim();

		// Validate fields
		if (FIRST_NAME.isEmpty() || LAST_NAME.isEmpty() || email.isEmpty() || contact.isEmpty() || 
			violationType.isEmpty() || description.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Create violation record in database
		boolean success = violationCRUD.createViolation(
			connect,
			lrn,
			violationType, 
			description,
			FIRST_NAME,
			LAST_NAME,
			email,
			contact,
			participantType,
			reinforcement,
			status
		);

		if (success) {
			JOptionPane.showMessageDialog(this, "Violation added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			clearFields(); // Clear the input fields
		} else {
			JOptionPane.showMessageDialog(this, "Error adding violation to database", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void clearFields() {
		lrnField.setText("");
		FIRST_NAMEField.setText("");
		LAST_NAMEField.setText("");
		emailField.setText("");
		contactField.setText("");
		descriptionArea.setText("");
	}
}
