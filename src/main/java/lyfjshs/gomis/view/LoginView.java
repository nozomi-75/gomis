package lyfjshs.gomis.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import lyfjshs.gomis.components.ModelColor;
import lyfjshs.gomis.components.PanelGradient;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import net.miginfocom.swing.MigLayout;

public class LoginView extends Form {

	private static final long serialVersionUID = 1L;
	private PanelGradient panelGradient;
	private JPanel loginPanel;
	private JButton loginBtn;
	private JPasswordField psTField;
	private JTextField unTField;
	private JLabel psLabel;
	private JLabel unLabel;
	private Connection conn;

	public LoginView(Connection connDB) {
		if (connDB == null) {
			throw new IllegalArgumentException("Database connection cannot be null.");
		}
		this.conn = connDB;
		initializeComponents();
		resetFields(); // Call reset after initialization
	}

	/**
	 * Initializes all GUI components.
	 */
	private void initializeComponents() {
		this.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));

		try {
			
			LogoPanel logoPanel = new LogoPanel("/LYFJSHS_Logo_0.5x.png", 200, 200);

			panelGradient = new PanelGradient();
			panelGradient.setLayout(new MigLayout("insets 0, fill", "[]", "[]"));
			panelGradient.addColor(new ModelColor(new Color(0x004aad), 0f), new ModelColor(new Color(0xcb6ce6), 1f));

			loginPanel = new JPanel();
			loginPanel.setLayout(
					new MigLayout("wrap,fillx,insets 35 45 30 45", "[pref!,grow,fill]", "[100px,grow][][][][][]"));

			loginPanel.add(logoPanel, "cell 0 0,grow");

			unLabel = new JLabel("Username:");
			loginPanel.add(unLabel, "cell 0 1,alignx center,aligny center");

			unTField = new JTextField();
			unTField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your username");
			loginPanel.add(unTField, "cell 0 2");

			psLabel = new JLabel("Password:");
			loginPanel.add(psLabel, "cell 0 3,alignx center,aligny center");

			psTField = new JPasswordField();
			psTField.putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true;" + "arc:20;");
			psTField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter your password");
			loginPanel.add(psTField, "cell 0 4");

			loginBtn = new JButton("Login");
			loginBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loginMethod();
				}
			});

			loginPanel.add(loginBtn, "cell 0 5");
			panelGradient.add(loginPanel, "align center");
			this.add(panelGradient, "cell 0 0,grow");

			loginBtn.putClientProperty(FlatClientProperties.STYLE,
					"" + "[light]background:darken(@background,10%);" + "[dark]background:lighten(@background,10%);"
							+ "borderWidth:0;" + "focusWidth:0;" + "innerFocusWidth:0");
		} catch (IOException e) {
			e.printStackTrace();
			// Consider adding better error handling here
			JOptionPane.showMessageDialog(this, "Error loading logo: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void loginMethod(){
		LoginController loginew= new LoginController();
		loginew.login(conn, unTField, psTField, FormManager.getFrame());
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
