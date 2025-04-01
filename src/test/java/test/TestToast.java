package test;

import java.awt.EventQueue;

import javax.swing.JButton;

import net.miginfocom.swing.MigLayout;
import raven.modal.Toast;
import raven.modal.toast.option.ToastDirection;
import raven.modal.toast.option.ToastLocation;
import raven.modal.toast.option.ToastOption;
import test.base.BaseFrame;

public class TestToast extends BaseFrame {

	public TestToast() {
		super("Test Toast");
		setLayout(new MigLayout("al center center"));

		JButton cmdShow = new JButton("Show Toast");
		cmdShow.addActionListener(e -> {

			// create toast option
			ToastOption toastOption = Toast.createOption();

			// set location
			toastOption.getLayoutOption()
					// setMargin(int top, int left, int bottom, int right)
					.setMargin(0, 0, 50, 0).setDirection(ToastDirection.TOP_TO_BOTTOM);

			// show toast
			Toast.show(this, Toast.Type.SUCCESS, "Success message", ToastLocation.BOTTOM_CENTER, toastOption);

		});
		add(cmdShow);
	}

	public static void main(String[] args) {
		installLaf();
		EventQueue.invokeLater(() -> new TestToast().setVisible(true));
	}
}
