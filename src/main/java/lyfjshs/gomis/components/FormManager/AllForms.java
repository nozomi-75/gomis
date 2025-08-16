package lyfjshs.gomis.components.FormManager;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

import lyfjshs.gomis.view.MainDashboard;

/**
 * The {@code AllForms} class is responsible for managing instances of form objects.
 * It ensures that each form is instantiated only once and provides a mechanism to retrieve
 * forms dynamically using reflection.
 */
public class AllForms {

    private static AllForms instance;
    private final Map<Class<? extends Form>, Form> formsMap;

    /**
     * Retrieves the singleton instance of {@code AllForms}.
     * 
     * @return The single instance of {@code AllForms}.
     */
    private static AllForms getInstance() {
        if (instance == null) {
            instance = new AllForms();
        }
        return instance;
    }

    /**
     * Private constructor to prevent direct instantiation and enforce singleton pattern.
     * Initializes the map that stores form instances.
     */
    private AllForms() {
        formsMap = new HashMap<>();
    }

    /**
     * Retrieves an instance of the specified form class. If the form has already been created, 
     * it returns the cached instance; otherwise, it instantiates a new one using reflection.
     * 
     * @param cls  The class of the form to be retrieved.
     * @param conn The database connection to be passed to the form's constructor.
     * @return An instance of the requested form.
     * @throws RuntimeException If an error occurs during form instantiation.
     */
    public static Form getForm(Class<?> clazz, Connection conn) {
        try {
            if (clazz == null) {
                throw new IllegalArgumentException("Class cannot be null");
            }

            // Use the connection from FormManager if none provided
            Connection connectionToUse = conn != null ? conn : FormManager.getCurrentConnection();
            if (connectionToUse == null) {
                throw new IllegalStateException("Database connection is null");
            }

            // Check for MainDashboard specifically
            if (clazz.equals(MainDashboard.class)) {
                return new MainDashboard(connectionToUse);
            }

            // For other forms that need connection
            Constructor<?> constructor = clazz.getConstructor(Connection.class);
            return (Form) constructor.newInstance(connectionToUse);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create an instance of " + clazz.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Initializes the given form by invoking its initialization logic asynchronously.
     * 
     * @param form The form instance to be initialized.
     */
    public static void formInit(Form form) {
        SwingUtilities.invokeLater(() -> form.formInit());
    }
}
