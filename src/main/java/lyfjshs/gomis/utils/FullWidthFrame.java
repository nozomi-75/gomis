package lyfjshs.gomis.utils;

import java.awt.Dimension;
import java.time.LocalDate;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.DropPanel;
import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;
import javax.swing.JButton;

public class FullWidthFrame extends JFrame {
	private JTextField fullNameField;
	private JTextField workPositionField;
	private JasperPreviewPanel previewPanel;

	public FullWidthFrame() {
	
	}

	private static String getOrdinalSuffix(int day) {
		if (day >= 11 && day <= 13) {
			return "th";
		}
		switch (day % 10) {
		case 1:
			return "st";
		case 2:
			return "nd";
		case 3:
			return "rd";
		default:
			return "th";
		}
	}

	public static void main(String[] args) {
		// Apply FlatLaf theme
		try {
			UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Run the JFrame on the Event Dispatch Thread
		SwingUtilities.invokeLater(() -> new FullWidthFrame());
	}
}
