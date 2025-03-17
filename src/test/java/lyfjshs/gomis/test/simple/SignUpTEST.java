package lyfjshs.gomis.test.simple;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import net.miginfocom.swing.MigLayout;

public class SignUpTEST {
    private static JLabel passwordStrengthLabel;
    
    public static void main(String[] args) {
        FlatMacLightLaf.setup();

        JFrame frame = new JFrame("Sign Up");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 780);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 20", "[350,fill]", "[][][][][][][][][][][][][][][][][]"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

        JLabel titleLabel = new JLabel("Create an Account", SwingConstants.CENTER);
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        
        JTextField txtFirstName = new JTextField();
        JTextField txtLastName = new JTextField();
        JTextField txtMiddleName = new JTextField();
        JTextField txtSuffix = new JTextField();
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmPassword = new JPasswordField();
        JTextField txtEmail = new JTextField();
        JTextField txtContact = new JTextField();
        JTextField txtSpecialization = new JTextField();
        JTextField txtWorkPosition = new JTextField();
        JButton btnSignUp = new JButton("Sign Up");

        txtFirstName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "First Name");
        txtLastName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Last Name");
        txtMiddleName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Middle Name");
        txtSuffix.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Suffix (Optional)");
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username");
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        txtConfirmPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Confirm Password");
        txtEmail.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Email");
        txtContact.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Contact No.");
        txtSpecialization.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Specialization");
        txtWorkPosition.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Work Position");
        
        txtPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        
        passwordStrengthLabel = new JLabel();
        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePasswordStrength(txtPassword); }
            public void removeUpdate(DocumentEvent e) { updatePasswordStrength(txtPassword); }
            public void changedUpdate(DocumentEvent e) { updatePasswordStrength(txtPassword); }
        });
        
        btnSignUp.putClientProperty(FlatClientProperties.STYLE, "background:darken(@background,10%); borderWidth:0; focusWidth:0");

        panel.add(titleLabel, "cell 0 0,gapy 10");
        
        JPanel panel_1 = new JPanel();
        panel.add(panel_1, "cell 0 1,alignx center,growy");
        panel_1.setLayout(new MigLayout("", "[grow][200px,center][grow]", "[200px]"));
        
        JLabel profilePicture = new JLabel("Select Profile Picture", SwingConstants.CENTER);
        panel_1.add(profilePicture, "cell 1 0,grow");
        profilePicture.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        profilePicture.setPreferredSize(new Dimension(150, 150));
        profilePicture.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg"));
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ImageIcon imageIcon = new ImageIcon(new ImageIcon(selectedFile.getAbsolutePath()).getImage()
                            .getScaledInstance(150, 150, Image.SCALE_SMOOTH));
                    profilePicture.setIcon(imageIcon);
                    profilePicture.setText("");
                }
            }
        });
        panel.add(txtFirstName, "cell 0 2");
        panel.add(txtLastName, "cell 0 2");
        panel.add(txtMiddleName, "cell 0 3");
        panel.add(txtSuffix, "cell 0 4");
        panel.add(new JLabel("Gender"), "cell 0 5,gapy 8");
        panel.add(createGenderPanel());
        panel.add(txtSpecialization, "cell 0 6");
        panel.add(txtWorkPosition, "cell 0 7");
        panel.add(txtEmail, "cell 0 8");
        panel.add(txtContact, "cell 0 9");
        panel.add(txtUsername, "cell 0 10");
        panel.add(new JLabel("Password"), "cell 0 11,gapy 8");
        panel.add(txtPassword, "cell 0 12");
        panel.add(passwordStrengthLabel, "cell 0 13,gapy 5");
        panel.add(new JLabel("Confirm Password"), "cell 0 14,gapy 5");
        panel.add(txtConfirmPassword, "cell 0 15");
        panel.add(btnSignUp, "cell 0 16,gapy 20");

        frame.getContentPane().add(panel);
        frame.setVisible(true);

        txtContact.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || txtContact.getText().length() >= 11) {
                    e.consume();
                }
            }
        });
    }

    private static Component createGenderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        JRadioButton maleButton = new JRadioButton("Male");
        JRadioButton femaleButton = new JRadioButton("Female");
        ButtonGroup groupGender = new ButtonGroup();
        groupGender.add(maleButton);
        groupGender.add(femaleButton);
        maleButton.setSelected(true);
        panel.add(maleButton);
        panel.add(femaleButton);
        return panel;
    }
    
    private static void updatePasswordStrength(JPasswordField passwordField) {
        String password = new String(passwordField.getPassword());
        int strength = password.length() < 6 ? 1 : password.matches(".*[A-Z].*") && password.matches(".*[!@#$%^&*].*") ? 3 : 2;
        passwordStrengthLabel.setText(strength == 1 ? "Weak" : strength == 2 ? "Medium" : "Strong");
        passwordStrengthLabel.setForeground(strength == 1 ? Color.RED : strength == 2 ? Color.ORANGE : Color.GREEN);
    }
}
