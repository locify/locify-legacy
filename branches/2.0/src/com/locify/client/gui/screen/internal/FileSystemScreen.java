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
import com.locify.client.gui.extension.FormLocify;
import com.locify.client.gui.extension.ListLocify;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.LocationContext;
import com.locify.client.utils.Commands;
import com.locify.client.utils.Comparable;
import com.locify.client.utils.Locale;
import com.locify.client.utils.R;
import com.locify.client.utils.ResourcesLocify;
import com.locify.client.utils.VectorSortable;
import com.sun.lwuit.Command;
import com.sun.lwuit.Image;
import com.sun.lwuit.Label;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import java.util.Vector;
import javax.microedition.rms.RecordComparator;


/**
 * Views files in filesystem and offers actions
 * @author Destil
 */
public class FileSystemScreen implements ActionListener {

    private FormLocify frmFiles;
    private ListLocify lstFiles;
    private VectorSortable filesystemItems;
    private boolean comparingDistance;
    private boolean canCompareDistance;
    private Command cmdSortName = new Command(Locale.get("Sort_by_name"), 20);
    private Command cmdSortDistance = new Command(Locale.get("Sort_by_distance"), 20);
    private Command cmdDeleteAll = new Command(Locale.get("Delete_all"), 30);
    private Command cmdMapAll = new Command(Locale.get("Show_on_map_all"), 40);
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
            imgRouteSymbol = ResourcesLocify.getImage("routeSymbol.png");
            imgWaypointSymbol = ResourcesLocify.getImage("waypointSymbol.png");
            imgWaypointCloudSymbol = ResourcesLocify.getImage("waypointCloudSymbol.png");
            imgCorruptedFile = ResourcesLocify.getImage("corruptedFile.png");
            imgMultiDataSymbol = ResourcesLocify.getImage("multiSymbol.png");
        } catch (Exception ex) {
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
//Logger.debug("---------- FILESYSTEM browser view -------");
            GeoFiles.fileBrowserOpened = true;

            this.filter = filter;
            this.target = target;
            if ("location".equals(target)) {
                frmFiles = new FormLocify(Locale.get("Select_saved_location"));
            } else if ("place".equals(filter)) {
                frmFiles = new FormLocify(Locale.get("Select_place"));
            } else if ("route".equals(filter)) {
                frmFiles = new FormLocify(Locale.get("Select_route"));
            } else {
                frmFiles = new FormLocify(Locale.get("Saved"));
            }
            lstFiles = new ListLocify();
            lstFiles.addActionListener(this);

            frmFiles.setLayout(new BorderLayout());
            frmFiles.addComponent(BorderLayout.CENTER, lstFiles);
            
            String[] pattern = {"*.kml", "*.gpx"};
            Vector files = R.getFileSystem().listFiles(FileSystem.FILES_FOLDER, pattern);
//Logger.debug("enumeration: " + files);
            filesystemItems = new VectorSortable();
            if (files != null) {
                //comparing mode
                if (R.getLocator().hasValidLocation()) {
                    canCompareDistance = true;
                } else {
                    canCompareDistance = false;
                }

                //reading files
//Logger.debug("file listing start");
                for (int k = 0; k < files.size(); k++) {
                    String filename = (String) files.elementAt(k);
                    if (filename.endsWith(".kml") || filename.endsWith(".gpx")) {
                        double distance = 9999;
//Logger.debug("file: " + filename);
                        int type = GeoFiles.getDataTypeFile(filename);
//Logger.debug("type: " + type);
                        if (type == GeoFiles.TYPE_ROUTE && (filter == null || "route".equals(filter))) {
                            String name = GeoFiles.parseGeoDataFile(filename, true).getName();
                            filesystemItems.addElement(new FileSystemScreenItem(
                                    filename, name, distance, GeoFiles.TYPE_ROUTE));
                        } else if (type == GeoFiles.TYPE_WAYPOINT && (filter == null || "place".equals(filter))) {
//Logger.debug("parsing waypoint");
                            Waypoint waypoint = (Waypoint) GeoFiles.parseGeoDataFile(filename, false).getGeoData(GeoFiles.TYPE_WAYPOINT, 0);
                            if (waypoint != null) {
//Logger.debug("waypoint name: " + waypoint.getName());
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
                        filesystemItems.sort(VectorSortable.ASC);
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

                    Label fsItem = new Label(actualImage);
                    if (comparingDistance) {
                        if (item.getDistance() != 9999) {
                            fsItem.setText(item.getDistance() + " " + item.getName());
                            lstFiles.addItem(fsItem);
                        } else  {
                            fsItem.setText(" - " + item.getName());
                            lstFiles.addItem(fsItem);
                        }
                    } else {
                        fsItem.setText(item.getName());
                        lstFiles.addItem(fsItem);
                    }
                }
                if (!canCompareDistance) {
                    frmFiles.removeCommand(cmdSortName);
                    frmFiles.removeCommand(cmdSortDistance);
                } else {
                    if (comparingDistance) {
                        frmFiles.removeCommand(cmdSortDistance);
                        frmFiles.addCommand(cmdSortName);
                    } else {
                        frmFiles.removeCommand(cmdSortName);
                        frmFiles.addCommand(cmdSortDistance);
                    }
                }
                frmFiles.addCommand(Commands.cmdSelect);

                if (filter == null) {
                    frmFiles.addCommand(Commands.cmdDelete);
                    frmFiles.addCommand(cmdDeleteAll);
                }
                if ("place".equals(filter)) {
                    frmFiles.addCommand(cmdMapAll);
                }
            }
            frmFiles.addCommand(Commands.cmdBack);
            frmFiles.addCommand(Commands.cmdHome);
            frmFiles.setCommandListener(this);

            frmFiles.show();
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
            lstFiles.getModel().removeItem(number);
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
            for (int i = lstFiles.getModel().getSize() - 1; i >= 0; i--)
                lstFiles.getModel().removeItem(i);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "FileSystemScreen.deleteAll", null);
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

    public void actionPerformed(ActionEvent evt) {
        if (evt.getCommand() == Commands.cmdHome) {
            R.getURL().call("locify://mainScreen");
        } else if (evt.getCommand() == Commands.cmdBack) {
            R.getBack().goBack();
        } else if (evt.getCommand() == Commands.cmdSelect || evt.getSource() == lstFiles) {
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
        } else if (evt.getCommand() == Commands.cmdDelete) {
            delete(lstFiles.getSelectedIndex());
        } else if (evt.getCommand() == cmdDeleteAll) {
            deleteAll();
        } else if (evt.getCommand() == cmdSortName) {
            comparingDistance = false;
            view(filter, target);
        } else if (evt.getCommand() == cmdSortDistance) {
            comparingDistance = true;
            view(filter, target);
        } else if (evt.getCommand() == cmdMapAll) {
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
}

class FileSystemScreenItem implements Comparable {

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

    public int compareTo(Object arg0) {
        return (int) (((FileSystemScreenItem) arg0).distance - distance);
    }
}
