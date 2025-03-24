/**
 * The TestAnimation class in Java creates a Swing GUI application with animated slide transitions using SlidePane and buttons to trigger different slides.
 */
package lyfjshs.gomis.test.forms;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import lyfjshs.gomis.test.simple.SimpleInputForms;
import lyfjshs.gomis.test.simple.SimpleInputForms2;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;


public class TestAnimation {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the main frame
            JFrame frame = new JFrame("Test Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            // Create the SlidePane for animated transitions
            SlidePane slidePane = new SlidePane();
            frame.add(slidePane);

            // Create buttons to trigger slide transitions
            JButton btnSlide1 = new JButton("Show Slide 1");
            JButton btnSlide2 = new JButton("Show Slide 2");

            // Add action listeners to buttons to show different slides with animations
            btnSlide1.addActionListener(e -> {
                slidePane.addSlide(new SimpleInputForms(), SlidePaneTransition.Type.ZOOM_IN);
            });

            btnSlide2.addActionListener(e -> {
                slidePane.addSlide(new SimpleInputForms2(), SlidePaneTransition.Type.BACK);
            });

            // Create a panel to hold the buttons
            JPanel panel = new JPanel();
            panel.add(btnSlide1);
            panel.add(btnSlide2);

            // Add the panel to the frame
            frame.add(panel, "North");
            // The line `frame.setVisible(true);` in the Java code snippet is setting the visibility of the JFrame object `frame` to true, which means it will make the frame visible on the screen. This method call is necessary to display the Swing GUI application window created by the JFrame with all its components like buttons, panels, and slide transitions.
            frame.setVisible(true);
        });
    }
}
