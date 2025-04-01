package test.notification;

import java.awt.EventQueue;

import javax.swing.JButton;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;
import raven.modal.option.LayoutOption;
import raven.modal.option.Location;
import raven.modal.option.ModalBorderOption;
import raven.modal.option.Option;
import test.base.BaseFrame;

public class TestNotification extends BaseFrame {

    public TestNotification() {
        super("Test Notification");

        setLayout(new MigLayout("al trailing top,insets 30"));

        JButton cmd = new JButton(new FlatSVGIcon("raven/icon/notification.svg", 0.5f));
        cmd.putClientProperty(FlatClientProperties.STYLE, "" +
                "margin:8,8,8,8;" +
                "arc:999;" +
                "borderWidth:0;" +
                "focusWidth:0;" +
                "innerFocusWidth:0");

        cmd.addActionListener(e -> {
            Option option = ModalDialog.createOption();
            option.getLayoutOption()
                    .setMargin(35, 0, 0, -10)
                    .setLocation(Location.TRAILING, 0)
                    .setOverflowAlignmentAuto(false)
                    .setRelativeToOwner(true)
                    .setRelativeToOwnerType(LayoutOption.RelativeToOwnerType.RELATIVE_GLOBAL);
            option.setOpacity(0f)
                    .getBorderOption()
                    .setBorderWidth(1)
                    .setShadow(BorderOption.Shadow.MEDIUM);
            ModalBorderOption borderOption = new ModalBorderOption().setUseScroll(true);
            ModalDialog.showModal(cmd, new SimpleModalBorder(new NotificationPanel(), "Notifications", borderOption), option);
        });
        add(cmd);
    }

    public static void main(String[] args) {
        installLaf();
        EventQueue.invokeLater(() -> new TestNotification().setVisible(true));
    }
}
