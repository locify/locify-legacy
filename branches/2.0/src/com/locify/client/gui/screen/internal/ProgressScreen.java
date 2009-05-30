/**
 * MainScreen.java
 * This file is part of Locify.
 *
 * Locify is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * (read more at: http://www.gnu.org/licenses/gpl.html)
 *
 * Commercial licenses are also available, please
 * refer http://code.google.com/p/locify/ for details.
 */

package com.locify.client.gui.screen.internal;

import com.locify.client.gui.extension.Progress;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * Shows progressbar for waiting
 * @author destil
 */
public class ProgressScreen implements ActionListener {

    private FormLocify form;
    private Progress progress;

    public ProgressScreen() {
    }
    

    public void view(String caption, String status) {
        form = new FormLocify(caption);
        form.addComponent(new Label(status));
        progress = new Progress();
        progress.infiniteRun();
        form.addComponent(progress);

        form.addCommand(Commands.cmdStop);
        form.setCommandListener(this);
        form.show();
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdStop) {
            progress.infiniteStop();
            R.getURL().call("locify://mainScreen");
        }

    }
}
