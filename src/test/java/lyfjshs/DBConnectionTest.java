package lyfjshs;

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
import java.util.stream.Collectors;

public class DBConnectionTest {
    private static final String URL = "jdbc:mariadb://localhost:3306/";
    private static final String DB_NAME = "gomisdb"; // Updated to match ERD_GOMIS.SQL
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";
    private static final String[] TABLES = {
        "ADDRESS", "CONTACT", "PARENTS", "GUARDIAN", "GUIDANCE_COUNSELORS", 
        "SCHOOL_FORM", "STUDENT", "PARTICIPANTS", "USERS", "REMARK", 
        "INCIDENTS", "VIOLATION_RECORD", "APPOINTMENTS", "APPOINTMENT_PARTICIPANTS", 
        "SESSIONS", "SESSIONS_PARTICIPANTS", "PREFERENCES"
    };
    private static final String[] DB_SERVICES = {"MySQL", "MariaDB"};

    public static void main(String[] args) {
        if (!isDatabaseServiceRunning()) {
            startDatabaseService();
        }
        testMariaDBConnection();
    }

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
                    Thread.sleep(5000); // Wait for service to initialize
                    return;
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Error starting " + service + " service: " + e.getMessage());
            }
        }
        System.err.println("Failed to start any database service. Please install MySQL or MariaDB.");
    }

    public static boolean testMariaDBConnection() {
        int retryCount = 0;
        while (retryCount < 3) {
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("Database connection successful.");
                return checkAndCreateDatabase(conn);
            } catch (SQLException e) {
                System.err.println("Connection attempt " + (retryCount + 1) + " failed: " + e.getMessage());
                if (!isDatabaseServiceRunning()) {
                    System.out.println("Database service not running. Attempting to start...");
                    startDatabaseService();
                }
                retryCount++;
                if (retryCount < 3) {
                    System.out.println("Retrying connection in 5 seconds...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        System.err.println("Failed to establish database connection after 3 attempts.");
        return false;
    }

    private static boolean checkAndCreateDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Drop database if exists to ensure clean state
            stmt.executeUpdate("DROP DATABASE IF EXISTS " + DB_NAME);
            System.out.println("Dropped existing database if present.");
            
            // Create fresh database
            stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
            System.out.println("Database '" + DB_NAME + "' created successfully.");
            
            // Switch to the new database and create tables
            stmt.executeUpdate("USE " + DB_NAME);
            return executeERDScript(conn);
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
            return false;
        }
    }

    private static boolean executeERDScript(Connection conn) {
        try {
            System.out.println("Executing ERD script...");
            InputStream is = DBConnectionTest.class.getResourceAsStream("/database/ERD_GOMIS.SQL");
            if (is == null) {
                throw new RuntimeException("Cannot find ERD_GOMIS.SQL in resources");
            }

            String sql = new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));

            // Split SQL statements by semicolon, but ignore semicolons within comments
            List<String> statements = new ArrayList<>();
            StringBuilder currentStatement = new StringBuilder();
            boolean inComment = false;

            for (String line : sql.split("\n")) {
                line = line.trim();
                if (line.startsWith("--")) continue; // Skip comment lines
                if (line.isEmpty()) continue;        // Skip empty lines
                
                currentStatement.append(line).append("\n");
                
                if (line.endsWith(";") && !inComment) {
                    statements.add(currentStatement.toString());
                    currentStatement = new StringBuilder();
                }
            }

            try (Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    stmt.execute(statement);
                }
                System.out.println("Database schema created successfully from ERD_GOMIS.SQL");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error executing SQL script: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
