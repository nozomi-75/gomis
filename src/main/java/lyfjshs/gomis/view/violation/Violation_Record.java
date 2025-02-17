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
    private JTextField searchField;
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

        // Search Panel
        JPanel searchPanel = createSearchPanel();
        headerPanel.add(searchPanel, "cell 3 0");
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new MigLayout("", "[grow][65px]", "[23px]"));
        
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Tahoma", Font.BOLD, 10));
        
        searchBtn.addActionListener(e -> tablePanel.searchViolations(searchField.getText()));
        
        searchPanel.add(searchField, "cell 0 0,grow");
        searchPanel.add(searchBtn, "cell 1 0");
        
        return searchPanel;
    }

    private void initializeTablePanel(Connection conn) {
        tablePanel = new ViolationTablePanel(conn, violationCRUD);
        add(tablePanel, "cell 0 1,grow");
        tablePanel.refreshData();
    }
}
