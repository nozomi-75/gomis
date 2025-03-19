package lyfjshs.gomis.FlatTest.ToggleSwitch;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import com.formdev.flatlaf.extras.components.FlatToggleButton;

public class FlatToggleSwitch extends FlatToggleButton {

    public Color activeColor;
    private int animationStep = 0;
    private final int animationDuration = 10; // More steps for a smoother transition
    private final Timer animationTimer;

    public FlatToggleSwitch(Color activeColor) {
        this.activeColor = activeColor;
        setPreferredSize(new Dimension(50, 25));
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setUI(new CustomButtonUI());

        // ✅ Create animation timer (but don't start yet)
        animationTimer = new Timer(15, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isSelected()) {
                    if (animationStep < animationDuration) {
                        animationStep++; // Move knob to the right
                    } else {
                        animationTimer.stop();
                    }
                } else {
                    if (animationStep > 0) {
                        animationStep--; // Move knob to the left
                    } else {
                        animationTimer.stop();
                    }
                }
                repaint(); // Update UI for smooth movement
            }
        });

        // ✅ Add ActionListener to start animation on toggle
        addActionListener(e -> {
            if (!animationTimer.isRunning()) { 
                animationTimer.start(); // Start animation only if not running
            }
        });
    }
    
    public void setAnimationStep(int step) {
        this.animationStep = step;
    }


    public int getAnimationStep() {
        return animationStep;
    }

    public int getAnimationDuration() {
        return animationDuration;
    }
}
