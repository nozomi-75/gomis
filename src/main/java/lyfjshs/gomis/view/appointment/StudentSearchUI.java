package lyfjshs.gomis.view.appointment;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lyfjshs.gomis.components.FormManager.Form;

public class StudentSearchUI extends Form {
    private boolean isAdvancedVisible = false;
    private JPanel advancedPanel;
    private JButton advancedSearchBtn, searchBtnAdvanced;

    public void createAndShowGUI() {
        JDialog dialog = new JDialog((Frame) null, "Student Search", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(600, 300); // Smaller initial size
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // LRN Field
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("LRN:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
        JTextField lrnField = new JTextField(20);
        mainPanel.add(lrnField, gbc);

        gbc.gridx = 4; gbc.gridy = 0; gbc.gridwidth = 1;
        JButton searchBtn = new JButton("Search");
        mainPanel.add(searchBtn, gbc);

        // Advanced Search Button
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5;
        advancedSearchBtn = new JButton("Advanced Search");
        mainPanel.add(advancedSearchBtn, gbc);

        // Advanced Search Panel (Initially Hidden)
        advancedPanel = new JPanel(new GridBagLayout());
        advancedPanel.setVisible(false);

        // Put advancedPanel right below the button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 5;
        mainPanel.add(advancedPanel, gbc);

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
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        advancedPanel.add(genderBox, gbc);

        // Search Button inside Advanced Panel
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 4;
        searchBtnAdvanced = new JButton("Search");
        advancedPanel.add(searchBtnAdvanced, gbc);

        // Toggle Advanced Search Panel Visibility
        advancedSearchBtn.addActionListener(e -> {
            isAdvancedVisible = !isAdvancedVisible;
            advancedPanel.setVisible(isAdvancedVisible);
            dialog.pack(); // Adjust window size dynamically
        });

        // Add everything to dialog
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setVisible(true);
    }
}
