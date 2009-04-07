/*
 * WaypointsCloud.java
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

import com.locify.client.locator.Location4D;
import com.locify.client.utils.Logger;
import java.util.Vector;

/**
 * Object representing multiple waypoints
 * @author MenKat
 */
public class WaypointsCloud extends GeoData {

    /** vector containing waypoints */
    private Vector waypoints;
    private boolean initialized;
    protected Location4D centerLocation;

    public WaypointsCloud() {
        super();
        waypoints = new Vector();
        initialized = false;
        centerLocation = new Location4D(0.0, 0.0, 0.0f);
    }

    public WaypointsCloud(String name) {
        this();
        this.name = name;
    }

    public String toString() {
        String data = super.toString();
        for (int i = 0; i < waypoints.size(); i++) {
            data += ("wpt: " + ((Waypoint) waypoints.elementAt(i)).toString() + "\n");
        }
        return data;
    }
    /**
     * Get vector of Waypoints points
     * @return Waypoints points
     */
    public Vector getWaypointsCloudPoints() {
        return waypoints;
    }
    
    public Location4D getCenterLocation() {
        if (waypoints.size() != 0) {
            if (!initialized) {
                double lat = 0;
                double lon = 0;
                float alt = 0;
                for (int i = 0; i < waypoints.size(); i++) {
                    lat += ((Waypoint) waypoints.elementAt(i)).latitude;
                    lon += ((Waypoint) waypoints.elementAt(i)).longitude;
                    alt += ((Waypoint) waypoints.elementAt(i)).getLocation().getAltitude();
                }
                centerLocation.setLatitude(lat/waypoints.size());
                centerLocation.setLongitude(lon/waypoints.size());
                centerLocation.setAltitude(alt/waypoints.size());
                initialized = true;
            }
            return centerLocation;
        }
        return new Location4D(0.0, 0.0, 0.0f);
    }

    public void clearWaypoints() {
        waypoints.removeAllElements();
    }

    public void addWaypoint(Waypoint waypoint) {
//Logger.debug("  WaypointsCloud.addWaypoint() " + waypoint.toString());
        waypoints.addElement(waypoint);
        initialized = false;
    }

    public int getWaypointsCount() {
        return waypoints.size();
    }

    public Waypoint getWaypoint(int index) {
        return (Waypoint) waypoints.elementAt(index);
    }
}
