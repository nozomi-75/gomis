package lyfjshs.gomis.view.loading;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.components.ModelColor;
import lyfjshs.gomis.components.PanelGradient;

public class SplashScreenFrame extends JFrame {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;
    private static final Color BACKGROUND_COLOR = new Color(26, 26, 46);
    private static final Color ACCENT_COLOR_1 = new Color(79, 172, 254);
    private static final Color ACCENT_COLOR_2 = new Color(0, 242, 254);
    
    private PanelGradient mainPanel;
    private LogoPanel logoPanel;
    private ProgressPanel progressPanel;
    private JLabel loadingLabel;
    private BackgroundCirclesPanel circlesPanel;
    private Timer dotAnimationTimer;
    private int dotState = 0;
    
    public SplashScreenFrame() {
        setTitle("Loading...");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        
        initComponents();
        startAnimations();
    }
    
    private void initComponents() {
        mainPanel = new PanelGradient();
        mainPanel.addColor(new ModelColor(new Color(0x004aad), 0f), new ModelColor(new Color(0xcb6ce6), 1f));

        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Logo panel
        logoPanel = new LogoPanel(ACCENT_COLOR_1, ACCENT_COLOR_2, BACKGROUND_COLOR);
        logoPanel.setPreferredSize(new Dimension(120, 120));
        
        // Progress panel
        progressPanel = new ProgressPanel(ACCENT_COLOR_1, ACCENT_COLOR_2);
        progressPanel.setPreferredSize(new Dimension(300, 6));
        
        // Loading text
        loadingLabel = new JLabel("LOADING");
        loadingLabel.setForeground(Color.WHITE);
        loadingLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Center panel for logo and loading components
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.add(Box.createVerticalGlue());
        
        JPanel logoContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoContainer.setOpaque(false);
        logoContainer.add(logoPanel);
        centerPanel.add(logoContainer);
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        JPanel progressContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressContainer.setOpaque(false);
        progressContainer.add(progressPanel);
        centerPanel.add(progressContainer);
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel textContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        textContainer.setOpaque(false);
        textContainer.add(loadingLabel);
        centerPanel.add(textContainer);
        
        centerPanel.add(Box.createVerticalGlue());
        
        // Background circles
        circlesPanel = new BackgroundCirclesPanel(ACCENT_COLOR_1, ACCENT_COLOR_2);
        
        // Add components to main panel
        mainPanel.setLayout(new OverlayLayout(mainPanel));
        mainPanel.add(centerPanel);
        mainPanel.add(circlesPanel);
        
        setContentPane(mainPanel);
    }
    
    private void startAnimations() {
        // Animate dots
        dotAnimationTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder dots = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    dots.append((i <= dotState) ? "." : " ");
                }
                loadingLabel.setText("LOADING" + dots.toString());
                dotState = (dotState + 1) % 4;
            }
        });
        dotAnimationTimer.start();
    }
    
    public void runInitialization() {
        // Create a SwingWorker to run the initialization in the background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Main.initDB(); // Call the method to initialize the database connection                
                Main.initFrame(); // Call the method to initialize the main frame
                return null;
            }

            @Override
            protected void done() {
                dispose(); // Close the splash screen
                Main.jFrame.setVisible(true);
               
            }
        };
        worker.execute(); // Start the SwingWorker
    }
}
