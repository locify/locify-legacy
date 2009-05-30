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
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.sun.lwuit.Button;
import com.sun.lwuit.Label;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.TextField;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Screen for saving unfinished route
 * @author Destil
 */
public class RouteSaveScreen implements ActionListener {

    private FormLocify frmSave;
    private TextArea tfRouteName;
    private TextArea tfRouteDescription;
    private Button btnOK;
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

            frmSave = new FormLocify(Locale.get("Save_last_route"));
            frmSave.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
            tfRouteName = new TextArea(Locale.get("Route_name"), 1, 100, TextField.ANY);
            tfRouteDescription = new TextArea(Locale.get("Route_description"), 5, 1000, TextField.ANY);
            btnOK = new Button(Locale.get("OK"));
            btnOK.addActionListener(this);

            frmSave.addComponent(tfRouteName);
            frmSave.addComponent(tfRouteDescription);
            frmSave.addComponent(btnOK);
            frmSave.addComponent(new Label(Locale.get("Route_length")));
            frmSave.addComponent(new Label(GpsUtils.formatDistance(routeDist)));
            frmSave.addComponent(new Label(Locale.get("Travel_time")));
            frmSave.addComponent(new Label(GpsUtils.formatTime(routeTime)));
            frmSave.addComponent(new Label(Locale.get("Waypoints_count")));
            frmSave.addComponent(new Label("" + waypointsCount));

            frmSave.addCommand(Commands.cmdBack);
            frmSave.addCommand(Commands.cmdHome);
            frmSave.setCommandListener(this);
            frmSave.show();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteSaveScreen.viewSaveScreen", null);
        }
    }

    public void viewLastSaveScreen() {
        viewSaveScreen(routeVariables);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdBack) {
            //R.getBack().goBack();
            R.getURL().call("locify://recordRoute");
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getSource() == btnOK) {
            if (tfRouteName.getText() != null && tfRouteName.getText().length() > 0) {
                String routeName = tfRouteName.getText();
                GeoFiles.saveRoute(GeoFiles.fileName(routeName), routeName, tfRouteDescription.getText(), routeVariables);

                R.getRouteScreen().routeReset();

                R.getCustomAlert().quickView(Locale.get("Route_saved"), Locale.get("Info"), "locify://refresh");
                R.getURL().call("locify://mainScreen");
            } else {
                description = tfRouteDescription.getText();
                R.getCustomAlert().quickView(Locale.get("Route_cant_be_saved"), Locale.get("Info"), "locify://saveRoute");
            }
        }

    }
}
