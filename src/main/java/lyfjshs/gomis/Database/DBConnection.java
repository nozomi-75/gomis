package lyfjshs.gomis.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager class following singleton pattern
 * Handles database connections efficiently with connection pooling
 */
public class DBConnection {
	// Database configuration constants
	private static final String URL = "jdbc:mysql://localhost:3306/gomisDB";
	private static final String USER = "root";
	private static final String PASSWORD = "";
	
	// Connection pool settings
	private static final int MAX_POOL_SIZE = 10;
	private static final int INITIAL_POOL_SIZE = 5;
	
	private static final Properties properties = new Properties();
	private static Connection[] connectionPool;
	private static boolean[] isConnectionUsed;
	private static DBConnection instance;

	private DBConnection() {
		try {
			// Initialize connection properties
			properties.setProperty("user", USER);
			properties.setProperty("password", PASSWORD);
			properties.setProperty("autoReconnect", "true");
			properties.setProperty("useSSL", "false");
			
			// Initialize connection pool
			connectionPool = new Connection[MAX_POOL_SIZE];
			isConnectionUsed = new boolean[MAX_POOL_SIZE];
			
			// Create initial connections
			for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
				connectionPool[i] = createNewConnection();
				isConnectionUsed[i] = false;
			}
		} catch (SQLException e) {
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
		DBConnection instance = getInstance();
		
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
		return DriverManager.getConnection(URL, properties);
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
}