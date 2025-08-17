/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment.add;

import java.sql.Connection;

import lyfjshs.gomis.Database.entity.Student;
import lyfjshs.gomis.view.students.StudentSearchPanel;

public class ParticipantStudentSearch extends StudentSearchPanel {
    private TempParticipant selectedParticipant;

    public ParticipantStudentSearch(Connection connection) {
        super(connection, "participant_student_search");
    }

    @Override
    protected void onStudentSelected(Student student) {
        selectedParticipant = new TempParticipant(
            null, // participantId (null for new student)
            Integer.valueOf(student.getStudentUid()),
            student.getStudentFirstname(),
            student.getStudentLastname(),
            "Student",
            student.getStudentSex(),
            student.getContact() != null ? student.getContact().getContactNumber() : "", // Contact number can be fetched if needed
            true, // isStudent
            false, // isViolator (default to false)
            false // isReporter (default to false)
        );
    }

    public static TempParticipant showDialog(Connection connection) {
        ParticipantStudentSearch search = new ParticipantStudentSearch(connection);
        search.setPreferredSize(new java.awt.Dimension(600, 500));
        
        raven.modal.component.SimpleModalBorder.Option[] options = {
            new raven.modal.component.SimpleModalBorder.Option("Select", raven.modal.component.SimpleModalBorder.YES_OPTION),
            new raven.modal.component.SimpleModalBorder.Option("Cancel", raven.modal.component.SimpleModalBorder.CLOSE_OPTION)
        };

        raven.modal.ModalDialog.showModal(null,
            new raven.modal.component.SimpleModalBorder(search, "Search Student", options,
                (controller, action) -> {
                    if (action == raven.modal.component.SimpleModalBorder.YES_OPTION) {
                        if (search.getSelectedStudent() == null) {
                            javax.swing.JOptionPane.showMessageDialog(search,
                                "Please select a student first.",
                                "No Selection",
                                javax.swing.JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        controller.close();
                    } else {
                        search.selectedParticipant = null;
                        controller.close();
                    }
                }), "participant_student_search");

        return search.selectedParticipant;
    }
} 