package lyfjshs.gomis.components.FormManager;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * The {@code Form} class represents a base form (panel) in the system.
 * It extends {@link JPanel} and provides lifecycle methods for initialization,
 * opening, refreshing, and checking UI consistency.
 */
public class Form extends JPanel {

    /**
     * Stores the look-and-feel theme applied when the form was created.
     * It is used to detect theme changes and update the UI accordingly.
     */
    private LookAndFeel oldTheme = UIManager.getLookAndFeel();

    /**
     * Constructs a new {@code Form} instance and initializes its components.
     */
    public Form() {
        init();
    }

    /**
     * Initializes the form's components.
     * This method is intended to be overridden by subclasses if needed.
     */
    private void init() {
    }

    /**
     * Performs additional initialization logic when the form is first loaded.
     * Subclasses should override this method to set up event listeners or 
     * pre-load data.
     */
    public void formInit() {
    }

    /**
     * Called when the form is opened.
     * This method can be used to refresh data or update the UI dynamically.
     */
    public void formOpen() {
    }

    /**
     * Refreshes the form to reflect any changes in data or UI components.
     * This method should be called when updates are made that require re-rendering.
     */
    public void formRefresh() {
    }

    /**
     * Checks if the look-and-feel theme has changed and updates the UI if necessary.
     * This method ensures that the form remains visually consistent when themes are switched.
     */
    protected final void formCheck() {
        if (oldTheme != UIManager.getLookAndFeel()) {
            oldTheme = UIManager.getLookAndFeel();
            SwingUtilities.updateComponentTreeUI(this);
        }
    }
}
