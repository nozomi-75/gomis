/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import com.formdev.flatlaf.ui.FlatToggleButtonUI;
import com.formdev.flatlaf.util.UIScale;

/**
 * A custom UI for a "toggle switch" style with improved smooth animation.
 */
public class FlatToggleSwitchUI extends FlatToggleButtonUI {
    protected FlatToggleSwitchUI(boolean shared) {
        super(shared);
    }

    // -------------------------------------------------
    // Animation settings
    // -------------------------------------------------
    // Try a slightly longer duration (e.g., 200ms) for smoother motion.
    private static final int DEFAULT_ANIMATION_DURATION = 200;  
    // Reduce delay to 5ms for ~200 FPS.
    private static final int TIMER_DELAY = 5;                  

    // Animation progress [0..1] where 0 = OFF and 1 = ON.
    private float animationProgress = 0f;
    private boolean animatingForward = false; // true when toggling ON, false for OFF.
    private Timer animationTimer;
    private long animationStartTime;

    // -------------------------------------------------
    // Create UI
    // -------------------------------------------------
    public static ComponentUI createUI(JComponent c) {
        return new FlatToggleSwitchUI(false);
    }

    // -------------------------------------------------
    // Install defaults and listeners
    // -------------------------------------------------
    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        // Set initial animation state based on selection.
        animationProgress = button.isSelected() ? 1f : 0f;
        
        // Listen for selection changes and start animation.
        button.getModel().addChangeListener(e -> {
            boolean nowSelected = button.isSelected();
            if (nowSelected && animationProgress < 1f) {
                startAnimation(button, true);
            } else if (!nowSelected && animationProgress > 0f) {
                startAnimation(button, false);
            }
        });
    }

    // -------------------------------------------------
    // Animation
    // -------------------------------------------------
    private void startAnimation(AbstractButton button, boolean forward) {
        animatingForward = forward;
        animationStartTime = System.currentTimeMillis();

        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        int duration = getAnimationDuration(button);

        animationTimer = new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - animationStartTime;
                float fraction = Math.min(1f, elapsed / (float) duration);

                // Use a cubic ease-in-out function for smooth acceleration and deceleration.
                fraction = easeInOutCubic(fraction);

                // For toggling on, progress goes 0 -> 1; for off, 1 -> 0.
                animationProgress = animatingForward ? fraction : (1f - fraction);
                button.repaint();

                if (elapsed >= duration) {
                    animationTimer.stop();
                    animationProgress = animatingForward ? 1f : 0f;
                    button.repaint();
                }
            }
        });
        animationTimer.start();
    }

    /**
     * Cubic ease-in-out function.
     * For t in [0,1]:
     *   if t < 0.5 then 4t^3 else 1 - (-2t + 2)^3 / 2.
     */
    private float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
    }

    /**
     * Retrieve the animation duration from client property, UIManager, or use the default.
     */
    private int getAnimationDuration(JComponent c) {
        Object prop = c.getClientProperty("ToggleSwitch.animationDuration");
        if (prop instanceof Number)
            return ((Number) prop).intValue();
        Object uiValue = UIManager.get("ToggleSwitch.animationDuration");
        if (uiValue instanceof Number)
            return ((Number) uiValue).intValue();
        return DEFAULT_ANIMATION_DURATION;
    }

    // -------------------------------------------------
    // Painting
    // -------------------------------------------------
    @Override
    public void paint(Graphics g, JComponent c) {
        // Let FlatToggleButtonUI handle default painting (e.g. focus ring)
        super.paint(g, c);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = c.getWidth();
            int height = c.getHeight();

            // Retrieve colors via helper methods.
            Color trackOn  = getTrackOnColor(c);
            Color trackOff = getTrackOffColor(c);
            Color thumb    = getThumbColor(c);

            // Interpolate the track color based on animation progress.
            Color trackColor = interpolateColor(trackOff, trackOn, animationProgress);
            g2.setColor(trackColor);
            int arc = height;
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            // Draw thumb.
            int thumbSize = height - 4;
            int travelWidth = width - height;
            int thumbX = (int) (2 + travelWidth * animationProgress);
            int thumbY = 2;
            g2.setColor(thumb);
            g2.fillOval(thumbX, thumbY, thumbSize, thumbSize);
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension size = super.getPreferredSize(c);
        size.width = Math.max(size.width, UIScale.scale(40));
        size.height = Math.max(size.height, UIScale.scale(20));
        return size;
    }

    // -------------------------------------------------
    // Color Utilities
    // -------------------------------------------------
    private Color getStyleColor(JComponent c, String key, Color defaultValue) {
        Object cp = c.getClientProperty(key);
        if (cp instanceof Color)
            return (Color) cp;
        Object ui = UIManager.get(key);
        if (ui instanceof Color)
            return (Color) ui;
        return defaultValue;
    }

    private Color getTrackOnColor(JComponent c) {
        return getStyleColor(c, "ToggleSwitch.trackOnColor", new Color(76, 175, 80));
    }

    private Color getTrackOffColor(JComponent c) {
        return getStyleColor(c, "ToggleSwitch.trackOffColor", new Color(160, 160, 160));
    }

    private Color getThumbColor(JComponent c) {
        return getStyleColor(c, "ToggleSwitch.thumbColor", Color.WHITE);
    }

    /**
     * Linearly interpolates between two colors based on a fraction [0..1].
     */
    private static Color interpolateColor(Color c1, Color c2, float frac) {
        float r = c1.getRed()   + (c2.getRed()   - c1.getRed())   * frac;
        float g = c1.getGreen() + (c2.getGreen() - c1.getGreen()) * frac;
        float b = c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * frac;
        float a = c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * frac;
        return new Color((int) r, (int) g, (int) b, (int) a);
    }
}
