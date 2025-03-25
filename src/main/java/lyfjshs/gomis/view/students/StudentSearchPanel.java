package lyfjshs.gomis.view.students;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.ModalDialog;
import raven.modal.component.Modal;

public abstract class StudentSearchPanel extends Modal {
    private JPanel advancedPanel;
    private JTextField firstNameField = new JTextField(20);
    private JTextField middleNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    private JFormattedTextField dobField;
    private DatePicker datePicker;
    protected final Connection connection;
    private JPanel panelResult;
    private Student selectedStudent; // Add this field to store selected student
    private List<StudentResult> resultPanels = new ArrayList<>();
    private StudentSelectionCallback callback;

    public StudentSearchPanel(Connection connection) {
        this(connection, null);
    }

    /**
     * @wbp.parser.constructor
     */
    public StudentSearchPanel(Connection connection, StudentSelectionCallback callback) {
        this.connection = connection;
        this.callback = callback;
        setLayout(new MigLayout("fillx,insets 0,wrap", "[500,grow,fill][]", "[][][pref!][][100px,grow]"));
        JTextField textSearch = new JTextField();
        panelResult = new JPanel(new MigLayout("insets 3 10 3 10,fillx,wrap", "[fill]"));
        textSearch.putClientProperty("JTextField.placeholderText", "Enter LRN");
        add(textSearch, "flowx,cell 0 0,grow");

        JButton btnNewButton = new JButton(new FlatSVGIcon("icons/search.svg", 0.4f));
        btnNewButton.putClientProperty(FlatClientProperties.STYLE,
                "margin:5,7,5,10;" +
                "arc:10;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0;" +
                "[light]background:shade($Panel.background,10%);" +
                "[dark]background:tint($Panel.background,10%);" +
                "[light]foreground:tint($Button.foreground,40%);" +
                "[dark]foreground:shade($Button.foreground,30%);");
        add(btnNewButton, "cell 1 0");
        add(new JSeparator(), "cell 0 2 2 1,height 2!");

        advancedPanel = new JPanel(new MigLayout("insets 5", "[100px][200px][][30px]", "[][][][]"));
        advancedPanel.setBorder(new TitledBorder(null, "Advanced Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        advancedPanel.add(new JLabel("First Name:"), "cell 0 0");
        advancedPanel.add(firstNameField, "cell 1 0");
        advancedPanel.add(new JLabel("Gender:"), "flowx,cell 3 0");
        advancedPanel.add(new JLabel("Middle Name:"), "cell 0 1");
        advancedPanel.add(middleNameField, "cell 1 1");
        advancedPanel.add(new JLabel("Date of Birth:"), "flowx,cell 3 1");
        advancedPanel.add(new JLabel("Last Name:"), "cell 0 2");
        advancedPanel.add(lastNameField, "cell 1 2");

        datePicker = new DatePicker();
        add(advancedPanel, "cell 0 1 2 1,alignx center,growy");
        JComboBox<String> genderBox = new JComboBox<>(new String[] { "Male", "Female" });
        advancedPanel.add(genderBox, "cell 3 0");

        dobField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        dobField.setColumns(10);
        datePicker.setEditor(dobField);
        advancedPanel.add(dobField, "cell 3 1");

        JButton searchByNameButton = new JButton("Search by Name and Gender");
        advancedPanel.add(searchByNameButton, "cell 3 2");

        JLabel lblNewLabel = new JLabel("Results: ");
        add(lblNewLabel, "cell 0 3");

        JScrollPane scrollPane = new JScrollPane(panelResult);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "cell 0 4 2 1,grow");

        // Add DocumentListener for automatic LRN search
        textSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkAndSearchLRN(textSearch.getText().trim());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkAndSearchLRN(textSearch.getText().trim());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkAndSearchLRN(textSearch.getText().trim());
            }
        });

        // Keep the manual search button functionality
        btnNewButton.addActionListener(e -> searchLRN(textSearch.getText().trim()));

        searchByNameButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String sex = (String) genderBox.getSelectedItem();
            if (!firstName.isEmpty() || !middleName.isEmpty() || !lastName.isEmpty()) {
                searchByNameAndGender(firstName, middleName, lastName, sex);
            } else {
                JOptionPane.showMessageDialog(StudentSearchPanel.this, "Please enter at least one name field",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    // Add getter method for selected student
    public Student getSelectedStudent() {
        return selectedStudent;
    }

    private void checkAndSearchLRN(String lrn) {
        if (lrn.length() == 7) { // Trigger search when 7 digits are typed
            searchLRN(lrn);
        } else if (lrn.length() < 7) {
            panelResult.removeAll(); // Clear results if less than 7 digits
            panelResult.revalidate();
            panelResult.repaint();
        }
    }

    private void searchLRN(String lrn) {
        if (!lrn.isEmpty()) {
            StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
            try {
                List<Student> students = studentsDataDAO.getStudentsByFilters(lrn, null, null, null);
                panelResult.removeAll();
                resultPanels.clear(); // Clear stored result panels

                if (students != null && !students.isEmpty()) {
                    for (Student student : students) {
                        StudentResult resultPanel = new StudentResult(
                            student.getStudentFirstname() + " " + student.getStudentLastname(),
                            student.getStudentLrn()
                        );
                        setupResultPanel(student, resultPanel);
                        panelResult.add(resultPanel);
                    }
                } else {
                    showNoResultsMessage(lrn);
                }
                panelResult.revalidate();
                panelResult.repaint();
            } catch (SQLException e) {
                handleSearchError(e);
            }
        }
    }

    private void searchByNameAndGender(String firstName, String middleName, String lastName, String sex) {
        StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
        try {
            List<Student> students = studentsDataDAO.getStudentsByFilters(null, firstName, lastName, sex);
            panelResult.removeAll();
            resultPanels.clear(); // Clear stored result panels

            if (!students.isEmpty()) {
                for (Student student : students) {
                    StudentResult resultPanel = new StudentResult(
                        student.getStudentFirstname() + " " + student.getStudentLastname(),
                        student.getStudentLrn()
                    );
                    setupResultPanel(student, resultPanel);
                    panelResult.add(resultPanel);
                }
            } else {
                showNoResultsMessage("");
            }
            panelResult.revalidate();
            panelResult.repaint();
        } catch (SQLException e) {
            handleSearchError(e);
        }
    }

    private void showNoResultsMessage(String searchTerm) {
        String message = searchTerm.isEmpty() ? 
            "No students found" : 
            "No students found with LRN prefix: " + searchTerm;
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSearchError(SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Error searching students: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    // Add this method to clear all selections
    private void clearSelections() {
        for (StudentResult panel : resultPanels) {
            panel.setSelected(false);
        }
    }

    private void setupResultPanel(Student student, StudentResult resultPanel) {
        resultPanels.add(resultPanel);
        resultPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearSelections();
                resultPanel.setSelected(true);
                selectedStudent = student;
                if (callback != null) {
                    callback.onStudentSelected(student);
                } else {
                    onStudentSelected(student);
                }
                ModalDialog.closeModal("search");
            }
        });
    }

    // Remove or modify displayStudentFullData to not show the full data dialog
    private void displayStudentFullData(Student student) {
        this.selectedStudent = student;
        // Remove the StudentFullData dialog display since we just want to store the selection
    }

    // This method will be called when a student is selected from search results
    protected void onStudentSelected(Student student) {
        // Default empty implementation
    }

    // Add this interface inside the StudentSearchPanel class
    public interface StudentSelectionCallback {
        void onStudentSelected(Student student);
    }
}