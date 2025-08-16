package lyfjshs.gomis.components;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class LogoPanel extends JPanel {

    public LogoPanel(String imagePath, int targetWidth, int targetHeight) throws IOException {
        if (imagePath == null || targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Invalid parameters: imagePath must not be null and dimensions must be positive");
        }

        try {
            // Set layout for the panel
            this.setLayout(new MigLayout("insets 0", "[center]", "[center][][]"));
            this.setOpaque(false);

            // Load and scale the image
            BufferedImage originalImage = loadImage(imagePath);
            Image scaledImage = getScaledImage(originalImage, targetWidth, targetHeight);
            
            // Create and add the logo label
            JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
            this.add(logoLabel, "cell 0 0, alignx center, aligny center");

            // Add title
            JLabel titleLabel = new JLabel("GOMIS");
            titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 25));
            this.add(titleLabel, "cell 0 1, alignx center");

            // Add subtitle
            JLabel subLabel = new JLabel("Guidance Office Management Information System");
            subLabel.setFont(new Font("Times New Roman", Font.PLAIN, 15));
            this.add(subLabel, "cell 0 2, alignx center, aligny center");

            // Cleanup
            originalImage.flush();
            this.revalidate();
            this.repaint();

        } catch (IOException ex) {
            throw new IOException("Failed to initialize LogoPanel: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error in LogoPanel initialization: " + ex.getMessage(), ex);
        }
    }

    /**
     * Loads an image from the given path, which can be a classpath resource or a
     * file path.
     *
     * @param imagePath The path to the image.
     * @return The loaded BufferedImage.
     * @throws IOException If the image cannot be loaded.
     */
    private BufferedImage loadImage(String imagePath) throws IOException {
        URL imageUrl = getClass().getResource(imagePath);
        if (imageUrl == null) {
            throw new IOException("Image not found: " + imagePath);
        }
        return ImageIO.read(imageUrl);
    }

    /**
     * Scales the given image to the specified width and height, preserving quality
     * and aspect ratio.
     *
     * @param originalImage The original BufferedImage.
     * @param targetWidth   The desired width.
     * @param targetHeight  The desired height.
     * @return A scaled Image with smooth rendering.
     */
    private Image getScaledImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int scaledWidth = targetWidth;
        int scaledHeight = targetHeight;

        // Maintain aspect ratio
        if (originalWidth != originalHeight) {
            double aspectRatio = (double) originalWidth / originalHeight;
            if (targetWidth / aspectRatio <= targetHeight) {
                scaledWidth = (int) (targetHeight * aspectRatio);
                scaledHeight = targetHeight;
            } else {
                scaledWidth = targetWidth;
                scaledHeight = (int) (targetWidth / aspectRatio);
            }
        }

        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();

        // Enable high-quality rendering hints
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the scaled image
        g2d.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        return scaledImage;
    }

}
