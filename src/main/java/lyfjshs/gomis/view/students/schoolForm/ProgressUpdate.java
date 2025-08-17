/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.view.students.schoolForm;

public class ProgressUpdate {
    final String message;
    final int progress;
    
    ProgressUpdate(String message, int progress) {
        this.message = message;
        this.progress = progress;
    }
}