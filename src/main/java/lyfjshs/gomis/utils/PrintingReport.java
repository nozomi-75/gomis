package lyfjshs.gomis.utils;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperPrintManager;

public class PrintingReport {
	public static void generateReport(JFrame parent, JasperReport jasperReport, Map<String, Object> parameters,
			String fileName, String windowTitle) {
		if (jasperReport == null) {
			showErrorDialog(parent, "Template is not loaded properly.");
			return;
		}

		try {
			// Fill report
			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

			// Print the report
			boolean printSuccess = JasperPrintManager.printReport(jasperPrint, true);
			if (printSuccess) {
				JOptionPane.showMessageDialog(parent, "Report sent to printer successfully.");
			} else {
				showErrorDialog(parent, "Printing was canceled or failed.");
			}
		} catch (Exception e) {
			showErrorDialog(parent, "Error printing report: " + e.getMessage());
		}
	}

	public static BufferedImage convertSvgToBufferedImage(FlatSVGIcon svgIcon) {
		if (svgIcon == null)
			return null;
		Image image = svgIcon.getImage();
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		bufferedImage.getGraphics().drawImage(image, 0, 0, null);
		return bufferedImage;
	}

	private static void showErrorDialog(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}
}
