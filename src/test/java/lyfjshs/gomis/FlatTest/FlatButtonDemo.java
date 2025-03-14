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
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType;
import javax.swing.JLabel;

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
			frame.setSize(704, 244);
			frame.getContentPane().setLayout(new GridLayout(2, 2, 10, 10));
			frame.setLocationRelativeTo(null); // Center the window

			JPanel panel = new JPanel(new MigLayout("", "[grow][][grow][][grow][][grow]", "[][][][][]"));

			JLabel titleLabel = new JLabel("Management");
			titleLabel.putClientProperty("FlatLaf.styleClass", "h1");
			panel.add(titleLabel, "cell 0 0 4 1,growx,aligny center");

			JLabel violationLabel = new JLabel("Violation");
			violationLabel.putClientProperty("FlatLaf.styleClass", "large");
			panel.add(violationLabel, "cell 1 2,alignx center");

			JLabel appointmentLabel = new JLabel("Appointment");
			appointmentLabel.putClientProperty("FlatLaf.styleClass", "large");
			panel.add(appointmentLabel, "cell 3 2,alignx center");

			JLabel studentLabel = new JLabel("Student");
			studentLabel.putClientProperty("FlatLaf.styleClass", "large");
			panel.add(studentLabel, "cell 5 2,alignx center");

			FlatButton standardButton_1 = new FlatButton();
			standardButton_1.setText("View Violation");
			standardButton_1.setButtonType(ButtonType.none);
			panel.add(standardButton_1, "cell 1 3");

			FlatButton standardButton_2 = new FlatButton();
			standardButton_2.setText("Set Appointment");
			standardButton_2.setButtonType(ButtonType.none);
			panel.add(standardButton_2, "flowx,cell 3 3");

			FlatButton standardButton_3 = new FlatButton();
			standardButton_3.setText("View Appointments");
			standardButton_3.setButtonType(ButtonType.none);
			panel.add(standardButton_3, "cell 3 3");

			FlatButton standardButton_4 = new FlatButton();
			standardButton_4.setText("View Students");
			standardButton_4.setButtonType(ButtonType.none);
			panel.add(standardButton_4, "cell 5 3");
			frame.getContentPane().add(panel);

			JPanel flatBtnsPanel = new JPanel(new MigLayout("", "[][grow][][grow][][grow][][grow][]", "[][][]"));
			frame.getContentPane().add(flatBtnsPanel);

			// Square FlatButton
			FlatButton squareButton = new FlatButton();
			flatBtnsPanel.add(squareButton, "cell 5 0,growx");
			squareButton.setText("Square");
			squareButton.setButtonType(FlatButton.ButtonType.square);
			squareButton.setBackground(new Color(0xFF9800)); // Orange background
			squareButton.setForeground(Color.WHITE);

			// Standard FlatButton
			FlatButton standardButton = new FlatButton();
			flatBtnsPanel.add(standardButton, "cell 1 1,growx");
			standardButton.setText("Standard");
			standardButton.setButtonType(FlatButton.ButtonType.none);

			// RoundRect FlatButton (using FlatClientProperties)
			FlatButton roundButton = new FlatButton();
			flatBtnsPanel.add(roundButton, "cell 3 2,growx");
			roundButton.setText("Round Rect");
			roundButton.setButtonType(FlatButton.ButtonType.roundRect);
			roundButton.putClientProperty(FlatClientProperties.STYLE, "arc: 10"); // Rounded corners
			roundButton.setBackground(new Color(0x4CAF50)); // Green background
			roundButton.setForeground(Color.WHITE);

			// Toolbar FlatButton
			FlatButton toolBarButton = new FlatButton();
			flatBtnsPanel.add(toolBarButton, "cell 7 2,growx");
			toolBarButton.setText("Toolbar");
			toolBarButton.setButtonType(FlatButton.ButtonType.toolBarButton);
			toolBarButton.setBackground(new Color(0x2196F3)); // Blue background
			toolBarButton.setForeground(Color.WHITE);

			frame.setVisible(true);
		});
	}
}
