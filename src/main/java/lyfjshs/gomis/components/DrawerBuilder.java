/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components;

import java.sql.Connection;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.Main;
import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.FormManager.AllForms;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.view.MainDashboard;
import lyfjshs.gomis.view.SettingsPanel;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.incident.IncidentList;
import lyfjshs.gomis.view.incident.INCIDENT_fill_up.IncidentFillUpFormPanel;
import lyfjshs.gomis.view.sessions.SessionRecords;
import lyfjshs.gomis.view.sessions.fill_up.SessionsFillUpFormPanel;
import lyfjshs.gomis.view.students.StudentsListMain;
import lyfjshs.gomis.view.students.create.CreateStudentData;
import lyfjshs.gomis.view.violation.Violation_Record;
import raven.extras.AvatarIcon;
import raven.modal.Drawer;
import raven.modal.drawer.DrawerPanel;
import raven.modal.drawer.data.Item;
import raven.modal.drawer.data.MenuItem;
import raven.modal.drawer.menu.AbstractMenuElement;
import raven.modal.drawer.menu.MenuAction;
import raven.modal.drawer.menu.MenuEvent;
import raven.modal.drawer.menu.MenuOption;
import raven.modal.drawer.menu.MenuStyle;
import raven.modal.drawer.renderer.DrawerStraightDotLineStyle;
import raven.modal.drawer.simple.SimpleDrawerBuilder;
import raven.modal.drawer.simple.footer.LightDarkButtonFooter;
import raven.modal.drawer.simple.footer.SimpleFooterData;
import raven.modal.drawer.simple.header.SimpleHeaderData;

/**
 * The {@code DrawerBuilder} class is responsible for constructing the
 * navigation drawer menu for the application. It extends
 * {@code SimpleDrawerBuilder} and provides custom header, footer, and menu
 * options.
 */
@SuppressWarnings("unused")
public class DrawerBuilder extends SimpleDrawerBuilder {

	private static Connection conn;
	private final Connection connection;

	/**
	 * Constructs a {@code DrawerBuilder} with a given database connection.
	 * 
	 * @param cn The database connection to be used for accessing user-related data.
	 */
	public DrawerBuilder(Connection conn) {
		super(createSimpleMenuOption());
		DrawerBuilder.conn = conn;
		this.connection = conn;
		LightDarkButtonFooter lightDarkButtonFooter = new LightDarkButtonFooter(getSimpleFooterData());
		lightDarkButtonFooter.addModeChangeListener(isDarkMode -> {
			// Event listener for light/dark mode changes
		});

		this.footer = (AbstractMenuElement) new FooterPanel();
	}

	/**
	 * Retrieves the header data for the drawer, including an avatar, title, and
	 * description.
	 * 
	 * @return The {@code SimpleHeaderData} containing header details.
	 */
	@Override
	public SimpleHeaderData getSimpleHeaderData() {
		GuidanceCounselor counselor = Main.formManager.getCounselorObject();
		
		// Create avatar icon
		AvatarIcon icon;
		if (counselor != null && counselor.getProfilePicture() != null) {
			 // Convert byte[] to ImageIcon first
            javax.swing.ImageIcon imageIcon = new javax.swing.ImageIcon(counselor.getProfilePicture());
            icon = new AvatarIcon(imageIcon, 50, 50, 3.5f);
		} else {
			// Use default logo if no profile picture
			icon = new AvatarIcon(getClass().getResource("/LYFJSHS_Logo_200x.png"), 50, 50, 3.5f);
		}
		
		icon.setType(AvatarIcon.Type.MASK_SQUIRCLE);
		icon.setBorder(2, 2);
		changeAvatarIconBorderColor(icon);

		UIManager.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("lookAndFeel")) {
				changeAvatarIconBorderColor(icon);
			}
		});

		String fullName = counselor != null ? 
			counselor.getFirstName() + " " + counselor.getMiddleName() + " " + counselor.getLastName() :
			"Guest User";
			
		String position = counselor != null ? 
			counselor.getPosition() :
			"No Position";

		// Debug: Print the details being set in the header
		System.out.println("Drawer Header: " + fullName + ", " + position);

		return new SimpleHeaderData()
				.setIcon(icon)
				.setTitle(fullName)
				.setDescription(position);
	}

	/**
	 * Updates the border color of the avatar icon based on UI theme settings.
	 * 
	 * @param icon The avatar icon to update.
	 */
	private void changeAvatarIconBorderColor(AvatarIcon icon) {
		icon.setBorderColor(new AvatarIcon.BorderColor(UIManager.getColor("Component.accentColor"), 0.7f));
	}

	/**
	 * Retrieves the footer data for the drawer, including the title and version.
	 * 
	 * @return The {@code SimpleFooterData} containing footer details.
	 */
	@Override
	public SimpleFooterData getSimpleFooterData() {
		return new SimpleFooterData().setTitle("GOMIS").setDescription("Version");
	}

	/**
	 * Creates and configures the menu options for the navigation drawer.
	 * 
	 * @return A {@code MenuOption} instance with customized menu items and styles.
	 */
	public static MenuOption createSimpleMenuOption() {
		MenuOption simpleMenuOption = new MenuOption();

		MenuItem items[] = new MenuItem[] {
				new Item.Label("Dashboard"),
				new Item("Home", "home.svg", MainDashboard.class), //menu [1]
				new Item.Label("Management"),
				new Item("Appointments", "calendar.svg", AppointmentManagement.class), //menu [2]
				new Item("Sessions", "gavel.svg") //menu [3]	
						.subMenu("Session Fill-Up Form", SessionsFillUpFormPanel.class)
						.subMenu("Session Records", SessionRecords.class),

				new Item("Students Management", "article_person.svg") //menu [4]
						.subMenu("Create Student", CreateStudentData.class)
						.subMenu("Import School Form", lyfjshs.gomis.view.students.schoolForm.ImportSF.class)
						.subMenu("Students Data", StudentsListMain.class),
				new Item("Incident Management", "assignment.svg") //menu [5]
						.subMenu("Incident Fill-Up Form", IncidentFillUpFormPanel.class)
						.subMenu("Incident Records", IncidentList.class),
				new Item("Violation Records", "forms.svg", Violation_Record.class), //menu [6]
				new Item("Setting", "setting.svg", SettingsPanel.class), //menu [7]
				new Item("Logout", "logout.svg") //menu [8]
		};

		simpleMenuOption.setMenuStyle(new MenuStyle() {
			@Override
			public void styleMenu(JComponent component) {
				component.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
			}
		});

		simpleMenuOption.getMenuStyle().setDrawerLineStyleRenderer(new DrawerStraightDotLineStyle());
		simpleMenuOption.setMenuItemAutoSelectionMode(MenuOption.MenuItemAutoSelectionMode.SELECT_SUB_MENU_LEVEL);
		simpleMenuOption.addMenuEvent(new MenuEvent() {
			@Override
			public void selected(MenuAction action, int[] index) {
				System.out.println("Drawer menu selected " + Arrays.toString(index));
				Class<?> itemClass = action.getItem().getItemClass();
				int i = index[0];

				// Handle logout (last item in the menu)
				if (i == 7) {
					action.consume();
					Drawer.setVisible(false);
					FormManager.logout();
					return;
				}
				
				if (itemClass == null || !Form.class.isAssignableFrom(itemClass)) {
					action.consume();
					return;
				}

				@SuppressWarnings("unchecked")
				Class<? extends Form> formClass = (Class<? extends Form>) itemClass;
				FormManager.showForm(AllForms.getForm(formClass, conn));
			}
		});

		simpleMenuOption.setMenus(items).setBaseIconPath("drawer/icon").setIconScale(0.45f);
		return simpleMenuOption;
	}

	/**
	 * Gets the width of the drawer when fully expanded.
	 * 
	 * @return The width of the drawer in pixels.
	 */
	@Override
	public int getDrawerWidth() {
		return 270;
	}

	/**
	 * Gets the compact width of the drawer when minimized.
	 * 
	 * @return The compact width of the drawer in pixels.
	 */
	@Override
	public int getDrawerCompactWidth() {
		return 80;
	}

	/**
	 * Determines when the drawer should be opened based on screen size.
	 * 
	 * @return The screen width at which the drawer should open.
	 */
	@Override
	public int getOpenDrawerAt() {
		return 1000;
	}

	/**
	 * Determines whether the drawer should open at a specific scale.
	 * 
	 * @return {@code false} to disable scaling-based opening.
	 */
	@Override
	public boolean openDrawerAtScale() {
		return false;
	}

	/**
	 * Builds and applies custom styling to the drawer panel.
	 * 
	 * @param drawerPanel The drawer panel to be styled.
	 */
	@Override
	public void build(DrawerPanel drawerPanel) {
		drawerPanel.putClientProperty(FlatClientProperties.STYLE, getDrawerBackgroundStyle());
	}

	/**
	 * Retrieves the background styling settings for the drawer.
	 * 
	 * @return A style string defining the background color for light and dark
	 *         modes.
	 */
	private static String getDrawerBackgroundStyle() {
		return "[light]background:tint($Panel.background,100%);" + "[dark]background:tint($Panel.background,5%);";
	}

	public static void switchToSessionsFillUpFormPanel() {
        Form[] forms = FormManager.getForms();
        SessionsFillUpFormPanel responsiveForm = null;
        
        // Try to find existing SessionsFillUpFormPanel
        for (Form form : forms) {
            if (form instanceof SessionsFillUpFormPanel) {
                responsiveForm = (SessionsFillUpFormPanel) form;
                break;
            }
        }
        
        // If not found, create new one
        if (responsiveForm == null) {
            responsiveForm = (SessionsFillUpFormPanel) AllForms.getForm(SessionsFillUpFormPanel.class, conn);
        }
        
        // Show the form
        FormManager.showForm(responsiveForm);
    }
}
