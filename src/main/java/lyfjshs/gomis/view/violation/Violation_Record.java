package lyfjshs.gomis.view.violation;

import java.awt.Font;
import java.sql.Connection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class Violation_Record extends Form {

    private static final long serialVersionUID = 1L;
    private ViolationTablePanel tablePanel;
    private ViolationDAO ViolationDAO;

    public Violation_Record(Connection connect) {
        ViolationDAO = new ViolationDAO(connect);

        setLayout(new MigLayout("", "[grow]", "[pref!][grow]"));

        initializeHeader();
        initializeTablePanel(connect);
    }

    private void initializeHeader() {
        JPanel headerPanel = new JPanel(new MigLayout("", "[][grow][]", "[]"));
        add(headerPanel, "cell 0 0,grow");

        // Title
        JLabel headerLabel = new JLabel("Violation Record");
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 22));
        headerPanel.add(headerLabel, "cell 1 0,alignx center,growy");
    }

    private void initializeTablePanel(Connection conn) {
        tablePanel = new ViolationTablePanel(conn, ViolationDAO);
        add(tablePanel, "cell 0 1,grow");
        tablePanel.refreshData();
    }

    // Add method to refresh violations
    public void refreshViolations() {
        if (tablePanel != null) {
            tablePanel.refreshData();
        }
    }
}
