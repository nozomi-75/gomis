/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.Database.DAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.Appointment;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.notification.NotificationManager;
import lyfjshs.gomis.utils.EventBus;

/**
 * The LoginController class provides methods for interacting with the 'users' table in the database.
 * It includes functionality to hash passwords securely, validate user login, and manage user records
 * (create, read, update, delete) for guidance counselors.
 */
public class LoginController {

    private final Connection connection;
    private GuidanceCounselor currentUser;

    public LoginController(Connection connection) {
        this.connection = connection;
    }

    /**
     * Hashes the given password using the SHA-256 algorithm.
     * This method converts the password into a hexadecimal string representation of the hash.
     *
     * @param password the plain text password to be hashed.
     * @return a hexadecimal string representing the hashed password.
     * @throws RuntimeException if there is an error during the hashing process.
     */
    public String hashPassword(String password) {
        try {
            // Get the SHA-256 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Hash the password and return the result as a byte array
            byte[] hash = digest.digest(password.getBytes());
            // Convert the byte array into a readable hexadecimal format
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b)); // Convert each byte to its hex equivalent
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // In case SHA-256 algorithm is not available (which should never happen)
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Checks if the given password matches the stored hashed password.
     * This method compares the hashed version of the input password with the stored hash.
     *
     * @param storedHash the hashed password stored in the database.
     * @param password   the plain text password to be validated.
     * @return true if the passwords match (after hashing the input password), false otherwise.
     */
    public boolean isPasswordValid(String storedHash, String password) {
        // Hash the input password and compare with the stored hash
        String hashedPassword = hashPassword(password);
        return hashedPassword.equals(storedHash);  // Validate password match
    }

    /**
     * Creates a new user in the 'users' table by storing the username, hashed password,
     * and optionally linking a guidance counselor ID.
     *
     * @param connection          the database connection to execute the query.
     * @param username            the username for the new user.
     * @param password            the plain text password for the new user.
     * @param guidanceCounselorId the ID of the associated guidance counselor (optional).
     * @return a PreparedStatement object to execute the query.
     * @throws SQLException if there is an error executing the SQL query.
     */
    public PreparedStatement createUser(String username, String password,
                                        Integer guidanceCounselorId) throws SQLException {
        // Remove USER_ID from the insert statement since it's now auto-increment
        String sql = "INSERT INTO users (u_name, u_pass, guidance_counselor_id, CREATED_AT) " +
                     "VALUES (?, ?, ?, (NOW()))";
        PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setString(1, username);
        ps.setString(2, hashPassword(password));
        if (guidanceCounselorId != null) {
            ps.setInt(3, guidanceCounselorId);
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);
        }
        return ps;
    }

    /**
     * Validates a user by checking if the username exists in the database and the provided password
     * matches the stored hashed password.
     *
     * @param connection the database connection to execute the query.
     * @param username   the username of the user to be validated.
     * @param password   the plain text password to be validated.
     * @return true if the username exists and the password is correct, false otherwise.
     */
    public boolean isValidUser(String username, String password) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // First get the stored hashed password
            ps = connection.prepareStatement("SELECT U_PASS FROM users WHERE u_name = ?");
            ps.setString(1, username);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("U_PASS");
                // Compare the hashed version of input password with stored hash
                String hashedInput = hashPassword(password);
                return hashedInput.equals(storedHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Retrieves a user by their ID from the 'users' table.
     *
     * @param connection the database connection to execute the query.
     * @param userId     the ID of the user to be retrieved.
     * @return a PreparedStatement object to execute the query.
     * @throws SQLException if there is an error executing the SQL query.
     */
    public PreparedStatement getUserById(int userId) throws SQLException {
        // SQL query to select a user by their user_id
        String sql = "SELECT * FROM users WHERE USER_ID = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        return ps;
    }

    /**
     * Updates the details of an existing user in the 'users' table.
     * The username, password, and optionally the guidance counselor ID can be updated.
     *
     * @param connection          the database connection to execute the query.
     * @param userId              the ID of the user to be updated.
     * @param username            the new username.
     * @param password            the new plain text password.
     * @param guidanceCounselorId the new guidance counselor ID (optional).
     * @return a PreparedStatement object to execute the query.
     * @throws SQLException if there is an error executing the SQL query.
     */
    public PreparedStatement updateUser(int userId, String username, String password,
                                         Integer guidanceCounselorId) throws SQLException {
        // SQL query to update the user's details
        String sql = "UPDATE users SET U_NAME = ?, U_PASS = ?, GUIDANCE_COUNSELOR_ID = ? WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, hashPassword(password));  // Store hashed password
        if (guidanceCounselorId != null) {
            ps.setInt(3, guidanceCounselorId);  // Set the guidance counselor ID
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);  // Set null if no counselor ID provided
        }
        ps.setInt(4, userId);
        return ps;
    }

    /**
     * Deletes a user by their ID from the 'users' table.
     *
     * @param connection the database connection to execute the query.
     * @param userId     the ID of the user to be deleted.
     * @return a PreparedStatement object to execute the query.
     * @throws SQLException if there is an error executing the SQL query.
     */
    public PreparedStatement deleteUser(int userId) throws SQLException {
        // SQL query to delete a user by their user_id
        String sql = "DELETE FROM users WHERE user_id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, userId);
        return ps;
    }

    // Add helper method for showing error messages
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void login(JTextField usernameTF, JPasswordField passwordTF, JFrame view) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String username = usernameTF.getText();
            String password = String.valueOf(passwordTF.getPassword());

            boolean validUser = isValidUser(username, password);

            if (validUser) {
                ps = connection.prepareStatement("SELECT user_id, guidance_counselor_id FROM USERS WHERE U_NAME = ?");
                ps.setString(1, username);
                rs = ps.executeQuery();

                if (rs.next()) {
                    int guidanceCounselorId = rs.getInt("guidance_counselor_id");

                    if (guidanceCounselorId != 0) {
                        PreparedStatement counselorPs = connection.prepareStatement(
                            "SELECT * FROM GUIDANCE_COUNSELORS WHERE guidance_counselor_id = ?"
                        );
                        counselorPs.setInt(1, guidanceCounselorId);
                        ResultSet counselorRs = counselorPs.executeQuery();
                        
                        if (counselorRs.next()) {
                            GuidanceCounselor counselor = new GuidanceCounselor(
                                counselorRs.getInt("guidance_counselor_id"),
                                counselorRs.getString("LAST_NAME"),
                                counselorRs.getString("FIRST_NAME"),
                                counselorRs.getString("MIDDLE_NAME"),
                                counselorRs.getString("SUFFIX"),
                                counselorRs.getString("GENDER"),
                                counselorRs.getString("SPECIALIZATION"),
                                counselorRs.getString("CONTACT_NUM"),
                                counselorRs.getString("EMAIL"),
                                counselorRs.getString("POSITION"),
                                counselorRs.getBytes("PROFILE_PICTURE")
                            );

                            // Debug: Print counselor details
                            System.out.println("Counselor Details while LOGGING IN: " + counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition());
                            
                            try {
                                // First set the counselor details
                                FormManager.setCounselorObject(counselor);
                                System.out.println("Set in FormManager: " + counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition());
                                
                                // Make view invisible first to prevent it from being shown alongside the main form
                                view.setVisible(false);
                                
                                // Show the main dashboard with all features
                                FormManager.login(connection);
                                
                                // Notify subscribers that a counselor has logged in
                                EventBus.publish("counselor_logged_in", counselor);
                                
                                // Ensure the main frame is visible and focused
                                JFrame mainFrame = FormManager.getFrame();
                                if (mainFrame != null) {
                                    mainFrame.setVisible(true);
                                    mainFrame.setExtendedState(JFrame.NORMAL);
                                    mainFrame.toFront();
                                    mainFrame.requestFocus();
                                }
                            } catch (Exception ex) {
                                System.err.println("Error loading main dashboard: " + ex.getMessage());
                                ex.printStackTrace();
                                showError("Error loading main dashboard: " + ex.getMessage());
                            }
                        } else {
                            showError("Counselor information not found.");
                        }
                        counselorRs.close();
                        counselorPs.close();
                    } else {
                        showError("This account is not linked to a counselor.");
                    }
                } else {
                    showError("User information not found.");
                }
            } else {
                showError("Invalid username or password.");
            }
        } catch (SQLException ex) {
            System.err.println("Database error during login: " + ex.getMessage());
            ex.printStackTrace();
            showError("Database error: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("General error during login: " + ex.getMessage());
            ex.printStackTrace();
            showError("Error: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkUpcomingAppointments(int counselorId) {
        try {
            AppointmentDAO appointmentDAO = new AppointmentDAO(connection);
            List<Appointment> appointments = appointmentDAO.getUpcomingAppointments();
            LocalDateTime now = LocalDateTime.now();
            NotificationManager notificationManager = Main.gFrame.getNotificationManager();

            // Get counselor details
            GuidanceCounselor counselor = Main.formManager.getCounselorObject();
            String counselorName = counselor != null ? 
                String.format("%s %s", counselor.getFirstName(), counselor.getLastName()) : 
                "Counselor";

            // Filter appointments for today only
            List<Appointment> todayAppointments = appointments.stream()
                .filter(appointment -> {
                    LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
                    return appointmentTime.toLocalDate().equals(now.toLocalDate());
                })
                .toList();

            if (todayAppointments.isEmpty()) {
                notificationManager.showInfoNotification(
                    "Welcome Back", 
                    String.format("Welcome back %s! You have no appointments scheduled for today.", counselorName)
                );
                return;
            }

            // Show summary notification for today's appointments
            notificationManager.showInfoNotification(
                "Welcome Back", 
                String.format("Welcome back %s! You have %d appointment(s) today.", 
                    counselorName, todayAppointments.size())
            );

            // Show details for each upcoming appointment today
            for (Appointment appointment : todayAppointments) {
                LocalDateTime appointmentTime = appointment.getAppointmentDateTime().toLocalDateTime();
                long minutesUntil = ChronoUnit.MINUTES.between(now, appointmentTime);

                if (minutesUntil > 0) {
                    String timeMessage;
                    if (minutesUntil < 60) {
                        timeMessage = "today at " + appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
                    } else {
                        timeMessage = "today at " + appointmentTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
                    }

                    notificationManager.showInfoNotification(
                        "Today's Appointment", 
                        appointment.getAppointmentTitle() + " " + timeMessage
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM GUIDANCE_COUNSELORS WHERE USERNAME = ? AND PASSWORD = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Create GuidanceCounselor object from result set
                    currentUser = new GuidanceCounselor(
                        rs.getInt("GUIDANCE_COUNSELOR_ID"),
                        rs.getString("LAST_NAME"),
                        rs.getString("FIRST_NAME"),
                        rs.getString("MIDDLE_NAME"),
                        rs.getString("SUFFIX"),
                        rs.getString("GENDER"),
                        rs.getString("SPECIALIZATION"),
                        rs.getString("CONTACT_NUM"),
                        rs.getString("EMAIL"),
                        rs.getString("POSITION"),
                        rs.getBytes("PROFILE_PICTURE")
                    );
                    return true;
                }
            }
        }
        return false;
    }

    public GuidanceCounselor getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean changePassword(String oldPassword, String newPassword) throws SQLException {
        if (!isLoggedIn()) {
            return false;
        }

        // First verify old password
        String verifySql = "SELECT * FROM GUIDANCE_COUNSELOR WHERE COUNSELOR_ID = ? AND PASSWORD = ?";
        try (PreparedStatement verifyStmt = connection.prepareStatement(verifySql)) {
            verifyStmt.setInt(1, currentUser.getCounselorId());
            verifyStmt.setString(2, oldPassword);
            
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (!rs.next()) {
                    return false; // Old password doesn't match
                }
            }
        }

        // Update to new password
        String updateSql = "UPDATE GUIDANCE_COUNSELOR SET PASSWORD = ? WHERE COUNSELOR_ID = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            updateStmt.setString(1, newPassword);
            updateStmt.setInt(2, currentUser.getCounselorId());
            
            int rowsAffected = updateStmt.executeUpdate();
            if (rowsAffected > 0) {
                currentUser.setPassword(newPassword);
                return true;
            }
        }
        return false;
    }

    public boolean updateProfile(String firstName, String lastName, String email, String contactNumber) throws SQLException {
        if (!isLoggedIn()) {
            return false;
        }

        String sql = "UPDATE GUIDANCE_COUNSELOR SET FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, CONTACT_NUMBER = ? " +
                    "WHERE COUNSELOR_ID = ?";
                    
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, contactNumber);
            stmt.setInt(5, currentUser.getCounselorId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update current user object
                currentUser.setFirstName(firstName);
                currentUser.setLastName(lastName);
                currentUser.setEmail(email);
                currentUser.setContactNumber(contactNumber);
                return true;
            }
        }
        return false;
    }
}