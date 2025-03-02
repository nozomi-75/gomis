package lyfjshs.gomis.view.violation;

import java.awt.Font;
import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lyfjshs.gomis.Database.DAO.ViolationCRUD;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class Violation_Record extends Form {

    private static final long serialVersionUID = 1L;
    private ViolationTablePanel tablePanel;
    private ViolationCRUD violationCRUD;

    public Violation_Record(Connection connect) {
        violationCRUD = new ViolationCRUD(connect);

        setLayout(new MigLayout("", "[grow]", "[pref!][grow]"));
        
        initializeHeader();
        initializeTablePanel(connect);
    }

    private void initializeHeader() {
        JPanel headerPanel = new JPanel(new MigLayout("", "[][][grow][160,fill]", "[]"));
        add(headerPanel, "cell 0 0,grow");

        // Title
        JLabel headerLabel = new JLabel("Violation Record");
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        headerPanel.add(headerLabel, "cell 2 0,alignx center,growy");

        // Add Student Search Button
        JButton studentSearchButton = new JButton("Search Student");
        studentSearchButton.addActionListener(e -> new StudentSearchUI());
        headerPanel.add(studentSearchButton, "cell 4 0");
    }

    private void initializeTablePanel(Connection conn) {
        tablePanel = new ViolationTablePanel(conn, violationCRUD);
        add(tablePanel, "cell 0 1,grow");
        tablePanel.refreshData();
    }

    public void addViolationRecord(String fIRST_NAME, String lAST_NAME, String contact, String violationType,
            String description) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addViolationRecord'");
    }
}
