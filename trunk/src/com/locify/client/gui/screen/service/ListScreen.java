/*
 * ListScreen.java
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

import com.locify.client.data.ServicesData;
import com.locify.client.data.IconData;
import com.locify.client.utils.Commands;
import java.util.Vector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import de.enough.polish.ui.List;
import com.locify.client.utils.R;
import de.enough.polish.ui.UiAccess;

/**
 * Shows list according to HTML page
 * @author David Vavra
 */
public class ListScreen implements CommandListener {

    private List list;
    private String title;
    private Vector menuItems;

    public ListScreen() {
        menuItems = new Vector();
    }

    /**
     * Sets title
     * @param tit
     */
    public void setTitle(String tit) {
        menuItems = new Vector();
        title = tit;
        //tvorba listu
        list = new List(title, Choice.IMPLICIT);
    }

    /**
     * Adds menuItem to Vector
     * @param title
     * @param url
     * @param icon
     */
    public void addMenuItem(String title, String url, String icon) {
        if (icon == null) {
            list.append(title, null);
        } else {
            list.append(title, IconData.get(icon));
        }
        menuItems.addElement(new MenuItem(title.trim(), url, icon));
    }

    /**
     * Refreshes icon after icon is downloaded
     * @param iconUrl
     * @param data image data
     */
    public void refreshIcon(String iconUrl, byte[] data) {
        try {
            for (int i = 0; i < menuItems.size(); i++) {
                MenuItem item = (MenuItem) menuItems.elementAt(i);
                if (item.icon.equals(iconUrl)) {
                    list.getItem(i).setImage(Image.createImage(data, 0, data.length));
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ListScreen.refreshIcon", iconUrl);
        }
    }

    /**
     * Creates and views the screen
     * @param url 
     */
    public void view(String url) {
        try {
            if (!ServicesData.getCurrent().equals("Locify")) {
                //#style imgManage
                list.addCommand(R.getMainScreen().cmdService);
                //#style imgInfo
                UiAccess.addSubCommand(R.getMainScreen().cmdMoreInfo, R.getMainScreen().cmdService, list);
                if (!"".equals(ServicesData.getService(ServicesData.getCurrent()).getSettingsUrl())) {
                    //#style imgServiceSettings
                    UiAccess.addSubCommand(R.getMainScreen().cmdServiceSettings, R.getMainScreen().cmdService, list);
                }
                //#style imgUpdateService
                UiAccess.addSubCommand(R.getMainScreen().cmdUpdateService, R.getMainScreen().cmdService, list);
            }
            list.addCommand(Commands.cmdBack);
            //#style imgHome
            list.addCommand(Commands.cmdHome);
            R.getMidlet().switchDisplayable(null, list);
            list.setCommandListener(this);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ListScreen.view", url);
        }
    }

    /**
     * Handles reaction to command events
     * @param command
     * @param displayable
     */
    public void commandAction(Command command, Displayable displayable) {
        if (displayable == list) {
            if (command == List.SELECT_COMMAND) {
                MenuItem item = (MenuItem) menuItems.elementAt(list.getSelectedIndex());
                R.getURL().call(item.url);
            } else if (command == Commands.cmdBack) {
                R.getBack().goBack();
            } else if (command == Commands.cmdHome) {
                R.getURL().call("locify://mainScreen");
            } else if (command == R.getMainScreen().cmdMoreInfo) {
                R.getMainScreen().commandAction(R.getMainScreen().cmdMoreInfo, list);
            } else if (command == R.getMainScreen().cmdServiceSettings) {
                R.getMainScreen().commandAction(R.getMainScreen().cmdServiceSettings, list);
            } else if (command == R.getMainScreen().cmdUpdateService) {
                R.getMainScreen().commandAction(R.getMainScreen().cmdUpdateService, list);
            }
        }
    }
}

/**
 * Object for storing menuItems
 * @author David Vavra
 */
class MenuItem {

    public String title;
    public String url;
    public String icon;

    public MenuItem(String t, String u, String i) {
        title = t;
        url = u;
        icon = i;
    }
}
