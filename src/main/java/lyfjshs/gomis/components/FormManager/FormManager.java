package lyfjshs.gomis.components.FormManager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.DrawerBuilder;
import lyfjshs.gomis.view.MainDashboard;
import lyfjshs.gomis.view.login.LoginView;
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
    private static Connection currentConnection;

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
        if (conn == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }
        currentConnection = conn;
        Drawer.installDrawer(Main.gFrame, new DrawerBuilder(conn));
        Drawer.setVisible(true);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(getMainForm());

        // Explicitly show the MainDashboard after login, triggering its responsive layout logic
        FormManager.showForm(new MainDashboard(currentConnection));
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

    // Static counselor object accessible application-wide
    public static GuidanceCounselor staticCounselorObject;
    
    /**
     * Sets the counselor object at the class level for static access
     * @param counselor The GuidanceCounselor object to set
     */
    public static void setCounselorObject(GuidanceCounselor counselor) {
        if (counselor == null) {
            System.err.println("Warning: Attempting to set null counselor");
            return;
        }
        staticCounselorObject = counselor;
        counselorFIRST_NAME = counselor.getFirstName();
        counselorLAST_NAME = counselor.getLastName();
        counselorPosition = counselor.getPosition();
        counselorID = counselor.getGuidanceCounselorId();
        System.out.println(
                "Counselor Details Set: " + counselorFIRST_NAME + " " + counselorLAST_NAME + ", " + counselorPosition);
                
        // Create instance of FormManager if not already
        if (Main.formManager == null) {
            Main.formManager = new FormManager();
        }
        
        // Also set in the instance for backward compatibility
        if (Main.formManager != null) {
            Main.formManager.setCounselorDetails(counselor);
        }
    }

    private GuidanceCounselor counselorObject;

    public void setCounselorDetails(GuidanceCounselor counselor) {
        if (counselor == null) {
            System.err.println("Warning: Attempting to set null counselor");
            return;
        }
        this.counselorObject = counselor;
        counselorFIRST_NAME = counselor.getFirstName();
        counselorLAST_NAME = counselor.getLastName();
        counselorPosition = counselor.getPosition();
        counselorID = counselor.getGuidanceCounselorId();
        System.out.println(
                "Counselor Details Set: " + counselorFIRST_NAME + " " + counselorLAST_NAME + ", " + counselorPosition); // Debug
                                                                                                                        // statement
    }

    public GuidanceCounselor getCounselorObject() {
        return counselorObject;
    }

    public int getCounselorID() {
        // Add null check and default value
        if (counselorObject == null) {
            System.err.println("Warning: Counselor object is null");
            return 1; // Default ID
        }
        return counselorObject.getGuidanceCounselorId();
    }

    public void setCounselorID(int counselorID) {
        this.counselorObject.setGuidanceCounselorId(counselorID);
        ;
        System.out.println("Setting Counselor ID: " + getCounselorID()); // Debug statement
    }

    public String getCounselorFullName() {
        return counselorFIRST_NAME + " " + counselorLAST_NAME;
    }

    public String getCounselorPosition() {
        return counselorPosition;
    }

    public static Form[] getForms() {
        if (mainForm != null) {
            return mainForm.getAllForms();
        }
        return new Form[0];
    }

    public static void refreshAllForms() {
        if (mainForm != null) {
            Form[] forms = mainForm.getAllForms();
            for (Form form : forms) {
                form.formRefresh();
            }
        }
        Main.gFrame.refresh();
        if (Drawer.isOpen()) {
            // Update drawer by closing and re-showing it
            Drawer.closeDrawer();
            Drawer.showDrawer();
        }
    }

    /**
     * Gets the current database connection
     * @return The current database connection
     */
    public static Connection getCurrentConnection() {
        return currentConnection;
    }

    /**
     * Triggers a layout update on the MainDashboard when the frame is resized.
     * This ensures proper responsive layout behavior.
     */
    public static void triggerDashboardLayoutUpdate(int frameWidth) {
        if (mainForm != null) {
            Form currentForm = mainForm.getCurrentForm();
            if (currentForm instanceof MainDashboard) {
                ((MainDashboard) currentForm).updateLayout(frameWidth);
            }
        }
    }

    /**
     * Navigates back to the previous form or to the main dashboard if no previous form exists.
     */
    public static void goBack() {
        if (mainForm != null) {
            Form[] forms = mainForm.getAllForms();
            if (forms.length > 1) {
                // Remove the current form and show the previous one
                mainForm.setForm(forms[forms.length - 2]);
            } else {
                // If no previous form, go to main dashboard
                FormManager.showForm(new MainDashboard(currentConnection));
            }
        }
    }

    /**
     * Notifies the currently active form of a parent frame resize event.
     */
    public static void notifyActiveFormOfResize(int width, int height) {
        int contentWidth = width;
        int contentHeight = height;
        if (frame != null && frame.getContentPane() != null) {
            contentWidth = frame.getContentPane().getWidth();
            contentHeight = frame.getContentPane().getHeight();
        }
        Form current = getCurrentForm();
        if (current != null) {
            current.onParentFrameResized(contentWidth, contentHeight);
        }
    }

    /**
     * Returns the currently active form in the main panel.
     */
    public static Form getCurrentForm() {
        if (mainForm != null) {
            return mainForm.getCurrentForm();
        }
        return null;
    }
}
