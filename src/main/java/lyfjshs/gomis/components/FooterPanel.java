/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
	private JLabel orgName;
	private JLabel orgType;
	private static SimpleFooterData sfd;
	public FooterPanel() {
		super(sfd = new SimpleFooterData()); // Create a SimpleFooterData instance
		
		// Use a generic logo image
		AvatarIcon icon = new AvatarIcon(getClass().getResource("/GOMIS_Circle.png"), 60, 60, 0);

		logoLabel = new JLabel("", icon, SwingConstants.HORIZONTAL);

		this.add(logoLabel, "cell 0 2 1 2,alignx left");

		orgName = new JLabel("Guidance Office Management");
		orgName.setFont(new Font("Tahoma", Font.BOLD, 17));

		this.add(orgName, "cell 1 2,alignx left");

		orgType = new JLabel("Information System");
		orgType.setFont(new Font("Tahoma", Font.BOLD, 17));

		this.add(orgType, "cell 1 3,alignx left");

		sfd.setTitle(orgName.getText() + orgType.getText());
		System.out.println(sfd.getTitle());

	}

	@Override
	protected void layoutOptionChanged(MenuOpenMode menuOpenMode) {
		if (orgName == null || orgType == null)
			return;

		if (menuOpenMode == MenuOption.MenuOpenMode.FULL) {
			orgName.setVisible(true);
			orgType.setVisible(true);
			logoLabel.setVisible(true);
		} else {
			orgName.setVisible(false);
			orgType.setVisible(false);
			logoLabel.setVisible(false);
		}
	}

}
