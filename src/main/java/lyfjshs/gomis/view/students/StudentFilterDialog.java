package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;
import raven.modal.component.SimpleModalBorder;

/**
 * A modern filter dialog for student data filtering
 */
public class StudentFilterDialog extends SimpleModalBorder {
    private static final long serialVersionUID = 1L;

    private static StudentMangementGUI parent;
    private static Map<String, String> activeFilters;

    // Filter components
    private static JTextField lrnFilterField;
    private static JTextField nameFilterField;
    private static JComboBox<String> gradeLevelComboBox;
    private static JComboBox<String> sectionComboBox;
    private static JComboBox<String> trackStrandComboBox;
    private static JCheckBox maleCheckbox;
    private static JCheckBox femaleCheckbox;
    private static JSpinner minAgeSpinner;
    private static JSpinner maxAgeSpinner;

    /**
     * Creates a new student filter dialog
     * 
     * @param connection    The database connection
     * @param parent        The parent GUI
     * @param activeFilters The map of active filters
     */
    public StudentFilterDialog(Connection connection, StudentMangementGUI parent, Map<String, String> activeFilters) {
        super(createContentPanel(connection, parent, activeFilters), "Filter Students", new Option[] {
                new Option("Apply", YES_OPTION), new Option("Reset", NO_OPTION), new Option("Close", CLOSE_OPTION) },
                (controller, action) -> {
                    if (action == YES_OPTION) {
                        // Apply filters
                        Map<String, String> filters = new HashMap<>();

                        if (!lrnFilterField.getText().trim().isEmpty()) {
                            filters.put("lrn", lrnFilterField.getText().trim());
                        }

                        if (!nameFilterField.getText().trim().isEmpty()) {
                            filters.put("name", nameFilterField.getText().trim());
                        }

                        String gradeLevel = (String) gradeLevelComboBox.getSelectedItem();
                        if (!gradeLevel.isEmpty()) {
                            filters.put("gradeLevel", gradeLevel);
                        }

                        String section = (String) sectionComboBox.getSelectedItem();
                        if (!section.isEmpty()) {
                            filters.put("section", section);
                        }

                        String trackStrand = (String) trackStrandComboBox.getSelectedItem();
                        if (!trackStrand.isEmpty()) {
                            filters.put("trackStrand", trackStrand);
                        }

                        // Handle sex filter
                        if (maleCheckbox.isSelected() && !femaleCheckbox.isSelected()) {
                            filters.put("sex", "Male");
                        } else if (!maleCheckbox.isSelected() && femaleCheckbox.isSelected()) {
                            filters.put("sex", "Female");
                        } else if (!maleCheckbox.isSelected() && !femaleCheckbox.isSelected()) {
                            filters.put("sex", "None");
                        }

                        // Handle age range
                        int minAge = (Integer) minAgeSpinner.getValue();
                        int maxAge = (Integer) maxAgeSpinner.getValue();
                        if (minAge > 12 || maxAge < 25) {
                            filters.put("ageRange", minAge + "-" + maxAge);
                        }

                        // Update the active filters in the parent
                        parent.setActiveFilters(filters);
                        controller.close();
                    } else if (action == NO_OPTION) {
                        // Reset filters
                        resetFilters();
                    } else if (action == CLOSE_OPTION) {
                        controller.close();
                    }
                });

        this.parent = parent;
        StudentFilterDialog.activeFilters = activeFilters;

        // Initialize components
        initializeComponents();
    }

    /**
     * Initializes the filter components
     */
    private void initializeComponents() {
        // LRN filter
        lrnFilterField = new JTextField(15);
        if (activeFilters.containsKey("lrn")) {
            lrnFilterField.setText(activeFilters.get("lrn"));
        }

        // Name filter
        nameFilterField = new JTextField(20);
        if (activeFilters.containsKey("name")) {
            nameFilterField.setText(activeFilters.get("name"));
        }

        // Grade Level filter
        gradeLevelComboBox = new JComboBox<>(new String[] { "", "Grade 11", "Grade 12" });
        if (activeFilters.containsKey("gradeLevel")) {
            String gradeLevel = activeFilters.get("gradeLevel");
            for (int i = 0; i < gradeLevelComboBox.getItemCount(); i++) {
                if (gradeLevelComboBox.getItemAt(i).equals(gradeLevel)) {
                    gradeLevelComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Section filter
        sectionComboBox = new JComboBox<>(new String[] { "", "KOTLIN", "JAVA", "PYTHON", "RUBY" });
        if (activeFilters.containsKey("section")) {
            String section = activeFilters.get("section");
            for (int i = 0; i < sectionComboBox.getItemCount(); i++) {
                if (sectionComboBox.getItemAt(i).equals(section)) {
                    sectionComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Track & Strand filter
        trackStrandComboBox = new JComboBox<>(new String[] { "", "STEM", "HUMSS", "ABM", "GAS", "TVL-ICT", "TVL-HE" });
        if (activeFilters.containsKey("trackStrand")) {
            String trackStrand = activeFilters.get("trackStrand");
            for (int i = 0; i < trackStrandComboBox.getItemCount(); i++) {
                if (trackStrandComboBox.getItemAt(i).equals(trackStrand)) {
                    trackStrandComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Sex filter
        maleCheckbox = new JCheckBox("Male");
        femaleCheckbox = new JCheckBox("Female");
        if (activeFilters.containsKey("sex")) {
            String sex = activeFilters.get("sex");
            if (sex.equals("Male")) {
                maleCheckbox.setSelected(true);
                femaleCheckbox.setSelected(false);
            } else if (sex.equals("Female")) {
                maleCheckbox.setSelected(false);
                femaleCheckbox.setSelected(true);
            } else if (sex.equals("None")) {
                maleCheckbox.setSelected(false);
                femaleCheckbox.setSelected(false);
            }
        } else {
            maleCheckbox.setSelected(true);
            femaleCheckbox.setSelected(true);
        }

        // Age Range filter
        minAgeSpinner = new JSpinner(new SpinnerNumberModel(12, 12, 25, 1));
        maxAgeSpinner = new JSpinner(new SpinnerNumberModel(19, 12, 25, 1));

        if (activeFilters.containsKey("ageRange")) {
            String ageRange = activeFilters.get("ageRange");
            String[] range = ageRange.split("-");
            minAgeSpinner.setValue(Integer.parseInt(range[0]));
            maxAgeSpinner.setValue(Integer.parseInt(range[1]));
        }
    }

    /**
     * Creates the content panel for the dialog
     */
    private static JPanel createContentPanel(Connection connection, StudentMangementGUI parent,
            Map<String, String> activeFilters) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Create the UI components
        JPanel bodyPanel = new JPanel(new MigLayout("insets 20", "[grow]", "[]"));
        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Applied filters section
        JPanel appliedFiltersSection = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
        appliedFiltersSection.setBorder(BorderFactory.createTitledBorder("Applied Filters"));

        if (activeFilters.isEmpty()) {
            JLabel noFiltersLabel = new JLabel("No filters applied");
            noFiltersLabel.setForeground(Color.GRAY);
            appliedFiltersSection.add(noFiltersLabel, "cell 0 0");
        } else {
            int row = 0;
            for (Map.Entry<String, String> entry : activeFilters.entrySet()) {
                JPanel filterPill = new JPanel(new MigLayout("insets 5", "[grow][]", "[]"));
                filterPill.setBackground(new Color(232, 245, 233)); // Light green
                filterPill.setBorder(
                        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 230, 201)),
                                BorderFactory.createEmptyBorder(2, 5, 2, 5)));

                JLabel filterLabel = new JLabel(entry.getValue());
                filterLabel.setForeground(new Color(46, 125, 50)); // Dark green
                filterPill.add(filterLabel, "cell 0 0");

                appliedFiltersSection.add(filterPill, "cell 0 " + row++ + ", growx, wrap");
            }
        }

        bodyPanel.add(appliedFiltersSection, "cell 0 0, growx, wrap");

        // Filter form
        JPanel filterForm = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
        filterForm.setBorder(BorderFactory.createTitledBorder("Filter Options"));

        // LRN filter
        JPanel lrnFilterGroup = createFilterGroup("LRN", "🔍");
        lrnFilterGroup.add(new JTextField(15), "cell 0 1, growx");
        filterForm.add(lrnFilterGroup, "cell 0 0, growx, wrap");

        // Name filter
        JPanel nameFilterGroup = createFilterGroup("Name", "👤");
        nameFilterGroup.add(new JTextField(20), "cell 0 1, growx");
        filterForm.add(nameFilterGroup, "cell 0 1, growx, wrap");

        // Grade Level filter
        JPanel gradeLevelFilterGroup = createFilterGroup("Grade Level", "🎓");
        JComboBox<String> gradeLevelComboBox = new JComboBox<>(new String[] { "", "Grade 11", "Grade 12" });
        gradeLevelFilterGroup.add(gradeLevelComboBox, "cell 0 1, growx");
        filterForm.add(gradeLevelFilterGroup, "cell 0 2, growx, wrap");

        // Section filter
        JPanel sectionFilterGroup = createFilterGroup("Section", "👥");
        JComboBox<String> sectionComboBox = new JComboBox<>(new String[] { "", "KOTLIN", "JAVA", "PYTHON", "RUBY" });
        sectionFilterGroup.add(sectionComboBox, "cell 0 1, growx");
        filterForm.add(sectionFilterGroup, "cell 0 3, growx, wrap");

        // Track & Strand filter
        JPanel trackStrandFilterGroup = createFilterGroup("Track & Strand", "🔍");
        JComboBox<String> trackStrandComboBox = new JComboBox<>(
                new String[] { "", "STEM", "HUMSS", "ABM", "GAS", "TVL-ICT", "TVL-HE" });
        trackStrandFilterGroup.add(trackStrandComboBox, "cell 0 1, growx");
        filterForm.add(trackStrandFilterGroup, "cell 0 4, growx, wrap");

        // Sex filter
        JPanel sexFilterGroup = createFilterGroup("Sex", "👤");
        JPanel sexCheckboxPanel = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
        JCheckBox maleCheckbox = new JCheckBox("Male");
        JCheckBox femaleCheckbox = new JCheckBox("Female");
        sexCheckboxPanel.add(maleCheckbox, "cell 0 0");
        sexCheckboxPanel.add(femaleCheckbox, "cell 1 0");
        sexFilterGroup.add(sexCheckboxPanel, "cell 0 1, growx");
        filterForm.add(sexFilterGroup, "cell 0 5, growx, wrap");

        // Age Range filter
        JPanel ageFilterGroup = createFilterGroup("Age Range", "📊");
        JPanel ageRangePanel = new JPanel(new MigLayout("insets 0", "[][grow][][grow]", "[]"));

        JLabel minAgeLabel = new JLabel("Min Age:");
        JSpinner minAgeSpinner = new JSpinner(new SpinnerNumberModel(12, 12, 25, 1));

        JLabel maxAgeLabel = new JLabel("Max Age:");
        JSpinner maxAgeSpinner = new JSpinner(new SpinnerNumberModel(19, 12, 25, 1));

        ageRangePanel.add(minAgeLabel, "cell 0 0");
        ageRangePanel.add(minAgeSpinner, "cell 1 0, growx");
        ageRangePanel.add(maxAgeLabel, "cell 2 0");
        ageRangePanel.add(maxAgeSpinner, "cell 3 0, growx");

        ageFilterGroup.add(ageRangePanel, "cell 0 1, growx");
        filterForm.add(ageFilterGroup, "cell 0 6, growx, wrap");

        bodyPanel.add(filterForm, "cell 0 1, growx, wrap");

        return contentPanel;
    }

    /**
     * Creates a filter group with label and icon
     */
    private static JPanel createFilterGroup(String label, String icon) {
        JPanel panel = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JLabel filterLabel = new JLabel(icon + " " + label);
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(filterLabel, "cell 0 0");

        return panel;
    }

    /**
     * Resets all filters
     */
    private static void resetFilters() {
        lrnFilterField.setText("");
        nameFilterField.setText("");
        gradeLevelComboBox.setSelectedIndex(0);
        sectionComboBox.setSelectedIndex(0);
        trackStrandComboBox.setSelectedIndex(0);
        maleCheckbox.setSelected(true);
        femaleCheckbox.setSelected(true);
        minAgeSpinner.setValue(12);
        maxAgeSpinner.setValue(19);

        activeFilters.clear();
        parent.setActiveFilters(activeFilters);
    }
}