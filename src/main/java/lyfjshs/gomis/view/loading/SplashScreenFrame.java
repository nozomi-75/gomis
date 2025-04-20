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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

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
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private BackgroundCirclesPanel circlesPanel;
    private Timer dotAnimationTimer;
    private int dotState = 0;
    
    // Define initialization steps with their weights
    private static final String[][] INIT_STEPS = {
        {"Checking database service", "10"},
        {"Starting database service if needed", "10"},
        {"Connecting to database", "20"},
        {"Initializing database schema", "15"},
        {"Setting up UI components", "15"},
        {"Loading application settings", "10"},
        {"Preparing main window", "10"},
        {"Finalizing initialization", "10"}
    };
    
    private int currentStep = 0;
    private int totalProgress = 0;
    private SwingWorker<Void, String> worker;
    
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
            java.net.URL logoUrl = getClass().getResource("/GOMIS Logo.png");
            if (logoUrl == null) {
                System.err.println("Could not find GOMIS Logo.png");
                return;
            }
            ImageIcon originalIcon = new ImageIcon(logoUrl);
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
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setForeground(ACCENT_COLOR_1);
        progressBar.setBackground(new Color(240, 240, 240));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(300, 10));
        
        // Status text
        statusLabel = new JLabel("INITIALIZING");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
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
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Progress bar container
        JPanel progressContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressContainer.setOpaque(false);
        progressContainer.add(progressBar);
        centerPanel.add(progressContainer);
        
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Status text container
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusContainer.setOpaque(false);
        statusContainer.add(statusLabel);
        centerPanel.add(statusContainer);
        
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
                statusLabel.setText(statusLabel.getText().replaceAll("\\.+$", "") + dots.toString());
                dotState = (dotState + 1) % 4;
            }
        });
        dotAnimationTimer.start();
    }
    
    public void runInitialization() {
        worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Step 1: Check database service
                    updateProgress(INIT_STEPS[0][0], Integer.parseInt(INIT_STEPS[0][1]));
                    Thread.sleep(500); // Simulate work
                    
                    // Step 2: Start database service if needed
                    updateProgress(INIT_STEPS[1][0], Integer.parseInt(INIT_STEPS[1][1]));
                    Thread.sleep(500); // Simulate work
                    
                    // Step 3: Initialize database
                    updateProgress(INIT_STEPS[2][0], Integer.parseInt(INIT_STEPS[2][1]));
                    Main.initDB();
                    
                    // Step 4: Initialize database schema
                    updateProgress(INIT_STEPS[3][0], Integer.parseInt(INIT_STEPS[3][1]));
                    Thread.sleep(500); // Simulate work
                    
                    // Step 5: Set up UI components
                    updateProgress(INIT_STEPS[4][0], Integer.parseInt(INIT_STEPS[4][1]));
                    Thread.sleep(500); // Simulate work
                    
                    // Step 6: Load application settings
                    updateProgress(INIT_STEPS[5][0], Integer.parseInt(INIT_STEPS[5][1]));
                    Thread.sleep(500); // Simulate work
                    
                    // Step 7: Prepare main window
                    updateProgress(INIT_STEPS[6][0], Integer.parseInt(INIT_STEPS[6][1]));
                    Main.initFrame();
                    
                    // Step 8: Finalize initialization
                    updateProgress(INIT_STEPS[7][0], Integer.parseInt(INIT_STEPS[7][1]));
                    Thread.sleep(500); // Simulate work
                    
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    
                    // Check if the error is related to database connection
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (
                        errorMessage.contains("password") || 
                        errorMessage.contains("authentication") || 
                        errorMessage.contains("Access denied") ||
                        errorMessage.contains("GSS-API"))) {
                        
                        // Show a more specific error message for password issues
                        errorMessage = "Database authentication failed. Please check your password.";
                    } else {
                        errorMessage = "Initialization failed: " + e.getMessage();
                    }
                    
                    throw new Exception(errorMessage, e);
                }
            }

            @Override
            protected void process(List<String> chunks) {
                // Update status text with the latest status
                if (!chunks.isEmpty()) {
                    String status = chunks.get(chunks.size() - 1);
                    statusLabel.setText(status);
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
                    
                    // Set progress to 100%
                    progressBar.setValue(100);
                    statusLabel.setText("INITIALIZATION COMPLETE");
                    
                    // Wait a moment before closing splash screen
                    Timer closeTimer = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            dispose(); // Close the splash screen
                            
                            if (Main.gFrame == null) {
                                throw new RuntimeException("Main window initialization failed.");
                            }
                            
                            Main.gFrame.setVisible(true);
                        }
                    });
                    closeTimer.setRepeats(false);
                    closeTimer.start();
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "Failed to start application: " + 
                        (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                    
                    // Check if the error is related to database connection
                    if (errorMessage.contains("password") || 
                        errorMessage.contains("authentication") || 
                        errorMessage.contains("Access denied") ||
                        errorMessage.contains("GSS-API")) {
                        
                        // Show a more specific error message for password issues
                        errorMessage = "Database authentication failed. Please check your password.";
                    }
                    
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
    
    /**
     * Updates the progress bar and status text
     */
    private void updateProgress(String status, int stepWeight) {
        totalProgress += stepWeight;
        int percentage = Math.min(totalProgress, 100);
        
        // Update UI on EDT
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(percentage);
                statusLabel.setText(status);
            }
        });
    }
}
