package lyfjshs.gomis.components;

import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;

public class DropPanel extends JPanel {
	private static final Logger logger = LogManager.getLogger(DropPanel.class);
	private JPanel dropdownPanel;
	private JPanel contentPanel;
	private boolean isDropdownVisible;
	private ActionListener clickListener;
	private String panelConstraints = "hidemode 3, growx";
	private boolean sticky = false;

	public DropPanel() {
		setLayout(new MigLayout("fillx, wrap 1, insets 0", "[grow]", "[][grow]"));
		dropdownPanel = new JPanel();
		dropdownPanel.setLayout(new MigLayout("fillx, insets 5", "[grow]", "[]"));
		dropdownPanel.setVisible(false);
		add(dropdownPanel, panelConstraints);

		// Prevent focus loss from hiding the dropdown
		dropdownPanel.setFocusable(true);
		dropdownPanel.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				// Do nothing: prevent auto-hide on focus loss
			}
		});
	}

	public DropPanel(FlatSVGIcon icon, String title, String description) {
		this();
		JPanel headerPanel = new JPanel(new MigLayout("fillx, insets 5", "[grow]", "[]"));
		headerPanel.add(new JLabel(icon), "cell 0 0");
		headerPanel.add(new JLabel(title), "cell 1 0");
		headerPanel.add(new JLabel(description), "cell 2 0");
		add(headerPanel, "growx");
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
		if (!visible && sticky) {
			logger.debug("DropPanel: Prevented auto-hide due to sticky=true");
			return;
		}
		if (isDropdownVisible == visible)
			return;

		// Improved debug log for dropdown visibility changes
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		String caller = stack.length > 2 ? stack[2].toString() : "unknown";
		StringBuilder trace = new StringBuilder();
		for (int i = 2; i < Math.min(stack.length, 7); i++) {
			trace.append("\n    at ").append(stack[i]);
		}
		logger.debug("DropPanel: setDropdownVisible({}) called by {}. Stack:{}", visible, caller, trace.toString());

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

	public void addClickListener(ActionListener listener) {
		this.clickListener = listener;
		addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (clickListener != null) {
					clickListener.actionPerformed(new java.awt.event.ActionEvent(this, 
						java.awt.event.ActionEvent.ACTION_PERFORMED, "click"));
				}
			}
		});
	}

	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}
}