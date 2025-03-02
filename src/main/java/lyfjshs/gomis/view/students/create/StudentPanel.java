package lyfjshs.gomis.view.students.create;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class StudentPanel extends JPanel {
    private JTextField lrnField;
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JComboBox<String> sexComboBox;
    private JSpinner birthDateSpinner;
    private JTextField ageField;
    private JTextField birthPlaceField;

    public StudentPanel() {
        setBorder(BorderFactory.createTitledBorder("STUDENT INFORMATION"));
        setLayout(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));

        lrnField = new JTextField(15);
        lastNameField = new JTextField(15);
        firstNameField = new JTextField(15);
        middleNameField = new JTextField(15);
        sexComboBox = new JComboBox<>(new String[]{"Male", "Female"});

        birthDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(birthDateSpinner, "yyyy-MM-dd");
        birthDateSpinner.setEditor(editor);

        ageField = new JTextField(5);
        birthPlaceField = new JTextField(20);

        // Restrict LRN to digits only
        lrnField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Arrange Components
        add(new JLabel("LRN:"));
        add(lrnField);
        add(new JLabel("Sex:"), "gapleft 15");
        add(sexComboBox);

        add(new JLabel("Last Name:"));
        add(lastNameField);
        add(new JLabel("First Name:"), "gapleft 15");
        add(firstNameField);

        add(new JLabel("Middle Name:"));
        add(middleNameField);
        add(new JLabel("Birthdate:"), "gapleft 15");
        add(birthDateSpinner);

        add(new JLabel("Age:"));
        add(ageField);
        add(new JLabel("Birthplace:"), "gapleft 15");
        add(birthPlaceField, "span");

        // Future Age Calculation Integration (Uncomment when implemented)
        // ageField.setText(StudentInfoFullForm.calculateAge((Date) birthDateSpinner.getValue()));
        // birthDateSpinner.addChangeListener(e -> {
        //    ageField.setText(StudentInfoFullForm.calculateAge((Date) birthDateSpinner.getValue()));
        // });
    }

    // Getters for form fields
    public String getLrn() {
        return lrnField.getText();
    }

    public String getLastName() {
        return lastNameField.getText();
    }

    public String getFirstName() {
        return firstNameField.getText();
    }

    public String getMiddleName() {
        return middleNameField.getText();
    }

    public String getSex() {
        return (String) sexComboBox.getSelectedItem();
    }

    public Date getBirthDate() {
        return (Date) birthDateSpinner.getValue();
    }

    public String getAge() {
        return ageField.getText();
    }

    public String getBirthPlace() {
        return birthPlaceField.getText();
    }
}
