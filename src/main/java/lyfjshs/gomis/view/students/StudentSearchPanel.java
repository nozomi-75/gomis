package lyfjshs.gomis.view.students;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import lyfjshs.gomis.Database.DAO.StudentsDataDAO;
import lyfjshs.gomis.Database.entity.Student;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import raven.modal.component.Modal;

public class StudentSearchPanel extends Modal {
    private JPanel advancedPanel;
    private JButton toggleButton;

    // Static text fields
    private JTextField firstNameField = new JTextField(20);
    private JTextField middleNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JTextField addressField = new JTextField(20);
    
    // Date Picker
    private JFormattedTextField dobField;
    private DatePicker datePicker;
    private Connection connection;
    private JPanel panelResult; // Made this a class field for easier access

    public StudentSearchPanel(Connection connection) {
        this.connection = connection;
        setLayout(new MigLayout("fillx,insets 0,wrap", "[500,grow,fill][]", "[][][][][100px,grow][]"));
        JTextField textSearch = new JTextField();
        panelResult = new JPanel(new MigLayout("insets 3 10 3 10,fillx,wrap", "[fill]"));
        textSearch.putClientProperty("JTextField.placeholderText", "Enter LRN");
        add(textSearch, "flowx,cell 0 0,grow");
        
        JButton btnNewButton = new JButton(new FlatSVGIcon("icons/search.svg", 0.4f));
        btnNewButton.putClientProperty(FlatClientProperties.STYLE, "" + "margin:5,7,5,10;" + "arc:10;" + "borderWidth:0;"
                + "focusWidth:0;" + "innerFocusWidth:0;" + "[light]background:shade($Panel.background,10%);"
                + "[dark]background:tint($Panel.background,10%);" + "[light]foreground:tint($Button.foreground,40%);"
                + "[dark]foreground:shade($Button.foreground,30%);");
        add(btnNewButton, "cell 1 0");
        add(new JSeparator(), "cell 0 1 2 1,height 2!");

        // Advanced Search Panel (Initially Hidden)
        advancedPanel = new JPanel(new MigLayout("insets 5", "[100px][200px][][30px]", "[][][][][]"));
        advancedPanel.setBorder(new TitledBorder(null, "Advanced Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        advancedPanel.setVisible(false); // Initially hidden

        // Add static fields directly
        advancedPanel.add(new JLabel("First Name:"), "cell 0 0");
        advancedPanel.add(firstNameField, "cell 1 0");
        
        // Gender Dropdown
        advancedPanel.add(new JLabel("Gender:"), "cell 3 0");
        advancedPanel.add(new JLabel("Middle Name:"), "cell 0 1");
        advancedPanel.add(middleNameField, "cell 1 1");
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        advancedPanel.add(genderBox, "cell 3 1");
        advancedPanel.add(new JLabel("Last Name:"), "cell 0 2");
        advancedPanel.add(lastNameField, "cell 1 2");
        advancedPanel.add(new JLabel("Date of Birth:"), "cell 3 2");

        // Implement Date Picker
        dobField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        dobField.setColumns(10);
        datePicker = new DatePicker();
        datePicker.setEditor(dobField); // Set the JFormattedTextField as the editor
        advancedPanel.add(dobField, "cell 3 3");

        advancedPanel.add(new JLabel("Email:"), "cell 0 3");
        advancedPanel.add(emailField, "cell 1 3");
        advancedPanel.add(new JLabel("Address:"), "cell 0 4");
        advancedPanel.add(addressField, "cell 1 4");

        // Toggle Button
        toggleButton = new JButton("Show Advanced Search");
        toggleButton.addActionListener(e -> {
            boolean isVisible = advancedPanel.isVisible();
            advancedPanel.setVisible(!isVisible);
            toggleButton.setText(isVisible ? "Show Advanced Search" : "Hide Advanced Search");
        });

        add(toggleButton, "cell 0 2 2 1,alignx right");
        add(advancedPanel, "cell 0 3 2 1,alignx center,growy");

        JScrollPane scrollPane = new JScrollPane(panelResult);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "cell 0 4 2 1,grow");

        btnNewButton.addActionListener(e -> {
            String lrn = textSearch.getText().trim();
            if (!lrn.isEmpty()) {
                StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
                try {
                    Student student = studentsDataDAO.getStudentDataByLrn(lrn);
                    if (student != null) {
                        panelResult.removeAll(); // Clear previous results
                        StudentResult resultPanel = new StudentResult(
                            student.getStudentFirstname() + " " + student.getStudentLastname(),
                            student.getStudentLrn()
                        );
                        resultPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                displayStudentFullData(student);
                            }
                        });
                        panelResult.add(resultPanel);
                        panelResult.revalidate();
                        panelResult.repaint();
                    } else {
                        JOptionPane.showMessageDialog(StudentSearchPanel.this, "No student found with LRN: " + lrn, "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(StudentSearchPanel.this, "Error retrieving student data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(StudentSearchPanel.this, "Please enter LRN", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add a new button for searching by name and sex
        JButton searchByNameButton = new JButton("Search by Name and Sex");
        searchByNameButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String middleName = middleNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String sex = (String) genderBox.getSelectedItem();

            if (!firstName.isEmpty() || !middleName.isEmpty() || !lastName.isEmpty()) {
                StudentsDataDAO studentsDataDAO = new StudentsDataDAO(connection);
                try {
                    List<Student> students = studentsDataDAO.getStudentDataByNameAndSex(firstName, middleName, lastName, sex);
                    panelResult.removeAll(); // Clear previous results
                    if (!students.isEmpty()) {
                        for (Student student : students) {
                            StudentResult resultPanel = new StudentResult(
                                student.getStudentFirstname() + " " + student.getStudentLastname(),
                                student.getStudentLrn()
                            );
                            resultPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                                public void mouseClicked(java.awt.event.MouseEvent evt) {
                                    displayStudentFullData(student);
                                }
                            });
                            panelResult.add(resultPanel);
                        }
                        panelResult.revalidate();
                        panelResult.repaint();
                    } else {
                        JOptionPane.showMessageDialog(StudentSearchPanel.this, "No students found", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(StudentSearchPanel.this, "Error retrieving student data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(StudentSearchPanel.this, "Please enter at least one name field", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
        add(searchByNameButton, "cell 0 5 2 1,alignx right");
    }

    private void displayStudentFullData(Student student) {
        // Create and display the StudentFullData panel in a new modal or frame
        StudentFullData studentFullData = new StudentFullData(connection, student);
  
    }
}