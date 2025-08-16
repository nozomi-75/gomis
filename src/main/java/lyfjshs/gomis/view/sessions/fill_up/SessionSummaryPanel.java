package lyfjshs.gomis.view.sessions.fill_up;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class SessionSummaryPanel extends JPanel {
	private final JTextArea summaryArea = new JTextArea(5, 0);
	private final JPanel content;

	public SessionSummaryPanel() {
		setOpaque(false);

		// Initialize summary area
		summaryArea.setLineWrap(true);
		summaryArea.setWrapStyleWord(true);
		summaryArea.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
				"Guidance counselor's conclusion about this case (60-100 words recommended)...");

		// Create scroll pane for summary area
		JScrollPane scrollPane = new JScrollPane(summaryArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.putClientProperty("JComponent.arc", 8);

		// Create content panel with MigLayout
		content = new JPanel(new MigLayout("fillx, insets 0", "[grow]", "[grow]"));
		content.setOpaque(false);
		content.add(scrollPane, "hmin 120,grow,wrap");
	}

	public String getSummary() {
		return summaryArea.getText();
	}

	public void setSummary(String summary) {
		summaryArea.setText(summary);
	}

	public void clearSummary() {
		summaryArea.setText("");
	}

	public JPanel getContentPanel() {
		return content;
	}

	public void getData(SessionFormData data) {
		data.summary = getSummary();
	}
	public void setData(SessionFormData data) {
		setSummary(data.summary);
	}

}