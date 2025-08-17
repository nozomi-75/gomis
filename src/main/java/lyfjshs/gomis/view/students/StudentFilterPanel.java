/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.miginfocom.swing.MigLayout;
import raven.modal.component.Modal;

public class StudentFilterPanel extends Modal {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(StudentFilterPanel.class);
    
    private JTextField nameFilterField;
    private JComboBox<String> gradeLevelComboBox;
    private JComboBox<String> sectionComboBox;
    private JComboBox<String> trackStrandComboBox;
    private JCheckBox maleCheckbox;
    private JCheckBox femaleCheckbox;
    private JSpinner minAgeSpinner;
    private JSpinner maxAgeSpinner;
    private JLabel filterCountLabel;
    private Connection connection;
    
    public StudentFilterPanel(Connection connection, Map<String, String> activeFilters) {
        this.connection = connection;
        initializeComponents();
        setupLayout();
        loadActiveFilters(activeFilters);
    }
    
    private void initializeComponents() {
        // Initialize text field
        nameFilterField = new JTextField(20);
        nameFilterField.putClientProperty("JTextField.placeholderText", "Enter student name...");
        
        // Initialize checkboxes
        maleCheckbox = new JCheckBox("Male");
        femaleCheckbox = new JCheckBox("Female");
        maleCheckbox.setSelected(true);
        femaleCheckbox.setSelected(true);
        
        // Initialize combo boxes
        gradeLevelComboBox = new JComboBox<>();
        sectionComboBox = new JComboBox<>();
        trackStrandComboBox = new JComboBox<>();
        
        // Style the combo boxes
        styleComboBox(gradeLevelComboBox);
        styleComboBox(sectionComboBox);
        styleComboBox(trackStrandComboBox);
        
        // Initialize age spinners with proper models
        SpinnerNumberModel minAgeModel = new SpinnerNumberModel(15, 14, 30, 1);
        SpinnerNumberModel maxAgeModel = new SpinnerNumberModel(25, 14, 30, 1);
        minAgeSpinner = new JSpinner(minAgeModel);
        maxAgeSpinner = new JSpinner(maxAgeModel);
        
        // Initialize filter count label
        filterCountLabel = new JLabel("0 filters applied");
        filterCountLabel.setHorizontalAlignment(JLabel.CENTER);
        filterCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterCountLabel.setForeground(Color.GRAY);
        
        // Initialize comboboxes with data from database
        try {
            populateComboBoxesFromDatabase();
        } catch (SQLException e) {
            logger.error("Error populating combo boxes from database", e);
        }
        
        // Add change listeners to spinners to maintain valid age range
        minAgeSpinner.addChangeListener(e -> {
            int minAge = (Integer) minAgeSpinner.getValue();
            int maxAge = (Integer) maxAgeSpinner.getValue();
            if (minAge > maxAge) {
                maxAgeSpinner.setValue(minAge);
            }
        });
        
        maxAgeSpinner.addChangeListener(e -> {
            int minAge = (Integer) minAgeSpinner.getValue();
            int maxAge = (Integer) maxAgeSpinner.getValue();
            if (maxAge < minAge) {
                minAgeSpinner.setValue(maxAge);
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Main content panel with scroll
        JPanel contentPanel = new JPanel(new MigLayout("insets 10, gap 10", "[grow]", "[]10[]10[]10[]10[]10[]10[][][]"));
        contentPanel.setBackground(Color.WHITE);
        
        // Basic Filters section
        JLabel basicFiltersLabel = new JLabel("Filters");
        basicFiltersLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(basicFiltersLabel, "cell 0 0");
        
        JLabel nameLabel = new JLabel("Student Name");
        contentPanel.add(nameLabel, "flowx,cell 0 1");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel panel = new JPanel();
        contentPanel.add(panel, "cell 0 2,grow");
        panel.setLayout(new MigLayout("", "[grow][][grow]", "[][]"));
        panel.setBackground(Color.WHITE);
        
        // Add labels
        JLabel label = new JLabel("Grade Level");
        panel.add(label, "cell 0 0");
        JLabel label_1 = new JLabel("Section");
        panel.add(label_1, "cell 2 0");
        
        panel.add(gradeLevelComboBox, "flowx,cell 0 1,growx");
        panel.add(sectionComboBox, "cell 2 1,growx");
        JLabel label_2 = new JLabel("Track & Strand");
        contentPanel.add(label_2, "flowx,cell 0 3");
        contentPanel.add(trackStrandComboBox, "cell 0 4,growx");
        
        // Set up scroll pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        // Initialize filter components
        nameFilterField = new JTextField(20);
        contentPanel.add(nameFilterField, "cell 0 1,growx");
        JLabel label_4 = new JLabel("Sex");
        contentPanel.add(label_4, "cell 0 5");
        
        // Sex filter
        JPanel sexPanel = new JPanel(new MigLayout("insets 0", "[][][]", "[]"));
        contentPanel.add(sexPanel, "cell 0 6");
        sexPanel.setBackground(Color.WHITE);
        
        maleCheckbox.setBackground(Color.WHITE);
        sexPanel.add(maleCheckbox, "cell 0 0");
        femaleCheckbox.setBackground(Color.WHITE);
        sexPanel.add(femaleCheckbox, "cell 1 0");
        JLabel label_1_1 = new JLabel("Age Range");
        contentPanel.add(label_1_1, "cell 0 7");
        
        // Age filter
        JPanel agePanel = new JPanel(new MigLayout("insets 0", "[]5[]5[]5[]5[]", "[]"));
        contentPanel.add(agePanel, "cell 0 8");
        agePanel.setBackground(Color.WHITE);
        JLabel label_2_1 = new JLabel("From:");
        agePanel.add(label_2_1, "cell 0 0,alignx right");
        agePanel.add(minAgeSpinner, "cell 1 0,width 60!");
        JLabel label_3 = new JLabel("To:");
        agePanel.add(label_3, "cell 3 0");
        agePanel.add(maxAgeSpinner, "cell 4 0,width 60!");
        
        nameFilterField.putClientProperty("JTextField.placeholderText", "Enter student name...");
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setLayout(new MigLayout("", "[525px,grow,center]", "[327px,grow][]"));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Add components to the main layout
        add(scrollPane, "cell 0 0,grow");
        
        // Filter count label
        add(filterCountLabel, "cell 0 1,grow");
    }
     
    private void populateComboBoxesFromDatabase() throws SQLException {
        // Clear existing items
        gradeLevelComboBox.removeAllItems();
        sectionComboBox.removeAllItems();
        trackStrandComboBox.removeAllItems();
        
        // Add empty options first with descriptive text
        gradeLevelComboBox.addItem("Select Grade Level");
        sectionComboBox.addItem("Select Section");
        trackStrandComboBox.addItem("Select Track & Strand");
        
        // Get grade levels
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT DISTINCT SF_GRADE_LEVEL FROM SCHOOL_FORM WHERE SF_GRADE_LEVEL IS NOT NULL AND TRIM(SF_GRADE_LEVEL) != '' ORDER BY LENGTH(SF_GRADE_LEVEL), SF_GRADE_LEVEL")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String gradeLevel = rs.getString("SF_GRADE_LEVEL");
                if (gradeLevel != null && !gradeLevel.trim().isEmpty()) {
                    // Add the grade level exactly as it appears in the database
                    gradeLevelComboBox.addItem(gradeLevel);
                }
            }
        }
        
        // Get sections
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT DISTINCT SF_SECTION FROM SCHOOL_FORM WHERE SF_SECTION IS NOT NULL AND TRIM(SF_SECTION) != '' ORDER BY SF_SECTION")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String section = rs.getString("SF_SECTION");
                if (section != null && !section.trim().isEmpty()) {
                    sectionComboBox.addItem(section);
                }
            }
        }
        
        // Get tracks and strands
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT DISTINCT SF_TRACK_AND_STRAND FROM SCHOOL_FORM WHERE SF_TRACK_AND_STRAND IS NOT NULL AND TRIM(SF_TRACK_AND_STRAND) != '' ORDER BY SF_TRACK_AND_STRAND")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String track = rs.getString("SF_TRACK_AND_STRAND");
                if (track != null && !track.trim().isEmpty()) {
                    trackStrandComboBox.addItem(track);
                }
            }
        }
    }
    
    public void loadActiveFilters(Map<String, String> activeFilters) {
        if (activeFilters == null) {
            resetFilters();
            return;
        }
        
        if (activeFilters.containsKey("name")) {
            nameFilterField.setText(activeFilters.get("name"));
        }
        
        if (activeFilters.containsKey("gradeLevel")) {
            setComboBoxValue(gradeLevelComboBox, activeFilters.get("gradeLevel"));
        }
        
        if (activeFilters.containsKey("section")) {
            setComboBoxValue(sectionComboBox, activeFilters.get("section"));
        }
        
        if (activeFilters.containsKey("trackStrand")) {
            setComboBoxValue(trackStrandComboBox, activeFilters.get("trackStrand"));
        }
        
        if (activeFilters.containsKey("sex")) {
            String sex = activeFilters.get("sex");
            maleCheckbox.setSelected(sex.contains("Male"));
            femaleCheckbox.setSelected(sex.contains("Female"));
        }
        
        if (activeFilters.containsKey("minAge")) {
            try {
                minAgeSpinner.setValue(Integer.parseInt(activeFilters.get("minAge")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid minAge value in filters: " + activeFilters.get("minAge") + ". Using default value.");
                minAgeSpinner.setValue(15); // Default to minimum age
            }
        }
        
        if (activeFilters.containsKey("maxAge")) {
            try {
                maxAgeSpinner.setValue(Integer.parseInt(activeFilters.get("maxAge")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid maxAge value in filters: " + activeFilters.get("maxAge") + ". Using default value.");
                maxAgeSpinner.setValue(25); // Default to maximum age
            }
        }
        
        updateFilterCount(activeFilters.size());
    }
    
    private void setComboBoxValue(JComboBox<String> comboBox, String value) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equals(value)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    public void updateFilterCount(int count) {
        filterCountLabel.setText(count + " filters applied");
        filterCountLabel.setForeground(count > 0 ? new Color(46, 125, 50) : Color.GRAY);
    }
    
    // Getters for the filter components
    public JTextField getNameFilterField() { return nameFilterField; }
    public JComboBox<String> getGradeLevelComboBox() { return gradeLevelComboBox; }
    public JComboBox<String> getSectionComboBox() { return sectionComboBox; }
    public JComboBox<String> getTrackStrandComboBox() { return trackStrandComboBox; }
    public JCheckBox getMaleCheckbox() { return maleCheckbox; }
    public JCheckBox getFemaleCheckbox() { return femaleCheckbox; }
    public JSpinner getMinAgeSpinner() { return minAgeSpinner; }
    public JSpinner getMaxAgeSpinner() { return maxAgeSpinner; }

    public Map<String, String> getFilters() {
        Map<String, String> filters = new HashMap<>();
        
        // Get name filter
        String name = nameFilterField.getText().trim();
        if (!name.isEmpty() && name.length() >= 2) {
            filters.put("name", name);
        }
        
        // Get grade level filter
        String gradeLevel = (String) gradeLevelComboBox.getSelectedItem();
        if (gradeLevel != null && !gradeLevel.equals("Select Grade Level")) {
            filters.put("gradeLevel", gradeLevel);
        }
        
        // Get section filter
        String section = (String) sectionComboBox.getSelectedItem();
        if (section != null && !section.equals("Select Section")) {
            filters.put("section", section);
        }
        
        // Get track/strand filter
        String trackStrand = (String) trackStrandComboBox.getSelectedItem();
        if (trackStrand != null && !trackStrand.equals("Select Track & Strand")) {
            filters.put("trackStrand", trackStrand);
        }
        
        // Get sex filter
        List<String> sexValues = new ArrayList<>();
        if (maleCheckbox.isSelected()) sexValues.add("Male");
        if (femaleCheckbox.isSelected()) sexValues.add("Female");
        if (!sexValues.isEmpty()) {
            filters.put("sex", String.join(",", sexValues));
        }
        
        // Get age range
        int minAge = (Integer) minAgeSpinner.getValue();
        int maxAge = (Integer) maxAgeSpinner.getValue();
        
        if (minAge > 14) {
            filters.put("minAge", String.valueOf(minAge));
        }
        if (maxAge < 30) {
            filters.put("maxAge", String.valueOf(maxAge));
        }
        
        return filters;
    }

    public void resetFilters() {
        nameFilterField.setText("");
        gradeLevelComboBox.setSelectedItem("Select Grade Level");
        sectionComboBox.setSelectedItem("Select Section");
        trackStrandComboBox.setSelectedItem("Select Track & Strand");
        maleCheckbox.setSelected(true);
        femaleCheckbox.setSelected(true);
        minAgeSpinner.setValue(15);
        maxAgeSpinner.setValue(25);
        updateFilterCount(0);
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }

    public void applyFilters() {
        try {
            // Implementation of applyFilters method
        } catch (Exception e) {
            logger.error("Error applying filters", e);
            JOptionPane.showMessageDialog(this, "Error applying filters: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void clearFilters() {
        try {
            resetFilters();
        } catch (Exception e) {
            logger.error("Error clearing filters", e);
            JOptionPane.showMessageDialog(this, "Error clearing filters: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 