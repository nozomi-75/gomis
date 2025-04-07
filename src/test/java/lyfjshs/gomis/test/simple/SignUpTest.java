package lyfjshs.gomis.test.simple;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatSeparator;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.DropPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

import javax.swing.UIManager;
import java.awt.BorderLayout;

public class SignUpTest extends JPanel {
    private JLabel passwordStrengthLabel;
    private ButtonGroup groupGender;

    public SignUpTest() {
        FlatMacLightLaf.setup();
        
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create content panel with MigLayout
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 0", "[right][grow,fill]", "[]10[]10[]10[]10[]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Create smooth scrolling pane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Purpose section
        contentPanel.add(new JLabel("Purpose:"), "cell 0 0");
        JTextArea purposeField = new JTextArea(5, 20);
        purposeField.setLineWrap(true);
        purposeField.setWrapStyleWord(true);
        JScrollPane purposeScrollPane = new JScrollPane(purposeField);
        purposeScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        contentPanel.add(purposeScrollPane, "cell 1 0,grow");

        // Date section
        JFormattedTextField dateGivenField = new JFormattedTextField();
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(dateGivenField);
        datePicker.setSelectedDate(java.time.LocalDate.now());

        contentPanel.add(new JLabel("Date Given:"), "cell 0 1");
        contentPanel.add(dateGivenField, "cell 1 1");

        // Format date with ordinal suffix
        JLabel formattedDateLabel = new JLabel();
        formattedDateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formattedDateLabel.setFont(formattedDateLabel.getFont().deriveFont(Font.ITALIC));
        
//        // Update the formatted date label initially
//        updateBatchFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
//        
//        // Add date selection listener
//        datePicker.addDateSelectionListener(e -> {
//            updateBatchFormattedDateLabel(formattedDateLabel, datePicker.getSelectedDate());
//        });
//        
        contentPanel.add(formattedDateLabel, "cell 0 2 2 1,alignx center");

        // Signer section
        String currentSigner = "SALLY P. GENUINO, Principal II";
        if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
            GuidanceCounselor counselor = Main.formManager.getCounselorObject();
            currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
        }

        String[] signersAndPosition = new String[] {
            "-Select Who to Sign-",
            currentSigner,
            "SALLY P. GENUINO, Principal II",
            "Other"
        };

        contentPanel.add(new JLabel("Signer and Position:"), "cell 0 3");
        JComboBox<String> signerComboBox = new JComboBox<>(signersAndPosition);
        contentPanel.add(signerComboBox, "cell 1 3");

        // Other signer panel
        DropPanel dropDownPanel = new DropPanel();
        JPanel otherSignerPanel = new JPanel(new MigLayout("fillx, insets 5", "[][grow]", "[]5[]"));
        otherSignerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextField fullNameField = new JTextField(10);
        JTextField workPositionField = new JTextField(10);

        otherSignerPanel.add(new JLabel("Full Name:"), "cell 0 0");
        otherSignerPanel.add(fullNameField, "cell 1 0,growx");
        otherSignerPanel.add(new JLabel("Position:"), "cell 0 1");
        otherSignerPanel.add(workPositionField, "cell 1 1,growx");

        dropDownPanel.setContent(otherSignerPanel);
        contentPanel.add(dropDownPanel, "cell 0 4 2 1,growx");

        signerComboBox.addActionListener(e -> {
            dropDownPanel.setDropdownVisible(signerComboBox.getSelectedItem().equals("Other"));
        });

        // Selected students section
        JPanel studentsPanel = new JPanel(new MigLayout("fillx, insets 10", "[grow]", "[][grow]"));
//        studentsPanel.setBorder(BorderFactory.createTitledBorder(
//            BorderFactory.createLineBorder(new Color(200, 200, 200)),
//            "Selected Students (" + students.size() + ")"
//        ));

        DefaultListModel<String> listModel = new DefaultListModel<>();
//        for (Student student : students) {
//            listModel.addElement(student.getStudentFirstname() + " " + student.getStudentLastname() +
//                               " (LRN: " + student.getStudentLrn() + ")");
//        }
        JList<String> studentList = new JList<>(listModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(studentList);
        listScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth scrolling
        studentsPanel.add(listScrollPane, "cell 0 0,grow,height 150:200:250");

        mainPanel.add(studentsPanel, BorderLayout.SOUTH);
        this.add(mainPanel);
    }
    
    public static void main(String[] args) {
    	try {
    		UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
        SwingUtilities.invokeLater(() -> {
        	FlatMacLightLaf.setup();
            JFrame frame = new JFrame("Sign Up Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 800);
            frame.getContentPane().add(new SignUpTest());
            frame.setVisible(true);
        });
    }
}