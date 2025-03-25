package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

public class ToggleSwitchExample {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup(); // Default Light Theme
            
            JFrame frame = new JFrame("FlatToggleSwitch Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(679, 371);
            frame.getContentPane().setLayout(new FlowLayout());

            JLabel label = new JLabel("Dark Mode");
            FlatToggleSwitch toggleSwitch = new FlatToggleSwitch(new Color(0, 122, 255));

            toggleSwitch.addActionListener(e -> {
                boolean isDarkMode = toggleSwitch.isSelected();
                FlatAnimatedLafChange.showSnapshot(); // Animation effect
                if (isDarkMode) {
                    FlatDarkLaf.setup();
                } else {
                    FlatLightLaf.setup();
                }
                SwingUtilities.updateComponentTreeUI(frame);
                FlatAnimatedLafChange.hideSnapshotWithAnimation();
            });

            frame.getContentPane().add(label);
            frame.getContentPane().add(toggleSwitch);
            
            JLabel lblNewLabel = new JLabel("New label");
            frame.getContentPane().add(lblNewLabel);

            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
