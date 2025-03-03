package lyfjshs.gomis.components;

import java.sql.Connection;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import lyfjshs.gomis.components.FormManager.AllForms;
import lyfjshs.gomis.components.FormManager.Form;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.view.MainDashboard;
import lyfjshs.gomis.view.appointment.AppointmentManagement;
import lyfjshs.gomis.view.incident.IncidentFillUpForm;
import lyfjshs.gomis.view.incident.IncidentList;
import lyfjshs.gomis.view.sessions.SessionRecords;
import lyfjshs.gomis.view.sessions.SessionsForm;
import lyfjshs.gomis.view.students.StudentMangementGUI;
import lyfjshs.gomis.view.students.StudentSearchPanel;
import lyfjshs.gomis.view.students.create.StudentInfoFullForm;
import lyfjshs.gomis.view.violation.ViolationFillUpForm;
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
public class DrawerBuilder extends SimpleDrawerBuilder {

	private static Connection conn;
	private Connection connection;

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
		AvatarIcon icon = new AvatarIcon(getClass().getResource("/LYFJSHS_Logo_200x200.png"), 50, 50, 3.5f);
		icon.setType(AvatarIcon.Type.MASK_SQUIRCLE);
		icon.setBorder(2, 2);
		changeAvatarIconBorderColor(icon);

		UIManager.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("lookAndFeel")) {
				changeAvatarIconBorderColor(icon);
			}
		});

		String fullName = FormManager.getCounselorFullName();
		String position = FormManager.getCounselorPosition();

		// Debug: Print the details being set in the header
		System.out.println("Drawer Header: " + fullName + ", " + position);

		// Add null checks
		if (fullName == null || fullName.trim().equals("null null")) {
			fullName = "Test User";
		}
		if (position == null || position.trim().isEmpty()) {
			position = "Test Position";
		}

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
				new Item("Home", "home.svg", MainDashboard.class),
				new Item.Label("Management"),
				new Item("Appointments", "calendar.svg", AppointmentManagement.class),
				new Item("Sessions", "gavel.svg")
						.subMenu("Session Fill-Up Form", SessionsForm.class)
						.subMenu("Session Records", SessionRecords.class),

				new Item("Students Management", "article_person.svg")
						.subMenu("Create Student", StudentInfoFullForm.class)
						.subMenu("Students Data", StudentMangementGUI.class),
				new Item("Incident Management", "assignment.svg")
						.subMenu("Incident Fill-Up Form", IncidentFillUpForm.class)
						.subMenu("Incident Records", IncidentList.class),
				new Item("Violation Management", "forms.svg")
						.subMenu("Violation Fill-Up Form", ViolationFillUpForm.class)
						.subMenu("Violation Records", Violation_Record.class),
				new Item("Setting", "setting.svg", MainDashboard.class),
				new Item("Logout", "logout.svg")
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

				// Handle settings (second to last item)
				if (i == 7) {
					action.consume();
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
}
