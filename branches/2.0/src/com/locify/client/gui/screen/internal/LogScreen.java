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
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.Utils;
import com.sun.lwuit.Command;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;

/**
 * Shows log screen - only in nightly version
 * @author David Vavra
 */
public class LogScreen implements ActionListener {

    private FormLocify form;
    private Command cmdRefresh = new Command(Locale.get("Refresh"), 1);

    /**
     * Views the log text
     */
    public void view() {
        try {
            form = new FormLocify("Logger");
            form.addComponent(new Label(Logger.getOutText()));
            form.addCommand(cmdRefresh);
            form.addCommand(Commands.cmdBack);
            form.addCommand(Commands.cmdHome);
            form.addCommand(Commands.cmdSave);
            form.setCommandListener(this);
            form.show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "LogScreen.view", null);
        }
    }

    /**
     * Handles reaction to command events
     * @param command
     * @param displayable
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdSave) {
            R.getFileSystem().saveString(FileSystem.LOG_FOLDER + String.valueOf(Utils.timestamp()) + ".txt", Logger.getOutText());
            R.getCustomAlert().quickView(Locale.get("Log_saved"), Locale.get("Info"), "locify://refresh");
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == cmdRefresh) {
            view();
        }
    }
}
