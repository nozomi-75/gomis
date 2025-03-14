package lyfjshs.gomis.FlatTest;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.extras.components.FlatCheckBox;
import com.formdev.flatlaf.extras.components.FlatComboBox;
import com.formdev.flatlaf.extras.components.FlatFormattedTextField;
import com.formdev.flatlaf.extras.components.FlatLabel;
import com.formdev.flatlaf.extras.components.FlatList;
import com.formdev.flatlaf.extras.components.FlatPasswordField;
import com.formdev.flatlaf.extras.components.FlatProgressBar;
import com.formdev.flatlaf.extras.components.FlatRadioButton;
import com.formdev.flatlaf.extras.components.FlatSeparator;
import com.formdev.flatlaf.extras.components.FlatSlider;
import com.formdev.flatlaf.extras.components.FlatSpinner;
import com.formdev.flatlaf.extras.components.FlatTabbedPane;
import com.formdev.flatlaf.extras.components.FlatTextField;
import com.formdev.flatlaf.extras.components.FlatToggleButton;
import com.formdev.flatlaf.extras.components.FlatTree;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.miginfocom.swing.MigLayout;

public class FlatLafExtrasDemo {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FlatLaf Extras Components");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 600);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.setLocationRelativeTo(null);

            // Main Panel
            JPanel panel = new JPanel();
            panel.setLayout(new MigLayout("", "[279px][279px]", "[][][baseline][][][][][][][][][][][][][][]"));
            
                        // Buttons
                        JLabel label = new JLabel("FlatButton:");
                        panel.add(label, "cell 0 0,grow");
            FlatButton flatButton = new FlatButton();
            flatButton.setText("Click Me");
            flatButton.setButtonType(FlatButton.ButtonType.roundRect);
            panel.add(flatButton, "cell 1 0,grow");

            // Toggle Button
            panel.add(new JLabel("FlatToggleButton:"), "cell 0 1,grow");
            FlatToggleButton toggleButton = new FlatToggleButton();
            toggleButton.setText("Toggle Me");
            panel.add(toggleButton, "cell 1 1,grow");

            // Tri-State Checkbox
            panel.add(new JLabel("FlatTriStateCheckBox:"), "cell 0 2,grow");
            FlatTriStateCheckBox triStateCheckBox = new FlatTriStateCheckBox("Tri-State");
            panel.add(triStateCheckBox, "cell 1 2,grow");

            // Checkbox
            panel.add(new JLabel("FlatCheckBox:"), "cell 0 3,grow");
            FlatCheckBox flatCB = new FlatCheckBox();
            flatCB.setText("Check Me");
            panel.add(flatCB, "cell 1 3,grow");

            // Radio Button
            panel.add(new JLabel("FlatRadioButton:"), "cell 0 4,grow");
            FlatRadioButton FlatRB = new FlatRadioButton();
            FlatRB.setText("Select Me");
            panel.add(FlatRB, "cell 1 4,grow");
            

            // Text Fields
            panel.add(new JLabel("FlatTextField:"), "cell 0 5,grow");
            panel.add(new FlatTextField(), "cell 1 5,grow");

            panel.add(new JLabel("FlatPasswordField:"), "cell 0 6,grow");
            panel.add(new FlatPasswordField(), "cell 1 6,grow");

            panel.add(new JLabel("FlatFormattedTextField:"), "cell 0 7,grow");
            panel.add(new FlatFormattedTextField(), "cell 1 7,grow");

            // Spinner
            panel.add(new JLabel("FlatSpinner:"), "cell 0 8,grow");
            FlatSpinner FlatSpinner = new FlatSpinner();
            FlatSpinner.setModel(new SpinnerNumberModel(5, 0, 10, 1));
            panel.add(FlatSpinner, "cell 1 8,grow");

            // ComboBox
            panel.add(new JLabel("FlatComboBox:"), "cell 0 9,grow");
            FlatComboBox<String> comboBox = new FlatComboBox<>();
            comboBox.addItem("Option 1");
            comboBox.addItem("Option 2");
            comboBox.addItem("Option 3");
            panel.add(comboBox, "cell 1 9,grow");

            // List
            panel.add(new JLabel("FlatList:"), "cell 0 10,grow");
            DefaultListModel<String> listModel = new DefaultListModel<>();
            listModel.addElement("Item 1");
            listModel.addElement("Item 2");
            listModel.addElement("Item 3");
            FlatList<String> list = new FlatList<>();
            list.setModel(listModel);
            panel.add(new JScrollPane(list), "cell 1 10,grow");

            // Label
            panel.add(new JLabel("FlatLabel:"), "cell 0 11,grow");
            FlatLabel flatLabel = new FlatLabel();
            flatLabel.setText("This is a FlatLabel");
            panel.add(flatLabel, "cell 1 11,grow");

            // Progress Bar
            panel.add(new JLabel("FlatProgressBar:"), "cell 0 12,grow");
            FlatProgressBar progressBar = new FlatProgressBar();
            progressBar.setValue(50);
            panel.add(progressBar, "cell 1 12,grow");

            // Slider
            panel.add(new JLabel("FlatSlider:"), "cell 0 13,grow");
            panel.add(new FlatSlider(), "cell 1 13,grow");

            // Tabbed Pane
            panel.add(new JLabel("FlatTabbedPane:"), "cell 0 14,grow");
            FlatTabbedPane tabbedPane = new FlatTabbedPane();
            tabbedPane.addTab("Tab 1", new JLabel("Content 1"));
            tabbedPane.addTab("Tab 2", new JLabel("Content 2"));
            panel.add(tabbedPane, "cell 1 14,grow");

            // Tree
            panel.add(new JLabel("FlatTree:"), "cell 0 15,grow");
            JTree tree = new FlatTree();
            tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Root") {{
                add(new DefaultMutableTreeNode("Node 1"));
                add(new DefaultMutableTreeNode("Node 2"));
            }}));
            panel.add(new JScrollPane(tree), "cell 1 15,grow");

            // Separator
            panel.add(new JLabel("FlatSeparator:"), "cell 0 16,grow");
            panel.add(new FlatSeparator(), "cell 1 16,grow");

            // Scroll Pane for Overflow
            JScrollPane scrollPane = new JScrollPane(panel);
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            
            frame.setVisible(true);
        });
    }
}
