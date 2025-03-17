package lyfjshs.gomis.FlatTest;

import javax.swing.*;
import com.formdev.flatlaf.FlatDarkLaf;
import net.miginfocom.swing.MigLayout;
import java.awt.Font;

public class FlatWindows11Settings extends JFrame {

    public FlatWindows11Settings() {
        setTitle("Settings");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new MigLayout("fillx, wrap", "[grow]", "[]10[]"));

        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        // Create FlatClickablePanel objects
        FlatClickablePanel colorsPanel = new FlatClickablePanel("Colors", "Accent color, transparency effects", "icons/colors.svg");
        FlatClickablePanel themesPanel = new FlatClickablePanel("Themes", "Install, create, manage", "icons/themes.svg");
        FlatClickablePanel dynamicLightingPanel = new FlatClickablePanel("Dynamic Lighting", "Connected devices, effects", "icons/lighting.svg");

        // Add components
        getContentPane().add(titleLabel, "cell 0 0, gapbottom 15");
        getContentPane().add(colorsPanel, "cell 0 1, growx");
        getContentPane().add(themesPanel, "cell 0 2, growx");
        getContentPane().add(dynamicLightingPanel, "cell 0 3, growx");

        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(FlatWindows11Settings::new);
    }
}
