package lyfjshs.gomis.FlatTest;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import net.miginfocom.swing.MigLayout;

public class FlatButtonDemo {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new FlatMacDarkLaf());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("FlatButton Example");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(529, 378);
			frame.getContentPane().setLayout(new GridLayout(2, 2, 10, 10));
			frame.setLocationRelativeTo(null); // Center the window

			JPanel flatBtnsPanel = new JPanel(new MigLayout("", "[grow][grow][grow][grow]", "[][][]"));
			frame.getContentPane().add(flatBtnsPanel);

			// Square FlatButton
			FlatButton squareButton = new FlatButton();
			flatBtnsPanel.add(squareButton, "cell 2 0,growx");
			squareButton.setText("Square");
			squareButton.setButtonType(FlatButton.ButtonType.square);
			squareButton.setBackground(new Color(0xFF9800)); // Orange background
			squareButton.setForeground(Color.WHITE);

			// Standard FlatButton
			FlatButton standardButton = new FlatButton();
			flatBtnsPanel.add(standardButton, "cell 0 1,growx");
			standardButton.setText("Standard");
			standardButton.setButtonType(FlatButton.ButtonType.none);

			// RoundRect FlatButton (using FlatClientProperties)
			FlatButton roundButton = new FlatButton();
			flatBtnsPanel.add(roundButton, "cell 1 2,growx");
			roundButton.setText("Round Rect");
			roundButton.setButtonType(FlatButton.ButtonType.roundRect);
			roundButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10"); // Rounded corners
			roundButton.setBackground(new Color(0x4CAF50)); // Green background
			roundButton.setForeground(Color.WHITE);

			// Toolbar FlatButton
			FlatButton toolBarButton = new FlatButton();
			flatBtnsPanel.add(toolBarButton, "cell 3 2,growx");
			toolBarButton.setText("Toolbar");
			toolBarButton.setButtonType(FlatButton.ButtonType.toolBarButton);
			toolBarButton.setBackground(new Color(0x2196F3)); // Blue background
			toolBarButton.setForeground(Color.WHITE);

			frame.setVisible(true);
		});
	}
}
