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
import com.locify.client.locator.Location4D;
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.GpsUtils;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.locify.client.utils.StringTokenizer;
import com.locify.client.utils.UTF8;
import com.locify.client.utils.Utils;
import de.enough.polish.util.Locale;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
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
    // kml tags
    private static final int STATE_DOCUMENT = 1;
    private static final int STATE_FOLDER = 2;
    private static final int STATE_PLACEMARK = 3;
    private static final int STATE_STYLE = 4;
    private static final int STATE_STYLE_MAP = 4;
    private static final int STATE_SCREEN_OVERLAY = 5;
    // gpx tags
    private static final int STATE_RTE = 11;
    private static final int STATE_TRK = 12;
    private static final int STATE_WPT = 13;

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
            R.getCustomAlert().quickView(Locale.get("Object_saved"), "info", "locify://refresh");
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
        R.getFileSystem().renameFile(FileSystem.RUNNING_TEMP_ROUTE, fileName);
    }

    public static String correctStringData(String string) {
        byte[] data = string.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        int i;
        int lastI = 0;
        while ((i = bais.read()) != -1) {
            // 10 - LF, 13 - CR
            if (i == 10 && lastI != 13) {
                baos.write(13);
                baos.write(10);
            } else {
                baos.write(i);
            }
            lastI = i;
        //System.out.println(i + " " + ((char) i));
        }
        return baos.toString();
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
            String fileName = fileName(name);
            //create kml
            String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<kml xmlns=\"http://earth.google.com/kml/2.2\">\n  <Placemark>\n    <name>" + name + "</name>\n    <description>" + description + "</description>\n    <Point>\n      <coordinates>" + longitude + "," + latitude + "</coordinates>\n    </Point>\n  </Placemark>\n</kml>";
            //write file
            R.getFileSystem().saveString(FileSystem.FILES_FOLDER + fileName, kml);
            //view alert
            R.getCustomAlert().quickView(Locale.get("Waypoint_saved"), "info", "locify://back");
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
            String name = parseGeoDataString(kml, true).getName();

            if (type != TYPE_CORRUPT && name != null && name.length() > 0) {
                R.getFileSystem().saveString(FileSystem.FILES_FOLDER + fileName(name), kml);
                //view alert
                R.getCustomAlert().quickView(Locale.get("Waypoint_saved"), "info", "locify://geoFileBrowser");
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "GeoFiles.saveWaypoint", kml);
        }
    }

    /****************************************************/
    /*                 PARSE FUNCTIONS                  */
    /****************************************************/
    public static MultiGeoData parseGeoDataFile(String fileName, boolean firstNameOnly) {
        FileConnection fileConnection = null;
        InputStream is = null;
        XmlPullParser parser;

        try {
//System.out.println("\n " + "file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
            fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
            if (!fileConnection.exists()) {
                return null;
            }
//System.out.println("\n " + fileName);
            is = fileConnection.openInputStream();
            parser = new KXmlParser();
            parser.setInput(is, "utf-8");

            MultiGeoData multiGeoData = new MultiGeoData();
            multiGeoData.name = fileName;
            if (fileName.endsWith("kml")) {
                multiGeoData = parseKml(parser, firstNameOnly, multiGeoData);
            } else if (fileName.endsWith("gpx")) {
                multiGeoData = parseGpx(parser, firstNameOnly, multiGeoData);
            }
            return multiGeoData;
        } catch (Exception e) {
            //R.getErrorScreen().view(e, "RouteData.isRoute", null);
            Logger.error("GeoFiles.parseKmlFile() - wrong file: " + fileName + " ex: " + e.toString());
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

    public static MultiGeoData parseGeoDataString(String data, boolean firstNameOnly) {
        ByteArrayInputStream stream = null;
        XmlPullParser parser;

        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            parser = new KXmlParser();
            parser.setInput(new InputStreamReader(stream));

            MultiGeoData multiGeoData = new MultiGeoData();
            multiGeoData.name = "unname data";
            if (data.indexOf("<kml xmlns=") != -1) {
                multiGeoData = parseKml(parser, firstNameOnly, multiGeoData);
            } else if (data.indexOf("<gpx xmlns=") != -1) {
                multiGeoData = parseGpx(parser, firstNameOnly, multiGeoData);
            }
            return multiGeoData;
        } catch (Exception e) {
            Logger.error("Parsing: wrongFile or data: " + data + " ex: " + e.toString());
            return null;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static MultiGeoData parseKml(XmlPullParser parser, boolean firstNameOnly, MultiGeoData multiData) {
        GeoData actualGeoData = null;

        try {
            String name = "";
            String description = "";
            String id = null;

            Waypoint waypoint = null;
            Hashtable styles = null;
            Hashtable stylesMap = null;
            GeoFileStyle style = null;
            GeoFileStyleMap styleMap = null;
            String styleURL = null;

            int event;
            String tagName;

            sBefore = STATE_NONE;
            sActual = STATE_NONE;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
//Logger.debug("  parseKML - tagName: " + tagName);
                    if (tagName.equalsIgnoreCase("Document")) {
                        try {
                            setState(STATE_DOCUMENT);
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Document' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Description")) {
                        try {
                            if (sActual == STATE_DOCUMENT) {
                                multiData.description = parser.nextText();
                                description = "";
                            } else if (sActual == STATE_SCREEN_OVERLAY) {
                                multiData.screenOverlay = parser.nextText();
                                setState(STATE_DOCUMENT);
                            } else {
                                description = parser.nextText();
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Description' tag error: " + e.toString());
                        }
                    /* it will be only cloud and containts only placemark tags */
                    } else if (tagName.equalsIgnoreCase("Folder")) {
                        try {
                            setState(STATE_FOLDER);
                            actualGeoData = new WaypointsCloud("");
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Folder' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("IconStyle")) {
                        try {
                            boolean inIcon = false;
                            if (sActual == STATE_STYLE) {
                                while (true) {
                                    event = parser.nextToken();
                                    if (event == XmlPullParser.START_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("Scale")) {
                                            style.scale = GpsUtils.parseFloat(parser.nextText());
                                            inIcon = false;
                                        } else if (tagName.equalsIgnoreCase("Icon")) {
                                            inIcon = true;
                                        } else if (tagName.equalsIgnoreCase("Href")) {
                                            if (inIcon) {
                                                style.setIcon(parser.nextText());
                                            }
                                        } else if (tagName.equalsIgnoreCase("HotSpot")) {
                                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                                if (parser.getAttributeName(i).equalsIgnoreCase("x")) {
                                                    style.hotSpotX = GpsUtils.parseInt(parser.getAttributeValue(i));
                                                } else if (parser.getAttributeName(i).equalsIgnoreCase("y")) {
                                                    style.hotSpotY = GpsUtils.parseInt(parser.getAttributeValue(i));
                                                } else if (parser.getAttributeName(i).equalsIgnoreCase("xunits")) {
                                                    style.hotSpotXunits = parser.getAttributeValue(i);
                                                } else if (parser.getAttributeName(i).equalsIgnoreCase("yunits")) {
                                                    style.hotSpotYunits = parser.getAttributeValue(i);
                                                }
                                            }
                                            inIcon = false;
                                        }
                                    } else if (event == XmlPullParser.END_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("IconStyle")) {
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'IconStyle' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Linestring")) {
                        try {
                            if (sActual == STATE_PLACEMARK) {
                                if (actualGeoData == null || !(actualGeoData instanceof Route)) {
                                    actualGeoData = new Route();
                                }

                                Route route = (Route) actualGeoData;

                                while (true) {
                                    event = parser.nextToken();
                                    if (event == XmlPullParser.START_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("Coordinates")) {
                                            if (route.points.size() != 0) {
                                                route.separating.addElement(new Integer(route.points.size()));
                                            }
                                            String coordinates = parser.nextText();
                                            coordinates = coordinates.replace('\t', ' ');
                                            coordinates = coordinates.replace('\n', ' ');
                                            coordinates = coordinates.trim();

                                            Vector token = StringTokenizer.getVector(coordinates, " ");

                                            double lat = 0.0, lon = 0.0;
                                            float alt = 0.0f;

                                            for (int i = 0; i < token.size(); i++) {
                                                de.enough.polish.util.StringTokenizer tokenPoint = new de.enough.polish.util.StringTokenizer((String) token.elementAt(i), ',');
                                                while (tokenPoint.hasMoreTokens()) {
                                                    lon = Double.parseDouble((String) tokenPoint.nextToken());
                                                    if (tokenPoint.hasMoreTokens()) {
                                                        lat = Double.parseDouble((String) tokenPoint.nextToken());
                                                        alt = 0.0f;
                                                        if (tokenPoint.hasMoreTokens()) {
                                                            alt = Float.parseFloat((String) tokenPoint.nextToken());
                                                        }
//Logger.log("Add: " + lat + " " + lon + " " + alt);
                                                        route.points.addElement(new Location4D(lat, lon, alt));
                                                    }
                                                }
                                            }
//Logger.log("Route: " + route.points.size());
                                        }
                                    } else if (event == XmlPullParser.END_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("Linestring")) {
                                            break;
                                        }
                                    }
                                }
                            } else {
                                Logger.warning("GeoFiles.parseKml() - LINESTRING error!!!");
                                return null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Linestring' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Name")) {
                        try {
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
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Name' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Networklink")) {
                        try {
                            NetworkLink nl = new NetworkLink();
                            while (true) {
                                event = parser.nextToken();
                                if (event == XmlPullParser.START_TAG) {
                                    tagName = parser.getName();
                                    if (tagName.equalsIgnoreCase("Name")) {
                                        nl.name = parser.nextText();
                                    } else if (tagName.equalsIgnoreCase("Description")) {
                                        nl.description = parser.nextText();
                                    } else if (tagName.equalsIgnoreCase("Href")) {
                                        nl.link = parser.nextText();
                                    } else if (tagName.equalsIgnoreCase("RefreshInterval")) {
                                        nl.refreshInterval = Integer.parseInt(parser.nextText());
                                    } else if (tagName.equalsIgnoreCase("ViewFormat")) {
                                        nl.viewFormat = parser.nextText();
                                    }
                                } else if (event == XmlPullParser.END_TAG) {
                                    tagName = parser.getName();
                                    if (tagName.equalsIgnoreCase("Networklink")) {
                                        multiData.addGeoData(nl);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Networklink' tag error! "+e);
                        }
                    } else if (tagName.equalsIgnoreCase("Pair")) {
                        try {
                            String actualKey = "";
                            String actualStyleUrl = "";
                            if (sActual == STATE_STYLE_MAP) {
                                while (true) {
                                    event = parser.nextToken();
                                    if (event == XmlPullParser.START_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("Key")) {
                                            actualKey = parser.nextText();
                                        } else if (tagName.equalsIgnoreCase("StyleUrl")) {
                                            actualStyleUrl = parser.nextText().substring(1);
                                        }
                                    } else if (event == XmlPullParser.END_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("Pair")) {
                                            styleMap.setPair(actualKey, actualStyleUrl);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Pair' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Placemark")) {
                        try {
                            setState(STATE_PLACEMARK);
                            id = parser.getAttributeValue(null, "id");
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Placemark' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Point")) {
                        try {
//Logger.debug("  parseKML - tagPoint");
                            /* sActual is always placemark but sBefore may be FOLDER
                             * (waypoint_cloud) or DOCUMENT (waypoint) */
                            if (sActual == STATE_PLACEMARK) {
                                waypoint = new Waypoint(0.0, 0.0, "", "", null);
                                while (true) {
                                    event = parser.nextToken();
                                    if (event == XmlPullParser.START_TAG) {
                                        tagName = parser.getName();
//Logger.debug("  parseKML - tagPoint - tagName:" + tagName);
                                        if (tagName.equalsIgnoreCase("Coordinates")) {
                                            String coordinates = parser.nextText();
                                            String[] parts = StringTokenizer.getArray(coordinates, ",");
                                            waypoint.setLongitude(Double.parseDouble(parts[0]));
                                            waypoint.setLatitude(Double.parseDouble(parts[1]));
                                        } else if (tagName.equalsIgnoreCase("value")) {
                                            //this is little stupid, because <value> has to be inside <data name="service"> and <extendedData>.
                                            //but we don't use it value anywhere else in KML, so for now it's enough
                                            waypoint.service = parser.nextText();
                                        }
                                    } else if (event == XmlPullParser.END_TAG) {
                                        tagName = parser.getName();
//Logger.debug("  parseKML - tagPoint - tagNameEnd:" + tagName);
                                        if (tagName.equalsIgnoreCase("Placemark")) {
//Logger.debug("  parseKML - tagPoint - tagNameEnd data:" + name + " " + description);
                                            waypoint.name = name;
                                            waypoint.description = description;
                                            waypoint.styleName = styleURL;
                                            waypoint.id = id;
                                            styleURL = null;
                                            if (sBefore == STATE_FOLDER || sBefore == STATE_PLACEMARK) {
                                                ((WaypointsCloud) actualGeoData).addWaypoint(waypoint);
                                            } else if (sBefore == STATE_DOCUMENT || sBefore == STATE_NONE) {
                                                multiData.addGeoData(waypoint);
                                            } else {
                                                Logger.warning("GeoFiles.parseKml() - POINT error1!!!");
                                                return null;
                                            }
                                            break;
                                        }
                                    }
                                }
                            } else {
                                Logger.warning("GeoFiles.parseKml() - POINT error2!!!");
                                return null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Point' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Style")) {
                        try {
                            if (sActual == STATE_DOCUMENT) {
                                setState(STATE_STYLE);
                                if (styles == null) {
                                    styles = new Hashtable();
                                }
                                for (int i = 0; i < parser.getAttributeCount(); i++) {
                                    if (parser.getAttributeName(i).equalsIgnoreCase("id")) {
                                        style = new GeoFileStyle(parser.getAttributeValue(i));
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Style' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("StyleMap")) {
                        try {
                            if (sActual == STATE_DOCUMENT) {
                                setState(STATE_STYLE_MAP);
                                if (stylesMap == null) {
                                    stylesMap = new Hashtable();
                                }
                                for (int i = 0; i < parser.getAttributeCount(); i++) {
                                    if (parser.getAttributeName(i).equalsIgnoreCase("id")) {
                                        styleMap = new GeoFileStyleMap(parser.getAttributeValue(i));
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'StyleMap' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("StyleUrl")) {
                        try {
                            // style begin with '#'
                            styleURL = parser.nextText().substring(1);
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'StyleUrl' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("ScreenOverlay")) {
                        setState(STATE_SCREEN_OVERLAY);
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
//Logger.debug("  parseKML - tagNameEnd:" + tagName);
                    if (tagName.equalsIgnoreCase("Folder")) {
                        try {
                            setState(STATE_DOCUMENT);
                            if (actualGeoData != null && actualGeoData instanceof WaypointsCloud) {
                                multiData.addGeoData(actualGeoData);
                                actualGeoData = null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Folder' endTag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Placemark")) {
                        try {
                            if (actualGeoData != null && actualGeoData instanceof Route) {
                                actualGeoData.name = name;
                                actualGeoData.description = description;
                                actualGeoData.styleName = styleURL;
                                styleURL = null;
                                ((Route) actualGeoData).routeOnlyInfo = firstNameOnly;
                                multiData.addGeoData(actualGeoData);
                                actualGeoData = null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Placemark' endTag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("Style")) {
                        try {
                            if (style.getIconUrl() != null) {
                                styles.put(style.name, style);
                            }
                            setState(STATE_DOCUMENT);
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'Style' endTag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("StyleMap")) {
                        try {
                            stylesMap.put(styleMap.name, styleMap);
                            setState(STATE_DOCUMENT);
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseKml() - 'StyleMap' endTag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("kml")) {
                        break;
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }

            multiData.stylesMap = stylesMap;
            multiData.styles = styles;
            multiData.finalizeData();
            return multiData;
        } catch (Exception e) {
            Logger.error("GeoFiles.parseKml() - FINAL error!!!\n" + e.getMessage());
            return null;
        }
    }

    private static MultiGeoData parseGpx(XmlPullParser parser, boolean firstNameOnly, MultiGeoData multiData) {
        GeoData actualGeoData = null;

        try {
            Waypoint waypoint = null;
            WaypointsCloud waypointCloud = new WaypointsCloud();

            int event;
            String tagName;

            sBefore = STATE_NONE;
            sActual = STATE_NONE;

            while (true) {
                event = parser.nextToken();
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
//Logger.debug("  parseKML - tagName: " + tagName);
                    if (tagName.equalsIgnoreCase("name")) {
                        try {
                            if (firstNameOnly) {
                                multiData.name = parser.nextText();
                                return multiData;
                            }

                            if (actualGeoData != null) {
                                actualGeoData.name = parser.nextText();
                            } else {
                                multiData.name = parser.nextText();
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseGpx() - 'Name' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("rte")) {
                        try {
                            if (sActual == STATE_NONE) {
                                setState(STATE_RTE);
                                actualGeoData = new Route();
                            } else {
                                Logger.warning("GeoFiles.parseGpx() - 'rte' name error!!!");
                                return null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseGpx() - 'rte' error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("rtept")) {
                        try {
                            if (sActual == STATE_RTE) {
                                Route route = (Route) actualGeoData;
                                double lat = GpsUtils.parseDouble(parser.getAttributeValue(null, "lat"));
                                double lon = GpsUtils.parseDouble(parser.getAttributeValue(null, "lon"));

                                route.points.addElement(new Location4D(lat, lon, 0.0f));
                            }
                        } catch (Exception ex) {
                            Logger.warning("GeoFiles.parseGpx() - 'rtept' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("trk")) {
                        try {
                            if (sActual == STATE_NONE) {
                                setState(STATE_TRK);
                                actualGeoData = new Route();
                            } else {
                                Logger.warning("GeoFiles.parseGpx() - 'trk' name error!!!");
                                return null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseGpx() - 'rte' error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("trkpt")) {
                        try {
                            if (sActual == STATE_TRK) {
                                Route route = (Route) actualGeoData;
                                double lat = GpsUtils.parseDouble(parser.getAttributeValue(null, "lat"));
                                double lon = GpsUtils.parseDouble(parser.getAttributeValue(null, "lon"));

                                route.points.addElement(new Location4D(lat, lon, 0.0f));
                            }
                        } catch (Exception ex) {
                            Logger.warning("GeoFiles.parseGpx() - 'rtept' tag error!!!");
                        }
                    } else if (tagName.equalsIgnoreCase("wpt")) {
                        try {
                            if (sActual == STATE_NONE || sActual == STATE_RTE) {
                                double lat = GpsUtils.parseDouble(parser.getAttributeValue(null, "lat"));
                                double lon = GpsUtils.parseDouble(parser.getAttributeValue(null, "lon"));
                                waypoint = new Waypoint(lat, lon, "", "", "");

                                while (true) {
                                    event = parser.nextToken();
                                    if (event == XmlPullParser.START_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("name")) {
                                            waypoint.name = parser.nextText();
                                        } else if (tagName.equalsIgnoreCase("desc")) {
                                            waypoint.description = parser.nextText();
                                        } else if (tagName.equalsIgnoreCase("ele")) {
                                            waypoint.setAltitude(GpsUtils.parseFloat(parser.nextText()));
                                        }
                                    } else if (event == XmlPullParser.END_TAG) {
                                        tagName = parser.getName();
                                        if (tagName.equalsIgnoreCase("wpt")) {
                                            break;
                                        }
                                    }
                                }
//Logger.log("Add WPT: " + waypoint.toString());
                                waypointCloud.addWaypoint(waypoint);
                            } else {
                                Logger.warning("GeoFiles.parseGpx() - 'rte' name error!!!");
                                return null;
                            }
                        } catch (Exception e) {
                            Logger.warning("GeoFiles.parseGpx() - 'rte' error!!!");
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
//Logger.debug("  parseKML - tagNameEnd:" + tagName);
                    if (tagName.equalsIgnoreCase("gpx")) {
                        break;
                    } else if (tagName.equalsIgnoreCase("rte")) {
                        multiData.addGeoData(actualGeoData);
                    } else if (tagName.equalsIgnoreCase("trk")) {
                        multiData.addGeoData(actualGeoData);
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }

            if (waypointCloud.getWaypointsCount() == 1) {
                multiData.addGeoData(waypointCloud.getWaypoint(0));
            } else if (waypointCloud.getWaypointsCount() > 1) {
                multiData.addGeoData(waypointCloud);
            }
            multiData.finalizeData();
            return multiData;
        } catch (Exception e) {
            Logger.error("GeoFiles.parseGpx() - FINAL error!!!\n" + e.getMessage());
            return null;
        }
    }

    private static void setState(int state) {
//Logger.debug("  parseKML - setState: " + state);
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
//Logger.debug("GeoFiles.getDataTypeFile() - " + fileName);
            fileSize = R.getFileSystem().getSize(FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName, FileSystem.SIZE_FILE);
            type = getDataTypeDatabase(fileName, fileSize);

//Logger.debug("  testFile: " + fileName + " type: " + type + " size: " + fileSize);
            if (type == 0) {
                fileConnection = (FileConnection) Connector.open("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName);
                if (!fileConnection.exists()) {
//Logger.debug("  connect to: " + ("file:///" + FileSystem.ROOT + FileSystem.FILES_FOLDER + fileName) + " problem, return corrupt result");
                    return TYPE_CORRUPT;
                }

                is = fileConnection.openInputStream();
                parser = new KXmlParser();
                parser.setInput(is, "utf-8");

                if (fileName.endsWith("kml")) {
                    type = getDataTypeKml(parser);
                } else if (fileName.endsWith("gpx")) {
                    type = getDataTypeGpx(parser);
                }
//Logger.debug(fileName + " return parser type: " + type);
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
        XmlPullParser parser;

        try {
            stream = new ByteArrayInputStream(UTF8.encode(data));
            parser = new KXmlParser();
            parser.setInput(new InputStreamReader(stream));

            if (data.indexOf("<kml xmlns=") != -1) {
                return getDataTypeKml(parser);
            } else if (data.indexOf("<gpx xmlns=") != -1) {
                return getDataTypeGpx(parser);
            }
            return 0;
        } catch (Exception e) {
            Logger.debug("Parsing: wrongFile or data: " + data + "\n" + e.getMessage());
            return TYPE_CORRUPT;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static int getDataTypeKml(XmlPullParser parser) {
        int actualType = TYPE_CORRUPT;
        boolean containPlacemark = false;

        try {
            int event;
            String tagName;
//for (int i = 0; i < XmlPullParser.TYPES.length; i++) {
//    Logger.debug("Type: " + XmlPullParser.TYPES[i] + " (" + i + ")");
//}
            while (true) {
                event = parser.nextToken();
//Logger.debug("    parserTag: token: " + event);
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
//Logger.debug("    parserTag: " + tagName);
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
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
//Logger.debug("    parserTagEnd: " + tagName);
                    if (tagName.equalsIgnoreCase("kml")) {
                        break;
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }

            if (actualType == TYPE_CORRUPT) {
//Logger.debug("  almost result - containPlacemark: " + containPlacemark);
                if (containPlacemark) {
                    return TYPE_WAYPOINT;
                } else {
                    return TYPE_CORRUPT;
                }
            } else {
                return actualType;
            }
        } catch (Exception e) {
            Logger.debug("GeoFiles.getDataType() err:" + e.toString());
            return TYPE_CORRUPT;
        }
    }

    private static int getDataTypeGpx(XmlPullParser parser) {
        int actualType = TYPE_CORRUPT;

        try {
            int event;
            String tagName;
            int waypointCount = 0;
//for (int i = 0; i < XmlPullParser.TYPES.length; i++) {
//    Logger.debug("Type: " + XmlPullParser.TYPES[i] + " (" + i + ")");
//}
            while (true) {
                event = parser.nextToken();
//Logger.debug("    parserTag: token: " + event);
                if (event == XmlPullParser.START_TAG) {
                    tagName = parser.getName();
//Logger.debug("    parserTag: " + tagName);
                    if (tagName.equalsIgnoreCase("rte")) {
                        if (actualType == TYPE_CORRUPT || actualType == TYPE_ROUTE) {
                            actualType = TYPE_ROUTE;
                        } else {
                            return TYPE_MULTI;
                        }
                    } else if (tagName.equalsIgnoreCase("wpt")) {
                        if (actualType == TYPE_CORRUPT || actualType == TYPE_WAYPOINTS_CLOUD) {
                            waypointCount++;
                            actualType = TYPE_WAYPOINTS_CLOUD;
                        } else {
                            return TYPE_MULTI;
                        }
                    } else if (tagName.equalsIgnoreCase("trk")) {
                        if (actualType == TYPE_CORRUPT || actualType == TYPE_ROUTE) {
                            actualType = TYPE_ROUTE;
                        } else {
                            return TYPE_MULTI;
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    tagName = parser.getName();
//Logger.debug("    parserTagEnd: " + tagName);
                    if (tagName.equalsIgnoreCase("gpx")) {
                        break;
                    }
                } else if (event == XmlPullParser.END_DOCUMENT) {
                    break;
                }
            }

            if (actualType == TYPE_CORRUPT) {
                return TYPE_CORRUPT;
            } else {
//Logger.log("ActualType: " + actualType);
                if (actualType == TYPE_WAYPOINTS_CLOUD && waypointCount == 1)
                    return TYPE_WAYPOINT;
                return actualType;
            }
        } catch (Exception e) {
            Logger.debug("GeoFiles.getDataType() err:" + e.toString());
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
//Logger.debug("  database compare succes, type: " + gft.getType());
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
        if (geoTypeDatabase == null) {
            geoTypeDatabase = new Vector();
        }

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
            //Logger.log("GeoFiles.saveDataTypeDatabase() succesfully saved!");
            } catch (RecordStoreException ex) {
                ex.printStackTrace();
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

            String[] pattern = {"*.kml"};
            Vector files = R.getFileSystem().listFiles(FileSystem.FILES_FOLDER, pattern);
            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    String fileName = (String) files.elementAt(i);

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

