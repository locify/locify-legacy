/*
 * LogScreen.java
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

import com.locify.client.data.FileSystem;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * Shows log screen - only in nightly version
 * @author David Vavra
 */
public class LogScreen implements CommandListener {

    private Form form;
    private Command cmdRefresh = new Command(Locale.get("Refresh"), Command.SCREEN, 1);

    /**
     * Views the log text
     */
    public void view() {
        Logger.debug(R.getFileSystem().getFileConnectionStatus());
        try {
            form = new Form("Logger");
            form.append(Logger.getOutText());
            form.addCommand(cmdRefresh);
            form.addCommand(Commands.cmdBack);
            //#style imgHome
            form.addCommand(Commands.cmdHome);
            //#style imgSaved
            form.addCommand(Commands.cmdSave);
            //#style imgClear
            form.addCommand(Commands.cmdClear);
            form.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, form);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LogScreen.view", null);
        }
    }

    /**
     * Handles reaction to command events
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (command == Commands.cmdSave) {
            R.getFileSystem().saveString(FileSystem.LOG_FOLDER + String.valueOf(Utils.timestamp()) + ".txt", Logger.getOutText());
            R.getCustomAlert().quickView(Locale.get("Log_saved"), Locale.get("Info"), "locify://refresh");
        } else if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == cmdRefresh) {
            view();
        } else if (command == Commands.cmdClear) {
            Logger.out = "";
            Logger.debug("Log was cleared at " + Utils.getTime());
            view();
        }
    }
}
