package lyfjshs.gomis.utils;

import javax.swing.JOptionPane;

/**
 * Utility class for showing error dialogs.
 */
public class ErrorDialogUtils {
    /**
     * Shows an error dialog with the given message.
     * @param parent the parent component
     * @param message the error message
     */
    public static void showError(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(java.awt.Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
} 