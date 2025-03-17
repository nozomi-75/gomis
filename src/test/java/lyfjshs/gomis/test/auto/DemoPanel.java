package lyfjshs.gomis.test.auto;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxUI;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;

class ComboSuggestionUI extends BasicComboBoxUI {

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        configureComboBox();
    }

    private void configureComboBox() {
        comboBox.setEditable(true);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(60, 60, 60));
        comboBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        editor.setSelectionColor(new Color(54, 189, 248));
        editor.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        // Enable auto-complete
        AutoCompleteDecorator.decorate(comboBox);
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton();
        try {
            button.setIcon(new FlatSVGIcon("icons/down-arrow.svg", 0.6f));
        } catch (Exception e) {
            button.setText("▼"); // Fallback if SVG icon is missing
        }
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }

    @Override
    protected ListCellRenderer<Object> createRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (c instanceof JLabel) {  // Use explicit casting
                    JLabel label = (JLabel) c;
                    label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
                    label.setForeground(isSelected ? new Color(17, 155, 215) : Color.BLACK);
                }
                return c;
            }
        };
    }
}

class ComboBoxSuggestion<E> extends JComboBox<E> {
    public ComboBoxSuggestion() {
        setUI(new ComboSuggestionUI());
    }
}

// Example usage
public class DemoPanel extends JPanel {
    private JTextField textField;
    private ComboBoxSuggestion<String> combo;

    private String[] minorOffenses = {
            "Absence/Late", "Minor Property Damage", "Threatening/Intimidating", "Pornographic Materials",
            "Gadget Use in Class", "Cheating", "Stealing", "No Pass"
    };

    private String[] majorOffenses = {
            "Bullying", "Sexual Abuse", "Illegal Drugs", "Alcohol", "Smoking/Vaping",
            "Gambling", "Public Display of Affection", "Fighting/Weapons", "Severe Property Damage"
    };

    private List<String> allOffenses = new ArrayList<>();

    public DemoPanel() {
        setLayout(new MigLayout("insets 10, wrap", "[grow]", "[]10[][][][]"));

        allOffenses.addAll(Arrays.asList(minorOffenses));
        allOffenses.addAll(Arrays.asList(majorOffenses));

        JLabel label = new JLabel("Select Violation Type:");
        combo = new ComboBoxSuggestion<>();
        populateComboBox();

        combo.setEditable(true);
        combo.addActionListener(new ViolationCheckListener());

        add(label, "cell 0 0");
        add(combo, "cell 0 1, growx");

        JLabel lblNewLabel = new JLabel("Enter Custom Violation:");
        add(lblNewLabel, "cell 0 3");

        textField = new JTextField();
        add(textField, "cell 0 4, growx");
        textField.setColumns(10);
    }

    private void populateComboBox() {
        combo.addItem("-- Minor Violations --");
        for (String offense : minorOffenses) {
            combo.addItem(offense);
        }
        combo.addItem("-- Major Violations --");
        for (String offense : majorOffenses) {
            combo.addItem(offense);
        }
    }

    private class ViolationCheckListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedViolation = (String) combo.getSelectedItem();
            if (selectedViolation == null || selectedViolation.isEmpty()) {
                return;
            }
            if (!allOffenses.contains(selectedViolation) && !selectedViolation.startsWith("--")) {
                int response = JOptionPane.showConfirmDialog(null,
                        "The violation '" + selectedViolation + "' is not in the list. Do you want to add it?",
                        "Add Violation", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    allOffenses.add(selectedViolation);
                    combo.addItem(selectedViolation);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup(); // Correctly placed setup
            JFrame frame = new JFrame("Combo Suggestion Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new DemoPanel());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
