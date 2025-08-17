/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.appointment.add;

import java.sql.Connection;

import lyfjshs.gomis.view.students.StudentSearchPanel;

public class AppointmentStudentSearch extends StudentSearchPanel {
    public AppointmentStudentSearch(Connection connection, StudentSelectionCallback callback) {
        super(connection, callback, "appointment_student_search");
    }
} 