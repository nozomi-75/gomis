package lyfjshs.gomis.view.students.create;

import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JPanel;

import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class StudentInfoFullForm extends Form {

    private StudentPanel studentPanel;
    private AddressPanel addressPanel;
    private ParentPanel parentPanel;
    private GuardianPanel guardianPanel;
    private Connection connect;
    public StudentInfoFullForm(Connection conn) {
        this.connect = conn;
        setLayout(new MigLayout("wrap 1, fillx, insets 10", "[grow]", "[]10[]10[]10[]10[]"));

        // Create panels
        studentPanel = new StudentPanel();
        addressPanel = new AddressPanel();
        parentPanel = new ParentPanel();
        guardianPanel = new GuardianPanel();

        // Add panels to frame
        add(studentPanel, "growx");
        add(addressPanel, "growx");
        add(parentPanel, "growx");
        add(guardianPanel, "growx");

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            submitForm(this, studentPanel, addressPanel, parentPanel, guardianPanel);
        });

        add(submitButton, "alignx center, width 100");
    }

    private void submitForm(JPanel parentPanel, StudentPanel studentPanel, AddressPanel addressPanel, ParentPanel parentPanel1, GuardianPanel guardianPanel) {
        // Implement your form submission logic here
        // Access data from the panels using their methods
    	// confimation dialog then save into 
    }

}