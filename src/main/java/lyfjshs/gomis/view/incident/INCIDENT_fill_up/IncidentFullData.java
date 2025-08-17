/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.incident.INCIDENT_fill_up;

import java.util.ArrayList;
import java.util.List;

public class IncidentFullData {
    public String reportedBy;
    public String gradeSection;
    public java.time.LocalDate date;
    public java.time.LocalTime time;
    public String status;
    public String narrative;
    public String actions;
    public String recommendations;
    public List<TempIncidentParticipant> participants = new ArrayList<>();
}