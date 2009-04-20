/*
 * ExternalBrowser.java
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
package com.locify.client.net;

import com.locify.client.data.SettingsData;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * Manages opening external browser
 * @author Destil
 */
public class ExternalBrowser implements CommandListener {

    private Form frmExternalClose;
    private Command cmdAlways = new Command(Locale.get("Always"), Command.SCREEN, 10);
    private Command cmdNever = new Command(Locale.get("Never"), Command.SCREEN, 11);

    public ExternalBrowser() {
    }

    /**
     * Opens given URL in phone's external browser
     * @param url url to be viewed
     */
    public void open(String url) {
        try {
            url = R.getHttp().makeAbsoluteURL(url);
            if (!url.startsWith("http://")) {
                url = "http://" + url;
            }
            Logger.log("Opening external browser: " + url);
            
            //pripadne zavreni klienta
            if (R.getSettings().getExternalClose() == SettingsData.ALWAYS) {
                R.getMidlet().platformRequest(url);
                R.getMidlet().exitMIDlet();
            }
            else if (R.getSettings().getExternalClose() == SettingsData.NEVER)
            {
                R.getMidlet().platformRequest(url);
            } else if (R.getSettings().getExternalClose() == SettingsData.ASK) {
                R.getURL().call("locify://externalBrowserOptions");
                R.getMidlet().platformRequest(url);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ExternalBrowser.open", url);
        }
    }

    public void viewCloseAppScreen() {
        frmExternalClose = new Form(Locale.get("External_browser_launch"));
        frmExternalClose.append(Locale.get("External_closing_information"));
        frmExternalClose.addCommand(Commands.cmdYes);
        frmExternalClose.addCommand(Commands.cmdNo);
        frmExternalClose.addCommand(cmdAlways);
        frmExternalClose.addCommand(cmdNever);
        //#style imgHome
        frmExternalClose.addCommand(Commands.cmdHome);
        frmExternalClose.setCommandListener(this);
        R.getMidlet().switchDisplayable(null, frmExternalClose);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == cmdAlways) {
            R.getSettings().setExternalClose(SettingsData.ALWAYS);
            R.getURL().call("locify://exit");
        } else if (command == cmdNever) {
            R.getSettings().setExternalClose(SettingsData.NEVER);
            R.getBack().goBack(2);
        } else if (command == Commands.cmdNo) {
            R.getBack().goBack(2);
        } else if (command == Commands.cmdYes) {
            R.getURL().call("locify://exit");
        } else if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        }
    }
}
