package lyfjshs.gomis.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.components.FlatToggleButton;

import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

/**
 * A reusable settings panel component that can be embedded in any Swing application
 */
public class SettingsPanel extends Form {
    private Map<String, Object> settings;
    private ActionListener saveListener;
    private ActionListener cancelListener;
    private Connection connection;
    
    public SettingsPanel(Connection conn) {
        // Initialize settings
        settings = new HashMap<>();
        settings.put("notifications", true);
        settings.put("darkMode", false);
        settings.put("fontSize", "medium");
        settings.put("autoSave", true);
        settings.put("language", "english");
        settings.put("privacyLevel", "standard");

        // Configure panel
        setLayout(new BorderLayout());
        
        // Create main panel with MigLayout
        JPanel mainPanel = new JPanel(new MigLayout("insets 20, fillx, wrap", "[grow]", "[]"));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Add title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 24));
        mainPanel.add(titleLabel, "gapleft 5, gapbottom 15");

        // Notifications Section
        JPanel notificationsPanel = createSectionPanel("Notifications");
        
        // Add toggle for notifications
        JPanel notificationTogglePanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        notificationTogglePanel.setOpaque(false);
        JLabel notificationLabel = new JLabel("Notifications");
        notificationLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        notificationTogglePanel.add(notificationLabel, "cell 0 0");
        
        FlatToggleButton notificationToggle = createToggleSwitch();
        notificationToggle.setSelected((Boolean) settings.get("notifications"));
        notificationToggle.addActionListener(e -> {
            settings.put("notifications", notificationToggle.isSelected());
            System.out.println("Notifications: " + notificationToggle.isSelected());
        });
        notificationTogglePanel.add(notificationToggle, "cell 1 0");
        
        notificationsPanel.add(notificationTogglePanel, "growx, wrap");
        
        JLabel notificationDescription = new JLabel("Enable or disable push notifications");
        notificationDescription.setForeground(Color.GRAY);
        notificationDescription.setFont(new Font("Dialog", Font.PLAIN, 14));
        notificationsPanel.add(notificationDescription, "gapleft 5, gapbottom 10, wrap");
        
        mainPanel.add(notificationsPanel, "growx, gapbottom 15");

        // Appearance Section
        JPanel appearancePanel = createSectionPanel("Appearance");
        
        // Dark Mode Toggle
        JPanel darkModePanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        darkModePanel.setOpaque(false);
        JLabel darkModeLabel = new JLabel("Dark Mode");
        darkModeLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        darkModePanel.add(darkModeLabel, "cell 0 0");
        
        FlatToggleButton darkModeToggle = createToggleSwitch();
        darkModeToggle.setSelected((Boolean) settings.get("darkMode"));
        darkModeToggle.addActionListener(e -> {
            settings.put("darkMode", darkModeToggle.isSelected());
            System.out.println("Dark Mode: " + darkModeToggle.isSelected());
        });
        darkModePanel.add(darkModeToggle, "cell 1 0");
        
        appearancePanel.add(darkModePanel, "growx, wrap");
        
        // Font Size Dropdown
        JPanel fontSizePanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]"));
        fontSizePanel.setOpaque(false);
        JLabel fontSizeLabel = new JLabel("Font Size");
        fontSizeLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        fontSizePanel.add(fontSizeLabel, "wrap");
        
        String[] fontSizes = {"small", "medium", "large"};
        JComboBox<String> fontSizeComboBox = new JComboBox<>(fontSizes);
        fontSizeComboBox.setSelectedItem(settings.get("fontSize"));
        fontSizeComboBox.addActionListener(e -> {
            settings.put("fontSize", fontSizeComboBox.getSelectedItem());
            System.out.println("Font Size: " + fontSizeComboBox.getSelectedItem());
        });
        fontSizePanel.add(fontSizeComboBox, "growx");
        
        appearancePanel.add(fontSizePanel, "growx, gapbottom 10, wrap");
        
        mainPanel.add(appearancePanel, "growx, gapbottom 15");

        // Advanced Section
        JPanel advancedPanel = createSectionPanel("Advanced");
        
        // Auto Save Toggle
        JPanel autoSavePanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        autoSavePanel.setOpaque(false);
        JLabel autoSaveLabel = new JLabel("Auto Save");
        autoSaveLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        autoSavePanel.add(autoSaveLabel, "cell 0 0");
        
        FlatToggleButton autoSaveToggle = createToggleSwitch();
        autoSaveToggle.setSelected((Boolean) settings.get("autoSave"));
        autoSaveToggle.addActionListener(e -> {
            settings.put("autoSave", autoSaveToggle.isSelected());
            System.out.println("Auto Save: " + autoSaveToggle.isSelected());
        });
        autoSavePanel.add(autoSaveToggle, "cell 1 0");
        
        advancedPanel.add(autoSavePanel, "growx, wrap");
        
        // Language Dropdown
        JPanel languagePanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]"));
        languagePanel.setOpaque(false);
        JLabel languageLabel = new JLabel("Language");
        languageLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        languagePanel.add(languageLabel, "wrap");
        
        String[] languages = {"english", "spanish", "french", "german"};
        JComboBox<String> languageComboBox = new JComboBox<>(languages);
        languageComboBox.setSelectedItem(settings.get("language"));
        languageComboBox.addActionListener(e -> {
            settings.put("language", languageComboBox.getSelectedItem());
            System.out.println("Language: " + languageComboBox.getSelectedItem());
        });
        languagePanel.add(languageComboBox, "growx");
        
        advancedPanel.add(languagePanel, "growx, gapbottom 10, wrap");
        
        // Privacy Level Dropdown
        JPanel privacyPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow]"));
        privacyPanel.setOpaque(false);
        JLabel privacyLabel = new JLabel("Privacy Level");
        privacyLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        privacyPanel.add(privacyLabel, "wrap");
        
        String[] privacyLevels = {"basic", "standard", "strict"};
        JComboBox<String> privacyComboBox = new JComboBox<>(privacyLevels);
        privacyComboBox.setSelectedItem(settings.get("privacyLevel"));
        privacyComboBox.addActionListener(e -> {
            settings.put("privacyLevel", privacyComboBox.getSelectedItem());
            System.out.println("Privacy Level: " + privacyComboBox.getSelectedItem());
        });
        privacyPanel.add(privacyComboBox, "growx");
        
        advancedPanel.add(privacyPanel, "growx, gapbottom 10, wrap");
        
        mainPanel.add(advancedPanel, "growx, gapbottom 15");

        // Action Buttons
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        buttonPanel.setOpaque(false);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.putClientProperty("JButton.buttonType", "roundRect");
        cancelButton.addActionListener(e -> {
            if (cancelListener != null) {
                cancelListener.actionPerformed(e);
            } else {
                System.out.println("Cancel clicked");
            }
        });
        buttonPanel.add(cancelButton, "cell 0 0, alignx right");
        
        JButton saveButton = new JButton("Save Changes");
        saveButton.putClientProperty("JButton.buttonType", "roundRect");
        saveButton.putClientProperty("JButton.buttonType", "primary");
        saveButton.addActionListener(e -> {
            if (saveListener != null) {
                saveListener.actionPerformed(e);
            } else {
                System.out.println("Save Changes clicked");
            }
        });
        buttonPanel.add(saveButton, "cell 1 0");
        
        mainPanel.add(buttonPanel, "growx, gaptop 15");

        // Add the main panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates a toggle switch using FlatLaf's FlatToggleButton
     * @return A configured toggle switch button
     */
    private FlatToggleButton createToggleSwitch() {
        FlatToggleButton toggleButton = new FlatToggleButton();
        toggleButton.putClientProperty("JButton.buttonType", "toggleButton");
        toggleButton.putClientProperty("JToggleButton.style", "switch");
        toggleButton.setFocusable(false);
        return toggleButton;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx, wrap", "[grow]"));
        panel.setOpaque(false);
        panel.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        panel.add(titleLabel, "gapleft 5, gapbottom 10");
        
        return panel;
    }
    
    public Map<String, Object> getSettings() {
        return new HashMap<>(settings);
    }
    
    public void setSettings(Map<String, Object> newSettings) {
        this.settings.putAll(newSettings);
        // TODO: Update UI components to reflect new settings
    }
    
    public void setSaveListener(ActionListener listener) {
        this.saveListener = listener;
    }
    
    public void setCancelListener(ActionListener listener) {
        this.cancelListener = listener;
    }

    // Sample main method to demonstrate usage
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Install FlatLaf theme
                FlatLightLaf.setup();
                UIManager.setLookAndFeel(new FlatLightLaf());
                
                // Configure FlatLaf globals
                UIManager.put("ToggleButton.arc", 999);
                UIManager.put("Component.arrowType", "chevron");
                UIManager.put("Component.focusWidth", 1);
                UIManager.put("ScrollBar.thumbArc", 999);
                UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            } catch (Exception ex) {
                System.err.println("Failed to initialize FlatLaf");
                ex.printStackTrace();
            }
            
            JFrame frame = new JFrame("Settings Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 700);
            
            SettingsPanel settingsPanel = new SettingsPanel(null);
            settingsPanel.setSaveListener(e -> JOptionPane.showMessageDialog(frame, "Settings saved!"));
            
            frame.getContentPane().add(settingsPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}