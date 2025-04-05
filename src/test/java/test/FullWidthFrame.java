package test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.DropPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import javax.swing.JTextField;

public class FullWidthFrame extends JFrame {
	private JTextField fullNameField;
	private JTextField workPositionField;
    
    public FullWidthFrame() {
    	   // Create the panel for input fields
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[100][][][][]"));
        JFormattedTextField dateGivenField = new JFormattedTextField();
        
        // Get current counselor's information
        String currentSigner = "SALLY P. GENUINO, Principal II"; // Default to principal
        if (Main.formManager != null && Main.formManager.getCounselorObject() != null) {
            GuidanceCounselor counselor = Main.formManager.getCounselorObject();
            currentSigner = counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition();
        }
        
        String[] signersAndPosition = new String[] { "-Select Who to Sign-", currentSigner , "SALLY P. GENUINO, Principal II" , "Other"};
        DropPanel dropDownPanel = new DropPanel();
        panel.add(dropDownPanel, "cell 0 4 2 1,grow");

        JPanel otherSignerPanel = new JPanel();
        dropDownPanel.setContent(otherSignerPanel);
        otherSignerPanel.setLayout(new MigLayout("", "[][grow]", "[][]"));
        otherSignerPanel.add( new JLabel("Full Name:"), "cell 0 0,alignx trailing");
        fullNameField = new JTextField(10);
        otherSignerPanel.add(fullNameField, "cell 1 0,growx");
        otherSignerPanel.add(new JLabel("Position:"), "cell 0 1,alignx trailing");
        workPositionField = new JTextField(10);
        otherSignerPanel.add(workPositionField, "cell 1 1,growx");
        
        
        // Create combo box with current counselor and principal as options
        JComboBox<String> signerComboBox = new JComboBox<>(signersAndPosition);
        if (signerComboBox.getSelectedIndex() == 3) {
        	//show dropDown panel
        	dropDownPanel.setDropdownVisible(true);
        } else {
        	dropDownPanel.setDropdownVisible(!dropDownPanel.isVisible());
        }
        DatePicker datePicker = new DatePicker();
        datePicker.setEditor(dateGivenField);
        datePicker.setSelectedDate(java.time.LocalDate.now());

        // Add fields to the panel
        panel.add(new JLabel("Purpose:"), "cell 0 0");

        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, "cell 1 0,grow");

        // Input fields
        JTextArea purposeField = new JTextArea();
        scrollPane.setViewportView(purposeField);
        purposeField.setColumns(1);
        purposeField.setRows(5);
        panel.add(new JLabel("Date Given:"), "cell 0 1");
        panel.add(dateGivenField, "cell 1 1");
        
        // Format the date with proper ordinal suffix
        LocalDate selectedDate = datePicker.getSelectedDate();
        int day = selectedDate.getDayOfMonth();
        String suffix = getOrdinalSuffix(day);
        DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy");
        String formatDateSelected = day + suffix + " day of " + selectedDate.format(monthYearFormatter);
        
        panel.add(new JLabel(formatDateSelected), "cell 0 2 2 1,alignx center");
        panel.add(new JLabel("Signer and Position:"), "cell 0 3");
        panel.add(signerComboBox, "cell 1 3");
        getContentPane().add(panel);
    }
    
    private static String getOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }


    public static void main(String[] args) {
        // Apply FlatLaf theme
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run the JFrame on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new FullWidthFrame());
    }
}
