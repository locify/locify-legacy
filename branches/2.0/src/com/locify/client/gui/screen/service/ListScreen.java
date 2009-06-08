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
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Command;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import java.util.Vector;


/**
 * Shows list according to HTML page
 * @author David Vavra
 */
public class ListScreen implements ActionListener {

    private FormLocify form;
    private ListLocify list;
    private Vector menuItems;

    public ListScreen() {
        menuItems = new Vector();
    }

    /**
     * Sets title
     * @param tit
     */
    public void setTitle(String tit) {
        if (form == null) {
            form = new FormLocify(tit);
            list = new ListLocify();
            menuItems = new Vector();
        } else {
            form.setAsNew(tit);
            list.removeAll();
            menuItems.removeAllElements();
        }
        form.setLayout(new BorderLayout());
        form.addComponent(BorderLayout.CENTER, list);
    }

    /**
     * Adds menuItem to Vector
     * @param title
     * @param url
     * @param icon
     */
    public void addMenuItem(String title, String url, String icon) {
        url = R.getHttp().makeAbsoluteURL(url);
        if (icon == null) {
            list.addItem(new Label(title));
        } else {
            Label lab = new Label(title);
            lab.setIcon(IconData.get(icon));
            list.addItem(lab);
        }
//System.out.println("AddElement: " + title.trim() + " " + url + " " + icon);
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
                    ((Label) list.getItemAt(i)).setIcon(Image.createImage(data, 0, data.length));
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
                if (!"".equals(ServicesData.getService(ServicesData.getCurrent()).getSettingsUrl())) {
                    form.addCommand(new ParentCommand(Locale.get("Service"), null,
                            new Command[] {Commands.cmdMoreInfo, Commands.cmdServiceSettings, Commands.cmdUpdateService}));
                } else {
                    form.addCommand(new ParentCommand(Locale.get("Service"), null,
                            new Command[] {Commands.cmdMoreInfo, Commands.cmdUpdateService}));
                }
            }
            
            list.addActionListener(this);
            form.addCommand(Commands.cmdBack);
            form.addCommand(Commands.cmdHome);
            form.setCommandListener(this);
            form.show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "ListScreen.view", url);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == list) {
            MenuItem item = (MenuItem) menuItems.elementAt(list.getSelectedIndex());
            R.getURL().call(item.url);
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdMoreInfo) {
            R.getMainScreen().actionCommand(Commands.cmdMoreInfo);
        } else if (evt.getCommand() == Commands.cmdServiceSettings) {
            R.getMainScreen().actionCommand(Commands.cmdServiceSettings);
        } else if (evt.getCommand() == Commands.cmdUpdateService) {
            R.getMainScreen().actionCommand(Commands.cmdUpdateService);
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
