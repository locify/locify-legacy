/*
 * UpdateScreen.java
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
import com.locify.client.net.Http;
import de.enough.polish.util.Locale;
import de.enough.polish.ui.Command; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.CommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Displayable; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Form; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.Item; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.ItemCommandListener; import de.enough.polish.ui.StyleSheet;
import de.enough.polish.ui.StringItem; import de.enough.polish.ui.StyleSheet;

/**
 * Manages interface of update Locify
 * @author Destil
 */
public class UpdateScreen extends Form implements CommandListener, ItemCommandListener {
    
    private StringItem btnDownload;
    private static final String LATEST_VERSION_URL = "http://www.locify.com/download/Locify.jad";
    private static final String UPDATE_URL = "http://www.locify.com/update";
    
    public UpdateScreen() {
        super(Locale.get(19));
    }
    
    public void view(String versionInfo)
    {
        this.deleteAll();
        StringItem siCurrentVersion = new StringItem(Locale.get(33), R.getMidlet().getAppProperty("MIDlet-Version"));
        this.append(siCurrentVersion);
        StringItem siLatestVersion = new StringItem(Locale.get(72), versionInfo);
        this.append(siLatestVersion);
        btnDownload = new StringItem("", Locale.get(44), StringItem.BUTTON);
        btnDownload.setDefaultCommand(Commands.cmdSend);
        btnDownload.setItemCommandListener(this);
        this.append(btnDownload);
        this.addCommand(Commands.cmdBack);
        //#style imgHome
        this.addCommand(Commands.cmdHome, de.enough.polish.ui.StyleSheet.imghomeStyle );
        this.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, this);
    }
    
    /**
     * Launches update check
     */
    public void checkVersion()
    {
        R.getHttp().start(UPDATE_URL,Http.UPDATER);
    }

    public void commandAction(Command command, Displayable displayable) {
       if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (command == Commands.cmdBack) {
            R.getBack().goBack();
        }
    }

    public void commandAction(Command command, Item item) {
        if (item == btnDownload)
        {
            R.getExternalBrowser().open(LATEST_VERSION_URL+"?user="+R.getSettings().getName()+"&version="+R.getMidlet().getAppProperty("MIDlet-Version"));
        }
    }

}
