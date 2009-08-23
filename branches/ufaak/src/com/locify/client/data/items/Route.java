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
import com.locify.client.route.RouteVariables;
import com.locify.client.utils.Logger;
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

    public String toString() {
        return "Route: " + super.toString() + " points: " + points.size() + " style: " + styleName;
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
        return new Location4D(getLatitude(), getLongitude(), 0.0f);
    }

    public Waypoint getFirstWaypoint() {
        return new Waypoint(getLatitude(), getLongitude(), "waypoint 01", "", null);
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
                last.getLongitude(), "waypoint " + pointCount, "", null);
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

    public void finalizeData() {
        try {
            if (description != null && description.length() > 0) {
                String trash;
                de.enough.polish.util.StringTokenizer token = new de.enough.polish.util.StringTokenizer(description.trim(), "\n");
                description = "";
                while (token.hasMoreTokens()) {
                    trash = token.nextToken().trim();
                    if (trash.startsWith(RouteVariables.DESC_LENGTH) && trash.length() > RouteVariables.DESC_LENGTH.length() + 3) {
                        routeDist = com.locify.client.utils.GpsUtils.parseDouble(
                                trash.substring(RouteVariables.DESC_LENGTH.length(), trash.length() - 2).trim());
                    } else if (trash.startsWith(RouteVariables.DESC_TRAVEL_TIME) && trash.length() > RouteVariables.DESC_TRAVEL_TIME.length() + 5) {
                        routeTime = com.locify.client.utils.GpsUtils.parseLong(
                                trash.substring(RouteVariables.DESC_TRAVEL_TIME.length(), trash.length() - 3).trim());
                    } else if (trash.startsWith(RouteVariables.DESC_POINTS)) {
                        pointCount = com.locify.client.utils.GpsUtils.parseInt(
                                trash.substring(RouteVariables.DESC_POINTS.length()).trim());
                    } else {
                        description += (trash + "\n");
                    }
                }
            }
            if (points.size() > 0) {
                pointCount = Math.max(pointCount, points.size());
                setLatitude(((Location4D) points.elementAt(0)).getLatitude());
                setLongitude(((Location4D) points.elementAt(0)).getLongitude());
            }
        } catch (Exception ex) {
            Logger.error("Route.finalizeData() " + ex.toString());
        }
    }
}
