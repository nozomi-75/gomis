package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lyfjshs.gomis.components.FormManager.Form;

import javax.swing.JOptionPane;

public class StudentSearchPanel extends Form {
    private final Connection connection;

    public StudentSearchPanel(Connection conn) {
        this.connection = conn;
      
        // Main Panel with a softer background and padding
        setLayout(null);
        setBackground(new Color(245, 245, 245)); // Soft gray background
        setPreferredSize(new Dimension(602, 701)); // Smaller panel size

        // Title Panel for Top Heading
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());
        titlePanel.setBackground(new Color(70, 130, 180)); // Deep blue background
        titlePanel.setBounds(0, 0, 600, 80); // Title panel size
        JLabel titleLabel = new JLabel("Student Search Panel", JLabel.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE); // White text for contrast
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel);

        // LRN Section
        JLabel lrnLabel = new JLabel("LRN:");
        lrnLabel.setBounds(50, 100, 80, 30);
        lrnLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField lrnField = new JTextField();
        lrnField.setBounds(150, 100, 300, 30);
        lrnField.setFont(new Font("Arial", Font.PLAIN, 16));
        lrnField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(lrnLabel);
        add(lrnField);

        // Search Button (Next to LRN field)
        JButton searchButton1 = new JButton("Search");
        searchButton1.setBounds(480, 100, 100, 30);
        searchButton1.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton1.setBackground(new Color(70, 130, 180));
        searchButton1.setForeground(Color.WHITE);
        searchButton1.setFocusPainted(false);
        searchButton1.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
        add(searchButton1);

        // Add action listener to the search button
        searchButton1.addActionListener(e -> searchStudent(lrnField.getText()));

        // Advanced Search Label
        JLabel advanceSLabel = new JLabel("Advanced Search");
        advanceSLabel.setBounds(50, 150, 200, 20);
        advanceSLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        advanceSLabel.setForeground(new Color(70, 130, 180));
        add(advanceSLabel);

        // First Name
        JLabel firstNameLabel = new JLabel("First Name:");
        firstNameLabel.setBounds(50, 190, 100, 30);
        firstNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField firstNameField = new JTextField();
        firstNameField.setBounds(150, 190, 300, 30);
        firstNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        firstNameField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(firstNameLabel);
        add(firstNameField);

        // Gender ComboBox (Placed beside the First Name field)
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setBounds(480, 190, 80, 30);
        genderLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        String[] genderOptions = {"Male", "Female"};
        JComboBox<String> genderComboBox = new JComboBox<>(genderOptions);
        genderComboBox.setBounds(480, 220, 100, 30);
        genderComboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        add(genderLabel);
        add(genderComboBox);

        // Middle Name
        JLabel middleNameLabel = new JLabel("Middle Name:");
        middleNameLabel.setBounds(50, 240, 120, 30);
        middleNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField middleNameField = new JTextField();
        middleNameField.setBounds(150, 240, 300, 30);
        middleNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        middleNameField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(middleNameLabel);
        add(middleNameField);

        // Last Name
        JLabel lastNameLabel = new JLabel("Last Name:");
        lastNameLabel.setBounds(50, 290, 100, 30);
        lastNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField lastNameField = new JTextField();
        lastNameField.setBounds(150, 290, 300, 30);
        lastNameField.setFont(new Font("Arial", Font.PLAIN, 16));
        lastNameField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(lastNameLabel);
        add(lastNameField);

        // Email (Updated Alignment)
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 340, 100, 30);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField emailField = new JTextField();
        emailField.setBounds(150, 340, 300, 30);
        emailField.setFont(new Font("Arial", Font.PLAIN, 16));
        emailField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(emailLabel);
        add(emailField);

        // Date of Birth (Updated Alignment)
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setBounds(50, 390, 100, 30);
        dobLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField dobField = new JTextField();
        dobField.setBounds(150, 390, 300, 30);
        dobField.setFont(new Font("Arial", Font.PLAIN, 16));
        dobField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(dobLabel);
        add(dobField);

        // Address (Updated Alignment)
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setBounds(50, 440, 100, 30);
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JTextField addressField = new JTextField();
        addressField.setBounds(150, 440, 300, 30);
        addressField.setFont(new Font("Arial", Font.PLAIN, 16));
        addressField.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));
        add(addressLabel);
        add(addressField);

        // Advanced Search Button
        JButton searchButton2 = new JButton("Search");
        searchButton2.setBounds(250, 520, 100, 30);
        searchButton2.setFont(new Font("Arial", Font.BOLD, 16));
        searchButton2.setBackground(new Color(70, 130, 180));
        searchButton2.setForeground(Color.WHITE);
        searchButton2.setFocusPainted(false);
        searchButton2.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(searchButton2);

        // Advanced Search Options (Checkboxes)
        // Date of Birth Checkbox
        JCheckBox dobCheck = new JCheckBox("Date of Birth");
        dobCheck.setBounds(150, 480, 150, 30); // Positioned below address, slightly adjusted
        dobCheck.setFont(new Font("Arial", Font.PLAIN, 16));
        dobCheck.setBackground(new Color(245, 245, 245));
        add(dobCheck);

        // Guardian Name Checkbox
        JCheckBox guardianCheck = new JCheckBox("Guardian Name");
        guardianCheck.setBounds(310, 480, 150, 30); // Positioned beside Date of Birth checkbox
        guardianCheck.setFont(new Font("Arial", Font.PLAIN, 16));
        guardianCheck.setBackground(new Color(245, 245, 245));
        add(guardianCheck);
    }

    private void searchStudent(String lrn) {
        // Implement your search logic here
        // Use the connection to query the database
        // Show results using JOptionPane
    }
}
