/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.sql.Connection;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import docPrinter.templateManager;
import docPrinter.goodMoral.goodMoralGenarate;
import docPrinter.goodMoral.goodMoralModalPanel;
import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;
import raven.modal.ModalDialog;

public class StudentsListMain extends Form {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(StudentsListMain.class);
	private JPanel contentPanel;
	private JPanel mainContentPanel;
	private JScrollPane tableSP;
	private GTable studentDataTable;
	private StudentsDataDAO studentsDataCRUD;
	private final Connection connection;
	private final String[] columnNames = { "#", "LRN", "NAME", "SEX", "GRADE & SECTION", "Actions" };
	private SlidePane slidePane;
	private Component currentDetailPanel;
	private ViolationDAO violationDAO;
	private final Integer[] availablePageSizes = { 10, 25, 50, 100, 250 };
	private int currentPageSize = 25;

	private JPanel paginationFootPanel;
	private JComboBox<Integer> pageSizeComboBox;
	private Map<String, String> activeFilters;
	private Map<String, Object> savedFilterState;
	private JButton prevPageButton;
	private JButton nextPageButton;
	private JLabel pageInfoLabel;
	private JButton firstPageButton;
	private JButton lastPageButton;
	private JPanel tablePanel;
	private Object[][] initialData = new Object[0][columnNames.length];
	private Class<?>[] columnTypes = { Boolean.class, Integer.class, String.class, String.class, String.class,
			String.class, Object.class };
	private boolean[] editableColumns = { true, false, false, false, false, false, true };

	private FilterCriteria currentFilters = new FilterCriteria();

	private double[] columnWidths = { 0.03, 0.05, 0.20, 0.10, 0.15, 0.15 }; // Sum to 1.0
	private int[] alignments = { SwingConstants.CENTER, // Checkbox
			SwingConstants.CENTER, // #
			SwingConstants.LEFT, // NAME
			SwingConstants.CENTER, // SEX
			SwingConstants.CENTER, // GRADE & SECTION
			SwingConstants.CENTER // Actions
	};
	private boolean includeCheckbox = true;

	private TableActionManager actionManager = new DefaultTableActionManager();
	private HeaderPanel headPanel;

	private void intializeComponents() {
		try {
			// Initialize maps
			activeFilters = new HashMap<>();
			savedFilterState = new HashMap<>();

			// content panel components
			contentPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow 1][grow]"));
			headPanel = new HeaderPanel();
			mainContentPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));

			// main content panel components
			slidePane = new SlidePane();
			tablePanel = new JPanel(new MigLayout("fill", "[grow]", "[grow][grow 1]"));

			// table panel components
			{
				studentDataTable = new GTable(initialData,
						new String[] { " ", "#", "LRN", "NAME", "SEX", "GRADE & SECTION", "Actions" }, columnTypes,
						editableColumns, columnWidths, alignments, includeCheckbox, actionManager);
				studentDataTable.setRowHeight(34);
				tableSP = new JScrollPane(studentDataTable);
				((DefaultTableActionManager) actionManager).addAction("View", (table, row) -> {
					String lrn = (String) table.getValueAt(row, 2);
					FlatAnimatedLafChange.showSnapshot();

					try {
						Student studentData = studentsDataCRUD.getStudentDataByLrn(lrn);
						if (studentData != null) {
							currentDetailPanel = new StudentFullData(connection, studentData);
							slidePane.addSlide(currentDetailPanel, SlidePaneTransition.Type.FORWARD);
							headPanel.getBackBtn().setVisible(true);
							headPanel.getHeaderSearchPanel().setVisible(false); // Hide search panel when viewing
																				// student
						} else {
							JOptionPane.showMessageDialog(this, "Student data not found for LRN: " + lrn,
									"Data Not Found", JOptionPane.WARNING_MESSAGE);
						}
					} catch (SQLException e) {
						logger.error("Error retrieving student data: " + e.getMessage(), e);
					}

					FlatAnimatedLafChange.hideSnapshotWithAnimation();
				}, new Color(0x518b6f), new FlatSVGIcon("icons/view.svg", 0.5f));
				((DefaultTableActionManager) actionManager).setupTableColumn(studentDataTable, 6);
			}
			// pagination panel components:
			paginationFootPanel = new JPanel(new MigLayout("insets 5", "[][grow][][][][][][grow][][]", "[grow]"));
			prevPageButton = new JButton("<");
			firstPageButton = new JButton("<< First");
			pageInfoLabel = new JLabel("Page ");
			nextPageButton = new JButton(">");
			lastPageButton = new JButton("Last >>");

			pageSizeComboBox = new JComboBox<>(availablePageSizes);

		} catch (Exception e) {
			logger.error("Error initializing components: " + e.getMessage(), e);
			JOptionPane.showMessageDialog(this, "Error initializing components: " + e.getMessage(),
					"Initialization Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public StudentsListMain(Connection connect) {
		this.connection = connect;
		this.studentsDataCRUD = new StudentsDataDAO(connection);
		this.violationDAO = new ViolationDAO(connection);

		intializeComponents();

		this.setLayout(new MigLayout("", "[grow]", "[grow]"));

		add(contentPanel, "cell 0 0,grow");
		contentPanel.add(headPanel, "cell 0 0,grow");

		// Initialize button actions
		initializeButtonActions();

		// slidePane is used as a holder of both panels to have a Slide Transition
		// Transition Types: BACK, FORWARD, ZOOM_IN, ZOOM_OUT, TOP_DOWN, DOWN_TOP
		slidePane.addSlide(mainContentPanel, SlidePaneTransition.Type.FORWARD);

		contentPanel.add(slidePane, "cell 0 1,grow");
		mainContentPanel.add(tablePanel, "cell 0 0,grow");
		tablePanel.add(tableSP, "cell 0 0,grow");
		studentDataTable.setPageSize(currentPageSize); // GTable manages its page size

		tablePanel.add(paginationFootPanel, "cell 0 1,grow");
		paginationFootPanel.setOpaque(false);

		paginationFootPanel.add(firstPageButton, "cell 2 0");
		paginationFootPanel.add(prevPageButton, "cell 3 0");
		paginationFootPanel.add(pageInfoLabel, "cell 4 0");
		paginationFootPanel.add(nextPageButton, "cell 5 0");
		paginationFootPanel.add(lastPageButton, "cell 6 0");

		firstPageButton.addActionListener(e -> {
			studentDataTable.goToFirstPage();
			updateExternalPaginationUI();
		});
		prevPageButton.addActionListener(e -> {
			studentDataTable.goToPreviousPage();
			updateExternalPaginationUI();
		});
		nextPageButton.addActionListener(e -> {
			studentDataTable.goToNextPage();
			updateExternalPaginationUI();
		});
		lastPageButton.addActionListener(e -> {
			studentDataTable.goToLastPage();
			updateExternalPaginationUI();
		});

		// Page size selector
		paginationFootPanel.add(new JLabel("Rows per page:"), "cell 8 0,growy");
		paginationFootPanel.add(pageSizeComboBox, "cell 9 0,growy");
		pageSizeComboBox.setSelectedItem(currentPageSize);
		pageSizeComboBox.setToolTipText("Select records per page");
		pageSizeComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				currentPageSize = (Integer) pageSizeComboBox.getSelectedItem();
				studentDataTable.setPageSize(currentPageSize);
				updateExternalPaginationUI();
			}
		});

		// Enable pagination with default page size
		studentDataTable.setPaginationEnabled(true, currentPageSize);

		loadData();
	}

	private void initializeButtonActions() {
		// Search button action
		headPanel.getSearchButton().addActionListener(e -> {
			logger.info("Search button clicked!");
			String searchTerm = headPanel.getSearchField().getText().trim();
			currentFilters.searchTerm = searchTerm;
			currentFilters.filterFirstName = "";
			currentFilters.filterLastName = "";
			currentFilters.filterMiddleName = "";
			loadData();
		});

		// Filter button action
		headPanel.getFilterButtton().addActionListener(e -> {
			logger.info("Filter button clicked!");
			openFilterDialog();
		});

		// Print button action
		headPanel.getPrintBtn().addActionListener(e -> {
			logger.info("Print button clicked!");
			printMULTI_GMC();
		});

		// Back button action
		headPanel.getBackBtn().addActionListener(e -> {
			logger.info("Back button clicked!");
			if (currentDetailPanel != null) {
				slidePane.addSlide(mainContentPanel, SlidePaneTransition.Type.BACK);
				headPanel.getBackBtn().setVisible(false);
				headPanel.getHeaderSearchPanel().setVisible(true);// Show search panel when returning to main view
				// Clear the filter dialog saved state to start fresh when returning to main
				if (savedFilterState != null) {
					savedFilterState.clear();
				}
				FlatAnimatedLafChange.hideSnapshotWithAnimation();
			}
		});

		// Add enter key listener to search field
		headPanel.getSearchField().addActionListener(e -> {
			String searchTerm = headPanel.getSearchField().getText().trim();
			currentFilters.searchTerm = searchTerm;
			currentFilters.filterFirstName = "";
			currentFilters.filterLastName = "";
			currentFilters.filterMiddleName = "";
			loadData();
		});

		// Page size combo box action
		pageSizeComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				currentPageSize = (Integer) pageSizeComboBox.getSelectedItem();
				studentDataTable.setPageSize(currentPageSize);
				updateExternalPaginationUI();
			}
		});
	}

	private void openFilterDialog() {
		// Clear search field and term when opening filter dialog
		headPanel.getSearchField().setText("");
		currentFilters.searchTerm = "";
		FilterDialogPanel filterDialog = new FilterDialogPanel(currentFilters, studentsDataCRUD);
		if (!ModalDialog.isIdExist("student-filter")) {
			StudentFilterModal.getInstance().showModal(studentsDataCRUD, this, currentFilters, filterDialog, 400, 600);
			if (filterDialog.wereFiltersApplied()) {
				loadData();
			}
		}

	}

	public void loadData() {
		try {
			String searchTerm = headPanel.getSearchField().getText().trim();
			List<Student> students = studentsDataCRUD.getStudentsByFilterCriteria(searchTerm,
					currentFilters.filterFirstName, currentFilters.filterLastName, currentFilters.filterMiddleName,
					currentFilters.middleInitialOnly, currentFilters.filterGradeLevel, currentFilters.filterSection,
					currentFilters.filterTrackStrand, currentFilters.filterMale, currentFilters.filterFemale,
					currentFilters.minAge, currentFilters.maxAge);

			// Update table with new data
			updateTableData(students);

			// Update active filters label
			updateActiveFiltersLabel(students.size());

			// Reset to first page after applying filters
			studentDataTable.goToFirstPage();
			updateExternalPaginationUI();
		} catch (SQLException ex) {
			logger.error("Error loading student data: " + ex.getMessage(), ex);
			JOptionPane.showMessageDialog(this, "Error loading student data: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateActiveFiltersLabel(int totalRecords) {
		StringBuilder filterText = new StringBuilder();
		int filterCount = 0;

		// Check each filter and build the label text
		if (!currentFilters.filterFirstName.isEmpty()) {
			filterText.append("First: ").append(currentFilters.filterFirstName).append(" ");
			filterCount++;
		}
		if (!currentFilters.filterLastName.isEmpty()) {
			filterText.append("Last: ").append(currentFilters.filterLastName).append(" ");
			filterCount++;
		}
		if (!currentFilters.filterGradeLevel.equals("All")) {
			filterText.append("Grade: ").append(currentFilters.filterGradeLevel).append(" ");
			filterCount++;
		}
		if (!currentFilters.filterSection.equals("All")) {
			filterText.append("Section: ").append(currentFilters.filterSection).append(" ");
			filterCount++;
		}
		if (!currentFilters.filterTrackStrand.equals("All")) {
			filterText.append("Track: ").append(currentFilters.filterTrackStrand).append(" ");
			filterCount++;
		}
		if (!currentFilters.filterMale || !currentFilters.filterFemale) {
			filterText.append("Sex: ").append(currentFilters.filterMale ? "Male" : "")
					.append(currentFilters.filterFemale ? "Female" : "").append(" ");
			filterCount++;
		}
		if (currentFilters.minAge > 0 || currentFilters.maxAge < Integer.MAX_VALUE) {
			filterText.append("Age: ").append(currentFilters.minAge).append("-").append(currentFilters.maxAge)
					.append(" ");
			filterCount++;
		}

		// Update the label based on filter count and search term
		if (filterCount == 0 && (currentFilters.searchTerm == null || currentFilters.searchTerm.isEmpty())) {
			headPanel.updateActiveFiltersLabel("Showing All: " + totalRecords + " Records");
		} else if (filterCount == 0 && currentFilters.searchTerm != null && !currentFilters.searchTerm.isEmpty()) {
			headPanel.updateActiveFiltersLabel("Search Active | " + totalRecords + " Records");
		} else {
			headPanel.updateActiveFiltersLabel(filterCount + " Active Filters | " + totalRecords + " Records");
		}
	}

	private void printMULTI_GMC() {
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
					logger.error("Error retrieving student data: " + e.getMessage(), e);
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
		// This will show a modal with ScrollPane for both the purpose field and
		// student list
		goodMoralGenarate generator = new goodMoralGenarate();
		goodMoralModalPanel modalPanel = new goodMoralModalPanel(generator);
		modalPanel.setSelectedStudents(selectedStudents);
		modalPanel.setTemplateAndOutput(
				templateManager.getActiveTemplate(templateManager.TemplateType.GOOD_MORAL),
				templateManager.getDefaultOutputFolder()
		);
		modalPanel.showModal(studentsDataCRUD, this, () -> {
			// Success callback, e.g., reload data or show message
		});
	}

	private void updateTableData(List<Student> students) {
		// Clear existing data
		studentDataTable.clearData();

		// Add new data
		int rowNum = 1;
		for (Student student : students) {
			Object[] rowData = { false, // checkbox
					rowNum++, student.getStudentLrn(),
					student.getStudentLastname() + ", " + student.getStudentFirstname()
							+ (student.getStudentMiddlename() != null && !student.getStudentMiddlename().isEmpty()
									? " " + student.getStudentMiddlename().charAt(0) + "."
									: ""),
					student.getStudentSex(),
					student.getSchoolForm().getSF_GRADE_LEVEL() + " - " + student.getSchoolForm().getSF_SECTION(), "" // actions
																														// column
			};
			studentDataTable.addRow(rowData);
		}

		// Update pagination
		updateExternalPaginationUI();
	}

	private void updateExternalPaginationUI() {
		if (studentDataTable == null)
			return;

		int current = studentDataTable.getCurrentPage();
		int total = studentDataTable.getTotalPages();
		int totalRows = studentDataTable.getTotalRows();

		if (totalRows == 0) { // Special handling for no data
			pageInfoLabel.setText("No records");
			firstPageButton.setEnabled(false);
			prevPageButton.setEnabled(false);
			nextPageButton.setEnabled(false);
			lastPageButton.setEnabled(false);
		} else {
			pageInfoLabel.setText("Page " + current + " of " + Math.max(1, total)); // Show "Page 1 of 1" if total is 0
																					// but rows exist (e.g. < pageSize)
			firstPageButton.setEnabled(current > 1);
			prevPageButton.setEnabled(current > 1);
			nextPageButton.setEnabled(current < total);
			lastPageButton.setEnabled(current < total);
		}
	}

	@Override
	public void dispose() {
		try {
			if (savedFilterState != null)
				savedFilterState.clear();
			if (activeFilters != null)
				activeFilters.clear();
			if (studentDataTable != null)
				studentDataTable.clearData();
		} finally {
			super.dispose();
		}
	}
}
