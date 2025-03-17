package lyfjshs.gomis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.TimingSource;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

import com.formdev.flatlaf.FlatLightLaf;

import net.miginfocom.swing.MigLayout;

public class LoadingAnimationTest extends JFrame {

    private float rotation = 0f;
    private float progress = 0f;
    private boolean isComplete = false;
    private final GomisPanel gomisPanel;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private Animator progressAnimator;
    private Animator rotationAnimator;
    private Animator transformAnimator;
    private TimingSource timingSource;

    public LoadingAnimationTest() {
        // Set up the FlatLaf look and feel
        FlatLightLaf.setup();

        // Configure the JFrame
        setTitle("G Loader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new MigLayout("fill, insets 20", "[center]", "[center]"));

        // Initialize UI components
        gomisPanel = new GomisPanel();
        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(new Color(0x5E2B7E));
        progressBar.setBackground(new Color(0xE2E8F0));
        statusLabel = new JLabel("0%");
        statusLabel.setForeground(new Color(0x4A5568));

        // Add components to the layout
        add(gomisPanel, "wrap, align center");
        add(progressBar, "wrap, width 256!, align center");
        add(statusLabel, "align center");

        // Set up animations
        setupAnimations();

        // Finalize JFrame setup
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        revalidate();
        repaint();
    }

    private void setupAnimations() {
        // Initialize and start the timing source
        timingSource = new SwingTimerTimingSource();
        timingSource.init();

        try {
            // Progress animation (5 seconds)
            progressAnimator = new Animator.Builder(timingSource)
                .setDuration(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTarget(new TimingTargetAdapter() {
                    public void timingEvent(double fraction) {
                        float easedFraction = (float) (1 - Math.pow(1 - fraction, 3)); // Ease-out effect
                        progress = easedFraction * 100;
                        progressBar.setValue((int) progress);
                        statusLabel.setText(Math.round(progress) + "%");
                        gomisPanel.repaint();
                        if (progress >= 100 && !isComplete) {
                            isComplete = true;
                            progressBar.setVisible(false);
                            statusLabel.setText("G");
                            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 20f));
                            statusLabel.setForeground(new Color(0x5E2B7E));
                            rotationAnimator.stop();
                            transformAnimator.start();
                        }
                    }
                })
                .build();
            progressAnimator.start();

            // Rotation animation (continuous)
            rotationAnimator = new Animator.Builder(timingSource)
                .setDuration(2000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .setRepeatCount(Animator.INFINITE)
                .addTarget(new TimingTargetAdapter() {
                    
                    public void timingEvent(double fraction) {
                        if (!isComplete) {
                            rotation = (float) fraction * 360;
                            gomisPanel.repaint();
                        }
                    }
                })
                .build();
            rotationAnimator.start();

            // Transform animation (circle to G)
            transformAnimator = new Animator.Builder(timingSource)
                .setDuration(800, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTarget(new TimingTargetAdapter() {
                    public void begin() {
                        gomisPanel.setTransformProgress(0f);
                    }

                    public void timingEvent(double fraction) {
                        // Use easing for smoother animation
                        float easedFraction = (float) (1 - Math.pow(1 - fraction, 2));
                        gomisPanel.setTransformProgress(easedFraction);
                        gomisPanel.repaint();
                    }

                    public void end() {
                        gomisPanel.setTransformProgress(1f);
                        gomisPanel.repaint();
                    }
                })
                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class GomisPanel extends JPanel {
        private float transformProgress = 0f;

        GomisPanel() {
            setPreferredSize(new Dimension(256, 256));
            setOpaque(false);
        }

        public void setTransformProgress(float progress) {
            this.transformProgress = progress;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Center the graphics context
            g2d.translate(width / 2, height / 2);

            if (isComplete) {
                // Draw the transform from circle to G
                drawTransformedG(g2d, width, height, transformProgress);
            } else {
                // Draw the background circle
                g2d.setColor(new Color(0x5E2B7E));
                g2d.fill(new Ellipse2D.Float(-width/2, -height/2, width, height));
                
                // Draw the rotating loading animation
                g2d.rotate(Math.toRadians(rotation));
                drawLoadingAnimation(g2d, width, height);
            }

            g2d.dispose();
        }

        private void drawLoadingAnimation(Graphics2D g2d, int width, int height) {
            int size = Math.min(width, height) - 40;
            
            // Draw the outer progress arc
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(12, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            float arcSize = size * 0.9f;
            float arcExtent = (progress / 100.0f) * 360.0f;
            
            g2d.draw(new Arc2D.Float(-arcSize/2, -arcSize/2, arcSize, arcSize, 90, -arcExtent, Arc2D.OPEN));
        }

        private void drawTransformedG(Graphics2D g2d, int width, int height, float progress) {
            int size = Math.min(width, height) - 40;
            float radius = size / 2.0f;
            
            // Base color
            g2d.setColor(new Color(0x5E2B7E));
            
            if (progress < 0.5f) {
                // First half of animation: Circle morphs to ellipse
                float scaleX = 1.0f + (0.3f * (progress * 2));
                float scaleY = 1.0f;
                
                g2d.fill(new Ellipse2D.Float(-radius * scaleX, -radius * scaleY, 
                                            2 * radius * scaleX, 2 * radius * scaleY));
            } else {
                // Second half: Ellipse to G
                float normalizedProgress = (progress - 0.5f) * 2; // 0 to 1 for this phase
                
                // Draw the outer circle/ellipse of G
                float scaleX = 1.3f;
                float scaleY = 1.0f;
                g2d.setStroke(new BasicStroke(radius * 0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Calculate the arc angles for the G shape
                float startAngle = -90;
                float arcAngle = 270 + (normalizedProgress * 90); // Goes from 270 to 360
                
                // Draw the main arc of the G
                g2d.draw(new Arc2D.Float(-radius * scaleX, -radius * scaleY, 
                                        2 * radius * scaleX, 2 * radius * scaleY, 
                                        startAngle, arcAngle, Arc2D.OPEN));
                
                // Draw the horizontal line of the G
                if (normalizedProgress > 0.3) {
                    float lineProgress = Math.min(1.0f, (normalizedProgress - 0.3f) / 0.7f);
                    float lineLength = radius * 0.8f * lineProgress;
                    
                    g2d.setStroke(new BasicStroke(radius * 0.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2d.drawLine(0, 0, (int)(lineLength), 0);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoadingAnimationTest::new);
    }
}