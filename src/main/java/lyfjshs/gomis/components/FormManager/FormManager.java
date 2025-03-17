package lyfjshs.gomis.components.FormManager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.view.LoginView;
import lyfjshs.gomis.view.MainDashboard;
import raven.modal.Drawer;

/**
 * The {@code FormManager} class is responsible for managing the application's
 * form navigation,
 * including login, logout, and displaying various forms in the main application
 * frame.
 */
public class FormManager {

    private static JFrame frame;
    private static MainForm mainForm;
    private static LoginView login;
    private static String counselorFIRST_NAME;
    private static String counselorLAST_NAME;
    private static String counselorPosition;
    private static int counselorID;

    /**
     * Initializes the form manager with a given JFrame and logs out the current
     * user by default.
     * 
     * @param f The main application frame.
     */
    public static void install(JFrame f) {
        frame = f;
        logout();
    }

    /**
     * Displays a given form within the main application window.
     * If the form is shown for the second time, it revalidates and closes the
     * drawer.
     * 
     * @param form The form to be displayed.
     */
    public static void showForm(Form form) {
        FlatAnimatedLafChange.showSnapshot();
        form.formCheck();
        form.formOpen();

        mainForm.revalidate();
        mainForm.repaint();

        // if (Drawer.isOpen() ) {
        // Drawer.setDrawerOpenMode(MenuOpenMode.COMPACT);;
        // } else {
        // Drawer.toggleMenuOpenMode();
        // }

        mainForm.setForm(form);
        FlatAnimatedLafChange.hideSnapshotWithAnimation();

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
     * Handles user logout by removing all content from the frame, hiding the
     * drawer,
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

    private GuidanceCounselor counselorObjedct;

    public void setCounselorDetails(GuidanceCounselor counselor) {
        this.counselorObjedct = counselor;
        counselorFIRST_NAME = counselor.getFirstName();
        counselorLAST_NAME = counselor.getLastName();
        counselorPosition = counselor.getPosition();
        System.out.println(
                "Counselor Details Set: " + counselorFIRST_NAME + " " + counselorLAST_NAME + ", " + counselorPosition); // Debug
                                                                                                                        // statement
    }

    public GuidanceCounselor getCounselorObject() {
        return counselorObjedct;
    }

    public int getCounselorID() {
        System.out.println("Getting Counselor ID: " + counselorID); // Debug statement
        return counselorObjedct.getGuidanceCounselorId();
    }

    public void setCounselorID(int counselorID) {
        this.counselorObjedct.setGuidanceCounselorId(counselorID);
        ;
        System.out.println("Setting Counselor ID: " + getCounselorID()); // Debug statement
    }

    public String getCounselorFullName() {
        return counselorFIRST_NAME + " " + counselorLAST_NAME;
    }

    public String getCounselorPosition() {
        return counselorPosition;
    }

}
