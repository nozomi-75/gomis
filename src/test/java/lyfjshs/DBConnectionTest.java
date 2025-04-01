package lyfjshs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DBConnectionTest {
    private static final String URL = "jdbc:mariadb://localhost:3306/";
    private static final String DB_NAME = "gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";
    private static final String[] TABLES = {
        "ADDRESS", "CONTACT", "PARENTS", "GUARDIAN", "GUIDANCE_COUNSELORS", 
        "SCHOOL_FORM", "STUDENT", "PARTICIPANTS", "USERS", "REMARK", 
        "INCIDENTS", "VIOLATION_RECORD", "APPOINTMENTS", "APPOINTMENT_PARTICIPANTS", 
        "SESSIONS", "SESSIONS_PARTICIPANTS", "PREFERENCES"
    };

    public static void main(String[] args) {
        testMariaDBConnection();
    }

    public static boolean testMariaDBConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("✅ MariaDB is running and connection is successful.");
            return checkAndCreateDatabase(conn);
        } catch (SQLException e) {
            System.err.println("❌ Error connecting to MariaDB: " + e.getMessage());
            return false;
        }
    }

    private static boolean checkAndCreateDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + DB_NAME + "'");
            if (!rs.next()) {
                System.out.println("⚠️ Database '" + DB_NAME + "' not found. Creating it now...");
                stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
                System.out.println("✅ Database '" + DB_NAME + "' created successfully.");
            } else {
                System.out.println("✅ Database '" + DB_NAME + "' exists.");
            }
            return checkAndCreateTables();
        } catch (SQLException e) {
            System.err.println("❌ Error checking/creating database: " + e.getMessage());
            return false;
        }
    }

    private static void dropAllTables(Statement stmt) throws SQLException {
        System.out.println("🛑 Dropping existing tables...");
        for (String table : TABLES) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + table + " CASCADE");
        }
        System.out.println("✅ All tables dropped.");
    }

    private static boolean checkAndCreateTables() {
        String dbUrl = URL + DB_NAME;
        try (Connection conn = DriverManager.getConnection(dbUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            List<String> missingTables = new ArrayList<>();
            for (String table : TABLES) {
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + table + "'");
                if (!rs.next()) {
                    missingTables.add(table);
                }
            }
            
            if (!missingTables.isEmpty()) {
                System.out.println("⚠️ Some tables are missing: " + missingTables);
                System.out.println("🛑 Dropping all tables and recreating...");
                dropAllTables(stmt);
                executeERDScript(conn);
            } else {
                System.out.println("✅ All tables exist. No action needed.");
            }
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error checking/creating tables: " + e.getMessage());
            return false;
        }
    }

    private static void executeERDScript(Connection conn) {
        try {
            // Read SQL file from resources
            InputStream is = DBConnectionTest.class.getResourceAsStream("/database/ERD_GOMIS.SQL");
            if (is == null) {
                throw new RuntimeException("Cannot find ERD_GOMIS.SQL in resources");
            }

            // Read file content
            String sql = new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));

            // Split SQL statements by delimiter
            String[] statements = sql.split(";");

            // Execute each statement
            try (Statement stmt = conn.createStatement()) {
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty() && !statement.startsWith("--")) {
                        stmt.execute(statement);
                    }
                }
                System.out.println("✅ Database schema created successfully from ERD_GOMIS.SQL");
            }
        } catch (Exception e) {
            System.err.println("❌ Error executing SQL script: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
