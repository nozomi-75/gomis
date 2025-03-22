package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import lyfjshs.gomis.test.simple.SimpleInputForms;
import net.miginfocom.swing.MigLayout;

public class ComboBoxPanel extends JPanel {
    private JButton toggleButton;
    private JPanel dropdownPanel;
    private boolean isDropdownVisible;

    public ComboBoxPanel() {
        setLayout(new MigLayout("insets 0", "[grow]", "[][grow, 0]"));

        // Toggle button to show/hide the dropdown panel
        toggleButton = new JButton("Select an option");
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleDropdown();
            }
        });
        add(toggleButton, "cell 0 0,grow");

        // Dropdown panel
        dropdownPanel = new JPanel();
        dropdownPanel.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[]"));
        dropdownPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        dropdownPanel.setVisible(false);

        // Add items to the dropdown panel
        SimpleInputForms simpleInputForms = new SimpleInputForms();
        dropdownPanel.add(simpleInputForms, "cell 0 0,grow");
        add(dropdownPanel, "hidemode 3,cell 0 1,grow");
    }

    private void toggleDropdown() {
        isDropdownVisible = !isDropdownVisible;
        dropdownPanel.setVisible(isDropdownVisible);
        revalidate();
        repaint();
    }
}
