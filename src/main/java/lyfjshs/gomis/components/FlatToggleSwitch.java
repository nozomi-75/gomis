/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components;

import java.awt.Color;
import java.awt.Dimension;

import com.formdev.flatlaf.extras.components.FlatToggleButton;

/**
 * A custom toggle switch component that uses our FlatToggleSwitchUI.
 * Inherits from FlatToggleButton for easy styling with FlatLaf extras.
 */
public class FlatToggleSwitch extends FlatToggleButton {
    public FlatToggleSwitch() {
        super();
        // recommended size
        setPreferredSize(new Dimension(50, 25));

        // no border or focus painted (we rely on FlatLaf's default focus painting)
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
    }

    @Override
    public void updateUI() {
        setUI(FlatToggleSwitchUI.createUI(this));
    }

    /**
     * Convenience setter for track "on" color if you want to override it for this instance.
     */
    public void setTrackOnColor(Color color) {
        putClientProperty("ToggleSwitch.trackOnColor", color);
    }

    /**
     * Convenience setter for track "off" color if you want to override it for this instance.
     */
    public void setTrackOffColor(Color color) {
        putClientProperty("ToggleSwitch.trackOffColor", color);
    }

    /**
     * Convenience setter for thumb color if you want to override it for this instance.
     */
    public void setThumbColor(Color color) {
        putClientProperty("ToggleSwitch.thumbColor", color);
    }

    /**
     * Convenience setter for animation duration (ms).
     */
    public void setAnimationDuration(int duration) {
        putClientProperty("ToggleSwitch.animationDuration", duration);
    }
}
