package test.notification;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;
import raven.modal.component.ModalBorderAction;
import raven.modal.component.SimpleModalBorder;

public class NotificationPanel extends JPanel {

    public NotificationPanel() {
        init();
    }

    private void init() {
        setLayout(new MigLayout("insets 0,wrap,fillx,width 300", "[fill]"));

        putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,3%);");

        JPanel panel = new JPanel(new MigLayout("wrap,fill", "fill"));
        panel.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,3%);");

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,3%);" +
                "trackInsets:0,3,0,3;" +
                "thumbInsets:0,3,0,3;" +
                "trackArc:$ScrollBar.thumbArc");
        add(scrollPane, "height 350");

        // add test notification
        panel.add(new NotificationItem("profile_1", "Steve", "reacted to your story", "just now"));
        panel.add(new NotificationItem("profile_2", "Sophie", "commented on your picture", "a few minutes ago"));
        panel.add(new NotificationItem("profile_3", "Dara", "replied to your message", "10 minutes ago"));
        panel.add(new NotificationItem("profile_4", "Steve", "liked your post", "2 hours ago"));
        panel.add(new NotificationItem("profile_5", "Sophie", "updated their status", "6 hours ago"));
        panel.add(new NotificationItem("profile_6", "Dara", "mentioned you in a comment", "5 hours ago"));
        panel.add(new NotificationItem("profile_7", "Steve", "shared your photo", "yesterday"));
        panel.add(new NotificationItem("profile_8", "Sophie", "shared a link with you", "yesterday"));
        panel.add(new NotificationItem("profile_9", "Dara", "sent you a friend request", "3 days ago"));

        LabelButton showAll = new LabelButton("Show all");
        showAll.addOnClick(object -> {
            ModalBorderAction.getModalBorderAction(this).doAction(SimpleModalBorder.CLOSE_OPTION);
            // here call another form to show all notification
        });
        add(showAll, "grow 0, al center");
    }
}
class LabelButton extends JLabel {

    public LabelButton(String text) {
        super("<html><a href=\"#\">" + text + "</a></html>");
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(true);
    }

    public LabelButton() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(true);
    }

    public void addOnClick(Consumer event) {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    requestFocus();
                    event.accept(null);
                }
            }
        });
    }
}
