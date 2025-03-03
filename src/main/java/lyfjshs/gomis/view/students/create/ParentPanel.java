package lyfjshs.gomis.view.students.create;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class ParentPanel extends JPanel {
	private JTextField fatherLastNameField;
    private JTextField fatherFirstNameField;
    private JTextField fatherMiddleNameField;
    private JTextField fatherPhoneNumberField;
    private JTextField motherLastNameField;
    private JTextField motherFirstNameField;
    private JTextField motherMiddleNameField;
    private JTextField motherPhoneNumberField;

    public ParentPanel() {
        setBorder(BorderFactory.createTitledBorder("PARENT'S INFORMATION"));
        setLayout(new MigLayout("wrap 4", "[][grow,fill]15[][grow,fill]", "[]10[]10[]10[]"));

        fatherLastNameField = new JTextField(15);
        fatherFirstNameField = new JTextField(15);
        fatherMiddleNameField = new JTextField(15);
        fatherPhoneNumberField = new JTextField(15);
        motherLastNameField = new JTextField(15);
        motherFirstNameField = new JTextField(15);
        motherMiddleNameField = new JTextField(15);
        motherPhoneNumberField = new JTextField(15);

        // Father's Information
        add(new JLabel("Father's Last Name:"));
        add(fatherLastNameField);
        add(new JLabel("Mother's Last Name:"));
        add(motherLastNameField);

        add(new JLabel("Father's First Name:"));
        add(fatherFirstNameField);
        add(new JLabel("Mother's First Name:"));
        add(motherFirstNameField);

        add(new JLabel("Father's Middle Name:"));
        add(fatherMiddleNameField);
        add(new JLabel("Mother's Middle Name:"));
        add(motherMiddleNameField);

        add(new JLabel("Father's Contact Number:"));
        add(fatherPhoneNumberField);
        add(new JLabel("Mother's Contact Number:"));
        add(motherPhoneNumberField);
    }
    
    // Getters for form fields
    public String getFatherLastName() { return fatherLastNameField.getText(); }
    public String getFatherFirstName() { return fatherFirstNameField.getText(); }
    public String getFatherMiddleName() { return fatherMiddleNameField.getText(); }
    public String getFatherPhoneNumber() { return fatherPhoneNumberField.getText(); }
    
    public String getMotherLastName() { return motherLastNameField.getText(); }
    public String getMotherFirstName() { return motherFirstNameField.getText(); }
    public String getMotherMiddleName() { return motherMiddleNameField.getText(); }
    public String getMotherPhoneNumber() { return motherPhoneNumberField.getText(); }
}
