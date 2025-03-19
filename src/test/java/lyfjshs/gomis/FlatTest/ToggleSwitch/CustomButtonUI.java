package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

public class CustomButtonUI extends BasicButtonUI {

    private int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (!(c instanceof FlatToggleSwitch)) return; // Ensure component is FlatToggleSwitch

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FlatToggleSwitch button = (FlatToggleSwitch) c;
        int width = button.getWidth();
        int height = button.getHeight();

        // Get animation values from FlatToggleSwitch
        int animationStep = button.getAnimationStep();
        int animationDuration = button.getAnimationDuration();

        // Background color transition
        Color bgColor = new Color(
            clamp((int) (220 + (button.activeColor.getRed() - 220) * (animationStep / (double) animationDuration))),
            clamp((int) (220 + (button.activeColor.getGreen() - 220) * (animationStep / (double) animationDuration))),
            clamp((int) (220 + (button.activeColor.getBlue() - 220) * (animationStep / (double) animationDuration)))
        );

        // Draw rounded background (toggle track)
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, width, height, height, height);

        // Subtle shadow effect (Windows 11 style)
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(2, 2, width - 4, height - 4, height, height);

        // 🎯 Corrected knob position and size
        int knobSize = height - 6; // Keep knob smaller than track
        int circleX = (int) ((width - height) * (animationStep / (double) animationDuration)); 
        int circleY = (height - knobSize) / 2; // Center the knob

        // Draw the knob
        g2.setColor(Color.WHITE);
        g2.fillOval(circleX, circleY, knobSize, knobSize);
    }
}
