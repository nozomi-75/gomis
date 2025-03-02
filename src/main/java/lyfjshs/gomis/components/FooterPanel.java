
package lyfjshs.gomis.components;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import raven.extras.AvatarIcon;
import raven.modal.drawer.menu.MenuOption;
import raven.modal.drawer.menu.MenuOption.MenuOpenMode;
import raven.modal.drawer.simple.footer.SimpleFooter;
import raven.modal.drawer.simple.footer.SimpleFooterData;

public class FooterPanel extends SimpleFooter {

	private static final long serialVersionUID = 1L;
	private JLabel logoLabel;
	private JLabel LYFJ;
	private JLabel SHS;
	private static SimpleFooterData sfd;
	public FooterPanel() {
		super(sfd = new SimpleFooterData()); // Create a SimpleFooterData instance
		
		AvatarIcon icon = new AvatarIcon(getClass().getResource("/LYFJSHS_Logo_200x200.png"), 60, 60, 0);

		logoLabel = new JLabel("", icon, SwingConstants.HORIZONTAL);

		this.add(logoLabel, "cell 0 2 1 2,alignx left");

		LYFJ = new JLabel("Luis Y. Ferrer Jr.");
		LYFJ.setFont(new Font("Tahom", Font.BOLD, 17));

		this.add(LYFJ, "cell 1 2,alignx center");

		SHS = new JLabel(" Senior High School");
		SHS.setFont(new Font("Tahom", Font.BOLD, 17));

		this.add(SHS, "cell 1 3,alignx center");

		sfd.setTitle(LYFJ.getText() + SHS.getText());
		System.out.println(sfd.getTitle());

	}

	@Override
	protected void layoutOptionChanged(MenuOpenMode menuOpenMode) {
		if (LYFJ == null || SHS == null)
			return;

		if (menuOpenMode == MenuOption.MenuOpenMode.FULL) {
			LYFJ.setVisible(true);
			SHS.setVisible(true);
			logoLabel.setVisible(true);
		} else {
			LYFJ.setVisible(false);
			SHS.setVisible(false);
			logoLabel.setVisible(false);
		}
	}

}
