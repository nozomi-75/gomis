package lyfjshs.gomis.view.loading;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class ProgressPanel extends JPanel {
    private Timer progressTimer;
    private double progress = 0;
    private boolean increasing = true;
    private Color ACCENT_COLOR_1, ACCENT_COLOR_2;
    
    public ProgressPanel(Color ac_1, Color ac_2) {
    	this.ACCENT_COLOR_1 = ac_1;
		this.ACCENT_COLOR_2 = ac_2;
        setOpaque(false);
        
        progressTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (increasing) {
                    progress += 0.01;
                    if (progress >= 1.0) {
                        increasing = false;
                    }
                } else {
                    progress -= 0.01;
                    if (progress <= 0) {
                        increasing = true;
                    }
                }
                repaint();
            }
        });
        
        progressTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        
        // Background
        g2d.setColor(new Color(255, 255, 255, 25));
        g2d.fillRoundRect(0, 0, w, h, h, h);
        
        // Progress
        int progressWidth = (int) (w * progress);
        if (progressWidth > 0) {
            GradientPaint gradient = new GradientPaint(
                0, 0, ACCENT_COLOR_1,
                w, 0, ACCENT_COLOR_2
            );
            g2d.setPaint(gradient);
            g2d.fillRoundRect(0, 0, progressWidth, h, h, h);
        }
        
        g2d.dispose();
    }
}