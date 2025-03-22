package lyfjshs.gomis.test;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.formdev.flatlaf.FlatIntelliJLaf;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.JLabel;

public class MainTest extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FlatIntelliJLaf.setup();
                    MainTest frame = new MainTest();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public MainTest() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(null);
        contentPane = new JPanel(new MigLayout("insets 0", "[grow]", "[][grow]"));
        setContentPane(contentPane);
        
        ComboBoxPanel comboBoxPanel = new ComboBoxPanel();
        contentPane.add(comboBoxPanel, "cell 0 0,growx");

        JPanel panel = new JPanel(new MigLayout("insets 0", "[grow]", "[][]"));
        panel.setBackground(Color.CYAN);
        contentPane.add(panel, "cell 0 1,grow");

        JLabel lblNewLabel = new JLabel("THIS PANEL SHOULD EXPAND ONLY SHOWING A BUTTON ABOVE");
        panel.add(lblNewLabel, "cell 0 0,growx,aligny top");

        JLabel lblNewLabel_1 = new JLabel("THAT WHEN CLICKED SHOWS A COLLAPSABLE PANEL JUST LIKE A COMBOBOX");
        panel.add(lblNewLabel_1, "cell 0 1,growx");
    }
}
