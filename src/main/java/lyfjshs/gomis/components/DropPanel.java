package lyfjshs.gomis.components;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class DropPanel extends JPanel {
	private JPanel dropdownPanel;
	private JPanel contentPanel;
	private boolean isDropdownVisible;

	private String panelConstraints = "hidemode 3, growx";


	public DropPanel() {
		setLayout(new MigLayout("fillx, wrap 1, insets 0", "[grow]", "[][grow]"));

		// Create dropdown panel
		dropdownPanel = new JPanel();
		dropdownPanel.setLayout(new MigLayout("fillx, insets 5", "[grow]", "[]"));
		dropdownPanel.setVisible(false);
		add(dropdownPanel, panelConstraints);
	}

	public void setContent(JComponent content) {
		dropdownPanel.removeAll();
		this.contentPanel = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[grow]"));
		this.contentPanel.add(content, "grow");
		dropdownPanel.add(this.contentPanel, "grow");
		revalidate();
		repaint();
	}

	public void toggleDropdown() {
		setDropdownVisible(!isDropdownVisible);
	}

	public void setDropdownVisible(boolean visible) {
		if (isDropdownVisible == visible)
			return;

		isDropdownVisible = visible;
		dropdownPanel.setVisible(visible);
		revalidate();
		repaint();
	}




	public void setPanelConstraints(String constraints) {
		this.panelConstraints = constraints;
		remove(dropdownPanel);
		add(dropdownPanel, panelConstraints);
		revalidate();
	}

	public interface DropdownListener {
		void onDropdownStateChanged(boolean isVisible);
	}

	// Getters
	public boolean isDropdownVisible() {
		return isDropdownVisible;
	}

	public JPanel getDropdownPanel() {
		return dropdownPanel;
	}

	public JPanel getContentPanel() {
		return contentPanel;
	}


	public void setDropdownPadding(int top, int left, int bottom, int right) {
		dropdownPanel.setLayout(
				new MigLayout("fillx, insets " + top + " " + left + " " + bottom + " " + right, "[grow]", "[]"));
		revalidate();
	}
}