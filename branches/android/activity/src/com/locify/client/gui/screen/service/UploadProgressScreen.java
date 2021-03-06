/*
 * UploadProgressScreen.java
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

package com.locify.client.gui.screen.service;

import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Gauge; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;


/**
 * Shows upload progress
 * @author Destil
 */
public class UploadProgressScreen implements CommandListener {
    private Form form;
    private int fileSize; // in kB
    private Gauge gauge;
    private StringItem siProgress;
    private Command cmdStop = new Command(Locale.get(141), Command.STOP, 1);

    /**
     * Views the screen with some file size
     * @param fileSize
     */
    public void view(int fileSize)
    {
        this.fileSize = (int)fileSize;
        form = new Form(Locale.get(450));
        StringItem siDummy = new StringItem("","");
        siDummy.setLayout(StringItem.LAYOUT_NEWLINE_AFTER);
        form.append(siDummy);
        gauge = new Gauge("", false, fileSize, 0);
        form.append(gauge);
        siProgress = new StringItem("","0/"+fileSize+" kB");
        siProgress.setLayout(StringItem.LAYOUT_CENTER);
        form.append(siProgress);
        form.addCommand(cmdStop);
        form.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, form);
    }

    public void update(long newSize)
    {
        gauge.setValue((int)newSize);
        siProgress.setText(newSize+"/"+this.fileSize+" kB");
    }

    public void complete()
    {
        siProgress.setText(Locale.get(451));
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdStop)
        {
            R.getURL().call("locify://htmlBrowser");
        }
    }
}
