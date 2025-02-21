package lyfjshs.gomis.view.incident;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;

public class IncidentList extends Form {

	private JTable table;
	private JPanel cardPanel;
	private CardLayout cardLayout;
	private static final String TABLE_VIEW = "table";
	private static final String DETAILS_VIEW = "details";
	private Connection connect;
	private JTextField nameField;
	private JTextField ageField;
	private JTextField contactField;
	private JTextField sexField;
	private JTextField guardianField;
	private JTextField guardianContactField;
	private JTextField incidentTypeField;
	private JTextArea narrativeField;
	private JTextArea actionTakenField;
	private JTextArea recommendationField;
	private JButton saveButton; // Declare Save button as a class variable

	public IncidentList(Connection conn) {
		this.connect = conn;
		setLayout(new BorderLayout(0, 0));

		// Create card layout and panel
		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);

		// Create and add table panel
		JPanel tablePanel = createTablePanel();
		cardPanel.add(tablePanel, TABLE_VIEW);

		add(cardPanel);
	}

	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());

		// Table Headers
		String[] columnNames = { "#", "LRN", "Student Name", "Incident Name", "Date", "Actions" };

		// Sample Data
		Object[][] data = { { 1, "136587425698", "John Smith", "Fighting in Class", "2024-02-09", "" },
				{ 2, "136587425699", "Mary Johnson", "Cutting Classes", "2024-02-08", "" },
				{ 3, "136587425700", "Peter Parker", "Bullying", "2024-02-07", "" },
				{ 4, "136587425701", "Jane Doe", "Cheating on Exam", "2024-02-06", "" } };

		DefaultTableModel model = new DefaultTableModel(data, columnNames);
		table = new JTable(model);
		table.setRowHeight(30);

		// Set preferred column widths
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(3).setPreferredWidth(150);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);

		// Configure action column using TableActionManager
		TableActionManager actionManager = new TableActionManager();
		actionManager.addAction("View", (t, row) -> {
			String studentName = t.getValueAt(row, 2).toString();
			String incidentName = t.getValueAt(row, 3).toString();
			showIncidentForm(studentName, incidentName);
		}, new Color(100, 149, 177), null);
		
		actionManager.applyTo(table, 5); // Apply to the "Actions" column (index 5)

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}

	private void showIncidentForm(String studentName, String incidentName) {
		// Get incident details based on the incident type
		IncidentDetails details = getIncidentDetails(incidentName);

		// Create the details panel
		JPanel detailsPanel = new JPanel(
				new MigLayout("fillx, insets 20, wrap 2", "[right][grow,fill]", "[]10[]10[]10[]"));

		// Title section
		JPanel titlePanel = new JPanel(new MigLayout("fillx, insets 0", "[center]", "[]5[]"));
		JLabel lblTitle = new JLabel("INCIDENT DETAILS");
		lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
		JLabel lblSubtitle = new JLabel("Student: " + studentName + " | Incident: " + incidentName);
		lblSubtitle.setFont(new Font("Arial", Font.ITALIC, 14));

		titlePanel.add(lblTitle, "wrap");
		titlePanel.add(lblSubtitle, "wrap");

		detailsPanel.add(titlePanel, "span 2, center, gapbottom 20");

		// Create and add all the form fields (same as before)
		JPanel formFields = createFormFields(studentName);
		detailsPanel.add(formFields, "span 2, grow");

		// Narrative Panel
		narrativeField = new JTextArea(details.narrative);
		detailsPanel.add(createTextAreaPanel("Narrative Report:", narrativeField), "span 2, grow");

		// Action & Recommendation Panel
		JPanel actionPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow,fill]10[grow,fill]", "[]"));
		actionTakenField = new JTextArea(details.actionTaken);
		recommendationField = new JTextArea(details.recommendation);
		actionPanel.add(createTextAreaPanel("Action Taken:", actionTakenField), "grow");
		actionPanel.add(createTextAreaPanel("Recommendation:", recommendationField), "grow");
		detailsPanel.add(actionPanel, "span 2, grow");

		// Back and Edit buttons
		JPanel buttonPanel = new JPanel(new MigLayout("insets 10", "[center, grow]", "[]"));
		JButton backButton = new JButton("Back to List");
		backButton.setPreferredSize(new Dimension(120, 30));
		backButton.addActionListener(e -> cardLayout.show(cardPanel, TABLE_VIEW));
		buttonPanel.add(backButton, "center");

		// Add Edit button
		JButton editButton = new JButton("Edit");
		editButton.setPreferredSize(new Dimension(120, 30));
		editButton.addActionListener(e -> editIncidentDetails());
		buttonPanel.add(editButton, "center");

		// Initialize Save button
		saveButton = new JButton("Save");
		saveButton.setPreferredSize(new Dimension(120, 30));
		saveButton.addActionListener(e -> saveIncidentDetails(studentName, incidentName));
		saveButton.setVisible(false); // Initially hidden
		buttonPanel.add(saveButton, "center");
		detailsPanel.add(buttonPanel, "span 2, grow");

		// Add the details panel to the card panel and show it
		cardPanel.add(detailsPanel, DETAILS_VIEW);
		cardLayout.show(cardPanel, DETAILS_VIEW);
	}

	private JPanel createFormFields(String studentName) {
		JPanel detailsPanel = new JPanel(
				new MigLayout("fillx, insets 10", "[right][grow,fill][right][grow,fill]", "[]5[]5[]5[]"));
		detailsPanel
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
						"INCIDENT DETAILS", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
						new Font("Arial", Font.BOLD, 12), new Color(100, 149, 177)));

		// Create text fields
		nameField = new JTextField(studentName);
		ageField = new JTextField("30");
		contactField = new JTextField("123-456-7890");
		sexField = new JTextField("Male");
		guardianField = new JTextField("John Doe");
		guardianContactField = new JTextField("987-654-3210");
		incidentTypeField = new JTextField(getIncidentTypeForStudent(studentName));

		// Add components with MigLayout constraints
		detailsPanel.add(new JLabel("Name:"), "right");
		detailsPanel.add(nameField, "growx");
		detailsPanel.add(new JLabel("Age:"), "right");
		detailsPanel.add(ageField, "growx, wrap");

		detailsPanel.add(new JLabel("Contact:"), "right");
		detailsPanel.add(contactField, "growx");
		detailsPanel.add(new JLabel("Sex:"), "right");
		detailsPanel.add(sexField, "growx, wrap");

		detailsPanel.add(new JLabel("Guardian:"), "right");
		detailsPanel.add(guardianField, "growx");
		detailsPanel.add(new JLabel("Guardian Contact:"), "right");
		detailsPanel.add(guardianContactField, "growx, wrap");

		detailsPanel.add(new JLabel("Type of Incident:"), "right");
		detailsPanel.add(incidentTypeField, "growx, span 3");

		// Make all fields read-only
		makeFieldsReadOnly(nameField, ageField, contactField, sexField, guardianField, guardianContactField,
				incidentTypeField);

		return detailsPanel;
	}

	// Update createTextAreaPanel to use MigLayout
	private JPanel createTextAreaPanel(String title, JTextArea textArea) {
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(0, 100)); // Set preferred height
		scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 177)),
				title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				new Font("Tahoma", Font.BOLD, 17), new Color(0, 0, 0)));

		JPanel panel = new JPanel(new net.miginfocom.swing.MigLayout("fill"));
		panel.add(scrollPane, "grow");
		return panel;
	}

	// Makes all fields non-editable
	private void makeFieldsReadOnly(Object... components) {
		for (Object component : components) {
			if (component instanceof JTextField) {
				((JTextField) component).setEditable(false);
			} else if (component instanceof JTextArea) {
				((JTextArea) component).setEditable(false);
			}
		}
	}

	// Add this new class to store incident-specific details
	private static class IncidentDetails {
		String narrative;
		String actionTaken;
		String recommendation;

		IncidentDetails(String narrative, String actionTaken, String recommendation) {
			this.narrative = narrative;
			this.actionTaken = actionTaken;
			this.recommendation = recommendation;
		}
	}

	// Add this method to get incident-specific details
	private IncidentDetails getIncidentDetails(String incidentName) {
		switch (incidentName) {
		case "Fighting in Class":
			return new IncidentDetails(
					"Student was involved in a physical altercation with another student during Math class. "
							+ "The incident occurred after a heated argument over a group project.",
					"1. Both students were immediately separated\n" + "2. Parents were called for a conference\n"
							+ "3. Both students received disciplinary action\n"
							+ "4. Conflict resolution session was conducted",
					"1. Implement peer mediation program\n" + "2. Regular counseling sessions\n"
							+ "3. Anger management workshop attendance\n" + "4. Parent-teacher conference follow-up");

		case "Cutting Classes":
			return new IncidentDetails(
					"Student has been consistently absent from afternoon classes for the past week. "
							+ "Was found spending time in the school cafeteria during class hours.",
					"1. Parent notification\n" + "2. Guidance counselor intervention\n"
							+ "3. Attendance contract created\n" + "4. Make-up assignments given",
					"1. Regular attendance monitoring\n" + "2. Weekly check-ins with guidance counselor\n"
							+ "3. Study habit workshop participation\n" + "4. Parent involvement in academic planning");

		case "Bullying":
			return new IncidentDetails(
					"Student was reported for repeatedly harassing and intimidating younger students. "
							+ "Incidents included verbal threats and social exclusion.",
					"1. Immediate intervention and investigation\n" + "2. Anti-bullying protocol activated\n"
							+ "3. Parents notified and meeting scheduled\n"
							+ "4. Temporary suspension from social activities",
					"1. Mandatory anti-bullying program participation\n" + "2. Regular counseling sessions\n"
							+ "3. Empathy development workshops\n" + "4. Supervised social interaction periods");

		case "Cheating on Exam":
			return new IncidentDetails(
					"Student was caught using unauthorized notes during the final examination. "
							+ "Hidden notes were discovered under the test paper.",
					"1. Exam paper confiscated\n" + "2. Parent notification and conference\n"
							+ "3. Zero grade given for the exam\n" + "4. Academic integrity violation recorded",
					"1. Academic integrity workshop attendance\n" + "2. Study skills development program\n"
							+ "3. Regular academic counseling\n" + "4. Retake opportunity with different questions");

		default:
			return new IncidentDetails("Incident details not available.", "No actions recorded.",
					"No recommendations available.");
		}
	}

	// Add this helper method to get the incident type for a student
	private String getIncidentTypeForStudent(String studentName) {
		// Search the table for the student's incident type
		for (int i = 0; i < table.getRowCount(); i++) {
			if (studentName.equals(table.getValueAt(i, 2))) { // Column 2 is Student Name
				return table.getValueAt(i, 3).toString(); // Column 3 is Incident Name
			}
		}
		return "Unknown Incident";
	}

	// New method to handle editing incident details
	private void editIncidentDetails() {
		// Make all fields editable
		nameField.setEditable(true);
		ageField.setEditable(true);
		contactField.setEditable(true);
		sexField.setEditable(true);
		guardianField.setEditable(true);
		guardianContactField.setEditable(true);
		incidentTypeField.setEditable(true);
		narrativeField.setEditable(true);
		actionTakenField.setEditable(true);
		recommendationField.setEditable(true);

		// Show the Save button
		saveButton.setVisible(true);
	}

	// New method to save incident details
	private void saveIncidentDetails(String studentName, String incidentName) {
		// Logic to save the edited details
		// You can implement the saving logic here, e.g., updating the database

		// Update the table model with the new values
		for (int i = 0; i < table.getRowCount(); i++) {
			if (studentName.equals(table.getValueAt(i, 2))) { // Column 2 is Student Name
				table.setValueAt(nameField.getText(), i, 2); // Update Student Name
				table.setValueAt(incidentTypeField.getText(), i, 3); // Update Incident Name
				// Add more fields as necessary
				break;
			}
		}

		// After saving, make all fields non-editable again
		makeFieldsReadOnly(nameField, ageField, contactField, sexField, guardianField, guardianContactField,
				incidentTypeField);
		narrativeField.setEditable(false);
		actionTakenField.setEditable(false);
		recommendationField.setEditable(false);

		// Hide the Save button after saving
		saveButton.setVisible(false);

		System.out.println("Saved incident details for " + studentName + " regarding " + incidentName);
	}

}


