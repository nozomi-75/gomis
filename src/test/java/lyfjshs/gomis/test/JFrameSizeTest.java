package lyfjshs.gomis.test;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JFrameSizeTest extends JFrame {

    public JFrameSizeTest() {
        setTitle("Frame Size Console");

        // Calculate the minimum size including insejts
        Insets insets = this.getInsets();
        int minWidth = 1590 + insets.left + insets.right;
        System.out.println("Minimum width: " + minWidth);

        int minHeight = 700 + insets.top + insets.bottom;
        System.out.println("Minimum height: " + minHeight);

        Dimension minSize = new Dimension(minWidth, minHeight);
        System.out.println("Minimum size: " + minSize);

        // setMinimumSize(minSize); // Set the minimum size

        setSize(new Dimension(1366, 720)); // Initial size, within bounds
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Add a component listener to track size changes
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                printFrameSize();
            }

            @Override
            public void componentShown(ComponentEvent e){
                printFrameSize();
            }
        });
    }

    private void printFrameSize() {
        Dimension size = getSize();
        System.out.println("Frame size: Width = " + size.width + ", Height = " + size.height);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrameSizeTest frame = new JFrameSizeTest();
            frame.setVisible(true);
            frame.printFrameSize();
        });
    }
}
