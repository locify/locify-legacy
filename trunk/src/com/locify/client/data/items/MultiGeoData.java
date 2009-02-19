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

import de.enough.polish.util.ArrayList;

/**
 *
 * @author menion
 */
public class MultiGeoData {

    protected String name;
    protected String description;

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
        GeoData data;
        for (int i = 0; i < geoData.size(); i++) {
            data = (GeoData) geoData.get(i);
            if (geoData.get(i) instanceof Route)
                ((Route) geoData.get(i)).processDescription();

            if (data.name.equals("")) {
                if (data instanceof Route)
                    data.name = name + "_R";
                if (data instanceof Waypoint)
                    data.name = name + "_W";
                if (data instanceof WaypointsCloud)
                    data.name = name + "_C";
            }
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

    /**
     * Return GeoData from object.
     * @param geoType Type of GeoData defined in GeoFiles.
     * @param index Index of this type. Eg. (GeoFiles.TYPE_WAYPOINT, 3)
     * mean third waypoint not trird object in array.
     * @return GeoData object or null if not exist.
     */
    public GeoData getGeoData(int geoType, int index) {
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
