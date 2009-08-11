/*
 * GeoFileBrowser.java
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

import com.locify.client.data.LandmarksExport;
import com.locify.client.data.items.GeoData;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.data.ServicesData;
import com.locify.client.data.items.MultiGeoData;
import com.locify.client.data.items.NetworkLink;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.gui.extension.ParentCommand;
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.screen.internal.MapScreen;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import com.locify.client.net.Http;
import com.locify.client.net.browser.XHtmlBrowser;
import com.locify.client.utils.Locale;
import com.locify.client.utils.UTF8;
import com.sun.lwuit.CheckBox;
import com.sun.lwuit.Command;
import com.sun.lwuit.Container;
import com.sun.lwuit.Dialog;
import com.sun.lwuit.Form;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BoxLayout;

/**
 * Viewer for KML files
 * @author MenKat
 */
public class GeoFileBrowser implements ActionListener {

    private FormLocify form;
    private static final int FORM_MULTI_GEO = 0;
    private static final int FORM_WAYPOINT = 1;
    private static final int FORM_ROUTE = 2;
    private static final int FORM_WAYPOINT_CLOUD = 3;
    private int actualForm;

    private Waypoint waypoint;
    private WaypointsCloud waypointCloud;
    private Route route;
    private NetworkLink networkLink;
    private MultiGeoData multiData;
    private int dataType;
    private int multiDataSource;
    private String geoData;
    private String fileName;
    private Container containerWaypoint;
    private boolean inService;
    private XHtmlBrowser htmlBrowser;
    private boolean online = false;

    public GeoFileBrowser() {
        form = new FormLocify();
        form.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        dataType = GeoFiles.TYPE_CORRUPT;
    }

    /**
     * Sets raw kml data from net
     * @param kml kml data
     * @param source source of data - from http response
     */
    public void setGeoData(String geoData, int source) {
        int type = GeoFiles.getDataTypeString(geoData);
        multiData = GeoFiles.parseGeoDataString(geoData, false);
        multiDataSource = source;
        if (multiData != null && multiData.getDataSize() > 0) {
            manageData(type);
            this.geoData = geoData;
            inService = true;
        } else {
            multiData = null;
        }
        online = true;
    }

    /**
     * Sets file with waypoint GeoData data
     * @param fileName file name
     */
    public void view(String fileName) {
        int type = GeoFiles.getDataTypeFile(fileName);
        multiData = GeoFiles.parseGeoDataFile(fileName, false);
        if (multiData != null && multiData.getDataSize() > 0) {
            manageData(type);
            this.fileName = fileName;
            inService = false;
        } else {
            multiData = null;
        }
        online = false;
        view();
    }

    private void manageData(int type) {
        if (multiData != null) {
            if (type == GeoFiles.TYPE_WAYPOINT) {
                waypoint = (Waypoint) multiData.getGeoData(GeoFiles.TYPE_WAYPOINT, 0);
                dataType = GeoFiles.TYPE_WAYPOINT;
            } else if (type == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                waypointCloud = (WaypointsCloud) multiData.getGeoData(GeoFiles.TYPE_WAYPOINTS_CLOUD, 0);
                dataType = GeoFiles.TYPE_WAYPOINTS_CLOUD;
            } else if (type == GeoFiles.TYPE_ROUTE) {
                route = (Route) multiData.getGeoData(GeoFiles.TYPE_ROUTE, 0);
                dataType = GeoFiles.TYPE_ROUTE;
            } else if (type == GeoFiles.TYPE_NETWORKLINK) {
                networkLink = (NetworkLink) multiData.getGeoData(GeoFiles.TYPE_NETWORKLINK, 0);
                dataType = GeoFiles.TYPE_NETWORKLINK;
            } else if (type == GeoFiles.TYPE_MULTI) {
                dataType = GeoFiles.TYPE_MULTI;
            }
        }
    }

    /**
     * Show user this what to do with received geoFile
     */
    public void view() {
        try {
            if (multiData != null) {
                if (dataType == GeoFiles.TYPE_NETWORKLINK) {
                    R.getMapScreen().view(networkLink);
                    //change the back screens
                    R.getBack().deleteLast();
                    R.getBack().goForward("locify://maps", null);
                    return;
                }

                if (multiDataSource==Http.NETWORKLINK_DOWNLOADER && !MapScreen.isNowDirectly())
                {
                    return; //file is from networklinkdownloader which was already stopped
                }

                if (MapScreen.isNowDirectly()) {
                    if (dataType == GeoFiles.TYPE_MULTI) {
                        R.getMapScreen().view(multiData);
                    } else if (dataType == GeoFiles.TYPE_WAYPOINT) {
                        R.getMapScreen().view(waypoint);
                    } else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                        R.getMapScreen().view(waypointCloud);
                    } else if (dataType == GeoFiles.TYPE_ROUTE) {
                        R.getMapScreen().view(route);
                    }
                    return;
                } else {
                    if (dataType == GeoFiles.TYPE_MULTI) {
                        viewDataMultiGeoFile();
                    } else if (dataType == GeoFiles.TYPE_WAYPOINT) {
                        viewDataWaypoint();
                    } else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                        viewDataWaypointCloud();
                    } else if (dataType == GeoFiles.TYPE_ROUTE) {
                        viewDataRoute();
                    }
                }
            } else {
                form.setAsNew(Locale.get("Error_occured"));
                form.addComponent(new Label(Locale.get("Wrong_file")));
                addCommands(form);
                form.show();
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFileBrowser.view()", null);
        }
    }

    private void viewDataMultiGeoFile() {
        actualForm = FORM_MULTI_GEO;
        form.setAsNew(multiData.getName());
        multiData.addItemLabels(form);
        addCommands(form);
        form.show();
    }

    private void viewDataRoute() {
        actualForm = FORM_ROUTE;
        form.setAsNew(route.getName());
        if (route.getRouteDist() > 0) {
            form.addComponent(new Label(Locale.get("Route_length") + ": " + GpsUtils.formatDistance(route.getRouteDist())));
        }
        if (route.getRouteTime() > 0) {
            form.addComponent(new Label(Locale.get("Travel_time") + ": " + GpsUtils.formatTime(route.getRouteTime())));
        }
        form.addComponent(new Label(Locale.get("Waypoints_count") + ": " + String.valueOf(route.getWaypointCount())));

        if (route.getFirstPoint() != null) {
            form.addComponent(new Label(Locale.get("First_waypoint")));
            form.addComponent(new Label(" " + Locale.get("Latitude") + ": " +
                    GpsUtils.formatLatitude(route.getFirstPoint().getLatitude())));
            form.addComponent(new Label(" " + Locale.get("Longitude") + ": " +
                    GpsUtils.formatLongitude(route.getFirstPoint().getLongitude())));
        }

        addDescription(form, route);

        addCommands(form);
        if (route.getFirstPoint() != null) {
            if (route.getWaypointCount() < 2) {
                form.addCommand(new ParentCommand(Locale.get("Navigate"), null,
                        new Command[] {Commands.cmdNavigate}));
            } else if (route.getWaypointCount() > 1) {
                form.addCommand(new ParentCommand(Locale.get("Navigate"), null,
                        new Command[] {Commands.cmdNavigate, Commands.cmdNavigateToLast, Commands.cmdNavigateAlong}));
            }
        }

        if (Capabilities.hasJSR179() && Capabilities.hasLandmarks()) {
            form.addCommand(new ParentCommand(Locale.get("Navigate"), null,
                    new Command[] {Commands.cmdNavigate, Commands.cmdNavigateToLast, Commands.cmdNavigateAlong}));
            form.addCommand(new ParentCommand(Locale.get("To_landmarks"), null,
                    new Command[] {Commands.cmdExportFirst, Commands.cmdExportLast}));
        }

        form.show();
    }

    private void viewDataWaypoint() {
        actualForm = FORM_WAYPOINT;
        form.setAsNew(waypoint.getName());

        addDescription(form, waypoint);

        form.addComponent(new Label(Locale.get("Latitude") + ": " +
                GpsUtils.formatLatitude(waypoint.getLatitude())));
        form.addComponent(new Label(Locale.get("Longitude") + ": " +
                GpsUtils.formatLongitude(waypoint.getLongitude())));

        addCommands(form);
        form.addCommand(Commands.cmdNavigate);
        if (Capabilities.hasJSR179() && Capabilities.hasLandmarks()) {
            form.addCommand(Commands.cmdExport);
        }

        if (!waypoint.getService().equals("")) {
            ServicesData.setCurrent(waypoint.getService());
        }
        form.show();
    }

    private void viewDataWaypointCloud() {
        actualForm = FORM_WAYPOINT_CLOUD;
        form.setAsNew(waypointCloud.getName());

        form.addComponent(new Label(Locale.get("Waypoints_count") + ": " + waypointCloud.getWaypointsCount()));

        containerWaypoint = new Container();
        containerWaypoint.setLayout(new BoxLayout(BoxLayout.Y_AXIS));
        for (int i = 0; i < waypointCloud.getWaypointsCount(); i++) {
            containerWaypoint.addComponent(new CheckBox(" " + waypointCloud.getWaypoint(i).getName(), null));
        }
        form.addComponent(containerWaypoint);

        addCommands(form);
        form.addCommand(Commands.cmdMapAll);
        form.show();
    }

    private void addDescription(Form form, GeoData data) {
        if (data.getDescription().length() > 0) {
            form.addComponent(new Label(Locale.get("Description")));
            
            Container container = new Container();
            htmlBrowser = new XHtmlBrowser(container);
            if (online) {
                htmlBrowser.loadPage(UTF8.decode(data.getDescription().getBytes(), 0, data.getDescription().getBytes().length));
            } else {
                htmlBrowser.loadPage(data.getDescription());
            }
            form.addComponent(container);
        }
    }

    private void addCommands(Form screen) {
        screen.addCommand(Commands.cmdBack);
        if (inService) {
            screen.addCommand(Commands.cmdSave);
        }
        screen.addCommand(Commands.cmdMap);
        screen.addCommand(Commands.cmdHome);
        screen.addCommandListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdSave) {
            GeoFiles.saveGeoFileData(geoData);
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdMap && actualForm != FORM_WAYPOINT_CLOUD) {
            R.getURL().call("locify://maps");
            if (dataType == GeoFiles.TYPE_WAYPOINT) {
                R.getMapScreen().view(waypoint);
            } else if (dataType == GeoFiles.TYPE_ROUTE) {
                if (route.isRouteOnlyInfo()) {
                    route = (Route) GeoFiles.parseGeoDataFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                }
                R.getMapScreen().view(route);
            } else if (dataType == GeoFiles.TYPE_MULTI) {
                R.getMapScreen().view(multiData);
            }
        } else {
            switch (actualForm) {
                case FORM_MULTI_GEO:
                    break;
                case FORM_ROUTE:
                    if (evt.getCommand() == Commands.cmdExportFirst || evt.getCommand() == Commands.cmdExportLast) {
                        Waypoint way;
                        if (route.isRouteOnlyInfo()) {
                            route = (Route) GeoFiles.parseGeoDataFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                        }

                        if (evt.getCommand() == Commands.cmdExportFirst) {
                            way = route.getFirstWaypoint();
                        } else {
                            way = route.getLastWaypoint();
                        }
                        LandmarksExport.export(way, "locify://geoFileBrowser");
                    } else if (evt.getCommand() == Commands.cmdNavigateToFirst || evt.getCommand() == Commands.cmdNavigateToLast) {
                        Waypoint way;
                        if (route.isRouteOnlyInfo()) {
                            route = (Route) GeoFiles.parseGeoDataFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                        }
                        if (evt.getCommand() == Commands.cmdNavigateToFirst) {
                            way = route.getFirstWaypoint();
                        } else {
                            way = route.getLastWaypoint();
                        }
                        if (way != null) {
                            R.getURL().call("locify://navigation?lat=" + way.getLatitude() + "&lon=" + way.getLongitude() + "&name=" + way.getName());
                        } else {
                            R.getCustomAlert().quickView(Locale.get("Unexpected_problem"), Dialog.TYPE_ERROR, "locify://refresh");
                        }
                    } else if (evt.getCommand() == Commands.cmdNavigateAlong) {
                        R.getURL().call("locify://navigation?file=" + fileName);
                    }
                    break;
                case FORM_WAYPOINT:
                    if (evt.getCommand() == Commands.cmdExport) {
                        LandmarksExport.export(waypoint, "locify://geoFileBrowser");
                    } else if (evt.getCommand() == Commands.cmdNavigate) {
                        R.getURL().call("locify://navigation?lat=" + waypoint.getLatitude() + "&lon=" + waypoint.getLongitude() + "&name=" + waypoint.getName());
                    }
                    break;
                case FORM_WAYPOINT_CLOUD:
                    if (evt.getCommand() == Commands.cmdNavigateToNearest) {
                        R.getURL().call("locify://navigation?" +
                                "lat=" + waypointCloud.getCenterLocation().getLatitude() + "&" +
                                "lon=" + waypointCloud.getCenterLocation().getLongitude() + "&" +
                                "name=" + waypointCloud.getName() + " - not yet function");
                    } else if (evt.getCommand() == Commands.cmdMap) {
                        WaypointsCloud mapCloud = new WaypointsCloud(waypointCloud.getName());
                        for (int i = 0; i < containerWaypoint.getComponentCount(); i++) {
                            CheckBox chb = (CheckBox) containerWaypoint.getComponentAt(i);
                            if (chb.isSelected()) {
                                mapCloud.addWaypoint(waypointCloud.getWaypoint(i));
                            }
                        }
                        R.getMapScreen().view(mapCloud);
                    } else if (evt.getCommand() == Commands.cmdMapAll) {
                        R.getMapScreen().view(waypointCloud);
                    }
                    break;
            }
        }
    }
}
