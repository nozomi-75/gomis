package lyfjshs.gomis.view.loading;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.Timer;

public class BackgroundCirclesPanel extends JPanel {
    private ArrayList<Circle> circles = new ArrayList<>();
    private Timer animationTimer;
    private Random random = new Random();
    private Color ACCENT_COLOR_1, ACCENT_COLOR_2;
    
    public BackgroundCirclesPanel(Color ac_1, Color ac_2) {
    	this.ACCENT_COLOR_1 = ac_1;
		this.ACCENT_COLOR_2 = ac_2;
        setOpaque(false);
        
        // Create circles
        for (int i = 0; i < 15; i++) {
            circles.add(new Circle());
        }
        
        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Circle circle : circles) {
                    circle.update();
                }
                repaint();
            }
        });
        
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (Circle circle : circles) {
            circle.draw(g2d);
        }
        
        g2d.dispose();
    }
    
    class Circle {
        private double x, y;
        private double size;
        private double opacity = 0;
        private double opacityStep;
        private boolean increasing = true;
        
        public Circle() {
            size = random.nextDouble() * 200 + 50;
            x = random.nextDouble() * getWidth() - size/2;
            y = random.nextDouble() * getHeight() - size/2;
            opacityStep = 0.005 + random.nextDouble() * 0.01;
        }
        
        public void update() {
            if (increasing) {
                opacity += opacityStep;
                if (opacity >= 0.15) {
                    increasing = false;
                }
            } else {
                opacity -= opacityStep;
                if (opacity <= 0) {
                    increasing = true;
                    // Reposition when completely faded out
                    x = random.nextDouble() * getWidth() - size/2;
                    y = random.nextDouble() * getHeight() - size/2;
                }
            }
        }
        
        public void draw(Graphics2D g2d) {
            if (opacity <= 0) return;
            
            GradientPaint gradient = new GradientPaint(
                (float)(x), (float)(y), 
                new Color(ACCENT_COLOR_1.getRed(), ACCENT_COLOR_1.getGreen(), ACCENT_COLOR_1.getBlue(), (int)(opacity * 255)),
                (float)(x + size), (float)(y + size), 
                new Color(ACCENT_COLOR_2.getRed(), ACCENT_COLOR_2.getGreen(), ACCENT_COLOR_2.getBlue(), (int)(opacity * 255))
            );
            
            g2d.setPaint(gradient);
            g2d.fill(new Ellipse2D.Double(x, y, size, size));
        }
    }
}

