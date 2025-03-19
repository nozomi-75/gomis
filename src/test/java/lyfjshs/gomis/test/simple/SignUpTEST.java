package lyfjshs.gomis.test.simple;

import java.awt.Color;
import java.awt.Dimension;
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
import com.formdev.flatlaf.extras.components.FlatSeparator;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import net.miginfocom.swing.MigLayout;

public class SignUpTEST {
	private static JLabel passwordStrengthLabel;

	public static void main(String[] args) {
		FlatMacLightLaf.setup();

		JFrame frame = new JFrame("Sign Up");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1270, 900);
		frame.setLocationRelativeTo(null);
		ButtonGroup groupGender = new ButtonGroup();

		JPanel framePanel = new JPanel(new MigLayout("", "[grow][][grow]", "[grow][][grow]"));
		frame.setContentPane(framePanel);

		JPanel panel = new JPanel(
				new MigLayout("wrap,fillx,insets 10", "[500,grow,fill]", "[][][grow][][][][][][][][][][][][][][]"));
		panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

		JLabel titleLabel = new JLabel("Create an Account", SwingConstants.CENTER);
		titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
		JTextField txtUsername = new JTextField();
		JPasswordField txtPassword = new JPasswordField();
		JPasswordField txtConfirmPassword = new JPasswordField();
		JTextField txtEmail = new JTextField();
		JTextField txtContact = new JTextField();
		JTextField txtSpecialization = new JTextField();
		JTextField txtWorkPosition = new JTextField();
		JButton btnSignUp = new JButton("Sign Up");
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
			public void insertUpdate(DocumentEvent e) {
				updatePasswordStrength(txtPassword);
			}

			public void removeUpdate(DocumentEvent e) {
				updatePasswordStrength(txtPassword);
			}

			public void changedUpdate(DocumentEvent e) {
				updatePasswordStrength(txtPassword);
			}
		});

		btnSignUp.putClientProperty(FlatClientProperties.STYLE,
				"background:darken(@background,10%); borderWidth:0; focusWidth:0");

		panel.add(titleLabel, "cell 0 0,growy,gapy 10");

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

		JPanel panel_2 = new JPanel();
		panel.add(panel_2, "cell 0 2,grow");
		panel_2.setLayout(new MigLayout("", "[64px,grow][64px,grow][64px,grow][][70px]", "[26px]"));

		JTextField txtFirstName = new JTextField();
		panel_2.add(txtFirstName, "cell 0 0,grow");
		JTextField txtMiddleName = new JTextField();
		panel_2.add(txtMiddleName, "cell 1 0,grow");
		JTextField txtLastName = new JTextField();
		panel_2.add(txtLastName, "cell 2 0,grow");
		JTextField txtSuffix = new JTextField();
		panel_2.add(txtSuffix, "cell 4 0,grow");
		txtSuffix.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Suffix (Optional)");
		txtLastName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Last Name");
		txtMiddleName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Middle Name");

		txtFirstName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "First Name");

		JPanel genderPanel = new JPanel();
		genderPanel.setOpaque(false);
		panel.add(genderPanel, "cell 0 3");
		genderPanel.setLayout(new MigLayout("", "[][][]", "[]"));
		JLabel label = new JLabel("Gender");
		genderPanel.add(label, "cell 0 0");
		JRadioButton maleButton = new JRadioButton("Male");
		genderPanel.add(maleButton, "cell 1 0,alignx left,aligny top");
		groupGender.add(maleButton);
		maleButton.setSelected(true);
		JRadioButton femaleButton = new JRadioButton("Female");
		genderPanel.add(femaleButton, "cell 2 0,alignx left,aligny top");
		groupGender.add(femaleButton);
		panel.add(txtSpecialization, "cell 0 4");
		panel.add(txtWorkPosition, "cell 0 5");
		panel.add(txtEmail, "cell 0 6");
		panel.add(txtContact, "cell 0 7");

		panel.add(new FlatSeparator(), "cell 0 8,grow");
		panel.add(txtUsername, "cell 0 9");
		panel.add(new JLabel("Password"), "cell 0 10,gapy 8");
		panel.add(txtPassword, "cell 0 11");
		panel.add(passwordStrengthLabel, "cell 0 12,gapy 5");
		panel.add(new JLabel("Confirm Password"), "cell 0 13,gapy 5");
		panel.add(txtConfirmPassword, "cell 0 14");
		panel.add(btnSignUp, "cell 0 15,gapy 20");
		framePanel.add(panel, "cell 1 1");

		txtContact.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) || txtContact.getText().length() >= 11) {
					e.consume();
				}
			}
		});
		frame.setVisible(true);
	}

	private static void updatePasswordStrength(JPasswordField passwordField) {
		String password = new String(passwordField.getPassword());
		int strength = password.length() < 6 ? 1
				: password.matches(".*[A-Z].*") && password.matches(".*[!@#$%^&*].*") ? 3 : 2;
		passwordStrengthLabel.setText(strength == 1 ? "Weak" : strength == 2 ? "Medium" : "Strong");
		passwordStrengthLabel.setForeground(strength == 1 ? Color.RED : strength == 2 ? Color.ORANGE : Color.GREEN);
	}
}
