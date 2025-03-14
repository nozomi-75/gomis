package lyfjshs.gomis.test.Modal;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import raven.modal.component.Modal;

public class TestModalPane extends Modal {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public TestModalPane() {
		this.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);

	}

}
