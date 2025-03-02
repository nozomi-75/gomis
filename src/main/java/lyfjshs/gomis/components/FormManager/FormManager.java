package lyfjshs.gomis.components.FormManager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.view.LoginView;
import lyfjshs.gomis.view.MainDashboard;
import raven.modal.Drawer;

/**
 * The {@code FormManager} class is responsible for managing the application's form navigation, 
 * including login, logout, and displaying various forms in the main application frame.
 */
public class FormManager {

    private static JFrame frame;
    private static MainForm mainForm;
    private static LoginView login;
    private static String counselorFIRST_NAME;
    private static String counselorLAST_NAME;
    private static String counselorPosition;

    /**
     * Initializes the form manager with a given JFrame and logs out the current user by default.
     * 
     * @param f The main application frame.
     */
    public static void install(JFrame f) {
        frame = f;
        logout();
    }

    /**
     * Displays a given form within the main application window.
     * 
     * @param form The form to be displayed.
     */
    public static void showForm(Form form) {
        form.formCheck();
        form.formOpen();
        mainForm.setForm(form);
    }

    /**
     * Handles user login by setting the main form in the application window 
     * and displaying the navigation drawer.
     */
    public static void login(Connection conn) {
        Drawer.installDrawer(Main.jFrame, new DrawerBuilder(conn));
        Drawer.setVisible(true);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(getMainForm());

        Drawer.setSelectedItemClass(MainDashboard.class);
        frame.repaint();
        frame.revalidate();
    }

    /**
     * Handles user logout by removing all content from the frame, hiding the drawer,
     * and displaying the login form.
     */
    public static void logout() {
        frame.getContentPane().removeAll();
        Connection conn;
        try {
            conn = DBConnection.getConnection();
            Form login = getLogin(conn);
            login.repaint();
            login.revalidate();
            login.formCheck();
            frame.getContentPane().add(login);
        } catch (SQLException e) {
            e.printStackTrace(); // Log database connection error
        }

        frame.repaint();
        frame.revalidate();
    }

    /**
     * Retrieves the main application frame.
     * 
     * @return The main application frame.
     */
    public static JFrame getFrame() {
        return frame;
    }

    /**
     * Retrieves the main form instance. If it does not exist, a new one is created.
     * 
     * @return The {@code MainForm} instance.
     */
    private static MainForm getMainForm() {
        if (mainForm == null) {
            mainForm = new MainForm();
        }
        return mainForm;
    }

    /**
     * Retrieves the login form instance, creating a new one if it does not exist.
     * 
     * @param conn The database connection used for authentication.
     * @return The {@code LoginView} instance.
     */
    private static LoginView getLogin(Connection conn) {
        if (login == null) {
            login = new LoginView(conn);
        }
        return login;
    }

    public static void setCounselorDetails(String FIRST_NAME, String LAST_NAME, String position) {
        counselorFIRST_NAME = FIRST_NAME;
        counselorLAST_NAME = LAST_NAME;
        counselorPosition = position;

    }
    
    public static String getCounselorFullName() {
        return counselorFIRST_NAME + " " + counselorLAST_NAME;
    }
    
    public static String getCounselorPosition() {
        return counselorPosition;
    }


}
