/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class StudentResult extends JPanel {
    private static final long serialVersionUID = 1L;
    private JLabel nameLabel;
    private JLabel lrnLabel;
    private Border defaultBorder;
    private Border selectedBorder;
    private Border hoverBorder;
    private boolean isSelected = false;

    public StudentResult(String name, String lrn) {
        setLayout(new MigLayout("insets 10", "[][grow]", "[][]"));
		putClientProperty(FlatClientProperties.STYLE, "arc:20; background:darken(@background,3%)");

        nameLabel = new JLabel(name);
        lrnLabel = new JLabel("LRN: " + lrn);
        
        add(nameLabel, "cell 0 0,alignx left");
        add(lrnLabel, "cell 0 1,alignx left");
        
        // Create borders
        defaultBorder = BorderFactory.createLineBorder(new Color(200, 200, 200), 1);
        selectedBorder = BorderFactory.createLineBorder(new Color(0, 120, 212), 2);
        hoverBorder = BorderFactory.createLineBorder(new Color(0, 120, 212), 1);
        
        // Set initial border and style
        setBorder(defaultBorder);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setupHoverEffect();
    }

    private void setupHoverEffect() {
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isSelected) {
                    setBorder(hoverBorder);
                    // Use darker color for dark mode, lighter color for light mode
                    setBackground(com.formdev.flatlaf.FlatLaf.isLafDark() 
                        ? new Color(45, 45, 45) 
                        : new Color(245, 245, 245));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isSelected) {
                    setBorder(defaultBorder);
                    // Reset to default background based on theme
                    setBackground(com.formdev.flatlaf.FlatLaf.isLafDark() 
                        ? new Color(35, 35, 35) 
                        : Color.WHITE);
                }
            }
        });
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
        setBorder(selected ? selectedBorder : defaultBorder);
        setBackground(selected ? new Color(240, 247, 255) : Color.WHITE);
    }

    public boolean isSelected() {
        return isSelected;
    }
}
