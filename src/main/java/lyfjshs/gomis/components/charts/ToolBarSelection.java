/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package lyfjshs.gomis.components.charts;

import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.formdev.flatlaf.FlatClientProperties;

public class ToolBarSelection<T> extends JToolBar {
    
    public ToolBarSelection(T[] data, Consumer<T> callBack) {
        putClientProperty(FlatClientProperties.STYLE, "" +
                "background:null;");
        ButtonGroup group = new ButtonGroup();
        boolean selected = false;
        for (T d : data) {
            JToggleButton button = new JToggleButton(d.toString());
            button.addActionListener(e -> callBack.accept(d));
            group.add(button);
            add(button);
            if (!selected) {
                button.setSelected(true);
                selected = true;
            }
            button.putClientProperty(FlatClientProperties.STYLE, "" +
                    "toolbar.margin:2,5,2,5;");
        }
    }
}
