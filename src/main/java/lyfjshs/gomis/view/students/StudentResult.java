package lyfjshs.gomis.view.students;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import java.awt.Font;

public class StudentResult extends JPanel {

	private static final long serialVersionUID = 1L;

	public StudentResult(String studName, String studLRN) {
		this.setLayout(new MigLayout("", "[grow]", "[][]"));
		
		JLabel studentName = new JLabel(studName);
		studentName.setFont(new Font("Tahoma", Font.BOLD, 15));
		add(studentName, "cell 0 0");
		
		JLabel studentLRN = new JLabel("LRN: " + studLRN);
		add(studentLRN, "cell 0 1");
	}

}
