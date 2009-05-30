/*
 * DottedLineMapItem.java
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
package com.locify.client.maps.mapItem;

import com.locify.client.data.items.Waypoint;
import com.locify.client.locator.Location4D;
import com.locify.client.maps.geometry.Point2D;
import com.locify.client.utils.ColorsFonts;
import com.locify.client.utils.Logger;
import com.locify.client.utils.R;
import com.sun.lwuit.Graphics;
import java.util.Vector;

/**
 * Dotted line from current location to navigation point
 * @author MenKat
 */
public class MapNavigationItem extends MapItem {

    /** default waypoints data */
    private Vector waypoints;
    /** points for painting */
    private Point2D.Int[] items;

    /** space and dist of Half line in px */
//    private int partSize = 10;
    public MapNavigationItem(Waypoint point1, Waypoint point2) {
        super();
        Vector data = new Vector();
        data.addElement(point1);
        data.addElement(point2);
        this.waypoints = data;
        initialize();
    }

    public Waypoint getTargetWaypoint() {
        if (waypoints.elementAt(1) == null) {
            return null;        
        } else {
            return (Waypoint) waypoints.elementAt(1);
        }
    }

    public void setTargetWaypoint(Waypoint waypoint) {
        waypoints.setElementAt(waypoint, 1);
        initialize();
    }

    public void initialize() {
        initializeFirstly(waypoints);
        items = initializePoints(waypoints);
    }

    public void panItem(int moveX, int moveY) {
        panItems(items, moveX, moveY);
    }

    public void drawItem(Graphics g) {
        try {
            if (enabled) {
                if (!initialized) {
                    initialize();
                }

                if (isInside() && actualState == STATE_WAITING && items != null) {
                    actualState = STATE_DRAWING;

                    g.setColor(ColorsFonts.RED);
                    if (items[0] != null && items[1] != null) {
                        g.drawLine(items[0].x, items[0].y, items[1].x, items[1].y);
                    }
                }
                actualState = STATE_WAITING;
            }
        } catch (Exception e) {
            R.getErrorScreen().view(e, "MapNavigationItem.drawItem()", null);
        }
    }

    public void actualizeActualPosition(Location4D location) {
        waypoints.removeElementAt(0);
        waypoints.insertElementAt(new Waypoint(location.getLatitude(), location.getLongitude(), "", "", null), 0);
        initialize();
    }

    public boolean touchInside(int x, int y) {
        return false;
    }

    public void getWaypointsAtPositionByPoint(Vector data, int x, int y, int radiusSquare) {
        return;
    }

    public void getWaypointsAtPositionByIcon(Vector data, int x, int y) {
        return;
    }
}
