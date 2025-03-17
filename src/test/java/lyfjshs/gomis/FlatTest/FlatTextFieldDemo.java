package lyfjshs.gomis.FlatTest;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.components.FlatPopupMenu;
import com.formdev.flatlaf.extras.components.FlatTextField;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

/**
 * Enhanced ComboBox-like TextField with improved UX, accessibility, and performance.
 */
public class FlatTextFieldDemo extends JFrame {
    private static final List<String> OFFENSES = Arrays.asList(
        "Absence/Late", "Minor Property Damage", "Threatening/Intimidating",
        "Pornographic Materials", "Gadget Use in Class", "Cheating", "Stealing", "No Pass",
        "Bullying", "Sexual Abuse", "Illegal Drugs", "Alcohol",
        "Smoking/Vaping", "Gambling", "Public Display of Affection",
        "Fighting/Weapons", "Severe Property Damage"
    );

    private final FlatTextField textField;
    private final JButton dropdownButton;
    private final JList<String> suggestionList;
    private final FlatPopupMenu popupMenu;
    private final DefaultListModel<String> listModel;
    private final Timer updateTimer;

    public FlatTextFieldDemo() {
        setTitle("Enhanced ComboBox");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel pane = new JPanel(new MigLayout("insets 10, fill", "[grow][]", "[]"));

        // Create accessible FlatLaf TextField
        textField = new FlatTextField();
        textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Type to search...");
        textField.setPreferredSize(new Dimension(280, 30));
        textField.getAccessibleContext().setAccessibleDescription("Search field with dropdown suggestions.");

        // Dropdown Button (🔽)
        dropdownButton = new JButton("▼");
        dropdownButton.setFocusable(false);
        dropdownButton.setPreferredSize(new Dimension(40, 30));
        dropdownButton.setToolTipText("Show all options");

        // Popup Menu for suggestions
        popupMenu = new FlatPopupMenu();
        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(suggestionList);
        popupMenu.add(scrollPane);

        // Load all options initially
        for (String offense : OFFENSES) {
            listModel.addElement(offense);
        }

        // Swing Timer (Debounce Effect)
        updateTimer = new Timer(100, e -> highlightBestMatch());
        updateTimer.setRepeats(false);

        // Document Listener for Typing Events
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { triggerUpdate(); }
            public void removeUpdate(DocumentEvent e) { triggerUpdate(); }
            public void changedUpdate(DocumentEvent e) { triggerUpdate(); }
        });

        // Dropdown Button Click = Show All Options
        dropdownButton.addActionListener(e -> showAllSuggestions());

        // Keyboard Navigation & Persistent Typing
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int selectedIndex = suggestionList.getSelectedIndex();
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!popupMenu.isVisible()) showAllSuggestions();
                    else if (selectedIndex < listModel.size() - 1) suggestionList.setSelectedIndex(selectedIndex + 1);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (selectedIndex > 0) suggestionList.setSelectedIndex(selectedIndex - 1);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER && selectedIndex != -1) {
                    selectSuggestion();
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    selectSuggestion();
                }
            }
        });

        pane.add(textField, "growx");
        pane.add(dropdownButton, "wrap");
        getContentPane().add(pane);
    }

    private void triggerUpdate() {
        updateTimer.restart();
    }

    private void highlightBestMatch() {
        String input = textField.getText().trim().toLowerCase();
        listModel.clear();

        // Add elements individually
        for (String offense : OFFENSES) {
            listModel.addElement(offense);
        }

        if (!input.isEmpty()) {
            for (int i = 0; i < OFFENSES.size(); i++) {
                String item = OFFENSES.get(i).toLowerCase();
                if (item.startsWith(input) || item.contains(input.substring(0, Math.min(3, input.length())))) {
                    suggestionList.setSelectedIndex(i);
                    suggestionList.ensureIndexIsVisible(i);
                    break;
                }
            }
        }
        popupMenu.show(textField, 0, textField.getHeight());
    }

    private void showAllSuggestions() {
        listModel.clear();

        // Add elements individually
        for (String offense : OFFENSES) {
            listModel.addElement(offense);
        }

        popupMenu.show(textField, 0, textField.getHeight());
        textField.requestFocus();
    }

    private void selectSuggestion() {
        String selectedValue = suggestionList.getSelectedValue();
        if (selectedValue != null) {
            textField.setText(selectedValue);
            popupMenu.setVisible(false);
            textField.requestFocus();
        }
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            FlatTextFieldDemo frame = new FlatTextFieldDemo();
            frame.setVisible(true);
        });
    }
}
