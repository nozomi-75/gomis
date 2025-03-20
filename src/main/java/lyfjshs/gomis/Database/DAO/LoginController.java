package lyfjshs.gomis.Database.DAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.FormManager.FormManager;

/**
 * The LoginController class provides methods for interacting with the 'users' table in the database.
 * It includes functionality to hash passwords securely, validate user login, and manage user records
 * (create, read, update, delete) for guidance counselors.
 */
public class LoginController {

    private Connection connection;

    public LoginController(Connection connect) {
        this.connection = connect;
    
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
        // SQL query to insert a new user
        String sql = "INSERT INTO users (u_name, u_pass, guidance_counselor_id, CREATED_AT) VALUES (?, ?, ? NOW())";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, username);
        ps.setString(2, hashPassword(password));  // Store hashed password
        if (guidanceCounselorId != null) {
            ps.setInt(3, guidanceCounselorId);  // Set the guidance counselor ID
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);  // Set null if no counselor ID provided
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
            ps = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE u_name = ? AND U_PASS = ?");
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                // Do not close the connection here if it's shared
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
                            System.out.println("Counselor Details while LOGING IN: " + counselor.getFirstName() + " " + counselor.getLastName() + ", " + counselor.getPosition());
                            
                            // First set the counselor details
                            Main.formManager.setCounselorDetails(counselor);
                            Main.formManager.setCounselorID(counselor.getGuidanceCounselorId());
                            
                            // Debug: Verify details set in FormManager
                            System.out.println("Set in FormManager: " + Main.formManager.getCounselorFullName() + ", " + Main.formManager.getCounselorPosition());
                            
                            // Then call login() which will create the drawer with the updated details
                            FormManager.login(connection);							
                            // Close the counselor resources
                            counselorRs.close();
                            counselorPs.close();
                        } else {
                            JOptionPane.showMessageDialog(view, "Counselor details not found", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(view, "User is not a guidance counselor", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(view, "Invalid username or password", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}