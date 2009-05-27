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

import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

/**
 * Shows progressbar for waiting
 * @author destil
 */
public class ProgressScreen implements CommandListener {

    private Form form;

    public ProgressScreen() {
    }
    

    public void view(String caption, String status) {
        form = new Form(caption);
        StringItem siSyncStatus = new StringItem("", status);
        form.append(siSyncStatus);
        Gauge gaSync = new Gauge("", false, 60, 0);
        gaSync.setMaxValue(Gauge.INDEFINITE);
        gaSync.setValue(Gauge.CONTINUOUS_RUNNING);
        form.append(gaSync);
        form.addCommand(Commands.cmdStop);
        form.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, form);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdStop)
        {
            R.getURL().call("locify://mainScreen");
        }
    }

}
