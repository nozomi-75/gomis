package lyfjshs.gomis.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class CirclePanel extends JPanel {
	private JPanel componentPanel;

	public CirclePanel(String number, String title, JComponent content) {
		setLayout(new MigLayout("insets 20, fillx", "[][grow,fill]", "[][grow]"));
		setOpaque(false);
		putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");
		
		// --- Responsive Circle Number using JLabel and FlatLaf ---
		JLabel circleLabel = new JLabel(number, SwingConstants.CENTER);
		circleLabel.setOpaque(true);
		circleLabel.setFont(circleLabel.getFont().deriveFont(Font.BOLD, 18f));
		circleLabel.setPreferredSize(new Dimension(36, 36));
		circleLabel.putClientProperty(FlatClientProperties.STYLE, "arc:999; background:#3498db; foreground:#FFFFFF");
		circleLabel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int size = Math.min(circleLabel.getWidth(), circleLabel.getHeight());
				float fontSize = Math.max(12f, size * 0.5f);
				circleLabel.setFont(circleLabel.getFont().deriveFont(Font.BOLD, fontSize));
			}
		});

		JLabel titleLabel = new JLabel(title);
		titleLabel.putClientProperty(FlatClientProperties.STYLE, "font: bold +3");
		add(circleLabel, "cell 0 0,alignx left,gapright 10,aligny center,gapy 5");
		add(titleLabel, "cell 1 0, aligny center");

		componentPanel = new JPanel(new MigLayout("", "[grow]", "[grow]"));
		componentPanel.setOpaque(false);
		componentPanel.add(content, "grow");
		add(componentPanel, "cell 0 1 2 1, grow");
	}
}
