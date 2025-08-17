/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.loading;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

public class LogoPanel extends JPanel {
    private Timer pulseTimer;
    private double scale = 1.0;
    private boolean growing = true;
    private double outerDashOffset = 0;
    private double innerDashOffset = 0;
    private Color ACCENT_COLOR_1, ACCENT_COLOR_2;
    private Image logoImage; // Store the loaded image

    public LogoPanel(Image logo, Color ac_1, Color ac_2, Color bg) {
        this.ACCENT_COLOR_1 = ac_1;
        this.ACCENT_COLOR_2 = ac_2;
        setOpaque(false);
        this.logoImage = logo;
        // Start pulsating effect
        pulseTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (growing) {
                    scale += 0.005;
                    if (scale >= 1.05) growing = false;
                } else {
                    scale -= 0.005;
                    if (scale <= 0.95) growing = true;
                }
                repaint();
            }
        });

        pulseTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int centerX = w / 2;
        int centerY = h / 2;

        // Apply scaling effect
        g2d.translate(centerX, centerY);
        g2d.scale(scale, scale);
        g2d.translate(-centerX, -centerY);

        // Draw the resized logo at the center (Static, No Rotation)
        int imgX = centerX - (logoImage.getWidth(null) / 2);
        int imgY = centerY - (logoImage.getHeight(null) / 2);
        g2d.drawImage(logoImage, imgX, imgY, null);

        // Outer animated circle (without rotation)
        float[] dashPattern1 = {5, 5};
        BasicStroke stroke1 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern1, (float) outerDashOffset);
        g2d.setStroke(stroke1);
        g2d.setColor(ACCENT_COLOR_1);
        g2d.drawOval(centerX - 50, centerY - 50, 100, 100);

        // Inner animated circle (without rotation)
        float[] dashPattern2 = {4, 4};
        BasicStroke stroke2 = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern2, (float) innerDashOffset);
        g2d.setStroke(stroke2);
        g2d.setColor(ACCENT_COLOR_2);
        g2d.drawOval(centerX - 40, centerY - 40, 80, 80);

        g2d.dispose();
    }
}
