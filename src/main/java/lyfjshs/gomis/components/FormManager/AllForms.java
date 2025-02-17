package lyfjshs.gomis.components.FormManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

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
    public static Form getForm(Class<? extends Form> cls, Connection conn) {
        if (getInstance().formsMap.containsKey(cls)) {
            return getInstance().formsMap.get(cls);
        }
        try {
            Constructor<? extends Form> constructor = cls.getDeclaredConstructor(Connection.class); // Get the constructor with Connection parameter
            Form form = constructor.newInstance(conn); // Instantiate the form with the connection
            getInstance().formsMap.put(cls, form); // Store the form instance for future use
            formInit(form);
            return form;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create an instance of " + cls.getName(), e);
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
