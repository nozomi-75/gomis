/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database;

import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLExceptionPane {

    private static final Logger LOGGER = Logger.getLogger(SQLExceptionPane.class.getName());

    // Handles SQL-related errors
    public static void showSQLException(SQLException e, String operation) {
        String errorMessage = String.format(
            "Database Error during: %s%n%nError Code: %d%nSQL State: %s%nMessage: %s",
            operation, e.getErrorCode(), e.getSQLState(), e.getMessage() != null ? e.getMessage() : "No message available"
        );

        showErrorDialog(errorMessage, "Database Error");
        LOGGER.log(Level.SEVERE, errorMessage, e);
    }

    // Handles other general exceptions
    public static void showGeneralException(Exception e, String operation) {
        String errorMessage = String.format(
            "Error occurred during: %s%n%nException Type: %s%nMessage: %s",
            operation, e.getClass().getSimpleName(), e.getMessage() != null ? e.getMessage() : "No message available"
        );

        showErrorDialog(errorMessage, "Application Error");
        LOGGER.log(Level.SEVERE, errorMessage, e);
    }

    // Private method to show error dialog
    private static void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
}