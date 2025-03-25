package lyfjshs.gomis;

import java.awt.EventQueue;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.components.GFrame;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.settings.SettingsManager;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.incident.IncidentFillUpForm;
import lyfjshs.gomis.view.incident.IncidentList;
import lyfjshs.gomis.view.loading.SplashScreenFrame;
import lyfjshs.gomis.view.login.LoginView;
import lyfjshs.gomis.view.sessions.SessionRecords;
import lyfjshs.gomis.view.sessions.SessionsForm;
import lyfjshs.gomis.view.students.StudentMangementGUI;
import lyfjshs.gomis.view.students.create.StudentInfoFullForm;
import lyfjshs.gomis.view.violation.Violation_Record;

/**
 * The Main class is responsible for initializing the application, setting up
 * the database connection, configuring the UI look and feel, and initializing
 * panels and the main frame.
 */
/**
 * 
 */
@SuppressWarnings("unused")
public class Main {

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
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				SplashScreenFrame splash = new SplashScreenFrame();
				splash.setVisible(true); // Show the splash screen
				splash.runInitialization(); // Run initialization
			}
		});
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
			FlatRobotoFont.install();
			FlatLaf.registerCustomDefaultsSource("lyfjshs.themes");
			
			// Initialize and apply settings
			settings.initializeAppSettings();
			
			// Set default UI properties
			UIManager.put("Button.arc", 80);
			UIManager.put("Component.arc", 50);
			UIManager.put("TextComponent.arc", 50);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"Error initializing Look and Feel: " + ex.getMessage(),
				"API Error: GMS-003",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	// instance of every Form Panel
	public static LoginView loginPanel;
	private static IncidentFillUpForm incidentFillUp;
	private static StudentMangementGUI studManagement;
	private static Violation_Record vrList;
	private static IncidentList incidentList;
	public static AppointmentManagement appointmentCalendar;
	private static SessionsForm sessionFillUp;
	private static SessionRecords sessionRecords;
	private static StudentInfoFullForm createStudent;

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

			sessionFillUp = new SessionsForm(conn);
			validateComponent(sessionFillUp, "SessionsForm");

			vrList = new Violation_Record(conn);
			validateComponent(vrList, "Violation_Record");

			sessionRecords = new SessionRecords(conn);
			validateComponent(sessionRecords, "SessionRecords");

			createStudent = new StudentInfoFullForm(conn);
			validateComponent(createStudent, "StudentInfoFullForm");

			incidentFillUp = new IncidentFillUpForm(conn);
			validateComponent(incidentFillUp, "IncidentFillUpForm");

			incidentList = new IncidentList(conn);
			validateComponent(incidentList, "IncidentList");

			studManagement = new StudentMangementGUI(conn);
			validateComponent(studManagement, "StudentManagementGUI");

		} catch (Exception e) {
			e.printStackTrace();
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
			// Initialize look and feel first
			initializeLookAndFeel();
			
			// Initialize database connection
			initDB();
			
			// Create main frame with proper size and visibility
			gFrame = new GFrame(1380, 750, false, "GOMIS", null, conn);
			gFrame.getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);
			
			// Install form manager before initializing panels
			FormManager.install(gFrame);
			formManager = new FormManager();
			
			// Initialize panels AFTER frame and form manager are ready
			initiPanels();
			
			// Make frame visible LAST
			gFrame.setVisible(true);
			gFrame.refresh();
			
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
				"Error initializing application: " + e.getMessage(),
				"Initialization Error",
				JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

}