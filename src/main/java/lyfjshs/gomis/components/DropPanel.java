package lyfjshs.gomis.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class DropPanel extends JPanel {
	private JButton toggleButton;
	private JPanel dropdownPanel;
	private JPanel contentPanel;
	private boolean isDropdownVisible;

	private String buttonConstraints = "growx";
	private String panelConstraints = "hidemode 3, growx";
	private Color buttonBackground;
	private Color buttonForeground;

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

	// Customization methods
	public void setButtonStyle(Color background, Color foreground) {
		this.buttonBackground = background;
		this.buttonForeground = foreground;
		updateButtonStyle();
	}

	private void updateButtonStyle() {
		if (buttonBackground != null) {
			toggleButton.setBackground(buttonBackground);
		}
		if (buttonForeground != null) {
			toggleButton.setForeground(buttonForeground);
		}
	}

	// Layout constraint methods
	public void setButtonConstraints(String constraints) {
		this.buttonConstraints = constraints;
		remove(toggleButton);
		add(toggleButton, buttonConstraints);
		revalidate();
	}

	public void setPanelConstraints(String constraints) {
		this.panelConstraints = constraints;
		remove(dropdownPanel);
		add(dropdownPanel, panelConstraints);
		revalidate();
	}

	// Event handling
	public void addDropdownListener(DropdownListener listener) {
		toggleButton.addActionListener(e -> listener.onDropdownStateChanged(isDropdownVisible));
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

	public JButton getToggleButton() {
		return toggleButton;
	}

	// Additional utility methods
	public void setButton(JButton button) {
		toggleButton = button;
	}

	public void addButtonActionListener(ActionListener listener) {
		toggleButton.addActionListener(listener);
	}

	public void removeButtonActionListener(ActionListener listener) {
		toggleButton.removeActionListener(listener);
	}

	public void setDropdownPadding(int top, int left, int bottom, int right) {
		dropdownPanel.setLayout(
				new MigLayout("fillx, insets " + top + " " + left + " " + bottom + " " + right, "[grow]", "[]"));
		revalidate();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		toggleButton.setEnabled(enabled);
		for (Component comp : dropdownPanel.getComponents()) {
			comp.setEnabled(enabled);
		}
	}
}