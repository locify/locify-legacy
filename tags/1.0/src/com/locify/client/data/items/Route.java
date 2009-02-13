/*
 * Route.java
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
import java.util.Vector;

/**
 * Object representing route
 * @author MenKat
 */
public class Route extends GeoData {
    
    /** vector containing Loaction4D data */
    protected Vector points;
    /* vector of integers defines which point is at new line */
    protected Vector separating;

    /* state or temp variables */
    protected boolean routeOnlyInfo;

    protected double routeDist;
    protected long routeTime;
    protected int pointCount;

    public Route() {
        super();
        points = new Vector();
        separating = new Vector();
        routeOnlyInfo = true;

        routeDist = 0;
        routeTime = 0;
        pointCount = 0;
    }
    
    /**
     * Get vector of Location4D points
     * @return Location4D points
     */
    public Vector getPoints() {
        return points;
    }

    public Vector getSeparating() {
        return separating;
    }

    public Location4D[] getRoutePoints2() {
        if (points == null) {
            return null;
        }

        Location4D[] rPoints = new Location4D[points.size()];
        for (int i = 0; i < points.size(); i++) {
            rPoints[i] = (Location4D) points.elementAt(i);
        }

        return rPoints;
    }

    public Location4D getFirstPoint() {
        return new Location4D(latitude, longitude, 0.0f);
    }
    
    public Waypoint getFirstWaypoint() {
        return new Waypoint(latitude, longitude, "waypoint 01", "");
    }

    public Location4D getLastPoint() {
        if (points.size() > 1) {
            return (Location4D) points.elementAt(points.size() - 1);
        } else {
            return null;
        }
    }
    
    public Waypoint getLastWaypoint() {
        Location4D last = getLastPoint();
        if (last != null) {
        return new Waypoint(last.getLatitude(),
                last.getLongitude(), "waypoint " + pointCount, "");
        } else {
            return null;
        }
    }

    public double getRouteDist() {
        return routeDist;
    }

    public long getRouteTime() {
        return routeTime;
    }

    public int getWaypointCount() {
        return pointCount;
    }
    
    public boolean isRouteOnlyInfo() {
        return routeOnlyInfo;
    }
}
