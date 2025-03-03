package lyfjshs.gomis;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatDarkLaf;

/**
 * A reusable and customizable button with a ripple effect.
 */
public class CoolButtonComponent extends JButton {
    private static final long serialVersionUID = 1L;
    private final List<Ripple> ripples = new ArrayList<>();
    private Timer rippleTimer = null;
    private boolean isPressed = false;
    private Color startColor = new Color(110, 87, 224);
    private Color endColor = new Color(224, 87, 148);
    private int cornerRadius = 16;

    /**
     * Constructor for CoolButtonComponent.
     * @param text The text on the button.
     * @param startColor The gradient start color.
     * @param endColor The gradient end color.
     * @param cornerRadius The radius of rounded corners.
     */
    public CoolButtonComponent(String text, Color startColor, Color endColor, int cornerRadius) {
        super(text);
        this.startColor = startColor;
        this.endColor = endColor;
        this.cornerRadius = cornerRadius;
        
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setFont(new Font("Arial", Font.BOLD, 16));
        setForeground(Color.WHITE);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                ripples.add(new Ripple(e.getX(), e.getY()));
                if (!rippleTimer.isRunning()) rippleTimer.start();
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
        
        rippleTimer = new Timer(16, event -> {
            ripples.removeIf(Ripple::update);
            if (ripples.isEmpty()) rippleTimer.stop();
            repaint();
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw gradient background
        g2d.setPaint(new GradientPaint(0, 0, startColor, width, height, endColor));
        RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, isPressed ? 2 : 0, width, height, cornerRadius, cornerRadius);
        g2d.fill(roundedRectangle);
        
        // Draw ripple effect
        g2d.setClip(roundedRectangle);
        for (Ripple ripple : ripples) {
            g2d.setColor(new Color(255, 255, 255, ripple.alpha));
            g2d.fill(new Ellipse2D.Double(ripple.x - ripple.radius, ripple.y - ripple.radius, ripple.radius * 2, ripple.radius * 2));
        }
        g2d.setClip(null);
        
        g2d.dispose();
        super.paintComponent(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(150, 50);
    }
    
    private class Ripple {
        private final int x, y;
        private float radius = 0;
        private int alpha = 120;
        private final int targetRadius;
        
        public Ripple(int x, int y) {
            this.x = x;
            this.y = y;
            int dx = Math.max(x, getWidth() - x);
            int dy = Math.max(y, getHeight() - y);
            this.targetRadius = (int) Math.sqrt(dx * dx + dy * dy);
        }
        
        public boolean update() {
            radius += targetRadius / 15.0f;
            alpha = (int) (120 * (1 - radius / targetRadius));
            return alpha <= 0;
        }
    }
}

/**
 * Example application using the CoolButtonComponent.
 */
class CoolButtonDemo extends JFrame {
    public CoolButtonDemo() {
        setTitle("Reusable Cool Button Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(18, 18, 18));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        CoolButtonComponent coolButton = new CoolButtonComponent("CLICK ME", new Color(110, 87, 224), new Color(224, 87, 148), 16);
        coolButton.addActionListener(e -> {
            coolButton.setText("CLICKED!");
            new Timer(1000, event -> coolButton.setText("CLICK ME")).start();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(18, 18, 18));
        buttonPanel.add(coolButton);
        
        mainPanel.add(buttonPanel);
        add(mainPanel);
    }
    
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } 
        catch (Exception ex) { System.err.println("Failed to initialize FlatLaf"); }
        SwingUtilities.invokeLater(() -> new CoolButtonDemo().setVisible(true));
    }
}
