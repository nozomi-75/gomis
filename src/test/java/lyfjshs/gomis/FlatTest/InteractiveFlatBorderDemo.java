package lyfjshs.gomis.FlatTest;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class InteractiveFlatBorderDemo {
	private static JTextField textField_1;
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Interactive FlatBorder Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.getContentPane().setLayout(new FlowLayout());
            frame.setLocationRelativeTo(null);

            // Create a JTextField with a default border
            JTextField textField = new JTextField(15);
            textField.putClientProperty(FlatClientProperties.STYLE, 
                "borderWidth: 2; borderColor: #B0BEC5; arc: 10");

            // Add focus listener to change the border color on focus
            textField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    textField.putClientProperty(FlatClientProperties.STYLE, 
                        "borderWidth: 2; borderColor: #2196F3; arc: 10"); // Blue border when focused
                }

                @Override
                public void focusLost(FocusEvent e) {
                    textField.putClientProperty(FlatClientProperties.STYLE, 
                        "borderWidth: 2; borderColor: #B0BEC5; arc: 10"); // Gray border when unfocused
                }
            });

            frame.getContentPane().add(new JLabel("Click inside the field:"));
            frame.getContentPane().add(textField);
            
            textField_1 = new JTextField();
            frame.getContentPane().add(textField_1);
            textField_1.setColumns(10);
            frame.setVisible(true);
        });
    }
}
