package lyfjshs.gomis.test;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.sql.Date;
import java.sql.Timestamp;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.model.StudentsData;
import lyfjshs.gomis.Database.model.StudentsRecord;
import lyfjshs.gomis.view.students.StudentFullData;
import net.miginfocom.swing.MigLayout;
import raven.extras.LightDarkButton;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;
import java.awt.event.ActionEvent;

public class TestModal extends JFrame {

    public TestModal() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(800, 800));
        setLocationRelativeTo(null);
        setLayout(new MigLayout("wrap,al center center"));
        JButton button = new JButton("show");
        // Sample objects of StudentsRecord and StudentsData
        StudentsRecord studentRecord = new StudentsRecord(
            1, // studentRecordId
            101, // studentUid
            5, // violationId
            "Regular", // typeOfStudent
            "2023-2024", // academicYear
            "1st Semester", // semester
            "STEM", // strand
            "Science", // track
            "Grade 12", // yearLevel
            "Mr. Smith", // adviser
            "Section A", // section
            "Active", // status
            new Timestamp(System.currentTimeMillis()) // updatedAt
        );

        StudentsData studentData = new StudentsData(
            101, // studentUid
            "123456789", // lrn
            "Doe", // LAST_NAME
            "John", // FIRST_NAME
            "A", // middleInitial
            "", // suffix
            "Male", // gender
            Date.valueOf("2005-05-15"), // dob
            "john.doe@example.com", // email
            "123-456-7890", // contactNumber
            "Jane Doe", // guardianName
            "jane.doe@example.com", // guardianEmail
            "098-765-4321", // guardianContactNumber
            "123 Main St" // address
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

            SimpleModalBorder modal = new SimpleModalBorder(new StudentFullData(studentData, studentRecord), "Input", customOptions, (controller, action) -> {
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
