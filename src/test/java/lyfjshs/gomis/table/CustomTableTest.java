package lyfjshs.gomis.table;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.components.table.TableActionManager;
import net.miginfocom.swing.MigLayout;

public class CustomTableTest {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			// Sample data with checkbox and actions
			Object[][] dataWithCheckbox = { { false, "John Doe", 25, 95.5, null },
					{ true, "Jane Smith", 30, 87.0, null }, { false, "Bob Johnson", 22, 91.5, null } };

			String[] columnNamesWithCheckbox = { "Select", "Name", "Age", "Score", "Actions" };
			Class<?>[] columnTypesWithCheckbox = { Boolean.class, String.class, Integer.class, Double.class,
					Object.class };
			boolean[] editableColumnsWithCheckbox = { true, false, false, false, true };
			double[] columnWidthsWithCheckbox = { 0.1, 0.3, 0.2, 0.2, 0.2 };
			int[] alignmentsWithCheckbox = { SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.RIGHT,
					SwingConstants.RIGHT, SwingConstants.CENTER };

			// Configure actions
			TableActionManager actionManager = new TableActionManager().addAction("Edit", (table, row) -> {
				JOptionPane.showMessageDialog(table, "Editing row " + row + ": " + table.getValueAt(row, 1));
			}, new Color(100, 150, 255), null).addAction("Delete", (table, row) -> {
				int confirm = JOptionPane.showConfirmDialog(table, "Delete " + table.getValueAt(row, 1) + "?",
						"Confirm", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					((DefaultTableModel) table.getModel()).removeRow(row);
				}
			}, new Color(255, 100, 100), null);

			// Sample data without checkbox
			Object[][] dataWithoutCheckbox = { { "John Doe", 25, 95.5 }, { "Jane Smith", 30, 87.0 },
					{ "Bob Johnson", 22, 91.5 } };

			String[] columnNamesWithoutCheckbox = { "Name", "Age", "Score" };
			Class<?>[] columnTypesWithoutCheckbox = { String.class, Integer.class, Double.class };
			boolean[] editableColumnsWithoutCheckbox = { false, false, false };
			double[] columnWidthsWithoutCheckbox = { 0.5, 0.25, 0.25 };
			int[] alignmentsWithoutCheckbox = { SwingConstants.LEFT, SwingConstants.RIGHT, SwingConstants.RIGHT };

			// Create main frame
			JFrame frame = new JFrame("CustomTable Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(1200, 400);
			frame.setLocationRelativeTo(null);

			JPanel mainPanel = new JPanel(new MigLayout("fill", "[grow][grow]", "[grow][]"));

			// Panel with checkbox and actions
			JPanel panelWithCheckbox = new JPanel(new MigLayout("fill"));
			panelWithCheckbox.setBorder(BorderFactory.createTitledBorder("With Checkbox & Actions"));
			GTable tableWithCheckbox = new GTable(
					dataWithCheckbox, // Data with checkbox 
					columnNamesWithCheckbox, // Column names with checkbox
					columnTypesWithCheckbox,  // Column types with checkbox
					editableColumnsWithCheckbox, // Editable columns with checkbox
					columnWidthsWithCheckbox, // Column widths with checkbox
					alignmentsWithCheckbox, // Alignments with checkbox
					true, // include Check Box column
					actionManager ); // Action manager	
			JScrollPane scrollPaneWithCheckbox = new JScrollPane(tableWithCheckbox);
			panelWithCheckbox.add(scrollPaneWithCheckbox, "grow");

			// Panel without checkbox
			JPanel panelWithoutCheckbox = new JPanel(new MigLayout("fill"));
			panelWithoutCheckbox.setBorder(BorderFactory.createTitledBorder("Without Checkbox"));
			GTable tableWithoutCheckbox = new GTable(dataWithoutCheckbox, columnNamesWithoutCheckbox,
					columnTypesWithoutCheckbox, editableColumnsWithoutCheckbox, columnWidthsWithoutCheckbox,
					alignmentsWithoutCheckbox, false, null);
			JScrollPane scrollPaneWithoutCheckbox = new JScrollPane(tableWithoutCheckbox);
			panelWithoutCheckbox.add(scrollPaneWithoutCheckbox, "grow");

			mainPanel.add(panelWithCheckbox, "cell 0 0, grow");
			mainPanel.add(panelWithoutCheckbox, "cell 1 0, grow");

			JButton modifyButton = new JButton("Modify Widths & Alignments");
			modifyButton.addActionListener(e -> {
				double[] newWidths = { 0.1, 0.25, 0.2, 0.2, 0.25 };
				tableWithCheckbox.setColumnWidths(newWidths);

				int[] newAlignments = { SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT,
						SwingConstants.RIGHT, SwingConstants.CENTER };
				tableWithCheckbox.setColumnAlignments(newAlignments);
			});
			mainPanel.add(modifyButton, "cell 0 1, span 2, center");

			frame.add(mainPanel);
			frame.setVisible(true);
		});
	}
}