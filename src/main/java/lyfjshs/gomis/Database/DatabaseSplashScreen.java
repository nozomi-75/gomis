package lyfjshs.gomis.Database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import lyfjshs.gomis.Database.DBConnection; // Import your DBConnection class

public class DatabaseSplashScreen extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JProgressBar progressBar;
    private JLabel loadingLabel;
    private JLabel imageLabel;

    private static final String[] TEST_TABLES = {
            "GUIDANCE_COUNSELORS", "USERS", "STUDENTS_DATA", "PARTICIPANTS",
            "VIOLATION_RECORD", "STUDENT_RECORD", "INCIDENTS", "APPOINTMENTS",
            "SESSIONS", "sessions_participants"
    };

    public DatabaseSplashScreen(ImageIcon splashImage) {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(splashImage.getIconWidth(), splashImage.getIconHeight() + 50);
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        imageLabel = new JLabel(splashImage);
        contentPane.add(imageLabel, BorderLayout.CENTER);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(getSize().width, 30));
        contentPane.add(progressBar, BorderLayout.SOUTH);

        loadingLabel = new JLabel("Testing Database Connection...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(loadingLabel, BorderLayout.NORTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnection.closeAllConnections(); // Close all connections on exit
                System.exit(0);
            }
        });
    }

    public void startDatabaseTest() {
        setVisible(true);
        new Thread(() -> {
            Connection connection = null;
            try {
                connection = DBConnection.getConnection(); // Get connection from pool

                if (connection != null) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(25);
                        loadingLabel.setText("Database Connection Successful! Checking Tables...");
                    });
                    DatabaseMetaData metaData = connection.getMetaData();

                    for (int i = 0; i < TEST_TABLES.length; i++) {
                        final int tableIndex = i;
                        if (metaData.getTables(null, null, TEST_TABLES[tableIndex], null).next() == false) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(DatabaseSplashScreen.this, "Test table '" + TEST_TABLES[tableIndex] + "' not found.", "Database Error", JOptionPane.ERROR_MESSAGE);
                                System.exit(1);
                            });
                            DBConnection.releaseConnection(connection); // Release connection back to pool
                            return;
                        }
                        int progress = 25 + (75 * (tableIndex + 1) / TEST_TABLES.length);
                        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    }

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DatabaseSplashScreen.this, "Database and Table tests successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    });
                    DBConnection.releaseConnection(connection); // Release connection back to pool
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DatabaseSplashScreen.this, "JDBC Connection failed. Check your connection details.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(1);
                    });
                }

            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    String errorMessage = "Database connection error: " + e.getMessage();
                    if (e.getErrorCode() == 1045) {
                        errorMessage = "Database access denied. Check your username and password.";
                    } else if (e.getErrorCode() == 1049) {
                        errorMessage = "Database 'gomisDB' does not exist.";
                    } else if (e.getMessage().contains("Communications link failure")) {
                        errorMessage = "Cannot connect to the database. Check your network connection and database server.";
                    }
                    JOptionPane.showMessageDialog(DatabaseSplashScreen.this, errorMessage, "Database Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(1);
                });
            } finally {
                if (connection != null) {
                    DBConnection.releaseConnection(connection); // Ensure connection is released
                }
            }
        }).start();
    }

}