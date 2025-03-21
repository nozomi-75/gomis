package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.DAO.PreferencesDAO;
import lyfjshs.gomis.Database.entity.PreferencesEntity;
import net.miginfocom.swing.MigLayout;

public class FlatWindows11Settings extends JFrame {
    private static final String DB_URL = "jdbc:mariadb://localhost:3306/gomisDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YourRootPassword123!";
    
    private FlatToggleSwitch darkModeToggle;
    private JComboBox<String> themeComboBox;
    private PreferencesDAO preferencesDAO;
    private int userId = 1; // Assume user ID 1 for now

    public FlatWindows11Settings() {
        // Initialize database connection
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            preferencesDAO = new PreferencesDAO(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(706, 300);
        this.getContentPane().setLayout(new MigLayout("fill, insets 10", "[grow]", "[pref!]"));

        // Main panel
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Theme & Appearance", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        getContentPane().add(panel, "cell 0 0,grow");
        panel.setLayout(new MigLayout("", "[grow]", "[pref!][][grow]"));

        // Dark mode panel
        JPanel darkModePanel = new JPanel(new MigLayout("", "[][][][][grow]", "[][][]"));
        panel.add(darkModePanel, "cell 0 0,grow");
        darkModePanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;"
                + "[light]background:shade($Panel.background,10%);"
                + "[dark]background:tint($Panel.background,10%);");

        JLabel lblDarkMode = new JLabel("Dark Mode");
        lblDarkMode.setFont(new Font("SansSerif", Font.BOLD, 14));
        darkModePanel.add(lblDarkMode, "cell 0 0 2 1");

        JLabel lblDarkModeDesc = new JLabel("Enable or disable dark mode.");
        darkModePanel.add(lblDarkModeDesc, "cell 0 1 4 2");

        // Dark Mode Toggle Switch
        darkModeToggle = new FlatToggleSwitch(new Color(0, 122, 255));
        darkModeToggle.addActionListener(e -> applySelectedTheme());
        darkModePanel.add(darkModeToggle, "cell 4 0 1 3,alignx right,aligny center");

        // Theme selection panel
        JPanel themePanel = new JPanel(new MigLayout("", "[][][][][grow][100px,fill]", "[][][]"));
        panel.add(themePanel, "cell 0 1,grow");
        themePanel.putClientProperty(FlatClientProperties.STYLE, "arc:20;"
                + "[light]background:shade($Panel.background,10%);"
                + "[dark]background:tint($Panel.background,10%);");

        JLabel lblThemeTitle = new JLabel("Select Theme");
        lblThemeTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        themePanel.add(lblThemeTitle, "cell 0 0 2 1");

        JLabel lblSelectTheme = new JLabel("Choose a theme for the application:");
        themePanel.add(lblSelectTheme, "cell 0 1 4 2");

        // Theme Selection ComboBox
        themeComboBox = new JComboBox<>();
        themeComboBox.setModel(new DefaultComboBoxModel<>(new String[]{
                "FlatLaf", "macOS", "Darcula"}));

        themeComboBox.addActionListener(e -> applySelectedTheme());

        themePanel.add(themeComboBox, "cell 5 0 1 3,alignx right,aligny center");

        // Load saved preferences on startup
        loadPreferences();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /**
     * Loads user preferences from the database.
     */
    private void loadPreferences() {
        List<PreferencesEntity> preferences = preferencesDAO.getPreferencesByUserId(userId);
        for (PreferencesEntity pref : preferences) {
            if ("Theme".equals(pref.getCategory()) && "SelectedTheme".equals(pref.getKey())) {
                themeComboBox.setSelectedItem(pref.getValue());
            } else if ("Theme".equals(pref.getCategory()) && "DarkMode".equals(pref.getKey())) {
                darkModeToggle.setSelected(Boolean.parseBoolean(pref.getValue()));
            }
        }
        applySelectedTheme(); // Apply retrieved settings
    }

    /**
     * Applies the selected theme based on the current toggle state and saves the preference.
     */
    private void applySelectedTheme() {
        boolean isDarkMode = darkModeToggle.isSelected();
        String selectedTheme = (String) themeComboBox.getSelectedItem();

        SwingUtilities.invokeLater(() -> {
            FlatAnimatedLafChange.showSnapshot();

            switch (selectedTheme) {
                case "FlatLaf":
                    if (isDarkMode) FlatDarkLaf.setup();
                    else FlatLightLaf.setup();
                    break;
                case "Darcula":
                    FlatDarculaLaf.setup(); // Always dark
                    darkModeToggle.setSelected(true); // Ensure toggle remains ON
                    break;
                case "macOS":
                    if (isDarkMode) FlatMacDarkLaf.setup();
                    else FlatMacLightLaf.setup();
                    break;
                default:
                    FlatLightLaf.setup();
            }

            // Save preferences to the database
            savePreference("Theme", "SelectedTheme", selectedTheme);
            savePreference("Theme", "DarkMode", String.valueOf(isDarkMode));

        	int previousStep = darkModeToggle.getAnimationStep(); // Preserve animation progress

    		SwingUtilities.updateComponentTreeUI(this);

    		// ✅ Instead of reapplying UI, restore animation progress and repaint
    		darkModeToggle.setAnimationStep(previousStep);
    		darkModeToggle.setUI(new CustomButtonUI());
    		darkModeToggle.repaint();
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        });
    }

    /**
     * Saves a user preference to the database.
     */
    private void savePreference(String category, String key, String value) {
        int prefId = preferencesDAO.findPreferenceId(userId, category, key);
        PreferencesEntity preference = new PreferencesEntity(
                prefId == -1 ? 0 : prefId, // Use existing ID if found, else create new
                userId, category, key, value, "String", null, null, null
        );

        if (prefId == -1) {
            preferencesDAO.createPreference(preference); // Insert new preference
        } else {
            preferencesDAO.updatePreference(preference); // Update existing preference
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup(); // Default theme
            new FlatWindows11Settings();
        });
    }
}
