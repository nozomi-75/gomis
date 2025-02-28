package lyfjshs.gomis.test;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.model.StudentsData;
import lyfjshs.gomis.view.students.StudentFullData;
import net.miginfocom.swing.MigLayout;
import raven.extras.LightDarkButton;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;

public class TestModal extends JFrame {

    public TestModal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(800, 800));
        setLocationRelativeTo(null);
        setLayout(new MigLayout("wrap,al center center"));
        JButton button = new JButton("show");

        StudentsData studentData = new StudentsData(
            1, // studentUid
            1, // parentID
            1, // guardianID
            1, // appointmentID
            1, // contactID
            "LRN123456790", // lrn
            "Doe", // lastName
            "John", // firstName
            "Smith", // middleName
            "john.doe@example.com", // email
            new java.sql.Date(new java.util.Date().getTime()), // birthDate
            "123 Main St", // address
            1234567890, // phoneNumber
            "Male", // gender
            "Single" // civilStatus
        );

        ModalDialog.getDefaultOption()
                .setOpacity(0f)
                .setAnimationOnClose(false)
                .getBorderOption()
                .setBorderWidth(0.5f)
                .setShadow(BorderOption.Shadow.MEDIUM);

        button.addActionListener(e -> {
            // Create custom options
            SimpleModalBorder.Option customOption = new SimpleModalBorder.Option("Custom Action", 3); // Custom action type
            SimpleModalBorder.Option[] customOptions = new SimpleModalBorder.Option[] { customOption };

            SimpleModalBorder modal = new SimpleModalBorder(new StudentFullData(studentData), "Input", customOptions, (controller, action) -> {
                System.out.println("Action: " + action);
                if (action == 3) { // Check for custom action
                    // Handle custom action here
                    System.out.println("Custom action performed!");
                    controller.consume(); // Prevent closing the modal if needed
                } else if (action == SimpleModalBorder.YES_OPTION) {
                    controller.consume();
                    ModalDialog.pushModal(new SimpleModalBorder(new Test1(), "New Input", SimpleModalBorder.YES_NO_OPTION, (controller1, action1) -> {
                    }), "input");
                }
            });
            
            // Set the size of the modal
            modal.setPreferredSize(new Dimension(this.getWidth() - 200 , this.getHeight() - 100)); // Set your desired width and height

            ModalDialog.showModal(this, modal, "input");
        });
        add(button);
        LightDarkButton lightDarkButton = new LightDarkButton();
        lightDarkButton.installAutoLafChangeListener();
        add(lightDarkButton);
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        FlatMacLightLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        EventQueue.invokeLater(() -> new TestModal().setVisible(true));
    }
}
