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
            frame.setLayout(new BorderLayout());
            frame.setLocationRelativeTo(null);

            // Main Panel
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0, 2, 10, 10)); // Two columns layout

            // Buttons
            panel.add(new JLabel("FlatButton:"));
            FlatButton flatButton = new FlatButton();
            flatButton.setText("Click Me");
            flatButton.setButtonType(FlatButton.ButtonType.roundRect);
            panel.add(flatButton);

            // Toggle Button
            panel.add(new JLabel("FlatToggleButton:"));
            FlatToggleButton toggleButton = new FlatToggleButton();
            toggleButton.setText("Toggle Me");
            panel.add(toggleButton);

            // Tri-State Checkbox
            panel.add(new JLabel("FlatTriStateCheckBox:"));
            FlatTriStateCheckBox triStateCheckBox = new FlatTriStateCheckBox("Tri-State");
            panel.add(triStateCheckBox);

            // Checkbox
            panel.add(new JLabel("FlatCheckBox:"));
            FlatCheckBox flatCB = new FlatCheckBox();
            flatCB.setText("Check Me");
            panel.add(flatCB);

            // Radio Button
            panel.add(new JLabel("FlatRadioButton:"));
            FlatRadioButton FlatRB = new FlatRadioButton();
            FlatRB.setText("Select Me");
            panel.add(FlatRB);
            

            // Text Fields
            panel.add(new JLabel("FlatTextField:"));
            panel.add(new FlatTextField());

            panel.add(new JLabel("FlatPasswordField:"));
            panel.add(new FlatPasswordField());

            panel.add(new JLabel("FlatFormattedTextField:"));
            panel.add(new FlatFormattedTextField());

            // Spinner
            panel.add(new JLabel("FlatSpinner:"));
            FlatSpinner FlatSpinner = new FlatSpinner();
            FlatSpinner.setModel(new SpinnerNumberModel(5, 0, 10, 1));
            panel.add(FlatSpinner);

            // ComboBox
            panel.add(new JLabel("FlatComboBox:"));
            FlatComboBox<String> comboBox = new FlatComboBox<>();
            comboBox.addItem("Option 1");
            comboBox.addItem("Option 2");
            comboBox.addItem("Option 3");
            panel.add(comboBox);

            // List
            panel.add(new JLabel("FlatList:"));
            DefaultListModel<String> listModel = new DefaultListModel<>();
            listModel.addElement("Item 1");
            listModel.addElement("Item 2");
            listModel.addElement("Item 3");
            FlatList<String> list = new FlatList<>();
            list.setModel(listModel);
            panel.add(new JScrollPane(list));

            // Label
            panel.add(new JLabel("FlatLabel:"));
            FlatLabel flatLabel = new FlatLabel();
            flatLabel.setText("This is a FlatLabel");
            panel.add(flatLabel);

            // Progress Bar
            panel.add(new JLabel("FlatProgressBar:"));
            FlatProgressBar progressBar = new FlatProgressBar();
            progressBar.setValue(50);
            panel.add(progressBar);

            // Slider
            panel.add(new JLabel("FlatSlider:"));
            panel.add(new FlatSlider());

            // Tabbed Pane
            panel.add(new JLabel("FlatTabbedPane:"));
            FlatTabbedPane tabbedPane = new FlatTabbedPane();
            tabbedPane.addTab("Tab 1", new JLabel("Content 1"));
            tabbedPane.addTab("Tab 2", new JLabel("Content 2"));
            panel.add(tabbedPane);

            // Tree
            panel.add(new JLabel("FlatTree:"));
            JTree tree = new FlatTree();
            tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Root") {{
                add(new DefaultMutableTreeNode("Node 1"));
                add(new DefaultMutableTreeNode("Node 2"));
            }}));
            panel.add(new JScrollPane(tree));

            // Separator
            panel.add(new JLabel("FlatSeparator:"));
            panel.add(new FlatSeparator());

            // Scroll Pane for Overflow
            JScrollPane scrollPane = new JScrollPane(panel);
            frame.add(scrollPane, BorderLayout.CENTER);
            
            frame.setVisible(true);
        });
    }
}
