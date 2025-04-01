package test;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;

import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.Toast;
import raven.modal.component.SimpleModalBorder;
import raven.modal.toast.ToastPromise;
import test.base.BaseFrame;

public class TestForm extends BaseFrame {

    public TestForm() {
        super("Test Form");
        setLayout(new MigLayout("al center center"));
        JButton show = new JButton("Show");
        show.addActionListener(e -> {
            Box b = new Box(this);
            b.setVisible(true);
        });
        Toast.getDefaultOption().setAnimationEnabled(false);
        add(show);
    }

    private class Box extends JDialog {
        public Box(Frame frame) {
            super(frame, true);
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setSize(new Dimension(500, 500));
            setLocationRelativeTo(null);
            JButton show = new JButton("show");
            ModalDialog.getDefaultOption().getLayoutOption().setOnTop(true);
            show.addActionListener(e -> {
                JButton hide = new JButton("Hide");
                hide.addActionListener(e1 -> setVisible(false));
                ModalDialog.showModal(this, new SimpleModalBorder(hide, "Test"));
            });
           
            setLayout(new MigLayout("al center center"));
            add(show);
            JButton showToast = new JButton("Show toast");
            showToast.addActionListener(e -> {
                Toast.showPromise(this, "HI", new ToastPromise() {
                    @Override
                    public void execute(PromiseCallback callback) {
                        try {
                            Thread.sleep(2000);
                            callback.done(Toast.Type.SUCCESS, "HI");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public boolean rejectAble() {
                        return true;
                    }
                });

            });
            add(showToast);
        }
    }

    public static void main(String[] args) {
        installLaf();
        EventQueue.invokeLater(() -> new TestForm().setVisible(true));
    }
}
