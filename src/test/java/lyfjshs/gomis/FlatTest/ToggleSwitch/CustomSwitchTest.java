package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

public class CustomSwitchTest extends JFrame {
	public CustomSwitchTest() {
	}

	private static final long serialVersionUID = 1L;
	public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup(); // Apply FlatLaf Theme
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("FlatLaf Toggle Switch");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.getContentPane().setLayout(new MigLayout("fill, insets 10", "[][grow]", "[]10[]"));

        JPanel panel = new JPanel(new MigLayout("fill, insets 0", "[]10[]10[]10[]", "[]"));
        panel.setOpaque(false);

        // Create custom toggle switches with different colors
        FlatToggleSwitch toggle1 = new FlatToggleSwitch(new Color(0, 150, 255));  // Blue
        FlatToggleSwitch toggle2 = new FlatToggleSwitch(new Color(150, 80, 255)); // Purple
        FlatToggleSwitch toggle3 = new FlatToggleSwitch(new Color(255, 70, 70));  // Red
        FlatToggleSwitch toggle4 = new FlatToggleSwitch(new Color(180, 210, 50)); // Greenish Yellow

        panel.add(toggle1);
        panel.add(toggle2);
        panel.add(toggle3);
        panel.add(toggle4);

        // Buttons to control the state
        JButton btnSetTrue = new JButton("Set True");
        JButton btnSetFalse = new JButton("Set False");

        btnSetTrue.addActionListener(e -> {
            toggle1.setSelected(true);
            toggle2.setSelected(true);
            toggle3.setSelected(true);
            toggle4.setSelected(true);
        });

        btnSetFalse.addActionListener(e -> {
            toggle1.setSelected(false);
            toggle2.setSelected(false);
            toggle3.setSelected(false);
            toggle4.setSelected(false);
        });

        JPanel buttonPanel = new JPanel(new MigLayout("fill, insets 0", "[][]", "[]"));
        buttonPanel.add(btnSetTrue);
        buttonPanel.add(btnSetFalse);

        frame.getContentPane().add(panel, "wrap");
        frame.getContentPane().add(buttonPanel);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}