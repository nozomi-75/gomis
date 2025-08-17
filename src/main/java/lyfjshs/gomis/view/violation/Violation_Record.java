/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.violation;

import java.awt.Font;
import java.sql.Connection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.DAO.ViolationDAO;
import lyfjshs.gomis.components.FormManager.Form;
import net.miginfocom.swing.MigLayout;

public class Violation_Record extends Form {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(Violation_Record.class);
    private ViolationTablePanel tablePanel;
    private ViolationDAO violationDAO;

    public Violation_Record(Connection connect) {
        violationDAO = new ViolationDAO(connect);
        setLayout(new MigLayout("", "[grow]", "[pref!][grow]"));

        initializeHeader();
        initializeTablePanel(connect);
        refreshViolations();

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
        tablePanel = new ViolationTablePanel(conn, violationDAO);
        add(tablePanel, "cell 0 1,grow,push");
        tablePanel.refreshData();
    }

    // Add method to refresh violations
    public void refreshViolations() {
        logger.info("refreshViolations called");
        if (tablePanel != null) {
            tablePanel.refreshData();
            if (tablePanel.getTable() != null && tablePanel.getTable().getRowCount() == 0) {
                logger.warn("No violation records found in tablePanel after refreshData()");
            }
        } else {
            logger.warn("tablePanel is null in refreshViolations()");
        }
    }
}
