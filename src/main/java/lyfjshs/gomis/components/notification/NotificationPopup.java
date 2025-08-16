package lyfjshs.gomis.components.notification;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import com.formdev.flatlaf.FlatClientProperties;

import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.Modal;

public class NotificationPopup extends Modal {
    private final JPanel notificationsContainer;
    private final NotificationManager notificationManager;
    private final NotificationCallback callback;
    private static final String MODAL_ID = "notifications";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, HH:mm");

    public NotificationPopup(JPanel parent, NotificationCallback callback) {
        this.callback = callback;
        this.notificationManager = NotificationManager.getInstance();
        notificationManager.registerPopup(this);

        // Main layout setup with title
        setLayout(new MigLayout("insets 0,wrap,fillx,hidemode 3", "[fill]"));
        
        // Add title panel
        JPanel titlePanel = new JPanel(new MigLayout("insets 15 20 15 20, fillx", "[grow]"));
        titlePanel.setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")));
        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.putClientProperty(FlatClientProperties.STYLE, "font:bold +2");
        titlePanel.add(titleLabel);
        add(titlePanel);

        // Create main container panel
        JPanel mainPanel = new JPanel(new MigLayout("insets 0,wrap,fillx", "[fill]"));
        mainPanel.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,3%);");

        // Create scrollable notifications panel
        notificationsContainer = new JPanel(new MigLayout("wrap,fillx,insets 0", "[fill]"));
        notificationsContainer.putClientProperty(FlatClientProperties.STYLE, "" +
                "[light]background:shade($Panel.background,3%);" +
                "[dark]background:tint($Panel.background,3%);");

        // Setup scroll pane
        JScrollPane scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE, "" +
                "track:$Panel.background;" +
                "thumb:$Component.accentColor;" +
                "thumbArc:999;" +
                "trackArc:999;" +
                "width:8");
        
        mainPanel.add(scrollPane, "height 350!");
        add(mainPanel);

        // Add "Show all" button at bottom
        JPanel bottomPanel = new JPanel(new MigLayout("insets 10", "[center]"));
        JButton showAllButton = new JButton("Show all");
        showAllButton.putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0");
        showAllButton.addActionListener(e -> {
            hide();
            // Add your show all logic here
        });
        bottomPanel.add(showAllButton);
        add(bottomPanel);

        // Set preferred size
        setPreferredSize(new Dimension(360, 480));
        
        // Load initial notifications
        updateNotifications();
    }

    /**
     * Format timestamp for display in notification
     */
    private String formatTime(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public void show(Component invoker) {
        if (ModalDialog.isIdExist(MODAL_ID)) {
            ModalDialog.closeModal(MODAL_ID);
            return;
        }

        Point p = invoker.getLocationOnScreen();
        
        raven.modal.option.Option option = new raven.modal.option.Option();
        option.setOpacity(0.2f)
              .setAnimationEnabled(true);
        
        option.getLayoutOption()
              .setMargin(0, 0, 0, 0)
              .setLocation(
                  p.x - getPreferredSize().width + invoker.getWidth(),
                  p.y + invoker.getHeight());

        ModalDialog.showModal(invoker, this, option, MODAL_ID);
    }

    public void hide() {
        if (ModalDialog.isIdExist(MODAL_ID)) {
            ModalDialog.closeModal(MODAL_ID);
        }
    }

    public NotificationCallback getCallback() {
        return callback;
    }

    public void addNotification(NotificationManager.Notification notification) {
        SwingUtilities.invokeLater(() -> {
            NotificationItem item = new NotificationItem(
                notification.getTitle(),
                notification.getMessage(), 
                formatTime(notification.getTimestamp())
            );
            notificationsContainer.add(item, "wrap, growx", 0);
            notificationsContainer.revalidate();
            notificationsContainer.repaint();
        });
    }

    public void updateNotifications() {
        SwingUtilities.invokeLater(() -> {
            notificationsContainer.removeAll();
            for (NotificationManager.Notification notification : notificationManager.getNotifications()) {
                NotificationItem item = new NotificationItem(
                    notification.getTitle(),
                    notification.getMessage(),
                    formatTime(notification.getTimestamp())
                );
                notificationsContainer.add(item, "wrap, growx");
            }
            notificationsContainer.revalidate();
            notificationsContainer.repaint();
        });
    }
}
