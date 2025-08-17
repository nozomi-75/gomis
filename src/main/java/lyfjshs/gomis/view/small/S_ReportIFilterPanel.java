/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.small;

import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import raven.datetime.DatePicker;

public class S_ReportIFilterPanel extends JPanel {
	private final DatePicker startDatePicker;
	private final DatePicker endDatePicker;
	private final JFormattedTextField startDateField;
	private final JFormattedTextField endDateField;
	private final JComboBox<String> violationTypeComboBox;
	private final JButton applyBtn;
	private final JButton clearBtn;
	private FilterListener filterListener;
	LocalDate today = LocalDate.now();
	private JPanel panel;

	public interface FilterListener {
		void onFilterChanged(LocalDate start, LocalDate end, String violationType);
	}

	public S_ReportIFilterPanel() {
		super(new MigLayout("insets 10, fillx", "[][::400px,grow][][grow][][grow,fill][]", "[][][]"));
		// Start Date
		add(new JLabel("Start Date:"), "cell 1 0");
		startDatePicker = new DatePicker();
		endDatePicker = new DatePicker();

		// End Date
		JLabel label = new JLabel("End Date:");
		add(label, "cell 3 0");

		// Violation Type Filter
		JLabel label_1 = new JLabel("Violation Type:");
		add(label_1, "cell 5 0");
		startDateField = new JFormattedTextField();
		startDateField.setColumns(15);
		add(startDateField, "cell 1 1,growx");
		endDateField = new JFormattedTextField();
		endDateField.setColumns(15);
		endDatePicker.setEditor(endDateField);
		add(endDateField, "cell 3 1,growx");
		violationTypeComboBox = new JComboBox<>(
				new String[] { "All", "Bullying", "Fighting/Weapons", "Vandalism", "Theft", "Cyberbullying",
						"Disrespect/Insubordination", "Substance Abuse", "Academic Dishonesty", "Other" });
		add(violationTypeComboBox, "cell 5 1,growx");
		panel = new JPanel();
		add(panel, "cell 0 2 7 1,grow");
		panel.setLayout(new MigLayout("", "[][][]", "[]"));

		// Apply Button
		applyBtn = new JButton("Apply");
		panel.add(applyBtn, "flowx,cell 1 0");

		// Clear Button
		clearBtn = new JButton("Clear");
		panel.add(clearBtn, "cell 1 0");

		actionsButtons();
		setDefault();
	}

	private void setDefault() {
		startDatePicker.setEditor(startDateField);

		startDatePicker.setSelectedDate(today.minusYears(3));
		endDatePicker.setSelectedDate(today);

	}

	private void actionsButtons() {
		applyBtn.addActionListener(e -> {
			notifyListener();
		});
		clearBtn.addActionListener(e -> {
			startDatePicker.setSelectedDate(today.minusYears(3));
			endDatePicker.setSelectedDate(today);
			violationTypeComboBox.setSelectedIndex(0);

			notifyListener();
		});
	}

	public void setFilterListener(FilterListener listener) {
		this.filterListener = listener;
	}

	private void notifyListener() {
		if (filterListener != null) {
			filterListener.onFilterChanged(startDatePicker.getSelectedDate(), endDatePicker.getSelectedDate(),
					(String) violationTypeComboBox.getSelectedItem());
		}
	}

	public LocalDate getStartDate() {
		return startDatePicker.getSelectedDate();
	}

	public LocalDate getEndDate() {
		return endDatePicker.getSelectedDate();
	}

	public String getViolationType() {
		return (String) violationTypeComboBox.getSelectedItem();
	}
}