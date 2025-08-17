/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.utils;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class ValidateGomisUser {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java ValidateGomisUser <username> <password>");
            System.exit(1);
        }
        String username = args[0];
        String password = args[1];
        try {
            Connection conn = DBConnection.getConnection();
            LoginController loginController = new LoginController(conn);
            boolean valid = loginController.isValidUser(username, password);
            conn.close();
            if (valid) {
                System.out.println("VALID");
                System.exit(0);
            } else {
                System.out.println("INVALID");
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class DBConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/gomisdb";
    private static final String USER = "root";
    private static final String DEFAULT_PASSWORD = "YourRootPassword123";
    private static final String DB_PASSWORD_KEY = "db_password";

    public static String loadPassword() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(DBConnection.class);
            String savedPassword = prefs.get(DB_PASSWORD_KEY, null);
            if (savedPassword != null && !savedPassword.isEmpty()) {
                return savedPassword;
            }
        } catch (Exception e) {
            System.err.println("Error loading password: " + e.getMessage());
        }
        return DEFAULT_PASSWORD;
    }

    public static Connection getConnection() throws SQLException {
        String password = loadPassword();
        return DriverManager.getConnection(URL, USER, password);
    }
}

class LoginController {
    private final Connection connection;

    public LoginController(Connection connection) {
        this.connection = connection;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean isValidUser(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT U_PASS FROM users WHERE u_name = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("U_PASS");
                    return hashPassword(password).equals(storedHash);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}