/*
 * WaypointRouteNavigatorModel.java
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
package com.locify.client.locator.impl;

import com.locify.client.data.items.Route;
import com.locify.client.locator.Location4D;
import com.locify.client.locator.Navigator;
import com.locify.client.utils.R;
import java.util.Vector;

/**
 * Navigator along the route using waypoints
 * @author Menion
 */
public class WaypointRouteNavigatorModel implements Navigator {

    /* vector containing Location4D points of route */
    private Vector route;
    /* vector containing names of waypoints */
    private Vector routeNames;
    /* array containing distance between waypoints */
    private double[] routeDist;
    /* total length of track */
    private double routeLength = 0;
    private int actualWaypoint = -1;
    private int lastWaypoint = -1;
    private double minDist = Double.POSITIVE_INFINITY;
    private double azimToTarget = 0.0;
    private double distToTarget = 0.0;
    private long lastActualizate;
    /* refresh time for actualization */
    private long refreshTime = 1000;

    public WaypointRouteNavigatorModel(Route route) {
        basicIni();
        this.route = route.getPoints();

        for (int i = 0; i < route.getWaypointCount(); i++) {
            routeNames.addElement("Waypoint "+i);
        }

        computeRouteDist();
    }

    private void basicIni() {
        lastActualizate = System.currentTimeMillis();
        route = new Vector();
        routeNames = new Vector();
    }

    private void computeRouteDist() {
        if (route.size() > 0) {
            actualWaypoint = 0;
            lastWaypoint = route.size() - 1;
            if (lastWaypoint > 0) {
                routeDist = new double[lastWaypoint];
                for (int i = 0; i < lastWaypoint; i++) {
                    routeDist[i] = ((Location4D) route.elementAt(i)).distanceTo((Location4D) route.elementAt(i + 1));
                    routeLength += routeDist[i];
                }
            } else {
                routeDist = null;
                routeLength = 0;
            }
        }
    }

    public double getAzimuthToTaget(Location4D actualPos) {
        actualizeData(actualPos);
        //System.out.println("\nazimuth: " + azimToTarget);
        //System.out.println("\nback: " + getRouteLenghtBack(actualPos));
        //System.out.println("\nforw: " + getRoudeLengthForward(actualPos));
        return azimToTarget;
    }

    public double getDistanceToTarget(Location4D actualPos) {
        actualizeData(actualPos);
        //System.out.println("\ndistance: " + distToTarget);
        return distToTarget;
    }

    public String getToName() {
        if (actualWaypoint != -1) {
            return (String) routeNames.elementAt(actualWaypoint);
        } else {
            return "No waypoint";
        }
    }

    public String getMessage() {
        return "";
    }

    private void actualizeData(Location4D actualPos) {
        try {
            if (lastWaypoint != -1 && (System.currentTimeMillis() - lastActualizate) > refreshTime) {
                double distToActual = actualPos.distanceTo((Location4D) route.elementAt(actualWaypoint));
                if (actualWaypoint < lastWaypoint && distToActual < minDist) {
                    double distToNext = actualPos.distanceTo((Location4D) route.elementAt(actualWaypoint + 1));
                    if (distToNext < routeDist[actualWaypoint]) {
                        waypointNext();
                        distToTarget = distToNext;
                    } else {
                        distToTarget = distToActual;
                    }

                    azimToTarget = actualPos.azimutTo((Location4D) route.elementAt(actualWaypoint));
                } else {
                    distToTarget = distToActual;
                    azimToTarget = actualPos.azimutTo((Location4D) route.elementAt(actualWaypoint));
                }
                lastActualizate = System.currentTimeMillis();
            }
        } catch (Exception ex) {
            R.getErrorScreen().view(ex, "WaypointRouteNavigatorModel.actualizeData", "");
        }
    }

    public boolean waypointNext() {
        if (actualWaypoint < lastWaypoint) {
            actualWaypoint++;
            return true;
        } else {
            return false;
        }
    }

    public boolean waypointPrev() {
        if (actualWaypoint > 0) {
            actualWaypoint--;
            return true;
        } else {
            return false;
        }
    }

    public double getRouteLengthTotal() {
        return routeLength;
    }

    public double getRouteLenghtBack(Location4D actualPos) {
        if (actualWaypoint > 0) {
            double dist = actualPos.distanceTo((Location4D) route.elementAt(actualWaypoint - 1));
            for (int i = 0; i < actualWaypoint - 1; i++) {
                dist += routeDist[i];
            }
            return dist;
        } else {
            return 0.0;
        }
    }

    public double getRoudeLengthForward(Location4D actualPos) {
        double dist = actualPos.distanceTo((Location4D) route.elementAt(actualWaypoint));

        if (actualWaypoint > 0) {
            for (int i = actualWaypoint; i < lastWaypoint; i++) {
                dist += routeDist[i];
            }
        } else {
            dist += routeLength;
        }

        return dist;
    }
}
