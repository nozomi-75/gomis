package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import net.miginfocom.swing.MigLayout;

public class CustomSwitchTest extends JFrame {
	private FlatToggleSwitch darkModeToggle;
	private JPanel panel_1;

	public CustomSwitchTest() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(706, 300);
		this.getContentPane().setLayout(new MigLayout("fill, insets 10", "[grow]", "[pref!]"));

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "JPanel title", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[pref!][][grow]"));

		panel_1 = new JPanel(new MigLayout("", "[][][][][grow]", "[][][]"));
		panel.add(panel_1, "cell 0 0,grow");
		panel_1.putClientProperty(FlatClientProperties.STYLE, "" + "arc:20;"
				+ "[light]background:shade($Panel.background,10%);" + "[dark]background:tint($Panel.background,10%);");
		JLabel lblNewLabel = new JLabel("Dark Mode");
		lblNewLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
		panel_1.add(lblNewLabel, "cell 0 0 2 1");

		JLabel lblNewLabel_1 = new JLabel("Turn on to switch to Dark Mode Theme");
		panel_1.add(lblNewLabel_1, "cell 0 1 4 2");

		// Create Dark Mode toggle switch
		darkModeToggle = new FlatToggleSwitch(new Color(0, 122, 255));
		darkModeToggle.addActionListener(e -> toggleDarkMode());

		panel_1.add(darkModeToggle, "cell 4 0 1 3,alignx right,aligny center");

		JPanel panel_2 = new JPanel(new MigLayout("", "[][][][][grow][100px,fill]", "[][][]"));
		panel.add(panel_2, "cell 0 1,grow");
		panel_2.putClientProperty(FlatClientProperties.STYLE, "" + "arc:20;"
				+ "[light]background:shade($Panel.background,10%);" + "[dark]background:tint($Panel.background,10%);");
		JLabel lblNewLabel1 = new JLabel("Select Theme");
		lblNewLabel1.setFont(new Font("SansSerif", Font.BOLD, 14));
		panel_2.add(lblNewLabel1, "cell 0 0 2 1");

		JComboBox comboBox = new JComboBox();
		panel_2.add(comboBox, "cell 5 0 1 3,alignx right,aligny center");

		JLabel lblNewLabel_11 = new JLabel("Select a Theme ");
		panel_2.add(lblNewLabel_11, "cell 0 1 4 2");
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void toggleDarkMode() {
		if (darkModeToggle.isSelected()) {
			SwingUtilities.invokeLater(() -> {
				FlatAnimatedLafChange.showSnapshot();
				FlatDarkLaf.setup();
				updateLookAndFeel();
			});
		} else {
			SwingUtilities.invokeLater(() -> {
				FlatAnimatedLafChange.showSnapshot();
				FlatLightLaf.setup();
				updateLookAndFeel();
			});
		}
	}

	private void updateLookAndFeel() {
		int previousStep = darkModeToggle.getAnimationStep(); // Preserve animation progress

		SwingUtilities.updateComponentTreeUI(this);

		// ✅ Instead of reapplying UI, restore animation progress and repaint
		darkModeToggle.setAnimationStep(previousStep);
		darkModeToggle.setUI(new CustomButtonUI());
		darkModeToggle.repaint();

		FlatAnimatedLafChange.hideSnapshotWithAnimation();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FlatLightLaf.setup(); // Apply FlatLaf Theme
			new CustomSwitchTest();
		});
	}
}
