package lyfjshs;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Database.DBConnection;
import lyfjshs.gomis.view.students.schoolForm.ImportSF;

/**
 * Demo class showing how to use the refactored components (SFtoDB,
 * SFExcelHighlighter) with a single database connection and JFrame.
 */
public class SFImportDemo extends JFrame {
    private Connection connection;
    private ImportSF sfToDB;
    private JPanel mainPanel;

    public SFImportDemo() {
        // Initialize frame
        super("School Forms Import Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);

        // Initialize database connection
        initDatabase();

        // Initialize UI components
        initComponents();

        // Set up closing handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });

        // Display the frame
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initDatabase() {
        try {
            connection = DBConnection.getConnection();
            if (connection == null) {
                throw new SQLException("Failed to get database connection");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to connect to database: " + e.getMessage()
                            + "\nMake sure MariaDB is running and configured correctly.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());

        // Create SFtoDB panel with our connection and frame
        sfToDB = new ImportSF(connection);

        mainPanel.add(sfToDB, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void cleanup() {
        if (connection != null) {
            DBConnection.releaseConnection(connection);
        }
        DBConnection.closeAllConnections();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatMacLightLaf.setup();
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SFImportDemo();
        });
    }
}