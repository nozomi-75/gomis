package lyfjshs.gomis.view.loading;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;

import com.formdev.flatlaf.themes.FlatMacLightLaf;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.components.ModelColor;
import lyfjshs.gomis.components.PanelGradient;

public class SplashScreenFrame extends JFrame {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 400;
    private static final Color ACCENT_COLOR_1 = new Color(79, 172, 254);
    private static final Color ACCENT_COLOR_2 = new Color(0, 242, 254);
    
    private PanelGradient mainPanel;
    private JPanel logoPanel;
    private ProgressPanel progressPanel;
    private JLabel loadingLabel;
    private BackgroundCirclesPanel circlesPanel;
    private Timer dotAnimationTimer;
    private int dotState = 0;
    
    public SplashScreenFrame() {
    	FlatMacLightLaf.setup();
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
        
        // Load logo with fallback
        JLabel logoLabel;
        try {
            java.net.URL logoUrl = getClass().getResource("/images/GOMIS Logo.png");
            if (logoUrl == null) {
                logoUrl = getClass().getResource("/GOMIS Logo.png");
            }
            if (logoUrl == null) {
                throw new Exception("Could not find logo image in resources");
            }
            
            ImageIcon originalIcon = new ImageIcon(logoUrl);
            if (originalIcon.getIconWidth() <= 0) {
                throw new Exception("Failed to load logo image");
            }
            Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // Create a text-based fallback logo
            logoLabel = new JLabel("GOMIS");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 36));
            logoLabel.setForeground(Color.WHITE);
            logoLabel.setPreferredSize(new Dimension(150, 150));
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            System.err.println("Warning: Could not load logo image: " + e.getMessage());
        }

        // Logo panel
        logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.add(logoLabel);
        
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
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    publish("Initializing database...");
                    Main.initDB(); // Initialize the database connection
                    
                    publish("Setting up main window...");
                    Main.initFrame(); // Initialize the main frame
                    
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception("Initialization failed: " + e.getMessage(), e);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                // Update loading text with the latest status
                if (!chunks.isEmpty()) {
                    loadingLabel.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground
                    
                    // Stop the dot animation timer
                    if (dotAnimationTimer != null) {
                        dotAnimationTimer.stop();
                    }
                    
                    dispose(); // Close the splash screen
                    
                    if (Main.gFrame == null) {
                        throw new Exception("Main window initialization failed.");
                    }
                    
                    Main.gFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "Failed to start application: " + 
                        (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                    
                    JOptionPane.showMessageDialog(
                        SplashScreenFrame.this,
                        errorMessage,
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    System.exit(1);
                }
            }
        };
        worker.execute();
    }
}
