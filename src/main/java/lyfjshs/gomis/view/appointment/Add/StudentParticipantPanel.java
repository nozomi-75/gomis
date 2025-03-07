package lyfjshs.gomis.view.appointment.Add;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class StudentParticipantPanel extends JPanel {
    private JTextField lrnField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JComboBox<String> sexComboBox;

    public StudentParticipantPanel() {
        setLayout(new MigLayout("wrap 2", "[right][grow]", "[][][][]"));
        setBorder(BorderFactory.createTitledBorder("Student Participant"));

        add(new JLabel("LRN:"));
        lrnField = new JTextField();
        add(lrnField, "growx");

        add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        add(firstNameField, "growx");

        add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        add(lastNameField, "growx");

        add(new JLabel("Sex:"));
        sexComboBox = new JComboBox<>(new String[]{"Male", "Female"});
        add(sexComboBox, "growx");
    }

    public JTextField getLrnField() {
        return lrnField;
    }

    public JTextField getFirstNameField() {
        return firstNameField;
    }

    public JTextField getLastNameField() {
        return lastNameField;
    }

    public JComboBox<String> getSexComboBox() {
        return sexComboBox;
    }
}
