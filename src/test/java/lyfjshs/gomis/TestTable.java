package lyfjshs.gomis;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import lyfjshs.gomis.test.CustomTablePanel;
import net.miginfocom.swing.MigLayout;

public class TestTable extends JFrame {
	private static final long serialVersionUID = 1L;

	public TestTable() {
		setTitle("Student Table - Custom FlatLaf UI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1330, 700);
		setLocationRelativeTo(null);

		Object[][] data = { { false, 1, "1234567890", "Smith", "John", "A.", "M", "2005-06-15", 18, "Christianity" },
				{ false, 2, "0987654321", "Doe", "Jane", "B.", "F", "2006-08-22", 17, "Islam" },
				{ false, 3, "1122334455", "Johnson", "Emily", "C.", "F", "2007-02-10", 16, "Buddhism" },
				{ false, 4, "6677889900", "Brown", "Michael", "D.", "M", "2005-11-30", 18, "Hinduism" },
				{ false, 5, "1234567891", "Taylor", "Chris", "E.", "M", "2004-07-20", 19, "Atheism" },
				{ false, 6, "9876543210", "Anderson", "Sarah", "F.", "F", "2003-09-25", 20, "Judaism" },
				{ false, 7, "2345678901", "Thomas", "David", "G.", "M", "2006-11-30", 17, "Buddhism" },
				{ false, 8, "8765432109", "Jackson", "Jessica", "H.", "F", "2005-05-12", 18, "Hinduism" },
				{ false, 9, "3456789012", "White", "Daniel", "I.", "M", "2007-03-18", 16, "Christianity" },
				{ false, 10, "7654321098", "Harris", "Amy", "J.", "F", "2004-08-15", 19, "Islam" },
				{ false, 11, "4567890123", "Martin", "James", "K.", "M", "2006-12-02", 17, "Buddhism" },
				{ false, 12, "6543210987", "Lee", "Emma", "L.", "F", "2005-04-28", 18, "Hinduism" },
				{ false, 13, "5678901234", "Walker", "Matthew", "M.", "M", "2007-01-05", 16, "Christianity" },
				{ false, 14, "7654321098", "Hall", "Hannah", "N.", "F", "2004-09-30", 19, "Islam" },
				{ false, 15, "8765432109", "Allen", "Joseph", "O.", "M", "2006-10-14", 17, "Buddhism" },
				{ false, 16, "9876543210", "Young", "Laura", "P.", "F", "2005-06-08", 18, "Hinduism" },
				{ false, 17, "2345678901", "King", "Kevin", "Q.", "M", "2007-02-22", 16, "Christianity" },
				{ false, 18, "3456789012", "Wright", "Linda", "R.", "F", "2004-07-11", 19, "Islam" },
				{ false, 19, "4567890123", "Lopez", "Karen", "S.", "F", "2006-08-03", 17, "Buddhism" },
				{ false, 20, "5678901234", "Hill", "Jason", "T.", "M", "2005-09-19", 18, "Hinduism" },
				{ false, 21, "6789012345", "Scott", "Nicole", "U.", "F", "2007-01-29", 16, "Christianity" },
				{ false, 22, "7890123456", "Green", "Eric", "V.", "M", "2004-05-07", 19, "Islam" },
				{ false, 23, "8901234567", "Adams", "Rachel", "W.", "F", "2006-11-16", 17, "Buddhism" },
				{ false, 24, "9012345678", "Baker", "Patricia", "X.", "F", "2005-03-24", 18, "Hinduism" } };

		String[] columnNames = { "", "#", "LRN", "Last Name", "First Name", "Middle Name", "Sex", "Birthdate", "Age",
				"Religion" };
		Class<?>[] columnTypes = { Boolean.class, Integer.class, String.class, String.class, String.class, String.class,
				String.class, String.class, Integer.class, String.class };
		boolean[] editableColumns = { true, false, false, false, false, false, false, false, false, false };

		JPanel contentPane = new JPanel(new MigLayout("", "[grow][800px,grow,fill][grow]", "[grow][][][grow]"));

		JPanel topPanel = new JPanel(new MigLayout("", "[][grow][][][]", "[]"));
		topPanel.setOpaque(false);

		JLabel lblSearch = new JLabel("Search: ");
		topPanel.add(lblSearch);

		JTextField searchField = new JTextField(15);
		topPanel.add(searchField, "growx");

		JButton btnNew = new JButton("New");
		JButton btnEdit = new JButton("Edit");
		JButton btnDelete = new JButton("Delete");
		topPanel.add(btnNew);
		topPanel.add(btnEdit);
		topPanel.add(btnDelete);

		contentPane.add(topPanel, "cell 1 1,grow");
		boolean[] editableColumns2 = { true, false, false, false, false, false, false, false, false, false };
		double[] columnWidths2 = { 0.04, 0.06, 0.10, 0.14, 0.14, 0.12, 0.06, 0.12, 0.06, 0.10 };
		int[] alignments2 = { SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.LEFT,
				SwingConstants.RIGHT, SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.RIGHT,
				SwingConstants.CENTER, SwingConstants.CENTER };
		CustomTablePanel tablePanel = new CustomTablePanel(data, columnNames, columnTypes, editableColumns2,
				columnWidths2, alignments2);

		contentPane.add(tablePanel, "cell 1 2,grow");
		setContentPane(contentPane);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(new com.formdev.flatlaf.themes.FlatMacLightLaf());
				new TestTable().setVisible(true);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
