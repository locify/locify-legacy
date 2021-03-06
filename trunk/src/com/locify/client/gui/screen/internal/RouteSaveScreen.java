/*
 * LocationScreens.java
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

import com.locify.client.data.items.GeoFiles;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.utils.UTF8;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * Screen for saving unfinished route
 * @author Destil
 */
public class RouteSaveScreen implements CommandListener, ItemCommandListener {

    private Form frmSave;
    private TextField tfRouteName;
    private TextField tfRouteDescription;
    private StringItem btnOK;
    private double routeDist;
    private long routeTime;
    private int waypointsCount;
    private RouteVariables routeVariables;
    private String description;

    public RouteSaveScreen() {
        routeDist = 0.0;
        routeTime = 0L;
        waypointsCount = 0;
        description = "";
    }

    public void viewSaveScreen(RouteVariables routeVariables) {
        try {
            this.routeVariables = routeVariables;
            this.routeDist = routeVariables.getRouteDist();
            this.routeTime = routeVariables.getRouteTime();
            this.waypointsCount = routeVariables.getPointsCount();

            frmSave = new Form(Locale.get("Save_last_route"));
            tfRouteName = new TextField(Locale.get("Route_name"), null, 25, TextField.ANY);
            tfRouteDescription = new TextField(Locale.get("Route_description"), description, 1000, TextField.ANY);
            btnOK = new StringItem("", Locale.get("OK"), StringItem.BUTTON);
            btnOK.setDefaultCommand(Commands.cmdSelect);
            btnOK.setItemCommandListener(this);

            frmSave.append(tfRouteName);
            frmSave.append(tfRouteDescription);
            frmSave.append(btnOK);
            frmSave.append(new StringItem(Locale.get("Route_length"), GpsUtils.formatDistance(routeDist)));
            frmSave.append(new StringItem(Locale.get("Travel_time"), GpsUtils.formatTime(routeTime)));
            frmSave.append(new StringItem(Locale.get("Waypoints_count"), waypointsCount + ""));

            frmSave.addCommand(Commands.cmdBack);
            //#style imgHome
            frmSave.addCommand(Commands.cmdHome);
            frmSave.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, frmSave);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteSaveScreen.viewSaveScreen", null);
        }
    }

    public void viewLastSaveScreen() {
        viewSaveScreen(routeVariables);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == Commands.cmdBack) {
            //R.getBack().goBack();
            R.getURL().call("locify://recordRoute");
        } else if (command == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        }
    }

    public void commandAction(Command command, Item item) {
        if (item == btnOK) {
            if (tfRouteName.getString() != null && tfRouteName.getString().length() > 0) {
                String routeName = tfRouteName.getString();
                GeoFiles.saveRoute(GeoFiles.fileName(routeName), routeName, tfRouteDescription.getString(), routeVariables);
                
                R.getRouteScreen().routeReset();

                R.getCustomAlert().quickView(Locale.get("Route_saved"), Locale.get("Info"), "locify://refresh");
                R.getURL().call("locify://mainScreen");
            } else {
                description = tfRouteDescription.getString();
                R.getCustomAlert().quickView(Locale.get("Route_cant_be_saved"), Locale.get("Info"), "locify://saveRoute");
            }
        }
    }
}
