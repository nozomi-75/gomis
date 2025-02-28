package lyfjshs.gomis.Database.DAO;

import java.sql.SQLException;

import javax.swing.JOptionPane;

public class SQLExceptionPane {

    // Handles SQL-related errors
    public static void showSQLException(SQLException e, String operation) {
        String errorMessage = "❌ Database Error during: " + operation + "\n\n"
                            + "🔹 Error Code: " + e.getErrorCode() + "\n"
                            + "🔹 SQL State: " + e.getSQLState() + "\n"
                            + "🔹 Message: " + e.getMessage();

        JOptionPane.showMessageDialog(null, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // Handles other general exceptions
    public static void showGeneralException(Exception e, String operation) {
        String errorMessage = "❌ Error occurred during: " + operation + "\n\n"
                            + "🔹 Exception Type: " + e.getClass().getSimpleName() + "\n"
                            + "🔹 Message: " + e.getMessage();

        JOptionPane.showMessageDialog(null, errorMessage, "Application Error", JOptionPane.ERROR_MESSAGE);
    }

}
