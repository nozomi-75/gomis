/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.sessions.fill_up;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.components.table.DefaultTableActionManager;
import lyfjshs.gomis.components.table.GTable;
import lyfjshs.gomis.view.appointment.add.AppointmentStudentSearch;
import lyfjshs.gomis.view.appointment.add.NonStudentPanel;
import lyfjshs.gomis.view.sessions.TempParticipant;
import net.miginfocom.swing.MigLayout;

public class ParticipantsPanel extends JPanel {
    private static final Logger logger = LogManager.getLogger(ParticipantsPanel.class);
    private JPanel studentPanelContainer;
    private JPanel nonStudentPanelContainer;
    private GTable participantsTable;
    private final List<TempParticipant> tempParticipants = new ArrayList<>();
    private final Connection conn;

    public ParticipantsPanel(Connection connect) {
        this.conn = connect;
        setOpaque(false);
        setLayout(new BorderLayout());
        JPanel content = new JPanel(new MigLayout("insets 5", "[grow]", "[][][][grow]"));
        content.setOpaque(false);
        add(content, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonsPanel = new JPanel(new MigLayout("insets 0", "[grow][grow]", "[]"));
        JButton addStudentBtn = new JButton("Add Student");
        JButton addNonStudentBtn = new JButton("Add Non-Student");
        buttonsPanel.add(addStudentBtn, "growx");
        buttonsPanel.add(addNonStudentBtn, "growx");
        content.add(buttonsPanel, "cell 0 0, growx");

        // DropPanels
        studentPanelContainer = new JPanel(new BorderLayout());
        nonStudentPanelContainer = new JPanel(new BorderLayout());

        // Student search panel
        AppointmentStudentSearch studentSearchPanel = new AppointmentStudentSearch(conn, student -> {
            if (student != null) {
                addStudentParticipant(student);
            }
        });
        studentPanelContainer.add(studentSearchPanel, BorderLayout.CENTER);
        studentPanelContainer.setVisible(false);

        // Non-student panel
        NonStudentPanel nonStudentPanel = new NonStudentPanel();
        nonStudentPanel.setNonStudentListener(participant -> {
            lyfjshs.gomis.view.sessions.TempParticipant newP = new lyfjshs.gomis.view.sessions.TempParticipant(
                null,
                participant.getStudentUid(),
                participant.getFirstName(),
                participant.getLastName(),
                participant.getType(),
                participant.getSex(),
                participant.getContactNumber(),
                false, // isStudent
                false, // isViolator
                false  // isReporter
            );
            addNonStudentParticipant(newP);
        });
        nonStudentPanelContainer.add(nonStudentPanel, BorderLayout.CENTER);
        nonStudentPanelContainer.setVisible(false);

        content.add(studentPanelContainer, "cell 0 1, growx");
        content.add(nonStudentPanelContainer, "cell 0 2, growx");

        // Button actions
        addStudentBtn.addActionListener(e -> {
            boolean currentlyVisible = studentPanelContainer.isVisible();
            studentPanelContainer.setVisible(!currentlyVisible);
        });
        addNonStudentBtn.addActionListener(e -> {
            boolean currentlyVisible = nonStudentPanelContainer.isVisible();
            nonStudentPanelContainer.setVisible(!currentlyVisible);
        });

        // Table setup
        setupParticipantsTable();
        content.add(new JScrollPane(participantsTable), "cell 0 3, grow");
    }

    private void setupParticipantsTable() {
        String[] columnNames = {"#", "Name", "Type", "Violator", "Reporter", "Actions"};
        Class<?>[] columnTypes = {Integer.class, String.class, String.class, Boolean.class, Boolean.class, Object.class};
        boolean[] editableColumns = {false, false, false, true, true, true};
        double[] columnWidths = {0.05, 0.30, 0.20, 0.10, 0.10, 0.15};
        int[] alignments = {SwingConstants.CENTER, SwingConstants.LEFT, SwingConstants.LEFT, SwingConstants.CENTER, SwingConstants.CENTER, SwingConstants.CENTER};
        DefaultTableActionManager actionManager = new DefaultTableActionManager();
        actionManager.addAction("Remove", (table, row) -> removeParticipant(row), new Color(0xdc3545), null);
        participantsTable = new GTable(new Object[0][6], columnNames, columnTypes, editableColumns, columnWidths, alignments, false, null);
        participantsTable.setRowHeight(40);
        actionManager.setupTableColumn(participantsTable, 5);
        participantsTable.getModel().addTableModelListener(e -> {
            int column = e.getColumn();
            int row = e.getFirstRow();
            if (row < 0 || row >= tempParticipants.size()) return;
            TempParticipant p = tempParticipants.get(row);
            if (column == 3) p.setViolator((Boolean) participantsTable.getValueAt(row, column));
            else if (column == 4) p.setReporter((Boolean) participantsTable.getValueAt(row, column));
        });
        updateParticipantsTable();
    }

    private void updateParticipantsTable() {
        Object[][] data = new Object[tempParticipants.size()][6];
        for (int i = 0; i < tempParticipants.size(); i++) {
            TempParticipant participant = tempParticipants.get(i);
            data[i][0] = i + 1;
            data[i][1] = participant.getFullName() != null ? participant.getFullName() : "";
            data[i][2] = participant.getType() != null ? participant.getType() : "";
            data[i][3] = participant.isViolator();
            data[i][4] = participant.isReporter();
            data[i][5] = "actions";
        }
        participantsTable.setData(data);
    }

    public void addStudentParticipant(Student student) {
        TempParticipant participant = new TempParticipant(null,
                student.getStudentUid(),
                student.getStudentFirstname(),
                student.getStudentLastname(),
                "Student",
                student.getStudentSex(),
                student.getContact() != null ? student.getContact().getContactNumber() : "",
                true,
                false,
                false);
        boolean isDuplicate = tempParticipants.stream().anyMatch(p ->
            (p.isStudent() && p.getStudentUid() != null && p.getStudentUid().equals(participant.getStudentUid())) ||
            (!p.isStudent() && p.getFullName().equalsIgnoreCase(participant.getFullName()))
        );
        if (!isDuplicate) {
            tempParticipants.add(participant);
            updateParticipantsTable();
        } else {
            JOptionPane.showMessageDialog(this,
                "This participant is already added to the session.",
                "Duplicate Entry",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    public void addNonStudentParticipant(TempParticipant participant) {
        boolean isDuplicate = tempParticipants.stream().anyMatch(p ->
            (!p.isStudent() && p.getFullName().equalsIgnoreCase(participant.getFullName()))
        );
        if (!isDuplicate) {
            tempParticipants.add(participant);
            updateParticipantsTable();
        } else {
            JOptionPane.showMessageDialog(this,
                "This participant is already added to the session.",
                "Duplicate Entry",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removeParticipant(int row) {
        if (row >= 0 && row < tempParticipants.size()) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove this participant?", "Confirm Remove", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tempParticipants.remove(row);
                updateParticipantsTable();
            }
        }
    }

    public List<TempParticipant> getParticipants() {
        return new ArrayList<>(tempParticipants);
    }

    public void setParticipants(List<TempParticipant> list) {
        tempParticipants.clear();
        if (list != null) tempParticipants.addAll(list);
        updateParticipantsTable();
    }

    public void clearParticipants() {
        tempParticipants.clear();
        updateParticipantsTable();
    }

    public void updateParticipant(int index, TempParticipant updated) {
        if (index >= 0 && index < tempParticipants.size()) {
            tempParticipants.set(index, updated);
            updateParticipantsTable();
        }
    }

    public TempParticipant getParticipantById(int id) {
        for (TempParticipant p : tempParticipants) {
            if (p.getParticipantId() != null && p.getParticipantId() == id) return p;
        }
        return null;
    }

    public GTable getParticipantsTable() { return participantsTable; }
    public JPanel getStudentPanelContainer() { return studentPanelContainer; }
    public JPanel getNonStudentPanelContainer() { return nonStudentPanelContainer; }
    public JPanel getContentPanel() { return this; }
}