package lyfjshs.gomis;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import net.miginfocom.swing.MigLayout;

public class AppTest {

    public static void main(String[] arg) {
        FlatRobotoFont.install();
        FlatMacLightLaf.setup();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        JFrame frame = new JFrame("Custom Panel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        CustomPanel customPanel = new CustomPanel("Custom Title", "This is the content area.");
        frame.add(customPanel);

        frame.setVisible(true);
    }

    // Custom panel class similar to SimpleModalBorder
    private static class CustomPanel extends JPanel {
        private final JLabel titleLabel;
        private final JButton closeButton;
        private final JTextField firstNameField;
        private final JTextField lastNameField;
        private final JTextField companyField;
        private final JTextField emailField;
        private final JComboBox<String> countryComboBox;

        public CustomPanel(String title, String content) {
            setLayout(new MigLayout("wrap 2", "[grow, fill]10[grow, fill]", "[]10[]10[]10[]10[]"));

            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            setBackground(UIManager.getColor("Panel.background"));

            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            closeButton = new JButton("Close");
            closeButton.addActionListener(e -> {
                // Close action (for example, dispose the parent frame)
                SwingUtilities.getWindowAncestor(this).dispose();
            });

            JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            headerPanel.add(titleLabel);
            headerPanel.add(closeButton);

            JTextArea contentArea = new JTextArea(content);
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Create input fields
            firstNameField = new JTextField(10);
            lastNameField = new JTextField(10);
            companyField = new JTextField(20);
            emailField = new JTextField(20);
            String[] countries = {"United States", "Canada", "Mexico"}; // Add more countries as needed
            countryComboBox = new JComboBox<>(countries);

            // Add components to the panel
            JPanel inputPanel = new JPanel(new GridLayout(5, 2));
            inputPanel.add(new JLabel("Full name:"));
            inputPanel.add(firstNameField);
            inputPanel.add(new JLabel(""));
            inputPanel.add(lastNameField);
            inputPanel.add(new JLabel("Company name:"));
            inputPanel.add(companyField);
            inputPanel.add(new JLabel("Email address:"));
            inputPanel.add(emailField);
            inputPanel.add(new JLabel("Country:"));
            inputPanel.add(countryComboBox);

            // Add buttons
            JButton yesButton = new JButton("Yes");
            JButton noButton = new JButton("No");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(yesButton);
            buttonPanel.add(noButton);

            // Add all components to the main panel
            add(headerPanel, "span 2, growx");
            add(inputPanel, "grow");
            add(buttonPanel, "span 2, growx");
        }
    }
}
