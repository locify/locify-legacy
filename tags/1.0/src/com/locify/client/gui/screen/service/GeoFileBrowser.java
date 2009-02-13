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
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.utils.Capabilities;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.R;
import de.enough.polish.ui.Form;
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
    private WaypointsCloud cloud;
    private Route route;
    private int dataType;
    private String kmlData;
    private String fileName;
    private Form form;
    private boolean inService;
    private Command cmdMap;
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
        int type = GeoFiles.getDataTypeString(kml);
        if (type == GeoFiles.TYPE_WAYPOINT) {
            waypoint = GeoFiles.loadWaypointString(kml);
            dataType = GeoFiles.TYPE_WAYPOINT;
        } else if (type == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
            cloud = GeoFiles.loadWaypointsCloudString(kml);
            dataType = GeoFiles.TYPE_WAYPOINTS_CLOUD;
        } else if (type == GeoFiles.TYPE_ROUTE) {
            route = GeoFiles.loadRouteString(kml);
            dataType = GeoFiles.TYPE_ROUTE;
        }
        inService = true;
        this.kmlData = kml;
    }

    /**
     * Sets file with waypoint kml data
     * @param fileName file name
     */
    public void view(String fileName) {
        int type = GeoFiles.getDataTypeFile(fileName);
        if (type == GeoFiles.TYPE_WAYPOINT) {
            waypoint = GeoFiles.loadWaypointFile(fileName);
            dataType = GeoFiles.TYPE_WAYPOINT;
        } else if (type == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
            cloud = GeoFiles.loadWaypointsCloudFile(fileName);
            dataType = GeoFiles.TYPE_WAYPOINTS_CLOUD;
        } else if (type == GeoFiles.TYPE_ROUTE) {
            route = GeoFiles.loadRouteFile(fileName, false);
            dataType = GeoFiles.TYPE_ROUTE;
        }
        this.fileName = fileName;
        inService = false;
        view();
    }

    /**
     * Show user this what to do with received geoFile
     */
    public void view() {
        try {
            GeoData data = null;
            if (dataType == GeoFiles.TYPE_WAYPOINT) {
                data = waypoint;
            } else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                data = cloud;
            } else if (dataType == GeoFiles.TYPE_ROUTE) {
                data = route;
            }

            form = new Form(data.getName());
            if (dataType == GeoFiles.TYPE_WAYPOINT) {
                form.append(new StringItem(Locale.get("Latitude"), GpsUtils.formatLatitude(waypoint.getLatitude(), R.getSettings().getCoordsFormat())));
                form.append(new StringItem(Locale.get("Longitude"), GpsUtils.formatLongitude(waypoint.getLongitude(), R.getSettings().getCoordsFormat())));
            }
            if (dataType == GeoFiles.TYPE_ROUTE) {
                form.append(new StringItem(Locale.get("Route_length") + " ", GpsUtils.formatDistance(route.getRouteDist())));
                form.append(new StringItem(Locale.get("Travel_time") + " ", GpsUtils.formatTime(route.getRouteTime())));
                form.append(new StringItem(Locale.get("Waypoints_count") + " ", route.getWaypointCount() + ""));

                if (route.getFirstWaypoint() != null) {
                    form.append(new StringItem("\n  " + Locale.get("First_waypoint"), ""));
                    form.append(new StringItem(Locale.get("Latitude"), GpsUtils.formatLatitude(route.getFirstWaypoint().getLatitude(), R.getSettings().getCoordsFormat())));
                    form.append(new StringItem(Locale.get("Longitude"), GpsUtils.formatLongitude(route.getFirstWaypoint().getLongitude(), R.getSettings().getCoordsFormat())));
                }
            } else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                form.append(new StringItem(Locale.get("Waypoints_count") + " ", cloud.getWaypointsCount() + ""));

                form.append(new StringItem("\n  " + Locale.get("Center_waypoint"), ""));
                form.append(new StringItem(Locale.get("Latitude"), GpsUtils.formatLatitude(cloud.getCenterLocation().getLatitude(), R.getSettings().getCoordsFormat())));
                form.append(new StringItem(Locale.get("Longitude"), GpsUtils.formatLongitude(cloud.getCenterLocation().getLongitude(), R.getSettings().getCoordsFormat())));
            }

            if (data.getDescription().length() > 0) {
                form.append(new StringItem("\n  " + Locale.get("Description"), ""));
                form.append(new StringItem("", data.getDescription()));
            }

            form.addCommand(Commands.cmdBack);
            if (inService) {
                //#style imgSaved
                form.addCommand(Commands.cmdSave);
            }

            if (dataType == GeoFiles.TYPE_WAYPOINT) {
                //#style imgNavigate
                form.addCommand(Commands.cmdNavigate);
            } else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                //#style imgNavigate
                form.addCommand(Commands.cmdNavigate);
                form.addSubCommand(cmdNavigateToNearest, Commands.cmdNavigate);
            } else if (dataType == GeoFiles.TYPE_ROUTE) {
                if (route.getFirstWaypoint() != null) {
                    //#style imgNavigate
                    form.addCommand(Commands.cmdNavigate);
                    form.addSubCommand(cmdNavigateToFirst, Commands.cmdNavigate);
                    if (route.getWaypointCount() > 1) {
                        form.addSubCommand(cmdNavigateToLast, Commands.cmdNavigate);
                        form.addSubCommand(cmdNavigateAlong, Commands.cmdNavigate);
                    }
                }
            }
            //#style imgMap
            form.addCommand(cmdMap);
            if (Capabilities.hasJSR179() && Capabilities.hasLandmarks()) {
                if (dataType == GeoFiles.TYPE_WAYPOINT) {
                    form.addCommand(cmdExport);
                } else if (dataType == GeoFiles.TYPE_ROUTE) {
                    form.addCommand(cmdExport);
                    form.addSubCommand(cmdExportFirst, cmdExport);
                    form.addSubCommand(cmdExportLast, cmdExport);
                }
            }

            //#style imgHome
            form.addCommand(Commands.cmdHome);
            form.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, form);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFileBrowser.view()", null);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (c == cmdNavigateToFirst || c == cmdNavigateToLast) {
            Waypoint way;
            if (c == cmdNavigateToFirst) {
                way = route.getFirstWaypoint();
            } else {
                if (route.isRouteOnlyInfo()) {
                    route = GeoFiles.loadRouteFile(fileName, false);
                }
                way = route.getLastWaypoint();
            }
            if (way != null) {
                R.getURL().call("locify://navigation?lat=" + way.getLatitude() + "&lon=" + way.getLongitude() + "&name=" + way.getName());
            } else {
                R.getCustomAlert().quickView(Locale.get("Unexpected_problem"), Locale.get("Error"), "locify://refresh");
            }
        } else if (c == cmdNavigateAlong) {
            R.getURL().call("locify://navigation?file=" + fileName);
        } else if (c == cmdNavigateToNearest) {
            R.getURL().call("locify://navigation?" +
                    "lat=" + cloud.getCenterLocation().getLatitude() + "&" +
                    "lon=" + cloud.getCenterLocation().getLongitude() + "&" +
                    "name=" + cloud.getName() + " - not yet function");
        } else if (c == cmdExportFirst || c == cmdExportLast) {
            Waypoint way;
            if (c == cmdExportFirst) {
                way = route.getFirstWaypoint();
            } else {
                if (route.isRouteOnlyInfo()) {
                    route = GeoFiles.loadRouteFile(fileName, false);
                }
                way = route.getLastWaypoint();
            }
            LandmarksExport.export(way, "locify://geoFileBrowser");
        } else if (c == cmdExport) {
            LandmarksExport.export(waypoint, "locify://geoFileBrowser");
        } else if (c == Commands.cmdSave) {
            GeoFiles.saveGeoFileData(kmlData);
        } else if (c == Commands.cmdNavigate) {
            R.getURL().call("locify://navigation?lat=" + waypoint.getLatitude() + "&lon=" + waypoint.getLongitude() + "&name=" + waypoint.getName());
        } else if (c == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (c == cmdExport) {
            LandmarksExport.export(waypoint, "locify://geoFileBrowser");
        } else if (c == cmdMap) {
            R.getURL().call("locify://maps");
            if (dataType == GeoFiles.TYPE_WAYPOINT) {
                R.getMapScreen().view(waypoint);
            } else if (dataType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                R.getMapScreen().view(cloud);
            } else if (dataType == GeoFiles.TYPE_ROUTE) {
                if (route.isRouteOnlyInfo()) {
                    route = GeoFiles.loadRouteFile(fileName, false);
                }
                R.getMapScreen().view(route);
            }
        }
    }
}
