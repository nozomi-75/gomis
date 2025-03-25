package lyfjshs.gomis;

import java.awt.EventQueue;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

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
import lyfjshs.gomis.view.violation.ViolationFillUpForm;
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
			
			FlatRobotoFont.install();
			FlatLightLaf.setup();

//			FlatLaf.registerCustomDefaultsSource("lyfjshs.themes"); 

			UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
			UIManager.put("Button.arc", 80);
			UIManager.put("Component.arc", 50);
			UIManager.put("TextComponent.arc", 50);

			settings = new SettingsManager(conn);
			settings.initializeAppSettings();

		

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Error initializing Look and Feel: " + ex.getMessage(),
					"API Error: GMS-003", JOptionPane.ERROR_MESSAGE);
		}
	}

	// instance of every Form Panel
	public static LoginView loginPanel;
	private static IncidentFillUpForm incidentFillUp;
	private static StudentMangementGUI studManagement;
	private static Violation_Record vrList;
	private static ViolationFillUpForm violationFillUp;
	private static IncidentList incidentList;
	private static AppointmentManagement appointmentCalendar;
	private static SessionsForm sessionFillUp;
	private static SessionRecords sessionRecords;
	private static StudentInfoFullForm createStudent;

	public static void initiPanels() {
		loginPanel = new LoginView(conn);
		appointmentCalendar = new AppointmentManagement(conn);
		sessionFillUp = new SessionsForm(conn);
		vrList = new Violation_Record(conn);
		violationFillUp = new ViolationFillUpForm(conn);
		sessionRecords = new SessionRecords(conn);
		createStudent = new StudentInfoFullForm(conn);
		incidentFillUp = new IncidentFillUpForm(conn);
		incidentList = new IncidentList(conn);
		studManagement = new StudentMangementGUI(conn);
	}

	public static FormManager formManager;

	/**
	 * Initializes and configures the main application frame. Installs the drawer
	 * navigation system and form manager.
	 */
	public static void initFrame() {
		initializeLookAndFeel();
		initDB();
		gFrame = new GFrame(1380, 750, false, "GOMIS", null, conn);
		initiPanels();

		FormManager.install(gFrame);
		formManager = new FormManager();

		gFrame.refresh();
	}

}