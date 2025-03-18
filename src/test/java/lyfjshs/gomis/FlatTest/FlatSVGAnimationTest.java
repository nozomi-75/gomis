package lyfjshs.gomis.FlatTest;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import lyfjshs.gomis.FlatTest.loading.LoadingGlassPane;

public class FlatSVGAnimationTest extends JFrame {
    private LoadingGlassPane loadingGlassPane;

    public FlatSVGAnimationTest() {
        setTitle("Main Application");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame

        // Set up an initial empty content pane
        setContentPane(new JPanel());

        // Create and set the glass pane
        loadingGlassPane = new LoadingGlassPane("loading.svg", 100, 10, "circleG.svg");
        setGlassPane(loadingGlassPane);
        loadingGlassPane.setVisible(true);
        loadingGlassPane.startAnimation();

        // Start background initialization
        initializeApplication();
    }

    private void initializeApplication() {
        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() {
                // Simulate background work (e.g., loading data or building UI)
                try {
                    Thread.sleep(4500); // 4.5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Build the main UI
                JPanel mainPanel = new JPanel(new BorderLayout());
                JLabel welcomeLabel = new JLabel("Welcome to the Main Window!");
                welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
                mainPanel.add(welcomeLabel, BorderLayout.CENTER);
                return mainPanel;
            }

            @Override
            protected void done() {
                try {
                    // Set the main UI as the content pane
                    JPanel mainPanel = get();
                    setContentPane(mainPanel);
                    revalidate();
                    repaint();

                    // Switch to logo and schedule fade-out
                    loadingGlassPane.stopAnimation();
                    Timer logoTimer = new Timer(1000, e -> startFadeOut());
                    logoTimer.setRepeats(false);
                    logoTimer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void startFadeOut() {
        final float[] alpha = {0.8f};
        Timer fadeTimer = new Timer(100, e -> {
            alpha[0] -= 0.1f;
            if (alpha[0] <= 0) {
                ((Timer)e.getSource()).stop();
                loadingGlassPane.setAlpha(0.0f);
                loadingGlassPane.setVisible(false);
            } else {
                loadingGlassPane.setAlpha(alpha[0]);
            }
        });
        fadeTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatSVGAnimationTest frame = new FlatSVGAnimationTest();
            frame.setVisible(true);
        });
    }
}