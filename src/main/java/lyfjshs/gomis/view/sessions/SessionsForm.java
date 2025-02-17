package lyfjshs.gomis.view.sessions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.utils.PrintingReport;
import net.miginfocom.swing.MigLayout;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

public class SessionsForm extends Form {
	private JTextField sessionIdField, dateField, participantsField, violationField, recordedByField;
	private JFormattedTextField startSessionTimeField, endSessionTimeField;
	private JTextArea sessionSummaryArea, notesArea;
	private JButton saveButton;
	private Connection connnect;

	public SessionsForm(Connection conn) {
		this.connnect = conn;
		initializeComponents();
		layoutComponents();
	}

	public JasperReport loadIncidentFormTemplate() {
		String templatePath = "templates/Incident Report Template.jrxml";

		try (InputStream templateStream = getClass().getClassLoader().getResourceAsStream(templatePath)) {
			if (templateStream == null) {
				JOptionPane.showMessageDialog(null, "Incident Report template not found in resources/" + templatePath,
						"Template Error", JOptionPane.ERROR_MESSAGE);
				throw new IllegalArgumentException("Incident Report template not found in resources/" + templatePath);
			}

			return JasperCompileManager.compileReport(templateStream);
		} catch (JRException e) {
			JOptionPane.showMessageDialog(null, "Error compiling JasperReport template:\n" + e.getMessage(),
					"Compilation Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Failed to compile JasperReport template", e);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Unexpected error loading the report template:\n" + e.getMessage(),
					"Unexpected Error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException("Unexpected error while loading the template", e);
		}
	}

	private void initializeComponents() {
		sessionIdField = new JTextField("SESSION-1", 20);
		sessionIdField.setEditable(false);
		sessionIdField.setBackground(Color.LIGHT_GRAY);

		dateField = new JTextField(20);
		participantsField = new JTextField(20);
		notesArea = new JTextArea(5, 20);
		sessionSummaryArea = new JTextArea(8, 50);

		saveButton = new JButton("SAVE");
		saveButton.setBackground(new Color(70, 130, 180));
		saveButton.setForeground(Color.WHITE);
		saveButton.setFocusPainted(false);
		saveButton.addActionListener(e -> showSessionDetails());
	}

	private void layoutComponents() {
		this.setLayout(new MigLayout("wrap 4", "[][right]10[grow]20[right]10[grow]", "[][][][][][][][][fill][]"));
		this.setBorder(BorderFactory.createTitledBorder("Session Details"));

		this.add(new JLabel("SESSION ID:"), "cell 1 0");
		this.add(sessionIdField, "cell 2 0");
		this.add(new JLabel("DATE:"), "cell 3 0");
		this.add(dateField, "cell 4 0");

		this.add(new JLabel("PARTICIPANTS:"), "cell 1 1");
		this.add(participantsField, "cell 2 1");
		this.add(new JLabel("NOTES:"), "cell 3 1,aligny top");
		this.add(new JScrollPane(notesArea), "cell 4 1 1 2,grow");
		JLabel label_3 = new JLabel("START SESSION TIME:");
		this.add(label_3, "cell 1 3");
		startSessionTimeField = new JFormattedTextField();

		startSessionTimeField.setColumns(20);
		this.add(startSessionTimeField, "cell 2 3,alignx left");

		JLabel label_4 = new JLabel("VIOLATION:");
		this.add(label_4, "cell 3 3");
		violationField = new JTextField(20);
		this.add(violationField, "cell 4 3");

		JLabel label = new JLabel("END SESSION TIME:");
		this.add(label, "flowx,cell 1 4");
		endSessionTimeField = new JFormattedTextField();
		endSessionTimeField.setColumns(20);
		this.add(endSessionTimeField, "cell 2 4,alignx left");

		JLabel label_2 = new JLabel("RECORDED BY:");
		this.add(label_2, "cell 3 4");
		recordedByField = new JTextField(20);
		this.add(recordedByField, "cell 4 4");

		JLabel label_1 = new JLabel("SESSION SUMMARY:");
		this.add(label_1, "cell 0 6 2 1,alignx center");
		this.add(new JScrollPane(sessionSummaryArea), "cell 0 7 5 1,grow");

		this.add(saveButton, "cell 1 8 4 1,alignx center,growy");
	}

	private void showSessionDetails() {
		JFrame detailsFrame = new JFrame("Session Details");
		detailsFrame.setSize(600, 500);
		detailsFrame.setLocationRelativeTo(null);
		detailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(new Color(240, 240, 240));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Adding components
		mainPanel.add(createStyledLabel("SESSION ID:", sessionIdField.getText()));
		mainPanel.add(createStyledLabel("DATE:", dateField.getText()));
		mainPanel.add(createStyledLabel("PARTICIPANTS:", participantsField.getText()));
		mainPanel.add(createStyledLabel("VIOLATION:", violationField.getText()));
		mainPanel.add(createStyledLabel("END TIME:", endSessionTimeField.getText()));
		mainPanel.add(createStyledLabel("RECORDED BY:", recordedByField.getText()));

		mainPanel.add(createStyledTextArea("NOTES", notesArea.getText()));
		mainPanel.add(createStyledTextArea("SESSION SUMMARY", sessionSummaryArea.getText()));

		// Add Export to PDF button
		JButton exportButton = new JButton("Export to PDF");
		exportButton.setBackground(new Color(70, 130, 180));
		exportButton.setForeground(Color.WHITE);
		exportButton.setFocusPainted(false);
		exportButton.addActionListener(e -> exportToPDF(detailsFrame));
		mainPanel.add(exportButton);

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		detailsFrame.getContentPane().add(scrollPane);
		detailsFrame.setVisible(true);
	}

	private void exportToPDF(JFrame parent) {
		Map<String, Object> parameters = new HashMap<>();

		// Map the session form fields to the report parameters
		parameters.put("IncidentType", violationField.getText());
		parameters.put("IncidentDateTime", dateField.getText() + " " + startSessionTimeField.getText());
		parameters.put("InvolvedPersons", participantsField.getText());
		parameters.put("NarrativeDetails", sessionSummaryArea.getText());
		parameters.put("ActionsTaken", notesArea.getText());
		parameters.put("Recommendations", ""); // Add if you have recommendations field
		parameters.put("ReviewedBy", ""); // Add if you have a reviewer field
		parameters.put("DateReceived", dateField.getText());
		parameters.put("ReportedBy", recordedByField.getText());
		parameters.put("ReceivedBy", recordedByField.getText());

		// Call the PrintingReport utility to generate the PDF
		PrintingReport.generateReport(parent, loadIncidentFormTemplate(), parameters,
				"Session_" + sessionIdField.getText() + "_Report", "Save Session Report");
	}

	// Method for styled labels
	private JPanel createStyledLabel(String title, String value) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setBackground(Color.WHITE);
		panel.setOpaque(true);

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Consolas", Font.BOLD, 14));
		titleLabel.setForeground(new Color(70, 70, 70));

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(new Font("Consolas", Font.PLAIN, 14));
		valueLabel.setForeground(new Color(30, 30, 30));

		panel.add(titleLabel, BorderLayout.WEST);
		panel.add(valueLabel, BorderLayout.EAST);
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
				BorderFactory.createEmptyBorder(5, 10, 5, 10)));

		return panel;
	}

	// Method for styled text areas
	private JPanel createStyledTextArea(String title, String content) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		panel.setBackground(new Color(230, 230, 230));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(new Font("Consolas", Font.BOLD, 14));
		titleLabel.setForeground(new Color(50, 50, 50));

		JTextArea textArea = new JTextArea(content, 5, 40);
		textArea.setEditable(false);
		textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
		textArea.setBackground(Color.WHITE);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));

		panel.add(titleLabel, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}
}
