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
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * Class for managing geodata
 * @author Menion
 */
public abstract class GeoFiles {

    private final static String linSe = "\n";

    public static final int TYPE_WAYPOINT = 1;
    public static final int TYPE_WAYPOINTS_CLOUD = 2;
    public static final int TYPE_ROUTE = 3;
    public static final int TYPE_NETWORKLINK = 4;
    public static final int TYPE_CORRUPT = 10;

    private static final int ROUTE_TYPE_FILE = 1;
    private static final int ROUTE_TYPE_STRING = 2;
    private static final int WAYPOINT_CLOUD_TYPE_FILE = 3;
    private static final int WAYPOINT_CLOUD_TYPE_STRING = 4;

    private static final int STATE_NONE = 0;
    private static final int STATE_DOCUMENT = 1;
    private static final int STATE_FOLDER = 2;
    private static final int STATE_PLACEMARK = 3;

    private static int sActual;
    private static int sBefore;

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

        routeVariables.dataEnd(description);
        routeVariables.dataFlush(false);
        routeVariables.dataAddName(routeName);
        routeVariables.dataFlush(true);

        if (!fileName.endsWith(".kml")) {
            fileName += ".kml";
        }

        if (R.getFileSystem().exists(FileSystem.FILES_FOLDER + fileName)) {
            R.getFileSystem().delete(FileSystem.FILES_FOLDER + fileName);
        }

        R.getFileSystem().renameFile(FileSystem.RUNNING_TEMP_ROUTE, fileName);
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
            GeoData data = null;
            if (type == TYPE_WAYPOINT) {
                data = loadWaypointString(kml);
            } else if (type == TYPE_WAYPOINTS_CLOUD) {
                data = loadWaypointsCloudString(kml);
            } else if (type == TYPE_ROUTE) {
                data = loadRouteString(kml);
            }

            if (data != null) {
                R.getFileSystem().saveString(FileSystem.FILES_FOLDER + GeoFiles.fileName(data.getName()), kml);
                //view alert
                if (!Sync.isRunning()) {
                    R.getCustomAlert().quickView(Locale.get("Waypoint_saved"), "info", "locify://geoFileBrowser");
                }
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.saveWaypoint", kml);
        }
    }

    /***************************************************/
    /*                 LOAD FUNCTIONS                  */
    /***************************************************/
    public static Route loadRouteFile(String filename, boolean onlyInfo) {
        try {
            return (Route) parseRouteOrCloud(filename, onlyInfo, ROUTE_TYPE_FILE);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.loadRoute()", filename);
            return null;
        }
    }

    public static Route loadRouteString(String data) {
        try {
            return (Route) parseRouteOrCloud(data, false, ROUTE_TYPE_STRING);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.loadRoute()", data);
            return null;
        }
    }

    /**
     * Loads KML waypoint from file and parses it to waypoint object
     * @param fileName file name of KML
     * @return object with waypoint properties
     */
    public static Waypoint loadWaypointFile(String fileName) {
        try {
            String kml = R.getFileSystem().loadString(FileSystem.FILES_FOLDER + fileName);
            return parseWaypoint(kml);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.loadWaypointFile", fileName);
            return null;
        }
    }

    public static Waypoint loadWaypointString(String data) {
        return parseWaypoint(data);
    }

    public static WaypointsCloud loadWaypointsCloudFile(String filename) {
        try {
            return (WaypointsCloud) parseRouteOrCloud(filename, false, WAYPOINT_CLOUD_TYPE_FILE);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.loadWaypointsCloudFile()", filename);
            return null;
        }
    }

    public static WaypointsCloud loadWaypointsCloudString(String kml) {
        try {
            return (WaypointsCloud) parseRouteOrCloud(kml, false, WAYPOINT_CLOUD_TYPE_STRING);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.loadWaypointsCloudString()", kml);
            return null;
        }
    }

    public static NetworkLink loadNetworkLinkString(String kml) {
        return parseNetworkLink(kml);
    }

    public static NetworkLink loadNetworkLinkFile(String file) {
        return parseNetworkLink(R.getFileSystem().loadString(file));
    }

    /****************************************************/
    /*                 PARSE FUNCTIONS                  */
    /****************************************************/

    public static MultiData parseKMLFile(String fileName) {
        try {
            FileConnection fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
            if (!fileConnection.exists()) {
                return null;
            }
            return parseKMLString(R.getFileSystem().loadString(FileSystem.FILES_FOLDER + fileName));
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static MultiData parseKMLString(String data) {
        MultiData multiData = new MultiData();
        GeoData actualGeoData = null;
        
        ByteArrayInputStream stream = null;
        InputStreamReader reader = null;

        XmlPullParser parser;
        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            reader = new InputStreamReader(stream);
            parser = new KXmlParser();
            parser.setInput(reader);

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

                                        if (sBefore == STATE_FOLDER) {
                                            ((WaypointsCloud) actualGeoData).addWaypoint(waypoint);
                                        } else if (sBefore == STATE_DOCUMENT) {
                                            multiData.addGeoData(waypoint);
                                        } else {
                                            Logger.log("GeoFiles.parseKML(" + data + ") - POINT error1!!!");
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            Logger.log("GeoFiles.parseKML(" + data + ") - POINT error2!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("linestring")) {
                        if (sActual == STATE_PLACEMARK) {
                            Route route = new Route();
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
                                    if (tagName.equalsIgnoreCase("placemark")) {
                                        route.name = name;
                                        route.description = description;
                                        multiData.addGeoData(route);
                                        break;
                                    }
                                }
                            }
                        } else {
                            Logger.log("GeoFiles.parseKML(" + data + ") - LINESTRING error!!!");
                        }
                    } else if (event == XmlPullParser.END_DOCUMENT) {
                        break;
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("folder")) {
                        setState(STATE_DOCUMENT);
                        multiData.addGeoData(actualGeoData);
                    }
                }
            }

            multiData.finalizeData();
            return multiData;
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "GeoFiles.parseRouteOrCloud()", fileOrData);
            Logger.debug("Parsing: wrongFile or data: " + data + "\n" + e.getMessage());
            return null;
        } finally {
            try {
                if (stream != null)
                    stream.close();
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
            }
        }
    }

    private static void setState(int state) {
        sBefore = sActual;
        sActual = state;
    }

    /**
     * Parser kml file to route object
     * @param kml kml data
     * @return routeData object
     */
    private static GeoData parseRouteOrCloud(String fileOrData, boolean onlyInfo, int dataType) {
//System.out.println("\nhe " + System.currentTimeMillis());
//System.out.println("Parse fileOrData: " + fileOrData + "  and dataType: " + dataType);
        FileConnection fileConnection = null;
        InputStream is = null;
        ByteArrayInputStream stream = null;
        InputStreamReader reader = null;

        XmlPullParser parser;

        try {
            String trash;
            String name = "";
            Route route = null;
            WaypointsCloud cloud = null;
            parser = new KXmlParser();
            int event;
            String tagName;

            if (dataType == ROUTE_TYPE_FILE || dataType == ROUTE_TYPE_STRING) {
                route = new Route();
                route.pointCount = 0;
                route.routeOnlyInfo = onlyInfo;
            } else if (dataType == WAYPOINT_CLOUD_TYPE_FILE || dataType == WAYPOINT_CLOUD_TYPE_STRING) {
                cloud = new WaypointsCloud();
            }

            if (dataType == ROUTE_TYPE_FILE || dataType == WAYPOINT_CLOUD_TYPE_FILE) {
                fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileOrData);
                if (!fileConnection.exists()) {
                    return null;
                }

                is = fileConnection.openInputStream();
                parser.setInput(is, "utf-8");
            } else if (dataType == ROUTE_TYPE_STRING || dataType == WAYPOINT_CLOUD_TYPE_STRING) {
                stream = new ByteArrayInputStream(UTF8.encode(fileOrData));
                reader = new InputStreamReader(stream);
                parser.setInput(reader);
            }

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();

                    if (tagName.equalsIgnoreCase("name")) {
                        name = parser.nextText();
                    } else if (tagName.equalsIgnoreCase("description")) {
                        trash = parser.nextText();

                        if (dataType == ROUTE_TYPE_FILE || dataType == ROUTE_TYPE_STRING) {
                            de.enough.polish.util.StringTokenizer token = new de.enough.polish.util.StringTokenizer(trash, "\n");
                            while (token.hasMoreTokens()) {
                                trash = token.nextToken();
                                if (trash.startsWith("      Route length: ")) {
                                    try {
                                        route.routeDist = Double.parseDouble(
                                                trash.substring("      Route length: ".length()));
                                    } catch (Exception e) {
                                        route.routeDist = 0;
                                    }

                                } else if (trash.startsWith("      Route travel time: ")) {
                                    try {
                                        route.routeTime = Long.parseLong(
                                                trash.substring("      Route travel time: ".length()));
                                    } catch (Exception e) {
                                        route.routeTime = 0;
                                    }

                                } else if (trash.startsWith("      Route points: ")) {
                                    try {
                                        route.pointCount = Integer.parseInt(
                                                trash.substring("      Route points: ".length()));
                                    } catch (Exception e) {
                                        route.pointCount = 0;
                                    }
                                }
                            }
                            route.description = trash;
                        } else if (dataType == WAYPOINT_CLOUD_TYPE_FILE || dataType == WAYPOINT_CLOUD_TYPE_STRING) {
                            cloud.description = trash;
                        }
                    } else if (tagName.equalsIgnoreCase("lineString") &&
                            (dataType == ROUTE_TYPE_FILE || dataType == ROUTE_TYPE_STRING)) {
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

                                    String data = "";
                                    de.enough.polish.util.StringTokenizer token = new de.enough.polish.util.StringTokenizer(coordinates, ' ');
                                    while (token.hasMoreTokens()) {
                                        data = token.nextToken();

                                        if (token.hasMoreTokens()) {
                                            route.points.addElement(new Location4D(
                                                    Double.parseDouble((String) token.nextToken()),
                                                    Double.parseDouble((String) data),
                                                    Float.parseFloat(token.nextToken())));
                                        }

                                        if (onlyInfo) {
                                            route.latitude = ((Location4D) route.points.elementAt(0)).getLatitude();
                                            route.longitude = ((Location4D) route.points.elementAt(0)).getLongitude();
                                            if (!name.equals("")) {
                                                route.name = name;
                                            } else {
                                                route.name = fileOrData;
                                            }
                                            return route;
                                        }
                                    }
                                }
                            } else if (event == XmlPullParser.END_TAG) {
                                tagName = parser.getName();
                                if (tagName.equalsIgnoreCase("lineString")) {
                                    break;
                                }
                            }
                        }
                    } else if (tagName.equalsIgnoreCase("folder") &&
                            (dataType == WAYPOINT_CLOUD_TYPE_FILE || dataType == WAYPOINT_CLOUD_TYPE_STRING)) {
                        Waypoint way = new Waypoint(0.0, 0.0, "", "");
                        while (true) {
                            event = parser.nextToken();
                            if (event == XmlPullParser.START_TAG) {
                                tagName = parser.getName();
//System.out.println("tagName: " + tagName);
                                if (tagName.equalsIgnoreCase("name")) {
//System.out.println(parser.isEmptyElementTag());

                                    way.name = parser.nextText();
//System.out.println(way.name);
                                } else if (tagName.equalsIgnoreCase("description")) {
//System.out.println(parser.isEmptyElementTag());
                                    way.description = parser.nextText();
//System.out.println(way.description);
                                } else if (tagName.equalsIgnoreCase("coordinates")) {
                                    String coordinates = parser.nextText();
                                    String[] parts = StringTokenizer.getArray(coordinates, ",");
                                    way.longitude = Double.parseDouble(parts[0]);
                                    way.latitude = Double.parseDouble(parts[1]);
                                }
                            } else if (event == XmlPullParser.END_TAG) {
                                tagName = parser.getName();
//System.out.println("tagName end: " + tagName);
                                if (tagName.equalsIgnoreCase("folder")) {
                                    break;
                                } else if (tagName.equalsIgnoreCase("placemark")) {
                                    cloud.addWaypoint(way);
                                    way = new Waypoint(0.0, 0.0, "", "");
                                }
                            }
                        }
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
//System.out.println(name + " end");
                    break;
                }
            }

            if (dataType == ROUTE_TYPE_FILE || dataType == ROUTE_TYPE_STRING) {
                route.pointCount = Math.max(route.pointCount, route.points.size());
                route.latitude = ((Location4D) route.points.elementAt(0)).getLatitude();
                route.longitude = ((Location4D) route.points.elementAt(0)).getLongitude();
                route.name = name;
                return route;
            } else if (dataType == WAYPOINT_CLOUD_TYPE_FILE || dataType == WAYPOINT_CLOUD_TYPE_STRING) {
                cloud.name = name;
                return cloud;
            }
            return null;
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "GeoFiles.parseRouteOrCloud()", fileOrData);
            Logger.debug("Parsing: wrongFile or data: " + fileOrData + "\n" + e.getMessage());
            return null;
        } finally {
            try {
                if (dataType == ROUTE_TYPE_FILE || dataType == WAYPOINT_CLOUD_TYPE_FILE) {
                    is.close();
                    fileConnection.close();
                } else if (dataType == ROUTE_TYPE_STRING || dataType == WAYPOINT_CLOUD_TYPE_STRING) {
                    stream.close();
                    reader.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Parser kml file to waypoint object
     * @param kml kml data
     * @return waypoint object
     */
    private static Waypoint parseWaypoint(String kml) {
        ByteArrayInputStream bais = null;
        try {
            Waypoint point = new Waypoint(0.0, 0.0, "", "");

            bais = new ByteArrayInputStream(UTF8.encode(kml));
            bais.reset();
            XmlPullParser parser = new KXmlParser();
            parser.setInput(bais, "utf-8");
            int event;
            String tagName;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("name")) {
                        point.name = parser.nextText();
                    } else if (tagName.equalsIgnoreCase("description")) {
                        point.description = parser.nextText();
                    } else if (tagName.equalsIgnoreCase("coordinates")) {
                        String coordinates = parser.nextText();
                        String[] parts = StringTokenizer.getArray(coordinates, ",");
                        point.longitude = Double.parseDouble(parts[0]);
                        point.latitude = Double.parseDouble(parts[1]);
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            return point;
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.parseWaypoint()", kml);
            return null;
        } finally {
            try {
                bais.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Parses kml file to networklink object
     * @param kml kml data
     * @return networklink object
     */
    private static NetworkLink parseNetworkLink(String kml) {
        ByteArrayInputStream bais = null;
        try {
            String name = "";
            String description = "";
            int refreshInterval = 10;
            String link = "";
            String viewFormat = "";

            bais = new ByteArrayInputStream(UTF8.encode(kml));
            bais.reset();
            XmlPullParser parser = new KXmlParser();
            parser.setInput(bais, "utf-8");
            int event;
            String tagName;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    if (tagName.equalsIgnoreCase("name")) {
                        name = parser.nextText();
                    } else if (tagName.equalsIgnoreCase("description")) {
                        description = parser.nextText();
                    } else if (tagName.equalsIgnoreCase("href")) {
                        link = parser.nextText();
                    } else if (tagName.equalsIgnoreCase("refreshInterval")) {
                        refreshInterval = Integer.parseInt(parser.nextText());
                    } else if (tagName.equalsIgnoreCase("viewFormat")) {
                        viewFormat = parser.nextText();
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            return new NetworkLink(name, description, refreshInterval, link, viewFormat);
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.parseNetworkLink()", kml);
            return null;
        } finally {
            try {
                bais.close();
            } catch (IOException ex) {
            }
        }
    }

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
            R.getErrorScreen().view(e, "WaypointData.fileName", name);
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
            R.getErrorScreen().view(e, "WaypointData.syncData", null);
            return "";
        }
    }

    public static int getDataTypeString(String data) {
        if (data.length() == 0) {
            return TYPE_CORRUPT;
        }

        if (data.indexOf("<LineString>") != -1 || data.indexOf("<linestring>") != -1 ||
                data.indexOf("<LineString>") != -1 || data.indexOf("<linestring>") != -1) {
            return TYPE_ROUTE;
        }

        if (data.indexOf("<Folder>") != -1 || data.indexOf("<folder>") != -1) {
            return TYPE_WAYPOINTS_CLOUD;
        }
        if (data.indexOf("<NetworkLink>") != -1 || data.indexOf("<networklink>") != -1) {
            return TYPE_NETWORKLINK;
        }

        return TYPE_WAYPOINT;
    }

    public static int getDataTypeFile(String fileName) {
        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;
        int actualType = TYPE_CORRUPT;

        try {
            parser = new KXmlParser();
            int event;
            String tagName;

            fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
            if (!fileConnection.exists()) {
                return TYPE_CORRUPT;
            }

            is = fileConnection.openInputStream();
            parser.setInput(is, "utf-8");

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
                    //System.out.println("Start-tag: " + parser.getName()) ;

                    if (tagName.equalsIgnoreCase("LineString")) {
                        return TYPE_ROUTE;
                    } else if (tagName.equalsIgnoreCase("Folder")) {
                        return TYPE_WAYPOINTS_CLOUD;
                    } else if (tagName.equalsIgnoreCase("NetworkLink")) {
                        return TYPE_NETWORKLINK;
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }
            return TYPE_WAYPOINT;
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "RouteData.isRoute", null);
            Logger.debug("Wrong file (RouteData.isRoute): " + fileName);
            return TYPE_CORRUPT;
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

    public static boolean isUnfinishedRoute() {
        try {
            if (!R.getFileSystem().exists(FileSystem.RUNNING_ROUTE_VARIABLES)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "RouteData.isUnfinishedRoute", null);
            return false;
        }
    }
}
