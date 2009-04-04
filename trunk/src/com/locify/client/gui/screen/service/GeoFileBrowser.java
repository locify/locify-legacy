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
import com.locify.client.data.items.MultiGeoData;
import com.locify.client.data.items.NetworkLink;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import de.enough.polish.ui.ChoiceGroup;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.Screen;
import de.enough.polish.util.Locale;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.StringItem;

/**
 * Viewer for KML files
 * @author MenKat
 */
public class GeoFileBrowser implements CommandListener {

    /* basic datas */
    private Waypoint waypoint;
    private WaypointsCloud waypointCloud;
    private Route route;
    private NetworkLink networkLink;
    private MultiGeoData multiData;
    
    private int dataType;
    private String kmlData;
    private String fileName;
    private Form formMultiGeoData;
    private Form formRoute;
    private Form formWaypoint;
    private Form formWaypointCloud;
    private ChoiceGroup waypointCloudItems;
    private boolean inService;
    private Command cmdMap;
    private Command cmdMapAll;
    private Command cmdExport;
    private Command cmdNavigateToFirst;
    private Command cmdNavigateToLast;
    private Command cmdNavigateAlong;
    private Command cmdNavigateToNearest;
    private Command cmdExportFirst;
    private Command cmdExportLast;

    public GeoFileBrowser() {
        dataType = GeoFiles.TYPE_CORRUPT;
        cmdMap = new Command(Locale.get("Show_on_map"), Command.SCREEN, 10);
        cmdMapAll = new Command(Locale.get("Show_on_map_all"), Command.SCREEN, 11);
        cmdExport = new Command(Locale.get("To_landmarks"), Command.SCREEN, 20);
        cmdNavigateToFirst = new Command(Locale.get("Navigate_to_first_point"), Command.SCREEN, 31);
        cmdNavigateToLast = new Command(Locale.get("Navigate_to_last_point"), Command.SCREEN, 32);
        cmdNavigateAlong = new Command(Locale.get("Navigate_along"), Command.SCREEN, 30);
        cmdNavigateToNearest = new Command(Locale.get("Navigate_to_nearest"), Command.SCREEN, 33);
        cmdExportFirst = new Command(Locale.get("First_point"), Command.SCREEN, 21);
        cmdExportLast = new Command(Locale.get("Last_point"), Command.SCREEN, 22);
    }

    /**
     * Sets raw kml data from net
     * @param kml kml data
     */
    public void setKml(String kml) {
        kml = GeoFiles.correctStringData(kml);
        int type = GeoFiles.getDataTypeString(kml);
        multiData = GeoFiles.parseKmlString(kml, false);
        if (multiData != null && multiData.getDataSize() > 0) {
            manageData(type);
            kmlData = kml;
            inService = true;
        } else
            multiData = null;
    }

    /**
     * Sets file with waypoint kml data
     * @param fileName file name
     */
    public void view(String fileName) {
        int type = GeoFiles.getDataTypeFile(fileName);
        multiData = GeoFiles.parseKmlFile(fileName, false);
        if (multiData != null && multiData.getDataSize() > 0) {
            manageData(type);
            this.fileName = fileName;
            inService = false;
        } else
            multiData = null;
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
                route =  (Route) multiData.getGeoData(GeoFiles.TYPE_ROUTE, 0);
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
                if (dataType == GeoFiles.TYPE_NETWORKLINK)
                    R.getMapScreen().view(networkLink);

                if (R.getMapScreen().isNowDirectly()) {
                    if (dataType == GeoFiles.TYPE_MULTI)
                        R.getMapScreen().view(multiData);
                    else if (dataType == GeoFiles.TYPE_WAYPOINT)
                        R.getMapScreen().view(waypoint);
                    else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD)
                        R.getMapScreen().view(waypointCloud);
                    else if (dataType == GeoFiles.TYPE_ROUTE)
                        R.getMapScreen().view(route);
                    return;
                } else {
                    if (dataType == GeoFiles.TYPE_MULTI)
                        viewDataMultiGeoFile();
                    else if (dataType == GeoFiles.TYPE_WAYPOINT)
                        viewDataWaypoint();
                    else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD)
                        viewDataWaypointCloud();
                    else if (dataType == GeoFiles.TYPE_ROUTE)
                        viewDataRoute();
                }
            } else {
                formWaypoint = new Form(Locale.get("Error_occured"));
                formWaypoint.append(new StringItem(Locale.get("Wrong_file"), ""));
                addCommands(formWaypoint);
                R.getMidlet().switchDisplayable(null, formWaypoint);
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFileBrowser.view()", null);
        }
    }
    
    private void viewDataMultiGeoFile() {
        formMultiGeoData = new Form(multiData.getName());
        multiData.addItemLabels(formMultiGeoData);

        addCommands(formMultiGeoData);
        R.getMidlet().switchDisplayable(null, formMultiGeoData);
    }
    
    private void viewDataRoute() {
        formRoute = new Form(route.getName());
        formRoute.append(new StringItem(Locale.get("Route_length") + " ", GpsUtils.formatDistance(route.getRouteDist())));
        formRoute.append(new StringItem(Locale.get("Travel_time") + " ", GpsUtils.formatTime(route.getRouteTime())));
        formRoute.append(new StringItem(Locale.get("Waypoints_count") + " ", route.getWaypointCount() + ""));

        if (route.getFirstPoint() != null) {
            formRoute.append(new StringItem("\n  " + Locale.get("First_waypoint"), ""));
            formRoute.append(new StringItem(Locale.get("Latitude"), GpsUtils.formatLatitude(route.getFirstPoint().getLatitude(), R.getSettings().getCoordsFormat())));
            formRoute.append(new StringItem(Locale.get("Longitude"), GpsUtils.formatLongitude(route.getFirstPoint().getLongitude(), R.getSettings().getCoordsFormat())));
        }

        addDescription(formRoute, route);

        if (route.getFirstPoint() != null) {
            //#style imgNavigate
            formRoute.addCommand(Commands.cmdNavigate);
            formRoute.addSubCommand(cmdNavigateToFirst, Commands.cmdNavigate);
            if (route.getWaypointCount() > 1) {
                formRoute.addSubCommand(cmdNavigateToLast, Commands.cmdNavigate);
                formRoute.addSubCommand(cmdNavigateAlong, Commands.cmdNavigate);
            }
        }

        if (Capabilities.hasJSR179() && Capabilities.hasLandmarks()) {
            formRoute.addCommand(cmdExport);
            formRoute.addSubCommand(cmdExportFirst, cmdExport);
            formRoute.addSubCommand(cmdExportLast, cmdExport);
        }
        addCommands(formRoute);
        R.getMidlet().switchDisplayable(null, formRoute);
    }

    private void viewDataWaypoint() {
        formWaypoint = new Form(waypoint.getName());
        formWaypoint.append(new StringItem(Locale.get("Latitude"), GpsUtils.formatLatitude(waypoint.getLatitude(), R.getSettings().getCoordsFormat())));
        formWaypoint.append(new StringItem(Locale.get("Longitude"), GpsUtils.formatLongitude(waypoint.getLongitude(), R.getSettings().getCoordsFormat())));

        addDescription(formWaypoint, waypoint);

        //#style imgNavigate
        formWaypoint.addCommand(Commands.cmdNavigate);

        if (Capabilities.hasJSR179() && Capabilities.hasLandmarks())
            formWaypoint.addCommand(cmdExport);

        addCommands(formWaypoint);
        R.getMidlet().switchDisplayable(null, formWaypoint);
    }

    private void viewDataWaypointCloud() {
        formWaypointCloud = new Form(waypointCloud.getName());
        formWaypointCloud.append(new StringItem(Locale.get("Waypoints_count") + " ", waypointCloud.getWaypointsCount() + ""));
//        formWaypointCloud.append(new StringItem("\n  " + Locale.get("Center_waypoint"), ""));
//        formWaypointCloud.append(new StringItem(Locale.get("Latitude"), GpsUtils.formatLatitude(waypointCloud.getCenterLocation().getLatitude(), R.getSettings().getCoordsFormat())));
//        formWaypointCloud.append(new StringItem(Locale.get("Longitude"), GpsUtils.formatLongitude(waypointCloud.getCenterLocation().getLongitude(), R.getSettings().getCoordsFormat())));

        waypointCloudItems = new ChoiceGroup("", ChoiceGroup.MULTIPLE);
        for (int i = 0; i < waypointCloud.getWaypointsCount(); i++) {
            waypointCloudItems.append(" " + waypointCloud.getWaypoint(i).getName(), null);
        }
        formWaypointCloud.append(waypointCloudItems);

//        //#style imgNavigate
//        listWaypointCloud.addCommand(Commands.cmdNavigate);
//        listWaypointCloud.addSubCommand(cmdNavigateToNearest, Commands.cmdNavigate);

        addCommands(formWaypointCloud);
        //#style imgMap
        formWaypointCloud.addCommand(cmdMapAll);
        R.getMidlet().switchDisplayable(null, formWaypointCloud);
    }

    private void addDescription(Form form, GeoData data) {
        if (data.getDescription().length() > 0) {
            form.append(new StringItem("\n  " + Locale.get("Description"), ""));
            form.append(new StringItem("", data.getDescription()));
        }
    }

    private void addCommands(Screen screen) {
        if (inService) {
            //#style imgSaved
            screen.addCommand(Commands.cmdSave);
        }
        //#style imgMap
        screen.addCommand(cmdMap);

        screen.addCommand(Commands.cmdBack);
        //#style imgHome
        screen.addCommand(Commands.cmdHome);
        
        screen.setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdSave) {
            GeoFiles.saveGeoFileData(kmlData);
        } else if (c == cmdMap && d != formWaypointCloud) {
            R.getURL().call("locify://maps");
            if (dataType == GeoFiles.TYPE_WAYPOINT) {
                R.getMapScreen().view(waypoint);
            } else if (dataType == GeoFiles.TYPE_ROUTE) {
                if (route.isRouteOnlyInfo()) {
                    route = (Route) GeoFiles.parseKmlFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                }
                R.getMapScreen().view(route);
            } else if (dataType == GeoFiles.TYPE_MULTI) {
                R.getMapScreen().view(multiData);
            }
        } else if (c == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (c == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (d == formMultiGeoData) {

        } else if (d == formRoute) {
            if (c == cmdExportFirst || c == cmdExportLast) {
                Waypoint way;
                if (route.isRouteOnlyInfo()) {
                    route = (Route) GeoFiles.parseKmlFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                }

                if (c == cmdExportFirst) {
                    way = route.getFirstWaypoint();
                } else {
                    way = route.getLastWaypoint();
                }
                LandmarksExport.export(way, "locify://geoFileBrowser");
            } else if (c == cmdNavigateToFirst || c == cmdNavigateToLast) {
                Waypoint way;
                if (route.isRouteOnlyInfo()) {
                    route = (Route) GeoFiles.parseKmlFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                }
                if (c == cmdNavigateToFirst) {
                    way = route.getFirstWaypoint();
                } else {
                    way = route.getLastWaypoint();
                }
                if (way != null) {
                    R.getURL().call("locify://navigation?lat=" + way.getLatitude() + "&lon=" + way.getLongitude() + "&name=" + way.getName());
                } else {
                    R.getCustomAlert().quickView(Locale.get("Unexpected_problem"), Locale.get("Error"), "locify://refresh");
                }
            } else if (c == cmdNavigateAlong) {
                R.getURL().call("locify://navigation?file=" + fileName);
            }
        } else if (d == formWaypoint) {
            if (c == cmdExport) {
                LandmarksExport.export(waypoint, "locify://geoFileBrowser");
            } else if (c == Commands.cmdNavigate) {
                R.getURL().call("locify://navigation?lat=" + waypoint.getLatitude() + "&lon=" + waypoint.getLongitude() + "&name=" + waypoint.getName());
            }
        } else if (d == formWaypointCloud) {
            if (c == cmdNavigateToNearest) {
                R.getURL().call("locify://navigation?" +
                        "lat=" + waypointCloud.getCenterLocation().getLatitude() + "&" +
                        "lon=" + waypointCloud.getCenterLocation().getLongitude() + "&" +
                        "name=" + waypointCloud.getName() + " - not yet function");
            } else if (c == cmdMap) {
                boolean[] flags = new boolean[waypointCloudItems.size()];
                waypointCloudItems.getSelectedFlags(flags);
                WaypointsCloud mapCloud = new WaypointsCloud(waypointCloud.getName());
                for (int i = 0; i < flags.length; i++) {
                    if (flags[i])
                        mapCloud.addWaypoint(waypointCloud.getWaypoint(i));
                }
                R.getMapScreen().view(mapCloud);
            } else if (c == cmdMapAll) {
                R.getMapScreen().view(waypointCloud);
            }
        }
    }
}
