package lyfjshs.gomis.FlatTest;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.formdev.flatlaf.FlatLightLaf;

public class FlatAnimation extends JWindow {
	private final AnimatedLoaderPanel loaderPanel;
	private Timer animationTimer;
	private double rotationAngle = 0;
	private double morphProgress = 0; // 0 = full loader, 1 = fully morphed to "G"
	private boolean morphing = false;

	public FlatAnimation() {
		// Set FlatLaf theme
		FlatLightLaf.setup();

		// Create animation panel
		loaderPanel = new AnimatedLoaderPanel();
		getContentPane().add(loaderPanel);

		setSize(200, 200);
		setLocationRelativeTo(null);

		// Timer to handle animation (60 FPS)
		animationTimer = new Timer(16, e -> {
			if (!morphing) {
				// Rotate loader for 27 seconds
				rotationAngle += 2; // Rotate smoothly (adjust speed)
				if (rotationAngle >= 100) { // 360 * 27 = 9720 degrees
					morphing = true; // Start morphing after 27 seconds
				}
			} else {
				// Morph animation lasts for 3 seconds
				morphProgress += 0.016 / 3; // 16ms per frame, lasts 3 seconds
				if (morphProgress >= 1) {
					morphProgress = 1;
					animationTimer.stop();
					dispose(); // Close splash screen after morphing
				}
			}
			loaderPanel.repaint();
		});
		animationTimer.start();
	}

	// Custom panel to draw the animated SVG manually
	class AnimatedLoaderPanel extends JPanel {
		public AnimatedLoaderPanel() {
			setPreferredSize(new Dimension(200, 200));
			setBackground(Color.WHITE);
		}

		@Override
		protected void paintComponent(Graphics g) {
		    super.paintComponent(g);
		    Graphics2D g2d = (Graphics2D) g.create();
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		    int centerX = getWidth() / 2;
		    int centerY = getHeight() / 2;

		    if (!morphing) {
		        // Apply rotation only during the loading phase
		        AffineTransform transform = new AffineTransform();
		        transform.rotate(Math.toRadians(rotationAngle), centerX, centerY);
		        g2d.setTransform(transform);
		        drawCircularLoader(g2d, centerX, centerY);
		    } else {
		        // Reset transformation to prevent slanted final "G"
		        g2d.setTransform(new AffineTransform());
		        drawMorphingG(g2d, centerX, centerY, morphProgress);
		    }

		    g2d.dispose();
		}

		private void drawCircularLoader(Graphics2D g2d, int x, int y) {
			g2d.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			g2d.setColor(new Color(97, 53, 131)); // Equivalent to #613583
			g2d.drawArc(x - 40, y - 40, 80, 80, 0, 270); // Circular arc (Loader)
		}

		private void drawMorphingG(Graphics2D g2d, int x, int y, double progress) {

			g2d.setColor(new Color(97, 53, 131)); // Equivalent to #613583
			// Morph from arc to G
			Path2D path = new Path2D.Double();

			// Starting point
			path.moveTo(0, 20.0001);

			// First segment
			path.lineTo(0, 40.0001);
			path.lineTo(0.000244141, 130);

			// Create the complex curved paths
			// Bottom right curved corner
			path.curveTo(0.000244141, 130, 0.000244141, 150.094, 0.000244141, 150.094);
			path.curveTo(0.000244141, 155.591, 1.9436, 160.283, 5.83057, 164.17);
			path.curveTo(9.71753, 168.057, 14.4094, 170, 19.9065, 170);

			// Bottom edge
			path.curveTo(19.9065, 170, 180.094, 170, 180.094, 170);

			// Bottom right corner
			path.curveTo(185.591, 170, 190.283, 168.057, 194.17, 164.17);
			path.curveTo(198.057, 160.283, 200, 155.591, 200, 150.094);

			// Right edge
			path.curveTo(200, 150.094, 200, 70.0001, 200, 70.0001);
			path.curveTo(200, 58.9528, 191.047, 50, 180, 50);

			// Top cutout section
			path.curveTo(180, 50, 101, 50.0001, 101, 50.0001);
			path.curveTo(92.7148, 50.0001, 86.0002, 56.7147, 86.0002, 65.0001);
			path.curveTo(86.0002, 65.0001, 86.0002, 75.0001, 86.0002, 75.0001);
			path.curveTo(86.0002, 83.2855, 92.7148, 90.0001, 101, 90.0001);
			path.lineTo(150.102, 90.0001);

			// Right middle cutout section - simplified for readability
			path.quadTo(155, 90, 160, 99.8985);
			path.lineTo(160, 120.109);
			path.quadTo(160, 125, 150.109, 130.007);

			// Bottom edge
			path.lineTo(49.9055, 130.077);

			// Left side - simplified for readability
			path.quadTo(45, 130, 40.0002, 120.179);
			path.lineTo(40.0002, 49.9747);
			path.quadTo(40, 45, 49.8906, 40.0763);

			// Continue with the complex path
			path.curveTo(76.0522, 40.0553, 145, 40.0001, 145, 40.0001);
			path.curveTo(153.286, 40.0001, 160, 33.2855, 160, 25.0001);
			path.curveTo(160, 25.0001, 160, 15.0001, 160, 15.0001);
			path.curveTo(160, 6.71466, 153.286, 0, 145, 0);

			// Top edge
			path.lineTo(40.0002, 0.0000610352);
			path.lineTo(20.0002, 0.0000610352);

			// Top left corner
			path.curveTo(20.0002, 0.0000610352, 19.9065, 0, 19.9065, 0);
			path.curveTo(14.4106, 0, 9.71924, 1.94286, 5.83252, 5.82858);
			path.curveTo(1.9458, 9.71432, 0.00170898, 14.4052, 0.000244141, 19.9011);

			// Close the path
			path.lineTo(0, 20.0001);


			g2d.fill(path); // Morphing effect
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FlatAnimation splash = new FlatAnimation();
			splash.setVisible(true);
		});
	}


}

class SVGPathDrawing extends JPanel {
	private Color shapeColor = new Color(97, 53, 131); // #613583 in RGB

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Create the custom path
		Path2D path = createCustomPath();

		// Fill the path with the color
		g2d.setColor(shapeColor);
		g2d.fill(path);

		g2d.dispose();
	}

	private Path2D createCustomPath() {
		Path2D path = new Path2D.Double();

		// Starting point
		path.moveTo(0, 20.0001);

		// First segment
		path.lineTo(0, 40.0001);
		path.lineTo(0.000244141, 130);

		// Create the complex curved paths
		// Bottom right curved corner
		path.curveTo(0.000244141, 130, 0.000244141, 150.094, 0.000244141, 150.094);
		path.curveTo(0.000244141, 155.591, 1.9436, 160.283, 5.83057, 164.17);
		path.curveTo(9.71753, 168.057, 14.4094, 170, 19.9065, 170);

		// Bottom edge
		path.curveTo(19.9065, 170, 180.094, 170, 180.094, 170);

		// Bottom right corner
		path.curveTo(185.591, 170, 190.283, 168.057, 194.17, 164.17);
		path.curveTo(198.057, 160.283, 200, 155.591, 200, 150.094);

		// Right edge
		path.curveTo(200, 150.094, 200, 70.0001, 200, 70.0001);
		path.curveTo(200, 58.9528, 191.047, 50, 180, 50);

		// Top cutout section
		path.curveTo(180, 50, 101, 50.0001, 101, 50.0001);
		path.curveTo(92.7148, 50.0001, 86.0002, 56.7147, 86.0002, 65.0001);
		path.curveTo(86.0002, 65.0001, 86.0002, 75.0001, 86.0002, 75.0001);
		path.curveTo(86.0002, 83.2855, 92.7148, 90.0001, 101, 90.0001);
		path.lineTo(150.102, 90.0001);

		// Right middle cutout section - simplified for readability
		path.quadTo(155, 90, 160, 99.8985);
		path.lineTo(160, 120.109);
		path.quadTo(160, 125, 150.109, 130.007);

		// Bottom edge
		path.lineTo(49.9055, 130.077);

		// Left side - simplified for readability
		path.quadTo(45, 130, 40.0002, 120.179);
		path.lineTo(40.0002, 49.9747);
		path.quadTo(40, 45, 49.8906, 40.0763);

		// Continue with the complex path
		path.curveTo(76.0522, 40.0553, 145, 40.0001, 145, 40.0001);
		path.curveTo(153.286, 40.0001, 160, 33.2855, 160, 25.0001);
		path.curveTo(160, 25.0001, 160, 15.0001, 160, 15.0001);
		path.curveTo(160, 6.71466, 153.286, 0, 145, 0);

		// Top edge
		path.lineTo(40.0002, 0.0000610352);
		path.lineTo(20.0002, 0.0000610352);

		// Top left corner
		path.curveTo(20.0002, 0.0000610352, 19.9065, 0, 19.9065, 0);
		path.curveTo(14.4106, 0, 9.71924, 1.94286, 5.83252, 5.82858);
		path.curveTo(1.9458, 9.71432, 0.00170898, 14.4052, 0.000244141, 19.9011);

		// Close the path
		path.lineTo(0, 20.0001);
		path.closePath();

		return path;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200, 170);
	}

}