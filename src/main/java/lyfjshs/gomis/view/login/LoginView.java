package lyfjshs.gomis.view.login;

import java.awt.CardLayout;
import java.awt.Color;
import java.sql.Connection;

import javax.swing.JPanel;

import lyfjshs.gomis.components.ModelColor;
import lyfjshs.gomis.components.PanelGradient;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class LoginView extends Form {

	private static final long serialVersionUID = 1L;
	private PanelGradient panelGradient;
	private LoginPanel loginPanel;
	private SignUpPanel signUpPanel;
	private Connection conn;
	private JPanel mainPanel;

	public LoginView(Connection connDB) {
		if (connDB == null) {
			throw new IllegalArgumentException("Database connection cannot be null.");
		}
		this.conn = connDB;
		initializeComponents();
	}

	/**
	 * Initializes all GUI components.
	 */
	private void initializeComponents() {
		this.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));

		panelGradient = new PanelGradient();
		panelGradient.setLayout(new MigLayout("insets 0, fill", "[]", "[]"));
		panelGradient.addColor(new ModelColor(new Color(0x004aad), 0f), new ModelColor(new Color(0xcb6ce6), 1f));

		mainPanel = new JPanel(new MigLayout("fill", "[grow]", "[grow]"));
		mainPanel.setOpaque(false);

		loginPanel = new LoginPanel(conn, this);
		signUpPanel = new SignUpPanel(conn, this);

		mainPanel.add(loginPanel, "grow");
		panelGradient.add(mainPanel, "align center");
		this.add(panelGradient, "cell 0 0,grow");

	}

	/**
	 * Switches the main panel to the specified panel.
	 * 
	 * @param panel The panel to switch to
	 */
	private void switchPanel(JPanel panel) {
		mainPanel.removeAll();
		mainPanel.add(panel, "grow");
		mainPanel.revalidate();
		mainPanel.repaint();
	}

	public void switchToLoginPanel() {
		switchPanel(loginPanel);
	}

	public void switchToSignUpPanel() {
		switchPanel(signUpPanel);
	}
}
