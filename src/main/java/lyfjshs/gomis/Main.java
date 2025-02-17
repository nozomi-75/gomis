package lyfjshs.gomis;

import java.awt.EventQueue;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.components.GFrame;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.view.LoginView;
import lyfjshs.gomis.view.appointment.AppointmentMangement;
import lyfjshs.gomis.view.incident.IncidentFillUpForm;
import lyfjshs.gomis.view.incident.IncidentList;
import lyfjshs.gomis.view.sessions.SessionRecords;
import lyfjshs.gomis.view.sessions.SessionsForm;
import lyfjshs.gomis.view.students.StudentMangementGUI;
import lyfjshs.gomis.view.violation.ViolationFillUpForm;
import lyfjshs.gomis.view.violation.Violation_Record;
import raven.modal.Drawer;

/**
 * The Main class is responsible for initializing the application, setting up
 * the database connection, configuring the UI look and feel, and initializing
 * panels and the main frame.
 */
public class Main {

	/** Database connection instance */
	private static Connection conn;

	/** The main application frame */
	public static GFrame jFrame;

	/**
	 * The entry point of the application. Initializes database connection, sets up
	 * UI, and initializes the main frame.
	 * 
	 * @param args Command line arguments (not used in this application)
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initDB();
					initializeLookAndFeel();
					initiPanels();
					initFrame();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initializes the database connection by retrieving a connection instance from
	 * the DBConnection class. If the connection fails, an error message is
	 * displayed, and the program exits.
	 */
	private static void initDB() {
		try {
			conn = DBConnection.getConnection();
			if (conn == null) {
				throw new SQLException("Connection is null after initialization.");
			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Error connecting to the database: " + e.getMessage(),
					"Database Error: GMS-001", JOptionPane.ERROR_MESSAGE);
			System.exit(1); // Stop execution if DB connection fails
		}
	}

/**
     * Sets up the application's look and feel using FlatLaf.
     * Configures UI properties such as button and component arc styles.
     */
    public static void initializeLookAndFeel() {
        try {
            FlatLightLaf.setup();
            // Setup FlatLaf theme
            FlatRobotoFont.install();
            FlatLaf.registerCustomDefaultsSource("themes"); // Loads properties from "themes" package
            UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
            // FlatDarkLaf.setup();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error initializing Look and Feel: " + ex.getMessage(), 
                    "API Error: GMS-003", JOptionPane.ERROR_MESSAGE);
        }
        FlatLaf.registerCustomDefaultsSource("kotlin.themes");
        UIManager.put("Button.arc", 80);
        UIManager.put("Component.arc", 50);
        UIManager.put("TextComponent.arc", 50);
    }

	// instance of every panel
	private static LoginView loginPanel;
	private static IncidentFillUpForm incidentFillUp;
	private static StudentMangementGUI studManagement;
	private static Violation_Record vrList;
	private static ViolationFillUpForm violationFillUp;
	private static IncidentList incidentList;
	private static AppointmentMangement appointmentCalendar;
	private static SessionsForm sessionFillUp;
	private static SessionRecords sessionRecords;
	
	public static void initiPanels() {
		loginPanel = new LoginView(conn);
		incidentFillUp = new IncidentFillUpForm(conn);
		studManagement = new StudentMangementGUI(conn);
		vrList = new Violation_Record(conn);
		violationFillUp = new ViolationFillUpForm(conn);
        incidentList = new IncidentList(conn);
    	appointmentCalendar = new AppointmentMangement(conn);
    	sessionFillUp = new SessionsForm(conn);
		sessionRecords = new SessionRecords(conn);
	}

	/**
	 * Initializes and configures the main application frame. Installs the drawer
	 * navigation system and form manager.
	 */
	public static void initFrame() {
		jFrame = new GFrame(1000, 700, true, "GOMIS",null);
		jFrame.getRootPane().putClientProperty(FlatClientProperties.FULL_WINDOW_CONTENT, true);

		Drawer.installDrawer(jFrame, new DrawerBuilder(conn));
		FormManager.install(jFrame);

		jFrame.refresh();
	}
}
