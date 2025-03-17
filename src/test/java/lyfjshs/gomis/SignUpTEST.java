package lyfjshs.gomis;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;

public class SignUpTEST {
    public static void main(String[] args) {
        // Setup FlatLaf
        FlatLightLaf.setup();

        JFrame frame = new JFrame("Sign-Up Creation");
        frame.setSize(950, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new MigLayout("insets 10", "[140][350,fill][grow][]", "[][][][][][]"));
        mainPanel.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        frame.getContentPane().add(scrollPane);

        // Title
        JLabel titleLabel = new JLabel("SIGN-UP CREATION");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(titleLabel, "cell 0 0 3 1");

        String[] labels = { "LAST NAME:", "FIRST NAME:", "MIDDLE NAME:", "SUFFIX:", "SEX:", "SPECIALIZATION:",
                "WORK POSITION:", "EMAIL:", "CONTACT NO:" };
        JTextField[] textFields = new JTextField[labels.length];

        // Form fields
        int row = 1;
        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            mainPanel.add(label, "cell 0 " + row);

            if (labels[i].equals("SEX:")) {
                JComboBox<String> sexComboBox = new JComboBox<>(new String[] { "Male", "Female" });
                mainPanel.add(sexComboBox, "cell 1 " + row);
            } else {
                textFields[i] = new JTextField();
                mainPanel.add(textFields[i], "cell 1 " + row);
            }
            row++;
        }

        // Contact number restriction
        textFields[8].addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || textFields[8].getText().length() >= 11) {
                    e.consume();
                }
            }
        });

        // Separator
        mainPanel.add(new JSeparator(), "cell 0 " + row + " 2 1");
        row++;

        // Username
        JLabel usernameLabel = new JLabel("USERNAME:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        JTextField usernameField = new JTextField();
        JLabel usernameWarning = new JLabel("⚠");
        usernameWarning.setForeground(Color.RED);
        usernameWarning.setVisible(false);

        mainPanel.add(usernameLabel, "cell 0 " + row);
        mainPanel.add(usernameField, "cell 1 " + row);
        mainPanel.add(usernameWarning, "cell 2 " + row);
        row++;

        // Password
        JLabel passwordLabel = new JLabel("PASSWORD:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 11));
        JPasswordField passwordField = new JPasswordField();
        JButton togglePassword = new JButton("👁");
        JLabel passwordWarning = new JLabel("⚠");
        passwordWarning.setForeground(Color.RED);
        passwordWarning.setVisible(false);

        mainPanel.add(passwordLabel, "cell 0 " + row);
        mainPanel.add(passwordField, "cell 1 " + row);
        mainPanel.add(togglePassword, "cell 1 " + row + ", split 2, flowx");
        mainPanel.add(passwordWarning, "cell 2 " + row);
        row++;

        togglePassword.addActionListener(e -> {
            passwordField.setEchoChar(passwordField.getEchoChar() == '\u2022' ? (char) 0 : '\u2022');
        });

        // Confirm Password
        JLabel confirmPasswordLabel = new JLabel("CONFIRM PASSWORD:");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 11));
        JPasswordField confirmPasswordField = new JPasswordField();
        JButton toggleConfirmPassword = new JButton("👁");
        JLabel confirmPasswordWarning = new JLabel("⚠");
        confirmPasswordWarning.setForeground(Color.RED);
        confirmPasswordWarning.setVisible(false);

        mainPanel.add(confirmPasswordLabel, "cell 0 " + row);
        mainPanel.add(confirmPasswordField, "cell 1 " + row);
        mainPanel.add(toggleConfirmPassword, "cell 1 " + row + ", split 2, flowx");
        mainPanel.add(confirmPasswordWarning, "cell 2 " + row);
        row++;

        toggleConfirmPassword.addActionListener(e -> {
            confirmPasswordField.setEchoChar(confirmPasswordField.getEchoChar() == '\u2022' ? (char) 0 : '\u2022');
        });

        // Submit Button
        JButton submitButton = new JButton("SIGN UP");
        submitButton.setFont(new Font("Arial", Font.BOLD, 11));
        submitButton.setBackground(Color.BLACK);
        submitButton.setForeground(Color.WHITE);
        submitButton.setEnabled(false);
        mainPanel.add(submitButton, "cell 1 " + row + ", gaptop 10");

        // Profile Picture
        JLabel pictureBox = new JLabel("Select Profile Picture", SwingConstants.CENTER);
        pictureBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pictureBox.setPreferredSize(new java.awt.Dimension(150, 150));
        mainPanel.add(pictureBox, "cell 3 1 1 5");

        pictureBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ImageIcon imageIcon = new ImageIcon(new ImageIcon(selectedFile.getAbsolutePath()).getImage()
                            .getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                    pictureBox.setIcon(imageIcon);
                    pictureBox.setText("");
                }
            }
        });

        // Validation
        KeyAdapter validationListener = new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                boolean usernameValid = usernameField.getText().length() >= 6;
                boolean passwordValid = isStrongPassword(new String(passwordField.getPassword()));
                boolean passwordsMatch = new String(passwordField.getPassword())
                        .equals(new String(confirmPasswordField.getPassword()));

                usernameWarning.setVisible(!usernameValid);
                passwordWarning.setVisible(!passwordValid);
                confirmPasswordWarning.setVisible(!passwordsMatch);

                submitButton.setEnabled(usernameValid && passwordValid && passwordsMatch);
            }
        };

        usernameField.addKeyListener(validationListener);
        passwordField.addKeyListener(validationListener);
        confirmPasswordField.addKeyListener(validationListener);

        submitButton.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Signed Up Successfully!!!", "Success",
                JOptionPane.INFORMATION_MESSAGE));

        frame.setVisible(true);
    }

    private static boolean isStrongPassword(String password) {
        return password.length() >= 6 && password.matches(".*[A-Za-z].*") && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }
}