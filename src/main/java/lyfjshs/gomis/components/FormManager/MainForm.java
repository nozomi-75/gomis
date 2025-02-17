package lyfjshs.gomis.components.FormManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;
import raven.modal.Drawer;

public class MainForm extends JPanel {

	public MainForm() {
		init();
	}

	private void init() {
		setLayout(new MigLayout("fillx,wrap,insets 0,gap 0", "[fill]", "[][grow]"));
		add(createHeader());
		add(createMain(), "grow");
	}

	private JPanel createHeader() {
		JPanel panel = new JPanel(new MigLayout("insets 3", "[]push[]push", "[fill]"));
		JToolBar toolBar = new JToolBar();

		JButton buttonDrawer = new JButton(new FlatSVGIcon("drawer/icon/menu.svg", 0.5f));
		
		URL iconUrl = getClass().getClassLoader().getResource("drawer/icon/menu.svg");
		System.out.println(iconUrl != null ? "Icon found: " + iconUrl : "Icon not found!");
		buttonDrawer.addActionListener(e -> {
			if (Drawer.isOpen()) {
				Drawer.showDrawer();
			} else {
				Drawer.toggleMenuOpenMode();
			}
		});
		
		toolBar.add(buttonDrawer);

		panel.add(toolBar);
		return panel;
	}
	
	private Component createMain() {
		mainPanel = new JPanel(new BorderLayout());
		return mainPanel;
	}

	public void setForm(Form form) {
		mainPanel.removeAll();
		mainPanel.add(form);
		mainPanel.repaint();
		mainPanel.revalidate();

	}

	private JPanel mainPanel;
}
