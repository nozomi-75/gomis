package lyfjshs.gomis.test;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.format.DateTimeFormatter;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class JFrameSizeTest extends JFrame {

    public JFrameSizeTest() {
        setTitle("Frame Size Console");

        // Calculate the minimum size including insejts
        Insets insets = this.getInsets();
        int minWidth = 1590 + insets.left + insets.right;
        System.out.println("Minimum width: " + minWidth);

        int minHeight = 700 + insets.top + insets.bottom;
        System.out.println("Minimum height: " + minHeight);

        Dimension minSize = new Dimension(minWidth, minHeight);
        System.out.println("Minimum size: " + minSize);

        // setMinimumSize(minSize); // Set the minimum size

        setSize(new Dimension(425, 720)); // Initial size, within bounds
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Add a component listener to track size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                printFrameSize();
            }

            @Override
            public void componentShown(ComponentEvent e){
                printFrameSize();
            }
        });
        
        // Create the panel for input fields
        JPanel panel = new JPanel(new MigLayout("fillx, insets 10", "[right]10[grow,fill]", "[100][][][]"));
        JFormattedTextField dateGivenField = new JFormattedTextField();
        JComboBox<String> signerComboBox = new JComboBox<>(
                new String[] { "SALLY P. GENUINO, Principal II", "RACQUEL D. COMANDANTE, Guidance Designate" });
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
        //get the selected date and format this in this format example: "27th day of April, 2019" 
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d'th day of' MMMM, yyyy");
        String formatDateSelected = datePicker.getSelectedDate().format(formatter);
        panel.add(new JLabel(formatDateSelected), "cell 0 2 2 1,alignx center");
        panel.add(new JLabel("Signer and Position:"), "cell 0 3");
        panel.add(signerComboBox, "cell 1 3");

        getContentPane().add(panel);
    }

    private void printFrameSize() {
        Dimension size = getSize();
        System.out.println("Frame size: Width = " + size.width + ", Height = " + size.height);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrameSizeTest frame = new JFrameSizeTest();
            frame.setVisible(true);
            frame.printFrameSize();
        });
    }
}
