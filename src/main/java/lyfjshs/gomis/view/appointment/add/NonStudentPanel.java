package lyfjshs.gomis.view.appointment.add;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class NonStudentPanel extends JPanel {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField contactField;
    private JComboBox<String> sexComboBox;
    private NonStudentListener listener;

    public interface NonStudentListener {
        void onNonStudentAdded(TempParticipant participant);
    }

    public NonStudentPanel() {
        setLayout(new MigLayout("wrap 2, fillx, insets 10", "[][grow]", "[]10[]10[]10[]10[]"));
        
        // Initialize components
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        contactField = new JTextField(20);
        sexComboBox = new JComboBox<>(new String[]{"Male", "Female"});

        // Add components
        add(new JLabel("First Name:*"), "right");
        add(firstNameField, "growx");
        
        add(new JLabel("Last Name:*"), "right");
        add(lastNameField, "growx");
        
        add(new JLabel("Sex:*"), "right");
        add(sexComboBox, "growx");
        
        add(new JLabel("Contact Number:*"), "right");
        add(contactField, "growx");

        // Add button
        JButton addButton = new JButton("Add Participant");
        addButton.addActionListener(e -> addNonStudent());
        add(addButton, "span 2, center");
    }

    public void setNonStudentListener(NonStudentListener listener) {
        this.listener = listener;
    }

    private void addNonStudent() {
        if (validateFields()) {
            TempParticipant participant = new TempParticipant(
                null, // participantId (new non-student)
                null, // studentUid is null for non-students
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                "Non-Student",
                (String) sexComboBox.getSelectedItem(),
                contactField.getText().trim(),
                false, // isStudent is false for non-students
                false, // isViolator (default for new non-student)
                false // isReporter (default for new non-student)
            );
            
            if (listener != null) {
                listener.onNonStudentAdded(participant);
            }
            
            // Clear fields
            clearFields();
        }
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        contactField.setText("");
        sexComboBox.setSelectedIndex(0);
    }

    private boolean validateFields() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String contact = contactField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all required fields.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate contact number format
        if (!contact.matches("\\d{11}|\\d{3}-\\d{4}|\\d{4}-\\d{4}")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid contact number format:\n" +
                "- 11 digits (e.g., 09123456789)\n" +
                "- XXX-XXXX\n" +
                "- XXXX-XXXX",
                "Invalid Contact Number",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
} 