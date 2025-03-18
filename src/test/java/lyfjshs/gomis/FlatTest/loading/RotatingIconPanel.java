package lyfjshs.gomis.FlatTest.loading;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.formdev.flatlaf.extras.FlatSVGIcon;

public class RotatingIconPanel extends JPanel {
    private final FlatSVGIcon icon;
    private double rotationAngle = 0;
    private final Timer rotationTimer;

    public RotatingIconPanel(String svgPath, int iconSize, int rotationSpeed) {
        this.icon = new FlatSVGIcon(svgPath, iconSize, iconSize);
        setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        setOpaque(false);

        rotationTimer = new Timer(10, e -> {
            rotateBy(rotationSpeed);
            repaint();
        });
    }

    public void startAnimation() {
        rotationTimer.start();
    }

    public void stopAnimation() {
        rotationTimer.stop();
    }

    private void rotateBy(double degrees) {
        rotationAngle = (rotationAngle + degrees) % 360;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int x = (getWidth() - icon.getIconWidth()) / 2;
        int y = (getHeight() - icon.getIconHeight()) / 2;
        double cx = x + icon.getIconWidth() / 2.0;
        double cy = y + icon.getIconHeight() / 2.0;
        g2d.rotate(Math.toRadians(rotationAngle), cx, cy);
        icon.paintIcon(this, g2d, x, y);
        g2d.dispose();
    }
}

