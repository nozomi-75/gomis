package lyfjshs.gomis.test.Modal;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import lyfjshs.gomis.test.forms.AddAppointmentPanel;
import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.component.SimpleModalBorder;
import raven.modal.option.BorderOption;

public class TestModal extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestModal frame = new TestModal();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public TestModal() {
		setTitle("Test 123");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(800, 800));
		setLocationRelativeTo(null);
		FlatRobotoFont.install();
		FlatLaf.registerCustomDefaultsSource("raven.modal.demo.themes");
		FlatMacDarkLaf.setup();
		UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
		initializeComponents();
	}

	private void initializeComponents() {
		contentPane = new JPanel();
		contentPane.setLayout(new MigLayout("fill", "[grow]", "[][grow]"));
		setContentPane(contentPane);

		JButton btn = new JButton("Modal Btn");
		contentPane.add(btn, "cell 0 0");

		ModalDialog.getDefaultOption()
				.setOpacity(0f)
				.setAnimationOnClose(false)
				.getBorderOption()
				.setBorderWidth(0.5f)
				.setShadow(BorderOption.Shadow.MEDIUM);
		
		btn.addActionListener(e -> {
			ModalDialog.showModal(this,
					new SimpleModalBorder(new AddAppointmentPanel(), "Add Appointment", new SimpleModalBorder.Option[] {
						new SimpleModalBorder.Option("Add Appointment", SimpleModalBorder.YES_OPTION),
						new SimpleModalBorder.Option("Cancel", SimpleModalBorder.NO_OPTION)
					}, (controller, action) -> {
						if (action == SimpleModalBorder.YES_OPTION) {
							controller.consume();
							// actions todo next after Confirm
						} else if (action == SimpleModalBorder.NO_OPTION) {
							controller.consume();
							// actions todo next after Reject
						} else if (action == SimpleModalBorder.CLOSE_OPTION
								|| action == SimpleModalBorder.CANCEL_OPTION) {
							controller.close();
							// actions todo next after Close or Cancel
						}
					}),
					"input");
				// set size of modal dialog to 800x800
				ModalDialog.getDefaultOption().getLayoutOption().setSize(800, 800);
		});

		
	}

}
