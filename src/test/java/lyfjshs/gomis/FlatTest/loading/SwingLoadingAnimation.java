package lyfjshs.gomis.FlatTest.loading;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;


public class SwingLoadingAnimation extends JPanel {
    private float orbitProgress = 0f; // Progress for orbit animation (0 to 1)
    private float landingProgress = 0f; // Progress for landing (0 to 1)
    private float morphProgress = 0f; // Progress for morphing effect (0 to 1)
    private float fadeProgress = 0f; // Progress for "G" shape fade-in

    private final Animator animator;

    public SwingLoadingAnimation() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);

        animator = new Animator.Builder()
                .setDuration(4000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setRepeatCount(Animator.INFINITE)
                .setStartDelay(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTarget(new TimingTargetAdapter() {
                    public void timingEvent(float fraction) {
                        if (fraction < 0.5) {
                            orbitProgress = fraction * 2;  // First 50% is orbiting
                        } else if (fraction < 0.75) {
                            landingProgress = (fraction - 0.5f) * 4; // Next 25% is landing
                        } else {
                            morphProgress = (fraction - 0.75f) * 4; // Last 25% is morphing
                            fadeProgress = (fraction - 0.75f) * 4;  // "G" shape fade-in
                        }
                        repaint();
                    }
                })
                .build();

        animator.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        int orbitRadius = 150;

        // Antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Colors for dots
        Color[] dotColors = {new Color(67, 137, 241), new Color(229, 53, 79), new Color(38, 223, 72)};
        int[][] initialPositions = {
            {15, -300}, {-65, -300}, {-275, -300}
        };

        // Orbiting dots
        for (int i = 0; i < 3; i++) {
            AffineTransform transform = new AffineTransform();
            transform.translate(cx, cy);
            transform.rotate(Math.toRadians(orbitProgress * 360 + i * 120));
            transform.translate(orbitRadius, 0);
            
            // Landing transition
            int finalX = initialPositions[i][0];
            int finalY = initialPositions[i][1];
            double landingX = transform.getTranslateX() + landingProgress * (finalX - transform.getTranslateX());
            double landingY = transform.getTranslateY() + landingProgress * (finalY - transform.getTranslateY());

            // Morphing transition
            int size = (int) (60 + morphProgress * 5);
            int arc = (int) (morphProgress * 10);

            // Draw dot (morphing into a square)
            g2d.setColor(dotColors[i]);
            g2d.fill(new RoundRectangle2D.Double(landingX, landingY, size, size, arc, arc));
        }

        // Fade-in "G" Shape
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeProgress));
        drawGShape(g2d, cx - 100, cy + 50);
        
        g2d.dispose();
    }

    private void drawGShape(Graphics2D g2d, int x, int y) {
        g2d.setColor(new Color(38, 223, 72)); // Green color
        g2d.setStroke(new BasicStroke(10));
        g2d.drawArc(x, y, 100, 100, 0, 270);
        g2d.drawLine(x + 50, y + 150, x + 100, y + 100);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java Swing Loading Animation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 600);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.add(new SwingLoadingAnimation());
            frame.setVisible(true);
        });
    }
}
