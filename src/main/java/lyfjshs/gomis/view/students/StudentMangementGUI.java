package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

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
	private Map<String, Object> savedFilterState;

	public StudentMangementGUI(Connection conn) {
		this.connection = conn;
		this.activeFilters = new HashMap<>();
		this.savedFilterState = new HashMap<>();
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
			
			// Clear the filter dialog saved state to start fresh when returning to main panel
			if (savedFilterState != null) {
				savedFilterState.clear();
			}
			
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
		// Create a more modern filter panel without border title
		filterPanel = new JPanel(new MigLayout("insets 5 10 5 10", "[grow]", "[]"));
		filterPanel.setBackground(null); // Use default background

		// Create a more stylish filter button
		filterButton = new JButton("Filter");
		filterButton.putClientProperty(FlatClientProperties.STYLE, "" +
				"background:$Component.accentColor;" +
				"foreground:#FFFFFF;" +  // Use hex color instead of 'white'
				"borderWidth:0;" +
				"focusWidth:0;" +
				"arc:8;" +
				"margin:5,10,5,10");
		
		// Add action to show filter dialog
		filterButton.addActionListener(e -> {
			// Only show the filter dialog if it's not already visible
			if (!ModalDialog.isIdExist("student-filter")) {
				StudentFilterModal.getInstance().showModal(
					connection,
					this,
					this,
					activeFilters,
					400,  // width
					600   // height
				);
			}
		});
		
		// Add the filter button to the panel
		filterPanel.add(filterButton, "cell 0 0, alignx right");

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
	public void showFilterDialog() {
		// If the filter dialog is already open, don't do anything
		if (ModalDialog.isIdExist("student-filter")) {
			return;
		}
		
		// Show the filter modal
		StudentFilterModal.getInstance().showModal(
			connection,
			this,
			this,
			activeFilters,
			400,  // width
			600   // height
		);
	}

	/**
	 * Sets up pagination controls
	 */
	private void setupPagination() {
		paginationPanel = new JPanel(new MigLayout("insets 5", "[][][]", "[]"));

		// Page size selector
		paginationPanel.add(new JLabel("Rows per page:"), "cell 0 0");
		pageSizeComboBox = new JComboBox<>(new Integer[] { 15, 25, 50, 100 });
		pageSizeComboBox.setSelectedItem(15);
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
		studentDataTable.setPaginationEnabled(true, 15 );
	}

	private void setupLayout() {
		setLayout(new BorderLayout());

		JPanel headerPanel = new JPanel(new MigLayout("", "[][][][grow][][]", "[]"));
		JLabel headerLabel = new JLabel("STUDENT DATA");
		headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

		headerPanel.add(headerLabel, "cell 1 0,alignx center,growy");

		headerSearchPanel = new JPanel(new MigLayout("fill", "[grow][][][]", "[grow]"));
		JButton searchButton = createSearchButton("Search Student", " ");
		searchButton.addActionListener(e -> showSearchPanel());
		headerSearchPanel.add(searchButton, "cell 0 0,grow");

		// Use the new filter button
		headerSearchPanel.add(filterButton, "cell 1 0,gapleft 10");

		JButton printGoodMoralButton = new JButton("Print Good Moral Certificate");
		printGoodMoralButton.addActionListener(e -> handlePrintGoodMoral());
		headerSearchPanel.add(printGoodMoralButton, "cell 2 0,gapleft 10");

		headerPanel.add(headerSearchPanel, "flowx,cell 3 0,grow");
		headerPanel.add(backBtn, "cell 5 0");
		add(headerPanel, BorderLayout.NORTH);

		// Create a central container with proper layout
		JPanel centralContainer = new JPanel(new BorderLayout());
		
		// We don't need to add the filter panel anymore since we've moved the button to the header
		// Just add the slide pane to the central container
		centralContainer.add(slidePane, BorderLayout.CENTER);
		
		// Add the central container to the main layout
		add(centralContainer, BorderLayout.CENTER);

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
		
		// Improve scroll pane performance
		scrollPane.getVerticalScrollBar().setUnitIncrement(20); // Faster scrolling
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.showButtons", true);
		scrollPane.getVerticalScrollBar().putClientProperty("JScrollBar.fastWheelScrolling", true);
		
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
	public void applyFilters() {
		try {
			StringBuilder query = new StringBuilder(
				"SELECT DISTINCT s.*, sf.SF_GRADE_LEVEL, sf.SF_SECTION, sf.SF_TRACK_AND_STRAND " +
				"FROM STUDENT s " +
				"LEFT JOIN SCHOOL_FORM sf ON s.SF_ID = sf.SF_ID " +
				"WHERE 1=1");
				
			List<Object> params = new ArrayList<>();

			// Process the active filters
			if (activeFilters != null && !activeFilters.isEmpty()) {
				if (activeFilters.containsKey("name") && !activeFilters.get("name").isEmpty()) {
					String name = activeFilters.get("name");
					query.append(" AND (LOWER(s.STUDENT_FIRSTNAME) LIKE LOWER(?) OR LOWER(s.STUDENT_LASTNAME) LIKE LOWER(?) OR LOWER(CONCAT(s.STUDENT_FIRSTNAME, ' ', s.STUDENT_LASTNAME)) LIKE LOWER(?))");
					params.add("%" + name + "%");
					params.add("%" + name + "%");
					params.add("%" + name + "%");
				}

				if (activeFilters.containsKey("gradeLevel") && !activeFilters.get("gradeLevel").isEmpty()) {
					String gradeLevel = activeFilters.get("gradeLevel");
					// Handle both formats: "Grade 12" and "12"
					if (gradeLevel.startsWith("Grade ")) {
						gradeLevel = "Grade " + gradeLevel.replace("Grade ", "").trim();
					} else {
						gradeLevel = "Grade " + gradeLevel.trim();
					}
					query.append(" AND sf.SF_GRADE_LEVEL = ?");
					params.add(gradeLevel);
				}

				if (activeFilters.containsKey("section") && !activeFilters.get("section").isEmpty()) {
					query.append(" AND UPPER(sf.SF_SECTION) = UPPER(?)");
					params.add(activeFilters.get("section"));
				}

				if (activeFilters.containsKey("trackStrand") && !activeFilters.get("trackStrand").isEmpty()) {
					query.append(" AND UPPER(sf.SF_TRACK_AND_STRAND) = UPPER(?)");
					params.add(activeFilters.get("trackStrand"));
				}

				if (activeFilters.containsKey("sex")) {
					String sexFilter = activeFilters.get("sex");
					if (sexFilter != null && !sexFilter.isEmpty()) {
						String[] sexes = sexFilter.split(",");
						if (sexes.length > 0) {
							query.append(" AND (");
							for (int i = 0; i < sexes.length; i++) {
								if (i > 0) query.append(" OR ");
								query.append("s.STUDENT_SEX IN (?, ?)");
								String sex = sexes[i];
								params.add(sex.substring(0, 1)); // M or F
								params.add(sex); // Male or Female
							}
							query.append(")");
						}
					}
				}

				if (activeFilters.containsKey("minAge") && !activeFilters.get("minAge").isEmpty()) {
					query.append(" AND CAST(s.STUDENT_AGE AS INTEGER) >= ?");
					params.add(Integer.parseInt(activeFilters.get("minAge")));
				}

				if (activeFilters.containsKey("maxAge") && !activeFilters.get("maxAge").isEmpty()) {
					query.append(" AND CAST(s.STUDENT_AGE AS INTEGER) <= ?");
					params.add(Integer.parseInt(activeFilters.get("maxAge")));
				}
			}

			query.append(" ORDER BY s.STUDENT_LASTNAME ASC, s.STUDENT_FIRSTNAME ASC");

			System.out.println("Query: " + query.toString()); // Debug print
			System.out.println("Params: " + params); // Debug print

			// Prepare and execute the query
			PreparedStatement statement = connection.prepareStatement(query.toString());
			for (int i = 0; i < params.size(); i++) {
				statement.setObject(i + 1, params.get(i));
			}

			ResultSet resultSet = statement.executeQuery();
			updateTableWithResults(resultSet);
			
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error querying database: " + ex.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateTableWithResults(ResultSet resultSet) throws SQLException {
		studentDataTable.clearData();

		int rowNum = 1;
		while (resultSet.next()) {
			String fullName = resultSet.getString("STUDENT_FIRSTNAME") + " " + resultSet.getString("STUDENT_LASTNAME");
			String gradeAndSection = "";
			
			String gradeLevel = resultSet.getString("SF_GRADE_LEVEL");
			String section = resultSet.getString("SF_SECTION");
			
			if (gradeLevel != null && section != null) {
				gradeAndSection = "Grade " + gradeLevel + " " + section;
			}
			
			String sex = resultSet.getString("STUDENT_SEX");
			// Convert database sex values to display values
			if (sex != null) {
				sex = sex.equals("M") ? "Male" : "Female";
			}
			
			studentDataTable.addRow(new Object[] { 
				false, // Checkbox column
				rowNum++, 
				resultSet.getString("STUDENT_LRN"), 
				fullName, 
				sex, 
				gradeAndSection, 
				null // Actions column handled by TableActionManager
			});
		}
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
		if (filters == null) {
			this.activeFilters = new HashMap<>();
			this.savedFilterState = new HashMap<>();
		} else {
			this.activeFilters = new HashMap<>(filters);
			// Save the filter state
			this.savedFilterState = new HashMap<>();
			for (Map.Entry<String, String> entry : filters.entrySet()) {
				this.savedFilterState.put(entry.getKey(), entry.getValue());
			}
		}
		
		// Apply the filters immediately
		applyFilters();
	}

	/**
	 * Gets the saved filter state
	 */
	public Map<String, Object> getSavedFilterState() {
		return savedFilterState;
	}

	@Override
	public void dispose() {
		// Clear the saved filter state when this form is disposed
		if (savedFilterState != null) {
			savedFilterState.clear();
		}
		if (activeFilters != null) {
			activeFilters.clear();
		}
		super.dispose();
	}

}