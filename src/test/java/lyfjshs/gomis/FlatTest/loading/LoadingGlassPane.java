package lyfjshs.gomis.FlatTest.loading;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class LoadingGlassPane extends JPanel {
    private final RotatingIconPanel rotatingPanel;
    private final JLabel logoLabel;
    private float alpha = 0.5f; // Opacity control for fade-out

    public LoadingGlassPane(String svgPath, int iconSize, int rotationSpeed, String logoPath) {
        setLayout(new BorderLayout());
        setOpaque(false); // Don't paint the default background

        // Initialize rotating icon panel
        rotatingPanel = new RotatingIconPanel(svgPath, iconSize, rotationSpeed);
        rotatingPanel.setOpaque(false);

        // Initialize logo label (hidden initially)
        logoLabel = new JLabel(new FlatSVGIcon(logoPath, 0.4f));
        logoLabel.setVisible(false);
        logoLabel.setHorizontalAlignment(JLabel.CENTER);

        // Add rotating panel initially
        add(rotatingPanel, BorderLayout.CENTER);
    }

    public void startAnimation() {
        rotatingPanel.startAnimation();
    }

    public void stopAnimation() {
        rotatingPanel.stopAnimation();
        remove(rotatingPanel);
        add(logoLabel, BorderLayout.CENTER);
        logoLabel.setVisible(true);
        revalidate();
        repaint();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        // Apply alpha composite for the entire pane (background + components)
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        // Paint semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 0.5f)); // 50% opaque black
        g2d.fillRect(0, 0, getWidth(), getHeight());
        // Paint components (rotating icon or logo) with the same alpha
        super.paintComponent(g2d);
        g2d.dispose();
    }
}