package lyfjshs.gomis.view.students.create;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class GuardianPanel extends JPanel {
    private JTextField guardianNameField;
    private JTextField relationToStudentField;
    private JTextField guardianPhoneField;
    
    public GuardianPanel() {
        setBorder(BorderFactory.createTitledBorder("GUARDIAN'S INFORMATION"));
        setLayout(new MigLayout("wrap 2", "[][grow]", "[]5[]5[]"));
        
        guardianNameField = new JTextField(10);
        relationToStudentField = new JTextField(10);
        guardianPhoneField = new JTextField(10);
        
        // Add components to panel
        add(new JLabel("Name of Guardian:"));
        add(guardianNameField, "growx");
        add(new JLabel("Relation to Student:"));
        add(relationToStudentField, "growx");
        add(new JLabel("Phone Number:"));
        add(guardianPhoneField, "growx");
    }
    
    // Getters for form fields
    public String getGuardianName() { return guardianNameField.getText(); }
    public String getRelationToStudent() { return relationToStudentField.getText(); }
    public String getGuardianPhone() { return guardianPhoneField.getText(); }
}