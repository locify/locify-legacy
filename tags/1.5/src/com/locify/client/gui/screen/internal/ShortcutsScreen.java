/*
 * Shortcuts.java
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

import com.locify.client.data.IconData;
import com.locify.client.gui.manager.InternalURL;
import com.locify.client.utils.Commands;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import java.util.Vector;

/**
 * Screen for listing available shortcuts. They are defined in InternalUrlManager
 * @author David Vavra
 */
public class ShortcutsScreen extends List implements CommandListener {

    public ShortcutsScreen() {
        super(Locale.get("Add_shortcut"), List.IMPLICIT);
    }

    public void view() {
        this.deleteAll();
        this.addCommand(Commands.cmdBack);
        //#style imgHome
        this.addCommand(Commands.cmdHome);
        this.setCommandListener(this);
        Vector shortcuts = R.getURL().getShortcuts();
        for (int i = 0; i < shortcuts.size(); i++) {
            InternalURL shortcut = (InternalURL) shortcuts.elementAt(i);
            if (shortcut.isShortcut()) {
                this.append(shortcut.getTitle(), IconData.get("locify://icons/" + shortcut.getIcon()));
            }
        }
        R.getMidlet().switchDisplayable(null, this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (c == List.SELECT_COMMAND) {
            InternalURL shortcut = R.getURL().getItemAt(this.getSelectedIndex());
            R.getMainScreen().addEdit(shortcut.getUrl(), shortcut.getTitle(), "locify://icons/" + shortcut.getIcon());
            R.getURL().call("locify://mainScreen");
        } else if (c == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        }
    }
}
