package lyfjshs.gomis.Database;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

/**
 * Database connection manager class following singleton pattern Handles
 * database connections efficiently with connection pooling
 */
public class DBConnection {
	// Database configuration constants
	private static final String URL = "jdbc:mariadb://localhost:3306/gomisDB?allowPublicKeyRetrieval=true&useSSL=false&disableMariaDbDriver=false";
	private static final String USER = "root";
	private static final String DEFAULT_PASSWORD = "YourRootPassword123";

	// Connection pool settings
	private static final int MAX_POOL_SIZE = 10;
	private static final int INITIAL_POOL_SIZE = 5;

	// Database service names to check
	private static final String[] DB_SERVICES = { "MySQL", "MariaDB" };

	// Preferences key for storing database password
	private static final String DB_PASSWORD_KEY = "db_password";

	private static final Properties properties = new Properties();
	private static Connection[] connectionPool;
	private static boolean[] isConnectionUsed;
	private static DBConnection instance;
	
	private static boolean isInitialized = false;
	private static String currentPassword;
	private static Component parentComponent;

	/**
	 * Sets the parent component for dialogs
	 */
	public static void setParentComponent(Component component) {
		parentComponent = component;
	}

	private DBConnection() {
		try {
			// Initialize FlatLaf for consistent UI
			try {
				FlatLightLaf.setup();
			} catch (Exception e) {
				System.err.println("Warning: Could not set up FlatLaf: " + e.getMessage());
			}

			// Load saved password or use default
			loadPassword();

			// Check if database service is running
			if (!isDatabaseServiceRunning()) {
				showErrorDialog("Database Service Not Running",
						"The database service is not running. The application will attempt to start it.");

				startDatabaseService();
				// Wait for service to initialize
				Thread.sleep(5000);
			}

			// Test connection with current password
			testConnection();

			// Initialize connection pool
			connectionPool = new Connection[MAX_POOL_SIZE];
			isConnectionUsed = new boolean[MAX_POOL_SIZE];

			// Create initial connections with retry logic
			boolean initialized = false;
			int retryCount = 0;
			int maxRetries = 3;

			while (!initialized && retryCount < maxRetries) {
				try {
					// First try to connect to the server without specifying a database
					try (Connection tempConn = DriverManager.getConnection(
							"jdbc:mariadb://localhost:3306/?useGssapi=false&auth=password", USER, currentPassword)) {

						// Check if database exists, create if not
						checkAndCreateDatabase(tempConn);
					}

					// Now create the connection pool
					for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
						connectionPool[i] = createNewConnection();
						isConnectionUsed[i] = false;
					}

					initialized = true;
					isInitialized = true;
					System.out.println("Database connection pool initialized successfully.");

				} catch (SQLException e) {
					retryCount++;
					System.err.println("Connection attempt " + retryCount + " failed: " + e.getMessage());

					// Check if it's an authentication error
					if (e.getMessage().contains("Access denied") || e.getMessage().contains("password")
							|| e.getMessage().contains("GSS-API") || e.getMessage().contains("authentication")) {

						// Show a specific message for password issues
						showErrorDialog("Database Password Error",
								"The default database password is not working. Please enter your database password.");

						// Ask for new password
						String newPassword = promptForPassword();
						if (newPassword != null && !newPassword.isEmpty()) {
							currentPassword = newPassword;
							// Ensure authentication settings are applied
							properties.setProperty("useGssapi", "false");
							properties.setProperty("auth", "password");
							savePassword();
							// Reset retry count since we have a new password
							retryCount = 0;

							// Test connection with new password
							testConnection();
						} else {
							throw new RuntimeException("Database connection failed: Invalid password", e);
						}
					} else if (retryCount < maxRetries) {
						showErrorDialog("Connection Error", "Failed to connect to database. Retrying in 5 seconds...");
						Thread.sleep(5000);
					} else {
						throw new RuntimeException(
								"Failed to initialize connection pool after " + maxRetries + " attempts", e);
					}
				}
			}
		} catch (Exception e) {
			showErrorDialog("Database Initialization Error",
					"Failed to initialize database connection: " + e.getMessage());
			throw new RuntimeException("Failed to initialize connection pool", e);
		}
	}

	/**
	 * Gets the singleton instance of DBConnection
	 */
	public static synchronized DBConnection getInstance() {
		if (instance == null) {
			instance = new DBConnection();
		}
		return instance;
	}

	/**
	 * Gets an available connection from the pool
	 */
	public static synchronized Connection getConnection() throws SQLException {
		getInstance();

		// Find first available connection
		for (int i = 0; i < MAX_POOL_SIZE; i++) {
			if (connectionPool[i] != null && !isConnectionUsed[i]) {
				// Check if connection is valid
				if (!connectionPool[i].isValid(1)) {
					connectionPool[i] = createNewConnection();
				}
				isConnectionUsed[i] = true;
				return connectionPool[i];
			}
		}

		// Create new connection if pool not full
		for (int i = 0; i < MAX_POOL_SIZE; i++) {
			if (connectionPool[i] == null) {
				connectionPool[i] = createNewConnection();
				isConnectionUsed[i] = true;
				return connectionPool[i];
			}
		}

		throw new SQLException("Connection pool is full");
	}

	/**
	 * Returns a connection back to the pool
	 */
	public static synchronized void releaseConnection(Connection conn) {
		for (int i = 0; i < MAX_POOL_SIZE; i++) {
			if (connectionPool[i] == conn) {
				isConnectionUsed[i] = false;
				break;
			}
		}
	}

	/**
	 * Creates a new database connection
	 */
	private static Connection createNewConnection() throws SQLException {
		try {
			Properties props = new Properties();
			props.setProperty("user", USER);
			props.setProperty("password", currentPassword);
			props.setProperty("allowPublicKeyRetrieval", "true");
			props.setProperty("useSSL", "false");
			props.setProperty("disableMariaDbDriver", "false");
			
			return DriverManager.getConnection(URL, props);
		} catch (SQLException e) {
			// Check if it's an authentication error
			if (e.getMessage().contains("Access denied") || 
				e.getMessage().contains("password") ||
				e.getMessage().contains("authentication")) {

				// Show a specific message for password issues
				showErrorDialog("Database Password Error",
						"The default database password is not working. Please enter your database password.");

				// Ask for new password
				String newPassword = promptForPassword();
				if (newPassword != null && !newPassword.isEmpty()) {
					// Update password
					currentPassword = newPassword;
					// Try connecting again with the new password
					try {
						return DriverManager.getConnection(URL, USER, currentPassword);
					} catch (SQLException ex) {
						// If still failing, throw the original error
						throw e;
					}
				} else {
					throw new SQLException("Database connection failed: Invalid password", e);
				}
			}
			throw e;
		}
	}

	/**
	 * Closes all connections in the pool
	 */
	public static synchronized void closeAllConnections() {
		for (int i = 0; i < MAX_POOL_SIZE; i++) {
			if (connectionPool[i] != null) {
				try {
					connectionPool[i].close();
					connectionPool[i] = null;
					isConnectionUsed[i] = false;
				} catch (SQLException e) {
					System.err.println("Error closing connection: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Checks if the database service is running
	 */
	private static boolean isDatabaseServiceRunning() {
		for (String service : DB_SERVICES) {
			try {
				Process process = Runtime.getRuntime().exec("sc query " + service);
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.trim().startsWith("STATE")) {
						if (line.toLowerCase().contains("running")) {
							System.out.println("Found running service: " + service);
							return true;
						}
					}
				}
			} catch (IOException e) {
				System.err.println("Error checking " + service + " service: " + e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Attempts to start the database service
	 */
	private static void startDatabaseService() {
		for (String service : DB_SERVICES) {
			try {
				System.out.println("Attempting to start " + service + " service...");
				Process process = Runtime.getRuntime().exec("net start " + service);
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				boolean started = false;

				while ((line = reader.readLine()) != null) {
					System.out.println(line);
					if (line.contains("started successfully")) {
						started = true;
						break;
					}
				}

				if (started) {
					System.out.println(service + " service started successfully");
					return;
				}
			} catch (IOException e) {
				System.err.println("Error starting " + service + " service: " + e.getMessage());
			}
		}
		showErrorDialog("Database Service Error",
				"Failed to start any database service. Please install MySQL or MariaDB.");
	}

	/**
	 * Checks if the database exists and creates it if not
	 */
	private static void checkAndCreateDatabase(Connection conn) throws SQLException {
		try (Statement stmt = conn.createStatement()) {
			// Check if database exists
			stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS gomisDB");
			System.out.println("Database 'gomisDB' checked/created successfully.");

			// Switch to the database
			stmt.executeUpdate("USE gomisDB");

			// Check if tables already exist by querying a system table
			boolean tablesExist = false;
			try {
				// Try to query a table that should exist in the schema
				// This assumes there's at least one table in your schema
				// You might need to adjust this query based on your actual schema
				stmt.executeQuery("SELECT 1 FROM information_schema.tables WHERE table_schema = 'gomisDB' LIMIT 1");
				java.sql.ResultSet rs = stmt.getResultSet();
				tablesExist = rs.next();
				rs.close();
			} catch (SQLException e) {
				// If there's an error, assume tables don't exist
				tablesExist = false;
			}

			// Only execute ERD script if tables don't exist
			if (!tablesExist) {
				System.out.println("Database tables not found. Creating schema...");
				executeERDScript(conn);
			} else {
				System.out.println("Database tables already exist. Skipping schema creation.");
			}
		}
	}

	/**
	 * Executes the ERD script to create database tables
	 */
	private static void executeERDScript(Connection conn) {
		try {
			System.out.println("Checking database schema...");
			InputStream is = DBConnection.class.getResourceAsStream("/database/ERD_GOMIS.SQL");
			if (is == null) {
				throw new RuntimeException("Cannot find ERD_GOMIS.SQL in resources");
			}

			String sql = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

			// Split SQL statements by semicolon, but ignore semicolons within comments
			List<String> statements = new ArrayList<>();
			StringBuilder currentStatement = new StringBuilder();
			boolean inComment = false;

			for (String line : sql.split("\n")) {
				line = line.trim();
				if (line.startsWith("--"))
					continue; // Skip comment lines
				if (line.isEmpty())
					continue; // Skip empty lines

				currentStatement.append(line).append("\n");

				if (line.endsWith(";") && !inComment) {
					statements.add(currentStatement.toString());
					currentStatement = new StringBuilder();
				}
			}

			try (Statement stmt = conn.createStatement()) {
				for (String statement : statements) {
					try {
						// Skip DROP statements to avoid deleting existing data
						if (statement.trim().toLowerCase().startsWith("drop")) {
							System.out.println("Skipping DROP statement to preserve existing data");
							continue;
						}
						
						// For CREATE TABLE statements, check if table already exists
						if (statement.trim().toLowerCase().startsWith("create table")) {
							// Extract table name from CREATE TABLE statement
							String tableName = extractTableName(statement);
							if (tableName != null && tableExists(conn, tableName)) {
								System.out.println("Table '" + tableName + "' already exists, skipping creation");
								continue;
							}
						}
						
						stmt.execute(statement);
					} catch (SQLException e) {
						// Log the error but continue with other statements
						System.out.println("Note: " + e.getMessage());
					}
				}
				System.out.println("Database schema checked/created successfully.");
			}
		} catch (Exception e) {
			System.err.println("Error executing SQL script: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Extracts table name from CREATE TABLE statement
	 */
	private static String extractTableName(String createTableStatement) {
		try {
			// Simple extraction - assumes format CREATE TABLE table_name
			String[] parts = createTableStatement.trim().split("\\s+");
			if (parts.length >= 3 && parts[0].equalsIgnoreCase("CREATE") && parts[1].equalsIgnoreCase("TABLE")) {
				// Remove any backticks or quotes
				return parts[2].replaceAll("`|\"", "");
			}
		} catch (Exception e) {
			System.err.println("Error extracting table name: " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Checks if a table exists in the database
	 */
	private static boolean tableExists(Connection conn, String tableName) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeQuery("SELECT 1 FROM information_schema.tables WHERE table_schema = 'gomisDB' AND table_name = '" + tableName + "' LIMIT 1");
			java.sql.ResultSet rs = stmt.getResultSet();
			boolean exists = rs.next();
			rs.close();
			return exists;
		} catch (SQLException e) {
			System.err.println("Error checking if table exists: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Shows an error dialog with the given title and message
	 */
	private static void showErrorDialog(String title, String message) {
		if (parentComponent != null) {
			JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
		} else {
			System.err.println(title + ": " + message);
		}
	}

	/**
	 * Prompts the user for a database password
	 */
	private static String promptForPassword() {
		if (parentComponent != null) {
			JPasswordField passwordField = new JPasswordField();
			int option = JOptionPane.showConfirmDialog(parentComponent, passwordField, "Enter Your Database Password",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (option == JOptionPane.OK_OPTION) {
				String password = new String(passwordField.getPassword());
				if (password != null && !password.isEmpty()) {
					// Save the password immediately
					currentPassword = password;
					savePassword();
					return password;
				}
			}
		}
		return null;
	}

	/**
	 * Loads the saved password from preferences
	 */
	private static void loadPassword() {
		try {
			// Try to load from Java Preferences API
			Preferences prefs = Preferences.userNodeForPackage(DBConnection.class);
			String savedPassword = prefs.get(DB_PASSWORD_KEY, null);

			if (savedPassword != null && !savedPassword.isEmpty()) {
				currentPassword = savedPassword;
				return;
			}

			// If no saved password found, use default
			currentPassword = DEFAULT_PASSWORD;

		} catch (Exception e) {
			System.err.println("Error loading password: " + e.getMessage());
			currentPassword = DEFAULT_PASSWORD;
		}
	}

	/**
	 * Saves the current password to preferences
	 */
	private static void savePassword() {
		try {
			// Save to Java Preferences API
			Preferences prefs = Preferences.userNodeForPackage(DBConnection.class);
			prefs.put(DB_PASSWORD_KEY, currentPassword);
			prefs.flush();

		} catch (Exception e) {
			System.err.println("Error saving password: " + e.getMessage());
		}
	}

	/**
	 * Tests the connection with the current password
	 */
	private void testConnection() throws SQLException {
		int maxRetries = 3;
		int currentRetry = 0;
		boolean connected = false;

		while (!connected && currentRetry < maxRetries) {
			try (Connection conn = DriverManager.getConnection(
					"jdbc:mariadb://localhost:3306/?allowPublicKeyRetrieval=true&useSSL=false&disableMariaDbDriver=false",
					USER, currentPassword)) {
				connected = true;
			} catch (SQLException e) {
				currentRetry++;
				if (e.getMessage().contains("Access denied") || 
					e.getMessage().contains("authentication")) {

					String newPassword = promptForInstallationPassword(currentRetry, maxRetries);
					if (newPassword != null && !newPassword.isEmpty()) {
						currentPassword = newPassword;
						continue;
					} else if (currentRetry >= maxRetries) {
						throw new SQLException("Failed to connect after " + maxRetries + " attempts: Invalid password");
					}
				} else {
					throw e;
				}
			}
		}

		if (!connected) {
			throw new SQLException("Failed to establish database connection after " + maxRetries + " attempts");
		}
	}

	private String promptForInstallationPassword(int currentAttempt, int maxAttempts) {
		// Create custom dialog
		JDialog dialog = new JDialog();
		dialog.setTitle("Database Authentication Required");
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		// Add window listener to handle dialog closing
		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.exit(0);
			}
		});

		// Main panel with MigLayout
		JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 20 25 20 25", "[pref!,grow,fill]", "[][][][]"));
		
		// Message label with HTML formatting
		JLabel messageLabel = new JLabel(String.format("<html>" +
			"<p style='margin-bottom: 10px'>The database password is incorrect.</p>" +
			"<p style='margin-bottom: 10px'>Please enter the password you set during MariaDB installation.</p>" +
			"<p style='color: #666666'><b>Attempt %d of %d</b></p>" +
			"</html>",
			currentAttempt, maxAttempts));
		panel.add(messageLabel, "cell 0 0");
		
		// Password field with show/hide functionality
		JPasswordField passwordField = new JPasswordField();
		passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true; arc:20;");
		passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter MariaDB root password");
		panel.add(passwordField, "cell 0 1");
		
		// Buttons panel
		JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]"));
		
		JButton tryAgainBtn = new JButton("Try Again");
		JButton exitBtn = new JButton("Exit Program");
		tryAgainBtn.putClientProperty(FlatClientProperties.STYLE, 
			"[light]background:darken(@background,10%);" +
			"[dark]background:lighten(@background,10%);" +
			"borderWidth:0; focusWidth:0; innerFocusWidth:0;");
		exitBtn.putClientProperty(FlatClientProperties.STYLE, 
			"[light]background:darken(@background,10%);" +
			"[dark]background:lighten(@background,10%);" +
			"borderWidth:0; focusWidth:0; innerFocusWidth:0;");
		
		buttonPanel.add(tryAgainBtn, "cell 0 0,growx");
		buttonPanel.add(exitBtn, "cell 1 0,growx");
		panel.add(buttonPanel, "cell 0 2");
		
		final String[] result = {null};
		
		tryAgainBtn.addActionListener(e -> {
			String password = new String(passwordField.getPassword());
			if (password == null || password.trim().isEmpty()) {
				JOptionPane.showMessageDialog(
					dialog,
					"Password cannot be empty. Please enter the MariaDB root password.",
					"Invalid Password",
					JOptionPane.WARNING_MESSAGE
				);
				return;
			}
			result[0] = password;
			// Save the password when a valid one is entered
			currentPassword = password;
			savePassword();
			dialog.dispose();
		});
		
		exitBtn.addActionListener(e -> {
			dialog.dispose();
			System.exit(0);
		});
		
		// Set dialog properties
		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(parentComponent);
		dialog.setResizable(false);
		
		// Show dialog and wait for result
		dialog.setVisible(true);
		
		return result[0];
	}
}