package lyfjshs.CRUDS;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
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

public class DatabaseSplashScreen extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JProgressBar progressBar;
	private JLabel loadingLabel;
	private JLabel imageLabel; 

	private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/gomisDB";
	private static final String JDBC_USER = "root";
	private static final String JDBC_PASSWORD = "";
	private static final String[] TEST_TABLES = { "GUIDANCE_COUNSELORS", "USERS", "STUDENTS_DATA", "PARTICIPANTS",
			"VIOLATION_RECORD", "STUDENT_RECORD", "INCIDENTS", "APPOINTMENTS", "SESSIONS", "sessions_participants" };

	public DatabaseSplashScreen(ImageIcon splashImage) {
		setUndecorated(true); // Remove window decorations for a splash screen look
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent closing during loading
		setSize(splashImage.getIconWidth(), splashImage.getIconHeight() + 50); // Set size based on image and progress
																				// bar
		setLocationRelativeTo(null); // Center the frame

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		imageLabel = new JLabel(splashImage);
		contentPane.add(imageLabel, BorderLayout.CENTER);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setPreferredSize(new Dimension(getSize().width, 30)); // Set progress bar size
		contentPane.add(progressBar, BorderLayout.SOUTH);

		loadingLabel = new JLabel("Testing Database Connection...");
		loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(loadingLabel, BorderLayout.NORTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public void startDatabaseTest() {
		setVisible(true); // Make the splash screen visible
		new Thread(() -> {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);

				if (connection != null) {
					SwingUtilities.invokeLater(() -> {
						progressBar.setValue(25);
						loadingLabel.setText("Database Connection Successful! Checking Tables...");
					});
					DatabaseMetaData metaData = connection.getMetaData();

					for (int i = 0; i < TEST_TABLES.length; i++) {
						final int tableIndex = i; // Create a final local variable
						if (metaData.getTables(null, null, TEST_TABLES[tableIndex], null).next() == false) {
							SwingUtilities.invokeLater(() -> {
								JOptionPane.showMessageDialog(DatabaseSplashScreen.this,
										"Test table '" + TEST_TABLES[tableIndex] + "' not found.", "Database Error",
										JOptionPane.ERROR_MESSAGE);
								System.exit(1);
							});
							connection.close();
							return;
						}
						int progress = 25 + (75 * (tableIndex + 1) / TEST_TABLES.length);
						SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
					}

					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(DatabaseSplashScreen.this, "Database and Table tests successful!",
								"Success", JOptionPane.INFORMATION_MESSAGE);
						dispose(); // Close the splash screen
					});
					connection.close();
				} else {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(DatabaseSplashScreen.this,
								"JDBC Connection failed. Check your connection details.", "Connection Error",
								JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					});
				}

			} catch (ClassNotFoundException e) {
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(DatabaseSplashScreen.this,
							"MySQL JDBC Driver not found. Make sure it's in your classpath.", "Driver Error",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(1);
				});
			} catch (SQLException e) {
				SwingUtilities.invokeLater(() -> {
					String errorMessage = "Database connection error: " + e.getMessage();
					if (e.getErrorCode() == 1045) {
						errorMessage = "Database access denied. Check your username and password.";
					} else if (e.getErrorCode() == 1049) {
						errorMessage = "Database '" + JDBC_URL.substring(JDBC_URL.lastIndexOf('/') + 1)
								+ "' does not exist.";
					} else if (e.getMessage().contains("Communications link failure")) {
						errorMessage = "Cannot connect to the database. Check your network connection and database server.";
					}
					JOptionPane.showMessageDialog(DatabaseSplashScreen.this, errorMessage, "Database Error",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(1);
				});
			}
		}).start();
	}

	public static void main(String[] args) {
		ImageIcon splashImage = new ImageIcon(DatabaseSplashScreen.class.getResource("/LYFJSHS_Logo_200x200.png")); 
		EventQueue.invokeLater(() -> {
			DatabaseSplashScreen splash = new DatabaseSplashScreen(splashImage);
			splash.startDatabaseTest();
		});
	}
}