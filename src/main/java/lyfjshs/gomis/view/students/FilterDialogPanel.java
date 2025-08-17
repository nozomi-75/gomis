/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import net.miginfocom.swing.MigLayout;

public class FilterDialogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private final FilterCriteria currentFilterCriteria; // The criteria object to modify
	private JTextField firstNameField, lastNameField, middleNameField;
	private JCheckBox middleInitialOnlyCheckBox;
	private JComboBox<String> gradeLevelComboBox, sectionComboBox, trackStrandComboBox;
	private JCheckBox maleCheckBox, femaleCheckBox;
	private JSpinner minAgeSpinner, maxAgeSpinner;
	private int initialDbMinAge, initialDbMaxAge; // Store initial DB min/max ages for reset
	private boolean filtersApplied = false;
	StudentsDataDAO dbManager;
	private JLabel appliedFilters;
	private static final Logger logger = LogManager.getLogger(FilterDialogPanel.class);

	/**
	 * Create the dialog.
	 */
	public FilterDialogPanel(FilterCriteria criteriaToModify, StudentsDataDAO dbManager) {
		this.currentFilterCriteria = criteriaToModify;
		this.dbManager = dbManager;

		// Fetch initial min/max ages from DB for spinner defaults and reset
		// functionality
		int[] dbAges;
		try {
			dbAges = dbManager.getMinMaxAge();
			initialDbMinAge = dbAges[0];
			initialDbMaxAge = dbAges[1];
		} catch (SQLException e) {
			logger.error("Error fetching initial ages", e);
			JOptionPane.showMessageDialog(this, "Error fetching initial ages: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}


		this.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[grow]", "[grow][grow][grow][]"));
		{
			JPanel namePanel = new JPanel(
					new MigLayout("insets 0, fillx, wrap 2", "[right]para[grow,fill]", "[][][][]"));
			namePanel.setBorder(new TitledBorder("Filter by Name"));
			namePanel.add(new JLabel("First Name:"), "cell 0 0");
			firstNameField = new JTextField();
			namePanel.add(firstNameField, "cell 1 0,growx");
			namePanel.add(new JLabel("Last Name:"), "cell 0 1");
			lastNameField = new JTextField();
			namePanel.add(lastNameField, "cell 1 1,growx");
			namePanel.add(new JLabel("Middle Name:"), "cell 0 2");
			middleNameField = new JTextField();
			namePanel.add(middleNameField, "cell 1 2,growx");
			middleInitialOnlyCheckBox = new JCheckBox("Initial Only");
			namePanel.add(middleInitialOnlyCheckBox, "cell 1 2,gapx 5");
			contentPanel.add(namePanel, "cell 0 0,grow");
		}
		{

			try {
				// Academic Filtering Section
				JPanel academicPanel = new JPanel(
						new MigLayout("insets 0, fillx, wrap 2", "[right]para[grow,fill]", "[][][]"));
				academicPanel.setBorder(new TitledBorder("Filter by Academics"));
				DefaultComboBoxModel<String> gradeModel = new DefaultComboBoxModel<>();
				gradeModel.addElement("All");

				dbManager.getDistinctGradeLevels().forEach(gradeModel::addElement);

				academicPanel.add(new JLabel("Grade Level:"), "cell 0 0");

				DefaultComboBoxModel<String> sectionModel = new DefaultComboBoxModel<>();
				sectionModel.addElement("All");
				dbManager.getDistinctSections().forEach(sectionModel::addElement);
				gradeLevelComboBox = new JComboBox<>(gradeModel);
				academicPanel.add(gradeLevelComboBox, "cell 1 0,growx");
				JLabel label = new JLabel("Section:");
				academicPanel.add(label, "cell 0 1");
				sectionComboBox = new JComboBox<>(sectionModel);
				academicPanel.add(sectionComboBox, "cell 1 1,growx");

				DefaultComboBoxModel<String> trackStrandModel = new DefaultComboBoxModel<>();
				trackStrandModel.addElement("All");
				dbManager.getDistinctTrackStrands().forEach(trackStrandModel::addElement);
				trackStrandComboBox = new JComboBox<>(trackStrandModel);
				academicPanel.add(new JLabel("Track & Strand:"), "cell 0 2");
				academicPanel.add(trackStrandComboBox, "cell 1 2,growx");
				contentPanel.add(academicPanel, "cell 0 1,grow");
			} catch (SQLException e) {
				logger.error("Error fetching academic filter data", e);
				JOptionPane.showMessageDialog(this, "Error fetching academic filter data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		{
			// Demographics Filtering Section
			JPanel demoPanel = new JPanel(
					new MigLayout("insets 0, fillx, wrap 2", "[right]para[]para[]", "[][][][][][]"));
			demoPanel.setBorder(new TitledBorder("Filter by Demographics"));
			contentPanel.add(demoPanel, "cell 0 2,grow");

			// Age Spinners
			// Use initialDbMinAge and initialDbMaxAge for spinner bounds
			SpinnerNumberModel minModel = new SpinnerNumberModel(initialDbMinAge, initialDbMinAge,
					Math.max(initialDbMinAge, initialDbMaxAge + 20), 1);
			SpinnerNumberModel maxModel = new SpinnerNumberModel(Math.min(initialDbMaxAge + 5, initialDbMaxAge + 20),
					initialDbMinAge, Math.max(initialDbMinAge, initialDbMaxAge + 20), 1);
			JLabel label = new JLabel("Sex:");
			demoPanel.add(label, "cell 0 1");
			femaleCheckBox = new JCheckBox("Female");
			demoPanel.add(femaleCheckBox, "cell 1 1");
			maleCheckBox = new JCheckBox("Male");
			demoPanel.add(maleCheckBox, "cell 2 1");
			demoPanel.add(new JLabel("Age Range:"), "cell 0 3");
			JLabel label_1 = new JLabel("Min:");
			demoPanel.add(label_1, "flowx,cell 1 3");
			JLabel label_2 = new JLabel("Max:");
			demoPanel.add(label_2, "flowx,cell 2 3");
			minAgeSpinner = new JSpinner(minModel);
			demoPanel.add(minAgeSpinner, "cell 1 3");
			maxAgeSpinner = new JSpinner(maxModel);
			demoPanel.add(maxAgeSpinner, "cell 2 3");
		}
		
					appliedFilters = new JLabel("New label");
					contentPanel.add(appliedFilters, "cell 0 3,alignx center,growy");
		loadCriteria(currentFilterCriteria); // Load existing criteria into dialog fields

	}

	/**
	 * Loads the current filter criteria into the dialog's input fields.
	 * 
	 * @param criteria The FilterCriteria object to load from.
	 */
	private void loadCriteria(FilterCriteria criteria) {
		firstNameField.setText(criteria.filterFirstName);
		lastNameField.setText(criteria.filterLastName);
		middleNameField.setText(criteria.filterMiddleName);
		middleInitialOnlyCheckBox.setSelected(criteria.middleInitialOnly);

		gradeLevelComboBox.setSelectedItem(criteria.filterGradeLevel);
		sectionComboBox.setSelectedItem(criteria.filterSection);

		// Handle trackStrandComboBox item existence
		if (criteria.filterTrackStrand == null || ((DefaultComboBoxModel<String>) trackStrandComboBox.getModel())
				.getIndexOf(criteria.filterTrackStrand) == -1) {
			trackStrandComboBox.setSelectedItem("All");
		} else {
			trackStrandComboBox.setSelectedItem(criteria.filterTrackStrand);
		}

		maleCheckBox.setSelected(criteria.filterMale);
		femaleCheckBox.setSelected(criteria.filterFemale);

		// Safely set spinner values within their model's bounds
		SpinnerNumberModel minModel = (SpinnerNumberModel) minAgeSpinner.getModel();
		minAgeSpinner.setValue(
				Math.max((Integer) minModel.getMinimum(), Math.min(criteria.minAge, (Integer) minModel.getMaximum())));

		SpinnerNumberModel maxModel = (SpinnerNumberModel) maxAgeSpinner.getModel();
		maxAgeSpinner.setValue(
				Math.min((Integer) maxModel.getMaximum(), Math.max(criteria.maxAge, (Integer) maxModel.getMinimum())));
	}

	/**
	 * Validates the current filter settings.
	 * @return true if filters are valid, false otherwise
	 */
	public boolean validateFilters() {
		// Validate age range
		int minAge = (Integer) minAgeSpinner.getValue();
		int maxAge = (Integer) maxAgeSpinner.getValue();
		if (minAge > maxAge) {
			JOptionPane.showMessageDialog(this,
				"Minimum age cannot be greater than maximum age.",
				"Invalid Age Range",
				JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// Validate sex selection
		if (!maleCheckBox.isSelected() && !femaleCheckBox.isSelected()) {
			JOptionPane.showMessageDialog(this,
				"Please select at least one sex option.",
				"Invalid Sex Selection",
				JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	/**
	 * Applies the filters set in the dialog to the currentFilterCriteria object.
	 */
	public void applyFilters() {
		// Validate filters before applying
		if (!validateFilters()) {
			return;
		}

		// Get validated values
		int minAge = (Integer) minAgeSpinner.getValue();
		int maxAge = (Integer) maxAgeSpinner.getValue();

		// Apply filters
		currentFilterCriteria.filterFirstName = firstNameField.getText().trim();
		currentFilterCriteria.filterLastName = lastNameField.getText().trim();
		currentFilterCriteria.filterMiddleName = middleNameField.getText().trim();
		currentFilterCriteria.middleInitialOnly = middleInitialOnlyCheckBox.isSelected();
		currentFilterCriteria.filterGradeLevel = (String) gradeLevelComboBox.getSelectedItem();
		currentFilterCriteria.filterSection = (String) sectionComboBox.getSelectedItem();
		currentFilterCriteria.filterTrackStrand = (String) trackStrandComboBox.getSelectedItem();
		currentFilterCriteria.filterMale = maleCheckBox.isSelected();
		currentFilterCriteria.filterFemale = femaleCheckBox.isSelected();
		currentFilterCriteria.minAge = minAge;
		currentFilterCriteria.maxAge = maxAge;

		filtersApplied = true;
	}

	/**
	 * Clears all filter fields in the dialog and applies these cleared filters.
	 */
	public void clearFiltersAndApply() {
		// Reset UI fields
		firstNameField.setText("");
		lastNameField.setText("");
		middleNameField.setText("");
		middleInitialOnlyCheckBox.setSelected(false);
		gradeLevelComboBox.setSelectedItem("All");
		sectionComboBox.setSelectedItem("All");
		trackStrandComboBox.setSelectedItem("All");
		maleCheckBox.setSelected(true);
		femaleCheckBox.setSelected(true);
		minAgeSpinner.setValue(initialDbMinAge);
		maxAgeSpinner.setValue(initialDbMaxAge > initialDbMinAge ? initialDbMaxAge + 5 : initialDbMinAge + 20);

		// Reset the underlying FilterCriteria object
		currentFilterCriteria.reset(initialDbMinAge, initialDbMaxAge);

		filtersApplied = true;

	}

	/**
	 * Checks if the current filter settings are valid without showing error messages.
	 * This is useful for silent validation.
	 * @return true if filters are valid, false otherwise
	 */
	public boolean isFilterValid() {
		int minAge = (Integer) minAgeSpinner.getValue();
		int maxAge = (Integer) maxAgeSpinner.getValue();
		return minAge <= maxAge && (maleCheckBox.isSelected() || femaleCheckBox.isSelected());
	}

	/**
	 * Gets the current filter values without applying them.
	 * @return FilterCriteria object with current values
	 */
	public FilterCriteria getCurrentFilterValues() {
		FilterCriteria values = new FilterCriteria();
		values.filterFirstName = firstNameField.getText().trim();
		values.filterLastName = lastNameField.getText().trim();
		values.filterMiddleName = middleNameField.getText().trim();
		values.middleInitialOnly = middleInitialOnlyCheckBox.isSelected();
		values.filterGradeLevel = (String) gradeLevelComboBox.getSelectedItem();
		values.filterSection = (String) sectionComboBox.getSelectedItem();
		values.filterTrackStrand = (String) trackStrandComboBox.getSelectedItem();
		values.filterMale = maleCheckBox.isSelected();
		values.filterFemale = femaleCheckBox.isSelected();
		values.minAge = (Integer) minAgeSpinner.getValue();
		values.maxAge = (Integer) maxAgeSpinner.getValue();
		return values;
	}

	/**
	 * Checks if the filters were applied by the user (i.e., "Apply" or "Clear" was
	 * clicked).
	 * 
	 * @return True if filters were applied, false otherwise (e.g., dialog was
	 *         cancelled).
	 */
	public boolean wereFiltersApplied() {
		return filtersApplied;
	}
}
