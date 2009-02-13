/*
 * PointMapItem.java
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
import com.locify.client.maps.geometry.Point2D;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 * One point on map
 * @author MenKat
 */
public class PointMapItem extends MapItem {
    
    /** default waypoints data */
    private Vector waypoints;
    /** points for painting */
    private Point2D.Int[] items;

    public PointMapItem(Vector waypoints) {
        super();
        this.waypoints = waypoints;
        initialize();
    }

    public void initialize() {
        items = initializePoints(waypoints);
    }

    public void panItem(int moveX, int moveY) {
        panItems(items, moveX, moveY);
    }

    public void drawItem(Graphics g) {
        if (enabled) {
            if (!initialized)
                initialize();
            if (isInside() && actualState == STATE_WAITING && items != null) {
                actualState = STATE_DRAWING;
                for (int i = 0; i < items.length; i++) {
                    g.drawImage(displayedPointIcon,
                            items[i].x, items[i].y,
                            Graphics.BOTTOM | Graphics.LEFT);
                }
            }
            actualState = STATE_WAITING;
        }
    }

    public void getWaypointsAtPosition(Vector data, int x, int y, int radius) {
//System.out.println("x: " + x + " y: " + y + " x: " + items[0].x + " y: " + items[0].y);
        for (int i = 0; i < items.length; i++) {
            Point2D.Int item = items[i];
            if (Math.sqrt((item.x - x) * (item.x - x) + (item.y - y) * (item.y - y)) <= radius)
                data.addElement((Waypoint) waypoints.elementAt(i));
        }
    }

    public boolean touchInside(int x, int y) {
        return false;
    }
}
