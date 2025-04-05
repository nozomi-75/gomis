package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.utils.GoodMoralGenerator;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class StudentMangementGUI extends Form {
	private static final long serialVersionUID = 1L;
	private GTable studentDataTable;
	private StudentsDataDAO studentsDataCRUD;
	private final Connection connection;
	private final String[] columnNames = { "#", "LRN", "NAME", "SEX", "GRADE & SECTION", "Actions" };
	private SlidePane slidePane;
	private JButton backBtn;
	private JPanel mainPanel;
	private Component currentDetailPanel;
	private JPanel headerSearchPanel;
	private ViolationDAO violationDAO;

	// Filter components
	private JPanel filterPanel;
	private JPanel paginationPanel;
	private JComboBox<Integer> pageSizeComboBox;
	private JButton filterButton;
	private Map<String, String> activeFilters;
	private JPanel appliedFiltersPanel;

	// Filter dialog components
	private JTextField lrnFilterField;
	private JTextField nameFilterField;
	private JComboBox<String> gradeLevelComboBox;
	private JComboBox<String> sectionComboBox;
	private JComboBox<String> trackStrandComboBox;
	private JCheckBox maleCheckbox;
	private JCheckBox femaleCheckbox;
	private JSpinner minAgeSpinner;
	private JSpinner maxAgeSpinner;
	private JLabel filterCountLabel;
	private JPanel appliedFiltersSection;

	public StudentMangementGUI(Connection conn) {
		this.connection = conn;
		this.activeFilters = new HashMap<>();
		studentsDataCRUD = new StudentsDataDAO(conn);
		this.violationDAO = new ViolationDAO(conn);
		initializeComponents();
		setupLayout();
		loadStudentData();
	}

	private void initializeComponents() {
		setupTable();
		setupFilterComponents();
		setupPagination();

		slidePane = new SlidePane();
		slidePane.setOpaque(true);

		mainPanel = createStudentTablePanel();

		backBtn = new JButton("Back");
		backBtn.setVisible(false);
		backBtn.addActionListener(e -> {
			slidePane.addSlide(mainPanel, SlidePaneTransition.Type.BACK);
			currentDetailPanel = null;
			backBtn.setVisible(false);
			headerSearchPanel.setVisible(true); // Show search panel when returning to main view
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		});

		slidePane.addSlide(mainPanel, SlidePaneTransition.Type.FORWARD);

		JButton printGoodMoralButton = new JButton("Print Good Moral Certificate");
		printGoodMoralButton.addActionListener(e -> handlePrintGoodMoral());
		// Add the button to your UI layout
	}

	/**
	 * Sets up the filter components
	 */
	private void setupFilterComponents() {
		// Create the main filter panel
		filterPanel = new JPanel(new MigLayout("insets 10", "[grow]", "[]"));
		filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Students"));

		// Create the filter button
		filterButton = new JButton("Filter Students", new FlatSVGIcon("icons/filter.svg", 0.4f));
		filterButton.addActionListener(e -> showFilterDialog());
		filterPanel.add(filterButton, "cell 0 0, growx");

		// Create the applied filters panel
		appliedFiltersPanel = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
		appliedFiltersPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		filterPanel.add(appliedFiltersPanel, "cell 0 1, growx");

		// Add a label for when no filters are applied
		JLabel noFiltersLabel = new JLabel("No filters applied");
		noFiltersLabel.setForeground(Color.GRAY);
		appliedFiltersPanel.add(noFiltersLabel, "cell 0 0");

		// Initialize comboboxes with data from database
		populateComboBoxesFromDatabase();
	}

	private void populateComboBoxesFromDatabase() {
		try {
			// Get unique values from SchoolForm table
			String gradeQuery = "SELECT DISTINCT SF_GRADE_LEVEL FROM SCHOOL_FORM WHERE SF_GRADE_LEVEL IS NOT NULL ORDER BY SF_GRADE_LEVEL";
			String sectionQuery = "SELECT DISTINCT SF_SECTION FROM SCHOOL_FORM WHERE SF_SECTION IS NOT NULL ORDER BY SF_SECTION";
			String trackQuery = "SELECT DISTINCT SF_TRACK_AND_STRAND FROM SCHOOL_FORM WHERE SF_TRACK_AND_STRAND IS NOT NULL ORDER BY SF_TRACK_AND_STRAND";

			List<String> gradeLevels = new ArrayList<>();
			List<String> sections = new ArrayList<>();
			List<String> tracks = new ArrayList<>();

			gradeLevels.add(""); // Add empty option
			sections.add(""); // Add empty option
			tracks.add(""); // Add empty option

			// Get grade levels
			try (PreparedStatement stmt = connection.prepareStatement(gradeQuery);
				 ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String gradeLevel = rs.getString("SF_GRADE_LEVEL");
					if (gradeLevel != null && !gradeLevel.trim().isEmpty()) {
						gradeLevels.add("Grade " + gradeLevel);
					}
				}
			}

			// Get sections
			try (PreparedStatement stmt = connection.prepareStatement(sectionQuery);
				 ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String section = rs.getString("SF_SECTION");
					if (section != null && !section.trim().isEmpty()) {
						sections.add(section);
					}
				}
			}

			// Get tracks and strands
			try (PreparedStatement stmt = connection.prepareStatement(trackQuery);
				 ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					String track = rs.getString("SF_TRACK_AND_STRAND");
					if (track != null && !track.trim().isEmpty()) {
						tracks.add(track);
					}
				}
			}

			// Initialize comboboxes with the retrieved data
			gradeLevelComboBox = new JComboBox<>(gradeLevels.toArray(new String[0]));
			sectionComboBox = new JComboBox<>(sections.toArray(new String[0]));
			trackStrandComboBox = new JComboBox<>(tracks.toArray(new String[0]));

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
				"Error loading filter options from database: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Shows the filter dialog
	 */
	private void showFilterDialog() {
		// Create the filter dialog panel with scrollpane
		JPanel filterDialogPanel = new JPanel(new BorderLayout());
		JPanel contentPanel = createFilterDialogPanel();
		
		// Create and configure the filter count label
		filterCountLabel = new JLabel("0 filters applied");
		filterCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
		filterCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
		filterCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		// Add components to the panel using proper BorderLayout constraints
		filterDialogPanel.add(contentPanel, BorderLayout.CENTER);
		filterDialogPanel.add(filterCountLabel, BorderLayout.SOUTH);

		// Define options for the SimpleModalBorder with explicit close button
		SimpleModalBorder.Option[] modalOptions = new SimpleModalBorder.Option[] {
				new SimpleModalBorder.Option("Apply", SimpleModalBorder.YES_OPTION),
				new SimpleModalBorder.Option("Reset", SimpleModalBorder.NO_OPTION),
				new SimpleModalBorder.Option("Close", SimpleModalBorder.CLOSE_OPTION)
		};

		// Create option for the modal dialog
		raven.modal.option.Option option = ModalDialog.getDefaultOption();
		
		// Show the modal dialog
		ModalDialog.showModal(this,
				new SimpleModalBorder(filterDialogPanel, "Filter Students", modalOptions, (controller, action) -> {
					if (action == SimpleModalBorder.YES_OPTION) {
						// Collect filter values
						Map<String, String> filters = new HashMap<>();

						// LRN filter
						String lrnValue = lrnFilterField.getText().trim();
						if (!lrnValue.isEmpty()) {
							filters.put("lrn", lrnValue);
						}

						// Name filter
						String nameValue = nameFilterField.getText().trim();
						if (!nameValue.isEmpty()) {
							filters.put("name", nameValue);
						}

						// Grade Level filter
						String gradeLevelValue = (String) gradeLevelComboBox.getSelectedItem();
						if (gradeLevelValue != null && !gradeLevelValue.isEmpty()) {
							filters.put("gradeLevel", gradeLevelValue);
						}

						// Section filter
						String sectionValue = (String) sectionComboBox.getSelectedItem();
						if (sectionValue != null && !sectionValue.isEmpty()) {
							filters.put("section", sectionValue);
						}

						// Track & Strand filter
						String trackStrandValue = (String) trackStrandComboBox.getSelectedItem();
						if (trackStrandValue != null && !trackStrandValue.isEmpty()) {
							filters.put("trackStrand", trackStrandValue);
						}

						// Sex filter
						if (maleCheckbox.isSelected() && !femaleCheckbox.isSelected()) {
							filters.put("sex", "Male");
						} else if (!maleCheckbox.isSelected() && femaleCheckbox.isSelected()) {
							filters.put("sex", "Female");
						}

						// Age range filter
						int minAge = (Integer) minAgeSpinner.getValue();
						int maxAge = (Integer) maxAgeSpinner.getValue();
						if (minAge > 12 || maxAge < 25) {
							filters.put("ageRange", minAge + "-" + maxAge);
						}

						// Update active filters and UI
						activeFilters = filters;
						updateAppliedFiltersPanel();
						applyFilters();
						controller.close();

					} else if (action == SimpleModalBorder.NO_OPTION) {
						resetFilterFields();
						updateFilterCount(0);
						controller.consume();
					} else {
						controller.close();
					}
				}), option, "filter-dialog");

		// Set size for the modal
		option.getLayoutOption().setSize(600, 500);

		// Initialize the filter count
		updateCurrentFilterCount();
		
		// Add listeners for real-time updates
		addFilterChangeListeners();
	}

	private JPanel createFilterDialogPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Body panel with scroll pane
		JPanel bodyPanel = new JPanel(new MigLayout("insets 0", "[grow]", "[]"));
		JScrollPane scrollPane = new JScrollPane(bodyPanel);
		scrollPane.setBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);

		// Applied filters section
		appliedFiltersSection = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
		appliedFiltersSection.setBorder(BorderFactory.createTitledBorder("Applied Filters"));

		JLabel noFiltersLabel = new JLabel("No filters applied");
		noFiltersLabel.setForeground(Color.GRAY);
		appliedFiltersSection.add(noFiltersLabel, "cell 0 0");

		bodyPanel.add(appliedFiltersSection, "cell 0 0, growx, wrap");

		// Filter form
		JPanel filterForm = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
		filterForm.setBorder(BorderFactory.createTitledBorder("Filter Options"));

		// LRN filter
		JPanel lrnFilterGroup = createFilterGroup("LRN", "🔍");
		lrnFilterField = new JTextField(15);
		lrnFilterGroup.add(lrnFilterField, "cell 0 1, growx");
		filterForm.add(lrnFilterGroup, "cell 0 0, growx, wrap");

		// Name filter
		JPanel nameFilterGroup = createFilterGroup("Name", "👤");
		nameFilterField = new JTextField(20);
		nameFilterGroup.add(nameFilterField, "cell 0 1, growx");
		filterForm.add(nameFilterGroup, "cell 0 1, growx, wrap");

		// Grade Level filter
		JPanel gradeLevelFilterGroup = createFilterGroup("Grade Level", "🎓");
		gradeLevelFilterGroup.add(gradeLevelComboBox, "cell 0 1, growx");
		filterForm.add(gradeLevelFilterGroup, "cell 0 2, growx, wrap");

		// Section filter
		JPanel sectionFilterGroup = createFilterGroup("Section", "👥");
		sectionFilterGroup.add(sectionComboBox, "cell 0 1, growx");
		filterForm.add(sectionFilterGroup, "cell 0 3, growx, wrap");

		// Track & Strand filter
		JPanel trackStrandFilterGroup = createFilterGroup("Track & Strand", "🔍");
		trackStrandFilterGroup.add(trackStrandComboBox, "cell 0 1, growx");
		filterForm.add(trackStrandFilterGroup, "cell 0 4, growx, wrap");

		// Sex filter
		JPanel sexFilterGroup = createFilterGroup("Sex", "👤");
		JPanel sexCheckboxPanel = new JPanel(new MigLayout("insets 0", "[][]", "[]"));
		maleCheckbox = new JCheckBox("Male");
		femaleCheckbox = new JCheckBox("Female");
		maleCheckbox.setSelected(true);
		femaleCheckbox.setSelected(true);
		sexCheckboxPanel.add(maleCheckbox, "cell 0 0");
		sexCheckboxPanel.add(femaleCheckbox, "cell 1 0");
		sexFilterGroup.add(sexCheckboxPanel, "cell 0 1, growx");
		filterForm.add(sexFilterGroup, "cell 0 5, growx, wrap");

		// Age Range filter
		JPanel ageFilterGroup = createFilterGroup("Age Range", "📊");
		JPanel ageRangePanel = new JPanel(new MigLayout("insets 0", "[][grow][][grow]", "[]"));

		JLabel minAgeLabel = new JLabel("Min Age:");
		minAgeSpinner = new JSpinner(new SpinnerNumberModel(12, 12, 25, 1));

		JLabel maxAgeLabel = new JLabel("Max Age:");
		maxAgeSpinner = new JSpinner(new SpinnerNumberModel(25, 12, 25, 1));

		ageRangePanel.add(minAgeLabel, "cell 0 0");
		ageRangePanel.add(minAgeSpinner, "cell 1 0, growx");
		ageRangePanel.add(maxAgeLabel, "cell 2 0");
		ageRangePanel.add(maxAgeSpinner, "cell 3 0, growx");

		ageFilterGroup.add(ageRangePanel, "cell 0 1, growx");
		filterForm.add(ageFilterGroup, "cell 0 6, growx, wrap");

		bodyPanel.add(filterForm, "cell 0 1, growx, wrap");

		return panel;
	}

	/**
	 * Creates a filter group with label and icon
	 */
	private JPanel createFilterGroup(String label, String icon) {
		JPanel panel = new JPanel(new MigLayout("insets 5", "[grow]", "[]"));
		panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

		JLabel filterLabel = new JLabel(icon + " " + label);
		filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
		panel.add(filterLabel, "cell 0 0");

		return panel;
	}

	/**
	 * Updates the filter count label
	 */
	private void updateFilterCount(int count) {
		if (filterCountLabel != null) {
			filterCountLabel.setText(count + " filter" + (count != 1 ? "s" : "") + " applied");
		}
	}

	/**
	 * Updates the applied filters section
	 */
	private void updateAppliedFiltersSection(JPanel panel, Map<String, String> filters) {
		panel.removeAll();

		if (filters.isEmpty()) {
			JLabel noFiltersLabel = new JLabel("No filters applied");
			noFiltersLabel.setForeground(Color.GRAY);
			panel.add(noFiltersLabel, "cell 0 0");
		} else {
			int row = 0;
			for (Map.Entry<String, String> entry : filters.entrySet()) {
				JPanel filterPill = new JPanel(new MigLayout("insets 5", "[grow][]", "[]"));
				filterPill.setBackground(new Color(232, 245, 233)); // Light green
				filterPill.setBorder(
						BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 230, 201)),
								BorderFactory.createEmptyBorder(2, 5, 2, 5)));

				JLabel filterLabel = new JLabel(entry.getValue());
				filterLabel.setForeground(new Color(46, 125, 50)); // Dark green
				filterPill.add(filterLabel, "cell 0 0");

				JButton removeButton = new JButton("×");
				removeButton.setForeground(new Color(102, 102, 102));
				removeButton.setBackground(new Color(232, 245, 233));
				removeButton.setBorderPainted(false);
				removeButton.setFocusPainted(false);
				removeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
				removeButton.addActionListener(e -> {
					filters.remove(entry.getKey());
					updateAppliedFiltersSection(panel, filters);
					updateFilterCount(filters.size());
				});
				filterPill.add(removeButton, "cell 1 0");

				panel.add(filterPill, "cell 0 " + row++ + ", growx, wrap");
			}
		}

		panel.revalidate();
		panel.repaint();
	}

	/**
	 * Updates the applied filters panel in the main UI
	 */
	private void updateAppliedFiltersPanel() {
		appliedFiltersPanel.removeAll();

		if (activeFilters.isEmpty()) {
			JLabel noFiltersLabel = new JLabel("No filters applied");
			noFiltersLabel.setForeground(Color.GRAY);
			appliedFiltersPanel.add(noFiltersLabel, "cell 0 0");
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

				JButton removeButton = new JButton("×");
				removeButton.setForeground(new Color(102, 102, 102));
				removeButton.setBackground(new Color(232, 245, 233));
				removeButton.setBorderPainted(false);
				removeButton.setFocusPainted(false);
				removeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
				removeButton.addActionListener(e -> {
					activeFilters.remove(entry.getKey());
					updateAppliedFiltersPanel();
					applyFilters();
				});
				filterPill.add(removeButton, "cell 1 0");

				appliedFiltersPanel.add(filterPill, "cell 0 " + row++ + ", growx, wrap");
			}
		}

		appliedFiltersPanel.revalidate();
		appliedFiltersPanel.repaint();
	}

	/**
	 * Sets up pagination controls
	 */
	private void setupPagination() {
		paginationPanel = new JPanel(new MigLayout("insets 5", "[][][]", "[]"));

		// Page size selector
		paginationPanel.add(new JLabel("Rows per page:"), "cell 0 0");
		pageSizeComboBox = new JComboBox<>(new Integer[] { 5, 10, 25, 50, 100 });
		pageSizeComboBox.setSelectedItem(10);
		pageSizeComboBox.addActionListener(e -> {
			int pageSize = (Integer) pageSizeComboBox.getSelectedItem();
			studentDataTable.setPageSize(pageSize);
		});
		paginationPanel.add(pageSizeComboBox, "cell 1 0");
	}

	private void setupTable() {
		Object[][] initialData = new Object[0][columnNames.length];
		Class<?>[] columnTypes = { Boolean.class, Integer.class, String.class, String.class, String.class, String.class,
				Object.class };
		boolean[] editableColumns = { true, false, false, false, false, false, true };

		// Adjusted column widths to match the image proportions
		double[] columnWidths = { 0.03, 0.05, 0.20, 0.10, 0.15, 0.15 }; // Sum to 1.0
		int[] alignments = { SwingConstants.CENTER, // Checkbox
				SwingConstants.CENTER, // #
				SwingConstants.LEFT, // NAME
				SwingConstants.CENTER, // SEX
				SwingConstants.CENTER, // GRADE & SECTION
				SwingConstants.CENTER // Actions
		};
		boolean includeCheckbox = true;

		TableActionManager actionManager = new DefaultTableActionManager();
		((DefaultTableActionManager) actionManager).addAction("View", (table, row) -> {
			String lrn = (String) table.getValueAt(row, 2);
			FlatAnimatedLafChange.showSnapshot();

			try {
				Student studentData = studentsDataCRUD.getStudentDataByLrn(lrn);
				if (studentData != null) {
					currentDetailPanel = new StudentFullData(connection, studentData);
					slidePane.addSlide(currentDetailPanel, SlidePaneTransition.Type.FORWARD);
					backBtn.setVisible(true);
					headerSearchPanel.setVisible(false); // Hide search panel when viewing student
				} else {
					JOptionPane.showMessageDialog(this, "Student data not found for LRN: " + lrn, "Data Not Found",
							JOptionPane.WARNING_MESSAGE);
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this, "Error retrieving student data: " + e.getMessage(),
						"Database Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}

			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		}, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));

		studentDataTable = new GTable(initialData,
				new String[] { " ", "#", "LRN", "NAME", "SEX", "GRADE & SECTION", "Actions" }, columnTypes,
				editableColumns, columnWidths, alignments, includeCheckbox, actionManager);

		// Enable pagination with default page size
		studentDataTable.setPaginationEnabled(true, 10);
	}

	private void setupLayout() {
		setLayout(new BorderLayout());

		JPanel headerPanel = new JPanel(new MigLayout("", "[][][][grow][][]", "[]"));
		JLabel headerLabel = new JLabel("STUDENT DATA");
		headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

		headerPanel.add(headerLabel, "cell 1 0,alignx center,growy");

		headerSearchPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		JButton searchButton = createSearchButton("Search Student", " ");
		searchButton.addActionListener(e -> showSearchPanel());
		headerSearchPanel.add(searchButton, "cell 0 0,grow");

		JButton filterButton = new JButton("Filter Students", new FlatSVGIcon("icons/filter.svg", 0.4f));
		filterButton.addActionListener(e -> showFilterDialog());
		headerSearchPanel.add(filterButton, "cell 1 0,gapleft 10");

		JButton printGoodMoralButton = new JButton("Print Good Moral Certificate");
		printGoodMoralButton.addActionListener(e -> handlePrintGoodMoral());
		headerSearchPanel.add(printGoodMoralButton, "cell 2 0,gapleft 10");

		headerPanel.add(headerSearchPanel, "flowx,cell 3 0,grow");
		headerPanel.add(backBtn, "cell 5 0");
		add(headerPanel, BorderLayout.NORTH);

		// Add filter panel below header
		add(filterPanel, BorderLayout.CENTER);

		// Add table panel
		add(slidePane, BorderLayout.CENTER);

		// Add pagination panel at the bottom
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(studentDataTable.getPaginationPanel(), BorderLayout.CENTER);
		bottomPanel.add(paginationPanel, BorderLayout.EAST);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	public static JButton createSearchButton(String text, String rightTxt) {
		JButton button = new JButton(text, new FlatSVGIcon("icons/search.svg", 0.4f));
		button.setLayout(new MigLayout("insets 0,al trailing,filly", "", "[center]"));
		button.setHorizontalAlignment(JButton.LEADING);
		button.putClientProperty(FlatClientProperties.STYLE, "" + "margin:5,7,5,10;" + "arc:10;" + "borderWidth:0;"
				+ "focusWidth:0;" + "innerFocusWidth:0;" + "[light]background:shade($Panel.background,10%);"
				+ "[dark]background:tint($Panel.background,10%);" + "[light]foreground:tint($Button.foreground,40%);"
				+ "[dark]foreground:shade($Button.foreground,30%);");
		JLabel label = new JLabel(rightTxt);
		label.putClientProperty(FlatClientProperties.STYLE, "" + "[light]foreground:tint($Button.foreground,40%);"
				+ "[dark]foreground:shade($Button.foreground,30%);");
		button.add(label);
		return button;
	}

	private void showSearchPanel() {
		if (ModalDialog.isIdExist("search")) {
			return;
		}
		Option option = ModalDialog.createOption();
		option.setAnimationEnabled(true);
		option.getLayoutOption().setMargin(40, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);

		// Create StudentSearchPanel with a callback to handle selected student
		StudentSearchPanel searchPanel = new StudentSearchPanel(connection, "student-search") {
			@Override
			protected void onStudentSelected(Student student) {
				ModalDialog.closeModal("student-search"); // Close the search panel
				showStudentDetails(student); // Show the student details
			}
		};

		ModalDialog.showModal(this, searchPanel, option, "student-search");
	}

	public void showStudentDetails(Student studentData) {
		FlatAnimatedLafChange.showSnapshot();
		if (studentData != null) {
			currentDetailPanel = new StudentFullData(connection, studentData);
			slidePane.addSlide(currentDetailPanel, SlidePaneTransition.Type.FORWARD);
			backBtn.setVisible(true);
			headerSearchPanel.setVisible(false);
		} else {
			JOptionPane.showMessageDialog(this, "Student data not found", "Data Not Found",
					JOptionPane.WARNING_MESSAGE);
		}
		FlatAnimatedLafChange.hideSnapshotWithAnimation();
	}

	private JPanel createStudentTablePanel() {
		JScrollPane scrollPane = new JScrollPane(studentDataTable);
		JPanel panel = new JPanel(new MigLayout("", "[grow]", "[grow]"));
		panel.add(scrollPane, "cell 0 0,grow");
		return panel;
	}

	private void loadStudentData() {
		try {
			List<Student> studentsDataList = studentsDataCRUD.getAllStudentsData();
			updateTableWithStudentData(studentsDataList);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	/**
	 * Updates the table with the provided student data
	 * 
	 * @param studentsDataList List of students to display
	 */
	private void updateTableWithStudentData(List<Student> studentsDataList) {
		studentDataTable.clearData();

		int rowNum = 1;
		for (Student studentData : studentsDataList) {
			String fullName = studentData.getStudentFirstname() + " " + studentData.getStudentLastname();
			String gradeAndSection = "";
			if (studentData.getSchoolForm() != null) {
				gradeAndSection = studentData.getSchoolForm().getSF_GRADE_LEVEL() + " "
						+ studentData.getSchoolForm().getSF_SECTION();
			}
			studentDataTable.addRow(new Object[] { false, // Checkbox column
					rowNum++, studentData.getStudentLrn(), fullName, studentData.getStudentSex(), gradeAndSection, null // Actions
																														// column
																														// handled
																														// by
																														// TableActionManager
			});
		}
	}

	/**
	 * Applies the filters to the student data
	 */
	private void applyFilters() {
		try {
			// If no filters are active, load all data
			if (activeFilters.isEmpty()) {
				loadStudentData();
				return;
			}

			// Get filter values
			String lrn = activeFilters.get("lrn");
			String name = activeFilters.get("name");
			String sex = activeFilters.get("sex");
			String gradeLevel = activeFilters.get("gradeLevel");
			String section = activeFilters.get("section");
			String trackStrand = activeFilters.get("trackStrand");
			String ageRange = activeFilters.get("ageRange");

			// First get students based on basic filters (LRN, name, sex)
			List<Student> filteredStudents = studentsDataCRUD.getStudentsByFilters(lrn, name, null, sex);

			// Then apply additional filters
			if (gradeLevel != null || section != null || trackStrand != null || ageRange != null) {
				filteredStudents = applyAdditionalFilters(filteredStudents, gradeLevel, section, trackStrand, ageRange);
			}

			// Update the table with filtered results
			updateTableWithStudentData(filteredStudents);

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
				"Error applying filters: " + e.getMessage(),
				"Database Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Applies additional filters that aren't supported by the DAO
	 */
	private List<Student> applyAdditionalFilters(List<Student> students, String gradeLevel, String section,
			String trackStrand, String ageRange) {
		List<Student> filteredStudents = new ArrayList<>(students);

		// Filter by grade level
		if (gradeLevel != null && !gradeLevel.isEmpty()) {
			String grade = gradeLevel.replace("Grade ", "").trim();
			filteredStudents.removeIf(student -> 
				student.getSchoolForm() == null || 
				!grade.equals(student.getSchoolForm().getSF_GRADE_LEVEL()));
		}

		// Filter by section
		if (section != null && !section.isEmpty()) {
			filteredStudents.removeIf(student -> 
				student.getSchoolForm() == null || 
				!section.equals(student.getSchoolForm().getSF_SECTION()));
		}

		// Filter by track and strand
		if (trackStrand != null && !trackStrand.isEmpty()) {
			filteredStudents.removeIf(student -> 
				student.getSchoolForm() == null || 
				!trackStrand.equals(student.getSchoolForm().getSF_TRACK_AND_STRAND()));
		}

		// Filter by age range
		if (ageRange != null && !ageRange.isEmpty()) {
			String[] range = ageRange.split("-");
			int minAge = Integer.parseInt(range[0]);
			int maxAge = Integer.parseInt(range[1]);

			filteredStudents.removeIf(student -> 
				student.getStudentAge() < minAge || 
				student.getStudentAge() > maxAge);
		}

		return filteredStudents;
	}

	/**
	 * Resets all filters and reloads all student data
	 */
	private void resetFilters() {
		activeFilters.clear();
		updateAppliedFiltersPanel();
		loadStudentData();
	}

	private void handlePrintGoodMoral() {
		List<Student> selectedStudents = new ArrayList<>();
		List<Student> studentsWithActiveViolations = new ArrayList<>();

		for (int i = 0; i < studentDataTable.getRowCount(); i++) {
			Boolean isSelected = (Boolean) studentDataTable.getValueAt(i, 0);
			if (isSelected != null && isSelected) {
				String lrn = (String) studentDataTable.getValueAt(i, 2);
				try {
					Student student = studentsDataCRUD.getStudentDataByLrn(lrn);
					if (student != null) {
						// Check for active violations using student's UID
						List<Violation> violations = violationDAO.getViolationsByStudentUID(student.getStudentUid());
						boolean hasActiveViolation = violations.stream().anyMatch(v -> v.getStatus().equals("Active"));

						if (hasActiveViolation) {
							studentsWithActiveViolations.add(student);
						} else {
							selectedStudents.add(student);
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		if (selectedStudents.isEmpty() && studentsWithActiveViolations.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select at least one student.", "No Selection",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (!studentsWithActiveViolations.isEmpty()) {
			StringBuilder message = new StringBuilder(
					"The following students have active violations and cannot receive a good moral certificate:\n\n");
			for (Student student : studentsWithActiveViolations) {
				message.append("- ").append(student.getStudentFirstname()).append(" ")
						.append(student.getStudentLastname()).append(" (LRN: ").append(student.getStudentLrn())
						.append(")\n");
			}
			message.append("\nPlease remove these students from the selection to proceed.");

			JOptionPane.showMessageDialog(this, message.toString(), "Active Violations Found",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// If no violations, proceed with printing using the GoodMoralGenerator
		// This will show a modal with scrollpanes for both the purpose field and
		// student list
		GoodMoralGenerator.createBatchGoodMoralReport(this, selectedStudents);
	}

	/**
	 * Sets the active filters and updates the table
	 * 
	 * @param filters The map of active filters
	 */
	public void setActiveFilters(Map<String, String> filters) {
		this.activeFilters = filters;
		applyFilters();
	}

	/**
	 * Resets all filter fields to their default values
	 */
	private void resetFilterFields() {
		lrnFilterField.setText("");
		nameFilterField.setText("");
		gradeLevelComboBox.setSelectedIndex(0);
		sectionComboBox.setSelectedIndex(0);
		trackStrandComboBox.setSelectedIndex(0);
		maleCheckbox.setSelected(true);
		femaleCheckbox.setSelected(true);
		minAgeSpinner.setValue(12);
		maxAgeSpinner.setValue(25);
		activeFilters.clear();
		updateAppliedFiltersPanel();
	}

	// Add action listeners to update filter count in real-time
	private void addFilterChangeListeners() {
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateCurrentFilterCount();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateCurrentFilterCount();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateCurrentFilterCount();
			}
		};

		lrnFilterField.getDocument().addDocumentListener(documentListener);
		nameFilterField.getDocument().addDocumentListener(documentListener);

		ActionListener comboBoxListener = e -> updateCurrentFilterCount();
		gradeLevelComboBox.addActionListener(comboBoxListener);
		sectionComboBox.addActionListener(comboBoxListener);
		trackStrandComboBox.addActionListener(comboBoxListener);

		ActionListener checkBoxListener = e -> updateCurrentFilterCount();
		maleCheckbox.addActionListener(checkBoxListener);
		femaleCheckbox.addActionListener(checkBoxListener);

		ChangeListener spinnerListener = e -> updateCurrentFilterCount();
		minAgeSpinner.addChangeListener(spinnerListener);
		maxAgeSpinner.addChangeListener(spinnerListener);
	}

	private void updateCurrentFilterCount() {
		int count = 0;
		
		// Count active filters
		if (!lrnFilterField.getText().trim().isEmpty()) count++;
		if (!nameFilterField.getText().trim().isEmpty()) count++;
		if (gradeLevelComboBox.getSelectedIndex() > 0) count++;
		if (sectionComboBox.getSelectedIndex() > 0) count++;
		if (trackStrandComboBox.getSelectedIndex() > 0) count++;
		
		// Count sex filter
		if (maleCheckbox.isSelected() != femaleCheckbox.isSelected()) count++;
		
		// Count age range filter
		int minAge = (Integer) minAgeSpinner.getValue();
		int maxAge = (Integer) maxAgeSpinner.getValue();
		if (minAge > 12 || maxAge < 25) count++;

		updateFilterCount(count);
	}

}