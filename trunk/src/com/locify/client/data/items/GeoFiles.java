/*
 * GeoFiles.java
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
package com.locify.client.data.items;

import com.locify.client.data.FileSystem;
import com.locify.client.data.Sync;
import com.locify.client.locator.Location4D;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Class for managing geodata
 * @author Menion
 */
public abstract class GeoFiles {
    public static final int TYPE_WAYPOINT = 1;
    public static final int TYPE_WAYPOINTS_CLOUD = 2;
    public static final int TYPE_ROUTE = 3;
    public static final int TYPE_NETWORKLINK = 4;
    public static final int TYPE_MULTI = 9;
    public static final int TYPE_CORRUPT = 10;

    private static final int STATE_NONE = 0;
    private static final int STATE_DOCUMENT = 1;
    private static final int STATE_FOLDER = 2;
    private static final int STATE_PLACEMARK = 3;

    private static int sActual;
    private static int sBefore;

    private static final String GEO_FILES_RECORD_STORE = "GeoFilesDatabase";
    private static Vector geoTypeDatabase;
    public static boolean fileBrowserOpened = false;


    /***************************************************/
    /*                 SAVE FUNCTIONS                  */
    /***************************************************/
    /**
     * Saves object from sync
     * @param kml kml content
     * @param filename custom filename
     */
    public static void save(String kml, String filename) {
        try {
            // does not use fileName function, file name is already formatted and unique
            // uniqueness handled by server
            R.getFileSystem().saveString(FileSystem.FILES_FOLDER + filename + ".kml", kml);
            //view alert
            if (!Sync.isRunning()) {
                R.getCustomAlert().quickView(Locale.get("Object_saved"), "info", "locify://refresh");
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFile.save()", kml);
        }
    }

    /**
     * Save temp route into selected file
     * @param routeName name of route
     * @param tripDist total distance of route
     * @param tripTime total travel time of route
     */
    public static void saveRoute(String fileName, String routeName, String description, RouteVariables routeVariables) {
        if (!R.getFileSystem().exists(FileSystem.RUNNING_TEMP_ROUTE)) {
            Logger.error("GeoFiles.saveRoute() error");
        }

        routeVariables.dataEnd(routeName, description);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        R.getFileSystem().renameFile(FileSystem.RUNNING_TEMP_ROUTE, fileName(fileName));
    }

    /**
     * Creates KML file and writes it in filesystem
     * @param latitude latitude in degrees
     * @param longitude longitude in degrees
     * @param name name of point
     * @param description description of point
     */
    public static void saveWaypoint(double latitude, double longitude, String name, String description) {
        try {
            String fileName = GeoFiles.fileName(name);
            //create kml
            String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.2\">\n  <Placemark>\n    <name>" + name + "</name>\n    <description>" + description + "</description>\n    <Point>\n      <coordinates>" + longitude + "," + latitude + "</coordinates>\n    </Point>\n  </Placemark>\n</kml>";
            //write file
            R.getFileSystem().saveString(FileSystem.FILES_FOLDER + fileName, kml);
            //view alert
            if (!Sync.isRunning()) {
                R.getCustomAlert().quickView(Locale.get("Waypoint_saved"), "info", "locify://back");
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.saveWaypoint", name);
        }
    }

    /**
     * Saves waypoint from waypoint options screen
     * @param kml kml content
     */
    public static void saveGeoFileData(String kml) {
        try {
            int type = getDataTypeString(kml);
            String name = parseKmlString(kml, true).getName();

            if (name != null && name.length() > 0) {
                R.getFileSystem().saveString(FileSystem.FILES_FOLDER + GeoFiles.fileName(name), kml);
                //view alert
                if (!Sync.isRunning()) {
                    R.getCustomAlert().quickView(Locale.get("Waypoint_saved"), "info", "locify://geoFileBrowser");
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.saveWaypoint", kml);
        }
    }

    /****************************************************/
    /*                 PARSE FUNCTIONS                  */
    /****************************************************/

    public static MultiGeoData parseKmlFile(String fileName, boolean firstNameOnly) {
        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;

        try {
            
            fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
            if (!fileConnection.exists()) {
                return null;
            }

            is = fileConnection.openInputStream();
            parser = new KXmlParser();
            parser.setInput(is, "utf-8");

            return parseKml(parser, firstNameOnly);
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "RouteData.isRoute", null);
            Logger.debug("GeoFiles.parseKmlFile() - wrong file: " + fileName);
            return null;
        } finally {
            try {
                if (fileConnection != null) {
                    fileConnection.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static MultiGeoData parseKmlString(String data, boolean firstNameOnly) {
        ByteArrayInputStream stream = null;
        InputStreamReader reader = null;
        XmlPullParser parser;
        
        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            reader = new InputStreamReader(stream);
            parser = new KXmlParser();
            parser.setInput(reader);

            return parseKml(parser, firstNameOnly);
        } catch (Exception e) {
            Logger.debug("Parsing: wrongFile or data: " + data + "\n" + e.getMessage());
            return null;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static MultiGeoData parseKml(XmlPullParser parser, boolean firstNameOnly) {
        MultiGeoData multiData = new MultiGeoData();
        GeoData actualGeoData = null;

        try {
            String name = "";
            String description = "";

            Waypoint waypoint = null;

            int event;
            String tagName;

            sBefore = STATE_NONE;
            sActual = STATE_NONE;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("document")) {
                        setState(STATE_DOCUMENT);
                    } else if (tagName.equalsIgnoreCase("name")) {
                        if (firstNameOnly) {
                            multiData.name = parser.nextText();
                            return multiData;
                        }

                        if (sActual == STATE_DOCUMENT) {
                            multiData.name = parser.nextText();
                            name = "";
                        } else {
                            name = parser.nextText();
                        }
                    } else if (tagName.equalsIgnoreCase("description")) {
                        if (sActual == STATE_DOCUMENT) {
                            multiData.description = parser.nextText();
                            description = "";
                        } else {
                            description = parser.nextText();
                        }
                    /* it will be only cloud and containts only placemark tags */
                    } else if (tagName.equalsIgnoreCase("folder")) {
                        setState(STATE_FOLDER);
                        actualGeoData = new WaypointsCloud("");
                    } else if (tagName.equalsIgnoreCase("placemark")) {
                        setState(STATE_PLACEMARK);
                    } else if (tagName.equalsIgnoreCase("point")) {
                        /* sActual is always placemark but sBefore may be FOLDER
                         * (waypoint_cloud) or DOCUMENT (waypoint) */
                        if (sActual == STATE_PLACEMARK) {
                            waypoint = new Waypoint(0.0, 0.0, "", "");
                            while (true) {
                                event = parser.nextToken();
                                if (event == XmlPullParser.START_TAG) {
                                    tagName = parser.getName();
                                    if (tagName.equalsIgnoreCase("coordinates")) {
                                        String coordinates = parser.nextText();
                                        String[] parts = StringTokenizer.getArray(coordinates, ",");
                                        waypoint.longitude = Double.parseDouble(parts[0]);
                                        waypoint.latitude = Double.parseDouble(parts[1]);
                                    }
                                } else if (event == XmlPullParser.END_TAG) {
                                    tagName = parser.getName();
                                    if (tagName.equalsIgnoreCase("placemark")) {
                                        waypoint.name = name;
                                        waypoint.description = description;
                                        if (sBefore == STATE_FOLDER || sBefore == STATE_PLACEMARK) {
                                            ((WaypointsCloud) actualGeoData).addWaypoint(waypoint);
                                        } else if (sBefore == STATE_DOCUMENT || sBefore == STATE_NONE) {
                                            multiData.addGeoData(waypoint);
                                        } else {
                                            Logger.log("GeoFiles.parseKml() - POINT error1!!!");
                                            return null;
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            Logger.log("GeoFiles.parseKml() - POINT error2!!!");
                            return null;
                        }
                    } else if (tagName.equalsIgnoreCase("linestring")) {
                        if (sActual == STATE_PLACEMARK) {
                            if (actualGeoData == null || !(actualGeoData instanceof Route)) {
                                actualGeoData = new Route();
                            }

                            Route route = (Route) actualGeoData;

                            while (true) {
                                event = parser.nextToken();
                                if (event == XmlPullParser.START_TAG) {
                                    tagName = parser.getName();
                                    if (tagName.equalsIgnoreCase("coordinates")) {
                                        if (route.points.size() != 0) {
                                            route.separating.addElement(new Integer(route.points.size()));
                                        }
                                        String coordinates = parser.nextText();
                                        coordinates = coordinates.replace(',', ' ');
                                        coordinates = coordinates.replace('\n', ' ');
                                        coordinates = coordinates.replace('\t', ' ');

                                        String coo = "";
                                        de.enough.polish.util.StringTokenizer token = new de.enough.polish.util.StringTokenizer(coordinates, ' ');
                                        while (token.hasMoreTokens()) {
                                            coo = token.nextToken();

                                            if (token.hasMoreTokens()) {
                                                route.points.addElement(new Location4D(
                                                        Double.parseDouble((String) token.nextToken()),
                                                        Double.parseDouble((String) coo),
                                                        Float.parseFloat(token.nextToken())));
                                            }
                                        }
                                    }
                                } else if (event == XmlPullParser.END_TAG) {
                                    tagName = parser.getName();
                                    if (tagName.equalsIgnoreCase("linestring")) {
                                        break;
                                    }
                                }
                            }
                        } else {
                            Logger.log("GeoFiles.parseKml() - LINESTRING error!!!");
                            return null;
                        }
                    } else if (tagName.equalsIgnoreCase("networklink")) {
                        NetworkLink nl = new NetworkLink();
                        while (true) {
                            event = parser.nextToken();
                            if (event == XmlPullParser.START_TAG) {
                                tagName = parser.getName();
                                if (tagName.equalsIgnoreCase("name")) {
                                    nl.name = parser.nextText();
                                } else if (tagName.equalsIgnoreCase("description")) {
                                    nl.description = parser.nextText();
                                } else if (tagName.equalsIgnoreCase("href")) {
                                    nl.link = parser.nextText();
                                } else if (tagName.equalsIgnoreCase("refreshInterval")) {
                                    nl.refreshInterval = Integer.parseInt(parser.nextText());
                                } else if (tagName.equalsIgnoreCase("viewFormat")) {
                                    nl.viewFormat = parser.nextText();
                                }
                            } else if (event == XmlPullParser.END_TAG) {
                                tagName = parser.getName();
                                if (tagName.equalsIgnoreCase("networklink")) {
                                    multiData.addGeoData(nl);
                                    break;
                                }
                            }
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("folder")) {
                        setState(STATE_DOCUMENT);
                        multiData.addGeoData(actualGeoData);
                    } else if (tagName.equalsIgnoreCase("placemark")) {
                        if (actualGeoData instanceof Route) {
                            actualGeoData.name = name;
                            actualGeoData.description = description;
                            ((Route) actualGeoData).routeOnlyInfo = firstNameOnly;
                            multiData.addGeoData(actualGeoData);
                        }
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }

            multiData.finalizeData();
            return multiData;
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "GeoFiles.parseRouteOrCloud()", fileOrData);
            Logger.log("GeoFiles.parseKml() - FINAL error!!!\n" + e.getMessage());
            return null;
        }
    }

    private static void setState(int state) {
        sBefore = sActual;
        sActual = state;
    }

    /***************************************************/
    /*       GET TYPE & RECORD STORE FUNCTIONS         */
    /***************************************************/

    public static int getDataTypeFile(String fileName) {
        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;
        
        int type = 0;
        long fileSize = 0;

        try {
            fileSize = R.getFileSystem().getFileSize(FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
            type = getDataTypeDatabase(fileName, fileSize);
            
//System.out.println("TestFile: " + fileName + " type: " + type + " size: " + fileSize);
            if (type == 0) {
                fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
                if (!fileConnection.exists()) {
                    return TYPE_CORRUPT;
                }

                is = fileConnection.openInputStream();
                parser = new KXmlParser();
                parser.setInput(is, "utf-8");

                type = getDataType(parser);
            }
            return type;
        } catch (Exception e) {
            Logger.debug("GeoFiles.parseKmlFile() - wrong file: " + fileName);
            return TYPE_CORRUPT;
        } finally {
            try {
                if (fileConnection != null) {
                    fileConnection.close();
                    addDataTypeToDatabase(fileName, fileSize, type);
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static int getDataTypeString(String data) {
        ByteArrayInputStream stream = null;
        InputStreamReader reader = null;
        XmlPullParser parser;

        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            reader = new InputStreamReader(stream);
            parser = new KXmlParser();
            parser.setInput(reader);

            return getDataType(parser);
        } catch (Exception e) {
            Logger.debug("Parsing: wrongFile or data: " + data + "\n" + e.getMessage());
            return TYPE_CORRUPT;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static int getDataType(XmlPullParser parser) {
        int actualType = TYPE_CORRUPT;
        boolean containPlacemark = false;

        try {
            int event;
            String tagName;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("linestring")) {
                        if (actualType == TYPE_CORRUPT || actualType == TYPE_ROUTE) {
                            actualType = TYPE_ROUTE;
                        } else {
                            return TYPE_MULTI;
                        }
                    } else if (tagName.equalsIgnoreCase("folder")) {
                        if (actualType == TYPE_CORRUPT) {
                            actualType = TYPE_WAYPOINTS_CLOUD;
                        } else {
                            return TYPE_MULTI;
                        }
                    } else if (tagName.equalsIgnoreCase("networklink")) {
                        return TYPE_NETWORKLINK;
                    } else if (tagName.equalsIgnoreCase("placemark")) {
                        containPlacemark = true;
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }

            if (actualType == TYPE_CORRUPT) {
                if (containPlacemark) {
                    return TYPE_WAYPOINT;
                } else {
                    return TYPE_CORRUPT;
                }
            } else {
                return actualType;
            }
        } catch (Exception e) {
            Logger.debug("GeoFiles.getDataType()");
            return TYPE_CORRUPT;
        }
    }

    // DATABASE TYPE MANIPULATION
    private static int getDataTypeDatabase(String fileName, long fileSize) {
        loadDataTypeDatabase();

        GeoFileType gft;
        for (int i = 0; i < geoTypeDatabase.size(); i++) {
            gft = (GeoFileType) geoTypeDatabase.elementAt(i);
            if (gft.compare(fileName, fileSize)) {
                return gft.getType();
            }
        }
        return 0;
    }

    private static void loadDataTypeDatabase() {
        if (geoTypeDatabase == null) {
            try {
                geoTypeDatabase = new Vector();
                RecordStore rs = RecordStore.openRecordStore(GEO_FILES_RECORD_STORE, true, RecordStore.AUTHMODE_PRIVATE, true);
                RecordEnumeration re = rs.enumerateRecords(null, null, false);
                while (re.hasNextElement()) {
                    geoTypeDatabase.addElement(new GeoFileType(new String(re.nextRecord())));
                }
                rs.closeRecordStore();
            } catch (RecordStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void addDataTypeToDatabase(String fileName, long fileSize, int type) {
//System.out.println("Add: " + testString);
        if (geoTypeDatabase == null)
            geoTypeDatabase = new Vector();

        geoTypeDatabase.addElement(new GeoFileType(fileName, fileSize, type));
    }

    public static void saveDataTypeDatabase() {
        if (fileBrowserOpened) {
            try {
                RecordStore.deleteRecordStore(GEO_FILES_RECORD_STORE);
                RecordStore rs = RecordStore.openRecordStore(GEO_FILES_RECORD_STORE, true, RecordStore.AUTHMODE_PRIVATE, true);
                if (geoTypeDatabase != null) {
                    GeoFileType gft;
                    for (int i = 0; i < geoTypeDatabase.size(); i++) {
                        gft = (GeoFileType) geoTypeDatabase.elementAt(i);
                        if (gft.isUsed()) {
                            byte[] bytes = gft.getBytesToWrite();
                            rs.addRecord(bytes, 0, bytes.length);
                        }
                    }
                }
                rs.closeRecordStore();
            } catch (RecordStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

//    private static class RecordFileFilter implements RecordFilter {
//        private String filter;
//
//        public RecordFileFilter(String filter) {
//            this.filter = filter;
//        }
//
//        public boolean matches(byte[] candidate) {
//            if ((new String(candidate)).startsWith(filter))
//                return true;
//            return false;
//        }
//    }

    /****************************************************/
    /*                 OTHER FUNCTIONS                  */
    /****************************************************/
    /**
     * Converts name to waypoint file name. It's camelcased name with _suffix if this file already exist
     * @param name name
     * @return file name
     */
    public static String fileName(String name) {
        try {
            if (name.equals("")) {
                name = "Waypoint";
            }
            //remove unwanted chars
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < name.length(); i++) {
                char ch = name.charAt(i);
                if (Character.isDigit(ch) || Character.isLowerCase(ch) || Character.isUpperCase(ch)) {
                    buffer.append(ch);
                }
            }
            name = buffer.toString();
            //create camelcase filename
            String[] words = StringTokenizer.getArray(name.trim(), " ");
            String fileName = "";
            for (int i = 0; i < words.length; i++) {
                fileName += words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            }
            //append suffix if file exists
            if (R.getFileSystem().exists(FileSystem.FILES_FOLDER + fileName + ".kml")) {
                int suffixIndex = 0;
                while (true) {
                    suffixIndex++;
                    if (!R.getFileSystem().exists(FileSystem.FILES_FOLDER + fileName + "_" + Utils.addZerosBefore(String.valueOf(suffixIndex), 3) + ".kml")) {
                        break;
                    }
                }
                fileName += "_" + Utils.addZerosBefore(String.valueOf(suffixIndex), 3);
            }
            fileName += ".kml";
            return fileName;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.fileName()", name);
            return "";
        }
    }

    /**
     * Returns all waypoints as sync data xml
     * @return sync data
     */
    public static String syncData() {
        String syncData = "";
        try {

            Enumeration files = R.getFileSystem().listFiles(FileSystem.FILES_FOLDER, "*.kml");
            if (files != null) {
                while (files.hasMoreElements()) {
                    String fileName = (String) files.nextElement();

                    String type = "";
                    int fileType = getDataTypeFile(fileName);
                    if (fileType == TYPE_WAYPOINT) {
                        type = "waypoint";
                    } else if (fileType == TYPE_ROUTE) {
                        type = "route";
                    } else if (fileType == TYPE_WAYPOINTS_CLOUD) {
                        type = "waypointCloud";
                    } else {
                        continue;
                    }

                    syncData += "<file>\n";
                    syncData += "<id>locify://filesystem/" + fileName + "</id>\n";
                    syncData += "<type>" + type + "</type>\n";
                    syncData += "<action>allSync</action>\n";
                    syncData += "<ts>" + R.getFileSystem().getTimestamp(FileSystem.FILES_FOLDER + fileName) + "</ts>\n";
                    syncData += "<content>\n";
                    syncData += Utils.removeXmlHeaders(R.getFileSystem().loadString(FileSystem.FILES_FOLDER + fileName));
                    syncData += "</content>\n";
                    syncData += "</file>\n";
                }
            }
            return syncData;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.syncData()", null);
            return "";
        }
    }

    public static boolean isUnfinishedRoute() {
        try {
            if (!R.getFileSystem().exists(FileSystem.RUNNING_ROUTE_VARIABLES)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.isUnfinishedRoute", null);
            return false;
        }
    }
}

