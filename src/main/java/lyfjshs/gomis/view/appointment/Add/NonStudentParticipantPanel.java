package lyfjshs.gomis.view.appointment.Add;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class NonStudentParticipantPanel extends JPanel {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField contactField;
    private JTextField emailField;

    public NonStudentParticipantPanel() {
        setLayout(new MigLayout("wrap 2", "[right][grow]", "[][][][]"));
        setBorder(BorderFactory.createTitledBorder("Non-Student Participant"));

        add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        add(firstNameField, "growx");

        add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        add(lastNameField, "growx");

        add(new JLabel("Contact Number:"));
        contactField = new JTextField();
        add(contactField, "growx");

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField, "growx");
    }

    public JTextField getFirstNameField() {
        return firstNameField;
    }

    public JTextField getLastNameField() {
        return lastNameField;
    }

    public JTextField getContactField() {
        return contactField;
    }

    public JTextField getEmailField() {
        return emailField;
    }
}
