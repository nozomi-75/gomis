package lyfjshs.gomis.view.login;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Database.DAO.LoginController;
import lyfjshs.gomis.components.LogoPanel;
import lyfjshs.gomis.components.FormManager.FormManager;
import net.miginfocom.swing.MigLayout;

public class LoginPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JPasswordField psTField;
	private JTextField unTField;

	private Connection conn;
	private JButton loginBtn;

	public LoginPanel(Connection connDB, LoginView parent) {
		this.conn = connDB; 
		this.setLayout(new MigLayout("wrap,fillx,insets 35 45 30 45", "[pref!,grow,fill]", "[100px][][][][][][]"));
		 putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
		LogoPanel logoPanel;
		try {
			logoPanel = new LogoPanel("/GOMIS_Circle.png", 200, 200);
			this.add(logoPanel, "cell 0 0,grow");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		JLabel unLabel = new JLabel("Username:");
		this.add(unLabel, "cell 0 1,alignx center,aligny center");

		unTField = new JTextField();
		unTField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
		this.add(unTField, "cell 0 2");

		JLabel psLabel = new JLabel("Password:");
		this.add(psLabel, "cell 0 3,alignx center,aligny center");

		psTField = new JPasswordField();
		psTField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true; arc:20;");
		psTField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
		this.add(psTField, "cell 0 4");

		loginBtn = new JButton("Login");
		loginBtn.addActionListener(e -> loginMethod());
		this.add(loginBtn, "cell 0 5");

		loginBtn.putClientProperty(FlatClientProperties.STYLE, "[light]background:darken(@background,10%);"
				+ "[dark]background:lighten(@background,10%);" + "borderWidth:0; focusWidth:0; innerFocusWidth:0;");

		// Panel to hold both labels for correct positioning
		JPanel signUpPanel = new JPanel();
		signUpPanel.setOpaque(false); // Transparent background
		// Separate JLabel for static text
		JLabel noAccountLabel = new JLabel("Don't have an account?");

		// Separate JLabel for clickable "Sign up"
		JLabel signUpLabel = new JLabel(" Sign up");
		signUpLabel.setForeground(new Color(0, 102, 204)); // Blue color for link
		signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// Click event for "Sign up"
		signUpLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				parent.switchToSignUpPanel();
			}
		});
		signUpPanel.add(noAccountLabel);
		signUpPanel.add(signUpLabel);

		// Add the sign-up section below the login button
		this.add(signUpPanel, "cell 0 6, alignx center");

	}

	// Login method with space validation
	private void loginMethod() {
		// Retrieve the text from the username and password fields
		String username = unTField.getText();
		String password = new String(psTField.getPassword());

		// Check if either the username or password contains spaces
		if (username.contains(" ") || password.contains(" ")) {
			// Display an error message
			JOptionPane.showMessageDialog(this, "Invalid username or password. Spaces are not allowed.",
					"Invalid Input", JOptionPane.ERROR_MESSAGE);
			return; // Exit the method to prevent further execution
		}

		// If no spaces, proceed with login
		LoginController loginew = new LoginController(conn);
		loginew.login(unTField, psTField, FormManager.getFrame());
		resetFields();
	}

	/**
	 * Resets all input fields and refreshes the panel. Call this method whenever
	 * the panel needs to be refreshed.
	 */
	public void resetFields() {
		if (unTField != null) {
			unTField.setText("");
		}
		if (psTField != null) {
			psTField.setText("");
		}
		// Request focus on username field for better user experience
		if (unTField != null) {
			unTField.requestFocusInWindow();
		}
		// Refresh the panel
		this.revalidate();
		this.repaint();
	}

	/**
	 * Override the addNotify method to reset fields when the panel becomes visible
	 */
	@Override
	public void addNotify() {
		super.addNotify();
		resetFields();
	}
}
