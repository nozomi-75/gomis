package lyfjshs.CRUDS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConnectionTest {

    private static final String URL = "jdbc:mariadb://localhost:3306/"; // MariaDB URL with port
    private static final String DB_NAME = "gomisDB";
    private static final String USER = "root";
    private static final String PASSWORD = "YourRootPassword123!";

    private static final String[] TABLES = {
        "ADDRESS", "CONTACT", "PARENTS", "GUARDIAN", "GUIDANCE_COUNSELORS", "SCHOOL_FORM", "STUDENT", "PARTICIPANTS", "USERS", "REMARK", "INCIDENTS", "VIOLATION_RECORD", "APPOINTMENTS", "APPOINTMENT_PARTICIPANTS", "SESSIONS", "SESSIONS_PARTICIPANTS"
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
                createAllTables(stmt);
            } else {
                System.out.println("✅ All tables exist. No action needed.");
            }
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking/creating tables: " + e.getMessage());
            return false;
        }
    }

    private static void createAllTables(Statement stmt) throws SQLException {
        String[] tableCreationQueries = {
            "CREATE TABLE ADDRESS (" +
                "ADDRESS_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "ADDRESS_HOUSE_NUMBER VARCHAR(50), " +
                "ADDRESS_STREET_SUBDIVISION VARCHAR(100), " +
                "ADDRESS_REGION VARCHAR(100), " +
                "ADDRESS_PROVINCE VARCHAR(100), " +
                "ADDRESS_MUNICIPALITY VARCHAR(100), " +
                "ADDRESS_BARANGAY VARCHAR(100), " +
                "ADDRESS_ZIP_CODE VARCHAR(20));",

            "CREATE TABLE CONTACT (" +
                "CONTACT_ID INT PRIMARY KEY, " +
                "CONTACT_NUMBER VARCHAR(20));",

            "CREATE TABLE PARENTS (" +
                "PARENT_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "FATHER_FIRSTNAME VARCHAR(100), " +
                "FATHER_LASTNAME VARCHAR(100), " +
                "FATHER_MIDDLENAME VARCHAR(100), " +
                "FATHER_CONTACT_NUMBER VARCHAR(100), " +
                "MOTHER_FIRSTNAME VARCHAR(100), " +
                "MOTHER_LASTNAME VARCHAR(100), " +
                "MOTHER_MIDDLE_NAME VARCHAR(100), " +
                "MOTHER_CONTACT_NUMBER VARCHAR(100));",

            "CREATE TABLE GUARDIAN (" +
                "GUARDIAN_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "GUARDIAN_LASTNAME VARCHAR(100), " +
                "GUARDIAN_FIRST_NAME VARCHAR(100), " +
                "GUARDIAN_MIDDLE_NAME VARCHAR(100), " +
                "GUARDIAN_RELATIONSHIP VARCHAR(50), " +
                "GUARDIAN_CONTACT_NUMBER VARCHAR(100));",

            "CREATE TABLE GUIDANCE_COUNSELORS (" +
                "GUIDANCE_COUNSELOR_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "LAST_NAME VARCHAR(100), " +
                "FIRST_NAME VARCHAR(100), " +
                "MIDDLE_NAME VARCHAR(10), " +
                "SUFFIX VARCHAR(10), " +
                "GENDER VARCHAR(10), " +
                "SPECIALIZATION VARCHAR(100), " +
                "CONTACT_NUMBER VARCHAR(100), " +
                "EMAIL VARCHAR(100), " +
                "POSITION VARCHAR(100), " +
                "PROFILE_PICTURE BLOB);",

            "CREATE TABLE STUDENT (" +
                "STUDENT_UID INT PRIMARY KEY, " +
                "PARENT_ID INT, " +
                "GUARDIAN_ID INT, " +
                "ADDRESS_ID INT, " +
                "CONTACT_ID INT, " +
                "SF_SECTION VARCHAR(50), " +
                "STUDENT_LRN VARCHAR(50), " +
                "STUDENT_LASTNAME VARCHAR(100), " +
                "STUDENT_FIRSTNAME VARCHAR(100), " +
                "STUDENT_MIDDLENAME VARCHAR(100), " +
                "STUDENT_SEX VARCHAR(10), " +
                "STUDENT_BIRTHDATE DATE, " +
                "STUDENT_MOTHERTONGUE VARCHAR(50), " +
                "STUDENT_AGE INT, " +
                "STUDENT_IP_TYPE VARCHAR(50), " +
                "STUDENT_RELIGION VARCHAR(50), " +
                "FOREIGN KEY (PARENT_ID) REFERENCES PARENTS (PARENT_ID), " +
                "FOREIGN KEY (GUARDIAN_ID) REFERENCES GUARDIAN (GUARDIAN_ID), " +
                "FOREIGN KEY (ADDRESS_ID) REFERENCES ADDRESS (ADDRESS_ID), " +
                "FOREIGN KEY (CONTACT_ID) REFERENCES CONTACT (CONTACT_ID));",

            "CREATE TABLE PARTICIPANTS (" +
                "PARTICIPANT_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "STUDENT_UID INT NULL, " +
                "PARTICIPANT_TYPE VARCHAR(50), " +
                "PARTICIPANT_LASTNAME VARCHAR(100), " +
                "PARTICIPANT_FIRSTNAME VARCHAR(100), " +
                "PARTICIPANT_SEX VARCHAR(100), " +
                "CONTACT_NUMBER VARCHAR(20), " +
                "FOREIGN KEY (STUDENT_UID) REFERENCES STUDENT (STUDENT_UID));",

            "CREATE TABLE USERS (" +
                "USER_ID INT PRIMARY KEY, " +
                "U_NAME VARCHAR(100), " +
                "U_PASS VARCHAR(100), " +
                "GUIDANCE_COUNSELOR_ID INT, " +
                "CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (GUIDANCE_COUNSELOR_ID) REFERENCES GUIDANCE_COUNSELORS (GUIDANCE_COUNSELOR_ID));",

            "CREATE TABLE REMARK (" +
                "REMARK_ID INT PRIMARY KEY, " +
                "STUDENT_ID INT, " +
                "REMARK_TEXT VARCHAR(255), " +
                "REMARK_DATE DATE, " +
                "FOREIGN KEY (STUDENT_ID) REFERENCES STUDENT (STUDENT_UID));",

            "CREATE TABLE INCIDENTS (" +
                "INCIDENT_ID INT PRIMARY KEY, " +
                "PARTICIPANT_ID INT, " +
                "INCIDENT_DATE DATETIME, " +
                "INCIDENT_DESCRIPTION TEXT, " +
                "ACTION_TAKEN TEXT, " +
                "RECOMMENDATION TEXT, " +
                "STATUS VARCHAR(50), " +
                "UPDATED_AT DATETIME, " +
                "FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS (PARTICIPANT_ID));",

            "CREATE TABLE VIOLATION_RECORD (" +
                "VIOLATION_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "PARTICIPANT_ID INT, " +
                "VIOLATION_TYPE VARCHAR(100), " +
                "VIOLATION_DESCRIPTION TEXT, " +
                "ANECDOTAL_RECORD TEXT, " +
                "REINFORCEMENT VARCHAR(100), " +
                "STATUS VARCHAR(50), " +
                "UPDATED_AT DATETIME, " +
                "FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS (PARTICIPANT_ID));",

            "CREATE TABLE APPOINTMENTS (" +
                "APPOINTMENT_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "GUIDANCE_COUNSELOR_ID INT, " +
                "APPOINTMENT_TITLE VARCHAR(100), " +
                "CONSULTATION_TYPE VARCHAR(50), " +
                "APPOINTMENT_DATE_TIME DATETIME, " +
                "APPOINTMENT_STATUS VARCHAR(50), " +
                "APPOINTMENT_NOTES VARCHAR(100), " +
                "UPDATED_AT DATETIME, " +
                "FOREIGN KEY (GUIDANCE_COUNSELOR_ID) REFERENCES GUIDANCE_COUNSELORS (GUIDANCE_COUNSELOR_ID));",

            "CREATE TABLE APPOINTMENT_PARTICIPANTS (" +
                "APPOINTMENT_ID INT, " +
                "PARTICIPANT_ID INT, " +
                "PRIMARY KEY (APPOINTMENT_ID, PARTICIPANT_ID), " +
                "FOREIGN KEY (APPOINTMENT_ID) REFERENCES APPOINTMENTS (APPOINTMENT_ID), " +
                "FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS (PARTICIPANT_ID));",

            "CREATE TABLE SESSIONS (" +
                "SESSION_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "APPOINTMENT_ID INT, " +
                "GUIDANCE_COUNSELOR_ID INT, " +
                "PARTICIPANT_ID INT, " +
                "VIOLATION_ID INT, " +
                "APPOINTMENT_TYPE VARCHAR(50), " +
                "CONSULTATION_TYPE VARCHAR(50), " +
                "SESSION_DATE_TIME DATETIME, " +
                "SESSION_NOTES TEXT, " +
                "SESSION_STATUS VARCHAR(50), " +
                "UPDATED_AT DATETIME, " +
                "FOREIGN KEY (APPOINTMENT_ID) REFERENCES APPOINTMENTS (APPOINTMENT_ID), " +
                "FOREIGN KEY (GUIDANCE_COUNSELOR_ID) REFERENCES GUIDANCE_COUNSELORS (GUIDANCE_COUNSELOR_ID), " +
                "FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS (PARTICIPANT_ID), " +
                "FOREIGN KEY (VIOLATION_ID) REFERENCES VIOLATION_RECORD (VIOLATION_ID));",

            "CREATE TABLE SESSIONS_PARTICIPANTS (" +
                "SESSIONS_PARTICIPANTS_ID INT PRIMARY KEY AUTO_INCREMENT, " +
                "PARTICIPANT_ID INT, " +
                "SESSION_ID INT, " +
                "FOREIGN KEY (PARTICIPANT_ID) REFERENCES PARTICIPANTS (PARTICIPANT_ID), " +
                "FOREIGN KEY (SESSION_ID) REFERENCES SESSIONS (SESSION_ID));"
        };

        for (String query : tableCreationQueries) {
            stmt.executeUpdate(query);
        }

        System.out.println("✅ Required tables checked/created.");

        createTrigger(stmt);
    }

    private static void createTrigger(Statement stmt) throws SQLException {
        String triggerQuery = 
            "CREATE TRIGGER before_sessions_update " +
            "BEFORE UPDATE ON SESSIONS " +
            "FOR EACH ROW " +
            "SET NEW.UPDATED_AT = NOW();";

        try {
            stmt.executeUpdate(triggerQuery);
            System.out.println("✅ Trigger 'before_sessions_update' created.");
        } catch (SQLException e) {
            if (e.getMessage().contains("already exists")) {
                System.out.println("⚠️ Trigger 'before_sessions_update' already exists.");
            } else {
                throw e;
            }
        }
    }
}
