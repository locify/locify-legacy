/*
 * MultiData.java
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

import com.locify.client.utils.Logger;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.StringItem;
import de.enough.polish.util.ArrayList;
import de.enough.polish.util.Locale;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @author menion
 */
public class MultiGeoData {

    protected String name;
    protected String description;
    protected Hashtable styles;
    protected Hashtable stylesMap;

    private ArrayList geoData;

    public MultiGeoData() {
        name = "";
        description = "";
        geoData = new ArrayList();
    }

    public void addGeoData(GeoData actualGeoData) {
        geoData.add(actualGeoData);
    }

    public void finalizeData() {
//Logger.debug("  MultiGeoData.finalizeData() " + toString());
        try {
            GeoData data;
            for (int i = 0; i < geoData.size(); i++) {
                data = (GeoData) geoData.get(i);

                // first finalize GeoFileStyleMap
                if (stylesMap != null) {
                    Enumeration enu = stylesMap.elements();
                    GeoFileStyleMap geoFileStyleMap;
                    while (enu.hasMoreElements()) {
                        geoFileStyleMap = (GeoFileStyleMap) enu.nextElement();
                        geoFileStyleMap.styleNormal = getStyle(geoFileStyleMap.styleNormalUrl);
                        geoFileStyleMap.styleHighLight = getStyle(geoFileStyleMap.styleHighLightUrl);
                    }
                }

                // first finalize GeoFileStyleMap
//                if (styles != null) {
//                    Enumeration enu = styles.elements();
//                    GeoFileStyle geoFileStyle;
//                    while (enu.hasMoreElements()) {
//                        geoFileStyle = (GeoFileStyle) enu.nextElement();
//                        geoFileStyle.finalizeData();
//                    }
//                }

                // add style to data object
                if (data.styleName != null) {
                    GeoFileStyleMap gfsm = getStyleMap(data.styleName);
                    if (gfsm != null) {
                        data.styleNormal = gfsm.styleNormal;
                        data.styleHighlight = gfsm.styleHighLight;
                    } else {
                        data.styleNormal = getStyle(data.styleName);
                    }
                }

                if (data instanceof Route)
                    ((Route) geoData.get(i)).finalizeData();

                if (data.name.equals("")) {
                    if (data instanceof Route)
                        data.name = name + "_R";
                    if (data instanceof Waypoint)
                        data.name = name + "_W";
                    if (data instanceof WaypointsCloud)
                        data.name = name + "_C";
                }
            }
        } catch (Exception e) {
            Logger.error("MultiGeoData.finalize()");
        }
    }

    public String getName() {
        return name;
    }

    public String toString() {
        String data = "MultiData: \n";
        for (int i = 0; i < geoData.size(); i++) {
            data += (geoData.get(i).toString() + "\n");
        }
        data += " ... end of listing";
        return data;
    }

    public GeoFileStyle getStyle(String styleName) {
        if (styleName != null && styleName.length() > 0) {
            if (styles != null) {
                Object style = styles.get(styleName);
                if (style != null)
                    return (GeoFileStyle) style;
            }
        }
        return null;
    }

    public GeoFileStyleMap getStyleMap(String styleName) {
        if (styleName != null && styleName.length() > 0) {
            if (stylesMap != null) {
                Object style = stylesMap.get(styleName);
                if (style != null)
                    return (GeoFileStyleMap) style;
            }
        }
        return null;
    }

    /**
     * Return GeoData from object.
     * @param geoType Type of GeoData defined in GeoFiles.
     * @param index Index of this type. Eg. (GeoFiles.TYPE_WAYPOINT, 3)
     * mean third waypoint not trird object in array.
     * @return GeoData object or null if not exist.
     */
    public GeoData getGeoData(int geoType, int index) {
//Logger.debug("  MultiGeoData.getGeoData " + geoType + " " + index + "  actualData: " + toString());
        int count = 0;
        GeoData data;
        for (int i = 0; i < geoData.size(); i++) {
            data = (GeoData) geoData.get(i);
            switch (geoType) {
                case GeoFiles.TYPE_ROUTE:
                    if (data instanceof Route) {
                        if (count == index)
                            return data;
                        count++;
                    }
                    break;
                case GeoFiles.TYPE_WAYPOINT:
                    if (data instanceof Waypoint) {
                        if (count == index)
                            return data;
                        count++;
                    }
                    break;
                case GeoFiles.TYPE_WAYPOINTS_CLOUD:
                    if (data instanceof WaypointsCloud) {
                        if (count == index)
                            return data;
                        count++;
                    }
                    break;
                case GeoFiles.TYPE_NETWORKLINK:
                    if (data instanceof NetworkLink) {
                        if (count == index)
                            return data;
                        count++;
                    }
                    break;
            }
        }
        return null;
    }

    /**
     * Return size of internal GeoData object array.
     * @return Num of objects.
     */
    public int getDataSize() {
        return geoData.size();
    }

    public void addItemLabels(Form form) {
        GeoData item;
        for (int i = 0; i < geoData.size(); i++) {
            item = (GeoData) geoData.get(i);
            if (item instanceof Route) {
                Route route = (Route) item;
                form.append(new StringItem(Locale.get("Route") + ": " + route.getName(),
                        "  (" + route.pointCount + " " + Locale.get("Points") + ")"));
            } else if (item instanceof Waypoint) {
                Waypoint wpt = (Waypoint) item;
                form.append(new StringItem(Locale.get("Waypoint") + ": " + wpt.getName(),
                        "  "));
            } else if (item instanceof WaypointsCloud) {
                WaypointsCloud wptCloud = (WaypointsCloud) item;
                form.append(new StringItem(Locale.get("Waypoint_cloud") + ": " + wptCloud.getName(),
                        "  (" + wptCloud.getWaypointsCount() + " " + Locale.get("Points") + ")"));
            }
        }
    }
    
    /**
     * Return GeoData from object.
     * @param index Index of object in internal array.
     * @return GeoData object or null if index too high.
     */
    public GeoData getGeoData(int index) {
        if (index < getDataSize()) {
            return (GeoData) geoData.get(index);
        } else {
            return null;
        }
    }
}
