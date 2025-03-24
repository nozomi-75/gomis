package lyfjshs.gomis.view.login;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

import lyfjshs.gomis.Database.DAO.GuidanceCounselorDAO;
import lyfjshs.gomis.Database.DAO.LoginController;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import net.miginfocom.swing.MigLayout;

public class SignUpPanel extends JPanel {
	private JLabel passwordStrengthLabel;
	private ButtonGroup groupGender;
	private Connection connDB;
	private LoginView parent;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JPasswordField txtConfirmPassword;
	private JTextField txtEmail;
	private JTextField txtContact;
	private JTextField txtSpecialization;
	private JTextField txtWorkPosition;
	private JTextField txtFirstName;
	private JTextField txtMiddleName;
	private JTextField txtLastName;
	private JTextField txtSuffix;
	private JLabel profilePicture;
	private JLabel lblNewLabel;

	public SignUpPanel(Connection conn, LoginView parent) {
		this.connDB = conn;
		this.parent = parent;
		setLayout(new MigLayout("wrap,fillx,insets 5", "[500,grow,fill]", "[center][][grow][][][][][][][][][][][][][][][grow]"));
		this.setOpaque(false);
		putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

		groupGender = new ButtonGroup();
		txtUsername = new JTextField();
		txtPassword = new JPasswordField();
		txtConfirmPassword = new JPasswordField();
		txtEmail = new JTextField();
		txtContact = new JTextField();
		txtSpecialization = new JTextField();
		txtWorkPosition = new JTextField();
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

		
		btnSignUp.putClientProperty(FlatClientProperties.STYLE, "[light]background:darken(@background,10%);"
				+ "[dark]background:lighten(@background,10%);" + "borderWidth:0; focusWidth:0; innerFocusWidth:0;");

		JLabel titleLabel = new JLabel("SignUp", SwingConstants.CENTER);
		titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

		add(titleLabel, "cell 0 0,aligny center");

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		add(panel_1, "cell 0 1,alignx center,growy");
		panel_1.setLayout(new MigLayout("", "[grow][170px,center][grow]", "[170px]"));

		profilePicture = new JLabel("Click to Select a Profile", SwingConstants.CENTER);
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
		panel_2.setOpaque(false);
		add(panel_2, "cell 0 2,grow");
		panel_2.setLayout(new MigLayout("", "[64px,grow][64px,grow][64px,grow][][70px]", "[26px]"));

		txtFirstName = new JTextField();
		panel_2.add(txtFirstName, "cell 0 0,grow");
		txtMiddleName = new JTextField();
		panel_2.add(txtMiddleName, "cell 1 0,grow");
		txtLastName = new JTextField();
		panel_2.add(txtLastName, "cell 2 0,grow");
		txtSuffix = new JTextField();
		panel_2.add(txtSuffix, "cell 4 0,grow");
		txtSuffix.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Suffix (Optional)");
		txtLastName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Last Name");
		txtMiddleName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Middle Name");
		txtFirstName.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "First Name");

		JPanel genderPanel = new JPanel();
		genderPanel.setOpaque(false);
		add(genderPanel, "cell 0 3");
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

		add(txtSpecialization, "cell 0 4");
		add(txtWorkPosition, "cell 0 5");
		add(txtEmail, "cell 0 6");
		add(txtContact, "cell 0 7");

		add(new FlatSeparator(), "cell 0 8,grow");

		lblNewLabel = new JLabel("Username");
		add(lblNewLabel, "cell 0 9");
		add(txtUsername, "cell 0 10");
		add(new JLabel("Password"), "cell 0 11,gapy 8");
		add(txtPassword, "cell 0 12");
		add(passwordStrengthLabel, "cell 0 13,gapy 5");
		add(new JLabel("Confirm Password"), "cell 0 14,gapy 5");
		add(txtConfirmPassword, "cell 0 15");
		add(btnSignUp, "cell 0 16,gapy 20");

		JPanel panel = new JPanel();
		panel.setOpaque(false); // Transparent background
		// Separate JLabel for static text
		JLabel haveAccountLabel = new JLabel("Have an account?");
		JLabel loginLabel = new JLabel(" Login instead");
		loginLabel.setForeground(new Color(0, 102, 204)); // Blue color for link
		loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		loginLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				parent.switchToLoginPanel();
			}
		});
		panel.add(haveAccountLabel);
		panel.add(loginLabel);

		add(panel, "cell 0 17,grow");

		txtContact.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!Character.isDigit(c) || txtContact.getText().length() >= 11) {
					e.consume();
				}
			}
		});

		btnSignUp.addActionListener(e -> handleSignUp());
	}

	private void handleSignUp() {
		if (!validateFields()) {
			return;
		}

		try {
			GuidanceCounselor newCounselor = new GuidanceCounselor(0, // ID will be auto-generated
					txtLastName.getText(), txtFirstName.getText(), txtMiddleName.getText(), txtSuffix.getText(),
					groupGender.getElements().nextElement().isSelected() ? "Male" : "Female",
					txtSpecialization.getText(), txtContact.getText(), txtEmail.getText(), txtWorkPosition.getText(),
					getProfilePictureBytes());

			GuidanceCounselorDAO counselorDAO = new GuidanceCounselorDAO(connDB);
			int generatedId = counselorDAO.createGuidanceCounselor(newCounselor);

			if (generatedId > 0) {
				LoginController loginController = new LoginController(connDB);
				String username = txtUsername.getText();
				String password = new String(txtPassword.getPassword());

				try {
					loginController.createUser(username, password, generatedId).executeUpdate();

					JOptionPane.showMessageDialog(this, "Account created successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);

					clearFields();
					parent.switchToLoginPanel();
				} catch (SQLException e) {
					// If user creation fails, delete the counselor
					counselorDAO.deleteGuidanceCounselor(generatedId);
					throw e;
				}
			} else {
				JOptionPane.showMessageDialog(this, "Failed to create account: Could not generate counselor ID",
						"Error", JOptionPane.ERROR_MESSAGE);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private boolean validateFields() {
		// Check for empty required fields
		if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty() || txtUsername.getText().isEmpty()
				|| new String(txtPassword.getPassword()).isEmpty()
				|| new String(txtConfirmPassword.getPassword()).isEmpty() || txtEmail.getText().isEmpty()
				|| txtContact.getText().isEmpty() || txtSpecialization.getText().isEmpty()
				|| txtWorkPosition.getText().isEmpty()) {

			JOptionPane.showMessageDialog(this, "Please fill in all required fields", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// Validate password match
		if (!new String(txtPassword.getPassword()).equals(new String(txtConfirmPassword.getPassword()))) {
			JOptionPane.showMessageDialog(this, "Passwords do not match", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// Validate email format
		if (!txtEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
			JOptionPane.showMessageDialog(this, "Please enter a valid email address", "Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	private byte[] getProfilePictureBytes() {
		if (profilePicture.getIcon() != null) {
			ImageIcon icon = (ImageIcon) profilePicture.getIcon();
			BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
					BufferedImage.TYPE_INT_RGB);
			icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);

			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				ImageIO.write(bufferedImage, "jpg", baos);
				return baos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	private void clearFields() {
		txtFirstName.setText("");
		txtMiddleName.setText("");
		txtLastName.setText("");
		txtSuffix.setText("");
		txtUsername.setText("");
		txtPassword.setText("");
		txtConfirmPassword.setText("");
		txtEmail.setText("");
		txtContact.setText("");
		txtSpecialization.setText("");
		txtWorkPosition.setText("");
		profilePicture.setIcon(null);
		profilePicture.setText("Click to Select a Profile");
	}

	private void updatePasswordStrength(JPasswordField passwordField) {
		String password = new String(passwordField.getPassword());
		int strength = password.length() < 6 ? 1
				: password.matches(".*[A-Z].*") && password.matches(".*[!@#$%^&*].*") ? 3 : 2;
		passwordStrengthLabel.setText(strength == 1 ? "Weak" : strength == 2 ? "Medium" : "Strong");
		passwordStrengthLabel.setForeground(strength == 1 ? Color.RED : strength == 2 ? Color.ORANGE : Color.GREEN);
	}
}