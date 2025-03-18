package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;

import com.formdev.flatlaf.extras.components.FlatToggleButton;

public class FlatToggleSwitch extends FlatToggleButton {

    private Color activeColor;
    private int animationStep = 0;
    private final int animationDuration = 10; // Duration of the animation in steps
    private final Timer animationTimer = new Timer(30, null);

    public FlatToggleSwitch(Color activeColor) {
        this.activeColor = activeColor;
        setPreferredSize(new Dimension(50, 25));
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setUI(new CustomToggleButtonUI());

        addActionListener(e -> {
            if (isSelected()) {
                animationStep = 0;
                animationTimer.addActionListener(animateAction(true));
            } else {
                animationStep = animationDuration;
                animationTimer.addActionListener(animateAction(false));
            }
            animationTimer.start();
        });
    }

    private ActionListener animateAction(boolean forward) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forward) {
                    animationStep++;
                    if (animationStep > animationDuration) {
                        animationTimer.stop();
                    }
                } else {
                    animationStep--;
                    if (animationStep < 0) {
                        animationTimer.stop();
                    }
                }
                repaint();
            }
        };
    }

    private class CustomToggleButtonUI extends BasicButtonUI {

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            FlatToggleSwitch button = (FlatToggleSwitch) c;
            int width = button.getWidth();
            int height = button.getHeight();

            // Background color based on selection
            Color bgColor = button.isSelected() ? button.activeColor : new Color(220, 220, 220);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, width, height, height, height);

            // Draw switch circle with animation
            int circleX = (int) (width * animationStep / animationDuration - height / 2);
            g2.setColor(Color.WHITE);
            g2.fillOval(circleX, height / 4, height / 2, height / 2);
        }
    }
}
