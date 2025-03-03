package lyfjshs.gomis;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;
import raven.modal.component.Modal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchPanel extends Modal {
    private JPanel advancedPanel;
    private JButton toggleButton;

    // Static text fields
    private JTextField firstNameField = new JTextField(20);
    private JTextField middleNameField = new JTextField(20);
    private JTextField lastNameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JTextField dobField = new JTextField(20);
    private JTextField addressField = new JTextField(20);

    public SearchPanel() {
        setLayout(new MigLayout("fillx,insets 0,wrap", "[500,grow,fill][]", "[][][][][100px,grow][]"));
        JTextField textSearch = new JTextField();
        JPanel panelResult = new JPanel(new MigLayout("insets 3 10 3 10,fillx,wrap", "[fill]"));
        textSearch.putClientProperty("JTextField.placeholderText", "Enter LRN");
        add(textSearch, "flowx,cell 0 0,grow");
        
        JButton btnNewButton = new JButton(new FlatSVGIcon("icons/search.svg", 0.4f));
        btnNewButton.putClientProperty(FlatClientProperties.STYLE, "" + "margin:5,7,5,10;" + "arc:10;" + "borderWidth:0;"
				+ "focusWidth:0;" + "innerFocusWidth:0;" + "[light]background:shade($Panel.background,10%);"
				+ "[dark]background:tint($Panel.background,10%);" + "[light]foreground:tint($Button.foreground,40%);"
				+ "[dark]foreground:shade($Button.foreground,30%);");
        add(btnNewButton, "cell 1 0");
        add(new JSeparator(), "cell 0 1 2 1,height 2!");

        // Advanced Search Panel (Initially Hidden)
        advancedPanel = new JPanel(new MigLayout("insets 5", "[100px][200px][][30px]", "[][][][][]"));
        advancedPanel.setBorder(new TitledBorder(null, "Advanced Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        advancedPanel.setVisible(false); // Initially hidden

        // Add static fields directly
        advancedPanel.add(new JLabel("First Name:"), "cell 0 0");
        advancedPanel.add(firstNameField, "cell 1 0");
        
                // Gender Dropdown
                JLabel label = new JLabel("Gender:");
                advancedPanel.add(label, "cell 3 0");
        advancedPanel.add(new JLabel("Middle Name:"), "cell 0 1");
        advancedPanel.add(middleNameField, "cell 1 1");
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female"});
        advancedPanel.add(genderBox, "cell 3 1");
        advancedPanel.add(new JLabel("Last Name:"), "cell 0 2");
        advancedPanel.add(lastNameField, "cell 1 2");
        JLabel label_1 = new JLabel("Date of Birth:");
        advancedPanel.add(label_1, "cell 3 2");
        advancedPanel.add(new JLabel("Email:"), "cell 0 3");
        advancedPanel.add(emailField, "cell 1 3");
        advancedPanel.add(dobField, "cell 3 3");
        JLabel label_2 = new JLabel("Address:");
        advancedPanel.add(label_2, "cell 0 4");

        // Toggle Button
        toggleButton = new JButton("Show Advanced Search");
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isVisible = advancedPanel.isVisible();
                advancedPanel.setVisible(!isVisible);
                toggleButton.setText(isVisible ? "Show Advanced Search" : "Hide Advanced Search");
            }
        });
        
        add(toggleButton, "cell 0 2 2 1,alignx right");

        add(advancedPanel, "cell 0 3 2 1,alignx center,growy");
        advancedPanel.add(addressField, "cell 1 4");

        JScrollPane scrollPane = new JScrollPane(panelResult);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "cell 0 4 2 1,grow");
    }
}
