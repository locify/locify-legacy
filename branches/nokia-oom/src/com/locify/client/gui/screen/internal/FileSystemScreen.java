/*
 * FileSystemScreen.java
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

import com.locify.client.data.DeletedData;
import com.locify.client.data.FileSystem;
import com.locify.client.data.items.GeoFiles;
import com.locify.client.data.items.Route;
import com.locify.client.data.items.Waypoint;
import com.locify.client.data.items.WaypointsCloud;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.utils.Commands;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import de.enough.polish.util.Arrays;
import de.enough.polish.util.Comparator;
import de.enough.polish.util.Locale;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordComparator;

/**
 * Views files in filesystem and offers actions
 * @author Destil
 */
public class FileSystemScreen implements CommandListener, Comparator {

    private List lstFiles;
    private Vector filesystemItems;
    private boolean comparingDistance;
    private boolean canCompareDistance;
    private Command cmdSortName = new Command(Locale.get("Sort_by_name"), Command.SCREEN, 20);
    private Command cmdSortDistance = new Command(Locale.get("Sort_by_distance"), Command.SCREEN, 20);
    private Command cmdDeleteAll = new Command(Locale.get("Delete_all"), Command.SCREEN, 30);
    private Command cmdMapAll = new Command(Locale.get("Show_on_map_all"), Command.SCREEN, 40);
    private Image imgRouteSymbol;
    private Image imgWaypointSymbol;
    private Image imgWaypointCloudSymbol;
    private Image imgCorruptedFile;
    private Image imgMultiDataSymbol;
    private String filter = "";
    private String target = "";

    public FileSystemScreen() {
        comparingDistance = false;
        canCompareDistance = false;
        try {
            imgRouteSymbol = Image.createImage("/routeSymbol.png");
            imgWaypointSymbol = Image.createImage("/waypointSymbol.png");
            imgWaypointCloudSymbol = Image.createImage("/waypointCloudSymbol.png");
            imgCorruptedFile = Image.createImage("/corruptedFile.png");
            imgMultiDataSymbol = Image.createImage("/multiSymbol.png");
        } catch (IOException ex) {
            R.getErrorScreen().view(ex, "FileSystemScreen.FileSystemScreen()", null);
        }
    }

    /**
     * Views list of files
     * @param filter "place", "route"
     * @param target "location", "maps", "navigation", "upload"
     */
    public void view(String filter, String target) {
        try {
Logger.debug("---------- FILESYSTEM browser view -------");
            GeoFiles.fileBrowserOpened = true;
            
            this.filter = filter;
            this.target = target;
            if ("location".equals(target)) {
                lstFiles = new List(Locale.get("Select_saved_location"), Choice.IMPLICIT);
            } else if ("place".equals(filter)) {
                lstFiles = new List(Locale.get("Select_place"), Choice.IMPLICIT);
            } else if ("route".equals(filter)) {
                lstFiles = new List(Locale.get("Select_route"), Choice.IMPLICIT);
            } else {
                lstFiles = new List(Locale.get("Saved"), Choice.IMPLICIT);
            }

            String[] pattern = {"*.kml", "*.gpx"};
            Vector files = R.getFileSystem().listFiles(FileSystem.FILES_FOLDER, pattern);
Logger.debug("enumeration: " + files);
            filesystemItems = new Vector();
            if (files != null) {
                //comparing mode
                if (R.getLocator().hasValidLocation()) {
                    canCompareDistance = true;
                } else {
                    canCompareDistance = false;
                }

                //reading files
Logger.debug("file listing start");
                for (int k = 0; k < files.size(); k++) {
                    String filename = (String) files.elementAt(k);
                    if (filename.endsWith(".kml") || filename.endsWith(".gpx")) {
                        double distance = 9999;
Logger.debug("file: " + filename);
                        int type = GeoFiles.getDataTypeFile(filename);
Logger.debug("type: " + type);
                        if (type == GeoFiles.TYPE_ROUTE && (filter == null || "route".equals(filter))) {
                            String name = GeoFiles.parseGeoDataFile(filename, true).getName();
                            filesystemItems.addElement(new FileSystemScreenItem(
                                    filename, name, distance, GeoFiles.TYPE_ROUTE));
                        } else if (type == GeoFiles.TYPE_WAYPOINT && (filter == null || "place".equals(filter))) {
Logger.debug("parsing waypoint");
                            Waypoint waypoint = (Waypoint) GeoFiles.parseGeoDataFile(filename, false).getGeoData(GeoFiles.TYPE_WAYPOINT, 0);
                            if (waypoint != null) {
Logger.debug("waypoint name: " + waypoint.getName());
                                if (comparingDistance) {
                                    distance = waypoint.getLocation().distanceTo(R.getLocator().getLastLocation());
                                }
                                filesystemItems.addElement(new FileSystemScreenItem(filename, waypoint.getName(), distance, GeoFiles.TYPE_WAYPOINT));
                            } else {
                                filesystemItems.addElement(new FileSystemScreenItem(filename, filename, 9999999, GeoFiles.TYPE_CORRUPT));
                            }
                        } else if (type == GeoFiles.TYPE_WAYPOINTS_CLOUD && filter == null) {
                            String name = GeoFiles.parseGeoDataFile(filename, true).getName();
                            filesystemItems.addElement(new FileSystemScreenItem(
                                    filename, name, distance, GeoFiles.TYPE_WAYPOINTS_CLOUD));
                        } else if (type == GeoFiles.TYPE_MULTI && filter == null) {
                            String name = GeoFiles.parseGeoDataFile(filename, true).getName();
                            filesystemItems.addElement(new FileSystemScreenItem(
                                    filename, name, 0, GeoFiles.TYPE_MULTI));
                        }
                    }
                    //comparing according to distance
                    if (comparingDistance) {
                        Object array[] = new FileSystemScreenItem[filesystemItems.size()];
                        for (int i = 0; i < filesystemItems.size(); i++) {
                            array[i] = filesystemItems.elementAt(i);
                        }
                        Arrays.sort(array, this);
                        filesystemItems = new Vector();
                        for (int i = 0; i < array.length; i++) {
                            filesystemItems.addElement(array[i]);
                        }
                    }
                }
                //output
                for (int i = 0; i < filesystemItems.size(); i++) {
                    FileSystemScreenItem item = (FileSystemScreenItem) filesystemItems.elementAt(i);
                    Image actualImage = null;
                    if (item.getType() == GeoFiles.TYPE_ROUTE) {
                        actualImage = imgRouteSymbol;
                    } else if (item.getType() == GeoFiles.TYPE_WAYPOINT) {
                        actualImage = imgWaypointSymbol;
                    } else if (item.getType() == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                        actualImage = imgWaypointCloudSymbol;
                    } else if (item.getType() == GeoFiles.TYPE_MULTI) {
                        actualImage = imgMultiDataSymbol;
                    } else if (item.getType() == GeoFiles.TYPE_CORRUPT) {
                        actualImage = imgCorruptedFile;
                    }

                    if (comparingDistance) {
                        if (item.getDistance() != 9999)
                            lstFiles.append(GpsUtils.formatDistance(item.getDistance()) + " " + item.getName(), actualImage);
                        else
                            lstFiles.append(" - " + item.getName(), actualImage);
                    } else {
                        lstFiles.append(item.getName(), actualImage);
                    }
                }
                if (!canCompareDistance) {
                    lstFiles.removeCommand(cmdSortName);
                    lstFiles.removeCommand(cmdSortDistance);
                } else {
                    if (comparingDistance) {
                        lstFiles.removeCommand(cmdSortDistance);
                        lstFiles.addCommand(cmdSortName);
                    } else {
                        lstFiles.removeCommand(cmdSortName);
                        lstFiles.addCommand(cmdSortDistance);
                    }
                }
                lstFiles.addCommand(Commands.cmdSelect);
                lstFiles.setSelectCommand(Commands.cmdSelect);
                if (filter == null) {
                    //#style imgDelete
                    lstFiles.addCommand(Commands.cmdDelete);
                    //#style imgDelete
                    lstFiles.addCommand(cmdDeleteAll);
                }
                if ("place".equals(filter)) {
                    lstFiles.addCommand(cmdMapAll);
                }
            }
            lstFiles.addCommand(Commands.cmdBack);
            //#style imgHome
            lstFiles.addCommand(Commands.cmdHome);
            lstFiles.setCommandListener(this);
            R.getMidlet().switchDisplayable(null, lstFiles);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystemScreen.view", null);
        }
    }

    /**
     * Views not filtered file list for browsing
     */
    public void view() {
        view(null, null);
    }

    /**
     * Deletes selected file
     * @param number number in list
     */
    public void delete(int number) {
        try {
            String fileName = ((FileSystemScreenItem) filesystemItems.elementAt(number)).getFileName();

            String type = "";
            int fileType = GeoFiles.getDataTypeFile(fileName);
            if (fileType == GeoFiles.TYPE_WAYPOINT) {
                type = "waypoint";
            } else if (fileType == GeoFiles.TYPE_ROUTE) {
                type = "route";
            } else if (fileType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                type = "waypointCloud";
            } else if (fileType == GeoFiles.TYPE_MULTI) {
                type = "multiData";
            } else if (fileType == GeoFiles.TYPE_CORRUPT) {
                type = "corruptFile";
            }

            DeletedData.add("locify://filesystem/" + fileName, type);
            R.getFileSystem().delete(FileSystem.FILES_FOLDER + fileName);
            filesystemItems.removeElementAt(number);
            lstFiles.delete(number);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystemScreen.delete", null);
        }
    }

    /**
     * Deletes all files
     */
    public void deleteAll() {
        try {
            R.getFileSystem().deleteAll(FileSystem.FILES_FOLDER);
            filesystemItems.removeAllElements();
            lstFiles.deleteAll();
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystemScreen.deleteAll", null);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (c == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (c == Commands.cmdSelect || c == List.SELECT_COMMAND) {
            FileSystemScreenItem item = (FileSystemScreenItem) filesystemItems.elementAt(lstFiles.getSelectedIndex());
            String fileName = item.getFileName();
            if (item.getType() == GeoFiles.TYPE_CORRUPT) {
                R.getCustomAlert().quickView(Locale.get("GeoFile_error"), Locale.get("Warning"), "locify://files");
                return;
            }
            if ("location".equals(target)) {
                Waypoint waypoint = null;
                int fileType = GeoFiles.getDataTypeFile(fileName);
                if (fileType == GeoFiles.TYPE_WAYPOINT) {
                    waypoint = (Waypoint) GeoFiles.parseGeoDataFile(fileName, false).getGeoData(GeoFiles.TYPE_WAYPOINT, 0);
                } else if (fileType == GeoFiles.TYPE_ROUTE) {
                    Route route = (Route) GeoFiles.parseGeoDataFile(fileName, false).getGeoData(GeoFiles.TYPE_ROUTE, 0);
                    waypoint = new Waypoint(
                            route.getFirstPoint().getLatitude(),
                            route.getFirstPoint().getLongitude(),
                            "", "", null);
                } else if (fileType == GeoFiles.TYPE_WAYPOINTS_CLOUD) {
                    WaypointsCloud cloud = (WaypointsCloud) GeoFiles.parseGeoDataFile(fileName, false).getGeoData(GeoFiles.TYPE_WAYPOINTS_CLOUD, 0);
                    waypoint = new Waypoint(cloud.getCenterLocation().getLatitude(),
                            cloud.getCenterLocation().getLongitude(), "", "", null);
                }
                if (waypoint != null) {
                    R.getContext().setLocation(new Location4D(waypoint.getLatitude(), waypoint.getLongitude(), 0), LocationContext.SAVED_LOCATION, waypoint.getName());
                }
            } else if ("maps".equals(target)) {
                R.getURL().call("locify://maps?file=" + fileName);
            } else if ("navigation".equals(target)) {
                R.getURL().call("locify://navigation?file=" + fileName);
            } else if (target == null) {
                R.getURL().call("locify://geoFileBrowser?file=" + fileName);
            } else if ("upload".equals(target)) {
                //input type file
                R.getFileBrowser().selectFile(FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName, fileName);
            }
        } else if (c == Commands.cmdDelete) {
            delete(lstFiles.getSelectedIndex());
        } else if (c == cmdDeleteAll) {
            deleteAll();
        } else if (c == cmdSortName) {
            comparingDistance = false;
            view(filter, target);
        } else if (c == cmdSortDistance) {
            comparingDistance = true;
            view(filter, target);
        } else if (c == cmdMapAll) {
            if (filesystemItems.size() > 0) {
                R.getURL().call("locify://maps");
            }
            WaypointsCloud cloud = new WaypointsCloud("All waypoints");
            for (int i = 0; i < filesystemItems.size(); i++) {
                FileSystemScreenItem item = (FileSystemScreenItem) filesystemItems.elementAt(i);
                cloud.addWaypoint(
                        (Waypoint) GeoFiles.parseGeoDataFile(item.getFileName(), false).getGeoData(GeoFiles.TYPE_WAYPOINT, 0));
            }
            R.getMapScreen().view(cloud);
        }
    }

    /**
     * Comparing according to distance
     * @param a
     * @param b
     * @return
     */
    public int compare(Object a, Object b) {
        FileSystemScreenItem aa = (FileSystemScreenItem) a;
        FileSystemScreenItem bb = (FileSystemScreenItem) b;
        if (aa.getDistance() == bb.getDistance()) {
            return RecordComparator.EQUIVALENT;
        }
        if (aa.getDistance() > bb.getDistance()) {
            return RecordComparator.FOLLOWS;
        }
        return RecordComparator.PRECEDES;
    }
}

class FileSystemScreenItem {

    private String fileName;
    private String name;
    private double distance;
    private int geoFilesType;

    public FileSystemScreenItem(String fileName, String name, double distance, int geoFilesType) {
        this.fileName = fileName;
        this.name = name;
        this.distance = distance;
        this.geoFilesType = geoFilesType;
    }

    public double getDistance() {
        return distance;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return geoFilesType;
    }
}
