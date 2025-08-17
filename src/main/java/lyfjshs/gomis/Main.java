/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis;

import java.awt.EventQueue;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.components.GFrame;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.utils.SystemOutCapture;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.incident.IncidentList;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.IncidentFillUpFormPanel;
import lyfjshs.gomis.view.loading.SplashScreenFrame;
import lyfjshs.gomis.view.login.LoginView;
import lyfjshs.gomis.view.sessions.SessionRecords;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import lyfjshs.gomis.view.students.StudentsListMain;
import lyfjshs.gomis.view.students.create.CreateStudentData;
import lyfjshs.gomis.view.students.schoolForm.ImportSF;
import lyfjshs.gomis.view.violation.Violation_Record;

/**
 * The Main class is responsible for initializing the application, setting up
 * the database connection, configuring the UI look and feel, and initializing
 * panels and the main frame.
 */
public class Main {

	private static final Logger logger = LogManager.getLogger(Main.class);

	/** Database connection instance */
	private static Connection conn;

	/** The main application frame */
	public static GFrame gFrame;

	/**
	 * The entry point of the application. Initializes database connection, sets up
	 * UI, and initializes the main frame.
	 * 
	 * @param args Command line arguments (not used in this application)
	 */
	public static void main(String[] args) {
		try {
			// --- LOGGING FIX START ---
			// 1. Force Log4j2 to load config from classpath
			System.setProperty("log4j.configurationFile", "log4j2.xml");
			// 2. Ensure C:/gomisLogs exists
			new File("C:/gomisLogs").mkdirs();
			// 3. Redirect System.out/err to Log4j2
			SystemOutCapture.redirectSystemStreamsToLog4j();
			// --- LOGGING FIX END ---

			// 1. Try to use the application directory (where GOMIS.exe is located)
			String appDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			File logDir = new File(appDir, "logs");
			boolean canWrite = true;
			if (!logDir.exists()) canWrite = logDir.mkdirs();
			else canWrite = logDir.canWrite();

			// 2. If not writable, fallback to C:\gomisLogs (as per installer)
			if (!canWrite) {
				String fallback = "C:\\gomisLogs";
				logDir = new File(fallback);
				if (!logDir.exists()) logDir.mkdirs();
			}

			// 3. Set the LOG_DIR property for Log4j2
			System.setProperty("LOG_DIR", logDir.getAbsolutePath());

			// Set global uncaught exception handler
			Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
				logger.error("Uncaught exception in thread " + t.getName(), e);
			});
			
			// Add shutdown hook for proper cleanup
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				logger.info("Application shutdown initiated...");
				try {
					// Cleanup EventBus
					lyfjshs.gomis.utils.EventBus.shutdown();
					
					// Cleanup NotificationManager
					if (gFrame != null && gFrame.getNotificationManager() != null) {
						gFrame.getNotificationManager().shutdown();
					}
					
					// Cleanup AlarmManagement
					lyfjshs.gomis.components.alarm.AlarmManagement.getInstance().shutdown();
					
					// Close database connections
					if (conn != null) {
						lyfjshs.gomis.Database.DBConnection.closeAllConnections();
					}
					
					logger.info("Application shutdown completed successfully");
				} catch (Exception e) {
					logger.error("Error during application shutdown", e);
				}
			}, "GOMIS-ShutdownHook"));
			
			logger.info("GOMIS Application starting...");
			logger.info("Log file: " + logDir.getAbsolutePath() + "/gomis.log");
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					SplashScreenFrame splash = new SplashScreenFrame();
					splash.setVisible(true); // Show the splash screen
					// Set the parent component for DBConnection dialogs
					DBConnection.setParentComponent(splash);
					splash.runInitialization(); // Run initialization
				}
			});
		} catch (Exception e) {
			logger.error("Error during application startup", e);
		}
	}

	/**
	 * Initializes the database connection by retrieving a connection instance from
	 * the DBConnection class. If the connection fails, an error message is
	 * displayed, and the program exits.
	 */
	public static void initDB() {
		try {
			conn = DBConnection.getConnection();

			if (conn == null) {
				throw new SQLException("Connection is null after initialization.");
			}

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Error connecting to the database: " + e.getMessage(),
					"Database Error: GMS-001", JOptionPane.ERROR_MESSAGE);
		}
	}

	public static SettingsManager settings;

	/**
	 * Sets up the application's look and feel using FlatLaf. Configures UI
	 * properties such as button and component arc styles.
	 */
	public static void initializeLookAndFeel() {
		try {
			// Initialize settings first
			settings = new SettingsManager(conn);
			
			// Install basic requirements
//			FlatRobotoFont.install();
			FlatLaf.registerCustomDefaultsSource("lyfjshs.themes");
			
			// Initialize and apply settings
			settings.initializeAppSettings();
			
			// Set default UI properties
			UIManager.put("Button.arc", 40);
			UIManager.put("Component.arc", 25);
			UIManager.put("TextComponent.arc", 25);
			
		} catch (Exception ex) {
			logger.error("Error initializing Look and Feel", ex);
			JOptionPane.showMessageDialog(null,
				"Error initializing Look and Feel: " + ex.getMessage(),
				"API Error: GMS-003",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// instance of every Form Panel
	public static LoginView loginPanel;
	public static Violation_Record vrList;
	private static IncidentFillUpFormPanel incidentFillUp;
	private static StudentsListMain studManagement;
	private static IncidentList incidentList;
	public static AppointmentManagement appointmentCalendar;
	private static SessionsFillUpFormPanel sessionFillUp;
	private static SessionRecords sessionRecords;
	private static CreateStudentData createStudent;
	public static ImportSF importSF;

	private static void initiPanels() {
		try {
			// Ensure frame is initialized first
			if (gFrame == null) {
				throw new IllegalStateException("Main frame must be initialized before panels");
			}

			// Initialize panels with proper parent and validation
			loginPanel = new LoginView(conn);
			validateComponent(loginPanel, "LoginView");

			appointmentCalendar = new AppointmentManagement(conn);
			validateComponent(appointmentCalendar, "AppointmentManagement");

			sessionFillUp = new SessionsFillUpFormPanel(conn);
			validateComponent(sessionFillUp, "SessionsFillUpFormPanel");

			vrList = new Violation_Record(conn);
			validateComponent(vrList, "Violation_Record");

			sessionRecords = new SessionRecords(conn);
			validateComponent(sessionRecords, "SessionRecords");

			createStudent = new CreateStudentData(conn);
			validateComponent(createStudent, "StudentInfoFullForm");

			incidentFillUp = new IncidentFillUpFormPanel(conn);
			validateComponent(incidentFillUp, "IncidentFillUpFormPanel");

			incidentList = new IncidentList(conn);
			validateComponent(incidentList, "IncidentList");

			studManagement = new StudentsListMain(conn);
			validateComponent(studManagement, "StudentManagementGUI");
			
			importSF = new ImportSF(conn);
			validateComponent(importSF, "ImportSF");

		} catch (Exception e) {
			logger.error("Error initializing panels", e);
			JOptionPane.showMessageDialog(null,
				"Error initializing panels: " + e.getMessage(),
				"Initialization Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void validateComponent(JComponent component, String componentName) {
		if (component == null) {
			throw new IllegalStateException(componentName + " failed to initialize");
		}

		// Force immediate validation
		component.revalidate();
		component.repaint();
	}

	public static FormManager formManager;

	/**
	 * Initializes and configures the main application frame. Installs the drawer
	 * navigation system and form manager.
	 */
	public static void initFrame() {
		try {
			logger.info("Initializing application frame...");
			// Initialize look and feel first
			logger.info("Setting up look and feel...");
			initializeLookAndFeel();
			
			// Initialize template system
			logger.info("Initializing template system...");
			try {
				docPrinter.templateManager.initializeTemplateSystem();
			} catch (Exception e) {
				logger.warn("Template system initialization failed: " + e.getMessage());
				// Don't fail the entire startup for template issues
			}
			
			// Initialize database connection
			logger.info("Initializing database connection...");
			initDB();
			// Load the application icon
			ImageIcon appIcon = null;
			try {
				java.net.URL iconURL = Main.class.getResource("/images/app_icon.png");
				if (iconURL != null) {
					appIcon = new ImageIcon(iconURL);
				} else {
					logger.warn("Application icon resource not found: /images/app_icon.png");
				}
			} catch (Exception e) {
				logger.error("Error loading application icon", e);
			}
			// Create main frame with proper size and visibility
			logger.info("Creating main application frame...");
			gFrame = new GFrame(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, 850, false, "GOMIS", appIcon, conn);
			gFrame.getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
			// Update parent component for DBConnection dialogs
			DBConnection.setParentComponent(gFrame);
			// Initialize notifications
			logger.info("Initializing notification system...");
			gFrame.initializeNotifications(conn);
			// Install form manager before initializing panels
			logger.info("Installing form manager...");
			FormManager.install(gFrame);
			formManager = new FormManager();
			// Initialize panels AFTER frame and form manager are ready
			logger.info("Initializing application panels...");
			initiPanels();
			// Initialize and start the alarm management system
			logger.info("Initializing alarm management system...");
			if (appointmentCalendar != null) {
				appointmentCalendar.initializeAlarmSystem();
			}
			// Make frame visible LAST
			logger.info("Making application frame visible...");
			gFrame.setVisible(true);
			gFrame.refresh();
			logger.info("Application initialization completed successfully!");
		} catch (Exception e) {
			logger.error("Error during application initialization", e);
			JOptionPane.showMessageDialog(null,
				"Error initializing application: " + e.getMessage(),
				"Initialization Error",
				JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}