package lyfjshs.gomis.test;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatIntelliJLaf;

import lyfjshs.gomis.test.simple.SimpleInputForms;
import net.miginfocom.swing.MigLayout;

public class DropDownPanel extends JPanel {
	private JButton toggleButton;
	private JPanel dropdownPanel;
	private boolean isDropdownVisible;

	public DropDownPanel() {
		setLayout(new MigLayout("fillx, wrap 1", "[grow]", "[][grow]"));

		// Dropdown panel
		dropdownPanel = new JPanel();
		dropdownPanel.setLayout(new MigLayout("fillx, insets 0", "[grow]", "[]"));
		dropdownPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		dropdownPanel.setVisible(false);
		
				// Toggle button to show/hide the dropdown panel
				toggleButton = new JButton("Select an option");
				toggleButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						toggleDropdown();
					}
				});
				add(toggleButton, "cell 0 0,grow");

//		 Add items to the dropdown panel
		SimpleInputForms SimpleInputForms = new SimpleInputForms();
        dropdownPanel.add(SimpleInputForms, "cell 0 0,grow");
		add(dropdownPanel, "hidemode 3,cell 0 1,grow");
	}

	private void toggleDropdown() {
		isDropdownVisible = !isDropdownVisible;
		dropdownPanel.setVisible(isDropdownVisible);
		revalidate();
		repaint();
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FlatIntelliJLaf.setup();
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setSize(500, 500);
					frame.setLocationRelativeTo(null);
					JPanel contentPane = new JPanel(new MigLayout("insets 0", "[grow]", "[][grow]"));

					DropDownPanel comboBoxPanel = new DropDownPanel();
					contentPane.add(comboBoxPanel, "cell 0 0,growx");
					frame.setContentPane(contentPane);

					JPanel panel = new JPanel(new MigLayout("", "[grow]", "[][]"));
					panel.setBackground(Color.CYAN);
					contentPane.add(panel, "cell 0 1,grow");

					JLabel lblNewLabel = new JLabel("THIS PANEL SHOULD EXPAND ONLY SHOWING A BUTTON ABOVE");
					panel.add(lblNewLabel, "cell 0 0,growx,aligny top");

					JLabel lblNewLabel_1 = new JLabel(
							"THAT WHEN CLICKED SHOWS A COLLAPSABLE PANEL JUST LIKE A COMBOBOX");
					panel.add(lblNewLabel_1, "cell 0 1,growx");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
