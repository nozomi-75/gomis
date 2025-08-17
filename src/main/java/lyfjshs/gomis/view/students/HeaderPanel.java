/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students;

import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import net.miginfocom.swing.MigLayout;

public class HeaderPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField searchField;
	private JButton searchButton;
	private JButton printGoodMoralButton;
	private JButton filterButton;
	private JButton backBtn;
	private JLabel activeFiltersLabel;
	private JPanel headerSearchPanel;

	private void intializeComponents() {
		headerSearchPanel = new JPanel(new MigLayout("", "[][grow][][fill][fill]", "[grow]"));
		searchField = new JTextField(25);
		filterButton = new JButton("Filter");
		printGoodMoralButton = new JButton("Print Selected");
		searchButton = new JButton("", new FlatSVGIcon("icons/search.svg", 0.4f));
		activeFiltersLabel = new JLabel(" Active Filter");
		backBtn = new JButton("Back");
		backBtn.setVisible(false);

	}

	public HeaderPanel() {
		setLayout(new MigLayout("", "[][][][grow][][]", "[grow,center]"));
		this.setOpaque(false);
		intializeComponents();

		JLabel headerLabel = new JLabel("STUDENT DATA");
		this.add(headerLabel, "cell 1 0");

		headerLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

		headerSearchPanel.setOpaque(false);
		this.add(headerSearchPanel, "cell 3 0,grow");

		headerSearchPanel.add(new JLabel("Search LRN/Name:"), "cell 0 0");
		headerSearchPanel.add(searchField, "flowx,cell 1 0,grow");
		searchField.putClientProperty("JTextField.placeholderText", "Enter LRN or any part of name...");

		filterButton.putClientProperty(FlatClientProperties.STYLE,
				"" + "background:$Component.accentColor;" + "foreground:#FFFFFF;" + // Use hex color instead of 'white'
						"borderWidth:0;" + "focusWidth:0;" + "arc:8;" + "margin:5,10,5,10");
		headerSearchPanel.add(filterButton, "flowx,cell 3 0");

//		printGoodMoralButton.addActionListener(e -> handlePrintGoodMoral());
		headerSearchPanel.add(printGoodMoralButton, "cell 4 0");

		headerSearchPanel.add(searchButton, "cell 1 0,growy");
		searchButton.setLayout(new MigLayout("insets 0,al trailing,filly", "", "[center]"));
		searchButton.putClientProperty(FlatClientProperties.STYLE,
				"" + "margin:5,7,5,10;" + "arc:10;" + "borderWidth:0;" + "focusWidth:0;" + "innerFocusWidth:0;"
						+ "[light]background:shade($Panel.background,10%);"
						+ "[dark]background:tint($Panel.background,10%);"
						+ "[light]foreground:tint($Button.foreground,40%);"
						+ "[dark]foreground:shade($Button.foreground,30%);");

		headerSearchPanel.add(activeFiltersLabel, "cell 3 0");

		this.add(backBtn, "cell 4 0");
	}

	public JButton getSearchButton() {
		return searchButton;
	}

	public JButton getBackBtn() {
		return backBtn;
	}

	public JButton getFilterButtton() {
		return filterButton;
	}

	public JButton getPrintBtn() {
		return printGoodMoralButton;
	}

	public void addBackButtonAction(ActionListener listener) {
		backBtn.addActionListener(listener);
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public JLabel getActiveFiltersLabel() {
		return activeFiltersLabel;
	}

	public JPanel getHeaderSearchPanel() {
		return headerSearchPanel;
	}

	public void updateActiveFiltersLabel(String text) {
		if (text == null || text.trim().isEmpty()) {
			activeFiltersLabel.setText("");
			activeFiltersLabel.setVisible(false);
		} else {
			activeFiltersLabel.setText(text);
			activeFiltersLabel.setVisible(true);
		}
	}

	public void clearActiveFiltersLabel() {
		activeFiltersLabel.setText("");
		activeFiltersLabel.setVisible(false);
	}

	public void setSearchFieldText(String text) {
		searchField.setText(text);
	}

	public void clearSearchField() {
		searchField.setText("");
	}

}

//	method used for properties only DO NOT REMOVE 
//	private void addTint(JButton button) {
//		JButton searchButton = new JButton("", new FlatSVGIcon("icons/search.svg", 0.4f));
//		searchButton.setLayout(new MigLayout("insets 0,al trailing,filly", "", "[center]"));
//		searchButton.setHorizontalAlignment(JButton.LEADING);
//		searchButton.putClientProperty(FlatClientProperties.STYLE,
//				"" + "margin:5,7,5,10;" + "arc:10;" + "borderWidth:0;" + "focusWidth:0;" + "innerFocusWidth:0;"
//						+ "[light]background:shade($Panel.background,10%);"
//						+ "[dark]background:tint($Panel.background,10%);"
//						+ "[light]foreground:tint($Button.foreground,40%);"
//						+ "[dark]foreground:shade($Button.foreground,30%);");
//		JLabel label = new JLabel("");
//		label.putClientProperty(FlatClientProperties.STYLE, "" + "[light]foreground:tint($Button.foreground,40%);"
//				+ "[dark]foreground:shade($Button.foreground,30%);");
//		button.add(label);
//	}