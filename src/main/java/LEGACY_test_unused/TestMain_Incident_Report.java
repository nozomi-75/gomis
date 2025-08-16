package LEGACY_test_unused;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import docPrinter.templateManager;
import docPrinter.incidentReport.incidentReportGenerator;
import net.miginfocom.swing.MigLayout;

public class TestMain_Incident_Report extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(TestMain_Incident_Report.class);
	private JPanel panel;
	private JButton btnNewButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestMain_Incident_Report frame = new TestMain_Incident_Report();
					frame.setVisible(true);
				} catch (Exception e) {
					logger.error("Error launching incident report test application", e);
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TestMain_Incident_Report() {
		// Use templateManager to get default output folder
		File outputFolder = templateManager.getDefaultOutputFolder();
		
		// Ensure output folder exists
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(718, 607));
		panel = new JPanel();
		setContentPane(panel);
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		btnNewButton = new JButton("Generate Incident Report Template");
		btnNewButton.addActionListener(e -> {
			try {
				incidentReportGenerator incidentReportGenerator = new incidentReportGenerator();
				boolean success = incidentReportGenerator.generateIncidentReport(outputFolder, null, "print");
				
				if (success) {
					logger.info("Incident report template generated successfully in: " + outputFolder.getAbsolutePath());
				} else {
					logger.error("Failed to generate incident report template");
				}
			} catch (Exception ex) {
				logger.error("Error generating incident report template", ex);
			}
		});
		panel.add(btnNewButton, "cell 0 0");
	}

}
