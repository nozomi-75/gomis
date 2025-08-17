/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.FormManager;

import java.awt.Dimension;

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

    private String title = ""; // Default title

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
     * Constructs a new {@code Form} instance with a specified title and initializes its components.
     * @param title The title of the form.
     */
    public Form(String title) {
        this.title = title;
        init();
    }

    /**
     * Initializes the form's components.
     * This method is intended to be overridden by subclasses if needed.
     */
    protected void init() {
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

    /**
     * Returns the main JPanel of this form. In this base class, it's simply 'this'.
     * Subclasses might return a specific panel if their structure is more complex.
     * @return The main JPanel of the form.
     */
    public JPanel getMainPanel() {
        return this;
    }

    /**
     * Returns the title of the form.
     * @return The title string.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Disposes of any resources used by this form.
     * This method should be overridden by subclasses that need to clean up resources.
     */
    public void dispose() {
        // Default implementation does nothing
    }

    /**
     * Called when the parent frame is resized. Default: refresh the form UI.
     */
    public void onParentFrameResized(int width, int height) {
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.revalidate();
        this.repaint();
        this.formRefresh();
    }
}
