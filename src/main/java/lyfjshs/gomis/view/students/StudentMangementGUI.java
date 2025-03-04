package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;
import raven.modal.ModalDialog;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class StudentMangementGUI extends Form {

	private static final long serialVersionUID = 1L;
	private JTable studentDataTable;
	private StudentsDataDAO studentsDataCRUD;
	private final Connection connection;
	private final String[] columnNames = { "#", "LRN", "NAME", "SEX", "Actions" };
	private SlidePane slidePane;
	private JButton backBtn;
	private JPanel mainPanel;
	private Component currentDetailPanel;
	private JPanel headerSearchPanel;

	public StudentMangementGUI(Connection conn) {
		this.connection = conn;
		studentsDataCRUD = new StudentsDataDAO(conn);
		initializeComponents();
		setupLayout();
		loadStudentData();
	}

	private void initializeComponents() {
		// Initialize table
		DefaultTableModel model = new DefaultTableModel(null, columnNames);
		studentDataTable = new JTable(model);
		setupTable();

		// Initialize SlidePane
		slidePane = new SlidePane();
		slidePane.setOpaque(true);

		// Create main panel that will contain the table
		mainPanel = createStudentTablePanel();

		// Initialize back button
		backBtn = new JButton("Back");
		backBtn.setVisible(false);
		backBtn.addActionListener(e -> {
			FlatAnimatedLafChange.showSnapshot();

			// Remove the current detail panel and show the main panel
			slidePane.addSlide(mainPanel, SlidePaneTransition.Type.BACK);
			currentDetailPanel = null;

			backBtn.setVisible(false);
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		});

		// Add the main panel as the first slide
		slidePane.addSlide(mainPanel, SlidePaneTransition.Type.FORWARD);
	}

	private void setupTable() {
		studentDataTable.setShowVerticalLines(false);
		studentDataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studentDataTable.setRowHeight(30);
		studentDataTable.setFont(new Font("Tahoma", Font.PLAIN, 14));
		studentDataTable.setShowGrid(false);

		// Set column widths
		studentDataTable.getColumnModel().getColumn(0).setPreferredWidth(50); // #
		studentDataTable.getColumnModel().getColumn(1).setPreferredWidth(100); // LRN
		studentDataTable.getColumnModel().getColumn(2).setPreferredWidth(150); // NAME
		studentDataTable.getColumnModel().getColumn(3).setPreferredWidth(100); // SEX
		studentDataTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Actions
		studentDataTable.getColumnModel().getColumn(4).setResizable(false);

		// Setup table actions
		setupTableActions();
	}

	private void setupTableActions() {
		TableActionManager actionManager = new TableActionManager();
		actionManager.addAction("View", (table, row) -> {
			String lrn = (String) table.getValueAt(row, 1);
			FlatAnimatedLafChange.showSnapshot();

			try {
				Student studentData = studentsDataCRUD.getStudentDataByLrn(lrn);
				if (studentData != null) {
					// Create and show the detail panel with retrieved data
					currentDetailPanel = new StudentFullData(connection, studentData);
					slidePane.addSlide(currentDetailPanel, SlidePaneTransition.Type.FORWARD);
					backBtn.setVisible(true);
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

		actionManager.applyTo(studentDataTable, 4);
	}

	private void setupLayout() {
		setLayout(new BorderLayout());

		// Create header panel
		JPanel headerPanel = new JPanel(new MigLayout("", "[][][][grow][][]", "[]"));
		JLabel headerLabel = new JLabel("STUDENT DATA");
		headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

		headerPanel.add(headerLabel, "cell 1 0,alignx center,growy");

		headerSearchPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		JButton searchButton = createSearchButton("Search Student", " ");
		searchButton.addActionListener(e -> showSearchPanel());
		headerSearchPanel.add(searchButton, "cell 0 0,grow");

		headerPanel.add(headerSearchPanel, "flowx,cell 3 0,grow");
		headerPanel.add(backBtn, "cell 5 0");
		add(headerPanel, BorderLayout.NORTH);
		add(slidePane, BorderLayout.CENTER);
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
		ModalDialog.showModal(this, new StudentSearchPanel(connection), option, "search");
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
			DefaultTableModel model = (DefaultTableModel) studentDataTable.getModel();
			model.setRowCount(0);

			int rowNum = 1;
			for (Student studentData : studentsDataList) {
				String fullName = studentData.getStudentFirstname() + " " + studentData.getStudentLastname();
				model.addRow(new Object[] { rowNum++, studentData.getStudentLrn(), fullName,
						studentData.getStudentSex(), "View" });
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

}