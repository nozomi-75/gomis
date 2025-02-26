package lyfjshs.gomis.view.loading;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;

import javax.swing.JPanel;
import javax.swing.Timer;

public class LogoPanel extends JPanel {
	private Timer rotationTimer;
	private Timer pulseTimer;
	private double rotation = 0;
	private double scale = 1.0;
	private boolean growing = true;
	private double outerDashOffset = 0;
	private double innerDashOffset = 0;
	private Color ACCENT_COLOR_1, ACCENT_COLOR_2, BACKGROUND_COLOR;

	public LogoPanel(Color ac_1, Color ac_2, Color bg) {
		this.ACCENT_COLOR_1 = ac_1;
		this.ACCENT_COLOR_2 = ac_2;
		this.BACKGROUND_COLOR = bg;
		setOpaque(false);

		rotationTimer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rotation += 2;
				if (rotation >= 360) {
					rotation = 0;
				}

				outerDashOffset += 2;
				if (outerDashOffset >= 314) {
					outerDashOffset = 0;
				}

				innerDashOffset += 2;
				if (innerDashOffset >= 251) {
					innerDashOffset = 0;
				}

				repaint();
			}
		});

		pulseTimer = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (growing) {
					scale += 0.005;
					if (scale >= 1.05) {
						growing = false;
					}
				} else {
					scale -= 0.005;
					if (scale <= 0.95) {
						growing = true;
					}
				}
				repaint();
			}
		});

		rotationTimer.start();
		pulseTimer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		int centerX = w / 2;
		int centerY = h / 2;

		// Apply scaling
		g2d.translate(centerX, centerY);
		g2d.scale(scale, scale);
		g2d.translate(-centerX, -centerY);

		// Outer circle
		float[] dashPattern1 = { 5, 5 };
		BasicStroke stroke1 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern1,
				(float) outerDashOffset);
		g2d.setStroke(stroke1);
		g2d.setColor(ACCENT_COLOR_1);
		g2d.drawOval(centerX - 50, centerY - 50, 100, 100);

		// Inner circle
		float[] dashPattern2 = { 4, 4 };
		BasicStroke stroke2 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern2,
				(float) innerDashOffset);
		g2d.setStroke(stroke2);
		g2d.setColor(ACCENT_COLOR_2);
		g2d.drawOval(centerX - 40, centerY - 40, 80, 80);

		// Center shape
		g2d.rotate(Math.toRadians(rotation), centerX, centerY);

		Path2D diamond = new Path2D.Double();
		diamond.moveTo(centerX, centerY - 30);
		diamond.lineTo(centerX + 25, centerY + 10);
		diamond.lineTo(centerX, centerY + 30);
		diamond.lineTo(centerX - 25, centerY + 10);
		diamond.closePath();

		g2d.setColor(BACKGROUND_COLOR);
		g2d.fill(diamond);
		g2d.setColor(Color.WHITE);
		g2d.setStroke(new BasicStroke(2));
		g2d.draw(diamond);

		g2d.dispose();
	}
}
