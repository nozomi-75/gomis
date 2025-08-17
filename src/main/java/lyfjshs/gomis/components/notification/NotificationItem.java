/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.notification;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;

public class NotificationItem extends JPanel {
    private static final Color HOVER_COLOR = new Color(242, 242, 242);
    private boolean unread = true;

    public NotificationItem(String title, String message, String timeAgo) {
        putClientProperty(FlatClientProperties.STYLE, "background:null;");
        setLayout(new MigLayout("insets 10", "[]push[]", "[]3[]")); // Simplified layout
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Title and message
        JLabel lbTitle = new JLabel(title);
        JLabel lbMessage = new JLabel(message);
        JLabel lbTimeAgo = new JLabel(timeAgo);

        // Style components
        lbTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        lbMessage.putClientProperty(FlatClientProperties.STYLE, "");
        lbTimeAgo.putClientProperty(FlatClientProperties.STYLE, "foreground:$Label.disabledForeground;");

        // Add components
        add(lbTitle, "cell 0 0");
        add(lbMessage, "cell 0 1");
        add(lbTimeAgo, "cell 1 0 1 2,right,gapx 10");

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(UIManager.getColor("Panel.background"));
            }
        });

        // Add bottom border
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
        repaint();
    }
}
