package lyfjshs.gomis.FlatTest;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import net.miginfocom.swing.MigLayout;

public class FlatSVGTest {
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(() -> {
			FlatMacDarkLaf.setup();
			JFrame frame = new JFrame("Dynamic Resize Example");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(300, 200);

			JPanel panel = new JPanel();
			frame.getContentPane().add(panel);

			// Load the original SVG icon
			FlatSVGIcon originalIcon = new FlatSVGIcon("LYFJSHS-optimized.svg");
			panel.setLayout(new MigLayout("", "[57px]", "[33px]"));

			// Create a button with the original icon
			JButton button = new JButton(originalIcon);
			panel.add(button, "cell 0 0,grow");

			// Add mouse listener to dynamically resize the icon on hover
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					// Resize the icon when the mouse enters the button
					FlatSVGIcon resizedIcon = originalIcon.derive(originalIcon.getIconWidth() + 10,
							originalIcon.getIconHeight() + 10);
					button.setIcon(resizedIcon);
					button.revalidate();
					button.repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// Restore the original icon size when the mouse exits the button
					button.setIcon(originalIcon);
					button.revalidate();
					button.repaint();
				}
			});

			frame.setVisible(true);
		});
	}
}
