package lyfjshs.gomis.view.students.create;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class StudentPanel extends JPanel {
    private JTextField lrnField;
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField middleNameField;
    private JComboBox<String> sexComboBox;
    private DatePicker birthDatePicker;
    private JFormattedTextField birthDateEditor;
    private JTextField ageField;
    private JTextField birthPlaceField;

    public StudentPanel() {
        setBorder(new TitledBorder("STUDENT INFORMATION"));
        setLayout(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));

        lrnField = new JTextField(15);
        lastNameField = new JTextField(15);
        firstNameField = new JTextField(15);
        middleNameField = new JTextField(15);
        sexComboBox = new JComboBox<>(new String[]{"Male", "Female"});

        // Raven DatePicker with JFormattedTextField as Editor
        birthDatePicker = new DatePicker();
        birthDatePicker.setDateSelectionMode(DatePicker.DateSelectionMode.SINGLE_DATE_SELECTED);
        birthDatePicker.setDateFormat("yyyy-MM-dd");

        birthDateEditor = new JFormattedTextField();
        birthDatePicker.setEditor(birthDateEditor);

        // Age Field (Read-Only)
        ageField = new JTextField(5);
        ageField.setEditable(false);

        birthPlaceField = new JTextField(20);

        // Restrict LRN to digits only
        lrnField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });

        // Auto-update age when selecting birthdate
        birthDatePicker.addDateSelectionListener(dateEvent -> updateAgeField());

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
        add(birthDateEditor); // Use the editor instead of the DatePicker component

        add(new JLabel("Age:"));
        add(ageField);
        add(new JLabel("Birthplace:"), "gapleft 15");
        add(birthPlaceField, "span");
    }

    // Calculate Age Based on Selected Birthdate
    private void updateAgeField() {
        LocalDate selectedDate = birthDatePicker.getSelectedDate();
        if (selectedDate != null) {
            int age = Period.between(selectedDate, LocalDate.now()).getYears();
            ageField.setText(String.valueOf(age));
        } else {
            ageField.setText("");
        }
    }

}
