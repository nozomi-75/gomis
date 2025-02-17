package lyfjshs.gomis.components;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class LogoPanel extends JPanel {

	public LogoPanel(String imagePath, int targetWidth, int targetHeight) throws IOException {
		try {
// Set layout for the panel
			this.setLayout(new MigLayout("insets 0", "[center]", "[center][][]"));
			this.setOpaque(false);

// Load and scale the image
			BufferedImage originalImage = ImageIO.read(getClass().getResource(imagePath));
			Image scaledImage = getScaledImage(originalImage, targetWidth, targetHeight);

// Add the scaled image to the panel
			JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
			this.add(logoLabel, "cell 0 0, grow");

// Add title
			JLabel titleLabel = new JLabel("GOMIS");
			titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 25));
			this.add(titleLabel, "cell 0 1, alignx center");

// Add subtitle
			JLabel subLabel = new JLabel("Guidance Office Management Information System");
			subLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
			this.add(subLabel, "cell 0 2, alignx center, aligny center");

// Revalidate and repaint
			this.revalidate();
			this.repaint();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new IOException("Error loading image: " + ex.getMessage());
		}
	}

	/**
	 * Scales the given image to the specified width and height, preserving quality.
	 *
	 * **@param** originalImage The original BufferedImage. **@param** targetWidth
	 * The desired width. **@param** targetHeight The desired height. **@return** A
	 * scaled Image with smooth rendering.
	 */
	private Image getScaledImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
		BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = scaledImage.createGraphics();

// Enable high-quality rendering hints
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

// Draw the scaled image
		g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		g2d.dispose();

		return scaledImage;
	}
}
