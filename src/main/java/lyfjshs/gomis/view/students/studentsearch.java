package lyfjshs.gomis.view.students;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lyfjshs.gomis.components.FormManager.Form;

public class studentsearch extends Form {
    private boolean isAdvancedVisible = false;
    private JPanel advancedPanel;
    private JTextField searchField;
    private JButton advancedSearchButton, searchBtnAdvanced, searchButton;

    public studentsearch () {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Panel for search field and icon
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // LRN Label
        JLabel lrnLabel = new JLabel("LRN:");
        searchPanel.add(lrnLabel);

        searchField = new JTextField(20);
        searchField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        searchPanel.add(searchField);

        // Add search icon
        ImageIcon searchIcon = new ImageIcon("path/to/search_icon.png"); // Update with the correct path
        JLabel searchLabel = new JLabel(searchIcon);
        searchPanel.add(searchLabel);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 5;
        add(searchPanel, gbc);

        // Search Button
        searchButton = new JButton("Search");
        searchButton.setBackground(Color.decode("#007BFF")); // Blue button
        searchButton.setForeground(Color.WHITE);
        gbc.gridx = 5; gbc.gridy = 0; gbc.gridwidth = 1;
        add(searchButton, gbc);

        // Advanced Search Button
        advancedSearchButton = new JButton("Advanced Search");
        advancedSearchButton.setBackground(Color.decode("#007BFF")); // Blue button
        advancedSearchButton.setForeground(Color.WHITE);
        gbc.gridx = 6; gbc.gridy = 0; gbc.gridwidth = 1;
        add(advancedSearchButton, gbc);

        // Advanced Search Panel (Initially Hidden)
        advancedPanel = new JPanel(new GridBagLayout());
        advancedPanel.setVisible(false);
        advancedPanel.setBackground(Color.WHITE);
        advancedPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        // Put advancedPanel right below the button
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 7;
        add(advancedPanel, gbc);

        String[] labels = { "First Name:", "Middle Name:", "Last Name:", "Email:", "Date of Birth:", "Address:" };
        JTextField[] textFields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.gridwidth = 1;
            advancedPanel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1; gbc.gridy = i; gbc.gridwidth = 3;
            textFields[i] = new JTextField(20);
            advancedPanel.add(textFields[i], gbc);
        }

        // Gender Dropdown
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 1;
        advancedPanel.add(new JLabel("Gender:"), gbc);

        gbc.gridx = 1; gbc.gridy = labels.length; gbc.gridwidth = 3;
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        advancedPanel.add(genderBox, gbc);

        // Search Button inside Advanced Panel
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 4;
        searchBtnAdvanced = new JButton("Search");
        searchBtnAdvanced.setBackground(Color.decode("#007BFF")); // Blue button
        searchBtnAdvanced.setForeground(Color.WHITE);
        advancedPanel.add(searchBtnAdvanced, gbc);

        // Toggle Advanced Search Panel Visibility
        advancedSearchButton.addActionListener(e -> {
            isAdvancedVisible = !isAdvancedVisible;
            advancedPanel.setVisible(isAdvancedVisible);
            revalidate(); // Refresh the layout
            repaint(); // Repaint the panel
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Student Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 200);
        frame.setLocationRelativeTo(null);
        frame.add(new studentsearch());
        frame.setVisible(true);
    }
}
