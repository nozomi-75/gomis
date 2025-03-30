package lyfjshs.gomis;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import net.miginfocom.swing.MigLayout;
import raven.modal.ModalDialog;
import raven.modal.option.Location;
import raven.modal.option.Option;

public class TestSearch extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton searchButton;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(new FlatMacLightLaf());
					TestSearch frame = new TestSearch();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TestSearch() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 592, 422);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new MigLayout("fill", "[grow]", "[][grow]"));
		setContentPane(contentPane);

		 searchButton = createSearchButton("Search Student", " ");

		searchButton.addActionListener(e -> showSearchPanel());
		contentPane.add(searchButton, "cell 0 0,grow");
	}

	public static JButton createSearchButton(String text, String rightTxt) {
		JButton button = new JButton(text, new FlatSVGIcon("icons/search.svg", 0.4f));
		button.setLayout(new MigLayout("insets 0,al trailing,filly", "", "[center]"));
		button.setHorizontalAlignment(JButton.LEADING);
		button.putClientProperty(FlatClientProperties.STYLE, "" + "margin:5,7,5,10;" + "arc:10;" + "borderWidth:0;"
				+ "focusWidth:0;" + "innerFocusWidth:0;" + "[light]background:shade($Panel.background,10%);"
				+ "[dark]background:tint($Panel.background,10%);" + "[light]foreground:tint($Button.foreground,40%);"
				+ "[dark]foreground:shade($Button.foreground,30%);");
		JLabel label = new JLabel(rightTxt);
		label.putClientProperty(FlatClientProperties.STYLE, "" + "[light]foreground:tint($Button.foreground,40%);"
				+ "[dark]foreground:shade($Button.foreground,30%);");
		button.add(label);
		return button;
	}

	private void showSearchPanel() {
		if (ModalDialog.isIdExist("search")) {
			return;
		}
		Option option = ModalDialog.createOption();
		option.setOpacity(0f);
		
		option.setAnimationEnabled(true);
		option.getLayoutOption().setMargin(40, 10, 10, 10).setLocation(Location.CENTER, Location.TOP);
		ModalDialog.showModal(this, new SearchPanel(), option, "search");
	}

}
