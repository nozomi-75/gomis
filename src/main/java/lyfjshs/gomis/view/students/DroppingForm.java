package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class DroppingForm extends JPanel {
	private JFormattedTextField dateField, effectiveDateField;
	private JTextField lrnField, nameField, adviserField, absencesField, gradeSecField;
	private JTextArea reasonTextArea;
	private JComboBox<String> trackComboBox;
	private JButton submitButton;
	private DatePicker dateChooser, effectiveDateChooser;

	private final String[] trackOptions = { "Accountancy, Business, and Management (ABM)",
			"Humanities and Social Sciences (HUMSS)", "Science, Technology, Engineering, and Mathematics (STEM)",
			"Technical-Vocational-Livelihood (TVL) Home Economics",
			"Technical-Vocational-Livelihood (TVL) Industrial Arts",
			"Technical-Vocational-Livelihood (TVL) ICT - Programming",
			"Technical-Vocational-Livelihood (TVL) ICT - Technical Drafting" };
	
	private Connection connection;
	private JLabel lblNewLabel;
	private JScrollPane scrollPane_1;
	private JTextArea textArea;
	
	
	public DroppingForm(Connection conn) {
		this.connection = conn;

		setSize(1032, 546);

		initializeComponents();
		setupLayout();
		addActionListeners();
	}

	private void initializeComponents() {
		dateField = new JFormattedTextField(new SimpleDateFormat("MM/dd/yyyy"));
		effectiveDateField = new JFormattedTextField();
		lrnField = new JTextField(20);
		nameField = new JTextField(20);
		adviserField = new JTextField(20);
		absencesField = new JTextField(20);
		gradeSecField = new JTextField(20);
		reasonTextArea = new JTextArea(5, 40);
		reasonTextArea.setLineWrap(true);
		reasonTextArea.setWrapStyleWord(true);
		trackComboBox = new JComboBox<>(trackOptions);
		dateChooser = new DatePicker();
		dateChooser.setEditor(dateField);
		effectiveDateChooser = new DatePicker();
		effectiveDateChooser.setEditor(effectiveDateField);
	}

	private void setupLayout() {
		JPanel mainPanel = new JPanel(
				new MigLayout("wrap 4, insets 20", "[150]10[250]10[]10[150]10[250]", "[][][][][][][187.00,grow][]"));
		mainPanel.setBackground(Color.WHITE);

		JLabel headerLabel = new JLabel("Drop-Out Fill-Up Form", JLabel.CENTER);
		headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
		headerLabel.setForeground(Color.WHITE);
		headerLabel.setOpaque(true);
		headerLabel.setBackground(new Color(0, 83, 156));
		headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
		mainPanel.add(headerLabel, "cell 0 0 5 1,growx");

		mainPanel.add(new JLabel("Date:"), "cell 0 1");
		mainPanel.add(dateField, "cell 1 1, growx");
		mainPanel.add(new JLabel("LRN:"), "cell 3 1");
		mainPanel.add(lrnField, "cell 4 1,growx");

		mainPanel.add(new JLabel("Name of Student:"), "cell 0 2");
		mainPanel.add(nameField, "cell 1 2, growx");
		mainPanel.add(new JLabel("Track/Strand-Specialization:"), "cell 3 2");
		mainPanel.add(trackComboBox, "cell 4 2,growx");

		mainPanel.add(new JLabel("Adviser:"), "cell 0 3");
		mainPanel.add(adviserField, "cell 1 3, growx");
		mainPanel.add(new JLabel("Grade & Section:"), "cell 3 3");
		mainPanel.add(gradeSecField, "cell 4 3,growx");

		mainPanel.add(new JLabel("Inclusive date of absences:"), "cell 0 4");
		mainPanel.add(absencesField, "cell 1 4, growx");
				
				lblNewLabel = new JLabel("Action Taken");
				mainPanel.add(lblNewLabel, "cell 0 5");
		
				JLabel label = new JLabel("Reason for dropping:");
				mainPanel.add(label, "cell 3 5");
		
		scrollPane_1 = new JScrollPane();
		mainPanel.add(scrollPane_1, "cell 0 6 2 1,grow");
		
		textArea = new JTextArea();
		scrollPane_1.setViewportView(textArea);
		JScrollPane scrollPane = new JScrollPane(reasonTextArea);
		mainPanel.add(scrollPane, "cell 3 6 2 1,grow");

		mainPanel.add(new JLabel("Effective Date:"), "cell 0 7");
		mainPanel.add(effectiveDateField, "cell 1 7,grow");

		this.add(mainPanel, BorderLayout.CENTER);
		submitButton = new JButton("SUBMIT");
		submitButton.setFont(new Font("Arial", Font.BOLD, 14));
		submitButton.setBackground(new Color(0, 120, 215));
		submitButton.setForeground(Color.WHITE);
		submitButton.setFocusPainted(false);
		submitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
		mainPanel.add(submitButton, "cell 4 7,alignx right");
	}

	private void addActionListeners() {
		submitButton.addActionListener(e -> {
			int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to drop this student?",
					"Confirmation", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				JOptionPane.showMessageDialog(this, "Form submitted successfully.");
				resetForm();
			}
		});
	}

	private void resetForm() {
		dateField.setText("");
		lrnField.setText("");
		nameField.setText("");
		adviserField.setText("");
		absencesField.setText("");
		gradeSecField.setText("");
		effectiveDateField.setText("");
		reasonTextArea.setText("");
		trackComboBox.setSelectedIndex(0);
	}

	public void populateForm(String studentName, String trackAndStrand, String counselorName) {
		nameField.setText(studentName);
		trackComboBox.setSelectedItem(trackAndStrand);
		adviserField.setText(counselorName);
	}

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            FlatLaf.updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dropping Form Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 550);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().add(new DroppingForm(null));
            frame.setVisible(true);
        });
    }
}
