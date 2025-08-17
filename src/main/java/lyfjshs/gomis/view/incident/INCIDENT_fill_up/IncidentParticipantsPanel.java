/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.components.DropPanel;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.view.appointment.add.AppointmentStudentSearch;
import lyfjshs.gomis.view.appointment.add.NonStudentPanel;

public class IncidentParticipantsPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(IncidentParticipantsPanel.class);
    private DropPanel studentDropPanel;
    private DropPanel nonStudentDropPanel;
    private GTable participantsTable;
    private List<TempIncidentParticipant> participants;
    private Connection connection;
    private DefaultTableActionManager actionManager;

    public IncidentParticipantsPanel(Connection connection) {
        this.connection = connection;
        this.participants = new ArrayList<>();
        setOpaque(false);
        setLayout(new net.miginfocom.swing.MigLayout("insets 5", "[grow]", "[][][][grow]"));
        initComponents();
    }

    private void initComponents() {
        // Button Panel
        JPanel buttonsPanel = new JPanel(new net.miginfocom.swing.MigLayout("insets 0", "[grow][grow]", "[]"));
        JButton addStudentBtn = new JButton("Add Student");
        JButton addNonStudentBtn = new JButton("Add Non-Student");
        buttonsPanel.add(addStudentBtn, "growx");
        buttonsPanel.add(addNonStudentBtn, "growx");
        add(buttonsPanel, "cell 0 0, growx");

        // DropPanels
        studentDropPanel = new DropPanel();
        nonStudentDropPanel = new DropPanel();
        add(studentDropPanel, "cell 0 1, growx");
        add(nonStudentDropPanel, "cell 0 2, growx");

        // Student search panel
        AppointmentStudentSearch studentSearchPanel = new AppointmentStudentSearch(connection, student -> {
            if (student != null) {
                TempIncidentParticipant participant = new TempIncidentParticipant(
                    null,
                    student.getStudentUid(),
                    student.getStudentFirstname(),
                    student.getStudentLastname(),
                    "Student",
                    student.getStudentSex(),
                    student.getContact() != null ? student.getContact().getContactNumber() : "",
                    true // isStudent
                );
                addParticipant(participant);
                studentDropPanel.setDropdownVisible(false);
            }
        });
        studentDropPanel.setContent(studentSearchPanel);

        // Non-student panel
        NonStudentPanel nonStudentPanel = new NonStudentPanel();
        nonStudentPanel.setNonStudentListener(participant -> {
            TempIncidentParticipant newP = new TempIncidentParticipant(
                null,
                participant.getStudentUid(),
                participant.getFirstName(),
                participant.getLastName(),
                participant.getType(),
                participant.getSex(),
                participant.getContactNumber(),
                false // isStudent
            );
            addParticipant(newP);
            nonStudentDropPanel.setDropdownVisible(false);
        });
        nonStudentDropPanel.setContent(nonStudentPanel);

        // Button actions
        addStudentBtn.addActionListener(e -> {
            if (!studentDropPanel.isDropdownVisible()) {
                studentDropPanel.setDropdownVisible(true);
                nonStudentDropPanel.setDropdownVisible(false);
            } else {
                studentDropPanel.setDropdownVisible(false);
            }
        });
        addNonStudentBtn.addActionListener(e -> {
            if (!nonStudentDropPanel.isDropdownVisible()) {
                nonStudentDropPanel.setDropdownVisible(true);
                studentDropPanel.setDropdownVisible(false);
            } else {
                nonStudentDropPanel.setDropdownVisible(false);
            }
        });

        // Table setup
        setupParticipantsTable();
        add(new JScrollPane(participantsTable), "cell 0 3, grow");
    }

    public void setParticipants(List<TempIncidentParticipant> participants) {
        this.participants = participants;
        updateParticipantsTable();
    }

    public List<TempIncidentParticipant> getParticipants() {
        return participants;
    }

    public DropPanel getStudentDropPanel() { return studentDropPanel; }
    public DropPanel getNonStudentDropPanel() { return nonStudentDropPanel; }

    public void clearFields() {
        participants.clear();
        updateParticipantsTable();
    }

    public boolean isValidPanel() {
        return !participants.isEmpty();
    }

    public void addParticipant(TempIncidentParticipant participant) {
        logger.info("Adding participant: {}", participant);
        participants.add(participant);
        updateParticipantsTable();
    }

    private void setupParticipantsTable() {
        String[] columnNames = {"#", "Name", "Type", "Actions"};
        Class<?>[] columnTypes = {Integer.class, String.class, String.class, Object.class};
        boolean[] editableColumns = {false, false, false, true};
        double[] columnWidths = {0.08, 0.50, 0.22, 0.20};
        int[] alignments = {javax.swing.SwingConstants.CENTER, javax.swing.SwingConstants.LEFT, javax.swing.SwingConstants.LEFT, javax.swing.SwingConstants.CENTER};
        actionManager = new DefaultTableActionManager();
        actionManager.addAction("View", (table, row) -> viewParticipant(row), new java.awt.Color(0, 123, 255), null);
        actionManager.addAction("Remove", (table, row) -> removeParticipant(row), new java.awt.Color(0xdc3545), null);
        participantsTable = new GTable(new Object[0][4], columnNames, columnTypes, editableColumns, columnWidths, alignments, false, null);
        participantsTable.setRowHeight(40);
        actionManager.setupTableColumn(participantsTable, 3);
        updateParticipantsTable();
    }

    private void updateParticipantsTable() {
        Object[][] data = new Object[participants.size()][4];
        for (int i = 0; i < participants.size(); i++) {
            TempIncidentParticipant participant = participants.get(i);
            data[i][0] = i + 1;
            data[i][1] = participant.getFullName();
            data[i][2] = participant.getType();
            data[i][3] = "actions";
        }
        participantsTable.setData(data);
    }

    private void viewParticipant(int row) {
        if (row >= 0 && row < participants.size()) {
            TempIncidentParticipant p = participants.get(row);
            StringBuilder sb = new StringBuilder();
            sb.append("Name: ").append(p.getFullName()).append("\n");
            sb.append("Type: ").append(p.getType()).append("\n");
            sb.append("Sex: ").append(p.getSex()).append("\n");
            sb.append("Contact: ").append(p.getContactNumber()).append("\n");
            if (p.isStudent() && p.getStudentUid() != null) {
                sb.append("Student UID: ").append(p.getStudentUid()).append("\n");
            }
            javax.swing.JOptionPane.showMessageDialog(this, sb.toString(), "Participant Details", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void removeParticipant(int row) {
        // Stop cell editing to prevent ArrayIndexOutOfBoundsException
        if (participantsTable.isEditing()) {
            participantsTable.getCellEditor().stopCellEditing();
        }
        if (row >= 0 && row < participants.size()) {
            participants.remove(row);
            updateParticipantsTable();
        }
    }
} 