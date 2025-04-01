package lyfjshs.gomis.view.students;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.Database.entity.Violation;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
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
    private final String[] columnNames = {"#", "LRN", "NAME", "SEX", "Actions"};
    private SlidePane slidePane;
    private JButton backBtn;
    private JPanel mainPanel;
    private Component currentDetailPanel;
    private JPanel headerSearchPanel;
    private ViolationDAO violationDAO;

    public StudentMangementGUI(Connection conn) {
        this.connection = conn;
        studentsDataCRUD = new StudentsDataDAO(conn);
        this.violationDAO = new ViolationDAO(conn);
        initializeComponents();
        setupLayout();
        loadStudentData();
    }

    private void initializeComponents() {
        setupTable();

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

    private void setupTable() {
        Object[][] initialData = new Object[0][columnNames.length];
        Class<?>[] columnTypes = {Boolean.class, Integer.class, String.class, String.class, String.class, Object.class};
        boolean[] editableColumns = {true, false, false, false, false, true};
        
        // Adjusted column widths to match the image proportions
        double[] columnWidths = {0.05, 0.05, 0.20, 0.45, 0.10, 0.15}; // Sum to 1.0
        int[] alignments = {
            SwingConstants.CENTER,  // Checkbox
            SwingConstants.CENTER,  // #
            SwingConstants.CENTER,  // LRN
            SwingConstants.LEFT,    // NAME
            SwingConstants.CENTER,  // SEX
            SwingConstants.CENTER   // Actions
        };
        boolean includeCheckbox = true;

        TableActionManager actionManager = new DefaultTableActionManager();
        ((DefaultTableActionManager)actionManager).addAction("View", (table, row) -> {
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

        studentDataTable = new GTable(
            initialData,
            new String[]{" ", "#", "LRN", "NAME", "SEX", "Actions"},
            columnTypes,
            editableColumns,
            columnWidths,
            alignments,
            includeCheckbox,
            actionManager
        );
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

        JButton printGoodMoralButton = new JButton("Print Good Moral Certificate");
        printGoodMoralButton.addActionListener(e -> handlePrintGoodMoral());
        headerSearchPanel.add(printGoodMoralButton, "cell 1 0,gapleft 10");

        headerPanel.add(headerSearchPanel, "flowx,cell 3 0,grow");
        headerPanel.add(backBtn, "cell 5 0");
        add(headerPanel, BorderLayout.NORTH);
        add(slidePane, BorderLayout.CENTER);
    }

    public static JButton createSearchButton(String text, String rightTxt) {
        JButton button = new JButton(text, new FlatSVGIcon("icons/search.svg", 0.4f));
        button.setLayout(new MigLayout("insets 0,al trailing,filly", "", "[center]"));
        button.setHorizontalAlignment(JButton.LEADING);
        button.putClientProperty(FlatClientProperties.STYLE, "" +
            "margin:5,7,5,10;" +
            "arc:10;" +
            "borderWidth:0;" +
            "focusWidth:0;" +
            "innerFocusWidth:0;" +
            "[light]background:shade($Panel.background,10%);" +
            "[dark]background:tint($Panel.background,10%);" +
            "[light]foreground:tint($Button.foreground,40%);" +
            "[dark]foreground:shade($Button.foreground,30%);");
        JLabel label = new JLabel(rightTxt);
        label.putClientProperty(FlatClientProperties.STYLE, "" +
            "[light]foreground:tint($Button.foreground,40%);" +
            "[dark]foreground:shade($Button.foreground,30%);");
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
            DefaultTableModel model = (DefaultTableModel) studentDataTable.getModel();
            model.setRowCount(0);

            int rowNum = 1;
            for (Student studentData : studentsDataList) {
                String fullName = studentData.getStudentFirstname() + " " + studentData.getStudentLastname();
                model.addRow(new Object[] {
                    false,  // Checkbox column
                    rowNum++,
                    studentData.getStudentLrn(),
                    fullName,
                    studentData.getStudentSex(),
                    null // Actions column handled by TableActionManager
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading student data: " + e.getMessage(), "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handlePrintGoodMoral() {
        List<Student> selectedStudents = new ArrayList<>();
        for (int i = 0; i < studentDataTable.getRowCount(); i++) {
            Boolean isSelected = (Boolean) studentDataTable.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                String lrn = (String) studentDataTable.getValueAt(i, 2);
                try {
                    Student student = studentsDataCRUD.getStudentDataByLrn(lrn);
                    if (student != null) {
                        selectedStudents.add(student);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (selectedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one student.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check for active violations
        List<Student> studentsWithViolations = new ArrayList<>();
        for (Student student : selectedStudents) {
            try {
                List<Violation> violations = violationDAO.getViolationsByStudentId(student.getStudentId());
                boolean hasActiveViolation = violations.stream()
                        .anyMatch(v -> v.getStatus().equals("Active"));
                if (hasActiveViolation) {
                    studentsWithViolations.add(student);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (!studentsWithViolations.isEmpty()) {
            StringBuilder message = new StringBuilder("The following students have active violations and cannot receive a good moral certificate:\n\n");
            for (Student student : studentsWithViolations) {
                message.append("- ").append(student.getStudentFirstname())
                       .append(" ").append(student.getStudentLastname())
                       .append(" (LRN: ").append(student.getStudentLrn()).append(")\n");
            }
            message.append("\nPlease remove these students from the selection to proceed.");
            
            JOptionPane.showMessageDialog(this, message.toString(), "Active Violations Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // If no violations, proceed with printing
        GoodMoralGenerator.createBatchGoodMoralReport(this, selectedStudents);
    }
}